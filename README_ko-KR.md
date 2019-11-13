# KoreanLunarCalendarUtils
**주의**: 이 문서 혹은 프로그램의 소스 코드에서 말하는 '음력', 혹은 '음력 달력' 은 순태음력(lunar)이 아닌
태음태양력(lunisolar)을 의미합니다. 한국의 '전통' 태음태양력은 1644년 중국의 청나라에서 개발한 '시헌력' 을 
기반으로 하고 있습니다.

## 개요
서력(그레고리력) 날짜를 한국의 음력 날짜로, 혹은 그 반대로 변환하는 유틸리티입니다.

허용하는 날짜 범위는 다음과 같습니다.

  |      |         서력        |          음력      |
  |------|---------------------|--------------------|
  | 시작  |  1900년 2월 1일    |  1900년 1월 1일    |
  |  끝  |  2049년 12월 31일   |  2049년 12월 30일  |

한국의 음력 달력은 중국의 시헌력을 기반으로 만들어 졌기 때문에 많은 부분이 호환되지만 음력 월별 날짜 길이가 일부 다른
구간이 있습니다. 그리고 이 차이가 누적되는 구간이 길지 않기 때문에 오류를 찾아내기가 매우 어렵습니다.

따라서 이 구현은 중국의 시헌력을 그대로 사용하는 대신 [한국천문연구원](https://astro.kasi.re.kr) 에서 공식 배포하는
날짜 데이터를 기반으로 만들어졌습니다.

## 호환성
이 구현은 Java 의 오래된 구현에 의존하기 때문에 Java 1.6 이상 혹은 모든 안드로이드 버전에서 동작합니다.

## 설치 방법
사용중인 빌드 시스템에 [jitpack.io](https://jitpack.io/) 플러그인을 설치해주세요.

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

## 사용법

### 양력을 음력으로 바꿀 때

    System.out.println(getLunarDateOf(2000, 1, 1));
    
    결과:
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

윤달은 월건(monthlyCycle) 이 없는 달이기 때문에 `monthlyCycle` 은 0이 될 수 있습니다. dailyCycle, monthlyCycle,
yearlyCycle 은 각각 육십갑자를 나타내는 값이며 1부터 60까지의 값이 허용됩니다.

### 음력을 양력으로 바꿀 때

    System.out.println(getSolarDateOf(1999, 11, 25, false));
    
    결과:
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

## 라이선스
이 프로그램은 [아파치 라이선스 v2](https://www.apache.org/licenses/LICENSE-2.0) 에 의거해 배포됩니다.

## 구현 안내
개인적으로 저는 Java 보다는 Kotlin 을 더 선호합니다만 2019년 현재까지도 Java 개발자의 숫자가 훨씬 더 많습니다.
그리고 Kotlin 의 Java 상호 운용성은 매우 우수하며 Java 코드를 Kotlin 에서 바로 사용하는 데에 큰 문제는 없지만
그 반대는 그렇지 않습니다.

왜냐하면 Kotlin 으로 코딩한 라이브러리는 Kotlin/JVM 으로 배포시 약 800KiB 에서 1MiB 정도 크기의 
`kotlin-stdlib.jar` 의존성이 추가로 더 필요하기 때문입니다. 이 프로그램의 크기를 고려했을 때 그런 '필수' 의존성을
추가하는 것은 불필요하기 때문에 이 프로그램은 순수 Java 로만 구현했습니다.
