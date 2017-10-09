package com.htlhl.tourismus_hl;

import android.app.Activity;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.widget.TextView;

import java.text.DecimalFormat;

class RoutenPoiInfoLocationListener implements LocationListener {
    private TextView tvEntf;

    RoutenPoiInfoLocationListener(Activity activity) {
        tvEntf = (TextView) activity.findViewById(R.id.tvEntfernungPoiInfo);
    }

    @Override
    public void onLocationChanged(Location location) {
        Location currLocation = new Location("");
        currLocation.setLatitude(location.getLatitude());
        currLocation.setLongitude(location.getLongitude());
        Location poiLocation = new Location("");
        poiLocation.setLatitude(RoutenPoiInfo.lat);
        poiLocation.setLongitude(RoutenPoiInfo.lng);

        float floatDistanceM = currLocation.distanceTo(poiLocation);
        float floatDistanceKM = floatDistanceM/1000;
        String stringDistanceM = new DecimalFormat("##").format(floatDistanceM) + " m";
        String stringDistanceKM = new DecimalFormat("###.#").format(floatDistanceKM) + " km";
        if(floatDistanceM<100){
            tvEntf.setText(stringDistanceM);
        }else{
            tvEntf.setText(stringDistanceKM);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}


