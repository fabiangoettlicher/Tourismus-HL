package com.htlhl.tourismus_hl;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class RoutenPoiList extends AppCompatActivity {

    static Boolean listViewShown = false;

    private Location lastLocation;
    private List<DbPoiXmlContainer> dbPoiXmlContainerList;
    private List<Integer> routenPoiIDsPoi, poiIcons, poiIDs;
    private List<String> poiNamen, poiEntfernungen, poiKontakte1;
    private List<Bitmap> poiBitmapLogoList, poiBew;
    private GoogleApiClient mGoogleApiClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routen_poi_list);
        listViewShown = true;
        createToolbar(R.id.toolbar_list_view_poi, R.color.Routen_rot);

        dbPoiXmlContainerList = ReadDataFromFile.getDbPoiXmlContainerList(this);
        if(dbPoiXmlContainerList==null){
            dbPoiXmlContainerList = ReadDataFromFile.getDbPoiXmlContainerListText(this);
        }
        routenPoiIDsPoi = RoutenHaupt.routenPoiIDsPoi;
        poiNamen = new ArrayList<>();
        poiBitmapLogoList = new ArrayList<>();
        poiEntfernungen = new ArrayList<>();
        poiKontakte1 = new ArrayList<>();
        poiIcons = new ArrayList<>();
        poiIDs = new ArrayList<>();
        poiBew = new ArrayList<>();
        final List<RoutenPoiListContainer> routenPoiListContainerList = new ArrayList<>();

        lastLocation = RoutenHaupt.lastLocation;


        getListViewData();

        //creates ArrayList of ListViewRows
        for (int i = 0; i < routenPoiIDsPoi.size(); i++) {
            routenPoiListContainerList.add(new RoutenPoiListContainer(poiBitmapLogoList.get(i), poiIcons.get(i),
                    poiBew.get(i), poiKontakte1.get(i), poiEntfernungen.get(i), poiNamen.get(i), poiIDs.get(i)));
        }
        //Sort ListView by distance
        Collections.sort(routenPoiListContainerList, new Comparator<RoutenPoiListContainer>() {
            @Override
            public int compare(RoutenPoiListContainer lhs, RoutenPoiListContainer rhs) {
                return lhs.getEntfernung().compareToIgnoreCase(rhs.getEntfernung()); // To compare string values
            }
        });
        //give the data to the adapter to create the ListRow
        RoutenPoiListAdapter adapter = new RoutenPoiListAdapter(this,
                R.layout.routen_poi_list_row, routenPoiListContainerList);
        ListView listViewPoi = (ListView) findViewById(R.id.listViewPoi);
        //set the content of the header
        View header = (View) getLayoutInflater().inflate(R.layout.routen_poi_list_header, null);
        listViewPoi.addHeaderView(header, null, false);
        listViewPoi.setAdapter(adapter);
        TextView tvHeader = (TextView) findViewById(R.id.txtHeader);
        tvHeader.setText(RoutenHaupt.routenname);
        System.out.println(RoutenHaupt.routenname);
        //go to informationpage if clicked on a list item
        listViewPoi.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RoutenPoiInfo.shownPoiId = routenPoiListContainerList.get(position - 1).getId();
                Intent intent = new Intent(RoutenPoiList.this, RoutenPoiInfo.class);
                startActivity(intent);
            }
        });
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

    private void getListViewData() {
        poiNamen.clear();
        poiKontakte1.clear();
        poiEntfernungen.clear();
        poiIcons.clear();
        poiIDs.clear();
        poiBew.clear();

        Location poiLocation = new Location("");
        float floatDistanceM, floatDistanceKM;
        String stringDistance;

        for (int y = 0; y < dbPoiXmlContainerList.size(); y++) {
            for (int z = 0; z < routenPoiIDsPoi.size(); z++) { //vergleiche jeden Poi mit jeder ID
                if (dbPoiXmlContainerList.get(y).getPoiID_() == routenPoiIDsPoi.get(z)) { //wenn übereinstimmt
                    poiIDs.add(dbPoiXmlContainerList.get(y).getPoiID_());
                    if (!dbPoiXmlContainerList.get(y).getPoiLat_().equals("") && !dbPoiXmlContainerList.get(y).getPoiLng_().equals("") && lastLocation!=null) {
                        poiLocation.setLatitude(Double.parseDouble(dbPoiXmlContainerList.get(y).getPoiLat_()));
                        poiLocation.setLongitude(Double.parseDouble(dbPoiXmlContainerList.get(y).getPoiLng_()));
                        floatDistanceM = lastLocation.distanceTo(poiLocation);
                        floatDistanceKM = floatDistanceM / 1000;
                        if (floatDistanceM < 100) {
                            stringDistance = new DecimalFormat("##").format(floatDistanceM) + " m";
                        } else {
                            stringDistance = new DecimalFormat("###.#").format(floatDistanceKM) + " km";
                        }
                        poiEntfernungen.add(stringDistance);
                    } else {
                        poiEntfernungen.add(" ");
                    }
                    poiNamen.add(dbPoiXmlContainerList.get(y).getPoiName_());
                    poiKontakte1.add(dbPoiXmlContainerList.get(y).getPoiKontak1_());
                    if (dbPoiXmlContainerList.get(y).getPathLogo1_() != null) {
                        poiBitmapLogoList.add(ReadDataFromFile.getBitmap(dbPoiXmlContainerList.get(y).getPathLogo1_()));
                    } else if (dbPoiXmlContainerList.get(y).getPathLogo2_() != null) {
                        poiBitmapLogoList.add(ReadDataFromFile.getBitmap(dbPoiXmlContainerList.get(y).getPathLogo2_()));
                    } else if (dbPoiXmlContainerList.get(y).getPathLogo3_() != null) {
                        poiBitmapLogoList.add(ReadDataFromFile.getBitmap(dbPoiXmlContainerList.get(y).getPathLogo3_()));
                    } else if (dbPoiXmlContainerList.get(y).getPathLogo4_() != null) {
                        poiBitmapLogoList.add(ReadDataFromFile.getBitmap(dbPoiXmlContainerList.get(y).getPathLogo4_()));
                    } else if (dbPoiXmlContainerList.get(y).getPathLogo5_() != null) {
                        poiBitmapLogoList.add(ReadDataFromFile.getBitmap(dbPoiXmlContainerList.get(y).getPathLogo5_()));
                    } else {
                        poiBitmapLogoList.add(null);
                    }
                    if(dbPoiXmlContainerList.get(y).getPathLogo6_() != null){
                        poiBew.add(ReadDataFromFile.getBitmap(dbPoiXmlContainerList.get(y).getPathLogo6_()));
                    } else {
                        poiBew.add(null);
                    }
                    int icon;
                    switch (dbPoiXmlContainerList.get(y).getPoiKatID_()) {
                        case 3:
                            icon = R.drawable.marker_restaurant;
                            break;
                        case 10:
                            icon = R.drawable.marker_sehenswuerdigkeit;
                            break;
                        case 2:
                            icon = R.drawable.marker_hotel;
                            break;
                        case 4:
                            icon = R.drawable.marker_heurigen;
                            break;
                        case 11:
                            icon = R.drawable.marker_kellergasse;
                            break;
                        case 1:
                            icon = R.drawable.marker_umweltmobil;
                            break;
                        default:
                            icon = -1;
                            break;
                    }
                    poiIcons.add(icon);
                }
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStart();
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
