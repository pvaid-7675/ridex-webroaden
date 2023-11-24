package com.speedride.customer.model;

import com.speedride.customer.modules.main.model.BookRide;

import retrofit2.Response;

public class SoketResponse {

    private int status;
    private BookRide data;
    private String msg;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public BookRide getData() {
        return data;
    }

    public void setData(BookRide data) {
        this.data = data;
    }

    public String getMessage() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int checkResponse(Response<SoketResponse> resp) {
        if (resp != null && resp.body() != null) {
            if (resp.body().getStatus() != 0) {
                return resp.body().getStatus();
            } else
                return 0;
        } else
            return 0;
    }
}
