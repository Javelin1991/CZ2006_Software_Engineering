package seaplus.seefood.controller;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import seaplus.seefood.R;
import seaplus.seefood.model.Restaurant;


public class FavouriteFragment extends Fragment {

    private ArrayList<Restaurant> favouritesArrayList;
    private FavouritesListViewAdapter favouritesListViewAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //returning our layout file
        //change R.layout.yourlayoutfilename for each of your fragments
        return inflater.inflate(R.layout.fragment_favourite, container, false);
    }

    //disable action button (search and filter) for account
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.search_btn).setVisible(false);
        menu.findItem(R.id.filter_btn).setVisible(false);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //disable action button (search etc.) for account
        setHasOptionsMenu(true);
        //set toolbar title
        getActivity().setTitle("Favourite");

        swipeRefreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swipeRefresh);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                populateListView();
            }
        });

        //on create populate
        populateListView();

    }

    //on back navigation populate listview
    public void onResume(){
        super.onResume();
        populateListView();
    }


    public void populateListView(){
        ParseQuery<ParseObject> favQuery = ParseQuery.getQuery("Favourites");
        ParseUser currentUser = ParseUser.getCurrentUser();
        favQuery.whereEqualTo("username", currentUser.getUsername());
        favQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                favouritesArrayList = new ArrayList<Restaurant>();
                for(int i=0; i<objects.size(); i++){

                    Restaurant restaurantObj = new Restaurant(
                            objects.get(i).getString("placeId"),
                            objects.get(i).getString("imageURL"),
                            objects.get(i).getString("name"),
                            objects.get(i).getDouble("rating"),
                            objects.get(i).getString("price"),
                            objects.get(i).getString("vicinity"));

                    favouritesArrayList.add(restaurantObj);
                }

                ListView favouriteListView = (ListView) getView().findViewById(R.id.favouriteList);

                favouritesListViewAdapter = new FavouritesListViewAdapter(getContext(), favouritesArrayList);
                favouriteListView.setAdapter(favouritesListViewAdapter);
                try {
                    favouriteListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                            Intent restaurantDetailsPage = new Intent(getContext(), RestaurantDetailsActivity.class);
                            restaurantDetailsPage.putExtra("RestaurantDetailObj", (Restaurant) adapterView.getAdapter().getItem(position));

                            QueryFavourite queryFavourite = new QueryFavourite();
                            ParseUser currentUser = ParseUser.getCurrentUser();
                            restaurantDetailsPage.putExtra("isFavouriteExist", queryFavourite.isFavouriteExist((Restaurant) adapterView.getAdapter().getItem(position),currentUser));

                            startActivity(restaurantDetailsPage);
                        }
                    });
                }
                catch(Exception ex) {
                    ex.printStackTrace();
                }


            }
        });

        if (swipeRefreshLayout.isRefreshing()) {
            Handler mHandler = new Handler();

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }, 700);

        }

    }

}