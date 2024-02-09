package com.kg.megaregionapp.expense;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.kg.megaregionapp.HomeActivity;
import com.kg.megaregionapp.R;
import com.kg.megaregionapp.app.AppConfig;
import com.kg.megaregionapp.app.AppController;
import com.kg.megaregionapp.helper.CustomJsonArrayRequest;
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

public class AccountantActivity extends AppCompatActivity {

    public static int DIALOG_ID = 0;
    private EditText ed_Date, ed_PaymentAmount, ed_ExpenseAmount, ed_Total, ed_Fuel;
    private ListView collectionListView;
    private int year_x, month_x, day_x;
    private Calendar calendar;
    private ProgressDialog pDialog;
    private List<Account> collectionList = new ArrayList<>();

    public long totalAmount, totalExpense, totalFuel, totalPayment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accountant);

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(true);

        ed_Date =  findViewById(R.id.ed_Date_Collection);
        ed_PaymentAmount = findViewById(R.id.ed_payment);
        ed_ExpenseAmount = findViewById(R.id.ed_expense);
        ed_Fuel =  findViewById(R.id.ed_fuel);
        ed_Total =  findViewById(R.id.ed_Total);
        collectionListView =  findViewById(R.id.listViewCollections);

        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");//dd/MM/yyyy
        Date now = new Date();
        final String strDate = sdfDate.format(now);

        ed_Date.setText(strDate);

        calendar = Calendar.getInstance();
        year_x = calendar.get(Calendar.YEAR);
        month_x = calendar.get(Calendar.MONTH);
        day_x = calendar.get(Calendar.DAY_OF_MONTH);

        ed_Date.setOnClickListener(v -> showDialog(DIALOG_ID));

    }

    public void listExpenses() throws ParseException {

        if (!NetworkUtil.isNetworkConnected(AccountantActivity.this)) {
            MyDialog.createSimpleOkErrorDialog(AccountantActivity.this,
                    getApplicationContext().getString(R.string.dialog_error_title),
                    getApplicationContext().getString(R.string.check_internet)).show();
        } else if (NetworkUtil.isTokenExpired()) {
            MyDialog.createSimpleOkErrorDialog(AccountantActivity.this,
                    getApplicationContext().getString(R.string.dialog_error_title),
                    getApplicationContext().getString(R.string.relogin)).show();
        } else {
            String tag_string_req = "req_list_collections";
            pDialog.setMessage("Listing Expenses...");
            showDialog();

            collectionList.clear();
            collectionListView.setAdapter(null);

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("expenseDate", ed_Date.getText());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            CustomJsonArrayRequest req = new CustomJsonArrayRequest(Request.Method.POST, AppConfig.URL_EXPENSE_LIST, jsonObject,
                    response -> {
                        hideDialog();
                        totalAmount = 0;
                        totalExpense = 0;
                        totalFuel = 0;
                        totalPayment = 0;
                        try {
                            if (response.length() > 0) {

                                for (int i = 0; i < response.length(); i++) {
                                    JSONObject c = response.getJSONObject(i);
                                    Account ex = new Account();
                                    ex.expense = Long.valueOf(c.getString("expense"));
                                    ex.amount = Long.valueOf(c.getString("amount"));
                                    ex.fuel = Long.valueOf(c.getString("fuel"));
                                    ex.postman = c.getString("postman");
                                    ex.expenseDate = c.getString("expenseDate");
                                    ex.expenseId = c.getString("expenseId");
                                    collectionList.add(ex);
                                    totalPayment += ex.amount;
                                    totalExpense += ex.expense;
                                    totalFuel += ex.fuel;
                                }
                                totalAmount = totalPayment - totalExpense - totalFuel;
                                ed_ExpenseAmount.setText(totalExpense + "");
                                ed_PaymentAmount.setText(totalPayment + "");
                                ed_Fuel.setText(totalFuel + "");
                                ed_Total.setText(totalAmount + "");
                                if (collectionList.size() > 0) {
                                    AccountListAdapter collListAdapter = new AccountListAdapter(collectionList, AccountantActivity.this);
                                    collectionListView.setAdapter(collListAdapter);
                                }
                            }

                        } catch (JSONException e) {
                            MyDialog.createSimpleOkErrorDialog(AccountantActivity.this,
                                    getApplicationContext().getString(R.string.dialog_error_title),
                                    getApplicationContext().getString(R.string.ErrorWhenLoading)).show();
                        }

                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    NetworkUtil.checkHttpStatus(AccountantActivity.this, error);
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
            try {
                listExpenses();
            } catch (ParseException e) {
                e.printStackTrace();
            }
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