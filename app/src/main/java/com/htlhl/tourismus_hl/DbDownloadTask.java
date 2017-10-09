package com.htlhl.tourismus_hl;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.List;


class DbDownloadTask extends AsyncTask<MainActivity.UrlParams, Integer, MainActivity.UrlParams> {
    private static final String POI_FILENAME = "PoiFile.srl";
    private static final String ROUTEN_FILENAME = "RoutenFile.srl";
    private static final String ROUTENPOI_FILENAME = "RoutenPoiFile.srl";
    private static final String POI_FILENAME_TEXT = "PoiFileText.srl";
    private static final String ROUTEN_FILENAME_TEXT = "RoutenFileText.srl";
    private static final String ROUTENPOI_FILENAME_TEXT = "RoutenPoiFileText.srl";
    private static final String ROUTEN_FILENAME_KML = "RoutenFileKml.srl";


    private LoadingTaskFinishedListener finishedListener;
    private MainActivity.UrlParams urlParams;
    private Boolean flagDownloaded = false;
    private Boolean flagError = false, flagErrorDIB = false, flagErrorNointernet = false;
    private Context context;
    private Boolean noInternet = true;

    interface LoadingTaskFinishedListener {
        void onTaskFinished(); // If you want to pass something back to the listener add a param to this method
    }

    DbDownloadTask(MainActivity.UrlParams urlParams, LoadingTaskFinishedListener finishedListener) {
        this.urlParams = urlParams;
        this.finishedListener = finishedListener;
        this.context = urlParams.contextMain;
    }

    DbDownloadTask(MainActivity.UrlParams urlParams){
        this.urlParams = urlParams;
        this.finishedListener = null;
        this.context = urlParams.contextMain;
    }

    @Override
    protected MainActivity.UrlParams doInBackground(MainActivity.UrlParams... urls) {
        try {
            return loadXmlFromNetwork(urlParams);
        } catch (IOException e) {
            if(noInternet){
                flagErrorNointernet = true;
            } else {
                flagErrorDIB = true;
                System.out.println("connection error");
            }
            return null;
        } catch (XmlPullParserException e) {
            flagErrorDIB=true;
            System.out.println("xml error");
            return null;
        }
    }

    @Override
    protected void onPostExecute(MainActivity.UrlParams result) {
        MainActivity.downloading = false;
        if(flagErrorNointernet){
            Toast.makeText(context, context.getResources().getString(R.string.errorNoInternet), Toast.LENGTH_LONG).show();
            System.out.println(context.getResources().getString(R.string.errorNoInternet));
            MainActivity.downloading = false;
        }
        if(flagErrorDIB){
            Toast.makeText(context, context.getResources().getString(R.string.errorDownloadingData), Toast.LENGTH_LONG).show();
            System.out.println(context.getResources().getString(R.string.errorDownloadingData));
            MainActivity.downloading = false;
            EventBus.getDefault().post(new MessageEvent("reload"));
        }
        if(flagDownloaded) {
            if(!flagError) {
                Toast.makeText(result.contextMain, result.contextMain.getResources().getString(R.string.datenErfolgreich), Toast.LENGTH_LONG).show();
                System.out.println(result.contextMain.getResources().getString(R.string.datenErfolgreich));
            } else {
                Toast.makeText(result.contextMain, result.contextMain.getResources().getString(R.string.errorDownloadingData), Toast.LENGTH_LONG).show();
                System.out.println(result.contextMain.getResources().getString(R.string.errorDownloadingData));
            }
            MainActivity.downloading = false;
            //Event ausgeben, damit die aktuelle Activity neu geladen wird
            EventBus.getDefault().post(new MessageEvent("reload"));
        }
        //finishedListener.onTaskFinished();
    }

    private MainActivity.UrlParams loadXmlFromNetwork(MainActivity.UrlParams urls) throws XmlPullParserException, IOException {
        InputStream inPoiXml = null, inRoutenXml = null, inRoutenPoiXml = null;
        // Instantiate the parser
        DbPoiXmlParser dbPoiXmlParser = new DbPoiXmlParser();
        DbRoutenXmlParser dbRoutenXmlParser = new DbRoutenXmlParser();
        DbRoutenPoiXmlParser dbRoutenPoiXmlParser = new DbRoutenPoiXmlParser();

        List<DbPoiXmlContainer> dbPoiXmlContainerList = null;
        List<DbRoutenXmlContainer> dbRoutenXmlContainerList = null;
        List<DbRoutenPoiXmlContainer> dbRoutenPoiXmlContainerList = null;

        String urlPoiXml = urls.urlPoiXml;
        String urlRoutenXml = urls.urlRoutenXml;
        String urlRoutenPoiXml = urls.urlRoutenPoiXml;
        try {
            //download Xml-File in Inputstream
            inPoiXml = downloadUrl(urlPoiXml);
            inRoutenXml = downloadUrl(urlRoutenXml);
            inRoutenPoiXml = downloadUrl(urlRoutenPoiXml);
            SharedPreferences dataPref = PreferenceManager.
                    getDefaultSharedPreferences(urls.contextMain);
            //get ModifiedDate of Xml-Files
            Long datePoi = getModifiedDateLong(urlPoiXml);
            Long dateRouten = getModifiedDateLong(urlRoutenXml);
            Long dateRoutenPoi = getModifiedDateLong(urlRoutenPoiXml);
            //if no Data is stored local
            if (ReadDataFromFile.getDbPoiXmlContainerList(urls.contextMain) == null ||
                    ReadDataFromFile.getDbRoutenXmlContainerList(urls.contextMain) == null ||
                    ReadDataFromFile.getDbRoutenPoiXmlContainerList(urls.contextMain) == null) {
                //safe ModifiedDate and parse Data
                dataPref.edit().putLong("datePoi", datePoi).apply();
                dbPoiXmlContainerList = dbPoiXmlParser.parse(inPoiXml);
                dataPref.edit().putLong("dateRouten", dateRouten).apply();
                dbRoutenXmlContainerList = dbRoutenXmlParser.parse(inRoutenXml);
                dataPref.edit().putLong("dateRoutenPoi", dateRoutenPoi).apply();
                dbRoutenPoiXmlContainerList = dbRoutenPoiXmlParser.parse(inRoutenPoiXml);
                //Write Lists with Data to Files on local Storage
                writePoiText(dbPoiXmlContainerList, urls.contextMain);
                writeRouteText(dbRoutenXmlContainerList, urls.contextMain);
                writeRoutePoiText(dbRoutenPoiXmlContainerList, urls.contextMain);
                if (finishedListener != null) {
                    finishedListener.onTaskFinished();
                }
            } else {
                if (finishedListener == null) {
                    dataPref.edit().putLong("datePoi", datePoi).apply();
                    dbPoiXmlContainerList = dbPoiXmlParser.parse(inPoiXml);
                    dataPref.edit().putLong("dateRouten", dateRouten).apply();
                    dbRoutenXmlContainerList = dbRoutenXmlParser.parse(inRoutenXml);
                    dataPref.edit().putLong("dateRoutenPoi", dateRoutenPoi).apply();
                    dbRoutenPoiXmlContainerList = dbRoutenPoiXmlParser.parse(inRoutenPoiXml);
                } else {
                    if (datePoi != dataPref.getLong("datePoi", -1)) {
                        dataPref.edit().putLong("datePoi", datePoi).apply();
                        dbPoiXmlContainerList = dbPoiXmlParser.parse(inPoiXml);
                        System.out.println("Poi download" + new Date(datePoi));
                    } else {
                        System.out.println("poi nicht: ");
                    }
                    if (dateRouten != dataPref.getLong("dateRouten", -1)) {
                        dataPref.edit().putLong("dateRouten", dateRouten).apply();
                        dbRoutenXmlContainerList = dbRoutenXmlParser.parse(inRoutenXml);
                        System.out.println("routen download: " + new Date(dateRouten));
                    } else {
                        System.out.println("routen nicht");
                    }
                    if (dateRoutenPoi != dataPref.getLong("dateRoutenPoi", -1)) {
                        dataPref.edit().putLong("dateRoutenPoi", dateRoutenPoi).apply();
                        dbRoutenPoiXmlContainerList = dbRoutenPoiXmlParser.parse(inRoutenPoiXml);
                        System.out.println("RoutenPoi download: " + new Date(dateRoutenPoi));
                    } else {
                        System.out.println("RoutenPoi nicht:");
                    }
                }
            }
            /*dataPref.edit().putLong("datePoi", 1).apply();
            dataPref.edit().putLong("dateRouten", 1).apply();
            dataPref.edit().putLong("dateRoutenPoi", 1).apply(); *///Simuliert update der Datenbank
        } finally {
            if (inPoiXml != null) {
                try {
                    inPoiXml.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (inRoutenXml != null) {
                try {
                    inRoutenXml.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (inRoutenPoiXml != null) {
                try {
                    inRoutenPoiXml.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if(dbPoiXmlContainerList!=null || dbRoutenPoiXmlContainerList!=null || dbRoutenXmlContainerList!=null){
            downloadKml(dbRoutenXmlContainerList, urls.contextMain);
            writeRouteKml(dbRoutenXmlContainerList, urls.contextMain);
            downloadPictures(dbRoutenXmlContainerList, dbPoiXmlContainerList, urls.contextMain);
            downloadMP3(dbPoiXmlContainerList, urls.contextMain);
            //Speichern der ArrayListen
            if(dbPoiXmlContainerList!=null) {
                writePoi(dbPoiXmlContainerList, urls.contextMain);
                System.out.println("PoiListe wird gespeichert");
            }
            if(dbRoutenXmlContainerList!=null) {
                writeRoute(dbRoutenXmlContainerList, urls.contextMain);
                System.out.println("RoutenListe wird gespeichert");
            }
            if(dbRoutenPoiXmlContainerList!=null) {
                writeRoutePoi(dbRoutenPoiXmlContainerList, urls.contextMain);
                System.out.println("RoutenPoiListe wird gespeichert");
            }
            flagDownloaded = true;
        } else {
            System.out.println("nothing to download");

       }
        return urls;
    }

    private InputStream downloadUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        //create Connection and returns InputStream
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        conn.connect();
        noInternet = false;
        return conn.getInputStream();
    }
    //returns ModifiedDate as Long Variable
    private Long getModifiedDateLong (String urlString) throws IOException{
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        return conn.getLastModified();
    }
    //returns ModifiedDate as Date Variable
    private Date getModifiedDateDate (String urlString) throws IOException{
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        long date = conn.getLastModified();
        Date Date = new Date(date);
        return Date;
    }

    private void downloadPictures(List<DbRoutenXmlContainer> dbRoutenXmlContainerList, List<DbPoiXmlContainer> dbPoiXmlContainerList, Context context) throws IOException {
        InputStream inLogo1 = null, inLogo2 = null, inLogo3 = null, inLogo4 = null, inLogo5 = null, inLogo6 = null, inBild = null;
        Bitmap bmLogo1, bmLogo2, bmLogo3, bmLogo4, bmLogo5, bmLogo6, bmBild;
        String nameLogo1=null, nameLogo2=null, nameLogo3=null, nameLogo4=null, nameLogo5=null, nameLogo6=null, nameBild=null;
        InputStream inBild1 = null, inBild2 = null, inBild3 = null;
        Bitmap bmBild1, bmBild2, bmBild3;
        String nameBild1=null, nameBild2=null, nameBild3=null;
        List<DbPoiXmlContainer> dbPoiXmlContainerListSaved = ReadDataFromFile.getDbPoiXmlContainerList(context);
        List<DbRoutenXmlContainer> dbRoutenXmlContainerListSaved = ReadDataFromFile.getDbRoutenXmlContainerList(context);
        for (int i = 0; i < dbPoiXmlContainerList.size(); i++) {
            System.out.println("picture poi" + i);
            if (!dbPoiXmlContainerList.get(i).getPoiLogo1_().isEmpty()) {
                Date dateLogo = getModifiedDateDate(dbPoiXmlContainerList.get(i).getPoiLogo1_());
                //if first download -> download picture
                if(dbPoiXmlContainerListSaved==null){
                    nameLogo1 = getNameOfFile(dbPoiXmlContainerList.get(i).getPoiLogo1_());
                    inLogo1 = downloadUrl(dbPoiXmlContainerList.get(i).getPoiLogo1_());
                } else {
                    //compare ModifiedDate
                    Date lastDateLogo = dbPoiXmlContainerListSaved.get(i).getDateLogo1_();
                    if (!dateLogo.equals(lastDateLogo)) {
                        //date is not equal -> new picture -> download it
                        System.out.println("new File -> download");
                        nameLogo1 = getNameOfFile(dbPoiXmlContainerList.get(i).getPoiLogo1_());
                        inLogo1 = downloadUrl(dbPoiXmlContainerList.get(i).getPoiLogo1_());
                    } else {
                        //date is equal -> same picture -> do not download it
                        System.out.println("old File -> do not download");
                        dbPoiXmlContainerList.get(i).setPathLogo1_(
                                dbPoiXmlContainerListSaved.get(i).getPathLogo1_());
                    }
                }
                //store new ModifiedDate in list
                dbPoiXmlContainerList.get(i).setDateLogo1_(dateLogo);
            }
            if (!dbPoiXmlContainerList.get(i).getPoiLogo2_().isEmpty()) {
                Date dateLogo = getModifiedDateDate(dbPoiXmlContainerList.get(i).getPoiLogo2_());
                if(dbPoiXmlContainerListSaved==null){
                    nameLogo2 = getNameOfFile(dbPoiXmlContainerList.get(i).getPoiLogo2_());
                    inLogo2 = downloadUrl(dbPoiXmlContainerList.get(i).getPoiLogo2_());
                } else {
                    Date lastDateLogo = dbPoiXmlContainerListSaved.get(i).getDateLogo2_();
                    if (!dateLogo.equals(lastDateLogo)) {
                        System.out.println("new File -> download");
                        nameLogo2 = getNameOfFile(dbPoiXmlContainerList.get(i).getPoiLogo2_());
                        inLogo2 = downloadUrl(dbPoiXmlContainerList.get(i).getPoiLogo2_());
                    } else {
                        System.out.println("old File -> do not download");
                        dbPoiXmlContainerList.get(i).setPathLogo2_(dbPoiXmlContainerListSaved.get(i).getPathLogo2_());
                    }
                }
                dbPoiXmlContainerList.get(i).setDateLogo2_(dateLogo);
            }
            if (!dbPoiXmlContainerList.get(i).getPoiLogo3_().isEmpty()) {
                Date dateLogo = getModifiedDateDate(dbPoiXmlContainerList.get(i).getPoiLogo3_());
                if(dbPoiXmlContainerListSaved==null){
                    nameLogo3 = getNameOfFile(dbPoiXmlContainerList.get(i).getPoiLogo3_());
                    inLogo3 = downloadUrl(dbPoiXmlContainerList.get(i).getPoiLogo3_());
                } else {
                    Date lastDateLogo = dbPoiXmlContainerListSaved.get(i).getDateLogo3_();
                    if (!dateLogo.equals(lastDateLogo)) {
                        System.out.println("new File -> download");
                        nameLogo3 = getNameOfFile(dbPoiXmlContainerList.get(i).getPoiLogo3_());
                        inLogo3 = downloadUrl(dbPoiXmlContainerList.get(i).getPoiLogo3_());
                    } else {
                        System.out.println("old File -> do not download");
                        dbPoiXmlContainerList.get(i).setPathLogo3_(dbPoiXmlContainerListSaved.get(i).getPathLogo3_());
                    }
                }
                dbPoiXmlContainerList.get(i).setDateLogo3_(dateLogo);
            }
            if (!dbPoiXmlContainerList.get(i).getPoiLogo4_().isEmpty()) {
                Date dateLogo = getModifiedDateDate(dbPoiXmlContainerList.get(i).getPoiLogo4_());
                if(dbPoiXmlContainerListSaved==null){
                    nameLogo4 = getNameOfFile(dbPoiXmlContainerList.get(i).getPoiLogo4_());
                    inLogo4 = downloadUrl(dbPoiXmlContainerList.get(i).getPoiLogo4_());
                } else {
                    Date lastDateLogo = dbPoiXmlContainerListSaved.get(i).getDateLogo4_();
                    if (!dateLogo.equals(lastDateLogo)) {
                        System.out.println("new File -> download");
                        nameLogo4 = getNameOfFile(dbPoiXmlContainerList.get(i).getPoiLogo4_());
                        inLogo4 = downloadUrl(dbPoiXmlContainerList.get(i).getPoiLogo4_());
                    } else {
                        System.out.println("old File -> do not download");
                        dbPoiXmlContainerList.get(i).setPathLogo4_(dbPoiXmlContainerListSaved.get(i).getPathLogo4_());
                    }
                }
                dbPoiXmlContainerList.get(i).setDateLogo4_(dateLogo);
            }
            if (!dbPoiXmlContainerList.get(i).getPoiLogo5_().isEmpty()) {
                Date dateLogo = getModifiedDateDate(dbPoiXmlContainerList.get(i).getPoiLogo5_());
                if(dbPoiXmlContainerListSaved==null){
                    nameLogo5 = getNameOfFile(dbPoiXmlContainerList.get(i).getPoiLogo5_());
                    inLogo5 = downloadUrl(dbPoiXmlContainerList.get(i).getPoiLogo5_());
                } else {
                    Date lastDateLogo = dbPoiXmlContainerListSaved.get(i).getDateLogo5_();
                    if (!dateLogo.equals(lastDateLogo)) {
                        System.out.println("new File -> download");
                        nameLogo5 = getNameOfFile(dbPoiXmlContainerList.get(i).getPoiLogo5_());
                        inLogo5 = downloadUrl(dbPoiXmlContainerList.get(i).getPoiLogo5_());
                    } else {
                        System.out.println("old File -> do not download");
                        dbPoiXmlContainerList.get(i).setPathLogo5_(dbPoiXmlContainerListSaved.get(i).getPathLogo5_());
                    }
                }
                dbPoiXmlContainerList.get(i).setDateLogo5_(dateLogo);
            }
            if (!dbPoiXmlContainerList.get(i).getPoiLogo6_().isEmpty()) {
                Date dateLogo = getModifiedDateDate(dbPoiXmlContainerList.get(i).getPoiLogo6_());
                if(dbPoiXmlContainerListSaved==null){
                    nameLogo6 = getNameOfFile(dbPoiXmlContainerList.get(i).getPoiLogo6_());
                    inLogo6 = downloadUrl(dbPoiXmlContainerList.get(i).getPoiLogo6_());
                } else {
                    Date lastDateLogo = dbPoiXmlContainerListSaved.get(i).getDateLogo6_();
                    if (!dateLogo.equals(lastDateLogo)) {
                        System.out.println("new File -> download");
                        nameLogo6 = getNameOfFile(dbPoiXmlContainerList.get(i).getPoiLogo6_());
                        inLogo6 = downloadUrl(dbPoiXmlContainerList.get(i).getPoiLogo6_());
                    } else {
                        System.out.println("old File -> do not download");
                        dbPoiXmlContainerList.get(i).setPathLogo6_(dbPoiXmlContainerListSaved.get(i).getPathLogo6_());
                    }
                }
                dbPoiXmlContainerList.get(i).setDateLogo6_(dateLogo);
            }
            if (!dbPoiXmlContainerList.get(i).getPoiBild_().isEmpty()) {
                Date dateBild = getModifiedDateDate(dbPoiXmlContainerList.get(i).getPoiBild_());
                if(dbPoiXmlContainerListSaved==null){
                    nameBild = getNameOfFile(dbPoiXmlContainerList.get(i).getPoiBild_());
                    inBild = downloadUrl(dbPoiXmlContainerList.get(i).getPoiBild_());
                } else {
                    Date lastDateBild = dbPoiXmlContainerListSaved.get(i).getDateBild_();
                    if (!dateBild.equals(lastDateBild)) {
                        System.out.println("new File -> download");
                        nameBild = getNameOfFile(dbPoiXmlContainerList.get(i).getPoiBild_());
                        inBild = downloadUrl(dbPoiXmlContainerList.get(i).getPoiBild_());
                    } else {
                        System.out.println("old File -> do not download");
                        dbPoiXmlContainerList.get(i).setPathBild_(dbPoiXmlContainerListSaved.get(i).getPathBild_());
                    }
                }
                dbPoiXmlContainerList.get(i).setDateBild_(dateBild);
            }

            if (inLogo1 != null && nameLogo1 != null) {
                //if the picture is downloaded -> decode stream and save as bitmap
                bmLogo1 = BitmapFactory.decodeStream(inLogo1);
                //save picture in internal storage and store link in list
                dbPoiXmlContainerList.get(i).setPathLogo1_(saveImageInternalStorage(bmLogo1, nameLogo1, context));
            }
            if (inLogo2 != null && nameLogo2 != null) {
                bmLogo2 = BitmapFactory.decodeStream(inLogo2);
                dbPoiXmlContainerList.get(i).setPathLogo2_(saveImageInternalStorage(bmLogo2, nameLogo2, context));
            }
            if (inLogo3 != null && nameLogo3 != null) {
                bmLogo3 = BitmapFactory.decodeStream(inLogo3);
                dbPoiXmlContainerList.get(i).setPathLogo3_(saveImageInternalStorage(bmLogo3, nameLogo3, context));
            }
            if (inLogo4 != null && nameLogo4 != null) {
                bmLogo4 = BitmapFactory.decodeStream(inLogo4);
                dbPoiXmlContainerList.get(i).setPathLogo4_(saveImageInternalStorage(bmLogo4, nameLogo4, context));
            }
            if (inLogo5 != null && nameLogo5 != null) {
                bmLogo5 = BitmapFactory.decodeStream(inLogo5);
                dbPoiXmlContainerList.get(i).setPathLogo5_(saveImageInternalStorage(bmLogo5, nameLogo5, context));
            }
            if (inLogo6 != null && nameLogo6 != null) {
                bmLogo6 = BitmapFactory.decodeStream(inLogo6);
                dbPoiXmlContainerList.get(i).setPathLogo6_(saveImageInternalStorage(bmLogo6, nameLogo6, context));
            }
            if (inBild != null && nameBild != null) {
                bmBild = BitmapFactory.decodeStream(inBild);
                dbPoiXmlContainerList.get(i).setPathBild_(saveImageInternalStorage(bmBild, nameBild, context));
            }
            inLogo1 = inLogo2 = inLogo3 = inLogo4 = inLogo5 = inLogo6 = inBild = null;
            bmLogo1 = bmLogo2 = bmLogo3 = bmLogo4 = bmLogo5 = bmLogo6 = bmBild = null;
            nameLogo1 = nameLogo2 = nameLogo3 = nameLogo4 = nameLogo5 = nameLogo6 = nameBild = null;
        }
        for (int i = 0; i < dbRoutenXmlContainerList.size(); i++) {
            System.out.println("picture routen" + i);
            if (!dbRoutenXmlContainerList.get(i).getRoutenBild1_().isEmpty()) {
                Date dateBild = getModifiedDateDate(dbRoutenXmlContainerList.get(i).getRoutenBild1_());
                if(dbRoutenXmlContainerListSaved==null){
                    nameBild1 = getNameOfFile(dbRoutenXmlContainerList.get(i).getRoutenBild1_());
                    inBild1 = downloadUrl(dbRoutenXmlContainerList.get(i).getRoutenBild1_());
                } else {
                    Date lastDateBild = dbRoutenXmlContainerListSaved.get(i).getDateBild1_();
                    if (!dateBild.equals(lastDateBild)) {
                        System.out.println("new File -> download");
                        nameBild1 = getNameOfFile(dbRoutenXmlContainerList.get(i).getRoutenBild1_());
                        inBild1 = downloadUrl(dbRoutenXmlContainerList.get(i).getRoutenBild1_());
                    } else {
                        System.out.println("old File -> do not download");
                        dbRoutenXmlContainerList.get(i).setPathBild1_(dbRoutenXmlContainerListSaved.get(i).getPathBild1_());
                    }
                }
                dbRoutenXmlContainerList.get(i).setDateBild1_(dateBild);
            }
            if (!dbRoutenXmlContainerList.get(i).getRoutenBild2_().isEmpty()) {
                Date dateBild = getModifiedDateDate(dbRoutenXmlContainerList.get(i).getRoutenBild2_());
                if(dbRoutenXmlContainerListSaved==null){
                    nameBild2 = getNameOfFile(dbRoutenXmlContainerList.get(i).getRoutenBild2_());
                    inBild2 = downloadUrl(dbRoutenXmlContainerList.get(i).getRoutenBild2_());
                } else {
                    Date lastDateBild = dbRoutenXmlContainerListSaved.get(i).getDateBild2_();
                    if (!dateBild.equals(lastDateBild)) {
                        System.out.println("new File -> download");
                        nameBild2 = getNameOfFile(dbRoutenXmlContainerList.get(i).getRoutenBild2_());
                        inBild2 = downloadUrl(dbRoutenXmlContainerList.get(i).getRoutenBild2_());
                    } else {
                        System.out.println("old File -> do not download");
                        dbRoutenXmlContainerList.get(i).setPathBild2_(dbRoutenXmlContainerListSaved.get(i).getPathBild2_());
                    }
                }
                dbRoutenXmlContainerList.get(i).setDateBild2_(dateBild);
            }
            if (!dbRoutenXmlContainerList.get(i).getRoutenBild3_().isEmpty()) {
                Date dateBild = getModifiedDateDate(dbRoutenXmlContainerList.get(i).getRoutenBild3_());
                if(dbRoutenXmlContainerListSaved==null){
                    nameBild3 = getNameOfFile(dbRoutenXmlContainerList.get(i).getRoutenBild3_());
                    inBild3 = downloadUrl(dbRoutenXmlContainerList.get(i).getRoutenBild3_());
                } else {
                    Date lastDateBild = dbRoutenXmlContainerListSaved.get(i).getDateBild3_();
                    if (!dateBild.equals(lastDateBild)) {
                        System.out.println("new File -> download");
                        nameBild3 = getNameOfFile(dbRoutenXmlContainerList.get(i).getRoutenBild3_());
                        inBild3 = downloadUrl(dbRoutenXmlContainerList.get(i).getRoutenBild3_());
                    } else {
                        System.out.println("old File -> do not download");
                        dbRoutenXmlContainerList.get(i).setPathBild3_(dbRoutenXmlContainerListSaved.get(i).getPathBild3_());
                    }
                }
                dbRoutenXmlContainerList.get(i).setDateBild3_(dateBild);
            }

            if (inBild1 != null && nameBild1 != null) {
                bmBild1 = BitmapFactory.decodeStream(inBild1);
                dbRoutenXmlContainerList.get(i).setPathBild1_(saveImageInternalStorage(bmBild1, nameBild1, context));
            }
            if (inBild2 != null && nameBild2 != null) {
                bmBild2 = BitmapFactory.decodeStream(inBild2);
                dbRoutenXmlContainerList.get(i).setPathBild2_(saveImageInternalStorage(bmBild2, nameBild2, context));
            }
            if (inBild3 != null && nameBild3 != null) {
                bmBild3 = BitmapFactory.decodeStream(inBild3);
                dbRoutenXmlContainerList.get(i).setPathBild3_(saveImageInternalStorage(bmBild3, nameBild3, context));
            }

            inBild1 = inBild2 = inBild3 = null;
            bmBild1 = bmBild2 = bmBild3 = null;
            nameBild1 = nameBild2 = nameBild3 = null;
        }
    }

    private void downloadKml(List<DbRoutenXmlContainer> dbRoutenXmlContainerList, Context context) throws IOException {
        String nameKml=null;
        List<DbRoutenXmlContainer> dbRoutenXmlContainerListSaved = ReadDataFromFile.getDbRoutenXmlContainerList(context);
        for (int i = 0; i < dbRoutenXmlContainerList.size(); i++) {
            System.out.println("kml" + i);
            if (!dbRoutenXmlContainerList.get(i).getRoutenKml_().isEmpty()) {
                Date dateKML = getModifiedDateDate(dbRoutenXmlContainerList.get(i).getRoutenKml_());
                System.out.println(dateKML);
                if(dbRoutenXmlContainerListSaved==null){
                    nameKml = getNameOfFile(dbRoutenXmlContainerList.get(i).getRoutenKml_());
                    System.out.println(nameKml);
                    dbRoutenXmlContainerList.get(i).setPathKml_(downloadKmlOrMp3AndSaveToInternalStorage(
                            dbRoutenXmlContainerList.get(i).getRoutenKml_(), nameKml, context, "kml"));
                } else {
                    Date lastDateKML = dbRoutenXmlContainerListSaved.get(i).getDateKml_();
                    if (!dateKML.equals(lastDateKML)) {
                        System.out.println("new File -> download");
                        nameKml = getNameOfFile(dbRoutenXmlContainerList.get(i).getRoutenKml_());
                        dbRoutenXmlContainerList.get(i).setPathKml_(downloadKmlOrMp3AndSaveToInternalStorage(
                                dbRoutenXmlContainerList.get(i).getRoutenKml_(), nameKml, context, "kml"));
                    } else {
                        System.out.println("old File -> do not download");
                        dbRoutenXmlContainerList.get(i).setPathKml_(dbRoutenXmlContainerListSaved.get(i).getPathKml_());
                    }
                }
                dbRoutenXmlContainerList.get(i).setDateKml_(dateKML);
            }
            nameKml=null;
        }
    }
    private void downloadMP3(List<DbPoiXmlContainer> dbPoiXmlContainerList, Context context) throws IOException {
        String nameMP3_ger=null, nameMP3_bri=null, nameMP3_cze=null;
        InputStream inMP3_ger=null, inMP3_bri=null, inMP3_cze=null;
        List<DbPoiXmlContainer> dbPoiXmlContainerListSaved = ReadDataFromFile.getDbPoiXmlContainerList(context);
        for(int i=0; i<dbPoiXmlContainerList.size(); i++){
            System.out.println("mp3" + i);
            if(!dbPoiXmlContainerList.get(i).getPoiAudioDE_().isEmpty()){
                Date dateMP3 = getModifiedDateDate(dbPoiXmlContainerList.get(i).getPoiAudioDE_());
                if(dbPoiXmlContainerListSaved==null){
                    nameMP3_ger = getNameOfFile(dbPoiXmlContainerList.get(i).getPoiAudioDE_());
                    dbPoiXmlContainerList.get(i).setPathMP3DE_(downloadKmlOrMp3AndSaveToInternalStorage(
                            dbPoiXmlContainerList.get(i).getPoiAudioDE_(), nameMP3_ger, context, "mp3"));
                } else {
                    Date lastDateMP3 = dbPoiXmlContainerListSaved.get(i).getDateMP3DE_();
                    if (!dateMP3.equals(lastDateMP3)) {
                        System.out.println("new File -> download");
                        nameMP3_ger = getNameOfFile(dbPoiXmlContainerList.get(i).getPoiAudioDE_());
                        dbPoiXmlContainerList.get(i).setPathMP3DE_(downloadKmlOrMp3AndSaveToInternalStorage(
                                dbPoiXmlContainerList.get(i).getPoiAudioDE_(), nameMP3_ger, context, "mp3"));
                    } else {
                        System.out.println("old File -> do not download");
                        dbPoiXmlContainerList.get(i).setPathMP3DE_(dbPoiXmlContainerListSaved.get(i).getPathMP3DE_());
                    }
                }
                dbPoiXmlContainerList.get(i).setDateMP3DE_(dateMP3);
            }
            if(!dbPoiXmlContainerList.get(i).getPoiAudioEN_().isEmpty()){
                Date dateMP3 = getModifiedDateDate(dbPoiXmlContainerList.get(i).getPoiAudioEN_());
                if(dbPoiXmlContainerListSaved==null){
                    nameMP3_bri = getNameOfFile(dbPoiXmlContainerList.get(i).getPoiAudioEN_());
                    dbPoiXmlContainerList.get(i).setPathMP3EN_(downloadKmlOrMp3AndSaveToInternalStorage(
                            dbPoiXmlContainerList.get(i).getPoiAudioEN_(), nameMP3_bri, context, "mp3"));
                } else {
                    Date lastDateMP3 = dbPoiXmlContainerListSaved.get(i).getDateMP3EN_();
                    if (!dateMP3.equals(lastDateMP3)) {
                        System.out.println("new File -> download");
                        nameMP3_bri = getNameOfFile(dbPoiXmlContainerList.get(i).getPoiAudioEN_());
                        dbPoiXmlContainerList.get(i).setPathMP3EN_(downloadKmlOrMp3AndSaveToInternalStorage(
                                dbPoiXmlContainerList.get(i).getPoiAudioEN_(), nameMP3_bri, context, "mp3"));
                    } else {
                        System.out.println("old File -> do not download");
                        dbPoiXmlContainerList.get(i).setPathMP3EN_(dbPoiXmlContainerListSaved.get(i).getPathMP3EN_());
                    }
                }
                dbPoiXmlContainerList.get(i).setDateMP3EN_(dateMP3);
            }
            if(!dbPoiXmlContainerList.get(i).getPoiAudioCZ_().isEmpty()){
                Date dateMP3 = getModifiedDateDate(dbPoiXmlContainerList.get(i).getPoiAudioCZ_());
                if(dbPoiXmlContainerListSaved==null){
                    nameMP3_cze = getNameOfFile(dbPoiXmlContainerList.get(i).getPoiAudioCZ_());
                    dbPoiXmlContainerList.get(i).setPathMP3CZ_(downloadKmlOrMp3AndSaveToInternalStorage(
                            dbPoiXmlContainerList.get(i).getPoiAudioCZ_(), nameMP3_cze, context, "mp3"));
                } else {
                    Date lastDateMP3 = dbPoiXmlContainerListSaved.get(i).getDateMP3CZ_();
                    if (!dateMP3.equals(lastDateMP3)) {
                        System.out.println("new File -> download");
                        nameMP3_cze = getNameOfFile(dbPoiXmlContainerList.get(i).getPoiAudioCZ_());
                        dbPoiXmlContainerList.get(i).setPathMP3CZ_(downloadKmlOrMp3AndSaveToInternalStorage(
                                dbPoiXmlContainerList.get(i).getPoiAudioCZ_(), nameMP3_cze, context, "mp3"));
                    } else {
                        System.out.println("old File -> do not download");
                        dbPoiXmlContainerList.get(i).setPathMP3CZ_(dbPoiXmlContainerListSaved.get(i).getPathMP3CZ_());
                    }
                }
                dbPoiXmlContainerList.get(i).setDateMP3CZ_(dateMP3);
            }
            nameMP3_ger = nameMP3_bri = nameMP3_cze = null;
        }
    }
    private void writePoi(List<DbPoiXmlContainer> dbPoiXmlContainerList, Context context) {
        try {
            FileOutputStream fos = context.openFileOutput(POI_FILENAME, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(dbPoiXmlContainerList);
            os.close();
            fos.close();
        } catch (IOException e) {
            System.out.println("error write poi");
            flagError = true;
            e.printStackTrace();
        }
    }
    private void writeRoute(List<DbRoutenXmlContainer> dbRoutenXmlContainerList, Context context) {
        try {
            FileOutputStream fos = context.openFileOutput(ROUTEN_FILENAME, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(dbRoutenXmlContainerList);
            os.close();
            fos.close();
        } catch (IOException e) {
            System.out.println("error write route");
            flagError = true;
            e.printStackTrace();
        }
    }
    private void writeRoutePoi(List<DbRoutenPoiXmlContainer> dbRoutenPoiXmlContainerList, Context context) {
        try {
            FileOutputStream fos = context.openFileOutput(ROUTENPOI_FILENAME, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(dbRoutenPoiXmlContainerList);
            os.close();
            fos.close();
        } catch (IOException e) {
            System.out.println("error write routepoi");
            flagError = true;
            e.printStackTrace();
        }
    }
    private void writePoiText(List<DbPoiXmlContainer> dbPoiXmlContainerList, Context context) {
        try {
            FileOutputStream fos = context.openFileOutput(POI_FILENAME_TEXT, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(dbPoiXmlContainerList);
            os.close();
            fos.close();
        } catch (IOException e) {
            System.out.println("error write poi text");
            flagError = true;
            e.printStackTrace();
        }
    }
    private void writeRouteText(List<DbRoutenXmlContainer> dbRoutenXmlContainerList, Context context) {
        try {
            FileOutputStream fos = context.openFileOutput(ROUTEN_FILENAME_TEXT, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(dbRoutenXmlContainerList);
            os.close();
            fos.close();
        } catch (IOException e) {
            System.out.println("error write route text");
            flagError = true;
            e.printStackTrace();
        }
    }
    private void writeRoutePoiText(List<DbRoutenPoiXmlContainer> dbRoutenPoiXmlContainerList, Context context) {
        try {
            FileOutputStream fos = context.openFileOutput(ROUTENPOI_FILENAME_TEXT, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(dbRoutenPoiXmlContainerList);
            os.close();
            fos.close();
        } catch (IOException e) {
            System.out.println("error write routepoi text");
            flagError = true;
            e.printStackTrace();
        }
    }
    private void writeRouteKml(List<DbRoutenXmlContainer> dbRoutenXmlContainerList, Context context) {
        try {
            FileOutputStream fos = context.openFileOutput(ROUTEN_FILENAME_KML, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(dbRoutenXmlContainerList);
            os.close();
            fos.close();
        } catch (IOException e) {
            System.out.println("error write route kml");
            flagError = true;
            e.printStackTrace();
        }
    }
    private String getNameOfFile(String link) {
        String[] linksplit = link.split("/"); //Teilt den Link in seine einzelteile bei "/"
        return linksplit[linksplit.length-2]+"_"+linksplit[linksplit.length-1]; //gibt den letzten & vorletzten Teil (letzter Ordner+Name des Files) zurück
    }
    @NonNull
    private String saveImageInternalStorage(Bitmap bitmapImage, String filename, Context context){
        //creates a new directory
        File directory = context.getDir("imageDir", Context.MODE_PRIVATE);
        //creates a new file
        File mypath=new File(directory,filename);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 50, fos);
        } catch (Exception e) {
            System.out.println("error");
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                System.out.println("error save image");
                flagError = true;
                e.printStackTrace();
            }
        }
        //returns the absolute path of the file which is stored in the list
        return mypath.getAbsolutePath();
    }
    @NonNull
    private String downloadKmlOrMp3AndSaveToInternalStorage(String Url, String filename, Context context, String kmlOrMp3){
        int count;
        File mypath = null;
        OutputStream output = null;
        try {
            //connects to URL of the file
            URL url = new URL(Url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.connect();
            // download the file
            InputStream input = new BufferedInputStream(url.openStream(),
                    8192);
            // Output stream
            //creates a new directory
            File directory;
            if(kmlOrMp3.equals("kml")) {
                 directory = context.getDir("kmlDir", Context.MODE_PRIVATE);
            } else if(kmlOrMp3.equals("mp3")){
                directory = context.getDir("mp3Dir", Context.MODE_PRIVATE);
            } else {
                System.out.println("Falscher Datentyp übergeben!");
                return "";
            }
            //creates a new file
            mypath=new File(directory,filename);
            output = new FileOutputStream(mypath);
            // writing data to file
            byte data[] = new byte[1024];
            while ((count = input.read(data)) != -1) {
                output.write(data, 0, count);
            }
            // flushing output
            output.flush();
            // closing streams
            output.close();
            input.close();
        } catch (Exception e) {
            System.out.println("Error kml/mp3");
            flagError = true;
            return "";
        }
        //returns absolute path of the file which is stored in the list
        return mypath.getAbsolutePath();
    }
}


