package com.speedride.driver.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

/**
 * Created by Payal K Goswami on 30-10-2022.
 */
public class BookRide implements Parcelable {
     public String dlong = "";
     public String paymode = "";
     public String dlat = "";
     public String vt_id = "";
     public String status = "";
     public Estimate estimate = new Estimate();
     public String km = "";
     public String id = "";
     public String estimate_time = "";

     public BookRide(){

     }
    public String getDlong() {
        return dlong;
    }

    public void setDlong(String dlong) {
        this.dlong = dlong;
    }

    public String getPaymode() {
        return paymode;
    }

    public void setPaymode(String paymode) {
        this.paymode = paymode;
    }

    public String getDlat() {
        return dlat;
    }

    public void setDlat(String dlat) {
        this.dlat = dlat;
    }

    public String getVt_id() {
        return vt_id;
    }

    public void setVt_id(String vt_id) {
        this.vt_id = vt_id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Estimate getEstimate() {
        return estimate;
    }

    public void setEstimate(Estimate estimate) {
        this.estimate = estimate;
    }

    public String getKm() {
        return km;
    }

    public void setKm(String km) {
        this.km = km;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPlat() {
        return plat;
    }

    public void setPlat(String plat) {
        this.plat = plat;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public DriverDetails getDriver_details() {
        return driver_details;
    }

    public void setDriver_details(DriverDetails driver_details) {
        this.driver_details = driver_details;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getPlong() {
        return plong;
    }

    public void setPlong(String plong) {
        this.plong = plong;
    }

    public String getCoupon() {
        return coupon;
    }

    public void setCoupon(String coupon) {
        this.coupon = coupon;
    }

    public String getCustomer_id() {
        return customer_id;
    }

    public void setCustomer_id(String customer_id) {
        this.customer_id = customer_id;
    }

    public String plat = "";
     public String updated_at = "";
     public DriverDetails driver_details = null;
     public String created_at = "";
     public String plong = "";
     public String coupon = "";
     public String customer_id = "";

    public BookRide(Parcel in) {
    }

    public static final Creator<BookRide> CREATOR = new Creator<BookRide>() {
        @Override
        public BookRide createFromParcel(Parcel in) {
            return new BookRide(in);
        }

        @Override
        public BookRide[] newArray(int size) {
            return new BookRide[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
    }
}
