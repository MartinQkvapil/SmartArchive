package uhk.sa.smartarchive;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import uhk.sa.smartarchive.entity.Person;

public class PersonActivity extends AppCompatActivity {

    private Database db;
    EditText person_name, person_surname, person_date;
    Button btn_save_per;
    Person person;
    ListView lv_persons;

    SQLiteDatabase sql;

    final Calendar myCalendar = Calendar.getInstance();
    List<Person> personsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person);

        db = new Database(PersonActivity.this);
        sql = db.getWritableDatabase();
        btn_save_per = findViewById(R.id.btn_save_per);
        person_name = findViewById(R.id.person_name);
        person_surname = findViewById(R.id.person_surname);
        person_date = findViewById(R.id.person_date);
        lv_persons = findViewById(R.id.lv_person_list);

        DatePickerDialog.OnDateSetListener date = (view, year, month, day) -> {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, month);
            myCalendar.set(Calendar.DAY_OF_MONTH, day);
            updateLabel();
        };

        person_date.setOnClickListener(view -> new DatePickerDialog(
                PersonActivity.this,
                date, myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show());

        // Remove person from listview
        lv_persons.setOnItemLongClickListener((adapterView, view, i, l) -> {
            sql.delete(Person.TABLE, Person.PERSON_ID + "=" + personsList.get(i).getId(), null);
            personsList.remove(i);
            updateView();
            Toast.makeText(PersonActivity.this, "Person removed", Toast.LENGTH_SHORT).show();
            return true;
        });

        // Add new person
        btn_save_per.setOnClickListener(v -> {
            if (!Constants.isValidDate(person_date.getText().toString())) {
                Toast.makeText(PersonActivity.this, "Please specify person birthday.", Toast.LENGTH_SHORT).show();
                return;
            }
            person = new Person(
                    person_name.getText().toString(),
                    person_surname.getText().toString(),
                    Constants.getDateFromString(person_date.getText().toString()));

            for (Person checked : personsList
            ) {
                if (person.equals(checked)) {
                    Toast.makeText(PersonActivity.this, "This person is already in table.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            sql.insert(Person.TABLE, null, person.putToDB());
            updateView();
        });

        updateView();
    }

    private void updateLabel() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.DATE_FORMAT, Locale.ENGLISH);
        person_date.setText(dateFormat.format(myCalendar.getTime()));
    }

    private void updateView() {
        personsList.clear();
        personsList.addAll(db.getPersons());
        ArrayAdapter<Person> arrayAdapter = new ArrayAdapter<Person>(
                this,
                android.R.layout.simple_list_item_1,
                personsList
        );
        lv_persons.setAdapter(arrayAdapter);
    }
}