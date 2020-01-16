package me.nakukibo.projectfoodpicker;

import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class HistoryActivity extends ThemedAppCompatActivity {

    private static final String TAG = HistoryActivity.class.getSimpleName();
    private List<Restaurant> previouslyAccessed;

    private PopupWindow popupWindow;
    private int currentImage = 0;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        previouslyAccessed = getHistory();
        initRecycler();
    }

    private void initRecycler(){
        recyclerView = findViewById(R.id.recycle_history);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new RestaurantAdapter(previouslyAccessed);
        recyclerView.setAdapter(adapter);
    }

    private List<Restaurant> getHistory(){
        List<Restaurant> restaurantList = new ArrayList<>();
        Set jsonSet = getApplicationSharedPreferences().getStringSet(getString(R.string.sp_previously_accessed_json), null);

        if(jsonSet != null){
            ArrayList<String> jsonList = new ArrayList<String>(jsonSet);
            for(int i = 0; i < jsonList.size(); i++) {
                Restaurant restaurant = new Restaurant(jsonList.get(i));
                restaurantList.add(restaurant);
                Log.d(TAG, "getHistory: " + restaurantList.get(i).getName());
            }
        }

        Log.d(TAG, "getHistory: " + restaurantList);

        return restaurantList;
    }

    public void getInfo(View view){
        LayoutInflater inflater = (LayoutInflater) view.getContext().getSystemService(view.getContext().LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.history_card,null);
        popupWindow = new PopupWindow(layout, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, true);

        if(Build.VERSION.SDK_INT >= 21){
            popupWindow.setElevation(5.0f);
        }
        popupWindow.showAtLocation(view, Gravity.CENTER,0,0);

        setValues(view, layout);

        //hisCard.setCardBackgroundColor();
    }

    public void closePopUpWindow(View view){
        popupWindow.dismiss();
    }

    private void setValues(View view, View card){
        Restaurant restaurant = previouslyAccessed.get(recyclerView.getChildLayoutPosition(view));

        CardView hisCard = card.findViewById(R.id.his_cardView);
        View restCard = hisCard.findViewById(R.id.hiscard_restcard);
        TextView restName = restCard.findViewById(R.id.txtvw_title_name);
        restName.setText(restaurant.getName());

        Log.d(TAG, "setValues: " + restaurant.getJsonFromRestaurant());

        Log.d(TAG, "setValues: " + restaurant.getPhotos());

        /*TextView restaurantName = card.findViewById(R.id.txtvw_hiscard_name);
        TextView restaurantOpenNow = card.findViewById(R.id.txtvw_hiscard_open_now);
        TextView restaurantDistance = card.findViewById(R.id.txtvw_hiscard_distance);
        ImageView restaurantPhoto = card.findViewById(R.id.imgvw_hiscard_restaurant);
        Button imageBtnLeft = card.findViewById(R.id.btn_hiscard_left);
        Button imageBtnRight = card.findViewById(R.id.btn_hiscard_right);

        Log.d(TAG, "getInfo: " + hisCard);

        restaurantName.setText(restaurant.getName());
        restaurantDistance.setText(restaurant.getDistanceMiles() == null? "Unknown Distance" : String.format(Locale.US, "%.2f miles", restaurant.getDistanceMiles()));
        String openNowText;
        if(restaurant.getOpen() == null){
            openNowText = "Unsure if open";
        } else if(restaurant.getOpen()){
            openNowText = "Open Now!";
        } else {
            openNowText = "Closed";
        }
        restaurantOpenNow.setText(openNowText);

        /*List<Photo> photos = restaurant.getPhotos();
        if(photos == null) {
            restaurantPhoto.setImageDrawable(getResources().getDrawable(R.drawable.ic_launcher_background));
            imageBtnLeft.setOnClickListener(v -> {
                if (currentImage > 0) {
                    currentImage--;
                    restaurantPhoto.setImageBitmap(photos.get(currentImage).getBitmap());
                }
            });
            imageBtnRight.setOnClickListener(v -> {
                if(currentImage < photos.size()){
                    currentImage++;
                    restaurantPhoto.setImageBitmap(photos.get(currentImage).getBitmap());
                }
            });
        }else {
            restaurantPhoto.setImageBitmap(photos.get(currentImage).getBitmap());
        }*/
    }
}

