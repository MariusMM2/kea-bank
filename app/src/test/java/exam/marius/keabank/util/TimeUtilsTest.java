package exam.marius.keabank.util;

import org.junit.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.assertEquals;

public class TimeUtilsTest {

    @Test
    public void weeksLeft() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2019, 6, 10);
        Date date = calendar.getTime();

        calendar = Calendar.getInstance();
        calendar.set(2019, 6, 30);
        Date date2 = calendar.getTime();

        assertEquals(2, TimeUtils.weeksLeft(date, date2));

        //
        calendar = Calendar.getInstance();
        calendar.set(2019, 6, 24);
        date = calendar.getTime();

        calendar = Calendar.getInstance();
        calendar.set(2019, 6, 30);
        date2 = calendar.getTime();

        assertEquals(0, TimeUtils.weeksLeft(date, date2));

        //
        calendar = Calendar.getInstance();
        calendar.set(2019, 6, 23);
        date = calendar.getTime();

        calendar = Calendar.getInstance();
        calendar.set(2019, 6, 30);
        date2 = calendar.getTime();

        assertEquals(1, TimeUtils.weeksLeft(date, date2));
    }

    @Test
    public void daysLeft() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2019, 6, 7);
        Date date = calendar.getTime();

        calendar = Calendar.getInstance();
        calendar.set(2019, 6, 7);
        Date date2 = calendar.getTime();

        assertEquals(0, TimeUtils.daysLeft(date, date2));

        //
        calendar = Calendar.getInstance();
        calendar.set(2019, 6, 7);
        date = calendar.getTime();

        calendar = Calendar.getInstance();
        calendar.set(2019, 6, 8);
        date2 = calendar.getTime();

        assertEquals(1, TimeUtils.daysLeft(date, date2));

        //
        calendar = Calendar.getInstance();
        calendar.set(2019, 6, 7);
        date = calendar.getTime();

        calendar = Calendar.getInstance();
        calendar.set(2019, 6, 9);
        date2 = calendar.getTime();

        assertEquals(2, TimeUtils.daysLeft(date, date2));

        //
        calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        date = calendar.getTime();
        System.out.println(date);
        calendar = Calendar.getInstance();
        calendar.set(2019, 5, 10);
        date2 = calendar.getTime();
        System.out.println(date2);

        assertEquals(1, TimeUtils.daysLeft(date, date2));
    }

    @Test
    public void asd() {
        System.out.println(TimeUtils.getToday());
    }

    private Date getDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }
}