package seaplus.seefood.controller;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

import seaplus.seefood.R;
import seaplus.seefood.model.CLocation;
import seaplus.seefood.model.Restaurant;

import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;
import static seaplus.seefood.controller.AppConfig.GEOMETRY;
import static seaplus.seefood.controller.AppConfig.GOOGLE_API_KEY;
import static seaplus.seefood.controller.AppConfig.LATITUDE;
import static seaplus.seefood.controller.AppConfig.LOCATION;
import static seaplus.seefood.controller.AppConfig.LONGITUDE;
import static seaplus.seefood.controller.AppConfig.NAME;
import static seaplus.seefood.controller.AppConfig.OK;
import static seaplus.seefood.controller.AppConfig.PHOTOS;
import static seaplus.seefood.controller.AppConfig.PLACE_ID;
import static seaplus.seefood.controller.AppConfig.PRICE_LEVEL;
import static seaplus.seefood.controller.AppConfig.PROXIMITY_RADIUS;
import static seaplus.seefood.controller.AppConfig.RATING;
import static seaplus.seefood.controller.AppConfig.REFERENCE;
import static seaplus.seefood.controller.AppConfig.STATUS;
import static seaplus.seefood.controller.AppConfig.VICINITY;
import static seaplus.seefood.controller.AppConfig.ZERO_RESULTS;

public class HomeFragment extends Fragment {
    //ToDo load place details from google place api

    public ArrayList<String> restaurantPlaceID = new ArrayList<String>();
    public ArrayList<String> restaurantName = new ArrayList<String>();
    public ArrayList<String> restaurantPrice = new ArrayList<String>();
    public ArrayList<String> restaurantImageList = new ArrayList<String>();
    public ArrayList<Double> restaurantRating = new ArrayList<Double>();
    public ArrayList<String> restaurantVicinity = new ArrayList<String>();
    public ArrayList<Object> filterSortList = new ArrayList<Object>();

    private ArrayList<Restaurant> restaurantArrayList;
    private RestaurantListViewAdapter restaurantListViewAdapter;

    private SwipeRefreshLayout swipeRefreshLayout;

    SplashActivity currentLocation = new SplashActivity();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //returning our layout file
        //change R.layout.yourlayoutfilename for each of your fragments
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //set toolbar title
        getActivity().setTitle("Home");

        swipeRefreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swipeRefresh);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                restaurantName.clear();
                restaurantPrice.clear();
                restaurantImageList.clear();
                restaurantRating.clear();
                restaurantVicinity.clear();
                populateRestaurant();
            }
        });
        populateRestaurant();
    }

    private void populateRestaurant(){
        loadNearByPlaces();

        ListView restaurantListView = (ListView) getView().findViewById(R.id.restaurantList);
        restaurantArrayList = new ArrayList<Restaurant>();

        for (int i = 0; i < restaurantName.size(); i++) {
            Restaurant restaurantObj = new Restaurant(restaurantPlaceID.get(i), restaurantImageList.get(i), restaurantName.get(i), restaurantRating.get(i), restaurantPrice.get(i), restaurantVicinity.get(i));
            restaurantArrayList.add(restaurantObj);
        }

        // Sorting
        sortRestaurants();

        restaurantListViewAdapter = new RestaurantListViewAdapter(getContext(), restaurantArrayList);

        restaurantListView.setAdapter(restaurantListViewAdapter);

        restaurantListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Intent restaurantDetailsPage = new Intent(getContext(), RestaurantDetailsActivity.class);
                restaurantDetailsPage.putExtra("RestaurantDetailObj", (Restaurant) adapterView.getAdapter().getItem(position));
                restaurantDetailsPage.putExtra("CLocationObj", getActivity().getIntent().getParcelableExtra("CLocationObj"));

                QueryFavourite queryFavourite = new QueryFavourite();
                ParseUser currentUser = ParseUser.getCurrentUser();
                if (currentUser != null) {
                    //String placeId = ((Restaurant) adapterView.getAdapter().getItem(position)).getPlaceID();
                    restaurantDetailsPage.putExtra("isFavouriteExist", queryFavourite.isFavouriteExist(((Restaurant) adapterView.getAdapter().getItem(position)),currentUser));
                }
                startActivity(restaurantDetailsPage);
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

    private void sortRestaurants(){
        // Sorting
        String sortValue = "None";
        Bundle extra = getActivity().getIntent().getBundleExtra("extra");
        if (extra != null) {
            filterSortList = (ArrayList<Object>) extra.getSerializable("FilterSortList");
            sortValue = filterSortList.get(4).toString();
        }
        if (sortValue.equals("Alphabetical (A -> Z)") || sortValue.equals("Alphabetical (Z -> A)") ) {
            Collections.sort(restaurantArrayList, Restaurant.RestaruantNameComparator);
        }
        else if (sortValue.equals("Price (Low to High)") || sortValue.equals("Price (High to Low)")) {
            Collections.sort(restaurantArrayList, Restaurant.RestaruantPriceComparator);
        }
        else if (sortValue.equals("Rating (Low to High)") || sortValue.equals("Rating (High to Low)")) {
            Collections.sort(restaurantArrayList);
        }
        // Descending Order
        if (sortValue.equals("Alphabetical (Z -> A)") || sortValue.equals("Price (High to Low)")|| sortValue.equals("Rating (High to Low)")) {
            Collections.reverse(restaurantArrayList);
        }
    }

    private void loadNearByPlaces() {
        CLocation cLocationObj = (CLocation) getActivity().getIntent().getParcelableExtra("CLocationObj");
        double latitude = cLocationObj.getLatitude();
        double longitude = cLocationObj.getLongitude();
        //double latitude = 37.757241; //1.3483153; // 37.757241
        //double longitude = -122.3983142; //103.680946; // -122.3983142

        String type = "restaurant";
        int numOfTypes = 0;
        do {
            StringBuilder googlePlacesUrl =
                    new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
            googlePlacesUrl.append("location=").append(latitude).append(",").append(longitude);
            googlePlacesUrl.append("&radius=").append(PROXIMITY_RADIUS);
            googlePlacesUrl.append("&types=").append(type);
            googlePlacesUrl.append("&sensor=true");
            googlePlacesUrl.append("&key=" + GOOGLE_API_KEY);

            try {
                JSONObject json = new JSONObject(AppConfig.readUrl(googlePlacesUrl.toString()));
                parseLocationResult(json);
            } catch (Exception e) {
                e.printStackTrace();
            }
            type = "cafe";
            numOfTypes++;
        }while(numOfTypes < 2);
    }

    private void parseLocationResult(JSONObject result) {

        String id, place_id, placeName = null, reference, photoURL, photoReference, vicinity, priceRange = null;
        double latitude, longitude, rating;
        JSONArray photosArray = new JSONArray();
        int price_level = 0;

        String startPrice, endPrice;
        double startRating, endRating;

        Bundle extra = getActivity().getIntent().getBundleExtra("extra");
        if (extra != null) {
            filterSortList = (ArrayList<Object>) extra.getSerializable("FilterSortList");
            startPrice = filterSortList.get(0).toString();
            endPrice = filterSortList.get(1).toString();
            startRating = Double.parseDouble(filterSortList.get(2).toString());
            endRating = Double.parseDouble(filterSortList.get(3).toString());
        }
        else{
            // No specific filterSort
            startPrice = "$";
            endPrice = "$$$$$";
            startRating = 0.0;
            endRating = 5.0;
        }

        try {
            JSONArray jsonArray = result.getJSONArray("results");

            // Restaruants found!
            if (result.getString(STATUS).equalsIgnoreCase(OK))
                for (int i = 0; i < jsonArray.length() - 10; i++) {
                    JSONObject place = jsonArray.getJSONObject(i);

                    if (!place.isNull(RATING)) {
                        rating = Double.parseDouble(place.getString(RATING));
                    } else {
                        rating = 0.0;
                    }
                    // Filtering based on ratings
                    if (startRating <= rating && rating <= endRating){
                        restaurantRating.add(rating);
                    }
                    else{
                        continue;
                    }

                    if (!place.isNull(PRICE_LEVEL)) {
                        price_level = Integer.parseInt(place.getString(PRICE_LEVEL));
                        switch (price_level) {
                            case 0:
                                priceRange = "$";
                                break;
                            case 1:
                                priceRange = "$";
                                break;
                            case 2:
                                priceRange = "$$";
                                break;
                            case 3:
                                priceRange = "$$$";
                                break;
                            case 4:
                                priceRange = "$$$$";
                                break;
                            case 5:
                                priceRange = "$$$$$";
                                break;
                            default:
                                priceRange = "$";
                        }
                    } else {
                        priceRange = "$";
                    }

                    // Filtering based on pricerange
                    if (startPrice.length() <= priceRange.length() && priceRange.length() <= endPrice.length()){
                        restaurantPrice.add(priceRange);
                    }
                    else{
                        continue;
                    }

                    place_id = place.getString(PLACE_ID);
                    restaurantPlaceID.add(place_id);
                    if (!place.isNull(NAME)) {
                        placeName = place.getString(NAME);
                        restaurantName.add(placeName);
                    } else {
                        restaurantName.add("-");
                    }
                    if (!place.isNull(VICINITY)) {
                        vicinity = place.getString(VICINITY);
                        restaurantVicinity.add(vicinity);
                    }
                    latitude = place.getJSONObject(GEOMETRY).getJSONObject(LOCATION)
                            .getDouble(LATITUDE);
                    longitude = place.getJSONObject(GEOMETRY).getJSONObject(LOCATION)
                            .getDouble(LONGITUDE);
                    reference = place.getString(REFERENCE);

                    if (!place.isNull(PHOTOS)) {
                        photosArray = place.getJSONArray(PHOTOS);
                        photoReference = photosArray.getJSONObject(0).getString("photo_reference");
                        photoURL = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=9999&photoreference="
                                + photoReference + "&key=" + GOOGLE_API_KEY;
                        restaurantImageList.add(photoURL);
                    } else {
                        restaurantImageList.add("http://www.ozarlington.com/wp-content/uploads/2017/04/bar-buffet.jpg");
                    }
                    //markerOptions.position(latLng);
                    //markerOptions.title(placeName + " : " + vicinity);
                }
            else if (result.getString(STATUS).equalsIgnoreCase(ZERO_RESULTS)) {
                // No Restaruants found in radius!!!

            }

        } catch (JSONException e) {

            e.printStackTrace();
            Log.e(TAG, "parseLocationResult: Error=" + e.getMessage());
        }
    }
}