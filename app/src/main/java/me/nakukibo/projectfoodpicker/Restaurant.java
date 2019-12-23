package me.nakukibo.projectfoodpicker;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.Locale;

public class Restaurant {
    private String name;
    private String foodType;
    private String rating;
    private String phoneNumber;
    private String price;
    private float distance;
    private String hours;
    private String address;
    private String website;

    public Restaurant(String name,
                      String foodType,
                      String rating,
                      String phoneNumber,
                      String price,
                      float distance,
                      String hours,
                      String address,
                      String website){
        this.name = name;
        this.foodType = foodType;
        this.rating = rating;
        this.phoneNumber = phoneNumber;
        this.price = price;
        this.distance = distance;
        this.hours = hours;
        this.address = address;
        this.website = website;
    }
}