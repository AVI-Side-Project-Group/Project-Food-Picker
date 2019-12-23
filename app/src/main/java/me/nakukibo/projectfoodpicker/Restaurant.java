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
    private float distance; //distance_meters return straight line distance from origin to place
    private String hours;
    private String address;
    private String website;

    //constructor
    public Restaurant(String name, String foodType, String rating, String phoneNumber,
                      String price, float distance, String hours, String address, String website){
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

    public String getNameR() {
        return name;
    }

    public String getFoodTypeR() {
        return foodType;
    }

    public String getRatingR() {
        return rating;
    }

    public String getPhoneNumberR() {
        return phoneNumber;
    }

    public String getPriceR() {
        return price;
    }

    public float getDistanceR() {
        return distance;
    }

    public String getHoursR() {
        return hours;
    }

    public String getAddressR() {
        return address;
    }

    public String getWebsiteR() {
        return website;
    }

    public void setNameR(String name) {
        this.name = name;
    }

    public void setFoodTypeR(String foodType) {
        this.foodType = foodType;
    }

    public void setRatingR(String rating) {
        this.rating = rating;
    }

    public void setPhoneNumberR(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setPriceR(String price) {
        this.price = price;
    }

    public void setDistanceR(float distance) {
        this.distance = distance;
    }

    public void setHours(String hours) {
        this.hours = hours;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setWebsite(String website) {
        this.website = website;
    }
}