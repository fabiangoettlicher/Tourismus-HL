package com.htlhl.tourismus_hl_old;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.htlhl.tourismus_hl_old.Data.Local.ReadDataFromFile;
import com.htlhl.tourismus_hl_old.Data.Model.PointOfInterest;
import com.htlhl.tourismus_hl_old.Data.Model.Routen;
import com.htlhl.tourismus_hl_old.Data.Model.RoutenPointOfInterestLinking;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RoutenMapFullscreen extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final boolean AUTO_HIDE = true;
    private static final int AUTO_HIDE_DELAY_MILLIS = 2000;
    private static final int UI_ANIMATION_DELAY = 1000;
    private static final int LOCATION_LISTENER_RATE_MILLS = 5000;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION_CLICK = 15;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION_MAPREADY = 25;
    static final float COORDINATE_OFFSET = 0.00008f;

    public static boolean mapWasFullscreen = false, markersVisibleFull = false, showLocation = false, shouldShowLocation = false;
    private static List<LatLng> newPositionsFull;

    private final Handler mHideHandler = new Handler();
    private ImageButton imgBtnPoi, imgBtnLocation;
    private String loggedRoute, pathKml;
    private List<Routen> routenList;
    private List<PointOfInterest> PointOfInterestList;
    private List<RoutenPointOfInterestLinking> routenPointOfInterestLinkingList;
    private View mContentView;
    private GoogleMap mapFullscreen = null;
    private MapFragment mapFragment;
    private Boolean isMapInit = false, permissionGranted = false;
    private int routenID = -1;
    private List<Integer> poiIDs, poiKatIDs, routenPoiIDsPoi;
    private List<String> poiNamen, poiLats, poiLngs;
    private Marker markerStart, markerEnd, markerStartEnd, currLocationMarker;
    private List<Marker> markers;
    private GoogleApiClient mGoogleApiClient;
    private LatLng currPositon;
    private SharedPreferences permissionExplanation;


    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        delayedHide(100);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routen_map_fullscreen);

        newPositionsFull = new ArrayList<>();
        permissionExplanation = getSharedPreferences("pref", 0);
        mapWasFullscreen = true;
        poiIDs = new ArrayList<>();
        poiKatIDs = new ArrayList<>();
        poiLats = new ArrayList<>();
        poiLngs = new ArrayList<>();
        poiNamen = new ArrayList<>();
        markers = new ArrayList<>();
        routenPoiIDsPoi = new ArrayList<>();
        mVisible = true;
        mContentView = findViewById(R.id.fullscreen_content);
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });
        mContentView.setOnTouchListener(mDelayHideTouchListener);
        mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.fullscreen_content);
        mapFragment.getMapAsync(this);
        ImageButton imgBtnFullscreen = (ImageButton) findViewById(R.id.ImgBtnFullscreenFull);
        imgBtnPoi = (ImageButton) findViewById(R.id.ImgBtnPoiFull);
        //set ImgButtonPoi and ImgBtnFullscreen
        if (!RoutenHaupt.markersVisible) {
            imgBtnPoi.setImageResource(R.drawable.btn_show_poi);
            imgBtnPoi.setTag(R.drawable.btn_show_poi);
        } else {
            imgBtnPoi.setImageResource(R.drawable.btn_hide_poi);
            imgBtnPoi.setTag(R.drawable.btn_hide_poi);
        }
        imgBtnPoi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!markersVisibleFull) {
                    imgBtnPoi.setImageResource(R.drawable.btn_hide_poi);
                    createMarkers();
                    createStartZielMarkers();
                } else {
                    imgBtnPoi.setImageResource(R.drawable.btn_show_poi);
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
                markersVisibleFull = !markersVisibleFull;
            }
        });

        imgBtnFullscreen.setImageResource(R.drawable.btn_exit_fullscreen_map);
        imgBtnFullscreen.setTag(R.drawable.btn_exit_fullscreen_map);
        imgBtnFullscreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                showLocation = false;
            }
        });

        imgBtnLocation = (ImageButton) findViewById(R.id.ImgBtnLocationFull);
        if (showLocation) {
            imgBtnLocation.setImageResource(R.drawable.btn_hide_location);
        } else {
            imgBtnLocation.setImageResource(R.drawable.btn_show_location);
        }
        imgBtnLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (showLocation) {
                    imgBtnLocation.setImageResource(R.drawable.btn_show_location);
                    mapFullscreen.setMyLocationEnabled(false);
                } else {
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        permissionGranted = false;
                        ActivityCompat.requestPermissions(RoutenMapFullscreen.this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION_CLICK);
                        return;
                    } else {
                        permissionGranted = true;
                        imgBtnLocation.setImageResource(R.drawable.btn_hide_location);
                        permissionExplanation.edit().putBoolean("firstSetting", true).apply();
                        permissionExplanation.edit().putBoolean("firstDeny", true).apply();
                    }
                    mapFullscreen.setMyLocationEnabled(true);
                }
                showLocation = !showLocation;
            }
        });

        //Listens to the Maplayout, if its changing -> visible (newLatLngBounds could find the biggest possible zoomlevel)
        findViewById(R.id.layout_fullscreen_map).getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                displayMap();
            }
        });

        loggedRoute = RoutenHaupt.tvRouten.getText().toString();
        routenList = ReadDataFromFile.getDbRoutenXmlContainerList(this);
        PointOfInterestList = ReadDataFromFile.getDbPoiXmlContainerList(this);
        routenPointOfInterestLinkingList = ReadDataFromFile.getDbRoutenPoiXmlContainerList(this);
        if(routenList ==null || PointOfInterestList ==null || routenPointOfInterestLinkingList ==null){
            PointOfInterestList = ReadDataFromFile.getDbPoiXmlContainerListText(this);
            routenList = ReadDataFromFile.getDbRoutenXmlContainerListKml(this);
            if(routenList == null){
                routenList = ReadDataFromFile.getDbRoutenXmlContainerListText(this);
            }
            routenPointOfInterestLinkingList = ReadDataFromFile.getDbRoutenPoiXmlContainerListText(this);
        }
        getData();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapFullscreen = googleMap;
        mapFullscreen.getUiSettings().setMapToolbarEnabled(false);
        mapFullscreen.getUiSettings().setMyLocationButtonEnabled(false);
        try {
            ReadDataFromFile.getKml(pathKml, googleMap, this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        displayMap();
        if (RoutenHaupt.markersVisible) {
            createMarkers();
            createStartZielMarkers();
            markersVisibleFull = true;
            imgBtnPoi.setImageResource(R.drawable.btn_hide_poi);
        }

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionGranted = false;
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION_MAPREADY);
            return;
        } else {
            permissionGranted = true;
            permissionExplanation.edit().putBoolean("firstSetting", true).apply();
            permissionExplanation.edit().putBoolean("firstDeny", true).apply();
        }
        if (showLocation) {
            mapFullscreen.setMyLocationEnabled(true);
        }
        buildGoogleApiClient();
        mGoogleApiClient.connect();
    }

    public void displayMap() {
        // Check that mMap has been initialised AND the width of the MapFragment has been set:
        if (isMapInit || mapFullscreen == null) return;
        if (mapFragment.getView().getRight() <= 0) return;
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(RoutenHaupt.bounds, RoutenHaupt.PADDING_MAP);
        mapFullscreen.moveCamera(cameraUpdate);
        isMapInit = true;
    }

    private void getData() {
        routenPoiIDsPoi.clear();
        for (int i = 0; i < routenList.size(); i++) {
            if (routenList.get(i).getRoutenName_().equals(loggedRoute)) {
                pathKml = routenList.get(i).getPathKml_();
                routenID = routenList.get(i).getRoutenID_();
            }
        }
        for (int i = 0; i < routenPointOfInterestLinkingList.size(); i++) {
            if (routenID == routenPointOfInterestLinkingList.get(i).getRoutenpoiIDrouten_()) {
                routenPoiIDsPoi.add(routenPointOfInterestLinkingList.get(i).getRoutenpoiIDpoi_());
            }
        }
        poiLats.clear();
        poiLngs.clear();
        poiNamen.clear();
        poiKatIDs.clear();
        poiIDs.clear();
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

    private void createStartZielMarkers() {
        if (!KmlCoordinates.flagNoKml) {
            if (KmlCoordinates.flagStartZiel) {
                markerStartEnd = mapFullscreen.addMarker(new MarkerOptions() //add markers to map
                        .position(KmlCoordinates.startendpoint)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_flagge_startziel)));
            } else {
                markerStart = mapFullscreen.addMarker(new MarkerOptions() //add markers to map
                        .position(KmlCoordinates.startpoint)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_flagge_start)));
                markerEnd = mapFullscreen.addMarker(new MarkerOptions() //add markers to map
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
            Boolean knownKat = true;
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
                    knownKat = false;
                    break;
            }
            if (lat != -1 && lng != -1 && !name.equals("") && kat != -1 && id != -1) {
                LatLng position = isMarkerAtPosition(new LatLng(lat, lng));
                if(position.latitude!=lat){
                    newPositionsFull.add(position);
                }
                if (knownKat) {
                    Marker marker = mapFullscreen.addMarker(new MarkerOptions() //add markers to map
                            .position(position)
                            .title(name)
                            .icon(BitmapDescriptorFactory.fromResource(icon)));
                    marker.setTag(id);
                    markers.add(marker);    //save markers in array
                } else {
                    Marker marker = mapFullscreen.addMarker(new MarkerOptions() //add markers to map
                            .position(position)
                            .title(name));
                    marker.setTag(id);
                    markers.add(marker);    //save markers in array
                }
            } else {
                System.out.println("ACHTUNG: Marker (" + poiNamen.get(i) + ") wurde nicht erstellt!");
            }
            mapFullscreen.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    Intent intent = new Intent(RoutenMapFullscreen.this, RoutenPoiInfo.class);
                    startActivity(intent);
                    RoutenPoiInfo.shownPoiId = (Integer) marker.getTag();
                }
            });
            kat = id - 1;
            lat = lng = -1;
            name = "";
        }
        newPositionsFull.clear();
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
                if(newPositionsFull.size()>0){
                    for (int x=0; x<newPositionsFull.size(); x++){
                        latMarkerPos = newPositionsFull.get(x).latitude;
                        lngMarkerPos = newPositionsFull.get(x).longitude;
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

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // is Required but not needed, because onConnected is only called if permission is granted
        }
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            currPositon = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(currPositon);
            markerOptions.visible(false);
            currLocationMarker = mapFullscreen.addMarker(markerOptions);
        }
        if(FragmentRoutentyp.flagLauf){
            imgBtnPoi.setVisibility(View.GONE);
        }
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(LOCATION_LISTENER_RATE_MILLS);
        mLocationRequest.setFastestInterval(LOCATION_LISTENER_RATE_MILLS / 2);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (showLocation) {
                    if (currLocationMarker != null) {
                        currLocationMarker.remove();
                    }
                    currPositon = new LatLng(location.getLatitude(), location.getLongitude());
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(currPositon);
                    markerOptions.visible(false);
                    currLocationMarker = mapFullscreen.addMarker(markerOptions);
                    //zoom to current position:
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(currPositon)
                            .zoom(14)
                            .bearing(location.getBearing())
                            .build();
                    mapFullscreen.animateCamera(CameraUpdateFactory
                            .newCameraPosition(cameraPosition));
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION_CLICK:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    permissionGranted = true;
                    showLocation = true;
                    Intent intent = new Intent(this, RoutenMapFullscreen.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                        permissionGranted = false;
                        if (permissionExplanation.getBoolean("firstDeny", true)) {
                            createDialogDeny();
                        }
                    } else {
                        if (permissionExplanation.getBoolean("firstSetting", true)) {
                            createDialogSettings();
                        }
                    }
                    permissionGranted = false;
                }
                break;
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION_MAPREADY:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    permissionGranted = true;
                    if(shouldShowLocation){
                        showLocation = true;
                    }
                    Intent intent = new Intent(this, RoutenMapFullscreen.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                        permissionGranted = false;
                        if (permissionExplanation.getBoolean("firstDeny", true)) {
                            createDialogDeny();
                        }
                    } else {
                        if (permissionExplanation.getBoolean("firstSetting", true)) {
                            createDialogSettings();
                        }
                    }
                    permissionGranted = false;
                }
                break;
        }
    }

    public void createDialogDeny() {
        AlertDialog.Builder denyDialog = new AlertDialog.Builder(this);
        denyDialog.setTitle(getResources().getString(R.string.attention));
        denyDialog.setMessage(getResources().getString(R.string.explanationLocation));
        denyDialog.setIcon(android.R.drawable.ic_dialog_map);
        denyDialog.show();
        permissionExplanation.edit().putBoolean("firstDeny", false).apply();
    }

    public void createDialogSettings() {
        AlertDialog.Builder db = new AlertDialog.Builder(this);
        db.setTitle(getResources().getString(R.string.attention));
        db.setMessage(getResources().getString(R.string.explanationNever));
        db.setIcon(android.R.drawable.ic_dialog_map);
        db.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(getApplicationContext(), RoutenMapFullscreen.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
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

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this, "onConnectionSuspended", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "onConnectionFailed", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        if (mGoogleApiClient != null)
            if (mGoogleApiClient.isConnected())
                mGoogleApiClient.disconnect();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    // This method will be called when a MessageEvent is posted
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        if (event.message.equals("reload")) {
            this.recreate();
        }
    }
}
