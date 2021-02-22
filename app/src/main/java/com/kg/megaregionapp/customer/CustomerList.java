package com.kg.megaregionapp.customer;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.kg.megaregionapp.HomeActivity;
import com.kg.megaregionapp.R;
import com.kg.megaregionapp.app.AppConfig;
import com.kg.megaregionapp.app.AppController;
import com.kg.megaregionapp.helper.CustomJsonArrayRequest;
import com.kg.megaregionapp.helper.HelperConstants;
import com.kg.megaregionapp.helper.StringData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomerList extends AppCompatActivity {
    private static final String TAG = CustomerList.class.getSimpleName();
    private ListView listViewCustomers;
    private List<Customer> customerList = new ArrayList<>();
    private Customer customer;
    private EditText cAddress, cName, cPhone, cCompany;
    private Button btnSearch;
    private Spinner cCity;
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_list);

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        listViewCustomers =  findViewById(R.id.listViewCustomers);

        cCity =  findViewById(R.id.spinner_custCity);
        cAddress =  findViewById(R.id.custAddress);
        cName =  findViewById(R.id.custName);
        cPhone =  findViewById(R.id.custPhone);
        cCompany =  findViewById(R.id.custCompany);
        btnSearch =  findViewById(R.id.btn_search);


        ArrayAdapter<String> cityAdapter = new ArrayAdapter<String>(
                CustomerList.this,
                android.R.layout.simple_spinner_dropdown_item,
                StringData.getCityList()
        );
        cCity.setAdapter(cityAdapter);

        listViewCustomers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                customer = (Customer) parent.getItemAtPosition(position);
                Intent intentDelivery = new Intent(CustomerList.this, CustomerUpdate.class);
                intentDelivery.putExtra(HelperConstants.CUSTOMER_OPERATION, HelperConstants.UPDATE_NORM_CUSTOMER);
                intentDelivery.putExtra("customer", customer);
                startActivityForResult(intentDelivery, 1);

            }
        });

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                customerList.clear();
                listCustomers(cAddress.getText().toString(), cName.getText().toString(), cCompany.getText().toString(),
                        cPhone.getText().toString(), cCity.getSelectedItem().toString());

            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        customerList.clear();
        listCustomers(cAddress.getText().toString(), cName.getText().toString(), cCompany.getText().toString(),
                cPhone.getText().toString(), cCity.getSelectedItem().toString());
    }


    public void listCustomers(final String address, final String name, final String company, final String phone, final String city) {

        String tag_string_req = "req_list_customers";
        pDialog.setMessage("Идет загрузка данных...");
        showDialog();

        listViewCustomers.setAdapter(null);

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("address", address);
            jsonObject.put("city", city);
            jsonObject.put("phone", phone);
            jsonObject.put("responsiblePerson", name);
            jsonObject.put("company", company);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        CustomJsonArrayRequest strReq = new CustomJsonArrayRequest(Request.Method.POST, AppConfig.URL_CUSTOMER_GET, jsonObject,
                new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {

                        hideDialog();
                        try {
                            if (response.length() > 0) {

                                for (int i = 0; i < response.length(); i++) {
                                    JSONObject c = response.getJSONObject(i);
                                    // Storing each json item in variable
                                    Customer cust = new Customer();
                                    cust.cusFullAddress = c.getString("city") + "-" + c.getString("address");
                                    cust.cusFullName = c.getString("company") + "-" + c.getString("responsiblePerson");
                                    cust.cus_Address = c.getString("address");
                                    cust.cus_City = c.getString("city");
                                    cust.cus_Company = c.getString("company");
                                    cust.cus_Name = c.getString("responsiblePerson");
                                    cust.cus_Phone = c.getString("phone");
                                    cust.cus_id = c.getString("customerId");
                                    customerList.add(cust);
                                }
                                // update the adapater
                                if (customerList.size() > 0) {
                                    CustomerListAdapter custListAdapter = new CustomerListAdapter(customerList, CustomerList.this);
                                    listViewCustomers.setAdapter(custListAdapter);
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), "Эч нерсе табылбады, же ката пайда болду!", Toast.LENGTH_LONG).show();
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
                hideDialog();
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
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
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }


    public void listCustomersA(final String address, final String name, final String company, final String phone, final String city) {
        // Tag used to cancel the request
        String tag_string_req = "req_list_customers";
        pDialog.setMessage("Listing Customers...");
        showDialog();

        listViewCustomers.setAdapter(null);

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_CUSTOMER_GET, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Customer Listing: " + response);
                hideDialog();
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    // Check for error node in json
                    if (!error) {

                        JSONArray customer = jObj.getJSONArray("customers");
                        for (int i = 0; i < customer.length(); i++) {
                            JSONObject c = customer.getJSONObject(i);
                            // Storing each json item in variable
                            Customer cust = new Customer();
                            cust.cusFullAddress = c.getString("city") + "-" + c.getString("address");
                            cust.cusFullName = c.getString("company") + "-" + c.getString("responsible");
                            cust.cus_Address = c.getString("address");
                            cust.cus_City = c.getString("city");
                            cust.cus_Company = c.getString("company");
                            cust.cus_Name = c.getString("responsible");
                            cust.cus_Phone = c.getString("phone");
                            cust.cus_id = c.getString("id");

                            customerList.add(cust);
                        }
                        // update the adapater
                        if (customerList.size() > 0) {
                            CustomerListAdapter custListAdapter = new CustomerListAdapter(customerList, CustomerList.this);
                            listViewCustomers.setAdapter(custListAdapter);
                        }
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
                hideDialog();
                System.out.println(error.getStackTrace());
                error.printStackTrace();
                VolleyLog.d("ErrorVolley", "Customer listing Error: " + error.getStackTrace());
                Log.e(TAG, "Customer listing Error: " + error.getStackTrace());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<>();
                params.put("address", address + "%");
                params.put("city", city + "%");
                params.put("phone", phone + "%");
                params.put("name", name + "%");
                params.put("company", company + "%");
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }
        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
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
