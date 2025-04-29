package com.stasha.info.flex;

import com.ibm.icu.text.MessageFormat;
import com.ibm.icu.util.ULocale;

import java.util.HashMap;
import java.util.Map;

public class ICU4JSerbianOrdinalTest {
    private static final ULocale LOCALE = new ULocale("sr_RS");
    private static final Map<Integer, Map<String, String>> ordinalCache = new HashMap<>();

    // Initialize ordinal cache for 0–100, m/f/n
    static {
        String[][] ordinals = {
            {"nulti", "nulta", "nulto"}, {"prvi", "prva", "prvo"}, {"drugi", "druga", "drugo"},
            {"treći", "treća", "treće"}, {"četvrti", "četvrta", "četvrto"}, {"peti", "peta", "peto"},
            {"šesti", "šesta", "šesto"}, {"sedmi", "sedma", "sedmo"}, {"osmi", "osma", "osmo"},
            {"deveti", "deveta", "deveto"}, {"deseti", "deseta", "deseto"},
            {"jedanaesti", "jedanaesta", "jedanaesto"}, {"dvanaesti", "dvanaesta", "dvanaesto"},
            {"trinaesti", "trinaesta", "trinaesto"}, {"četrnaesti", "četrnaesta", "četrnaesto"},
            {"petnaesti", "petnaesta", "petnaesto"}, {"šesnaesti", "šesnaesta", "šesnaesto"},
            {"sedamnaesti", "sedamnaesta", "sedamnaesto"}, {"osamnaesti", "osamnaesta", "osamnaesto"},
            {"devetnaesti", "devetnaesta", "devetnaesto"}, {"dvadeseti", "dvadeseta", "dvadeseto"}
        };
        for (int i = 0; i <= 100; i++) {
            Map<String, String> genderMap = new HashMap<>();
            if (i <= 20) {
                genderMap.put("m", ordinals[i][0]);
                genderMap.put("f", ordinals[i][1]);
                genderMap.put("n", ordinals[i][2]);
            } else {
                String tens = switch (i / 10) {
                    case 2 -> "dvadeset"; case 3 -> "trideset"; case 4 -> "četrdeset";
                    case 5 -> "pedeset"; case 6 -> "šezdeset"; case 7 -> "sedamdeset";
                    case 8 -> "osamdeset"; case 9 -> "devedeset"; case 10 -> "stoti";
                    default -> "";
                };
                int unit = i % 10;
                String base = unit == 0 ? tens : tens + " " + ordinals[unit][0];
                genderMap.put("m", base);
                genderMap.put("f", unit == 0 ? tens + "a" : tens + " " + ordinals[unit][1]);
                genderMap.put("n", unit == 0 ? tens + "o" : tens + " " + ordinals[unit][2]);
            }
            ordinalCache.put(i, genderMap);
        }
    }

    // Format ordinal number with gender
    private static String formatOrdinal(int number, String gender) {
        Map<String, String> genderMap = ordinalCache.get(number);
        if (genderMap != null) {
            String ordinal = genderMap.get(gender);
            if (ordinal != null) return ordinal;
        }
        return String.valueOf(number); // Fallback for unsupported numbers
    }

    public static void main(String[] args) {
        // Pattern: {0} is gender (m/f/n), {1} is pre-formatted ordinal string
        String pattern = "{0,select,m{Bio sam} f{Bila sam} n{Bilo je} other{}} {1}";
        MessageFormat messageFormat = new MessageFormat(pattern, LOCALE);

        String[] genders = {"m", "f", "n"};

        // Test loop: 0–100, m/f/n
        long start = System.currentTimeMillis();
        for (int i = 0; i <= 10000000; i++) {
            for (String gender : genders) {
                // Pre-format ordinal
                String ordinal = formatOrdinal(i, gender);
                // Pass gender and ordinal as arguments
                Object[] arguments = {gender, ordinal};
                String result = messageFormat.format(arguments);
//                System.out.println(result);
            }
        }
        long end = System.currentTimeMillis();
        System.out.println("ICU4J loadOrdinalsTest end time: " + (end - start) + " ms");
    }
}