package com.kg.megaregionapp.delivery;

import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by ASUS on 7/2/2017.
 */

public class DeliveryViewItem {
    TextView txtSenderName;
    TextView txtSenderAddress;
    TextView txtReceiverName;
    TextView txtReceiverAddress;
    TextView txtAssignedPostman;
    TextView txtDeliveredPostman;
    TextView txtAcceptedPostman;
    TextView txtNumber;

    TextView ed_id;
    TextView ed_sName;
    TextView ed_sCity;
    TextView ed_sAddress;
    TextView ed_sCompany;
    TextView ed_sPhone;
    TextView ed_rCity;
    TextView ed_rName;
    TextView ed_rAddress;
    TextView ed_rCompany;
    TextView ed_rPhone;

    TextView ed_dType;
    TextView ed_dCount;
    TextView ed_dCost;
    TextView ed_diCost;
    TextView ed_dExpl;
    TextView ed_payment;

    TextView ed_paidAmount;

    ImageView delivery_icon;

    CheckBox check_Tick;
    //  Button btn_Deliver;
    // Button btn_Update;
}