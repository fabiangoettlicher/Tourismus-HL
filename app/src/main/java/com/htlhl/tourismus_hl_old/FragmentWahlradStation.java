package com.htlhl.tourismus_hl_old;


import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

public class FragmentWahlradStation extends ListFragment implements AdapterView.OnItemClickListener {
    static String tvStationLogged;
    static int pos, posLogStation;

    int idTvStation;
    private TextView tvStation;
    private String stringStationname;
    private FragmentManager fm;
    private String [] LatArray, LngArray, stationsArray;
    private MarkerColor mC;
    private WahlradCustomRow adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wahlrad, container, false);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        stringStationname = tvStation.getText().toString();
        for(int i = 0; i < adapter.getCount(); i++) {
            if(adapter.getItem(i).equals(stringStationname)){ //vergleich des eingeloggten namen
                posLogStation = i;
                getListView().smoothScrollToPosition(i);
                FragmentWahlradRouten.posLogRoute = -1;
                WahlradCustomRow.selectedRow = -1; //rücksetzen des items das davor angeklickt wurde
                adapter.notifyDataSetChanged(); //Änderungen übernehmen
                break;
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fm = getFragmentManager();
        mC = new MarkerColor(); //Erstelle Objekt der Classe markerColor um auf non-static methoden zugreifen zu können

        LinearLayout layoutRoutenwahl = (LinearLayout) getActivity().findViewById(R.id.Top_Layout_KG);
        layoutRoutenwahl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stringStationname = tvStation.getText().toString();
                for(int i = 0; i < adapter.getCount(); i++) {
                    if(adapter.getItem(i).equals(stringStationname)){ //vergleich des eingeloggten namen
                        posLogStation = i;
                        getListView().smoothScrollToPosition(i);
                        FragmentWahlradRouten.posLogRoute = -1;
                        WahlradCustomRow.selectedRow = -1; //rücksetzen des items das davor angeklickt wurde
                        adapter.notifyDataSetChanged(); //Änderungen übernehmen
                        break;
                    }
                }

                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.setCustomAnimations(R.animator.wahlrad_fragment_in,
                        R.animator.wahlrad_fragment_out);
                if (fm.findFragmentById(R.id.wahlrad_station).isHidden()) {
                    ft.show(fm.findFragmentById(R.id.wahlrad_station));
                    ft.show(fm.findFragmentById(R.id.wahlrad_station_grey_overlay));
                }
                ft.commit();
            }
        });

        //Deklaration der Buttons und des Textfeldes
        tvStation = (TextView) getActivity().findViewById(R.id.tvStationKG);
        TextView tvFragment = (TextView) getActivity().findViewById(R.id.tvWahlradFragment);
        tvFragment.setTextColor(getResources().getColor(R.color.KG_green));
        RelativeLayout relativeLayout = (RelativeLayout) getActivity().findViewById(R.id.wahlradTopLayout);
        tvFragment.setText(getActivity().getResources().getString(R.string.naechste_station));
        relativeLayout.setBackgroundColor(getResources().getColor(R.color.KG_orange));

        //ID der Buttons und des Textfeldes abspeichern
        idTvStation = tvStation.getId();

        //Beim erstellen der Activity wird die derzeit eingeloggte Station gespeichert
        tvStationLogged = tvStation.getText().toString();

        List<String> stationNamenList = KellerkatzeHaupt.stationNamen;
        stationsArray = new String[KellerkatzeHaupt.stationNamen.size()];
        for(int i=0; i<stationNamenList.size(); i++){
            stationsArray[i] = i+1+". " + stationNamenList.get(i);
        }
        adapter = new WahlradCustomRow(getActivity(), stationsArray, R.drawable.ic_done_green_24dp);
        getListView().setAdapter(adapter);
        getListView().setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        WahlradCustomRow.selectedRow = position; //jetzige Position an Adaper übergeben
        //Überschreiben der anderen positionen
        posLogStation = position;
        FragmentWahlradRouten.posLogRoute = position;
        adapter.notifyDataSetChanged(); //Änderungen übernehmen
        boolean flagNextStationLocal = KellerkatzeStation.flagNextStation;
        if(flagNextStationLocal){ //wenn auf NextStation gedrückt wurde
            tvStationLogged = tvStation.getText().toString(); //hol derzeit angezeigte Station
            flagNextStationLocal = false;
        }
        String data = (String) parent.getItemAtPosition(position);
        tvStation.setText(data);            //Wahlfeld auf angeklickten Text seten
        pos = position;
        mC.ChangeMarkerColor(KellerkatzeHaupt.stationLATs, KellerkatzeHaupt.stationLNGs, KellerkatzeHaupt.stationNamen);
    }

    public static int getPos()
    {
        return pos;
    }
}
