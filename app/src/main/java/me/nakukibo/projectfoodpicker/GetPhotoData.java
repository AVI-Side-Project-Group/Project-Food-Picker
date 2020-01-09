package me.nakukibo.projectfoodpicker;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class GetPhotoData extends AsyncTask<Object, String, String> {

    private static final String TAG = GetNearbyData.class.getSimpleName();

    private Restaurant selectedRestaurant;
    private ReceiveDetailData receiveDetailData;
    private String googlePlacesData;
    private List<Photo> photos;
    private List<String> photoRetrieved;

    private Integer cIndex = null;

    GetPhotoData(Restaurant selectedRestaurant, ReceiveDetailData receiveDetailData) {
        this.selectedRestaurant = selectedRestaurant;
        this.receiveDetailData = receiveDetailData;
    }

    @Override
    protected String doInBackground(Object... objects) {
        String url = (String) objects[0];
        Log.d(TAG, "doInBackground: url to search=" + url);

        DownloadUrl downloadURL = new DownloadUrl();
        try {
            googlePlacesData = downloadURL.readUrl(url);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return googlePlacesData;
    }

    @Override
    protected void onPostExecute(String s) {
        DataParser parser = new DataParser();
        parser.parseDetails(s, selectedRestaurant);

        photos = DataParser.parsePhotos(selectedRestaurant.getPhotosJson());
        receiveDetailData.sendDetailData(selectedRestaurant);
    }
}