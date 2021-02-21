package com.kg.megaregionapp.orders;

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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
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

public class OrderList extends AppCompatActivity {

    private static final String TAG = OrderList.class.getSimpleName();
    public static int DIALOG_ID1 = 0;
    public static int DIALOG_ID2 = 1;

    ListView listViewOrders;
    private ArrayList<String> sectorList = new ArrayList<>();
    private ProgressDialog pDialog;
    private EditText ed_Date1, ed_Date2, phone;
    private Button btn_List;
    private Spinner sp_Origin;
    private Spinner sp_Sectors;
    private Calendar calendar;
    int year_x, month_x, day_x;

    String usersCity = "";
    String token = "";

    private List<Orders> orderList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_list);

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        listViewOrders = findViewById(R.id.listViewOrders);
        ed_Date1 = findViewById(R.id.ed_Date1);
        ed_Date2 = findViewById(R.id.ed_Date2);
        phone = findViewById(R.id.sp_Phone);
        btn_List = findViewById(R.id.btn_oList);
        sp_Origin = findViewById(R.id.sp_Origin);
        sp_Sectors = findViewById(R.id.sp_sectors);

        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");//dd/MM/yyyy
        Date now = new Date();
        String strDate = sdfDate.format(now);

        ed_Date1.setText(strDate);
        ed_Date2.setText(strDate);


        calendar = Calendar.getInstance();
        year_x = calendar.get(Calendar.YEAR);
        month_x = calendar.get(Calendar.MONTH);
        day_x = calendar.get(Calendar.DAY_OF_MONTH);

        usersCity = HomeActivity.userCity;
        token = HomeActivity.token;

        ArrayAdapter<String> cityAdapter = new ArrayAdapter<String>(
                OrderList.this,
                android.R.layout.simple_spinner_dropdown_item,
                StringData.getCityList()
        );

        sp_Origin.setAdapter(cityAdapter);
        sp_Origin.setSelection(getIndex(sp_Origin, usersCity));


        sp_Origin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sectorList.clear();
                sp_Sectors.setAdapter(null);
                String senderCity = sp_Origin.getSelectedItem().toString();

                try {
                    PostHelper.listSectors(senderCity, OrderList.this, sp_Sectors);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


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

        btn_List.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orderList.clear();
                listViewOrders.setAdapter(null);
                try {
                    if(sp_Sectors.getSelectedItem() != null) {
                        listOrders(ed_Date1.getText().toString(), ed_Date2.getText().toString(), sp_Sectors.getSelectedItem().toString(), sp_Origin.getSelectedItem().toString(), phone.getText().toString());
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
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

    protected Dialog onCreateDialog(int id) {

        if (id == DIALOG_ID1)
            return new DatePickerDialog(this, datePickerListener, year_x, month_x, day_x);
        else if (id == DIALOG_ID2)
            return new DatePickerDialog(this, datePickerListener2, year_x, month_x, day_x);
        else
            return null;
    }

    protected DatePickerDialog.OnDateSetListener datePickerListener
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



    public void listOrders(final String entryDate1, final String entryDate2, final String responsible, final String origin, final String phone) throws ParseException {

        if (!NetworkUtil.isNetworkConnected(OrderList.this)) {
            MyDialog.createSimpleOkErrorDialog(OrderList.this,
                    getApplicationContext().getString(R.string.dialog_error_title),
                    getApplicationContext().getString(R.string.check_internet)).show();
        } else if (NetworkUtil.isTokenExpired()) {
            MyDialog.createSimpleOkErrorDialog(OrderList.this,
                    getApplicationContext().getString(R.string.dialog_error_title),
                    getApplicationContext().getString(R.string.relogin)).show();
        } else {
            String tag_string_req = "req_getOrders";
            pDialog.setMessage("Идет загрузка данных...");
            showDialog();

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("entryDate1", entryDate1);
                jsonObject.put("entryDate2", entryDate2);
                jsonObject.put("status", "%");
                jsonObject.put("sector", responsible);
                jsonObject.put("origin", origin);
                jsonObject.put("phone", phone+"%");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            CustomJsonArrayRequest strReq = new CustomJsonArrayRequest(Request.Method.POST, AppConfig.URL_ORDER_GET, jsonObject,
                    new Response.Listener<JSONArray>() {

                        @Override
                        public void onResponse(JSONArray response) {
                            
                            hideDialog();

                            try {
                                // Check for error node in json
                                if (response.length() > 0) {

                                    for (int i = 0; i < response.length(); i++) {
                                        JSONObject c = response.getJSONObject(i);
                                        Orders o = new Orders();
                                        // Storing each json item in variable

                                        o.Name = c.getString("senderPhone") + " - " + c.getString("senderName") + " - " + c.getString("senderCompany");
                                        o.Address = c.getString("senderCity") + " - " + c.getString("senderAddress");
                                        o.responsible = c.getString("assignedSector");

                                        o.id = c.getString("orderId");
                                        o.senderCity = c.getString("senderCity");
                                        o.senderPhone = c.getString("senderPhone");
                                        o.senderCompany = c.getString("senderCompany");
                                        o.senderAddress = c.getString("senderAddress");
                                        o.senderName = c.getString("senderName");
                                        o.receiverCity = c.getString("receiverCity");
                                        o.receiverName = c.getString("receiverName");
                                        o.receiverPhone = c.getString("receiverPhone");
                                        o.receiverAddress = c.getString("receiverAddress");
                                        o.receiverCompany = c.getString("receiverCompany");
                                        o.status = c.getString("status");
                                        o.entrydate = c.getString("entryDate");
                                        o.time = c.getString("entryTime");
                                        o.enteredUser = c.getString("enteredUser");
                                        o.updatedUser = c.getString("updatedUser");
                                        o.updatedDate = c.getString("updatedDate");
                                        o.orderExplanation = c.getString("explanation");

                                        orderList.add(o);
                                    }

                                    if (orderList.size() > 0) {
                                        OrderListAdapter orderListAdapter = new OrderListAdapter(orderList, OrderList.this);
                                        listViewOrders.setAdapter(orderListAdapter);
                                    }

                                } else {
                                    Toast.makeText(OrderList.this,  getApplicationContext().getString(R.string.NoData), Toast.LENGTH_LONG).show();
                                }
                            } catch (JSONException e) {
                                Toast.makeText(OrderList.this,  getApplicationContext().getString(R.string.ErrorWhenLoading), Toast.LENGTH_LONG).show();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    NetworkUtil.checkHttpStatus(OrderList.this, error);
                    hideDialog();
                }
            }) {

                @Override
                public Map<String, String> getHeaders() {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("Authorization", token);
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
