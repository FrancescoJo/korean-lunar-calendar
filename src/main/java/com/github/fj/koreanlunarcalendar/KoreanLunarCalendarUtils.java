/*
 * Copyright 2019 Francesco Jo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.fj.koreanlunarcalendar;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * A date conversion utility between Gregorian calendar date to Korean lunisolar calendar date.
 * Accepted date range is described below:
 * <pre>
 * |      |      Gregorian      |      Lunisolar      |
 * |------|---------------------|---------------------|
 * | From |   1st - Feb - 1900  |   1st - Jan - 1900  |
 * |  To  |  31st - Dec - 2049  |  30rd - Dec - 2049  |
 * </pre>
 * Although many parts of Korean lunisolar calendar is compatible to Chinese Shixian calendar, length of month data
 * is slightly different. Moreover, this subtle difference does not accumulates itself in short date ranges - which
 * makes detecting error more difficult. Therefore, this implementation uses calendar data officially presented by
 * <a href="https://astro.kasi.re.kr">Korea Astronomy and Space Science Institute<a/>.
 * <p>
 * This class is designed as standalone utility class rather than inheriting {@link java.util.Calendar} as many other
 * cultural calendar implementations do. The main advantage of other implementations doing so is compatibility.
 * Regarding such advantage, however, there are many drawbacks as follows:
 * <ol>
 *     <li>You have to specify a EXACT locale for obtaining a culture-specific calendar instance via
 *     {@link java.util.Calendar#getInstance(java.util.Locale)} which approach is prone to error.
 *     </li>
 *     <li>This class itself, should register custom Korean locale calendar through
 *     {@link java.util.spi.LocaleServiceProvider} in order to blend into legacy Calendar API, which requires
 *     a lot of effort for error prone solution(described in #1).</li>
 *     <li>You must check thread safety manipulating date fields of instance, since {@link java.util.Calendar}
 *     is not thread safe.</li>
 * </ol>
 * <p>
 * Since this implementation relies only on Java legacy Calendar API, it can be used under Java 1.6+ or any Android
 * versions.
 *
 * @author Francesco Jo(nimbusob@gmail.com)
 * @version 1.0
 * @since 08 - Jan - 2019
 */
public final class KoreanLunarCalendarUtils {
    private static final int BASE_LUNAR_YEAR = 1900;
    private static final int END_LUNAR_YEAR = 2049;
    private static final int BASE_SOLAR_YEAR = 1900;
    private static final int END_SOLAR_YEAR = 2049;

    private static final int SHORT_LUNAR_MONTH_DAYS = 29;
    private static final int LONG_LUNAR_MONTH_DAYS = 30;

    private static final int SOLAR_BASE_JULIAN_DAYS = 2415021;
    private static final int LUNAR_BASE_JULIAN_DAYS = 2415051;
    private static final int CVT_JULIAN_DAYS_OFFSET = LUNAR_BASE_JULIAN_DAYS - SOLAR_BASE_JULIAN_DAYS;
    /**
     * Gregorian Calendar adoption date Oct. 15, 1582 (jd. 2299161)
     */
    private static final int JULIAN_ADOPTION_OFFSET = 15 + 31 * (10 + 12 * 1582);

    private static final int LUNAR_SEXAGENARY_CYCLE_YEAR_BASE = 36;
    private static final int LUNAR_SEXAGENARY_CYCLE_MONTH_BASE = 14;
    private static final int LUNAR_SEXAGENARY_CYCLE_DAY_BASE = 10;
    private static final int[] SOLAR_BASE_JULIAN_WEEKDAYS = new int[]{
            Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY,
            Calendar.SATURDAY, Calendar.SUNDAY
    };
    private static final int[] CALENDAR_MONTHS = new int[]{
            Calendar.JANUARY, Calendar.FEBRUARY, Calendar.MARCH, Calendar.APRIL,
            Calendar.MAY, Calendar.JUNE, Calendar.JULY, Calendar.AUGUST,
            Calendar.SEPTEMBER, Calendar.OCTOBER, Calendar.NOVEMBER, Calendar.DECEMBER
    };

    /**
     * A table of days of month per lunar years, since 1900 to 2049 in 16 bits.
     * In terms of lunar month, large month has 30 days length and small month has 29 days. This information requires
     * at least a single bit - thus this implementation regards 1(positive) as large and 0(negative) as small month.
     * <p>
     * Any nonzero value from top 4 bits represents leap month and zero means no leap month in that year.
     * Unfortunately, there is no rule of days length in every leap months, thus we need extra space to hold
     * information of the days length of given leap month. However, such data was not easy to to pack into this
     * bit chains, since it will require at least 17 bits per year, which will cause bitwise operations more difficult.
     * Therefore, All large leap months of year information is stored in another space.
     * See {@link KoreanLunarCalendarUtils#LONG_LEAP_MONTH_YEARS} for details.
     * <p>
     * Lower 12 bits represents length of month in that year.
     */
    private static final int[] NDAYS = new int[]{
            0x84bd04ae, 0x0a57554d, 0x0d260d95, 0x4655056a, 0x09ad255d,     // 1900
            0x04ae6a5b, 0x0a4d0d25, 0x5da90b55, 0x056a2ada, 0x095d74bb,     // 1910
            0x049b0a4b, 0x5b4b06a9, 0x0ad44bb5, 0x02b6095b, 0x25370497,     // 1920
            0x66560e4a, 0x0ea556a9, 0x05b502b6, 0x38ae092e, 0x7c8d0c95,     // 1930
            0x0d4a6d8a, 0x0b69056d, 0x425b025d, 0x092d2d2b, 0x0a957d55,     // 1940
            0x0b4a0b55, 0x555504db, 0x025b3857, 0x052b8a9b, 0x069506aa,     // 1950
            0x6aea0ab5, 0x04b64aae, 0x0a570527, 0x37260d95, 0x76b5056a,     // 1960
            0x09ad54dd, 0x04ae0a4e, 0x4d4d0d25, 0x8d590b54, 0x0d6a695a,     // 1970
            0x095b049b, 0x4a9b0a4b, 0xab2706a5, 0x06d46b75, 0x02b6095b,     // 1980
            0x54b70497, 0x064b374a, 0x0ea586d9, 0x05ad02b6, 0x596e092e,     // 1990
            0x0c964e95, 0x0d4a0da5, 0x2755056c, 0x7abb025d, 0x092d5cab,     // 2000
            0x0a950b4a, 0x3b4a0b55, 0x955d04ba, 0x0a5b5557, 0x052b0a95,     // 2010
            0x4b9506aa, 0x0ad526b5, 0x04b66a6e, 0x0a570527, 0x56a60d93,     // 2020
            0x05aa3b6a, 0x096db4af, 0x04ae0a4d, 0x6d0d0d25, 0x0d525dd4,     // 2030
            0x0b6a096d, 0x255b049b, 0x7a570a4b, 0x0b255b25, 0x06d40ada,     // 2040
    };

    /**
     * A table of julian days of every lunar year since 01 - Jan - 1900(jd. 2415051).
     * <p>
     * This table plays an important rule to reduce calculations of lunar date to solar date conversion.
     * Without this table, we have to linear search {@link KoreanLunarCalendarUtils#NDAYS} for every calculations.
     * Such algorithm takes O(N) time complexity, and it would be N=1800 in worst case.
     * <p>
     * Introducing this table however, we can jump between lunar years by binary search. Such implementation takes
     * O(logN₁) + O(N₂) time complexity, and it would be N₁ = 150, N₂ = 12 in worst case. Even in a such bad scenario,
     * calculation could be reduced by 99% thanks for this small amount(600 bytes) of extra data.
     */
    // @formatter:off
    private static final int[] YEAR_JULIAN_DAYS = new int[]{
                0,   384,   738,  1093,  1476,  1830,  2185,  2569,  2923,  3278,   // 1900
             3662,  4016,  4400,  4754,  5108,  5492,  5847,  6201,  6585,  6940,   // 1910
             7324,  7678,  8032,  8416,  8770,  9124,  9509,  9863, 10218, 10602,   // 1920
            10956, 11339, 11693, 12048, 12432, 12787, 13141, 13525, 13879, 14263,   // 1930
            14617, 14971, 15355, 15710, 16065, 16449, 16803, 17157, 17541, 17895,   // 1940
            18279, 18633, 18988, 19372, 19727, 20081, 20465, 20819, 21203, 21557,   // 1950
            21911, 22295, 22650, 23004, 23388, 23743, 24097, 24480, 24835, 25219,   // 1960
            25573, 25928, 26312, 26666, 27020, 27404, 27758, 28142, 28496, 28851,   // 1970
            29235, 29590, 29944, 30328, 30682, 31066, 31420, 31774, 32159, 32513,   // 1980
            32868, 33252, 33606, 33960, 34343, 34698, 35082, 35437, 35791, 36175,   // 1990
            36529, 36883, 37267, 37621, 37976, 38360, 38714, 39099, 39453, 39807,   // 2000
            40191, 40545, 40899, 41283, 41638, 42022, 42376, 42731, 43115, 43469,   // 2010
            43823, 44207, 44561, 44916, 45300, 45654, 46038, 46393, 46747, 47130,   // 2020
            47485, 47839, 48223, 48578, 48962, 49316, 49670, 50054, 50408, 50762,   // 2030
            51146, 51501, 51856, 52240, 52594, 52978, 53332, 53686, 54070, 54424,   // 2040
    };
    // @formatter:on

    /**
     * A table of years holding large leap months since lunar year 1900.
     */
    private static final short[] LONG_LEAP_MONTH_YEARS = new short[]{
            6, 33, 36, 38, 41, 44, 52, 55, 79, 112, 136, 147
    };

    private static final char[] HEAVENLY_STEMS_CHINESE = new char[]{
            '甲', '乙', '丙', '丁', '戊', '己', '庚', '辛', '壬', '癸'
    };
    private static final char[] HEAVENLY_STEMS_KOREAN = new char[]{
            '갑', '을', '병', '정', '무', '기', '경', '신', '임', '계'
    };

    private static final char[] EARTHLY_BRANCHES_CHINESE = new char[]{
            '子', '丑', '寅', '卯', '辰', '巳', '午', '未', '申', '酉', '戌', '亥'
    };
    private static final char[] EARTHLY_BRANCHES_KOREAN = new char[]{
            '자', '축', '인', '묘', '진', '사', '오', '미', '신', '유', '술', '해'
    };

    /**
     * @param cycle cycle number derived by {@link KoreanLunarCalendarUtils}, must be between 1 and 60.
     * @return A Chinese <a href="https://en.wikipedia.org/wiki/Heavenly_Stems">heavenly stem</a> character,
     * called Cheon'gan(천간) in Korean.
     */
    public static char getHeavenlyStemChinese(final int cycle) {
        if (cycle == 0) {
            return 0;
        }

        return HEAVENLY_STEMS_CHINESE[getStemNumber(cycle)];
    }

    /**
     * @param cycle cycle number derived by {@link KoreanLunarCalendarUtils}, must be between 1 and 60.
     * @return A Korean <a href="https://en.wikipedia.org/wiki/Heavenly_Stems">heavenly stem</a> character,
     * called Cheon'gan(천간) in Korean.
     */
    public static char getHeavenlyStemKorean(final int cycle) {
        if (cycle == 0) {
            return 0;
        }

        return HEAVENLY_STEMS_KOREAN[getStemNumber(cycle)];
    }

    /**
     * @param cycle cycle number derived by {@link KoreanLunarCalendarUtils}, must be between 1 and 60.
     * @return A Chinese <a href="https://en.wikipedia.org/wiki/Earthly_Branches">earthly branch</a> character,
     * called Jiji(지지) in Korean.
     */
    public static char getEarthlyBranchChinese(final int cycle) {
        if (cycle == 0) {
            return 0;
        }

        return EARTHLY_BRANCHES_CHINESE[getBranchNumber(cycle)];
    }

    /**
     * @param cycle cycle number derived by {@link KoreanLunarCalendarUtils}, must be between 1 and 60.
     * @return A Korean <a href="https://en.wikipedia.org/wiki/Earthly_Branches">earthly branch</a> character,
     * called Jiji(지지) in Korean.
     */
    public static char getEarthlyBranchKorean(final int cycle) {
        if (cycle == 0) {
            return 0;
        }

        return EARTHLY_BRANCHES_KOREAN[getBranchNumber(cycle)];
    }

    /**
     * @param cycle cycle number derived by {@link KoreanLunarCalendarUtils}, must be between 1 and 60.
     * @return A Chinese representation of <a href="https://en.wikipedia.org/wiki/Earthly_Branches">Sexagenary cycle</a>,
     * called Yooksipgapza(60 갑자) in Korean.
     */
    public static String getSexagenaryCycleChinese(final int cycle) {
        if (cycle == 0) {
            return "";
        }

        return Character.toString(getHeavenlyStemChinese(cycle)) + getEarthlyBranchChinese(cycle);
    }

    /**
     * @param cycle cycle number derived by {@link KoreanLunarCalendarUtils}, must be between 1 and 60.
     * @return A Korean representation of <a href="https://en.wikipedia.org/wiki/Earthly_Branches">Sexagenary cycle</a>,
     * called Yooksipgapza(60 갑자) in Korean.
     */
    public static String getSexagenaryCycleKorean(final int cycle) {
        if (cycle == 0) {
            return "";
        }

        return Character.toString(getHeavenlyStemKorean(cycle)) + getEarthlyBranchKorean(cycle);
    }

    private static int getStemNumber(final int cycle) {
        return (cycle - 1) % 10;
    }

    private static int getBranchNumber(final int cycle) {
        return (cycle - 1) % 12;
    }

    /**
     * Converts given {@link KoreanLunarDate} to standard {@link Date} object.
     *
     * @return converted {@link Date} object that holds gregorian date of given {@link KoreanLunarDate}.
     */
    @SuppressWarnings("MagicConstant")
    public Date toGregorianDate(final KoreanLunarDate lunarDate) {
        final Calendar cal = GregorianCalendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"));
        cal.clear();
        cal.set(lunarDate.solYear, CALENDAR_MONTHS[lunarDate.solMonth - 1], lunarDate.solDay);

        return cal.getTime();
    }

    /**
     * Calculates lunar date of given solar date. The date must be between 01 - Feb - 1900 and 31 - Dec - 2049.
     *
     * @return Lunar date of given solar date.
     * @throws IllegalArgumentException if every date component(s) is/are out of range.
     */
    public static KoreanLunarDate getLunarDateOf(final int solarYear, final int solarMonth, final int solarDay) {
        assertSolarDateInBounds(solarYear, solarMonth, solarDay);

        int daysLeft = getNJulianDaysSinceBase(solarYear, solarMonth, solarDay);
        final int julianDays = daysLeft + SOLAR_BASE_JULIAN_DAYS;
        daysLeft -= CVT_JULIAN_DAYS_OFFSET;

        final int index = Arrays.binarySearch(YEAR_JULIAN_DAYS, daysLeft);
        final int lunYear;
        if (index >= 0) {
            lunYear = BASE_LUNAR_YEAR + index;
        } else {
            final int insertionPoint = (index + 1) * -1;
            lunYear = BASE_LUNAR_YEAR + (insertionPoint - 1);
        }
        daysLeft -= YEAR_JULIAN_DAYS[lunYear - BASE_LUNAR_YEAR];

        final int leapMonth = getLeapMonthOf(lunYear, false);
        int lunMonth = 1;
        // Every lunar January are not leap months.
        int daysOfMonth = getDaysOfLunarMonth(lunYear, lunMonth, false, false);
        boolean isLeapMonthProcessed = false;
        boolean isLeapMonthFound = false;

        while (daysLeft >= daysOfMonth) {
            if (lunMonth == leapMonth) {
                if (isLeapMonthFound) {
                    isLeapMonthFound = false;
                } else {
                    isLeapMonthProcessed = true;
                    isLeapMonthFound = true;
                }
            }

            if (!isLeapMonthFound) {
                lunMonth++;
            }

            daysLeft -= daysOfMonth;
            daysOfMonth = getDaysOfLunarMonth(lunYear, lunMonth, isLeapMonthFound, false);
        }

        final int lunDay = daysLeft + 1;
        final boolean isLeapMonth = isLeapMonthProcessed && lunMonth == leapMonth;

        return buildDate(solarYear, solarMonth, solarDay, julianDays, lunYear, lunMonth, lunDay, isLeapMonth);
    }

    /**
     * Calculates solar date of given lunar date. The date must be between 01 - Jan - 1900 and 30 - Dec - 2049.
     * This method cannot accepts if given date holds leap month. Therefore, lunar date of
     * {@link KoreanLunarDate#monthlyCycle} returned by this method must be non-zero value.
     * <p>
     * <em>NOTE:</em> Callers must know exact month length of every lunar dates in order to avoid random
     * {@link IllegalArgumentException}. To do so, it is strongly advised to check maximum lunar day of given date
     * by {@link KoreanLunarCalendarUtils#getDaysOfLunarMonth(int, int)} or
     * {@link KoreanLunarCalendarUtils#getDaysOfLunarMonth(int, int, boolean)} before invoking this method,
     * especially <code>lunarDay</code> is on the edge of every months, usually 29 or 30.
     *
     * @return Solar date of given lunar date.
     * @throws IllegalArgumentException if every date component(s) is/are out of range.
     */
    public static KoreanLunarDate getSolarDateOf(final int lunarYear, final int lunarMonth, final int lunarDay) {
        return getSolarDateOf(lunarYear, lunarMonth, lunarDay, false);
    }

    /**
     * Calculates solar date of given lunar date. The date must be between 01 - Jan - 1900 and 30 - Dec - 2049.
     * Use {@link KoreanLunarCalendarUtils#getLeapMonthOf(int)} to determine given date contains a lunar month.
     * <p>
     * <em>NOTE:</em> Callers must know exact month length of every lunar dates in order to avoid random
     * {@link IllegalArgumentException}. To do so, it is strongly advised to check maximum lunar day of given date
     * by {@link KoreanLunarCalendarUtils#getDaysOfLunarMonth(int, int)} or
     * {@link KoreanLunarCalendarUtils#getDaysOfLunarMonth(int, int, boolean)} before invoking this method,
     * especially <code>lunarDay</code> is on the edge of every months, usually 29 or 30.
     * <p>
     * <em>NOTE:</em> <code>isLeapMonth</code> is ignored if given date would never been to leap month. For example,
     * Lunar year 2000 does not holds any leap months, therefore <code>getSolarDateOf(2000, 1, 1, true)</code> will
     * yield same result as <code>getSolarDateOf(2000, 1, 1, false)</code>. However, in case of date 01 - Apr - 2001
     * which may be a leap month, the difference between <code>getSolarDateOf(2001, 4, 1, true)</code> and
     * <code>getSolarDateOf(2001, 4, 1, false)</code> is significant.
     *
     * @return Solar date of given lunar date.
     * @throws IllegalArgumentException if every date component(s) is/are out of range.
     */
    /* Time complexity: O(N), N = (Best 1, worst 13) */
    public static KoreanLunarDate getSolarDateOf(final int lunarYear, final int lunarMonth, final int lunarDay,
            final boolean isLeapMonth) {
        final int leapMonth = getLeapMonthOf(lunarYear, false);
        final boolean checkedLeapMonth = (leapMonth == lunarMonth) && isLeapMonth;
        assertLunarYearInBounds(lunarYear, lunarMonth, lunarDay, checkedLeapMonth);

        int baseJulianDay = YEAR_JULIAN_DAYS[lunarYear - BASE_LUNAR_YEAR];
        int currentLunMonth = 1;

        while (currentLunMonth < lunarMonth) {
            int days = getDaysOfLunarMonth(lunarYear, currentLunMonth, false, false);
            baseJulianDay += days;
            currentLunMonth++;
        }

        /*
         * Compensate current month if client code required a solar day for leap month.
         * Due to while loop condition `lunarMonth` days are not accumulated and,
         * every leap months appears after 'normal' months, in general rules of Shixian calendar.
         */
        if (checkedLeapMonth && currentLunMonth == leapMonth) {
            int days = getDaysOfLunarMonth(lunarYear, leapMonth, false, false);
            baseJulianDay += days;
        }

        /*
         * Compensate leap month if `lunarMonth` is larger than leap month of `lunarYear`,
         * since leap month days are never accumulated in while loop.
         *
         * This is a intended design for easy debuggability.
         */
        if (leapMonth != 0 && currentLunMonth > leapMonth) {
            int days = getDaysOfLunarMonth(lunarYear, leapMonth, true, false);
            baseJulianDay += days;
        }

        baseJulianDay += (lunarDay - 1);

        /*
         * Returns the Julian day number that begins at noon of this day.
         *
         * Reference:
         *   Numerical Recipes in C, 2nd ed., Cambridge University Press 1992
         */
        final int julianDays = baseJulianDay + LUNAR_BASE_JULIAN_DAYS;
        int jAlpha, ja, jb, jc, jd, je, solYear, solMonth, solDay;
        ja = julianDays;
        if (ja >= JULIAN_ADOPTION_OFFSET) {
            jAlpha = (int) (((ja - 1867216) - 0.25) / 36524.25);
            ja = ja + 1 + jAlpha - jAlpha / 4;
        }

        jb = ja + 1524;
        jc = (int) (6680.0 + ((jb - 2439870) - 122.1) / 365.25);
        jd = 365 * jc + jc / 4;
        je = (int) ((jb - jd) / 30.6001);

        solDay = jb - jd - (int) (30.6001 * je);

        solMonth = je - 1;
        if (solMonth > 12) {
            solMonth = solMonth - 12;
        }

        solYear = jc - 4715;
        if (solMonth > 2) {
            solYear--;
        }

        return buildDate(solYear, solMonth, solDay, julianDays, lunarYear, lunarMonth, lunarDay, checkedLeapMonth);
    }

    /**
     * Calculates any leap months between lunar year 1900 and 2049.
     *
     * @return Leap month of given lunar year if found, 0 otherwise.
     */
    public static int getLeapMonthOf(final int lunarYear) {
        return getLeapMonthOf(lunarYear, true);
    }

    /* Time complexity: O(1) */
    private static int getLeapMonthOf(final int lunYr, final boolean checkArgs) {
        if (checkArgs) {
            assertLunarYearInBounds(lunYr);
        }

        final int data = getNDaysData(lunYr);
        final int leapMonthBitmask = 0xF000;
        return (data & leapMonthBitmask) >>> 12;
    }

    /**
     * Calculates maximum days of given lunar date between Jan - 1900 and Dec - 2049.
     * When given date is leap month, use {@link KoreanLunarCalendarUtils#getDaysOfLunarMonth(int, int, boolean)}
     * instead.
     *
     * @return maximum days of given lunar date. Must be 29 or 30.
     */
    public static int getDaysOfLunarMonth(final int lunarYear, final int lunarMonth) {
        return getDaysOfLunarMonth(lunarYear, lunarMonth, false);
    }

    /**
     * Calculates maximum days of given lunar date between Jan - 1900 and Dec - 2049.
     * <p>
     * <em>NOTE:</em> <code>isLeapMonth</code> is ignored if given date would never been to leap month.
     *
     * @return maximum days of given lunar date. Must be 29 or 30.
     */
    public static int getDaysOfLunarMonth(final int lunarYear, final int lunarMonth, final boolean isLeapMonth) {
        return getDaysOfLunarMonth(lunarYear, lunarMonth, isLeapMonth, true);
    }

    /* Time complexity: O(LogN), N = 12 */
    private static int getDaysOfLunarMonth(final int lunYr, final int lunMo, final boolean isLeapMonth,
            final boolean checkArgs) {
        final int leapMonth = getLeapMonthOf(lunYr, false);
        final boolean checkedLeapMonth = (leapMonth == lunMo) && isLeapMonth;

        if (checkArgs) {
            assertLunarYearInBounds(lunYr, lunMo, 1, checkedLeapMonth);
        }

        if (isLeapMonth) {
            final short offsetReducedYear = (short) (lunYr - BASE_LUNAR_YEAR);
            final int idx = Arrays.binarySearch(LONG_LEAP_MONTH_YEARS, offsetReducedYear);

            if (idx < 0) {
                return SHORT_LUNAR_MONTH_DAYS;
            } else {
                return LONG_LUNAR_MONTH_DAYS;
            }
        } else {
            final int data = getNDaysData(lunYr) & 0x0FFF;
            final int bitMask = 0x0800 >>> (lunMo - 1);
            final int bitResult = data & bitMask;
            final int daysBit = bitResult >>> (12 - lunMo);

            if (daysBit == 0) {
                return SHORT_LUNAR_MONTH_DAYS;
            } else {
                return LONG_LUNAR_MONTH_DAYS;
            }
        }
    }

    /* Time complexity: O(1) */
    private static int getNDaysData(final int lunYr) {
        final int delta = lunYr - BASE_LUNAR_YEAR;
        final int index = delta / 2;
        final int shifts = (Math.abs(1 - (delta % 2)) * 16);
        final int bitmask = 0x0000FFFF << shifts;

        // Logical shr to ignore MSB fill (data is unsigned)
        return (NDAYS[index] & bitmask) >>> shifts;
    }

    private static int getSexagenaryCycleOfYear(final int lunYr) {
        return 1 + ((lunYr - BASE_LUNAR_YEAR) + LUNAR_SEXAGENARY_CYCLE_YEAR_BASE) % 60;
    }

    private static int getSexagenaryCycleOfMonth(final int lunYr, final int lunMo) {
        final int months = (lunYr - BASE_LUNAR_YEAR) * 12 + (lunMo - 1);
        return 1 + (months + LUNAR_SEXAGENARY_CYCLE_MONTH_BASE) % 60;
    }

    private static int getSexagenaryCycleOfDay(final int lunJulD) {
        return 1 + ((lunJulD - LUNAR_BASE_JULIAN_DAYS) + LUNAR_SEXAGENARY_CYCLE_DAY_BASE) % 60;
    }

    /* Time complexity: O(N), N = (best 1, worst 12) */
    private static int getNJulianDaysSinceBase(final int solYr, final int solMo, final int solD) {
        int nDaysAcc = 0;
        final int yearDelta = solYr - BASE_SOLAR_YEAR;
        final int leapYears = (yearDelta - 1) / 4;
        final int plainYears = yearDelta - leapYears;
        nDaysAcc += (plainYears * 365) + (leapYears * 366);

        for (int i = 1; i < solMo; i++) {
            nDaysAcc += getSolarNDaysOf(solYr, i);
        }
        nDaysAcc += (solD - 1);

        return nDaysAcc;
    }

    private static int getDayOfWeekOf(final int solJulD) {
        return SOLAR_BASE_JULIAN_WEEKDAYS[solJulD % 7];
    }

    /* Time complexity: O(1) */
    private static int getSolarNDaysOf(final int solYr, final int solMo) {
        switch (solMo) {
            case 1:
            case 3:
            case 5:
            case 7:
            case 8:
            case 10:
            case 12:
                return 31;
            case 4:
            case 6:
            case 9:
            case 11:
                return 30;
            case 2:
                if (isSolarLeapYear(solYr)) {
                    return 29;
                } else {
                    return 28;
                }
            default:
                throw new IllegalArgumentException("Solar month must be bound between 1 and 12");
        }
    }

    /* Time complexity: O(1) */
    private static boolean isSolarLeapYear(final int year) {
        return year % 400 == 0 || (year % 4 == 0 && year % 100 != 0);
    }

    private static KoreanLunarDate buildDate(final int solYear, final int solMonth, final int solDay,
            final int julianDays, final int lunYear, final int lunMonth, final int lunDay,
            final boolean isLeapMonth
    ) {
        final int monthlyCycle;
        if (isLeapMonth) {
            monthlyCycle = 0;
        } else {
            monthlyCycle = getSexagenaryCycleOfMonth(lunYear, lunMonth);
        }

        return new KoreanLunarDate.Builder()
                .solYear(solYear)
                .solMonth(solMonth)
                .solDay(solDay)
                .solDayOfWeek(getDayOfWeekOf(julianDays))
                .solLeapYear(isSolarLeapYear(solYear))
                .julianDays(julianDays)
                .lunYear(lunYear)
                .lunMonth(lunMonth)
                .lunDay(lunDay)
                .lunLeapMonth(isLeapMonth)
                .lunDaysOfMonth(getDaysOfLunarMonth(lunYear, lunMonth, isLeapMonth, false))
                .dailyCycle(getSexagenaryCycleOfDay(julianDays + CVT_JULIAN_DAYS_OFFSET))
                .monthlyCycle(monthlyCycle)
                .yearlyCycle(getSexagenaryCycleOfYear(lunYear))
                .build();
    }

    private static void assertSolarDateInBounds(final int solYr, final int solMo, final int solD) {
        if (solYr < BASE_SOLAR_YEAR || solYr > END_SOLAR_YEAR) {
            throw new IllegalArgumentException("Solar year must be bound between " + BASE_SOLAR_YEAR +
                    " to " + END_SOLAR_YEAR);
        } else if (solYr == BASE_SOLAR_YEAR && solMo == 1 && solD < 31) {
            throw new IllegalArgumentException("This calendar only supports solar date since 1900-02-01.");
        }

        if (solMo < 1 || solMo > 12) {
            throw new IllegalArgumentException("Month must be bound between 1 and 12.");
        }

        final int maxSolarDays = getSolarNDaysOf(solYr, solMo);
        if (solD < 1 || solD > maxSolarDays) {
            throw new IllegalArgumentException("Day must be bound between 1 and " + maxSolarDays +
                    " for date " + solYr + "-" + padZeros(solMo, 2));
        }
    }

    private static void assertLunarYearInBounds(final int lunYr) {
        assertLunarYearInBounds(lunYr, 1, 1, false);
    }

    private static void assertLunarYearInBounds(final int lunYr, final int lunMo, final int lunD,
            final boolean isLeapMonth) {
        if (lunYr < BASE_LUNAR_YEAR || lunYr > END_LUNAR_YEAR) {
            throw new IllegalArgumentException("Lunar year " + lunYr + " is not in bounds(" + BASE_LUNAR_YEAR +
                    " - " + END_LUNAR_YEAR + ")");
        }

        if (lunMo < 1 || lunMo > 12) {
            throw new IllegalArgumentException("Month must be bound between 1 and 12.");
        }

        final int maxLunarDays = getDaysOfLunarMonth(lunYr, lunMo, isLeapMonth, false);
        if (lunD < 1 || lunD > maxLunarDays) {
            throw new IllegalArgumentException("Day must be bound between 1 and " + maxLunarDays +
                    " for date " + lunYr + "-" + padZeros(lunMo, 2));
        }
    }

    private static String padZeros(final int number, final int padSize) {
        return String.format("%1$" + padSize + "s", number).replace(' ', '0');
    }
}
