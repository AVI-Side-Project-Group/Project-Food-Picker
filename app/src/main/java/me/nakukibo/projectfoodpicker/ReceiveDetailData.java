package me.nakukibo.projectfoodpicker;

import java.util.HashMap;
import java.util.List;

public interface ReceiveDetailData {
    /**
     * method used to transmit data
     */
    void sendDetailData(HashMap<String, String> selectedRestaurant);
}
