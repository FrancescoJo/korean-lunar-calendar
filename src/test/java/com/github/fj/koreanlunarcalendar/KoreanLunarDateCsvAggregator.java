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

import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.aggregator.ArgumentsAggregationException;
import org.junit.jupiter.params.aggregator.ArgumentsAggregator;

/**
 * @since 25 - Oct - 2019
 */
class KoreanLunarDateCsvAggregator implements ArgumentsAggregator {
    @Override
    public Object aggregateArguments(final ArgumentsAccessor args, final ParameterContext context)
            throws ArgumentsAggregationException {
        return new KoreanLunarDate.Builder()
                .solYear(args.getInteger(0))
                .solMonth(args.getInteger(1))
                .solDay(args.getInteger(2))
                .solDayOfWeek(args.getInteger(4))
                .solLeapYear(args.getBoolean(5))
                .julianDays(args.getInteger(3))
                .lunYear(args.getInteger(6))
                .lunMonth(args.getInteger(7))
                .lunDay(args.getInteger(8))
                .lunLeapMonth(args.getBoolean(9))
                .lunDaysOfMonth(args.getInteger(10))
                .dailyCycle(args.getInteger(11))
                .monthlyCycle(args.getInteger(12))
                .yearlyCycle(args.getInteger(13))
                .build();
    }
}
