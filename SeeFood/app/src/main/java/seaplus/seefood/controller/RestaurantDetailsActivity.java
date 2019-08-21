package seaplus.seefood.controller;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

import seaplus.seefood.R;
import seaplus.seefood.model.Restaurant;

import static seaplus.seefood.controller.AppConfig.FORMATTED_ADDRESS;
import static seaplus.seefood.controller.AppConfig.GOOGLE_API_KEY;
import static seaplus.seefood.controller.AppConfig.INTERNATIONAL_PHONE_NUMBER;
import static seaplus.seefood.controller.AppConfig.OK;
import static seaplus.seefood.controller.AppConfig.OPENING_HOURS;
import static seaplus.seefood.controller.AppConfig.OPEN_NOW;
import static seaplus.seefood.controller.AppConfig.STATUS;
import static seaplus.seefood.controller.AppConfig.TAG;
import static seaplus.seefood.controller.AppConfig.URL;
import static seaplus.seefood.controller.AppConfig.WEEKDAY_TEXT;
import static seaplus.seefood.controller.AppConfig.ZERO_RESULTS;

public class RestaurantDetailsActivity extends AppCompatActivity {

    public ArrayList<String> restaurantDetailsArray = new ArrayList<String>();

    private RequestOptions imgOptions;
    private TextView restaurantName;
    private TextView restaurantRating;
    private TextView restaurantPrice;
    private TextView restaurantAddress;
    private TextView restaurantOpeningHours;
    private TextView restaurantOpenNow;
    private ImageView carparkBtn;
    private ImageView locationBtn;
    private ImageView phoneBtn;
    private ImageView shareBtn;
    private ToggleButton favouriteBtn;
    private String mapURL;
    private String internationalPhoneNumber;

    private Restaurant restaurantObj;

    private RestaurantListViewAdapter restaurantListViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_details);


        // my_child_toolbar is defined in the layout file
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowTitleEnabled(false);

        if (getIntent().hasExtra("isFavouriteExist")) {
            //Get the bundle
            Bundle isFavouriteBundle = getIntent().getExtras();
            //Extract the data…
            boolean isFavourite = isFavouriteBundle.getBoolean("isFavouriteExist");

            //check if favourite exist and set toggle accordingly
            setFavouriteToggle(isFavourite);
        }

        if (getIntent().hasExtra("RestaurantDetailObj")) {
            Intent restaurantDetailsIntent = getIntent();
            restaurantObj = (Restaurant) restaurantDetailsIntent.getParcelableExtra("RestaurantDetailObj");

            loadPlaceDetails();

            attachedOnclickListener();
        }
    }

    private void attachedOnclickListener() {
        carparkBtn = (ImageView) (findViewById(R.id.carparkButton));
        carparkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), CarparkActivity.class);
                intent.putExtra("CLocationObj", getIntent().getParcelableExtra("CLocationObj"));
                intent.putExtra("PlaceID", restaurantObj.getPlaceID());
                intent.putExtra("PlaceName", restaurantObj.getName());
                startActivity(intent);
            }
        });

        phoneBtn = (ImageView) (findViewById(R.id.phoneButton));
        phoneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", internationalPhoneNumber, null));
                startActivity(intent);
            }
        });

        locationBtn = (ImageView)(findViewById(R.id.locationButton));
        locationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse(mapURL));
                startActivity(intent);
            }
        });

        shareBtn = (ImageView)(findViewById(R.id.shareButton));
        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent waIntent = new Intent(Intent.ACTION_SEND);
                waIntent.setType("text/plain");
                if (waIntent != null) {
                    waIntent.putExtra(
                            Intent.EXTRA_TEXT,
                            "*Shared using SeeFOOD:*\n\n" +
                                    restaurantName.getText() + "\n" +
                                    restaurantAddress.getText() + "\n" +
                                    internationalPhoneNumber + "\n\n" +
                                    mapURL);
                    startActivity(Intent.createChooser(waIntent, "Share using"));
                } else {
                    Toast.makeText(getApplicationContext(), "WhatsApp not Installed", Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });



        favouriteBtn = (ToggleButton) (findViewById(R.id.favouriteButton));
        favouriteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ParseUser currentUser = ParseUser.getCurrentUser();

                if (currentUser != null) {
                    QueryFavourite queryFavourite = new QueryFavourite();
                    if (favouriteBtn.isChecked()) {
                       // queryFavourite.addFavourite(restaurantObj.getPlaceID(), restaurantObj.getImage(), restaurantObj.getName(),
                               // restaurantObj.getPrice(), restaurantObj.getRating(), restaurantObj.getVicinity(), currentUser.getUsername());
                        queryFavourite.addFavourite(restaurantObj, currentUser);
                        Toast toast = Toast.makeText(getApplicationContext(), "Restuarant Favourited", Toast.LENGTH_SHORT);
                        toast.show();
                    } else

                    {
                        //queryFavourite.removeFavourite(currentUser.getUsername(), restaurantObj.getPlaceID());
                        queryFavourite.removeFavourite(restaurantObj, currentUser);
                        Toast toast = Toast.makeText(getApplicationContext(), "Restuarant Removed", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }

                else{
                    favouriteBtn.setChecked(false);
                    Toast toast = Toast.makeText(getApplicationContext(), "Hi, please login to use our favourite feature!", Toast.LENGTH_SHORT);
                    toast.show();
                }

            }
        });
    }

    private void setFavouriteToggle(boolean isFavourite){
        favouriteBtn = (ToggleButton) (findViewById(R.id.favouriteButton));;
        favouriteBtn.setChecked(isFavourite);
    }

    private void loadPlaceDetails() {
        restaurantName = (TextView) (findViewById(R.id.restaurantName));
        restaurantName.setText(restaurantObj.getName());

        restaurantRating = (TextView) (findViewById(R.id.restaurantRating)) ;
        restaurantRating.setText(Double.toString(restaurantObj.getRating()));

        restaurantPrice = (TextView) (findViewById(R.id.restaurantPrice)) ;
        restaurantPrice.setText(restaurantObj.getPrice());

        //download & display image from url using glide library
        imgOptions = new RequestOptions()
                .override(800,800)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL);

        Glide.with(this).load(restaurantObj.getImage())
                .apply(imgOptions)
                .into((ImageView) findViewById(R.id.restaurantPic));

        String type = "restaurant";
        StringBuilder googlePlacesUrl =
                new StringBuilder("https://maps.googleapis.com/maps/api/place/details/json?");
        googlePlacesUrl.append("placeid=").append(restaurantObj.getPlaceID());
        googlePlacesUrl.append("&key=" + GOOGLE_API_KEY);

        try {
            JSONObject json = new JSONObject(AppConfig.readUrl(googlePlacesUrl.toString()));
            parsePlaceDetailsResult(json);
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int i = 0; i < restaurantDetailsArray.size(); i++){
            switch(i){
                case 0: // internationalPhoneNumber
                    internationalPhoneNumber = restaurantDetailsArray.get(i);
                    break;
                case 1: // mapURL
                    mapURL = restaurantDetailsArray.get(i);
                    break;
                case 2: // address
                    restaurantAddress = (TextView) (findViewById(R.id.restaurantAddress)) ;
                    restaurantAddress.setText(restaurantDetailsArray.get(i));
                    break;
                case 3: // open_now
                    restaurantOpenNow = (TextView) (findViewById(R.id.openNow)) ;
                    if (restaurantDetailsArray.get(i) == "true"){
                        restaurantOpenNow.setTextColor(Color.GREEN);
                        restaurantOpenNow.setText("Open Now");
                    }
                    else{
                        restaurantOpenNow.setTextColor(Color.RED);
                        restaurantOpenNow.setText("Closed Now");
                    }
                    break;
                case 4: // openingHours
                    // get the listview
                    restaurantOpeningHours = (TextView) (findViewById(R.id.restaurantOpeningHours));
                    restaurantOpeningHours.setText(Html.fromHtml(restaurantDetailsArray.get(i)));
                    break;
            }
        }
    }

    private void parsePlaceDetailsResult(JSONObject result) {

        String internationalPhoneNumber, mapURL, address, open_now;
        JSONArray weekdayTextArray = new JSONArray();

        try {
            JSONObject placeDetail = result.getJSONObject("result");

            // Restaruant Details found!
            if (result.getString(STATUS).equalsIgnoreCase(OK)){
                if (!placeDetail.isNull(INTERNATIONAL_PHONE_NUMBER)) {
                    internationalPhoneNumber = placeDetail.getString(INTERNATIONAL_PHONE_NUMBER);
                    restaurantDetailsArray.add(internationalPhoneNumber);
                } else {
                    restaurantDetailsArray.add("-");
                }
                if (!placeDetail.isNull(URL)) {
                    mapURL = placeDetail.getString(URL);
                    restaurantDetailsArray.add(mapURL);
                } else {
                    restaurantDetailsArray.add("-");
                }
                if (!placeDetail.isNull(FORMATTED_ADDRESS)) {
                    address = placeDetail.getString(FORMATTED_ADDRESS);
                    restaurantDetailsArray.add(address);
                } else {
                    restaurantDetailsArray.add("-");
                }

                open_now = placeDetail.getJSONObject(OPENING_HOURS).getString(OPEN_NOW);
                restaurantDetailsArray.add(open_now);

                weekdayTextArray = placeDetail.getJSONObject(OPENING_HOURS).getJSONArray(WEEKDAY_TEXT);
                StringBuilder openingHours = new StringBuilder("");
                for (int i = 0; i < weekdayTextArray.length(); i++) {
                    String openingHoursText = weekdayTextArray.getString(i);
                    openingHoursText = openingHoursText.replaceAll("Monday", "Mon\t");
                    openingHoursText = openingHoursText.replaceAll("Tuesday", "Tue\t\t");
                    openingHoursText = openingHoursText.replaceAll("Wednesday", "Wed\t");
                    openingHoursText = openingHoursText.replaceAll("Thursday", "Thu\t\t");
                    openingHoursText = openingHoursText.replaceAll("Friday", "Fri\t\t\t");
                    openingHoursText = openingHoursText.replaceAll("Saturday", "Sat\t\t");
                    openingHoursText = openingHoursText.replaceAll("Sunday", "Sun\t\t");
                    openingHoursText = openingHoursText.replaceAll(" AM", "am");
                    openingHoursText = openingHoursText.replaceAll(" PM", "pm");

                    Calendar calendar = Calendar.getInstance();
                    int day = calendar.get(Calendar.DAY_OF_WEEK);

                    switch (day) {
                        // Current day is...
                        case Calendar.MONDAY:
                            if (openingHoursText.startsWith("Mon")) {
                                openingHoursText = "<span style=\"color: blue\"><b>" + openingHoursText + "</b></span>";
                            }
                            break;
                        case Calendar.TUESDAY:
                            if (openingHoursText.startsWith("Tue")) {
                                openingHoursText = "<span style=\"color: blue\"><b>" + openingHoursText + "</b></span>";
                            }
                            break;
                        case Calendar.WEDNESDAY:
                            if (openingHoursText.startsWith("Wed")) {
                                openingHoursText = "<span style=\"color: blue\"><b>" + openingHoursText + "</b></span>";
                            }
                            break;
                        case Calendar.THURSDAY:
                            if (openingHoursText.startsWith("Thu")) {
                                openingHoursText = "<span style=\"color: blue\"><b>" + openingHoursText + "</b></span>";
                            }
                            break;
                        case Calendar.FRIDAY:
                            if (openingHoursText.startsWith("Fri")) {
                                openingHoursText = "<span style=\"color: blue\"><b>" + openingHoursText + "</b></span>";
                            }
                            break;
                        case Calendar.SATURDAY:
                            if (openingHoursText.startsWith("Sat")) {
                                openingHoursText = "<span style=\"color: blue\"><b>" + openingHoursText + "</b></span>";
                            }
                            break;
                        case Calendar.SUNDAY:
                            if (openingHoursText.startsWith("Sun")) {
                                openingHoursText = "<span style=\"color: blue\"><b>" + openingHoursText + "</b></span>";
                            }
                            break;
                    }

                    if (i != weekdayTextArray.length() - 1) {
                        openingHours.append(openingHoursText + "<br>");
                    }
                    else{
                        openingHours.append(openingHoursText);
                    }
                }
                restaurantDetailsArray.add(openingHours.toString());
            }
            else if (result.getString(STATUS).equalsIgnoreCase(ZERO_RESULTS)) {
                // No Restaruant Details found !!!

            }

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "parseLocationResult: Error=" + e.getMessage());
        }
    }
}
