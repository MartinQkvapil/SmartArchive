package uhk.sa.smartarchive.entity;

import android.content.ContentValues;

public class PersonToEvent {
    private int id;
    private final int event_id;
    private final int person_id;

    // DB_COLUMNS
    public static String TABLE = "person_to_event";
    public static String ID = "id";
    public static String PERSON_ID = "person_id";
    public static String EVENT_ID = "event_id";

    public PersonToEvent(int event_id, int person_id) {
        this.event_id = event_id;
        this.person_id = person_id;
    }
    public ContentValues putToDB() {
        ContentValues person_to_event = new ContentValues();
        person_to_event.put(PERSON_ID, this.person_id);
        person_to_event.put(EVENT_ID, this.event_id);
        return person_to_event;
    }
    public static String createTable() {
        return "CREATE TABLE " + TABLE + " ( " +
                ID + " INTEGER primary key," +
                PERSON_ID + " INTEGER," +
                EVENT_ID + " INTEGER " + " ) ";
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public static String dropTable() {
        return "DROP TABLE IF EXISTS " + TABLE;
    }


}
