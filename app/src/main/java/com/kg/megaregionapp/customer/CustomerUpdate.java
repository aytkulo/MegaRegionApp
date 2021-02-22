package com.kg.megaregionapp.customer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.kg.megaregionapp.R;
import com.kg.megaregionapp.helper.HelperConstants;
import com.kg.megaregionapp.helper.SessionManager;
import com.kg.megaregionapp.helper.StringData;

import org.json.JSONException;

public class CustomerUpdate extends AppCompatActivity {

    private Customer customer;
    private EditText cAddress, cName, cPhone, cCompany, cId;
    private Button btnSave;
    private Button btnDelete;
    private Spinner cCity;
    private String operationType = "";
    private String token = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_update);

        cCity = findViewById(R.id.spinner_custCity);
        cAddress = findViewById(R.id.custAddress);
        cName = findViewById(R.id.custName);
        cPhone = findViewById(R.id.custPhone);
        cCompany = findViewById(R.id.custCompany);
        cId = findViewById(R.id.customer_id);
        btnSave = findViewById(R.id.btn_save_customer_info);
        btnDelete = findViewById(R.id.btn_delete_customer);


        ArrayAdapter<String> cityAdapter = new ArrayAdapter<String>(
                CustomerUpdate.this,
                android.R.layout.simple_spinner_dropdown_item,
                StringData.getCityList()
        );

        SessionManager session = new SessionManager(getApplicationContext());
        token = session.getToken();

        cCity.setAdapter(cityAdapter);

        Intent orderIntent = getIntent();
        Bundle extras = orderIntent.getExtras();

        if (extras != null) {

            operationType = extras.getString(HelperConstants.CUSTOMER_OPERATION);

            if (operationType.equalsIgnoreCase(HelperConstants.UPDATE_CORP_CUSTOMER)) {
                customer = (Customer) orderIntent.getSerializableExtra("customer");
                cId.setText(customer.cus_id);
                cAddress.setText(customer.cus_Address);
                cPhone.setText(customer.cus_Phone);
                cCompany.setText(customer.cus_Company);
                cCity.setSelection(getIndex(cCity, customer.cus_City));
                cName.setEnabled(false);
            } else if (operationType.equalsIgnoreCase(HelperConstants.UPDATE_NORM_CUSTOMER)) {
                customer = (Customer) orderIntent.getSerializableExtra("customer");
                cId.setText(customer.cus_id);
                cAddress.setText(customer.cus_Address);
                cPhone.setText(customer.cus_Phone);
                cCompany.setText(customer.cus_Company);
                cCity.setSelection(getIndex(cCity, customer.cus_City));
                cName.setText(customer.cus_Name);
            } else if (operationType.equalsIgnoreCase(HelperConstants.NEW_CORP_CUSTOMER)) {
                cName.setEnabled(false);
            }
        }

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    if (operationType.equalsIgnoreCase(HelperConstants.UPDATE_CORP_CUSTOMER) && cId.getText().toString().length() > 0) {

                        CustomerHelper.updateCorporateCustomer(cPhone.getText().toString(), cCompany.getText().toString(),
                                cCity.getSelectedItem().toString(), cAddress.getText().toString(), cId.getText().toString(), token);

                    } else if (operationType.equalsIgnoreCase(HelperConstants.UPDATE_NORM_CUSTOMER) && cId.getText().toString().length() > 0) {
                        {
                            CustomerHelper.updateCustomer(cName.getText().toString(), cPhone.getText().toString(), cCompany.getText().toString(),
                                    cCity.getSelectedItem().toString(), cAddress.getText().toString(), cId.getText().toString(), token);
                        }
                    } else if (operationType.equalsIgnoreCase(HelperConstants.NEW_CORP_CUSTOMER)) {
                        CustomerHelper.saveCorporateCustomer(cPhone.getText().toString(), cCompany.getText().toString(),
                                cCity.getSelectedItem().toString(), cAddress.getText().toString(), token);
                    } else
                        CustomerHelper.saveCustomer(cName.getText().toString(), cPhone.getText().toString(), cCompany.getText().toString(),
                                cCity.getSelectedItem().toString(), cAddress.getText().toString(), token);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cId.getText().toString().length() > 0) {
                    try {
                        CustomerHelper.deleteCustomer(cId.getText().toString(), token);
                        Intent intent = new Intent();
                        setResult(RESULT_OK, intent);
                        finish();
                    } catch (JSONException e) {
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
}
