package com.kg.megaregionapp.users;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.kg.bar.HomeActivity;
import com.kg.bar.R;
import com.kg.bar.app.AppConfig;
import com.kg.bar.app.AppController;
import com.kg.bar.utils.MyDialog;
import com.kg.bar.utils.NetworkUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class PermissionListAdapter extends BaseAdapter {
    private Context context;
    private List<com.kg.bar.users.PermissionObject> valueList;

    PermissionListAdapter(List<com.kg.bar.users.PermissionObject> listValue, Context context) {
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
    public View getView(final int position, View convertView, final ViewGroup parent) {
        final PermissionViewItem viewItem;
        if (convertView == null) {
            viewItem = new PermissionViewItem();
            LayoutInflater layoutInfiater = (LayoutInflater) this.context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            //LayoutInflater layoutInfiater = LayoutInflater.from(context);
            convertView = layoutInfiater.inflate(R.layout.template_permission, null);

            viewItem.permissionSwitch = convertView.findViewById(R.id.postman_permission);
            convertView.setTag(viewItem);
        } else {
            viewItem = (PermissionViewItem) convertView.getTag();
        }

        viewItem.permissionSwitch.setText(valueList.get(position).postman);
        viewItem.permissionSwitch.setChecked(valueList.get(position).permission);


        viewItem.permissionSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                try {
                    updatePermission(viewItem.permissionSwitch.getText().toString(), isChecked);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });

        return convertView;
    }


    private void updatePermission(String postman, boolean isChecked) throws ParseException {

        if (!NetworkUtil.isNetworkConnected(context)) {
            MyDialog.createSimpleOkErrorDialog(context,
                    context.getString(R.string.dialog_error_title),
                    context.getString(R.string.check_internet)).show();
        } else if (NetworkUtil.isTokenExpired()) {
            MyDialog.createSimpleOkErrorDialog(context,
                    context.getString(R.string.dialog_error_title),
                    context.getString(R.string.relogin)).show();
        } else {
            String tag_string_req = "req_update_user_permission";

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("permission", isChecked);
                jsonObject.put("user", HomeActivity.userLogin);
                jsonObject.put("postman", postman);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JsonObjectRequest req = new JsonObjectRequest(AppConfig.URL_USER_PERMISSION, jsonObject,
                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    NetworkUtil.checkHttpStatus(context, error);
                }
            }) {
                @Override
                public Map<String, String> getHeaders() {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("Authorization", HomeActivity.token);
                    return headers;
                }
            };
            AppController.getInstance().addToRequestQueue(req, tag_string_req);
        }
    }
}
