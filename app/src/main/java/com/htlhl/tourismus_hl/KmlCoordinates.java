package com.htlhl.tourismus_hl;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.htlhl.tourismus_hl.Data.Local.ReadDataFromFile;
import com.htlhl.tourismus_hl.Data.Model.PointOfInterest;
import com.htlhl.tourismus_hl.Data.Model.Routen;
import com.htlhl.tourismus_hl.Data.Model.RoutenPointOfInterestLinking;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class KmlCoordinates extends AppCompatActivity{
    //Wenn kein Layer verfügbar -> Bounds mit diesen Koordinaten
    private static final double neLat = 48.652309;
    private static final double neLng = 16.297428;
    private static final double swLat = 48.430550;
    private static final double swLng = 15.894205;

    public static boolean flagStartZiel; //false->Start und Ziel; true->StartZiel
    public static boolean flagNoKml = false;
    public static LatLng startpoint, endpoint, startendpoint;
    private LatLngBounds bounds;
    private List<PointOfInterest> PointOfInterestList;
    private List<RoutenPointOfInterestLinking> routenPointOfInterestLinkingList;
    private List<Routen> routenList;
    private List<String> poiLats;
    private List<String> poiLngs;
    private List<Integer> routenPoiIDsPoi;


    public LatLngBounds readKML(String pathKml, TextView tvRouten, Context context) {
        String column = null;
        Boolean placeCorrect = Boolean.FALSE;
        poiLats = new ArrayList<>();
        poiLngs = new ArrayList<>();
        routenPoiIDsPoi = new ArrayList<>();
        PointOfInterestList = ReadDataFromFile.getDbPoiXmlContainerList(context);
        routenPointOfInterestLinkingList = ReadDataFromFile.getDbRoutenPoiXmlContainerList(context);
        routenList = ReadDataFromFile.getDbRoutenXmlContainerList(context);
        if(routenList ==null || PointOfInterestList ==null || routenPointOfInterestLinkingList ==null){
            PointOfInterestList = ReadDataFromFile.getDbPoiXmlContainerListText(context);
            routenList = ReadDataFromFile.getDbRoutenXmlContainerListText(context);
            routenPointOfInterestLinkingList = ReadDataFromFile.getDbRoutenPoiXmlContainerListText(context);
        }

        getLatLngPois(tvRouten);
        InputStream inputStream = getInputstreamKml(pathKml);
        if (inputStream==null){
            flagNoKml = true;
            LatLng NEbound = new LatLng(neLat, neLng);
            LatLng SWbound = new LatLng(swLat, swLng);
            bounds = new LatLngBounds(SWbound, NEbound);
            return bounds;
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(getInputstreamKml(pathKml)));
        try {
            while ((column = br.readLine()) != null) {
                int coordin = column.indexOf("<coordinates>");
                if (coordin != -1) {
                    String tmpCoordin = column;
                    //tmpCoordin = tmpCoordin.replaceAll(" ", "");
                    tmpCoordin = tmpCoordin.replaceAll("\t", "");
                    tmpCoordin = tmpCoordin.replaceAll("<coordinates>", "");
                    tmpCoordin = tmpCoordin
                            .replaceAll("</coordinates>", "");
                    String[] points = tmpCoordin.split(" ");
                    String[] coo;
                    List<String> lng = new ArrayList<String>();
                    List<String> lat = new ArrayList<String>();
                    //lat & lng getrennt in ArrayListen speichern
                    for (String point : points) {
                        coo = point.split(",");
                        lng.add(coo[0]);
                        lat.add(coo[1]);
                    }
                    float latMIN, latMAX, lngMIN, lngMAX;
                    float[] lngFLOAT = convertToFloats(lng);
                    float[] latFLOAT = convertToFloats(lat);
                    float lngStart = lngFLOAT[0];
                    float lngEnd = lngFLOAT[lngFLOAT.length-1];
                    float latStart = latFLOAT[0];
                    float latEnd = latFLOAT[latFLOAT.length-1];
                    float difLng = Math.abs(lngStart-lngEnd);
                    float difLat = Math.abs(latStart-latEnd);
                    //if the distance between the start- and destination-point is too few
                    //show only on start/destination-point
                    if (difLat > 0.001 || difLng > 0.001){
                        flagStartZiel = false;
                        startpoint = new LatLng(latStart, lngStart);
                        endpoint = new LatLng(latEnd, lngEnd);
                    } else {
                        flagStartZiel = true;
                        float latStartEnd = (latStart+latEnd)/2;
                        float lngStartEnd = (lngStart+lngEnd)/2;
                        startendpoint = new LatLng(latStartEnd, lngStartEnd);
                    }
                    lngMAX = lngMIN = lngFLOAT[0];
                    latMAX = latMIN = latFLOAT[0];
                    for(int i=0; i<lngFLOAT.length; i++){
                        if(lngMAX < lngFLOAT[i]){
                            lngMAX = lngFLOAT[i];
                        }
                        if(lngMIN > lngFLOAT[i]){
                            lngMIN = lngFLOAT[i];
                        }
                    }
                    for(int i=0; i<latFLOAT.length; i++){
                        if(latMAX < latFLOAT[i]){
                            latMAX = latFLOAT[i];
                        }
                        if(latMIN > latFLOAT[i]){
                            latMIN = latFLOAT[i];
                        }
                    }
                    for(int i=0; i<poiLats.size(); i++){
                        if(!poiLats.get(i).equals("")) { //Falls keine Lat vorhanden
                            if (latMAX < Float.parseFloat(poiLats.get(i))) {
                                latMAX = Float.parseFloat(poiLats.get(i));
                            }
                            if (latMIN > Float.parseFloat(poiLats.get(i))) {
                                latMIN = Float.parseFloat(poiLats.get(i));
                            }
                        }
                    }
                    for(int i=0; i<poiLngs.size(); i++){
                        if(!poiLats.get(i).equals("")) { //Falls keine Lng vorhanden
                            if (lngMAX < Float.parseFloat(poiLngs.get(i))) {
                                lngMAX = Float.parseFloat(poiLngs.get(i));
                            }
                            if (lngMIN > Float.parseFloat(poiLngs.get(i))) {
                                lngMIN = Float.parseFloat(poiLngs.get(i));
                            }
                        }
                    }
                    LatLng NWbound = new LatLng(latMAX, lngMAX);
                    LatLng SWbound = new LatLng(latMIN, lngMIN);
                    bounds = new LatLngBounds(SWbound, NWbound);
                    break;
                }

            }
            br.close();
        } catch (IOException e ) {
            e.printStackTrace();
        } catch (ArrayIndexOutOfBoundsException e) {
            flagNoKml = true;
            LatLng NEbound = new LatLng(neLat, neLng);
            LatLng SWbound = new LatLng(swLat, swLng);
            bounds = new LatLngBounds(SWbound, NEbound);
            return bounds;
        }
        return bounds;
    }

    public static float[] convertToFloats(List<String> strings)
    {
        float[] ret = new float[strings.size()];
        for (int i=0; i < strings.size(); i++)
        {
            ret[i] = Float.parseFloat(strings.get(i));
        }
        return ret;
    }

    public InputStream getInputstreamKml(String fullpath){
        InputStream is = null;
        if(fullpath!=null) {
            String[] splitfullpath = fullpath.split("/");
            String path = "";
            for (int i = 0; i < splitfullpath.length - 1; i++) {
                if (i != 0) {
                    path = path.concat("/");
                }
                path = path.concat(splitfullpath[i]);
            }
            String name = splitfullpath[splitfullpath.length - 1];
            try {
                File f = new File(path, name);
                is = new FileInputStream(f);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return is;
    }
    public void getLatLngPois(TextView tvRouten){
        for(int i = 0; i< routenList.size(); i++){
            if(routenList.get(i).getRoutenName_().equals(tvRouten.getText())){ //aktuelle Route
                int routenID = routenList.get(i).getRoutenID_(); //speichere RoutenID
                for(int x = 0; x< routenPointOfInterestLinkingList.size(); x++){ //finde dazugehoerige Poi
                    if(routenPointOfInterestLinkingList.get(x).getRoutenpoiIDrouten_()==routenID){
                        routenPoiIDsPoi.add(routenPointOfInterestLinkingList.get(x).getRoutenpoiIDpoi_());
                    }
                }
            }
        }
        poiLats.clear();
        poiLngs.clear();
        for(int y = 0; y< PointOfInterestList.size(); y++){
            for(int z=0; z<routenPoiIDsPoi.size(); z++){ //vergleiche jeden Poi mit jeder ID
                if(PointOfInterestList.get(y).getPoiID_()==routenPoiIDsPoi.get(z)){ //wenn übereinstimmt
                    poiLats.add(PointOfInterestList.get(y).getPoiLat_());
                    poiLngs.add(PointOfInterestList.get(y).getPoiLng_());
                }
            }
        }
    }
}