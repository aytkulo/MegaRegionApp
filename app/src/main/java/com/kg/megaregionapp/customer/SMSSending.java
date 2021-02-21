package com.kg.megaregionapp.customer;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.kg.bar.R;
import com.kg.bar.app.AppController;
import com.kg.bar.helper.SMSManager;
import com.kg.bar.helper.SQLiteHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SMSSending extends AppCompatActivity {

    private static final String TAG = com.kg.bar.customer.SMSSending.class.getSimpleName();
    private SQLiteHandler db;
    private EditText sms_content;
    private Button btn_send_sms;
    private Spinner spn_usersCity;
    private String usersCity = "";
    public static int DIALOG_ID = 1;
    public static int DIALOG_ID_2 = 2;

    EditText ed_Date1, ed_Date2;
    Calendar calendar;
    int year_x, month_x, day_x;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smssending);

        sms_content = findViewById(R.id.smsContent);
        btn_send_sms = findViewById(R.id.btn_send_sms);
        spn_usersCity = findViewById(R.id.spinner_usersCity);
        ed_Date1 = findViewById(R.id.ed_Date_Begin);
        ed_Date2 = findViewById(R.id.ed_Date_End);

        db = new SQLiteHandler(getApplicationContext());
        HashMap<String, String> user = db.getUserDetails();
        usersCity = user.get("city");

        spn_usersCity.setSelection(getIndex(spn_usersCity, usersCity));
        spn_usersCity.setEnabled(false);


        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");//dd/MM/yyyy
        Date now = new Date();
        String strDate = sdfDate.format(now);

        ed_Date1.setText(strDate);
        ed_Date2.setText(strDate);

        ed_Date1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(DIALOG_ID);
            }
        });

        ed_Date2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(DIALOG_ID_2);
            }
        });


        btn_send_sms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToAllCustomers(usersCity, ed_Date1.getText().toString(), ed_Date2.getText().toString());
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


    public void sendToAllCustomers(final String city, final String beginDate, final String endDate) {

        String tag_string_req = "req_list_customers";


        StringRequest strReq = new StringRequest(Request.Method.POST,
                "", new Response.Listener<String>() {

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
                params.put("city", city);
                params.put("date1", beginDate);
                params.put("date2", endDate);
                return params;
            }

        };
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }


    protected Dialog onCreateDialog(int id) {

        if (id == DIALOG_ID)
            return new DatePickerDialog(this, datePickerListener, year_x, month_x, day_x);
        else if (id == DIALOG_ID_2)
            return new DatePickerDialog(this, datePickerListener2, year_x, month_x, day_x);
        else
            return null;
    }


    protected DatePickerDialog.OnDateSetListener datePickerListener
            = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

            year_x = year;
            month_x = month;
            day_x = dayOfMonth;
            String dateS = String.valueOf(year);
            dateS = getDateInFormat(dateS, month_x, day_x);
            ed_Date1.setText(dateS);
        }
    };

    protected String getDateInFormat(String date, int month, int day) {

        if (month < 10)
            date = date + "-0" + month;
        else
            date = date + "-" + month;
        if (day < 10)
            date = date + "-0" + day;
        else
            date = date + "-" + day;
        return date;
    }

    protected DatePickerDialog.OnDateSetListener datePickerListener2
            = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

            year_x = year;
            month_x = month;
            day_x = dayOfMonth;
            String dateS = String.valueOf(year);
            dateS = getDateInFormat(dateS, month_x, day_x);
            ed_Date2.setText(dateS);
        }
    };

}
