package com.kg.megaregionapp.delivery;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.kg.megaregionapp.HomeActivity;
import com.kg.megaregionapp.R;
import com.kg.megaregionapp.app.AppConfig;
import com.kg.megaregionapp.app.AppController;
import com.kg.megaregionapp.helper.StringData;
import com.kg.megaregionapp.utils.MyDialog;
import com.kg.megaregionapp.utils.NetworkUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

public class DeliveryObserve extends AppCompatActivity {
    private static final String TAG = DeliveryObserve.class.getSimpleName();
    private Spinner sCity, rCity, delType;
    private EditText sName, sPhone, sComp, sAdres;
    private EditText rName, rPhone, rComp, rAdres;
    private EditText delCount, delPrice, delItemPrice, delExpl, differentReceiver, paidAmount;
    private RadioButton rb_rc, rb_sc, rb_sb, rb_rb;
    private RadioButton rb_bc, rb_bd, rb_bt;
    private ImageView imageViewDelivery;

    String senderSignatureString = "";
    String receiverSignatureString = "";
    String differentReceiverString = "";
    LinearLayout senderSignature;
    Delivery deliveryData;

    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_observe);

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        initializeItems();

        ArrayAdapter<String> cityAdapter = new ArrayAdapter<String>(
                DeliveryObserve.this,
                android.R.layout.simple_spinner_dropdown_item,
                StringData.getCityList()
        );

        sCity.setAdapter(cityAdapter);
        rCity.setAdapter(cityAdapter);


        Intent deliveryIntent = getIntent();
        Bundle extras = deliveryIntent.getExtras();
        if (extras != null) {
            deliveryData = (Delivery) deliveryIntent.getSerializableExtra("delivery");
            putIncomingData(deliveryData);
            /*
            try {
                getDelivery(deliveryData.deliveryId);
            } catch (ParseException e) {
                e.printStackTrace();
            }

             */
        }
    }


    public void getDelivery(final String id) throws ParseException {

        if (!NetworkUtil.isNetworkConnected(DeliveryObserve.this)) {
            MyDialog.createSimpleOkErrorDialog(DeliveryObserve.this,
                    getApplicationContext().getString(R.string.dialog_error_title),
                    getApplicationContext().getString(R.string.check_internet)).show();
        } else if (NetworkUtil.isTokenExpired()) {
            MyDialog.createSimpleOkErrorDialog(DeliveryObserve.this,
                    getApplicationContext().getString(R.string.dialog_error_title),
                    getApplicationContext().getString(R.string.relogin)).show();
        } else {
            String tag_string_req = "req_get_deliveries";
            pDialog.setMessage("Getting Receiver Signature ...");
            showDialog();

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("deliveryId", id);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, AppConfig.URL_DELIVERY_GET, jsonObject,
                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d(TAG, "Delivery Get Response: " + response);
                            hideDialog();

                            try {
                                // Check for error node in json
                                if (response.getString("deliveryId").length() > 0) {

                                    senderSignatureString = "";
                                    receiverSignatureString = response.getString("receiverSignature");
                                    differentReceiverString = response.getString("receiver");
                                    showSignatures();

                                } else {
                                    MyDialog.createSimpleOkErrorDialog(DeliveryObserve.this,
                                            getApplicationContext().getString(R.string.dialog_error_title),
                                            getApplicationContext().getString(R.string.NoData)).show();
                                }
                            } catch (JSONException e) {
                                MyDialog.createSimpleOkErrorDialog(DeliveryObserve.this,
                                        getApplicationContext().getString(R.string.dialog_error_title),
                                        getApplicationContext().getString(R.string.ErrorWhenLoading)).show();
                            }
                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {

                    NetworkUtil.checkHttpStatus(DeliveryObserve.this, error);
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

    private void showSignatures() {

        if (receiverSignatureString.length() > 0) {
            byte[] a = Base64.decode(receiverSignatureString, Base64.DEFAULT);
            Bitmap image = BitmapFactory.decodeByteArray(a, 0, a.length);
            BitmapDrawable background = new BitmapDrawable(this.getResources(), image);
        }

        differentReceiver.setText(differentReceiverString);

    }

    public void initializeItems() {

        imageViewDelivery = findViewById(R.id.imageDeliveryPhoto);
        rb_rb = findViewById(R.id.rb_rb);
        rb_rc = findViewById(R.id.rb_rc);
        rb_sb = findViewById(R.id.rb_sb);
        rb_sc = findViewById(R.id.rb_sc);


        rb_bc = findViewById(R.id.rb_buy_cash);
        rb_bt = findViewById(R.id.rb_buy_transfer);
        rb_bd = findViewById(R.id.rb_buy_debt);

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
        differentReceiver = findViewById(R.id.differentReceiver);
        paidAmount = findViewById(R.id.deliveryPaidAmount);


        sCity = findViewById(R.id.spinner_senderCity);
        rCity = findViewById(R.id.spinner_receiverCity);
        delType = findViewById(R.id.spinner_deliveryType);

        senderSignature = findViewById(R.id.linearLayoutS);

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
        paidAmount.setText(delivery.paidAmount);

        differentReceiver.setText(delivery.receiver);

        setRadioGroupValue(delivery.paymentType);
        setBuyingRadioGroupValue(delivery.buyType);

        if (delivery.deliveryImage != null && delivery.deliveryImage.length() > 1)
            Glide.with(DeliveryObserve.this).load(AppConfig.IMAGES_URL + delivery.deliveryImage).into(imageViewDelivery);
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

    private void setBuyingRadioGroupValue(String selectedValue) {

        if (selectedValue.equalsIgnoreCase("C"))
            rb_bc.setChecked(true);
        else if (selectedValue.equalsIgnoreCase("D"))
            rb_bd.setChecked(true);
        else if (selectedValue.equalsIgnoreCase("T"))
            rb_bt.setChecked(true);

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
