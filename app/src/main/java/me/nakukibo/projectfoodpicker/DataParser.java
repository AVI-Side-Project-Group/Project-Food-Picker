package me.nakukibo.projectfoodpicker;

import android.location.Location;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

class DataParser {

    private static final String TAG = DataParser.class.getSimpleName();

    // used for storing and loading values into HashMap
    static final String DATA_KEY_NAME = "restaurant_name";
    static final String DATA_KEY_ADDRESS = "formatted_address";
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
    private String nextPageToken;

    /**
     * return list of HashMaps for the JSON passed
     *
     * @param jsonData JSON data to be parsed
     * @return List<HashMap < String, String> parsed List for the JSON data
     */
    List<HashMap<String, String>> parse(String jsonData, Location userLocation, int maxDistance,
                                        int pricingRange, int minRating) throws RuntimeException {

        nextPageToken = null;

        JSONArray jsonArray = null;
        JSONObject jsonObject;

        try {
            Log.d(TAG, "parse: jsonData=" + jsonData);
            jsonObject = new JSONObject(jsonData);

            if(jsonObject.getString("status").equals("INVALID_REQUEST")) throw new RuntimeException("Invalid Request");

            jsonArray = jsonObject.getJSONArray("results");
            try {
                nextPageToken = jsonObject.getString("next_page_token");
            } catch (JSONException e){
                e.printStackTrace();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "parse: logging new set of jsonData=======================================");
        if(jsonArray == null) return null;
        return getAllPlacesData(jsonArray, userLocation, maxDistance, pricingRange, minRating);
    }

    void parseDetails(String jsonData, HashMap<String, String> selectedRestaurant){
        String hours = DATA_DEFAULT;
        String phoneNumber = DATA_DEFAULT;
        String website = DATA_DEFAULT;

        JSONObject jsonObject = null;

        try {
            Log.d(TAG, "parseDetails: jsonData=" + jsonData);
            jsonObject = new JSONObject(jsonData);

            if(jsonObject.getString("status").equals("INVALID_REQUEST")) return;

            jsonObject = jsonObject.getJSONObject("result");
        } catch (JSONException e) {
            e.printStackTrace();
            jsonObject = null;
        }

        if(jsonObject == null) return;

        try {
            // opening_hours
            JSONArray periodsArray = null;
            if (!jsonObject.isNull("opening_hours")) {
                JSONObject hoursObj = jsonObject.getJSONObject("opening_hours");
                try {
                    periodsArray = hoursObj.getJSONArray("periods");
                } catch (JSONException e){
                    e.printStackTrace();
                    Log.d(TAG, "parseDetails: opening hour periods errors");
                }
            }

            if(periodsArray != null) hours = getHours(periodsArray);

            // formatted_phone_number
            if (!jsonObject.isNull("formatted_phone_number")) {
                phoneNumber = jsonObject.getString("formatted_phone_number");
            }

            // website
            if (!jsonObject.isNull("website")) {
                website = jsonObject.getString("website");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // pass into map
        selectedRestaurant.put(DATA_KEY_HOURS, hours);
        selectedRestaurant.put(DATA_KEY_PHONE_NUMBER, phoneNumber);
        selectedRestaurant.put(DATA_KEY_WEBSITE, website);
    }

    private String getHours(JSONArray periodsArray) {

        StringBuilder hours = new StringBuilder();
        String[] dayHours = getDayHoursStr(periodsArray);
        if(dayHours == null) return DATA_DEFAULT;

        for(String day: dayHours){
            if(day == null) hours.append(DATA_DEFAULT);
            hours.append(day).append("\n");
        }

//        return hours.toString();
        return DATA_DEFAULT;
    }

    private String[] getDayHoursStr(JSONArray periodsArray){
        String[] dayHours = new String[7];

        for(int i=0; i<periodsArray.length(); i++){
            try {
                JSONObject dayObj = periodsArray.getJSONObject(i);
                Log.d(TAG, "getDayHoursStr: data=" + i + ", data=" + dayObj);
//
//                JSONObject closed = null;
//                JSONObject open = null;
//
//                if(!dayObj.isNull("open")) {
//                    open = dayObj.getJSONObject("open");
//                }
//
//                if(!dayObj.isNull("close")) {
//                    closed = dayObj.getJSONObject("close");
//                }
//
//                int currentDay = -1;
//                String openingHours = null;
//                String closingHours = null;
//
//                if(open == null && closed == null) continue;
//
//                if(open != null) {
//                    currentDay = Integer.parseInt(open.getString("day"));
//                    openingHours = open.getString("time");
//                }
//
//                if(closed != null){
//                    currentDay = Integer.parseInt(closed.getString("day"));
//                    closingHours = closed.getString("time");
//                }
//
//                Integer openInt = openingHours == null ? null : Integer.parseInt(openingHours);
//                Integer closeInt = closingHours == null ? null : Integer.parseInt(closingHours);
//
//                dayHours[currentDay] = new DayHours(openInt, closeInt);
            } catch (JSONException e){
                e.printStackTrace();
            }
        }
        return dayHours;
    }

    private String getTimeFormat(int timeInt){;
        boolean isAM = timeInt < 1200;
        if(timeInt >= 1200) timeInt -= 1200;
        if(timeInt == 0) timeInt = 1200;

        String timeStr = String.valueOf(timeInt);
        if(timeStr.length() < 4) timeStr = "0" + timeStr;

        return timeStr.substring(0, 2) + ":" + timeStr.substring(2) + (isAM ? "AM":"PM");

    }

    /**
     * returns the HashMap for all JSON data (ALL locations)
     *
     * @param jsonArray all of the JSON to be parsed
     * @return List<HashMap < String, String>>  list of all HashMaps returned for each location
     */
    private List<HashMap<String, String>> getAllPlacesData(JSONArray jsonArray, Location userLocation,
                                                           int maxDistance, int pricingRange, int minRating) {
        List<HashMap<String, String>> placelist = new ArrayList<>();
        HashMap<String, String> placeMap;

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                placeMap = getPlaceData((JSONObject) jsonArray.get(i), userLocation, maxDistance, pricingRange, minRating);
                if(placeMap != null) {
                    placelist.add(placeMap);
                    Log.d(TAG, "getAllPlacesData: place added");
                }else {
                    Log.d(TAG, "getAllPlacesData: place not added");
                }
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
    private HashMap<String, String> getPlaceData(JSONObject googlePlaceJson, Location userLocation,
                                                 int maxDistance, int pricingRange, int minRating) {
        HashMap<String, String> googlePlaceMap = new HashMap<>();

        // initialize all values to default
        String name = DATA_DEFAULT;
        String address = DATA_DEFAULT;
        String isCurrentlyOpen = DATA_DEFAULT;
        String photo = DATA_DEFAULT;
        String rating = DATA_DEFAULT;
        String totRating = DATA_DEFAULT;
        String priceLevel = DATA_DEFAULT;
        String placeId = DATA_DEFAULT;

        Log.d("DataParser", "jsonobject =" + googlePlaceJson.toString());

        try {
            // name
            if (!googlePlaceJson.isNull("name")) {
                name = googlePlaceJson.getString("name");
            }

            // if too far, then return null
            if (!isCloseToUser(userLocation, googlePlaceJson, name, maxDistance)) return null;

            // address
            if (!googlePlaceJson.isNull("formatted_address")) {
                address = googlePlaceJson.getString("formatted_address");
            }

            // is open right now
            if (!googlePlaceJson.isNull("opening_hours")) {
                JSONObject openHoursObject = googlePlaceJson.getJSONObject("opening_hours");
                if (!openHoursObject.isNull("open_now")) {
                    isCurrentlyOpen = openHoursObject.getString("open_now");
                }
            }

            // photos
            if (!googlePlaceJson.isNull("photos")) {
                photo = googlePlaceJson.getJSONArray("photos").get(0).toString();
            }

            // rating
            if (!googlePlaceJson.isNull("rating")) {
                rating = googlePlaceJson.getString("rating");
                // if rating too low, then return null
                if (Double.parseDouble(rating) < minRating) {
                    Log.d(TAG, "getPlaceData: " + name + "'s rating is too low. Returning null");
                    return null;
                }
            }

            // total rating count
            if (!googlePlaceJson.isNull("user_ratings_total")) {
                totRating = googlePlaceJson.getString("user_ratings_total");
            }

            // price level
            if (!googlePlaceJson.isNull("price_level")) {
                priceLevel = googlePlaceJson.getString("price_level");
                // if price level not correct, return null
                if (Double.parseDouble(priceLevel) != pricingRange && pricingRange != PreferencesActivity.PREF_ANY_INT_REP) {
                    Log.d(TAG, "getPlaceData: " + name + " has incorrect price level.");
                    return null;
                }
            }

            // place id
            if (!googlePlaceJson.isNull("place_id")) {
                placeId = googlePlaceJson.getString("place_id");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // pass into map
        googlePlaceMap.put(DATA_KEY_NAME, name);
        googlePlaceMap.put(DATA_KEY_ADDRESS, address);
        googlePlaceMap.put(DATA_KEY_HOURS, DATA_DEFAULT);
        googlePlaceMap.put(DATA_KEY_CURRENTLY_OPEN, isCurrentlyOpen);
        googlePlaceMap.put(DATA_KEY_PHOTO, photo);
        googlePlaceMap.put(DATA_KEY_RATING, rating);
        googlePlaceMap.put(DATA_KEY_TOT_RATING, totRating);
        googlePlaceMap.put(DATA_KEY_PRICE_LEVEL, priceLevel);
        googlePlaceMap.put(DATA_KEY_PHONE_NUMBER, DATA_DEFAULT);
        googlePlaceMap.put(DATA_KEY_WEBSITE, DATA_DEFAULT);
        googlePlaceMap.put(DATA_KEY_PLACE_ID, placeId);

        Log.d(TAG, "getPlaceData: ---------------------------------------------------------");
        return googlePlaceMap;
    }

    private boolean isCloseToUser(Location userLocation, JSONObject googlePlaceJson, String name, int maxDistance) {
        double latitude;
        double longitude;
        try {
            latitude = Double.parseDouble(
                    googlePlaceJson.getJSONObject("geometry").getJSONObject("location").getString("lat")
            );
            longitude = Double.parseDouble(
                    googlePlaceJson.getJSONObject("geometry").getJSONObject("location").getString("lng")
            );
        } catch (JSONException e) {
            Log.d(TAG, "getPlaceData: " + name + " cannot determine location. Returning null.");
            return false;
        }

        Location restLocation = new Location("");
        restLocation.setLatitude(latitude);
        restLocation.setLongitude(longitude);

        boolean closeEnough = userLocation.distanceTo(restLocation) <= maxDistance;
        if (!closeEnough) Log.d(TAG, "isCloseToUser: " + name + " removed b/c too far.");
        return closeEnough;
    }

    String getNextPageToken(){
        return nextPageToken;
    }
}
