package uhk.sa.smartarchive.entity;

import android.content.ContentValues;

import androidx.annotation.NonNull;

public class Spot {

    private int id;
    private double lat;
    private double lng;

    private String title;
    private String note;

    // DB_COLUMNS
    public static String TABLE = "spot";
    public static String SPOT_ID = "id";
    public static String SPOT_LAT = "spot_lat";
    public static String SPOT_LNG = "spot_lng";

    public static String SPOT_TITLE = "spot_title";
    public static String SPOT_NOTE = "spot_note";


    public Spot(double lat, double lng, String title, String note) {
        this.lat = lat;
        this.lng = lng;
        this.title = title;
        this.note = note;
    }

    public Spot(int id, double lat, double lng, String title, String note) {
        this.id = id;
        this.lat = lat;
        this.lng = lng;
        this.title = title;
        this.note = note;
    }

    public ContentValues putToDB()
    {
        ContentValues spot = new ContentValues();
        spot.put(SPOT_LAT, this.lat);
        spot.put(SPOT_LNG, this.lng);
        spot.put(SPOT_TITLE, this.title);
        spot.put(SPOT_NOTE,  this.note);

        return spot;
    }

    public static String createTable() {
        return "create table " + Spot.TABLE + " ( " +
                Spot.SPOT_ID + " INTEGER primary key AUTOINCREMENT," +
                Spot.SPOT_LAT + " INTEGER, " +
                Spot.SPOT_LNG + " INTEGER, " +
                Spot.SPOT_TITLE + " text, " +
                Spot.SPOT_NOTE + " text " + " ) ";
    }

    public static String dropTable() {
        return "DROP TABLE IF EXISTS " + TABLE;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @NonNull
    @Override
    public String toString() {
        return "Spot: " + title;
    }
}
