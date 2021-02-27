package com.kg.megaregionapp.helper;

import android.telephony.SmsManager;

import java.util.ArrayList;

/**
 * Created by Aytkul Omurzakov on 7/11/2017.
 */

public class SMSManager {

    public static void sendAcceptanceSMS(String phoneNo, String smsContent) {


        try {
            SmsManager sms = SmsManager.getDefault();
            ArrayList<String> parts = sms.divideMessage(smsContent);
/*
            ArrayList<PendingIntent> sendList = new ArrayList<>();
            sendList.add(sentPI);

            ArrayList<PendingIntent> deliverList = new ArrayList<>();
            deliverList.add(deliveredPI);
*/
         //   sms.sendMultipartTextMessage(phoneNo, null, parts, null, null);

          //  sms.sendTextMessage(phoneNo, null, smsContent, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendCustomerSMS(String phoneNo, String smsContent) {

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, smsContent, null, null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
