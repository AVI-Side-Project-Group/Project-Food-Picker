package me.nakukibo.projectfoodpicker;

import android.content.pm.ActivityInfo;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class RestaurantCardFinder extends AppCompatActivity implements ReceiveNearbyData, ReceiveDetailData{

    private static final String TAG = RestaurantCardFinder.class.getSimpleName();
    private static final int ERROR_PASSED_VALUE = -1;
    private static final String DEFAULT_PLACES_SEARCH_URL = "https://maps.googleapis.com/maps/api/place/textsearch/json?";
    private static final int DEFAULT_WAIT_MS = 500;

    private static List<HashMap<String, String>> nearbyPlaceListCombined; // accumulates the list over several calls
    private static List<HashMap<String, String>> previouslyAccessed; // stores the restaurants that have been accessed

    // data passed from PreferencesActivity.java
    private String foodType;
    private int distance;
    private int pricing;
    private int rating;

    private RestaurantCard restCard1;
    private RestaurantCard restCard2;
    private RestaurantCard activeCard = null;

    private ConstraintLayout noRestaurantsError;
    private ConstraintLayout buttonSet;
    private boolean firstCard;

    // previous pageToken for multiple calls
    private String previousPageToken;

    private SharedPreferences sharedPreferences = FoodPicker.getSharedPreferences();
    private SharedPreferences.Editor editor = FoodPicker.getEditor();

    private Set tempSet;

    private Calendar calendar = Calendar.getInstance();
    private int today = calendar.get(calendar.DAY_OF_MONTH);
    private int remainedRerolls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(FoodPicker.getSharedPreferences().getInt(getString(R.string.sp_theme), R.style.Light));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_card_finder);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        initViews();
        retrievePassedValues();
        fetchLocation(null);
        resetValues();
    }

    private void resetValues() {
        Log.d(TAG, "resetValues: " + today);
        int lastDay = sharedPreferences.getInt(getString(R.string.sp_date), 0);
        Log.d(TAG, "resetValues: " + lastDay);
        if(today != lastDay){
            editor.remove(getString(R.string.sp_previously_accessed));
            editor.commit();
            editor.remove(getString(R.string.sp_remained_rerolls));
            editor.commit();
            Log.d(TAG, "resetValues: " + "removed");
        }
        editor.putInt(getString(R.string.sp_date), today);
        editor.commit();
    }

    /**
     * initialize the cards and certain global variables
     * cards will also initialize their setOnTouchListener and be set to View.GONE
     */
    private void initViews() {
        noRestaurantsError = findViewById(R.id.no_restaurants_layout);
        noRestaurantsError.setVisibility(View.GONE);
        buttonSet = findViewById(R.id.restcard_finder_btn_set);
        buttonSet.setVisibility(View.GONE);

        nearbyPlaceListCombined = new ArrayList<>();
        previouslyAccessed = getPreviouslyAccessed();

        firstCard = true;

        restCard1 = findViewById(R.id.restcard);
        restCard2 = findViewById(R.id.restcard2);
        restCard1.setDefaultValues();
        restCard2.setDefaultValues();
        restCard1.setVisibility(View.GONE);
        restCard2.setVisibility(View.GONE);

        restCard1.setOnSwipeEvent(() -> defaultSwipeEvent(restCard1, restCard2));
        restCard2.setOnSwipeEvent(() -> defaultSwipeEvent(restCard2, restCard1));

        FloatingActionButton btnSwipe = findViewById(R.id.btn_roll_again);

        OnOpenContents onOpenContents = btnSwipe::hide;
        OnCloseContents onCloseContents = btnSwipe::show;

        restCard1.setOnOpenContents(onOpenContents);
        restCard2.setOnOpenContents(onOpenContents);
        restCard1.setOnCloseContents(onCloseContents);
        restCard2.setOnCloseContents(onCloseContents);

        View loadingView = findViewById(R.id.restcard_loading);
        loadingView.setVisibility(View.VISIBLE);
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

        nearbyPlaceListCombined.addAll(nearbyPlaceList);
        Log.d(TAG, "sendData: combined list has new size of " + nearbyPlaceListCombined.size());

        if(nextPageToken != null){
            requestNextPageSearch(nextPageToken);
            return;
        }

        Log.d(TAG, "sendData: logging the combined list");
        logAllPlacesList(nearbyPlaceListCombined);

        View loadingView = findViewById(R.id.restcard_loading);
        loadingView.setAnimation(outToLeftAnimation());
        activeCard = restCard1;
        loadingView.setVisibility(View.GONE);

        attemptRandomRestaurant(R.string.restcard_finder_no_restaurants);
    }


    /**
     * called when DetailData AsyncTask has finished fetching the detailed information
     */
    @Override
    public void sendDetailData(HashMap<String, String> selectedRestaurant) {
        previouslyAccessed.add(selectedRestaurant); // selectedRestaurant has been accessed
        savePreviouslyAccessedData(previouslyAccessed);

        if(firstCard) {
            restCard1.setVisibility(View.VISIBLE);
            buttonSet.setVisibility(View.VISIBLE);
            firstCard = false;
        }

        setViewValues(selectedRestaurant, activeCard);
    }

    private void savePreviouslyAccessedData(List<HashMap<String, String>> previouslyAccessed) {
        tempSet = new HashSet(previouslyAccessed);
        editor.putStringSet(getString(R.string.sp_previously_accessed), tempSet);
        editor.commit();
    }

    /**
     * finish activity and return back to preferences
     * */
    public void finishCardFinder(View view){
        finish();
    }

    public void swipeCard(View view){
        activeCard.swipeCard();
    }

    public void toggleContents(View view){
        if(activeCard.isContentsVisible()){
            activeCard.closeContents();
        }else {
            activeCard.openContents();
        }
    }

    private void turnOffBothCards(){
        restCard1.setVisibility(View.GONE);
        restCard2.setVisibility(View.GONE);
    }

    private boolean attemptRandomRestaurant(int errorTxt){
        boolean success = getRandomRestaurant();
        if(!success){
            TextView txtvwNoRestaurants = findViewById(R.id.txtvw_no_restaurants);
            txtvwNoRestaurants.setText(getResources().getString(errorTxt));
            turnOffBothCards();
            noRestaurantsError.setVisibility(View.VISIBLE);
        }
        return success;
    }

    /**
     * get a random restaurant from the List of HashMaps and find the detailed data on it
     * @return boolean - true if successfully retrieved a random restaurant
     *                 - false otherwise (eg 0 possible restaurants)
     * */
    private boolean getRandomRestaurant(){
        Set<HashMap<String, String>> potentials = new HashSet<>(nearbyPlaceListCombined);
        previouslyAccessed = getPreviouslyAccessed();
        potentials.removeAll(previouslyAccessed);

        if (potentials.size() == 0) return false;

        remainedRerolls = sharedPreferences.getInt(getString(R.string.sp_remained_rerolls), 10);
        Log.d(TAG, "getRandomRestaurant: remainingRolls = " + remainedRerolls);

        //TODO: remove the comment out
        if(remainedRerolls <= 0) return false;

        List<HashMap<String, String>> potentialsList = new ArrayList<>(potentials);
        int index = new Random().nextInt(potentialsList.size());
        HashMap<String, String> selectedRestaurant = potentialsList.get(index);

        Object[] dataTransfer = new Object[5];

        // find restaurants
        DetailData getDetailData = new DetailData(selectedRestaurant, this);
        String url = getDetailsUrl(selectedRestaurant.get(DataParser.DATA_KEY_PLACE_ID));
        dataTransfer[0] = url;
        getDetailData.execute(dataTransfer);

        remainedRerolls--;
        Log.d(TAG, "getRandomRestaurant: " + remainedRerolls);
        editor.putInt(getString(R.string.sp_remained_rerolls), remainedRerolls);
        editor.commit();
        Log.d(TAG, "getRandomRestaurant: " + sharedPreferences.getInt(getString(R.string.sp_remained_rerolls), 10));

        return true;
    }

    private List<HashMap<String, String>> getPreviouslyAccessed() {
        tempSet = sharedPreferences.getStringSet(getString(R.string.sp_previously_accessed), null);
        List<HashMap<String, String>> tempList;
        if(tempSet == null){
            tempList = new ArrayList<>();
        } else {
            tempList = new ArrayList<>(tempSet);
        }

        return tempList;
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

    private void defaultSwipeEvent(RestaurantCard thisCard, RestaurantCard otherCard){
        thisCard.setVisibility(View.INVISIBLE);
        thisCard.setDefaultValues();
        otherCard.setVisibility(View.VISIBLE);
        activeCard = otherCard;

        if(attemptRandomRestaurant(R.string.restcard_finder_no_more_restaurants)) {
            otherCard.setAnimation(inFromRightAnimation());
        }
    }

    /**
     * Animation for a card to move from in card to out of screen
     * */
    static Animation outToLeftAnimation() {
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
     * Animation for a card to move from out of screen to into screen
     * */
    private static Animation inFromRightAnimation() {
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
     * set values of views to values in HashMap<String, String>
     * */
    private void setViewValues(HashMap<String, String> selectedRestaurant, RestaurantCard card) {
        card.setValues(selectedRestaurant);
    }
}
