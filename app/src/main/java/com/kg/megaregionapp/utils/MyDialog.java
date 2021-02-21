package com.kg.megaregionapp.utils;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;

import com.kg.bar.R;

public final class MyDialog {

    public static Dialog createSimpleOkErrorDialog(Context context, String message) {

        return createSimpleOkErrorDialog(context,
                context.getString(R.string.dialog_error_title),
                message);
    }

    public static Dialog createSimpleOkErrorDialog(Context context, String title, String message) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(context.getResources().getString(R.string.OK), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {


                    }
                });
        return alertDialog.create();
    }





    public static Dialog createSimpleOkErrorDialog(Context context,
                                                   @StringRes int titleResource,
                                                   @StringRes int messageResource) {

        return createSimpleOkErrorDialog(context,
                context.getString(titleResource),
                context.getString(messageResource));
    }

    public static Dialog createGenericErrorDialog(Context context, String message) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.dialog_error_title))
                .setMessage(message)
                .setNeutralButton(R.string.dialog_action_ok, null);
        return alertDialog.create();
    }

    public static Dialog createGenericErrorDialog(Context context, @StringRes int messageResource) {
        return createGenericErrorDialog(context, context.getString(messageResource));
    }

    public static ProgressDialog createProgressDialog(Context context, String message) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(message);
        return progressDialog;
    }

    public static ProgressDialog createProgressDialog(Context context,
                                                      @StringRes int messageResource) {
        return createProgressDialog(context, context.getString(messageResource));
    }

    public static ProgressDialog createStickyProgressDialog(Context context, @StringRes int messageResource) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle(context.getString(R.string.dialog_error_title));
        progressDialog.setCancelable(false);
        progressDialog.setMessage(context.getString(messageResource));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        return progressDialog;
    }

    public static ProgressDialog createServerRequestProgressDialog(Context context) {
        return createStickyProgressDialog(context, R.string.sending_data_to_server);
    }

}
