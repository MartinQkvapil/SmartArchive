package uhk.sa.smartarchive.entity;

import android.content.ContentValues;

import androidx.annotation.NonNull;

public class ImageToEvent {
    private final int eventId;
    private int id;
    private final String link;
    private final String imageTitle;

    // DB_COLUMNS
    public static String TABLE = "image_to_event";
    public static String IMAGE_ID = "image_id";
    public static String IMAGE_TITLE = "title";
    public static String EVENT_ID = "event_id";
    public static String LINK = "link";
    public ContentValues putToDB() {
        ContentValues person_to_event = new ContentValues();
        person_to_event.put(EVENT_ID, this.eventId);
        person_to_event.put(IMAGE_TITLE, this.imageTitle);
        person_to_event.put(LINK, this.link);
        return person_to_event;
    }
    public static String createTable() {
        return "CREATE TABLE " + TABLE + " ( " +
                IMAGE_ID + " INTEGER primary key AUTOINCREMENT," +
                EVENT_ID + " INTEGER," +
                IMAGE_TITLE + " TEXT," +
                LINK + " TEXT " + " ) ";
    }
    public static String dropTable() {
        return "DROP TABLE IF EXISTS " + TABLE;
    }

    public ImageToEvent(int eventId, String link, String imageTitle) {
        this.eventId = eventId;
        this.link = link;
        this.imageTitle = imageTitle;
    }
    public ImageToEvent(int imageId, int eventId, String link, String imageTitle) {
        this.id = imageId;
        this.eventId = eventId;
        this.link = link;
        this.imageTitle = imageTitle;
    }

    @NonNull
    @Override
    public String toString() {
        return "Image: " + id;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getLink() {
        return link;
    }
    public String getImageTitle() {
        return imageTitle;
    }
}
