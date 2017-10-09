package com.htlhl.tourismus_hl;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.BoolRes;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.InputFilter;
import android.text.InputType;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.htlhl.tourismus_hl.KellerkatzeHaupt.stringEtStationNr;


public class KellerkatzeStation extends AppCompatActivity {
    public static String stringETStationNr, markerLoggedStation;
    static TextView tvAudioPlay, tvHeader;
    static ImageButton ImgBtnPlay;
    static Button btnNextStation;
    static SeekBar seekBar;
    static boolean flagNextStation = false, flagLastStation = false, correct, markerClick;
    private final Handler handler = new Handler();
    private String Header, stringNextStation, stringTextStation, loggedStation;
    private FragmentManager fm;
    private TextView tvTextStation;
    private ImageView Stationsbild;
    private List<DbPoiXmlContainer> dbPoiXmlContainerList;
    private List<String> stationNumbers, stationLats, stationLngs, stationNamen;
    private List<Integer> stationIDs;
    private FloatingActionMenu fabMenu;
    private FloatingActionButton fabItem1, fabItem2;

    @Override
    public void onPause() {
        super.onPause();
        if (FragmentAudioplayer.mediaPlayer != null)
            FragmentAudioplayer.mediaPlayer.pause();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment audioFragment = fm.findFragmentById(R.id.AudioplayerFragment);
        ft.hide(audioFragment);
        ft.commit();
        tvAudioPlay.setVisibility(View.VISIBLE);
        btnNextStation.setVisibility(View.VISIBLE);
        ImgBtnPlay.setVisibility(View.VISIBLE);
        findViewById(R.id.tvLangAudio).setVisibility(View.VISIBLE);
        fabMenu.setVisibility(View.VISIBLE);
        correct=false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences language = getSharedPreferences("pref", 0);
        // tvRouten.setText(screen.getString("RoutenName", ""));
        if (language.getString("ActiveLang", "").equals("ger")) {
            fabMenu.getMenuIconView().setImageResource(R.drawable.aut_flag_round);
            language.edit().putString("AudioLang", "ger").apply();
        } else if (language.getString("ActiveLang", "").equals("bri")) {
            fabMenu.getMenuIconView().setImageResource(R.drawable.bri_flag_round);
            language.edit().putString("AudioLang", "bri").apply();
        } else if (language.getString("ActiveLang", "").equals("cze")) {
            fabMenu.getMenuIconView().setImageResource(R.drawable.cze_flag_round);
            language.edit().putString("AudioLang", "cze").apply();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kellerkatze_station);
        createToolbar(R.id.toolbar_station, R.color.KG_green);

        seekBar = (SeekBar) findViewById(R.id.SeekBar01);
        fabMenu = (FloatingActionMenu) findViewById(R.id.langAudio_fab);
        fabItem1 = (FloatingActionButton) findViewById(R.id.langAudio_item_1);
        fabItem2 = (FloatingActionButton) findViewById(R.id.langAudio_item_2);
        dbPoiXmlContainerList = ReadDataFromFile.getDbPoiXmlContainerList(this);
        if (dbPoiXmlContainerList == null) {
            dbPoiXmlContainerList = ReadDataFromFile.getDbPoiXmlContainerListText(this);
        }
        stationIDs = new ArrayList<>();
        stationNumbers = new ArrayList<>();
        stationLats = new ArrayList<>();
        stationLngs = new ArrayList<>();
        stationNamen = new ArrayList<>();
        getData();

        flagNextStation = false;
        Stationsbild = (ImageView) findViewById(R.id.ImgViewStation);
        btnNextStation = (Button) findViewById(R.id.btnNextStation);
        tvHeader = (TextView) findViewById(R.id.tvHeaderStation);
        tvTextStation = (TextView) findViewById(R.id.tvTextStation);
        ImgBtnPlay = (ImageButton) findViewById(R.id.ImgBtnPlay);
        tvAudioPlay = (TextView) findViewById(R.id.tvAudioPlay);

        chooceLayout();

        tvHeader.setText(Header);
        tvTextStation.setText(stringTextStation);

        fm = getFragmentManager();
        Fragment codeFragment = fm.findFragmentById(R.id.CodeFragment);
        Fragment audioplayer = fm.findFragmentById(R.id.AudioplayerFragment);
        addShowhideListener(R.id.ImgBtnPlay, codeFragment, audioplayer);

        btnNextStation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!flagLastStation) {
                    KellerkatzeHaupt.tvStation.setText(stringNextStation);
                    MarkerColor mC = new MarkerColor();
                    mC.ChangeMarkerColor(stationLats, stationLngs, stationNamen);
                    flagNextStation = true;
                    finish();
                } else {
                    flagLastStation = false;
                    Intent intent = new Intent(KellerkatzeStation.this, MainActivity.class);
                    startActivity(intent);
                }
            }
        });

        RelativeLayout rlMain = (RelativeLayout) findViewById(R.id.rlStation);
        rlMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fabMenu.isOpened()) {
                    fabMenu.close(true);
                }
            }
        });

        fabMenu.setOnMenuButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fabMenu.isOpened()) {
                    fabMenu.close(true);
                } else {
                    SharedPreferences language = getSharedPreferences("pref", 0);
                    if (language.getString("AudioLang", "").equals("ger")) {
                        fabItem1.setImageResource(R.drawable.bri_flag_mini);
                        fabItem1.setTag("bri");
                        fabItem2.setImageResource(R.drawable.cze_flag_mini);
                        fabItem2.setTag("cze");
                    } else if (language.getString("AudioLang", "").equals("bri")) {
                        fabItem1.setImageResource(R.drawable.cze_flag_mini);
                        fabItem1.setTag("cze");
                        fabItem2.setImageResource(R.drawable.aut_flag_mini);
                        fabItem2.setTag("ger");
                    } else if (language.getString("AudioLang", "").equals("cze")) {
                        fabItem1.setImageResource(R.drawable.aut_flag_mini);
                        fabItem1.setTag("ger");
                        fabItem2.setImageResource(R.drawable.bri_flag_mini);
                        fabItem2.setTag("bri");
                    }
                    fabMenu.open(true);
                }
            }
        });
    }

    public void chooceLayout() {
        SharedPreferences language = getSharedPreferences("pref", 0);
        if(markerClick){
            loggedStation = markerLoggedStation;
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
        for (int i = 0; i < dbPoiXmlContainerList.size(); i++) {
            if (dbPoiXmlContainerList.get(i).getPoiName_().equals(loggedStation)) {
                Header = dbPoiXmlContainerList.get(i).getPoiName_();
                String stationcode = dbPoiXmlContainerList.get(i).getPoiStat_();
                if (language.getString("ActiveLang", "").equals("ger")) {
                    stringTextStation = dbPoiXmlContainerList.get(i).getPoiTextDE_();
                } else if (language.getString("ActiveLang", "").equals("bri")) {
                    stringTextStation = dbPoiXmlContainerList.get(i).getPoiTextEN_();
                } else if (language.getString("ActiveLang", "").equals("cze")) {
                    stringTextStation = dbPoiXmlContainerList.get(i).getPoiTextCZ_();
                }
                if (ReadDataFromFile.getDbPoiXmlContainerList(this) == null) {
                    for (int x = 0; x < dbPoiXmlContainerList.size(); x++) {
                        if (dbPoiXmlContainerList.get(x).getPoiStat_().equals(stringETStationNr)) {
                            String url = dbPoiXmlContainerList.get(x).getPoiBild_();
                            GetBitmapOnline task = new GetBitmapOnline(url, new GetBitmapOnline.AsyncResponse() {
                                @Override
                                public void processFinish(Bitmap bitmap) {
                                    Stationsbild.setImageBitmap(bitmap);
                                }
                            });
                            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        }
                    }
                } else {
                    Stationsbild.setImageBitmap(ReadDataFromFile.getBitmap(dbPoiXmlContainerList.get(i).getPathBild_()));
                }
                for (int y = 0; y < stationNumbers.size(); y++) {
                    if (stationNumbers.get(y).equals(stationcode)) {
                        if (y == stationNumbers.size() - 1) {
                            flagLastStation = true;
                        } else {
                            if (!stationLats.get(y + 1).equals("") && !stationLngs.get(y + 1).equals("")) {
                                KellerkatzeHaupt.mapLAT = stationLats.get(y + 1);
                                KellerkatzeHaupt.mapLNG = stationLngs.get(y + 1);
                            }
                            stringNextStation = y + 2 + ". " + stationNamen.get(y + 1);
                        }
                    }
                }
            }
        }
    }

    void addShowhideListener(int buttonId, final Fragment code, final Fragment audioplayer) {
        final ImageButton button = (ImageButton) findViewById(buttonId);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                fm = getFragmentManager();
                btnNextStation.setVisibility(View.INVISIBLE);
                fabMenu.setVisibility(View.INVISIBLE);
                tvAudioPlay.setVisibility(View.INVISIBLE);
                ImgBtnPlay.setVisibility(View.INVISIBLE);
                findViewById(R.id.tvLangAudio).setVisibility(View.INVISIBLE);
                if(!correct) {
                    tvHeader.setVisibility(View.INVISIBLE);
                    tvTextStation.setVisibility(View.INVISIBLE);
                    fm.beginTransaction().show(code).commit();
                } else {
                    if (ReadDataFromFile.getDbPoiXmlContainerList(KellerkatzeStation.this) != null) {
                        FragmentTransaction ft = fm.beginTransaction();
                        ft.setCustomAnimations(android.R.animator.fade_in,
                                android.R.animator.fade_out);
                        if (audioplayer.isHidden()) {
                            ft.show(audioplayer).commit();
                        }

                        FragmentAudioplayer.fragmentShown = true;
                        FragmentAudioplayer.mediaPlayer.start();
                        FragmentAudioplayer.btnPlayStop.setImageResource(R.drawable.ic_pause_circle_outline_white_36dp);
                        FragmentAudioplayer.btnPlayStop.setTag(true);
                        startPlayProgressUpdater();
                    } else {
                        Toast.makeText(KellerkatzeStation.this, getString(R.string.toastDownloadIncompleteMp3), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    public void startPlayProgressUpdater() {
        seekBar.setProgress(FragmentAudioplayer.mediaPlayer.getCurrentPosition());

        if (FragmentAudioplayer.mediaPlayer.isPlaying()) {
            Runnable notification = new Runnable() {
                public void run() {
                    startPlayProgressUpdater();
                }
            };
            handler.postDelayed(notification, 500);
        } else {
            FragmentAudioplayer.mediaPlayer.pause();
            FragmentAudioplayer.btnPlayStop.setImageResource(R.drawable.ic_play_circle_outline_white_36dp);
            FragmentAudioplayer.btnPlayStop.setTag(false);
            seekBar.setProgress(0);
        }
    }

    private void getData() {
        for (int i = 0; i < dbPoiXmlContainerList.size(); i++) {
            if (dbPoiXmlContainerList.get(i).getPoiKatID_() == 9) { //KatID 9 = Station
                stationIDs.add(dbPoiXmlContainerList.get(i).getPoiID_());
            }
        }
        Collections.sort(stationIDs);
        for (int x = 0; x < stationIDs.size(); x++) {
            for (int y = 0; y < dbPoiXmlContainerList.size(); y++) {
                if (stationIDs.get(x) == dbPoiXmlContainerList.get(y).getPoiID_()) {
                    stationNumbers.add(dbPoiXmlContainerList.get(y).getPoiStat_());
                    stationLngs.add(dbPoiXmlContainerList.get(y).getPoiLng_());
                    stationLats.add(dbPoiXmlContainerList.get(y).getPoiLat_());
                    stationNamen.add(dbPoiXmlContainerList.get(y).getPoiName_());
                }
            }
        }
    }

    public void createToolbar(int toolbarID, int color) {
        //Toolbar with Backbutton
        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(toolbarID);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //Set Homebutton
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //display Homebutton as Arrow (Back)
        getSupportActionBar().setDisplayShowTitleEnabled(false); //do not show Title
        // Change Color of the toolbar Navigationbuttons (Backbutton)
        final Drawable upArrow = toolbar.getNavigationIcon();
        upArrow.setColorFilter(getResources().getColor(color), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    // This method will be called when a MessageEvent is posted
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        if (event.message.equals("reload")) {
            this.recreate();
        }
    }
}