package com.htlhl.tourismus_hl;

import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.vision.text.Text;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

public class Impressum extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 15;

    private List<DbPoiXmlContainer> dbPoiXmlContainerList;
    private String lat, lng, name, addr, tel, email, homepage, kontakt5, info;
    private GoogleApiClient mGoogleApiClient;
    private Boolean permissionGranted = false;
    private SharedPreferences permissionExplanation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_impressum);
        createToolbar(R.id.toolbar_impressum, R.color.KG_green);
        permissionExplanation = getSharedPreferences("pref", 0);
        dbPoiXmlContainerList = ReadDataFromFile.getDbPoiXmlContainerList(this);
        if(dbPoiXmlContainerList==null){
            dbPoiXmlContainerList = ReadDataFromFile.getDbPoiXmlContainerListText(this);
        }
        getData();

        TextView tvKontakt = (TextView) findViewById(R.id.tvKontaktImp);
        tvKontakt.setText(getResources().getString(R.string.kontakt));
        TextView tvAddr = (TextView) findViewById(R.id.tvKontakt1Imp);
        tvAddr.setText(addr);
        if(addr.equals("")){
            tvAddr.setVisibility(View.GONE);
        }
        TextView tvTel = (TextView) findViewById(R.id.tvKontakt2Imp);
        tvTel.setText(getResources().getString(R.string.tel) + " " + tel);
        if(tel.equals("")){
            tvTel.setVisibility(View.GONE);
        }
        TextView tvEmail = (TextView) findViewById(R.id.tvKontakt3Imp);
        //tvEmail.setText(Html.fromHtml("<a href=\"mailto:"+getString(R.string.email)+"?subject="+"Android:"+"\" >"+getString(R.string.email)+"</a>"));
        //tvEmail.setMovementMethod(LinkMovementMethod.getInstance());
        tvEmail.setText(getResources().getString(R.string.email) + " " + email);
        if(email.equals("")){
            tvEmail.setVisibility(View.GONE);
        }
        TextView tvHomepage = (TextView) findViewById(R.id.tvKontakt4Imp);
        tvHomepage.setText(homepage);
        if(homepage.equals("")){
            tvHomepage.setVisibility(View.GONE);
        }
        TextView tvKontakt5 = (TextView) findViewById(R.id.tvKontakt5Imp);
        tvKontakt5.setText(kontakt5);

        TextView tvInfo = (TextView) findViewById(R.id.tvInfoImpressum);
        tvInfo.setText(info);

        TextView tvProjekthomepage = (TextView) findViewById(R.id.tvKontakt6Imp);
        tvProjekthomepage.setText(getResources().getString(R.string.projecthomepage));
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

    public void getData() {
        SharedPreferences language = getSharedPreferences("pref", 0);
        for (int i = 0; i < dbPoiXmlContainerList.size(); i++) {
            if (dbPoiXmlContainerList.get(i).getPoiID_() == 66) {
                lat = dbPoiXmlContainerList.get(i).getPoiLat_();
                lng = dbPoiXmlContainerList.get(i).getPoiLng_();
                name = dbPoiXmlContainerList.get(i).getPoiName_();
                addr = dbPoiXmlContainerList.get(i).getPoiKontak1_();
                tel = dbPoiXmlContainerList.get(i).getPoiKontak2_();
                email = dbPoiXmlContainerList.get(i).getPoiKontak3_();
                homepage = dbPoiXmlContainerList.get(i).getPoiKontak4_();
                kontakt5 = dbPoiXmlContainerList.get(i).getPoiKontak5_();
                if (language.getString("ActiveLang", "").equals("ger")) {
                    info = dbPoiXmlContainerList.get(i).getPoiTextDE_();
                } else if (language.getString("ActiveLang", "").equals("bri")) {
                    info = dbPoiXmlContainerList.get(i).getPoiTextEN_();
                } else if (language.getString("ActiveLang", "").equals("cze")) {
                    info = dbPoiXmlContainerList.get(i).getPoiTextCZ_();
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
