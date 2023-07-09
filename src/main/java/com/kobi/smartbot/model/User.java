package com.kobi.smartbot.model;

import com.vk.api.sdk.objects.users.responses.GetResponse;
import org.json.simple.JSONObject;

public class User {

    private Long userId;
    private String firstName;
    private String lastName;
    private boolean online;
    private String city;
    private Boolean isFriend;
    private int sex;
    private String screenName;
    private String photo;


    public User() {

    }

    public User(JSONObject userJson) {

        userId = Long.parseLong(userJson.get("id").toString());
        firstName = userJson.get("first_name").toString();
        lastName = userJson.get("last_name").toString();

        if (userJson.get("online").toString().equals("1"))
            online = true;

        city = null;
        isFriend = null;
    }

    public User(GetResponse item) {

        this.userId = Long.valueOf(item.getId());
        this.firstName = item.getFirstName();
        this.lastName = item.getLastName();
        this.online = item.isOnline();
        this.isFriend = false;
        this.screenName = item.getScreenName();
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Boolean getFriend() {
        return isFriend;
    }

    public void setFriend(Boolean friend) {
        isFriend = friend;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }
}
