package com.kg.megaregionapp.delivery;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.kg.megaregionapp.R;

import java.util.List;

/**
 * Created by ASUS on 7/4/2017.
 */

class DeliveryStatusAdapter extends BaseAdapter {
    private Context context;
    private List<Delivery> valueList;

    DeliveryStatusAdapter(List<Delivery> listValue, Context context) {
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
        DeliveryViewItem viewItem;
        if (convertView == null) {
            viewItem = new DeliveryViewItem();
            LayoutInflater layoutInfiater = (LayoutInflater) this.context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInfiater.inflate(R.layout.template_delivery_status, null);

            viewItem.txtSenderAddress = (TextView) convertView.findViewById(R.id.txtSenderAddress);
            viewItem.txtSenderName = (TextView) convertView.findViewById(R.id.txtSenderName);
            viewItem.txtReceiverAddress = (TextView) convertView.findViewById(R.id.txtReceiverAddress);
            viewItem.txtReceiverName = (TextView) convertView.findViewById(R.id.txtReceiverName);
            viewItem.txtAssignedPostman = (TextView) convertView.findViewById(R.id.txtAssignedPostman);

            viewItem.ed_id = (TextView) convertView.findViewById(R.id.ed_id);

            viewItem.check_Tick = (CheckBox) convertView.findViewById(R.id.checkTick);

            convertView.setTag(viewItem);
        } else {
            viewItem = (DeliveryViewItem) convertView.getTag();
        }


        viewItem.txtSenderAddress.setText(valueList.get(position).sFullAddress);
        viewItem.txtSenderName.setText(valueList.get(position).sFullName);
        viewItem.txtReceiverAddress.setText(valueList.get(position).rFullAddress);
        viewItem.txtReceiverName.setText(valueList.get(position).rFullName);
        viewItem.txtAssignedPostman.setText(valueList.get(position).assignedSector);

        viewItem.ed_id.setText(valueList.get(position).deliveryId);

        final int pos = position;

        viewItem.check_Tick.setChecked(true);
        valueList.get(pos).checked = true;

        viewItem.check_Tick.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked)
                    valueList.get(pos).checked = true;
                else
                    valueList.get(pos).checked = false;

            }
        });

        return convertView;
    }
}
