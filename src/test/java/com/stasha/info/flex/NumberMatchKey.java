package com.stasha.info.flex;

import java.util.*;

// Optimized NumberMatchKey with string constructor (unchanged)
class NumberMatchKey {
    public enum MatchType {
        EXACT, PREFIX, SUFFIX, FLOOR, CEILING, RANGE
    }

    public static final long[] POWERS_OF_10 = {
        1L, 10L, 100L, 1000L, 10000L, 100000L, 1000000L, 10000000L,
        100000000L, 1000000000L, 10000000000L, 100000000000L,
        1000000000000L, 10000000000000L, 100000000000000L,
        1000000000000000L, 10000000000000000L, 100000000000000000L,
        1000000000000000000L
    };

    final MatchType matchType;
    final long number;
    final int digitCount;
    final long digitValue;
    final long rangeEnd;
    final long prefixDivisor;
    final int cachedHashCode;
    
    
        // Unified constructor for all match types
    public NumberMatchKey(MatchType matchType, long number, long rangeEnd, int digitCount) {
        this.matchType = matchType;
        this.number = number;
        this.rangeEnd = matchType == MatchType.RANGE ? rangeEnd : 0;
        this.digitCount = (matchType == MatchType.PREFIX || matchType == MatchType.SUFFIX) ? digitCount : 0;
        this.digitValue = computeDigitValue();
        this.prefixDivisor = matchType == MatchType.PREFIX ? POWERS_OF_10[countDigits(number) - digitCount] : 0;
        this.cachedHashCode = computeHashCode();
    }

    public NumberMatchKey(MatchType matchType, long number) {
        if (matchType != MatchType.EXACT && matchType != MatchType.FLOOR && matchType != MatchType.CEILING) {
            throw new IllegalArgumentException("Match type must be EXACT, FLOOR, or CEILING");
        }
        validateNumber(number);
        this.matchType = matchType;
        this.number = number;
        this.digitCount = 0;
        this.digitValue = number;
        this.rangeEnd = 0;
        this.prefixDivisor = 0;
        this.cachedHashCode = computeHashCode();
    }

    public NumberMatchKey(MatchType matchType, long number, int digitCount) {
        if (matchType != MatchType.PREFIX && matchType != MatchType.SUFFIX) {
            throw new IllegalArgumentException("Match type must be PREFIX or SUFFIX");
        }
        validateNumber(number);
        if (digitCount <= 0 || digitCount > countDigits(number) || digitCount >= POWERS_OF_10.length) {
            throw new IllegalArgumentException("Invalid digit count: " + digitCount);
        }
        this.matchType = matchType;
        this.number = number;
        this.digitCount = digitCount;
        this.digitValue = computeDigitValue();
        this.rangeEnd = 0;
        this.prefixDivisor = matchType == MatchType.PREFIX ? POWERS_OF_10[countDigits(number) - digitCount] : 0;
        this.cachedHashCode = computeHashCode();
    }

    public NumberMatchKey(NumberMatchKey start, NumberMatchKey end) {
        MatchType rangeMatchType = start.matchType == MatchType.PREFIX || start.matchType == MatchType.SUFFIX
            ? start.matchType : MatchType.EXACT;
        if (start.matchType != end.matchType &&
            !(start.matchType == MatchType.CEILING && end.matchType == MatchType.EXACT) &&
            !(start.matchType == MatchType.EXACT && end.matchType == MatchType.CEILING)) {
            throw new IllegalArgumentException("Invalid range: must be same match type or CEILING-EXACT/EXACT-CEILING");
        }
        if (start.digitCount != end.digitCount) {
            throw new IllegalArgumentException("Range start and end must have same digit count");
        }
        if (rangeMatchType == MatchType.EXACT && start.digitValue > end.digitValue) {
            throw new IllegalArgumentException("Range start must be less than or equal to end");
        }
        if ((rangeMatchType == MatchType.PREFIX || rangeMatchType == MatchType.SUFFIX) &&
            start.digitValue > end.digitValue) {
            throw new IllegalArgumentException("Range start digits must be less than or equal to end digits");
        }
        this.matchType = MatchType.RANGE;
        this.number = start.number;
        this.digitCount = start.digitCount;
        this.digitValue = start.digitValue;
        this.rangeEnd = end.digitValue;
        this.prefixDivisor = rangeMatchType == MatchType.PREFIX ? POWERS_OF_10[countDigits(start.number) - digitCount] : 0;
        this.cachedHashCode = computeHashCode();
    }

    public NumberMatchKey(String key) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Key string cannot be null or empty");
        }
        key = key.trim();

        if (key.contains("-")) {
            String[] parts = key.split("-");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid range format: " + key);
            }
            String start = parts[0].trim();
            String end = parts[1].trim();
            NumberMatchKey startKey, endKey;

            if (start.startsWith("~")) {
                String num = start.substring(1).trim();
                startKey = new NumberMatchKey(MatchType.CEILING, parseNumber(num));
            } else if (start.endsWith("~")) {
                String num = start.substring(0, start.length() - 1).trim();
                startKey = new NumberMatchKey(MatchType.FLOOR, parseNumber(num));
            } else if (start.endsWith("_")) {
                String num = start.substring(0, start.length() - 1).trim();
                int digits = countDigits(parseNumber(num));
                startKey = new NumberMatchKey(MatchType.PREFIX, parseNumber(num), digits);
            } else {
                startKey = new NumberMatchKey(MatchType.EXACT, parseNumber(start));
            }

            if (end.startsWith("~")) {
                String num = end.substring(1).trim();
                endKey = new NumberMatchKey(MatchType.CEILING, parseNumber(num));
            } else if (end.endsWith("~")) {
                String num = start.substring(0, start.length() - 1).trim();
                endKey = new NumberMatchKey(MatchType.FLOOR, parseNumber(num));
            } else if (end.endsWith("_")) {
                String num = end.substring(0, end.length() - 1).trim();
                int digits = countDigits(parseNumber(num));
                endKey = new NumberMatchKey(MatchType.PREFIX, parseNumber(num), digits);
            } else if (end.startsWith("_")) {
                String num = end.substring(1).trim();
                int digits = countDigits(parseNumber(num));
                endKey = new NumberMatchKey(MatchType.SUFFIX, parseNumber(num), digits);
            } else {
                endKey = new NumberMatchKey(MatchType.EXACT, parseNumber(end));
            }

            if ((startKey.matchType == MatchType.PREFIX && endKey.matchType == MatchType.SUFFIX) ||
                (startKey.matchType == MatchType.SUFFIX && endKey.matchType == MatchType.PREFIX)) {
                throw new IllegalArgumentException("Cannot mix prefix and suffix in range: " + key);
            }

            this.matchType = MatchType.RANGE;
            this.number = startKey.number;
            this.digitCount = startKey.digitCount;
            this.digitValue = startKey.digitValue;
            this.rangeEnd = endKey.digitValue;
            this.prefixDivisor = startKey.matchType == MatchType.PREFIX ? POWERS_OF_10[countDigits(startKey.number) - digitCount] : 0;
            this.cachedHashCode = computeHashCode();
            return;
        }

        if (key.endsWith("_")) {
            String num = key.substring(0, key.length() - 1).trim();
            long value = parseNumber(num);
            int digits = countDigits(value);
            this.matchType = MatchType.PREFIX;
            this.number = value;
            this.digitCount = digits;
            this.digitValue = computeDigitValue();
            this.rangeEnd = 0;
            this.prefixDivisor = POWERS_OF_10[countDigits(number) - digitCount];
            this.cachedHashCode = computeHashCode();
            return;
        }

        if (key.startsWith("_")) {
            String num = key.substring(1).trim();
            long value = parseNumber(num);
            int digits = countDigits(value);
            this.matchType = MatchType.SUFFIX;
            this.number = value;
            this.digitCount = digits;
            this.digitValue = computeDigitValue();
            this.rangeEnd = 0;
            this.prefixDivisor = 0;
            this.cachedHashCode = computeHashCode();
            return;
        }

        if (key.startsWith("~")) {
            String num = key.substring(1).trim();
            long value = parseNumber(num);
            this.matchType = MatchType.FLOOR;
            this.number = value;
            this.digitCount = 0;
            this.digitValue = value;
            this.rangeEnd = 0;
            this.prefixDivisor = 0;
            this.cachedHashCode = computeHashCode();
            return;
        }

        if (key.endsWith("~")) {
            String num = key.substring(0, key.length() - 1).trim();
            long value = parseNumber(num);
            this.matchType = MatchType.CEILING;
            this.number = value;
            this.digitCount = 0;
            this.digitValue = value;
            this.rangeEnd = 0;
            this.prefixDivisor = 0;
            this.cachedHashCode = computeHashCode();
            return;
        }

        long value = parseNumber(key);
        this.matchType = MatchType.EXACT;
        this.number = value;
        this.digitCount = 0;
        this.digitValue = value;
        this.rangeEnd = 0;
        this.prefixDivisor = 0;
        this.cachedHashCode = computeHashCode();
    }

    private void validateNumber(long number) {
        if (number < 0) {
            throw new IllegalArgumentException("Number must be positive");
        }
    }

    public static int countDigits(long num) {
        if (num < 10) return 1;
        if (num < 100) return 2;
        if (num < 1000) return 3;
        if (num < 10000) return 4;
        if (num < 100000) return 5;
        if (num < 1000000) return 6;
        if (num < 10000000) return 7;
        if (num < 100000000) return 8;
        if (num < 1000000000) return 9;
        if (num < 10000000000L) return 10;
        if (num < 100000000000L) return 11;
        if (num < 1000000000000L) return 12;
        if (num < 10000000000000L) return 13;
        if (num < 100000000000000L) return 14;
        if (num < 1000000000000000L) return 15;
        if (num < 10000000000000000L) return 16;
        if (num < 100000000000000000L) return 17;
        if (num < 1000000000000000000L) return 18;
        return 19;
    }

    private long computeDigitValue() {
        if (matchType == MatchType.PREFIX) {
            return number / POWERS_OF_10[countDigits(number) - digitCount];
        } else if (matchType == MatchType.SUFFIX) {
            return number % POWERS_OF_10[digitCount];
        }
        return number;
    }

    public long getNumber() {
        return number;
    }

    public MatchType getMatchType() {
        return matchType;
    }

    public boolean matches(long testNumber) {
        if (testNumber < 0) return false;
        switch (matchType) {
            case EXACT:
                return testNumber == number;
            case FLOOR:
                return testNumber <= number;
            case CEILING:
                return testNumber >= number;
            case SUFFIX:
                return (testNumber % POWERS_OF_10[digitCount]) == digitValue;
            case PREFIX:
                int testDigits = countDigits(testNumber);
                if (digitCount > testDigits) return false;
                return (testNumber / POWERS_OF_10[testDigits - digitCount]) == digitValue;
            case RANGE:
                long testValue = digitCount == 0 ? testNumber :
                    (matchType == MatchType.PREFIX ?
                        (countDigits(testNumber) >= digitCount ? testNumber / POWERS_OF_10[countDigits(testNumber) - digitCount] : -1) :
                        testNumber % POWERS_OF_10[digitCount]);
                return testValue >= digitValue && testValue <= rangeEnd;
            default:
                return false;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NumberMatchKey that = (NumberMatchKey) o;
        return matchType == that.matchType &&
               digitCount == that.digitCount &&
               digitValue == that.digitValue &&
               rangeEnd == that.rangeEnd &&
               prefixDivisor == that.prefixDivisor;
    }

    @Override
    public int hashCode() {
        return cachedHashCode;
    }

    private int computeHashCode() {
        return Objects.hash(matchType, digitCount, digitValue, rangeEnd, prefixDivisor);
    }

    private long parseNumber(String str) {
        try {
            long value = Long.parseLong(str.trim());
            if (value <= 0) {
                throw new IllegalArgumentException("Number must be positive: " + str);
            }
            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number format: " + str);
        }
    }
}
