package com.kg.megaregionapp.users;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.kg.bar.HomeActivity;
import com.kg.bar.R;
import com.kg.bar.app.AppConfig;
import com.kg.bar.app.AppController;
import com.kg.bar.helper.SessionManager;
import com.kg.bar.utils.MyDialog;
import com.kg.bar.utils.NetworkUtil;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends Activity {
    private static final String TAG = com.kg.bar.users.LoginActivity.class.getSimpleName();

    private EditText inputEmail;
    private EditText inputPassword;
    private ProgressDialog pDialog;
    private Button russian, kyrgyz;
    private SessionManager session;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inputEmail = findViewById(R.id.email);
        inputPassword = findViewById(R.id.password);
        Button btnLogin = findViewById(R.id.btnLogin);
        russian = findViewById(R.id.btn_russian);
        kyrgyz = findViewById(R.id.btn_kyrgyz);

        /*
        kyrgyz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Resources resources = getResources();
                Configuration configuration = resources.getConfiguration();
                Locale locale = new Locale("en");
                configuration.setLocale(locale);
                getBaseContext().createConfigurationContext(configuration);
                showAlert();
            }
        });

        russian.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Resources resources = getResources();
                Configuration configuration = resources.getConfiguration();
                Locale locale = new Locale("ru");
                configuration.setLocale(locale);
                getBaseContext().createConfigurationContext(configuration);
                showAlert();
            }
        });


         */

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        session = new SessionManager(getApplicationContext());

        // Login button Click Event
        btnLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {

                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();

                // Check for empty data in the form
                if (!email.isEmpty() && !password.isEmpty()) {
                    // login user
                    checkLogin(email, password);
                } else {
                    // Prompt user to enter credentials
                    Toast.makeText(getApplicationContext(),
                            "Учетные данные...", Toast.LENGTH_LONG)
                            .show();
                }

            }

        });

    }

    private void showAlert() {
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setMessage(getResources().getString(R.string.LanguageChanged));
        adb.setIcon(R.drawable.icon_delivery_onway);

        adb.setPositiveButton("МАКУЛ", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        adb.show();
    }

    @Override
    public void onBackPressed() {

    }

    private void checkLogin(final String email, final String password) {

        if (!NetworkUtil.isNetworkConnected(com.kg.bar.users.LoginActivity.this)) {
            MyDialog.createSimpleOkErrorDialog(com.kg.bar.users.LoginActivity.this,
                    getApplicationContext().getString(R.string.dialog_error_title),
                    getApplicationContext().getString(R.string.check_internet)).show();
        } else {  // Tag used to cancel the request
            String tag_string_req = "req_login";

            pDialog.setMessage("Вход...");
            showDialog();

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("email", email);
                jsonObject.put("password", password);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JsonObjectRequest req = new JsonObjectRequest(AppConfig.URL_LOGIN, jsonObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            hideDialog();
                            try {

                                String token = response.getString("Authorization");
                                String role = response.getString("UserRole");
                                String login = response.getString("UserLogin");
                                String city = response.getString("UserCity");
                                String name = response.getString("UserName");
                                String tillDate = response.getString("TillDate");

                                session.setUser(token, role, city, login, name, tillDate);

                                HomeActivity.token = token;
                                HomeActivity.userCity = city;
                                HomeActivity.userLogin = login;
                                HomeActivity.apiDate = tillDate;
                                HomeActivity.userRole = role;

                                Intent intent = new Intent();
                                setResult(RESULT_OK, intent);
                                    finish();

                            } catch (JSONException e) {
                                Toast.makeText(com.kg.bar.users.LoginActivity.this,  getApplicationContext().getString(R.string.ErrorWhenLoading), Toast.LENGTH_LONG).show();
                            }
                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(com.kg.bar.users.LoginActivity.this, "Неверны учетные данные!", Toast.LENGTH_LONG).show();
                    hideDialog();
                }
            }) {

            };

            // Adding request to request queue
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
}
