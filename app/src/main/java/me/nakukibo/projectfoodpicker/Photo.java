package me.nakukibo.projectfoodpicker;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.io.ByteArrayOutputStream;

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

    Photo(Bitmap bitmap){
        this.bitmap = bitmap;
        onFinishFetch = null;
        onFailFetch = null;
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

    String getStringFromBitmap(Bitmap bitmapPicture) {
        final int COMPRESSION_QUALITY = 100;
        String encodedImage;
        ByteArrayOutputStream byteArrayBitmapStream = new ByteArrayOutputStream();
        bitmapPicture.compress(Bitmap.CompressFormat.PNG, COMPRESSION_QUALITY,
                byteArrayBitmapStream);
        byte[] b = byteArrayBitmapStream.toByteArray();
        encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
        return encodedImage;
    }
}
