package uhk.sa.smartarchive;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.List;
import uhk.sa.smartarchive.entity.Event;
import uhk.sa.smartarchive.entity.Person;

public class EventsActivity extends AppCompatActivity {

    ListView events;
    List<Event> sortedEvents;

    Database db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        events = findViewById(R.id.lv_events);
        sortedEvents = new ArrayList<>();
        db = new Database(EventsActivity.this);
        events.setOnItemClickListener((parent, view, position, id) -> {
            Intent intentEvent = new Intent(EventsActivity.this, EventActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt("spot_id", sortedEvents.get(position).getSpot());
            intentEvent.putExtra("newSpot", bundle);
            intentEvent.putExtra("event", sortedEvents.get(position));
            startActivity(intentEvent);
        });
        populateSortedEvents();
    }

    private void populateSortedEvents() {
        sortedEvents.addAll(db.getSortedEvents());
        ArrayAdapter<Event> arrayAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                sortedEvents
        );
        events.setAdapter(arrayAdapter);
    }


}