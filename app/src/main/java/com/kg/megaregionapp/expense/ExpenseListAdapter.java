package com.kg.megaregionapp.expense;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.kg.bar.R;

import java.util.List;

/**
 * Created by Aytkul Omurzakov on 7/4/2017.
 */

class ExpenseListAdapter extends BaseAdapter {
    private Context context;
    private List<com.kg.bar.expense.Expense> valueList;

    ExpenseListAdapter(List<com.kg.bar.expense.Expense> listValue, Context context) {
        this.context = context;
        this.valueList = listValue;
    }

    @Override
    public int getCount() {
        return this.valueList.size();
    }

    @Override
    public Object getItem(int position) {
        return this.valueList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ExpenseViewItem viewItem;
        if (convertView == null) {
            viewItem = new ExpenseViewItem();
            LayoutInflater layoutInfiater = (LayoutInflater) this.context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            //LayoutInflater layoutInfiater = LayoutInflater.from(context);
            convertView = layoutInfiater.inflate(R.layout.template_expense_list, null);

            viewItem.txtAmount = (TextView) convertView.findViewById(R.id.txt_ex_amount);
            viewItem.txtName = (TextView) convertView.findViewById(R.id.txt_ex_user);
            viewItem.txtId = (TextView) convertView.findViewById(R.id.txt_ex_id);
            viewItem.txtCity = (TextView) convertView.findViewById(R.id.txt_ex_city);
            viewItem.txtExplanation = (TextView) convertView.findViewById(R.id.txt_ex_explanation);
            viewItem.txtDate = (TextView) convertView.findViewById(R.id.txt_ex_date);

            convertView.setTag(viewItem);
        } else {
            viewItem = (ExpenseViewItem) convertView.getTag();
        }


        viewItem.txtName.setText(valueList.get(position).exName);
        viewItem.txtAmount.setText(valueList.get(position).exAmount);
        viewItem.txtId.setText(valueList.get(position).exId);
        viewItem.txtCity.setText(valueList.get(position).exCity);
        viewItem.txtExplanation.setText(valueList.get(position).exExplanation);
        viewItem.txtDate.setText(valueList.get(position).exDate);

        return convertView;
    }
}
