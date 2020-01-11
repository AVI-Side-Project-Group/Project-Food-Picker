package me.nakukibo.projectfoodpicker;

import android.location.Location;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

class DataParser {

    private static final String TAG = DataParser.class.getSimpleName();
    private String nextPageToken; // for storing and later accessing the nextPageToken since the process runs in background

    /**
     * return list of HashMaps for the JSON passed
     * the value will be set to default if not available
     *
     * @param jsonData JSON data to be parsed
     * @return List<HashMap < String, String> parsed List for the JSON data
     */
    List<Restaurant> parse(String jsonData, Location userLocation, int maxDistance,
                                        int pricingRange, int minRating) throws RuntimeException {
        Log.d(TAG, "parse: jsonData=" + jsonData);

        JSONArray jsonArray;
        JSONObject jsonObject;
        nextPageToken = null;

        try {
            jsonObject = new JSONObject(jsonData);
            if (!jsonObject.isNull("status") && jsonObject.getString("status").equals("INVALID_REQUEST")) {
                throw new RuntimeException("Invalid Request");
            }

            jsonArray = jsonObject.getJSONArray("results");

            try {
                nextPageToken = jsonObject.getString("next_page_token");
            } catch (JSONException e) {
                Log.d(TAG, "parse: error in fetching next_page_token");
                e.printStackTrace();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return getAllPlacesData(jsonArray, userLocation, maxDistance, pricingRange, minRating);
    }

    /**
     * returns the HashMap for all JSON data (ALL locations)
     *
     * @param jsonArray all of the JSON to be parsed
     * @return List<HashMap < String, String>>  list of all HashMaps returned for each location
     */
    private List<Restaurant> getAllPlacesData(JSONArray jsonArray, Location userLocation,
                                                           int maxDistance, int pricingRange, int minRating) {
        List<Restaurant> restaurants = new ArrayList<>();
        Restaurant restaurantObj;

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                restaurantObj = getRestaurantData((JSONObject) jsonArray.get(i), userLocation, maxDistance, pricingRange, minRating);
                if (restaurantObj != null) {
                    restaurants.add(restaurantObj);
                    Log.d(TAG, "getAllPlacesData: place added");
                } else {
                    Log.d(TAG, "getAllPlacesData: place not added");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return restaurants;
    }

    /**
     * returns HashMap of the JSONObject passed for ONE location
     *
     * @param googlePlaceJson the JSON to be converted
     * @return HashMap<String, String> key values are declared as constants for easy access
     */
    private Restaurant getRestaurantData(JSONObject googlePlaceJson, Location userLocation,
                                         int maxDistance, int pricingRange, int minRating) {
        // initialize all values to default
        Float distFromUser = null;
        String name = null;
        String address = null;
        Boolean isCurrentlyOpen = null;
        Double rating = null;
        Integer totRating = null;
        Integer priceLevel = null;
        String placeId = null;

        Log.d("DataParser", "jsonobject =" + googlePlaceJson.toString());

        try {
            // name
            if (!googlePlaceJson.isNull("name")) {
                name = googlePlaceJson.getString("name");
            }

            // if too far, then return null
            distFromUser = distFromUser(userLocation, googlePlaceJson, name, maxDistance);
            if (distFromUser == null) return null;

            // address
            if (!googlePlaceJson.isNull("formatted_address")) {
                address = googlePlaceJson.getString("formatted_address");
            }

            // is open right now
            if (!googlePlaceJson.isNull("opening_hours")) {
                JSONObject openHoursObject = googlePlaceJson.getJSONObject("opening_hours");
                if (!openHoursObject.isNull("open_now")) {
                    isCurrentlyOpen = openHoursObject.getBoolean("open_now");
                }
            }

            // rating
            if (!googlePlaceJson.isNull("rating")) {
                rating = googlePlaceJson.getDouble("rating");
                // if rating too low, then return null
                if (rating < minRating) {
                    Log.d(TAG, "getRestaurantData: " + name + "'s rating is too low. Returning null");
                    return null;
                }
            }

            // total rating count
            if (!googlePlaceJson.isNull("user_ratings_total")) {
                totRating = googlePlaceJson.getInt("user_ratings_total");
            }

            // price level
            if (!googlePlaceJson.isNull("price_level")) {
                priceLevel = googlePlaceJson.getInt("price_level");
                // if price level not correct, return null
                if (priceLevel > pricingRange && pricingRange != PreferencesActivity.PREF_ANY_INT_REP) {
                    Log.d(TAG, "getRestaurantData: " + name + " has incorrect price level.");
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

        Log.d(TAG, "getRestaurantData: ---------------------------------------------------------");

        return new Restaurant(
                name, address, isCurrentlyOpen, rating, totRating, priceLevel,
                PreferencesActivity.metersToMiles(distFromUser), placeId
        );
    }

    /**
     * checks if the location is out of the acceptable range set by the user
     *
     * @param userLocation    Location object representing user's location with latitude and longitude
     * @param googlePlaceJson the JSONObject representing the data retrieved from Google Places
     * @param name            name of the restaurant
     * @return Float - distance from user in meters, null if out of range
     */
    private Float distFromUser(Location userLocation, JSONObject googlePlaceJson, String name, int maxDistance) {
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
            Log.d(TAG, "getRestaurantData: " + name + " cannot determine location. Returning false.");
            return null;
        }

        Location restLocation = new Location("");
        restLocation.setLatitude(latitude);
        restLocation.setLongitude(longitude);

        boolean closeEnough = userLocation.distanceTo(restLocation) <= maxDistance;
        if (!closeEnough) Log.d(TAG, "distFromUser: " + name + " removed b/c too far.");
        return closeEnough ? userLocation.distanceTo(restLocation) : null;
    }

    String getNextPageToken() {
        return nextPageToken;
    }
}
