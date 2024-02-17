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

public class PostmanCheckActivity extends AppCompatActivity {

     public static int DIALOG_ID = 0;
    private EditText ed_Date;
    private ListView postmanListView;
    private int year_x, month_x, day_x;
    private Calendar calendar;
    private ProgressDialog pDialog;
    private List<Account> collectionList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_postman_check);

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(true);

        ed_Date =  findViewById(R.id.ed_Date_Collection);
        postmanListView =  findViewById(R.id.listViewCollections);

        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");//dd/MM/yyyy
        Date now = new Date();
        final String strDate = sdfDate.format(now);

        ed_Date.setText(strDate);

        calendar = Calendar.getInstance();
        year_x = calendar.get(Calendar.YEAR);
        month_x = calendar.get(Calendar.MONTH);
        day_x = calendar.get(Calendar.DAY_OF_MONTH);

        ed_Date.setOnClickListener(v -> showDialog(DIALOG_ID));

        try {
            listExpenses();
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    public void listExpenses() throws ParseException {

        if (!NetworkUtil.isNetworkConnected(PostmanCheckActivity.this)) {
            MyDialog.createSimpleOkErrorDialog(PostmanCheckActivity.this,
                    getApplicationContext().getString(R.string.dialog_error_title),
                    getApplicationContext().getString(R.string.check_internet)).show();
        } else if (NetworkUtil.isTokenExpired()) {
            MyDialog.createSimpleOkErrorDialog(PostmanCheckActivity.this,
                    getApplicationContext().getString(R.string.dialog_error_title),
                    getApplicationContext().getString(R.string.relogin)).show();
        } else {
            String tag_string_req = "req_list_collections";
            pDialog.setMessage("Listing postman ...");
            showDialog();

            collectionList.clear();
            postmanListView.setAdapter(null);

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("expenseDate", ed_Date.getText());
                jsonObject.put("city", HomeActivity.userCity);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            CustomJsonArrayRequest req = new CustomJsonArrayRequest(Request.Method.POST, AppConfig.URL_EXPENSE_LIST_POSTMAN_REP, jsonObject,
                    response -> {
                        hideDialog();
                        try {
                            if (response.length() > 0) {

                                for (int i = 0; i < response.length(); i++) {
                                    JSONObject c = response.getJSONObject(i);
                                    Account ex = new Account();
                                    ex.amount = Long.valueOf(c.getString("total"));
                                    ex.postman = c.getString("postman");
                                    collectionList.add(ex);
                                }
                                if (collectionList.size() > 0) {
                                    PostmanCheckListAdapter collListAdapter = new PostmanCheckListAdapter(collectionList, PostmanCheckActivity.this);
                                    postmanListView.setAdapter(collListAdapter);
                                }
                            }

                        } catch (JSONException e) {
                            MyDialog.createSimpleOkErrorDialog(PostmanCheckActivity.this,
                                    getApplicationContext().getString(R.string.dialog_error_title),
                                    getApplicationContext().getString(R.string.ErrorWhenLoading)).show();
                        }

                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    NetworkUtil.checkHttpStatus(PostmanCheckActivity.this, error);
                    hideDialog();
                }
            }) {

                @Override
                public Map<String, String> getHeaders() {
                    HashMap<String, String> headers = new HashMap<>();
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