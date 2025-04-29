package com.stasha.info.flex;

import org.openjdk.jmh.annotations.*;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 10, time = 1)
@Measurement(iterations = 10, time = 1)
@Fork(1)
public class NumberMatchKeyBenchmark {

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(NumberMatchKeyBenchmark.class.getSimpleName())
                .forks(1)
                .warmupIterations(10)
                .warmupTime(TimeValue.milliseconds(1000))
                .timeUnit(TimeUnit.NANOSECONDS)
                .mode(Mode.AverageTime)
                .build();

        new Runner(opt).run();
    }

    private NumberMatchKey exactKey;
    private NumberMatchKey prefixKey;
    private NumberMatchKey suffixKey;
    private NumberMatchKey rangeKey;
    private NumberMatchKey floorKey;
    private NumberMatchKey ceilingKey;
    private long[] testNumbers;
    private NumberMatchKey[] smallDatasetKeys;

    @Setup
    public void setup() {
        // Initialize keys
        exactKey = new NumberMatchKey(NumberMatchKey.MatchType.EXACT, 20);
        prefixKey = new NumberMatchKey(NumberMatchKey.MatchType.PREFIX, 12, 2);
        suffixKey = new NumberMatchKey(NumberMatchKey.MatchType.SUFFIX, 78, 2);
        rangeKey = new NumberMatchKey(NumberMatchKey.MatchType.RANGE, 10, 20, 0);
        floorKey = new NumberMatchKey(NumberMatchKey.MatchType.FLOOR, 20);
        ceilingKey = new NumberMatchKey(NumberMatchKey.MatchType.CEILING, 20);

        // Test numbers from small dataset
        testNumbers = new long[]{12345678L, 3278L, 5678L, 15L, 20L, 25L, 30L, 5L, 40L, 99999999L, 0L};

        // Small dataset keys (mimicking HybridNumberMatchMap)
        smallDatasetKeys = new NumberMatchKey[]{
            new NumberMatchKey("_78"),
            new NumberMatchKey("12_"),
            new NumberMatchKey("20"),
            new NumberMatchKey("~20"),
            new NumberMatchKey("20~"),
            new NumberMatchKey("10-20"),
            new NumberMatchKey("10~-30"),
            new NumberMatchKey("99999999")
        };
    }

    @Benchmark
    public void matchesExact() {
        exactKey.matches(20);
    }

    @Benchmark
    public void matchesPrefix() {
        prefixKey.matches(1234);
    }

    @Benchmark
    public void matchesSuffix() {
        suffixKey.matches(5678);
    }

    @Benchmark
    public void matchesRange() {
        rangeKey.matches(15);
    }

    @Benchmark
    public void matchesFloor() {
        floorKey.matches(19);
    }

    @Benchmark
    public void matchesCeiling() {
        ceilingKey.matches(21);
    }

    @Benchmark
    public void constructorString() {
        new NumberMatchKey("10-20");
    }

    @Benchmark
    public void constructorNumeric() {
        new NumberMatchKey(NumberMatchKey.MatchType.RANGE, 10, 20, 0);
    }

    @Benchmark
    public void smallDatasetSearch() {
        for (long num : testNumbers) {
            for (NumberMatchKey key : smallDatasetKeys) {
                if (key.matches(num)) {
                    break;
                }
            }
        }
    }

    @Benchmark
    public void largeDatasetSearch() {
        for (int i = 0; i < 1000000; i++) {
            rangeKey.matches(i);
        }
    }
}
