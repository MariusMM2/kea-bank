package exam.marius.keabank.util;

import org.junit.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import static org.junit.Assert.assertEquals;

public class TimeUtilsTest {

    @Test
    public void weeksLeft() {
        LocalDate d1 = LocalDate.parse("2019-04-10");
        LocalDate d2 = LocalDate.parse("2019-04-30");
        long weeksLeft = TimeUtils.weeksLeft(getDate(d1), getDate(d2));
        assertEquals(2, weeksLeft);

        d1 = LocalDate.parse("2019-04-24");
        d2 = LocalDate.parse("2019-04-30");
        weeksLeft = TimeUtils.weeksLeft(getDate(d1), getDate(d2));
        assertEquals(0, weeksLeft);

        d1 = LocalDate.parse("2019-04-23");
        d2 = LocalDate.parse("2019-04-30");
        weeksLeft = TimeUtils.weeksLeft(getDate(d1), getDate(d2));
        assertEquals(1, weeksLeft);
    }

    private Date getDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }
}