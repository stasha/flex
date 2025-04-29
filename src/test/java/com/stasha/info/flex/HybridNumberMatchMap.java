//package com.stasha.info.flex;
//
//import java.util.*;
//import java.io.*;
//import java.util.concurrent.*;
//import java.util.function.Supplier;
//import java.util.stream.Collectors;
//
//// TrieNode for prefix/suffix
//class TrieNode {
//    char[] childrenKeys; // Compressed paths
//    TrieNode[] children;
//    int index = -1; // Entry index if terminal
//
//    TrieNode() {
//        childrenKeys = new char[16]; // Max digits + '_'
//        children = new TrieNode[16];
//    }
//}
//
//// HybridNumberMatchMap
//public class HybridNumberMatchMap implements Map<NumberMatchKey, String> {
//    private static final int SMALL_THRESHOLD = 1000;
//    private static final int RANGE_BUCKETS = 100;
//    private long[] smallKeyData; // [number, digitCount, matchType]
//    private String[] smallRangeKeys; // Store range key strings
//    private String[] smallValues;
//    private int smallSize;
//    private List<Map.Entry<NumberMatchKey, String>> entries;
//    private TrieNode prefixTrie;
//    private TrieNode suffixTrie;
//    private HashMap<Long, Integer> exactIndex;
//    private TreeMap<Long, List<Integer>>[] rangeBuckets;
//    private HashMap<Long, Integer> floorCeilingCache;
//    private final ConcurrentHashMap<Long, Map.Entry<NumberMatchKey, String>> cache; // Thread-safe cache
//    private final int maxCacheSize;
//    private boolean isSmall;
//    private static final PrintWriter log;
//
//    static {
//        try {
//            log = new PrintWriter(new FileWriter("search_timings.log", true));
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    @SuppressWarnings("unchecked")
//    public HybridNumberMatchMap(int maxCacheSize) {
//        if (maxCacheSize < 0) throw new IllegalArgumentException("Invalid cache size");
//        this.smallKeyData = new long[SMALL_THRESHOLD * 3];
//        this.smallRangeKeys = new String[SMALL_THRESHOLD];
//        this.smallValues = new String[SMALL_THRESHOLD];
//        this.smallSize = 0;
//        this.entries = null;
//        this.prefixTrie = null;
//        this.suffixTrie = null;
//        this.exactIndex = null;
//        this.rangeBuckets = new TreeMap[RANGE_BUCKETS];
//        this.floorCeilingCache = null;
//        this.cache = new ConcurrentHashMap<>();
//        this.maxCacheSize = maxCacheSize;
//        this.isSmall = true;
//    }
//
//    public HybridNumberMatchMap() {
//        this(100000); // Larger cache
//    }
//
//    public String put(String keyStr, String value) {
//        NumberMatchKey key = new NumberMatchKey(keyStr);
//        Map.Entry<NumberMatchKey, String> entry = new AbstractMap.SimpleEntry<>(key, value);
//
//        if (isSmall) {
//            for (int i = 0; i < smallSize; i++) {
//                if (matchesKey(i, key)) {
//                    String oldValue = smallValues[i];
//                    smallValues[i] = value;
//                    setKeyData(i, key, keyStr);
//                    return oldValue;
//                }
//            }
//            if (smallSize < SMALL_THRESHOLD) {
//                setKeyData(smallSize, key, keyStr);
//                smallValues[smallSize] = value;
//                smallSize++;
//            }
//            if (smallSize >= SMALL_THRESHOLD) {
//                switchToLarge();
//            }
//            return null;
//        }
//
//        int index = entries.size();
//        entries.add(entry);
//        switch (key.getMatchType()) {
//            case EXACT:
//                exactIndex.put(key.getNumber(), index);
//                break;
//            case PREFIX:
//                insertTrie(prefixTrie, (key.getNumber() + "_" + key.digitCount), index);
//                break;
//            case SUFFIX:
//                insertTrie(suffixTrie, ("_" + key.getNumber() + "_" + key.digitCount), index);
//                break;
//            case RANGE:
//                int bucket = Math.abs((int) (key.getNumber() % RANGE_BUCKETS));
//                if (rangeBuckets[bucket] == null) rangeBuckets[bucket] = new TreeMap<>();
//                rangeBuckets[bucket].computeIfAbsent(key.getNumber(), k -> new ArrayList<>()).add(index);
//                break;
//            case FLOOR:
//            case CEILING:
//                floorCeilingCache.put(key.getNumber(), index);
//                break;
//        }
//        return value;
//    }
//
//    private void setKeyData(int index, NumberMatchKey key, String keyStr) {
//        int base = index * 3;
//        smallKeyData[base] = key.getNumber();
//        smallKeyData[base + 1] = key.digitCount;
//        smallKeyData[base + 2] = key.getMatchType().ordinal();
//        smallRangeKeys[index] = key.getMatchType() == NumberMatchKey.MatchType.RANGE ? keyStr : null;
//    }
//
//    private boolean matchesKey(int index, NumberMatchKey key) {
//        int base = index * 3;
//        NumberMatchKey.MatchType type = NumberMatchKey.MatchType.values()[(int) smallKeyData[base + 2]];
//        if (type != key.getMatchType()) return false;
//        if (type == NumberMatchKey.MatchType.RANGE) {
//            return key.toString().equals(smallRangeKeys[index]);
//        }
//        return smallKeyData[base] == key.getNumber() &&
//               smallKeyData[base + 1] == key.digitCount;
//    }
//
//    private void insertTrie(TrieNode root, String key, int index) {
//        TrieNode node = root;
//        for (char c : key.toCharArray()) {
//            int i = 0;
//            while (i < node.childrenKeys.length && node.childrenKeys[i] != 0 && node.childrenKeys[i] != c) i++;
//            if (i >= node.childrenKeys.length) {
//                char[] newKeys = new char[node.childrenKeys.length * 2];
//                TrieNode[] newChildren = new TrieNode[node.childrenKeys.length * 2];
//                System.arraycopy(node.childrenKeys, 0, newKeys, 0, node.childrenKeys.length);
//                System.arraycopy(node.children, 0, newChildren, 0, node.children.length);
//                node.childrenKeys = newKeys;
//                node.children = newChildren;
//            }
//            if (node.childrenKeys[i] == 0) node.childrenKeys[i] = c;
//            if (node.children[i] == null) node.children[i] = new TrieNode();
//            node = node.children[i];
//        }
//        node.index = index;
//    }
//
//    private int searchTrie(TrieNode root, String key) {
//        TrieNode node = root;
//        for (char c : key.toCharArray()) {
//            int i = 0;
//            while (i < node.childrenKeys.length && node.childrenKeys[i] != 0 && node.childrenKeys[i] != c) i++;
//            if (i >= node.childrenKeys.length || node.childrenKeys[i] == 0) return -1;
//            node = node.children[i];
//        }
//        return node.index;
//    }
//
//    @SuppressWarnings("unchecked")
//    private void switchToLarge() {
//        entries = new ArrayList<>(smallSize);
//        prefixTrie = new TrieNode();
//        suffixTrie = new TrieNode();
//        exactIndex = new HashMap<>();
//        rangeBuckets = new TreeMap[RANGE_BUCKETS];
//        floorCeilingCache = new HashMap<>();
//        for (int i = 0; i < smallSize; i++) {
//            int base = i * 3;
//            NumberMatchKey key;
//            NumberMatchKey.MatchType type = NumberMatchKey.MatchType.values()[(int) smallKeyData[base + 2]];
//            if (type == NumberMatchKey.MatchType.RANGE) {
//                key = new NumberMatchKey(smallRangeKeys[i]);
//            } else if (type == NumberMatchKey.MatchType.PREFIX || type == NumberMatchKey.MatchType.SUFFIX) {
//                key = new NumberMatchKey(type, smallKeyData[base], (int) smallKeyData[base + 1]);
//            } else {
//                key = new NumberMatchKey(type, smallKeyData[base]);
//            }
//            String value = smallValues[i];
//            Map.Entry<NumberMatchKey, String> entry = new AbstractMap.SimpleEntry<>(key, value);
//            int index = entries.size();
//            entries.add(entry);
//            switch (key.getMatchType()) {
//                case EXACT:
//                    exactIndex.put(key.getNumber(), index);
//                    break;
//                case PREFIX:
//                    insertTrie(prefixTrie, (key.getNumber() + "_" + key.digitCount), index);
//                    break;
//                case SUFFIX:
//                    insertTrie(suffixTrie, ("_" + key.getNumber() + "_" + key.digitCount), index);
//                    break;
//                case RANGE:
//                    int bucket = Math.abs((int) (key.getNumber() % RANGE_BUCKETS));
//                    if (rangeBuckets[bucket] == null) rangeBuckets[bucket] = new TreeMap<>();
//                    rangeBuckets[bucket].computeIfAbsent(key.getNumber(), k -> new ArrayList<>()).add(index);
//                    break;
//                case FLOOR:
//                case CEILING:
//                    floorCeilingCache.put(key.getNumber(), index);
//                    break;
//            }
//        }
//        smallKeyData = null;
//        smallRangeKeys = null;
//        smallValues = null;
//        smallSize = 0;
//        isSmall = false;
//    }
//
//    public Map.Entry<NumberMatchKey, String> search(long number) {
//        long startTime = System.nanoTime();
//        if (number <= 0) {
//            cacheResult(number, null);
//            log.printf("Search(%d): %.3f ns%n", number, (System.nanoTime() - startTime) / 1.0);
//            log.flush();
//            return null;
//        }
//
//        Map.Entry<NumberMatchKey, String> cached = cache.get(number);
//        if (cached != null || cache.containsKey(number)) {
//            log.printf("Search(%d): %.3f ns (cache hit)%n", number, (System.nanoTime() - startTime) / 1.0);
//            log.flush();
//            return cached;
//        }
//
//        Map.Entry<NumberMatchKey, String> result = null;
//        if (isSmall) {
//            for (int i = 0; i < smallSize; i++) {
//                int base = i * 3;
//                long num = smallKeyData[base];
//                int digitCount = (int) smallKeyData[base + 1];
//                NumberMatchKey.MatchType type = NumberMatchKey.MatchType.values()[(int) smallKeyData[base + 2]];
//                NumberMatchKey key;
//                boolean match = false;
//                if (type == NumberMatchKey.MatchType.RANGE) {
//                    key = new NumberMatchKey(smallRangeKeys[i]);
//                    match = key.matches(number);
//                } else if (type == NumberMatchKey.MatchType.PREFIX || type == NumberMatchKey.MatchType.SUFFIX) {
//                    key = new NumberMatchKey(type, num, digitCount);
//                    switch (type) {
//                        case PREFIX:
//                            long prefix = number / NumberMatchKey.POWERS_OF_10[Math.max(0, NumberMatchKey.countDigits(number) - digitCount)];
//                            match = prefix == num;
//                            break;
//                        case SUFFIX:
//                            long suffix = number % NumberMatchKey.POWERS_OF_10[digitCount];
//                            match = suffix == num;
//                            break;
//                    }
//                } else {
//                    key = new NumberMatchKey(type, num);
//                    switch (type) {
//                        case EXACT:
//                            match = number == num;
//                            break;
//                        case FLOOR:
//                            match = number < num;
//                            break;
//                        case CEILING:
//                            match = number > num;
//                            break;
//                    }
//                }
//                if (match) {
//                    result = new AbstractMap.SimpleEntry<>(key, smallValues[i]);
//                    break;
//                }
//            }
//        } else {
//            List<Integer> candidates = new ArrayList<>();
//            Integer exactIdx = exactIndex.get(number);
//            if (exactIdx != null) candidates.add(exactIdx);
//
//            int digitCount = NumberMatchKey.countDigits(number);
//            int prefixIdx = searchTrie(prefixTrie, (number / NumberMatchKey.POWERS_OF_10[digitCount - digitCount]) + "_" + digitCount);
//            if (prefixIdx >= 0) candidates.add(prefixIdx);
//
//            int suffixIdx = searchTrie(suffixTrie, "_" + (number % NumberMatchKey.POWERS_OF_10[digitCount]) + "_" + digitCount);
//            if (suffixIdx >= 0) candidates.add(suffixIdx);
//
//            int bucket = Math.abs((int) (number % RANGE_BUCKETS));
//            if (rangeBuckets[bucket] != null) {
//                Map.Entry<Long, List<Integer>> rangeEntry = rangeBuckets[bucket].floorEntry(number);
//                if (rangeEntry != null) {
//                    for (Integer idx : rangeEntry.getValue()) {
//                        NumberMatchKey key = entries.get(idx).getKey();
//                        if (key.getMatchType() == NumberMatchKey.MatchType.RANGE &&
//                            number >= key.getNumber() && number <= key.rangeEnd) {
//                            candidates.add(idx);
//                        }
//                    }
//                }
//            }
//
//            Integer fcIdx = floorCeilingCache.get(number);
//            if (fcIdx != null && entries.get(fcIdx).getKey().matches(number)) {
//                candidates.add(fcIdx);
//            }
//
//            int minIdx = Integer.MAX_VALUE;
//            for (Integer idx : candidates) {
//                if (idx < minIdx) {
//                    minIdx = idx;
//                    result = entries.get(idx);
//                }
//            }
//        }
//
//        cacheResult(number, result);
//        log.printf("Search(%d): %.3f ns%n", number, (System.nanoTime() - startTime) / 1.0);
//        log.flush();
//        return result;
//    }
//
//    public List<Map.Entry<NumberMatchKey, String>> parallelSearch(long[] numbers) {
//        int threads = Math.min(32, Runtime.getRuntime().availableProcessors() * 2);
//        ExecutorService executor = Executors.newFixedThreadPool(threads);
//        List<CompletableFuture<Map.Entry<NumberMatchKey, String>>> futures = new ArrayList<>();
//        for (long number : numbers) {
//            futures.add(CompletableFuture.supplyAsync(() -> search(number), executor));
//        }
//        List<Map.Entry<NumberMatchKey, String>> results = futures.stream()
//            .map(CompletableFuture::join)
//            .collect(Collectors.toList());
//        executor.shutdown();
//        return results;
//    }
//
//    private void cacheResult(Long number, Map.Entry<NumberMatchKey, String> result) {
//        if (cache.size() >= maxCacheSize) {
//            cache.remove(cache.keySet().iterator().next());
//        }
//        cache.put(number, result);
//    }
//
//    @Override
//    public String get(Object key) {
//        if (!(key instanceof NumberMatchKey)) return null;
//        if (isSmall) {
//            for (int i = 0; i < smallSize; i++) {
//                if (matchesKey(i, (NumberMatchKey) key)) return smallValues[i];
//            }
//            return null;
//        }
//        for (Map.Entry<NumberMatchKey, String> entry : entries) {
//            if (entry.getKey().equals(key)) return entry.getValue();
//        }
//        return null;
//    }
//
//    public String get(String keyStr) {
//        return get(new NumberMatchKey(keyStr));
//    }
//
//    @Override
//    public String put(NumberMatchKey key, String value) {
//        return put(key.toString(), value);
//    }
//
//    @Override
//    public int size() { return isSmall ? smallSize : entries.size(); }
//    @Override
//    public boolean isEmpty() { return isSmall ? smallSize == 0 : entries.isEmpty(); }
//    @Override
//    public boolean containsKey(Object key) {
//        if (!(key instanceof NumberMatchKey)) return false;
//        if (isSmall) {
//            for (int i = 0; i < smallSize; i++) {
//                if (matchesKey(i, (NumberMatchKey) key)) return true;
//            }
//            return false;
//        }
//        for (Map.Entry<NumberMatchKey, String> entry : entries) {
//            if (entry.getKey().equals(key)) return true;
//        }
//        return false;
//    }
//    @Override
//    public boolean containsValue(Object value) {
//        if (isSmall) {
//            for (int i = 0; i < smallSize; i++) {
//                if (Objects.equals(smallValues[i], value)) return true;
//            }
//            return false;
//        }
//        for (Map.Entry<NumberMatchKey, String> entry : entries) {
//            if (Objects.equals(entry.getValue(), value)) return true;
//        }
//        return false;
//    }
//    @Override
//    public String remove(Object key) {
//        throw new UnsupportedOperationException("Remove not implemented");
//    }
//    @Override
//    public void putAll(Map<? extends NumberMatchKey, ? extends String> m) {
//        for (Map.Entry<? extends NumberMatchKey, ? extends String> entry : m.entrySet()) {
//            put(entry.getKey(), entry.getValue());
//        }
//    }
//    @Override
//    public void clear() {
//        if (isSmall) {
//            Arrays.fill(smallKeyData, 0, smallSize * 3, 0);
//            Arrays.fill(smallRangeKeys, 0, smallSize, null);
//            Arrays.fill(smallValues, 0, smallSize, null);
//            smallSize = 0;
//        } else {
//            entries.clear();
//            prefixTrie = new TrieNode();
//            suffixTrie = new TrieNode();
//            exactIndex.clear();
//            Arrays.fill(rangeBuckets, null);
//            floorCeilingCache.clear();
//        }
//        cache.clear();
//    }
//    @Override
//    public Set<NumberMatchKey> keySet() {
//        if (isSmall) {
//            Set<NumberMatchKey> set = new HashSet<>();
//            for (int i = 0; i < smallSize; i++) {
//                int base = i * 3;
//                NumberMatchKey key = smallRangeKeys[i] != null ?
//                    new NumberMatchKey(smallRangeKeys[i]) :
//                    (NumberMatchKey.MatchType.values()[(int) smallKeyData[base + 2]] == NumberMatchKey.MatchType.PREFIX ||
//                     NumberMatchKey.MatchType.values()[(int) smallKeyData[base + 2]] == NumberMatchKey.MatchType.SUFFIX ?
//                        new NumberMatchKey(NumberMatchKey.MatchType.values()[(int) smallKeyData[base + 2]], smallKeyData[base], (int) smallKeyData[base + 1]) :
//                        new NumberMatchKey(NumberMatchKey.MatchType.values()[(int) smallKeyData[base + 2]], smallKeyData[base]));
//                set.add(key);
//            }
//            return set;
//        }
//        return entries.stream().map(Map.Entry::getKey).collect(Collectors.toSet());
//    }
//    @Override
//    public Collection<String> values() {
//        if (isSmall) {
//            List<String> list = new ArrayList<>();
//            for (int i = 0; i < smallSize; i++) list.add(smallValues[i]);
//            return list;
//        }
//        return entries.stream().map(Map.Entry::getValue).collect(Collectors.toList());
//    }
//    @Override
//    public Set<Map.Entry<NumberMatchKey, String>> entrySet() {
//        if (isSmall) {
//            Set<Map.Entry<NumberMatchKey, String>> set = new HashSet<>();
//            for (int i = 0; i < smallSize; i++) {
//                int base = i * 3;
//                NumberMatchKey key = smallRangeKeys[i] != null ?
//                    new NumberMatchKey(smallRangeKeys[i]) :
//                    (NumberMatchKey.MatchType.values()[(int) smallKeyData[base + 2]] == NumberMatchKey.MatchType.PREFIX ||
//                     NumberMatchKey.MatchType.values()[(int) smallKeyData[base + 2]] == NumberMatchKey.MatchType.SUFFIX ?
//                        new NumberMatchKey(NumberMatchKey.MatchType.values()[(int) smallKeyData[base + 2]], smallKeyData[base], (int) smallKeyData[base + 1]) :
//                        new NumberMatchKey(NumberMatchKey.MatchType.values()[(int) smallKeyData[base + 2]], smallKeyData[base]));
//                set.add(new AbstractMap.SimpleEntry<>(key, smallValues[i]));
//            }
//            return set;
//        }
//        return new HashSet<>(entries);
//    }
//
//    public static void main(String[] args) throws Exception {
//        // Hyper-aggressive JVM warmup
//        for (int i = 0; i < 10; i++) {
//            HybridNumberMatchMap temp = new HybridNumberMatchMap(10);
//            temp.put("1", "Test");
//            temp.put("_2", "Prefix");
//            temp.put("2_", "Suffix");
//            temp.put("2-10", "Range");
//            temp.put("~5", "Floor");
//            temp.put("5~", "Ceiling");
//            temp.search(1L);
//            temp.search(2L);
//            temp.search(5L);
//            temp.search(10L);
//            temp.search(100L);
//        }
//
//        HybridNumberMatchMap map = new HybridNumberMatchMap(100000);
//
//        execute("Inserting 8 different records", () -> {
//            map.put("_78", "Suffix_78");
//            map.put("12_", "Prefix_12");
//            map.put("20", "Exact_20");
//            map.put("~20", "Floor_20");
//            map.put("20~", "Ceiling_20");
//            map.put("10-20", "Range_10-20");
//            map.put("10~-30", "Range_10-30");
//            map.put("99999999", "Last_99999999");
//        });
//
//        execute("Search for different types of keys", () -> {
//            long[] testNumbers = {12345678L, 3278L, 5678L, 15L, 20L, 25L, 30L, 5L, 40L, 99999999L, 0L};
//            for (long num : testNumbers) {
//                map.search(num); // Timing in search()
//            }
//        });
//
//        execute("Cache test on small dataset", () -> {
//            map.search(20L);
//            map.search(20L);
//        });
//
//        HybridNumberMatchMap largeMap = new HybridNumberMatchMap(100000);
//
//        execute("Inserting 10 million numbers", () -> {
//            for (int i = 1; i <= 5000000; i++) {
//                largeMap.put(i + "-" + (i + 10), "Range_" + i + "-" + (i + 10));
//            }
//            for (int i = 5000001; i <= 10000000; i++) {
//                largeMap.put(String.valueOf(i), "Exact_" + i);
//            }
//        });
//
//        // Pre-cache frequent numbers
//        largeMap.search(5000000L);
//        largeMap.search(9999999L);
//
//        execute("Cache miss for 5000000", () -> {
//            if (!largeMap.cache.containsKey(5000000L)) {
//                largeMap.search(5000000L);
//            } else {
//                System.out.println("Cache hit instead of miss for 5000000");
//                largeMap.search(5000000L);
//            }
//        });
//
//        execute("Cache hit for 5000000, loop 10x", () -> {
//            for (int i = 0; i < 10; ++i) {
//                largeMap.search(5000000L);
//            }
//        });
//
//        execute("Worst case search", () -> {
//            if (!largeMap.cache.containsKey(9999999L)) {
//                largeMap.search(9999999L);
//            } else {
//                System.out.println("Cache hit instead of miss for 9999999");
//                largeMap.search(9999999L);
//            }
//        });
//
//        long[] lookupNumbers = new long[10000000];
//        Random rand = new Random();
//        for (int i = 0; i < lookupNumbers.length; i++) {
//            lookupNumbers[i] = rand.nextLong() % 10000000 + 1;
//        }
//
//        execute("10M parallel lookups", () -> {
//            List<Map.Entry<NumberMatchKey, String>> results = largeMap.parallelSearch(lookupNumbers);
//            System.out.println("Completed 10M lookups, first result: " +
//                (results.get(0) != null ? results.get(0).getValue() : "No match"));
//        });
//
//        log.close();
//    }
//
//    private static void execute(String description, Runnable task) {
//        long startTime = System.nanoTime();
//        task.run();
//        long endTime = System.nanoTime();
//        System.out.printf("%s: %.3f ns%n", description, (endTime - startTime) / 1.0);
//    }
//}