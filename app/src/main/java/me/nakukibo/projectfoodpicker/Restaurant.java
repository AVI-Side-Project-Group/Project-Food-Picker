package me.nakukibo.projectfoodpicker;

import android.graphics.Bitmap;

import java.util.List;

import static me.nakukibo.projectfoodpicker.DataParser.DATA_DEFAULT;

class Restaurant {
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

    Restaurant(String name, String address, Boolean isOpen, String photosJson, Double rating,
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

    String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    String getAddress() {
        return address == null ? DATA_DEFAULT : address;
    }

    void setAddress(String address) {
        this.address = address;
    }

    Boolean getOpen() {
        return isOpen;
    }

    void setOpen(Boolean open) {
        isOpen = open;
    }

    String getPhotosJson() {
        return photosJson == null ? DATA_DEFAULT : photosJson;
    }

    void setPhotosJson(String photosJson) {
        this.photosJson = photosJson;
    }

    Double getRating() {
        return rating;
    }

    void setRating(Double rating) {
        this.rating = rating;
    }

    Integer getTotRating() {
        return totRating;
    }

    void setTotRating(Integer totRating) {
        this.totRating = totRating;
    }

    Integer getPriceLevel() {
        return priceLevel;
    }

    void setPriceLevel(Integer priceLevel) {
        this.priceLevel = priceLevel;
    }

    Double getDistanceMiles() {
        return distanceMiles;
    }

    void setDistanceMiles(Double distanceMiles) {
        this.distanceMiles = distanceMiles;
    }

    String getId() {
        return id == null ? DATA_DEFAULT : id;
    }

    void setId(String id) {
        this.id = id;
    }

    String getHours() {
        return hours == null ? DATA_DEFAULT : hours;
    }

    void setHours(String hours) {
        this.hours = hours;
    }

    String getPhoneNumber() {
        return phoneNumber == null ? DATA_DEFAULT : phoneNumber;
    }

    void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    String getWebsite() {
        return website == null ? DATA_DEFAULT : website;
    }

    void setWebsite(String website) {
        this.website = website;
    }

    List<Bitmap> getPhotoBitmaps() {
        return photoBitmaps;
    }

    void setPhotoBitmaps(List<Bitmap> photoBitmaps) {
        this.photoBitmaps = photoBitmaps;
    }
}