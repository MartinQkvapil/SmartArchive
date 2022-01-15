package uhk.sa.smartarchive;

import static uhk.sa.smartarchive.R.string.location_is_not_tracked;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import uhk.sa.smartarchive.entity.Event;
import uhk.sa.smartarchive.entity.Person;
import uhk.sa.smartarchive.entity.Spot;

public class MainActivity extends AppCompatActivity {
    public static final int DEFAULT_LOCATION_UPDATE_INTERVAL = 180;
    public static final int FASTEST_LOCATION_UPDATE_INTERVAL = 90;
    public static final int PERMISSION_FINE_LOCATION = 99;
    public static final int PERMISSION_COARSE_LOCATION = 88;

    private ListView lvSpots;
    private LocationCallback locationCallback;
    private final Person currentUser = new Person(Integer.MAX_VALUE, "Martin", "Kvapil", Constants.getDateFromString("1995-01-11"));
    private TextView tv_lat;
    private TextView tv_lon;
    private TextView tv_updates;
    private TextView tv_address;
    private TextView eventCount;
    private TextView spotCount;

    public Spot lastNotifiedSpot;
    public Event lastTimeNotifiedEvent;

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    Switch sw_locationupdates, sw_gps;
    Button btnShowLifeStories, btnShowEvents, btnShowMap, btnAddPerson;
    Database db;

    // device current location
    Location currentLocation;
    // list of saved locations
    List<Location> savedLocations;
    // list of nearest spots in 1000 around current location
    List<Spot> nearestSpots;

    // Location request is a config file for all settings related to FusedLocationProviderClient
    LocationRequest locationRequest;

    // Google's API for location services. The majority of the app functions using this class.
    FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // db connection
        db = new Database(MainActivity.this);

        // User of the app
        TextView logged_user = findViewById(R.id.logged_user);
        logged_user.setText(currentUser.getSurname());
        nearestSpots = new ArrayList<>();

        tv_lat = findViewById(R.id.tv_lat);
        tv_lon = findViewById(R.id.tv_lon);
        btnAddPerson = findViewById(R.id.btn_add_person_main);
        tv_updates = findViewById(R.id.tv_updates);
        tv_address = findViewById(R.id.tv_address);
        sw_gps = findViewById(R.id.sw_gps);
        sw_locationupdates = findViewById(R.id.sw_locationsupdates);
        lvSpots = findViewById(R.id.lv_closest_spots);

        btnShowLifeStories = findViewById(R.id.btn_show_lifestories);
        btnShowEvents = findViewById(R.id.btn_showEvents);

        eventCount = findViewById(R.id.tv_event_count);
        spotCount = findViewById(R.id.tv_spot_count);

        btnShowMap = findViewById(R.id.btn_showMap);

        locationRequest = LocationRequest.create()
                .setInterval(1000 * DEFAULT_LOCATION_UPDATE_INTERVAL)
                .setFastestInterval(1000 * FASTEST_LOCATION_UPDATE_INTERVAL)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // event that is triggered when the update interval is met
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                updateUIValues(locationResult.getLastLocation());
            }
        };

        btnShowLifeStories.setOnClickListener(v -> {
            storyAboutPerson();
        });
        btnShowEvents.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, EventsActivity.class);
            startActivity(i);
        });
        // Show events on map
        btnShowMap.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, MapsActivity.class);
            startActivity(i);
        });
        // Add/Remove Persons
        btnAddPerson.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, PersonActivity.class);
            startActivity(intent);
        });
        // GPS Settings
        sw_gps.setOnClickListener(v -> {
            if (sw_gps.isChecked()) {
                // most accurate - use GPS
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            } else {
                locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            }
        });
        sw_locationupdates.setOnClickListener(v -> {
            if (sw_locationupdates.isChecked()) {
                // turn on location tracking
                startLocationUpdates();
            } else {
                // turn off location tracking
                stopLocationUpdates();
            }
        });
        updateGPS();
        updateCounts();

        lvSpots.setOnItemClickListener((parent, view, position, id) -> {
            Spot spot = nearestSpots.get(position);

            Intent intent = new Intent(MainActivity.this, SpotActivity.class);
                Bundle bundle = new Bundle();
                // spot
                bundle.putDouble("latitude", spot.getLat());
                bundle.putDouble("longitude", spot.getLng());
                bundle.putString("spot_title", spot.getTitle());
                bundle.putString("spot_note", spot.getNote());
                bundle.putInt("spot_id", spot.getId());
                intent.putExtra("newSpot", bundle);

                // events
                List<Event> eventsToSend = new ArrayList<>();
                for(Event event : db.getEventsBySpotId(spot.getId())) {
                    if(event.getSpot() ==  spot.getId()) {
                        eventsToSend.add(event);
                    }
                }
                intent.putExtra("events", (Serializable) eventsToSend);
                startActivity(intent);

        });
    }

    private void updateCounts() {
        eventCount.setText(String.valueOf(db.getRowCountOfTable(Event.TABLE)));
        spotCount.setText(String.valueOf(db.getRowCountOfTable(Spot.TABLE)));
    }

    private void stopLocationUpdates() {
        tv_updates.setText(R.string.location_is_not_being_tracked);
        tv_lat.setText(location_is_not_tracked);
        tv_lon.setText(location_is_not_tracked);
        tv_address.setText(location_is_not_tracked);
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    private void startLocationUpdates() {
        tv_updates.setText(R.string.location_is_tracked);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
        updateGPS();

    }

    private void updateSpotsLv(Location location) {
        if(location == null) {
            return;
        }
        nearestSpots.clear();
        nearestSpots.addAll(db.getNearestSpots(location));
        ArrayAdapter<Spot> arrayAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                nearestSpots
        );
        lvSpots.setAdapter(arrayAdapter);
        sendSpotNotification();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSION_COARSE_LOCATION:
            case PERMISSION_FINE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    updateGPS();
                } else {
                    Toast.makeText(this, "The app requires permission to be granted in order to work properly", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void updateGPS() {
        // Get permissions from the user to track GPS
        // Get the current location from the fused client
        // Update the UI - i.e. set all properties in their associated
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this,  Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // Granted permission
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, location -> {
                updateUIValues(location);
                currentLocation = location;
                updateSpotsLv(location);
                sendTimeNotification();
            });
        } else {
            // Not granted permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_FINE_LOCATION);
            }
        }
    }

    private void updateUIValues(Location location) {
        // update all of the text view objects with a new location
        if(location == null) {
            return;
        }
        tv_lat.setText(Constants.round(location.getLatitude(), 4));
        tv_lon.setText(Constants.round(location.getLongitude(), 4));

        // update address
        Geocoder geocoder = new Geocoder(MainActivity.this);
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            tv_address.setText(addresses.get(0).getAddressLine(0));
        } catch (Exception e) {
            tv_address.setText(R.string.unable_to_get_street_address);
        }
        MyApplication myApplication = (MyApplication) getApplicationContext();
        savedLocations = myApplication.getMyLocations();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        updateGPS();
        updateCounts();
    }

    private void sendSpotNotification() {
        if (lastNotifiedSpot == null && nearestSpots.size() != 0) {
            lastNotifiedSpot = nearestSpots.get(0);

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setCancelable(true);

            builder.setTitle("You are close to: " + lastNotifiedSpot.getTitle());
            builder.setMessage("Show me the spot!");

            builder.setNegativeButton("Cancel", (dialog, which) -> {});
            builder.setPositiveButton("Ok", (dialog, which) -> {
                Intent intent = new Intent(MainActivity.this, SpotActivity.class);
                Bundle bundle = new Bundle();
                // spot
                bundle.putDouble("latitude", lastNotifiedSpot.getLat());
                bundle.putDouble("longitude", lastNotifiedSpot.getLng());
                bundle.putString("spot_title", lastNotifiedSpot.getTitle());
                bundle.putString("spot_note", lastNotifiedSpot.getNote());
                bundle.putInt("spot_id", lastNotifiedSpot.getId());
                intent.putExtra("newSpot", bundle);

                // events
                List<Event> eventsToSend = new ArrayList<>();
                for(Event event : db.getEventsBySpotId(lastNotifiedSpot.getId())) {
                    if(event.getSpot() ==  lastNotifiedSpot.getId()) {
                        eventsToSend.add(event);
                    }
                }
                intent.putExtra("events", (Serializable) eventsToSend);
                startActivity(intent);
            });
            builder.show();
        }
    }

    private void storyAboutPerson() {
        List<Person> spinnerPersons = db.getPersons();
        final ArrayAdapter<Person> adp = new ArrayAdapter<>(MainActivity.this,
                android.R.layout.simple_spinner_item, spinnerPersons);
        final Spinner sp = new Spinner(MainActivity.this);
        sp.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        sp.setAdapter(adp);
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setCancelable(true);
        builder.setTitle("Story about?");
        builder.setNegativeButton("Cancel", (dialog, which) -> {});
        builder.setPositiveButton("Ok", (dialog, which) -> {
            Intent i = new Intent(MainActivity.this, StoryActivity.class);
            Person np = (Person) sp.getSelectedItem();
            i.putExtra("person_id", np.getId());
            startActivity(i);
        });
        builder.setView(sp);
        builder.create().show();
    }

    private void sendTimeNotification() {
        for (Event event: db.getEvents()) {
            Date date = Constants.getDatePlusReminder(event.getDate(), event.getReminder());
            if (date.getTime() == Constants.getCurrentDate().getTime() && lastTimeNotifiedEvent == null) {
                lastTimeNotifiedEvent = event;
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setCancelable(true);
                builder.setTitle("Today is " + event.getReminder() + "th anniversary of the event: " + event.getTitle());
                builder.setMessage("Show me the event:");
                builder.setNegativeButton("Cancel", (dialog, which) -> {});
                builder.setPositiveButton("Ok", (dialog, which) -> {
                    Intent intentEvent = new Intent(MainActivity.this, EventActivity.class);
                    intentEvent.putExtra("event", event);
                    startActivity(intentEvent);
                });
                builder.show();
            }
        }
    }


}