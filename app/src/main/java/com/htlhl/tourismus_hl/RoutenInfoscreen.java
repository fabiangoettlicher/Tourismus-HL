package com.htlhl.tourismus_hl;


import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.widget.TextView;

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
        List<DbPoiXmlContainer> dbPoiXmlContainerList = ReadDataFromFile.getDbPoiXmlContainerList(this);
        if(dbPoiXmlContainerList==null){
            dbPoiXmlContainerList = ReadDataFromFile.getDbPoiXmlContainerListText(this);
        }
        SharedPreferences language = getSharedPreferences("pref", 0);
        for(int i=0; i<dbPoiXmlContainerList.size(); i++){
            if(dbPoiXmlContainerList.get(i).getPoiID_() == 64){ //Routeninfoseite
                if (language.getString("ActiveLang", "").equals("ger")) {
                    tvInfo.setText(dbPoiXmlContainerList.get(i).getPoiTextDE_());
                } else if (language.getString("ActiveLang", "").equals("bri")) {
                    tvInfo.setText(dbPoiXmlContainerList.get(i).getPoiTextEN_());
                } else if (language.getString("ActiveLang", "").equals("cze")) {
                    tvInfo.setText(dbPoiXmlContainerList.get(i).getPoiTextCZ_());
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
