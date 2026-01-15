package com.khchan.petstore.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe utility for tracking transaction events (commit/rollback) during tests.
 * Works seamlessly with @Transactional and TransactionTemplate - no app code changes needed.
 */
public class TransactionTracker {

    public enum TransactionEvent {
        BEGIN,
        COMMIT,
        ROLLBACK
    }

    public static class TransactionRecord {
        private final TransactionEvent event;
        private final long timestamp;
        private final String threadName;

        public TransactionRecord(TransactionEvent event) {
            this.event = event;
            this.timestamp = System.currentTimeMillis();
            this.threadName = Thread.currentThread().getName();
        }

        public TransactionEvent getEvent() {
            return event;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public String getThreadName() {
            return threadName;
        }

        @Override
        public String toString() {
            return String.format("[%s] %s on thread '%s'", timestamp, event, threadName);
        }
    }

    private static final AtomicInteger commitCount = new AtomicInteger(0);
    private static final AtomicInteger rollbackCount = new AtomicInteger(0);
    private static final AtomicInteger beginCount = new AtomicInteger(0);

    private static final List<TransactionRecord> events = Collections.synchronizedList(new ArrayList<>());

    private TransactionTracker() {
    }

    public static void reset() {
        commitCount.set(0);
        rollbackCount.set(0);
        beginCount.set(0);
        events.clear();
    }

    public static void recordBegin() {
        beginCount.incrementAndGet();
        events.add(new TransactionRecord(TransactionEvent.BEGIN));
    }

    public static void recordCommit() {
        commitCount.incrementAndGet();
        events.add(new TransactionRecord(TransactionEvent.COMMIT));
    }

    public static void recordRollback() {
        rollbackCount.incrementAndGet();
        events.add(new TransactionRecord(TransactionEvent.ROLLBACK));
    }

    public static int getCommitCount() {
        return commitCount.get();
    }

    public static int getRollbackCount() {
        return rollbackCount.get();
    }

    public static int getBeginCount() {
        return beginCount.get();
    }

    public static List<TransactionRecord> getEvents() {
        return new ArrayList<>(events);
    }

    public static List<TransactionEvent> getEventTypes() {
        List<TransactionEvent> result = new ArrayList<>();
        for (TransactionRecord record : events) {
            result.add(record.getEvent());
        }
        return result;
    }

    public static String getSummary() {
        return String.format(
            "Transaction Summary: BEGIN=%d, COMMIT=%d, ROLLBACK=%d",
            beginCount.get(), commitCount.get(), rollbackCount.get()
        );
    }

    public static String getDetailedReport() {
        StringBuilder sb = new StringBuilder();
        sb.append(getSummary()).append("\n");
        sb.append("--- Transaction Events ---\n");
        int index = 1;
        for (TransactionRecord record : events) {
            sb.append(String.format("%d. %s\n", index++, record));
        }
        return sb.toString();
    }

    public static boolean hasCommitted() {
        return commitCount.get() > 0;
    }

    public static boolean hasRolledBack() {
        return rollbackCount.get() > 0;
    }

    public static boolean allTransactionsCommitted() {
        return beginCount.get() > 0 && beginCount.get() == commitCount.get() && rollbackCount.get() == 0;
    }

    public static boolean allTransactionsRolledBack() {
        return beginCount.get() > 0 && beginCount.get() == rollbackCount.get() && commitCount.get() == 0;
    }
}
