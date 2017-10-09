package com.htlhl.tourismus_hl;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.htlhl.tourismus_hl.Data.Local.ReadDataFromFile;
import com.htlhl.tourismus_hl.Data.Model.PointOfInterest;

import java.util.List;

public class FragmentAudioplayer extends Fragment {
    public static MediaPlayer mediaPlayer;
    public static boolean fragmentShown;
    public static ImageButton btnPlayStop;

    private FragmentManager fm;
    private SeekBar seekBar;
    private ImageButton btnFastForward, btnFastRewind, ImgBtnGer, ImgBtnCze, ImgBtnBri;
    private Boolean isForwardBtnLongPressed = false, isRewindBtnLongPressed = false;
    private String uriGer, uriCze, uriBri;
    private Uri uri;
    private List<PointOfInterest> PointOfInterestList;
    private FloatingActionButton fabItem1, fabItem2;
    private FloatingActionMenu fabMenu;


    private final Handler handler = new Handler();

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_audioplayer, container, false);
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //beim verlassen der seite musik abdrehen
        if(mediaPlayer!=null)
            mediaPlayer.pause();
    }

    @Override
    public void onResume() {
        super.onResume();
        //fragment nicht sichtbar aber offen -> deswegen schließen
        closeFragment();
        btnPlayStop.setEnabled(true);
        btnFastForward.setEnabled(true);
        btnFastRewind.setEnabled(true);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        PointOfInterestList = ReadDataFromFile.getDbPoiXmlContainerList(getActivity().getApplicationContext());
        if(PointOfInterestList ==null){
            PointOfInterestList = ReadDataFromFile.getDbPoiXmlContainerListText(getActivity().getApplicationContext());
        }
        getURLs();
        fm = getFragmentManager();
        fabItem1 = (FloatingActionButton) getActivity().findViewById(R.id.langAudio_item_1);
        fabItem2 = (FloatingActionButton) getActivity().findViewById(R.id.langAudio_item_2);
        fabMenu = (FloatingActionMenu) getActivity().findViewById(R.id.langAudio_fab);
        btnFastForward = (ImageButton) getActivity().findViewById(R.id.ImgBtnForwardFragment);
        btnFastRewind = (ImageButton) getActivity().findViewById(R.id.ImgBtnRewindFragment);
        ImageButton btnCloseFragment = (ImageButton) getActivity().findViewById(R.id.ImgBtnCloseFragment);
        //Festlegen des Play/Pause buttons (Bild+zustand (true if play / false if pause) )
        btnPlayStop = (ImageButton) getActivity().findViewById(R.id.ImgBtnPlayPauseFragment);
        btnPlayStop.setImageResource(R.drawable.ic_play_circle_outline_white_36dp);
        btnPlayStop.setTag(false);  //false if pause
        SharedPreferences langPref = getActivity().getSharedPreferences("pref",0);
        if (langPref.getString("ActiveLang", "").equals("ger")) {
            if(uriGer!=null && !uriGer.equals("")) {
                uri = Uri.parse(uriGer);
                mediaPlayer = MediaPlayer.create(getActivity(), uri);
                initViews();
            } else {
                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.toastNoMp3), Toast.LENGTH_SHORT).show();
            }
        } else if (langPref.getString("ActiveLang", "").equals("bri")) {
            if(uriBri!=null && !uriBri.equals("")) {
                uri = Uri.parse(uriBri);
                mediaPlayer = MediaPlayer.create(getActivity(), uri);
                initViews();
            } else {
                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.toastNoMp3), Toast.LENGTH_SHORT).show();
            }
        } else if (langPref.getString("ActiveLang", "").equals("cze")) {
            if(uriCze!=null && !uriCze.equals("")) {
                uri = Uri.parse(uriCze);
                mediaPlayer = MediaPlayer.create(getActivity(), uri);
                initViews();
            } else {
                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.toastNoMp3), Toast.LENGTH_SHORT).show();
            }
        }


        fabItem1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fabMenu.close(true);
                SharedPreferences language = getActivity().getSharedPreferences("pref", 0);
                if(fabItem1.getTag().equals("ger")){
                    fabMenu.getMenuIconView().setImageResource(R.drawable.aut_flag_round);
                    language.edit().putString("AudioLang", "ger").apply();
                    if (!fragmentShown) {
                        if (uriGer != null && !uriGer.equals("")) {
                            uri = Uri.parse(uriGer);
                            mediaPlayer = MediaPlayer.create(getActivity(), uri);
                            initViews();
                        } else {
                            Toast.makeText(getActivity().getApplicationContext(), getString(R.string.toastNoMp3), Toast.LENGTH_SHORT).show();
                        }
                    }
                } else if(fabItem1.getTag().equals("bri")){
                    fabMenu.getMenuIconView().setImageResource(R.drawable.bri_flag_round);
                    language.edit().putString("AudioLang", "bri").apply();
                    if (!fragmentShown) {
                        if(uriBri!=null && !uriBri.equals("")) {
                            uri = Uri.parse(uriBri);
                            mediaPlayer = MediaPlayer.create(getActivity(), uri);
                            initViews();
                        } else {
                            Toast.makeText(getActivity().getApplicationContext(), getString(R.string.toastNoMp3), Toast.LENGTH_SHORT).show();
                        }
                    }
                } else if(fabItem1.getTag().equals("cze")){
                    fabMenu.getMenuIconView().setImageResource(R.drawable.cze_flag_round);
                    language.edit().putString("AudioLang", "cze").apply();
                    if (!fragmentShown) {
                        if(uriCze!=null && !uriCze.equals("")) {
                            uri = Uri.parse(uriCze);
                            mediaPlayer = MediaPlayer.create(getActivity(), uri);
                            initViews();
                        } else {
                            Toast.makeText(getActivity().getApplicationContext(), getString(R.string.toastNoMp3), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });
        fabItem2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fabMenu.close(true);
                SharedPreferences language = getActivity().getSharedPreferences("pref", 0);
                if(fabItem2.getTag().equals("ger")){
                    fabMenu.getMenuIconView().setImageResource(R.drawable.aut_flag_round);
                    language.edit().putString("AudioLang", "ger").apply();
                    if (!fragmentShown) {
                        if (uriGer != null && !uriGer.equals("")) {
                            uri = Uri.parse(uriGer);
                            mediaPlayer = MediaPlayer.create(getActivity(), uri);
                            initViews();
                        } else {
                            Toast.makeText(getActivity().getApplicationContext(), getString(R.string.toastNoMp3), Toast.LENGTH_SHORT).show();
                        }
                    }
                } else if(fabItem2.getTag().equals("bri")){
                    fabMenu.getMenuIconView().setImageResource(R.drawable.bri_flag_round);
                    language.edit().putString("AudioLang", "bri").apply();
                    if (!fragmentShown) {
                        if(uriBri!=null && !uriBri.equals("")) {
                            uri = Uri.parse(uriBri);
                            mediaPlayer = MediaPlayer.create(getActivity(), uri);
                            initViews();
                        } else {
                            Toast.makeText(getActivity().getApplicationContext(), getString(R.string.toastNoMp3), Toast.LENGTH_SHORT).show();
                        }
                    }
                } else if(fabItem2.getTag().equals("cze")){
                    fabMenu.getMenuIconView().setImageResource(R.drawable.cze_flag_round);
                    language.edit().putString("AudioLang", "cze").apply();
                    if (!fragmentShown) {
                        if(uriCze!=null && !uriCze.equals("")) {
                            uri = Uri.parse(uriCze);
                            mediaPlayer = MediaPlayer.create(getActivity(), uri);
                            initViews();
                        } else {
                            Toast.makeText(getActivity().getApplicationContext(), getString(R.string.toastNoMp3), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });

        btnCloseFragment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeFragment();
                //warten bis fragmentvollständig weg
                showLayoutAudio();
            }
        });
        btnFastForward.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (!isForwardBtnLongPressed) {
                            isForwardBtnLongPressed = true;
                            new SendForward().execute();
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        isForwardBtnLongPressed = false;
                }
                return true;
            }
        });
        btnFastRewind.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (!isRewindBtnLongPressed) {
                            isRewindBtnLongPressed = true;
                            new SendRewind().execute();
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        isRewindBtnLongPressed = false;
                }
                return true;
            }
        });

        //Beim start der Activity wird das Fragment nicht angezeigt
        FragmentTransaction ft = fm.beginTransaction();
        Fragment fragment = (Fragment) fm.findFragmentById(R.id.AudioplayerFragment);
        ft.hide(fragment);
        ft.commit();
    }

    private void initViews() {
        btnPlayStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonClick();
            }
        });
        //horzontal verlaufender Titel des Liedes
        TextView tvTitle = (TextView) getActivity().findViewById(R.id.tvTitleAudioFragment);
        tvTitle.setText(KellerkatzeStation.tvHeader.getText().toString());
        tvTitle.setSelected(true);
        tvTitle.setMovementMethod(new ScrollingMovementMethod());

        seekBar = (SeekBar) getActivity().findViewById(R.id.SeekBar01);
        seekBar.setMax(mediaPlayer.getDuration());
        seekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                seekChange(v);
                return false;
            }
        });
    }

    // This is event handler thumb moving event
    private void seekChange(View v) {
        if (mediaPlayer.isPlaying()) {
            SeekBar sb = (SeekBar) v;
            mediaPlayer.seekTo(sb.getProgress());
        }
    }

    // This is event handler for buttonClick event
    private void buttonClick() {
        if (!((Boolean) btnPlayStop.getTag())) {
            btnPlayStop.setImageResource(R.drawable.ic_pause_circle_outline_white_36dp);
            btnPlayStop.setTag(true);   //true if play
            try {
                mediaPlayer.start();
                startPlayProgressUpdater();
            } catch (IllegalStateException e) {
                mediaPlayer.pause();
            }
        } else {
            btnPlayStop.setImageResource(R.drawable.ic_play_circle_outline_white_36dp);
            btnPlayStop.setTag(false);
            mediaPlayer.pause();
            seekBar.setProgress(mediaPlayer.getCurrentPosition());
        }
    }

    public void startPlayProgressUpdater() {
        seekBar.setProgress(mediaPlayer.getCurrentPosition());
        if (mediaPlayer.isPlaying()) {
            Runnable notification = new Runnable() {
                public void run() {
                    startPlayProgressUpdater();
                }
            };
            handler.postDelayed(notification, 500);
        } else {
            mediaPlayer.pause();
            btnPlayStop.setImageResource(R.drawable.ic_play_circle_outline_white_36dp);
            btnPlayStop.setTag(false);
            seekBar.setProgress(0);
        }
    }

    void addShowhideListener(final Fragment fragment) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.setCustomAnimations(android.R.animator.fade_in,
                android.R.animator.fade_out);
        if (fragment.isVisible()) {
            ft.hide(fragment);
        }
        ft.commit();
    }

    //methoden um vor bzw zurückzuspulen
    class SendForward extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... arg0) {
            while (isForwardBtnLongPressed) {
                mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + 5);
            }
            return null;
        }
    }
    class SendRewind extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... arg0) {
            while (isRewindBtnLongPressed) {
                mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() - 5);
            }
            return null;
        }
    }

    public void closeFragment() {
        if (((Boolean) btnPlayStop.getTag())) {    //abfrage ob spielt
            mediaPlayer.pause();
            addShowhideListener(fm.findFragmentById(R.id.AudioplayerFragment));
        } else {
            addShowhideListener(fm.findFragmentById(R.id.AudioplayerFragment));
        }
        fragmentShown = false;
    }

    public void getURLs() {
        String loggedStation;
        if(KellerkatzeStation.markerClick){
            loggedStation = KellerkatzeStation.markerLoggedStation;
        } else {
            loggedStation = KellerkatzeHaupt.tvStation.getText().toString();
            String[] splitLoggedStation = loggedStation.split(" ");
            loggedStation = "";
            for (int x = 0; x < splitLoggedStation.length; x++) {
                if (x == 1) {
                    loggedStation = loggedStation.concat(splitLoggedStation[x]);
                } else if (x > 1) {
                    loggedStation = loggedStation.concat(" " + splitLoggedStation[x]);
                }
            }
        }
        for (int i = 0; i < PointOfInterestList.size(); i++) {
            if (PointOfInterestList.get(i).getPoiName_().equals(loggedStation)) {
                uriGer = PointOfInterestList.get(i).getPathMP3DE_();
                uriBri = PointOfInterestList.get(i).getPathMP3EN_();
                uriCze = PointOfInterestList.get(i).getPathMP3CZ_();
            }
        }
    }

    public void showLayoutAudio() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                KellerkatzeStation.tvAudioPlay.setVisibility(View.VISIBLE);
                KellerkatzeStation.btnNextStation.setVisibility(View.VISIBLE);
                KellerkatzeStation.ImgBtnPlay.setVisibility(View.VISIBLE);
                fabMenu.setVisibility(View.VISIBLE);
                getActivity().findViewById(R.id.tvLangAudio).setVisibility(View.VISIBLE);
            }
        }, 600);
    }
}
