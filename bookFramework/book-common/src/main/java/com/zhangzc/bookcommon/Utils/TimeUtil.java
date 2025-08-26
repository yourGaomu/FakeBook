package com.zhangzc.bookcommon.Utils;

import java.time.*;
import java.util.Date;

public class TimeUtil {

    public static Date getDateTime(LocalDate localDate) {
        // 将 LocalDate 与当前时间合并为 LocalDateTime
        LocalDateTime localDateTime = localDate.atTime(LocalTime.now());
        // 转换为 Instant
        Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
        // 转换为 Date
        return Date.from(instant);
    }

    public static LocalDateTime getLocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static Date getDateTime(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }


}
