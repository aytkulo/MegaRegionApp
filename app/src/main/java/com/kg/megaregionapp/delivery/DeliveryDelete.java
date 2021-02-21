package com.kg.megaregionapp.delivery;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.kg.megaregionapp.HomeActivity;
import com.kg.megaregionapp.R;
import com.kg.megaregionapp.app.AppConfig;
import com.kg.megaregionapp.app.AppController;
import com.kg.megaregionapp.helper.HelperConstants;
import com.kg.megaregionapp.helper.StringData;
import com.kg.megaregionapp.utils.MyDialog;
import com.kg.megaregionapp.utils.NetworkUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

public class DeliveryDelete extends AppCompatActivity {
    private static final String TAG = DeliveryObserve.class.getSimpleName();
    private Spinner sCity, rCity, delType;
    private Button btn_delete;
    private EditText sName, sPhone, sComp, sAdres;
    private EditText rName, rPhone, rComp, rAdres;
    private EditText delCount, delPrice, delItemPrice, delExpl;
    private RadioButton rb_rc, rb_sc, rb_sb, rb_rb;

    Delivery deliveryData;
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_delete);

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        initializeItems();

        ArrayAdapter<String> cityAdapter = new ArrayAdapter<String>(
                DeliveryDelete.this,
                android.R.layout.simple_spinner_dropdown_item,
                StringData.getCityList()
        );
        rCity.setAdapter(cityAdapter);
        sCity.setAdapter(cityAdapter);

        Intent deliveryIntent = getIntent();
        Bundle extras = deliveryIntent.getExtras();
        if (extras != null) {
            deliveryData = (Delivery) deliveryIntent.getSerializableExtra("delivery");
            putIncomingData(deliveryData);
        }

        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlert();
            }
        });
    }


    private void showAlert() {
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle(getResources().getString(R.string.TitleAttention));
        adb.setMessage(getResources().getString(R.string.InformationWillBeDeleted));

        adb.setPositiveButton("МАКУЛ", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                try {
                    deleteDelivery(deliveryData.deliveryId, HomeActivity.userLogin);
                    finish();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });


        adb.setNegativeButton("ТОКТОТ", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        adb.show();
    }

    private void deleteDelivery(final String id, final String user) throws ParseException {

        if (!NetworkUtil.isNetworkConnected(DeliveryDelete.this)) {
            MyDialog.createSimpleOkErrorDialog(DeliveryDelete.this,
                    getApplicationContext().getString(R.string.dialog_error_title),
                    getApplicationContext().getString(R.string.check_internet)).show();
        } else if (NetworkUtil.isTokenExpired()) {
            MyDialog.createSimpleOkErrorDialog(DeliveryDelete.this,
                    getApplicationContext().getString(R.string.dialog_error_title),
                    getApplicationContext().getString(R.string.relogin)).show();
        } else {
            // Tag used to cancel the request
            String tag_string_req = "req_delete_delivery";

            pDialog.setMessage(getApplicationContext().getString(R.string.Processing));
            showDialog();

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("deliveryId", id);
                jsonObject.put("user", user);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JsonObjectRequest strReq = new JsonObjectRequest(Request.Method.POST, AppConfig.URL_DELIVERY_DELETE, jsonObject,
                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            hideDialog();

                            try {
                                if (response.getString("status").equalsIgnoreCase("-1")) {

                                    Bundle b = new Bundle();
                                    b.putString("STATUS", HelperConstants.DELIVERYDELETED);
                                    Intent intent = new Intent();
                                    intent.putExtras(b);
                                    setResult(RESULT_OK, intent);
                                    finish();

                                } else {
                                    MyDialog.createSimpleOkErrorDialog(DeliveryDelete.this,
                                            getApplicationContext().getString(R.string.dialog_error_title),
                                            getApplicationContext().getString(R.string.NoData)).show();
                                }
                            } catch (JSONException e) {
                                MyDialog.createSimpleOkErrorDialog(DeliveryDelete.this,
                                        getApplicationContext().getString(R.string.dialog_error_title),
                                        getApplicationContext().getString(R.string.ErrorWhenLoading)).show();
                            }
                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    NetworkUtil.checkHttpStatus(DeliveryDelete.this, error);
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
            AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
        }
    }


    public void initializeItems() {

        rb_rb = findViewById(R.id.rb_rb);
        rb_rc = findViewById(R.id.rb_rc);
        rb_sb = findViewById(R.id.rb_sb);
        rb_sc = findViewById(R.id.rb_sc);

        sName = findViewById(R.id.senderName);
        sPhone = findViewById(R.id.senderPhone);
        sAdres = findViewById(R.id.senderAddress);
        sComp = findViewById(R.id.senderCompany);
        rName = findViewById(R.id.receiverName);
        rPhone = findViewById(R.id.receiverPhone);
        rAdres = findViewById(R.id.receiverAddress);
        rComp = findViewById(R.id.receiverCompany);

        delExpl = findViewById(R.id.deliveryExplanation);
        delCount = findViewById(R.id.deliveryCount);
        delPrice = findViewById(R.id.deliveryCost);
        delItemPrice = findViewById(R.id.deliveryItemCost);

        btn_delete = findViewById(R.id.btn_delete_delivery);

        sCity = findViewById(R.id.spinner_senderCity);
        rCity = findViewById(R.id.spinner_receiverCity);
        delType = findViewById(R.id.spinner_deliveryType);

        delType.setEnabled(false);
        sCity.setEnabled(false);
        rCity.setEnabled(false);

    }

    public void putIncomingData(Delivery delivery) {

        sName.setText(delivery.senderName);
        sPhone.setText(delivery.senderPhone);
        sAdres.setText(delivery.senderAddress);
        sComp.setText(delivery.senderCompany);
        rName.setText(delivery.receiverName);
        rPhone.setText(delivery.receiverPhone);
        rAdres.setText(delivery.receiverAddress);
        rComp.setText(delivery.receiverCompany);
        sCity.setSelection(getIndex(sCity, delivery.senderCity));
        rCity.setSelection(getIndex(rCity, delivery.receiverCity));

        delType.setSelection(getIndex(delType, delivery.deliveryType));
        delCount.setText(delivery.deliveryCount);
        delPrice.setText(delivery.deliveryCost);
        delItemPrice.setText(delivery.deliveryiCost);

        delExpl.setText(delivery.deliveryExplanation);

        setRadioGroupValue(delivery.paymentType);
    }

    private void setRadioGroupValue(String selectedValue) {

        if (selectedValue.equalsIgnoreCase("RB"))
            rb_rb.setChecked(true);
        else if (selectedValue.equalsIgnoreCase("RC"))
            rb_rc.setChecked(true);
        else if (selectedValue.equalsIgnoreCase("SB"))
            rb_sb.setChecked(true);
        else if (selectedValue.equalsIgnoreCase("SC"))
            rb_sc.setChecked(true);
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

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}
