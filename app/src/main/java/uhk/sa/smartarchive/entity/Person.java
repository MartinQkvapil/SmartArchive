package uhk.sa.smartarchive.entity;

import android.content.ContentValues;

import androidx.annotation.NonNull;

import java.util.Date;

import uhk.sa.smartarchive.Constants;

public class Person {
    private int id;
    private String name;
    private final String surname;
    private final Date birth_day;

    // DB_COLUMNS
    public static String TABLE = "person";
    public static String PERSON_ID = "id";
    public static String PERSON_NAME = "person_name";
    public static String PERSON_SURNAME = "person_surname";
    public static String PERSON_BIRTH_DAY = "person_birth_day";

    public Person(String name, String surname, Date birth_day) {
        this.name = name;
        this.surname = surname;
        this.birth_day = birth_day;
    }
    public Person(int id, String name, String surname, Date birth_day) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.birth_day = birth_day;
    }

    public ContentValues putToDB() {
        ContentValues person = new ContentValues();
        person.put(PERSON_NAME, this.name);
        person.put(PERSON_SURNAME, this.surname);
        person.put(PERSON_BIRTH_DAY,  Constants.getStringFromDate(this.birth_day));
        return person;
    }
    public static String createTable() {
        return "CREATE TABLE " + Person.TABLE + " ( " +
        Person.PERSON_ID + " INTEGER primary key AUTOINCREMENT," +
                Person.PERSON_NAME + " text," +
                Person.PERSON_SURNAME + " text," +
                Person.PERSON_BIRTH_DAY + " text " + " ) ";
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
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getSurname() {
        return surname;
    }

    @NonNull
    @Override
    public String toString() {
        return getPersonNice();
    }
    public String getPersonNice() {
        return "ID: " + id + " - " + surname + " " + name;
    }
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != Person.class) {
            return false;
        }
        Person other = (Person) obj;
        return other.getName().equals(this.getName()) || other.getSurname().equals(this.getSurname());
    }
}
