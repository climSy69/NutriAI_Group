package com.example.advancedcomputersciencecn6008_1;

import android.content.Context;
import android.content.SharedPreferences;

public class UserSession {
    private static final String PREF_NAME = "UserSessionPref";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_ACCESS_TOKEN = "accessToken";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_USERNAME = "username";

    private static UserSession instance;
    private String userId;
    private String accessToken;
    private String email;
    private String username;
    private final SharedPreferences sharedPreferences;

    private UserSession(Context context) {
        sharedPreferences = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.userId = sharedPreferences.getString(KEY_USER_ID, null);
        this.accessToken = sharedPreferences.getString(KEY_ACCESS_TOKEN, null);
        this.email = sharedPreferences.getString(KEY_EMAIL, null);
        this.username = sharedPreferences.getString(KEY_USERNAME, null);
    }

    public static synchronized UserSession getInstance(Context context) {
        if (instance == null) {
            instance = new UserSession(context);
        }
        return instance;
    }

    public static UserSession getInstance() {
        return instance;
    }

    public void setSession(String userId, String accessToken, String email) {
        setSession(userId, accessToken, email, this.username);
    }

    public void setSession(String userId, String accessToken, String email, String username) {
        this.userId = userId;
        this.accessToken = accessToken;
        this.email = email;
        this.username = username;

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_ACCESS_TOKEN, accessToken);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_USERNAME, username);
        editor.apply();
    }

    public void setUsername(String username) {
        this.username = username;
        sharedPreferences.edit().putString(KEY_USERNAME, username).apply();
    }

    public void clearSession() {
        this.userId = null;
        this.accessToken = null;
        this.email = null;
        this.username = null;

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    public String getUserId() {
        return userId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public boolean isLoggedIn() {
        return userId != null && accessToken != null;
    }
}
