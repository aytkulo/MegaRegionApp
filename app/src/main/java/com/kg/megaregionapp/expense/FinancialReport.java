package com.kg.megaregionapp.expense;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.kg.bar.HomeActivity;
import com.kg.bar.R;
import com.kg.bar.app.AppConfig;
import com.kg.bar.app.AppController;
import com.kg.bar.customer.Customer;
import com.kg.bar.delivery.Delivery;
import com.kg.bar.helper.CustomJsonArrayRequest;
import com.kg.bar.helper.StringData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.UnderlineStyle;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

public class FinancialReport extends AppCompatActivity {
    private static final String TAG = com.kg.bar.expense.FinancialReport.class.getSimpleName();
    public static int DIALOG_ID = 1;
    public static int DIALOG_ID_2 = 2;
    Button btn_fin_report;
    Button btn_expense_report;
    Button btn_bank_paid_list;
    Button btn_generate_all_list;
    Button btn_generate_bought_list;
    Button btn_generate_proforma;
    EditText ed_file_location;
    EditText ed_Date1, ed_Date2;
    Calendar calendar;
    List<com.kg.bar.expense.Expense> expList = new ArrayList<>();
    List<com.kg.bar.expense.Finance> finList = new ArrayList<>();
    List<com.kg.bar.expense.BankPaidDelivery> delList = new ArrayList<>();
    List<com.kg.bar.expense.Boughts> boughtList = new ArrayList<>();
    List<com.kg.bar.expense.VDelivery> deliveryList = new ArrayList<>();
    List<Delivery> deliveryActList = new ArrayList<>();
    int year_x, month_x, day_x;
    private ProgressDialog pDialog;
    private Spinner sCity, rCity;
    private List<Customer> customerList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_financial_report);

        btn_fin_report = findViewById(R.id.btn_generate_report);
        btn_bank_paid_list = findViewById(R.id.btn_generate_delivery_list);
        btn_generate_all_list = findViewById(R.id.btn_generate_all_list);
        btn_generate_bought_list = findViewById(R.id.btn_generate_bought_list);
        btn_expense_report = findViewById(R.id.btn_expense_report);
        btn_generate_proforma = findViewById(R.id.btn_proforma);
        ed_file_location = findViewById(R.id.ed_file_location);
        ed_Date1 = findViewById(R.id.ed_date1);
        ed_Date2 = findViewById(R.id.ed_date2);
        sCity = findViewById(R.id.spinner_senderCity);
        rCity = findViewById(R.id.spinner_receiverCity);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        calendar = Calendar.getInstance();
        year_x = calendar.get(Calendar.YEAR);
        month_x = calendar.get(Calendar.MONTH) + 1;
        day_x = calendar.getActualMinimum(Calendar.DAY_OF_MONTH);

        String dateS = String.valueOf(year_x);
        dateS = getDateInFormat(dateS, month_x, day_x);
        ed_Date1.setText(dateS);

        day_x = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        dateS = String.valueOf(year_x);
        dateS = getDateInFormat(dateS, month_x, day_x);
        ed_Date2.setText(dateS);

        ed_Date1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(DIALOG_ID);
            }
        });

        ed_Date2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(DIALOG_ID_2);
            }
        });

        rCity.setBackgroundColor(Color.GRAY);
        sCity.setBackgroundColor(Color.GRAY);

        ArrayAdapter<String> cityAdapter = new ArrayAdapter<String>(
                com.kg.bar.expense.FinancialReport.this,
                android.R.layout.simple_spinner_dropdown_item,
                StringData.getCityList()
        );
        rCity.setAdapter(cityAdapter);
        sCity.setAdapter(cityAdapter);

        btn_generate_proforma.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                listDeliveriesForProforma(ed_Date1.getText().toString(), ed_Date2.getText().toString());
            }
        });

        //OK - Teksherildi...
        btn_fin_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                listFinancialReport(ed_Date1.getText().toString(), ed_Date2.getText().toString());
            }
        });

        // OK - Teksherildi...
        btn_expense_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                listExpenses(ed_Date1.getText().toString(), ed_Date2.getText().toString());
            }
        });

        // OK - Teksherildi...
        btn_bank_paid_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                listBankPayingDeliveries(ed_Date1.getText().toString(), ed_Date2.getText().toString());
            }
        });
        // OK - Teksherildi...
        btn_generate_all_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean processS = true;
                boolean processR = true;

                if (sCity.getSelectedItem().toString().length() < 1) {
                    sCity.setBackground(getShape(Color.MAGENTA));
                    processS = false;
                } else {
                    sCity.setBackgroundColor(Color.GRAY);
                }

                if (rCity.getSelectedItem().toString().length() < 1) {
                    rCity.setBackground(getShape(Color.MAGENTA));
                    processR = false;
                } else {
                    rCity.setBackgroundColor(Color.GRAY);
                }

                if (processS && processR)
                    listAllDeliveries(ed_Date1.getText().toString(), ed_Date2.getText().toString(), sCity.getSelectedItem().toString(), rCity.getSelectedItem().toString());
            }
        });

        // OK - Teksherildi...
        btn_generate_bought_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                listBoughtDeliveries(ed_Date1.getText().toString(), ed_Date2.getText().toString());
            }
        });

    }

    private ShapeDrawable getShape(int color) {

        ShapeDrawable shape = new ShapeDrawable(new RectShape());
        shape.getPaint().setColor(color);
        shape.getPaint().setStyle(Paint.Style.STROKE);
        shape.getPaint().setStrokeWidth(3);

        return shape;
    }

    protected Dialog onCreateDialog(int id) {

        if (id == DIALOG_ID)
            return new DatePickerDialog(this, datePickerListener, year_x, month_x, day_x);
        else if (id == DIALOG_ID_2)
            return new DatePickerDialog(this, datePickerListener2, year_x, month_x, day_x);
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
            dateS = getDateInFormat(dateS, month_x, day_x);
            ed_Date1.setText(dateS);
        }
    };

    protected String getDateInFormat(String date, int month, int day) {

        if (month_x < 10)
            date = date + "-0" + month_x;
        else
            date = date + "-" + month_x;
        if (day_x < 10)
            date = date + "-0" + day_x;
        else
            date = date + "-" + day_x;
        return date;
    }

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


    public void listDeliveriesForProforma(final String beginDate, final String endDate) {

        String tag_string_req = "req_list_deliveries_for_report";
        deliveryList.clear();
        pDialog.setMessage("Generating ...");
        showDialog();

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("beginDate", beginDate);
            jsonObject.put("endDate", endDate);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        CustomJsonArrayRequest req = new CustomJsonArrayRequest(Request.Method.POST, AppConfig.URL_GET_CUSTOMER_DELIVERIES, jsonObject,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d(TAG, "List All Deliveries for report: " + response);
                        hideDialog();
                        try {
                            if (response.length() > 0) {
                                for (int i = 0; i < response.length(); i++) {
                                    JSONObject c = response.getJSONObject(i);
                                    Delivery vd = new Delivery();
                                    vd.entryDate = c.getString("entryDate");
                                    vd.sFullName = c.getString("senderName");
                                    vd.senderPhone = c.getString("senderPhone");
                                    vd.rFullName = c.getString("receiverName");
                                    vd.receiverPhone = c.getString("receiverPhone");
                                    vd.senderCity = c.getString("senderCity");
                                    vd.receiverCity = c.getString("receiverCity");
                                    vd.deliveryCount = c.getString("deliveryCount");
                                    vd.deliveryType = c.getString("deliveryType");
                                    vd.senderCompany = c.getString("company");
                                    vd.receiverCompany = c.getString("city");
                                    vd.deliveryCost = c.getString("deliveryCost");
                                    deliveryActList.add(vd);
                                }
                                try {
                                    generateActExcel(deliveryActList);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } catch (RowsExceededException e) {
                                    hideDialog();
                                    e.printStackTrace();
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), "Эч нерсе табылбады, же бир ката пайда болду.", Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "General error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                hideDialog();
                Log.e(TAG, "Delivery listing error for proforma: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
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


    private void generateActExcel(List<Delivery> deliveryList) throws IOException, RowsExceededException {

        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");//dd/MM/yyyy
        Date now = new Date();
        String strDate = sdfDate.format(now);

        File sdCard = Environment.getExternalStorageDirectory();
        File directory = new File(sdCard.getAbsolutePath() + "/YLDAMREPORT");
        directory.mkdirs();
        File fileAct = new File(directory, "YldamAct.xls");
        File fileInvoice = new File(directory, "YldamInvoice.xls");

        WorkbookSettings wbSettings = new WorkbookSettings();
        wbSettings.setLocale(new Locale("en", "EN"));
        WritableWorkbook workbookAct;
        WritableWorkbook workbookInvoice;

        try {
            workbookAct = Workbook.createWorkbook(fileAct, wbSettings);
            workbookInvoice = Workbook.createWorkbook(fileInvoice, wbSettings);

            WritableSheet sheetAct = null;
            WritableSheet sheetInvoice = null;

            Label label;
            Number number;

            SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM, yyyy");
            Date date = new Date();
            String invoiceDate = formatter.format(date);

            try {
                int row = 2;
                int rowInv = 0;
                int count = 0;
                int sheetNo = 0;
                int sheetNoInvoice = 0;

                Delivery ff;
                String prevCity = "";
                String prevCompany = "";
                String destination = "";
                int total = 0;

                WritableCellFormat headerCenterCellFormat = getHeaderCenterCellFormat();
                WritableCellFormat headerLeftCellFormat = getHeaderLeftCellFormat();
                WritableCellFormat headerRightCellFormat = getHeaderRightCellFormat();
                WritableCellFormat normalLeftCellFormat = getNormalUnderlineLeftCellFormat();

                WritableCellFormat normalRightCellFormat = getNormalRightCellFormat();
                WritableCellFormat underlineCellFormat = getUnderlineCellFormat();

                WritableCellFormat normalCellFormat = getNormalLeftCellFormat();

                WritableCellFormat normalLeftNoBordersCellFormat = getNormalLeftNoBordersCellFormat();


                for (int k = 0; k < deliveryList.size(); k++) {

                    ff = deliveryList.get(k);

                    if (!ff.receiverCompany.equalsIgnoreCase(prevCity) || !ff.senderCompany.equalsIgnoreCase(prevCompany)) {

                        if (k > 0) {

                            sheetAct.mergeCells(0, row, 2, row);
                            label = new Label(0, row, "Итого:", headerLeftCellFormat);
                            sheetAct.addCell(label);

                            number = new Number(3, row, total, headerRightCellFormat);
                            sheetAct.addCell(number);

                            row = row + 3;

                            sheetAct.mergeCells(0, row, 3, row);
                            label = new Label(0, row, "ОсОО \"Ылдам-Экспресс\"", headerCenterCellFormat);
                            sheetAct.addCell(label);

                            sheetAct.mergeCells(4, row, 7, row);
                            label = new Label(4, row, prevCompany, headerCenterCellFormat);
                            sheetAct.addCell(label);

                            row += 2;


                            label = new Label(1, row, "", underlineCellFormat);
                            sheetAct.addCell(label);
                            label = new Label(2, row, "", underlineCellFormat);
                            sheetAct.addCell(label);
                            label = new Label(5, row, "", underlineCellFormat);
                            sheetAct.addCell(label);
                            label = new Label(6, row, "", underlineCellFormat);
                            sheetAct.addCell(label);


                            // Invoice
                            for (int c = 0; c < 25; c += 23) {
                                label = new Label(0, c + 11, "1", normalLeftCellFormat);
                                sheetInvoice.addCell(label);
                                label = new Label(1, c + 11, "За услугу доставки посылок по регионам.", normalLeftCellFormat);
                                sheetInvoice.addCell(label);
                                label = new Label(2, c + 11, String.valueOf(count), normalLeftCellFormat);
                                sheetInvoice.addCell(label);
                                label = new Label(4, c + 11, String.valueOf(total), normalLeftCellFormat);
                                sheetInvoice.addCell(label);

                                label = new Label(1, c + 13, "Итого: ", normalLeftCellFormat);
                                sheetInvoice.addCell(label);
                                label = new Label(4, c + 13, String.valueOf(total), normalLeftCellFormat);
                                sheetInvoice.addCell(label);

                                sheetInvoice.mergeCells(0, c + 15, 6, c + 15);
                                label = new Label(0, c + 15, "Сумма прописью: " + com.kg.bar.expense.ExpenseHelper.number2string(total), normalCellFormat);
                                sheetInvoice.addCell(label);

                                label = new Label(0, c + 17, "Руководитель: ", normalCellFormat);
                                sheetInvoice.addCell(label);
                                label = new Label(0, c + 19, "Гл. бухгалтер: ", normalCellFormat);
                                sheetInvoice.addCell(label);

                                label = new Label(3, c + 17, "Получил: ", normalCellFormat);
                                sheetInvoice.addCell(label);
                                label = new Label(3, c + 19, "Отпустил : ", normalCellFormat);
                                sheetInvoice.addCell(label);
                            }
                            count = 0;
                        }

                        total = 0;
                        sheetAct = workbookAct.createSheet(ff.senderCompany + "-" + ff.receiverCompany, sheetNo);
                        sheetAct.setColumnView(0, 10);
                        sheetAct.setColumnView(1, 12);
                        sheetAct.setColumnView(2, 14);
                        sheetAct.setColumnView(3, 7);
                        sheetAct.setColumnView(4, 13);
                        sheetAct.setColumnView(5, 11);
                        sheetAct.setColumnView(6, 13);
                        sheetAct.setColumnView(7, 11);
                        sheetNo++;


                        sheetAct.mergeCells(0, 0, 7, 0);
                        label = new Label(0, 0, "АКТ выполненных работ между", headerCenterCellFormat);
                        sheetAct.addCell(label);
                        sheetAct.mergeCells(0, 1, 7, 1);
                        label = new Label(0, 1, "ОсОО \"Ылдам-Экспресс\" и " + ff.senderCompany, headerCenterCellFormat);
                        sheetAct.addCell(label);
                        sheetAct.mergeCells(0, 2, 7, 2);
                        label = new Label(0, 2, "с " + ed_Date1.getText().toString() + " по " + ed_Date2.getText().toString(), headerCenterCellFormat);
                        sheetAct.addCell(label);


                        label = new Label(0, 4, "Дата", headerLeftCellFormat);
                        sheetAct.addCell(label);
                        label = new Label(1, 4, "Направление", headerLeftCellFormat);
                        sheetAct.addCell(label);
                        label = new Label(2, 4, "Предмет", headerLeftCellFormat);
                        sheetAct.addCell(label);

                        label = new Label(3, 4, "Цена", headerRightCellFormat);
                        sheetAct.addCell(label);

                        sheetAct.mergeCells(4, 4, 5, 4);
                        label = new Label(4, 4, "Отправитель", headerLeftCellFormat);
                        sheetAct.addCell(label);

                        sheetAct.mergeCells(6, 4, 7, 4);
                        label = new Label(6, 4, "Получатель", headerLeftCellFormat);
                        sheetAct.addCell(label);

                        row = 5;

                        /// Buradan ashagisi invoice....

                        sheetInvoice = workbookInvoice.createSheet(ff.senderCompany + "-" + ff.receiverCompany, sheetNoInvoice);
                        sheetInvoice.setColumnView(0, 6);
                        sheetInvoice.setColumnView(1, 30);
                        sheetInvoice.setColumnView(2, 6);
                        sheetInvoice.setColumnView(3, 10);
                        sheetInvoice.setColumnView(4, 10);
                        sheetInvoice.setColumnView(5, 10);
                        sheetInvoice.setColumnView(6, 10);
                        sheetNoInvoice++;

                        for (int c = 0; c < 25; c += 23) {
                            sheetInvoice.mergeCells(0, c, 6, c);
                            label = new Label(0, c, "Счет фактура №", headerCenterCellFormat);
                            sheetInvoice.addCell(label);
                            sheetInvoice.mergeCells(0, c + 1, 6, c + 1);
                            label = new Label(0, c + 1, "Дата: " + invoiceDate, headerCenterCellFormat);
                            sheetInvoice.addCell(label);

                            sheetInvoice.mergeCells(0, c + 3, 6, c + 3);
                            label = new Label(0, c + 3, "Наименование организации: ОсОО «Ылдам-Экспресс»", normalLeftNoBordersCellFormat);
                            sheetInvoice.addCell(label);
                            sheetInvoice.mergeCells(0, c + 4, 6, c + 4);
                            label = new Label(0, c + 4, "г.Жалал-Абад мкр.Спутник пр. Манас 2-1, ИНН: 02711201410042", normalLeftNoBordersCellFormat);
                            sheetInvoice.addCell(label);
                            sheetInvoice.mergeCells(0, c + 5, 6, c + 5);
                            label = new Label(0, c + 5, "Номер расчетного счета:1212000200081318", normalLeftNoBordersCellFormat);
                            sheetInvoice.addCell(label);
                            sheetInvoice.mergeCells(0, c + 6, 6, c + 6);
                            label = new Label(0, c + 6, "Банк: ЖАФ ОАО «Дос Кредо Банк», БИК 121002", normalLeftNoBordersCellFormat);
                            sheetInvoice.addCell(label);
                            sheetInvoice.mergeCells(0, c + 8, 6, c + 8);
                            label = new Label(0, c + 8, "Кому: " + ff.senderCompany, normalLeftNoBordersCellFormat);
                            sheetInvoice.addCell(label);

                            label = new Label(0, c + 10, "№", headerLeftCellFormat);
                            sheetInvoice.addCell(label);
                            label = new Label(1, c + 10, "Наименование", headerLeftCellFormat);
                            sheetInvoice.addCell(label);
                            label = new Label(2, c + 10, "Кол-во", headerLeftCellFormat);
                            sheetInvoice.addCell(label);
                            label = new Label(3, c + 10, "Цена за ед.", headerLeftCellFormat);
                            sheetInvoice.addCell(label);
                            label = new Label(4, c + 10, "Стоимость без НДС", headerLeftCellFormat);
                            sheetInvoice.addCell(label);
                            label = new Label(5, c + 10, "Ставка НДС", headerLeftCellFormat);
                            sheetInvoice.addCell(label);
                            label = new Label(6, c + 10, "Сумма НДС", headerLeftCellFormat);
                            sheetInvoice.addCell(label);

                            for (int v = 11; v < 14; v++) {
                                for (int w = 0; w < 7; w++) {
                                    label = new Label(w, c + v, "", normalLeftCellFormat);
                                    sheetInvoice.addCell(label);
                                }
                            }
                        }
                        // Invoice sonu
                    }

                    label = new Label(0, row, ff.entryDate, normalLeftCellFormat);
                    sheetAct.addCell(label);

                    if (ff.senderCity.length() > 3)
                        destination = ff.senderCity.substring(0, 3);
                    else
                        destination = ff.senderCity;

                    if (ff.receiverCity.length() > 3)
                        destination = destination + "-" + ff.receiverCity.substring(0, 3);
                    else
                        destination = destination + "-" + ff.receiverCity;

                    label = new Label(1, row, destination, normalLeftCellFormat);
                    sheetAct.addCell(label);

                    label = new Label(2, row, ff.deliveryCount + " " + ff.deliveryType, normalLeftCellFormat);
                    sheetAct.addCell(label);
                    number = new Number(3, row, Integer.parseInt(ff.deliveryCost), normalRightCellFormat);
                    sheetAct.addCell(number);
                    label = new Label(4, row, ff.sFullName, normalLeftCellFormat);
                    sheetAct.addCell(label);
                    label = new Label(5, row, ff.senderPhone, normalLeftCellFormat);
                    sheetAct.addCell(label);
                    label = new Label(6, row, ff.rFullName, normalLeftCellFormat);
                    sheetAct.addCell(label);
                    label = new Label(7, row, ff.receiverPhone, normalLeftCellFormat);
                    sheetAct.addCell(label);

                    total += Integer.parseInt(ff.deliveryCost);
                    count++;

                    row++;

                    prevCity = ff.receiverCompany;
                    prevCompany = ff.senderCompany;

                }


                sheetAct.mergeCells(0, row, 2, row);
                label = new Label(0, row, "Итого:", headerLeftCellFormat);
                sheetAct.addCell(label);

                number = new Number(3, row, total, headerRightCellFormat);
                sheetAct.addCell(number);

                row = row + 3;

                sheetAct.mergeCells(0, row, 3, row);
                label = new Label(0, row, "ОсОО \"Ылдам-Экспресс\"", headerCenterCellFormat);
                sheetAct.addCell(label);

                sheetAct.mergeCells(4, row, 7, row);
                label = new Label(4, row, prevCompany, headerCenterCellFormat);
                sheetAct.addCell(label);

                row += 2;
                label = new Label(1, row, "", underlineCellFormat);
                sheetAct.addCell(label);
                label = new Label(2, row, "", underlineCellFormat);
                sheetAct.addCell(label);
                label = new Label(5, row, "", underlineCellFormat);
                sheetAct.addCell(label);
                label = new Label(6, row, "", underlineCellFormat);
                sheetAct.addCell(label);


                // Invoice

                for (int v = 11; v < 35; v += 22) {

                    label = new Label(0, v, "1", normalLeftCellFormat);
                    sheetInvoice.addCell(label);
                    label = new Label(1, v, "За услугу доставки посылок по регионам.", normalLeftCellFormat);
                    sheetInvoice.addCell(label);
                    label = new Label(2, v, String.valueOf(count), normalLeftCellFormat);
                    sheetInvoice.addCell(label);
                    label = new Label(4, v, String.valueOf(total), normalLeftCellFormat);
                    sheetInvoice.addCell(label);

                    label = new Label(1, v + 3, "Итого: ", normalLeftCellFormat);
                    sheetInvoice.addCell(label);
                    label = new Label(4, v + 2, String.valueOf(total), normalLeftCellFormat);
                    sheetInvoice.addCell(label);

                    sheetInvoice.mergeCells(0, v + 4, 6, v + 4);
                    label = new Label(0, v + 4, "Сумма прописью: " + com.kg.bar.expense.ExpenseHelper.number2string(total), normalCellFormat);
                    sheetInvoice.addCell(label);

                    label = new Label(0, v + 6, "Руководитель: ", normalCellFormat);
                    sheetInvoice.addCell(label);
                    label = new Label(0, v + 8, "Гл. бухгалтер: ", normalCellFormat);
                    sheetInvoice.addCell(label);

                    label = new Label(3, v + 6, "Получил: ", normalCellFormat);
                    sheetInvoice.addCell(label);
                    label = new Label(3, v + 8, "Отпустил : ", normalCellFormat);
                    sheetInvoice.addCell(label);
                }


            } catch (RowsExceededException e) {
                throw e;
            } catch (WriteException e) {
                e.printStackTrace();
            }

            workbookAct.write();
            workbookInvoice.write();
            ed_file_location.setText("Акттар жана счет-фактуралар даярдалды!\nЖайгашкан жери:" + directory.toString());
            Toast.makeText(getApplicationContext(), "Акттар жана счет-фактуралар даярдалды.", Toast.LENGTH_LONG).show();
            try {
                workbookAct.close();
                workbookInvoice.close();
            } catch (WriteException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static WritableCellFormat getUnderlineCellFormat() throws WriteException {

        WritableCellFormat headerCellFormat = null;
        WritableFont wf_head = new WritableFont(WritableFont.TIMES, 11, WritableFont.BOLD, false, UnderlineStyle.SINGLE, jxl.format.Colour.BLACK);
        headerCellFormat = new WritableCellFormat(wf_head);
        headerCellFormat.setBorder(Border.BOTTOM, BorderLineStyle.THIN);
        headerCellFormat.setWrap(true);
        headerCellFormat.setAlignment(jxl.format.Alignment.CENTRE);
        return headerCellFormat;
    }


    public static WritableCellFormat getHeaderCenterCellFormat() throws WriteException {

        WritableCellFormat headerCellFormat = null;
        WritableFont wf_head = new WritableFont(WritableFont.TIMES, 11, WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE, jxl.format.Colour.BLACK);
        headerCellFormat = new WritableCellFormat(wf_head);
        headerCellFormat.setAlignment(jxl.format.Alignment.CENTRE);
        return headerCellFormat;
    }


    public static WritableCellFormat getHeaderLeftCellFormat() throws WriteException {

        WritableCellFormat headerCellFormat = null;
        WritableFont wf_head = new WritableFont(WritableFont.TIMES, 10, WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE, jxl.format.Colour.BLACK);
        headerCellFormat = new WritableCellFormat(wf_head);
        headerCellFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
        headerCellFormat.setAlignment(Alignment.LEFT);
        headerCellFormat.setWrap(true);
        return headerCellFormat;
    }

    public static WritableCellFormat getHeaderRightCellFormat() throws WriteException {

        WritableCellFormat headerCellFormat = null;
        WritableFont wf_head = new WritableFont(WritableFont.TIMES, 10, WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE, jxl.format.Colour.BLACK);
        headerCellFormat = new WritableCellFormat(wf_head);
        headerCellFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
        headerCellFormat.setAlignment(Alignment.RIGHT);
        headerCellFormat.setWrap(true);
        return headerCellFormat;
    }


    public static WritableCellFormat getNormalLeftNoBordersCellFormat() throws WriteException {

        WritableCellFormat headerCellFormat = null;
        WritableFont wf_head = new WritableFont(WritableFont.TIMES, 10, WritableFont.NO_BOLD, false, UnderlineStyle.NO_UNDERLINE, jxl.format.Colour.BLACK);
        headerCellFormat = new WritableCellFormat(wf_head);
        headerCellFormat.setWrap(true);
        headerCellFormat.setAlignment(jxl.format.Alignment.LEFT);
        return headerCellFormat;
    }

    public static WritableCellFormat getNormalUnderlineLeftCellFormat() throws WriteException {

        WritableCellFormat headerCellFormat = null;
        WritableFont wf_head = new WritableFont(WritableFont.TIMES, 10, WritableFont.NO_BOLD, false, UnderlineStyle.NO_UNDERLINE, jxl.format.Colour.BLACK);
        headerCellFormat = new WritableCellFormat(wf_head);
        headerCellFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
        headerCellFormat.setWrap(true);
        headerCellFormat.setAlignment(jxl.format.Alignment.LEFT);
        return headerCellFormat;
    }

    public static WritableCellFormat getNormalLeftCellFormat() throws WriteException {

        WritableCellFormat headerCellFormat = null;
        WritableFont wf_head = new WritableFont(WritableFont.TIMES, 10, WritableFont.NO_BOLD, false, UnderlineStyle.NO_UNDERLINE, jxl.format.Colour.BLACK);
        headerCellFormat = new WritableCellFormat(wf_head);
        headerCellFormat.setAlignment(jxl.format.Alignment.LEFT);
        return headerCellFormat;
    }

    public static WritableCellFormat getNormalRightCellFormat() throws WriteException {

        WritableCellFormat headerCellFormat = null;
        WritableFont wf_head = new WritableFont(WritableFont.TIMES, 10, WritableFont.NO_BOLD, false, UnderlineStyle.NO_UNDERLINE, jxl.format.Colour.BLACK);
        headerCellFormat = new WritableCellFormat(wf_head);
        headerCellFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
        headerCellFormat.setWrap(true);
        headerCellFormat.setAlignment(Alignment.RIGHT);
        return headerCellFormat;
    }


    public void listAllDeliveries(final String beginDate, final String endDate, final String sCity, final String rCity) {

        String tag_string_req = "req_list_deliveries_for_report";
        deliveryList.clear();
        pDialog.setMessage("Generating ...");
        showDialog();

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("beginDate", beginDate);
            jsonObject.put("endDate", endDate);
            jsonObject.put("receiverCity", rCity);
            jsonObject.put("senderCity", sCity);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        CustomJsonArrayRequest strReq = new CustomJsonArrayRequest(Request.Method.POST, AppConfig.URL_GET_DELIVERIES_FOR_REPORT, jsonObject,
                new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d(TAG, "List All Deliveries for report: " + response);
                        hideDialog();
                        try {
                            if (response.length() > 0) {
                                for (int i = 0; i < response.length(); i++) {
                                    JSONObject c = response.getJSONObject(i);
                                    // Storing each json item in variable
                                    com.kg.bar.expense.VDelivery vd = new com.kg.bar.expense.VDelivery();
                                    vd.entrydate = c.getString("entryDate");
                                    vd.senderName = c.getString("senderName");
                                    vd.senderPhone = c.getString("senderPhone");
                                    vd.senderCity = c.getString("senderCity");
                                    vd.senderCompany = c.getString("senderCompany");
                                    vd.receiverName = c.getString("receiverName");
                                    vd.receiverPhone = c.getString("receiverPhone");
                                    vd.receiverCity = c.getString("receiverCity");
                                    vd.receiverCompany = c.getString("receiverCompany");
                                    vd.paymentType = c.getString("paymentType");
                                    vd.deliveryCost = c.getString("deliveryCost");
                                    vd.deliveryiCost = c.getString("deliveryiCost");
                                    deliveryList.add(vd);
                                }
                                try {
                                    generateGeneralExcel(deliveryList);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } catch (RowsExceededException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                // Error in login. Get the error message
                                Toast.makeText(getApplicationContext(), "Эч нерсе табылбады, же бир ката пайда болду.", Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "General error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hideDialog();
                Log.e(TAG, "Expenses listing Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", HomeActivity.token);
                return headers;
            }
        };
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }


    private void generateGeneralExcel(List<com.kg.bar.expense.VDelivery> deliveryList) throws IOException, RowsExceededException {

        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");//dd/MM/yyyy
        Date now = new Date();
        String strDate = sdfDate.format(now);

        String Fnamexls = "YldamJalpySpisok" + strDate + ".xls";

        File sdCard = Environment.getExternalStorageDirectory();
        File directory = new File(sdCard.getAbsolutePath() + "/YLDAMREPORT");
        directory.mkdirs();
        File file = new File(directory, Fnamexls);

        WorkbookSettings wbSettings = new WorkbookSettings();
        wbSettings.setLocale(new Locale("en", "EN"));
        WritableWorkbook workbook;

        try {
            workbook = Workbook.createWorkbook(file, wbSettings);
            WritableSheet sheet = workbook.createSheet("First Sheet", 0);
            Label label;
            com.kg.bar.expense.VDelivery bb = new com.kg.bar.expense.VDelivery();

            try {

                label = new Label(0, 0, "Дата");
                sheet.addCell(label);
                label = new Label(1, 0, "Город отпр.");
                sheet.addCell(label);
                label = new Label(2, 0, "Тел. отправ.");
                sheet.addCell(label);
                label = new Label(3, 0, "Город полу.");
                sheet.addCell(label);
                label = new Label(4, 0, "Тел. получ.");
                sheet.addCell(label);
                label = new Label(5, 0, "Услуга дос.");
                sheet.addCell(label);
                label = new Label(6, 0, "Сумма выкупа");
                sheet.addCell(label);
                label = new Label(7, 0, "Тип платежа");
                sheet.addCell(label);
                int col = 0;

                for (int i = 0; i < deliveryList.size(); i++) {

                    bb = deliveryList.get(i);

                    label = new Label(0, i + 1, bb.entrydate);
                    sheet.addCell(label);
                    label = new Label(1, i + 1, bb.senderCity);
                    sheet.addCell(label);
                    label = new Label(2, i + 1, bb.senderPhone);
                    sheet.addCell(label);
                    label = new Label(3, i + 1, bb.receiverCity);
                    sheet.addCell(label);
                    label = new Label(4, i + 1, bb.receiverPhone);
                    sheet.addCell(label);
                    label = new Label(5, i + 1, bb.deliveryCost);
                    sheet.addCell(label);
                    label = new Label(6, i + 1, bb.deliveryiCost);
                    sheet.addCell(label);
                    label = new Label(7, i + 1, bb.paymentType);
                    sheet.addCell(label);

                }

                sheet.setColumnView(0, 12);
                sheet.setColumnView(1, 12);
                sheet.setColumnView(2, 12);
                sheet.setColumnView(3, 12);
                sheet.setColumnView(4, 12);
                sheet.setColumnView(5, 10);
                sheet.setColumnView(6, 10);
                sheet.setColumnView(7, 10);

            } catch (RowsExceededException e) {
                throw e;
            } catch (WriteException e) {
                e.printStackTrace();
            }

            workbook.write();
            ed_file_location.setText("Жөнөтүүлөрдүн отчёту даярдалды!\n\nОтчет файлдын аты:" + Fnamexls + "\nЖайгашкан жери:" + directory.toString());
            Toast.makeText(getApplicationContext(), "Отчет даярдалды.", Toast.LENGTH_LONG).show();
            try {
                workbook.close();
            } catch (WriteException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void listBoughtDeliveries(final String beginDate, final String endDate) {

        String tag_string_req = "req_list_bought_deliveries";
        boughtList.clear();
        pDialog.setMessage("Generating list ...");
        showDialog();

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("beginDate", beginDate);
            jsonObject.put("endDate", endDate);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        CustomJsonArrayRequest strReq = new CustomJsonArrayRequest(Request.Method.POST, AppConfig.URL_GET_DELIVERIES_BOUGHT, jsonObject,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d(TAG, "List bought deliveries: ");
                        hideDialog();
                        try {
                            if (response.length() > 0) {

                                for (int i = 0; i < response.length(); i++) {
                                    JSONObject c = response.getJSONObject(i);
                                    com.kg.bar.expense.Boughts fin = new com.kg.bar.expense.Boughts();
                                    fin.entrydate = c.getString("entryDate");
                                    fin.origin = c.getString("senderCity");
                                    fin.destination = c.getString("receiverCity");
                                    fin.boughtAmount = c.getString("deliveryiCost");
                                    fin.deliveryAmount = c.getString("deliveryCost");
                                    fin.senderPhone = c.getString("senderPhone");
                                    fin.receiverPhone = c.getString("receiverPhone");
                                    fin.paymentType = c.getString("paymentType");
                                    fin.explanation = c.getString("deliveryExplanation");
                                    boughtList.add(fin);
                                }
                                try {
                                    generateBoughtExcel(boughtList);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } catch (RowsExceededException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Toast.makeText(getApplicationContext(),
                                        "Эч нерсе табылбады, же бир ката пайда болду.", Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            // JSON error
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, " listing Error: " + error.getMessage());
                hideDialog();
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", HomeActivity.token);
                return headers;
            }
        };
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }


    private void generateBoughtExcel(List<com.kg.bar.expense.Boughts> buyList) throws IOException, RowsExceededException {

        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");//dd/MM/yyyy
        Date now = new Date();
        String strDate = sdfDate.format(now);

        String Fnamexls = "YldamSatinAlinanlar" + strDate + ".xls";

        File sdCard = Environment.getExternalStorageDirectory();
        File directory = new File(sdCard.getAbsolutePath() + "/YLDAMREPORT");
        directory.mkdirs();
        File file = new File(directory, Fnamexls);

        WorkbookSettings wbSettings = new WorkbookSettings();
        wbSettings.setLocale(new Locale("en", "EN"));
        WritableWorkbook workbook;

        try {
            workbook = Workbook.createWorkbook(file, wbSettings);
            WritableSheet sheet = workbook.createSheet("First Sheet", 0);
            Label label;
            Number number;
            com.kg.bar.expense.Boughts bb = new com.kg.bar.expense.Boughts();

            try {

                label = new Label(0, 0, "Дата");
                sheet.addCell(label);
                label = new Label(1, 0, "Город отпр.");
                sheet.addCell(label);
                label = new Label(2, 0, "Город полу.");
                sheet.addCell(label);
                label = new Label(3, 0, "Сумма выкупа");
                sheet.addCell(label);
                label = new Label(4, 0, "Услуга дос.");
                sheet.addCell(label);
                label = new Label(5, 0, "Тел. отправ.");
                sheet.addCell(label);
                label = new Label(6, 0, "Тел. получ.");
                sheet.addCell(label);
                label = new Label(7, 0, "Тип платежа");
                sheet.addCell(label);
                label = new Label(8, 0, "Примечание");
                sheet.addCell(label);
                int col = 0;

                for (int i = 0; i < buyList.size(); i++) {

                    bb = buyList.get(i);

                    label = new Label(0, i + 1, bb.entrydate);
                    sheet.addCell(label);
                    label = new Label(1, i + 1, bb.origin);
                    sheet.addCell(label);
                    label = new Label(2, i + 1, bb.destination);
                    sheet.addCell(label);
                    label = new Label(3, i + 1, bb.boughtAmount);
                    sheet.addCell(label);
                    label = new Label(4, i + 1, bb.deliveryAmount);
                    sheet.addCell(label);
                    label = new Label(5, i + 1, bb.senderPhone);
                    sheet.addCell(label);
                    label = new Label(6, i + 1, bb.receiverPhone);
                    sheet.addCell(label);
                    label = new Label(7, i + 1, bb.paymentType);
                    sheet.addCell(label);
                    label = new Label(8, i + 1, bb.explanation);
                    sheet.addCell(label);
                }

                sheet.setColumnView(0, 12);
                sheet.setColumnView(1, 12);
                sheet.setColumnView(2, 12);
                sheet.setColumnView(3, 8);
                sheet.setColumnView(4, 8);
                sheet.setColumnView(5, 12);
                sheet.setColumnView(6, 12);
                sheet.setColumnView(7, 10);
                sheet.setColumnView(8, 50);

            } catch (RowsExceededException e) {
                throw e;
            } catch (WriteException e) {
                e.printStackTrace();
            }

            workbook.write();
            ed_file_location.setText("Сатып алынган буюмдардын отчёту даярдалды!\n\nОтчет файлдын аты:" + Fnamexls + "\nЖайгашкан жери:" + directory.toString());
            Toast.makeText(getApplicationContext(), "Отчет даярдалды.", Toast.LENGTH_LONG).show();
            try {
                workbook.close();
            } catch (WriteException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void listFinancialReport(final String beginDate, final String endDate) {

        String tag_string_req = "req_list_finance";
        finList.clear();
        pDialog.setMessage("Generating ...");
        showDialog();

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("beginDate", beginDate);
            jsonObject.put("endDate", endDate);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        CustomJsonArrayRequest strReq = new CustomJsonArrayRequest(Request.Method.POST, AppConfig.URL_GET_FIN_REPORT, jsonObject,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            hideDialog();
                            if (response.length() > 0) {

                                for (int i = 0; i < response.length(); i++) {
                                    JSONObject c = response.getJSONObject(i);
                                    com.kg.bar.expense.Finance fin = new com.kg.bar.expense.Finance();
                                    fin.entrydate = c.getString("entryDate");
                                    fin.origin = c.getString("origin");
                                    fin.destination = c.getString("destination");
                                    fin.payment = c.getString("payment");
                                    fin.summa = c.getString("amount");
                                    finList.add(fin);
                                }
                                try {
                                    generateExcel(finList);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } catch (RowsExceededException e) {
                                    e.printStackTrace();
                                }

                            } else {
                                Toast.makeText(getApplicationContext(),
                                        "Эч нерсе табылбады, же бир ката пайда болду.", Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Expenses listing Error: " + error.getMessage());
                hideDialog();
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", HomeActivity.token);
                return headers;
            }
        };
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }


    public void listBankPayingDeliveries(final String beginDate, final String endDate) {

        String tag_string_req = "req_list_bank_payings";
        delList.clear();
        pDialog.setMessage("Generating ...");
        showDialog();

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("city", "");
            jsonObject.put("beginDate", beginDate);
            jsonObject.put("endDate", endDate);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        CustomJsonArrayRequest req = new CustomJsonArrayRequest(Request.Method.POST, AppConfig.URL_GET_BANK_PAID_LIST, jsonObject,
                new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d(TAG, "List Bak Paying Deliveries Response: " + response);
                        hideDialog();
                        try {
                            if (response.length() > 0) {
                                for (int i = 0; i < response.length(); i++) {
                                    JSONObject c = response.getJSONObject(i);
                                    com.kg.bar.expense.BankPaidDelivery fin = new com.kg.bar.expense.BankPaidDelivery();
                                    fin.entrydate = c.getString("entryDate");
                                    fin.senderCity = c.getString("senderCity");
                                    fin.receiverCity = c.getString("receiverCity");
                                    fin.senderCompany = c.getString("senderCompany");
                                    fin.receiverCompany = c.getString("receiverCompany");
                                    fin.payment = c.getString("paymentType");
                                    fin.summa = c.getString("deliveryCost");
                                    delList.add(fin);
                                }
                                try {
                                    generateBankPaidExcel(delList);
                                    hideDialog();
                                } catch (RowsExceededException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Toast.makeText(getApplicationContext(),
                                        "Эч нерсе табылбады, же бир ката пайда болду.", Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            // JSON error
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hideDialog();
                Log.e(TAG, "Expenses listing Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
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


    public void listExpenses(final String beginDate, final String endDate) {

        String tag_string_req = "req_list_expenses";
        expList.clear();
        pDialog.setMessage("Generating ...");
        showDialog();

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("city", "");
            jsonObject.put("beginDate", beginDate);
            jsonObject.put("endDate", endDate);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        CustomJsonArrayRequest req = new CustomJsonArrayRequest(Request.Method.POST, AppConfig.URL_GET_EXPENSE_LIST, jsonObject,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            hideDialog();
                            if (response.length() > 0) {

                                for (int i = 0; i < response.length(); i++) {
                                    JSONObject c = response.getJSONObject(i);
                                    com.kg.bar.expense.Expense fin = new com.kg.bar.expense.Expense();
                                    fin.exDate = c.getString("expenseDate");
                                    fin.exCity = c.getString("city");
                                    fin.exAmount = c.getString("amount");
                                    fin.exExplanation = c.getString("explanation");
                                    fin.exName = c.getString("enteredUser");
                                    fin.exId = c.getString("createdDate");
                                    expList.add(fin);
                                }
                                try {
                                    expenseReportExcel(expList);
                                } catch (RowsExceededException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Toast.makeText(getApplicationContext(),
                                        "Эч нерсе табылбады, же бир ката пайда болду.", Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hideDialog();
                Log.e(TAG, "Expenses listing Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
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


    private static WritableCellFormat getTextCenterCellFormat() throws WriteException {

        WritableCellFormat headerCellFormat = null;
        WritableFont wf_head = new WritableFont(WritableFont.TIMES, 11, WritableFont.NO_BOLD, false, UnderlineStyle.NO_UNDERLINE, jxl.format.Colour.BLACK);
        headerCellFormat = new WritableCellFormat(wf_head);
        headerCellFormat.setAlignment(Alignment.CENTRE);
        return headerCellFormat;
    }


    private void generateBankPaidExcel(List<com.kg.bar.expense.BankPaidDelivery> delList) throws IOException, RowsExceededException {

        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");//dd/MM/yyyy
        Date now = new Date();
        String strDate = sdfDate.format(now);

        String Fnamexls = "YldamBankFinance.xls";
        File sdCard = Environment.getExternalStorageDirectory();
        File directory = new File(sdCard.getAbsolutePath() + "/YLDAMREPORT");
        directory.mkdirs();
        File file = new File(directory, Fnamexls);

        WorkbookSettings wbSettings = new WorkbookSettings();
        wbSettings.setLocale(new Locale("en", "EN"));
        WritableWorkbook workbook;

        com.kg.bar.expense.BankPaidDelivery ff = new com.kg.bar.expense.BankPaidDelivery();

        try {
            workbook = Workbook.createWorkbook(file, wbSettings);
            WritableSheet sheet = workbook.createSheet("First Sheet", 0);
            Label label;

            try {

                int col = 0;
                int row = 0;

                label = new Label(col, row, "Направ.");
                sheet.addCell(label);
                label = new Label(col + 1, row, "Дата");
                sheet.addCell(label);
                label = new Label(col + 2, row, "Сумма");
                sheet.addCell(label);
                label = new Label(col + 3, row, "Фирма");
                sheet.addCell(label);
                label = new Label(col + 4, row, "Тип");
                sheet.addCell(label);
                sheet.setColumnView(col, 20);
                sheet.setColumnView(col + 1, 12);
                sheet.setColumnView(col + 2, 10);
                sheet.setColumnView(col + 3, 20);
                sheet.setColumnView(col + 4, 8);
                row++;

                for (int k = 0; k < delList.size(); k++) {

                    ff = delList.get(k);


                    label = new Label(col, row, ff.senderCity + " - " + ff.receiverCity);
                    sheet.addCell(label);
                    label = new Label(col + 1, row, ff.entrydate);
                    sheet.addCell(label);
                    label = new Label(col + 2, row, ff.summa);
                    sheet.addCell(label);

                    if (ff.payment.equalsIgnoreCase("SB")) {
                        label = new Label(col + 3, row, ff.senderCompany);
                        sheet.addCell(label);
                        label = new Label(col + 4, row, "Отправ");
                        sheet.addCell(label);
                    } else {
                        label = new Label(col + 3, row, ff.receiverCompany);
                        sheet.addCell(label);
                        label = new Label(col + 4, row, "Получа");
                        sheet.addCell(label);
                    }
                    row++;
                }

            } catch (RowsExceededException e) {
                throw e;
            } catch (WriteException e) {
                e.printStackTrace();
            }

            workbook.write();
            ed_file_location.setText("Финансылык отчет даярдалды!\n\nОтчет файлдын аты:" + Fnamexls + "\nЖайгашкан жери:" + directory.toString());
            Toast.makeText(getApplicationContext(), "Отчет даярдалды.", Toast.LENGTH_LONG).show();
            try {
                workbook.close();
            } catch (WriteException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void expenseReportExcel(List<com.kg.bar.expense.Expense> expList) throws IOException, RowsExceededException {

        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");//dd/MM/yyyy
        Date now = new Date();
        String strDate = sdfDate.format(now);

        String Fnamexls = "YldamExpenseReport.xls";
        File sdCard = Environment.getExternalStorageDirectory();
        File directory = new File(sdCard.getAbsolutePath() + "/YLDAMREPORT");
        directory.mkdirs();
        File file = new File(directory, Fnamexls);

        WorkbookSettings wbSettings = new WorkbookSettings();
        wbSettings.setLocale(new Locale("en", "EN"));
        WritableWorkbook workbook;

        com.kg.bar.expense.Expense ff = new com.kg.bar.expense.Expense();

        try {
            workbook = Workbook.createWorkbook(file, wbSettings);
            WritableSheet sheet = workbook.createSheet("First Sheet", 0);
            Label label;

            try {

                int row = 0;

                label = new Label(1, row, "Город");
                sheet.addCell(label);
                label = new Label(0, row, "Дата");
                sheet.addCell(label);
                label = new Label(2, row, "Сумма");
                sheet.addCell(label);
                label = new Label(3, row, "Коммент");
                sheet.addCell(label);
                label = new Label(4, row, "");
                sheet.addCell(label);
                label = new Label(5, row, "Кто");
                sheet.addCell(label);
                sheet.setColumnView(0, 12);
                sheet.setColumnView(1, 20);
                sheet.setColumnView(2, 12);
                sheet.setColumnView(3, 50);
                sheet.setColumnView(4, 20);
                sheet.setColumnView(5, 20);
                row++;

                for (int k = 0; k < expList.size(); k++) {

                    ff = expList.get(k);
                    label = new Label(0, row, ff.exDate);
                    sheet.addCell(label);
                    label = new Label(1, row, ff.exCity);
                    sheet.addCell(label);
                    label = new Label(2, row, ff.exAmount);
                    sheet.addCell(label);
                    label = new Label(3, row, ff.exExplanation);
                    sheet.addCell(label);
                    label = new Label(4, row, ff.exId);
                    sheet.addCell(label);
                    label = new Label(5, row, ff.exName);
                    sheet.addCell(label);
                    row++;
                }

            } catch (RowsExceededException e) {
                throw e;
            } catch (WriteException e) {
                e.printStackTrace();
            }

            workbook.write();
            ed_file_location.setText("Каражаттар отчёту даярдалды!\n\nОтчет файлдын аты:" + Fnamexls + "\nЖайгашкан жери:" + directory.toString());
            Toast.makeText(getApplicationContext(), "Отчет даярдалды.", Toast.LENGTH_LONG).show();
            try {
                workbook.close();
            } catch (WriteException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private boolean checkExists(List<com.kg.bar.expense.Finance> finList, String origin, String destination) throws IOException, RowsExceededException {

        try {
            boolean exists = false;
            com.kg.bar.expense.Finance ff;

            for (int k = 0; k < finList.size(); k++) {

                ff = finList.get(k);

                if (ff.origin.equalsIgnoreCase(origin) && ff.destination.equalsIgnoreCase(destination)) {
                    exists = true;
                    break;
                }
            }
            return exists;

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    private void generateExcel(List<com.kg.bar.expense.Finance> finList) throws IOException, RowsExceededException {

        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");//dd/MM/yyyy
        Date now = new Date();
        String strDate = sdfDate.format(now);

        String Fnamexls = "YldamFinance.xls";
        File sdCard = Environment.getExternalStorageDirectory();
        File directory = new File(sdCard.getAbsolutePath() + "/YLDAMREPORT");
        directory.mkdirs();
        File file = new File(directory, Fnamexls);

        WorkbookSettings wbSettings = new WorkbookSettings();
        wbSettings.setLocale(new Locale("en", "EN"));
        WritableWorkbook workbook;

        com.kg.bar.expense.Finance ff = new com.kg.bar.expense.Finance();

        try {
            workbook = Workbook.createWorkbook(file, wbSettings);

            Label label;
            Number number;

            List<String> citiesList = StringData.getCityList();
            String[] citiArray = new String[citiesList.size() - 1];

            int count = 0;
            for (String city : citiesList) {
                if (city.length() > 0) {
                    citiArray[count] = city;
                    count++;
                }
            }

            try {
                int row = 2;

                int col = 0;
                String origin;
                String destination;
                int originTotal = 0;
                int destinationTotal = 0;
                String prevDate = "";

                int sheetNo = 0;
                int width = 20;

                for (int i = 0; i < citiArray.length; i++) {
                    for (int j = 0; j < citiArray.length; j++) {

                        origin = citiArray[i];
                        destination = citiArray[j];

                        if (checkExists(finList, origin, destination)) {
                            WritableSheet sheet = workbook.createSheet(origin + destination, sheetNo);

                            label = new Label(col + 1, 0, origin + "-" + destination, getTextCenterCellFormat());
                            sheet.addCell(label);
                            sheet.mergeCells(col + 1, 0, col + 2, 0);
                            label = new Label(col + 1, 1, origin, getTextCenterCellFormat());
                            sheet.addCell(label);
                            label = new Label(col + 2, 1, destination, getTextCenterCellFormat());
                            sheet.addCell(label);

                            row = 2;
                            originTotal = 0;
                            destinationTotal = 0;

                            for (int k = 0; k < finList.size(); k++) {
                                ff = finList.get(k);

                                if (ff.origin.equalsIgnoreCase(origin) && ff.destination.equalsIgnoreCase(destination)) {
                                    if (!ff.entrydate.equalsIgnoreCase(prevDate))
                                        row++;
                                    label = new Label(col, row, ff.entrydate);
                                    sheet.addCell(label);

                                    if (ff.payment.equalsIgnoreCase("SC")) {
                                        number = new Number(col + 1, row, Double.valueOf(ff.summa));
                                        sheet.addCell(number);
                                        originTotal = originTotal + Integer.valueOf(ff.summa);
                                    } else if (ff.payment.equalsIgnoreCase("RC")) {
                                        label = new Label(col + 2, row, ff.summa);
                                        sheet.addCell(label);
                                        destinationTotal = destinationTotal + Integer.valueOf(ff.summa);
                                    }
                                    prevDate = ff.entrydate;
                                }
                            }

                            row = row + 2;
                            number = new Number(col + 1, row, Double.valueOf(originTotal));
                            sheet.addCell(number);
                            number = new Number(col + 2, row, Double.valueOf(destinationTotal));
                            sheet.addCell(number);

                            sheetNo++;


                            for (int y = 0; y < col; y++) {
                                sheet.setColumnView(y, width);
                            }
                        }
                    }
                }
            } catch (RowsExceededException e) {
                throw e;
            } catch (WriteException e) {
                e.printStackTrace();
            }

            workbook.write();
            ed_file_location.setText("Финансылык отчет даярдалды!\n\nОтчет файлдын аты:" + Fnamexls + "\nЖайгашкан жери:" + directory.toString());
            Toast.makeText(getApplicationContext(), "Финансылык отчет даярдалды.", Toast.LENGTH_LONG).show();
            try {
                workbook.close();
            } catch (WriteException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void generateExcel1(List<com.kg.bar.expense.Finance> finList) throws IOException, RowsExceededException {

        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");//dd/MM/yyyy
        Date now = new Date();
        String strDate = sdfDate.format(now);

        String Fnamexls = "YldamFinance.xls";
        File sdCard = Environment.getExternalStorageDirectory();
        File directory = new File(sdCard.getAbsolutePath() + "/YLDAMREPORT");
        directory.mkdirs();
        File file = new File(directory, Fnamexls);

        WorkbookSettings wbSettings = new WorkbookSettings();
        wbSettings.setLocale(new Locale("en", "EN"));
        WritableWorkbook workbook;

        com.kg.bar.expense.Finance ff = new com.kg.bar.expense.Finance();

        try {
            workbook = Workbook.createWorkbook(file, wbSettings);
            WritableSheet sheet = workbook.createSheet("First Sheet", 0);
            Label label;
            Number number;


            // String[] citiArray = getResources().getStringArray(R.array.cities);
            // {"Бишкек", "Жалал-Абад", "Ош", "Нарын", "Каракол", "Талас", "Баткен", "Чуй"};//getResources().getStringArray(R.array.cities);
            List<String> citiesList = StringData.getCityList();

            String[] citiArray = new String[citiesList.size() - 1];

            int count = 0;
            for (String city : citiesList) {
                if (city.length() > 0) {
                    citiArray[count] = city;
                    count++;
                }
            }

            try {
                int row = 2;

                int col = 0;
                String origin;
                String destination;
                int originTotal = 0;
                int destinationTotal = 0;
                String prevDate = "";

                for (int i = 0; i < citiArray.length; i++) {
                    for (int j = 0; j < citiArray.length; j++) {

                        origin = citiArray[i];
                        destination = citiArray[j];

                        label = new Label(col + 1, 0, origin + "-" + destination, getTextCenterCellFormat());
                        sheet.addCell(label);
                        sheet.mergeCells(col + 1, 0, col + 2, 0);
                        label = new Label(col + 1, 1, origin, getTextCenterCellFormat());
                        sheet.addCell(label);
                        label = new Label(col + 2, 1, destination, getTextCenterCellFormat());
                        sheet.addCell(label);

                        row = 2;
                        originTotal = 0;
                        destinationTotal = 0;

                        System.out.println(origin);
                        System.out.println(destination);

                        for (int k = 0; k < finList.size(); k++) {
                            ff = finList.get(k);

                            if (ff.origin.equalsIgnoreCase(origin) && ff.destination.equalsIgnoreCase(destination)) {
                                if (!ff.entrydate.equalsIgnoreCase(prevDate))
                                    row++;
                                label = new Label(col, row, ff.entrydate);
                                sheet.addCell(label);

                                if (ff.payment.equalsIgnoreCase("SC")) {
                                    number = new Number(col + 1, row, Double.valueOf(ff.summa));
                                    sheet.addCell(number);
                                    originTotal = originTotal + Integer.valueOf(ff.summa);
                                } else if (ff.payment.equalsIgnoreCase("RC")) {
                                    label = new Label(col + 2, row, ff.summa);
                                    sheet.addCell(label);
                                    destinationTotal = destinationTotal + Integer.valueOf(ff.summa);
                                }
                                prevDate = ff.entrydate;
                            }
                        }

                        row = row + 2;
                        number = new Number(col + 1, row, Double.valueOf(originTotal));
                        sheet.addCell(number);
                        number = new Number(col + 2, row, Double.valueOf(destinationTotal));
                        sheet.addCell(number);

                        col = col + 4;
                    }
                }

                int width = 12;
                //sheet.setColumnView(0, 12);
                for (int i = 0; i < col; i++) {
                    sheet.setColumnView(i, width);
                }

            } catch (RowsExceededException e) {
                throw e;
            } catch (WriteException e) {
                e.printStackTrace();
            }

            workbook.write();
            ed_file_location.setText("Финансылык отчет даярдалды!\n\nОтчет файлдын аты:" + Fnamexls + "\nЖайгашкан жери:" + directory.toString());
            Toast.makeText(getApplicationContext(), "Финансылык отчет даярдалды.", Toast.LENGTH_LONG).show();
            try {
                workbook.close();
            } catch (WriteException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
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

}
