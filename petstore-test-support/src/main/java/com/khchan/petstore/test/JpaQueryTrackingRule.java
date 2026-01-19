package com.khchan.petstore.test;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 Extension for tracking and asserting JPA/SQL query counts and transaction events.
 *
 * Usage:
 * <pre>
 * {@code
 * @RegisterExtension
 * JpaQueryTrackingRule queryTracking = new JpaQueryTrackingRule();
 *
 * @Test
 * public void testNoNPlusOne() {
 *     // Execute code that makes queries
 *     List<Pet> pets = repository.findAll();
 *     pets.forEach(p -> p.getTags().size());
 *
 *     // Assert query counts
 *     queryTracking.assertSelectCount(2); // Should be 2, not N+1
 *     queryTracking.assertNoNPlusOne(2);  // Fails if more than 2 selects
 * }
 * }
 * </pre>
 */
public class JpaQueryTrackingRule implements BeforeEachCallback, AfterEachCallback {

    private boolean printQueriesOnFailure = true;
    private boolean printQueriesAlways = false;

    public JpaQueryTrackingRule() {
    }

    public JpaQueryTrackingRule printQueriesOnFailure(boolean enabled) {
        this.printQueriesOnFailure = enabled;
        return this;
    }

    public JpaQueryTrackingRule printQueriesAlways(boolean enabled) {
        this.printQueriesAlways = enabled;
        return this;
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        // Reset counters before each test
        QueryCounter.reset();
        TransactionTracker.reset();
    }

    @Override
    public void afterEach(ExtensionContext context) {
        if (printQueriesAlways) {
            System.out.println("\n" + QueryCounter.getDetailedReport());
            System.out.println(TransactionTracker.getDetailedReport());
        }

        // Check if test failed and print debug info
        if (printQueriesOnFailure && context.getExecutionException().isPresent()) {
            System.err.println("\n=== TEST FAILED: Query and Transaction Report ===");
            System.err.println(QueryCounter.getDetailedReport());
            System.err.println(TransactionTracker.getDetailedReport());
            System.err.println("=================================================\n");
        }
    }

    // ========== Query Count Assertions ==========

    public void assertSelectCount(int expected) {
        assertEquals(
            expected,
            QueryCounter.getSelectCount(),
            formatMessage("SELECT count", expected, QueryCounter.getSelectCount())
        );
    }

    public void assertSelectCountAtMost(int maxExpected) {
        int actual = QueryCounter.getSelectCount();
        if (actual > maxExpected) {
            fail(String.format(
                "Expected at most %d SELECT queries but got %d.%s",
                maxExpected, actual, getQueryHint()
            ));
        }
    }

    public void assertSelectCountAtLeast(int minExpected) {
        int actual = QueryCounter.getSelectCount();
        if (actual < minExpected) {
            fail(String.format(
                "Expected at least %d SELECT queries but got %d.%s",
                minExpected, actual, getQueryHint()
            ));
        }
    }

    public void assertInsertCount(int expected) {
        assertEquals(
            expected,
            QueryCounter.getInsertCount(),
            formatMessage("INSERT count", expected, QueryCounter.getInsertCount())
        );
    }

    public void assertUpdateCount(int expected) {
        assertEquals(
            expected,
            QueryCounter.getUpdateCount(),
            formatMessage("UPDATE count", expected, QueryCounter.getUpdateCount())
        );
    }

    public void assertDeleteCount(int expected) {
        assertEquals(
            expected,
            QueryCounter.getDeleteCount(),
            formatMessage("DELETE count", expected, QueryCounter.getDeleteCount())
        );
    }

    public void assertTotalQueryCount(int expected) {
        assertEquals(
            expected,
            QueryCounter.getTotalCount(),
            formatMessage("Total query count", expected, QueryCounter.getTotalCount())
        );
    }

    /**
     * Asserts that no N+1 query problem exists by checking if SELECT count exceeds the expected maximum.
     *
     * @param maxExpectedSelects The maximum number of SELECT queries expected for efficient loading.
     *                           For example, if loading pets with tags, expect 2 SELECTs (one for pets, one for tags).
     */
    public void assertNoNPlusOne(int maxExpectedSelects) {
        int actual = QueryCounter.getSelectCount();
        if (actual > maxExpectedSelects) {
            List<String> selectQueries = QueryCounter.getSelectQueries();
            StringBuilder sb = new StringBuilder();
            sb.append(String.format(
                "N+1 QUERY PROBLEM DETECTED! Expected at most %d SELECT queries but got %d.\n",
                maxExpectedSelects, actual
            ));
            sb.append("SELECT queries executed:\n");
            for (int i = 0; i < selectQueries.size(); i++) {
                sb.append(String.format("  %d. %s\n", i + 1, selectQueries.get(i)));
            }
            sb.append("\nConsider using JOIN FETCH, @EntityGraph, or batch fetching to fix this.");
            fail(sb.toString());
        }
    }

    // ========== Transaction Assertions ==========

    public void assertCommitCount(int expected) {
        assertEquals(
            expected,
            TransactionTracker.getCommitCount(),
            formatMessage("COMMIT count", expected, TransactionTracker.getCommitCount())
        );
    }

    public void assertRollbackCount(int expected) {
        assertEquals(
            expected,
            TransactionTracker.getRollbackCount(),
            formatMessage("ROLLBACK count", expected, TransactionTracker.getRollbackCount())
        );
    }

    public void assertTransactionCommitted() {
        assertTrue(
            TransactionTracker.hasCommitted(),
            "Expected at least one transaction to commit, but none did.\n" + TransactionTracker.getSummary()
        );
    }

    public void assertTransactionRolledBack() {
        assertTrue(
            TransactionTracker.hasRolledBack(),
            "Expected at least one transaction to rollback, but none did.\n" + TransactionTracker.getSummary()
        );
    }

    public void assertNoRollback() {
        assertFalse(
            TransactionTracker.hasRolledBack(),
            "Expected no rollbacks, but rollback occurred.\n" + TransactionTracker.getSummary()
        );
    }

    public void assertAllTransactionsCommitted() {
        assertTrue(
            TransactionTracker.allTransactionsCommitted(),
            "Expected all transactions to commit.\n" + TransactionTracker.getDetailedReport()
        );
    }

    // ========== Utility Methods ==========

    /**
     * Manually reset counters. Useful if you want to reset in the middle of a test.
     */
    public void reset() {
        QueryCounter.reset();
        TransactionTracker.reset();
    }

    /**
     * Reset only query counters, keeping transaction tracking.
     */
    public void resetQueryCounters() {
        QueryCounter.reset();
    }

    /**
     * Reset only transaction tracking, keeping query counters.
     */
    public void resetTransactionTracking() {
        TransactionTracker.reset();
    }

    public int getSelectCount() {
        return QueryCounter.getSelectCount();
    }

    public int getInsertCount() {
        return QueryCounter.getInsertCount();
    }

    public int getUpdateCount() {
        return QueryCounter.getUpdateCount();
    }

    public int getDeleteCount() {
        return QueryCounter.getDeleteCount();
    }

    public int getCommitCount() {
        return TransactionTracker.getCommitCount();
    }

    public int getRollbackCount() {
        return TransactionTracker.getRollbackCount();
    }

    public List<String> getQueries() {
        return QueryCounter.getQueries();
    }

    public List<String> getSelectQueries() {
        return QueryCounter.getSelectQueries();
    }

    public String getQuerySummary() {
        return QueryCounter.getSummary();
    }

    public String getTransactionSummary() {
        return TransactionTracker.getSummary();
    }

    public void printReport() {
        System.out.println(QueryCounter.getDetailedReport());
        System.out.println(TransactionTracker.getDetailedReport());
    }

    private String formatMessage(String metric, int expected, int actual) {
        return String.format("%s mismatch: expected %d but was %d.%s", metric, expected, actual, getQueryHint());
    }

    private String getQueryHint() {
        return "\n" + QueryCounter.getSummary() + "\nUse printReport() to see all queries.";
    }
}
