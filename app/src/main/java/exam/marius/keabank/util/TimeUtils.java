package exam.marius.keabank.util;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class TimeUtils {

    public static Date addMonths(Date dueDate, long months) {
        Instant instant = Instant.ofEpochMilli(dueDate.getTime());
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        LocalDate localDate = localDateTime.toLocalDate();

        LocalDateTime startOfDay = localDate.plusMonths(months).atStartOfDay();
        ZonedDateTime zonedDateTime = startOfDay.atZone(ZoneId.systemDefault());
        return Date.from(zonedDateTime.toInstant());
    }

    public static long weeksLeft(Date dateBefore, Date dateAfter) {
        if (dateBefore.compareTo(dateAfter) > -1) {
            // dateAfter is same or before dateBefore, return 0 weeks
            return 0;
        } else {
            // dateAfter is after dateBefore, return difference in weeks
            Instant instantBefore = Instant.ofEpochMilli(dateBefore.getTime());
            Instant instantAfter = Instant.ofEpochMilli(dateAfter.getTime());

            LocalDateTime localDateTimeBefore = LocalDateTime.ofInstant(instantBefore, ZoneId.systemDefault());
            LocalDateTime localDateTimeAfter = LocalDateTime.ofInstant(instantAfter, ZoneId.systemDefault());

            return ChronoUnit.WEEKS.between(localDateTimeBefore, localDateTimeAfter);
        }
    }
}
