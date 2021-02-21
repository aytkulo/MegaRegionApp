package com.kg.megaregionapp.expense;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.kg.bar.HomeActivity;
import com.kg.bar.R;
import com.kg.bar.app.AppConfig;
import com.kg.bar.app.AppController;
import com.kg.bar.helper.HelperConstants;
import com.kg.bar.helper.PostHelper;

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

public class TransactionOperation extends AppCompatActivity {

    private static final String TAG = com.kg.bar.expense.ExpenseHelper.class.getSimpleName();
    public static int DIALOG_ID = 0;
    private EditText exp_Amount, exp_Date, exp_Id;
    private Button btn_expSave;
    private ProgressDialog pDialog;
    private List<com.kg.bar.expense.Expense> expenseList = new ArrayList<>();
    private int year_x, month_x, day_x;
    private Calendar calendar;
    private com.kg.bar.expense.Expense expenseData;
    private Spinner spn_postmans;
    String operationType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_operation);

        exp_Amount = findViewById(R.id.exAmount);
        exp_Date = findViewById(R.id.ed_Date);
        exp_Id = findViewById(R.id.exId);
        btn_expSave = findViewById(R.id.btn_exSave);
        spn_postmans = findViewById(R.id.spinner_users);

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");//dd/MM/yyyy
        Date now = new Date();
        String strDate = sdfDate.format(now);
        exp_Date.setText(strDate);
        calendar = Calendar.getInstance();
        year_x = calendar.get(Calendar.YEAR);
        month_x = calendar.get(Calendar.MONTH);
        day_x = calendar.get(Calendar.DAY_OF_MONTH);

        try {
            PostHelper.listPostmans(HomeActivity.userCity, com.kg.bar.expense.TransactionOperation.this, spn_postmans);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Intent espenseIntent = getIntent();
        Bundle extras = espenseIntent.getExtras();

        if (extras != null) {

            expenseData = (com.kg.bar.expense.Expense) espenseIntent.getSerializableExtra("expense");
            operationType = extras.getString(HelperConstants.EXPENSE_OPERATION);

            if (operationType.equalsIgnoreCase(HelperConstants.EXPENSE_UPDATE)) {
                expenseData = (com.kg.bar.expense.Expense) espenseIntent.getSerializableExtra("expense");
                exp_Amount.setText(expenseData.exAmount);
                exp_Date.setText(expenseData.exDate);
                exp_Id.setText(expenseData.exId);

            }
        }

        exp_Date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(DIALOG_ID);
            }
        });

        btn_expSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (operationType.equalsIgnoreCase(HelperConstants.EXPENSE_NEW)) {
                    showDialog();
                    saveTransaction(spn_postmans.getSelectedItem().toString(), exp_Amount.getText().toString(), exp_Date.getText().toString());
                    Toast.makeText(getApplicationContext(),
                            "Транзакция киргизилди!", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent();
                    setResult(RESULT_OK, intent);
                    finish();
                } else if (operationType.equalsIgnoreCase(HelperConstants.EXPENSE_UPDATE)) {
                    updateTransaction(spn_postmans.getSelectedItem().toString(), exp_Amount.getText().toString(), exp_Date.getText().toString(), exp_Id.getText().toString());
                    Toast.makeText(getApplicationContext(),
                            "Транзакция жаңыланды!", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent();
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });
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


    public void saveTransaction(final String postman, final String amount, final String expenseDate) {
        // Tag used to cancel the request
        String tag_string_req = "req_save_transaction";

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("enteredUser", HomeActivity.userLogin);
            jsonObject.put("postman", postman);
            jsonObject.put("amount", amount);
            jsonObject.put("expenseDate", expenseDate);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest req = new JsonObjectRequest(AppConfig.URL_TRANSACTION_SAVE, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        hideDialog();
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                hideDialog();
                Log.e(TAG, "Expense Saving Error: " + error.getMessage());
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


    public void updateTransaction(final String postman, final String amount, final String expenseDate, final String id) {
        // Tag used to cancel the request
        String tag_string_req = "req_update_transaction";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("enteredUser", HomeActivity.userLogin);
            jsonObject.put("postman", postman);
            jsonObject.put("amount", amount);
            jsonObject.put("expenseDate", expenseDate);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest req = new JsonObjectRequest(AppConfig.URL_TRANSACTION_UPDATE, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        hideDialog();
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Expense Updating Error: " + error.getMessage());
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
            exp_Date.setText(dateS);
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
