package com.kg.megaregionapp.users;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.kg.megaregionapp.HomeActivity;
import com.kg.megaregionapp.R;
import com.kg.megaregionapp.app.AppConfig;
import com.kg.megaregionapp.app.AppController;
import com.kg.megaregionapp.helper.CustomJsonArrayRequest;
import com.kg.megaregionapp.utils.MyDialog;
import com.kg.megaregionapp.utils.NetworkUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserPermission extends AppCompatActivity {

    Switch permissionSwitch;
    private ProgressDialog pDialog;
    ListView listViewPermissions;

    List<PermissionObject> permissionList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_permission);

        listViewPermissions = findViewById(R.id.listPostmanPermissions);

        permissionSwitch = findViewById(R.id.switch_permission);
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);


        try {
            getPermission();
            listPermissions();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        permissionSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                try {
                    updatePermission(isChecked);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });

        listViewPermissions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                PermissionObject pObject = (PermissionObject) parent.getItemAtPosition(position);
            }
        });


    }


    private void updatePermission(final boolean isChecked) throws ParseException {

        if (!NetworkUtil.isNetworkConnected(UserPermission.this)) {
            MyDialog.createSimpleOkErrorDialog(UserPermission.this,
                    getApplicationContext().getString(R.string.dialog_error_title),
                    getApplicationContext().getString(R.string.check_internet)).show();
        } else if (NetworkUtil.isTokenExpired()) {
            MyDialog.createSimpleOkErrorDialog(UserPermission.this,
                    getApplicationContext().getString(R.string.dialog_error_title),
                    getApplicationContext().getString(R.string.relogin)).show();
        } else {
            String tag_string_req = "req_update_user_permission";

            pDialog.setMessage(getApplicationContext().getString(R.string.Processing));
            showDialog();

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("permission", isChecked);
                jsonObject.put("user", HomeActivity.userLogin);
                jsonObject.put("postman", "G");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JsonObjectRequest req = new JsonObjectRequest(AppConfig.URL_USER_PERMISSION, jsonObject,
                    response -> hideDialog(), error -> {
                        NetworkUtil.checkHttpStatus(UserPermission.this, error);
                        hideDialog();
                    }) {
                @Override
                public Map<String, String> getHeaders() {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("Authorization", HomeActivity.token);
                    return headers;
                }
            };
            AppController.getInstance().addToRequestQueue(req, tag_string_req);
        }
    }


    private void getPermission() throws ParseException {

        if (!NetworkUtil.isNetworkConnected(UserPermission.this)) {
            MyDialog.createSimpleOkErrorDialog(UserPermission.this,
                    getApplicationContext().getString(R.string.dialog_error_title),
                    getApplicationContext().getString(R.string.check_internet)).show();
        } else if (NetworkUtil.isTokenExpired()) {
            MyDialog.createSimpleOkErrorDialog(UserPermission.this,
                    getApplicationContext().getString(R.string.dialog_error_title),
                    getApplicationContext().getString(R.string.relogin)).show();
        } else {
            String tag_string_req = "req_update_user_permission";

            pDialog.setMessage("Загрузка данных ...");
            showDialog();

            JsonObjectRequest req = new JsonObjectRequest(AppConfig.URL_GET_USER_PERMISSION+"/G", null,
                    response -> {
                        hideDialog();
                        if (response != null)
                        {
                            JsonParser parser = new JsonParser();
                            Gson gson = new Gson();
                            JsonElement mJsonM = parser.parse(response.toString());
                            Permission dd = gson.fromJson(mJsonM, Permission.class);
                            if(!dd.permission)
                                permissionSwitch.setChecked(true);
                            else
                                permissionSwitch.setChecked(false);
                        }
                    }, error -> {
                        NetworkUtil.checkHttpStatus(UserPermission.this, error);
                        hideDialog();
                    }) {
                @Override
                public Map<String, String> getHeaders() {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("Authorization", HomeActivity.token);
                    return headers;
                }
            };
            AppController.getInstance().addToRequestQueue(req, tag_string_req);
        }
    }


    private void listPermissions() throws ParseException {

        if (!NetworkUtil.isNetworkConnected(UserPermission.this)) {
            MyDialog.createSimpleOkErrorDialog(UserPermission.this,
                    getApplicationContext().getString(R.string.dialog_error_title),
                    getApplicationContext().getString(R.string.check_internet)).show();
        } else if (NetworkUtil.isTokenExpired()) {
            MyDialog.createSimpleOkErrorDialog(UserPermission.this,
                    getApplicationContext().getString(R.string.dialog_error_title),
                    getApplicationContext().getString(R.string.relogin)).show();
        } else {
            String tag_string_req = "req_list_user_permission";

            pDialog.setMessage("Загрузка данных ...");
            showDialog();

            CustomJsonArrayRequest req = new CustomJsonArrayRequest(Request.Method.GET, AppConfig.URL_LIST_PERMISSIONS, null,
                    response -> {
                        hideDialog();


                        try {
                            // Check for error node in json
                            if (response.length() > 0) {

                                JsonParser parser = new JsonParser();
                                Gson gson = new Gson();

                                for (int i = 0; i < response.length(); i++) {

                                    JsonElement mJsonM = parser.parse(response.getString(i));
                                    PermissionObject dd = gson.fromJson(mJsonM, PermissionObject.class);
                                    permissionList.add(dd);
                                }

                                if (permissionList.size() > 0) {
                                    PermissionListAdapter orderListAdapter = new PermissionListAdapter(permissionList, UserPermission.this);
                                    listViewPermissions.setAdapter(orderListAdapter);
                                }

                            } else {
                                Toast.makeText(UserPermission.this, getApplicationContext().getString(R.string.NoData), Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(UserPermission.this, getApplicationContext().getString(R.string.ErrorWhenLoading), Toast.LENGTH_LONG).show();
                        }
                    }, error -> {
                        NetworkUtil.checkHttpStatus(UserPermission.this, error);
                        hideDialog();
                    }) {
                @Override
                public Map<String, String> getHeaders() {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("Authorization", HomeActivity.token);
                    return headers;
                }
            };
            AppController.getInstance().addToRequestQueue(req, tag_string_req);
        }
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    private class Permission
    {
        boolean permission;
    }

}


