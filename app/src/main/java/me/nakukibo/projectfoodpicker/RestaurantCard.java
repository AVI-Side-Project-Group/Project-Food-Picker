package me.nakukibo.projectfoodpicker;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.Locale;

public class RestaurantCard extends CardView {

    private float startX;
    private float startY;

    private float dx;
    private float dy;

    private static final String TAG = RestaurantCard.class.getSimpleName();

    public RestaurantCard(@NonNull Context context) {
        this(context, null);
    }

    public RestaurantCard(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RestaurantCard(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initCard(context, attrs);
    }

    /**
     * initialize the views in the card to specific values
     */
    private void initCard(@NonNull Context context, @Nullable AttributeSet attrs) {
        inflate(context, R.layout.restaurant_card, this);

        // defines all views
        TextView txtvwName = findViewById(R.id.txtvw_name);
        ImageView imgvwRestaurant = findViewById(R.id.imgvw_restaurant);
        TextView txtvwRating = findViewById(R.id.txtvw_rating);
        TextView txtvwPriceLevel = findViewById(R.id.txtvw_price_level);
        TextView txtvwAddress = findViewById(R.id.txtvw_address);
        TextView txtvwPhoneNumber = findViewById(R.id.txtvw_phone_number);
        TextView txtvwWebsite = findViewById(R.id.txtvw_website);
        TextView txtvwHours = findViewById(R.id.txtvw_hours_values);

        // set view values to attribute values
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.RestaurantCard);

        txtvwName.setText(attributes.getString(R.styleable.RestaurantCard_name));
        imgvwRestaurant.setImageDrawable(attributes.getDrawable(R.styleable.RestaurantCard_android_src));
        txtvwRating.setText(String.format(Locale.US, "%1.1f stars", attributes.getFloat(R.styleable.RestaurantCard_rating, 1f)));
        txtvwPriceLevel.setText(String.format(Locale.US, "Price Level: %d", attributes.getInteger(R.styleable.RestaurantCard_price_level, 1)));
        txtvwAddress.setText(attributes.getString(R.styleable.RestaurantCard_address));
        txtvwPhoneNumber.setText(attributes.getString(R.styleable.RestaurantCard_phone_number));
        txtvwWebsite.setText(attributes.getString(R.styleable.RestaurantCard_website));
        txtvwHours.setText(attributes.getString(R.styleable.RestaurantCard_hours));

        attributes.recycle();

        ConstraintLayout restCardLayout = findViewById(R.id.restcard_layout);
        startX = restCardLayout.getX();
        startY = restCardLayout.getY();

        restCardLayout.setOnTouchListener((view, motionEvent) -> {
            Log.d(TAG, "initCard: initiated motion event=" + motionEvent.getAction());
            switch(motionEvent.getAction()){
                case MotionEvent.ACTION_DOWN:
                    dx = view.getX() - motionEvent.getRawX();
                    dy = view.getY() - motionEvent.getRawY();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    view.setX(motionEvent.getRawX() + dx);
                    view.setY(motionEvent.getRawY() + dy);
                    break;
                case MotionEvent.ACTION_UP:
                    view.setX(startX);
                    view.setY(startY);
                    return true;
            }
            return false;
        });
    }
}
