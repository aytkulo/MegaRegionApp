package com.kg.megaregionapp.delivery;

import java.io.Serializable;


public class Delivery implements Serializable {

    public String deliveryImage;

    public String sFullName;
    public String sFullAddress;
    public String rFullName;
    public String rFullAddress;

    public int number;
    public String deliveryId;
    public String senderCity;
    public String senderCompany;
    public String senderPhone;
    public String senderName;
    public String senderAddress;
    public String receiverCity;
    public String receiverCompany;
    public String receiverPhone;
    public String receiverName;
    public String receiverAddress;

    public String deliveryType;
    public String deliveryCount;
    public String deliveryCost;
    public String deliveryiCost;
    public String deliveryExplanation;
    public String paymentType;
    public String assignedSector;
    public String deliveredPerson;
    public String acceptedPerson;
    public String buyType;

    public String costPaidDate;
    public String costPaidUser;

    public String paidAmount;

    public String entryDate;
    public String deliveredDate;
    public String status;
    public boolean checked;

    public String entryDateText;
    public String deliveredDateText;
    public String updatedDateText;
    public String costPaidDateText;
    public String entryDateOnly;

    public String receiver;
}
