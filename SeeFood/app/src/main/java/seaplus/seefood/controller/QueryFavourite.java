package seaplus.seefood.controller;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;

import seaplus.seefood.model.Restaurant;

/**
 * Created by NTU user on 3/11/2017.
 */

public class QueryFavourite implements FavouriteDAO{



    @Override
    public void addFavourite(Restaurant restaurant, ParseUser currentUser) {
        ParseObject fav = new ParseObject("Favourites");
        fav.put("placeId", restaurant.getPlaceID());
        fav.put("imageURL", restaurant.getImage());
        fav.put("name", restaurant.getName());
        fav.put("price", restaurant.getPrice());
        fav.put("rating", restaurant.getRating());
        fav.put("vicinity", restaurant.getVicinity());
        fav.put("username", currentUser.getUsername());
        try {
            fav.saveInBackground();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeFavourite(Restaurant restaurant, ParseUser currentUser) {
        ParseQuery<ParseObject> favQuery = ParseQuery.getQuery("Favourites");
        favQuery.whereEqualTo("username", currentUser.getUsername());
        favQuery.whereEqualTo("placeId",restaurant.getPlaceID());
        favQuery.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if (object == null) {

                } else {
                    object.deleteInBackground();
                }
            }
        });
    }

    @Override
    public boolean isFavouriteExist(Restaurant restaurant, ParseUser currentUser) {
        //check if user favourite this restaurant before
        ParseQuery<ParseObject> favQuery = ParseQuery.getQuery("Favourites");
        favQuery.whereEqualTo("username", currentUser.getUsername());
        favQuery.whereEqualTo("placeId",restaurant.getPlaceID());

        try {
            if (favQuery.getFirst() != null) {
                return true;
            }
            else{
                return false;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public ArrayList<Restaurant> retrieveFavourite(ParseUser currentUser) {
        return null;
    }


    /*
    @Override
    public ArrayList<Restaurant> retrieveFavourite(ParseUser currentUser) {

        final ArrayList<Restaurant> favouritesArrayList;
        favouritesArrayList = new ArrayList<Restaurant>();

        ParseQuery<ParseObject> favQuery = ParseQuery.getQuery("Favourites");
        favQuery.whereEqualTo("username", currentUser.getUsername());
        favQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {

                for (int i = 0; i < objects.size(); i++) {

                    Restaurant restaurantObj = new Restaurant(
                            objects.get(i).getString("placeId"),
                            objects.get(i).getString("imageURL"),
                            objects.get(i).getString("name"),
                            objects.get(i).getDouble("rating"),
                            objects.get(i).getString("price"),
                            objects.get(i).getString("vicinity"));

                    favouritesArrayList.add(restaurantObj);

                }
            }
        });
        return favouritesArrayList;
    }
*/

}