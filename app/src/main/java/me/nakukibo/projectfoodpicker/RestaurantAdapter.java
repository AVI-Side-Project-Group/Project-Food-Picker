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
    private List<HashMap<String, String>> data;

    public static class RestaurantViewHolder extends RecyclerView.ViewHolder {
        public TextView txtvwName;
        public TextView txtvwAddress;
        public Button btnMoreInfo;

        public RestaurantViewHolder(View itemView){
            super(itemView);
            //historyCard = h;
            txtvwName = itemView.findViewById(R.id.his_txtvw_name);
            txtvwAddress = itemView.findViewById(R.id.his_txtvw_address);
            btnMoreInfo = itemView.findViewById(R.id.btn_getinfo);

            Log.d(TAG, "RestaurantViewHolder: " + txtvwName);

        }
    }

    public RestaurantAdapter(List<HashMap<String, String>> data) {
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
        HashMap<String, String> currentItem = data.get(position);

        holder.txtvwName.setText(currentItem.get(DataParser.DATA_KEY_NAME));
        holder.txtvwAddress.setText(currentItem.get(DataParser.DATA_KEY_ADDRESS));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}