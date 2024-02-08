package com.kg.megaregionapp.expense;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.kg.megaregionapp.R;

import java.util.List;

/**
 * Created by Aytkul Omurzakov on 7/4/2017.
 */

class AccountListAdapter extends BaseAdapter {
    private Context context;
    private List<Account> valueList;

    AccountListAdapter(List<Account> listValue, Context context) {
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
            convertView = layoutInfiater.inflate(R.layout.template_account_list, null);

            viewItem.txtAmount = (TextView) convertView.findViewById(R.id.txt_ex_amount);
            viewItem.txtName = (TextView) convertView.findViewById(R.id.txt_ex_user);
            viewItem.txtDate = (TextView) convertView.findViewById(R.id.txt_ex_date);
            viewItem.txtExplanation = (TextView) convertView.findViewById(R.id.txt_ex_expense);

            convertView.setTag(viewItem);
        } else {
            viewItem = (ExpenseViewItem) convertView.getTag();
        }

        viewItem.txtName.setText(valueList.get(position).postman);
        viewItem.txtAmount.setText(String.valueOf(valueList.get(position).amount));
        viewItem.txtExplanation.setText(String.valueOf(valueList.get(position).expense));
        viewItem.txtDate.setText(valueList.get(position).expenseDate);

        return convertView;
    }
}
