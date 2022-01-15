package uhk.sa.smartarchive;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import uhk.sa.smartarchive.databinding.ActivityMapsBinding;
import uhk.sa.smartarchive.entity.Event;
import uhk.sa.smartarchive.entity.Spot;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, Serializable {
    private Database db;
    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    private List<Event> events;

    @Override
    protected void onCreate(@NonNull Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        db = new Database(MapsActivity.this);
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        // show current location
        mMap.setMyLocationEnabled(true);
        try {
            events = db.getEvents();
        } catch (Exception e) {
            e.printStackTrace();
        }
        loadMarkersToMap();
        // ADD new spot to map
        mMap.setOnMapLongClickListener(latLng -> {
            Intent intent = new Intent(MapsActivity.this, SpotActivity.class);
            Bundle bundle = new Bundle();
            bundle.putDouble("latitude", latLng.latitude);
            bundle.putDouble("longitude", latLng.longitude);
            intent.putExtra("newSpot", bundle);
            startActivity(intent);
        });

        mMap.setOnMarkerClickListener(marker -> {
            // TODO open new intent and store new spot.
            Spot spot = (Spot) marker.getTag();
            if (spot != null) {
                Intent intent = new Intent(MapsActivity.this, SpotActivity.class);
                Bundle bundle = new Bundle();
                // spot
                bundle.putDouble("latitude", marker.getPosition().latitude);
                bundle.putDouble("longitude", marker.getPosition().longitude);
                bundle.putString("spot_title", spot.getTitle());
                bundle.putString("spot_note", spot.getNote());
                bundle.putInt("spot_id", spot.getId());
                intent.putExtra("newSpot", bundle);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        loadMarkersToMap();
    }

    private void loadMarkersToMap() {
        // Load existing markers from database
        List<Spot> spots = db.getSpots();
        for (Spot spot : spots) {
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(new LatLng(spot.getLat(), spot.getLng()))
                    .title(spot.getTitle());
            Marker marker = mMap.addMarker(markerOptions);
            Objects.requireNonNull(marker).setTag(spot);
        }
    }

}