package me.nakukibo.projectfoodpicker;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DataParser {

    public static final String DATA_KEY_NAME = "restaurant_name";
    public static final String DATA_KEY_ADDRESS = "formatted_addresss";
    public static final String DATA_KEY_HOURS = "opening_hours";
    public static final String DATA_KEY_CURRENTLY_OPEN = "currently_open";
    public static final String DATA_KEY_PHOTO = "restaurant_photo";
    public static final String DATA_KEY_RATING = "restaurant_rating";
    public static final String DATA_KEY_TOT_RATING = "restaurant_total_rating";
    public static final String DATA_KEY_PRICE_LEVEL = "price_level";
    public static final String DATA_KEY_PHONE_NUMBER = "phone_number";
    public static final String DATA_KEY_WEBSITE = "website";
    public static final String DATA_KEY_PLACE_ID = "restaurant_place_id";

    private static final String DATA_DEFAULT = "--NA--";

    private static final String TAG = DataParser.class.getSimpleName();

    private HashMap<String, String> getPlace(JSONObject googlePlaceJson)
    {
        HashMap<String, String> googlePlaceMap = new HashMap<>();
        String name = DATA_DEFAULT;
        String address = DATA_DEFAULT;
        String hours = DATA_DEFAULT;
        String isCurrentlyOpen = DATA_DEFAULT;
        String photo = DATA_DEFAULT;
        String rating = DATA_DEFAULT;
        String totRating = DATA_DEFAULT;
        String priceLevel = DATA_DEFAULT;
        String phoneNumber = DATA_DEFAULT;
        String website = DATA_DEFAULT;
        String placeId = DATA_DEFAULT;

        Log.d("DataParser","jsonobject ="+googlePlaceJson.toString());

        try {
            if (!googlePlaceJson.isNull("name")) {
                name = googlePlaceJson.getString("name");
            }
            if (!googlePlaceJson.isNull("formatted_address")) {
                address = googlePlaceJson.getString("formatted_address");
            }
            if (!googlePlaceJson.isNull("opening_hours")) {
                isCurrentlyOpen = googlePlaceJson.getJSONObject("opening_hours").getString("open_now");
            }
            if (!googlePlaceJson.isNull("photos")) {
                photo = googlePlaceJson.getJSONArray("photos").get(0).toString(); // TODO: fix this
            }
            if (!googlePlaceJson.isNull("rating")) {
                rating = googlePlaceJson.getString("rating");
            }
            if (!googlePlaceJson.isNull("user_ratings_total")) {
                totRating = googlePlaceJson.getString("user_ratings_total");
            }
            if (!googlePlaceJson.isNull("price_level")) {
                priceLevel = googlePlaceJson.getString("price_level");
            }
            if (!googlePlaceJson.isNull("place_id")) {
                placeId = googlePlaceJson.getString("place_id");
            }

            // add detail search here


            googlePlaceMap.put(DATA_KEY_NAME, name);
            googlePlaceMap.put(DATA_KEY_ADDRESS, address);
            googlePlaceMap.put(DATA_KEY_HOURS, hours);
            googlePlaceMap.put(DATA_KEY_CURRENTLY_OPEN, isCurrentlyOpen);
            googlePlaceMap.put(DATA_KEY_PHOTO, photo);
            googlePlaceMap.put(DATA_KEY_RATING, rating);
            googlePlaceMap.put(DATA_KEY_TOT_RATING, totRating);
            googlePlaceMap.put(DATA_KEY_PRICE_LEVEL, priceLevel);
            googlePlaceMap.put(DATA_KEY_PHONE_NUMBER, phoneNumber);
            googlePlaceMap.put(DATA_KEY_WEBSITE, website);
            googlePlaceMap.put(DATA_KEY_PLACE_ID, placeId);

            Log.d(TAG, "getPlace: Values from parse attempt");
            MainActivity.logValues(TAG, "getPlace", name, address, isCurrentlyOpen, hours,
                    photo, rating, totRating, priceLevel, phoneNumber, website, placeId);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return googlePlaceMap;

    }
    private List<HashMap<String, String>>getPlaces(JSONArray jsonArray)
    {
        int count = jsonArray.length();
        List<HashMap<String, String>> placelist = new ArrayList<>();
        HashMap<String, String> placeMap = null;

        for(int i = 0; i<count;i++)
        {
            try {
                placeMap = getPlace((JSONObject) jsonArray.get(i));
                placelist.add(placeMap);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return placelist;
    }

    public List<HashMap<String, String>> parse(String jsonData)
    {
        JSONArray jsonArray = null;
        JSONObject jsonObject;

        Log.d("json data", jsonData);

        try {
            jsonObject = new JSONObject(jsonData);
            jsonArray = jsonObject.getJSONArray("results");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return getPlaces(jsonArray);
    }

    private static String getDetailsUrl(String placeId) {
        StringBuilder googlePlaceUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/details/json?");
        googlePlaceUrl.append("place_id=").append(placeId);
        googlePlaceUrl.append("&fields=formatted_phone_number,opening_hours,website");
        googlePlaceUrl.append("&key=AIzaSyCd9Q5wxR59XOi1ugwZzH4l8fa2_BnBvOI");

        return googlePlaceUrl.toString();
    }
}
