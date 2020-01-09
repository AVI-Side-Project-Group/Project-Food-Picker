package me.nakukibo.projectfoodpicker;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class RestaurantCardContents extends ScrollView {

    private Context context;
    private ClipboardManager clipboardManager;

    private String url = "";
    private String phoneNumber = "";
    private String address = "";

    public RestaurantCardContents(@NonNull Context context) {
        this(context, null);
    }

    public RestaurantCardContents(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RestaurantCardContents(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initInternalFields(context);
        initCardContents();
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

        TextView txtvwHours = findViewById(R.id.txtvw_hours_values);
        txtvwHours.setText(hours);

        this.address = address;
        this.phoneNumber = phoneNumber;
        this.url = websiteURL;

        Button btnAddress = findViewById(R.id.btn_address);
        setClickEvents(btnAddress, address, "Address");

        Button btnPhoneNumber = findViewById(R.id.btn_phone_number);
        setClickEvents(btnPhoneNumber, phoneNumber, "Phone number");

        Button btnWebsite = findViewById(R.id.btn_website);
        setClickEvents(btnWebsite, url, "Website URL");
    }

    private void setClickEvents(Button btn, String btnText, String clipLabel){
        if(btnText.equals(DataParser.DATA_DEFAULT)) {
            activateClickEvents(btn);
        } else {
            deactivateClickEvents(btn);
        }

        btn.setText(btnText);
        btn.setPaintFlags(btn.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        btn.setOnLongClickListener(v -> {
            defaultLongClickEvent(clipLabel, btnText);
            return true;
        });
    }

    private void activateClickEvents(Button btn){
        btn.setClickable(true);
        btn.setLongClickable(true);
    }

    private void deactivateClickEvents(Button btn){
        btn.setClickable(false);
        btn.setLongClickable(false);
    }

    private void defaultLongClickEvent(String label, String clipText){
        ClipData clipData = ClipData.newPlainText(label, clipText);
        clipboardManager.setPrimaryClip(clipData);
        Toast.makeText(context, label + " copied to clipboard.", Toast.LENGTH_LONG).show();
    }

    private void initInternalFields(@NonNull Context context) {
        this.context = context;
        clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
    }

    /**
     * initialize the views in the card to specific values
     */
    private void initCardContents() {
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

