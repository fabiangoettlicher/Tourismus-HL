package com.htlhl.tourismus_hl;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

public class SplashActivity extends AppCompatActivity implements DbDownloadTask.LoadingTaskFinishedListener {

    private static final String URL_POI_XML = "http://www.hollabrunn-tourismus.at/poi.xml";
    private static final String URL_ROUTEN_XML = "http://www.hollabrunn-tourismus.at/routen.xml";
    private static final String URL_ROUTENPOI_XML = "http://www.hollabrunn-tourismus.at/routenpoi.xml";
    private Boolean flagDownload  = false;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        context = this;
        List<DbPoiXmlContainer> dbPoiXmlContainerList = ReadDataFromFile.getDbPoiXmlContainerList(this);
        List<DbRoutenXmlContainer> dbRoutenXmlContainerList = ReadDataFromFile.getDbRoutenXmlContainerList(this);
        List<DbRoutenPoiXmlContainer> dbRoutenPoiXmlContainerList = ReadDataFromFile.getDbRoutenPoiXmlContainerList(this);
        ProgressBar splashProgress = (ProgressBar) findViewById(R.id.splashProgress);
        splashProgress.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.KG_orange), android.graphics.PorterDuff.Mode.MULTIPLY);
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        //lokal Databasefiles are not available
        if (dbPoiXmlContainerList == null || dbRoutenXmlContainerList == null ||
                dbRoutenPoiXmlContainerList == null) {
            if (mWifi.isConnected()) { //check if WIFI is connected
                //start downloading
                MainActivity.UrlParams urlParams = new MainActivity.UrlParams(
                        URL_POI_XML, URL_ROUTEN_XML, URL_ROUTENPOI_XML, context);
                DbDownloadTask task = new DbDownloadTask(urlParams, this);
                task.execute();
                MainActivity.downloading=true;
                flagDownload = false;
            } else {
                //if WIFI is not connectet -> warn user of larg data
                AlertDialog.Builder db = new AlertDialog.Builder(this);
                db.setTitle(getResources().getString(R.string.attentionEnglisch));
                db.setMessage(getResources().getString(R.string.alertWIFI));
                db.setCancelable(false);
                db.setIcon(android.R.drawable.ic_dialog_alert);
                db.setPositiveButton("WIFI", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int which) {
                        //button to WIFI-Settings to connect to WIFI
                        final Intent i = new Intent();
                        i.setAction(Settings.ACTION_WIFI_SETTINGS);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                        startActivityForResult(i, 0);
                        finish();
                        Toast.makeText(getApplicationContext(), getResources()
                                .getString(R.string.toastWifiSetting),
                                Toast.LENGTH_LONG).show();
                    }
                });
                db.setNegativeButton("Mobile Data", new
                        DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //button to accept the warning and start download via mobile data
                        MainActivity.UrlParams urlParams = new MainActivity.UrlParams(
                                URL_POI_XML, URL_ROUTEN_XML, URL_ROUTENPOI_XML,
                                SplashActivity.this);
                        DbDownloadTask task = new DbDownloadTask(urlParams,
                                SplashActivity.this);
                        task.execute();
                        MainActivity.downloading=true;
                        flagDownload = false;
                    }
                });
                db.show();
            }
        }
        //lokal Datafiles ar available
        if (dbPoiXmlContainerList !=null && dbRoutenXmlContainerList !=null
                && dbRoutenPoiXmlContainerList !=null) {
            //start download
            MainActivity.UrlParams urlParams = new MainActivity.UrlParams(
                    URL_POI_XML, URL_ROUTEN_XML, URL_ROUTENPOI_XML, context);
            DbDownloadTask task = new DbDownloadTask(urlParams, this);
            task.execute();
            MainActivity.downloading=true;
            flagDownload = true;
            findViewById(R.id.tvSplash).setVisibility(View.GONE);
            //close splashscreen after 1s and got to MainActivity
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    completeSplash();
                }
            }, 1000);
        }
    }

    // Callback when Asynktask finished
    @Override
    public void onTaskFinished() {
        if (!flagDownload) {
            EventBus.getDefault().post(new MessageEvent("reload"));
            completeSplash();
        }
    }

    private void completeSplash() {
        startApp();
        finish();
    }

    private void startApp() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
    }

}
