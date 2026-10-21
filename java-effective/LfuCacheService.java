import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

public class LfuCacheService {
    private static final int MAX_SIZE = 100_000;
    private static final long EXPIRATION_SECONDS = 5;
    private static final Logger LOGGER = Logger.getLogger(LfuCacheService.class.getName());

    private final int maxSize;
    private final long expirationSeconds;
    private final Map<String, CacheEntry> store = new ConcurrentHashMap<>();
    private final AtomicLong evictionCount = new AtomicLong(0);
    private final AtomicLong totalPutTimeNanos = new AtomicLong(0);
    private final AtomicLong putOperationsCount = new AtomicLong(0);
    private final ScheduledExecutorService cleanupExecutor =
            Executors.newSingleThreadScheduledExecutor();

    public static class CacheEntry {
        private final String value;
        private final Instant creationTime;
        private volatile Instant lastAccessTime;
        private final AtomicLong frequency = new AtomicLong(1);

        public CacheEntry(String value) {
            this.value = Objects.requireNonNull(value, "value");
            this.creationTime = Instant.now();
            this.lastAccessTime = creationTime;
        }

        public String getValue() {
            return value;
        }

        public Instant getCreationTime() {
            return creationTime;
        }

        public Instant getLastAccessTime() {
            return lastAccessTime;
        }

        public void updateAccess() {
            lastAccessTime = Instant.now();
            frequency.incrementAndGet();
        }

        public long getFrequency() {
            return frequency.get();
        }
    }

    public LfuCacheService() {
        this(MAX_SIZE, EXPIRATION_SECONDS);
    }

    LfuCacheService(int maxSize, long expirationSeconds) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize must be positive");
        }
        if (expirationSeconds <= 0) {
            throw new IllegalArgumentException("expirationSeconds must be positive");
        }

        this.maxSize = maxSize;
        this.expirationSeconds = expirationSeconds;
        cleanupExecutor.scheduleAtFixedRate(
                evictExpiredEntries(), 2, 2, TimeUnit.SECONDS);
    }

    public String get(String key) {
        Objects.requireNonNull(key, "key");
        while (true) {
            CacheEntry entry = store.get(key);
            if (entry == null) {
                return null;
            }

            synchronized (entry) {
                if (store.get(key) != entry) {
                    continue;
                }

                if (isExpired(entry, Instant.now())) {
                    if (store.remove(key, entry)) {
                        evictionCount.incrementAndGet();
                        LOGGER.info("Evicted expired entry on get: " + key);
                    }
                    return null;
                }

                entry.updateAccess();
                return entry.getValue();
            }
        }
    }

    public synchronized void put(String key, String value) {
        long startTime = System.nanoTime();
        try {
            Objects.requireNonNull(key, "key");
            Objects.requireNonNull(value, "value");

            if (!store.containsKey(key) && store.size() >= maxSize) {
                evictLfu();
            }
            store.put(key, new CacheEntry(value));
        } finally {
            totalPutTimeNanos.addAndGet(System.nanoTime() - startTime);
            putOperationsCount.incrementAndGet();
        }
    }

    private void evictLfu() {
        while (true) {
            String candidateKey = null;
            CacheEntry oldestCandidate = null;

            for (Map.Entry<String, CacheEntry> mapEntry : store.entrySet()) {
                CacheEntry entry = mapEntry.getValue();
                if (oldestCandidate == null
                        || entry.getFrequency() < oldestCandidate.getFrequency()
                        || (entry.getFrequency() == oldestCandidate.getFrequency()
                        && entry.getLastAccessTime()
                        .isBefore(oldestCandidate.getLastAccessTime()))) {
                    oldestCandidate = entry;
                    candidateKey = mapEntry.getKey();
                }
            }

            if (candidateKey == null) {
                return;
            }
            if (store.remove(candidateKey, oldestCandidate)) {
                evictionCount.incrementAndGet();
                LOGGER.info("Evicted LFU entry due to capacity: " + candidateKey);
                return;
            }
        }
    }

    private Runnable evictExpiredEntries() {
        return () -> {
            Instant threshold = Instant.now().minusSeconds(expirationSeconds);
            store.forEach((key, entry) -> {
                synchronized (entry) {
                    if (entry.getLastAccessTime().isBefore(threshold)
                            && store.remove(key, entry)) {
                        evictionCount.incrementAndGet();
                        LOGGER.info("Expired entry removed: " + key);
                    }
                }
            });
        };
    }

    private boolean isExpired(CacheEntry entry, Instant now) {
        return entry.getLastAccessTime().isBefore(now.minusSeconds(expirationSeconds));
    }

    public double getAveragePutTimeMillis() {
        long count = putOperationsCount.get();
        if (count == 0) {
            return 0.0;
        }
        return totalPutTimeNanos.get() / (double) count / 1_000_000.0;
    }

    public long getEvictionCount() {
        return evictionCount.get();
    }

    public void shutdown() {
        cleanupExecutor.shutdown();
    }
}