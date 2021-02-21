package com.kg.megaregionapp.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

public class SessionManager {
    // LogCat tag
    private static String TAG = SessionManager.class.getSimpleName();

    // Shared Preferences
    SharedPreferences pref;

    Editor editor;
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Shared preferences file name
    private static final String PREF_NAME = "YldamAdminLogin";

    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_LOGGIN_DATE = "loginDate";

    private static final String KEY_TOKEN = "TOKEN";
    private static final String KEY_ROLE = "ROLE";
    private static final String KEY_CITY = "CITY";
    private static final String KEY_USERNAME = "USERNAME";
    private static final String KEY_NAMESURNAME = "NAMESURNAME";
    private static final String KEY_TILLDATE = "TILLDATE";

    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void setLogin(boolean isLoggedIn, String loginDate) {
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);
        editor.putString(KEY_LOGGIN_DATE, loginDate);
        editor.commit();
        Log.d(TAG, "User login session modified!");
    }

    public void setUser(String token, String role, String city, String username, String name, String tillDate) {

        editor.putString(KEY_TOKEN, token);
        editor.putString(KEY_CITY, city);
        editor.putString(KEY_ROLE, role);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_NAMESURNAME, name);
        editor.putString(KEY_TILLDATE, tillDate);
        editor.commit();
        Log.d(TAG, "User login session modified!");
    }

    public String getApiDate(){
        return pref.getString(KEY_TILLDATE, "");
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getLoginDate() {
        return pref.getString(KEY_LOGGIN_DATE, "2000-01-01");
    }

    public String getRole() {
        return pref.getString(KEY_ROLE, "POSTMAN");
    }

    public String getCity() {
        return pref.getString(KEY_CITY, "");
    }

    public String getLogin() {
        return pref.getString(KEY_USERNAME, "");
    }

    public String getNameSurname() {
        return pref.getString(KEY_NAMESURNAME, "");
    }

    public String getToken(){
        return pref.getString(KEY_TOKEN, "");
    }

    public String getTillDate() {
        return pref.getString(KEY_TILLDATE, "");
    }


    public void setToken(String token) {
        editor.putString(KEY_TOKEN, token);
        editor.commit();
    }

    public void setTillDate(String tillDate) {
        editor.putString(KEY_TILLDATE, tillDate);
        editor.commit();
    }

    public void seRole(String role) {
        editor.putString(KEY_ROLE, role);
        editor.commit();
    }
}
