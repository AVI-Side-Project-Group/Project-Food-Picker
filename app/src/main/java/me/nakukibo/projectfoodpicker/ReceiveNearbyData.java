package me.nakukibo.projectfoodpicker;

import java.util.HashMap;
import java.util.List;

public interface ReceiveNearbyData {
    /**
     * method used to transmit data
     */
    void sendData(List<Restaurant> restaurants, String nextPageToken);
}
