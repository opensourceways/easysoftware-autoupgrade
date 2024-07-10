package com.softwaremarket.autoupgrade.util;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


@Slf4j
public class DateTimeStrUtils {

    public static final String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss";

    public static final String DEFAULT_PATTERN_2 = "yyyy-MM-dd HH:mm";
    public static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd";

    public static final String MM_dd_HH_mm = "MM-dd HH:mm";

    public static final String HH_mm = "HH:mm";


    private DateTimeStrUtils() {

    }

    /**
     * '2024-1-20 9:00:00' -> '2024-01-20 09:00:00'
     * '2024/1/20 9:00:00' -> '2024-01-20 09:00:00'
     */
    public static String parseStr(String dateTime) {
        if (StringUtils.isEmpty(dateTime)) {
            return null;
        }
        dateTime = dateTime.replace("/", "-");
        if (dateTime.length() == 19) {
            return dateTime;
        }
        String[] dateTimeAttr = dateTime.split(" ");
        String dateStr = dateTimeAttr[0];
        String[] dateAttr = dateStr.split("-");
        String month = dateAttr[1];
        if (month.length() == 1) {
            month = "0" + month;
        }
        String day = dateAttr[2];
        if (day.length() == 1) {
            day = "0" + day;
        }
        String timeStr = dateTimeAttr[1];
        String[] timeAttr = timeStr.split(":");
        String hours = timeAttr[0];
        if (hours.length() == 1) {
            hours = "0" + hours;
        }
        return dateAttr[0] + "-" + month + "-" + day + " " + hours + ":" + timeAttr[1] + ":" + timeAttr[2];
    }



    /**
     * 返回未来N+1天的日期
     */
    public static List<String> getFutureDay(int daysToAdd) {
        LocalDate today = LocalDate.now();
        List<String> dates = Lists.newArrayList();
        for (int i = 0; i < daysToAdd; i++) {
            LocalDate nextDay = today.plusDays(i + 1L);
            dates.add(toLocalDateStr(nextDay, DEFAULT_DATE_PATTERN));
        }
        return dates;
    }

    public static LocalDateTime toDeafultLocalDateTime(String dateStr) {
        return LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern(DEFAULT_PATTERN));
    }

    public static LocalDateTime toLocalDateTime(String dateStr, String pattern) {
        return LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern(pattern));
    }

    public static String toLocalDateStr(LocalDate localDate, String pattern) {
        return localDate.format(DateTimeFormatter.ofPattern(pattern));
    }

    public static String toLocalDateTimeStr(LocalDateTime localDateTime, String pattern) {
        return localDateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    public static String toDefaultLocalDateStr(LocalDate localDate) {
        return localDate.format(DateTimeFormatter.ofPattern(DEFAULT_DATE_PATTERN));
    }

    /**
     * 获取当天日期
     */
    public static String getTodayDate() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern(DEFAULT_DATE_PATTERN));
    }




    /**
     * 获取 时 分
     */
    public static String getHHmm(String time) {
        String substring = time.substring(time.length() - 8);
        return substring.substring(0, substring.length() - 3);
    }

    public static String getMMddHHmm(String time) {
        LocalDateTime localDateTime = toLocalDateTime(time, DEFAULT_PATTERN);
        return localDateTime.format(DateTimeFormatter.ofPattern(MM_dd_HH_mm));
    }
}
