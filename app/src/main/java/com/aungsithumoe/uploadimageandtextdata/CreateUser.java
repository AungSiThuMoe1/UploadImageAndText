package com.aungsithumoe.uploadimageandtextdata;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface CreateUser {
    @POST("createuser.php")
    @Headers({
            "Accept: application/json",
            "Content-Type: application/json"
    })
    Call<String> createUser(@Body Users users);
}
