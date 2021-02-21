package com.kg.megaregionapp.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.VolleyError;
import com.kg.megaregionapp.HomeActivity;
import com.kg.megaregionapp.R;

import java.text.ParseException;
import java.util.Date;

public class NetworkUtil {

    /**
     * Returns true if the Throwable is an instance of RetrofitError with an
     * http status code equals to the given one.
     */

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    public static void checkConnection(Context context) {
        if (!NetworkUtil.isNetworkConnected(context)) {
            MyDialog.createSimpleOkErrorDialog(context,
                    context.getString(R.string.dialog_error_title),
                    context.getString(R.string.check_internet)).show();
        }
    }

    public static boolean isTokenExpired() throws ParseException {
        try {
            if (HomeActivity.apiDate == null || HomeActivity.apiDate.length() < 1)
                return true;
            else {
                long tillDate = Long.parseLong(HomeActivity.apiDate);
                return tillDate < new Date().getTime();
            }
        } catch (Exception e) {
            return true;
        }
    }

    public static void checkHttpStatus(Context context, VolleyError error) {
        if (error instanceof AuthFailureError) {
            Toast.makeText(context, "Бул операция үчүн уруксатыңыз жок!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, context.getString(R.string.server_error), Toast.LENGTH_LONG).show();
        }

    }
}
