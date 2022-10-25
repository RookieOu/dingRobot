package com.ding.utils;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Months;
import org.joda.time.Weeks;
import org.joda.time.format.DateTimeFormat;

import java.util.Arrays;

/**
 * @author yanou
 * @date 2022年10月24日 4:07 下午
 */
public class TimeUtils {
    public static final int DAILY_RESET_HOUR = 5;
    public static final int DAY_HOURS = 24;
    public static final int DAY_MINUTES = 1440;
    public static final int DAY_SECONDS = 86400;
    public static final long DAY_MILLS = 86400000;
    public static final int SEC_MILLS = 1000;
    public static final int HOUR_MINUTES = 60;
    public static final int MINUTE_SECS = 60;
    public static final int HOUR_SECS = 3600;
    public static final int HOUR_MILLS = 3600000;
    public static final int MINUTE_MILLS = 60000;
    public static final int WEEK_DAYS = 7;
    public static final int YEAR_MOUTH = 12;

    private static final String YM_FMT = "yyyy-MM";
    private static final String YMD_FMT = "yyyy-MM-dd";
    private static final String YMDHIS_FMT = "yyyy-MM-dd HH:mm:ss";

    private TimeUtils() {
    }

    /**
     * 当前业务毫秒数
     *
     * @return long
     */
    public static long getBizMillis() {
        return System.currentTimeMillis();
    }

    /**
     * 当前物理毫秒数
     *
     * @return long
     */
    public static long getPhysicalMillis() {
        return System.currentTimeMillis();
    }

    /**
     * 业务时间戳（秒级）
     *
     * @return long
     */
    public static long getBizTs() {
        return getBizMillis() / SEC_MILLS;
    }

    /**
     * 物理时间戳（秒级）
     *
     * @return long
     */
    public static long getPhysicalTS() {
        return System.currentTimeMillis() / SEC_MILLS;
    }

    /**
     * 时间戳转换成joda DateTime
     *
     * @param millis
     * @return DateTime
     */
    public static DateTime getDateTime(long millis) {
        return new DateTime(millis);
    }

    /**
     * 获取某日指定的整点时间的时间戳
     *
     * @param dt        基准日DateTime对象
     * @param dayOffset 相对基准日的天数偏移量，往前为负数/当日为0
     * @param hour      指定的整点时间（0-23）
     * @return long
     */
    public static long getGivenHourMillisFromNow(DateTime dt, int dayOffset, int hour) {
        DateTime newDt = dt.plusDays(dayOffset).withHourOfDay(hour).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
        return newDt.getMillis();
    }

    /**
     * 获取某日指定的整点时间的时间戳
     *
     * @param millis    基准日时间戳（毫秒）
     * @param dayOffset 相对基准日的天数偏移量，往前为负数/当日为0
     * @param hour      指定的整点时间（0-23）
     * @return long
     */
    public static long getGivenHourMillisFromNow(long millis, int dayOffset, int hour) {
        return getGivenHourMillisFromNow(getDateTime(millis), dayOffset, hour);
    }

    /**
     * 根据给定时间戳获取当日服务器刷新时间点时间戳，传入未到当日刷新时间点的时间戳也不会向前减少1天
     * 举例2017-01-01 00:00:00和2017-01-01 23:59:59返回都是2017-01-01 05:00:00对应的时间戳
     * 正常逻辑不会使用此方法,请确定你需要使用此方法
     *
     * @param millis
     * @return long
     */
    @Deprecated
    public static long getMillisAt5(long millis) {
        return getGivenHourMillisFromNow(millis, 0, DAILY_RESET_HOUR);
    }

    /**
     * 根据给定的时间戳获取下一个服务器刷新时间点时间戳
     *
     * @param millis
     * @return long
     */
    public static long getNextMillisAt5(long millis) {
        return getNextMillisAtGivenHour(millis, DAILY_RESET_HOUR);
    }

    public static long getNextMillisAtGivenHour(long millis, int hour) {
        DateTime dt = new DateTime(millis);
        if (dt.getHourOfDay() >= hour) {
            return getGivenHourMillisFromNow(dt, 1, hour);
        } else {
            return getGivenHourMillisFromNow(dt, 0, hour);
        }
    }

    /**
     * 根据给定时间戳获取当日服务器刷新时间点时间戳，传入未到当日刷新时间点的时间戳会向前减少1天
     * 举例2017-01-01 05:00:00和2017-01-02 04:59:59返回都是2017-01-01 05:00:00对应的时间戳
     *
     * @param millis
     * @return long
     */
    public static long getPreviousMillisAt5(long millis) {
        return getPreviousMillisAtGivenHour(millis, DAILY_RESET_HOUR);
    }

    public static long getPreviousMillisAtGivenHour(long millis, int hour) {
        DateTime dt = new DateTime(millis);
        if (dt.getHourOfDay() < hour) {
            return getGivenHourMillisFromNow(dt, -1, hour);
        } else {
            return getGivenHourMillisFromNow(dt, 0, hour);
        }
    }

    /**
     * 判断两个时间戳是否跨过指定的小时
     *
     * @param lastTime
     * @param newTime
     * @return boolean
     */
    public static boolean isPassGivenHour(long lastTime, long newTime, int hour) {
        if (lastTime >= newTime) {
            return false;
        }

        //加这一步主要是兼容 lastTime 为0的情况，之前如果lastTime为0，newTime是一个8点前的时间戳，则返回的有问题, 例如0和1540411200000L
        if (newTime - lastTime > TimeUtils.DAY_MILLS) {
            return true;
        }

        long lastHour = getGivenHourMillisFromNow(lastTime, 0, hour);
        long newHour = getGivenHourMillisFromNow(newTime, 0, hour);
        return (lastTime < lastHour && newTime >= lastHour) || (lastTime < newHour && newTime >= newHour);
    }

    /**
     * 判断两个时间戳是否跨过服务器刷新时间点
     *
     * @param lastTimeMillis
     * @param newTimeMillis
     * @return boolean
     */
    public static boolean dayPassAt5(long lastTimeMillis, long newTimeMillis) {
        return isPassGivenHour(lastTimeMillis, newTimeMillis, DAILY_RESET_HOUR);
    }

    /**
     * 判断两个给定的毫秒时间戳是否在同一天，以给定的时间点为界限
     *
     * @param lastTimeMillis
     * @param newTimeMillis
     * @param givenHour
     * @return boolean
     */
    public static boolean isSameDayAtGivenHour(long lastTimeMillis, long newTimeMillis, int givenHour) {
        long givenMillis = givenHour * HOUR_SECS * SEC_MILLS;
        DateTime lastDt = getDateTime(lastTimeMillis > givenMillis ? lastTimeMillis - givenMillis : 0);
        DateTime newDt = getDateTime(newTimeMillis > givenMillis ? newTimeMillis - givenMillis : 0);
        return (lastDt.toLocalDate().compareTo(newDt.toLocalDate()) == 0);
    }

    /**
     * 判断两个给定的毫秒时间戳是否在同一天，以给定的时间点为界限
     *
     * @param lastTimeMillis
     * @param newTimeMillis
     * @param givenHour
     * @param givenMinute
     * @return boolean
     */
    public static boolean isSameDayAtGivenHM(long lastTimeMillis, long newTimeMillis, int givenHour, int givenMinute) {
        long givenMillis = (long) givenHour * HOUR_MILLS + (long) givenMinute * MINUTE_MILLS;
        DateTime lastDt = getDateTime(lastTimeMillis > givenMillis ? lastTimeMillis - givenMillis : 0);
        DateTime newDt = getDateTime(newTimeMillis > givenMillis ? newTimeMillis - givenMillis : 0);
        return (lastDt.toLocalDate().compareTo(newDt.toLocalDate()) == 0);
    }

    /**
     * 判断两个时间戳是否在同一天，以0点为标准
     *
     * @param lastTimeMillis
     * @param newTimeMillis
     * @return boolean
     */
    public static boolean isSameDay(long lastTimeMillis, long newTimeMillis) {
        return isSameDayAtGivenHour(lastTimeMillis, newTimeMillis, 0);
    }

    /**
     * 判断两个时间戳是否在同一天，以5点为标准
     *
     * @param lastTimeMillis
     * @param newTimeMillis
     * @return boolean
     */
    public static boolean isSameDayAt5(long lastTimeMillis, long newTimeMillis) {
        return isSameDayAtGivenHour(lastTimeMillis, newTimeMillis, DAILY_RESET_HOUR);
    }

    /**
     * 判断两个时间是否在同一个月
     *
     * @param lastTimeMillis
     * @param newTimeMillis
     * @return
     */
    public static boolean isSameMonth(long lastTimeMillis, long newTimeMillis) {
        String l = getDateYM(lastTimeMillis);
        String n = getDateYM(newTimeMillis);
        return l.equals(n);
    }

    /**
     * 判断两个时间是否在同一个月
     *
     * @param lastTimeMillis
     * @param newTimeMillis
     * @return
     */
    public static boolean isSameMonthAt5(long lastTimeMillis, long newTimeMillis) {
        DateTime ldt = getDateTime(lastTimeMillis);
        DateTime ndt = getDateTime(newTimeMillis);
        if (ldt.getHourOfDay() < DAILY_RESET_HOUR) {
            ldt = ldt.minusDays(1);
        }
        if (ndt.getHourOfDay() < DAILY_RESET_HOUR) {
            ndt = ndt.minusDays(1);
        }

        return ldt.getYear() == ndt.getYear() && ldt.getMonthOfYear() == ndt.getMonthOfYear();
    }


    /**
     * 获得时间所属日期的星期几，以给定的时间点为界限
     *
     * @param millis
     * @param givenHour
     * @return 星期几
     */
    public static int getDayOfWeekAtGivenHour(long millis, int givenHour) {
        DateTime dt = getDateTime(millis);
        if (dt.getHourOfDay() < givenHour) {
            dt = dt.minusDays(1);
        }
        return dt.getDayOfWeek();
    }

    /**
     * 获得时间所属日期的星期几，以0点为标准
     *
     * @param millis
     * @return 星期几
     */
    public static int getDayOfWeek(long millis) {
        return getDayOfWeekAtGivenHour(millis, 0);
    }

    /**
     * 获得时间所属日期的星期几，以5点为标准
     *
     * @param millis
     * @return 星期几
     */
    public static int getDayOfWeekAt5(long millis) {
        return getDayOfWeekAtGivenHour(millis, DAILY_RESET_HOUR);
    }

    public static int getTotalDaysOfMonthAt5(long millis) {
        return getTotalDaysOfMonthAtGivenHour(millis, DAILY_RESET_HOUR);
    }

    /**
     * 获取指定时间对应月份的天数，以给定的时间点为界限
     *
     * @param millis
     * @param givenHour
     * @return
     */
    public static int getTotalDaysOfMonthAtGivenHour(long millis, int givenHour) {
        DateTime dt = getDateTime(millis);
        if (dt.getHourOfDay() < givenHour) {
            dt = dt.minusDays(1);
        }
        return dt.dayOfMonth().getMaximumValue();
    }

    public static int getMonthOfYearAt5(long millis) {
        return getMonthOfYearAtGivenHour(millis, DAILY_RESET_HOUR);
    }

    /**
     * 获取指定时间的月份，以5点为标准
     *
     * @param millis
     * @param givenHour
     * @return
     */
    public static int getMonthOfYearAtGivenHour(long millis, int givenHour) {
        DateTime dt = getDateTime(millis);
        if (dt.getHourOfDay() < givenHour) {
            dt = dt.minusDays(1);
        }
        return dt.getMonthOfYear();
    }

    /**
     * 获得时间所属日期的当月几号，以给定的时间点为界限
     *
     * @param millis
     * @param givenHour
     * @return 几号
     */
    public static int getDayOfMonthAtGivenHour(long millis, int givenHour) {
        DateTime dt = getDateTime(millis);
        if (dt.getHourOfDay() < givenHour) {
            dt = dt.minusDays(1);
        }

        return dt.getDayOfMonth();
    }

    /**
     * 获得时间所属日期的当月几号，以0点为标准
     *
     * @param millis
     * @return 几号
     */
    public static int getDayOfMonth(long millis) {
        return getDayOfMonthAtGivenHour(millis, 0);
    }

    /**
     * 获得时间所属日期的当月几号，以5点为标准
     *
     * @param millis
     * @return 几号
     */
    public static int getDayOfMonthAt5(long millis) {
        return getDayOfMonthAtGivenHour(millis, DAILY_RESET_HOUR);
    }

    /**
     * @param millis
     * @return string
     */
    public static String getDateYMD(long millis) {
        DateTime dt = getDateTime(millis);
        return dt.toLocalDate().toString(YMD_FMT);
    }

    public static String getDateYM(long millis) {
        DateTime dt = getDateTime(millis);
        return dt.toLocalDate().toString(YM_FMT);
    }

    /**
     * @param millis
     * @return string
     */
    public static String getDateYMDAt5(long millis) {
        DateTime dt = getDateTime(millis);
        if (dt.getHourOfDay() < DAILY_RESET_HOUR) {
            dt = dt.minusDays(1);
        }

        return dt.toLocalDate().toString();
    }

    /**
     * @param millis
     * @return string
     */
    public static String getDateYMDHIS(long millis) {
        DateTime dt = getDateTime(millis);
        return dt.toLocalDateTime().toString(YMDHIS_FMT);
    }

    /**
     * 获取今天指定时分秒的时间戳
     *
     * @param currMillis
     * @param his,兼容:和-分隔，如:10:00:00,18-00-00
     * @return 返回时间戳 单位毫秒
     */
    public static long getMillisOfTodayByHIS(long currMillis, String his) {
        final int arrSize = 3;
        String[] hisArr = (his.split(":").length == arrSize) ? his.split(":") : his.split("-");
        return getDateTime(currMillis)
                .withHourOfDay(Integer.parseInt(hisArr[0]))
                .withMinuteOfHour(Integer.parseInt(hisArr[1]))
                .withSecondOfMinute(Integer.parseInt(hisArr[2]))
                .withMillisOfSecond(0)
                .getMillis();
    }

    /**
     * 获取今天指定时间, 如果不到5点，返回前一天的该时刻
     *
     * @param currMillis
     * @param his,兼容:和-分隔，如:10:00:00,18-00-00
     * @return 返回时间戳 单位毫秒
     */
    public static long getMillisOfTodayByHISAt5(long currMillis, String his) {
        DateTime dateTime = getDateTime(currMillis);
        //小于5点，未到重置时间，使用昨天的日期
        if (dateTime.getHourOfDay() < DAILY_RESET_HOUR) {
            dateTime = dateTime.minusDays(1);
        }
        final int arrSize = 3;
        String[] hisArr = (his.split(":").length == arrSize) ? his.split(":") : his.split("-");
        dateTime = dateTime.withHourOfDay(Integer.parseInt(hisArr[0]));
        dateTime = dateTime.withMinuteOfHour(Integer.parseInt(hisArr[1]));
        dateTime = dateTime.withSecondOfMinute(Integer.parseInt(hisArr[2]));

        return dateTime.getMillis();
    }

    /**
     * 获得特定时间字符串的时间戳
     *
     * @param ymd_his
     * @return 毫秒数
     */
    public static long getMillisOfYMDHIS(String ymd_his) {
        DateTime dateTime = DateTime.parse(ymd_his, DateTimeFormat.forPattern(YMDHIS_FMT));
        return dateTime.getMillis();
    }


    /**
     * 判断当前时间是否在给定的时间范围内，以物理时间为标准
     *
     * @param start
     * @param end
     * @return
     */
    public static boolean isBetweenAtPhysical(String start, String end) {
        long s = getMillisOfYMDHIS(start);
        long e = getMillisOfYMDHIS(end);
        long millis = getPhysicalMillis();
        return isBetween(millis, s, e);
    }

    /**
     * 判断当前时间是否在给定的时间范围内，以业务时间为标准
     *
     * @param start
     * @param end
     * @return
     */
    public static boolean isBetweenAtBiz(String start, String end) {
        long s = getMillisOfYMDHIS(start);
        long e = getMillisOfYMDHIS(end);
        long millis = getBizMillis();
        return isBetween(millis, s, e);
    }

    public static boolean isBetweenAtBiz(long start, long end) {
        long millis = getBizMillis();
        return isBetween(millis, start, end);
    }

    public static boolean isBetweenAtHour(String start, String end) {
        long millis = getBizMillis();
        long startTime = getMillisOfTodayByHIS(millis, start);
        long endTime = getMillisOfTodayByHIS(millis, end);
        if (startTime > endTime) {
            long time = getMillisOfTodayByHIS(millis, "00:00:00");
            return isBetween(millis, time, endTime)
                    || isBetween(millis, startTime, time + DAY_MILLS);
        }
        return isBetween(millis, startTime, endTime);
    }

    /**
     * 判断当前时间是否在给定的时间范围内，以业务时间为标准
     *
     * @param start
     * @param end
     * @return
     */
    public static boolean isBetween(long millis, long start, long end) {
        return millis >= start && millis < end;
    }

    /**
     * 以当前起始点获取下一个刷新点时间戳（例：下一个周三5点的时间戳）
     *
     * @return
     */
    public static long getTimeOfNextWeekDayAtHour(int dayOfWeek, int givenHour) {
        long bizMillis = getBizMillis();
        return getTimeOfNextWeekDayAtHourWithTime(bizMillis, dayOfWeek, givenHour);
    }

    /**
     * 从传入时间点获取下一个刷新点时间戳（例：下一个周三5点的时间戳）
     *
     * @return
     */
    public static long getTimeOfNextWeekDayAtHourWithTime(long bizMillis, int dayOfWeek, int hour) {
        // 获取指定时间是星期几，以hour为基准。周日返回值为7
        int dayOfWeekAtGivenHour = getDayOfWeekAtGivenHour(bizMillis, hour);
        DateTime currentWeekDateTime = getCurrentWeekDateTimeAt5(bizMillis, dayOfWeek, hour);
        long millis = currentWeekDateTime.getMillis();
        // 过了本周刷新时间则返回下周刷新时间
        if (dayOfWeekAtGivenHour >= dayOfWeek) {
            millis += WEEK_DAYS * DAY_MILLS;
        }
        return millis;
    }

    /**
     * 获取给定时间同一周里某一天某个小时的时间
     * 例如：millis 为 2019-12-26 18:27:21 （星期四），期望获得本周三（dayOfWeek）5点（hour）的时间
     *
     * @param millis
     * @param dayOfWeek
     * @param givenHour
     * @return
     */
    public static DateTime getCurrentWeekDateTimeAt5(long millis, int dayOfWeek, int givenHour) {
        DateTime dt = getDateTime(millis);
        if (dt.getHourOfDay() < givenHour) {
            dt = dt.minusDays(1);
        }
        return dt.withDayOfWeek(dayOfWeek).withHourOfDay(givenHour).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
    }

    /**
     * 获取指定时间点后的制定月份数的重制时间点
     *
     * @param millis 时间戳
     * @return
     */
    public static long getNextMonthMillsAt5(long millis) {
        DateTime dt = new DateTime(millis);
        if (dt.getDayOfMonth() != 1 || dt.getHourOfDay() >= DAILY_RESET_HOUR) {
            dt = dt.plusMonths(1);
        }
        DateTime time = new DateTime(dt.getYear(), dt.getMonthOfYear(), 1, DAILY_RESET_HOUR, 0);
        return time.getMillis();
    }
    /**
     * 获取对应时间点的时间戳
     * 例如：08:00:00 返回 8 * 60 * 60 * 1000
     */
    public static long getClockMillis(String clock) {
        String[] hisArr = clock.split(":");
        int time = 0;
        time += Integer.parseInt(hisArr[0]) * HOUR_MILLS;
        time += Integer.parseInt(hisArr[1]) * MINUTE_MILLS;
        time += Integer.parseInt(hisArr[2]) * SEC_MILLS;
        return time;
    }
}
