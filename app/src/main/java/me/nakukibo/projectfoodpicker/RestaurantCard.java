package me.nakukibo.projectfoodpicker;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RestaurantCard extends ScrollView {
    private static final String TAG = RestaurantCard.class.getSimpleName();

    private TextView txtvwName;
    private TextView txtvwOpenNow;
    private TextView txtvwDistance;
    private ImageView restPhoto;
    private RestaurantCardContents restaurantCardContents;

    private OnSwipeEnd onSwipeEndEvent;
    private OnOpenContents onOpenContents;
    private OnCloseContents onCloseContents;

    private View viewLastPhoto;
    private View viewNextPhoto;
    private View viewOpenContents;

    // values for swiping
    private float restCardStartX;
    private float restCardStartY;
    private float restCardDx = 0;
    private float restCardDy = 0;
    private boolean isBeingSwiped = false;

    private Context context;

    private List<Photo> photos;
    private int cImage;
    private OnSwipeStart onSwipeStartEvent;

//    private static final String TAG = RestaurantCard.class.getSimpleName();

    public RestaurantCard(@NonNull Context context) {
        this(context, null);
    }

    public RestaurantCard(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RestaurantCard(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;

        onOpenContents = null;
        onCloseContents = null;
        onSwipeEndEvent = null;
        onSwipeStartEvent = null;
        initCard(context, attrs);
        initSwipeVariables();
        initEvents();
    }

    /**
     * set restaurant card to values passed as HashMap<String, String> with keys used by DataParser class
     */
    void setValues(Restaurant selectedRestaurant){
        Log.d(TAG, "setValues: setting values for restaurant " + selectedRestaurant.getName());
        setValues(
                selectedRestaurant.getName(),
                selectedRestaurant.getOpen(),
                selectedRestaurant.getDistanceMiles(),
                selectedRestaurant.getPhotos(),
                String.format(Locale.US, "%.2f stars (%d)", selectedRestaurant.getRating(), selectedRestaurant.getTotRating()),
                selectedRestaurant.getPriceLevel(),
                selectedRestaurant.getAddress(),
                selectedRestaurant.getPhoneNumber(),
                selectedRestaurant.getWebsite(),
                selectedRestaurant.getWeekdayTextConcatenated()
        );
    }

    /**
     * set restaurant card to default values
     */
    void setDefaultValues(){
        setValues(
                getResources().getString(R.string.restcard_default_name),
                true,
                12.20,
                null,
                getResources().getString(R.string.restcard_default_rating),
                1,
                getResources().getString(R.string.restcard_default_address),
                getResources().getString(R.string.restcard_default_phone_number),
                getResources().getString(R.string.restcard_default_website),
                getResources().getString(R.string.restcard_default_hours));
    }

    /**
     * set restaurant card to values passed
     */
    private void setValues(String name, Boolean openNow, Double distanceMiles, List<Photo> photos,
                           String rating, Integer pricing, String address, String phoneNumber,
                           String website, String hours){
        txtvwName.setText(name);

        String openNowText;
        if(openNow == null){
            openNowText = "Unsure if open";
        } else if(openNow){
            openNowText = "Open Now!";
        } else {
            openNowText = "Closed";
        }

        txtvwOpenNow.setText(openNowText);
        txtvwDistance.setText(distanceMiles == null? "Unknown Distance" : String.format(Locale.US, "%.2f miles", distanceMiles));

        this.photos = photos;
        cImage = 0;
        setPhoto();
        restaurantCardContents.setValues(rating, pricing, address, phoneNumber, website, hours);
    }

    private void setPhoto(){
        if(photos == null) {
            restPhoto.setImageDrawable(getResources().getDrawable(R.drawable.ic_launcher_background));
        }else {
            restPhoto.setImageBitmap(photos.get(cImage).getBitmap());
        }
    }

    private void initSwipeVariables() {
        restCardStartX = this.getX();
        restCardStartY = this.getY();
    }

    /**
     * initialize the views in the card to specific values
     */
    private void initCard(@NonNull Context context, @Nullable AttributeSet attrs) {
        inflate(context, R.layout.restaurant_card, this);

        txtvwName = findViewById(R.id.txtvw_title_name);
        txtvwOpenNow = findViewById(R.id.txtvw_title_open_now);
        txtvwDistance = findViewById(R.id.txtvw_title_distance);
        restPhoto = findViewById(R.id.imgvw_title_restaurant);
        restaurantCardContents = findViewById(R.id.restcard_contents);

        viewLastPhoto = findViewById(R.id.view_last_image);
        viewNextPhoto = findViewById(R.id.view_next_image);
        viewOpenContents = findViewById(R.id.view_open_contents);

        restaurantCardContents.setVisibility(GONE);

        // set view values to attribute values
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.RestaurantCard);
//        setValues(
//                attributes.getString(R.styleable.RestaurantCard_name),
//                true,
//                12.20,
//                R.styleable.RestaurantCard_android_src,
//                String.format(Locale.US, "%1.1f stars", attributes.getFloat(R.styleable.RestaurantCard_rating, 1f)),
//                String.format(Locale.US, "Price Level: %d", attributes.getInteger(R.styleable.RestaurantCard_price_level, 1)),
//                attributes.getString(R.styleable.RestaurantCard_address),
//                attributes.getString(R.styleable.RestaurantCard_phone_number),
//                attributes.getString(R.styleable.RestaurantCard_website),
//                attributes.getString(R.styleable.RestaurantCard_hours)
//        );

        attributes.recycle();
    }

    private void initEvents() {
        viewLastPhoto.setOnTouchListener((view, motionEvent) -> {
            Log.d(TAG, "initEvents: viewLastPhoto touch event");

            if(cannotPerformEvents()) return true;
            boolean isSwiped = checkForSwipe(motionEvent);

            if(motionEvent.getAction() == MotionEvent.ACTION_UP && !isSwiped){
                Log.d(TAG, "initEvents: viewing last image");
                if(cImage > 0) {
                    cImage --;
                    setPhoto();
                }
            }

            return true;
        });

        viewNextPhoto.setOnTouchListener((view, motionEvent) -> {
            Log.d(TAG, "initEvents: viewNextPhoto touch event");

            if(cannotPerformEvents()) return true;
            boolean isSwiped = checkForSwipe(motionEvent);

            if(motionEvent.getAction() == MotionEvent.ACTION_UP && !isSwiped){
                Log.d(TAG, "initEvents: viewing next image");
                if(cImage < photos.size() - 1) {
                    cImage ++;
                    setPhoto();
                }
            }

            return true;
        });

        viewOpenContents.setOnTouchListener((view, motionEvent) -> {
            Log.d(TAG, "initEvents: viewOpenContents touch event");

            if(cannotPerformEvents()) return true;
            boolean isSwiped = checkForSwipe(motionEvent);

            if(motionEvent.getAction() == MotionEvent.ACTION_UP && !isSwiped){
                Log.d(TAG, "initEvents: opening contents");
                if(!isContentsVisible()) {
                    openContents();
                } else {
                    closeContents();
                }
            }

            return true;
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if(isContentsVisible()){
            return super.onTouchEvent(ev);
        }  else {
            Log.d(TAG, "initEvents: RestaurantCard touch event");

            if(cannotPerformEvents()) return true;
            checkForSwipe(ev);
            return true;
        }
    }

    public void openContents(){
        restaurantCardContents.setVisibility(VISIBLE);
        if(onOpenContents != null) onOpenContents.onOpen();
        else Log.d(TAG, "openContents: onOpenContents is null");
    }

    public void closeContents(){
        restaurantCardContents.setVisibility(GONE);
        if(onCloseContents != null) onCloseContents.onClose();
        else Log.d(TAG, "closeContents: onCloseContents is null");
    }

    private boolean cannotPerformEvents(){
        return this.getVisibility() != View.VISIBLE;
    }

    private boolean checkForSwipe(MotionEvent motionEvent){
        if(isContentsVisible()) return false;

        final float minDistance = 30f;

        float width = this.getWidth();
        float newX = motionEvent.getRawX() + restCardDx;
        float newY = motionEvent.getRawY() + restCardDy;

        switch(motionEvent.getAction()){
            case MotionEvent.ACTION_DOWN:
                restCardDx = this.getX() - motionEvent.getRawX();
                restCardDy = this.getY() - motionEvent.getRawY();
                break;

            case MotionEvent.ACTION_MOVE:
                float dx = newX - restCardStartX;
                float dy = newY - restCardStartY;

                if(dx*dx + dy*dy >= minDistance*minDistance) {
                    isBeingSwiped = true;
                }

                if(isBeingSwiped){
                    this.setX(newX);
                    this.setY(newY);
                }
                break;

            case MotionEvent.ACTION_UP:
                // if pass threshold, then new card, else place card back in center
                if(isBeingSwiped){

                    if (newX <= restCardStartX - width/2) {
                        swipeCard();
                    }

                    this.setX(restCardStartX);
                    this.setY(restCardStartY);

                    isBeingSwiped = false;
                    return true;
                }
        }
        return false;
    }

    public void swipeCard(){
        Log.d(TAG, "initViews: card is swiped left");

        if(onSwipeStartEvent != null) onSwipeStartEvent.onSwipeStart();
        Animation exitAnimation = RestaurantCardFinder.outToLeftAnimation();
        exitAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) { }

            @Override
            public void onAnimationEnd(Animation animation) {
                Log.d(TAG, "onAnimationEnd: animation finished");
                if(onSwipeEndEvent != null) onSwipeEndEvent.onSwipeEnd();
            }

            @Override
            public void onAnimationRepeat(Animation animation) { }
        });

        this.startAnimation(exitAnimation);
    }

    public boolean isContentsVisible(){
        return restaurantCardContents.getVisibility() == VISIBLE;
    }

    public void setOnSwipeEndEvent(OnSwipeEnd onSwipeEndEvent){
        this.onSwipeEndEvent = onSwipeEndEvent;
    }

    public void setOnSwipeStartEvent(OnSwipeStart onSwipeStartEvent){
        this.onSwipeStartEvent = onSwipeStartEvent;
    }

    public void setOnOpenContents(OnOpenContents onOpenContents){
        this.onOpenContents = onOpenContents;
    }

    public void setOnCloseContents(OnCloseContents onCloseContents){
        this.onCloseContents = onCloseContents;
    }

    public String getURL() {
        return restaurantCardContents.getUrl();
    }

    public String getPhoneNumber(){
        return restaurantCardContents.getPhoneNumber();
    }

    public String getAddress() {
        return restaurantCardContents.getAddress();
    }

    public int getImageWidth(){
        return restPhoto.getWidth();
    }

    public int getImageHeight(){
        return restPhoto.getHeight();
    }

    public static interface OnCloseContents {
        void onClose();
    }

    public static interface OnOpenContents {
        void onOpen();
    }

    public static interface OnSwipeEnd {
        void onSwipeEnd();
    }

    public static interface OnSwipeStart {
        void onSwipeStart();
    }
}
