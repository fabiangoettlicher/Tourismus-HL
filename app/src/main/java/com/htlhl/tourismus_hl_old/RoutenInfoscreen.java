package com.htlhl.tourismus_hl_old;


import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.htlhl.tourismus_hl_old.Data.Local.ReadDataFromFile;
import com.htlhl.tourismus_hl_old.Data.Model.PointOfInterest;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

public class RoutenInfoscreen extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routen_infoscreen);
        createToolbar(R.id.toolbar_info_routen, R.color.Routen_rot);
        getData();
    }

    public void createToolbar(int toolbarID, int color){
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

    public void getData (){
        TextView tvInfo = (TextView) findViewById(R.id.tv_info_route);
        List<PointOfInterest> PointOfInterestList = ReadDataFromFile.getDbPoiXmlContainerList(this);
        if(PointOfInterestList ==null){
            PointOfInterestList = ReadDataFromFile.getDbPoiXmlContainerListText(this);
        }
        SharedPreferences language = getSharedPreferences("pref", 0);
        for(int i = 0; i< PointOfInterestList.size(); i++){
            if(PointOfInterestList.get(i).getPoiID_() == 64){ //Routeninfoseite
                if (language.getString("ActiveLang", "").equals("ger")) {
                    tvInfo.setText(PointOfInterestList.get(i).getPoiTextDE_());
                } else if (language.getString("ActiveLang", "").equals("bri")) {
                    tvInfo.setText(PointOfInterestList.get(i).getPoiTextEN_());
                } else if (language.getString("ActiveLang", "").equals("cze")) {
                    tvInfo.setText(PointOfInterestList.get(i).getPoiTextCZ_());
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
    public void onMessageEvent(MessageEvent event){
        if(event.message.equals("reload")) {
            this.recreate();
        }
    }
}
