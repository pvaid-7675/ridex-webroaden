package com.speedride.driver.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

/**
 * Created by Payal K Goswami on 30-10-2022.
 */
public class DriverDetails implements Parcelable {
     public String  status = "";
     public String  rating_avg = "";
     public  VehicleDetails vehicle_details;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRating_avg() {
        return rating_avg;
    }

    public void setRating_avg(String rating_avg) {
        this.rating_avg = rating_avg;
    }

    public VehicleDetails getVehicle_details() {
        return vehicle_details;
    }

    public void setVehicle_details(VehicleDetails vehicle_details) {
        this.vehicle_details = vehicle_details;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getSocial_id() {
        return social_id;
    }

    public void setSocial_id(String social_id) {
        this.social_id = social_id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone_verified() {
        return phone_verified;
    }

    public void setPhone_verified(String phone_verified) {
        this.phone_verified = phone_verified;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getOn_duty() {
        return on_duty;
    }

    public void setOn_duty(String on_duty) {
        this.on_duty = on_duty;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getDrive_status() {
        return drive_status;
    }

    public void setDrive_status(String drive_status) {
        this.drive_status = drive_status;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLogin_type() {
        return login_type;
    }

    public void setLogin_type(String login_type) {
        this.login_type = login_type;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String  image = "";
     public String  lng = "";
     public String  social_id = "";
     public String  id = "";
     public String  name = "";
     public String  phone_verified = "";
     public String  updated_at = "";
     public String  email = "";
     public String  last_name = "";
     public String  created_at = "";
     public String  on_duty = "";
     public String  role = "";
     public String  drive_status = "";
     public String  lat = "";
     public String  login_type = "";
     public String  mobile = "";

    protected DriverDetails(Parcel in) {
        status = in.readString();
        rating_avg = in.readString();
        image = in.readString();
        lng = in.readString();
        social_id = in.readString();
        id = in.readString();
        name = in.readString();
        phone_verified = in.readString();
        updated_at = in.readString();
        email = in.readString();
        last_name = in.readString();
        created_at = in.readString();
        on_duty = in.readString();
        role = in.readString();
        drive_status = in.readString();
        lat = in.readString();
        login_type = in.readString();
        mobile = in.readString();
    }

    public static final Creator<DriverDetails> CREATOR = new Creator<DriverDetails>() {
        @Override
        public DriverDetails createFromParcel(Parcel in) {
            return new DriverDetails(in);
        }

        @Override
        public DriverDetails[] newArray(int size) {
            return new DriverDetails[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(status);
        dest.writeString(rating_avg);
        dest.writeString(image);
        dest.writeString(lng);
        dest.writeString(social_id);
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(phone_verified);
        dest.writeString(updated_at);
        dest.writeString(email);
        dest.writeString(last_name);
        dest.writeString(created_at);
        dest.writeString(on_duty);
        dest.writeString(role);
        dest.writeString(drive_status);
        dest.writeString(lat);
        dest.writeString(login_type);
        dest.writeString(mobile);
    }
}
