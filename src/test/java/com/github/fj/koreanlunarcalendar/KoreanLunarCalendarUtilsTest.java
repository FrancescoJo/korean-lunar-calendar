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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.AggregateWith;
import org.junit.jupiter.params.provider.CsvFileSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @since 25 - Oct - 2019
 */
public class KoreanLunarCalendarUtilsTest {
    @ParameterizedTest
    @CsvFileSource(resources = {"/lunisolar/sol_1900.csv", "/lunisolar/sol_1910.csv", "/lunisolar/sol_1920.csv",
            "/lunisolar/sol_1930.csv", "/lunisolar/sol_1940.csv", "/lunisolar/sol_1950.csv",
            "/lunisolar/sol_1960.csv", "/lunisolar/sol_1970.csv", "/lunisolar/sol_1980.csv",
            "/lunisolar/sol_1990.csv", "/lunisolar/sol_2000.csv", "/lunisolar/sol_2010.csv",
            "/lunisolar/sol_2020.csv", "/lunisolar/sol_2030.csv", "/lunisolar/sol_2040.csv",
    }, numLinesToSkip = 1)
    public void onDataAggregated(final @AggregateWith(KoreanLunarDateCsvAggregator.class) KoreanLunarDate expected) {
        matchSolarToLunar(expected);
        matchLunarToSolar(expected);
        circulateSolarToLunar(expected);
        circulateLunarToSolar(expected);
    }

    private void matchSolarToLunar(final KoreanLunarDate expected) {
        // then:
        final KoreanLunarDate actual = KoreanLunarCalendarUtils.getLunarDateOf(
                expected.solYear, expected.solMonth, expected.solDay
        );

        // expect:
        assertEquals(expected, actual);
    }

    private void matchLunarToSolar(final KoreanLunarDate expected) {
        // then:
        final KoreanLunarDate actual = KoreanLunarCalendarUtils.getSolarDateOf(
                expected.lunYear, expected.lunMonth, expected.lunDay, expected.isLunLeapMonth
        );

        // expect:
        assertEquals(expected, actual);
    }

    private void circulateSolarToLunar(final KoreanLunarDate givenSolarDate) {
        // given:
        final KoreanLunarDate lunarDate = KoreanLunarCalendarUtils.getLunarDateOf(
                givenSolarDate.solYear, givenSolarDate.solMonth, givenSolarDate.solDay
        );

        // and:
        final KoreanLunarDate solarDate = KoreanLunarCalendarUtils.getSolarDateOf(
                lunarDate.lunYear, lunarDate.lunMonth, lunarDate.lunDay, lunarDate.isLunLeapMonth
        );

        // expect:
        assertEquals(givenSolarDate, lunarDate);
        assertEquals(lunarDate, solarDate);
        assertEquals(givenSolarDate, solarDate);
    }

    private void circulateLunarToSolar(final KoreanLunarDate givenLunarDate) {
        // given:
        final KoreanLunarDate solarDate = KoreanLunarCalendarUtils.getSolarDateOf(
                givenLunarDate.lunYear, givenLunarDate.lunMonth, givenLunarDate.lunDay, givenLunarDate.isLunLeapMonth
        );

        // and:
        final KoreanLunarDate lunarDate = KoreanLunarCalendarUtils.getLunarDateOf(
                solarDate.solYear, solarDate.solMonth, solarDate.solDay
        );

        // expect:
        assertEquals(givenLunarDate, solarDate);
        assertEquals(solarDate, lunarDate);
        assertEquals(givenLunarDate, lunarDate);
    }
}
