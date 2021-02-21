package com.kg.megaregionapp.customer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.kg.bar.R;
import com.kg.bar.app.AppConfig;
import com.kg.bar.app.AppController;
import com.kg.bar.helper.SMSManager;
import com.kg.bar.helper.SQLiteHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SMSSendingOld extends AppCompatActivity {

    private static final String TAG = com.kg.bar.customer.SMSSendingOld.class.getSimpleName();
    private SQLiteHandler db;
    private EditText sms_content;
    private Button btn_send_sms;
    private Spinner spn_usersCity;
    private String usersCity = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smssending);

        sms_content = (EditText) findViewById(R.id.smsContent);
        btn_send_sms = (Button) findViewById(R.id.btn_send_sms);
        spn_usersCity = (Spinner) findViewById(R.id.spinner_usersCity);

        db = new SQLiteHandler(getApplicationContext());
        HashMap<String, String> user = db.getUserDetails();
        usersCity = user.get("city");

        spn_usersCity.setSelection(getIndex(spn_usersCity, usersCity));
        spn_usersCity.setEnabled(false);


        btn_send_sms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToAllCustomers(usersCity);
            }
        });
    }

    private int getIndex(Spinner spinner, String myString) {

        int index = 0;

        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).equals(myString)) {
                index = i;
            }
        }
        return index;
    }


    public void sendToAllCustomers(final String city) {

        String tag_string_req = "req_list_customers";


        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_CUSTOMER_GET, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "List customers Response: " + response);

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    // Check for error node in json
                    if (!error) {

                        JSONArray customer = jObj.getJSONArray("customers");
                        for (int i = 0; i < customer.length(); i++) {
                            JSONObject c = customer.getJSONObject(i);
                            // Storing each json item in variable
                            SMSManager.sendCustomerSMS(c.getString("phone"), sms_content.getText().toString());
                        }

                        Bundle b = new Bundle();
                        b.putString("STATUS", "OK");
                        Intent intent = new Intent();
                        intent.putExtras(b);
                        setResult(RESULT_OK, intent);
                        finish();

                    } else {
                        // Error in login. Get the error message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Customer listing Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<>();
                params.put("address", "%");
                params.put("city", city + "%");
                params.put("phone", "%");
                params.put("name", "%");
                params.put("company", "%");
                return params;
            }

        };
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }
}
