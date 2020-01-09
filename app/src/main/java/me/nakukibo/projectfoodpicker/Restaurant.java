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

import static me.nakukibo.projectfoodpicker.DataParser.DATA_DEFAULT;

public class Restaurant {
    private String name;
    private String address;
    private Boolean isOpen;
    private String photosJson;
    private Double rating;
    private Integer totRating;
    private Integer priceLevel;
    private Double distanceMiles; //distance_meters return straight line distance from origin to place
    private String id;

    private String hours;
    private String phoneNumber;
    private String website;
    private List<Bitmap> photoBitmaps;

    public Restaurant(String name, String address, Boolean isOpen, String photosJson, Double rating,
                      Integer totRating, Integer priceLevel, Double distanceMiles, String id) {
        this.name = name;
        this.address = address;
        this.isOpen = isOpen;
        this.photosJson = photosJson;
        this.rating = rating;
        this.totRating = totRating;
        this.priceLevel = priceLevel;
        this.distanceMiles = distanceMiles;
        this.id = id;

        this.phoneNumber = null;
        this.website = null;
        this.hours = null;
        this.photoBitmaps = null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address == null ? DATA_DEFAULT : address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Boolean getOpen() {
        return isOpen;
    }

    public void setOpen(Boolean open) {
        isOpen = open;
    }

    public String getPhotosJson() {
        return photosJson == null ? DATA_DEFAULT : photosJson;
    }

    public void setPhotosJson(String photosJson) {
        this.photosJson = photosJson;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public Integer getTotRating() {
        return totRating;
    }

    public void setTotRating(Integer totRating) {
        this.totRating = totRating;
    }

    public Integer getPriceLevel() {
        return priceLevel;
    }

    public void setPriceLevel(Integer priceLevel) {
        this.priceLevel = priceLevel;
    }

    public Double getDistanceMiles() {
        return distanceMiles;
    }

    public void setDistanceMiles(Double distanceMiles) {
        this.distanceMiles = distanceMiles;
    }

    public String getId() {
        return id == null ? DATA_DEFAULT : id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHours() {
        return hours == null ? DATA_DEFAULT : hours;
    }

    public void setHours(String hours) {
        this.hours = hours;
    }

    public String getPhoneNumber() {
        return phoneNumber == null ? DATA_DEFAULT : phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getWebsite() {
        return website == null ? DATA_DEFAULT : website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public List<Bitmap> getPhotoBitmaps() {
        return photoBitmaps;
    }

    public void setPhotoBitmaps(List<Bitmap> photoBitmaps) {
        this.photoBitmaps = photoBitmaps;
    }
}