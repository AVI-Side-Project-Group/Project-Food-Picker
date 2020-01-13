package me.nakukibo.projectfoodpicker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class RestaurantCardFinder extends ThemedAppCompatActivity implements GetNearbyData.ReceiveNearbyData {

    private static final String TAG = RestaurantCardFinder.class.getSimpleName();
    private static final int ERROR_PASSED_VALUE = -1;

    private View loadingView;
    private View wrapperOpen;
    private View wrapperClose;

    private ConstraintLayout noRestaurantsError;
    private ConstraintLayout buttonSet;

    private FloatingActionButton btnSwipe;
    private FloatingActionButton btnBlock;
    private FloatingActionButton btnOpenContents;
    private FloatingActionButton btnCloseContents;

    private RestaurantCard restCard1;
    private RestaurantCard restCard2;
    private RestaurantCard activeCard;

    private List<Restaurant> nearbyRestaurants;
    private LinkedList<Restaurant> placesProcessed;
    private List<Restaurant> previouslyAccessed;

    private Set jsonSet;
    private Calendar calendar;

    private boolean firstCard;
    private boolean needToSetCard;

    // data passed from PreferencesActivity.java
    private String foodType;
    private int distance;
    private int pricing;
    private int rating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_restaurant_card_finder);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        initViewVariables();
        initViewEvents();
        initViewValues();
        resetGlobalVariables();

        retrievePassedValues();
        resetRolls();
        fetchLocation();
    }

    private void initViewEvents() {

        RestaurantCard.OnOpenContents onOpenContents = () -> {
            Log.d(TAG, "initViewEvents: opening contents");

            btnOpenContents.setClickable(false);
            wrapperOpen.setVisibility(View.INVISIBLE);
            btnOpenContents.hide();

            btnCloseContents.setClickable(true);
            wrapperClose.setVisibility(View.VISIBLE);
            btnCloseContents.show();

            btnSwipe.setClickable(false);
            btnSwipe.hide();
        };

        RestaurantCard.OnCloseContents onCloseContents = () -> {
            Log.d(TAG, "initViewEvents: closing contents");

            btnCloseContents.setClickable(false);
            wrapperClose.setVisibility(View.INVISIBLE);
            btnCloseContents.hide();

            btnOpenContents.setClickable(true);
            wrapperOpen.setVisibility(View.VISIBLE);
            btnOpenContents.show();

            btnSwipe.setClickable(true);
            btnSwipe.show();
        };

        restCard1.setOnSwipeStartEvent(this::deactivateFloatingButtons);
        restCard2.setOnSwipeStartEvent(this::deactivateFloatingButtons);
        restCard1.setOnSwipeEndEvent(() -> defaultSwipeEndEvent(restCard1, restCard2));
        restCard2.setOnSwipeEndEvent(() -> defaultSwipeEndEvent(restCard2, restCard1));

        restCard1.setOnOpenContents(onOpenContents);
        restCard2.setOnOpenContents(onOpenContents);
        restCard1.setOnCloseContents(onCloseContents);
        restCard2.setOnCloseContents(onCloseContents);
    }

    private void resetGlobalVariables() {
        Places.initialize(getApplicationContext(), getResources().getString(R.string.google_maps_key));
        nearbyRestaurants = new ArrayList<>();
        placesProcessed = new LinkedList<>();
        //previouslyAccessed = new ArrayList<>();
        previouslyAccessed = getPreviouslyAccessed(); //TODO: put back in when interface with restaurant

        calendar = Calendar.getInstance();

        firstCard = true;
        needToSetCard = true;
    }

    private void initViewVariables() {
        loadingView = findViewById(R.id.restcard_loading);
        wrapperOpen = findViewById(R.id.btn_open_wrapper);
        wrapperClose = findViewById(R.id.btn_close_wrapper);
        noRestaurantsError = findViewById(R.id.no_restaurants_layout);
        buttonSet = findViewById(R.id.restcard_finder_btn_set);

        btnSwipe = findViewById(R.id.btn_roll_again);
        btnBlock = findViewById(R.id.btn_block_location);
        btnOpenContents = findViewById(R.id.btn_open_contents);
        btnCloseContents = findViewById(R.id.btn_close_contents);

        restCard1 = findViewById(R.id.restcard);
        restCard2 = findViewById(R.id.restcard2);
        restCard1.resetCard();
        restCard2.resetCard();
        activeCard = null;
    }

    private void initViewValues() {
        loadingView.setVisibility(View.VISIBLE);
        noRestaurantsError.setVisibility(View.GONE);
        buttonSet.setVisibility(View.INVISIBLE);

        deactivateFloatingButtons();

        restCard1.setDefaultValues();
        restCard2.setDefaultValues();
        restCard1.setVisibility(View.INVISIBLE);
        restCard2.setVisibility(View.INVISIBLE);
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

    private void resetRolls() {
        SharedPreferences sharedPreferences = getApplicationSharedPreferences();
        SharedPreferences.Editor editor = sharedPreferences.edit();

        int day = calendar.get(Calendar.DATE);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        String date = "" + month + day + year;

        int lastDay = sharedPreferences.getInt(getString(R.string.sp_date), 0);
        int lastMonth = sharedPreferences.getInt(getString(R.string.sp_month), 0);
        int lastYear = sharedPreferences.getInt(getString(R.string.sp_year), 0);
        String lastDate = "" + lastMonth + lastDay + lastYear;

        if(!lastDate.equals(date)){
            editor.remove(getString(R.string.sp_previously_accessed_json));
            editor.apply();
            editor.remove(getString(R.string.sp_remained_rerolls));
            editor.apply();

            editor.putInt(getString(R.string.sp_date), day);
            editor.commit();
            editor.putInt(getString(R.string.sp_month), month);
            editor.commit();
            editor.putInt(getString(R.string.sp_year), year);
            editor.commit();
        }
    }

    /**
     * fetch user current location and find the nearby restaurants based on preferences
     */
    private void fetchLocation() {
        // if no rolls remaining, then error
        if(outOfRolls()){
            showRestaurantError(R.string.restcard_finder_no_rolls);
            return;
        }

        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        // get locational information
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        Object[] dataTransfer = new Object[5];
                        Log.d(TAG, "fetchLocation: calling getnearbydata");

                        // find restaurants
                        GetNearbyData getNearbyPlacesData = new GetNearbyData(getResources().getString(R.string.google_maps_key),
                                this, nearbyRestaurants);
                        String url = getUrl(latitude, longitude);
                        dataTransfer[0] = url;
                        dataTransfer[1] = location;
                        dataTransfer[2] = distance;
                        dataTransfer[3] = pricing;
                        dataTransfer[4] = rating;
                        getNearbyPlacesData.execute(dataTransfer);
                    }
                });
    }

    /**
     * called when the GetNearbyData AsyncTask has finished retrieving the information for all of the restaurants
     * nearby
     */
    @Override
    public void onFinishNearbyFetch(){
        Log.d(TAG, "onFinishNearbyFetch: combined list has a size of " + nearbyRestaurants.size());
        logAllPlacesList(nearbyRestaurants);

        final PlacesClient placesClient = Places.createClient(this);
        LinkedList<Restaurant> unvisitedRestaurants = new LinkedList<>(removeVisited(nearbyRestaurants));
        Collections.shuffle(unvisitedRestaurants);

        Restaurant.OnFinishRetrievingImages onFinishRetrievingImages = new Restaurant.OnFinishRetrievingImages() {
            @Override
            public void onFinishRetrieve(Restaurant restaurant) {
                onFinishDetailsFetch(restaurant);
            }
        };
        FetchDetails fetchDetails = new FetchDetails(unvisitedRestaurants, onFinishRetrievingImages);
        fetchDetails.execute(placesClient);
    }

    private void onFinishDetailsFetch(Restaurant selectedRestaurant){
        if(buttonSet.getVisibility() == View.VISIBLE){
            Log.d(TAG, "onFinishDetailsFetch: button set visible");
        }else {
            Log.d(TAG, "onFinishDetailsFetch: button set not visible");
        }


        Log.d(TAG, "onFinishDetailsFetch: finished fetching details for " + selectedRestaurant.getName());

        if (firstCard) {
            firstCard = false;
            hideLoadingScreen();
            activateFloatingButtons();

            btnOpenContents.show();
            btnCloseContents.hide();
            wrapperOpen.setVisibility(View.VISIBLE);
            activeCard = restCard1;
            activeCard.setVisibility(View.VISIBLE);
            buttonSet.setVisibility(View.VISIBLE);
        }

        if(needToSetCard){
            hideLoadingScreen();
            activateFloatingButtons();
            needToSetCard = false; // hint: you can make it swipe automatically if u set this to true
            makeRoll(selectedRestaurant);
        }else {
            placesProcessed.add(selectedRestaurant);
            Log.d(TAG, "sendDetailData: adding to placesProcessed");
            logAllPlacesList(placesProcessed);
        }
    }

    private void fetchNextRestaurant(int errorTxt) {
        if(outOfRolls()){
            showRestaurantError(R.string.restcard_finder_no_rolls);
            return;
        }else if(!haveUnseenRestaurants()) {
            showRestaurantError(errorTxt);
            return;
        }

        if(placesProcessed.size() > 0){
            hideLoadingScreen();
            activateFloatingButtons();
            Restaurant nextRestaurant = placesProcessed.pop();
            makeRoll(nextRestaurant);
        } else{
            needToSetCard = true;
            showLoadingScreen();
            deactivateFloatingButtons();
        }
    }

    private void showRestaurantError(int errorTxt){
        deactivateFloatingButtons();
        hideLoadingScreen();
        hideCards();
        TextView txtvwNoRestaurants = findViewById(R.id.txtvw_no_restaurants);
        txtvwNoRestaurants.setText(getResources().getString(errorTxt));
        noRestaurantsError.setVisibility(View.VISIBLE);
    }

    private boolean haveUnseenRestaurants() {
        Set<Restaurant> potentials = removeVisited(nearbyRestaurants);
        return potentials.size() > 0;
    }

    private boolean outOfRolls(){
        int remainingRolls = getApplicationSharedPreferences().getInt(getString(R.string.sp_remained_rerolls), 10);
        Log.d(TAG, "haveUnseenRestaurants: remainingRolls = " + remainingRolls);

        return remainingRolls <= 0;
    }

    private void makeRoll(Restaurant selectedRestaurant){
        setViewValues(selectedRestaurant);

        //int remainingRolls = getApplicationSharedPreferences().getInt(getString(R.string.sp_remained_rerolls), 10);
        int remainingRolls = 10;
        Log.d(TAG, "haveUnseenRestaurants: " + remainingRolls);
        //remainingRolls--;

        SharedPreferences.Editor editor = getApplicationSharedPreferences().edit();
        editor.putInt(getString(R.string.sp_remained_rerolls), remainingRolls);
        editor.apply();
    }


    private void defaultSwipeEndEvent(RestaurantCard thisCard, RestaurantCard otherCard) {
        btnOpenContents.setClickable(false);
        btnCloseContents.setClickable(false);
        thisCard.setVisibility(View.GONE);
        thisCard.setDefaultValues();
        thisCard.resetCard();
        activeCard = otherCard;
        fetchNextRestaurant(R.string.restcard_finder_no_more_restaurants);
    }

    /**
     * set values of views to values in HashMap<String, String>
     */
    private void setViewValues(Restaurant selectedRestaurant) {
        previouslyAccessed = getPreviouslyAccessed();
        previouslyAccessed.add(selectedRestaurant);

        Log.d(TAG, "setViewValues: " + previouslyAccessed);

        savePreviouslyAccessedData(previouslyAccessed);

        Animation inAnimation = inFromRightAnimation();
        inAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                activateFloatingButtons();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        activeCard.setVisibility(View.VISIBLE);
        activeCard.setValues(selectedRestaurant);
        activeCard.setAnimation(inAnimation);
    }

    private Set<Restaurant> removeVisited(List<Restaurant> list){
        Set<Restaurant> potentials = new HashSet<>(list);
        previouslyAccessed = getPreviouslyAccessed(); //TODO: put back in when interface with restaurant
        potentials.removeAll(previouslyAccessed);
        return potentials;
    }

    // TODO: rewrite using Restaurant classes
    private void savePreviouslyAccessedData(List<Restaurant> previouslyAccessed) {
        jsonSet = new HashSet<String>();
        for(int i = 0; i < previouslyAccessed.size(); i++){
            jsonSet.add(previouslyAccessed.get(i).getJsonFromResturant());
        }

        Log.d(TAG, "savePreviouslyAccessedData: " + jsonSet);

        SharedPreferences.Editor editor = getApplicationSharedPreferences().edit();
        editor.putStringSet(getString(R.string.sp_previously_accessed_json), jsonSet);
        editor.apply();
    }

    // TODO: put back in when interface with restaurant
    private List<Restaurant> getPreviouslyAccessed() {
        List<Restaurant> restaurantList = new ArrayList<>();
        jsonSet = getApplicationSharedPreferences().getStringSet(getString(R.string.sp_previously_accessed_json), null);

        Log.d(TAG, "getPreviouslyAccessed: " + jsonSet);

        if(jsonSet != null){
            ArrayList<String> jsonList = new ArrayList<String>(jsonSet);
            for(int i = 0; i < jsonList.size(); i++) {
                Restaurant restaurant = new Restaurant(jsonList.get(i));
                restaurantList.add(restaurant);
            }
        }

        Log.d(TAG, "getPreviouslyAccessed: " + restaurantList);

        return restaurantList;
    }

    /**
     * get the google place url based on the values passed
     */
    private String getUrl(double latitude, double longitude) {
        StringBuilder googlePlaceUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/textsearch/json?");

        googlePlaceUrl.append("location=").append(latitude).append(",").append(longitude);
        googlePlaceUrl.append("&radius=").append(distance);

        if (!foodType.equals("any")) googlePlaceUrl.append("&query=").append(foodType);
        googlePlaceUrl.append("&type=restaurant");

        googlePlaceUrl.append("&field=formatted_address,name,permanently_closed,place_id," +
                "price_level,rating,user_ratings_total");

        googlePlaceUrl.append("&key=").append(getResources().getString(R.string.google_maps_key));

        Log.d(TAG, "getUrl: " + googlePlaceUrl.toString());
        return googlePlaceUrl.toString();
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

    public void swipeCard(View view) {
        activeCard.swipeCard();
    }

    public void toggleContents(View view) {
        if (view.getId() == R.id.btn_close_contents) {
            activeCard.closeContents();
        } else if (view.getId() == R.id.btn_open_contents) {
            activeCard.openContents();
        }
    }

    /**
     * finish activity and return back to preferences
     */
    public void finishCardFinder(View view) {
        finish();
    }

    private void showLoadingScreen(){
        loadingView.setVisibility(View.VISIBLE);
    }

    private void hideLoadingScreen(){
        loadingView.setVisibility(View.GONE);
    }

    private void deactivateFloatingButtons(){
        Log.d(TAG, "deactivateFloatingButtons: deactivating buttons");
        btnSwipe.setClickable(false);
        btnBlock.setClickable(false);
        btnOpenContents.setClickable(false);
        btnCloseContents.setClickable(false);

        btnSwipe.hide();
        btnBlock.hide();
        btnOpenContents.hide();
        btnCloseContents.hide();
    }

    private void activateFloatingButtons(){
        Log.d(TAG, "activateFloatingButtons: activating buttons");
        btnSwipe.setClickable(true);
        btnSwipe.show();

        btnBlock.setClickable(true);
        btnBlock.show();

        if(activeCard == null || !activeCard.isContentsVisible()){
            btnOpenContents.setClickable(true);
            btnOpenContents.show();
        }else {
            btnCloseContents.setClickable(true);
            btnCloseContents.show();
        }
    }

    private void hideCards() {
        restCard1.setVisibility(View.INVISIBLE);
        restCard2.setVisibility(View.INVISIBLE);
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
     * logs all of the nearbyPlacesList's HashMaps' name fields for debugging purposes
     */
    private void logAllPlacesList(List<Restaurant> restaurants) {
        Log.d(TAG, "logAllPlacesList: printing nearbyPlacesList---------------------");
        for (Restaurant restaurant: restaurants) {
            Log.d(TAG, "logAllPlacesList: restaurant name=" + restaurant.getName());
        }
        Log.d(TAG, "logAllPlacesList: -----------------------------------------------");
    }
}
