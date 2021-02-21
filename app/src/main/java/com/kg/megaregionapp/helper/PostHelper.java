package com.kg.megaregionapp.helper;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.android.volley.Request;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.kg.bar.HomeActivity;
import com.kg.bar.R;
import com.kg.bar.app.AppConfig;
import com.kg.bar.app.AppController;
import com.kg.bar.orders.Sector;
import com.kg.bar.users.User;
import com.kg.bar.utils.MyDialog;
import com.kg.bar.utils.NetworkUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PostHelper {

    static ArrayList<User> userList = new ArrayList<>();
    static ArrayList<Sector> sectorList = new ArrayList<>();

    public static void listPostmans(final String city, final Context context, final Spinner postmans) throws ParseException {

        if (!NetworkUtil.isNetworkConnected(context)) {
            MyDialog.createSimpleOkErrorDialog(context,
                    context.getString(R.string.dialog_error_title),
                    context.getString(R.string.check_internet)).show();
        } else if (NetworkUtil.isTokenExpired()) {
            MyDialog.createSimpleOkErrorDialog(context,
                    context.getString(R.string.dialog_error_title),
                    context.getString(R.string.relogin)).show();
        } else {
            String tag_string_req = "req_get_deliveries";

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("city", city);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            com.kg.bar.helper.CustomJsonArrayRequest req = new com.kg.bar.helper.CustomJsonArrayRequest(Request.Method.POST, AppConfig.URL_GET_USERS, jsonObject,
                    response -> {

                        try {
                            if (response.length() > 0) {

                                userList.clear();
                                JsonParser parser = new JsonParser();
                                Gson gson = new Gson();

                                for (int i = 0; i < response.length(); i++) {

                                    JsonElement mJsonM = parser.parse(response.getString(i));
                                    User dd = gson.fromJson(mJsonM, User.class);
                                    userList.add(dd);
                                }

                                if (userList.size() > 0) {
                                    populateSpinner(context, postmans);
                                }

                            } else {
                                MyDialog.createSimpleOkErrorDialog(context,
                                        context.getString(R.string.dialog_error_title),
                                        context.getString(R.string.NoData)).show();
                            }
                        } catch (JSONException e) {
                            MyDialog.createSimpleOkErrorDialog(context,
                                    context.getString(R.string.dialog_error_title),
                                    context.getString(R.string.ErrorWhenLoading)).show();
                        }

                    }, error -> NetworkUtil.checkHttpStatus(context, error)) {

                @Override
                public Map<String, String> getHeaders() {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("Authorization", HomeActivity.token);
                    return headers;
                }

            };
            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(req, tag_string_req);
        }
    }


    public static void listSectors(final String city, final Context context, final Spinner incomingSpinner) throws ParseException {

        if (!NetworkUtil.isNetworkConnected(context)) {
            MyDialog.createSimpleOkErrorDialog(context,
                    context.getString(R.string.dialog_error_title),
                    context.getString(R.string.check_internet)).show();
        } else if (NetworkUtil.isTokenExpired()) {
            MyDialog.createSimpleOkErrorDialog(context,
                    context.getString(R.string.dialog_error_title),
                    context.getString(R.string.relogin)).show();
        } else {
            String tag_string_req = "req_get_deliveries";

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("city", city);
                jsonObject.put("sector", "");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            com.kg.bar.helper.CustomJsonArrayRequest req = new com.kg.bar.helper.CustomJsonArrayRequest(Request.Method.POST, AppConfig.URL_GET_SECTORS, jsonObject,
                    response -> {

                        try {
                            if (response.length() > 0) {

                                sectorList.clear();
                                JsonParser parser = new JsonParser();
                                Gson gson = new Gson();

                                for (int i = 0; i < response.length(); i++) {

                                    JsonElement mJsonM = parser.parse(response.getString(i));
                                    Sector dd = gson.fromJson(mJsonM, Sector.class);
                                    sectorList.add(dd);
                                }

                                if (sectorList.size() > 0) {
                                    populateSectorSpinner(context, incomingSpinner);
                                }

                            }
                        } catch (JSONException e) {

                        }

                    }, error -> NetworkUtil.checkHttpStatus(context, error)) {

                @Override
                public Map<String, String> getHeaders() {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("Authorization", HomeActivity.token);
                    return headers;
                }

            };
            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(req, tag_string_req);
        }
    }

    private static void populateSpinner(Context context, Spinner postmans) {

        postmans.setAdapter(null);
        ArrayList<String> lables = new ArrayList<String>();

        lables.add("%");
        for (int i = 0; i < userList.size(); i++) {
            lables.add(userList.get(i).getEmail());
        }
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(context,
                android.R.layout.simple_spinner_item, lables);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        postmans.setAdapter(spinnerAdapter);
    }

    private static void populateSectorSpinner(Context context, Spinner postmans) {

        postmans.setAdapter(null);
        ArrayList<String> lables = new ArrayList<String>();

        lables.add("%");
        for (int i = 0; i < sectorList.size(); i++) {
            lables.add(sectorList.get(i).getSector());
        }
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(context,
                android.R.layout.simple_spinner_item, lables);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        postmans.setAdapter(spinnerAdapter);
    }


}

