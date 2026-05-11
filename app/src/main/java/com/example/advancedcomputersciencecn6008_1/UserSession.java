package com.example.advancedcomputersciencecn6008_1;

import android.content.Context;
import android.content.SharedPreferences;

public class UserSession {
    private static final String PREF_NAME = "UserSessionPref";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_ACCESS_TOKEN = "accessToken";
    private static final String KEY_EMAIL = "email";

    private static UserSession instance;
    private String userId;
    private String accessToken;
    private String email;
    private final SharedPreferences sharedPreferences;

    private UserSession(Context context) {
        sharedPreferences = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        // Load persisted data on initialization
        this.userId = sharedPreferences.getString(KEY_USER_ID, null);
        this.accessToken = sharedPreferences.getString(KEY_ACCESS_TOKEN, null);
        this.email = sharedPreferences.getString(KEY_EMAIL, null);
    }

    public static synchronized UserSession getInstance(Context context) {
        if (instance == null) {
            instance = new UserSession(context);
        }
        return instance;
    }

    // Overload for cases where we know it's initialized
    public static UserSession getInstance() {
        return instance;
    }

    public void setSession(String userId, String accessToken, String email) {
        this.userId = userId;
        this.accessToken = accessToken;
        this.email = email;

        // Persist to SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_ACCESS_TOKEN, accessToken);
        editor.putString(KEY_EMAIL, email);
        editor.apply();
    }

    public void clearSession() {
        this.userId = null;
        this.accessToken = null;
        this.email = null;

        // Clear SharedPreferences
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

    public boolean isLoggedIn() {
        return userId != null && accessToken != null;
    }
}
