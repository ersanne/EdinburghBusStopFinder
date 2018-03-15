package com.eriksanne.edinburghbus.EdinburghBus;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.eriksanne.edinburghbus.EdinburghBus.Data.ApiKey;
import com.eriksanne.edinburghbus.EdinburghBus.Data.BusStopDatabase;
import com.eriksanne.edinburghbus.EdinburghBus.Data.GetLocation;
import com.eriksanne.edinburghbus.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

/**
 * This class handles the main activity as well as updating the database.
 * @author Erik.Sanne
 */
public class MainActivity extends AppCompatActivity {

    private ProgressBar spinner;
    private LinearLayout content;
    private ListView lv;
    private EditText search;
    private boolean locationservices = false;
    private LocationManager mLocationManager;
    private Location location;
    private static final int PERMISSIONS_REQUEST_LOCATION = 0;
    private  ListViewAdapter adapter;
    private boolean dbUpdating = false;
    private Button refreshButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        //Setting design objects
        spinner = (ProgressBar) findViewById(R.id.progressBar1);
        content = (LinearLayout) findViewById(R.id.layoutContent);
        lv = (ListView) findViewById(R.id.listView);
        search = (EditText) findViewById(R.id.search);
        refreshButton = (Button) findViewById(R.id.toolbar_refresh_button);

        //Refresh button listener
        refreshButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                refreshApp();
            }
        });

        //Check that the db exists and has data
        //This should only happen the first time the app is opened
        BusStopDatabase db = BusStopDatabase.getInstance(this);
        if(db.isEmpty()){
            updateDB();
        }

        //Checking that location permission is granted
        isLocationPermissonGranted();

        //If permission was granted, locationservices will be true which means that the distance to
        //the bus stop will be calculated and displayed
        if(locationservices){
            mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            if(!isLocationEnabled()){
                showAlert();
            }

            location = GetLocation.getBestInitialLocation(mLocationManager);

        }

        //Starting asyinc task to load the listview items
        new LoadBusStops().execute();

        /**
         * On selecting a ListView item this will open the BusStopActivity, which displays the
         * location of the Bus Stop.
         */
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BusStop selectedItem = (BusStop) parent.getItemAtPosition(position);
                Intent intent = new Intent(MainActivity.this, BusStopActivity.class);
                intent.putExtra("latLang", selectedItem.getLatLang());
                startActivity(intent);
            }
        });

        /**
         * Listener to check for text entered into the search bar,
         * if text is entered the listview will be filtered accordingly.
         */
        search.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Call back the Adapter with current character to Filter
                adapter.getFilter().filter(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    /**
     * Creating the overview menu for the toolbar
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    /**
     * Handling when the user selects and option from the overview menu
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_updatedb: {
                updateDB();
                break;
            }
            // case blocks for other MenuItems (if any)
        }
        return false;
    }

    /**
     * Method to refresh the app
     */
    private void refreshApp(){
        Intent refresh = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(refresh);//Start the same Activity
        finish(); //finish Activity.
    }

    /**
     * Set the current distance for each of the BusStops
     * @param stopList
     * @param location
     */
    private void setLocation(ArrayList<BusStop> stopList, Location location){
        if(location != null){
            for (BusStop busStop : stopList) {
                busStop.setDistance(location);
            }
        }
    }

    /**
     * Check that location services are enabled.
     * @return
     */
    private boolean isLocationEnabled() {
        return mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    /**
     * Alert window if location services are disabled.
     */
    private void showAlert() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Enable Location")
                .setMessage("Your Locations Settings is set to 'Off'.\nWithout location distances " +
                        "will not be viewable")
                .setPositiveButton("Location Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    }
                });
        dialog.show();
    }

    /**
     * Check that location permission is granted, if it isn't onRequestPermissionsResult will handle
     * the enabling or running the app without location services
     */
    private void isLocationPermissonGranted(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_LOCATION);
        } else {
            locationservices = true;
        }
    }

    /**
     * Method to start the UpdateDB async task. Also creates the API URL to request bus stops.
     */
    public void updateDB(){

        BusStopDatabase db = BusStopDatabase.getInstance(this);

        db.resetDB();

        final String URL = "http://www.mybustracker.co.uk/ws.php?module=json&key=";
        final Random rand = new Random(System.currentTimeMillis());

        final StringBuilder sb = new StringBuilder();
        sb.append(URL);
        sb.append(ApiKey.getKey());
        sb.append("&function=getBusStops&operatorID=LB&");
        sb.append("&random=");
        sb.append(rand.nextInt());

        URL url = null;

        try {
            url = new URL(sb.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        new UpdateDB().execute(url);

    }

    /**
     * Async task to Update the BusStopDatabase
     * Unfortunatley there is no way to check if an update is available, and due to the size of the
     * database it will only be updated manually.
     */
    public class UpdateDB extends AsyncTask<URL, Integer, String> {

        BusStopDatabase db = BusStopDatabase.getInstance(getApplicationContext());
        private String result;

        @Override
        protected void onPreExecute(){

            spinner.setVisibility(View.VISIBLE);
            content.setVisibility(View.GONE);
            db.resetDB();
            dbUpdating = true;
            Toast.makeText(getApplicationContext(), "Started DB update...", Toast.LENGTH_SHORT).show();

        }


        @Override
        protected String doInBackground(URL... urls){
            int count = urls.length;
            long totalSize = 0;
            StringBuilder resultBuilder = new StringBuilder();
            for (int i = 0; i < count; i++) {
                try {
                    // Read all the text returned by the server
                    InputStreamReader reader = new InputStreamReader(urls[i].openStream());
                    BufferedReader in = new BufferedReader(reader);
                    String resultPiece;
                    while ((resultPiece = in.readLine()) != null) {
                        resultBuilder.append(resultPiece);
                    }
                    in.close();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // if cancel() is called, leave the loop early
                if (isCancelled()) {
                    break;
                }
            }
            // save the result
            this.result = resultBuilder.toString();

            return result;
        }

        protected void onPostExecute(String result){

            try {
                JSONObject jObj = new JSONObject(result);
                JSONArray jsonArray = jObj.getJSONArray("busStops");
                int i = 0;
                while (i <jsonArray.length()) {
                    JSONObject jsonObj = jsonArray.getJSONObject(i);

                    db.insertBusStop(jsonObj.getString("stopId"),
                            jsonObj.getString("name"),
                            jsonObj.getString("x"),
                            jsonObj.getString("y"),
                            jsonObj.getInt("cap"));

                    i++;
                }

            } catch (JSONException e) {}

            Toast.makeText(getApplicationContext(), "DB update complete...", Toast.LENGTH_SHORT).show();
            spinner.setVisibility(View.GONE);
            content.setVisibility(View.VISIBLE);

            dbUpdating = false;
            refreshApp();



        }
    }

    /**
     * Async task to load an ArrayList of BusStops from the Database and set a ListView adapter
     * for this list.
     *
     */
    public class LoadBusStops extends AsyncTask<Void, Void, Void> {

        BusStopDatabase db = BusStopDatabase.getInstance(getApplicationContext());
        ArrayList<BusStop> stopList;

        @Override
        protected void onPreExecute(){
            spinner.setVisibility(View.VISIBLE);
            content.setVisibility(View.GONE);
        }


        @Override
        protected Void doInBackground(Void... params){

            stopList = db.getAllStops();

            return null;
        }

        @Override
        protected void onPostExecute(Void param){

            if(locationservices){
                setLocation(stopList, location);
            }

            adapter = new ListViewAdapter(getApplicationContext(), stopList);
            lv.setAdapter(adapter);

            spinner.setVisibility(View.GONE);
            content.setVisibility(View.VISIBLE);

        }
    }

    /**
     * ListViewAdapter to use an ArrayList of BusStop objects and map them to the custom ListView
     * item defined in list_item.xml under the layout folder.
     */
    public class ListViewAdapter extends ArrayAdapter<BusStop> implements Filterable {

        private Context mContext;
        private ArrayList<BusStop> busStopList;
        private ArrayList<BusStop> originalList;
        private Filter filter;

        public ListViewAdapter(Context context, ArrayList<BusStop> list){
            super(context, 0, list);
            mContext = context;
            busStopList = new ArrayList<>(list);
            originalList = new ArrayList<>(list);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            View listItem = convertView;

            if(listItem == null){
                listItem = LayoutInflater.from(mContext).inflate(R.layout.list_item, parent, false);
            }

            BusStop currentStop = busStopList.get(position);

            TextView stopName = (TextView)listItem.findViewById(
                    android.R.id.text1);
            TextView distance = (TextView)listItem.findViewById(
                    R.id.txtNearestDistance);
            TextView orientation = (TextView)listItem.findViewById(
                    android.R.id.text2);

            stopName.setText(currentStop.getName());
            orientation.setText(currentStop.getOrientation());

            if(locationservices) {
                distance.setText(currentStop.getDistance() + " m");
            }else{
                distance.setText("No location info");
            }
            return listItem;

        }

        @Override
        public Filter getFilter()
        {
            if (filter == null)
                filter = new BusStopFiler();

            return filter;
        }

        private class BusStopFiler extends Filter
        {
            @Override
            protected FilterResults performFiltering(CharSequence constraint)
            {
                FilterResults results = new FilterResults();
                String prefix = constraint.toString().toLowerCase();

                if (prefix == null || prefix.length() == 0)
                {
                    ArrayList<BusStop> list = new ArrayList<BusStop>(originalList);
                    results.values = list;
                    results.count = list.size();
                }
                else
                {
                    final ArrayList<BusStop> list = new ArrayList<BusStop>(originalList);
                    final ArrayList<BusStop> nlist = new ArrayList<BusStop>();
                    int count = list.size();

                    for (int i=0; i<count; i++)
                    {
                        final BusStop busStop = list.get(i);
                        final String value = busStop.getName().toLowerCase();

                        if (value.startsWith(prefix))
                        {
                            nlist.add(busStop);
                        }
                    }
                    results.values = nlist;
                    results.count = nlist.size();
                }
                return results;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                busStopList = (ArrayList<BusStop>)results.values;

                clear();
                int count = busStopList.size();
                for (int i=0; i<count; i++)
                {
                    BusStop busStop = (BusStop)busStopList.get(i);
                    add(busStop);
                }
            }

        }

    }



    /**
     * Result handling for permission requests. In this case to update the locationservices variable
     * true or leave it as false.
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    locationservices = true;

                } else {

                    locationservices = false;
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }


}
