package com.kg.megaregionapp.expense;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.kg.megaregionapp.HomeActivity;
import com.kg.megaregionapp.R;
import com.kg.megaregionapp.app.AppConfig;
import com.kg.megaregionapp.app.AppController;
import com.kg.megaregionapp.helper.CustomJsonArrayRequest;
import com.kg.megaregionapp.helper.HelperConstants;
import com.kg.megaregionapp.helper.PostHelper;

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

public class Transaction extends AppCompatActivity {

    private static final String TAG = ExpenseList.class.getSimpleName();
    private ProgressDialog pDialog;
    public static int DIALOG_ID1 = 1;
    public static int DIALOG_ID2 = 2;
    ListView listViewExpenses;
    private EditText ed_Date1, ed_Date2, ed_total;
    private Spinner sPostmans;
    private Button btn_dList, btn_newE;
    private Calendar calendar;
    private String strDate = "";
    private int year_x, month_x, day_x;

    private List<Expense> expenseList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        listViewExpenses = findViewById(R.id.listViewExpenses);
        ed_Date1 = findViewById(R.id.ed_Date1);
        ed_Date2 = findViewById(R.id.ed_Date2);
        ed_total = findViewById(R.id.ed_total);

        sPostmans = findViewById(R.id.sp_postman);
        btn_dList = findViewById(R.id.btn_dList);
        btn_newE = findViewById(R.id.btn_newE);

        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");//dd/MM/yyyy
        Date now = new Date();
        strDate = sdfDate.format(now);

        ed_Date1.setText(strDate);
        ed_Date2.setText(strDate);

        calendar = Calendar.getInstance();
        year_x = calendar.get(Calendar.YEAR);
        month_x = calendar.get(Calendar.MONTH);
        day_x = calendar.get(Calendar.DAY_OF_MONTH);

        ed_Date1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(DIALOG_ID1);
            }
        });

        ed_Date2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(DIALOG_ID2);
            }
        });

        btn_newE.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentE = new Intent(Transaction.this, TransactionOperation.class);
                intentE.putExtra(HelperConstants.EXPENSE_OPERATION, HelperConstants.EXPENSE_NEW);
                startActivity(intentE);
            }
        });

        btn_dList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expenseList.clear();
                listViewExpenses.setAdapter(null);
                listTransactions(ed_Date1.getText().toString(), ed_Date2.getText().toString(), sPostmans.getSelectedItem().toString());
            }
        });


        listViewExpenses.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Expense expense = (Expense) parent.getItemAtPosition(position);
                Intent intentE = new Intent(Transaction.this, TransactionOperation.class);
                intentE.putExtra("expense", expense);
                intentE.putExtra(HelperConstants.EXPENSE_OPERATION, HelperConstants.EXPENSE_UPDATE);
                startActivity(intentE);
            }
        });

        try {
            PostHelper.listPostmans(HomeActivity.userCity, Transaction.this, sPostmans);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


    public void listTransactions(final String date1, final String date2, final String postman) {

        String tag_string_req = "req_list_expenses";
        pDialog.setMessage("Загрузка данных...");
        showDialog();

        expenseList.clear();
        listViewExpenses.setAdapter(null);

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
                            double totalAmount = 0;
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject c = response.getJSONObject(i);
                                Expense ex = new Expense();
                                ex.exAmount = c.getString("amount");
                                ex.exDate = c.getString("expenseDate").substring(0, 10);
                                ex.exExplanation = c.getString("postman");
                                ex.exId = c.getString("transactionId");
                                ex.exName = c.getString("enteredUser");
                                expenseList.add(ex);
                                totalAmount = totalAmount + Double.parseDouble(ex.exAmount);
                            }
                            if (expenseList.size() > 0) {
                                ExpenseListAdapter expenseListAdapter = new ExpenseListAdapter(expenseList, Transaction.this);
                                listViewExpenses.setAdapter(expenseListAdapter);
                                ed_total.setText("" + totalAmount);
                            }
                        } else {
                            Toast.makeText(Transaction.this,  getApplicationContext().getString(R.string.NoData), Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(Transaction.this,  getApplicationContext().getString(R.string.ErrorWhenLoading), Toast.LENGTH_LONG).show();
                    }
                }, error -> {
                    hideDialog();
                    Log.e(TAG, "Expenses listing Error: " + error.getMessage());
                    Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
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


    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }


    protected Dialog onCreateDialog(int id) {

        if (id == DIALOG_ID1)
            return new DatePickerDialog(this, datePickerListener1, year_x, month_x, day_x);
        else if (id == DIALOG_ID2)
            return new DatePickerDialog(this, datePickerListener2, year_x, month_x, day_x);
        else
            return null;
    }

    protected DatePickerDialog.OnDateSetListener datePickerListener1
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
            ed_Date1.setText(dateS);

        }
    };

    protected DatePickerDialog.OnDateSetListener datePickerListener2
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
            ed_Date2.setText(dateS);

        }
    };

    private int getIndex(Spinner spinner, String myString) {

        int index = 0;

        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).equals(myString)) {
                index = i;
            }
        }
        return index;
    }
}
