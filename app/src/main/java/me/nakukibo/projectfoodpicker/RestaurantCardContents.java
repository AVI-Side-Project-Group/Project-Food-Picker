package me.nakukibo.projectfoodpicker;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static android.text.Html.fromHtml;

public class RestaurantCardContents extends ScrollView {

    private String url = "";
    private String phoneNumber = "";
    private String address = "";

    private ClipboardManager clipboardManager = (ClipboardManager)FoodPicker.getApp().getSystemService(Context.CLIPBOARD_SERVICE);

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
                           String phoneNumber, String websiteURL, String hours){
        TextView txtvwRating = findViewById(R.id.txtvw_rating);
        txtvwRating.setText(rating);

        TextView txtvwPricing = findViewById(R.id.txtvw_price_level);
        txtvwPricing.setText(pricing);

        Button btnAddress = findViewById(R.id.btn_address);
        if(address == DataParser.DATA_DEFAULT){
            btnAddress.setClickable(false);
        } else {
            btnAddress.setClickable(true);
        }
        btnAddress.setText(address);
        this.address = address;
        btnAddress.setPaintFlags(btnAddress.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        btnAddress.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View v) {
                ClipData clipData = ClipData.newPlainText("Address", address);
                clipboardManager.setPrimaryClip(clipData);
                return true;
            }
        });


        Button btnPhoneNumber = findViewById(R.id.btn_phone_number);
        if(phoneNumber == DataParser.DATA_DEFAULT){
            btnPhoneNumber.setClickable(false);
        } else {
            btnPhoneNumber.setClickable(true);
        }
        btnPhoneNumber.setText(phoneNumber);
        this.phoneNumber = phoneNumber;
        btnPhoneNumber.setPaintFlags(btnPhoneNumber.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        btnAddress.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View v) {
                ClipData clipData = ClipData.newPlainText("Phone Number", phoneNumber);
                clipboardManager.setPrimaryClip(clipData);
                return true;
            }
        });


        Button btnWebsite = findViewById(R.id.btn_website);
        if(websiteURL == DataParser.DATA_DEFAULT){
            btnWebsite.setClickable(false);
        } else {
            btnWebsite.setClickable(true);
        }
        //btnWebsite.setText(getResources().getString(R.string.restcard_default_website));
        btnWebsite.setText(websiteURL);
        this.url = websiteURL;
        btnWebsite.setPaintFlags(btnWebsite.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        btnAddress.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View v) {
                ClipData clipData = ClipData.newPlainText("Website URL", url);
                clipboardManager.setPrimaryClip(clipData);
                return true;
            }
        });


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

    public String getUrl(){
        return url;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getAddress () {
        return address;
    }
}

