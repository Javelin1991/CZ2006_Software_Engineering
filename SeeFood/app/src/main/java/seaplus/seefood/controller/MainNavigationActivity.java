package seaplus.seefood.controller;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import seaplus.seefood.R;
import seaplus.seefood.model.Restaurant;
import seaplus.seefood.model.User;

import static com.facebook.AccessToken.getCurrentAccessToken;
import static seaplus.seefood.controller.AppConfig.GOOGLE_API_KEY;

public class MainNavigationActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;
    private Toolbar toolbar;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;
    private Fragment newFragment;
    private final Handler appDrawerHandler = new Handler();
    public static final int TYPE_RESTAURANT = 79;

    private ImageView profile_image;
    private TextView username;
    private boolean logoutChecker = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_navigation);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        displaySelectedScreen(R.id.nav_home);


        //if current user is logged in using fb
        boolean flag = false;
        if(isFBLoggedIn()){

            //the user is logged in via facebook, so load the info from facebook
            displayFacebookProfileInfo();
            flag = true;
        }else {

            //the user is not logged in via facebook yet, hence, load the fb info from account fragment
            getFBuserFromAccountFragment();
        }

        //if current user is logged in using normal login
        if(ParseUser.getCurrentUser() != null && !flag && ParseUser.getCurrentUser().get("name") != null){
            hidePreviousHeaderView();
            final View headerView = navigationView.inflateHeaderView(R.layout.nav_header_main_navigation);
            username = (TextView) headerView.findViewById(R.id.name);
            username.setText(ParseUser.getCurrentUser().get("name").toString());
        }
    }


    public void getFBuserFromAccountFragment(){
        RequestOptions imgOptions;
        User user = (User) getIntent().getParcelableExtra("UserObj");

        if(user != null) {
            //to hide the previous header when the user logout and login again
            hidePreviousHeaderView();

            final View headerView = navigationView.inflateHeaderView(R.layout.nav_header_main_navigation);
            username = (TextView) headerView.findViewById(R.id.name);
            username.setText(user.getUsername());

            if (!user.getUrl().equals("")) {
                imgOptions = new RequestOptions()
                        .override(800, 800)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL);

                Glide.with(this).load(user.getUrl())
                        .apply(imgOptions)
                        .into((ImageView) headerView.findViewById(R.id.profile_image));
            }
        }

    }

    //checking if a user is logged in using facebook or not
    public boolean isFBLoggedIn(){
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return (accessToken != null);
    }



    private void openAutocompleteActivity() {
        try {
            // The autocomplete activity requires Google Play Services to be available. The intent
            // builder checks this and throws an exception if it is not the case.
            AutocompleteFilter typeFilter = new AutocompleteFilter.Builder().setCountry("SG").setTypeFilter(AutocompleteFilter.TYPE_FILTER_ESTABLISHMENT).build();

            Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN).setFilter(typeFilter).build(this);
            startActivityForResult(intent, 1);
        } catch (GooglePlayServicesRepairableException e) {
            // Indicates that Google Play Services is either not installed or not up to date. Prompt
            // the user to correct the issue.
            GoogleApiAvailability.getInstance().getErrorDialog(this, e.getConnectionStatusCode(),
                    0 /* requestCode */).show();
        } catch (GooglePlayServicesNotAvailableException e) {
            // Indicates that Google Play Services is not available and the problem is not easily
            // resolvable.
            String message = "Google Play Services is not available: " + GoogleApiAvailability.getInstance().getErrorString(e.errorCode);

            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    //trying to display facebook profile when already logged in
    public void displayFacebookProfileInfo(){

        //getting user info from FB by GraphRequest
        GraphRequest request = GraphRequest.newMeRequest(getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        try {
                            String email = object.getString("email");
                            String fbusername = object.getString("name");
                            String fbuserId = object.getString("id");

                            String tmpURL = "https://graph.facebook.com/" + fbuserId + "/picture?type=large";

                            hidePreviousHeaderView();
                            showFacebookUserinfo(fbusername, tmpURL);

                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                            // } catch (MalformedURLException e) {
                            //   e.printStackTrace();
                        }

                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,email,gender,birthday");
        request.setParameters(parameters);
        request.executeAsync();
    }
    //trying to display facebook profile when already logged in
    public void showFacebookUserinfo(String name, String tmpURL) {

        RequestOptions imgOptions;

        final View headerView = navigationView.inflateHeaderView(R.layout.nav_header_main_navigation);
        username = (TextView) headerView.findViewById(R.id.name);
        username.setText(name);

        imgOptions = new RequestOptions()
                .override(800, 800)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL);

        Glide.with(this).load(tmpURL)
                .apply(imgOptions)
                .into((ImageView) headerView.findViewById(R.id.profile_image));
    }

    public void hidePreviousHeaderView(){
        if (navigationView != null) {
            for (int i = 0; i < navigationView.getHeaderCount(); i++) {
                if (navigationView.getHeaderView(i) != null)
                    navigationView.getHeaderView(i).setVisibility(View.GONE);
                if (i == 0){
                    navigationView.getHeaderView(i).setVisibility(View.GONE);
                }
            }
        }
    }

    /**
     * Called after the autocomplete activity has finished to return its result.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check that the result was from the autocomplete widget.
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                // Get the user's selected place from the Intent.
                Place place = PlaceAutocomplete.getPlace(this, data);
                String placeID = place.getId();
                String photoURL = "http://www.ozarlington.com/wp-content/uploads/2017/04/bar-buffet.jpg";
                String name = place.getName().toString();
                double rating = Double.parseDouble(String.format("%.2f", place.getRating()));
                String price = "$";
                String vicinity = place.getAddress().toString();

                try {
                    JSONObject placeObj = new JSONObject(AppConfig.readUrl("https://maps.googleapis.com/maps/api/place/details/json?key=" + GOOGLE_API_KEY + "&placeid=" + placeID));
                    JSONObject result = placeObj.getJSONObject("result");
                    JSONArray photosArray = result.getJSONArray("photos");
                    String photoReference = photosArray.getJSONObject(0).getString("photo_reference");
                    photoURL = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=9999&photoreference="
                                + photoReference + "&key=" + GOOGLE_API_KEY;
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Restaurant restaruantObj = new Restaurant(placeID, photoURL, name, rating, price, vicinity);
                // Store the restaurant details in an array list
                Intent restaurantDetailsPage = new Intent(this, RestaurantDetailsActivity.class);
                restaurantDetailsPage.putExtra("RestaurantDetailObj", restaruantObj);
                restaurantDetailsPage.putExtra("CLocationObj", getIntent().getParcelableExtra("CLocationObj"));
                startActivity(restaurantDetailsPage);
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);

            } else if (resultCode == RESULT_CANCELED) {
                // Indicates that the activity closed before a selection was made. For example if
                // the user pressed the back button.
            }
        }
    }

    @Override
    public void onBackPressed() {
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    //on selected item in navigation menu display fragment
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        displaySelectedScreen(item.getItemId());
        return true;
    }

    private void displaySelectedScreen(int itemId) {
        //creating fragment object
        newFragment = null;

        switch (itemId) {
            case R.id.nav_home:
                newFragment = new HomeFragment();
                break;
            case R.id.nav_account:
                loginProcess();
                break;
            case R.id.nav_favourites:
                goToFavourite();
                break;
            case R.id.action_logout:
                logOutUser();
                break;
            default:
                newFragment = new HomeFragment();

        }

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        if (newFragment != null) {
            RunnableReplaceFragment(newFragment);
        }
    }

    public void goToFavourite(){
        Profile profile = Profile.getCurrentProfile();
        ParseUser currentUser = ParseUser.getCurrentUser();



        if (!isFBLoggedIn() && currentUser == null){
            displayToast("Hi there, please login to unlock the favourite feature");
        }
        else
        {
            newFragment = new FavouriteFragment();
        }


    }

    //if the login button is pressed, execute the method below
    public void loginProcess(){
        Profile profile = Profile.getCurrentProfile();
        ParseUser currentUser = ParseUser.getCurrentUser();

        boolean flag = false;
        if(isFBLoggedIn()){
            displayToast("Hello " + profile.getName()
                    + " ,you're already logged in. Enjoy using our app!");
            flag = true;
        }

        if (currentUser != null && !flag && currentUser.get("name") != null) {
            displayToast("Hello " + currentUser.get("name")
                    + " ,you're already logged in. Enjoy using our app!");
        }else{
            if(!isFBLoggedIn()) {
                newFragment = new AccountFragment();
            }
        }

        // newFragment = new AccountFragment();
    }

    //if the logout button is pressed, execute the method below
    public void logOutUser(){

        Profile profile = Profile.getCurrentProfile();
        ParseUser currentUser = ParseUser.getCurrentUser();

        boolean flag = false;
        if(isFBLoggedIn()){
            displayToast("Goodbye " + profile.getName() + ", hope to see you again!");
            flag = true;
            LoginManager.getInstance().logOut();
            ParseUser.logOut();
            newFragment = new HomeFragment();
        }

        if (currentUser != null && !flag && currentUser.get("name") != null) {
            displayToast("Goodbye " + currentUser.get("name") + ", hope to see you again!");
            ParseUser.logOut();
            newFragment = new HomeFragment();
        }

        //setting currentUser = null after logging out (mandatory line of code)
        currentUser = ParseUser.getCurrentUser();
        //to hide the previous header when the user logout and login again
        hidePreviousHeaderAfterLogOut();

    }

    //hiding the previous header to avoid duplicated header views
    public void hidePreviousHeaderAfterLogOut(){

        if (navigationView != null) {
            for (int i = 0; i < navigationView.getHeaderCount(); i++) {
                if (navigationView.getHeaderView(i) != null)
                    navigationView.getHeaderView(i).setVisibility(View.GONE);
                if (i == 0){
                    navigationView.getHeaderView(i).setVisibility(View.VISIBLE);
                }
            }
        }
    }

    //show required display message
    public void displayToast(String s){
        CharSequence text = s;
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(this, text, duration);
        TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
        if( v != null) v.setGravity(Gravity.CENTER);
        toast.show();
    }


    //create delay when replacing fragment to ensure smooth navigation
    public void RunnableReplaceFragment(final Fragment newFragment) {
        appDrawerHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                replaceFragment(newFragment);
            }
        }, 0);
    }

    //Replace Fragment
    public void replaceFragment(Fragment newFragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        // fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        fragmentTransaction.replace(R.id.content_frame, newFragment);
        fragmentTransaction.commit();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.action_button_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.filter_btn:
                Intent filterSortIntent = new Intent(this, FilterSortActivity.class);
                filterSortIntent.putExtra("CLocationObj", getIntent().getParcelableExtra("CLocationObj"));
                startActivity(filterSortIntent);

                return true;
            case R.id.search_btn:
                openAutocompleteActivity();
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }
}