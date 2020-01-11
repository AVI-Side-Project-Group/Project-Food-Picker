package me.nakukibo.projectfoodpicker;

import android.net.Uri;
import android.util.Log;

import com.google.android.gms.common.api.ApiException;
import com.google.android.libraries.places.api.model.OpeningHours;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.Arrays;
import java.util.List;

class FetchDetails {

    /**
     * Fetches Google Place details for a specific location using PlacesClient instance
     * */

    private static final String TAG = GetNearbyData.class.getSimpleName();
    private Restaurant.OnFinishRetrievingImages onFinishRetrievingImages;

    FetchDetails(Restaurant.OnFinishRetrievingImages onFinishRetrievingImages) {
        this.onFinishRetrievingImages = onFinishRetrievingImages;
    }

    void fetch(Restaurant selectedRestaurant, PlacesClient placesClient) {
        Log.d(TAG, "fetch: fetching details for " + selectedRestaurant.getName());

        selectedRestaurant.setOnFinishRetrievingImages(onFinishRetrievingImages);

        List<Place.Field> placeFields = Arrays.asList(Place.Field.PHONE_NUMBER, Place.Field.OPENING_HOURS,
                Place.Field.WEBSITE_URI, Place.Field.PHOTO_METADATAS);
        FetchPlaceRequest request = FetchPlaceRequest.newInstance(selectedRestaurant.getId(), placeFields);

        placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
            Place place = response.getPlace();
            OpeningHours openingHours = place.getOpeningHours();
            Uri website = place.getWebsiteUri();

            selectedRestaurant.setPhoneNumber(place.getPhoneNumber());
            selectedRestaurant.setWeekdayTextConcatenated(openingHours == null ? null : openingHours.getWeekdayText());
            selectedRestaurant.setWebsite(website == null ? null : website.toString());
            selectedRestaurant.fetchImages(placesClient, place.getPhotoMetadatas());

        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                Log.e(TAG, "Place not found " + selectedRestaurant.getName());
                exception.printStackTrace();
            }
        });
    }
}