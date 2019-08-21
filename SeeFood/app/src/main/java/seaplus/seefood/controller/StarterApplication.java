package seaplus.seefood.controller;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseACL;

/**
 * Created by htetnaing on 18/10/17.
 */

public class StarterApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);

        // Add your initialization code here
        Parse.initialize(new Parse.Configuration.Builder(getApplicationContext())
                .applicationId("ff7ad7879d36e4fc1e5f4bc272c8e8236376092c")
                .clientKey("7906139105fd34697030e125a1003915ba92dc26")
                .server("http://18.221.252.68:80/parse/")
                .build()
        );

        //ParseUser.enableAutomaticUser();

        ParseACL defaultACL = new ParseACL();
        defaultACL.setPublicReadAccess(true);
        defaultACL.setPublicWriteAccess(true);
        ParseACL.setDefaultACL(defaultACL, true);

    }
}
