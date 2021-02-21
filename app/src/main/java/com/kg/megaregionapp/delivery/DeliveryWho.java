package com.kg.megaregionapp.delivery;


import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DeliveryWho extends AppCompatActivity {

    private static final String TAG = DeliveryList.class.getSimpleName();
    public static int DIALOG_DATE1_ID = 1;
    public static int DIALOG_DATE2_ID = 2;
    ListView listViewDeliveries;

    private ProgressDialog pDialog;
    private EditText ed_Date1, ed_Date2, ed_Name, ed_Phone;
    private Button btn_dList;
    private Calendar calendar;
    int year_x, month_x, day_x;
    private Delivery delivery;

    private List<Delivery> deliveryList = new ArrayList<>();
    private String selectedDate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_who);


        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        listViewDeliveries = findViewById(R.id.listViewDeliveries);
        ed_Date1 = findViewById(R.id.ed_Date1);
        ed_Date2 = findViewById(R.id.ed_Date2);
        ed_Name = findViewById(R.id.ed_Name);
        ed_Phone = findViewById(R.id.ed_Phone);

        btn_dList = findViewById(R.id.btn_dList);

        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");//dd/MM/yyyy
        Date now = new Date();
        String strDate = sdfDate.format(now);


        ed_Date1.setText(strDate);
        ed_Date2.setText(strDate);

        calendar = Calendar.getInstance();
        year_x = calendar.get(Calendar.YEAR);
        month_x = calendar.get(Calendar.MONTH);
        day_x = calendar.get(Calendar.DAY_OF_MONTH);

        ed_Date1.setOnClickListener(v -> showDialog(DIALOG_DATE1_ID));

        ed_Date2.setOnClickListener(v -> showDialog(DIALOG_DATE2_ID));

        btn_dList.setOnClickListener(v -> {
            //    arrangeCities();
            deliveryList.clear();
            listViewDeliveries.setAdapter(null);
            try {
                listDeliveries(ed_Date1.getText().toString(), ed_Date2.getText().toString(), ed_Name.getText().toString(), ed_Phone.getText().toString());
            } catch (ParseException e) {
                e.printStackTrace();
            }

        });


        listViewDeliveries.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                delivery = (Delivery) parent.getItemAtPosition(position);

                Intent intentDelivery = new Intent(DeliveryWho.this, DeliveryObserve.class);
                intentDelivery.putExtra("delivery", delivery);
                startActivityForResult(intentDelivery, 4);
            }
        });
    }


    public void listDeliveries(final String date1, final String date2, final String name, final String phone) throws ParseException {

        if (!NetworkUtil.isNetworkConnected(DeliveryWho.this)) {
            MyDialog.createSimpleOkErrorDialog(DeliveryWho.this,
                    getApplicationContext().getString(R.string.dialog_error_title),
                    getApplicationContext().getString(R.string.check_internet)).show();
        } else if (NetworkUtil.isTokenExpired()) {
            MyDialog.createSimpleOkErrorDialog(DeliveryWho.this,
                    getApplicationContext().getString(R.string.dialog_error_title),
                    getApplicationContext().getString(R.string.relogin)).show();
        } else {
            String tag_string_req = "req_get_deliveries";
            pDialog.setMessage("Идет загрузка данных...");
            showDialog();

            deliveryList.clear();
            listViewDeliveries.setAdapter(null);

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("beginDate", date1);
                jsonObject.put("endDate", date2);
                if (name.length() > 0)
                    jsonObject.put("owner", "%" + name + "%");
                else
                    jsonObject.put("owner", "%");
                if (phone.length() > 0)
                    jsonObject.put("phone", "%" + phone + "%");
                else
                    jsonObject.put("phone", "%");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            CustomJsonArrayRequest req = new CustomJsonArrayRequest(Request.Method.POST, AppConfig.URL_DELIVERY_LIST_WHO, jsonObject,
                    response -> {
                        hideDialog();
                        try {

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
                                    DeliveryListAdapter orderListAdapter = new DeliveryListAdapter(deliveryList, DeliveryWho.this);
                                    listViewDeliveries.setAdapter(orderListAdapter);
                                }

                            } else {
                                MyDialog.createSimpleOkErrorDialog(DeliveryWho.this,
                                        getApplicationContext().getString(R.string.dialog_error_title),
                                        getApplicationContext().getString(R.string.NoData)).show();
                            }
                        } catch (JSONException e) {
                            MyDialog.createSimpleOkErrorDialog(DeliveryWho.this,
                                    getApplicationContext().getString(R.string.dialog_error_title),
                                    getApplicationContext().getString(R.string.ErrorWhenLoading)).show();
                        }

                    }, error -> {
                        NetworkUtil.checkHttpStatus(DeliveryWho.this, error);
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

        if (id == DIALOG_DATE1_ID)
            return new DatePickerDialog(this, datePickerListener1, year_x, month_x, day_x);
        else if (id == DIALOG_DATE2_ID)
            return new DatePickerDialog(this, datePickerListener2, year_x, month_x, day_x);
        else
            return null;
    }

    protected DatePickerDialog.OnDateSetListener datePickerListener1
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
            ed_Date1.setText(dateS);
        }
    };

    protected DatePickerDialog.OnDateSetListener datePickerListener2
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
            ed_Date2.setText(dateS);
        }
    };
}
