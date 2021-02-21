package com.kg.megaregionapp.delivery;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.kg.megaregionapp.HomeActivity;
import com.kg.megaregionapp.R;
import com.kg.megaregionapp.app.AppConfig;
import com.kg.megaregionapp.app.AppController;
import com.kg.megaregionapp.customer.CorporateSelectionList;
import com.kg.megaregionapp.helper.HelperConstants;
import com.kg.megaregionapp.helper.StringData;
import com.kg.megaregionapp.utils.MyDialog;
import com.kg.megaregionapp.utils.NetworkUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

public class DeliveryUpdate extends AppCompatActivity {

    private static final String TAG = DeliveryUpdate.class.getSimpleName();
    public static final int SENDER_COMPANY_LIST = 2;
    public static final int RECEIVER_COMPANY_LIST = 3;

    private Spinner sCity, rCity, delType;
    private EditText sName, sPhone, sComp, sAdres;
    private EditText rName, rPhone, rComp, rAdres;
    private EditText delCount, delPrice, delItemPrice, delExpl, paidAmount;
    private Button btn_save_data;
    private ProgressDialog pDialog;
    private RadioGroup rg_payment;
    private RadioGroup rg_buying;
    private RadioButton rb_rc, rb_sc, rb_sb, rb_rb, rb_bc, rb_bt, rb_bd;
    private CardView card_view_saving;

    Delivery deliveryData;
    private String currentUser;
    private String token;

    private String senderCity, receiverCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_update);

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        initializeItems();

        currentUser = HomeActivity.userLogin;
        token = HomeActivity.token;

        ArrayAdapter<String> cityAdapter = new ArrayAdapter<String>(
                DeliveryUpdate.this,
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
        }

        btn_save_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (deliveryDataCheck()) {
                    try {
                        updateDelivery(sName.getText().toString(), sPhone.getText().toString(), sComp.getText().toString(), sCity.getSelectedItem().toString(), sAdres.getText().toString(),
                                rName.getText().toString(), rPhone.getText().toString(), rComp.getText().toString(), rCity.getSelectedItem().toString(), rAdres.getText().toString(),
                                delType.getSelectedItem().toString(), delCount.getText().toString(), delPrice.getText().toString(), paidAmount.getText().toString(), delItemPrice.getText().toString(),
                                getRadioGroupValue(), delExpl.getText().toString(), deliveryData.deliveryId, currentUser, getBuyingRadioGroupValue());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        });


        rg_payment.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton checkedRadioButton = (RadioButton) group.findViewById(checkedId);
                // This puts the value (true/false) into the variable

                if (checkedRadioButton!=null && checkedRadioButton.equals(rb_rb)) {

                    Intent intent = new Intent(DeliveryUpdate.this, CorporateSelectionList.class);

                    if (rCity.getSelectedItem() != null && rCity.getSelectedItem().toString().length() > 0) {
                        if (rComp.getText() == null || rComp.getText().toString().length() < 1) {
                            intent.putExtra("city", rCity.getSelectedItem().toString());
                            startActivityForResult(intent, RECEIVER_COMPANY_LIST);
                        }
                        else
                        {
                            try {
                                checkCorporateCustomer(rCity.getSelectedItem().toString(), rComp.getText().toString(), intent, RECEIVER_COMPANY_LIST);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        rb_rb.setChecked(false);
                        rAdres.setEnabled(true);
                        rComp.setEnabled(true);
                        Toast.makeText(getApplicationContext(), "Биринчи кайсыл шаарга кетишин тандаңыз.", Toast.LENGTH_LONG).show();
                    }


                } else if (checkedRadioButton!=null && checkedRadioButton.equals(rb_sb)) {

                    Intent intent = new Intent(DeliveryUpdate.this, CorporateSelectionList.class);

                    if (sCity.getSelectedItem() != null && sCity.getSelectedItem().toString().length() > 0) {
                        if (sComp.getText() == null || sComp.getText().toString().length() < 1) {
                            intent.putExtra("city", sCity.getSelectedItem().toString());
                            startActivityForResult(intent, SENDER_COMPANY_LIST);
                        }
                        else
                        {
                            try {
                                checkCorporateCustomer(sCity.getSelectedItem().toString(), sComp.getText().toString(), intent, SENDER_COMPANY_LIST);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        rb_sb.setChecked(false);
                        sAdres.setEnabled(true);
                        sComp.setEnabled(true);
                        Toast.makeText(getApplicationContext(), "Биринчи кайсыл шаарга кетишин тандаңыз.", Toast.LENGTH_LONG).show();
                    }
                }
                else
                {
                    sAdres.setEnabled(true);
                    sComp.setEnabled(true);
                    rAdres.setEnabled(true);
                    rComp.setEnabled(true);
                }
            }
        });

    }


    private String getBuyingRadioGroupValue() {

        String selected = "";

        int selectedValue = rg_buying.getCheckedRadioButtonId();
        if (selectedValue == R.id.rb_buy_cash)
            selected = "C";
        else if (selectedValue == R.id.rb_buy_debt)
            selected = "D";
        else if (selectedValue == R.id.rb_buy_transfer)
            selected = "T";
        return selected;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SENDER_COMPANY_LIST) {
            if (resultCode == RESULT_OK) {

                Bundle extras = data.getExtras();
                if (extras != null) {
                    sAdres.setText(extras.getString("address"));
                    sComp.setText(extras.getString("company"));
                    sComp.setEnabled(false);
                    Toast.makeText(getApplicationContext(), "Жөнөтүүчү фирма аты жана адреси өзгөрдү!", Toast.LENGTH_LONG).show();
                }
            } else {
                rb_sb.setChecked(false);
                rg_payment.clearCheck();
                sComp.setEnabled(true);
                Toast.makeText(getApplicationContext(), "Төлөм түрү өзгөртүлдү!", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == RECEIVER_COMPANY_LIST) {
            if (resultCode == RESULT_OK) {

                Bundle extras = data.getExtras();
                if (extras != null) {
                    rAdres.setText(extras.getString("address"));
                    rComp.setText(extras.getString("company"));
                    rComp.setEnabled(false);
                    Toast.makeText(getApplicationContext(), "Алуучу фирма аты жана адреси өзгөрдү!", Toast.LENGTH_LONG).show();
                }
            } else {
                rb_rb.setChecked(false);
                rg_payment.clearCheck();
                rComp.setEnabled(true);
                Toast.makeText(getApplicationContext(), "Төлөм түрү өзгөртүлдү!", Toast.LENGTH_LONG).show();
            }
        }
    }


    public void checkCorporateCustomer(final String city, final String companyName, final Intent intent, final int COMPANY_LIST) throws ParseException {

        if (!NetworkUtil.isNetworkConnected(DeliveryUpdate.this)) {
            MyDialog.createSimpleOkErrorDialog(DeliveryUpdate.this,
                    DeliveryUpdate.this.getString(R.string.dialog_error_title),
                    DeliveryUpdate.this.getString(R.string.check_internet)).show();
        } else if (NetworkUtil.isTokenExpired()) {
            MyDialog.createSimpleOkErrorDialog(DeliveryUpdate.this,
                    DeliveryUpdate.this.getString(R.string.dialog_error_title),
                    DeliveryUpdate.this.getString(R.string.relogin)).show();
        } else {
            String tag_string_req = "req_check_corporate_customer";

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("company", companyName);
                jsonObject.put("city", city);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, AppConfig.URL_CORPORATE_CUSTOMER_CHECK, jsonObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            if (response == null) {
                                intent.putExtra("city", city);
                                startActivityForResult(intent, COMPANY_LIST);
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    intent.putExtra("city", city);
                    startActivityForResult(intent, COMPANY_LIST);
                }
            }) {
                @Override
                public Map<String, String> getHeaders() {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("Authorization", HomeActivity.token);
                    return headers;
                }
            };
            AppController.getInstance().addToRequestQueue(req, tag_string_req);
        }
    }

    private void updateDelivery(final String sName, final String sPhone, final String sComp, final String sCity, final String sAddress,
                                final String rName, final String rPhone, final String rComp, final String rCity, final String rAddress,
                                final String delType, final String delCount, final String delCost, final String paidAmount, final String deliCost,
                                final String paymentType, final String delExpl, final String id, final String user, final String buyType) throws ParseException {

        if (!NetworkUtil.isNetworkConnected(DeliveryUpdate.this)) {
            MyDialog.createSimpleOkErrorDialog(DeliveryUpdate.this,
                    getApplicationContext().getString(R.string.dialog_error_title),
                    getApplicationContext().getString(R.string.check_internet)).show();
        } else if (NetworkUtil.isTokenExpired()) {
            MyDialog.createSimpleOkErrorDialog(DeliveryUpdate.this,
                    getApplicationContext().getString(R.string.dialog_error_title),
                    getApplicationContext().getString(R.string.relogin)).show();
        } else {
            // Tag used to cancel the request
            String tag_string_req = "req_update_delivery";

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
                jsonObject.put("deliveryType", delType);
                jsonObject.put("deliveryCount", delCount);
                jsonObject.put("deliveryCost", delCost);
                jsonObject.put("paidAmount", paidAmount);
                jsonObject.put("deliveryiCost", deliCost);
                jsonObject.put("deliveryExplanation", delExpl);
                jsonObject.put("paymentType", paymentType);
                jsonObject.put("buyType", buyType);
                jsonObject.put("deliveryId", id);
                jsonObject.put("user", user);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            JsonObjectRequest strReq = new JsonObjectRequest(AppConfig.URL_DELIVERY_UPDATE, jsonObject,
                    response -> {
                        hideDialog();
                        if (response != null) {
                            Bundle b = new Bundle();
                            b.putString("STATUS", HelperConstants.DELIVERYACCEPTED);
                            Intent intent = new Intent();
                            intent.putExtras(b);
                            setResult(RESULT_OK, intent);
                            finish();
                        } else {
                            MyDialog.createSimpleOkErrorDialog(DeliveryUpdate.this,
                                    getApplicationContext().getString(R.string.dialog_error_title),
                                    getApplicationContext().getString(R.string.NoData)).show();
                        }
                    }, error -> {
                        NetworkUtil.checkHttpStatus(DeliveryUpdate.this, error);
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
            AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
        }
    }


    private String getRadioGroupValue() {

        String selected = "";

        int selectedValue = rg_payment.getCheckedRadioButtonId();
        if (selectedValue == R.id.rb_rb)
            selected = "RB";
        else if (selectedValue == R.id.rb_sb)
            selected = "SB";
        else if (selectedValue == R.id.rb_rc)
            selected = "RC";
        else if (selectedValue == R.id.rb_sc)
            selected = "SC";
        return selected;
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
        else if (selectedValue.equalsIgnoreCase("T"))
            rb_bt.setChecked(true);
        else if (selectedValue.equalsIgnoreCase("C"))
            rb_bc.setChecked(true);

    }

    public void initializeItems() {
        rg_payment =  findViewById(R.id.rg_payment);
        rg_buying =  findViewById(R.id.rg_buying);

        rb_rb =  findViewById(R.id.rb_rb);
        rb_rc =  findViewById(R.id.rb_rc);
        rb_sb =  findViewById(R.id.rb_sb);
        rb_sc =  findViewById(R.id.rb_sc);

        rb_bc =  findViewById(R.id.rb_buy_cash);
        rb_bd =  findViewById(R.id.rb_buy_debt);
        rb_bt =  findViewById(R.id.rb_buy_transfer);

        sName =  findViewById(R.id.senderName);
        sPhone =  findViewById(R.id.senderPhone);
        sAdres =  findViewById(R.id.senderAddress);
        sComp =  findViewById(R.id.senderCompany);
        rName =  findViewById(R.id.receiverName);
        rPhone =  findViewById(R.id.receiverPhone);
        rAdres =  findViewById(R.id.receiverAddress);
        rComp =  findViewById(R.id.receiverCompany);

        delExpl =  findViewById(R.id.deliveryExplanation);
        delCount =  findViewById(R.id.deliveryCount);
        delPrice =  findViewById(R.id.deliveryCost);
        delItemPrice =  findViewById(R.id.deliveryItemCost);
        paidAmount =  findViewById(R.id.deliveryPaidAmount);


        sCity =  findViewById(R.id.spinner_senderCity);
        rCity =  findViewById(R.id.spinner_receiverCity);
        delType =  findViewById(R.id.spinner_deliveryType);

        btn_save_data =  findViewById(R.id.btn_save_data);
        btn_save_data.setText(getResources().getString(R.string.DeliverUpdate));

        card_view_saving =  findViewById(R.id.card_view_saving);

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

        delType.setSelection(getIndex(delType, delivery.deliveryType));
        delCount.setText(delivery.deliveryCount);
        delPrice.setText(delivery.deliveryCost);
        delItemPrice.setText(delivery.deliveryiCost);
        paidAmount.setText(delivery.paidAmount);

        delExpl.setText(delivery.deliveryExplanation);

        setRadioGroupValue(delivery.paymentType);
        setBuyingRadioGroupValue(delivery.buyType);

        sCity.setSelection(getIndex(sCity, delivery.senderCity));
        rCity.setSelection(getIndex(rCity, delivery.receiverCity));

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

    private boolean deliveryDataCheck() {

        boolean ok = true;

        if (delItemPrice.getText().toString().length() == 0) {
            delItemPrice.setText("0");
        } else if (Long.parseLong(delItemPrice.getText().toString()) > 0) {
            if (rg_buying.getCheckedRadioButtonId() == -1) {
                String message = "Выкуп түрүн тандабадыңыз.";
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                return false;
            }
        }

        if (sName.length() < 1) {
            sName.setBackground(getShape(Color.MAGENTA));
            ok = false;
        } else {
            sName.setBackgroundColor(Color.WHITE);
        }
        if (sAdres.length() < 1) {
            sAdres.setBackground(getShape(Color.MAGENTA));
            ok = false;
        } else {
            sAdres.setBackgroundColor(Color.WHITE);
        }

        if (sCity.getSelectedItem().toString().equalsIgnoreCase("Москва")) {
            if(sPhone.length() < 10) {
                sPhone.setBackground(getShape(Color.MAGENTA));
                ok = false;
            }
        }
        else if (sPhone.length() != 10)
        {
            sPhone.setBackground(getShape(Color.MAGENTA));
            ok = false;
        }
        else {
            sPhone.setBackgroundColor(Color.WHITE);
        }

        if (rName.length() < 1) {
            rName.setBackground(getShape(Color.MAGENTA));
            ok = false;
        } else {
            rName.setBackgroundColor(Color.WHITE);
        }


        if (rCity.getSelectedItem().toString().equalsIgnoreCase("Москва")) {
            if(rPhone.length() < 10) {
                rPhone.setBackground(getShape(Color.MAGENTA));
                ok = false;
            }
        }
        else if (rPhone.length() != 10)
        {
            rPhone.setBackground(getShape(Color.MAGENTA));
            ok = false;
        }
        else {
            rPhone.setBackgroundColor(Color.WHITE);
        }

        if (delCount.length() < 1) {
            delCount.setBackground(getShape(Color.MAGENTA));
            ok = false;
        } else {
            delCount.setBackgroundColor(Color.WHITE);
        }

        if (delPrice.length() < 1) {
            delPrice.setBackground(getShape(Color.MAGENTA));
            ok = false;
        } else {
            delPrice.setBackgroundColor(Color.WHITE);
        }

        if (!ok) {
            String message = getResources().getString(R.string.FillAllDataCorrectly);
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
        }

        return ok;
    }

    private ShapeDrawable getShape(int color) {

        ShapeDrawable shape = new ShapeDrawable(new RectShape());
        shape.getPaint().setColor(color);
        shape.getPaint().setStyle(Paint.Style.STROKE);
        shape.getPaint().setStrokeWidth(3);

        return shape;
    }
}
