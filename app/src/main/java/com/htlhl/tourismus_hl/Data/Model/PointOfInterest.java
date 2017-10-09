package com.htlhl.tourismus_hl.Data.Model;

import java.io.Serializable;
import java.util.Date;

public class PointOfInterest implements Serializable{
    private String poiName_, poiLat_, poiLng_, poiTextDE_,
            poiTextEN_, poiTextCZ_, poiStat_;
    private String poiLogo1_, poiLogo2_, poiLogo3_, poiLogo4_,
            poiLogo5_, poiLogo6_, poiBild_;
    private String poiKontak1_, poiKontak2_, poiKontak3_,
            poiKontak4_, poiKontak5_;
    private String poiOffenDE_, poiOffenEN_, poiOffenCZ_,
            poiAudioDE_, poiAudioEN_, poiAudioCZ_;
    private int poiKatID_, poiID_;
    private String pathLogo1_, pathLogo2_, pathLogo3_, pathLogo4_,
            pathLogo5_, pathLogo6_, pathBild_;
    private String pathMP3DE_, pathMP3EN_, pathMP3CZ_;
    private Date dateLogo1_, dateLogo2_, dateLogo3_, dateLogo4_,
            dateLogo5_, dateLogo6_, dateBild_;
    private Date dateMP3DE_, dateMP3EN_, dateMP3CZ_;

    public PointOfInterest(String poiName_, String poiLat_, String poiLng_,
                    String poiTextDE_, String poiTextEN_, String poiTextCZ_,
                    String poiStat_, String poiLogo1_, String poiLogo2_,
                    String poiLogo3_, String poiLogo4_, String poiLogo5_,
                    String poiLogo6_, String poiBild_, String poiKontak1_,
                    String poiKontak2_, String poiKontak3_, String poiKontak4_,
                    String poiKontak5_, String poiOffenDE_, String poiOffenEN_,
                    String poiOffenCZ_, String poiAudioDE_, String poiAudioEN_,
                    String poiAudioCZ_, int poiKatID_, int poiID_) {

        this.poiName_ = poiName_; this.poiLat_ = poiLat_; this.poiLng_ = poiLng_;
        this.poiTextDE_ = poiTextDE_;this.poiTextEN_ = poiTextEN_;
        this.poiTextCZ_ = poiTextCZ_; this.poiStat_ = poiStat_;
        this.poiLogo1_ = poiLogo1_; this.poiLogo2_ = poiLogo2_;
        this.poiLogo3_ = poiLogo3_; this.poiLogo4_ = poiLogo4_;
        this.poiLogo5_ = poiLogo5_; this.poiLogo6_ = poiLogo6_;
        this.poiBild_ = poiBild_; this.poiKontak1_ = poiKontak1_;
        this.poiKontak2_ = poiKontak2_; this.poiKontak3_ = poiKontak3_;
        this.poiKontak4_ = poiKontak4_; this.poiKontak5_ = poiKontak5_;
        this.poiOffenDE_ = poiOffenDE_; this.poiOffenEN_ = poiOffenEN_;
        this.poiOffenCZ_ = poiOffenCZ_; this.poiAudioDE_ = poiAudioDE_;
        this.poiAudioEN_ = poiAudioEN_; this.poiAudioCZ_ = poiAudioCZ_;
        this.poiKatID_ = poiKatID_; this.poiID_ = poiID_;
    }
    public String getPoiTextDE_() {
        return poiTextDE_;
    }
    public String getPoiTextEN_() {
        return poiTextEN_;
    }

    public String getPoiTextCZ_() {
        return poiTextCZ_;
    }

    public String getPoiStat_() {
        return poiStat_;
    }

    public String getPoiLogo1_() {
        return poiLogo1_;
    }

    public String getPoiLogo2_() {
        return poiLogo2_;
    }

    public String getPoiLogo3_() {
        return poiLogo3_;
    }

    public String getPoiLogo4_() {
        return poiLogo4_;
    }

    public String getPoiLogo5_() {
        return poiLogo5_;
    }

    public String getPoiLogo6_() {
        return poiLogo6_;
    }

    public String getPoiBild_() {
        return poiBild_;
    }

    public String getPoiKontak1_() {
        return poiKontak1_;
    }

    public String getPoiKontak2_() {
        return poiKontak2_;
    }

    public String getPoiKontak3_() {
        return poiKontak3_;
    }

    public String getPoiKontak4_() {
        return poiKontak4_;
    }

    public String getPoiKontak5_() {
        return poiKontak5_;
    }

    public String getPoiOffenDE_() {
        return poiOffenDE_;
    }

    public String getPoiOffenEN_() {
        return poiOffenEN_;
    }

    public String getPoiOffenCZ_() {
        return poiOffenCZ_;
    }

    public String getPoiAudioDE_() {
        return poiAudioDE_;
    }

    public String getPoiAudioEN_() {
        return poiAudioEN_;
    }

    public String getPoiAudioCZ_() {
        return poiAudioCZ_;
    }

    public int getPoiKatID_() {
        return poiKatID_;
    }

    public int getPoiID_() {
        return poiID_;
    }

    public String getPoiLng_() {
        return poiLng_;
    }

    public String getPoiName_() { return poiName_; }

    public String getPoiLat_() { return poiLat_; }

    public String getPathLogo1_() {
        return pathLogo1_;
    }

    public void setPathLogo1_(String pathLogo1_) {
        this.pathLogo1_ = pathLogo1_;
    }

    public String getPathLogo2_() {
        return pathLogo2_;
    }

    public void setPathLogo2_(String pathLogo2_) {
        this.pathLogo2_ = pathLogo2_;
    }

    public String getPathLogo3_() {
        return pathLogo3_;
    }

    public void setPathLogo3_(String pathLogo3_) {
        this.pathLogo3_ = pathLogo3_;
    }

    public String getPathLogo4_() {
        return pathLogo4_;
    }

    public void setPathLogo4_(String pathLogo4_) {
        this.pathLogo4_ = pathLogo4_;
    }

    public String getPathLogo5_() {
        return pathLogo5_;
    }

    public void setPathLogo5_(String pathLogo5_) {
        this.pathLogo5_ = pathLogo5_;
    }

    public String getPathLogo6_() {
        return pathLogo6_;
    }

    public void setPathLogo6_(String pathLogo6_) {
        this.pathLogo6_ = pathLogo6_;
    }

    public String getPathBild_() {
        return pathBild_;
    }

    public void setPathBild_(String pathBild_) {
        this.pathBild_ = pathBild_;
    }

    public String getPathMP3DE_() {
        return pathMP3DE_;
    }

    public void setPathMP3DE_(String pathMP3DE_) {
        this.pathMP3DE_ = pathMP3DE_;
    }

    public String getPathMP3EN_() {
        return pathMP3EN_;
    }

    public void setPathMP3EN_(String pathMP3EN_) {
        this.pathMP3EN_ = pathMP3EN_;
    }

    public String getPathMP3CZ_() {
        return pathMP3CZ_;
    }

    public void setPathMP3CZ_(String pathMP3CZ_) {
        this.pathMP3CZ_ = pathMP3CZ_;
    }

    public Date getDateMP3CZ_() {return dateMP3CZ_;}

    public void setDateMP3CZ_(Date dateMP3CZ_) {
        this.dateMP3CZ_ = dateMP3CZ_;
    }

    public Date getDateMP3EN_() {
        return dateMP3EN_;
    }

    public void setDateMP3EN_(Date dateMP3EN_) {
        this.dateMP3EN_ = dateMP3EN_;
    }

    public Date getDateMP3DE_() {
        return dateMP3DE_;
    }

    public void setDateMP3DE_(Date dateMP3DE_) {
        this.dateMP3DE_ = dateMP3DE_;
    }

    public Date getDateBild_() {
        return dateBild_;
    }

    public void setDateBild_(Date dateBild_) {
        this.dateBild_ = dateBild_;
    }

    public Date getDateLogo6_() {
        return dateLogo6_;
    }

    public void setDateLogo6_(Date dateLogo6_) {
        this.dateLogo6_ = dateLogo6_;
    }

    public Date getDateLogo5_() {
        return dateLogo5_;
    }

    public void setDateLogo5_(Date dateLogo5_) {
        this.dateLogo5_ = dateLogo5_;
    }

    public Date getDateLogo4_() {
        return dateLogo4_;
    }

    public void setDateLogo4_(Date dateLogo4_) {
        this.dateLogo4_ = dateLogo4_;
    }

    public Date getDateLogo3_() {
        return dateLogo3_;
    }

    public void setDateLogo3_(Date dateLogo3_) {
        this.dateLogo3_ = dateLogo3_;
    }

    public Date getDateLogo2_() {
        return dateLogo2_;
    }

    public void setDateLogo2_(Date dateLogo2_) {
        this.dateLogo2_ = dateLogo2_;
    }

    public Date getDateLogo1_() {
        return dateLogo1_;
    }

    public void setDateLogo1_(Date dateLogo1_) {
        this.dateLogo1_ = dateLogo1_;
    }
}
