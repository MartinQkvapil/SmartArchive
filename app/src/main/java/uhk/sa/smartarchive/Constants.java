package uhk.sa.smartarchive;

import android.annotation.SuppressLint;
import android.util.Log;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Constants {
    protected static final int VERSION = 33;
    protected static final String DB_NAME = "smart_archives_" + VERSION;
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static int SEARCHING_RADIUS = 400;

    @SuppressLint("SimpleDateFormat")
    public static Date getDateFromString(String dateString) {
        Date date = null;
        try {
            date = new SimpleDateFormat(DATE_FORMAT).parse(dateString);
        } catch (ParseException e) {
            Log.d("SimpleDateFormat err:", "Error during parsing string to date!");
            e.printStackTrace();
        }
        return date;
    }

    public static String getStringFromDate(Date date) {
        @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        return dateFormat.format(date);
    }

    public static boolean isValidDate(String inDate) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        dateFormat.setLenient(false);
        try {
            dateFormat.parse(inDate.trim());
        } catch (ParseException pe) {
            return false;
        }
        return true;
    }

    public static String round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        DecimalFormat deciFormat = new DecimalFormat();
        deciFormat.setMaximumFractionDigits(places);
        return deciFormat.format(value);
    }

    public static Date getCurrentDate() {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat formatter = new SimpleDateFormat(Constants.DATE_FORMAT);
        Date date = new Date();
        return Constants.getDateFromString(formatter.format(date));
    }

    public static Date getDatePlusReminder(Date oldDate, int reminder) {
        Calendar c = Calendar.getInstance();
        c.setTime(oldDate);
        c.add(Calendar.YEAR, reminder);
        return c.getTime();
    }
}
