package com.speedride.driver.modules.userManagement.dataModel

class RatingDetail {
    /*public String getTotal_users() {
        return total_users;
    }

    public void setTotal_users(String total_users) {
        this.total_users = total_users;
    }*/
    //private String total_users;
    var average_rating: String? = null
    var starcount: List<starcount>? = null
    var reviewdata: List<reviewdata>? = null
    var total_rating_counts:TotalReviewCount?=null
}