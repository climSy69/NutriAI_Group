package com.example.advancedcomputersciencecn6008_1;

import android.content.Context;
import android.content.SharedPreferences;

public class UserSession {
    private static final String PREF_NAME = "UserSessionPref";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_ACCESS_TOKEN = "accessToken";

    private static UserSession instance;
    private String userId;
    private String accessToken;
    private final SharedPreferences sharedPreferences;

    private UserSession(Context context) {
        sharedPreferences = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        // Load persisted data on initialization
        this.userId = sharedPreferences.getString(KEY_USER_ID, null);
        this.accessToken = sharedPreferences.getString(KEY_ACCESS_TOKEN, null);
    }

    public static synchronized UserSession getInstance(Context context) {
        if (instance == null) {
            instance = new UserSession(context);
        }
        return instance;
    }

    // Overload for cases where we know it's initialized (like after LoginActivity)
    public static UserSession getInstance() {
        return instance;
    }

    public void setSession(String userId, String accessToken) {
        this.userId = userId;
        this.accessToken = accessToken;

        // Persist to SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_ACCESS_TOKEN, accessToken);
        editor.apply();
    }

    public void clearSession() {
        this.userId = null;
        this.accessToken = null;

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

    public boolean isLoggedIn() {
        return userId != null && accessToken != null;
    }
}
