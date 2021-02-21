package com.kg.megaregionapp.users;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.kg.megaregionapp.HomeActivity;
import com.kg.megaregionapp.R;
import com.kg.megaregionapp.app.AppConfig;
import com.kg.megaregionapp.app.AppController;
import com.kg.megaregionapp.helper.PostHelper;
import com.kg.megaregionapp.orders.OrderEntry;
import com.kg.megaregionapp.utils.MyDialog;
import com.kg.megaregionapp.utils.NetworkUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DeleteUser extends AppCompatActivity {
    private static final String TAG = OrderEntry.class.getSimpleName();
    private ArrayList<Users> usersList = new ArrayList<>();
    Spinner spinner_users;
    Button btn_delete_user;
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_user);

        spinner_users = (Spinner) findViewById(R.id.spinner_postman);
        btn_delete_user = (Button) findViewById(R.id.btnDeleteUser);

        try {
            PostHelper.listPostmans(HomeActivity.userCity, DeleteUser.this, spinner_users);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        btn_delete_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showAlert();
            }
        });

    }

    private void showAlert() {
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle(getResources().getString(R.string.TitleAttention));
        adb.setMessage(spinner_users.getSelectedItem().toString() + " " + getResources().getString(R.string.UserWillBeDeleted));
        adb.setPositiveButton("МАКУЛ", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                try {
                    deleteUser(spinner_users.getSelectedItem().toString());
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.UserDeleted), Toast.LENGTH_LONG).show();
                    finish();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });


        adb.setNegativeButton("ТОКТОТ", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        adb.show();
    }

    public void deleteUser(final String id) throws ParseException {

        if (!NetworkUtil.isNetworkConnected(DeleteUser.this)) {
            MyDialog.createSimpleOkErrorDialog(DeleteUser.this,
                    getApplicationContext().getString(R.string.dialog_error_title),
                    getApplicationContext().getString(R.string.check_internet)).show();
        } else if (NetworkUtil.isTokenExpired()) {
            MyDialog.createSimpleOkErrorDialog(DeleteUser.this,
                    getApplicationContext().getString(R.string.dialog_error_title),
                    getApplicationContext().getString(R.string.relogin)).show();
        } else {

            // Tag used to cancel the request
            String tag_string_req = "req_delete_user";

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("userId", id);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            JsonObjectRequest strReq = new JsonObjectRequest(AppConfig.URL_DELETE_USER, jsonObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d(TAG, "User Delete Response: " + response);
                        }
                    }, error -> NetworkUtil.checkHttpStatus(DeleteUser.this, error)) {

                @Override
                public Map<String, String> getHeaders() {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("Authorization", HomeActivity.token);
                    return headers;
                }
            };
            AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
        }
    }


}
