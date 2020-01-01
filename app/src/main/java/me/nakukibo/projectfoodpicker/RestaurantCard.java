package me.nakukibo.projectfoodpicker;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.HashMap;
import java.util.Locale;

public class RestaurantCard extends CardView {

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

    void setValues(HashMap<String, String> values){
        TextView txtvwName = findViewById(R.id.txtvw_name);
        txtvwName.setText(values.get(DataParser.DATA_KEY_NAME));

        ImageView restPhoto = findViewById(R.id.imgvw_restaurant);
        restPhoto.setImageResource(R.drawable.ic_launcher_background);

        TextView txtvwRating = findViewById(R.id.txtvw_rating);
        txtvwRating.setText(String.format(Locale.US, "%s stars (%s)",
                values.get(DataParser.DATA_KEY_RATING), values.get(DataParser.DATA_KEY_TOT_RATING)));

        TextView txtvwPricing = findViewById(R.id.txtvw_price_level);
        txtvwPricing.setText(String.format(Locale.US, "Pricing Level: %s",
                values.get(DataParser.DATA_KEY_PRICE_LEVEL)));

        TextView txtvwAddress = findViewById(R.id.txtvw_address);
        txtvwAddress.setText(values.get(DataParser.DATA_KEY_ADDRESS));

        TextView txtvwPhoneNumber = findViewById(R.id.txtvw_phone_number);
        txtvwPhoneNumber.setText(values.get(DataParser.DATA_KEY_PHONE_NUMBER));

        TextView txtvwWebsite = findViewById(R.id.txtvw_website);
        txtvwWebsite.setText(values.get(DataParser.DATA_KEY_WEBSITE));

        TextView txtvwHours = findViewById(R.id.txtvw_hours_values);
        txtvwHours.setText(values.get(DataParser.DATA_KEY_HOURS));
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
    }
}
