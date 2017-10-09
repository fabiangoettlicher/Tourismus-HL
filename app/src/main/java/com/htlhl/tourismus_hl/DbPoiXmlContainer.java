package com.htlhl.tourismus_hl;

import java.io.Serializable;
import java.util.Date;

class DbPoiXmlContainer implements Serializable{
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

    DbPoiXmlContainer(String poiName_, String poiLat_, String poiLng_,
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
    String getPoiTextDE_() {
        return poiTextDE_;
    }
    String getPoiTextEN_() {
        return poiTextEN_;
    }

    String getPoiTextCZ_() {
        return poiTextCZ_;
    }

    String getPoiStat_() {
        return poiStat_;
    }

    String getPoiLogo1_() {
        return poiLogo1_;
    }

    String getPoiLogo2_() {
        return poiLogo2_;
    }

    String getPoiLogo3_() {
        return poiLogo3_;
    }

    String getPoiLogo4_() {
        return poiLogo4_;
    }

    String getPoiLogo5_() {
        return poiLogo5_;
    }

    String getPoiLogo6_() {
        return poiLogo6_;
    }

    String getPoiBild_() {
        return poiBild_;
    }

    String getPoiKontak1_() {
        return poiKontak1_;
    }

    String getPoiKontak2_() {
        return poiKontak2_;
    }

    String getPoiKontak3_() {
        return poiKontak3_;
    }

    String getPoiKontak4_() {
        return poiKontak4_;
    }

    String getPoiKontak5_() {
        return poiKontak5_;
    }

    String getPoiOffenDE_() {
        return poiOffenDE_;
    }

    String getPoiOffenEN_() {
        return poiOffenEN_;
    }

    String getPoiOffenCZ_() {
        return poiOffenCZ_;
    }

    String getPoiAudioDE_() {
        return poiAudioDE_;
    }

    String getPoiAudioEN_() {
        return poiAudioEN_;
    }

    String getPoiAudioCZ_() {
        return poiAudioCZ_;
    }

    int getPoiKatID_() {
        return poiKatID_;
    }

    int getPoiID_() {
        return poiID_;
    }

    String getPoiLng_() {
        return poiLng_;
    }

    String getPoiName_() { return poiName_; }

    String getPoiLat_() { return poiLat_; }

    String getPathLogo1_() {
        return pathLogo1_;
    }

    void setPathLogo1_(String pathLogo1_) {
        this.pathLogo1_ = pathLogo1_;
    }

    String getPathLogo2_() {
        return pathLogo2_;
    }

    void setPathLogo2_(String pathLogo2_) {
        this.pathLogo2_ = pathLogo2_;
    }

    String getPathLogo3_() {
        return pathLogo3_;
    }

    void setPathLogo3_(String pathLogo3_) {
        this.pathLogo3_ = pathLogo3_;
    }

    String getPathLogo4_() {
        return pathLogo4_;
    }

    void setPathLogo4_(String pathLogo4_) {
        this.pathLogo4_ = pathLogo4_;
    }

    String getPathLogo5_() {
        return pathLogo5_;
    }

    void setPathLogo5_(String pathLogo5_) {
        this.pathLogo5_ = pathLogo5_;
    }

    String getPathLogo6_() {
        return pathLogo6_;
    }

    void setPathLogo6_(String pathLogo6_) {
        this.pathLogo6_ = pathLogo6_;
    }

    String getPathBild_() {
        return pathBild_;
    }

    void setPathBild_(String pathBild_) {
        this.pathBild_ = pathBild_;
    }

    String getPathMP3DE_() {
        return pathMP3DE_;
    }

    void setPathMP3DE_(String pathMP3DE_) {
        this.pathMP3DE_ = pathMP3DE_;
    }

    String getPathMP3EN_() {
        return pathMP3EN_;
    }

    void setPathMP3EN_(String pathMP3EN_) {
        this.pathMP3EN_ = pathMP3EN_;
    }

    String getPathMP3CZ_() {
        return pathMP3CZ_;
    }

    void setPathMP3CZ_(String pathMP3CZ_) {
        this.pathMP3CZ_ = pathMP3CZ_;
    }

    public Date getDateMP3CZ_() {
        return dateMP3CZ_;
    }

    void setDateMP3CZ_(Date dateMP3CZ_) {
        this.dateMP3CZ_ = dateMP3CZ_;
    }

    Date getDateMP3EN_() {
        return dateMP3EN_;
    }

    void setDateMP3EN_(Date dateMP3EN_) {
        this.dateMP3EN_ = dateMP3EN_;
    }

    Date getDateMP3DE_() {
        return dateMP3DE_;
    }

    void setDateMP3DE_(Date dateMP3DE_) {
        this.dateMP3DE_ = dateMP3DE_;
    }

    Date getDateBild_() {
        return dateBild_;
    }

    void setDateBild_(Date dateBild_) {
        this.dateBild_ = dateBild_;
    }

    Date getDateLogo6_() {
        return dateLogo6_;
    }

    void setDateLogo6_(Date dateLogo6_) {
        this.dateLogo6_ = dateLogo6_;
    }

    Date getDateLogo5_() {
        return dateLogo5_;
    }

    void setDateLogo5_(Date dateLogo5_) {
        this.dateLogo5_ = dateLogo5_;
    }

    Date getDateLogo4_() {
        return dateLogo4_;
    }

    void setDateLogo4_(Date dateLogo4_) {
        this.dateLogo4_ = dateLogo4_;
    }

    Date getDateLogo3_() {
        return dateLogo3_;
    }

    void setDateLogo3_(Date dateLogo3_) {
        this.dateLogo3_ = dateLogo3_;
    }

    Date getDateLogo2_() {
        return dateLogo2_;
    }

    void setDateLogo2_(Date dateLogo2_) {
        this.dateLogo2_ = dateLogo2_;
    }

    Date getDateLogo1_() {
        return dateLogo1_;
    }

    void setDateLogo1_(Date dateLogo1_) {
        this.dateLogo1_ = dateLogo1_;
    }
}
