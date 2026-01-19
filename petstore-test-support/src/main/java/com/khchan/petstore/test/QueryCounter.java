package com.khchan.petstore.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe utility for counting SQL statements during tests.
 * Tracks SELECT, INSERT, UPDATE, DELETE queries and stores query strings for debugging.
 */
public class QueryCounter {

    private static final AtomicInteger selectCount = new AtomicInteger(0);
    private static final AtomicInteger insertCount = new AtomicInteger(0);
    private static final AtomicInteger updateCount = new AtomicInteger(0);
    private static final AtomicInteger deleteCount = new AtomicInteger(0);
    private static final AtomicInteger totalCount = new AtomicInteger(0);

    private static final List<String> queries = Collections.synchronizedList(new ArrayList<>());

    private QueryCounter() {
    }

    public static void reset() {
        selectCount.set(0);
        insertCount.set(0);
        updateCount.set(0);
        deleteCount.set(0);
        totalCount.set(0);
        queries.clear();
    }

    public static void recordQuery(String query) {
        if (query == null || query.isEmpty()) {
            return;
        }

        String normalizedQuery = query.trim().toLowerCase();
        queries.add(query);
        totalCount.incrementAndGet();

        if (normalizedQuery.startsWith("select")) {
            selectCount.incrementAndGet();
        } else if (normalizedQuery.startsWith("insert")) {
            insertCount.incrementAndGet();
        } else if (normalizedQuery.startsWith("update")) {
            updateCount.incrementAndGet();
        } else if (normalizedQuery.startsWith("delete")) {
            deleteCount.incrementAndGet();
        }
    }

    public static int getSelectCount() {
        return selectCount.get();
    }

    public static int getInsertCount() {
        return insertCount.get();
    }

    public static int getUpdateCount() {
        return updateCount.get();
    }

    public static int getDeleteCount() {
        return deleteCount.get();
    }

    public static int getTotalCount() {
        return totalCount.get();
    }

    public static List<String> getQueries() {
        return new ArrayList<>(queries);
    }

    public static List<String> getSelectQueries() {
        List<String> result = new ArrayList<>();
        for (String query : queries) {
            if (query.trim().toLowerCase().startsWith("select")) {
                result.add(query);
            }
        }
        return result;
    }

    public static String getSummary() {
        return String.format(
            "Query Summary: SELECT=%d, INSERT=%d, UPDATE=%d, DELETE=%d, TOTAL=%d",
            selectCount.get(), insertCount.get(), updateCount.get(), deleteCount.get(), totalCount.get()
        );
    }

    public static String getDetailedReport() {
        StringBuilder sb = new StringBuilder();
        sb.append(getSummary()).append("\n");
        sb.append("--- Queries ---\n");
        int index = 1;
        for (String query : queries) {
            sb.append(String.format("%d. %s\n", index++, query));
        }
        return sb.toString();
    }
}
