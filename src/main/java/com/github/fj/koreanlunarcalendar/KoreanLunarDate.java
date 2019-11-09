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

import java.util.Objects;

import static com.github.fj.koreanlunarcalendar.KoreanLunarCalendarUtils.getSexagenaryCycleChinese;
import static com.github.fj.koreanlunarcalendar.KoreanLunarCalendarUtils.getSexagenaryCycleKorean;

/**
 * An immutable data class that represents a single Korean lunar date corresponds to Gregorian date.
 *
 * @author Francesco Jo(nimbusob@gmail.com)
 * @version 1.0
 * @since 08 - Jan - 2019
 */
public final class KoreanLunarDate {
    /**
     * Value of solar year.
     */
    public final int solYear;
    /**
     * Value of solar month. Note that this value starts from 1, not 0, unlike the notorious
     * {@link java.util.Calendar}, as Java calendar implementation is so confusing.
     * <p>
     * Once again, this field is NOT COMPATIBLE WITH month fields in {@link java.util.Calendar}.
     */
    public final int solMonth;
    /**
     * Value of solar day.
     */
    public final int solDay;
    /**
     * A field holds day of week(DoW) value. This value is compatible with date fields in
     * {@link java.util.Calendar}. For example, value <code>1</code> represents
     * {@link java.util.Calendar#MONDAY}.
     */
    public final int solDayOfWeek;
    /**
     * Value of solar leap year.
     */
    public final boolean isSolLeapYear;
    /**
     * <a href="https://en.wikipedia.org/wiki/Julian_day">Julian day</a>, since 1st - Jan - 4713 BC.
     */
    public final int julianDays;

    /**
     * Value of lunar year.
     */
    public final int lunYear;
    /**
     * Value of lunar month.
     */
    public final int lunMonth;
    /**
     * Value of lunar day.
     */
    public final int lunDay;
    /**
     * Value of lunar leap month. In Korea, lunar leap month is regarded as 'a blank month',
     * therefore {@link KoreanLunarDate#monthlyCycle} must be 0 if this value is <code>true</code>.
     */
    public final boolean isLunLeapMonth;
    /**
     * Value of length(days) of given lunar month. This information is very important for
     * some applications, for example, drawing a calendar.
     */
    public final int lunDaysOfMonth;
    /**
     * Sexagenary cycle number of lunar day. Value ranged between 1 and 60. To convert to
     * Sexagenary cycle characters, use
     * {@link KoreanLunarCalendarUtils#getSexagenaryCycleChinese(int)} or similar method
     * ends with "Korean".
     */
    public final int dailyCycle;
    /**
     * Sexagenary cycle number of month. Value ranged between 0 and 60. To convert to
     * Sexagenary cycle characters, use
     * {@link KoreanLunarCalendarUtils#getSexagenaryCycleChinese(int)} or similar method
     * ends with "Korean".
     * <p>
     * In Korea, lunar leap month is regarded as 'a blank month', therefore this value must be
     * 0 if {@link KoreanLunarDate#isLunLeapMonth} is <code>true</code>.
     */
    public final int monthlyCycle;
    /**
     * Sexagenary cycle number of lunar day. Value ranged between 1 and 60. To convert to
     * Sexagenary cycle characters, use
     * {@link KoreanLunarCalendarUtils#getSexagenaryCycleChinese(int)} or similar method
     * ends with "Korean".
     */
    public final int yearlyCycle;

    private final String dailyCycleDesc;
    private final String monthlyCycleDesc;
    private final String yearlyCycleDesc;

    private KoreanLunarDate(final int solYear, final int solMonth, final int solDay,
                            final int solDayOfWeek, final boolean isSolLeapYear, final int julianDays,
                            final int lunYear, final int lunMonth, final int lunDay,
                            final boolean isLunLeapMonth, final int lunDaysOfMonth,
                            final int dailyCycle, final int monthlyCycle, final int yearlyCycle) {
        this.solYear = solYear;
        this.solMonth = solMonth;
        this.solDay = solDay;
        this.solDayOfWeek = solDayOfWeek;
        this.isSolLeapYear = isSolLeapYear;
        this.julianDays = julianDays;
        this.lunYear = lunYear;
        this.lunMonth = lunMonth;
        this.lunDay = lunDay;
        this.isLunLeapMonth = isLunLeapMonth;
        this.lunDaysOfMonth = lunDaysOfMonth;
        this.dailyCycle = dailyCycle;
        this.monthlyCycle = monthlyCycle;
        this.yearlyCycle = yearlyCycle;

        this.dailyCycleDesc = getSexagenaryCycleChinese(dailyCycle) + ", " +
                getSexagenaryCycleKorean(dailyCycle);
        this.monthlyCycleDesc = getSexagenaryCycleChinese(monthlyCycle) + ", " +
                getSexagenaryCycleKorean(monthlyCycle);
        this.yearlyCycleDesc = getSexagenaryCycleChinese(yearlyCycle) + ", " +
                getSexagenaryCycleKorean(yearlyCycle);
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof KoreanLunarDate)) {
            return false;
        }

        KoreanLunarDate rhs = (KoreanLunarDate) o;
        return solYear == rhs.solYear &&
                solMonth == rhs.solMonth &&
                solDay == rhs.solDay &&
                solDayOfWeek == rhs.solDayOfWeek &&
                isSolLeapYear == rhs.isSolLeapYear &&
                julianDays == rhs.julianDays &&
                lunYear == rhs.lunYear &&
                lunMonth == rhs.lunMonth &&
                lunDay == rhs.lunDay &&
                isLunLeapMonth == rhs.isLunLeapMonth &&
                lunDaysOfMonth == rhs.lunDaysOfMonth &&
                dailyCycle == rhs.dailyCycle &&
                monthlyCycle == rhs.monthlyCycle &&
                yearlyCycle == rhs.yearlyCycle;
    }

    @Override
    public int hashCode() {
        return Objects.hash(solYear, solMonth, solDay, solDayOfWeek, isSolLeapYear, julianDays,
                lunYear, lunMonth, lunDay, isLunLeapMonth, lunDaysOfMonth, dailyCycle, monthlyCycle, yearlyCycle);
    }

    @Override
    public String toString() {
        return "KoreanLunarDate{" +
                "solYear=" + solYear +
                ", solMonth=" + solMonth +
                ", solDay=" + solDay +
                ", solDayOfWeek=" + solDayOfWeek +
                ", solLeapYear=" + isSolLeapYear +
                ", julianDays=" + julianDays +
                ", lunYear=" + lunYear +
                ", lunMonth=" + lunMonth +
                ", lunDay=" + lunDay +
                ", lunLeapMonth=" + isLunLeapMonth +
                ", lunDaysOfMonth=" + lunDaysOfMonth +
                ", dailyCycle=" + dailyCycle + " (" + dailyCycleDesc + ")" +
                ", monthlyCycle=" + monthlyCycle + " (" + monthlyCycleDesc + ")" +
                ", yearlyCycle=" + yearlyCycle + " (" + yearlyCycleDesc + ")" +
                '}';
    }

    static final class Builder {
        private int solYear;
        private int solMonth;
        private int solDay;
        private int julianDays;
        private int solDayOfWeek;
        private boolean solLeapYear;
        private int lunYear;
        private int lunMonth;
        private int lunDay;
        private boolean lunLeapMonth;
        private int lunDaysOfMonth;
        private int dailyCycle;
        private int monthlyCycle;
        private int yearlyCycle;

        Builder solYear(final int value) {
            this.solYear = value;
            return this;
        }

        Builder solMonth(final int value) {
            this.solMonth = value;
            return this;
        }

        Builder solDay(final int value) {
            this.solDay = value;
            return this;
        }

        Builder julianDays(final int value) {
            this.julianDays = value;
            return this;
        }

        Builder solDayOfWeek(final int value) {
            this.solDayOfWeek = value;
            return this;
        }

        Builder solLeapYear(final boolean value) {
            this.solLeapYear = value;
            return this;
        }

        Builder lunYear(final int value) {
            this.lunYear = value;
            return this;
        }

        Builder lunMonth(final int value) {
            this.lunMonth = value;
            return this;
        }

        Builder lunDay(final int value) {
            this.lunDay = value;
            return this;
        }

        Builder lunLeapMonth(final boolean value) {
            this.lunLeapMonth = value;
            return this;
        }

        Builder lunDaysOfMonth(final int value) {
            this.lunDaysOfMonth = value;
            return this;
        }

        Builder dailyCycle(final int value) {
            this.dailyCycle = value;
            return this;
        }

        Builder monthlyCycle(final int value) {
            this.monthlyCycle = value;
            return this;
        }

        Builder yearlyCycle(final int value) {
            this.yearlyCycle = value;
            return this;
        }

        KoreanLunarDate build() {
            return new KoreanLunarDate(solYear, solMonth, solDay, solDayOfWeek, solLeapYear, julianDays,
                    lunYear, lunMonth, lunDay, lunLeapMonth, lunDaysOfMonth,
                    dailyCycle, monthlyCycle, yearlyCycle);
        }
    }
}
