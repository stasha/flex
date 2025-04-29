package com.stasha.info.flex;

public class NumberMatchKeyTest {
    public static void main(String[] args) {
        testCorrectness();
        System.out.println("All correctness tests passed!");
    }

    private static void testCorrectness() {
        // Test EXACT
        NumberMatchKey exact = new NumberMatchKey(NumberMatchKey.MatchType.EXACT, 20);
        assertTrue(exact.matches(20), "EXACT should match 20");
        assertFalse(exact.matches(21), "EXACT should not match 21");
        assertFalse(exact.matches(0), "EXACT should not match 0");

        // Test PREFIX
        NumberMatchKey prefix = new NumberMatchKey(NumberMatchKey.MatchType.PREFIX, 12, 2);
        assertTrue(prefix.matches(1234), "PREFIX should match 1234");
        assertTrue(prefix.matches(1200), "PREFIX should match 1200");
        assertFalse(prefix.matches(3456), "PREFIX should not match 3456");
        assertFalse(prefix.matches(1), "PREFIX should not match 1 (too few digits)");

        // Test SUFFIX
        NumberMatchKey suffix = new NumberMatchKey(NumberMatchKey.MatchType.SUFFIX, 78, 2);
        assertTrue(suffix.matches(5678), "SUFFIX should match 5678");
        assertTrue(suffix.matches(12345678), "SUFFIX should match 12345678");
        assertFalse(suffix.matches(5679), "SUFFIX should not match 5679");
        assertFalse(suffix.matches(7), "SUFFIX should not match 7 (too few digits)");

        // Test RANGE
        NumberMatchKey range = new NumberMatchKey(NumberMatchKey.MatchType.RANGE, 10, 20, 0);
        assertTrue(range.matches(10), "RANGE should match 10");
        assertTrue(range.matches(15), "RANGE should match 15");
        assertTrue(range.matches(20), "RANGE should match 20");
        assertFalse(range.matches(9), "RANGE should not match 9");
        assertFalse(range.matches(21), "RANGE should not match 21");

        // Test FLOOR
        NumberMatchKey floor = new NumberMatchKey(NumberMatchKey.MatchType.FLOOR, 20);
        assertTrue(floor.matches(19), "FLOOR should match 19");
        assertTrue(floor.matches(20), "FLOOR should match 20");
        assertFalse(floor.matches(21), "FLOOR should not match 21");

        // Test CEILING
        NumberMatchKey ceiling = new NumberMatchKey(NumberMatchKey.MatchType.CEILING, 20);
        assertTrue(ceiling.matches(21), "CEILING should match 21");
        assertTrue(ceiling.matches(20), "CEILING should match 20");
        assertFalse(ceiling.matches(19), "CEILING should not match 19");

        // Test String Constructor
        NumberMatchKey exactStr = new NumberMatchKey("20");
        assertTrue(exactStr.matches(20), "String EXACT should match 20");
        NumberMatchKey prefixStr = new NumberMatchKey("12_");
        assertTrue(prefixStr.matches(1234), "String PREFIX should match 1234");
        NumberMatchKey suffixStr = new NumberMatchKey("_78");
        assertTrue(suffixStr.matches(5678), "String SUFFIX should match 5678");
        NumberMatchKey rangeStr = new NumberMatchKey("10-20");
        assertTrue(rangeStr.matches(15), "String RANGE should match 15");
        NumberMatchKey floorStr = new NumberMatchKey("~20");
        assertTrue(floorStr.matches(19), "String FLOOR should match 19");
        NumberMatchKey ceilingStr = new NumberMatchKey("20~");
        assertTrue(ceilingStr.matches(21), "String CEILING should match 21");

        // Test Edge Cases
        NumberMatchKey zeroExact = new NumberMatchKey(NumberMatchKey.MatchType.EXACT, 0);
        assertTrue(zeroExact.matches(0), "EXACT should match 0");
        assertFalse(zeroExact.matches(1), "EXACT should not match 1");

        NumberMatchKey largePrefix = new NumberMatchKey(NumberMatchKey.MatchType.PREFIX, 99999999, 8);
        assertTrue(largePrefix.matches(999999991234L), "PREFIX should match 999999991234");
        assertFalse(largePrefix.matches(99999998), "PREFIX should not match 99999998");

        // Test Invalid String Inputs
        try {
            new NumberMatchKey("");
            throw new AssertionError("Empty string should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
        try {
            new NumberMatchKey("invalid");
            throw new AssertionError("Invalid string should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertFalse(boolean condition, String message) {
        if (condition) {
            throw new AssertionError(message);
        }
    }
}