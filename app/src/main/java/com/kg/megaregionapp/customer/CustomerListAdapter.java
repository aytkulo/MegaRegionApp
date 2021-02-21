package com.kg.megaregionapp.customer;

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

class CustomerListAdapter extends BaseAdapter {
    private Context context;
    private List<Customer> valueList;

    CustomerListAdapter(List<Customer> listValue, Context context) {
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
        com.kg.bar.customer.CustomerViewItem viewItem;
        if (convertView == null) {
            viewItem = new com.kg.bar.customer.CustomerViewItem();
            LayoutInflater layoutInfiater = (LayoutInflater) this.context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            //LayoutInflater layoutInfiater = LayoutInflater.from(context);
            convertView = layoutInfiater.inflate(R.layout.template_customer_list, null);

            viewItem.txtAddress = (TextView) convertView.findViewById(R.id.txtAddress);
            viewItem.txtName = (TextView) convertView.findViewById(R.id.txtName);

            viewItem.cus_id = (TextView) convertView.findViewById(R.id.cus_id);

            viewItem.cus_City = (TextView) convertView.findViewById(R.id.cus_City);
            viewItem.cus_Phone = (TextView) convertView.findViewById(R.id.cus_Phone);
            viewItem.cus_Company = (TextView) convertView.findViewById(R.id.cus_Company);
            viewItem.cus_Address = (TextView) convertView.findViewById(R.id.cus_Address);
            viewItem.cus_Name = (TextView) convertView.findViewById(R.id.cus_Name);

            convertView.setTag(viewItem);
        } else {
            viewItem = (com.kg.bar.customer.CustomerViewItem) convertView.getTag();
        }


        viewItem.txtAddress.setText(valueList.get(position).cusFullAddress);
        viewItem.txtName.setText(valueList.get(position).cusFullName);

        viewItem.cus_id.setText(valueList.get(position).cus_id);

        viewItem.cus_City.setText(valueList.get(position).cus_City);
        viewItem.cus_Address.setText(valueList.get(position).cus_Address);
        viewItem.cus_Company.setText(valueList.get(position).cus_Company);
        viewItem.cus_Phone.setText(valueList.get(position).cus_Phone);
        viewItem.cus_Name.setText(valueList.get(position).cus_Name);

        return convertView;
    }
}
