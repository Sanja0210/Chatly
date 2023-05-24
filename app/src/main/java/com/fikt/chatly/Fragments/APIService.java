package com.fikt.chatly.Fragments;

import com.fikt.chatly.Izvestuvanja.IResponse;
import com.fikt.chatly.Izvestuvanja.Isprakjac;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=BAB8oWidH0v_RuwzqUoc525lZho2peOJQAd-tq-P52L2oh7HiilaW_Q1Dlr3h4NnQZf0pR_KpuJeYSxF_8m7ecg"
    })

    @POST("fcm/send")
    Call<IResponse> sendNotification(@Body Isprakjac body);
}
