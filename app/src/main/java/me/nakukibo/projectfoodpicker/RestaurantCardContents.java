package me.nakukibo.projectfoodpicker;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class RestaurantCardContents extends ScrollView {

    // TODO: add distance and open now

    public RestaurantCardContents(@NonNull Context context) {
        this(context, null);
    }

    public RestaurantCardContents(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RestaurantCardContents(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initCardContents(context);
    }

    /**
     * set restaurant card to default values
     */
    void setDefaultValues(){
        setValues(
                getResources().getString(R.string.restcard_default_rating),
                getResources().getString(R.string.restcard_default_pricing),
                getResources().getString(R.string.restcard_default_address),
                getResources().getString(R.string.restcard_default_phone_number),
                getResources().getString(R.string.restcard_default_website),
                getResources().getString(R.string.restcard_default_hours));
    }

    /**
     * set restaurant card to values passed
     */
    void setValues(String rating, String pricing, String address,
                           String phoneNumber, String website, String hours){
        TextView txtvwRating = findViewById(R.id.txtvw_rating);
        txtvwRating.setText(rating);

        TextView txtvwPricing = findViewById(R.id.txtvw_price_level);
        txtvwPricing.setText(pricing);

        TextView txtvwAddress = findViewById(R.id.txtvw_address);
        txtvwAddress.setText(address);

        TextView txtvwPhoneNumber = findViewById(R.id.txtvw_phone_number);
        txtvwPhoneNumber.setText(phoneNumber);

        TextView txtvwWebsite = findViewById(R.id.txtvw_website);
        txtvwWebsite.setText(website);

        TextView txtvwHours = findViewById(R.id.txtvw_hours_values);
        txtvwHours.setText(hours);
    }

    /**
     * initialize the views in the card to specific values
     */
    private void initCardContents(@NonNull Context context) {
        inflate(context, R.layout.restaurant_card_contents, this);

        // set values to default
        setValues(
                getResources().getString(R.string.restcard_default_rating),
                getResources().getString(R.string.restcard_default_pricing),
                getResources().getString(R.string.restcard_default_address),
                getResources().getString(R.string.restcard_default_phone_number),
                getResources().getString(R.string.restcard_default_website),
                getResources().getString(R.string.restcard_default_hours)
        );
    }
}

