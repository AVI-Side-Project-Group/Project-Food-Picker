package me.nakukibo.projectfoodpicker;

import android.util.Log;

import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.ArrayList;
import java.util.List;

class Restaurant {

    /**
     * Restaurant class represents each individual restaurant
     * */

    static final String DATA_DEFAULT = "--NA--";

    private static final int MAX_NUM_PHOTOS = 5;    // max photos allowed for a restaurant

    private String name;
    private String address;
    private Boolean isOpen;
    private Double rating;
    private Integer totRating;
    private Integer priceLevel;
    private Double distanceMiles;
    private String id;

    private List<Photo> photos;
    private String weekdayTextConcatenated;
    private String phoneNumber;
    private String website;

    private OnFinishRetrievingImages onFinishRetrievingImages;
    private static final String TAG = Restaurant.class.getSimpleName();

    Restaurant(String name, String address, Boolean isOpen, Double rating,
                      Integer totRating, Integer priceLevel, Double distanceMiles, String id) {
        this.name = name;
        this.address = address;
        this.isOpen = isOpen;
        this.rating = rating;
        this.totRating = totRating;
        this.priceLevel = priceLevel;
        this.distanceMiles = distanceMiles;
        this.id = id;

        this.photos = new ArrayList<>();
        this.phoneNumber = DATA_DEFAULT;
        this.website = DATA_DEFAULT;
        this.weekdayTextConcatenated = DATA_DEFAULT;

        this.onFinishRetrievingImages = null;
    }

    /**
     * populates the photos array by fetching them from Google Places and instantiating the Photo obj
     *
     * @param placesClient      PlacesClient to be used to fetch data from Google Places
     * @param photosMetadata    list of PhotoMetadata instances representing individual images
     * */
    void fetchImages(PlacesClient placesClient, List<PhotoMetadata> photosMetadata){
        Log.d(TAG, "fetchImages: fetching photos");
        if(photosMetadata == null) return;

        final int numPhotos = Math.min(photosMetadata.size(), MAX_NUM_PHOTOS);
        List<PhotoMetadata> processedMetadata = new ArrayList<>();
        Log.d(TAG, "fetchImages: will be fetching " + numPhotos + " photos.");

        for(int i=0; i<numPhotos; i++){
            PhotoMetadata photoMetadata = photosMetadata.get(i);
            Photo photo = new Photo(placesClient, photoMetadata);
            Log.d(TAG, "fetchImages: photometadata=" + photoMetadata.getAttributions());

            photo.setOnFinishFetch(() -> {
                Log.d(TAG, "fetchImages: successfully added photo");
                photos.add(photo);
                defaultLastPhotoEvent(processedMetadata, photoMetadata, numPhotos);
            });
            photo.setOnFailFetch(() -> {
                Log.d(TAG, "fetchImages: failed to photo");
                defaultLastPhotoEvent(processedMetadata, photoMetadata, numPhotos);
            });
        }
    }

    /**
     * checks if the photoMetaData being processed is the last to be processed
     * and if so then runs the onFinishRetrievingImages.onFinishRetrieve() if not null
     * */
    private void defaultLastPhotoEvent(List<PhotoMetadata> processed, PhotoMetadata photoMetadata,
                                       int numPhotos){
        if(isLastPhoto(processed, photoMetadata, numPhotos)){
            if(onFinishRetrievingImages != null) onFinishRetrievingImages.onFinishRetrieve(this);
        }
    }

    /**
     * checks if the photoMetadata is the last to be processed
     * @return true     - is the last
     *         false    - there is more to come
     * */
    private boolean isLastPhoto(List<PhotoMetadata> processed, PhotoMetadata photoMetadata,
                                int numPhotos){
        processed.add(photoMetadata);
        return processed.size() == numPhotos;
    }

    String getName() {
        return name;
    }

    String getAddress() {
        return address;
    }

    Boolean getOpen() {
        return isOpen;
    }

    Double getRating() {
        return rating;
    }

    Integer getTotRating() {
        return totRating;
    }

    Integer getPriceLevel() {
        return priceLevel;
    }

    Double getDistanceMiles() {
        return distanceMiles;
    }

    String getId() {
        return id;
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

    String getWeekdayTextConcatenated() {
        return weekdayTextConcatenated == null ? DATA_DEFAULT : weekdayTextConcatenated;
    }

    void setWeekdayTextConcatenated(List<String> weekdayText) {
        if(weekdayText != null) {
            StringBuilder weekdayTextBuilder = new StringBuilder();

            for(String dayText : weekdayText){
                weekdayTextBuilder.append(dayText).append("\n");
            }

            weekdayTextConcatenated = weekdayTextBuilder.toString();
        }else {
            weekdayTextConcatenated = DATA_DEFAULT;
        }

    }

    List<Photo> getPhotos(){
        return photos;
    }

    void setOnFinishRetrievingImages(OnFinishRetrievingImages onFinishRetrievingImages) {
        this.onFinishRetrievingImages = onFinishRetrievingImages;
    }

    interface OnFinishRetrievingImages{
        void onFinishRetrieve(Restaurant restaurant);
    }
}