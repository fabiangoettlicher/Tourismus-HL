package com.htlhl.tourismus_hl_old;

import android.graphics.Bitmap;

class RoutenPoiListContainer {
    int imgArt, id;
    String Adressline1, entfernung, name;
    Bitmap logo, imgBewertung;
    public RoutenPoiListContainer(){
        super();
    }
    RoutenPoiListContainer(Bitmap logo, int imgArt, Bitmap imgBewertung, String Adressline1, String entfernung, String name, int id) {
        super();
        this.logo = logo;
        this.imgArt = imgArt;
        this.imgBewertung = imgBewertung;
        this.Adressline1 = Adressline1;
        this.entfernung = entfernung;
        this.name = name;
        this.id = id;
    }
    public String getEntfernung() {
        return entfernung;
    }

    public Integer getId() {
        return id;
    }
}
