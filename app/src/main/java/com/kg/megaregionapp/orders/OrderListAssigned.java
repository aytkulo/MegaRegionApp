package com.kg.megaregionapp.orders;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.kg.bar.HomeActivity;
import com.kg.bar.R;
import com.kg.bar.app.AppConfig;
import com.kg.bar.app.AppController;
import com.kg.bar.delivery.DeliveryEntry;
import com.kg.bar.helper.CustomJsonArrayRequest;
import com.kg.bar.helper.PostHelper;
import com.kg.bar.helper.StringData;
import com.kg.bar.users.LoginActivity;
import com.kg.bar.utils.MyDialog;
import com.kg.bar.utils.NetworkUtil;

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

public class OrderListAssigned extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = OrderList.class.getSimpleName();
    private ArrayList<String> sectorList = new ArrayList<>();
    public static int DIALOG_ID1 = 0;
    public static int DIALOG_ID2 = 0;
    ListView listViewOrders;
    com.kg.bar.orders.Orders order;
    private EditText ed_Date1, ed_Date2, phone;
    private Button btn_List;
    private Spinner sp_Origin;
    private Spinner sp_Sector;
    int year_x, month_x, day_x;
    private Calendar calendar;
    String token = "";
    String userLogin = "";

    public static final int DELIVERY_ENTRY_ACTIVITY = 1;
    private ProgressDialog pDialog;

    private List<com.kg.bar.orders.Orders> orderList = new ArrayList<>();

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
        sp_Sector = findViewById(R.id.sp_sectors);
        sp_Origin.setEnabled(false);

        token = HomeActivity.token;
        userLogin = HomeActivity.userLogin;
        String userCity = HomeActivity.userCity;

        ArrayAdapter<String> cityAdapter = new ArrayAdapter<String>(
                com.kg.bar.orders.OrderListAssigned.this,
                android.R.layout.simple_spinner_dropdown_item,
                StringData.getCityList()
        );

        sp_Origin.setAdapter(cityAdapter);
        sp_Origin.setSelection(getIndex(sp_Origin, userCity));

        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");//dd/MM/yyyy
        Date now = new Date();
        String strDate = sdfDate.format(now);
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


        sp_Origin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sectorList.clear();
                sp_Sector.setAdapter(null);
                String senderCity = sp_Origin.getSelectedItem().toString();

                try {
                    PostHelper.listSectors(senderCity, com.kg.bar.orders.OrderListAssigned.this, sp_Sector);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        btn_List.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orderList.clear();
                listViewOrders.setAdapter(null);
                try {
                    listOrders(ed_Date1.getText().toString(), ed_Date2.getText().toString(), sp_Sector.getSelectedItem().toString(), sp_Origin.getSelectedItem().toString(), phone.getText().toString());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });

        listViewOrders.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                order = (com.kg.bar.orders.Orders) parent.getItemAtPosition(position);
                Intent intentDelivery = new Intent(com.kg.bar.orders.OrderListAssigned.this, DeliveryEntry.class);
                intentDelivery.putExtra("order", order);
                startActivityForResult(intentDelivery, DELIVERY_ENTRY_ACTIVITY);
            }
        });


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


    private int getIndex(Spinner spinner, String myString) {

        int index = 0;

        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).equals(myString)) {
                index = i;
            }
        }
        return index;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DELIVERY_ENTRY_ACTIVITY) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                updateOrderStatus();
                orderList.clear();
                listViewOrders.setAdapter(null);
                try {
                    listOrders(ed_Date1.getText().toString(), ed_Date2.getText().toString(), sp_Sector.getSelectedItem().toString(), sp_Origin.getSelectedItem().toString(), phone.getText().toString());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
    }



    public void listOrders(final String entryDate1, final String entryDate2, final String responsible, final String origin, final String phone) throws ParseException {

        if (!NetworkUtil.isNetworkConnected(com.kg.bar.orders.OrderListAssigned.this)) {
            MyDialog.createSimpleOkErrorDialog(com.kg.bar.orders.OrderListAssigned.this,
                    getApplicationContext().getString(R.string.dialog_error_title),
                    getApplicationContext().getString(R.string.check_internet)).show();
        } else if (NetworkUtil.isTokenExpired()) {
            MyDialog.createSimpleOkErrorDialog(com.kg.bar.orders.OrderListAssigned.this,
                    getApplicationContext().getString(R.string.dialog_error_title),
                    getApplicationContext().getString(R.string.relogin)).show();
        } else {
            String tag_string_req = "req_getOrders";
            pDialog.setMessage("Listing orders ...");
            showDialog();

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("entryDate1", entryDate1);
                jsonObject.put("entryDate2", entryDate2);
                jsonObject.put("phone", phone+"%");
                jsonObject.put("sector", responsible);
                jsonObject.put("origin", origin);
                jsonObject.put("status", "0");
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
                                        com.kg.bar.orders.Orders o = new com.kg.bar.orders.Orders();
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
                                        com.kg.bar.orders.OrderListAdapter orderListAdapter = new com.kg.bar.orders.OrderListAdapter(orderList, com.kg.bar.orders.OrderListAssigned.this);
                                        listViewOrders.setAdapter(orderListAdapter);
                                    }

                                } else {
                                    Toast.makeText(com.kg.bar.orders.OrderListAssigned.this,  getApplicationContext().getString(R.string.NoData), Toast.LENGTH_LONG).show();
                                }
                            } catch (JSONException e) {
                                Toast.makeText(com.kg.bar.orders.OrderListAssigned.this,  getApplicationContext().getString(R.string.ErrorWhenLoading), Toast.LENGTH_LONG).show();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    NetworkUtil.checkHttpStatus(com.kg.bar.orders.OrderListAssigned.this, error);
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


    public void updateOrderStatus() {

        String tag_string_req = "req_update_order_status";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("orderId", order.id);
            jsonObject.put("user", userLogin);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest req = new JsonObjectRequest(AppConfig.URL_ORDER_UPDATE_ACCEPT, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (response != null) {
                            //  listOrders(strDate, "0", sp_Sector.getSelectedItem().toString() , sp_Origin.getSelectedItem().toString());
                        } else {
                            Toast.makeText(getApplicationContext(), "Бир ката пайда болду", Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof AuthFailureError) {
                    Toast.makeText(getApplicationContext(), "Бул операция үчүн уруксатыңыз жок!", Toast.LENGTH_LONG).show();
                    Intent loginIntent = new Intent(com.kg.bar.orders.OrderListAssigned.this, LoginActivity.class);
                    startActivity(loginIntent);
                } else {
                    Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                }
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
        AppController.getInstance().addToRequestQueue(req, tag_string_req);
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    @Override
    public void onRefresh() {
        try {
            listOrders(ed_Date1.getText().toString(), ed_Date2.getText().toString(), sp_Sector.getSelectedItem().toString(), sp_Origin.getSelectedItem().toString(), phone.getText().toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

}
