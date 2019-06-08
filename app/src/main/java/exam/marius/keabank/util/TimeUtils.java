package exam.marius.keabank.util;

import java.time.*;
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
}
