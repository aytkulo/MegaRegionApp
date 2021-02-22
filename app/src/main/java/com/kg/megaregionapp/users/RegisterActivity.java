package com.kg.megaregionapp.users;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.kg.megaregionapp.HomeActivity;
import com.kg.megaregionapp.R;
import com.kg.megaregionapp.app.AppConfig;
import com.kg.megaregionapp.app.AppController;
import com.kg.megaregionapp.helper.SQLiteHandler;
import com.kg.megaregionapp.helper.StringData;
import com.kg.megaregionapp.utils.MyDialog;
import com.kg.megaregionapp.utils.NetworkUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;


public class RegisterActivity extends Activity {
    private static final String TAG = RegisterActivity.class.getSimpleName();
    private Button btnRegister;
    private EditText inputFullName;
    private EditText inputEmail;
    private EditText inputPassword;
    private Spinner inputCity;
    private Spinner inputRole;
    private ProgressDialog pDialog;
    private SQLiteHandler db;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        inputFullName = findViewById(R.id.name);
        inputEmail = findViewById(R.id.email);
        inputPassword = findViewById(R.id.password);
        btnRegister = findViewById(R.id.btnRegister);
        inputCity = findViewById(R.id.spinner_city);
        inputRole = findViewById(R.id.spinner_role);

        ArrayAdapter<String> cityAdapterAll = new ArrayAdapter<String>(
                RegisterActivity.this,
                android.R.layout.simple_spinner_dropdown_item,
                StringData.getCityList()
        );
        inputCity.setAdapter(cityAdapterAll);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // Register Button Click event
        btnRegister.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                String name = inputFullName.getText().toString().trim();
                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();
                String city = inputCity.getSelectedItem().toString();
                String role = inputRole.getSelectedItem().toString();

                if (registerDataCheck() && !city.isEmpty() && !role.isEmpty()) {
                    try {
                        registerUser(name, email, password, city, role);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Маалымат толук киргизилбеди!", Toast.LENGTH_LONG)
                            .show();
                }
            }
        });
    }

    /**
     * Function to store user in MySQL database will post params(tag, name,
     * email, password) to register url
     */
    private void registerUser(final String name, final String email,
                              final String password, final String city, final String role) throws ParseException {

        if (!NetworkUtil.isNetworkConnected(RegisterActivity.this)) {
            MyDialog.createSimpleOkErrorDialog(RegisterActivity.this,
                    getApplicationContext().getString(R.string.dialog_error_title),
                    getApplicationContext().getString(R.string.check_internet)).show();
        } else {
            // Tag used to cancel the request
            String tag_string_req = "req_register";

            pDialog.setMessage(getApplicationContext().getString(R.string.Processing));
            showDialog();

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("nameSurname", name);
                jsonObject.put("email", email);
                jsonObject.put("password", password);
                jsonObject.put("city", city);
                jsonObject.put("role", role);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JsonObjectRequest strReq = new JsonObjectRequest(AppConfig.URL_REGISTER, jsonObject,
                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d(TAG, "Register Response: " + response.toString());
                            hideDialog();

                            try {
                                if (response.getString("userId").length() > 0) {

                                    Toast.makeText(getApplicationContext(), "Колдонуучу ийгиликтүү катталды!", Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    MyDialog.createSimpleOkErrorDialog(RegisterActivity.this,
                                            getApplicationContext().getString(R.string.dialog_error_title),
                                            getApplicationContext().getString(R.string.NoData)).show();
                                }
                            } catch (JSONException e) {
                                MyDialog.createSimpleOkErrorDialog(RegisterActivity.this,
                                        getApplicationContext().getString(R.string.dialog_error_title),
                                        getApplicationContext().getString(R.string.ErrorWhenLoading)).show();
                            }
                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    if (error instanceof ServerError) {
                        if (error.networkResponse.headers.containsKey("X-Android-Response-Source")) {
                            String value = error.networkResponse.headers.get("X-Android-Response-Source");
                            if (value.equalsIgnoreCase("NETWORK 409")) {
                                new AlertDialog.Builder(RegisterActivity.this)
                                        .setTitle(getApplicationContext().getResources().getString(R.string.Attention))
                                        .setMessage(getApplicationContext().getResources().getString(R.string.RecordAlreadyExists))
                                        .setPositiveButton(getApplicationContext().getResources().getString(R.string.OK), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                            }
                                        })
                                        .show().setCanceledOnTouchOutside(false);
                            }
                        }

                    } else {
                        Toast.makeText(getApplicationContext(), getApplicationContext().getResources().getString(R.string.server_error), Toast.LENGTH_LONG).show();
                    }
                    hideDialog();
                }
            }) {

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


    private boolean registerDataCheck() {

        boolean ok = true;
        String message = getResources().getString(R.string.FillAllDataCorrectly);

        if (inputFullName.length() < 0) {
            inputFullName.setBackground(getShape(Color.MAGENTA));
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            return false;
        } else {
            inputFullName.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        }

        if (inputEmail.length() < 1) {
            inputEmail.setBackground(getShape(Color.MAGENTA));
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            return false;
        } else {
            inputEmail.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        }

        if (inputPassword.length() < 8) {
            inputPassword.setBackground(getShape(Color.MAGENTA));
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.PasswordShouldBeMoreThanFourCharacters), Toast.LENGTH_LONG).show();
            return false;
        } else if (inputPassword.length() > 16) {
            inputPassword.setBackground(getShape(Color.MAGENTA));
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.PasswordShouldBeLessThanTenCharacters), Toast.LENGTH_LONG).show();
            return false;
        } else {
            inputPassword.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        }
        return ok;
    }

    private ShapeDrawable getShape(int color) {

        ShapeDrawable shape = new ShapeDrawable(new RectShape());
        shape.getPaint().setColor(color);
        shape.getPaint().setStyle(Paint.Style.STROKE);
        shape.getPaint().setStrokeWidth(3);

        return shape;
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
