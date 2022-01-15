package uhk.sa.smartarchive;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.List;
import uhk.sa.smartarchive.entity.Event;
import uhk.sa.smartarchive.entity.Spot;

public class SpotActivity extends AppCompatActivity {
    Button btn_add_new_event, btn_update_spot;
    TextView tv_lat_spot, tv_lng_spot;
    EditText tv_title_spot;
    ListView tv_events;
    List<Event> eventsList;
    Database db;

    Spot spot;
    Integer spotId;
    SQLiteDatabase sql;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spot);

        spotId = 0;
        db = new Database(SpotActivity.this);
        sql = db.getWritableDatabase();

        tv_lat_spot = findViewById(R.id.tv_spot_lat);
        tv_lng_spot = findViewById(R.id.tv_spot_lng);
        tv_title_spot = findViewById(R.id.tv_title_spot);
        tv_events = findViewById(R.id.tv_events);

        btn_add_new_event = findViewById(R.id.btn_add_event);
        btn_update_spot = findViewById(R.id.btn_update_spot);

        Intent intent = getIntent();
        Bundle incoming = intent.getBundleExtra("newSpot");
        spotId = incoming.getInt("spot_id");
        if (spotId != 0) {
            // Known spot
            tv_lat_spot.setText(String.valueOf(incoming.getDouble("latitude")));
            tv_lng_spot.setText(String.valueOf(incoming.getDouble("longitude")));
            tv_title_spot.setText(String.valueOf(incoming.getString("spot_title")));
            spotId = incoming.getInt("spot_id");
            eventsList = db.getEventsBySpotId(spotId);
            updateListView();
        } else {
            // New spot
            tv_lat_spot.setText(String.valueOf(incoming.getDouble("latitude")));
            tv_lng_spot.setText(String.valueOf(incoming.getDouble("longitude")));
            tv_title_spot.setText(R.string.new_spot_title);
        }

        btn_add_new_event.setOnClickListener(v -> {
            if (spotId == 0) {
                Toast.makeText(SpotActivity.this, "You have to save activity!", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intentNewEmptyEvent = new Intent(SpotActivity.this, EventActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt("spot_id", spotId);
            intentNewEmptyEvent.putExtra("new_event", bundle);
            startActivity(intentNewEmptyEvent);
        });
        btn_update_spot.setOnClickListener(v -> spotId = saveSpot());
        tv_events.setOnItemClickListener((adapterView, view, i, l) -> {
            Intent intentEvent = new Intent(SpotActivity.this, EventActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt("spot_id", spotId);
            intentEvent.putExtra("newSpot", bundle);
            intentEvent.putExtra("event", eventsList.get(i));
            startActivity(intentEvent);
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        updateListView();
    }

    private int saveSpot() {
        int spotId;
        spot = new Spot(
            Double.parseDouble(tv_lat_spot.getText().toString()),
            Double.parseDouble(tv_lng_spot.getText().toString()),
            tv_title_spot.getText().toString(),
            "Note"
        );
        spot.setId(this.spotId);
        if (spot.getId() != 0) {
            sql.update(Spot.TABLE, spot.putToDB(), "id =" + spot.getId(), null);
            Toast.makeText(SpotActivity.this, "New spot and marker updated!", Toast.LENGTH_SHORT).show();
            return spot.getId();
        } else {
            spotId = (int) sql.insert(Spot.TABLE, null, spot.putToDB());
            Toast.makeText(SpotActivity.this, "New spot and marker created!", Toast.LENGTH_SHORT).show();
            return spotId;
        }
    }

    private void updateListView() {
        eventsList = db.getEventsBySpotId(spotId);
        ArrayAdapter<Event> arrayAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                eventsList
        );
        tv_events.setAdapter(arrayAdapter);
    }
}