package com.htlhl.tourismus_hl;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.htlhl.tourismus_hl.Data.Local.ReadDataFromFile;
import com.htlhl.tourismus_hl.Data.Model.PointOfInterest;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

public class KellerkatzeInfoscreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kellerkatze_infoscreen);
        createToolbar(R.id.toolbar_info_kg, R.color.KG_green);

        List<PointOfInterest> PointOfInterestList = ReadDataFromFile.getDbPoiXmlContainerList(this);
        if(PointOfInterestList ==null){
            PointOfInterestList = ReadDataFromFile.getDbPoiXmlContainerListText(this);
        }
        String welcomeText="", headerText="";
        SharedPreferences language = getSharedPreferences("pref", 0);
        for(int i = 0; i< PointOfInterestList.size(); i++){
            if(PointOfInterestList.get(i).getPoiID_() == 63){
                headerText = PointOfInterestList.get(i).getPoiName_();
                if (language.getString("ActiveLang", "").equals("ger")) {
                    welcomeText = PointOfInterestList.get(i).getPoiTextDE_();
                } else if (language.getString("ActiveLang", "").equals("bri")) {
                    welcomeText = PointOfInterestList.get(i).getPoiTextEN_();
                } else if (language.getString("ActiveLang", "").equals("cze")) {
                    welcomeText = PointOfInterestList.get(i).getPoiTextCZ_();
                }
            }
        }
        TextView tvWelcometext = (TextView) findViewById(R.id.tv_info_kg);
        tvWelcometext.setText(welcomeText);
        TextView tvHeaderKGInfo = (TextView) findViewById(R.id.tvHeaderKGInfo);
        tvHeaderKGInfo.setText(headerText);
        Button btnStartKG = (Button) findViewById(R.id.btnStartKG);

        btnStartKG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(KellerkatzeInfoscreen.this, KellerkatzeHaupt.class);
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
    public void onMessageEvent(MessageEvent event){
        if(event.message.equals("reload")) {
            this.recreate();
        }
    }
}
