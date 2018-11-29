package com.htlhl.tourismus_hl_old.Data.Model;


import java.io.Serializable;
import java.util.Date;

public class Routen implements Serializable {
    private String routenName_, routenKml_, routenLink_,
            routenBild1_, routenBild2_, routenBild3_;
    private String routenInfo1DE_, routenInfo2DE_, routenInfo3DE_,
            routenInfo4DE_, routenInfo5DE_, routenInfo6DE_, routenInfo7DE_;
    private String routenInfo1EN_, routenInfo2EN_, routenInfo3EN_,
            routenInfo4EN_, routenInfo5EN_, routenInfo6EN_, routenInfo7EN_;
    private String routenInfo1CZ_, routenInfo2CZ_, routenInfo3CZ_,
            routenInfo4CZ_, routenInfo5CZ_, routenInfo6CZ_, routenInfo7CZ_;
    private int routenID_, routenKatID_;
    private String pathBild1_, pathBild2_, pathBild3_, pathKml_;
    private Date dateBild1_, dateBild2_, dateBild3_, dateKml_;

    public Routen(String routenName_, String routenKml_, String routenLink_,
           String routenBild1_, String routenBild2_,
           String routenBild3_, String routenInfo1DE_,
           String routenInfo2DE_, String routenInfo3DE_,
           String routenInfo4DE_, String routenInfo5DE_,
           String routenInfo6DE_, String routenInfo7DE_,
           String routenInfo1EN_, String routenInfo2EN_,
           String routenInfo3EN_, String routenInfo4EN_,
           String routenInfo5EN_, String routenInfo6EN_,
           String routenInfo7EN_, String routenInfo1CZ_,
           String routenInfo2CZ_, String routenInfo3CZ_,
           String routenInfo4CZ_, String routenInfo5CZ_,
           String routenInfo6CZ_, String routenInfo7CZ_,
           int routenID_, int routenKatID_) {
        this.routenName_ = routenName_; this.routenKml_ = routenKml_;
        this.routenLink_ = routenLink_; this.routenBild1_ = routenBild1_;
        this.routenBild2_ = routenBild2_; this.routenBild3_ = routenBild3_;
        this.routenInfo1DE_ = routenInfo1DE_;
        this.routenInfo2DE_ = routenInfo2DE_;
        this.routenInfo3DE_ = routenInfo3DE_;
        this.routenInfo4DE_ = routenInfo4DE_;
        this.routenInfo5DE_ = routenInfo5DE_;
        this.routenInfo6DE_ = routenInfo6DE_;
        this.routenInfo7DE_ = routenInfo7DE_;
        this.routenInfo1EN_ = routenInfo1EN_;
        this.routenInfo2EN_ = routenInfo2EN_;
        this.routenInfo3EN_ = routenInfo3EN_;
        this.routenInfo4EN_ = routenInfo4EN_;
        this.routenInfo5EN_ = routenInfo5EN_;
        this.routenInfo6EN_ = routenInfo6EN_;
        this.routenInfo7EN_ = routenInfo7EN_;
        this.routenInfo1CZ_ = routenInfo1CZ_;
        this.routenInfo2CZ_ = routenInfo2CZ_;
        this.routenInfo3CZ_ = routenInfo3CZ_;
        this.routenInfo4CZ_ = routenInfo4CZ_;
        this.routenInfo5CZ_ = routenInfo5CZ_;
        this.routenInfo6CZ_ = routenInfo6CZ_;
        this.routenInfo7CZ_ = routenInfo7CZ_;
        this.routenID_ = routenID_;
        this.routenKatID_ = routenKatID_;
    }

    public String getRoutenName_() {
        return routenName_;
    }

    public String getRoutenKml_() {
        return routenKml_;
    }

    public String getRoutenBild1_() {
        return routenBild1_;
    }

    public String getRoutenBild2_() {
        return routenBild2_;
    }

    public String getRoutenBild3_() {
        return routenBild3_;
    }

    public String getRoutenInfo1DE_() {
        return routenInfo1DE_;
    }

    public String getRoutenInfo2DE_() {
        return routenInfo2DE_;
    }

    public String getRoutenInfo3DE_() {
        return routenInfo3DE_;
    }

    public String getRoutenInfo4DE_() {
        return routenInfo4DE_;
    }

    public String getRoutenInfo5DE_() {
        return routenInfo5DE_;
    }

    public String getRoutenInfo6DE_() {
        return routenInfo6DE_;
    }

    public String getRoutenInfo7DE_() {
        return routenInfo7DE_;
    }

    public String getRoutenInfo1EN_() {
        return routenInfo1EN_;
    }

    public String getRoutenInfo2EN_() {
        return routenInfo2EN_;
    }

    public String getRoutenInfo3EN_() {
        return routenInfo3EN_;
    }

    public String getRoutenInfo4EN_() {
        return routenInfo4EN_;
    }

    public String getRoutenInfo5EN_() {
        return routenInfo5EN_;
    }

    public String getRoutenInfo6EN_() {
        return routenInfo6EN_;
    }

    public String getRoutenInfo7EN_() {
        return routenInfo7EN_;
    }

    public String getRoutenInfo1CZ_() {
        return routenInfo1CZ_;
    }

    public String getRoutenInfo2CZ_() {
        return routenInfo2CZ_;
    }

    public String getRoutenInfo3CZ_() {
        return routenInfo3CZ_;
    }

    public String getRoutenInfo4CZ_() {
        return routenInfo4CZ_;
    }

    public String getRoutenInfo5CZ_() {
        return routenInfo5CZ_;
    }

    public String getRoutenInfo6CZ_() {
        return routenInfo6CZ_;
    }

    public String getRoutenInfo7CZ_() {
        return routenInfo7CZ_;
    }

    public int getRoutenID_() {
        return routenID_;
    }

    public int getRoutenKatID_() {
        return routenKatID_;
    }

    public String getPathBild1_() {
        return pathBild1_;
    }

    public void setPathBild1_(String pathBild1_) {
        this.pathBild1_ = pathBild1_;
    }

    public String getPathBild2_() {
        return pathBild2_;
    }

    public void setPathBild2_(String pathBild2_) {
        this.pathBild2_ = pathBild2_;
    }

    public String getPathBild3_() {
        return pathBild3_;
    }

    public void setPathBild3_(String pathBild3_) {
        this.pathBild3_ = pathBild3_;
    }

    public String getPathKml_() {
        return pathKml_;
    }

    public void setPathKml_(String pathKml_) {
        this.pathKml_ = pathKml_;
    }

    public Date getDateKml_() {
        return dateKml_;
    }

    public void setDateKml_(Date dateKml_) {
        this.dateKml_ = dateKml_;
    }

    public Date getDateBild3_() {
        return dateBild3_;
    }

    public void setDateBild3_(Date dateBild3_) {
        this.dateBild3_ = dateBild3_;
    }

    public Date getDateBild2_() {
        return dateBild2_;
    }

    public void setDateBild2_(Date dateBild2_) {
        this.dateBild2_ = dateBild2_;
    }

    public Date getDateBild1_() {
        return dateBild1_;
    }

    public void setDateBild1_(Date dateBild1_) {
        this.dateBild1_ = dateBild1_;
    }
}
