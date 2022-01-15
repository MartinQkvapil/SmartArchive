package uhk.sa.smartarchive.entity;

import android.content.ContentValues;
import androidx.annotation.NonNull;
import java.io.Serializable;
import java.util.Date;
import uhk.sa.smartarchive.Constants;

public class Event implements Serializable {
    private int event_id;
    private String title;
    private int reminder;
    private Date date;
    private final int spot;

    // DB_COLUMNS
    public static String TABLE = "event";
    public static String EVENT_ID = "id";
    public static String DATE = "date";
    public static String SPOT_ID = "spot_id";

    public static String EVENT_TITLE = "event_title";
    public static String EVENT_DESCRIPTION = "event_description";

    public Event(int event_id, int spot, String title, int reminder, Date date) {
        this.event_id = event_id;
        this.spot = spot;
        this.title = title;
        this.reminder = reminder;
        this.date = date;
    }
    public Event(int spot, String title, int reminder, Date date) {
       this.spot = spot;
        this.title = title;
        this.reminder = reminder;
        this.date = date;
    }
    public ContentValues putToDB() {
        ContentValues person = new ContentValues();

        if (this.event_id > 0) {
            person.put(EVENT_ID, this.event_id);
        }
        person.put(EVENT_TITLE, this.title);
        person.put(EVENT_DESCRIPTION, this.reminder);
        person.put(SPOT_ID, this.spot);
        person.put(DATE,  Constants.getStringFromDate(this.date));

        return person;
    }
    public static String createTable() {
        return "create table " + TABLE + " ( " +
                EVENT_ID + " INTEGER primary key AUTOINCREMENT," +
                DATE + " text, " +
                SPOT_ID + " INTEGER, " +
                EVENT_TITLE + " text, " +
                EVENT_DESCRIPTION + " text " + " ) ";
    }

    @NonNull
    @Override
    public String toString() {
        return "Date: " +  Constants.getStringFromDate(date) + " - Title: " + title;
    }
    public static String dropTable() {
        return "DROP TABLE IF EXISTS " + TABLE;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public int getReminder() {
        return reminder;
    }
    public void setReminder(int reminder) {
        this.reminder = reminder;
    }
    public Date getDate() {
        return date;
    }
    public void setDate(Date date) {
        this.date = date;
    }
    public int getEvent_id() {
        return event_id;
    }
    public int getSpot() {
        return spot;
    }

}
