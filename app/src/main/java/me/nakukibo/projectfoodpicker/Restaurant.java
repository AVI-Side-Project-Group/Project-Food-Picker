package me.nakukibo.projectfoodpicker;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
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

import java.util.List;
import java.util.Locale;

public class Restaurant {
    private String name;
    private String address;
    private String hours;
    private boolean isOpen;
    private String photosJson;
    private float rating;
    private int totRating;
    private int priceLevel;
    private String phoneNumber;
    private String website;
    private float distanceMiles; //distance_meters return straight line distance from origin to place
    private String id;
    private List<Bitmap> photoBitmaps;

    public Restaurant(String name, String address, String hours, boolean isOpen, String photosJson,
                      float rating, int totRating, int priceLevel, String phoneNumber, String website,
                      float distanceMiles, String id) {
        this.name = name;
        this.address = address;
        this.hours = hours;
        this.isOpen = isOpen;
        this.photosJson = photosJson;
        this.rating = rating;
        this.totRating = totRating;
        this.priceLevel = priceLevel;
        this.phoneNumber = phoneNumber;
        this.website = website;
        this.distanceMiles = distanceMiles;
        this.id = id;
        this.photoBitmaps = null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getHours() {
        return hours;
    }

    public void setHours(String hours) {
        this.hours = hours;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }

    public String getPhotosJson() {
        return photosJson;
    }

    public void setPhotosJson(String photosJson) {
        this.photosJson = photosJson;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public int getTotRating() {
        return totRating;
    }

    public void setTotRating(int totRating) {
        this.totRating = totRating;
    }

    public int getPriceLevel() {
        return priceLevel;
    }

    public void setPriceLevel(int priceLevel) {
        this.priceLevel = priceLevel;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public float getDistanceMiles() {
        return distanceMiles;
    }

    public void setDistanceMiles(float distanceMiles) {
        this.distanceMiles = distanceMiles;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Bitmap> getPhotoBitmaps() {
        return photoBitmaps;
    }

    public void setPhotoBitmaps(List<Bitmap> photoBitmaps) {
        this.photoBitmaps = photoBitmaps;
    }
}