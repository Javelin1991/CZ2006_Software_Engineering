package seaplus.seefood.controller;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import seaplus.seefood.R;
import seaplus.seefood.model.CLocation;
import seaplus.seefood.model.Carpark;


public class CarparkActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    ArrayList<Carpark> carparkArrayList = new ArrayList<Carpark>();
    ArrayList<Carpark> finalnearestArrayList = new ArrayList<Carpark>();
    ArrayList<Float> distanceBetweenList = new ArrayList<Float>();
    String place_id;
    String selectedPlaceID = "";
    String placeName = "";
    double latitude, longitude;
    boolean newUpdateGov = false;
    CLocation locationObj;
    double eglat, eglong;
    boolean readFromGoogle = false;

    private TextView wkdayBef6pmRates;
    private TextView wkdayAft6pmRates;
    private TextView satRates;
    private TextView sunRates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carpark);

        // my_child_toolbar is defined in the layout file
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowTitleEnabled(false);
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (getIntent().hasExtra("PlaceID")) {
            Intent placeIntent = getIntent();
            locationObj = placeIntent.getParcelableExtra("CLocationObj");
            selectedPlaceID = placeIntent.getStringExtra("PlaceID");
            placeName = placeIntent.getStringExtra("PlaceName");
        }

        checkCarparkUpdate();
    }

    public void checkCarparkUpdate(){
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        parseCarparkResult();

        writeLongLatToFile();

        // Read longlat from resourse
        double lat;
        double longi;
        try {
            File sdcard = Environment.getExternalStorageDirectory();
            //Get the text file
            File file = new File(sdcard,"latlong.txt");
            InputStream fis = new FileInputStream(file);
            if (fis != null) {

                // prepare the file for reading
                InputStreamReader Reader = new InputStreamReader(fis);
                BufferedReader buffreader = new BufferedReader(Reader);

                String line = "";
                while(line != null) {
                    for (int j = 0; j < carparkArrayList.size(); j++) {
                        // read every line of the file into the line-variable, on line at the time
                        line = buffreader.readLine();
                        carparkArrayList.get(j).setLatitude(Double.parseDouble(line));
                        line = buffreader.readLine();
                        carparkArrayList.get(j).setLongitude(Double.parseDouble(line));

                        // do something with the line
                    }
                }
                fis.close();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        try {
            // Using Golden Mile Tower as dummy location to try distance
            JSONObject googleexampleObj = new JSONObject(AppConfig.readUrl("https://maps.googleapis.com/maps/api/place/details/json?placeid=" + selectedPlaceID + "&key=" + AppConfig.GOOGLE_API_KEY));
            JSONObject placeObjeg1 = googleexampleObj.getJSONObject("result");
            JSONObject placeObjeg2 = placeObjeg1.getJSONObject("geometry");
            JSONObject placeObjeg3 = placeObjeg2.getJSONObject("location");
            eglat = placeObjeg3.getDouble("lat");
            eglong = placeObjeg3.getDouble("lng");
            finalnearestArrayList = FindNearest(eglat, eglong);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void writeLongLatToFile(){
        if (newUpdateGov) {
            if (readFromGoogle) {
                for (int i = 0; i < carparkArrayList.size(); i++) {
                    String carparkName = carparkArrayList.get(i).getCarpark().replace(" ", "+");

                    try {
                        // To get Place ID
                        JSONObject googleObj = new JSONObject(AppConfig.readUrl("https://maps.googleapis.com/maps/api/place/queryautocomplete/json?key=" + AppConfig.GOOGLE_API_KEY + "&input=" + carparkName));
                        JSONArray carparkArray = googleObj.getJSONArray("predictions");
                        JSONObject carparkObj = carparkArray.getJSONObject(0);

                        place_id = carparkObj.getString("place_id");

                        // To Get Long & Lat & Set it to Carpark object
                        JSONObject placeObj = new JSONObject(AppConfig.readUrl("https://maps.googleapis.com/maps/api/place/details/json?key=" + AppConfig.GOOGLE_API_KEY + "&placeid=" + place_id));
                        JSONObject placeObj2 = placeObj.getJSONObject("result");
                        JSONObject placeObj3 = placeObj2.getJSONObject("geometry");
                        JSONObject placeObj4 = placeObj3.getJSONObject("location");
                        latitude = placeObj4.getDouble("lat");
                        longitude = placeObj4.getDouble("lng");

                        carparkArrayList.get(i).setLatitude(latitude);
                        carparkArrayList.get(i).setLongitude(longitude);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
            try {
                File sdcard = Environment.getExternalStorageDirectory();

                //Get the text file
                File file = new File(sdcard, "latlong.txt");
                FileOutputStream fOut = new FileOutputStream(file);
                OutputStreamWriter osw = new OutputStreamWriter(fOut);
                osw.close();
                FileOutputStream fOut2 = new FileOutputStream(file);
                OutputStreamWriter osw2 = new OutputStreamWriter(fOut2);

                for (int j = 0; j < carparkArrayList.size(); j++) {
                    if (readFromGoogle) {
                        // update longlat resourse file
                        osw2.write(Double.toString(carparkArrayList.get(j).getLatitude()));
                        osw2.write("\n");
                        osw2.write(Double.toString(carparkArrayList.get(j).getLongitude()));
                        osw2.write("\n");
                        //updateLongLatResFile(latitude,longitude);
                    }
                    else{
                        osw2.write(getLatLong());
                    }
                }
                osw2.flush();
                osw2.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        newUpdateGov = false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        //ToDo: get latitude and longtitude from govData
        double lat = 0;
        double lon = 0;
        String carparkName = "";

        for (int i = 0; i < finalnearestArrayList.size(); i++){
            lat = finalnearestArrayList.get(i).getLatitude();
            lon = finalnearestArrayList.get(i).getLongitude();
            carparkName = finalnearestArrayList.get(i).getCarpark();

            googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(lat, lon))
                    .title(carparkName));
        }

        // Add place location
        googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(eglat, eglong))
                .title(placeName)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

        CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(eglat,eglong));
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);

        googleMap.moveCamera(center);
        googleMap.animateCamera(zoom);

        // Set a listener for marker click.
        googleMap.setOnMarkerClickListener(this);
    }

    /**
     * handle marker click event
     */
    @Override
    public boolean onMarkerClick(Marker marker) {
        wkdayBef6pmRates = (TextView) (findViewById(R.id.wkdayBef6pmRates));
        wkdayAft6pmRates = (TextView) (findViewById(R.id.wkdayAft6pmRates));
        satRates = (TextView) (findViewById(R.id.satRates));
        sunRates = (TextView) (findViewById(R.id.sunRates));

        for (int i = 0; i < finalnearestArrayList.size(); i++){
            if (marker.getTitle().equals(finalnearestArrayList.get(i).getCarpark())){
                wkdayBef6pmRates.setText(finalnearestArrayList.get(i).getWeekdays_rate1());
                wkdayAft6pmRates.setText(finalnearestArrayList.get(i).getWeekdays_rate2());
                satRates.setText(finalnearestArrayList.get(i).getSaturday_rate());
                sunRates.setText(finalnearestArrayList.get(i).getSunday_public_holiday_rate());
            }
            else if (marker.getTitle().equals(placeName)){
                wkdayBef6pmRates.setText("-");
                wkdayAft6pmRates.setText("-");
                satRates.setText("-");
                sunRates.setText("-");
            }
        }
        return false;
    }

    private void parseCarparkResult() {
        String category, weekdays_rate1, weekdays_rate2, sunday_public_holiday_rate, carpark, saturday_rate;

        try {
            //String dataGovURL = "https://data.gov.sg/api/action/datastore_search?resource_id=e2468b11-6cac-42e4-8891-145c4fc1cba2&limit=5";
            //JSONObject result = new JSONObject(readUrl(dataGovURL));

            String dataGovURL = "https://data.gov.sg/api/action/datastore_search?resource_id=e2468b11-6cac-42e4-8891-145c4fc1cba2&limit=999";
            String test = AppConfig.readUrl(dataGovURL);
            String checkFromResJson = "";

            try {
                File sdcard = Environment.getExternalStorageDirectory();
                //Get the text file
                File file = new File(sdcard,"carparkjson.txt");
                InputStream fis = new FileInputStream(file);
                InputStreamReader Reader = new InputStreamReader(fis);
                BufferedReader buffreader = new BufferedReader(Reader);
                String temp;
                StringBuilder stringBuilder = new StringBuilder();
                while ( (temp = buffreader.readLine()) != null ) {
                    stringBuilder.append(temp);
                }
                fis.close();
                checkFromResJson = stringBuilder.toString();
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            if (!checkFromResJson.equals(test)){
                // update json resourse file
                //updateJsonResFile(readUrl(dataGovURL));
                try {
                    File sdcard = Environment.getExternalStorageDirectory();
                    //Get the text file
                    File file = new File(sdcard,"carparkjson.txt");
                    FileOutputStream output = new FileOutputStream(file);
                    OutputStreamWriter o = new OutputStreamWriter(output);
                    o.write(test);
                    o.flush();
                    o.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                newUpdateGov = true;
            }

            String fromResJson = "";

            try {
                File sdcard = Environment.getExternalStorageDirectory();
                //Get the text file
                File file = new File(sdcard,"carparkjson.txt");
                InputStream fis = new FileInputStream(file);
                InputStreamReader Reader = new InputStreamReader(fis);
                BufferedReader buffreader = new BufferedReader(Reader);
                String temp;
                StringBuilder stringBuilder = new StringBuilder();
                while ( (temp = buffreader.readLine()) != null ) {
                    stringBuilder.append(temp);
                    fromResJson = stringBuilder.toString();
                }
                fis.close();

            }
            catch (IOException e) {
                e.printStackTrace();
            }
            // read from json resourse file
            JSONObject result = new JSONObject(fromResJson);

            JSONObject resultObj = result.getJSONObject("result");
            JSONArray recordsArray = resultObj.getJSONArray("records");

            for (int i = 0; i < recordsArray.length(); i++) {
                JSONObject carparkObj = recordsArray.getJSONObject(i);

                category = carparkObj.getString("category");
                weekdays_rate1 = carparkObj.getString("weekdays_rate1");
                weekdays_rate2 = carparkObj.getString("weekdays_rate2");
                sunday_public_holiday_rate = carparkObj.getString("sunday_public_holiday_rate");
                carpark = carparkObj.getString("carpark");
                saturday_rate = carparkObj.getString("saturday_rate");

                carparkArrayList.add(new Carpark(category, weekdays_rate1, weekdays_rate2, sunday_public_holiday_rate, carpark, saturday_rate, 0, 0));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ArrayList FindNearest(double latt, double longg){
        ArrayList<Carpark> nearestArrayList = new ArrayList<Carpark>();
        double lat;
        double longti;
        boolean isExistCarpark = false;
        float [] dist = new float[1];
        for(int i=0; i < carparkArrayList.size(); i++)
        {
            lat = carparkArrayList.get(i).getLatitude();
            longti = carparkArrayList.get(i).getLongitude();

            Location.distanceBetween(lat, longti, latt, longg, dist);
            if(dist[0] < 800)
            {
                for (int j=0; j<nearestArrayList.size(); j++){
                    if (carparkArrayList.get(i).getCarpark().equals(nearestArrayList.get(j).getCarpark())){
                        isExistCarpark = true;
                    }
                }
                if (!isExistCarpark) {
                    nearestArrayList.add(carparkArrayList.get(i));
                    distanceBetweenList.add(dist[0]);
                    isExistCarpark = false;
                }
            }
        }
        return nearestArrayList;
    }

    public String getLatLong(){
        String latLong = "1.3027906\n" +
                "103.8653773\n" +
                "0.0\n" +
                "0.0\n" +
                "1.2902491\n" +
                "103.8351346\n" +
                "0.0\n" +
                "0.0\n" +
                "1.2925284\n" +
                "103.8500923\n" +
                "1.2847751\n" +
                "103.8474319\n" +
                "1.2940922\n" +
                "103.8308378\n" +
                "1.2621119\n" +
                "103.8179189\n" +
                "1.288878\n" +
                "103.834491\n" +
                "1.2814095\n" +
                "103.8510185\n" +
                "0.0\n" +
                "0.0\n" +
                "1.2752066\n" +
                "103.8441782\n" +
                "32.87063000000001\n" +
                "-117.1994792\n" +
                "0.0\n" +
                "0.0\n" +
                "0.0\n" +
                "0.0\n" +
                "27.9608744\n" +
                "-82.52308889999999\n" +
                "1.280314\n" +
                "103.8489857\n" +
                "0.0\n" +
                "0.0\n" +
                "0.0\n" +
                "0.0\n" +
                "0.0\n" +
                "0.0\n" +
                "1.3016902\n" +
                "103.8575302\n" +
                "1.2915711\n" +
                "103.8446322\n" +
                "1.273767\n" +
                "103.8450399\n" +
                "45.46947720000001\n" +
                "9.190932499999999\n" +
                "0.0\n" +
                "0.0\n" +
                "1.282302\n" +
                "103.8585308\n" +
                "0.0\n" +
                "0.0\n" +
                "1.2794432\n" +
                "103.8498877\n" +
                "37.7117886\n" +
                "-122.1626444\n" +
                "51.5189515\n" +
                "-0.1731155\n" +
                "1.2926644\n" +
                "103.8604486\n" +
                "0.0\n" +
                "0.0\n" +
                "0.0\n" +
                "0.0\n" +
                "0.0\n" +
                "0.0\n" +
                "1.2971401\n" +
                "103.8548649\n" +
                "1.2913726\n" +
                "103.8448496\n" +
                "1.2851863\n" +
                "103.8488897\n" +
                "1.28573\n" +
                "103.8539282\n" +
                "1.281292\n" +
                "103.851701\n" +
                "1.2844523\n" +
                "103.8510426\n" +
                "0.0\n" +
                "0.0\n" +
                "49.28794\n" +
                "-123.1130519\n" +
                "1.3005433\n" +
                "103.8493917\n" +
                "0.0\n" +
                "0.0\n" +
                "1.299718\n" +
                "103.860563\n" +
                "1.2814161\n" +
                "103.8405614\n" +
                "1.2919369\n" +
                "103.8497929\n" +
                "1.2920822\n" +
                "103.8512599\n" +
                "1.2858915\n" +
                "103.8440564\n" +
                "0.0\n" +
                "0.0\n" +
                "1.2822843\n" +
                "103.8490773\n" +
                "1.2940117\n" +
                "103.8533839\n" +
                "1.2945685\n" +
                "103.854012\n" +
                "1.2831805\n" +
                "103.8508729\n" +
                "1.2535219\n" +
                "103.8257032\n" +
                "1.2919747\n" +
                "103.8414577\n" +
                "0.0\n" +
                "0.0\n" +
                "40.45282949999999\n" +
                "-80.16091209999999\n" +
                "0.0\n" +
                "0.0\n" +
                "1.2785988\n" +
                "103.8501183\n" +
                "18.961511\n" +
                "72.81773640000002\n" +
                "1.3037646\n" +
                "103.8547326\n" +
                "0.0\n" +
                "0.0\n" +
                "0.0\n" +
                "0.0\n" +
                "1.2847487\n" +
                "103.8519923\n" +
                "1.2855363\n" +
                "103.8516683\n" +
                "-26.655081\n" +
                "153.088388\n" +
                "1.2959623\n" +
                "103.8579517\n" +
                "0.0\n" +
                "0.0\n" +
                "0.0\n" +
                "0.0\n" +
                "1.3163386\n" +
                "103.894331\n" +
                "53.793346\n" +
                "-1.540985\n" +
                "-36.8487937\n" +
                "174.6358817\n" +
                "1.2787938\n" +
                "103.8481197\n" +
                "43.6483452\n" +
                "-79.3703211\n" +
                "1.2922525\n" +
                "103.842744\n" +
                "-6.1988713\n" +
                "106.8226037\n" +
                "1.2655135\n" +
                "103.8221097\n" +
                "33.62018849999999\n" +
                "-117.923533\n" +
                "1.3735059\n" +
                "103.763909\n" +
                "1.2739814\n" +
                "103.8014294\n" +
                "1.2887273\n" +
                "103.80518\n" +
                "1.3802472\n" +
                "103.7642886\n" +
                "0.0\n" +
                "0.0\n" +
                "1.3431947\n" +
                "103.7759689\n" +
                "1.323853\n" +
                "103.810004\n" +
                "1.3103752\n" +
                "103.7955019\n" +
                "1.2880072\n" +
                "103.8060273\n" +
                "0.0\n" +
                "0.0\n" +
                "12.925772\n" +
                "77.56403089999999\n" +
                "51.86950159999999\n" +
                "-0.4178174\n" +
                "1.3395894\n" +
                "103.7061259\n" +
                "1.3257101\n" +
                "103.8504087\n" +
                "0.0\n" +
                "0.0\n" +
                "21.966807\n" +
                "-159.3817409\n" +
                "1.2874858\n" +
                "103.8032421\n" +
                "51.51009800000001\n" +
                "-0.1207622\n" +
                "1.3226018\n" +
                "103.8136977\n" +
                "1.3782777\n" +
                "103.7390269\n" +
                "1.3148573\n" +
                "103.7642179\n" +
                "1.3059013\n" +
                "103.7917421\n" +
                "1.3067634\n" +
                "103.7884236\n" +
                "1.2860005\n" +
                "103.8274741\n" +
                "1.337685\n" +
                "103.793667\n" +
                "1.2929613\n" +
                "103.8270554\n" +
                "1.303644\n" +
                "103.765892\n" +
                "49.2620113\n" +
                "-123.253479\n" +
                "36.13599809999999\n" +
                "-115.1515128\n" +
                "1.39729\n" +
                "103.746546\n" +
                "1.3432771\n" +
                "103.847263\n" +
                "1.3597681\n" +
                "103.8596263\n" +
                "1.3694045\n" +
                "103.848792\n" +
                "1.3227638\n" +
                "103.8520112\n" +
                "1.2969076\n" +
                "103.8536035\n" +
                "1.4359844\n" +
                "103.7860127\n" +
                "38.2228173\n" +
                "-122.1256589\n" +
                "1.3114724\n" +
                "103.8566701\n" +
                "39.138712\n" +
                "-76.4948\n" +
                "1.3178172\n" +
                "103.8434145\n" +
                "1.3595839\n" +
                "103.8852768\n" +
                "0.0\n" +
                "0.0\n" +
                "1.3792309\n" +
                "103.8878695\n" +
                "1.3726291\n" +
                "103.893938\n" +
                "1.3712804\n" +
                "103.8920463\n" +
                "1.3505524\n" +
                "103.8488353\n" +
                "0.0\n" +
                "0.0\n" +
                "0.0\n" +
                "0.0\n" +
                "1.309985\n" +
                "103.8554636\n" +
                "21.3488557\n" +
                "-157.9306542\n" +
                "1.4295657\n" +
                "103.8361296\n" +
                "-35.3072666\n" +
                "149.122774\n" +
                "25.3901111\n" +
                "55.4623546\n" +
                "1.3922849\n" +
                "103.9044292\n" +
                "1.4418577\n" +
                "103.825096\n" +
                "1.3249047\n" +
                "103.8509656\n" +
                "0.0\n" +
                "0.0\n" +
                "41.3159924\n" +
                "-81.8349071\n" +
                "3.5830178\n" +
                "98.6719227\n" +
                "28.5875666\n" +
                "-81.2099663\n" +
                "1.3554\n" +
                "103.8314804\n" +
                "1.3322562\n" +
                "103.8485391\n" +
                "1.3208572\n" +
                "103.8424319\n" +
                "1.3196341\n" +
                "103.8434302\n" +
                "1.4388503\n" +
                "103.8412139\n" +
                "1.3052458\n" +
                "103.9050356\n" +
                "1.3243582\n" +
                "103.9286897\n" +
                "1.3249063\n" +
                "103.9323644\n" +
                "40.34698420000001\n" +
                "-79.94618950000002\n" +
                "0.0\n" +
                "0.0\n" +
                "0.0\n" +
                "0.0\n" +
                "1.3332724\n" +
                "103.9620956\n" +
                "1.390005\n" +
                "103.9867546\n" +
                "-6.235659800000001\n" +
                "106.8242199\n" +
                "1.3798904\n" +
                "103.9549644\n" +
                "49.284004\n" +
                "-123.0919496\n" +
                "0.0\n" +
                "0.0\n" +
                "1.3735059\n" +
                "103.763909\n" +
                "39.2946237\n" +
                "-76.5104885\n" +
                "1.303397\n" +
                "103.9046199\n" +
                "1.3743394\n" +
                "103.9319676\n" +
                "1.3039671\n" +
                "103.9012865\n" +
                "1.303713\n" +
                "103.9029349\n" +
                "1.3196856\n" +
                "103.892495\n" +
                "40.75957260000001\n" +
                "-73.9870887\n" +
                "1.3030126\n" +
                "103.9059617\n" +
                "-8.1837991\n" +
                "113.6629958\n" +
                "1.3335251\n" +
                "103.959537\n" +
                "1.3186175\n" +
                "103.8935557\n" +
                "1.3541377\n" +
                "103.9425603\n" +
                "1.3523757\n" +
                "103.9418713\n" +
                "1.352614\n" +
                "103.9446926\n" +
                "0.0\n" +
                "0.0\n" +
                "1.3534646\n" +
                "103.9420973\n" +
                "1.3724701\n" +
                "103.9496179\n" +
                "1.2753354\n" +
                "103.8435764\n" +
                "6.8389003\n" +
                "79.86361029999999\n" +
                "1.2959064\n" +
                "103.8525424\n" +
                "1.390005\n" +
                "103.9867546\n" +
                "3.155619\n" +
                "101.705984\n" +
                "0.0\n" +
                "0.0\n" +
                "1.2882482\n" +
                "103.8366477\n" +
                "5.4670108\n" +
                "100.2922219\n" +
                "0.0\n" +
                "0.0\n" +
                "-33.8615479\n" +
                "151.2076072\n" +
                "1.2863993\n" +
                "103.8447538\n" +
                "1.2877268\n" +
                "103.8361551\n" +
                "41.3949827\n" +
                "2.1593994\n" +
                "-34.9483547\n" +
                "138.5895991\n" +
                "1.2902491\n" +
                "103.8351346\n" +
                "25.2260701\n" +
                "55.3280835\n" +
                "1.303397\n" +
                "103.9046199\n" +
                "51.51009800000001\n" +
                "-0.1207622\n" +
                "0.0\n" +
                "0.0\n" +
                "0.0\n" +
                "0.0\n" +
                "1.3030672\n" +
                "103.8360734\n" +
                "0.0\n" +
                "0.0\n" +
                "1.288878\n" +
                "103.834491\n" +
                "1.303304\n" +
                "103.8366149\n" +
                "1.3022488\n" +
                "103.8410623\n" +
                "1.2983091\n" +
                "103.855048\n" +
                "0.0\n" +
                "0.0\n" +
                "0.0\n" +
                "0.0\n" +
                "1.273767\n" +
                "103.8450399\n" +
                "1.3020052\n" +
                "103.8359339\n" +
                "45.46947720000001\n" +
                "9.190932499999999\n" +
                "1.282302\n" +
                "103.8585308\n" +
                "0.0\n" +
                "0.0\n" +
                "1.2913726\n" +
                "103.8448496\n" +
                "1.28573\n" +
                "103.8539282\n" +
                "0.0\n" +
                "0.0\n" +
                "0.0\n" +
                "0.0\n" +
                "1.306701\n" +
                "103.827795\n" +
                "49.28794\n" +
                "-123.1130519\n" +
                "1.3074414\n" +
                "103.829745\n" +
                "40.75957260000001\n" +
                "-73.9870887\n" +
                "1.299718\n" +
                "103.860563\n" +
                "1.2919369\n" +
                "103.8497929\n" +
                "1.2945685\n" +
                "103.854012\n" +
                "25.3901111\n" +
                "55.4623546\n" +
                "1.3046611\n" +
                "103.824931\n" +
                "-37.8184282\n" +
                "144.9636072\n" +
                "1.3070224\n" +
                "103.8326552\n" +
                "0.0\n" +
                "0.0\n" +
                "0.0\n" +
                "0.0\n" +
                "1.305104\n" +
                "103.832837\n" +
                "0.0\n" +
                "0.0\n" +
                "0.0\n" +
                "0.0\n" +
                "1.2905269\n" +
                "103.8600616\n" +
                "3.153875399999999\n" +
                "101.7146687\n" +
                "1.3073054\n" +
                "103.835584\n" +
                "1.3009959\n" +
                "103.8384159\n" +
                "0.0\n" +
                "0.0\n" +
                "1.29976\n" +
                "103.8455131\n" +
                "1.3016415\n" +
                "103.8362236\n" +
                "1.2988469\n" +
                "103.838201\n" +
                "3.155619\n" +
                "101.705984\n" +
                "1.307238\n" +
                "103.828526\n" +
                "0.0\n" +
                "0.0\n" +
                "1.30732\n" +
                "103.8331609\n" +
                "1.3053796\n" +
                "103.8300404\n" +
                "1.3062639\n" +
                "103.8286812\n" +
                "-33.8615479\n" +
                "151.2076072\n" +
                "-34.9483547\n" +
                "138.5895991\n" +
                "1.3064961\n" +
                "103.8330143\n" +
                "1.3030672\n" +
                "103.8360734\n" +
                "0.0\n" +
                "0.0\n" +
                "0.0\n" +
                "0.0\n" +
                "1.303304\n" +
                "103.8366149\n" +
                "1.3022488\n" +
                "103.8410623\n" +
                "1.3060349\n" +
                "103.8310181\n" +
                "1.3039937\n" +
                "103.8319701\n" +
                "16.7710697\n" +
                "96.16725810000001\n" +
                "1.3038954\n" +
                "103.8340843\n" +
                "1.3020052\n" +
                "103.8359339\n" +
                "1.3020052\n" +
                "103.8359339\n" +
                "1.3021134\n" +
                "103.8343167\n" +
                "1.3013261\n" +
                "103.8405742\n" +
                "1.3014117\n" +
                "103.8372446\n" +
                "1.3006576\n" +
                "103.8396965\n" +
                "0.0\n" +
                "0.0\n" +
                "1.306701\n" +
                "103.827795\n" +
                "1.3007397\n" +
                "103.841005\n" +
                "36.82689999999999\n" +
                "-81.515381\n" +
                "1.300661\n" +
                "103.838869\n" +
                "1.3063796\n" +
                "103.8321663\n" +
                "1.3066492\n" +
                "103.8295057\n" +
                "1.3074414\n" +
                "103.829745\n" +
                "1.3036428\n" +
                "103.8357331\n" +
                "36.19074760000001\n" +
                "5.4101854\n" +
                "0.0\n" +
                "0.0\n" +
                "1.3004751\n" +
                "103.8453614\n" +
                "1.3046611\n" +
                "103.824931\n" +
                "1.3070224\n" +
                "103.8326552\n" +
                "38.64493600000001\n" +
                "-90.2634154\n" +
                "0.0\n" +
                "0.0\n" +
                "45.42419700000001\n" +
                "-75.691773\n" +
                "1.3055124\n" +
                "103.8317552\n" +
                "1.3118244\n" +
                "103.8364761\n" +
                "1.305104\n" +
                "103.832837\n" +
                "1.298146\n" +
                "103.8441579\n" +
                "6.5989875\n" +
                "3.374478099999999\n" +
                "1.3049989\n" +
                "103.8238576\n" +
                "1.3062726\n" +
                "103.8266803\n" +
                "53.73245600000001\n" +
                "10.0865489\n" +
                "1.2992801\n" +
                "103.8475303\n" +
                "1.3021552\n" +
                "103.8397579\n" +
                "1.3025117\n" +
                "103.837112\n" +
                "3.153875399999999\n" +
                "101.7146687\n" +
                "1.30018\n" +
                "103.837331\n" +
                "1.3049686\n" +
                "103.8311662\n" +
                "1.2995873\n" +
                "103.8402436\n" +
                "1.3038568\n" +
                "103.8330558\n" +
                "1.3073054\n" +
                "103.835584\n" +
                "0.0\n" +
                "0.0\n" +
                "1.2835085\n" +
                "103.8443515\n" +
                "-33.8765192\n" +
                "151.2027618\n" +
                "1.2831258\n" +
                "103.7819544\n" +
                "0.0\n" +
                "0.0\n" +
                "1.3187065\n" +
                "103.7064417\n" +
                "0.0\n" +
                "0.0\n" +
                "0.0\n" +
                "0.0\n" +
                "0.0\n" +
                "0.0\n" +
                "0.0\n" +
                "0.0\n" +
                "1.2966375\n" +
                "103.8486646\n" +
                "0.0\n" +
                "0.0\n" +
                "0.0\n" +
                "0.0\n" +
                "0.0\n" +
                "0.0\n" +
                "0.0\n" +
                "0.0\n" +
                "1.243299\n" +
                "103.828128\n" +
                "1.2973072\n" +
                "103.8510615\n" +
                "1.3221102\n" +
                "103.8149828\n" +
                "0.0\n" +
                "0.0\n" +
                "22.2828374\n" +
                "114.1569173\n" +
                "1.2895301\n" +
                "103.8632483\n" +
                "0.0\n" +
                "0.0\n" +
                "1.2928447\n" +
                "103.8487025\n" +
                "0.0\n" +
                "0.0\n" +
                "0.0\n" +
                "0.0\n" +
                "0.0\n" +
                "0.0\n" +
                "1.4467195\n" +
                "103.7301489\n" +
                "0.0\n" +
                "0.0\n" +
                "0.0\n" +
                "0.0\n" +
                "1.258183\n" +
                "103.811225\n" +
                "1.2734327\n" +
                "103.8334053\n" +
                "1.2730833\n" +
                "103.8406381\n" +
                "1.2762877\n" +
                "103.7971727\n" +
                "1.3735059\n" +
                "103.763909\n" +
                "1.2755992\n" +
                "103.8473293\n" +
                "1.2753354\n" +
                "103.8435764\n" +
                "1.2790314\n" +
                "103.8514261\n" +
                "1.2820883\n" +
                "103.8591431\n" +
                "6.8389003\n" +
                "79.86361029999999\n" +
                "1.2996001\n" +
                "103.8551282\n" +
                "1.3023079\n" +
                "103.8528753\n" +
                "0.0\n" +
                "0.0\n" +
                "-33.87923290000001\n" +
                "151.2057852\n" +
                "1.2777366\n" +
                "103.8476215\n" +
                "1.2959064\n" +
                "103.8525424\n" +
                "18.535001\n" +
                "73.838836\n" +
                "-6.186467400000001\n" +
                "106.8296372\n" +
                "1.295059\n" +
                "103.8521521\n" +
                "1.283653\n" +
                "103.8469901\n" +
                "1.285246\n" +
                "103.8450159\n" +
                "1.2906024\n" +
                "103.8464742\n" +
                "1.283857\n" +
                "103.852378\n" +
                "1.2934379\n" +
                "103.8587261\n" +
                "1.2882482\n" +
                "103.8366477\n" +
                "5.4670108\n" +
                "100.2922219\n" +
                "1.277478\n" +
                "103.8481197\n" +
                "1.2778231\n" +
                "103.8489535\n" +
                "1.2830682\n" +
                "103.848281\n" +
                "1.2798824\n" +
                "103.8482219\n" +
                "1.300376\n" +
                "103.852234\n" +
                "1.3015627\n" +
                "103.8543985\n" +
                "0.0\n" +
                "0.0\n" +
                "0.0\n" +
                "0.0\n" +
                "1.2863993\n" +
                "103.8447538\n" +
                "1.2877268\n" +
                "103.8361551\n" +
                "41.3949827\n" +
                "2.1593994\n" +
                "1.2783906\n" +
                "103.8043766\n" +
                "1.3027906\n" +
                "103.8653773\n" +
                "0.0\n" +
                "0.0\n" +
                "1.2902491\n" +
                "103.8351346\n" +
                "0.0\n" +
                "0.0\n" +
                "1.2925284\n" +
                "103.8500923\n" +
                "1.2847751\n" +
                "103.8474319\n" +
                "1.2940922\n" +
                "103.8308378\n" +
                "1.2621119\n" +
                "103.8179189\n" +
                "1.288878\n" +
                "103.834491\n" +
                "1.2814095\n" +
                "103.8510185\n" +
                "0.0\n" +
                "0.0\n" +
                "1.2752066\n" +
                "103.8441782\n" +
                "32.87063000000001\n" +
                "-117.1994792\n" +
                "0.0\n" +
                "0.0\n" +
                "0.0\n" +
                "0.0\n" +
                "27.9608744\n" +
                "-82.52308889999999\n" +
                "1.280314\n" +
                "103.8489857\n" +
                "0.0\n" +
                "0.0\n" +
                "0.0\n" +
                "0.0\n" +
                "0.0\n" +
                "0.0\n" +
                "1.3016902\n" +
                "103.8575302\n" +
                "1.2915711\n" +
                "103.8446322\n" +
                "1.273767\n" +
                "103.8450399\n" +
                "45.46947720000001\n" +
                "9.190932499999999\n" +
                "0.0\n" +
                "0.0\n" +
                "1.282302\n" +
                "103.8585308\n" +
                "0.0\n" +
                "0.0\n" +
                "37.7117886\n" +
                "-122.1626444\n" +
                "51.5189515\n" +
                "-0.1731155\n" +
                "1.3037646\n" +
                "103.8547326\n" +
                "1.2926644\n" +
                "103.8604486\n" +
                "0.0\n" +
                "0.0\n" +
                "0.0\n" +
                "0.0\n" +
                "0.0\n" +
                "0.0\n" +
                "1.2971401\n" +
                "103.8548649\n" +
                "1.2913726\n" +
                "103.8448496\n" +
                "1.2851863\n" +
                "103.8488897\n" +
                "1.28573\n" +
                "103.8539282\n" +
                "1.281292\n" +
                "103.851701\n" +
                "1.2844523\n" +
                "103.8510426\n" +
                "0.0\n" +
                "0.0\n" +
                "49.28794\n" +
                "-123.1130519\n" +
                "1.3005433\n" +
                "103.8493917\n" +
                "1.299718\n" +
                "103.860563\n" +
                "1.2814161\n" +
                "103.8405614\n" +
                "1.2919369\n" +
                "103.8497929\n" +
                "1.2920822\n" +
                "103.8512599\n" +
                "1.2858915\n" +
                "103.8440564\n" +
                "0.0\n" +
                "0.0\n" +
                "1.2822843\n" +
                "103.8490773\n" +
                "1.2940117\n" +
                "103.8533839\n" +
                "1.2945685\n" +
                "103.854012\n" +
                "1.2831805\n" +
                "103.8508729\n" +
                "1.2535219\n" +
                "103.8257032\n" +
                "1.2919747\n" +
                "103.8414577\n" +
                "0.0\n" +
                "0.0\n" +
                "40.45282949999999\n" +
                "-80.16091209999999\n" +
                "0.0\n" +
                "0.0\n" +
                "1.2794432\n" +
                "103.8498877\n" +
                "1.2785988\n" +
                "103.8501183\n" +
                "18.961511\n" +
                "72.81773640000002\n" +
                "0.0\n" +
                "0.0\n" +
                "0.0\n" +
                "0.0\n" +
                "1.2847487\n" +
                "103.8519923\n" +
                "1.2855363\n" +
                "103.8516683\n" +
                "-26.655081\n" +
                "153.088388\n" +
                "1.2959623\n" +
                "103.8579517\n" +
                "0.0\n" +
                "0.0\n" +
                "0.0\n" +
                "0.0\n" +
                "1.3163386\n" +
                "103.894331\n" +
                "53.793346\n" +
                "-1.540985\n" +
                "-36.8487937\n" +
                "174.6358817\n" +
                "1.2787938\n" +
                "103.8481197\n" +
                "43.6483452\n" +
                "-79.3703211\n" +
                "1.2922525\n" +
                "103.842744\n" +
                "-6.1988713\n" +
                "106.8226037\n" +
                "1.2655135\n" +
                "103.8221097\n" +
                "33.62018849999999\n" +
                "-117.923533\n" +
                "1.2739814\n" +
                "103.8014294\n" +
                "1.2887273\n" +
                "103.80518\n" +
                "1.3802472\n" +
                "103.7642886\n" +
                "0.0\n" +
                "0.0\n" +
                "1.3431947\n" +
                "103.7759689\n" +
                "1.323853\n" +
                "103.810004\n" +
                "1.3103752\n" +
                "103.7955019\n" +
                "1.2880072\n" +
                "103.8060273\n" +
                "0.0\n" +
                "0.0\n" +
                "12.925772\n" +
                "77.56403089999999\n" +
                "51.86950159999999\n" +
                "-0.4178174\n" +
                "1.3395894\n" +
                "103.7061259\n" +
                "0.0\n" +
                "0.0\n" +
                "0.0\n" +
                "0.0\n" +
                "21.966807\n" +
                "-159.3817409\n" +
                "1.2874858\n" +
                "103.8032421\n" +
                "1.3226018\n" +
                "103.8136977\n" +
                "1.3782777\n" +
                "103.7390269\n" +
                "1.3148573\n" +
                "103.7642179\n" +
                "1.3059013\n" +
                "103.7917421\n" +
                "1.3067634\n" +
                "103.7884236\n" +
                "1.2860005\n" +
                "103.8274741\n" +
                "1.337685\n" +
                "103.793667\n" +
                "1.2929613\n" +
                "103.8270554\n" +
                "1.303644\n" +
                "103.765892\n" +
                "49.2620113\n" +
                "-123.253479\n" +
                "36.13599809999999\n" +
                "-115.1515128\n" +
                "1.39729\n" +
                "103.746546\n";
        return latLong;
    }

}
