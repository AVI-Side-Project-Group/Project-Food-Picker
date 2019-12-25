package me.nakukibo.projectfoodpicker;

import java.util.HashMap;
import java.util.List;

public interface ReceiveData {
    void sendData(List<HashMap<String, String>> nearbyPlaceList);
}
