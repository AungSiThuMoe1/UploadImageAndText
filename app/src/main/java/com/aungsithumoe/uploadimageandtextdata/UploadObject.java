package com.aungsithumoe.uploadimageandtextdata;

import com.google.gson.annotations.SerializedName;

public class UploadObject {
    @SerializedName("success")
    private String success;
    public UploadObject(String success) {
        this.success = success;
    }
    public String getSuccess() {
        return success;
    }
}