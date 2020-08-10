package com.manu.connect.view.ui.fragments;

import com.manu.connect.notification.MyReponse;
import com.manu.connect.notification.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ApiService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAA05xeNMQ:APA91bFPnfAsg0IwKhzbOSL9btDAsgyqXLH-hhNgVqtIf7Zo69eDRpVAKA8Kw4-JTgkNKgEmCGYpVtGNeGcwFHJjGSbV6z5FTrr4LF_rM2dhNlphxEfpZeLUrYh5EfdYGxM6VlE0mygn"
    })

    @POST("fcm/send")
    Call<MyReponse> sendNotification(
            @Body Sender body);
}
