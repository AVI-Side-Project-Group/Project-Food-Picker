package me.nakukibo.projectfoodpicker;

import java.util.HashMap;
import java.util.List;

public interface ReceiveData {
    /**
     * method used to transmit data
     */
    void sendData(List<HashMap<String, String>> nearbyPlaceList, String nextPageToken);
}
