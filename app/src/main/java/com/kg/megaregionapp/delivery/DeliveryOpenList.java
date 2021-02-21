package com.kg.megaregionapp.delivery;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
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
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.kg.megaregionapp.HomeActivity;
import com.kg.megaregionapp.R;
import com.kg.megaregionapp.app.AppConfig;
import com.kg.megaregionapp.app.AppController;
import com.kg.megaregionapp.helper.CustomJsonArrayRequest;
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

public class DeliveryOpenList extends AppCompatActivity {

    private static final String TAG = DeliveryOpenList.class.getSimpleName();
    private ProgressDialog pDialog;
    public static int DIALOG_ID1 = 1;
    public static int DIALOG_ID2 = 2;
    ListView listViewDeliveries;
    private EditText ed_Date1, ed_Date2;
    private Spinner sCity, rCity;
    private Button btn_dList;
    private Calendar calendar;
    private String strDate = "";
    int year_x, month_x, day_x;
    private String senderCity = "", receiverCity="";

    private List<Delivery> deliveryList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_open_list);

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);


        listViewDeliveries =  findViewById(R.id.listViewDeliveries);
        ed_Date1 =  findViewById(R.id.beginDate);
        ed_Date2 =  findViewById(R.id.enDate);

        sCity = findViewById(R.id.sp_Origin);
        rCity = findViewById(R.id.sp_Destination);

        ArrayAdapter<String> cityAdapterAll = new ArrayAdapter<String>(
                DeliveryOpenList.this,
                android.R.layout.simple_spinner_dropdown_item,
                StringData.getCityList()
        );
        sCity.setAdapter(cityAdapterAll);
        rCity.setAdapter(cityAdapterAll);

        btn_dList =  findViewById(R.id.btn_dList);

        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");//dd/MM/yyyy
        Date now = new Date();
        strDate = sdfDate.format(now);

        ed_Date1.setText(strDate);
        ed_Date2.setText(strDate);

        calendar = Calendar.getInstance();
        year_x = calendar.get(Calendar.YEAR);
        month_x = calendar.get(Calendar.MONTH);
        day_x = calendar.get(Calendar.DAY_OF_MONTH);

        ed_Date1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(DIALOG_ID1);
            }
        });
        ed_Date2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(DIALOG_ID2);
            }
        });

        ArrayAdapter<String> cityAdapter = new ArrayAdapter<String>(
                DeliveryOpenList.this,
                android.R.layout.simple_spinner_dropdown_item,
                StringData.getCityList()
        );

        sCity.setAdapter(cityAdapter);
        rCity.setAdapter(cityAdapter);

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

        btn_dList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deliveryList.clear();
                listViewDeliveries.setAdapter(null);
                try {
                    listOpenDeliveries(ed_Date1.getText().toString(), ed_Date2.getText().toString());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });

    }


    public void listOpenDeliveries(final String date1, final String date2) throws ParseException {

        if (!NetworkUtil.isNetworkConnected(DeliveryOpenList.this)) {
            MyDialog.createSimpleOkErrorDialog(DeliveryOpenList.this,
                    getApplicationContext().getString(R.string.dialog_error_title),
                    getApplicationContext().getString(R.string.check_internet)).show();
        } else if (NetworkUtil.isTokenExpired()) {
            MyDialog.createSimpleOkErrorDialog(DeliveryOpenList.this,
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
                jsonObject.put("senderCity", senderCity+"%");
                jsonObject.put("receiverCity", receiverCity+"%");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            CustomJsonArrayRequest req = new CustomJsonArrayRequest(Request.Method.POST, AppConfig.URL_DELIVERY_OPEN_LIST, jsonObject,
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
                                        DeliveryListAdapter orderListAdapter = new DeliveryListAdapter(deliveryList, DeliveryOpenList.this);
                                        listViewDeliveries.setAdapter(orderListAdapter);
                                    }

                                } else {
                                    MyDialog.createSimpleOkErrorDialog(DeliveryOpenList.this,
                                            getApplicationContext().getString(R.string.dialog_error_title),
                                            getApplicationContext().getString(R.string.NoData)).show();
                                }
                            } catch (JSONException e) {
                                MyDialog.createSimpleOkErrorDialog(DeliveryOpenList.this,
                                        getApplicationContext().getString(R.string.dialog_error_title),
                                        getApplicationContext().getString(R.string.ErrorWhenLoading)).show();
                            }
                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    NetworkUtil.checkHttpStatus(DeliveryOpenList.this, error);
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

        if (id == DIALOG_ID1)
            return new DatePickerDialog(this, datePickerListener1, year_x, month_x, day_x);
        else if (id == DIALOG_ID2)
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
