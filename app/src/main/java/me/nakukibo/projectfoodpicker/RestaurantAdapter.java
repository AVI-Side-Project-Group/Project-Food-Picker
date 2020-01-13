package me.nakukibo.projectfoodpicker;

import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.List;

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder> {
    private static String TAG = RestaurantAdapter.class.getSimpleName();
    private List<Restaurant> data;

    public static class RestaurantViewHolder extends RecyclerView.ViewHolder {
        public TextView txtvwName;
        public TextView txtvwAddress;

        public RestaurantViewHolder(View itemView){
            super(itemView);
            txtvwName = itemView.findViewById(R.id.his_txtvw_name);
            txtvwAddress = itemView.findViewById(R.id.his_txtvw_address);

            Log.d(TAG, "RestaurantViewHolder: " + txtvwName);

        }
    }

    public RestaurantAdapter(List<Restaurant> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public RestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item, parent, false);

        RestaurantViewHolder viewHolder = new RestaurantViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RestaurantViewHolder holder, int position) {
        Restaurant currentItem = data.get(position);

        holder.txtvwName.setText(currentItem.getName());
        holder.txtvwAddress.setText(currentItem.getAddress());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}