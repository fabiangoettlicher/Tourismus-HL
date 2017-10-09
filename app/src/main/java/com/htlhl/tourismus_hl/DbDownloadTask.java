package com.htlhl.tourismus_hl;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.htlhl.tourismus_hl.Data.Local.ReadDataFromFile;
import com.htlhl.tourismus_hl.Data.Model.PointOfInterest;
import com.htlhl.tourismus_hl.Data.Model.Routen;
import com.htlhl.tourismus_hl.Data.Model.RoutenPointOfInterestLinking;
import com.htlhl.tourismus_hl.Data.XmlParser.DbPoiXmlParser;
import com.htlhl.tourismus_hl.Data.XmlParser.DbRoutenPoiXmlParser;
import com.htlhl.tourismus_hl.Data.XmlParser.DbRoutenXmlParser;

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


public class DbDownloadTask extends AsyncTask<MainActivity.UrlParams, Integer, MainActivity.UrlParams> {
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

    public DbDownloadTask(MainActivity.UrlParams urlParams, LoadingTaskFinishedListener finishedListener) {
        this.urlParams = urlParams;
        this.finishedListener = finishedListener;
        this.context = urlParams.contextMain;
    }

    public DbDownloadTask(MainActivity.UrlParams urlParams){
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

        List<PointOfInterest> PointOfInterestList = null;
        List<Routen> routenList = null;
        List<RoutenPointOfInterestLinking> routenPointOfInterestLinkingList = null;

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
                PointOfInterestList = dbPoiXmlParser.parse(inPoiXml);
                dataPref.edit().putLong("dateRouten", dateRouten).apply();
                routenList = dbRoutenXmlParser.parse(inRoutenXml);
                dataPref.edit().putLong("dateRoutenPoi", dateRoutenPoi).apply();
                routenPointOfInterestLinkingList = dbRoutenPoiXmlParser.parse(inRoutenPoiXml);
                //Write Lists with Data to Files on local Storage
                writePoiText(PointOfInterestList, urls.contextMain);
                writeRouteText(routenList, urls.contextMain);
                writeRoutePoiText(routenPointOfInterestLinkingList, urls.contextMain);
                if (finishedListener != null) {
                    finishedListener.onTaskFinished();
                }
            } else {
                if (finishedListener == null) {
                    dataPref.edit().putLong("datePoi", datePoi).apply();
                    PointOfInterestList = dbPoiXmlParser.parse(inPoiXml);
                    dataPref.edit().putLong("dateRouten", dateRouten).apply();
                    routenList = dbRoutenXmlParser.parse(inRoutenXml);
                    dataPref.edit().putLong("dateRoutenPoi", dateRoutenPoi).apply();
                    routenPointOfInterestLinkingList = dbRoutenPoiXmlParser.parse(inRoutenPoiXml);
                } else {
                    if (datePoi != dataPref.getLong("datePoi", -1)) {
                        dataPref.edit().putLong("datePoi", datePoi).apply();
                        PointOfInterestList = dbPoiXmlParser.parse(inPoiXml);
                        System.out.println("Poi download" + new Date(datePoi));
                    } else {
                        System.out.println("poi nicht: ");
                    }
                    if (dateRouten != dataPref.getLong("dateRouten", -1)) {
                        dataPref.edit().putLong("dateRouten", dateRouten).apply();
                        routenList = dbRoutenXmlParser.parse(inRoutenXml);
                        System.out.println("routen download: " + new Date(dateRouten));
                    } else {
                        System.out.println("routen nicht");
                    }
                    if (dateRoutenPoi != dataPref.getLong("dateRoutenPoi", -1)) {
                        dataPref.edit().putLong("dateRoutenPoi", dateRoutenPoi).apply();
                        routenPointOfInterestLinkingList = dbRoutenPoiXmlParser.parse(inRoutenPoiXml);
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

        if(PointOfInterestList !=null || routenPointOfInterestLinkingList !=null || routenList !=null){
            downloadKml(routenList, urls.contextMain);
            writeRouteKml(routenList, urls.contextMain);
            downloadPictures(routenList, PointOfInterestList, urls.contextMain);
            downloadMP3(PointOfInterestList, urls.contextMain);
            //Speichern der ArrayListen
            if(PointOfInterestList !=null) {
                writePoi(PointOfInterestList, urls.contextMain);
                System.out.println("PoiListe wird gespeichert");
            }
            if(routenList !=null) {
                writeRoute(routenList, urls.contextMain);
                System.out.println("RoutenListe wird gespeichert");
            }
            if(routenPointOfInterestLinkingList !=null) {
                writeRoutePoi(routenPointOfInterestLinkingList, urls.contextMain);
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

    private void downloadPictures(List<Routen> routenList, List<PointOfInterest> PointOfInterestList, Context context) throws IOException {
        InputStream inLogo1 = null, inLogo2 = null, inLogo3 = null, inLogo4 = null, inLogo5 = null, inLogo6 = null, inBild = null;
        Bitmap bmLogo1, bmLogo2, bmLogo3, bmLogo4, bmLogo5, bmLogo6, bmBild;
        String nameLogo1=null, nameLogo2=null, nameLogo3=null, nameLogo4=null, nameLogo5=null, nameLogo6=null, nameBild=null;
        InputStream inBild1 = null, inBild2 = null, inBild3 = null;
        Bitmap bmBild1, bmBild2, bmBild3;
        String nameBild1=null, nameBild2=null, nameBild3=null;
        List<PointOfInterest> PointOfInterestListSaved = ReadDataFromFile.getDbPoiXmlContainerList(context);
        List<Routen> routenListSaved = ReadDataFromFile.getDbRoutenXmlContainerList(context);
        for (int i = 0; i < PointOfInterestList.size(); i++) {
            System.out.println("picture poi" + i);
            if (!PointOfInterestList.get(i).getPoiLogo1_().isEmpty()) {
                Date dateLogo = getModifiedDateDate(PointOfInterestList.get(i).getPoiLogo1_());
                //if first download -> download picture
                if(PointOfInterestListSaved ==null){
                    nameLogo1 = getNameOfFile(PointOfInterestList.get(i).getPoiLogo1_());
                    inLogo1 = downloadUrl(PointOfInterestList.get(i).getPoiLogo1_());
                } else {
                    //compare ModifiedDate
                    Date lastDateLogo = PointOfInterestListSaved.get(i).getDateLogo1_();
                    if (!dateLogo.equals(lastDateLogo)) {
                        //date is not equal -> new picture -> download it
                        System.out.println("new File -> download");
                        nameLogo1 = getNameOfFile(PointOfInterestList.get(i).getPoiLogo1_());
                        inLogo1 = downloadUrl(PointOfInterestList.get(i).getPoiLogo1_());
                    } else {
                        //date is equal -> same picture -> do not download it
                        System.out.println("old File -> do not download");
                        PointOfInterestList.get(i).setPathLogo1_(
                                PointOfInterestListSaved.get(i).getPathLogo1_());
                    }
                }
                //store new ModifiedDate in list
                PointOfInterestList.get(i).setDateLogo1_(dateLogo);
            }
            if (!PointOfInterestList.get(i).getPoiLogo2_().isEmpty()) {
                Date dateLogo = getModifiedDateDate(PointOfInterestList.get(i).getPoiLogo2_());
                if(PointOfInterestListSaved ==null){
                    nameLogo2 = getNameOfFile(PointOfInterestList.get(i).getPoiLogo2_());
                    inLogo2 = downloadUrl(PointOfInterestList.get(i).getPoiLogo2_());
                } else {
                    Date lastDateLogo = PointOfInterestListSaved.get(i).getDateLogo2_();
                    if (!dateLogo.equals(lastDateLogo)) {
                        System.out.println("new File -> download");
                        nameLogo2 = getNameOfFile(PointOfInterestList.get(i).getPoiLogo2_());
                        inLogo2 = downloadUrl(PointOfInterestList.get(i).getPoiLogo2_());
                    } else {
                        System.out.println("old File -> do not download");
                        PointOfInterestList.get(i).setPathLogo2_(PointOfInterestListSaved.get(i).getPathLogo2_());
                    }
                }
                PointOfInterestList.get(i).setDateLogo2_(dateLogo);
            }
            if (!PointOfInterestList.get(i).getPoiLogo3_().isEmpty()) {
                Date dateLogo = getModifiedDateDate(PointOfInterestList.get(i).getPoiLogo3_());
                if(PointOfInterestListSaved ==null){
                    nameLogo3 = getNameOfFile(PointOfInterestList.get(i).getPoiLogo3_());
                    inLogo3 = downloadUrl(PointOfInterestList.get(i).getPoiLogo3_());
                } else {
                    Date lastDateLogo = PointOfInterestListSaved.get(i).getDateLogo3_();
                    if (!dateLogo.equals(lastDateLogo)) {
                        System.out.println("new File -> download");
                        nameLogo3 = getNameOfFile(PointOfInterestList.get(i).getPoiLogo3_());
                        inLogo3 = downloadUrl(PointOfInterestList.get(i).getPoiLogo3_());
                    } else {
                        System.out.println("old File -> do not download");
                        PointOfInterestList.get(i).setPathLogo3_(PointOfInterestListSaved.get(i).getPathLogo3_());
                    }
                }
                PointOfInterestList.get(i).setDateLogo3_(dateLogo);
            }
            if (!PointOfInterestList.get(i).getPoiLogo4_().isEmpty()) {
                Date dateLogo = getModifiedDateDate(PointOfInterestList.get(i).getPoiLogo4_());
                if(PointOfInterestListSaved ==null){
                    nameLogo4 = getNameOfFile(PointOfInterestList.get(i).getPoiLogo4_());
                    inLogo4 = downloadUrl(PointOfInterestList.get(i).getPoiLogo4_());
                } else {
                    Date lastDateLogo = PointOfInterestListSaved.get(i).getDateLogo4_();
                    if (!dateLogo.equals(lastDateLogo)) {
                        System.out.println("new File -> download");
                        nameLogo4 = getNameOfFile(PointOfInterestList.get(i).getPoiLogo4_());
                        inLogo4 = downloadUrl(PointOfInterestList.get(i).getPoiLogo4_());
                    } else {
                        System.out.println("old File -> do not download");
                        PointOfInterestList.get(i).setPathLogo4_(PointOfInterestListSaved.get(i).getPathLogo4_());
                    }
                }
                PointOfInterestList.get(i).setDateLogo4_(dateLogo);
            }
            if (!PointOfInterestList.get(i).getPoiLogo5_().isEmpty()) {
                Date dateLogo = getModifiedDateDate(PointOfInterestList.get(i).getPoiLogo5_());
                if(PointOfInterestListSaved ==null){
                    nameLogo5 = getNameOfFile(PointOfInterestList.get(i).getPoiLogo5_());
                    inLogo5 = downloadUrl(PointOfInterestList.get(i).getPoiLogo5_());
                } else {
                    Date lastDateLogo = PointOfInterestListSaved.get(i).getDateLogo5_();
                    if (!dateLogo.equals(lastDateLogo)) {
                        System.out.println("new File -> download");
                        nameLogo5 = getNameOfFile(PointOfInterestList.get(i).getPoiLogo5_());
                        inLogo5 = downloadUrl(PointOfInterestList.get(i).getPoiLogo5_());
                    } else {
                        System.out.println("old File -> do not download");
                        PointOfInterestList.get(i).setPathLogo5_(PointOfInterestListSaved.get(i).getPathLogo5_());
                    }
                }
                PointOfInterestList.get(i).setDateLogo5_(dateLogo);
            }
            if (!PointOfInterestList.get(i).getPoiLogo6_().isEmpty()) {
                Date dateLogo = getModifiedDateDate(PointOfInterestList.get(i).getPoiLogo6_());
                if(PointOfInterestListSaved ==null){
                    nameLogo6 = getNameOfFile(PointOfInterestList.get(i).getPoiLogo6_());
                    inLogo6 = downloadUrl(PointOfInterestList.get(i).getPoiLogo6_());
                } else {
                    Date lastDateLogo = PointOfInterestListSaved.get(i).getDateLogo6_();
                    if (!dateLogo.equals(lastDateLogo)) {
                        System.out.println("new File -> download");
                        nameLogo6 = getNameOfFile(PointOfInterestList.get(i).getPoiLogo6_());
                        inLogo6 = downloadUrl(PointOfInterestList.get(i).getPoiLogo6_());
                    } else {
                        System.out.println("old File -> do not download");
                        PointOfInterestList.get(i).setPathLogo6_(PointOfInterestListSaved.get(i).getPathLogo6_());
                    }
                }
                PointOfInterestList.get(i).setDateLogo6_(dateLogo);
            }
            if (!PointOfInterestList.get(i).getPoiBild_().isEmpty()) {
                Date dateBild = getModifiedDateDate(PointOfInterestList.get(i).getPoiBild_());
                if(PointOfInterestListSaved ==null){
                    nameBild = getNameOfFile(PointOfInterestList.get(i).getPoiBild_());
                    inBild = downloadUrl(PointOfInterestList.get(i).getPoiBild_());
                } else {
                    Date lastDateBild = PointOfInterestListSaved.get(i).getDateBild_();
                    if (!dateBild.equals(lastDateBild)) {
                        System.out.println("new File -> download");
                        nameBild = getNameOfFile(PointOfInterestList.get(i).getPoiBild_());
                        inBild = downloadUrl(PointOfInterestList.get(i).getPoiBild_());
                    } else {
                        System.out.println("old File -> do not download");
                        PointOfInterestList.get(i).setPathBild_(PointOfInterestListSaved.get(i).getPathBild_());
                    }
                }
                PointOfInterestList.get(i).setDateBild_(dateBild);
            }

            if (inLogo1 != null && nameLogo1 != null) {
                //if the picture is downloaded -> decode stream and save as bitmap
                bmLogo1 = BitmapFactory.decodeStream(inLogo1);
                //save picture in internal storage and store link in list
                PointOfInterestList.get(i).setPathLogo1_(saveImageInternalStorage(bmLogo1, nameLogo1, context));
            }
            if (inLogo2 != null && nameLogo2 != null) {
                bmLogo2 = BitmapFactory.decodeStream(inLogo2);
                PointOfInterestList.get(i).setPathLogo2_(saveImageInternalStorage(bmLogo2, nameLogo2, context));
            }
            if (inLogo3 != null && nameLogo3 != null) {
                bmLogo3 = BitmapFactory.decodeStream(inLogo3);
                PointOfInterestList.get(i).setPathLogo3_(saveImageInternalStorage(bmLogo3, nameLogo3, context));
            }
            if (inLogo4 != null && nameLogo4 != null) {
                bmLogo4 = BitmapFactory.decodeStream(inLogo4);
                PointOfInterestList.get(i).setPathLogo4_(saveImageInternalStorage(bmLogo4, nameLogo4, context));
            }
            if (inLogo5 != null && nameLogo5 != null) {
                bmLogo5 = BitmapFactory.decodeStream(inLogo5);
                PointOfInterestList.get(i).setPathLogo5_(saveImageInternalStorage(bmLogo5, nameLogo5, context));
            }
            if (inLogo6 != null && nameLogo6 != null) {
                bmLogo6 = BitmapFactory.decodeStream(inLogo6);
                PointOfInterestList.get(i).setPathLogo6_(saveImageInternalStorage(bmLogo6, nameLogo6, context));
            }
            if (inBild != null && nameBild != null) {
                bmBild = BitmapFactory.decodeStream(inBild);
                PointOfInterestList.get(i).setPathBild_(saveImageInternalStorage(bmBild, nameBild, context));
            }
            inLogo1 = inLogo2 = inLogo3 = inLogo4 = inLogo5 = inLogo6 = inBild = null;
            bmLogo1 = bmLogo2 = bmLogo3 = bmLogo4 = bmLogo5 = bmLogo6 = bmBild = null;
            nameLogo1 = nameLogo2 = nameLogo3 = nameLogo4 = nameLogo5 = nameLogo6 = nameBild = null;
        }
        for (int i = 0; i < routenList.size(); i++) {
            System.out.println("picture routen" + i);
            if (!routenList.get(i).getRoutenBild1_().isEmpty()) {
                Date dateBild = getModifiedDateDate(routenList.get(i).getRoutenBild1_());
                if(routenListSaved ==null){
                    nameBild1 = getNameOfFile(routenList.get(i).getRoutenBild1_());
                    inBild1 = downloadUrl(routenList.get(i).getRoutenBild1_());
                } else {
                    Date lastDateBild = routenListSaved.get(i).getDateBild1_();
                    if (!dateBild.equals(lastDateBild)) {
                        System.out.println("new File -> download");
                        nameBild1 = getNameOfFile(routenList.get(i).getRoutenBild1_());
                        inBild1 = downloadUrl(routenList.get(i).getRoutenBild1_());
                    } else {
                        System.out.println("old File -> do not download");
                        routenList.get(i).setPathBild1_(routenListSaved.get(i).getPathBild1_());
                    }
                }
                routenList.get(i).setDateBild1_(dateBild);
            }
            if (!routenList.get(i).getRoutenBild2_().isEmpty()) {
                Date dateBild = getModifiedDateDate(routenList.get(i).getRoutenBild2_());
                if(routenListSaved ==null){
                    nameBild2 = getNameOfFile(routenList.get(i).getRoutenBild2_());
                    inBild2 = downloadUrl(routenList.get(i).getRoutenBild2_());
                } else {
                    Date lastDateBild = routenListSaved.get(i).getDateBild2_();
                    if (!dateBild.equals(lastDateBild)) {
                        System.out.println("new File -> download");
                        nameBild2 = getNameOfFile(routenList.get(i).getRoutenBild2_());
                        inBild2 = downloadUrl(routenList.get(i).getRoutenBild2_());
                    } else {
                        System.out.println("old File -> do not download");
                        routenList.get(i).setPathBild2_(routenListSaved.get(i).getPathBild2_());
                    }
                }
                routenList.get(i).setDateBild2_(dateBild);
            }
            if (!routenList.get(i).getRoutenBild3_().isEmpty()) {
                Date dateBild = getModifiedDateDate(routenList.get(i).getRoutenBild3_());
                if(routenListSaved ==null){
                    nameBild3 = getNameOfFile(routenList.get(i).getRoutenBild3_());
                    inBild3 = downloadUrl(routenList.get(i).getRoutenBild3_());
                } else {
                    Date lastDateBild = routenListSaved.get(i).getDateBild3_();
                    if (!dateBild.equals(lastDateBild)) {
                        System.out.println("new File -> download");
                        nameBild3 = getNameOfFile(routenList.get(i).getRoutenBild3_());
                        inBild3 = downloadUrl(routenList.get(i).getRoutenBild3_());
                    } else {
                        System.out.println("old File -> do not download");
                        routenList.get(i).setPathBild3_(routenListSaved.get(i).getPathBild3_());
                    }
                }
                routenList.get(i).setDateBild3_(dateBild);
            }

            if (inBild1 != null && nameBild1 != null) {
                bmBild1 = BitmapFactory.decodeStream(inBild1);
                routenList.get(i).setPathBild1_(saveImageInternalStorage(bmBild1, nameBild1, context));
            }
            if (inBild2 != null && nameBild2 != null) {
                bmBild2 = BitmapFactory.decodeStream(inBild2);
                routenList.get(i).setPathBild2_(saveImageInternalStorage(bmBild2, nameBild2, context));
            }
            if (inBild3 != null && nameBild3 != null) {
                bmBild3 = BitmapFactory.decodeStream(inBild3);
                routenList.get(i).setPathBild3_(saveImageInternalStorage(bmBild3, nameBild3, context));
            }

            inBild1 = inBild2 = inBild3 = null;
            bmBild1 = bmBild2 = bmBild3 = null;
            nameBild1 = nameBild2 = nameBild3 = null;
        }
    }

    private void downloadKml(List<Routen> routenList, Context context) throws IOException {
        String nameKml=null;
        List<Routen> routenListSaved = ReadDataFromFile.getDbRoutenXmlContainerList(context);
        for (int i = 0; i < routenList.size(); i++) {
            System.out.println("kml" + i);
            if (!routenList.get(i).getRoutenKml_().isEmpty()) {
                Date dateKML = getModifiedDateDate(routenList.get(i).getRoutenKml_());
                System.out.println(dateKML);
                if(routenListSaved ==null){
                    nameKml = getNameOfFile(routenList.get(i).getRoutenKml_());
                    System.out.println(nameKml);
                    routenList.get(i).setPathKml_(downloadKmlOrMp3AndSaveToInternalStorage(
                            routenList.get(i).getRoutenKml_(), nameKml, context, "kml"));
                } else {
                    Date lastDateKML = routenListSaved.get(i).getDateKml_();
                    if (!dateKML.equals(lastDateKML)) {
                        System.out.println("new File -> download");
                        nameKml = getNameOfFile(routenList.get(i).getRoutenKml_());
                        routenList.get(i).setPathKml_(downloadKmlOrMp3AndSaveToInternalStorage(
                                routenList.get(i).getRoutenKml_(), nameKml, context, "kml"));
                    } else {
                        System.out.println("old File -> do not download");
                        routenList.get(i).setPathKml_(routenListSaved.get(i).getPathKml_());
                    }
                }
                routenList.get(i).setDateKml_(dateKML);
            }
            nameKml=null;
        }
    }
    private void downloadMP3(List<PointOfInterest> PointOfInterestList, Context context) throws IOException {
        String nameMP3_ger=null, nameMP3_bri=null, nameMP3_cze=null;
        InputStream inMP3_ger=null, inMP3_bri=null, inMP3_cze=null;
        List<PointOfInterest> PointOfInterestListSaved = ReadDataFromFile.getDbPoiXmlContainerList(context);
        for(int i = 0; i< PointOfInterestList.size(); i++){
            System.out.println("mp3" + i);
            if(!PointOfInterestList.get(i).getPoiAudioDE_().isEmpty()){
                Date dateMP3 = getModifiedDateDate(PointOfInterestList.get(i).getPoiAudioDE_());
                if(PointOfInterestListSaved ==null){
                    nameMP3_ger = getNameOfFile(PointOfInterestList.get(i).getPoiAudioDE_());
                    PointOfInterestList.get(i).setPathMP3DE_(downloadKmlOrMp3AndSaveToInternalStorage(
                            PointOfInterestList.get(i).getPoiAudioDE_(), nameMP3_ger, context, "mp3"));
                } else {
                    Date lastDateMP3 = PointOfInterestListSaved.get(i).getDateMP3DE_();
                    if (!dateMP3.equals(lastDateMP3)) {
                        System.out.println("new File -> download");
                        nameMP3_ger = getNameOfFile(PointOfInterestList.get(i).getPoiAudioDE_());
                        PointOfInterestList.get(i).setPathMP3DE_(downloadKmlOrMp3AndSaveToInternalStorage(
                                PointOfInterestList.get(i).getPoiAudioDE_(), nameMP3_ger, context, "mp3"));
                    } else {
                        System.out.println("old File -> do not download");
                        PointOfInterestList.get(i).setPathMP3DE_(PointOfInterestListSaved.get(i).getPathMP3DE_());
                    }
                }
                PointOfInterestList.get(i).setDateMP3DE_(dateMP3);
            }
            if(!PointOfInterestList.get(i).getPoiAudioEN_().isEmpty()){
                Date dateMP3 = getModifiedDateDate(PointOfInterestList.get(i).getPoiAudioEN_());
                if(PointOfInterestListSaved ==null){
                    nameMP3_bri = getNameOfFile(PointOfInterestList.get(i).getPoiAudioEN_());
                    PointOfInterestList.get(i).setPathMP3EN_(downloadKmlOrMp3AndSaveToInternalStorage(
                            PointOfInterestList.get(i).getPoiAudioEN_(), nameMP3_bri, context, "mp3"));
                } else {
                    Date lastDateMP3 = PointOfInterestListSaved.get(i).getDateMP3EN_();
                    if (!dateMP3.equals(lastDateMP3)) {
                        System.out.println("new File -> download");
                        nameMP3_bri = getNameOfFile(PointOfInterestList.get(i).getPoiAudioEN_());
                        PointOfInterestList.get(i).setPathMP3EN_(downloadKmlOrMp3AndSaveToInternalStorage(
                                PointOfInterestList.get(i).getPoiAudioEN_(), nameMP3_bri, context, "mp3"));
                    } else {
                        System.out.println("old File -> do not download");
                        PointOfInterestList.get(i).setPathMP3EN_(PointOfInterestListSaved.get(i).getPathMP3EN_());
                    }
                }
                PointOfInterestList.get(i).setDateMP3EN_(dateMP3);
            }
            if(!PointOfInterestList.get(i).getPoiAudioCZ_().isEmpty()){
                Date dateMP3 = getModifiedDateDate(PointOfInterestList.get(i).getPoiAudioCZ_());
                if(PointOfInterestListSaved ==null){
                    nameMP3_cze = getNameOfFile(PointOfInterestList.get(i).getPoiAudioCZ_());
                    PointOfInterestList.get(i).setPathMP3CZ_(downloadKmlOrMp3AndSaveToInternalStorage(
                            PointOfInterestList.get(i).getPoiAudioCZ_(), nameMP3_cze, context, "mp3"));
                } else {
                    Date lastDateMP3 = PointOfInterestListSaved.get(i).getDateMP3CZ_();
                    if (!dateMP3.equals(lastDateMP3)) {
                        System.out.println("new File -> download");
                        nameMP3_cze = getNameOfFile(PointOfInterestList.get(i).getPoiAudioCZ_());
                        PointOfInterestList.get(i).setPathMP3CZ_(downloadKmlOrMp3AndSaveToInternalStorage(
                                PointOfInterestList.get(i).getPoiAudioCZ_(), nameMP3_cze, context, "mp3"));
                    } else {
                        System.out.println("old File -> do not download");
                        PointOfInterestList.get(i).setPathMP3CZ_(PointOfInterestListSaved.get(i).getPathMP3CZ_());
                    }
                }
                PointOfInterestList.get(i).setDateMP3CZ_(dateMP3);
            }
            nameMP3_ger = nameMP3_bri = nameMP3_cze = null;
        }
    }
    private void writePoi(List<PointOfInterest> PointOfInterestList, Context context) {
        try {
            FileOutputStream fos = context.openFileOutput(POI_FILENAME, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(PointOfInterestList);
            os.close();
            fos.close();
        } catch (IOException e) {
            System.out.println("error write poi");
            flagError = true;
            e.printStackTrace();
        }
    }
    private void writeRoute(List<Routen> routenList, Context context) {
        try {
            FileOutputStream fos = context.openFileOutput(ROUTEN_FILENAME, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(routenList);
            os.close();
            fos.close();
        } catch (IOException e) {
            System.out.println("error write route");
            flagError = true;
            e.printStackTrace();
        }
    }
    private void writeRoutePoi(List<RoutenPointOfInterestLinking> routenPointOfInterestLinkingList, Context context) {
        try {
            FileOutputStream fos = context.openFileOutput(ROUTENPOI_FILENAME, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(routenPointOfInterestLinkingList);
            os.close();
            fos.close();
        } catch (IOException e) {
            System.out.println("error write routepoi");
            flagError = true;
            e.printStackTrace();
        }
    }
    private void writePoiText(List<PointOfInterest> PointOfInterestList, Context context) {
        try {
            FileOutputStream fos = context.openFileOutput(POI_FILENAME_TEXT, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(PointOfInterestList);
            os.close();
            fos.close();
        } catch (IOException e) {
            System.out.println("error write poi text");
            flagError = true;
            e.printStackTrace();
        }
    }
    private void writeRouteText(List<Routen> routenList, Context context) {
        try {
            FileOutputStream fos = context.openFileOutput(ROUTEN_FILENAME_TEXT, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(routenList);
            os.close();
            fos.close();
        } catch (IOException e) {
            System.out.println("error write route text");
            flagError = true;
            e.printStackTrace();
        }
    }
    private void writeRoutePoiText(List<RoutenPointOfInterestLinking> routenPointOfInterestLinkingList, Context context) {
        try {
            FileOutputStream fos = context.openFileOutput(ROUTENPOI_FILENAME_TEXT, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(routenPointOfInterestLinkingList);
            os.close();
            fos.close();
        } catch (IOException e) {
            System.out.println("error write routepoi text");
            flagError = true;
            e.printStackTrace();
        }
    }
    private void writeRouteKml(List<Routen> routenList, Context context) {
        try {
            FileOutputStream fos = context.openFileOutput(ROUTEN_FILENAME_KML, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(routenList);
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


