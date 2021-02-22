package com.kg.megaregionapp.users;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.toolbox.JsonObjectRequest;
import com.kg.megaregionapp.HomeActivity;
import com.kg.megaregionapp.R;
import com.kg.megaregionapp.app.AppConfig;
import com.kg.megaregionapp.app.AppController;
import com.kg.megaregionapp.helper.SessionManager;
import com.kg.megaregionapp.utils.MyDialog;
import com.kg.megaregionapp.utils.NetworkUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

public class UpdateData extends AppCompatActivity {
    private static final String TAG = UpdateData.class.getSimpleName();

    private Button btn_update;
    private EditText txt_username;
    private EditText txt_fullname;
    private EditText txt_old_password;
    private EditText txt_new_password;
    private ProgressDialog pDialog;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_data);

        btn_update = findViewById(R.id.btnUpdate);
        txt_old_password = findViewById(R.id.old_password);
        txt_new_password = findViewById(R.id.new_password);
        txt_username = findViewById(R.id.username);
        txt_fullname = findViewById(R.id.fullname);

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        // session manager

        session = new SessionManager(getApplicationContext());
        String login = session.getLogin();
        String fulname = session.getNameSurname();

        // Displaying the user details on the screen
        txt_username.setText(login);
        txt_fullname.setText(fulname);

        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String old_password = txt_old_password.getText().toString().trim();
                String username = txt_username.getText().toString().trim();
                String new_password = txt_new_password.getText().toString().trim();
                String fullName = txt_fullname.getText().toString().trim();

                if (!old_password.isEmpty() && !username.isEmpty() && !new_password.isEmpty()) {
                    try {
                        updatePassword(old_password, username, new_password, fullName);
                        Intent intent = new Intent(UpdateData.this, HomeActivity.class);
                        startActivity(intent);
                        finish();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Please enter your details!", Toast.LENGTH_LONG)
                            .show();
                }
            }
        });
    }


    private void logoutUser() {
        session.setLogin(false, "2000-01-01");
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void updatePassword(final String old_password, final String username,
                                final String new_password, final String fullname) throws ParseException {

        if (!NetworkUtil.isNetworkConnected(UpdateData.this)) {
            MyDialog.createSimpleOkErrorDialog(UpdateData.this,
                    getApplicationContext().getString(R.string.dialog_error_title),
                    getApplicationContext().getString(R.string.check_internet)).show();
        } else if (NetworkUtil.isTokenExpired()) {
            MyDialog.createSimpleOkErrorDialog(UpdateData.this,
                    getApplicationContext().getString(R.string.dialog_error_title),
                    getApplicationContext().getString(R.string.relogin)).show();
        } else {
            // Tag used to cancel the request

            String tag_string_req = "req_update";
            pDialog.setMessage(getApplicationContext().getString(R.string.Processing));
            showDialog();

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("nameSurname", fullname);
                jsonObject.put("email", username);
                jsonObject.put("new_password", new_password);
                jsonObject.put("old_password", old_password);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JsonObjectRequest strReq = new JsonObjectRequest(AppConfig.URL_UPDATE_PSW, jsonObject,
                    response -> {
                        hideDialog();
                        try {
                            if (response.getString("userId").length() > 0) {
                                Toast.makeText(getApplicationContext(), "Пароль өзгөтүлдү!", Toast.LENGTH_LONG).show();
                                logoutUser();
                            } else {
                                Toast.makeText(UpdateData.this,  getApplicationContext().getString(R.string.NoData), Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(UpdateData.this,  getApplicationContext().getString(R.string.ErrorWhenLoading), Toast.LENGTH_LONG).show();
                        }
                    }, error -> {
                        NetworkUtil.checkHttpStatus(UpdateData.this, error);
                        hideDialog();
                    }) {

                @Override
                public Map<String, String> getHeaders() {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("Authorization", HomeActivity.token);
                    return headers;
                }
            };
            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
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
}
