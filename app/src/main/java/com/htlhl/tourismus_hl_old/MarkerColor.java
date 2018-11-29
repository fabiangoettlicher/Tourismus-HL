package com.htlhl.tourismus_hl_old;


import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;


public class MarkerColor extends AppCompatActivity{
    static LatLng previousMarker, BreakCurrPosition, currPosition;
    static boolean flagFirstMarker = false, flagAbbrechen = false;
    static String currLat, currLng;
    static Marker mMarker;

    public void ChangeMarkerColor(List<String> stationLats, List<String> stationLngs, List<String> stationNamen){
        currPosition = findCoordinates(stationLats, stationLngs, stationNamen);
        MarkerOptionsGreen(currPosition);
        if (!flagFirstMarker) {
            flagFirstMarker = true;
        } else {
            if(!currPosition.equals(previousMarker)) {
                MarkerOptionsRed(previousMarker);
            }
        }
        previousMarker = currPosition;
    }
    public void MarkerOptionsGreen(LatLng pos) {
        for(int i=0; i<KellerkatzeHaupt.markers.size(); i++){
            if(KellerkatzeHaupt.markers.get(i).getPosition().equals(pos)){
                mMarker = KellerkatzeHaupt.getMapKG().addMarker(new MarkerOptions()
                        .position(pos)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                KellerkatzeHaupt.markers.add(i, mMarker);
                KellerkatzeHaupt.markers.remove(i+1);
            }
        }
        final CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(pos,(float)16);
        KellerkatzeHaupt.getMapKG().animateCamera(cameraUpdate);
    }
    public void MarkerOptionsRed(LatLng pos) {
        for(int i=0; i<KellerkatzeHaupt.markers.size(); i++){
            if(KellerkatzeHaupt.markers.get(i).getPosition().equals(pos)){
                mMarker = KellerkatzeHaupt.getMapKG().addMarker(new MarkerOptions()
                        .position(pos)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                KellerkatzeHaupt.markers.add(i, mMarker);
                KellerkatzeHaupt.markers.remove(i+1);
            }
        }
    }

    public LatLng findCoordinates(List<String> stationLats, List<String> stationLngs, List<String> stationNamen){
        String loggedStation = KellerkatzeHaupt.tvStation.getText().toString();
        String[] splitLoggedStation = loggedStation.split(" ");
        loggedStation = "";
        for(int x=0; x<splitLoggedStation.length; x++){
            if(x==1){
                loggedStation = loggedStation.concat(splitLoggedStation[x]);
            }else if(x>1){
                loggedStation = loggedStation.concat(" "+splitLoggedStation[x]);
            }
        }
        for(int i=0; i<stationNamen.size(); i++){
            if(loggedStation.equals(stationNamen.get(i))){
                //speichern der Position der eingeloggten stationNamen
                currLat = stationLats.get(i);
                currLng = stationLngs.get(i);
            }
        }
        double latitude = -1;
        double longitude = -1;
        if(!currLat.equals("")) {
            latitude=Double.parseDouble(currLat);
        }
        if(!currLng.equals("")) {
            longitude = Double.parseDouble(currLng);
        }
        return new LatLng(latitude, longitude);
    }
}
