package com.zhangzc.bookcommon.Utils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class TimeUtil {

    private static final DateTimeFormatter FULL_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    /**
     * 计算年龄
     *
     * @param birthDate 出生日期（LocalDate）
     * @return 计算得到的年龄（以年为单位）
     */
    public static int calculateAge(LocalDate birthDate) {
        // 获取当前日期
        LocalDate currentDate = LocalDate.now();

        // 计算出生日期到当前日期的 Period 对象
        Period period = Period.between(birthDate, currentDate);

        // 返回完整的年份（即年龄）
        return period.getYears();
    }


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

    public static LocalDate getLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static String getLocalDateTime() {
        LocalDateTime time = LocalDateTime.now();
        return time.format(FULL_TIME_FORMATTER);
    }

    //当天时间减去一天
    public static String getLocalDateTimeDownOneDay(){
        LocalDateTime time = LocalDateTime.now().minusDays(1);
        return time.format(FULL_TIME_FORMATTER);
    }

    //当天时间减去一周
    public static String getLocalDateTimeDownOneWeek(){
        LocalDateTime time = LocalDateTime.now().minusWeeks(1);
        return time.format(FULL_TIME_FORMATTER);
    }

    //当天时间减去半年
    public static String getLocalDateTimeDownHalfYear(){
        LocalDateTime time = LocalDateTime.now().minusMonths(6);
        return time.format(FULL_TIME_FORMATTER);
    }

}
