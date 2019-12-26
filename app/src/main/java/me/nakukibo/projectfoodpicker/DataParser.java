package me.nakukibo.projectfoodpicker;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

class DataParser {

    // used for storing and loading values into HashMap
    static final String DATA_KEY_NAME = "restaurant_name";
    static final String DATA_KEY_ADDRESS = "formatted_addresss";
    static final String DATA_KEY_HOURS = "opening_hours";
    static final String DATA_KEY_CURRENTLY_OPEN = "currently_open";
    static final String DATA_KEY_PHOTO = "restaurant_photo";
    static final String DATA_KEY_RATING = "restaurant_rating";
    static final String DATA_KEY_TOT_RATING = "restaurant_total_rating";
    static final String DATA_KEY_PRICE_LEVEL = "price_level";
    static final String DATA_KEY_PHONE_NUMBER = "phone_number";
    static final String DATA_KEY_WEBSITE = "website";
    static final String DATA_KEY_PLACE_ID = "restaurant_place_id";

    // HashMap value if null or by default
    private static final String DATA_DEFAULT = "--NA--";

    private static final String TAG = DataParser.class.getSimpleName();

    /**
     * return the url for detailed informational fetch
     *
     * @param placeId place_id for the location where the data is to be fetched
     * @return String the url to be used to fetch the said data (phone number, opening hours, website)
     */
    private static String getDetailsUrl(String placeId) {
        String googlePlaceUrl = "https://maps.googleapis.com/maps/api/place/details/json?";
        googlePlaceUrl += "place_id=" + placeId;
        googlePlaceUrl += "&fields=formatted_phone_number,opening_hours,website";
        googlePlaceUrl += "&key=AIzaSyCd9Q5wxR59XOi1ugwZzH4l8fa2_BnBvOI";

        return googlePlaceUrl;
    }

    /**
     * return list of HashMaps for the JSON passed
     *
     * @param jsonData JSON data to be parsed
     * @return List<HashMap < String, String> parsed List for the JSON data
     */
    List<HashMap<String, String>> parse(String jsonData) {
        JSONArray jsonArray = null;
        JSONObject jsonObject;

        Log.d("json data", jsonData);

        try {
            jsonObject = new JSONObject(jsonData);
            jsonArray = jsonObject.getJSONArray("results");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return getAllPlacesData(jsonArray);
    }

    /**
     * returns the HashMap for all JSON data (ALL locations)
     *
     * @param jsonArray all of the JSON to be parsed
     * @return List<HashMap < String, String>>  list of all HashMaps returned for each location
     */
    private List<HashMap<String, String>> getAllPlacesData(JSONArray jsonArray) {
        List<HashMap<String, String>> placelist = new ArrayList<>();
        HashMap<String, String> placeMap;

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                placeMap = getPlaceData((JSONObject) jsonArray.get(i));
                placelist.add(placeMap);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return placelist;
    }

    /**
     * returns HashMap of the JSONObject passed for ONE location
     *
     * @param googlePlaceJson the JSON to be converted
     * @return HashMap<String, String> key values are declared as constants for easy access
     */
    private HashMap<String, String> getPlaceData(JSONObject googlePlaceJson) {
        HashMap<String, String> googlePlaceMap = new HashMap<>();

        // initialize all values to default
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

        Log.d("DataParser", "jsonobject =" + googlePlaceJson.toString());

        try {
            // overwrite the values individually if not null
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

            // add detail search here (for website, opening hours, phone number)

            // pass into map
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

            // log all values for debugging
            Log.d(TAG, "getPlaceData: Values from parse attempt");
            MainActivity.logValues(TAG, "getPlaceData", name, address, isCurrentlyOpen, hours,
                    photo, rating, totRating, priceLevel, phoneNumber, website, placeId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return googlePlaceMap;
    }
}
