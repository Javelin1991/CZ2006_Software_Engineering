package seaplus.seefood.controller;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.parse.LogInCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import seaplus.seefood.R;
import seaplus.seefood.model.User;

import static com.facebook.AccessToken.getCurrentAccessToken;

public class FacebookLoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facebook_login);

        initializeVariables();
        manageFBuserLogin();
    }

    //getting callback result from facebook
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
        //callbackManager.onActivityResult(requestCode, resultCode, data);
    }




    public void initializeVariables() {

        //Log.i("Fifth Node", "This is while initializing variables");
        //running on UiThread to reduce workload on the main thread
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    //fb libraries setup and current user login check
                    ParseFacebookUtils.initialize(getApplicationContext());

                    //necessary setup
                    ParseAnalytics.trackAppOpenedInBackground(getIntent());
                } catch (Exception e) {
                    Log.i("Error Node", "It's inside initializeV");
                    Log.i("Error", e.getMessage());
                    //Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
                }
            }

        });

        //callbackManager = CallbackManager.Factory.create();

    }

    //call this method when the FB login button is pressed
    private void manageFBuserLogin() {
        ParseFacebookUtils.logInWithReadPermissionsInBackground(this,
                Arrays.asList("email", "public_profile"),
                new LogInCallback() {

                    @Override
                    public void done(ParseUser user, ParseException err) {

                        if (user == null) {
                            Log.d("MyApp", "Uh oh. The user cancelled the Facebook login.");
                            //fbtn.setVisibility(View.VISIBLE);
                        } else if (user.isNew()) {
                            Log.d("MyApp", "User signed up and logged in through Facebook!");
                            displayNameAndEmail();
                        } else {
                            Log.d("MyApp", "User logged in through Facebook!");
                            displayNameAndEmail();
                            //logOutUser();
                        }
                    }
                });
    }

    public void displayNameAndEmail(){
        //getting user info from FB by GraphRequest
        GraphRequest request = GraphRequest.newMeRequest(getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        try {
                            String email = object.getString("email");
                            String username = object.getString("name");
                            String userId = object.getString("id");
                            Log.i("FB email ", email);
                            Log.i("FB username ", username);
                            Log.i("FB userId", userId);

                            String tmpURL = "https://graph.facebook.com/" + userId + "/picture?type=large";
                            User user = new User(username,tmpURL);
                            Intent mainNavigationPage = new Intent(getApplicationContext(), MainNavigationActivity.class);
                            mainNavigationPage.putExtra("UserObj", user);
                            mainNavigationPage.putExtra("CLocationObj", getIntent().getParcelableExtra("CLocationObj"));
                            startActivity(mainNavigationPage);

                            //remove from stack
                            finish();
                        } catch (JSONException e) {

                            e.printStackTrace();

                        }

                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,email,gender,birthday");
        request.setParameters(parameters);
        request.executeAsync();
    }
}
