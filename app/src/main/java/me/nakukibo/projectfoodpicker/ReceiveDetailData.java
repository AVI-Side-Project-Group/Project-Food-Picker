package me.nakukibo.projectfoodpicker;

import java.util.HashMap;

public interface ReceiveDetailData {
    /**
     * method used to transmit data
     */
    void sendDetailData(HashMap<String, String> selectedRestaurant);
}
