package com.kg.megaregionapp.expense;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
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
import com.kg.bar.helper.StringData;
import com.kg.bar.utils.MyDialog;
import com.kg.bar.utils.NetworkUtil;

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

public class ExpenseOperation extends AppCompatActivity {

    private static final String TAG = com.kg.bar.expense.ExpenseHelper.class.getSimpleName();
    public static int DIALOG_ID = 0;
    private EditText exp_Expl, exp_Amount, exp_Date, exp_Id;
    private Button btn_expSave;
    private Spinner spn_exp_City;
    private ListView listViewExpenses;
    private ProgressDialog pDialog;
    private List<com.kg.bar.expense.Expense> expenseList = new ArrayList<>();
    private int year_x, month_x, day_x;
    private Calendar calendar;
    String operationType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_operation);

        exp_Expl = (EditText) findViewById(R.id.exExplanation);
        exp_Amount = (EditText) findViewById(R.id.exAmount);
        exp_Date = (EditText) findViewById(R.id.ed_Date);
        exp_Id = (EditText) findViewById(R.id.exId);
        btn_expSave = (Button) findViewById(R.id.btn_exSave);
        listViewExpenses = (ListView) findViewById(R.id.listViewExpenses);
        spn_exp_City = (Spinner) findViewById(R.id.spinner_exCity);

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




        ArrayAdapter<String> cityAdapter = new ArrayAdapter<String>(
                com.kg.bar.expense.ExpenseOperation.this,
                android.R.layout.simple_spinner_dropdown_item,
                StringData.getCityList()
        );

        spn_exp_City.setAdapter(cityAdapter);
        spn_exp_City.setSelection(getIndex(spn_exp_City, HomeActivity.userCity));
        spn_exp_City.setEnabled(false);


        Intent espenseIntent = getIntent();
        Bundle extras = espenseIntent.getExtras();
        if (extras != null) {

            operationType = extras.getString(HelperConstants.EXPENSE_OPERATION);
            if (operationType.equalsIgnoreCase(HelperConstants.EXPENSE_UPDATE)) {
                com.kg.bar.expense.Expense expenseData = (com.kg.bar.expense.Expense) espenseIntent.getSerializableExtra("expense");
                exp_Amount.setText(expenseData.exAmount);
                exp_Expl.setText(expenseData.exExplanation);
                exp_Date.setText(expenseData.exDate);
                exp_Id.setText(expenseData.exId);

                spn_exp_City.setSelection(getIndex(spn_exp_City, expenseData.exCity));
                spn_exp_City.setEnabled(false);

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
                    try {
                        saveExpense(spn_exp_City.getSelectedItem().toString(), exp_Amount.getText().toString(), HomeActivity.userLogin,
                                exp_Date.getText().toString(), exp_Expl.getText().toString());

                        exp_Amount.setText("");
                        exp_Expl.setText("");
                        Toast.makeText(getApplicationContext(),
                                "Каражат киргизилди!", Toast.LENGTH_LONG).show();

                        Intent intent = new Intent();
                        setResult(RESULT_OK, intent);
                        finish();

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                } else if (operationType.equalsIgnoreCase(HelperConstants.EXPENSE_UPDATE)) {

                    try {
                        updateExpense(spn_exp_City.getSelectedItem().toString(), exp_Amount.getText().toString(), exp_Date.getText().toString(), exp_Expl.getText().toString(), exp_Id.getText().toString());

                        exp_Amount.setText("");
                        exp_Expl.setText("");
                        Toast.makeText(getApplicationContext(),
                                "Каражат жаңыланды!", Toast.LENGTH_LONG).show();

                        Intent intent = new Intent();
                        setResult(RESULT_OK, intent);
                        finish();

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
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

    public void saveExpense(final String city, final String amount, final String user, final String expenseDate, final String explanation) throws ParseException {

        if (!NetworkUtil.isNetworkConnected(com.kg.bar.expense.ExpenseOperation.this)) {
            MyDialog.createSimpleOkErrorDialog(com.kg.bar.expense.ExpenseOperation.this,
                    getApplicationContext().getString(R.string.dialog_error_title),
                    getApplicationContext().getString(R.string.check_internet)).show();
        } else if (NetworkUtil.isTokenExpired()) {
            MyDialog.createSimpleOkErrorDialog(com.kg.bar.expense.ExpenseOperation.this,
                    getApplicationContext().getString(R.string.dialog_error_title),
                    getApplicationContext().getString(R.string.relogin)).show();
        } else {// Tag used to cancel the request
            String tag_string_req = "req_save_expense";
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("enteredUser", user);
                jsonObject.put("expenseDate", expenseDate);
                jsonObject.put("explanation", explanation);
                jsonObject.put("city", city);
                jsonObject.put("amount", amount);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            JsonObjectRequest req = new JsonObjectRequest(AppConfig.URL_EXPENSE_SAVE, jsonObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            hideDialog();
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    NetworkUtil.checkHttpStatus(com.kg.bar.expense.ExpenseOperation.this, error);
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


    public void updateExpense(final String city, final String amount, final String expenseDate, final String explanation, final String id) throws ParseException {

            if (!NetworkUtil.isNetworkConnected(com.kg.bar.expense.ExpenseOperation.this)) {
                MyDialog.createSimpleOkErrorDialog(com.kg.bar.expense.ExpenseOperation.this,
                        getApplicationContext().getString(R.string.dialog_error_title),
                        getApplicationContext().getString(R.string.check_internet)).show();
            } else if (NetworkUtil.isTokenExpired()) {
                MyDialog.createSimpleOkErrorDialog(com.kg.bar.expense.ExpenseOperation.this,
                        getApplicationContext().getString(R.string.dialog_error_title),
                        getApplicationContext().getString(R.string.relogin)).show();
            } else {
                // Tag used to cancel the request
            String tag_string_req = "req_save_expense";
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("enteredUser", HomeActivity.userLogin);
                jsonObject.put("expenseDate", expenseDate);
                jsonObject.put("explanation", explanation);
                jsonObject.put("city", city);
                jsonObject.put("amount", amount);
                jsonObject.put("expenseId", id);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JsonObjectRequest req = new JsonObjectRequest(AppConfig.URL_EXPENSE_UPDATE, jsonObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            hideDialog();
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
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

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
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
}
