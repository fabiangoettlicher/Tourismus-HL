package com.htlhl.tourismus_hl_old;


import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

class RoutenPoiListAdapter extends ArrayAdapter<RoutenPoiListContainer>{
    private Context context;
    private int layoutResourceId;
    private RoutenPoiListContainer data[] = null;
    private List<RoutenPoiListContainer> routenPoiListContainerList;

    RoutenPoiListAdapter(Context context, int layoutResourceId, List<RoutenPoiListContainer> routenPoiListContainerList) {
        super(context, layoutResourceId, routenPoiListContainerList);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.routenPoiListContainerList=routenPoiListContainerList;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View row = convertView;
        PoiHolder holder = null;

        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new PoiHolder();
            holder.imgLogo = (ImageView)row.findViewById(R.id.lvpLogo);
            holder.imgArt = (ImageView)row.findViewById(R.id.lvpIvKat);
            holder.txtAdressline1 = (TextView)row.findViewById(R.id.lvpAdressline1);
            holder.txtName = (TextView)row.findViewById(R.id.lvpName);
            holder.txtEntfernung = (TextView)row.findViewById(R.id.lvpEntfernung);
            holder.imgBew = (ImageView) row.findViewById(R.id.lvpIvBew);

            row.setTag(holder);
        }
        else
        {
            holder = (PoiHolder)row.getTag();
        }

        //RoutenPoiListContainer poiListview = data[position];
        RoutenPoiListContainer poiListview = routenPoiListContainerList.get(position);
        holder.txtAdressline1.setText(poiListview.Adressline1);
        holder.txtEntfernung.setText(poiListview.entfernung);
        holder.txtName.setText(poiListview.name);
        if(holder.imgLogo != null) {
            holder.imgLogo.setImageBitmap(poiListview.logo);
        } else {
            holder.imgLogo.setVisibility(View.GONE);
        }
        if(poiListview.imgArt != -1) {
            holder.imgArt.setImageResource(poiListview.imgArt);
        }
        if(poiListview.imgBewertung != null) {
            holder.imgBew.setImageBitmap(poiListview.imgBewertung);
        } else {
            holder.imgBew.setVisibility(View.GONE);
        }
        return row;
    }

    //Speichert die ID der Tv und Iv (verbessert Performance)
    private static class PoiHolder
    {
        ImageView imgLogo, imgArt, imgBew;
        TextView txtAdressline1, txtEntfernung, txtName;
    }
}
