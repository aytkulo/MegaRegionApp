package com.kg.megaregionapp.helper;

import android.content.Context;

import com.android.volley.Request;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.kg.megaregionapp.HomeActivity;
import com.kg.megaregionapp.app.AppConfig;
import com.kg.megaregionapp.app.AppController;
import com.kg.megaregionapp.utils.NetworkUtil;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StringData {


    public static List<String> cityList = new ArrayList<>();

    public static List<String> getCityList() {
        if (cityList.size() > 0)
            return cityList;
        else
            return getCityListConstant();
    }


    public static void listRegions(final Context context) {

        String tag_string_req = "req_get_cities";

        CustomJsonArrayRequest req = new CustomJsonArrayRequest(Request.Method.GET, AppConfig.URL_GET_CITIES, null,
                response -> {

                    try {
                        if (response.length() > 0) {

                            cityList.clear();
                            cityList.add("");
                            JsonParser parser = new JsonParser();
                            Gson gson = new Gson();

                            for (int i = 0; i < response.length(); i++) {

                                JsonElement mJsonM = parser.parse(response.getString(i));
                                RegionEntity dd = gson.fromJson(mJsonM, RegionEntity.class);
                                cityList.add(dd.getRegionName());
                            }

                        } else {

                        }
                    } catch (JSONException e) {

                    }

                }, error -> NetworkUtil.checkHttpStatus(context, error)) {

            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", HomeActivity.token);
                return headers;
            }
        };
        AppController.getInstance().addToRequestQueue(req, tag_string_req);

    }

    public class RegionEntity {

        public String regionName;

        public String getRegionName() {
            return regionName;
        }
        public void setRegionName(String regionName) {
            this.regionName = regionName;
        }
    }

    public static List<String> getCityListConstant() {
        List<String> spinnerArray = new ArrayList<>();
        spinnerArray.add("");
        spinnerArray.add("Бишкек");
        spinnerArray.add("Ош");
        spinnerArray.add("Жалал-Абад");
        spinnerArray.add("Нарын");
        spinnerArray.add("Каракол");
        spinnerArray.add("Талас");
        spinnerArray.add("Чолпоната");
        spinnerArray.add("Токтогул");
        spinnerArray.add("Балыкчы");
        return spinnerArray;
    }

}
