package uhk.sa.smartarchive;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import uhk.sa.smartarchive.entity.Event;

public class StoryActivity extends AppCompatActivity {

    private ListView lvStory;
    List<Event> story;

    Database db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story);

        lvStory = findViewById(R.id.lv_story);
        story = new ArrayList<>();
        db = new Database(StoryActivity.this);

        lvStory.setOnItemClickListener((parent, view, position, id) -> {
            Intent intentEvent = new Intent(StoryActivity.this, EventActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt("spot_id", story.get(position).getSpot());
            intentEvent.putExtra("newSpot", bundle);
            intentEvent.putExtra("event", story.get(position));
            startActivity(intentEvent);
        });
        populateStory();
    }

    private void populateStory() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        story.addAll(db.getEventsByPersonId(bundle.getInt("person_id")));
        ArrayAdapter<Event> arrayAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                story
        );
        lvStory.setAdapter(arrayAdapter);
    }
}