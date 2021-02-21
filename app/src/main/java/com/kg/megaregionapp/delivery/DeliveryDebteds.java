package com.kg.megaregionapp.delivery;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.kg.megaregionapp.HomeActivity;
import com.kg.megaregionapp.R;
import com.kg.megaregionapp.app.AppConfig;
import com.kg.megaregionapp.app.AppController;
import com.kg.megaregionapp.helper.CustomJsonArrayRequest;
import com.kg.megaregionapp.helper.PostHelper;
import com.kg.megaregionapp.helper.StringData;
import com.kg.megaregionapp.utils.MyDialog;
import com.kg.megaregionapp.utils.NetworkUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeliveryDebteds extends AppCompatActivity {

    ListView listViewDeliveries;
    public static int DIALOG_ID_BEGIN = 0;
    public static int DIALOG_ID_END = 1;

    private ProgressDialog pDialog;
    private EditText beginDate, endDate, senderPhone;
    private Spinner sCity, rCity;
    private Button btn_dList;
    private Calendar calendar;
    private String strDate = "";
    private String senderCity = "%", receiverCity="%";

    Delivery delivery;
    int year_x, month_x, day_x;
    private List<Delivery> deliveryList = new ArrayList<>();
    Dialog dialogPaying;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_debteds);

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        listViewDeliveries = findViewById(R.id.listViewDeliveries);
        beginDate = findViewById(R.id.beginDate);
        endDate = findViewById(R.id.enDate);
        btn_dList = findViewById(R.id.btn_dList);
        sCity = findViewById(R.id.sp_Origin);
        rCity = findViewById(R.id.sp_Destination);
        senderPhone = findViewById(R.id.senderPhone);

        ArrayAdapter<String> cityAdapterAll = new ArrayAdapter<String>(
                DeliveryDebteds.this,
                android.R.layout.simple_spinner_dropdown_item,
                StringData.getCityList()
        );
        sCity.setAdapter(cityAdapterAll);
        rCity.setAdapter(cityAdapterAll);

        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");//dd/MM/yyyy
        Date now = new Date();
        strDate = sdfDate.format(now);

        calendar = Calendar.getInstance();
        year_x = calendar.get(Calendar.YEAR);
        month_x = calendar.get(Calendar.MONTH);
        day_x = calendar.get(Calendar.DAY_OF_MONTH);

        beginDate.setText(strDate);
        endDate.setText(strDate);

        beginDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(DIALOG_ID_BEGIN);
            }
        });

        endDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(DIALOG_ID_END);
            }
        });


        btn_dList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    listDeliveries(beginDate.getText().toString(), endDate.getText().toString(), senderPhone.getText().toString());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });

        listViewDeliveries.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                delivery = (Delivery) parent.getItemAtPosition(position);
                showCustomDialog();
            }
        });

        sCity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                senderCity = sCity.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                senderCity = "%";
            }
        });

        rCity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                receiverCity = rCity.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                receiverCity = "%";
            }
        });


    }


    public void showCustomDialog() {

        dialogPaying = new Dialog(DeliveryDebteds.this);
        dialogPaying.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogPaying.setCancelable(false);
        dialogPaying.setContentView(R.layout.pay_debt_dialog);

        final Spinner postmanSpinner = dialogPaying.findViewById(R.id.spinner_postman);

        try {
            PostHelper.listPostmans(HomeActivity.userCity, DeliveryDebteds.this, postmanSpinner);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Button btnOK = dialogPaying.findViewById(R.id.btnOk);
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    payDebt(delivery.deliveryId, postmanSpinner.getSelectedItem().toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        Button btnCancel = dialogPaying.findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    dialogPaying.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        dialogPaying.show();

        dialogPaying.show();
    }

    public void payDebt(String deliveryId, String selectedPostman) throws ParseException {

        if (!NetworkUtil.isNetworkConnected(DeliveryDebteds.this)) {
            MyDialog.createSimpleOkErrorDialog(DeliveryDebteds.this,
                    getApplicationContext().getString(R.string.dialog_error_title),
                    getApplicationContext().getString(R.string.check_internet)).show();
        } else if (NetworkUtil.isTokenExpired()) {
            MyDialog.createSimpleOkErrorDialog(DeliveryDebteds.this,
                    getApplicationContext().getString(R.string.dialog_error_title),
                    getApplicationContext().getString(R.string.relogin)).show();
        } else {
            String tag_string_req = "req_pay_debt";
            pDialog.setMessage("Processing ...");
            showDialog();

            deliveryList.clear();
            listViewDeliveries.setAdapter(null);

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("deliveryId", deliveryId);
                jsonObject.put("payingUser", selectedPostman);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, AppConfig.URL_DELIVERY_PAY_DEBT, jsonObject,
                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            hideDialog();
                            if (response != null) {
                                dialogPaying.dismiss();
                            } else {
                                MyDialog.createSimpleOkErrorDialog(DeliveryDebteds.this,
                                        getApplicationContext().getString(R.string.dialog_error_title),
                                        getApplicationContext().getString(R.string.ErrorWhenLoading)).show();
                            }

                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    NetworkUtil.checkHttpStatus(DeliveryDebteds.this, error);
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
            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(req, tag_string_req);
        }
    }

    public void listDeliveries(final String beginingDate, final String endingDate, final String senderPhone) throws ParseException {

        if (!NetworkUtil.isNetworkConnected(DeliveryDebteds.this)) {
            MyDialog.createSimpleOkErrorDialog(DeliveryDebteds.this,
                    getApplicationContext().getString(R.string.dialog_error_title),
                    getApplicationContext().getString(R.string.check_internet)).show();
        } else if (NetworkUtil.isTokenExpired()) {
            MyDialog.createSimpleOkErrorDialog(DeliveryDebteds.this,
                    getApplicationContext().getString(R.string.dialog_error_title),
                    getApplicationContext().getString(R.string.relogin)).show();
        } else {
            String tag_string_req = "req_get_deliveries";
            pDialog.setMessage("Listing Deliveries ...");
            showDialog();

            deliveryList.clear();
            listViewDeliveries.setAdapter(null);

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("beginDate", beginingDate);
                jsonObject.put("endDate", endingDate);
                jsonObject.put("senderCity", senderCity+"%");
                jsonObject.put("receiverCity", receiverCity+"%");
                jsonObject.put("senderPhone", senderPhone+"%");
                jsonObject.put("belongingUser", "%");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            CustomJsonArrayRequest req = new CustomJsonArrayRequest(Request.Method.POST, AppConfig.URL_DELIVERY_LIST_WITH_DEBTS, jsonObject,
                    new Response.Listener<JSONArray>() {

                        @Override
                        public void onResponse(JSONArray response) {
                            hideDialog();

                            try {
                                // Check for error node in json
                                if (response.length() > 0) {

                                    JsonParser parser = new JsonParser();
                                    Gson gson = new Gson();

                                    for (int i = 0; i < response.length(); i++) {

                                        JsonElement mJsonM = parser.parse(response.getString(i));
                                        Delivery dd = gson.fromJson(mJsonM, Delivery.class);

                                        dd.number = i + 1;
                                        dd.sFullName = dd.senderName + " - " + dd.senderPhone + " - " + dd.senderCompany;
                                        dd.sFullAddress = dd.senderCity + " - " + dd.senderAddress;
                                        dd.rFullName = dd.receiverName + " - " + dd.receiverPhone + " - " + dd.receiverCompany;
                                        dd.rFullAddress = dd.receiverCity + " - " + dd.receiverAddress;

                                        deliveryList.add(dd);
                                    }

                                    if (deliveryList.size() > 0) {
                                        DeliveryListAdapter orderListAdapter = new DeliveryListAdapter(deliveryList, DeliveryDebteds.this);
                                        listViewDeliveries.setAdapter(orderListAdapter);
                                    }

                                } else {
                                    MyDialog.createSimpleOkErrorDialog(DeliveryDebteds.this,
                                            getApplicationContext().getString(R.string.dialog_error_title),
                                            getApplicationContext().getString(R.string.NoData)).show();
                                }
                            } catch (JSONException e) {
                                MyDialog.createSimpleOkErrorDialog(DeliveryDebteds.this,
                                        getApplicationContext().getString(R.string.dialog_error_title),
                                        getApplicationContext().getString(R.string.ErrorWhenLoading)).show();
                            }

                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    NetworkUtil.checkHttpStatus(DeliveryDebteds.this, error);
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

    protected Dialog onCreateDialog(int id) {

        if (id == DIALOG_ID_BEGIN)
            return new DatePickerDialog(this, datePickerListenerBegin, year_x, month_x, day_x);
        else if (id == DIALOG_ID_END)
            return new DatePickerDialog(this, datePickerListenerEnd, year_x, month_x, day_x);
        else
            return null;
    }

    protected DatePickerDialog.OnDateSetListener datePickerListenerBegin
            = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

            year_x = year;
            month_x = month + 1;
            day_x = dayOfMonth;
            String dateS = String.valueOf(year);
            if (month_x < 10)
                dateS = dateS + "-0" + month_x;
            else
                dateS = dateS + "-" + month_x;
            if (day_x < 10)
                dateS = dateS + "-0" + day_x;
            else
                dateS = dateS + "-" + day_x;
            beginDate.setText(dateS);

        }
    };

    protected DatePickerDialog.OnDateSetListener datePickerListenerEnd
            = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

            year_x = year;
            month_x = month + 1;
            day_x = dayOfMonth;
            String dateS = String.valueOf(year);
            if (month_x < 10)
                dateS = dateS + "-0" + month_x;
            else
                dateS = dateS + "-" + month_x;
            if (day_x < 10)
                dateS = dateS + "-0" + day_x;
            else
                dateS = dateS + "-" + day_x;
            endDate.setText(dateS);

        }
    };
}