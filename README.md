# KoreanLunarCalendarUtils
**NOTE**: All terms of 'lunar', such as 'lunar date', 'lunar calendar', etc., in this utility's context means
lunisolar date. Korean 'traditional' lunisolar date refers Chinese Shixian calendar which is developed 
by Qing dynasity of China, in 1644.

## Abstract 
A date conversion utility between Gregorian calendar system date to Korean lunisolar calendar system date.

Accepted date range is described below:

  |      |      Gregorian      |      Lunisolar      |
  |------|---------------------|---------------------|
  | From |   1st - Feb - 1900  |   1st - Jan - 1900  |
  |  To  |  31st - Dec - 2049  |  30rd - Dec - 2049  |

Although many parts of Korean lunisolar system is compatible to Chinese Shixian calendar, length of month data
is slightly different. Moreover, this subtle difference does not accumulates itself in short date ranges - which
makes detecting error more difficult.

This implementation uses calendar data officially presented by
[Korea Astronomy and Space Science Institute](https://astro.kasi.re.kr).

## Compatibility
Since this implementation relies only on Java legacy Calendar API, it works under Java 1.6+ or any Android versions.

## Download
You must install [jitpack.io](https://jitpack.io/) plugin to your build script in order to import this project.

### Gradle

    dependencies {
        implementation "com.github.FrancescoJo:korean-lunar-calendar:1.0"
    }

### Maven

    <dependency>
        <groupId>com.github.FrancescoJo</groupId>
        <artifactId>korean-lunar-calendar</artifactId>
        <version>1.0</version>
    </dependency>

## Usage

### From solar date to lunar date

    System.out.println(getSolarDayOf(2000, 1, 1));
    
    output:
    KoreanLunarDate{solYear=2000, 
      solMonth=1,
      solDay=1,
      solDayOfWeek=7,
      solLeapYear=true,
      julianDays=2451545,
      lunYear=1999,
      lunMonth=11,
      lunDay=25,
      lunLeapMonth=false,
      lunDaysOfMonth=30,
      dailyCycle=55 (戊午, 무오),
      monthlyCycle=13 (丙子, 병자),
      yearlyCycle=16 (己卯, 기묘)
    }

Note that `monthlyCycle` could be `0` only if given date is converted as lunar leap month,
because leap month does not hold any sexagenary cycles.

### From lunar date to solar date

    System.out.println(getSolarDateOf(1999, 11, 25, false));
    
    output:
    KoreanLunarDate{solYear=2000, 
      solMonth=1,
      solDay=1,
      solDayOfWeek=7,
      solLeapYear=true,
      julianDays=2451545,
      lunYear=1999,
      lunMonth=11,
      lunDay=25,
      lunLeapMonth=false,
      lunDaysOfMonth=30,
      dailyCycle=55 (戊午, 무오),
      monthlyCycle=13 (丙子, 병자),
      yearlyCycle=16 (己卯, 기묘)
    }

## License
This code is under the [Apache Licence v2](https://www.apache.org/licenses/LICENSE-2.0).

## Implementation note
Personally I prefer Kotlin over Java, but still there are more Java users than Kotlin(year 2019). Also, Kotlin has
a great Java interoperability, a pure Java based library would no hassles to Kotlin users.
However, it is not like that in vice versa.

Because coding in Kotlin requires additional JAR dependency called `kotlin-stdlib.jar`, usually 800KiB to 1MiB long.
Considering about the size of this project, such 'crucial' addition is undesirable, therefore this project is 
implemented in pure Java language.
