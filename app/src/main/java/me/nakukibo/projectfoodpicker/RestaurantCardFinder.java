package me.nakukibo.projectfoodpicker;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class RestaurantCardFinder extends AppCompatActivity implements ReceiveNearbyData, ReceiveDetailData {

    private static final String TAG = RestaurantCardFinder.class.getSimpleName();
    private static final int ERROR_PASSED_VALUE = -1;
    private static final String DEFAULT_PLACES_SEARCH_URL = "https://maps.googleapis.com/maps/api/place/textsearch/json?";
    private static final int DEFAULT_WAIT_MS = 500;

    private static List<HashMap<String, String>> nearbyPlaceListCombined; // accumulates the list over several calls
    private static List<HashMap<String, String>> previouslyAccessed; // stores the restaurants that have been accessed
    private static LinkedList<HashMap<String, String>> placesProcessed;

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
    private boolean needNextCard;

    // previous pageToken for multiple calls
    private String previousPageToken;

    private SharedPreferences sharedPreferences = FoodPicker.getSharedPreferences();
    private SharedPreferences.Editor editor = FoodPicker.getEditor();

    private Set tempSet;
    View loadingView;

    private Calendar calendar = Calendar.getInstance();
    private int today = calendar.get(calendar.DAY_OF_MONTH);
    boolean isOutOfRolls = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(FoodPicker.getSharedPreferences().getInt(getString(R.string.sp_theme), R.style.Light));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_card_finder);

        needNextCard = true;
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
        FloatingActionButton btnToggleContents = findViewById(R.id.btn_open_contents);
        OnOpenContents onOpenContents = () -> {
            btnSwipe.hide();
            btnToggleContents.setImageDrawable(getDrawable(R.drawable.up));
        };
        OnCloseContents onCloseContents = () -> {
            btnSwipe.show();
            btnToggleContents.setImageDrawable(getDrawable(R.drawable.down));
        };

        restCard1.setOnOpenContents(onOpenContents);
        restCard2.setOnOpenContents(onOpenContents);
        restCard1.setOnCloseContents(onCloseContents);
        restCard2.setOnCloseContents(onCloseContents);

        View loadingView = findViewById(R.id.restcard_loading);
        loadingView.setVisibility(View.VISIBLE);
    }

    public void goToWebsite(View view){
        Uri uriUrl = Uri.parse(activeCard.getURL());
        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
        startActivity(launchBrowser);
    }

    public void callNumber(View view){
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + activeCard.getPhoneNumber()));
        startActivity(intent);
    }

    public void openMap(View view){
        String map = "http://maps.google.co.in/maps?q=" + activeCard.getAddress();
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(map));
        startActivity(i);
    }

    /**
     * called when the NearbyData AsyncTask has finished retrieving the information for the restaurants
     * nearby
     */
    @Override
    public void sendData(List<HashMap<String, String>> nearbyPlaceList, String nextPageToken) {
        if (nearbyPlaceList == null) {
            // if null then that means the nextPageToken request failed so search again
            requestNextPageSearch(previousPageToken);
            return;
        } else {
            previousPageToken = nextPageToken;
        }

        nearbyPlaceListCombined.addAll(nearbyPlaceList);
        Log.d(TAG, "sendData: combined list has new size of " + nearbyPlaceListCombined.size());

        if (nextPageToken != null) {
            requestNextPageSearch(nextPageToken);
            return;
        }

        Log.d(TAG, "sendData: logging the combined list");
        logAllPlacesList(nearbyPlaceListCombined);

        // randomize list
        Collections.shuffle(nearbyPlaceListCombined);

        // fetch first restaurant
        fetchNextRestaurant(R.string.restcard_finder_no_restaurants, false);
    }


    /**
     * called when DetailData AsyncTask has finished fetching the detailed information
     */
    @Override
    public void sendDetailData(HashMap<String, String> selectedRestaurant) {
        previouslyAccessed.add(selectedRestaurant); // selectedRestaurant has been accessed
        savePreviouslyAccessedData(previouslyAccessed);

        if (firstCard) {
            // make loading screen invisible
            View loadingView = findViewById(R.id.restcard_loading);
            loadingView.setAnimation(outToLeftAnimation());
            activeCard = restCard1;

            restCard1.setVisibility(View.VISIBLE);
            buttonSet.setVisibility(View.VISIBLE);
            firstCard = false;
        }

        if(needNextCard){
            needNextCard = false;
            loadingView.setVisibility(View.GONE);
            setViewValues(selectedRestaurant, activeCard);
        } else {
            placesProcessed.add(selectedRestaurant);
            Log.d(TAG, "sendDetailData: adding to placesProcessed");
            logAllPlacesList(placesProcessed);
        }

        Set<HashMap<String, String>> potentials = removeVisited(nearbyPlaceListCombined);
        removeProcessed(potentials);
        // if zero then all of the potentials are already processed
        if(potentials.size() == 0) return;
        rollNextRestaurant(potentials);
    }

    public void swipeCard(View view) {
        activeCard.swipeCard();
    }

    public void toggleContents(View view) {
        if (activeCard.isContentsVisible()) {
            activeCard.closeContents();
        } else {
            activeCard.openContents();
        }
    }

    /**
     * finish activity and return back to preferences
     */
    public void finishCardFinder(View view) {
        finish();
    }

    private void fetchNextRestaurant(int errorTxt, boolean needSetCard) {

        if(needSetCard && placesProcessed.size() > 0){
            if(haveRolls()) {
                makeRoll();
                setViewValues(placesProcessed.pop(), activeCard);
                return;
            } else {
                isOutOfRolls = true;
            }
        } else if (placesProcessed.size() == 0){
            loadingView.setVisibility(View.VISIBLE);
            needNextCard = true;
        }
        if(isOutOfRolls) {
            showRestaurantError(R.string.restcard_finder_no_rolls);
            return;
        }
        boolean success = getNextRestaurantDetails();
        if(isOutOfRolls) {
            showRestaurantError(R.string.restcard_finder_no_rolls);
            return;
        }
        if (!success) {
            showRestaurantError(errorTxt);
        }
    }

    private void showRestaurantError(int errorTxt){
        loadingView.setVisibility(View.GONE);
        TextView txtvwNoRestaurants = findViewById(R.id.txtvw_no_restaurants);
        txtvwNoRestaurants.setText(getResources().getString(errorTxt));
        turnOffBothCards();
        noRestaurantsError.setVisibility(View.VISIBLE);
    }

    /**
     * get a random restaurant from the List of HashMaps and find the detailed data on it
     *
     * @return boolean - true if successfully retrieved a random restaurant
     *                 - false otherwise (eg 0 possible restaurants)
     */
    private boolean getNextRestaurantDetails() {
        Set<HashMap<String, String>> potentials = removeVisited(nearbyPlaceListCombined);
        // if zero then user has went through all restaurants
        if (potentials.size() == 0) return false;

        if(haveRolls()) makeRoll();
        else {
            isOutOfRolls = true;
            return false;
        }

        removeProcessed(potentials);
        // if zero then all of the potentials are already processed
        if(potentials.size() == 0) return true;

        rollNextRestaurant(potentials);

        return true;
    }

    private void rollNextRestaurant(Set<HashMap<String, String>> potentials){
        List<HashMap<String, String>> potentialsList = new ArrayList<>(potentials);
        HashMap<String, String> selectedRestaurant = potentialsList.get(0);

        Object[] dataTransfer = new Object[5];

        // find restaurants
        DetailData getDetailData = new DetailData(selectedRestaurant, this);
        String url = getDetailsUrl(selectedRestaurant.get(DataParser.DATA_KEY_PLACE_ID));
        dataTransfer[0] = url;
        getDetailData.execute(dataTransfer);

        makeRoll();
        Log.d(TAG, "getNextRestaurantDetails: " + sharedPreferences.getInt(getString(R.string.sp_remained_rerolls), 10));
    }

    private boolean haveRolls(){
        int remainedRerolls = sharedPreferences.getInt(getString(R.string.sp_remained_rerolls), 10);
        Log.d(TAG, "getNextRestaurantDetails: remainingRolls = " + remainedRerolls);
        //TODO: remove the comment out
        return remainedRerolls > 0;
    }

    private void makeRoll(){
        int remainedRerolls = sharedPreferences.getInt(getString(R.string.sp_remained_rerolls), 10);
        remainedRerolls --;
        Log.d(TAG, "getNextRestaurantDetails: " + remainedRerolls);
        editor.putInt(getString(R.string.sp_remained_rerolls), remainedRerolls);
        editor.commit();
    }

    private Set<HashMap<String, String>> removeVisited(List<HashMap<String, String>> list){
        Set<HashMap<String, String>> potentials = new HashSet<>(list);
        previouslyAccessed = getPreviouslyAccessed();
        potentials.removeAll(previouslyAccessed);
        return potentials;
    }

    private void removeProcessed(Set<HashMap<String, String>> set){
        set.removeAll(placesProcessed);
    }

    private List<HashMap<String, String>> getPreviouslyAccessed() {
        tempSet = sharedPreferences.getStringSet(getString(R.string.sp_previously_accessed), null);
        List<HashMap<String, String>> tempList;
        if (tempSet == null) {
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
     * initialize the cards and certain global variables
     * cards will also initialize their setOnTouchListener and be set to View.GONE
     */
    private void initViews() {
        noRestaurantsError = findViewById(R.id.no_restaurants_layout);
        noRestaurantsError.setVisibility(View.GONE);
        buttonSet = findViewById(R.id.restcard_finder_btn_set);
        buttonSet.setVisibility(View.GONE);

        nearbyPlaceListCombined = new ArrayList<>();
        placesProcessed = new LinkedList<>();
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
        FloatingActionButton btnToggleContents = findViewById(R.id.btn_open_contents);
        OnOpenContents onOpenContents = () -> {
            btnSwipe.hide();
            btnToggleContents.setImageDrawable(getDrawable(R.drawable.up));
        };
        OnCloseContents onCloseContents = () -> {
            btnSwipe.show();
            btnToggleContents.setImageDrawable(getDrawable(R.drawable.down));
        };

        restCard1.setOnOpenContents(onOpenContents);
        restCard2.setOnOpenContents(onOpenContents);
        restCard1.setOnCloseContents(onCloseContents);
        restCard2.setOnCloseContents(onCloseContents);

        loadingView = findViewById(R.id.restcard_loading);
        loadingView.setVisibility(View.VISIBLE);
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
    private String getUrlNextPage(String nextPageToken) {
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
     * restore values passed in from PreferencesActivity.java
     */
    private void retrievePassedValues() {
        foodType = getIntent().getStringExtra(PreferencesActivity.PREF_INTENT_FOOD_TYPE);
        rating = getIntent().getIntExtra(PreferencesActivity.PREF_INTENT_RATING, ERROR_PASSED_VALUE);
        distance = getIntent().getIntExtra(PreferencesActivity.PREF_INTENT_DISTANCE, ERROR_PASSED_VALUE);
        pricing = getIntent().getIntExtra(PreferencesActivity.PREF_INTENT_PRICING, ERROR_PASSED_VALUE);
    }

    private void savePreviouslyAccessedData(List<HashMap<String, String>> previouslyAccessed) {
        tempSet = new HashSet(previouslyAccessed);
        editor.putStringSet(getString(R.string.sp_previously_accessed), tempSet);
        editor.commit();
    }

    private void defaultSwipeEvent(RestaurantCard thisCard, RestaurantCard otherCard) {
        thisCard.setVisibility(View.INVISIBLE);
        thisCard.setDefaultValues();
        otherCard.setVisibility(View.VISIBLE);
        activeCard = otherCard;

        fetchNextRestaurant(R.string.restcard_finder_no_more_restaurants, true);
    }

    private void resetValues() {
        Log.d(TAG, "resetValues: " + today);
        int lastDay = sharedPreferences.getInt(getString(R.string.sp_date), 0);
        Log.d(TAG, "resetValues: " + lastDay);
        if (today != lastDay) {
            editor.remove(getString(R.string.sp_previously_accessed));
            editor.commit();
            editor.remove(getString(R.string.sp_remained_rerolls));
            editor.commit();
            Log.d(TAG, "resetValues: " + "removed");
        }
        editor.putInt(getString(R.string.sp_date), today);
        editor.commit();
    }

    private void turnOffBothCards() {
        restCard1.setVisibility(View.GONE);
        restCard2.setVisibility(View.GONE);
    }

    /**
     * logs all of the nearbyPlacesList's HashMaps' name fields for debugging purposes
     */
    private void logAllPlacesList(List<HashMap<String, String>> nearbyPlaceList) {
        Log.d(TAG, "logAllPlacesList: printing nearbyPlacesList---------------------");
        for (HashMap<String, String> placesInfo : nearbyPlaceList) {
            Log.d(TAG, "logAllPlacesList: restaurant name=" + placesInfo.get(DataParser.DATA_KEY_NAME));
        }
        Log.d(TAG, "logAllPlacesList: -----------------------------------------------");
    }

    /**
     * Animation for a card to move from in card to out of screen
     */
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
     */
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
     */
    private void setViewValues(HashMap<String, String> selectedRestaurant, RestaurantCard card) {
        card.setValues(selectedRestaurant);
        activeCard.setAnimation(inFromRightAnimation());
    }
}
