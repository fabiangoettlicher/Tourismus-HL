package com.htlhl.tourismus_hl;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class FragmentWahlradOverlay extends Fragment{
    FragmentManager fm;
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wahlrad_overlay, container, false);
        return view;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fm = getFragmentManager();
        if(RoutenPoiList.listViewShown || FragmentRoutentyp.flagLauf) {
            //Beim start der Activity wird das Fragment nicht angezeigt (nur bei wiederaufruf)
            FragmentTransaction ft = fm.beginTransaction();
            Fragment fragment = fm.findFragmentById(R.id.wahlrad_routen_grey_overlay);
            ft.hide(fragment);
            ft.commit();
        }
    }
}