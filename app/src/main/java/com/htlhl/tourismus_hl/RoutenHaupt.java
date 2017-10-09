package com.htlhl.tourismus_hl;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.Target;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.htlhl.tourismus_hl.Data.Local.ReadDataFromFile;
import com.htlhl.tourismus_hl.Data.Model.PointOfInterest;
import com.htlhl.tourismus_hl.Data.Model.Routen;
import com.htlhl.tourismus_hl.Data.Model.RoutenPointOfInterestLinking;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RoutenHaupt extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    public static final int PADDING_MAP = 100;
    public static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 15;
    static final float COORDINATE_OFFSET = 0.00008f;

    public static String routenname;
    public static LatLngBounds bounds;
    public static Boolean markersVisible;
    public static List<Marker> markers = new ArrayList<>();
    public static List<String> radwegeNamen, wanderwegNamen, radwegeKmlPaths, wanderwegeKmlPaths;
    public static List<Integer> routenPoiIDsPoi;
    public static GoogleMap mapRoutenwahl;
    public static Location lastLocation;
    public static TextView tvRouten;
    private static List<LatLng> newPositions;


    private int tutorialNext = 0;
    private ShowcaseView showcaseView;
    private ViewTarget targetPoi, targetFullscreen, targetWahl, targetLocation;
    private Target targetList;
    private Context context;
    private String stringTvTitle, pathHoehen, pathRoutenLogo1, pathRoutenLogo2, pathKml, kellergassenlaufKmlPath;
    private String[] routenInfos, Tutorial;
    private FragmentManager fm = getFragmentManager();
    private Boolean isMapInit = false, markersVisibleReload;
    private ImageButton ImgBtnPoi;
    private ScrollView svRouten;
    private android.support.v7.widget.Toolbar toolbar;
    private MapFragment mapFragment;
    private List<Routen> routenList;
    private List<RoutenPointOfInterestLinking> routenPointOfInterestLinkingList;
    private List<PointOfInterest> PointOfInterestList;
    private List<Integer> poiIDs, poiKatIDs;
    private List<String> poiNamen, poiLats, poiLngs;
    private GoogleMap mapRoute;
    private GoogleApiClient mGoogleApiClient;
    private Marker markerStart, markerEnd, markerStartEnd;
    private SharedPreferences permissionExplanation;

    @Override
    protected void onPause() {
        super.onPause();
        RoutenPoiList.listViewShown = false;
        //create sharedPreferences
        SharedPreferences screen = getSharedPreferences("pref", 0);
        SharedPreferences.Editor editor = screen.edit();
        editor.apply();
        //put Layer
        editor.putString("LayerPath", pathKml);
        //put Routenname
        editor.putString("RoutenName", tvRouten.getText().toString());
        //put markersVisible
        editor.putBoolean("MarkerShown", markersVisible);
        editor.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getRoutenInfoDB(null, this);
        setRoutenInfo(this);
        if (RoutenPoiList.listViewShown) {
            SharedPreferences screen = getSharedPreferences("pref", 0);
            tvRouten.setText(screen.getString("RoutenName", ""));
            markersVisibleReload = screen.getBoolean("MarkerShown", false);
        }

        if (RoutenMapFullscreen.mapWasFullscreen) {
            RoutenMapFullscreen.mapWasFullscreen = false;
            if (RoutenMapFullscreen.markersVisibleFull) {
                if (!markersVisible) {
                    createMarkers();
                    createStartZielMarkers();
                    markersVisible = true;
                    ImgBtnPoi.setImageResource(R.drawable.btn_hide_poi);
                }
            } else {
                if (markersVisible) {
                    markersVisible = false;
                    ImgBtnPoi.setImageResource(R.drawable.btn_show_poi);
                    for (Marker marker : markers) {
                        marker.remove();
                    }
                    markers.clear();
                    if (KmlCoordinates.flagStartZiel) {
                        if(markerStartEnd!=null)
                            markerStartEnd.remove();
                    } else {
                        if(markerStart!=null)
                            markerStart.remove();
                        if(markerEnd!=null)
                            markerEnd.remove();
                    }
                }
            }
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, PADDING_MAP);
            mapRoutenwahl.moveCamera(cameraUpdate);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        newPositions = new ArrayList<>();
        RoutenMapFullscreen.mapWasFullscreen = false;
        setContentView(R.layout.activity_routen_haupt);
        createToolbar(R.id.toolbar_routen_haupt, R.color.Routen_rot);
        if (FragmentRoutentyp.flagLauf) {
            findViewById(R.id.ImgViewChooseRouten).setVisibility(View.GONE);
        }
        permissionExplanation = getSharedPreferences("pref", 0);
        context = this;
        routenList = ReadDataFromFile.getDbRoutenXmlContainerList(this);
        PointOfInterestList = ReadDataFromFile.getDbPoiXmlContainerList(this);
        routenPointOfInterestLinkingList = ReadDataFromFile.getDbRoutenPoiXmlContainerList(this);
        if (routenList == null || PointOfInterestList == null || routenPointOfInterestLinkingList == null) {
            PointOfInterestList = ReadDataFromFile.getDbPoiXmlContainerListText(this);
            routenList = ReadDataFromFile.getDbRoutenXmlContainerListKml(this);
            if(routenList == null){
                routenList = ReadDataFromFile.getDbRoutenXmlContainerListText(this);
                Toast.makeText(this, R.string.waitkmldownload, Toast.LENGTH_LONG).show();
            }
            routenPointOfInterestLinkingList = ReadDataFromFile.getDbRoutenPoiXmlContainerListText(this);
        }
        radwegeNamen = new ArrayList<>();
        wanderwegNamen = new ArrayList<>();
        poiIDs = new ArrayList<>();
        routenPoiIDsPoi = new ArrayList<>();
        poiLats = new ArrayList<>();
        poiLngs = new ArrayList<>();
        poiNamen = new ArrayList<>();
        poiKatIDs = new ArrayList<>();
        radwegeKmlPaths = new ArrayList<>();
        wanderwegeKmlPaths = new ArrayList<>();

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        getRoutenDB();

        //variable definition
        svRouten = (ScrollView) findViewById(R.id.sv_routen_haupt);
        TextView tvTitle = (TextView) findViewById(R.id.toolbar_title_routenwahl);
        tvRouten = (TextView) findViewById(R.id.tvRoutenwahl);
        LinearLayout topLayout = (LinearLayout) findViewById(R.id.Top_Layout_routenwahl);
        ImgBtnPoi = (ImageButton) findViewById(R.id.ImgBtnPoi);
        ImageButton imgBtnFullscreen = (ImageButton) findViewById(R.id.ImgBtnFullscreenMap);
        ImageButton imgBtnLocation = (ImageButton) findViewById(R.id.ImgBtnLocation);

        //GoogleMaps
        mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map_routen);
        mapFragment.getMapAsync(this);


        //set ImgButtonPoi and ImgBtnFullscreen and imgBtnLocation
        ImgBtnPoi.setImageResource(R.drawable.btn_show_poi);
        ImgBtnPoi.setTag(R.drawable.btn_show_poi);
        markersVisible = false;

        imgBtnFullscreen.setImageResource(R.drawable.btn_fullscreen_map);
        imgBtnFullscreen.setTag(R.drawable.btn_fullscreen_map);

        imgBtnLocation.setImageResource(R.drawable.btn_show_location);
        imgBtnLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RoutenHaupt.this, RoutenMapFullscreen.class);
                startActivity(intent);

                if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    RoutenMapFullscreen.shouldShowLocation = true;
                } else {
                    RoutenMapFullscreen.showLocation = true;
                }
            }
        });


        //Listens to the Maplayout, if its changing -> visible (newLatLngBounds could find the biggest possible zoomlevel)
        findViewById(R.id.middle_Layout_routenwahl).getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                displayMap();
            }
        });

        if (FragmentRoutentyp.flagWander) {
            stringTvTitle = getResources().getString(R.string.btnWanderwege);
            tvRouten.setText(wanderwegNamen.get(0));
        } else if (FragmentRoutentyp.flagRad) {
            stringTvTitle = getResources().getString(R.string.btnRadwege);
            tvRouten.setText(radwegeNamen.get(0));
        } else if (FragmentRoutentyp.flagLauf) {
            stringTvTitle = getResources().getString(R.string.Kellergassenlauf);
            topLayout.setClickable(false);
            for (int i = 0; i < routenList.size(); i++) {
                if (routenList.get(i).getRoutenID_() == 25) {
                    tvRouten.setText(routenList.get(i).getRoutenName_());
                }
            }
            ImgBtnPoi.setVisibility(View.GONE);
        }
        tvTitle.setText(stringTvTitle);
        SharedPreferences firstStartRoute = getSharedPreferences("pref", 0);
        if(firstStartRoute.getBoolean("FirstStartRoute", true)){
            firstStartRoute.edit().putBoolean("FirstStartRoute", false).apply();
            createShowcaseView();
        }

        //OnClickListeners
        ImgBtnPoi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!markersVisible) {
                    getPoisDB();
                    ImgBtnPoi.setImageResource(R.drawable.btn_hide_poi);
                    createMarkers();
                    createStartZielMarkers();
                } else {
                    ImgBtnPoi.setImageResource(R.drawable.btn_show_poi);
                    for (Marker marker : markers) {
                        marker.remove();
                    }
                    markers.clear();
                    if (KmlCoordinates.flagStartZiel) {
                        if (markerStartEnd != null)
                            markerStartEnd.remove();
                    } else {
                        if (markerStart != null)
                            markerStart.remove();
                        if (markerEnd != null)
                            markerEnd.remove();
                    }
                }
                markersVisible = !markersVisible;
            }
        });

        imgBtnFullscreen.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), RoutenMapFullscreen.class);
                startActivity(intent);
                RoutenMapFullscreen.showLocation = false;
            }
        }));

        //transparentImageView catches all Motions on Map (Use Map in Scrollview)
        ImageView transparentImageView = (ImageView) findViewById(R.id.transparent_image);
        transparentImageView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        // Disallow ScrollView to intercept touch events.
                        svRouten.requestDisallowInterceptTouchEvent(true);
                        // Disable touch on transparent view
                        return false;

                    case MotionEvent.ACTION_UP:
                        // Allow ScrollView to intercept touch events.
                        svRouten.requestDisallowInterceptTouchEvent(false);
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        svRouten.requestDisallowInterceptTouchEvent(true);
                        return false;

                    default:
                        return true;
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) { //Wenn Map bereit ist und angezeigt wird
        mapRoutenwahl = googleMap;
        mapRoute = googleMap;
        mapRoute.getUiSettings().setMapToolbarEnabled(false);
        displayMap();
        if (RoutenPoiList.listViewShown) {
            if (markersVisibleReload) {
                ImgBtnPoi.setImageResource(R.drawable.btn_hide_poi);
                markersVisible = true;
                getPoisDB();
                createMarkers();
                createStartZielMarkers();
            }
        }
        //detect which type has to be shown
        if (FragmentRoutentyp.flagRad) {
            getPoisDB();
            //find the right layer
            for (int i = 0; i < radwegeNamen.size(); i++) {
                if (radwegeNamen.get(i).equals(tvRouten.getText().toString())) {
                    pathKml = radwegeKmlPaths.get(i);
                }
            }
            try {
                //get the layer from storage and show it on the map
                ReadDataFromFile.getKml(pathKml, mapRoutenwahl, context);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (FragmentRoutentyp.flagWander) {
            getPoisDB();
            for (int i = 0; i < wanderwegNamen.size(); i++) {
                if (wanderwegNamen.get(i).equals(tvRouten.getText().toString())) {
                    pathKml = wanderwegeKmlPaths.get(i);
                }
            }
            try {
                ReadDataFromFile.getKml(pathKml, mapRoutenwahl, context);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (FragmentRoutentyp.flagLauf) {
            getPoisDB();
            try {
                ReadDataFromFile.getKml(kellergassenlaufKmlPath, mapRoutenwahl, context);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //get bounds of Layer to fit it into the map
        KmlCoordinates gck = new KmlCoordinates();
        if (FragmentRoutentyp.flagLauf) {
            bounds = gck.readKML(kellergassenlaufKmlPath, tvRouten, context);
        } else {
            bounds = gck.readKML(pathKml, tvRouten, context);
        }
    }

    private void createStartZielMarkers() {
        if (!KmlCoordinates.flagNoKml) {
            if (KmlCoordinates.flagStartZiel) {
                markerStartEnd = mapRoutenwahl.addMarker(new MarkerOptions() //add markers to map
                        .position(KmlCoordinates.startendpoint)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_flagge_startziel)));
            } else {
                markerStart = mapRoutenwahl.addMarker(new MarkerOptions() //add markers to map
                        .position(KmlCoordinates.startpoint)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_flagge_start)));
                markerEnd = mapRoutenwahl.addMarker(new MarkerOptions() //add markers to map
                        .position(KmlCoordinates.endpoint)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_flagge_ziel)));
            }
        }
    }

    public void createMarkers() {
        int markerCount = poiIDs.size();
        int kat = -1, id = -1;
        double lat = -1, lng = -1;
        String name = "";
        for (int i = 0; i < markerCount; i++) {
            kat = poiKatIDs.get(i);
            id = poiIDs.get(i);
            if (poiLats.get(i).equals("")) {
                System.out.println("ERROR: Latitude nicht definiert!");
            } else {
                lat = Double.parseDouble(poiLats.get(i));
            }
            if (poiLngs.get(i).equals("")) {
                System.out.println("ERROR: Longitude nicht definiert!");
            } else {
                lng = Double.parseDouble(poiLngs.get(i));
            }
            if (poiNamen.get(i).equals("")) {
                System.out.println("ERROR: Name nicht definiert!");
            } else {
                name = poiNamen.get(i);
            }
            Boolean knownkat = true;
            int icon;
            switch (kat) {   //different markertypes
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
                    knownkat = false;
                    break;
            }
            if (lat != -1 && lng != -1 && !name.equals("") && kat != -1 && id != -1) {
                LatLng position = isMarkerAtPosition(new LatLng(lat, lng));
                if(position.latitude!=lat){
                    newPositions.add(position);
                }
                if (knownkat) {
                    Marker marker = mapRoutenwahl.addMarker(new MarkerOptions() //add markers to map
                            .position(position)
                            .title(name)
                            .icon(BitmapDescriptorFactory.fromResource(icon)));
                    marker.setTag(id);
                    markers.add(marker);    //save markers in array
                } else {
                    Marker marker = mapRoutenwahl.addMarker(new MarkerOptions() //add markers to map
                            .position(position)
                            .title(name));
                    marker.setTag(id);
                    markers.add(marker);    //save markers in array
                }
            } else {
                System.out.println("ACHTUNG: Marker (" + poiNamen.get(i) + ") wurde nicht erstellt!");
            }
            mapRoutenwahl.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    Intent intent = new Intent(RoutenHaupt.this, RoutenPoiInfo.class);
                    startActivity(intent);
                    RoutenPoiInfo.shownPoiId = (Integer) marker.getTag();
                }
            });
            kat = id - 1;
            lat = lng = -1;
            name = "";
        }
        newPositions.clear();
    }

    public LatLng isMarkerAtPosition(LatLng oldPosition){
        double oldLng, oldLat, latMarker, lngMarker, newLat=-1, newLng=-1, latMarkerPos, lngMarkerPos;
        oldLat = oldPosition.latitude;
        oldLng = oldPosition.longitude;
        for(int i=0; i<markers.size(); i++){
            latMarker = markers.get(i).getPosition().latitude;
            lngMarker = markers.get(i).getPosition().longitude;
            if(oldLat-latMarker < COORDINATE_OFFSET && oldLat-latMarker > -COORDINATE_OFFSET || oldLng-lngMarker < COORDINATE_OFFSET && oldLng-lngMarker > -COORDINATE_OFFSET){
                newLat = oldLat+COORDINATE_OFFSET;
                newLng = oldLng;
                if(newPositions.size()>0){
                    for (int x=0; x<newPositions.size(); x++){
                        latMarkerPos = newPositions.get(x).latitude;
                        lngMarkerPos = newPositions.get(x).longitude;
                        if(newLat-latMarkerPos < COORDINATE_OFFSET && newLat-latMarkerPos > -COORDINATE_OFFSET || newLng-lngMarkerPos < COORDINATE_OFFSET && newLng-lngMarkerPos > -COORDINATE_OFFSET){
                            newLat = newLat+COORDINATE_OFFSET;
                        }
                    }
                }
                return new LatLng(newLat, newLng);
            }
        }
        return oldPosition;
    }

    public void toListViewPoi() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        } else {
            permissionExplanation.edit().putBoolean("firstSetting", true).apply();
            permissionExplanation.edit().putBoolean("firstDeny", true).apply();
            getPoisDB();
            Intent intent = new Intent(RoutenHaupt.this, RoutenPoiList.class);
            startActivity(intent);
        }
    }

    public void onClickGreyOverlay(View view) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.animator.wahlrad_fragment_in,
                R.animator.wahlrad_fragment_out);
        if (fm.findFragmentById(R.id.wahlrad_routen_grey_overlay).isVisible()) {
            ft.hide(fm.findFragmentById(R.id.wahlrad_routen_grey_overlay));
            ft.hide(fm.findFragmentById(R.id.wahlrad_routen));
        }
        ft.commit();
        FragmentWahlradRouten.tvRoutenLogged = tvRouten.getText().toString();
    }

    private void createToolbar(int toolbarID, int color) {
        //toolbar with Backbutton
        toolbar = (android.support.v7.widget.Toolbar) findViewById(toolbarID);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //set homebutton
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //display homebutton as arrow (Back)
        getSupportActionBar().setDisplayShowTitleEnabled(false); //do not show title
        // change color of the toolbar navigationbuttons (Backbutton)
        final Drawable upArrow = toolbar.getNavigationIcon();
        upArrow.setColorFilter(getResources().getColor(color), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
    }

    public void displayMap() {
        // Check that mMap has been initialised AND the width of the MapFragment has been set:
        if (isMapInit || mapRoutenwahl == null || mapRoute == null) return;
        if (mapFragment.getView().getRight() <= 0) return;
        if (bounds == null) return;
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, PADDING_MAP);
        mapRoutenwahl.moveCamera(cameraUpdate);
        isMapInit = true;
    }

    private void getRoutenDB() {
        radwegeNamen.clear();
        wanderwegNamen.clear();
        if (FragmentRoutentyp.flagRad) {
            for (int i = 0; i < routenList.size(); i++) {
                if (routenList.get(i).getRoutenKatID_() == 5) { //KatID 5 = Radwege
                    radwegeNamen.add(routenList.get(i).getRoutenName_());
                    radwegeKmlPaths.add(routenList.get(i).getPathKml_());
                }
            }
        } else if (FragmentRoutentyp.flagWander) {
            for (int i = 0; i < routenList.size(); i++) {
                if (routenList.get(i).getRoutenKatID_() == 6) { //KatID 6 = Wanderwege
                    wanderwegNamen.add(routenList.get(i).getRoutenName_());
                    wanderwegeKmlPaths.add(routenList.get(i).getPathKml_());
                }
            }
        }
        if (FragmentRoutentyp.flagLauf) {
            for (int i = 0; i < routenList.size(); i++) {
                if (routenList.get(i).getRoutenID_() == 25) { //Kellergassenlauf
                    kellergassenlaufKmlPath = routenList.get(i).getPathKml_();
                }
            }
        }
    }

    private void getPoisDB() {
        routenPoiIDsPoi.clear();
        SharedPreferences language = getSharedPreferences("pref", 0);
        for (int i = 0; i < routenList.size(); i++) {
            if (routenList.get(i).getRoutenName_().equals(tvRouten.getText())) { //aktuelle Route
                int routenID = routenList.get(i).getRoutenID_(); //speichere RoutenID
                routenname = routenList.get(i).getRoutenName_();
                for (int x = 0; x < routenPointOfInterestLinkingList.size(); x++) { //finde dazugehoerige Poi
                    if (routenPointOfInterestLinkingList.get(x).getRoutenpoiIDrouten_() == routenID) {
                        routenPoiIDsPoi.add(routenPointOfInterestLinkingList.get(x).getRoutenpoiIDpoi_());
                    }
                }
            }
        }
        poiLats.clear();
        poiLngs.clear();
        poiNamen.clear();
        poiIDs.clear();
        poiKatIDs.clear();
        for (int y = 0; y < PointOfInterestList.size(); y++) {
            for (int z = 0; z < routenPoiIDsPoi.size(); z++) { //vergleiche jeden Poi mit jeder ID
                if (PointOfInterestList.get(y).getPoiID_() == routenPoiIDsPoi.get(z)) { //wenn übereinstimmt
                    poiLats.add(PointOfInterestList.get(y).getPoiLat_());
                    poiLngs.add(PointOfInterestList.get(y).getPoiLng_());
                    poiNamen.add(PointOfInterestList.get(y).getPoiName_());
                    poiKatIDs.add(PointOfInterestList.get(y).getPoiKatID_());
                    poiIDs.add(PointOfInterestList.get(y).getPoiID_());
                }
            }
        }
    }

    public void getRoutenInfoDB(String data, Context con) {
        String currRoute;
        if (data != null) {
            currRoute = data;
            routenList = ReadDataFromFile.getDbRoutenXmlContainerList(con);
            if (routenList == null) {
                routenList = ReadDataFromFile.getDbRoutenXmlContainerListText(con);
            }
        } else {
            currRoute = tvRouten.getText().toString();
        }
        SharedPreferences language = con.getSharedPreferences("pref", 0);
        routenInfos = new String[7];
        for (int i = 0; i < routenList.size(); i++) {
            if (routenList.get(i).getRoutenName_().equals(currRoute)) {
                pathHoehen = routenList.get(i).getPathBild1_();
                pathRoutenLogo1 = routenList.get(i).getPathBild2_();
                pathRoutenLogo2 = routenList.get(i).getPathBild3_();
                if (language.getString("ActiveLang", "").equals("ger")) {
                    routenInfos[0] = routenList.get(i).getRoutenInfo1DE_();
                    routenInfos[1] = routenList.get(i).getRoutenInfo2DE_();
                    routenInfos[2] = routenList.get(i).getRoutenInfo3DE_();
                    routenInfos[3] = routenList.get(i).getRoutenInfo4DE_();
                    routenInfos[4] = routenList.get(i).getRoutenInfo5DE_();
                    routenInfos[5] = routenList.get(i).getRoutenInfo6DE_();
                    routenInfos[6] = routenList.get(i).getRoutenInfo7DE_();
                } else if (language.getString("ActiveLang", "").equals("bri")) {
                    routenInfos[0] = routenList.get(i).getRoutenInfo1EN_();
                    routenInfos[1] = routenList.get(i).getRoutenInfo2EN_();
                    routenInfos[2] = routenList.get(i).getRoutenInfo3EN_();
                    routenInfos[3] = routenList.get(i).getRoutenInfo4EN_();
                    routenInfos[4] = routenList.get(i).getRoutenInfo5EN_();
                    routenInfos[5] = routenList.get(i).getRoutenInfo6EN_();
                    routenInfos[6] = routenList.get(i).getRoutenInfo7EN_();
                } else if (language.getString("ActiveLang", "").equals("cze")) {
                    routenInfos[0] = routenList.get(i).getRoutenInfo1CZ_();
                    routenInfos[1] = routenList.get(i).getRoutenInfo2CZ_();
                    routenInfos[2] = routenList.get(i).getRoutenInfo3CZ_();
                    routenInfos[3] = routenList.get(i).getRoutenInfo4CZ_();
                    routenInfos[4] = routenList.get(i).getRoutenInfo5CZ_();
                    routenInfos[5] = routenList.get(i).getRoutenInfo6CZ_();
                    routenInfos[6] = routenList.get(i).getRoutenInfo7CZ_();
                }
            }
        }
    }

    public void setRoutenInfo(Activity con) {
        LinearLayout llHoehen = (LinearLayout) con.findViewById(R.id.layout_hoehenprofil);
        LinearLayout llInfoTop = (LinearLayout) con.findViewById(R.id.layout_info_top);
        LinearLayout llInfoMiddle = (LinearLayout) con.findViewById(R.id.layout_info_middle);
        LinearLayout llInfoBottom = (LinearLayout) con.findViewById(R.id.layout_info_bottom);
        LinearLayout llInfoText = (LinearLayout) con.findViewById(R.id.layout_info_text);
        LinearLayout llInfoLogos = (LinearLayout) con.findViewById(R.id.layout_info_logo1);
        llHoehen.removeAllViews();
        llInfoTop.removeAllViews();
        llInfoMiddle.removeAllViews();
        llInfoBottom.removeAllViews();
        llInfoLogos.removeAllViews();
        llInfoText.removeAllViews();
        TextView tvAusgangspunkt = new TextView(con);
        TextView tvMarkierung = new TextView(con);
        TextView tvLaenge = new TextView(con);
        TextView tvDauer = new TextView(con);
        TextView tvEignung = new TextView(con);
        TextView tvTextRouteninfo = new TextView(con);
        TextView tvHoehenmeter = new TextView(con);
        ImageView ivHoehen = new ImageView(con);
        ImageView ivLogo1 = new ImageView(con);
        ImageView ivLogo2 = new ImageView(con);
        con.findViewById(R.id.divider_info_1).setVisibility(View.GONE);
        con.findViewById(R.id.divider_info_2).setVisibility(View.GONE);
        con.findViewById(R.id.divider_info_3).setVisibility(View.GONE);
        con.findViewById(R.id.divider_info_4).setVisibility(View.GONE);
        con.findViewById(R.id.divider_info_5).setVisibility(View.GONE);
        if (pathHoehen != null) {
            ivHoehen.setId(View.generateViewId());
            LinearLayout.LayoutParams paramsHoehen = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT); //hoehe stimmt afoch ned
            ivHoehen.setImageBitmap(ReadDataFromFile.getBitmap(pathHoehen));
            llHoehen.addView(ivHoehen, paramsHoehen);
        }
        if (!routenInfos[0].equals("")) {
            tvAusgangspunkt.setId(View.generateViewId());
            LinearLayout.LayoutParams paramsAusgang = new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            tvAusgangspunkt.setText(Html.fromHtml(routenInfos[0]));
            tvAusgangspunkt.setGravity(Gravity.CENTER);
            tvAusgangspunkt.setTextColor(con.getResources().getColor(R.color.black));
            tvAusgangspunkt.setTextSize(12);
            paramsAusgang.weight = 1;
            llInfoTop.addView(tvAusgangspunkt, paramsAusgang);
        }
        if (!routenInfos[1].equals("")) {
            tvLaenge.setId(View.generateViewId());
            LinearLayout.LayoutParams paramslaenge = new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            tvLaenge.setText(Html.fromHtml(routenInfos[1]));
            tvLaenge.setGravity(Gravity.CENTER);
            tvLaenge.setTextColor(con.getResources().getColor(R.color.black));
            tvLaenge.setTextSize(12);
            paramslaenge.weight = 1;
            llInfoTop.addView(tvLaenge, paramslaenge);
        }
        if (!routenInfos[2].equals("")) {
            tvEignung.setId(View.generateViewId());
            LinearLayout.LayoutParams paramsEignung = new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            tvEignung.setText(Html.fromHtml(routenInfos[2]));
            tvEignung.setGravity(Gravity.CENTER);
            tvEignung.setTextColor(con.getResources().getColor(R.color.black));
            tvEignung.setTextSize(12);
            paramsEignung.weight = 1;
            if (llInfoTop.getChildCount() < 2) {
                llInfoTop.addView(tvEignung, paramsEignung);
            } else {
                llInfoMiddle.addView(tvEignung, paramsEignung);
            }
        }
        if (!routenInfos[4].equals("")) {
            tvDauer.setId(View.generateViewId());
            LinearLayout.LayoutParams paramsDauer = new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            tvDauer.setText(Html.fromHtml(routenInfos[4]));
            tvDauer.setGravity(Gravity.CENTER);
            tvDauer.setTextColor(con.getResources().getColor(R.color.black));
            tvDauer.setTextSize(12);
            paramsDauer.weight = 1;
            if (llInfoTop.getChildCount() < 2) {
                llInfoTop.addView(tvDauer, paramsDauer);
            } else {
                llInfoMiddle.addView(tvDauer, paramsDauer);
            }
        }
        if (!routenInfos[5].equals("")) {
            tvHoehenmeter.setId(View.generateViewId());
            LinearLayout.LayoutParams paramsHoehenges = new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            tvHoehenmeter.setText(Html.fromHtml(routenInfos[5]));
            tvHoehenmeter.setGravity(Gravity.CENTER);
            tvHoehenmeter.setTextColor(con.getResources().getColor(R.color.black));
            tvHoehenmeter.setTextSize(12);
            paramsHoehenges.weight = 1;
            if (llInfoTop.getChildCount() < 2) {
                llInfoTop.addView(tvHoehenmeter, paramsHoehenges);
            } else if (llInfoMiddle.getChildCount() < 2) {
                llInfoMiddle.addView(tvHoehenmeter, paramsHoehenges);
            } else {
                llInfoBottom.addView(tvHoehenmeter, paramsHoehenges);
            }
        }
        if (!routenInfos[6].equals("")) {
            tvMarkierung.setId(View.generateViewId());
            LinearLayout.LayoutParams paramsMarkierung = new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            tvMarkierung.setText(Html.fromHtml(routenInfos[6]));
            tvMarkierung.setGravity(Gravity.CENTER);
            tvMarkierung.setTextColor(con.getResources().getColor(R.color.black));
            tvMarkierung.setTextSize(12);
            paramsMarkierung.weight = 1;
            if (llInfoTop.getChildCount() < 2) {
                llInfoTop.addView(tvMarkierung, paramsMarkierung);
            } else if (llInfoMiddle.getChildCount() < 2) {
                llInfoMiddle.addView(tvMarkierung, paramsMarkierung);
            } else {
                llInfoBottom.addView(tvMarkierung, paramsMarkierung);
            }
        }
        if (!routenInfos[3].equals("")) {
            tvTextRouteninfo.setId(View.generateViewId());
            LinearLayout.LayoutParams paramsText = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            tvTextRouteninfo.setText(Html.fromHtml(routenInfos[3]));
            tvTextRouteninfo.setGravity(Gravity.CENTER);
            tvTextRouteninfo.setTextColor(con.getResources().getColor(R.color.black));
            tvTextRouteninfo.setTextSize(12);
            llInfoText.addView(tvTextRouteninfo, paramsText);
        }
        if (pathRoutenLogo1 != null) {
            ivLogo1.setId(View.generateViewId());
            LinearLayout.LayoutParams paramsLogo1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            paramsLogo1.gravity = Gravity.CENTER_HORIZONTAL;
            paramsLogo1.setMargins(0, 10, 0, 10);
            ivLogo1.setImageBitmap(ReadDataFromFile.getBitmap(pathRoutenLogo1));
            llInfoLogos.addView(ivLogo1, paramsLogo1);
        }
        if (llHoehen.getChildCount() == 1) {
            con.findViewById(R.id.divider_info_1).setVisibility(View.VISIBLE);
        }
        if (llInfoTop.getChildCount() > 0) {
            con.findViewById(R.id.divider_info_2).setVisibility(View.VISIBLE);
        }
        if (llInfoMiddle.getChildCount() > 0) {
            con.findViewById(R.id.divider_info_3).setVisibility(View.VISIBLE);
        }
        if (llInfoBottom.getChildCount() > 0) {
            con.findViewById(R.id.divider_info_4).setVisibility(View.VISIBLE);
        }
        if (llInfoText.getChildCount() > 0) {
            con.findViewById(R.id.divider_info_5).setVisibility(View.VISIBLE);
        }
    }

    private void createShowcaseView() {
        getRoutenInfoDB(null, this);
        setRoutenInfo(this);
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.hide(fm.findFragmentById(R.id.wahlrad_routen));
        ft.hide(fm.findFragmentById(R.id.wahlrad_routen_grey_overlay));
        ft.commit();
        //define the targets by the ID
        Tutorial = getResources().getStringArray(R.array.Tutorial);
        Target targetTutorial = new Target() {
            @Override
            public Point getPoint() {
                return new ViewTarget(toolbar.findViewById(R.id.menuStartTutorialRh)).getPoint();
            }
        };
        targetList = new Target() {
            @Override
            public Point getPoint() {
                return new ViewTarget(toolbar.findViewById(R.id.menuToListViewRh)).getPoint();
            }
        };
        targetWahl = new ViewTarget(R.id.tvRoutenwahl, this);
        targetPoi = new ViewTarget(R.id.ImgBtnPoi, this);
        targetLocation = new ViewTarget(R.id.ImgBtnLocation, this);
        targetFullscreen = new ViewTarget(R.id.ImgBtnFullscreenMap, this);
        //create the tutorialscreen
        showcaseView = new ShowcaseView.Builder(this)
                .setTarget(targetTutorial).setOnClickListener(this)
                .setContentTitle(Tutorial[2]).setContentText(Tutorial[3])
                .setStyle(R.style.ShowCaseViewStyle).blockAllTouches()
                .withMaterialShowcase().build();
        showcaseView.setButtonText(Tutorial[0]);
        showcaseView.show();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        mGoogleApiClient.disconnect();
    }

    // This method will be called when a MessageEvent is posted
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        if (event.message.equals("reload")) {
            this.recreate();
        }
    }

    @Override
    public void onClick(View v) {
        if(FragmentRoutentyp.flagLauf){
            switch (tutorialNext) {
                case 0:
                    showcaseView.setShowcase(targetWahl, true);
                    showcaseView.setContentTitle(Tutorial[7]);
                    showcaseView.setContentText(Tutorial[8]);
                    break;
                case 1:
                    showcaseView.setShowcase(targetFullscreen, true);
                    showcaseView.setContentTitle(Tutorial[9]);
                    showcaseView.setContentText(Tutorial[10]);
                    break;
                case 2:
                    showcaseView.setShowcase(targetLocation, true);
                    showcaseView.setContentTitle(Tutorial[19]);
                    showcaseView.setContentText(Tutorial[20]);
                    showcaseView.setButtonText(Tutorial[1]);
                    break;
                case 3:
                    showcaseView.hide();
                    tutorialNext = -1;
                    break;
            }
            tutorialNext++;
        } else {
            switch (tutorialNext) {
                case 0:
                    showcaseView.setShowcase(targetList, true);
                    showcaseView.setContentTitle(Tutorial[4]);
                    showcaseView.setContentText(Tutorial[5]);
                    break;
                case 1:
                    showcaseView.setShowcase(targetWahl, true);
                    showcaseView.setContentTitle(Tutorial[7]);
                    showcaseView.setContentText(Tutorial[8]);
                    break;
                case 2:
                    showcaseView.setShowcase(targetPoi, true);
                    showcaseView.setContentTitle(Tutorial[4]);
                    showcaseView.setContentText(Tutorial[6]);
                    break;
                case 3:
                    showcaseView.setShowcase(targetFullscreen, true);
                    showcaseView.setContentTitle(Tutorial[9]);
                    showcaseView.setContentText(Tutorial[10]);
                    break;
                case 4:
                    showcaseView.setShowcase(targetLocation, true);
                    showcaseView.setContentTitle(Tutorial[19]);
                    showcaseView.setContentText(Tutorial[20]);
                    showcaseView.setButtonText(Tutorial[1]);
                    break;
                case 5:
                    showcaseView.hide();
                    tutorialNext = -1;
                    break;
            }
            tutorialNext++;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if(FragmentRoutentyp.flagLauf){
            getMenuInflater().inflate(R.menu.menu_rh_kellerlauf, menu);
        } else {
            getMenuInflater().inflate(R.menu.menu_rh, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.menuStartTutorialRh:
                createShowcaseView();
                svRouten.fullScroll(ScrollView.FOCUS_UP);
                return true;
            case R.id.menuToListViewRh:
                toListViewPoi();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,@NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                //if the user accepted the permission -> do the stuff
                if (grantResults.length > 0 && grantResults[0]
                        == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(this, this.getClass());
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in,
                            android.R.anim.fade_out);
                    finish();
                    overridePendingTransition(android.R.anim.fade_in,
                            android.R.anim.fade_out);
                } else {
                    //if user denys the dialog -> explain why the app needs the permission
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                        if (permissionExplanation.getBoolean("firstDeny", true)) {
                            AlertDialog.Builder denyDialog = new AlertDialog.Builder(this);
                            denyDialog.setTitle(getResources().getString(R.string.attention));
                            denyDialog.setMessage(getResources().getString(R.string.explanationList));
                            denyDialog.setIcon(android.R.drawable.ic_dialog_map);
                            denyDialog.show();
                            permissionExplanation.edit().putBoolean("firstDeny", false).apply();
                        }
                    } else {
                        //if the user do not want to see the dialog again -> show warning
                        if (permissionExplanation.getBoolean("firstSetting", true)) {
                            AlertDialog.Builder db = new AlertDialog.Builder(this);
                            db.setTitle(getResources().getString(R.string.attention));
                            db.setMessage(getResources().getString(R.string.explanationNever));
                            db.setIcon(android.R.drawable.ic_dialog_map);
                            db.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    final Intent i = new Intent();
                                    i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    i.addCategory(Intent.CATEGORY_DEFAULT);
                                    i.setData(Uri.parse("package:" + getPackageName()));
                                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                    i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                    startActivityForResult(i, 0);
                                }
                            });
                            db.show();
                            permissionExplanation.edit().putBoolean("firstSetting", false).apply();
                        }
                    }
                }
            }
        }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        lastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}

