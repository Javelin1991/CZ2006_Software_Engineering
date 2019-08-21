package seaplus.seefood.controller;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

import seaplus.seefood.R;
import seaplus.seefood.model.Restaurant;

/**
 * Created by NTU user on 30/9/2017.
 */

public class RestaurantListViewAdapter extends BaseAdapter{

    private Context context;
    private ArrayList<Restaurant> restaurantArrayList;
    private RequestOptions imgOptions;

    public RestaurantListViewAdapter(Context context, ArrayList<Restaurant> restaurantArrayList) {
        this.context = context;
        this.restaurantArrayList = restaurantArrayList;
    }

    @Override
    public int getCount() {
        return restaurantArrayList.size();
    }

    @Override
    public Object getItem(int i) {
        return restaurantArrayList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    //inflate the restaurant listview layout on every item in the listview
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        RestaurantViewHolder restaurantViewHolder;

        if (view == null){
            LayoutInflater layoutInflater = (LayoutInflater)  context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.restaurant_listview, viewGroup, false);

            restaurantViewHolder = new RestaurantViewHolder();

            restaurantViewHolder.image = (ImageView) view.findViewById(R.id.image);
            restaurantViewHolder.name = (TextView) view.findViewById(R.id.name);
            restaurantViewHolder.price = (TextView) view.findViewById(R.id.price);
            restaurantViewHolder.rating = (TextView) view.findViewById(R.id.rating);

            view.setTag(restaurantViewHolder);

        } else {
            restaurantViewHolder = (RestaurantViewHolder) view.getTag();
        }

        Restaurant restaurant = (Restaurant) getItem(i);

        //download & display image from url using glide library
        imgOptions = new RequestOptions()
                .centerCrop()
                .override(800,550)
                .diskCacheStrategy(DiskCacheStrategy.ALL);
        Glide.with(context).load(restaurant.getImage())
                .apply(imgOptions)
                .into((ImageView) view.findViewById(R.id.nearbyRestaurant));

        //restaurantViewHolder.image.setImageResource(restaurant.getImage());
        restaurantViewHolder.name.setText(restaurant.getName());
        restaurantViewHolder.price.setText(restaurant.getPrice());
        restaurantViewHolder.rating.setText(Double.toString(restaurant.getRating()));

        return view;
    }

    private class RestaurantViewHolder{
        ImageView image;
        TextView name;
        TextView  rating;
        TextView price;
    }

}


