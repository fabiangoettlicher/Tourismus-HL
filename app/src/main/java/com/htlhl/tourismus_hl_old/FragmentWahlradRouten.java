package com.htlhl.tourismus_hl_old;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.htlhl.tourismus_hl_old.Data.Local.ReadDataFromFile;
import com.htlhl.tourismus_hl_old.Data.Model.Routen;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class FragmentWahlradRouten extends ListFragment implements AdapterView.OnItemClickListener {

    static String tvRoutenLogged;
    static int pos, route, posLogRoute;

    private Context context;
    private String[] routenInfos;
    private TextView tvRouten;
    private String stringRoutenname, pathHoehen, pathRoutenLogo1, pathRoutenLogo2;
    private FragmentManager fm;
    private WahlradCustomRow adapter;
    private List<Routen> routenList;
    public ListView lvWahlrad;
    private List<String> radwegeNamen, wanderwegeNamen;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wahlrad, container, false);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        stringRoutenname = tvRouten.getText().toString();
        if(!FragmentRoutentyp.flagLauf) {
            for (int i = 0; i < adapter.getCount(); i++) {
                if (adapter.getItem(i).equals(stringRoutenname)) { //vergleich des eingeloggten namen
                    posLogRoute = i;
                    getListView().smoothScrollToPosition(i);
                    FragmentWahlradStation.posLogStation = -1;
                    WahlradCustomRow.selectedRow = -1; //rücksetzen des items das davor angeklickt wurde
                    adapter.notifyDataSetChanged(); //Änderungen übernehmen
                    break;
                }
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        context=getActivity();
        routenList = ReadDataFromFile.getDbRoutenXmlContainerList(getActivity());
        LinearLayout layoutRoutenwahl = (LinearLayout) getActivity().findViewById(R.id.Top_Layout_routenwahl);
        lvWahlrad = (ListView) getActivity().findViewById(android.R.id.list);
        fm = getFragmentManager();
        TextView tvFragment = (TextView) getActivity().findViewById(R.id.tvWahlradFragment);
        tvFragment.setTextColor(getResources().getColor(R.color.Routen_rot));
        tvFragment.setText(getResources().getString(R.string.naechsteroute));
        RelativeLayout relativeLayout = (RelativeLayout) getActivity().findViewById(R.id.wahlradTopLayout);
        relativeLayout.setBackgroundColor(getResources().getColor(R.color.Routen_gelb));
        if(RoutenPoiList.listViewShown || FragmentRoutentyp.flagLauf) {
            //Beim start der Activity wird das Fragment nicht angezeigt (nur bei wiederaufruf
            FragmentTransaction ft = fm.beginTransaction();
            Fragment fragment = fm.findFragmentById(R.id.wahlrad_routen);
            ft.hide(fragment);
            ft.commit();
        }

        radwegeNamen = new ArrayList<>();
        wanderwegeNamen = new ArrayList<>();

        tvRouten = (TextView) getActivity().findViewById(R.id.tvRoutenwahl);
        tvRoutenLogged = tvRouten.getText().toString();
        //Auswahl der Markerarrays

        layoutRoutenwahl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(FragmentRoutentyp.flagLauf) return;
                stringRoutenname = tvRouten.getText().toString();
                for(int i = 0; i < adapter.getCount(); i++) {
                    //find logged route
                    if(adapter.getItem(i).equals(stringRoutenname)){
                        posLogRoute = i;
                        //scroll to logged item an set selected
                        getListView().smoothScrollToPosition(i);
                        FragmentWahlradStation.posLogStation = -1;
                        WahlradCustomRow.selectedRow = -1;
                        //get updates
                        adapter.notifyDataSetChanged();
                        break;
                    }
                }
                //open "Wahlrad"
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.setCustomAnimations(R.animator.wahlrad_fragment_in,
                        R.animator.wahlrad_fragment_out);
                if (fm.findFragmentById(R.id.wahlrad_routen).isHidden()) {
                    ft.show(fm.findFragmentById(R.id.wahlrad_routen));
                    ft.show(fm.findFragmentById(R.id.wahlrad_routen_grey_overlay));
                }
                ft.commit();
            }
        });

        if(FragmentRoutentyp.flagRad){
            radwegeNamen = RoutenHaupt.radwegeNamen;
            String[] radroutenNamen = new String[RoutenHaupt.radwegeNamen.size()];
            for(int i=0; i<radwegeNamen.size(); i++){
                radroutenNamen[i] = radwegeNamen.get(i);
            }
            adapter = new WahlradCustomRow(getActivity(), radroutenNamen, R.drawable.ic_done_red_24dp);
            getListView().setAdapter(adapter);
            getListView().setOnItemClickListener(this);
        } else if(FragmentRoutentyp.flagWander){
            wanderwegeNamen = RoutenHaupt.wanderwegNamen;
            String[] wanderwegNamen = new String[RoutenHaupt.wanderwegNamen.size()];
            for(int i=0; i<wanderwegeNamen.size(); i++){
                wanderwegNamen[i] = wanderwegeNamen.get(i);
            }
            adapter = new WahlradCustomRow(getActivity(), wanderwegNamen, R.drawable.ic_done_red_24dp);
            getListView().setAdapter(adapter);
            getListView().setOnItemClickListener(this);
        }


    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        WahlradCustomRow.selectedRow = position;
        posLogRoute = position;
        FragmentWahlradStation.posLogStation = position;
        adapter.notifyDataSetChanged();
        //get text of logged item
        String data = (String) parent.getItemAtPosition(position);
        tvRouten.setText(data);
        pos = position;
        changeKmlLayer(data);
        ImageButton ImgBtnPoi = (ImageButton) getActivity().findViewById(R.id.ImgBtnPoi);
        if (RoutenHaupt.markersVisible) {
            ImgBtnPoi.setImageResource(R.drawable.btn_show_poi);
            RoutenHaupt.markersVisible = false;
        }
        RoutenHaupt rh = new RoutenHaupt();
        rh.getRoutenInfoDB(data, getActivity());
        rh.setRoutenInfo(getActivity());
    }

    public void changeKmlLayer(String routenname) {
        String pathKml = null;
        KmlCoordinates gck = new KmlCoordinates();
        InputStream in;
        if (FragmentRoutentyp.flagRad) {
            for(int i=0; i<radwegeNamen.size(); i++){
                if(radwegeNamen.get(i).equals(routenname)){
                    pathKml = RoutenHaupt.radwegeKmlPaths.get(i);
                }
            }
        } else if (FragmentRoutentyp.flagWander) {
            for(int i=0; i<wanderwegeNamen.size(); i++){
                if(wanderwegeNamen.get(i).equals(routenname)){
                    pathKml = RoutenHaupt.wanderwegeKmlPaths.get(i);
                }
            }
        }
        try {
            RoutenHaupt.mapRoutenwahl.clear();
            ReadDataFromFile.getKml(pathKml, RoutenHaupt.mapRoutenwahl, getActivity().getApplicationContext());
            RoutenHaupt.bounds = gck.readKML(pathKml, tvRouten, context);
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(RoutenHaupt.bounds, RoutenHaupt.PADDING_MAP);
            RoutenHaupt.mapRoutenwahl.moveCamera(cameraUpdate);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int getPos()
    {
        return pos;
    }
}