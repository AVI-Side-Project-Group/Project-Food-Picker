package me.nakukibo.projectfoodpicker;

import android.graphics.Bitmap;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

class Photo {

    private Bitmap bitmap;
    private OnFinishFetch onFinishFetch;
    private OnFailFetch onFailFetch;

    Photo(PlacesClient placesClient, PhotoMetadata photoMetadata){

        bitmap = null;
        onFinishFetch = null;
        onFailFetch = null;

        FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata)
                .build();

        placesClient.fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) -> {

            bitmap = fetchPhotoResponse.getBitmap();
            if(onFinishFetch != null) onFinishFetch.onFinishFetch();

        }).addOnFailureListener((exception) -> {

            if(onFailFetch != null) onFailFetch.onFailFetch();

        });
    }

    Bitmap getBitmap() {
        return bitmap;
    }

    void setOnFinishFetch(OnFinishFetch onFinishFetch) {
        this.onFinishFetch = onFinishFetch;
    }

    void setOnFailFetch(OnFailFetch onFailFetch) {
        this.onFailFetch = onFailFetch;
    }

    interface OnFinishFetch{
        /* *
         * OnFinishFetch interface used to set behavior after successful fetch of the photo
         * */

        void onFinishFetch();
    }

    interface OnFailFetch{
        /* *
         * OnFinishFetch interface used to set behavior after fetching of photo results in exception
         * */

        void onFailFetch();
    }
}
