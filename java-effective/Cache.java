class Main {
    public static void main(String[] args) {
        LfuCacheService cache = new LfuCacheService(2, 5);
        try {
            cache.put("first", "one");
            cache.put("second", "two");
            cache.get("first");
            cache.put("third", "three");

            if (cache.get("second") != null
                    || !"one".equals(cache.get("first"))
                    || !"three".equals(cache.get("third"))) {
                throw new AssertionError("LFU eviction check failed");
            }
            if (cache.getEvictionCount() != 1
                    || cache.getAveragePutTimeMillis() <= 0.0) {
                throw new AssertionError("Cache metrics check failed");
            }
        } finally {
            cache.shutdown();
        }
    }
}