//usr/bin/env jshell --show-version --execution local "$0" "$@"; exit $?
class DataScrapper {
    private static final Pattern SEXAGENARY_CYCLE_PATTERN = Pattern.compile("..\\((..)\\)");

    private static final Map<String, Integer> SEXAGENARY_CYCLE_SYMBOLS = new HashMap<String, Integer>() {
        {
            put("甲子", 1);
            put("乙丑", 2);
            put("丙寅", 3);
            put("丁卯", 4);
            put("戊辰", 5);
            put("己巳", 6);
            put("庚午", 7);
            put("辛未", 8);
            put("壬申", 9);
            put("癸酉", 10);
            put("甲戌", 11);
            put("乙亥", 12);
            put("丙子", 13);
            put("丁丑", 14);
            put("戊寅", 15);
            put("己卯", 16);
            put("庚辰", 17);
            put("辛巳", 18);
            put("壬午", 19);
            put("癸未", 20);
            put("甲申", 21);
            put("乙酉", 22);
            put("丙戌", 23);
            put("丁亥", 24);
            put("戊子", 25);
            put("己丑", 26);
            put("庚寅", 27);
            put("辛卯", 28);
            put("壬辰", 29);
            put("癸巳", 30);
            put("甲午", 31);
            put("乙未", 32);
            put("丙申", 33);
            put("丁酉", 34);
            put("戊戌", 35);
            put("己亥", 36);
            put("庚子", 37);
            put("辛丑", 38);
            put("壬寅", 39);
            put("癸卯", 40);
            put("甲辰", 41);
            put("乙巳", 42);
            put("丙午", 43);
            put("丁未", 44);
            put("戊申", 45);
            put("己酉", 46);
            put("庚戌", 47);
            put("辛亥", 48);
            put("壬子", 49);
            put("癸丑", 50);
            put("甲寅", 51);
            put("乙卯", 52);
            put("丙辰", 53);
            put("丁巳", 54);
            put("戊午", 55);
            put("己未", 56);
            put("庚申", 57);
            put("辛酉", 58);
            put("壬戌", 59);
            put("癸亥", 60);
        }
    };

    private static final Map<String, Integer> WEEKDAYS = new HashMap<String, Integer>() {
        {
            put("일", Calendar.SUNDAY);
            put("월", Calendar.MONDAY);
            put("화", Calendar.TUESDAY);
            put("수", Calendar.WEDNESDAY);
            put("목", Calendar.THURSDAY);
            put("금", Calendar.FRIDAY);
            put("토", Calendar.SATURDAY);
        }
    };

    /* Data can be acquired at: https://astro.kasi.re.kr/information/pageView/31 */
    void visit(final String dir, final NodeReaderCallback callback) throws Exception {
        for (int year = 1900; year <= 2050; year++) {
            for (int month = 1; month <= 12; month++) {
                if (year == 2050 && month > 1) {
                    break;
                }
                final String yearPrefix = "" + (year - (year % 10));
                final String fileName = year + "_" + String.format("%02d", month) + "_01" + ".xml";
                final File xmlFile = new File(dir, yearPrefix + "/" + fileName);

                final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                final DocumentBuilder dBuilder = factory.newDocumentBuilder();
                final Document doc = dBuilder.parse(xmlFile);
                doc.getDocumentElement().normalize();
                final XPath xPath = XPathFactory.newInstance().newXPath();
                final String expression = "/response/body/items";
                final NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);
                final NodeList itemsChilds = nodeList.item(0).getChildNodes();
                final List<KoreanLunarDate> dateItems = new ArrayList<>();
                final KoreanLunarDate.Builder builder = new KoreanLunarDate.Builder();

                for (int i = 0; i < itemsChilds.getLength(); i++) {
                    final Node item = itemsChilds.item(i);

                    if (item.getNodeType() == Node.ELEMENT_NODE) {
                        final Element element = (Element) item;
                        final boolean isLeapMonth = isLeap(valueOf(element, "lunLeapmonth"));

                        builder.solYear(Integer.parseInt(valueOf(element, "solYear")))
                                .solMonth(Integer.parseInt(valueOf(element, "solMonth")))
                                .solDay(Integer.parseInt(valueOf(element, "solDay")))
                                .solJulianDays(Integer.parseInt(valueOf(element, "solJd")))
                                .solWeekDay(WEEKDAYS.get(valueOf(element, "solWeek")))
                                .solLeapYear(isLeap(valueOf(element, "solLeapyear")))
                                .lunYear(Integer.parseInt(valueOf(element, "lunYear")))
                                .lunMonth(Integer.parseInt(valueOf(element, "lunMonth")))
                                .lunDay(Integer.parseInt(valueOf(element, "lunDay")))
                                .lunLeapMonth(isLeapMonth)
                                .lunMonthDays(Integer.parseInt(valueOf(element, "lunNday")))
                                .dailyCycle(getCycle(valueOf(element, "lunIljin")))
                                .yearlyCycle(getCycle(valueOf(element, "lunSecha")));

                        final int monthlyCycle;
                        if (isLeapMonth) {
                            monthlyCycle = 0;
                        } else {
                            monthlyCycle = getCycle(valueOf(element, "lunWolgeon"));
                        }
                        builder.monthlyCycle(monthlyCycle);

                        dateItems.add(builder.build());
                    }
                }

                callback.applyNode(year, month, dateItems);
            }
        }
    }

    private String valueOf(final Element element, final String elementName) {
        return element.getElementsByTagName(elementName).item(0).getTextContent();
    }

    private boolean isLeap(final String str) {
        return "윤".equals(str);
    }

    private int getCycle(final String sexagenarySymbols) {
        final Matcher m = SEXAGENARY_CYCLE_PATTERN.matcher(sexagenarySymbols);
        if (!m.find()) {
            throw new IllegalArgumentException("Illegal data format.");
        }
        return SEXAGENARY_CYCLE_SYMBOLS.get(m.group(1));
    }

    private int findLeapMonth(final List<KoreanLunarDate> nodeList) {
        if (nodeList.size() == 1) {
            return 0;
        }

        final KoreanLunarDate leapMonth = find(nodeList, it -> it.isLunLeapMonth);
        if (leapMonth == null) {
            return 0;
        } else {
            return leapMonth.lunMonth;
        }
    }

    private <T> T find(final Collection<T> data, final Predicate<T> predicate) {
        for (final T datum : data) {
            if (predicate.test(datum)) {
                return datum;
            }
        }

        return null;
    }

    @FunctionalInterface
    private interface NodeReaderCallback {
        void applyNode(final int year, final int month, final List<KoreanLunarDate> nodeList);
    }
}

new DataScrapper().visit("/var/tmp/lun2Sol", (year, month, dateList) -> {
    System.out.println("Data: " + dateList);
});

/exit
