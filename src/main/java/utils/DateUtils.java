package utils;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {

    public static String dateToStringISO(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        return format.format(date);
    }

    public static Integer dateToInt(Date date) {
        if (date == null)
            return null;

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        return cal.get(Calendar.YEAR) * 10000 + (cal.get(Calendar.MONTH) + 1) * 100 + cal.get(Calendar.DAY_OF_MONTH);
    }

    public static Date intToDate(Integer intDate) {
        if (intDate == null || intDate < 19000000 || intDate > 29000000)
            return null;

        return numbersToDate(intDate / 10000, intDate / 100 % 100, intDate % 100);
    }

    public static Date numbersToDate(int year, int month, int day) {
        LocalDate localDate = LocalDate.of(year, month, day);

        return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

    public static Date dateAddDays(Date date, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, days); //minus number would decrement the days

        return cal.getTime();
    }

    public static int dateGetDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        return cal.get(Calendar.DAY_OF_MONTH);
    }

    public static int dateGetMonth(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        return cal.get(Calendar.MONTH) + 1;
    }

    public static int getYear(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        return cal.get(Calendar.YEAR);
    }

    public static int dateDifferenceDays(Date date1, Date date2) throws Exception {
        if (date1 == null || date2 == null) {
            throw new Exception("NULL date");
        }

        long difference = date1.getTime() - date2.getTime();

        return (int)(difference / (24 * 60 * 60 * 1000));
    }

}
