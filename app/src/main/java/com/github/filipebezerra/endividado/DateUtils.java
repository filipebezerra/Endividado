package com.github.filipebezerra.endividado;

import android.support.annotation.NonNull;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * .
 *
 * @author Filipe Bezerra
 * @version #, 22/10/2015
 * @since #
 */
public class DateUtils {
    private static final DateFormat sDateFormat = DateFormat.getDateInstance();

    public static int getMonthNumber(@NonNull String monthName) throws ParseException {
        final Date date = new SimpleDateFormat("MMMM", Locale.getDefault()).parse(monthName);
        final Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.MONTH);
    }

    public static int getCurrentMonthNumber() {
        final Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.MONTH);
    }

    public static long getDateInMillis(String dateString) throws ParseException {
        return sDateFormat.parse(dateString).getTime();
    }

    public static DateFormat getDateFormat() {
        return sDateFormat;
    }
}
