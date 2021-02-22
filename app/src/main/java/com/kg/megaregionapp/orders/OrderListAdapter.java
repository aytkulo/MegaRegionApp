package com.kg.megaregionapp.orders;

/**
 * Created by ASUS on 7/1/2017.
 */

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.kg.megaregionapp.R;

import java.util.List;

public class OrderListAdapter extends BaseAdapter {
    Context context;
    List<Orders> valueList;

    public OrderListAdapter(List<Orders> listValue, Context context) {
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
        OrderViewItem viewItem = null;
        if (convertView == null) {
            viewItem = new OrderViewItem();
            LayoutInflater layoutInfiater = (LayoutInflater) this.context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            //LayoutInflater layoutInfiater = LayoutInflater.from(context);
            convertView = layoutInfiater.inflate(R.layout.template_order_list, null);

            viewItem.txtAddress =  convertView.findViewById(R.id.txtAddress);
            viewItem.txtName =  convertView.findViewById(R.id.txtName);
            viewItem.txtResponsible =  convertView.findViewById(R.id.txtResponsible);
            viewItem.txtTime =  convertView.findViewById(R.id.txtTime);

            viewItem.ed_id =  convertView.findViewById(R.id.ed_id);
            viewItem.ed_sCity =  convertView.findViewById(R.id.ed_sCity);
            viewItem.ed_sName =  convertView.findViewById(R.id.ed_sName);
            viewItem.ed_sPhone =  convertView.findViewById(R.id.ed_sPhone);
            viewItem.ed_sAddress =  convertView.findViewById(R.id.ed_sAddress);
            viewItem.ed_sCompany =  convertView.findViewById(R.id.ed_sCompany);
            viewItem.ed_rCity =  convertView.findViewById(R.id.ed_rCity);
            viewItem.ed_rName =  convertView.findViewById(R.id.ed_rName);
            viewItem.ed_rPhone =  convertView.findViewById(R.id.ed_rPhone);
            viewItem.ed_rAddress =  convertView.findViewById(R.id.ed_rAddress);
            viewItem.ed_rCompany =  convertView.findViewById(R.id.ed_rCompany);
            viewItem.order_icon =  convertView.findViewById(R.id.order_icon);
            viewItem.ed_eplanation =  convertView.findViewById(R.id.orderExplanation);

            convertView.setTag(viewItem);
        } else {
            viewItem = (OrderViewItem) convertView.getTag();
        }

        if (valueList.get(position).status.equalsIgnoreCase("1")) {
            viewItem.order_icon.setImageResource(R.drawable.icon_delivered_40);
        } else {
            viewItem.order_icon.setImageResource(R.drawable.icon_delivery_onway);
        }

        viewItem.txtAddress.setText(valueList.get(position).Address);
        viewItem.txtName.setText(valueList.get(position).Name);

        Orders or = valueList.get(position);

        String res = or.responsible;
        res = res + " (" + or.enteredUser + " - " + or.entrydate.substring(0, 10) + ", " + or.entrydate.substring(11, 16) + ")";
        viewItem.txtResponsible.setText(res);

        if (!or.updatedDate.equalsIgnoreCase("null")) {
            res = or.updatedUser + " (" + or.updatedDate.substring(0, 10) + ", " + or.updatedDate.substring(11, 16) + ")";
            viewItem.txtTime.setText(res);
        }

        viewItem.ed_eplanation.setText(or.orderExplanation);
        viewItem.ed_id.setText(valueList.get(position).id);

        viewItem.ed_sCity.setText(valueList.get(position).senderCity);
        viewItem.ed_sPhone.setText(valueList.get(position).senderPhone);
        viewItem.ed_sCompany.setText(valueList.get(position).senderCompany);
        viewItem.ed_sAddress.setText(valueList.get(position).senderAddress);
        viewItem.ed_sName.setText(valueList.get(position).senderName);

        viewItem.ed_rCity.setText(valueList.get(position).receiverCity);
        viewItem.ed_rPhone.setText(valueList.get(position).receiverPhone);
        viewItem.ed_rCompany.setText(valueList.get(position).receiverCompany);
        viewItem.ed_rAddress.setText(valueList.get(position).receiverAddress);
        viewItem.ed_rName.setText(valueList.get(position).receiverName);



        return convertView;
    }
}




