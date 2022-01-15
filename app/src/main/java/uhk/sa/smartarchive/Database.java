package uhk.sa.smartarchive;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.PointF;
import android.location.Location;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import uhk.sa.smartarchive.entity.Event;
import uhk.sa.smartarchive.entity.ImageToEvent;
import uhk.sa.smartarchive.entity.Person;
import uhk.sa.smartarchive.entity.PersonToEvent;
import uhk.sa.smartarchive.entity.Spot;

public class Database extends SQLiteOpenHelper {

    public Database(Context context) {
        super(context, Constants.DB_NAME, null, Constants.VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Person.createTable());
        db.execSQL(Spot.createTable());
        db.execSQL(Event.createTable());
        db.execSQL(ImageToEvent.createTable());
        db.execSQL(PersonToEvent.createTable());
        setDemoData(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public List<Person> getPersons() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                Person.TABLE,   // The table to query
                null,             // The array of columns to return (pass null to get all)
                null,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                null               // The sort order
        );
        List<Person> persons = new ArrayList<>();
        while(cursor.moveToNext()) {
            Person person;
            person = new Person(
                Integer.parseInt(cursor.getString(cursor.getColumnIndex(Person.PERSON_ID))),
                cursor.getString(cursor.getColumnIndex(Person.PERSON_NAME)),
                cursor.getString(cursor.getColumnIndex(Person.PERSON_SURNAME)),
                Constants.getDateFromString(cursor.getString(cursor.getColumnIndex(Person.PERSON_BIRTH_DAY)))
            );
            persons.add(person);
        }
        cursor.close();
        return persons;
    }

    public List<Event> getEvents() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                Event.TABLE,   // The table to query
                null,             // The array of columns to return (pass null to get all)
                null,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                null               // The sort order
        );
        List<Event> events = new ArrayList<>();
        while(cursor.moveToNext()) {
            Event event;
            event = new Event(
                    Integer.parseInt(cursor.getString(cursor.getColumnIndex(Event.EVENT_ID))),
                    Integer.parseInt(cursor.getString(cursor.getColumnIndex(Event.SPOT_ID))),
                    cursor.getString(cursor.getColumnIndex(Event.EVENT_TITLE)),
                    cursor.getInt(cursor.getColumnIndex(Event.EVENT_DESCRIPTION)),
                    Constants.getDateFromString(cursor.getString(cursor.getColumnIndex(Event.DATE)))
            );
            events.add(event);
        }
        cursor.close();
        return events;
    }

    public int getRowCountOfTable(String TableName) {
        int count;

        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(1) as count " +
                "FROM " + TableName;
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        count = Integer.parseInt(cursor.getString(cursor.getColumnIndex("count")));
        cursor.close();

        return count;
    }

    public List<Spot> getSpots() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                Spot.TABLE,// The table to query
                null,   // The array of columns to return (pass null to get all)
                null,   // The columns for the WHERE clause
                null,// The values for the WHERE clause
                null,   // don't group the rows
                null,    // don't filter by row groups
                null    // The sort order
        );
        List<Spot> spots = new ArrayList<>();
        while(cursor.moveToNext()) {
            Spot spot;
            spot = new Spot(
                Integer.parseInt(cursor.getString(cursor.getColumnIndex(Spot.SPOT_ID))),
                Double.parseDouble(cursor.getString(cursor.getColumnIndex(Spot.SPOT_LAT))),
                Double.parseDouble(cursor.getString(cursor.getColumnIndex(Spot.SPOT_LNG))),
                cursor.getString(cursor.getColumnIndex(Spot.SPOT_TITLE)),
                cursor.getString(cursor.getColumnIndex(Spot.SPOT_NOTE))
            );
            spots.add(spot);
        }
        cursor.close();
        db.close();
        return spots;
    }

    public List<Spot> getNearestSpots(Location location) {
        PointF center = new PointF((float)location.getLatitude(), (float)location.getLongitude());
        final double mult = 1;
        PointF p1 = Calculations.calculateMarginalPosition(center, mult * Constants.SEARCHING_RADIUS, 0);
        PointF p2 = Calculations.calculateMarginalPosition(center, mult * Constants.SEARCHING_RADIUS, 90);
        PointF p3 = Calculations.calculateMarginalPosition(center, mult * Constants.SEARCHING_RADIUS, 180);
        PointF p4 = Calculations.calculateMarginalPosition(center, mult * Constants.SEARCHING_RADIUS, 270);

        String strWhere = " WHERE " +
        Spot.SPOT_LAT + " > " + p3.x + " AND " +
        Spot.SPOT_LAT + " < " + p1.x + " AND " +
        Spot.SPOT_LNG + " < " + p2.y + " AND " +
        Spot.SPOT_LNG + " > " + p4.y;

        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * " +
                "FROM " + Spot.TABLE + strWhere;
        Cursor cursor = db.rawQuery(query, null);

        List<Spot> spots = new ArrayList<>();
        while(cursor.moveToNext()) {
            Spot spot;
            spot = new Spot(
                    Integer.parseInt(cursor.getString(cursor.getColumnIndex(Spot.SPOT_ID))),
                    Double.parseDouble(cursor.getString(cursor.getColumnIndex(Spot.SPOT_LAT))),
                    Double.parseDouble(cursor.getString(cursor.getColumnIndex(Spot.SPOT_LNG))),
                    cursor.getString(cursor.getColumnIndex(Spot.SPOT_TITLE)),
                    cursor.getString(cursor.getColumnIndex(Spot.SPOT_NOTE))
            );
            spots.add(spot);
        }

        Collections.sort(
                spots,
                createComparator(location.getLatitude(), location.getLongitude()));

        cursor.close();
        return spots;
    }

    private static Comparator<Spot> createComparator(double x, double y) {
        return (p0, p1) -> {
            double ds0 = Calculations.getDistanceBetweenTwoPoints(
                    new PointF((float)p0.getLat(), (float)p0.getLng()),
                    new PointF((float)x, (float)y));

            double ds1 = Calculations.getDistanceBetweenTwoPoints(
                    new PointF((float)p1.getLat(), (float)p1.getLng()),
                    new PointF((float)x, (float)y));
            return Double.compare(ds0, ds1);
        };
    }

    public List<Person> getPersonsByEventId(int eventId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * " +
                "FROM person_to_event " +
                "JOIN person ON person_to_event.person_id = person.id " +
                "WHERE event_id = " +eventId;
        Cursor cursor = db.rawQuery(query, null);

        List<Person> persons = new ArrayList<>();
        while(cursor.moveToNext()) {
            Person person;
            person = new Person(
                    Integer.parseInt(cursor.getString(cursor.getColumnIndex(Person.PERSON_ID))),
                    cursor.getString(cursor.getColumnIndex(Person.PERSON_NAME)),
                    cursor.getString(cursor.getColumnIndex(Person.PERSON_SURNAME)),
                    Constants.getDateFromString(cursor.getString(cursor.getColumnIndex(Person.PERSON_BIRTH_DAY)))
            );
            persons.add(person);
        }
        cursor.close();
        return persons;
    }

    public List<Event> getEventsByPersonId(int personId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * " +
                "FROM event " +
                "JOIN person_to_event ON person_to_event.event_id = event.id " +
                "WHERE " + PersonToEvent.PERSON_ID + " = " + personId;
        Cursor cursor = db.rawQuery(query, null);

        List<Event> events = new ArrayList<>();
        while(cursor.moveToNext()) {
            Event event;
            event = new Event(
                    Integer.parseInt(cursor.getString(cursor.getColumnIndex(Event.EVENT_ID))),
                    Integer.parseInt(cursor.getString(cursor.getColumnIndex(Event.SPOT_ID))),
                    cursor.getString(cursor.getColumnIndex(Event.EVENT_TITLE)),
                    cursor.getInt(cursor.getColumnIndex(Event.EVENT_DESCRIPTION)),
                    Constants.getDateFromString(cursor.getString(cursor.getColumnIndex(Event.DATE)))
            );
            events.add(event);
        }
        cursor.close();
        return events;
    }

    public void setDemoData(SQLiteDatabase db) {
        // Persons
        Person person0 = new Person("Martin", "Kvapil", Constants.getDateFromString("1992-01-05"));
        Person person1 = new Person("Ondřej", "Majvald", Constants.getDateFromString("1993-01-05"));
        Person person2 = new Person("Zdenek", "Kvapil", Constants.getDateFromString("1994-01-05"));
        Person person3 = new Person("Jiří", "Kvapil", Constants.getDateFromString("1995-01-05"));
        long p0id = db.insert(Person.TABLE, null, person0.putToDB());
        db.insert(Person.TABLE, null, person1.putToDB());
        db.insert(Person.TABLE, null, person2.putToDB());
        db.insert(Person.TABLE, null, person3.putToDB());
        // Spots
        Spot spot0 = new Spot(50.02296035794438, 16.519516548894853, "Kampelička", "Spolek Kampelička a.s.");
        long row_id = db.insert(Spot.TABLE, null, spot0.putToDB());

        // events
        Event event0 = new Event((int) row_id, "Oslava narozenin", 10, Constants.getDateFromString("2021-01-25"));
        long e0id = db.insert(Event.TABLE, null, event0.putToDB());
        Event event1 = new Event((int) row_id, "Oslava Nového roku 2021", 1, Constants.getDateFromString("2020-12-31"));
        long e1id = db.insert(Event.TABLE, null, event1.putToDB());

        PersonToEvent p2e0 = new PersonToEvent((int)e0id, (int)p0id);
        db.insert(PersonToEvent.TABLE, null, p2e0.putToDB());
        PersonToEvent p2e1 = new PersonToEvent((int)e1id, (int)p0id);
        db.insert(PersonToEvent.TABLE, null, p2e1.putToDB());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(Person.dropTable());
        db.execSQL(Spot.dropTable());
        db.execSQL(ImageToEvent.dropTable());
        db.execSQL(PersonToEvent.dropTable());
        db.execSQL(Event.dropTable());
    }

    public List<ImageToEvent> getImagesByEventId(int event_id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * " +
                "FROM image_to_event " +
                "JOIN event ON image_to_event.event_id = event.id " +
                "WHERE image_to_event.event_id = " + event_id;
        Cursor cursor = db.rawQuery(query, null);

        List<ImageToEvent> images = new ArrayList<>();
        while(cursor.moveToNext()) {
            ImageToEvent image;
            image = new ImageToEvent(
                    Integer.parseInt(cursor.getString(cursor.getColumnIndex(ImageToEvent.IMAGE_ID))),
                    Integer.parseInt(cursor.getString(cursor.getColumnIndex(ImageToEvent.EVENT_ID))),
                    cursor.getString(cursor.getColumnIndex(ImageToEvent.IMAGE_TITLE)),
                    cursor.getString(cursor.getColumnIndex(ImageToEvent.LINK))
            );
            images.add(image);
        }
        cursor.close();
        return images;
    }

    public List<Event> getEventsBySpotId(Integer spotId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * " +
                "FROM event " +
                "WHERE " + Event.SPOT_ID + " = " + spotId;
        Cursor cursor = db.rawQuery(query, null);

        List<Event> events = new ArrayList<>();
        while(cursor.moveToNext()) {
            Event event;
            event = new Event(
                Integer.parseInt(cursor.getString(cursor.getColumnIndex(Event.EVENT_ID))),
                Integer.parseInt(cursor.getString(cursor.getColumnIndex(Event.SPOT_ID))),
                cursor.getString(cursor.getColumnIndex(Event.EVENT_TITLE)),
                cursor.getInt(cursor.getColumnIndex(Event.EVENT_DESCRIPTION)),
                Constants.getDateFromString(cursor.getString(cursor.getColumnIndex(Event.DATE)))
            );
            events.add(event);
        }
        cursor.close();
        return events;
    }

    public List<Event> getSortedEvents() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                Event.TABLE,
                null,
                null,
                null, null, null, "date ASC");

        List<Event> events = new ArrayList<>();
        while(cursor.moveToNext()) {
            Event event;
            event = new Event(
                    Integer.parseInt(cursor.getString(cursor.getColumnIndex(Event.EVENT_ID))),
                    Integer.parseInt(cursor.getString(cursor.getColumnIndex(Event.SPOT_ID))),
                    cursor.getString(cursor.getColumnIndex(Event.EVENT_TITLE)),
                    cursor.getInt(cursor.getColumnIndex(Event.EVENT_DESCRIPTION)),
                    Constants.getDateFromString(cursor.getString(cursor.getColumnIndex(Event.DATE)))
            );
            events.add(event);
        }
        cursor.close();
        return events;
    }
}
