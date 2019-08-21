package seaplus.seefood.controller;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.parse.ParseUser;

import java.util.ArrayList;

import seaplus.seefood.R;
import seaplus.seefood.model.Restaurant;

/**
 * Created by NTU user on 30/9/2017.
 */

public class FavouritesListViewAdapter extends BaseAdapter{

    private Context context;
    private ArrayList<Restaurant> favouriteArrayList;
    private RequestOptions imgOptions;
    private ToggleButton favouriteBtn;
    private Restaurant favourite;

    public FavouritesListViewAdapter(Context context, ArrayList<Restaurant> favouriteArrayList) {
        this.context = context;
        this.favouriteArrayList = favouriteArrayList;
    }

    @Override
    public int getCount() {
        return favouriteArrayList.size();
    }

    @Override
    public Object getItem(int i) {
        return favouriteArrayList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    //inflate the restaurant listview layout on every item in the listview
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        FavouriteViewHolder favouriteViewHolder;


        if (view == null){
            LayoutInflater layoutInflater = (LayoutInflater)  context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.favourite_listview, viewGroup, false);

            favouriteViewHolder = new FavouriteViewHolder();

            favouriteViewHolder.image = (ImageView) view.findViewById(R.id.image);
            favouriteViewHolder.name = (TextView) view.findViewById(R.id.name);
            favouriteViewHolder.price = (TextView) view.findViewById(R.id.price);
            favouriteViewHolder.rating = (TextView) view.findViewById(R.id.rating);
            favouriteViewHolder.favourites = (ToggleButton) view.findViewById(R.id.favouriteButton);

            view.setTag(favouriteViewHolder);

        } else {

            favouriteViewHolder = (FavouriteViewHolder) view.getTag();
        }

        favourite = (Restaurant) getItem(i);

        //download & display image from url using glide library
        imgOptions = new RequestOptions()
                .centerCrop()
                .override(800,550)
                .diskCacheStrategy(DiskCacheStrategy.ALL);
        Glide.with(context).load(favourite.getImage())
                .apply(imgOptions)
                .into((ImageView) view.findViewById(R.id.nearbyRestaurant));

        favouriteViewHolder.name.setText(favourite.getName());
        favouriteViewHolder.price.setText(favourite.getPrice());
        favouriteViewHolder.rating.setText(Double.toString(favourite.getRating()));

        favouriteBtn = (ToggleButton) (view.findViewById(R.id.favouriteButton));

        favouriteBtn.setTag(i);

        favouriteBtn.setChecked(true);

        favouriteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                int position=(Integer)view.getTag();
                Restaurant removeFavouriteObj;
                removeFavouriteObj = (Restaurant) getItem(position);
                QueryFavourite queryFavourite = new QueryFavourite();
                ParseUser currentUser = ParseUser.getCurrentUser();
                //queryFavourite.removeFavourite(currentUser.getUsername(),removeFavouriteObj.getPlaceID());
                queryFavourite.removeFavourite(removeFavouriteObj,currentUser);
                Handler mHandler = new Handler();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Toast toast = Toast.makeText(view.getContext(), "Restuarant Removed", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                },300);


            }
        });

        return view;
    }


    private class FavouriteViewHolder{
        ImageView image;
        TextView name;
        TextView  rating;
        TextView price;
        ToggleButton favourites;
    }

}




