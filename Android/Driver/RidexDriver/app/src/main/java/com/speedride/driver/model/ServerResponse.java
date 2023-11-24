package com.speedride.driver.model;

import retrofit2.Response;

public class ServerResponse<T> {

    private int status;
    private String accessToken;
    private String message;
    private T data;
    private T dvehicle;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public T getDvehicle() {
        return dvehicle;
    }

    public void setDvehicle(T dvehicle) {
        this.dvehicle = dvehicle;
    }

    public int checkResponse(Response<ServerResponse> resp) {
        if (resp != null && resp.body() != null) {
            if (resp.body().getStatus() != 0) {
                return resp.body().getStatus();
            } else
                return 0;
        } else
            return 0;
    }

    public int checkResponse_(Response<ServerResponse<Data>> resp) {
        if (resp != null && resp.body() != null) {
            if (resp.body().getStatus() != 0) {
                return resp.body().getStatus();
            } else
                return 0;
        } else
            return 0;
    }
}
