package data;

import org.junit.jupiter.api.Test;
import utils.DateUtils;

import java.util.Date;

public class UtilsTest {

    @Test
    void testDatesConverting() {
        Date date0 = DateUtils.numbersToDate(2024, 5, 1);
        Date date1 = DateUtils.numbersToDate(2024, 5, 31);
        System.out.println("Date0: " + DateUtils.dateToStringISO(date0));
        System.out.println("Date1: " + DateUtils.dateToStringISO(date1));

        int period0 = DateUtils.dateToInt(date0);
        int period1 = DateUtils.dateToInt(date1);
        System.out.println("Date0 int: " + period0);
        System.out.println("Date1 int: " + period1);

        Date dateRes0 = DateUtils.intToDate(period0);
        Date dateRes1 = DateUtils.intToDate(period1);
        System.out.println("Date0 result: " + DateUtils.dateToStringISO(dateRes0));
        System.out.println("Date1 result: " + DateUtils.dateToStringISO(dateRes1));

        Date date2 = DateUtils.numbersToDate(2024, 2, 15);
        Date dateBegin = DateUtils.dateMonthFirstDay(date2);
        Date dateEnd = DateUtils.dateMonthLastDay(date2);
        Date datePrevBegin = DateUtils.dateMonthFirstDay(DateUtils.dateAddDays(DateUtils.dateMonthFirstDay(date2), -1));
        Date datePrevEnd = DateUtils.dateMonthLastDay(datePrevBegin);
        System.out.println("First day of the month: " + DateUtils.dateToStringISO(dateBegin));
        System.out.println("Last day of the month: " + DateUtils.dateToStringISO(dateEnd));
        System.out.println("Previous period: " + DateUtils.dateToStringISO(datePrevBegin) + " - " + DateUtils.dateToStringISO(datePrevEnd));
    }

}
