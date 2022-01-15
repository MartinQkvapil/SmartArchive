package uhk.sa.smartarchive;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import uhk.sa.smartarchive.entity.Event;
import uhk.sa.smartarchive.entity.ImageToEvent;
import uhk.sa.smartarchive.entity.Person;
import uhk.sa.smartarchive.entity.PersonToEvent;


public class EventActivity extends AppCompatActivity {

    private static final int SELECT_PICTURES = 1;
    Button bnt_event_save, btn_add_images, btn_add_participant;
    ListView lv_images, lv_participants;
    EditText event_date, event_reminder, event_title;
    List<ImageToEvent> eventsImages;
    Database db;
    Spinner personsParticipants;
    List<Person> allPersons;
    Event eventSelected;
    List<Person> spinnerArray;
    SQLiteDatabase sql;
    final Calendar myCalendarTime = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        allPersons = new ArrayList<>();
        spinnerArray = new ArrayList<>();
        eventsImages = new ArrayList<>();

        db = new Database(EventActivity.this);
        sql = db.getWritableDatabase();
        bnt_event_save = findViewById(R.id.btn_save_event);
        btn_add_images = findViewById(R.id.btn_add_image_to_event);
        btn_add_participant = findViewById(R.id.btn_add_participant);
        personsParticipants = findViewById(R.id.spinner_participants);

        lv_images = findViewById(R.id.lv_images);
        lv_participants = findViewById(R.id.lv_participants);

        event_date = findViewById(R.id.events_date);
        event_reminder = findViewById(R.id.events_reminder);
        event_title = findViewById(R.id.events_title);

        DatePickerDialog.OnDateSetListener date = (view, year, month, day) -> {
            myCalendarTime.set(Calendar.YEAR, year);
            myCalendarTime.set(Calendar.MONTH, month);
            myCalendarTime.set(Calendar.DAY_OF_MONTH, day);
            updateSelectedDate();
        };

        event_date.setOnClickListener(view -> new DatePickerDialog(
                EventActivity.this,
                date,
                myCalendarTime.get(Calendar.YEAR),
                myCalendarTime.get(Calendar.MONTH),
                myCalendarTime.get(Calendar.DAY_OF_MONTH))
                .show()
        );

        Intent intent = getIntent();
        eventSelected = (Event) intent.getSerializableExtra("event");
        if (eventSelected == null) {
            Bundle temp = intent.getBundleExtra("new_event");
            eventSelected = new Event(temp.getInt("spot_id"), "Empty title", 1, Constants.getDateFromString("2000-01-01"));
        }
        event_date.setText(Constants.getStringFromDate(eventSelected.getDate()));
        event_title.setText(eventSelected.getTitle());
        event_reminder.setText(String.valueOf(eventSelected.getReminder()));

        btn_add_images.setOnClickListener(v -> {
            if (eventSelected == null ) {
                quoteSaveEventFirst();
            }
            Intent selectImages = new Intent(Intent.ACTION_GET_CONTENT);
            selectImages.setType("image/*"); //allows any image file type. Change * to specific extension to limit it
            selectImages.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            startActivityForResult(Intent.createChooser(selectImages, "Select Picture"), SELECT_PICTURES);
        });

        btn_add_participant.setOnClickListener(v -> {
            if (eventSelected.getEvent_id() != 0) {
                Person selectedPerson = (Person) personsParticipants.getSelectedItem();

                for (Person tested : db.getPersonsByEventId(eventSelected.getEvent_id())
                ) {
                    if (selectedPerson.getId() == tested.getId()) {
                        Toast.makeText(EventActivity.this, "Person already in event!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                PersonToEvent p2e = new PersonToEvent(eventSelected.getEvent_id(), selectedPerson.getId());
                sql.insert(PersonToEvent.TABLE, null, p2e.putToDB());
                Toast.makeText(EventActivity.this, "Person inserted to current event", Toast.LENGTH_SHORT).show();
                populateParticipants();
            } else {
                Toast.makeText(EventActivity.this, "Save event first!", Toast.LENGTH_SHORT).show();
            }
        });

        // Save or update event
        bnt_event_save.setOnClickListener(v -> {
            if (!Constants.isValidDate(event_date.getText().toString())) {
                Toast.makeText(EventActivity.this, "Not valid date" + event_date.getText().toString(), Toast.LENGTH_SHORT).show();
                return;
            }
            eventSelected.setTitle(event_title.getText().toString());
            eventSelected.setDate(Constants.getDateFromString(event_date.getText().toString()));
            eventSelected.setReminder(Integer.parseInt(event_reminder.getText().toString()));

            if (eventSelected.getEvent_id() != 0) {
                sql.update(Event.TABLE, eventSelected.putToDB(), "id =" + eventSelected.getEvent_id(), null);
                Toast.makeText(EventActivity.this, "Event updated", Toast.LENGTH_SHORT).show();
            } else {
                sql.insert(Event.TABLE, null, eventSelected.putToDB());
                Toast.makeText(EventActivity.this, "New event inserted", Toast.LENGTH_SHORT).show();
            }
        });

        // Remove event participant
        lv_participants.setOnItemLongClickListener((adapterView, view, i, l) -> {
            sql.delete(PersonToEvent.TABLE, PersonToEvent.PERSON_ID + "=" + allPersons.get(i).getId(), null);
            Toast.makeText(EventActivity.this, "Item removed: " + allPersons.get(i).getName(), Toast.LENGTH_SHORT).show();
            allPersons.remove(allPersons.get(i));
            populateParticipants();
            return true;
        });

        // Show image
        lv_images.setOnItemClickListener((parent, view, position, id) -> {
            ImageToEvent i2e = eventsImages.get(position);
            Intent intent1 = new Intent(EventActivity.this, ImageActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("path", i2e.getLink());
            bundle.putString("title", i2e.getImageTitle());
            intent1.putExtra("bundle", bundle);
            startActivity(intent1);
        });

        // Remove image from list
        lv_images.setOnItemLongClickListener((adapterView, view, i, l) -> {
            sql.delete(ImageToEvent.TABLE, ImageToEvent.IMAGE_ID + "=" + eventsImages.get(i).getId(), null);
            Toast.makeText(EventActivity.this, "Image removed", Toast.LENGTH_SHORT).show();
            eventsImages.remove(eventsImages.get(i));
            populateImages();
            return true;
        });
        populatePersonsSpinner();
        populateParticipants();
        populateImages();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (resultCode == RESULT_OK) {
                if (requestCode == SELECT_PICTURES) {
                    Uri selectedImageUri = data.getData();
                    final String path = createCopyAndReturnRealPath(this, selectedImageUri);
                    String s =  createCopyAndReturnRealPath2(EventActivity.this, selectedImageUri);
                    ImageToEvent i2e = new ImageToEvent(eventSelected.getEvent_id(), s, s);
                    sql.insert(ImageToEvent.TABLE, null, i2e.putToDB());
                    populateImages();

                    // Set the image in ImageView
                    Intent intent = new Intent(EventActivity.this, ImageActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("path", path);
                    intent.putExtra("bundle", bundle);
                    startActivity(intent);
                }
            }
        } catch (Exception e) {
            Log.e("FileSelectorActivity", "Error during select file", e);
        }
    }

    @Nullable
    public static String createCopyAndReturnRealPath(
            @NonNull Context context, @NonNull Uri uri) {
        final ContentResolver contentResolver = context.getContentResolver();
        if (contentResolver == null)
            return null;

        String filePath = context.getApplicationInfo().dataDir + File.separator + "temp_file";
        File file = new File(filePath);
        try {
            InputStream inputStream = contentResolver.openInputStream(uri);
            if (inputStream == null)
                return null;
            OutputStream outputStream = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) > 0)
                outputStream.write(buf, 0, len);
            outputStream.close();
            inputStream.close();
        } catch (IOException ignore) {
            return null;
        }
        return file.getAbsolutePath();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        populatePersonsSpinner();
        populateParticipants();
        populateImages();
    }

    private void populatePersonsSpinner() {
        spinnerArray.clear();
        spinnerArray.addAll(db.getPersons());
        ArrayAdapter<Person> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, spinnerArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        personsParticipants.setAdapter(adapter);
    }

    private void populateParticipants() {
        allPersons.clear();
        allPersons.addAll(db.getPersonsByEventId(eventSelected.getEvent_id()));
        ArrayAdapter<Person> arrayAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                allPersons
        );
        lv_participants.setAdapter(arrayAdapter);
    }

    private void populateImages() {
        eventsImages.clear();
        eventsImages = db.getImagesByEventId(eventSelected.getEvent_id());
        ArrayAdapter<ImageToEvent> arrayAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                eventsImages
        );
        lv_images.setAdapter(arrayAdapter);
    }

    private void updateSelectedDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.DATE_FORMAT, Locale.ENGLISH);
        event_date.setText(dateFormat.format(myCalendarTime.getTime()));
    }

    @Nullable
    public static String createCopyAndReturnRealPath2(
            @NonNull Context context, @NonNull Uri uri) {
        final ContentResolver contentResolver = context.getContentResolver();
        if (contentResolver == null)
            return null;

        // Create file path inside app's data dir
        String filePath = context.getApplicationInfo().dataDir + File.separator
                + System.currentTimeMillis();

        File file = new File(filePath);
        try {
            InputStream inputStream = contentResolver.openInputStream(uri);
            if (inputStream == null)
                return null;

            OutputStream outputStream = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) > 0)
                outputStream.write(buf, 0, len);

            outputStream.close();
            inputStream.close();
        } catch (IOException ignore) {
            return null;
        }

        return file.getAbsolutePath();
    }

    private void quoteSaveEventFirst() {
        Toast.makeText(EventActivity.this, "Save event first!", Toast.LENGTH_SHORT).show();
    }


}

