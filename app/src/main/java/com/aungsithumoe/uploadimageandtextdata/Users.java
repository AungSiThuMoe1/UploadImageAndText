package com.aungsithumoe.uploadimageandtextdata;

public class Users {
    String username;
    String password;
    String photo;

    public Users(String username, String password, String photo) {
        this.username = username;
        this.password = password;
        this.photo = photo;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }
}
