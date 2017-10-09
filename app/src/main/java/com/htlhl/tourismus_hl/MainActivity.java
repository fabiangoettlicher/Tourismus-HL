package com.htlhl.tourismus_hl;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Locale;


public class MainActivity extends AppCompatActivity {
    private static final String URL_POI_XML = "http://www.hollabrunn-tourismus.at/poi.xml";
    private static final String URL_ROUTEN_XML = "http://www.hollabrunn-tourismus.at/routen.xml";
    private static final String URL_ROUTENPOI_XML = "http://www.hollabrunn-tourismus.at/routenpoi.xml";

    public static String activeLang;
    public static Boolean downloading;

    private Context context;
    private SharedPreferences afterDestroy;
    private FloatingActionButton fabItem1, fabItem2;
    private FloatingActionMenu fabMenu;
    private String stringLocale;
    private Menu menuMain = null;

    protected void onPause() {
        super.onPause();
        if (fabMenu.isOpened()) {
            fabMenu.close(true);
        }
        //create sharedPreferences
        SharedPreferences language = getSharedPreferences("pref", 0);
        SharedPreferences.Editor lang_editor = language.edit();
        lang_editor.apply();
        lang_editor.putString("ActiveLang", activeLang);
        lang_editor.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //open SharedPreferences for language
        SharedPreferences language = getSharedPreferences("pref", 0);
        //detect active Language
        if (language.getString("ActiveLang", "").equals("ger")) {
            fabMenu.getMenuIconView().setImageResource(R.drawable.aut_flag_round);
        } else if (language.getString("ActiveLang", "").equals("bri")) {
            fabMenu.getMenuIconView().setImageResource(R.drawable.bri_flag_round);
        } else if (language.getString("ActiveLang", "").equals("cze")) {
            fabMenu.getMenuIconView().setImageResource(R.drawable.cze_flag_round);
        }
        activeLang = language.getString("ActiveLang", "");
        if (menuMain != null) {
            if (downloading) {
                menuMain.getItem(0).setIcon(R.drawable.ic_down_green_18dp);
            } else {
                menuMain.getItem(0).setIcon(R.drawable.ic_done_green_18dp);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        afterDestroy.edit().putBoolean("AfterDestroy", true).apply(); //Abspeichern dass die App beendet wurde
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createToolbar(R.id.toolbarMain, R.color.KG_green);
        fabMenu = (FloatingActionMenu) findViewById(R.id.lang_fab);
        fabItem1 = (FloatingActionButton) findViewById(R.id.lang_item_1);
        fabItem2 = (FloatingActionButton) findViewById(R.id.lang_item_2);
        context = this;
        afterDestroy = getSharedPreferences("pref", 0);
        SharedPreferences firstStart = getSharedPreferences("pref", 0);
        if (firstStart.getBoolean("FirstStart", true)) { //wenn die App das erste mal Startet (nach Installation)
            firstStart.edit().putString("ActiveLang", "ger").apply();
            firstStart.edit().putBoolean("FirstStart", false).apply();
            SharedPreferences firstDownload = getSharedPreferences("pref", 0);
            firstDownload.edit().putBoolean("FirstDownload", true).apply();
        } else {
            SharedPreferences saveLang = getSharedPreferences("pref", 0);
            if (afterDestroy.getBoolean("AfterDestroy", true)) { //wenn die App vor diesem Start beendet wurde
                setLocale(saveLang.getString("Locale", "")); //Sprache auf zuvor gespeicherte Stellen
                afterDestroy.edit().putBoolean("AfterDestroy", false).apply(); //app wird nicht beendet
            }
        }
        if (ReadDataFromFile.getDbPoiXmlContainerList(this) == null && downloading) {
            if (firstStart.getBoolean("FirstStart3", true)) {
                firstStart.edit().putBoolean("FirstStart3", false).apply();
                AlertDialog.Builder db = new AlertDialog.Builder(this);
                db.setTitle(getResources().getString(R.string.attentionEnglisch));
                db.setMessage(getResources().getString(R.string.waitDownloadingBackground));
                db.setIcon(android.R.drawable.ic_dialog_alert);
                db.show();
            }
        }

        RelativeLayout rlMain = (RelativeLayout) findViewById(R.id.relativeLayoutmain);
        rlMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fabMenu.isOpened()) {
                    fabMenu.close(true);
                }
            }
        });

        //listen to clicks on FAB
        fabMenu.setOnMenuButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fabMenu.isOpened()) {
                    fabMenu.close(true);
                } else {
                    //detect current language
                    SharedPreferences language = getSharedPreferences("pref", 0);
                    if (language.getString("ActiveLang", "").equals("ger")) {
                        //set selectable languages
                        fabItem1.setImageResource(R.drawable.bri_flag_mini);
                        fabItem1.setTag("bri");
                        fabItem2.setImageResource(R.drawable.cze_flag_mini);
                        fabItem2.setTag("cze");
                    } else if (language.getString("ActiveLang", "").equals("bri")) {
                        fabItem1.setImageResource(R.drawable.cze_flag_mini);
                        fabItem1.setTag("cze");
                        fabItem2.setImageResource(R.drawable.aut_flag_mini);
                        fabItem2.setTag("ger");
                    } else if (language.getString("ActiveLang", "").equals("cze")) {
                        fabItem1.setImageResource(R.drawable.aut_flag_mini);
                        fabItem1.setTag("ger");
                        fabItem2.setImageResource(R.drawable.bri_flag_mini);
                        fabItem2.setTag("bri");
                    }
                    fabMenu.open(true);
                }
            }
        });
        //listen to clicks on menuItems
        fabItem1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //detect selected language
                if (fabItem1.getTag().equals("ger")) {
                    //save language and start method to change language
                    MainActivity.activeLang = "ger";
                    stringLocale = "de";
                    setLocaleClick(stringLocale);
                } else if (fabItem1.getTag().equals("bri")) {
                    MainActivity.activeLang = "bri";
                    stringLocale = "en";
                    setLocaleClick(stringLocale);
                } else if (fabItem1.getTag().equals("cze")) {
                    MainActivity.activeLang = "cze";
                    stringLocale = "cs";
                    setLocaleClick(stringLocale);
                }
            }
        });
        fabItem2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fabItem2.getTag().equals("ger")) {
                    MainActivity.activeLang = "ger";
                    stringLocale = "de";
                    setLocaleClick(stringLocale);
                } else if (fabItem2.getTag().equals("bri")) {
                    MainActivity.activeLang = "bri";
                    stringLocale = "en";
                    setLocaleClick(stringLocale);
                } else if (fabItem2.getTag().equals("cze")) {
                    MainActivity.activeLang = "cze";
                    stringLocale = "cs";
                    setLocaleClick(stringLocale);
                }
            }
        });

        Button btnImpressum = (Button) findViewById(R.id.btnToImpressum);
        btnImpressum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, Impressum.class);
                startActivity(intent);
            }
        });

    }

    private void createToolbar(int toolbarID, int color) {
        //toolbar with Backbutton
        Toolbar toolbar = (Toolbar) findViewById(toolbarID);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false); //do not show title
    }

    public void toRouten(View view) {
        Intent intent = new Intent(MainActivity.this, RoutenInfoscreen.class);
        startActivity(intent);
    }

    public void toKellerkatze(View view) {
        Intent intent = new Intent(MainActivity.this, KellerkatzeInfoscreen.class);
        startActivity(intent);
    }

    public void setLocaleClick(String lang) {
        //create new locale with language-string
        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
        //save local for next appstart
        SharedPreferences saveLang = getSharedPreferences("pref", 0);
        saveLang.edit().putString("Locale", lang).apply();
        //refresh current activity to update language
        Intent refresh = new Intent(this, MainActivity.class);
        startActivity(refresh);
        //use animations to avoid bugs
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    public void setLocale(String lang) {
        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
        SharedPreferences saveLang = getSharedPreferences("pref", 0);
        saveLang.edit().putString("Locale", lang).apply();
        Intent refresh = new Intent(this, MainActivity.class);
        startActivity(refresh);
        finish();
    }

    public static class UrlParams {
        String urlPoiXml, urlRoutenXml, urlRoutenPoiXml;
        Context contextMain;

        UrlParams(String urlPoiXml, String urlRoutenXml, String urlRoutenPoiXml, Context contextMain) {
            this.urlPoiXml = urlPoiXml;
            this.urlRoutenXml = urlRoutenXml;
            this.urlRoutenPoiXml = urlRoutenPoiXml;
            this.contextMain = contextMain;
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
            downloading=false;
            this.recreate();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menuMain = menu;
        if (downloading == null) return false;
        if (downloading) {
            menu.getItem(0).setIcon(R.drawable.ic_down_green_18dp);
        } else {
            menu.getItem(0).setIcon(R.drawable.ic_done_green_18dp);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!downloading) {
            AlertDialog.Builder db = new AlertDialog.Builder(this);
            db.setTitle(getResources().getString(R.string.attention));
            db.setMessage(getResources().getString(R.string.down_again));
            db.setCancelable(false);
            db.setIcon(android.R.drawable.ic_dialog_alert);
            db.setPositiveButton(getResources().getString(R.string.alert_ja), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    MainActivity.UrlParams urlParams = new MainActivity.UrlParams(
                            URL_POI_XML, URL_ROUTEN_XML, URL_ROUTENPOI_XML, context);
                    DbDownloadTask task = new DbDownloadTask(urlParams);
                    task.execute();
                    downloading = true;
                    MainActivity.this.recreate();
                }
            });
            db.setNegativeButton(getResources().getString(R.string.alert_nein), new
                    DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            db.show();
        } else {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.laedt), Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }
}
