package com.kg.megaregionapp.expense;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.kg.megaregionapp.HomeActivity;
import com.kg.megaregionapp.R;
import com.kg.megaregionapp.app.AppConfig;
import com.kg.megaregionapp.app.AppController;
import com.kg.megaregionapp.helper.CustomJsonArrayRequest;
import com.kg.megaregionapp.helper.PostHelper;
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

public class CollectionAmounts extends AppCompatActivity {
    private static final String TAG = CollectionAmounts.class.getSimpleName();
    public static int DIALOG_ID = 0;
    private EditText ed_Date, ed_PaymentAmount, ed_Remaining;
    private EditText dd_Date, dd_Postman, dd_Amount, dd_Expense, dd_Fuel;
    Spinner spinner_users;
    private ListView collectionListView;
    private int year_x, month_x, day_x;
    private Calendar calendar;
    private ProgressDialog pDialog;
    private List<Expense> collectionList = new ArrayList<>();
    private long totalCollection = 0;
    private long totalPodOtchet = 0;
    private Switch permissionSwitch;
    private Button btn_open_dialog, dd_cancel, dd_save;
    private boolean permissionOperation = true;
    private Dialog dialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_amounts);

        ed_Date =  findViewById(R.id.ed_Date_Collection);
        ed_PaymentAmount = findViewById(R.id.ed_payment_amount);
        ed_Remaining = findViewById(R.id.ed_remaining);
        collectionListView =  findViewById(R.id.listViewCollections);
        spinner_users =  findViewById(R.id.spinner_postman);
        btn_open_dialog =  findViewById(R.id.btn_save_data);

        permissionSwitch = findViewById(R.id.switch_permission);

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(true);

        permissionSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            try {
                String postman = spinner_users.getSelectedItem().toString();
                if (postman.length() > 2 && permissionOperation)
                    updatePermission(isChecked, postman);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        });



        try {
            PostHelper.listPostmans(HomeActivity.userCity, CollectionAmounts.this, spinner_users);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");//dd/MM/yyyy
        Date now = new Date();
        final String strDate = sdfDate.format(now);

        ed_Date.setText(strDate);

        calendar = Calendar.getInstance();
        year_x = calendar.get(Calendar.YEAR);
        month_x = calendar.get(Calendar.MONTH);
        day_x = calendar.get(Calendar.DAY_OF_MONTH);

        ed_Date.setOnClickListener(v -> showDialog(DIALOG_ID));


        dialog = new Dialog(this);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LayoutInflater inflater = this.getLayoutInflater();
        View vDialog = inflater.inflate(R.layout.record_expense_dialog, null);  // this line
        dialog.setContentView(vDialog);

        dd_Date = vDialog.findViewById(R.id.dd_date);
        dd_Postman = vDialog.findViewById(R.id.dd_postman);
        dd_Amount = vDialog.findViewById(R.id.dd_collection_amount);
        dd_Expense = vDialog.findViewById(R.id.dd_expense);
        dd_cancel = vDialog.findViewById(R.id.dd_btn_cancel);
        dd_save = vDialog.findViewById(R.id.dd_btn_save_data);
        dd_Fuel = vDialog.findViewById(R.id.dd_fuel);
        dd_Date.setText(strDate);
        dd_Expense.setText("0");
        dd_Fuel.setText("0");

        btn_open_dialog.setOnClickListener(v -> {
            Window window = dialog.getWindow();
            dd_Expense.setText("0");
            dd_Fuel.setText("0");
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.show();
        });

        dd_cancel.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dd_save.setOnClickListener(v -> {
            try {
                saveCollection();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        });

        spinner_users.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String postman = spinner_users.getSelectedItem().toString();
                dd_Postman.setText(postman);
                try {
                    if (postman.length() > 1)
                    {
                        getPermission(postman);
                        listTransactions(ed_Date.getText().toString(), ed_Date.getText().toString(), postman);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }



    private void saveCollection() throws ParseException {

        if (!NetworkUtil.isNetworkConnected(CollectionAmounts.this)) {
            MyDialog.createSimpleOkErrorDialog(CollectionAmounts.this,
                    getApplicationContext().getString(R.string.dialog_error_title),
                    getApplicationContext().getString(R.string.check_internet)).show();
        } else if (NetworkUtil.isTokenExpired()) {
            MyDialog.createSimpleOkErrorDialog(CollectionAmounts.this,
                    getApplicationContext().getString(R.string.dialog_error_title),
                    getApplicationContext().getString(R.string.relogin)).show();
        } else {
            String tag_string_req = "req_update_user_permission";

            pDialog.setMessage("Процесс идет ...");
            showDialog();

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("amount", dd_Amount.getText());
                jsonObject.put("enteredUser", HomeActivity.userLogin);
                jsonObject.put("postman", dd_Postman.getText());
                jsonObject.put("expense", dd_Expense.getText());
                jsonObject.put("fuel", dd_Fuel.getText());
                jsonObject.put("expenseDate", dd_Date.getText());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            JsonObjectRequest req = new JsonObjectRequest(AppConfig.URL_EXPENSE_SAVE, jsonObject,
                    response -> {
                        hideDialog();
                        dialog.dismiss();
                    }, error -> {
                NetworkUtil.checkHttpStatus(CollectionAmounts.this, error);
                hideDialog();

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


    private void updatePermission(final boolean isChecked, final String postman) throws ParseException {

        if (!NetworkUtil.isNetworkConnected(CollectionAmounts.this)) {
            MyDialog.createSimpleOkErrorDialog(CollectionAmounts.this,
                    getApplicationContext().getString(R.string.dialog_error_title),
                    getApplicationContext().getString(R.string.check_internet)).show();
        } else if (NetworkUtil.isTokenExpired()) {
            MyDialog.createSimpleOkErrorDialog(CollectionAmounts.this,
                    getApplicationContext().getString(R.string.dialog_error_title),
                    getApplicationContext().getString(R.string.relogin)).show();
        } else {
            String tag_string_req = "req_update_user_permission";

            pDialog.setMessage("Saving Data ...");
            showDialog();

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("permission", isChecked);
                jsonObject.put("user", HomeActivity.userLogin);
                jsonObject.put("postman", postman);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JsonObjectRequest req = new JsonObjectRequest(AppConfig.URL_USER_PERMISSION, jsonObject,
                    response -> hideDialog(), error -> {
                        NetworkUtil.checkHttpStatus(CollectionAmounts.this, error);
                        hideDialog();
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

    private void getPermission(final String postman) throws ParseException {

        if (!NetworkUtil.isNetworkConnected(CollectionAmounts.this)) {
            MyDialog.createSimpleOkErrorDialog(CollectionAmounts.this,
                    getApplicationContext().getString(R.string.dialog_error_title),
                    getApplicationContext().getString(R.string.check_internet)).show();
        } else if (NetworkUtil.isTokenExpired()) {
            MyDialog.createSimpleOkErrorDialog(CollectionAmounts.this,
                    getApplicationContext().getString(R.string.dialog_error_title),
                    getApplicationContext().getString(R.string.relogin)).show();
        } else {
            String tag_string_req = "req_get_user_permission";
            permissionOperation = false;
            JsonObjectRequest req = new JsonObjectRequest(AppConfig.URL_GET_USER_PERMISSION + "/" + postman, null,
                    response -> {
                        if (response != null)
                        {
                            JsonParser parser = new JsonParser();
                            Gson gson = new Gson();
                            JsonElement mJsonM = parser.parse(response.toString());
                            Permission dd = gson.fromJson(mJsonM, Permission.class);
                            if(dd.permission)
                                permissionSwitch.setChecked(false);
                            else
                                permissionSwitch.setChecked(true);
                        }
                        permissionOperation = true;
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    NetworkUtil.checkHttpStatus(CollectionAmounts.this, error);
                    permissionOperation = true;
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

    public void listCollections(final String collectionDate, final String postman) throws ParseException {

        if (!NetworkUtil.isNetworkConnected(CollectionAmounts.this)) {
            MyDialog.createSimpleOkErrorDialog(CollectionAmounts.this,
                    getApplicationContext().getString(R.string.dialog_error_title),
                    getApplicationContext().getString(R.string.check_internet)).show();
        } else if (NetworkUtil.isTokenExpired()) {
            MyDialog.createSimpleOkErrorDialog(CollectionAmounts.this,
                    getApplicationContext().getString(R.string.dialog_error_title),
                    getApplicationContext().getString(R.string.relogin)).show();
        } else {
            String tag_string_req = "req_list_collections";
            pDialog.setMessage("Listing Collections...");
            showDialog();

            collectionList.clear();
            collectionListView.setAdapter(null);
            totalCollection = 0;

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("collectionDate", collectionDate);
                jsonObject.put("postman", postman);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            CustomJsonArrayRequest req = new CustomJsonArrayRequest(Request.Method.POST, AppConfig.URL_COLLECTION_LIST, jsonObject,
                    response -> {
                        hideDialog();
                        long summa = 0;
                        try {
                            if (response.length() > 0) {

                                int count = 1;
                                for (int i = 0; i < response.length(); i++) {
                                    JSONObject c = response.getJSONObject(i);
                                    Expense ex = new Expense();
                                    ex.exAmount = c.getString("amount");
                                    summa = summa + Long.valueOf(ex.exAmount);
                                    ex.exDate = count + ".";
                                    ex.exName = c.getString("senderCity") + "-" + c.getString("receiverCity") + "\n" +
                                            c.getString("name") + "-" + c.getString("phone") + "-" + c.getString("address")
                                            + "\n" + c.getString("paymentType");
                                    collectionList.add(ex);
                                    count++;
                                }
                                totalCollection = summa;

                                if (summa != 0) {
                                    Expense ex = new Expense();
                                    ex.exAmount = String.valueOf(summa);
                                    ex.exName = "Жалпы";
                                    collectionList.add(ex);
                                }
                                // update the adapater
                                if (collectionList.size() > 0) {
                                    CollectionListAdapter collListAdapter = new CollectionListAdapter(collectionList, CollectionAmounts.this);
                                    collectionListView.setAdapter(collListAdapter);
                                }
                            }

                            ed_Remaining.setText(totalPodOtchet + totalCollection + "");
                            dd_Amount.setText(totalPodOtchet + totalCollection + "");

                        } catch (JSONException e) {
                            MyDialog.createSimpleOkErrorDialog(CollectionAmounts.this,
                                    getApplicationContext().getString(R.string.dialog_error_title),
                                    getApplicationContext().getString(R.string.ErrorWhenLoading)).show();
                        }

                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    NetworkUtil.checkHttpStatus(CollectionAmounts.this, error);
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

    public void listTransactions(final String date1, final String date2, final String postman) throws ParseException {

        if (!NetworkUtil.isNetworkConnected(CollectionAmounts.this)) {
            MyDialog.createSimpleOkErrorDialog(CollectionAmounts.this,
                    getApplicationContext().getString(R.string.dialog_error_title),
                    getApplicationContext().getString(R.string.check_internet)).show();
        } else if (NetworkUtil.isTokenExpired()) {
            MyDialog.createSimpleOkErrorDialog(CollectionAmounts.this,
                    getApplicationContext().getString(R.string.dialog_error_title),
                    getApplicationContext().getString(R.string.relogin)).show();
        } else {
            String tag_string_req = "req_list_expenses";
            pDialog.setMessage("Listing Transactions...");
            showDialog();

            totalPodOtchet = 0;

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("postman", postman);
                jsonObject.put("date1", date1);
                jsonObject.put("date2", date2);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            CustomJsonArrayRequest req = new CustomJsonArrayRequest(Request.Method.POST, AppConfig.URL_TRANSACTIONS_LIST, jsonObject,
                    response -> {
                        hideDialog();
                        try {
                            if (response.length() > 0) {
                                long totalAmount = 0;
                                for (int i = 0; i < response.length(); i++) {
                                    JSONObject c = response.getJSONObject(i);
                                    totalAmount = totalAmount + Long.parseLong(c.getString("amount"));
                                }
                                totalPodOtchet = totalAmount;
                                ed_PaymentAmount.setText("" + totalAmount);
                            } else {
                                totalPodOtchet = 0;
                                ed_PaymentAmount.setText("0");
                            }

                            listCollections(ed_Date.getText().toString(), postman);

                        } catch (JSONException | ParseException e) {
                            MyDialog.createSimpleOkErrorDialog(CollectionAmounts.this,
                                    getApplicationContext().getString(R.string.dialog_error_title),
                                    getApplicationContext().getString(R.string.ErrorWhenLoading)).show();
                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    NetworkUtil.checkHttpStatus(CollectionAmounts.this, error);
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
            dd_Date.setText(dateS);
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

    private class Permission
    {
        boolean permission;
    }

}
