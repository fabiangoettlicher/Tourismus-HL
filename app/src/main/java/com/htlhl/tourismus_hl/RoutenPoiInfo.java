package com.htlhl.tourismus_hl;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.util.Linkify;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DecimalFormat;
import java.util.List;

public class RoutenPoiInfo extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final Integer TEXT_SIZE_CONTENT = 12;
    public static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 15;

    static Integer shownPoiId;
    static Double lat=null, lng=null;
    TextView tvEntf;

    private String name, addr, tel, email, homepage, kontakt5, oeffnung, pathBild, info;
    private int katID;
    private String[] pathLogos;
    private LocationManager locationManager = null;
    private GoogleApiClient mGoogleApiClient;
    private SharedPreferences permissionExplanation;

    private List<DbPoiXmlContainer> dbPoiXmlContainerList;

    @Override
    protected void onPause() {
        super.onPause();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routen_poi_info);
        createToolbar(R.id.toolbar_poi_info, R.color.Routen_rot);
        permissionExplanation = getSharedPreferences("pref", 0);
        pathLogos = new String[6];
        dbPoiXmlContainerList = ReadDataFromFile.getDbPoiXmlContainerList(this);
        if(dbPoiXmlContainerList==null){
            dbPoiXmlContainerList = ReadDataFromFile.getDbPoiXmlContainerListText(this);
        }

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        getData();

        tvEntf = (TextView) findViewById(R.id.tvEntfernungPoiInfo);
        TextView tvName = (TextView) findViewById(R.id.NamePoiInfo);
        tvName.setText(name);

        int icon;
        ImageView ivArt = (ImageView) findViewById(R.id.imgViewArtInfoPoi);
        Boolean flagKnownKat = true;
        switch (katID) {
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
                flagKnownKat = false;
        }
        if(flagKnownKat) {
            ivArt.setImageResource(icon);
        }

        if (pathLogos[5] != null) { //if rating available -> create View
            LinearLayout llTopPoiInfo = (LinearLayout) findViewById(R.id.llTopInfoPoi);
            LinearLayout.LayoutParams paramsBew = new LinearLayout.LayoutParams(200, 70);
            ImageView ivBewertung = new ImageView(this);
            ivBewertung.setId(View.generateViewId());
            ivBewertung.setImageBitmap(ReadDataFromFile.getBitmap(pathLogos[5]));
            paramsBew.setMarginStart(15 );
            llTopPoiInfo.addView(ivBewertung, paramsBew);
        }
        final ImageView ivBild = (ImageView) findViewById(R.id.ImgViewInfoPoi);
        if(ReadDataFromFile.getDbPoiXmlContainerList(this)==null){
            for(int i=0; i<dbPoiXmlContainerList.size(); i++){
                if(dbPoiXmlContainerList.get(i).getPoiID_()==shownPoiId){
                    String url = dbPoiXmlContainerList.get(i).getPoiBild_();
                    GetBitmapOnline task = new GetBitmapOnline(url, new GetBitmapOnline.AsyncResponse() {
                        @Override
                        public void processFinish(Bitmap bitmap) {
                            ivBild.setImageBitmap(bitmap);
                        }
                    });
                    task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
        } else {
            ivBild.setImageBitmap(ReadDataFromFile.getBitmap(pathBild));
        }

        creatKontakt();

        if (!oeffnung.equals("")) {
            TextView tvOeffnung = (TextView) findViewById(R.id.tvContentOeffnungszeitenPoiInfo);
            tvOeffnung.setText(oeffnung);
        } else { //Wenn es keine Oeffnungszeiten gibt
            RelativeLayout layoutOeffnung = (RelativeLayout) findViewById(R.id.layout_oeffnung);
            layoutOeffnung.setVisibility(View.GONE); //Layout nicht anzeigen
        }

        if(!info.equals("")){
            TextView tvInfo = (TextView) findViewById(R.id.tvContentInfoPoiInfo);
            tvInfo.setText(info);
        } else {
            RelativeLayout layoutInfo = (RelativeLayout) findViewById(R.id.layout_info_poi);
            layoutInfo.setVisibility(View.GONE);
        }

        createLogos();
    }

    private void createToolbar(int toolbarID, int color) {
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getData() {
        SharedPreferences language = getSharedPreferences("pref", 0);
        for (int i = 0; i < dbPoiXmlContainerList.size(); i++) {
            if (dbPoiXmlContainerList.get(i).getPoiID_() == shownPoiId) {
                name = dbPoiXmlContainerList.get(i).getPoiName_();
                katID = dbPoiXmlContainerList.get(i).getPoiKatID_();
                if (!dbPoiXmlContainerList.get(i).getPoiLat_().equals("") || !dbPoiXmlContainerList.get(i).getPoiLng_().equals("")) {
                    lat = Double.parseDouble(dbPoiXmlContainerList.get(i).getPoiLat_());
                    lng = Double.parseDouble(dbPoiXmlContainerList.get(i).getPoiLng_());
                } else {
                    lat = null;
                    lng = null;
                }
                addr = dbPoiXmlContainerList.get(i).getPoiKontak1_();
                tel = dbPoiXmlContainerList.get(i).getPoiKontak2_();
                email = dbPoiXmlContainerList.get(i).getPoiKontak3_();
                homepage = dbPoiXmlContainerList.get(i).getPoiKontak4_();
                kontakt5 = dbPoiXmlContainerList.get(i).getPoiKontak5_();
                pathBild = dbPoiXmlContainerList.get(i).getPathBild_();
                pathLogos[0] = dbPoiXmlContainerList.get(i).getPathLogo1_();
                pathLogos[1] = dbPoiXmlContainerList.get(i).getPathLogo2_();
                pathLogos[2] = dbPoiXmlContainerList.get(i).getPathLogo3_();
                pathLogos[3] = dbPoiXmlContainerList.get(i).getPathLogo4_();
                pathLogos[4] = dbPoiXmlContainerList.get(i).getPathLogo5_();
                pathLogos[5] = dbPoiXmlContainerList.get(i).getPathLogo6_();
                if (language.getString("ActiveLang", "").equals("ger")) {
                    oeffnung = dbPoiXmlContainerList.get(i).getPoiOffenDE_();
                    info = dbPoiXmlContainerList.get(i).getPoiTextDE_();
                } else if (language.getString("ActiveLang", "").equals("bri")) {
                    oeffnung = dbPoiXmlContainerList.get(i).getPoiOffenEN_();
                    info = dbPoiXmlContainerList.get(i).getPoiTextEN_();
                } else if (language.getString("ActiveLang", "").equals("cze")) {
                    oeffnung = dbPoiXmlContainerList.get(i).getPoiOffenCZ_();
                    info = dbPoiXmlContainerList.get(i).getPoiTextCZ_();
                }
            }
        }
    }

    private void creatKontakt() {
        RelativeLayout rlKontakt = (RelativeLayout) findViewById(R.id.rlKontakt);
        TextView tvAddr = new TextView(this);
        TextView tvTel = new TextView(this);
        TextView tvEmail = new TextView(this);
        TextView tvHomepage = new TextView(this);
        TextView tvKontakt5 = new TextView(this);

        if (!addr.equals("")) {
            tvAddr.setId(View.generateViewId());
            RelativeLayout.LayoutParams paramsAddr = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            paramsAddr.setMargins(0, 15, 0, 0);
            tvAddr.setTextSize(TEXT_SIZE_CONTENT);
            tvAddr.setTextColor(getResources().getColor(R.color.black));
            tvAddr.setText(addr);
            rlKontakt.addView(tvAddr, paramsAddr);
        }
        if (!tel.equals("")) {
            tvTel.setId(View.generateViewId());
            RelativeLayout.LayoutParams paramsTel = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            paramsTel.setMargins(0, 15, 0, 0);
            if (!addr.equals("")) {
                paramsTel.addRule(RelativeLayout.BELOW, tvAddr.getId());
            }
            tvTel.setTextSize(TEXT_SIZE_CONTENT);
            tvTel.setTextColor(getResources().getColor(R.color.black));
            tvTel.setText(getResources().getString(R.string.tel) + " " + tel);
            Linkify.addLinks(tvTel, Linkify.PHONE_NUMBERS);
            tvTel.setLinksClickable(true);
            rlKontakt.addView(tvTel, paramsTel);
        }
        if (!email.equals("")) {
            tvEmail.setId(View.generateViewId());
            RelativeLayout.LayoutParams paramsEmail = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            paramsEmail.setMargins(0, 15, 0, 0);
            if (!tel.equals("")) {
                paramsEmail.addRule(RelativeLayout.BELOW, tvTel.getId());
            } else if (!addr.equals("")) {
                paramsEmail.addRule(RelativeLayout.BELOW, tvAddr.getId());
            }
            tvEmail.setTextSize(TEXT_SIZE_CONTENT);
            tvEmail.setTextColor(getResources().getColor(R.color.black));
            tvEmail.setText(getResources().getString(R.string.email) + " " + email);
            Linkify.addLinks(tvEmail, Linkify.EMAIL_ADDRESSES);
            tvEmail.setLinksClickable(true);
            rlKontakt.addView(tvEmail, paramsEmail);
        }
        if (!homepage.equals("")) {
            tvHomepage.setId(View.generateViewId());
            RelativeLayout.LayoutParams paramsHomepage = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            paramsHomepage.setMargins(0, 15, 0, 0);
            if (!email.equals("")) {
                paramsHomepage.addRule(RelativeLayout.BELOW, tvEmail.getId());
            } else if (!tel.equals("")) {
                paramsHomepage.addRule(RelativeLayout.BELOW, tvTel.getId());
            } else if (!addr.equals("")) {
                paramsHomepage.addRule(RelativeLayout.BELOW, tvAddr.getId());
            }
            tvHomepage.setTextSize(TEXT_SIZE_CONTENT);
            tvHomepage.setTextColor(getResources().getColor(R.color.black));
            tvHomepage.setText(homepage);
            Linkify.addLinks(tvHomepage, Linkify.WEB_URLS);
            tvHomepage.setLinksClickable(true);
            rlKontakt.addView(tvHomepage, paramsHomepage);
        }
        if (kontakt5 != null) {
            //if row 5 exists -> create layout
            tvKontakt5.setId(View.generateViewId());
            RelativeLayout.LayoutParams paramsKontakt5 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            paramsKontakt5.setMargins(0, 15, 0, 0);
            //if row 4 exists, place row 5 below row 4
            if (!homepage.equals("")) {
                paramsKontakt5.addRule(RelativeLayout.BELOW, tvHomepage.getId());
            //if row 4 dows not exists but row 3 exists, place row 5 below row 3
            } else if (!email.equals("")) {
                paramsKontakt5.addRule(RelativeLayout.BELOW, tvEmail.getId());
            } else if (!tel.equals("")) {
                paramsKontakt5.addRule(RelativeLayout.BELOW, tvTel.getId());
            } else if (!addr.equals("")) {
                paramsKontakt5.addRule(RelativeLayout.BELOW, tvAddr.getId());
            }
            tvKontakt5.setTextSize(TEXT_SIZE_CONTENT);
            tvKontakt5.setTextColor(getResources().getColor(R.color.black));
            tvKontakt5.setText(kontakt5);
            //create a clickable link
            Linkify.addLinks(tvKontakt5, Linkify.ALL);
            tvHomepage.setLinksClickable(true);
            rlKontakt.addView(tvKontakt5, paramsKontakt5);
        }
    }

    private void createLogos() {
        LinearLayout llLogos1 = (LinearLayout) findViewById(R.id.layout_logos_1);
        LinearLayout llLogos2 = (LinearLayout) findViewById(R.id.layout_logos_2);
        LinearLayout llLogos3 = (LinearLayout) findViewById(R.id.layout_logos_3);
        ImageView ivLogo1 = new ImageView(this);
        ImageView ivLogo2 = new ImageView(this);
        ImageView ivLogo3 = new ImageView(this);
        ImageView ivLogo4 = new ImageView(this);
        ImageView ivLogo5 = new ImageView(this);
        Bitmap bmLogo1, bmLogo2, bmLogo3, bmLogo4, bmLogo5;
        bmLogo1 = ReadDataFromFile.getBitmap(pathLogos[0]);
        bmLogo2 = ReadDataFromFile.getBitmap(pathLogos[1]);
        bmLogo3 = ReadDataFromFile.getBitmap(pathLogos[2]);
        bmLogo4 = ReadDataFromFile.getBitmap(pathLogos[3]);
        bmLogo5 = ReadDataFromFile.getBitmap(pathLogos[4]);
        if (pathLogos[0] != null) {
            ivLogo1.setId(View.generateViewId());
            LinearLayout.LayoutParams paramsLogo1 = new LinearLayout.LayoutParams(0,
                    250);
            ivLogo1.setImageBitmap(bmLogo1);
            paramsLogo1.setMargins(10, 0, 10, 0);
            paramsLogo1.weight = 1;
            llLogos1.addView(ivLogo1, paramsLogo1);
        }
        if (pathLogos[1] != null) {
            ivLogo2.setId(View.generateViewId());
            LinearLayout.LayoutParams paramsLogo2 = new LinearLayout.LayoutParams(0,
                    250);
            ivLogo2.setImageBitmap(bmLogo2);
            paramsLogo2.setMargins(10, 0, 10, 0);
            paramsLogo2.weight = 1;
            llLogos1.addView(ivLogo2, paramsLogo2);
        }
        if (pathLogos[2] != null) {
            ivLogo3.setId(View.generateViewId());
            LinearLayout.LayoutParams paramsLogo3 = new LinearLayout.LayoutParams(0,
                    250);
            ivLogo3.setImageBitmap(bmLogo3);
            paramsLogo3.setMargins(10, 0, 10, 0);
            paramsLogo3.weight = 1;
            if (llLogos1.getChildCount() < 2) {
                llLogos1.addView(ivLogo3, paramsLogo3);
            } else {
                llLogos2.addView(ivLogo3, paramsLogo3);
            }
        }
        if (pathLogos[3] != null) {
            ivLogo4.setId(View.generateViewId());
            LinearLayout.LayoutParams paramsLogo4 = new LinearLayout.LayoutParams(0,
                    250);
            ivLogo4.setImageBitmap(bmLogo4);
            paramsLogo4.setMargins(10, 0, 10, 0);
            paramsLogo4.weight = 1;
            if (llLogos1.getChildCount() < 2) {
                llLogos1.addView(ivLogo4, paramsLogo4);
            } else {
                llLogos2.addView(ivLogo4, paramsLogo4);
            }
        }
        if (pathLogos[4] != null) {
            ivLogo5.setId(View.generateViewId());
            LinearLayout.LayoutParams paramsLogo5 = new LinearLayout.LayoutParams(0,
                    250);
            ivLogo5.setImageBitmap(bmLogo5);
            paramsLogo5.setMargins(10, 0, 10, 0);
            paramsLogo5.weight = 1;
            if (llLogos1.getChildCount() < 2) {
                llLogos1.addView(ivLogo5, paramsLogo5);
            } else if (llLogos2.getChildCount() < 2) {
                llLogos2.addView(ivLogo5, paramsLogo5);
            } else {
                llLogos3.addView(ivLogo5, paramsLogo5);
            }
        }
    }

    public void navigateToPoi(View view){
        if(lat!=null && lng!=null) {
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + lat + "," + lng);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
        } else {
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + addr);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
       }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStart();
        EventBus.getDefault().unregister(this);
        mGoogleApiClient.disconnect();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        if (event.message.equals("reload")) {
            this.recreate();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            return;
        } else {
            permissionExplanation.edit().putBoolean("firstSetting", true).apply();
            permissionExplanation.edit().putBoolean("firstDeny", true).apply();
        }
        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if(lastLocation != null && lat != null && lng != null){
            Location poiLocation = new Location("");
            poiLocation.setLatitude(lat);
            poiLocation.setLongitude(lng);
            float floatDistanceM = lastLocation.distanceTo(poiLocation);
            float floatDistanceKM = floatDistanceM / 1000;
            String stringDistanceM = new DecimalFormat("##").format(floatDistanceM) + " m";
            String stringDistanceKM = new DecimalFormat("###.#").format(floatDistanceKM) + " km";
            TextView tvEntf = (TextView) findViewById(R.id.tvEntfernungPoiInfo);
            if (floatDistanceM < 100) {
                tvEntf.setText(stringDistanceM);
            } else {
                tvEntf.setText(stringDistanceKM);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(this, this.getClass());
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                        if(permissionExplanation.getBoolean("firstDeny", true)) {
                            AlertDialog.Builder denyDialog = new AlertDialog.Builder(this);
                            denyDialog.setTitle(getResources().getString(R.string.attention));
                            denyDialog.setMessage(getResources().getString(R.string.explanationLocation));
                            denyDialog.setIcon(android.R.drawable.ic_dialog_map);
                            denyDialog.show();
                            permissionExplanation.edit().putBoolean("firstDeny", false).apply();
                        }
                    } else {
                        if(permissionExplanation.getBoolean("firstSetting", true)) {
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
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}