package com.htlhl.tourismus_hl;

import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.htlhl.tourismus_hl.Data.Local.ReadDataFromFile;
import com.htlhl.tourismus_hl.Data.Model.PointOfInterest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.htlhl.tourismus_hl.KellerkatzeHaupt.stationNamen;
import static com.htlhl.tourismus_hl.KellerkatzeHaupt.stringEtStationNr;
import static com.htlhl.tourismus_hl.KellerkatzeStation.correct;

public class FragmentStationcode extends android.app.Fragment implements View.OnClickListener{

    static SeekBar seekBar;

    private android.app.FragmentManager fm;
    private List<String> stationNumbers;
    private List<PointOfInterest> PointOfInterestList;
    private List<Integer> stationIDs;
    private final Handler handler = new Handler();
    private EditText etStationNr;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fragment_stationcode, container, false);
        return view;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        android.app.Fragment fragment = fm.findFragmentById(R.id.CodeFragment);
        ft.hide(fragment);
        ft.commit();
        Button btnCancle = (Button) getActivity().findViewById(R.id.btnCancleCode);
        Button btnOk = (Button) getActivity().findViewById(R.id.btnOKCode);
        btnCancle.setOnClickListener(this);
        btnOk.setOnClickListener(this);
        seekBar = (SeekBar) getActivity().findViewById(R.id.SeekBar01);

        stationNumbers = new ArrayList<>();
        stationIDs = new ArrayList<>();
        PointOfInterestList = ReadDataFromFile.getDbPoiXmlContainerList(getActivity());
        if (PointOfInterestList == null) {
            PointOfInterestList = ReadDataFromFile.getDbPoiXmlContainerListText(getActivity());
        }
        getData();

        etStationNr = (EditText) getActivity().findViewById(R.id.etStationNr);
        etStationNr.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int Id, KeyEvent keyEvent) {
                if (Id == EditorInfo.IME_ACTION_DONE) {
                    //check if 5 digits
                    if (etStationNr.getText().toString().trim().length() < 5) {
                        etStationNr.setError(getResources().getString(R.string.errorShortStationNr));
                    } else {
                        stringEtStationNr = etStationNr.getText().toString();
                        correct = correctNumber(stringEtStationNr);
                        if (correct) {
                            String loggedStation;
                            if(KellerkatzeStation.markerClick){
                                loggedStation = KellerkatzeStation.markerLoggedStation;
                            } else {
                                loggedStation = KellerkatzeHaupt.tvStation.getText().toString();
                                String[] splitLoggedStation = loggedStation.split(" ");
                                loggedStation = "";
                                for (int i = 0; i < splitLoggedStation.length; i++) {
                                    if (i == 1) {
                                        loggedStation = loggedStation.concat(
                                                splitLoggedStation[i]);
                                    } else if (i > 1) {
                                        loggedStation = loggedStation.concat(
                                                " " + splitLoggedStation[i]);
                                    }
                                }
                            }
                            for (int x = 0; x < stationNamen.size(); x++) {
                                if (stationNamen.get(x).equals(loggedStation)) {
                                    //check if number from right station
                                    if (!etStationNr.getText().toString().equals(
                                            stationNumbers.get(x))) {
                                        etStationNr.setError(getResources().getString(
                                                R.string.errorETWrongStationNumber));
                                    } else {
                                        //if code is right logg code
                                        etStationNr.setText(stringEtStationNr);
                                        InputMethodManager imm = (InputMethodManager)
                                                getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                                        imm.hideSoftInputFromWindow(
                                                etStationNr.getWindowToken(), 0);
                                    }
                                }
                            }
                        } else {
                            //code is not a code of one of 24 stations
                            etStationNr.setError(getResources().getString(R.string.errorFalseStationNr));
                        }
                    }
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onClick(View view) {
        fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        android.app.Fragment fragment;
        switch (view.getId()){
            case R.id.btnCancleCode:
                fragment = fm.findFragmentById(R.id.CodeFragment);
                ft.hide(fragment);
                ft.commit();
                getActivity().findViewById(R.id.tvHeaderStation).setVisibility(View.VISIBLE);
                getActivity().findViewById(R.id.tvTextStation).setVisibility(View.VISIBLE);
                getActivity().findViewById(R.id.btnNextStation).setVisibility(View.VISIBLE);
                getActivity().findViewById(R.id.langAudio_fab).setVisibility(View.VISIBLE);
                getActivity().findViewById(R.id.tvAudioPlay).setVisibility(View.VISIBLE);
                getActivity().findViewById(R.id.ImgBtnPlay).setVisibility(View.VISIBLE);
                getActivity().findViewById(R.id.tvLangAudio).setVisibility(View.VISIBLE);
                break;
            case R.id.btnOKCode:
                if(correct) {
                    fragment = fm.findFragmentById(R.id.CodeFragment);
                    ft.hide(fragment);
                    ft.commit();
                    getActivity().findViewById(R.id.tvHeaderStation).setVisibility(View.VISIBLE);
                    getActivity().findViewById(R.id.tvTextStation).setVisibility(View.VISIBLE);
                    etStationNr.setText("");

                    if (ReadDataFromFile.getDbPoiXmlContainerList(getActivity()) != null) {
                        ft = getFragmentManager().beginTransaction();
                        ft.setCustomAnimations(android.R.animator.fade_in,
                                android.R.animator.fade_out);
                        android.app.Fragment fragment2 = fm.findFragmentById(R.id.AudioplayerFragment);
                        if (fragment2.isHidden()) {
                            ft.show(fragment2);
                        }
                        ft.commit();
                        FragmentAudioplayer.fragmentShown = true;
                        FragmentAudioplayer.mediaPlayer.start();
                        FragmentAudioplayer.btnPlayStop.setImageResource(R.drawable.ic_pause_circle_outline_white_36dp);
                        FragmentAudioplayer.btnPlayStop.setTag(true);
                        startPlayProgressUpdater();
                    } else {
                        Toast.makeText(getActivity(), getString(R.string.toastDownloadIncompleteMp3), Toast.LENGTH_SHORT).show();
                    }
                    break;
                }
        }
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

    public void getData(){
        for (int i = 0; i < PointOfInterestList.size(); i++) {
            if (PointOfInterestList.get(i).getPoiKatID_() == 9) { //KatID 9 = Station
                stationIDs.add(PointOfInterestList.get(i).getPoiID_());
            }
        }
        Collections.sort(stationIDs);
        for (int x = 0; x < stationIDs.size(); x++) {
            for (int y = 0; y < PointOfInterestList.size(); y++) {
                if (stationIDs.get(x) == PointOfInterestList.get(y).getPoiID_()) {
                    stationNumbers.add(PointOfInterestList.get(y).getPoiStat_());
                }
            }
        }
    }

    public boolean correctNumber(String stringeEtStationNr) {
        boolean correct = false;
        for (int i = 0; i < stationNumbers.size(); i++) {
            if (stringeEtStationNr.equals(stationNumbers.get(i))) {
                correct = true;
            }
        }
        return correct;
    }
}