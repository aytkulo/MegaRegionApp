package com.kg.megaregionapp.delivery;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.kg.megaregionapp.HomeActivity;
import com.kg.megaregionapp.R;
import com.kg.megaregionapp.app.AppConfig;
import com.kg.megaregionapp.app.AppController;
import com.kg.megaregionapp.customer.CustomerHelper;
import com.kg.megaregionapp.helper.HelperConstants;
import com.kg.megaregionapp.helper.SMSManager;
import com.kg.megaregionapp.helper.StringData;
import com.kg.megaregionapp.utils.MyDialog;
import com.kg.megaregionapp.utils.NetworkUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

public class DeliveryDeliver extends AppCompatActivity {
    private static final String TAG = DeliveryDeliver.class.getSimpleName();
    public static final int SIGNATURE_ACTIVITY = 1;

    private Spinner sCity, rCity, delType;
    private EditText sName, sPhone, sComp, sAdres;
    private EditText rName, rPhone, rComp, rAdres;
    private EditText delCount, delPrice, delItemPrice, delExpl, paidAmount;
    private Button btnSaveData;
    private ProgressDialog pDialog;
    private RadioGroup rg_payment;
    private RadioButton rb_rc, rb_sc, rb_sb, rb_rb;
    private RadioButton rb_bc, rb_bd, rb_bt;

    private EditText differentReceiver;
    private LinearLayout mContent;
    private Delivery deliveryData;
    private String currentUser;
    private String token;

    private Button btn_takePhoto;
    private ImageView imageDelivery;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_deliver);
        mContent = findViewById(R.id.linearLayout);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        initializeItems();
        disableItems();

        currentUser = HomeActivity.userLogin;
        token = HomeActivity.token;


        ArrayAdapter<String> cityAdapter = new ArrayAdapter<String>(
                DeliveryDeliver.this,
                android.R.layout.simple_spinner_dropdown_item,
                StringData.getCityList()
        );

        sCity.setAdapter(cityAdapter);
        rCity.setAdapter(cityAdapter);


        btnSaveData.setOnClickListener(v -> {
            if (differentReceiver.getText() != null && differentReceiver.getText().length() > 0) {
                try {
                    deliverDelivery(deliveryData.deliveryId, "", deliveryData.senderPhone, currentUser, differentReceiver.getText().toString());
                    CustomerHelper.saveCustomer(rName.getText().toString(), rPhone.getText().toString(), rComp.getText().toString(), rCity.getSelectedItem().toString(), rAdres.getText().toString(), token);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } else
                Toast.makeText(getApplicationContext(), getString(R.string.FillTheReceiverData), Toast.LENGTH_LONG).show();// Set your own toast  message
        });

        Intent deliveryIntent = getIntent();
        Bundle extras = deliveryIntent.getExtras();
        if (extras != null) {
            deliveryData = (Delivery) deliveryIntent.getSerializableExtra("delivery");
            putIncomingData(deliveryData);
        }

    }


    private void deliverDelivery(final String id, final String signature, final String sPhone, final String currentUser, final String differentReceiver) throws ParseException {

        if (!NetworkUtil.isNetworkConnected(DeliveryDeliver.this)) {
            MyDialog.createSimpleOkErrorDialog(DeliveryDeliver.this,
                    getApplicationContext().getString(R.string.dialog_error_title),
                    getApplicationContext().getString(R.string.check_internet)).show();
        } else if (NetworkUtil.isTokenExpired()) {
            MyDialog.createSimpleOkErrorDialog(DeliveryDeliver.this,
                    getApplicationContext().getString(R.string.dialog_error_title),
                    getApplicationContext().getString(R.string.relogin)).show();
        } else {
            // Tag used to cancel the request
            String tag_string_req = "req_deliver_delivery";

            pDialog.setMessage(getApplicationContext().getString(R.string.Processing));
            showDialog();

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("deliveryId", id);
                jsonObject.put("signature", signature);
                jsonObject.put("user", currentUser);
                jsonObject.put("receiverName", differentReceiver);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JsonObjectRequest req = new JsonObjectRequest(AppConfig.URL_DELIVERY_DELIVER, jsonObject,
                    response -> {
                        hideDialog();
                        try {

                            if (response.getString("deliveryId").length() > 0) {

                                String expl = getResources().getString(R.string.DeliveryMessage) + " Баракат Экспрес.";
                                if (differentReceiver.length() > 0)
                                    expl = expl + "(" + differentReceiver + ")";
                                SMSManager.sendAcceptanceSMS(sPhone, expl);

                                Bundle b = new Bundle();
                                b.putString("STATUS", HelperConstants.DELIVERYDELIVERED);
                                Intent intent = new Intent();
                                intent.putExtras(b);
                                setResult(RESULT_OK, intent);
                                finish();

                            }
                        } catch (JSONException e) {
                            Toast.makeText(getApplicationContext(),  getApplicationContext().getString(R.string.ErrorWhenLoading), Toast.LENGTH_LONG).show();
                        }
                    }, error -> {
                        NetworkUtil.checkHttpStatus(DeliveryDeliver.this, error);
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

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
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

        if (selectedValue.equalsIgnoreCase("D"))
            rb_bd.setChecked(true);
        else if (selectedValue.equalsIgnoreCase("C"))
            rb_bc.setChecked(true);
        else if (selectedValue.equalsIgnoreCase("T"))
            rb_bt.setChecked(true);
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
        paidAmount.setText(delivery.paidAmount);

        delExpl.setText(delivery.deliveryExplanation);

        setRadioGroupValue(delivery.paymentType);
        setBuyingRadioGroupValue(delivery.buyType);

        if (delivery.deliveryImage != null && delivery.deliveryImage.length() > 1)
            Glide.with(DeliveryDeliver.this).load(AppConfig.IMAGES_URL + delivery.deliveryImage).into(imageDelivery);
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

    public void initializeItems() {

        btn_takePhoto = findViewById(R.id.btn_take_photo);
        btn_takePhoto.setVisibility(View.GONE);

        imageDelivery = findViewById(R.id.imageDelivery);

        rg_payment = findViewById(R.id.rg_payment);
        rb_rb = findViewById(R.id.rb_rb);
        rb_rc = findViewById(R.id.rb_rc);
        rb_sb = findViewById(R.id.rb_sb);
        rb_sc = findViewById(R.id.rb_sc);

        rb_bc = findViewById(R.id.rb_buy_cash);
        rb_bd = findViewById(R.id.rb_buy_debt);
        rb_bt = findViewById(R.id.rb_buy_transfer);

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
        paidAmount = findViewById(R.id.deliveryPaidAmount);

        btnSaveData = findViewById(R.id.btn_save_data);

        sCity = findViewById(R.id.spinner_senderCity);
        rCity = findViewById(R.id.spinner_receiverCity);
        delType = findViewById(R.id.spinner_deliveryType);

        differentReceiver = findViewById(R.id.differentReceiver);

    }

    public void disableItems() {

        rg_payment.setEnabled(false);
        rb_rb.setEnabled(false);
        rb_rc.setEnabled(false);
        rb_sc.setEnabled(false);
        rb_sb.setEnabled(false);
        rb_bc.setEnabled(false);
        rb_bd.setEnabled(false);
        rb_bt.setEnabled(false);
        sName.setEnabled(false);
        sPhone.setEnabled(false);
        sAdres.setEnabled(false);
        sComp.setEnabled(false);
        rPhone.setEnabled(false);
        /*
        rName.setEnabled(false);
        rAdres.setEnabled(false);
        rComp.setEnabled(false);
*/
        delExpl.setEnabled(false);
        delCount.setEnabled(false);
        delPrice.setEnabled(false);
        delItemPrice.setEnabled(false);
        paidAmount.setEnabled(false);


        btnSaveData.setText(getResources().getString(R.string.DeliverDelivery));

        sCity.setEnabled(false);
        rCity.setEnabled(false);
        delType.setEnabled(false);

    }
}
