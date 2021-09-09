package com.kg.megaregionapp.delivery;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.kg.megaregionapp.HomeActivity;
import com.kg.megaregionapp.R;
import com.kg.megaregionapp.app.AppConfig;
import com.kg.megaregionapp.app.AppController;
import com.kg.megaregionapp.helper.CustomJsonArrayRequest;
import com.kg.megaregionapp.helper.HelperConstants;
import com.kg.megaregionapp.helper.PostHelper;
import com.kg.megaregionapp.helper.StringData;
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

public class DeliveryList extends AppCompatActivity {

    public static int DIALOG_ID = 0;
    ListView listViewDeliveries;

    private ProgressDialog pDialog;
    private EditText ed_Date, ed_Address, ed_Name, ed_Phone;
    private Button buttonSubmit;
    private ImageView bulkImage;
    private Calendar calendar;
    private Spinner sCity, rCity, postmans;
    int year_x, month_x, day_x;
    private String senderCity = "";
    private String receiverCity = "";
    private String status = "%";
    private String acceptedPostman = "%";
    private String assignedPostman = "%";
    private String token = "";

    private List<Delivery> deliveryList = new ArrayList<>();
    private Delivery delivery;
    private String operationType;
    private String strDate = "";
    private String payment_type = "%";
    private Dialog dialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_list);

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        dialog = new Dialog(this);

        listViewDeliveries = findViewById(R.id.listViewDeliveries);

       bulkImage = findViewById(R.id.imageBulk);

        token = HomeActivity.token;

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LayoutInflater inflater = this.getLayoutInflater();
        View vDialog = inflater.inflate(R.layout.delivery_search_filter, null);  // this line
        dialog.setContentView(vDialog);

        buttonSubmit = (Button) vDialog.findViewById(R.id.btn_submit);

        ed_Date = vDialog.findViewById(R.id.ed_Date);
        ed_Address = vDialog.findViewById(R.id.ed_Address);
        ed_Name = vDialog.findViewById(R.id.ed_Name);
        ed_Phone = vDialog.findViewById(R.id.ed_Tel);
        sCity = vDialog.findViewById(R.id.sp_Origin);
        rCity = vDialog.findViewById(R.id.sp_Destination);
        postmans = vDialog.findViewById(R.id.spinner_postman);

        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");//dd/MM/yyyy
        Date now = new Date();
        strDate = sdfDate.format(now);

        ed_Date.setText(strDate);

        calendar = Calendar.getInstance();
        year_x = calendar.get(Calendar.YEAR);
        month_x = calendar.get(Calendar.MONTH);
        day_x = calendar.get(Calendar.DAY_OF_MONTH);

        ed_Date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(DIALOG_ID);
            }
        });

        ArrayAdapter<String> cityAdapterAll = new ArrayAdapter<String>(
                DeliveryList.this,
                android.R.layout.simple_spinner_dropdown_item,
                StringData.getCityList()
        );
        sCity.setAdapter(cityAdapterAll);
        rCity.setAdapter(cityAdapterAll);

        Window window = dialog.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.show();

        buttonSubmit.setOnClickListener(v -> {
            deliveryList.clear();
            listViewDeliveries.setAdapter(null);
            try {
                listDeliveries(ed_Date.getText().toString(), ed_Address.getText().toString(), ed_Name.getText().toString(), ed_Phone.getText().toString(), payment_type);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            dialog.dismiss();
        });



        FloatingActionButton fab = findViewById(R.id.fabButtonFilter);
        fab.setOnClickListener(view -> {
            arrangeCities();
            Window window1 = dialog.getWindow();
            window1.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.show();
        });
/*

        btn_dList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Window window = dialog.getWindow();
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.show();
            }
        });

 */


        listViewDeliveries.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                delivery = (Delivery) parent.getItemAtPosition(position);
                if (operationType.equalsIgnoreCase(HelperConstants.DELIVERY_DELIVER)) {
                    Intent intentDelivery = new Intent(DeliveryList.this, DeliveryDeliver.class);
                    intentDelivery.putExtra("delivery", delivery);
                    startActivityForResult(intentDelivery, 1);
                } else if (operationType.equalsIgnoreCase(HelperConstants.DELIVERY_UPDATE)) {
                    Intent intentDelivery = new Intent(DeliveryList.this, DeliveryUpdate.class);
                    intentDelivery.putExtra("delivery", delivery);
                    startActivityForResult(intentDelivery, 200);
                } else if (operationType.equalsIgnoreCase(HelperConstants.DELIVERY_DELETE)) {
                    Intent intentDelivery = new Intent(DeliveryList.this, DeliveryDelete.class);
                    intentDelivery.putExtra("delivery", delivery);
                    startActivityForResult(intentDelivery, 3);
                } else if (operationType.equalsIgnoreCase(HelperConstants.DELIVERY_ASSIGN)) {
                    Intent intentDelivery = new Intent(DeliveryList.this, DeliveryAssign.class);
                    intentDelivery.putExtra("delivery", delivery);
                    startActivityForResult(intentDelivery, 5);
                } else {
                    Intent intentDelivery = new Intent(DeliveryList.this, DeliveryObserve.class);
                    intentDelivery.putExtra("delivery", delivery);
                    startActivityForResult(intentDelivery, 4);
                }

            }
        });



        postmans.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    acceptedPostman = postmans.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                    acceptedPostman = "%";
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


        try {
            PostHelper.listPostmans("%", DeliveryList.this, postmans);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        arrangeCities();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Bul jerde listelegende, bashka operasyon yaptiktan sonra listeni yenilemiyorum...
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200 || requestCode == 1|| requestCode == 3) {
            if (resultCode == RESULT_OK) {
                try {
                    listDeliveries(ed_Date.getText().toString(), ed_Address.getText().toString(), ed_Name.getText().toString(), ed_Phone.getText().toString(), payment_type);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public void listDeliveries(final String entryDate, final String address, final String name, final String phone, final String payment_type) throws ParseException {

        if (!NetworkUtil.isNetworkConnected(DeliveryList.this)) {
            MyDialog.createSimpleOkErrorDialog(DeliveryList.this,
                    getApplicationContext().getString(R.string.dialog_error_title),
                    getApplicationContext().getString(R.string.check_internet)).show();
        } else if (NetworkUtil.isTokenExpired()) {
            MyDialog.createSimpleOkErrorDialog(DeliveryList.this,
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
                jsonObject.put("entryDate", entryDate);
                jsonObject.put("status", status);
                jsonObject.put("receiverCity", receiverCity + "%");
                jsonObject.put("senderCity", senderCity + "%");
                jsonObject.put("assignedSector", assignedPostman);
                jsonObject.put("address", address + "%");
                jsonObject.put("paymentType", payment_type);
                jsonObject.put("name", name + "%");
                jsonObject.put("phone", phone + "%");
                jsonObject.put("acceptedPerson", acceptedPostman);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            CustomJsonArrayRequest req = new CustomJsonArrayRequest(Request.Method.POST, AppConfig.URL_DELIVERY_LIST, jsonObject,
                    response -> {

                        try {
                            // Check for error node in json
                            if (response.length() > 0) {

                                bulkImage.setVisibility(View.GONE);

                                JsonParser parser = new JsonParser();
                                Gson gson = new Gson();

                                for (int i = 0; i < response.length(); i++) {

                                    JsonElement mJsonM = parser.parse(response.getString(i));
                                    Delivery dd = gson.fromJson(mJsonM, Delivery.class);

                                    dd.number = i + 1;
                                    /*
                                    dd.sFullAddress = dd.senderCity + " - " + dd.senderPhone;
                                    if (dd.senderName.length() > 3)
                                        dd.sFullAddress = dd.sFullAddress + ", " + dd.senderName;
                                    if (dd.senderAddress.length() > 3)
                                        dd.sFullAddress = dd.sFullAddress + ", " + dd.senderAddress;
                                    if (dd.senderCompany.length() > 3)
                                        dd.sFullAddress = dd.sFullAddress + ", " + dd.senderCompany;

                                    dd.rFullAddress = dd.receiverCity + " - " + dd.receiverPhone;
                                    if (dd.receiverName.length() > 3)
                                        dd.rFullAddress = dd.rFullAddress + ", " + dd.receiverName;
                                    if (dd.receiverAddress.length() > 3)
                                        dd.rFullAddress = dd.rFullAddress + ", " + dd.receiverAddress;
                                    if (dd.receiverCompany.length() > 3)
                                        dd.rFullAddress = dd.rFullAddress + ", " + dd.receiverCompany;
*/
                                    deliveryList.add(dd);
                                }
                                if (deliveryList.size() > 0) {
                                    DeliveryListAdapter orderListAdapter = new DeliveryListAdapter(deliveryList, DeliveryList.this);
                                    listViewDeliveries.setAdapter(orderListAdapter);
                                }
                            } else {
                                bulkImage.setVisibility(View.VISIBLE);
                                Toast.makeText(DeliveryList.this, getApplicationContext().getString(R.string.NoData), Toast.LENGTH_LONG).show();
                            }
                            hideDialog();
                        } catch (JSONException e) {
                            hideDialog();
                            bulkImage.setVisibility(View.VISIBLE);
                            Toast.makeText(DeliveryList.this, getApplicationContext().getString(R.string.ErrorWhenLoading), Toast.LENGTH_LONG).show();
                        }
                    }, error -> {
                        bulkImage.setVisibility(View.VISIBLE);
                        NetworkUtil.checkHttpStatus(DeliveryList.this, error);
                        hideDialog();
                    }) {

                @Override
                public Map<String, String> getHeaders() {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("Authorization", token);
                    return headers;
                }

            };

            req.setRetryPolicy(new DefaultRetryPolicy(6000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(req, tag_string_req);
        }
    }


    private void arrangeCities() {

        Intent orderIntent = getIntent();
        Bundle extras = orderIntent.getExtras();

        if (extras != null) {

            operationType = extras.getString(HelperConstants.DELIVERY_OPERATION);

            if (operationType.equalsIgnoreCase(HelperConstants.DELIVERY_DELIVER)) {
                receiverCity = HomeActivity.userCity;
                rCity.setSelection(getIndex(rCity, receiverCity));
                status = HelperConstants.DELIVERY_STATUS_NEW;
            } else if (operationType.equalsIgnoreCase(HelperConstants.DELIVERY_UPDATE)) {
                status = "%";
                sCity.setSelection(getIndex(sCity, senderCity));
            } else if (operationType.equalsIgnoreCase(HelperConstants.DELIVERY_ASSIGN)) {
                status = HelperConstants.DELIVERY_STATUS_NEW;
            } else {
                status = "%";
                sCity.setSelection(getIndex(sCity, HomeActivity.userCity));
            }

        }
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

        if (id == DIALOG_ID)
            return new DatePickerDialog(this, datePickerListener, year_x, month_x, day_x);
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
            ed_Date.setText(dateS);

        }
    };

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}
