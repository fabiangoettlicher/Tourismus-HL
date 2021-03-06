package com.htlhl.tourismus_hl;


import java.io.Serializable;

public class DbRoutenPoiXmlContainer implements Serializable{
    private int routenpoiID_, routenpoiIDrouten_, routenpoiIDpoi_;

    public DbRoutenPoiXmlContainer(int routenpoiID_, int routenpoiIDrouten_, int routenpoiIDpoi_) {
        this.routenpoiID_ = routenpoiID_;
        this.routenpoiIDrouten_ = routenpoiIDrouten_;
        this.routenpoiIDpoi_ = routenpoiIDpoi_;
    }

    public int getRoutenpoiID_() {
        return routenpoiID_;
    }

    public int getRoutenpoiIDrouten_() {
        return routenpoiIDrouten_;
    }

    public int getRoutenpoiIDpoi_() {
        return routenpoiIDpoi_;
    }
}
