package com.kg.megaregionapp.app;

/**
 * Created by ASUS on 6/22/2017.
 */
public class AppConfig {

 //   public static String PURE_URL = "http://10.0.2.2:8080/";

    public static String PURE_URL = "http://194.163.181.93:8080/mega-api/";

    public static String IMAGES_URL = "http://194.163.181.93:8080/mega_region_images/";

    public static String URL_IMAGES_SAVE = PURE_URL + "files/upload/string";

    public static String URL_GET_CITIES = PURE_URL + "sectors/regions";

    public static String URL_DELIVERY_CHECK = PURE_URL + "delivery/check";

    public static String URL_DELIVERY_ENTRY = PURE_URL + "delivery/save";

    public static String URL_DELIVERY_GET = PURE_URL + "delivery/get";

    public static String URL_DELIVERY_DELETE = PURE_URL + "delivery/delete";

    public static String URL_DELIVERY_LIST = PURE_URL + "delivery/list";

    public static String URL_DELIVERY_UPDATE = PURE_URL + "delivery/update";

    public static String URL_DELIVERY_OPEN_LIST = PURE_URL + "delivery/list/open";

    public static String URL_DELIVERY_LIST_WITH_DEBTS = PURE_URL + "delivery/list/debt";

    public static String URL_DELIVERY_PAY_DEBT = PURE_URL + "delivery/payCost";

    public static String URL_DELIVERY_LIST_WHO = PURE_URL + "delivery/list/who";

    public static String URL_DELIVERY_DELIVER = PURE_URL + "delivery/deliver";

    public static String URL_DELIVERY_ASSIGN = PURE_URL + "delivery/assign";

    public static String URL_GET_BANK_PAID_LIST = PURE_URL + "delivery/list/bank";

    public static String URL_GET_DELIVERIES_BOUGHT = PURE_URL + "delivery/list/bought";

    public static String URL_GET_DELIVERIES_FOR_REPORT = PURE_URL + "delivery/list/report";


    public static String URL_CORPORATE_CUSTOMER_LIST = PURE_URL + "customers/corporate/list";

    public static String URL_CORPORATE_CUSTOMER_CHECK = PURE_URL + "customers/corporate/check";

    public static String URL_CORPORATE_CUSTOMER_SAVE = PURE_URL + "customers/corporate/save";

    public static String URL_CUSTOMER_UPDATE = PURE_URL + "customer/retail/update";

    public static String URL_CUSTOMER_GET = PURE_URL + "customers/retail/list";

    public static String URL_CUSTOMER_SAVE = PURE_URL + "customers/retail/save";

    public static String URL_CORPORATE_CUSTOMER_UPDATE = PURE_URL + "customers/corporate/update";

    public static String URL_CUSTOMER_DELETE = PURE_URL + "customers/delete";

    public static String URL_GET_SECTORS = PURE_URL + "sectors/list";


    public static String URL_LOGIN = PURE_URL + "users/login";

    public static String URL_GET_USERS = PURE_URL + "users/list";

    public static String URL_REGISTER = PURE_URL + "users";

    public static String URL_UPDATE_PSW = PURE_URL + "users/update";

    public static String URL_DELETE_USER = PURE_URL + "users/delete";

    public static String URL_USER_PERMISSION = PURE_URL + "users/updatepermission";

    public static String URL_GET_USER_PERMISSION = PURE_URL + "users/getPermission";

    public static String URL_LIST_PERMISSIONS = PURE_URL + "users/listPermissions";

    public static String URL_ORDER_SAVE = PURE_URL + "order/save";

    public static String URL_ORDER_GET = PURE_URL + "order/list";

    public static String URL_ORDER_UPDATE_ACCEPT = PURE_URL + "order/accept";


    public static String URL_EXPENSE_LIST = PURE_URL + "expense/list";

    public static String URL_EXPENSE_LIST_POSTMAN_REP = PURE_URL + "expense/list/postman";

    public static String URL_COLLECTION_LIST = PURE_URL + "reports/collections";

    public static String URL_GET_FIN_REPORT = PURE_URL + "reports/financial";

    public static String URL_GET_CUSTOMER_DELIVERIES = PURE_URL + "reports/proforma";


    public static String URL_TRANSACTION_SAVE = PURE_URL + "transactions/save";

    public static String URL_TRANSACTION_UPDATE = PURE_URL + "transactions/update";

    public static String URL_TRANSACTIONS_LIST = PURE_URL + "transactions/list";

    public static String URL_EXPENSE_SAVE = PURE_URL + "expense/save";

    public static String URL_EXPENSE_UPDATE = PURE_URL + "expense/update";

    public static String URL_GET_EXPENSE_LIST = PURE_URL + "expense/list/report";

    public static String URL_SAVE_LOCATION = PURE_URL + "reports/save/location";

    public static String URL_GET_LAST_LOCATION = PURE_URL + "location_get_last.php";


    // global topic to receive app wide push notifications
    public static final String TOPIC_GLOBAL = "global";

    // broadcast receiver intent filters
    public static final String REGISTRATION_COMPLETE = "registrationComplete";
    public static final String PUSH_NOTIFICATION = "pushNotification";

    // id to handle the notification in the notification tray
    public static final int NOTIFICATION_ID = 100;
    public static final int NOTIFICATION_ID_BIG_IMAGE = 101;

    public static final String SHARED_PREF = "firebase";
}

