package com.kg.megaregionapp.customer;

import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.kg.bar.app.AppConfig;
import com.kg.bar.app.AppController;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ASUS on 6/29/2017.
 */

public class CustomerHelper {

    private static final String TAG = com.kg.bar.customer.CustomerHelper.class.getSimpleName();

    public static void saveCustomer(final String sName, final String sPhone, final String sComp, final String sCity, final String sAddress, final String token) {
        // Tag used to cancel the request
        String tag_string_req = "req_save_customer";

        JSONObject jsonObject = new JSONObject();
        try {

            jsonObject.put("responsiblePerson", sName);
            jsonObject.put("phone", sPhone);
            jsonObject.put("address", sAddress);
            jsonObject.put("city", sCity);
            jsonObject.put("company", sComp);

        } catch (JSONException e) {
        }

        JsonObjectRequest req = new JsonObjectRequest(AppConfig.URL_CUSTOMER_SAVE, jsonObject,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "Save customer Response: " + response);
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Customer Saving Error: " + error.getMessage());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", token);
                return headers;
            }
        };
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(req, tag_string_req);
    }


    public static void updateCustomer(final String sName, final String sPhone, final String sComp, final String sCity, final String sAddress, final String id, final String token) {
        // Tag used to cancel the request
        String tag_string_req = "req_save_customer";

        JSONObject jsonObject = new JSONObject();
        try {

            jsonObject.put("responsiblePerson", sName);
            jsonObject.put("phone", sPhone);
            jsonObject.put("address", sAddress);
            jsonObject.put("city", sCity);
            jsonObject.put("company", sComp);
            jsonObject.put("customerId", id);

        } catch (JSONException e) {
        }

        JsonObjectRequest req = new JsonObjectRequest(AppConfig.URL_CUSTOMER_UPDATE, jsonObject,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "Save customer Response: " + response);
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Customer Saving Error: " + error.getMessage());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", token);
                return headers;
            }
        };
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(req, tag_string_req);
    }

    public static void saveCorporateCustomer(final String sPhone, final String sComp, final String sCity, final String sAddress, final String token) throws JSONException {
        // Tag used to cancel the request
        String tag_string_req = "req_save_customer";

        JSONObject jsonObject = new JSONObject();
        try {

            jsonObject.put("responsiblePerson", "");
            jsonObject.put("phone", sPhone);
            jsonObject.put("address", sAddress);
            jsonObject.put("city", sCity);
            jsonObject.put("company", sComp);

        } catch (JSONException e) {
            e.printStackTrace();
            throw e;
        }

        JsonObjectRequest req = new JsonObjectRequest(AppConfig.URL_CORPORATE_CUSTOMER_SAVE, jsonObject,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "Save corporate customer Response: " + response);
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Customer Saving Error: " + error.getMessage());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", token);
                return headers;
            }
        };
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(req, tag_string_req);
    }


    public static void updateCorporateCustomer(final String sPhone, final String sComp, final String sCity, final String sAddress, final String id, final String token) throws JSONException {
        // Tag used to cancel the request
        String tag_string_req = "req_save_customer";

        JSONObject jsonObject = new JSONObject();
        try {

            jsonObject.put("responsiblePerson", "");
            jsonObject.put("phone", sPhone);
            jsonObject.put("address", sAddress);
            jsonObject.put("city", sCity);
            jsonObject.put("company", sComp);
            jsonObject.put("customerId", id);

        } catch (JSONException e) {
            e.printStackTrace();
            throw e;
        }

        JsonObjectRequest req = new JsonObjectRequest(AppConfig.URL_CORPORATE_CUSTOMER_UPDATE, jsonObject,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "Save corporate customer Response: " + response);
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Customer Saving Error: " + error.getMessage());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", token);
                return headers;
            }
        };
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(req, tag_string_req);
    }


    public static void deleteCustomer(final String id, final String token) throws JSONException {
        // Tag used to cancel the request
        String tag_string_req = "req_delete_customer";

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("customerId", id);
        } catch (JSONException e) {
            e.printStackTrace();
            throw e;
        }

        JsonObjectRequest req = new JsonObjectRequest(AppConfig.URL_CUSTOMER_DELETE, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "delete customer Response: " + response);
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "delete customer  Error: " + error.getMessage());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", token);
                return headers;
            }
        };
        AppController.getInstance().addToRequestQueue(req, tag_string_req);
    }


}
