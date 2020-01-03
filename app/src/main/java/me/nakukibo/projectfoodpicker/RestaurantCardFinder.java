package me.nakukibo.projectfoodpicker;

import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class RestaurantCardFinder extends AppCompatActivity implements ReceiveNearbyData, ReceiveDetailData,
        View.OnTouchListener {

    private static final String TAG = RestaurantCardFinder.class.getSimpleName();
    private static final int ERROR_PASSED_VALUE = -1;
    private static final String DEFAULT_PLACES_SEARCH_URL = "https://maps.googleapis.com/maps/api/place/textsearch/json?";
    private static final int DEFAULT_WAIT_MS = 500;

    // accumulates the list over several calls
    private static List<HashMap<String, String>> nearbyPlaceListCombined;

    // data passed from PreferencesActivity.java
    private String foodType;
    private int distance;
    private int pricing;
    private int rating;

    // values for the RestaurantCard
    private float restCardStartX;
    private float restCardStartY;
    private float restCardDx = 0;
    private float restCardDy = 0;

    private RestaurantCard restCard1;
    private RestaurantCard restCard2;
    private boolean firstCard;

    // previous pageToken for multiple calls
    private String previousPageToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_card_finder);

        initCards();
        retrievePassedValues();
        fetchLocation(null);
    }

    /**
     * initialize the cards and certain global variables
     * cards will also initialize their setOnTouchListener and be set to View.GONE
     */
    private void initCards() {
        nearbyPlaceListCombined = null;
        firstCard = true;

        restCard1 = findViewById(R.id.restcard);
        restCard2 = findViewById(R.id.restcard2);
        restCard1.setDefaultValues();
        restCard2.setDefaultValues();
        restCard1.setVisibility(View.GONE);
        restCard2.setVisibility(View.GONE);

        restCardStartX = restCard1.getX();
        restCardStartY = restCard1.getY();

        restCard1.setOnTouchListener(this);
        restCard2.setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        // if invisible or gone, then no response
        if (view.getVisibility() == View.GONE || view.getVisibility() == View.INVISIBLE)
            return true;

        float width = view.getWidth();
        float newX = motionEvent.getRawX() + restCardDx;
        float newY = motionEvent.getRawY() + restCardDy;

        switch(motionEvent.getAction()){
            case MotionEvent.ACTION_DOWN:
                restCardDx = view.getX() - motionEvent.getRawX();
                restCardDy = view.getY() - motionEvent.getRawY();
                return true;

            case MotionEvent.ACTION_MOVE:
                view.setX(newX);
                view.setY(newY);
                break;

            case MotionEvent.ACTION_UP:
                // if pass threshold, then new card, else place card back in center
                if (newX <= restCardStartX - width / 2) {
                    RestaurantCard thisCard = (RestaurantCard) view;

                    Log.d(TAG, "initCards: card is swiped left");

                    thisCard.startAnimation(outToLeftAnimation());
                    thisCard.setVisibility(View.GONE);
                    thisCard.setDefaultValues();

                    RestaurantCard otherCard;
                    if(thisCard.getId() == restCard1.getId()){
                        otherCard = restCard2;
                    }else {
                        otherCard = restCard1;
                    }

                    // get new restaurant info on other card
                    otherCard.setVisibility(View.VISIBLE);
                    getRandomRestaurant();
                    otherCard.setAnimation(inFromRightAnimation());
                }

                view.setX(restCardStartX);
                view.setY(restCardStartY);

                return true;
        }
        return false;
    }

    /**
     * called when the NearbyData AsyncTask has finished retrieving the information for the restaurants
     * nearby
     */
    @Override
    public void sendData(List<HashMap<String, String>> nearbyPlaceList, String nextPageToken) {
        if(nearbyPlaceList == null){
            // if null then that means the nextPageToken request failed so search again
            requestNextPageSearch(previousPageToken);
            return;
        } else {
            previousPageToken = nextPageToken;
        }

        if(nearbyPlaceListCombined == null) {
            nearbyPlaceListCombined = nearbyPlaceList;
        } else {
            nearbyPlaceListCombined.addAll(nearbyPlaceList);
            Log.d(TAG, "sendData: combined list has new size of " + nearbyPlaceListCombined.size());
        }

        if(nextPageToken != null){
            requestNextPageSearch(nextPageToken);
            return;
        }

        Log.d(TAG, "sendData: logging the combined list");
        logAllPlacesList(nearbyPlaceListCombined);

        View loadingView = findViewById(R.id.restcard_loading);
        loadingView.setAnimation(outToLeftAnimation());
        loadingView.setVisibility(View.GONE);
        getRandomRestaurant();
    }


    /**
     * called when DetailData AsyncTask has finished fetching the detailed information
     */
    @Override
    public void sendDetailData(HashMap<String, String> selectedRestaurant) {
        if(firstCard) {
            restCard1.setVisibility(View.VISIBLE);
            firstCard = false;
        }

        RestaurantCard selectedCard;
        if(restCard1.getVisibility() == View.VISIBLE){
            selectedCard = restCard1;
        }else{
            selectedCard = restCard2;
        }

        setViewValues(selectedRestaurant, selectedCard);
    }

    /**
     * get a random restaurant from the List of HashMaps and find the detailed data on it
     * */
    private void getRandomRestaurant(){
        int index = new Random().nextInt(nearbyPlaceListCombined.size());
        HashMap<String, String> selectedRestaurant = nearbyPlaceListCombined.get(index);

        Object[] dataTransfer = new Object[5];

        // find restaurants
        DetailData getDetailData = new DetailData(selectedRestaurant, this);
        String url = getDetailsUrl(selectedRestaurant.get(DataParser.DATA_KEY_PLACE_ID));
        dataTransfer[0] = url;
        getDetailData.execute(dataTransfer);
    }

    /**
     * fetch user current location and find the nearby restaurants based on preferences
     */
    private void fetchLocation(String customUrl) {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        // get locational information
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        Object[] dataTransfer = new Object[5];

                        // find restaurants
                        NearbyData getNearbyPlacesData = new NearbyData(RestaurantCardFinder.this);
                        String url = getUrl(latitude, longitude);
                        dataTransfer[0] = customUrl == null ? url : customUrl;
                        dataTransfer[1] = location;
                        dataTransfer[2] = distance;
                        dataTransfer[3] = pricing;
                        dataTransfer[4] = rating;
                        getNearbyPlacesData.execute(dataTransfer);
                    }
                });
    }

    /**
     * send search request with nextPageToken
     */
    private void requestNextPageSearch(String nextPageToken) {
        SystemClock.sleep(DEFAULT_WAIT_MS);
        String nextPage = getUrlNextPage(nextPageToken);
        Log.d(TAG, "sendData: search with url=" + nextPage);
        fetchLocation(nextPage);
    }

    /**
     * get the google place url based on nextPageToken only
     */
    private String getUrlNextPage(String nextPageToken){
        String googlePlaceUrl = DEFAULT_PLACES_SEARCH_URL;
        googlePlaceUrl += "pagetoken=" + nextPageToken;
        googlePlaceUrl += "&key=" + getResources().getString(R.string.google_maps_key);
        return googlePlaceUrl;
    }

    /**
     * get the google place url based on the values passed
     */
    private String getUrl(double latitude, double longitude) {
        StringBuilder googlePlaceUrl = new StringBuilder(DEFAULT_PLACES_SEARCH_URL);

        googlePlaceUrl.append("location=").append(latitude).append(",").append(longitude);
        googlePlaceUrl.append("&radius=").append(distance);

        if (!foodType.equals("any")) googlePlaceUrl.append("&query=").append(foodType);
        googlePlaceUrl.append("&type=restaurant");

        googlePlaceUrl.append("&field=formatted_address,name,permanently_closed,photos,place_id," +
                "price_level,rating,user_ratings_total");

        googlePlaceUrl.append("&key=").append(getResources().getString(R.string.google_maps_key));

        Log.d(TAG, "getUrl: " + googlePlaceUrl.toString());
        return googlePlaceUrl.toString();
    }

    /**
     * return the url for detailed informational fetch
     *
     * @param placeId place_id for the location where the data is to be fetched
     * @return String the url to be used to fetch the said data (phone number, opening hours, website)
     */
    private String getDetailsUrl(String placeId) {
        String googlePlaceUrl = "https://maps.googleapis.com/maps/api/place/details/json?";
        googlePlaceUrl += "place_id=" + placeId;
        googlePlaceUrl += "&fields=formatted_phone_number,opening_hours,website";
        googlePlaceUrl += "&key=" + getResources().getString(R.string.google_maps_key);

        return googlePlaceUrl;
    }

    /**
     * logs all of the nearbyPlacesList's HashMaps' name fields for debugging purposes
     * */
    private void logAllPlacesList(List<HashMap<String, String>> nearbyPlaceList){
        Log.d(TAG, "logAllPlacesList: printing nearbyPlacesList---------------------");
        for(HashMap<String, String> placesInfo : nearbyPlaceList){
            Log.d(TAG, "logAllPlacesList: restaurant name=" + placesInfo.get(DataParser.DATA_KEY_NAME));
        }
        Log.d(TAG, "logAllPlacesList: -----------------------------------------------");
    }

    /**
     * restore values passed in from PreferencesActivity.java
     */
    private void retrievePassedValues() {
        foodType = getIntent().getStringExtra(PreferencesActivity.PREF_INTENT_FOOD_TYPE);
        rating = getIntent().getIntExtra(PreferencesActivity.PREF_INTENT_RATING, ERROR_PASSED_VALUE);
        distance = getIntent().getIntExtra(PreferencesActivity.PREF_INTENT_DISTANCE, ERROR_PASSED_VALUE);
        pricing = getIntent().getIntExtra(PreferencesActivity.PREF_INTENT_PRICING, ERROR_PASSED_VALUE);
    }

    /**
     * Animation for a card to move from out of screen to into screen
     * */
    private Animation inFromRightAnimation() {
        Animation inFromRight = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, +1.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f);
        inFromRight.setDuration(400);
        inFromRight.setInterpolator(new AccelerateInterpolator());
        return inFromRight;
    }

    /**
     * Animation for a card to move from in card to out of screen
     * */
    private Animation outToLeftAnimation() {
        Animation outToLeft = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, -1.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f);
        outToLeft.setDuration(200);
        outToLeft.setInterpolator(new AccelerateInterpolator());
        return outToLeft;
    }

    /**
     * set values of views to values in HashMap<String, String>
     */
    private void setViewValues(HashMap<String, String> selectedRestaurant, RestaurantCard card) {
        card.setValues(selectedRestaurant);
    }
}
