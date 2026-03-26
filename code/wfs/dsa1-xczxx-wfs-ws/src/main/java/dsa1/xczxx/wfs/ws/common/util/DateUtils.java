package dsa1.xczxx.wfs.ws.common.util;

import java.util.Date;
import java.time.LocalDate;
import java.time.ZoneId;

public class DateUtils {

    /**
     * 从Date对象获取年份
     */
    public static int getYear(Date date) {
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .getYear();
    }

    /**
     * 从Date对象获取月份 (1-12)
     */
    public static int getMonth(Date date) {
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .getMonthValue();
    }

    /**
     * 从Date对象获取日期 (1-31)
     */
    public static int getDay(Date date) {
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .getDayOfMonth();
    }

    /**
     * 获取年月日数组 [年, 月, 日]
     */
    public static int[] getYearMonthDay(Date date) {
        LocalDate localDate = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        return new int[] {
                localDate.getYear(),
                localDate.getMonthValue(),
                localDate.getDayOfMonth()
        };
    }

    // 使用示例
//    public static void main(String[] args) {
//        Date date = new Date();
//
//        System.out.println("年份: " + getYear(date));
//        System.out.println("月份: " + getMonth(date));
//        System.out.println("日期: " + getDay(date));
//
//        int[] ymd = getYearMonthDay(date);
//        System.out.println("年月日: " + ymd[0] + "-" + ymd[1] + "-" + ymd[2]);
//    }
}