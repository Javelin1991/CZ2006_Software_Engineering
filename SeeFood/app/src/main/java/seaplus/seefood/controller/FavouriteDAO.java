package seaplus.seefood.controller;

import com.parse.ParseUser;

import java.util.ArrayList;

import seaplus.seefood.model.Restaurant;

/**
 * Created by NTU user on 12/11/2017.
 */

public interface FavouriteDAO {

     void addFavourite(Restaurant restaurant, ParseUser currentUser);

     void removeFavourite(Restaurant restaurant, ParseUser currentUser);

     boolean isFavouriteExist(Restaurant restaurant, ParseUser currentUser);

     ArrayList<Restaurant> retrieveFavourite(ParseUser currentUser);
}
