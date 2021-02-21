package com.kg.megaregionapp.delivery;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.kg.megaregionapp.HomeActivity;
import com.kg.megaregionapp.R;
import com.kg.megaregionapp.app.AppConfig;
import com.kg.megaregionapp.app.AppController;
import com.kg.megaregionapp.helper.HelperConstants;
import com.kg.megaregionapp.helper.PostHelper;
import com.kg.megaregionapp.users.User;
import com.kg.megaregionapp.utils.MyDialog;
import com.kg.megaregionapp.utils.NetworkUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DeliveryAssign extends AppCompatActivity {
    private static final String TAG = DeliveryAssign.class.getSimpleName();
    private ArrayList<User> userList = new ArrayList<>();
    private Button btn_assign;
    private Spinner spn_users;
    private Delivery delivery;
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_assign);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int) (width), (int) (height));

        btn_assign =  findViewById(R.id.btn_assign);
        spn_users =  findViewById(R.id.spinner_assigned_postman);


        Intent deliveryIntent = getIntent();
        Bundle extras = deliveryIntent.getExtras();
        if (extras != null) {
            delivery = (Delivery) deliveryIntent.getSerializableExtra("delivery");
        }


       // getSectors();

        try {
            PostHelper.listPostmans(HomeActivity.userCity, DeliveryAssign.this, spn_users);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        spn_users.setSelection(getIndex(spn_users, HomeActivity.userLogin));

        btn_assign.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    updateDelivery(spn_users.getSelectedItem().toString(), delivery.deliveryId);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    private void updateDelivery(final String assignedSector, final String id) throws ParseException {

        if (!NetworkUtil.isNetworkConnected(DeliveryAssign.this)) {
            MyDialog.createSimpleOkErrorDialog(DeliveryAssign.this,
                    getApplicationContext().getString(R.string.dialog_error_title),
                    getApplicationContext().getString(R.string.check_internet)).show();
        } else if (NetworkUtil.isTokenExpired()) {
            MyDialog.createSimpleOkErrorDialog(DeliveryAssign.this,
                    getApplicationContext().getString(R.string.dialog_error_title),
                    getApplicationContext().getString(R.string.relogin)).show();
        } else {  // Tag used to cancel the request
            String tag_string_req = "req_assign_delivery";

            pDialog.setMessage(getApplicationContext().getString(R.string.Processing));
            showDialog();

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("deliveryId", id);
                jsonObject.put("assignedSector", assignedSector);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JsonObjectRequest req = new JsonObjectRequest(AppConfig.URL_DELIVERY_ASSIGN, jsonObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            hideDialog();

                            if (response != null) {
                                Bundle b = new Bundle();
                                b.putString("STATUS", HelperConstants.DELIVERYASSIGNED);
                                Intent intent = new Intent();
                                intent.putExtras(b);
                                setResult(RESULT_OK, intent);
                                finish();
                            } else {
                                MyDialog.createSimpleOkErrorDialog(DeliveryAssign.this,
                                        getApplicationContext().getString(R.string.dialog_error_title),
                                        getApplicationContext().getString(R.string.NoData)).show();
                            }

                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    NetworkUtil.checkHttpStatus(DeliveryAssign.this, error);
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
            AppController.getInstance().addToRequestQueue(req, tag_string_req);
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


    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

}
