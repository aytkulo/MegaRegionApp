package com.kg.megaregionapp.orders;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.kg.megaregionapp.HomeActivity;
import com.kg.megaregionapp.R;
import com.kg.megaregionapp.app.AppConfig;
import com.kg.megaregionapp.app.AppController;
import com.kg.megaregionapp.customer.CustomerHelper;
import com.kg.megaregionapp.helper.CustomJsonArrayRequest;
import com.kg.megaregionapp.helper.PostHelper;
import com.kg.megaregionapp.helper.StringData;
import com.kg.megaregionapp.utils.MyDialog;
import com.kg.megaregionapp.utils.NetworkUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OrderEntry extends AppCompatActivity {

    private ArrayList<String> usersList = new ArrayList<>();
    private static final String TAG = OrderEntry.class.getSimpleName();
    private ProgressDialog pDialog;
    Spinner spinner_users;
    Spinner spinner_senderCity;
    Spinner spinner_receiverCity;

    Button btn_save_order;
    EditText senderName;

    AutoCompleteTextView senderPhone;
    AutoCompleteTextView receiverPhone;
    AutoCompleteTextView senderCompany;
    AutoCompleteTextView receiverCompany;

    EditText receiverName;

    CheckBox check_addCustomer;

    EditText receiverAddress;
    EditText senderAddress, orderExplanation;
    ArrayAdapter<String> myAdapterS;
    ArrayAdapter<String> myAdapterR;

    ArrayAdapter<String> myAdapterSC;
    ArrayAdapter<String> myAdapterRC;


    String[] item = new String[]{""};
    private String usersCity = "";
    private String userName = "";
    private String token = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_entry);

        spinner_users = (Spinner) findViewById(R.id.spinner_postman);
        spinner_senderCity = (Spinner) findViewById(R.id.spinner_senderCity);
        spinner_receiverCity = (Spinner) findViewById(R.id.spinner_receiverCity);

        btn_save_order = findViewById(R.id.btn_save_order);

        senderName = findViewById(R.id.senderName);
        senderPhone = findViewById(R.id.senderPhone);
        senderCompany = findViewById(R.id.senderCompany);


        receiverName = findViewById(R.id.receiverName);
        receiverPhone = findViewById(R.id.receiverPhone);
        receiverCompany = findViewById(R.id.receiverCompany);
        receiverAddress = findViewById(R.id.receiverAddress);
        senderAddress = findViewById(R.id.senderAddress);
        orderExplanation = findViewById(R.id.orderExplanation);

        check_addCustomer = findViewById(R.id.check_addToDB);


        ArrayAdapter<String> cityAdapter = new ArrayAdapter<String>(
                OrderEntry.this,
                android.R.layout.simple_spinner_dropdown_item,
                StringData.getCityList()
        );

        spinner_senderCity.setAdapter(cityAdapter);
        spinner_receiverCity.setAdapter(cityAdapter);

        usersCity = HomeActivity.userCity;
        userName = HomeActivity.userLogin;
        token = HomeActivity.token;

        spinner_senderCity.setSelection(getIndex(spinner_senderCity, usersCity));

        senderPhone.setText("0");
        int position = senderPhone.length();
        Editable etext = senderPhone.getText();
        senderPhone.requestFocus();
        senderPhone.setSelection(senderPhone.getText().length());
        Selection.setSelection(etext, position);

        senderPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.length() > 3) {
                    try {
                        getSenderCustomers(s.toString(), "");
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 3) {
                    String[] parts = s.toString().split("__");
                    if (parts.length > 1) {
                        senderName.setText(parts[1]);
                        senderPhone.setText(parts[0]);

                        spinner_senderCity.setSelection(getIndex(spinner_senderCity, parts[3]));
                        senderAddress.setText(parts[2]);
                        if (parts.length > 4)
                            senderCompany.setText(parts[4]);
                    }
                }
            }
        });

        receiverPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.length() > 3) {
                    try {
                        getReceiverCustomers(s.toString(), "");
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 3) {
                    String[] parts = s.toString().split("__");
                    if (parts.length > 1) {
                        receiverName.setText(parts[1]);
                        receiverPhone.setText(parts[0]);
                        spinner_receiverCity.setSelection(getIndex(spinner_receiverCity, parts[3]));
                        receiverAddress.setText(parts[2]);
                        if (parts.length > 4)
                            receiverCompany.setText(parts[4]);
                    }
                }
            }
        });


        senderCompany.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.length() > 3) {
                    try {
                        getSenderCustomers("", s.toString());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 3) {
                    String[] parts = s.toString().split("__");
                    if (parts.length > 1) {
                        senderName.setText(parts[1]);
                        senderPhone.setText(parts[0]);

                        spinner_senderCity.setSelection(getIndex(spinner_senderCity, parts[3]));

                        senderAddress.setText(parts[2]);
                        senderCompany.setText(parts[4]);
                    }
                }
            }
        });

        receiverCompany.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.length() > 3) {
                    try {
                        getReceiverCustomers("", s.toString());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 3) {
                    String[] parts = s.toString().split("__");
                    if (parts.length > 1) {
                        receiverName.setText(parts[1]);
                        receiverPhone.setText(parts[0]);

                        spinner_receiverCity.setSelection(getIndex(spinner_receiverCity, parts[3]));

                        receiverAddress.setText(parts[2]);
                        receiverCompany.setText(parts[4]);
                    }
                }
            }
        });


        myAdapterS = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, item);
        senderPhone.setAdapter(myAdapterS);
        senderPhone.setThreshold(3);

        myAdapterR = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, item);
        receiverPhone.setAdapter(myAdapterR);
        receiverPhone.setThreshold(3);

        myAdapterSC = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, item);
        senderCompany.setAdapter(myAdapterSC);
        senderCompany.setThreshold(3);

        myAdapterRC = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, item);
        receiverCompany.setAdapter(myAdapterRC);
        receiverCompany.setThreshold(3);


        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        btn_save_order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String sName = senderName.getText().toString();
                String sAddr = senderAddress.getText().toString();
                String sPhone = senderPhone.getText().toString();
                String sCity = "";
                if (spinner_senderCity.getSelectedItem() != null)
                    sCity = spinner_senderCity.getSelectedItem().toString();
                String sComp = senderCompany.getText().toString();

                String rName = receiverName.getText().toString();
                String rPhone = receiverPhone.getText().toString();
                String rAddr = receiverAddress.getText().toString();
                String rCity = "";
                if (spinner_receiverCity.getSelectedItem() != null)
                    rCity = spinner_receiverCity.getSelectedItem().toString();
                String rComp = receiverCompany.getText().toString();
                String explanation = orderExplanation.getText().toString();

                if (check_addCustomer.isChecked() && addCustomerCheck(sName, sPhone, sComp, sAddr, sCity)) {
                    CustomerHelper.saveCustomer(sName, sPhone, sComp, sCity, sAddr, token);
                }

                if (orderDataCheck()) {
                    try {
                        String sector = spinner_users.getSelectedItem().toString();
                        saveOrder(sName, sPhone, sComp, sCity, sAddr, rName, rPhone, rComp, rCity, rAddr, sector, explanation);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }


            }
        });

        spinner_senderCity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                usersList.clear();
                spinner_users.setAdapter(null);
                String senderCity = spinner_senderCity.getSelectedItem().toString();

                try {
                    PostHelper.listSectors(senderCity, OrderEntry.this, spinner_users);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

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

    public void saveOrder(final String sName, final String sPhone, final String sComp, final String sCity, final String sAddress,
                          final String rName, final String rPhone, final String rComp, final String rCity, final String rAddress,
                          final String sector, final String explanation) throws ParseException {

        if (!NetworkUtil.isNetworkConnected(OrderEntry.this)) {
            MyDialog.createSimpleOkErrorDialog(OrderEntry.this,
                    getApplicationContext().getString(R.string.dialog_error_title),
                    getApplicationContext().getString(R.string.check_internet)).show();
        } else if (NetworkUtil.isTokenExpired()) {
            MyDialog.createSimpleOkErrorDialog(OrderEntry.this,
                    getApplicationContext().getString(R.string.dialog_error_title),
                    getApplicationContext().getString(R.string.relogin)).show();
        } else {
            // Tag used to cancel the request
            String tag_string_req = "req_save_order";

            pDialog.setMessage(getApplicationContext().getString(R.string.Processing));
            showDialog();

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("senderName", sName);
                jsonObject.put("senderPhone", sPhone);
                jsonObject.put("senderAddress", sAddress);
                jsonObject.put("senderCity", sCity);
                jsonObject.put("senderCompany", sComp);
                jsonObject.put("receiverName", rName);
                jsonObject.put("receiverPhone", rPhone);
                jsonObject.put("receiverAddress", rAddress);
                jsonObject.put("receiverCity", rCity);
                jsonObject.put("receiverCompany", rComp);
                jsonObject.put("assignedSector", sector);
                jsonObject.put("user", userName);
                jsonObject.put("explanation", explanation);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            JsonObjectRequest req = new JsonObjectRequest(AppConfig.URL_ORDER_SAVE, jsonObject,
                    response -> {
                        hideDialog();
                        if (response != null) {
                            Intent intent = new Intent(OrderEntry.this, HomeActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            MyDialog.createSimpleOkErrorDialog(OrderEntry.this,
                                    getApplicationContext().getString(R.string.dialog_error_title),
                                    getApplicationContext().getString(R.string.ErrorWhenLoading)).show();
                        }
                    }, error -> {
                        NetworkUtil.checkHttpStatus(OrderEntry.this, error);
                        hideDialog();
                    }) {
                @Override
                public Map<String, String> getHeaders() {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("Authorization", token);
                    return headers;
                }
            };
                AppController.getInstance().addToRequestQueue(req, tag_string_req);

        }
    }

    private ShapeDrawable getShape(int color) {

        ShapeDrawable shape = new ShapeDrawable(new RectShape());
        shape.getPaint().setColor(color);
        shape.getPaint().setStyle(Paint.Style.STROKE);
        shape.getPaint().setStrokeWidth(3);

        return shape;
    }



    public void getSenderCustomers(final String phone, final String company) throws ParseException {

        if (!NetworkUtil.isNetworkConnected(OrderEntry.this)) {
            MyDialog.createSimpleOkErrorDialog(OrderEntry.this,
                    getApplicationContext().getString(R.string.dialog_error_title),
                    getApplicationContext().getString(R.string.check_internet)).show();
        } else if (NetworkUtil.isTokenExpired()) {
            MyDialog.createSimpleOkErrorDialog(OrderEntry.this,
                    getApplicationContext().getString(R.string.dialog_error_title),
                    getApplicationContext().getString(R.string.relogin)).show();
        } else {
            String tag_string_req = "req_get_customers";

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("phone", phone);
                jsonObject.put("company", company);
                jsonObject.put("city", "");
                jsonObject.put("address", "");
                jsonObject.put("responsiblePerson", "");
                jsonObject.put("customerId", "");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            CustomJsonArrayRequest req = new CustomJsonArrayRequest(Request.Method.POST, AppConfig.URL_CUSTOMER_GET, jsonObject,
                    response -> {
                        try {
                            if (response.length() > 0) {
                                String[] custs = new String[response.length()];
                                for (int i = 0; i < response.length(); i++) {
                                    JSONObject c = response.getJSONObject(i);
                                    custs[i] = c.getString("phone") + "__" + c.getString("responsiblePerson") + "__" + c.getString("address") + "__" + c.getString("city") + "__" + c.getString("company");
                                }
                                // update the adapater
                                myAdapterS = new ArrayAdapter<String>(OrderEntry.this, android.R.layout.simple_dropdown_item_1line, custs);
                                myAdapterSC = new ArrayAdapter<String>(OrderEntry.this, android.R.layout.simple_dropdown_item_1line, custs);
                                senderPhone.setAdapter(myAdapterS);
                                senderCompany.setAdapter(myAdapterSC);
                                myAdapterS.notifyDataSetChanged();
                                myAdapterSC.notifyDataSetChanged();
                            }
                        } catch (JSONException e) {
                            MyDialog.createSimpleOkErrorDialog(OrderEntry.this,
                                    getApplicationContext().getString(R.string.dialog_error_title),
                                    getApplicationContext().getString(R.string.ErrorWhenLoading)).show();
                        }

                    }, error -> {
                        NetworkUtil.checkHttpStatus(OrderEntry.this, error);
                        hideDialog();
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
    }


    public void getReceiverCustomers(final String phone, final String company) throws ParseException {

        if (!NetworkUtil.isNetworkConnected(OrderEntry.this)) {
            MyDialog.createSimpleOkErrorDialog(OrderEntry.this,
                    getApplicationContext().getString(R.string.dialog_error_title),
                    getApplicationContext().getString(R.string.check_internet)).show();
        } else if (NetworkUtil.isTokenExpired()) {
            MyDialog.createSimpleOkErrorDialog(OrderEntry.this,
                    getApplicationContext().getString(R.string.dialog_error_title),
                    getApplicationContext().getString(R.string.relogin)).show();
        } else {
            String tag_string_req = "req_get_customers";

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("phone", phone);
                jsonObject.put("company", company);
                jsonObject.put("city", "");
                jsonObject.put("address", "");
                jsonObject.put("responsiblePerson", "");
                jsonObject.put("customerId", "");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            CustomJsonArrayRequest req = new CustomJsonArrayRequest(Request.Method.POST, AppConfig.URL_CUSTOMER_GET, jsonObject,
                    response -> {
                        try {
                            if (response.length() > 0) {

                                String[] custs = new String[response.length()];
                                for (int i = 0; i < response.length(); i++) {
                                    JSONObject c = response.getJSONObject(i);
                                    custs[i] = c.getString("phone") + "__" + c.getString("responsiblePerson") + "__" + c.getString("address") + "__" + c.getString("city") + "__" + c.getString("company");
                                }
                                // update the adapater
                                myAdapterR = new ArrayAdapter<String>(OrderEntry.this, android.R.layout.simple_dropdown_item_1line, custs);
                                receiverPhone.setAdapter(myAdapterR);
                                myAdapterR.notifyDataSetChanged();
                                myAdapterRC = new ArrayAdapter<String>(OrderEntry.this, android.R.layout.simple_dropdown_item_1line, custs);
                                receiverCompany.setAdapter(myAdapterRC);
                                myAdapterRC.notifyDataSetChanged();
                            }
                        } catch (JSONException e) {
                            MyDialog.createSimpleOkErrorDialog(OrderEntry.this,
                                    getApplicationContext().getString(R.string.dialog_error_title),
                                    getApplicationContext().getString(R.string.ErrorWhenLoading)).show();
                        }
                    }, error -> {
                        NetworkUtil.checkHttpStatus(OrderEntry.this, error);
                        hideDialog();
                    }) {

                @Override
                public Map<String, String> getHeaders() {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("Authorization", token);
                    return headers;
                }
            };
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


    private boolean orderDataCheck() {

        boolean ok = true;

        String sName = senderName.getText().toString();
        String sAddr = senderAddress.getText().toString();
        String sPhone = senderPhone.getText().toString();
        String cCity = "";
        if (spinner_senderCity.getSelectedItem() != null)
            cCity = spinner_senderCity.getSelectedItem().toString();


        String sector = "";
        if (spinner_users.getSelectedItem() != null)
            sector = spinner_users.getSelectedItem().toString();

        if (sector.length() < 1) {
            spinner_users.setBackground(getShape(Color.MAGENTA));
            ok = false;
        } else {
            spinner_users.setBackgroundColor(Color.WHITE);
        }


        if (cCity.length() < 1) {
            spinner_senderCity.setBackground(getShape(Color.MAGENTA));
            ok = false;
        } else {
            spinner_senderCity.setBackgroundColor(Color.WHITE);
        }
        if (sName.length() < 1) {
            senderName.setBackground(getShape(Color.MAGENTA));
            ok = false;
        } else {
            senderName.setBackgroundColor(Color.WHITE);
        }
        if (sAddr.length() < 1) {
            senderAddress.setBackground(getShape(Color.MAGENTA));
            ok = false;
        } else {
            senderAddress.setBackgroundColor(Color.WHITE);
        }
        if (sPhone.length() != 10) {
            senderPhone.setBackground(getShape(Color.MAGENTA));
            ok = false;
        } else {
            senderPhone.setBackgroundColor(Color.WHITE);
        }

        if (!ok) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.FillAllDataCorrectly), Toast.LENGTH_LONG).show();
        }

        return ok;
    }


    private boolean addCustomerCheck(final String sName, final String sPhone, final String sComp, final String sAddress, final String city) {

        boolean ok = true;
        if (check_addCustomer.isChecked()) {

            if (city.length() < 1) {
                spinner_senderCity.setBackground(getShape(Color.MAGENTA));
                ok = false;
            } else {
                spinner_senderCity.setBackgroundColor(Color.WHITE);
            }
            if (sName.length() < 1) {
                senderName.setBackground(getShape(Color.MAGENTA));
                ok = false;
            } else {
                senderName.setBackgroundColor(Color.WHITE);
            }
            if (sAddress.length() < 1) {
                senderAddress.setBackground(getShape(Color.MAGENTA));
                ok = false;
            } else {
                senderAddress.setBackgroundColor(Color.WHITE);
            }
            if (sPhone.length() < 1) {
                senderPhone.setBackground(getShape(Color.MAGENTA));
                ok = false;
            } else {
                senderPhone.setBackgroundColor(Color.WHITE);
            }
            if (sComp.length() < 1) {
                senderCompany.setBackground(getShape(Color.MAGENTA));
                ok = false;
            } else {
                senderCompany.setBackgroundColor(Color.WHITE);
            }

            if (!ok) {
                String message = getResources().getString(R.string.FillAllDataCorrectly);
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
        }

        return ok;
    }
}
