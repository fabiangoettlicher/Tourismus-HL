package com.htlhl.tourismus_hl;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.Target;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.kml.KmlLayer;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class KellerkatzeHaupt extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {

    public static GoogleMap mapKG;
    public static String stringEtStationNr, mapLAT, mapLNG;
    public static boolean flagMapReady;
    public static List<Marker> markers = new ArrayList<>();
    public static List<String> stationNamen, stationLATs, stationLNGs;
    public static TextView tvStation;
    public static EditText etStationNr;

    private ShowcaseView showcaseView;
    private int tutorialNext=0;
    private Target targetNavigate,targetStation,targetWahl;
    private String [] Tutorial;
    private boolean correct;
    private List<DbPoiXmlContainer> dbPoiXmlContainerList;
    private List<DbRoutenXmlContainer> dbRoutenXmlContainerList;
    private List<Integer> stationIDs;
    private List<String> stationNumbers;
    private android.support.v7.widget.Toolbar toolbar;
    private FragmentManager fm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kellerkatze_haupt);
        createToolbar(R.id.toolbar_kg_haupt, R.color.KG_green);

        dbPoiXmlContainerList = ReadDataFromFile.getDbPoiXmlContainerList(this);
        dbRoutenXmlContainerList = ReadDataFromFile.getDbRoutenXmlContainerList(this);
        if(dbPoiXmlContainerList==null){
            dbPoiXmlContainerList = ReadDataFromFile.getDbPoiXmlContainerListText(this);
            dbRoutenXmlContainerList = ReadDataFromFile.getDbRoutenXmlContainerListKml(this);
            if(dbRoutenXmlContainerList == null){
                dbRoutenXmlContainerList = ReadDataFromFile.getDbRoutenXmlContainerListText(this);
            }
        }
        stationIDs = new ArrayList<>();
        stationNamen = new ArrayList<>();
        stationLATs = new ArrayList<>();
        stationLNGs = new ArrayList<>();
        stationNumbers = new ArrayList<>();

        getData();

        fm = getFragmentManager();
        addShowhideListener(R.id.Top_Layout_KG, fm.findFragmentById(R.id.wahlrad_station), fm.findFragmentById(R.id.wahlrad_station_grey_overlay));

        //GoogleMaps
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map_KG);
        mapFragment.getMapAsync(this);

        LinearLayout bootomLayout = (LinearLayout) findViewById(R.id.bottom_layout_kg);
        bootomLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(KellerkatzeHaupt.this, KellerkatzeStation.class);
                startActivity(intent);
                KellerkatzeStation.markerClick = false;
            }
        });

        tvStation = (TextView) findViewById(R.id.tvStationKG);
        ImageButton imgBtnNavi = (ImageButton) findViewById(R.id.ImgBtnNaviKG);

        String tvStationString = "1. " + stationNamen.get(0);
        tvStation.setText(tvStationString);


        imgBtnNavi.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(!KellerkatzeStation.flagNextStation) {
                    for(int i=0; i<stationNamen.size(); i++){
                        String loggedStation = tvStation.getText().toString();
                        String[] splitLoggedStation = loggedStation.split(" ");
                        loggedStation = "";
                        for(int x=0; x<splitLoggedStation.length; x++){
                            if(x==1){
                                loggedStation = loggedStation.concat(splitLoggedStation[x]);
                            }else if(x>1){
                                loggedStation = loggedStation.concat(" "+splitLoggedStation[x]);
                            }
                        }
                        if(stationNamen.get(i).equals(loggedStation)){
                            mapLAT = stationLATs.get(i);
                            mapLNG = stationLNGs.get(i);
                        }
                    }
                }
                Uri gmmIntentUri = Uri.parse("google.navigation:q="+mapLAT+","+mapLNG);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
                KellerkatzeStation.flagNextStation = false;
            }
        });

        SharedPreferences firstStart2 = getSharedPreferences("pref", 0);
        if(firstStart2.getBoolean("FirstStart2", true)){
            firstStart2.edit().putBoolean("FirstStart2", false).apply();
            System.out.println("hallo");
            createShowcaseView();
        }
    }


    void createShowcaseView() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.hide(fm.findFragmentById(R.id.wahlrad_station));
        ft.hide(fm.findFragmentById(R.id.wahlrad_station_grey_overlay));
        ft.commit();

        Tutorial = getResources().getStringArray(R.array.Tutorial);
        Target targetTutorial = new Target() {
            @Override
            public Point getPoint() {
                return new ViewTarget(toolbar.findViewById(R.id.menuStartTutorialKg)).getPoint();
            }
        };
        targetWahl = new ViewTarget(R.id.tvStationKG, this);
        targetStation = new ViewTarget(R.id.bottom_layout_kg, this);
        targetNavigate = new ViewTarget(R.id.ImgBtnNaviKG, this);
        showcaseView = new ShowcaseView.Builder(this)
                .setTarget(targetTutorial)
                .setOnClickListener(this)
                .setContentTitle(Tutorial[2])
                .setContentText(Tutorial[3])
                .setStyle(R.style.ShowCaseViewStyle)
                .blockAllTouches()
                .withMaterialShowcase()
                .build();
        showcaseView.setButtonText(Tutorial[0]);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.addRule(RelativeLayout.ALIGN_PARENT_END);
        params.setMargins(0,0,20,125);
        showcaseView.setButtonPosition(params);
        showcaseView.show();
    }

    void addShowhideListener(int LayoutId, final Fragment fragment, final Fragment greyOverlay) {
        final LinearLayout LayoutChooseStation = (LinearLayout) findViewById(LayoutId);
        LayoutChooseStation.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.setCustomAnimations(R.animator.wahlrad_fragment_in,
                        R.animator.wahlrad_fragment_out);
                if (fragment.isHidden()) {
                    ft.show(fragment);
                    ft.show(greyOverlay);
                }
                ft.commit();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mapKG = map;
        flagMapReady = true;
        try {
            KmlLayer layer = new KmlLayer(map, R.raw.kellergassenweg, getApplicationContext());
            layer.addLayerToMap();
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }

        CameraUpdate center=
                CameraUpdateFactory.newLatLng(new LatLng(48.562085, 16.055127));
        CameraUpdate zoom=CameraUpdateFactory.zoomTo(14);
        map.moveCamera(center);
        map.animateCamera(zoom);
        map.getUiSettings().setMapToolbarEnabled(false);

        //Erstellen der Marker
        createMarkers(map);
        MarkerColor mC = new MarkerColor();
        mC.ChangeMarkerColor(stationLATs, stationLNGs, stationNamen);
    }

    public void createMarkers(GoogleMap map) {
        markers.clear();
        double doubLat=-1, doubLng=-1;
        for (int i=0; i<stationLATs.size(); i++) {
            String lat = stationLATs.get(i);
            String lng = stationLNGs.get(i);
            String name = stationNamen.get(i);
            String title = i+1+". "+name;
            if(!lat.equals("")) {
                doubLat = Double.parseDouble(lat);
            } else {
                System.out.println("Latitude nicht definiert");
            }
            if(!lng.equals("")) {
                doubLng = Double.parseDouble(lng);
            } else {
                System.out.println("Longitude nicht definiert");
            }
            if(doubLat!=-1 && doubLng!=-1 && !name.equals("")) {
                Marker marker = map.addMarker(new MarkerOptions()
                        .position(new LatLng(doubLat, doubLng))
                        .title(title));
                marker.setTag(name);
                markers.add(marker);
            }
            map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    Intent intent = new Intent(KellerkatzeHaupt.this, KellerkatzeStation.class);
                    startActivity(intent);
                    KellerkatzeStation.markerClick = true;
                    KellerkatzeStation.markerLoggedStation = marker.getTag().toString();
                }
            });
        }
    }

    public static GoogleMap getMapKG(){
        return mapKG;
    }

    public void createToolbar(int toolbarID, int color){
        //Toolbar with Backbutton
        toolbar = (android.support.v7.widget.Toolbar) findViewById(toolbarID);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //Set Homebutton
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //display Homebutton as Arrow (Back)
        getSupportActionBar().setDisplayShowTitleEnabled(false); //do not show Title
        // Change Color of the toolbar Navigationbuttons (Backbutton)
        final Drawable upArrow = toolbar.getNavigationIcon();
        upArrow.setColorFilter(getResources().getColor(color), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
    }

    public void onClickGreyOverlay (View view) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.animator.wahlrad_fragment_in,
                R.animator.wahlrad_fragment_out);
        if (getFragmentManager().findFragmentById(R.id.wahlrad_station_grey_overlay).isVisible()) {
            ft.hide(getFragmentManager().findFragmentById(R.id.wahlrad_station_grey_overlay));
            ft.hide(getFragmentManager().findFragmentById(R.id.wahlrad_station));
        }
        ft.commit();
        FragmentWahlradStation.tvStationLogged = tvStation.getText().toString();
    }

    public void getData(){
        for(int i=0; i<dbPoiXmlContainerList.size(); i++){
            if(dbPoiXmlContainerList.get(i).getPoiKatID_()==9){ //KatID 9 = Station
                stationIDs.add(dbPoiXmlContainerList.get(i).getPoiID_());
            }
        }
        Collections.sort(stationIDs);
        for(int x = 0; x< stationIDs.size(); x++){
            for(int y=0; y<dbPoiXmlContainerList.size(); y++){
                if(stationIDs.get(x) == dbPoiXmlContainerList.get(y).getPoiID_()){
                    stationNamen.add(dbPoiXmlContainerList.get(y).getPoiName_());
                    stationLATs.add(dbPoiXmlContainerList.get(y).getPoiLat_());
                    stationLNGs.add(dbPoiXmlContainerList.get(y).getPoiLng_());
                    stationNumbers.add(dbPoiXmlContainerList.get(y).getPoiStat_());
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_kg, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.menuStartTutorialKg:
                createShowcaseView();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {
        switch (tutorialNext){
            case 0:
                showcaseView.setShowcase(targetWahl, true);
                showcaseView.setContentTitle(Tutorial[11]);
                showcaseView.setContentText(Tutorial[12]);
                break;
            case 1:
                showcaseView.setShowcase(targetNavigate, true);
                showcaseView.setContentTitle(Tutorial[13]);
                showcaseView.setContentText(Tutorial[14]);
                break;
            case 2:
                showcaseView.setShowcase(targetStation, true);
                showcaseView.setContentTitle(Tutorial[15]);
                showcaseView.setContentText(Tutorial[16]);
                break;
            case 3:
                showcaseView.hide();
                tutorialNext=-1;
                break;
        }
        tutorialNext ++;
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
    public void onMessageEvent(MessageEvent event){
        if(event.message.equals("reload")) {
            this.recreate();
        }
    }
}