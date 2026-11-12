import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

public class PharmacyRedesignValidation {

    static class Medicine {
        final String code;
        final String name;
        int stock;

        Medicine(String code, String name, int stock) {
            this.code = code;
            this.name = name;
            this.stock = stock;
        }
    }

    // ORIGINAL SOLUTION (from Q4): Single LinkedHashMap that must copy & sort all keys for Code-Order Report
    static class OriginalPharmacySystem {
        private final Map<String, Medicine> store = new LinkedHashMap<>();

        void addOrUpdate(Medicine m) {
            store.put(m.code, m);
        }

        Medicine lookup(String code) {
            return store.get(code);
        }

        List<Medicine> generateCodeOrderReport() {
            List<String> keys = new ArrayList<>(store.keySet());
            keys.sort(Comparator.naturalOrder()); // Bottleneck: O(n log n) sort + O(n) key copy on every report
            List<Medicine> report = new ArrayList<>(keys.size());
            for (String k : keys) {
                report.add(store.get(k));
            }
            return report;
        }

        List<Medicine> generateAdditionOrderReport() {
            return new ArrayList<>(store.values());
        }
    }

    // REDESIGNED SOLUTION: Dual-Index Synchronization (LinkedHashMap primary + TreeMap sorted secondary index)
    static class RedesignedPharmacySystem {
        private final Map<String, Medicine> primaryIndex = new LinkedHashMap<>();
        private final TreeMap<String, Medicine> sortedCodeIndex = new TreeMap<>();

        void addOrUpdate(Medicine m) {
            primaryIndex.put(m.code, m);
            sortedCodeIndex.put(m.code, m);
        }

        Medicine lookup(String code) {
            return primaryIndex.get(code); // Still O(1) hash lookup!
        }

        List<Medicine> generateCodeOrderReport() {
            // Eliminates sorting and secondary get() lookups: O(n) in-order traversal
            return new ArrayList<>(sortedCodeIndex.values());
        }

        List<Medicine> generateAdditionOrderReport() {
            return new ArrayList<>(primaryIndex.values());
        }
    }

    public static void main(String[] args) {
        int[] sizes = {5_000, 25_000, 100_000};
        Random rng = new Random(2026);

        for (int n : sizes) {
            List<Medicine> dataset = new ArrayList<>(n);
            for (int i = 0; i < n; i++) {
                dataset.add(new Medicine(String.format("MED-%07d", rng.nextInt(n * 20)), "Med_" + i, 100));
            }

            OriginalPharmacySystem orig = new OriginalPharmacySystem();
            RedesignedPharmacySystem redesign = new RedesignedPharmacySystem();

            long t0 = System.nanoTime();
            for (Medicine m : dataset) orig.addOrUpdate(m);
            long origInsertNs = System.nanoTime() - t0;

            t0 = System.nanoTime();
            for (Medicine m : dataset) redesign.addOrUpdate(m);
            long redInsertNs = System.nanoTime() - t0;

            // Measure 100,000 lookups
            String[] queries = new String[100_000];
            for (int i = 0; i < queries.length; i++) queries[i] = dataset.get(rng.nextInt(n)).code;

            long sink = 0;
            t0 = System.nanoTime();
            for (String q : queries) sink += orig.lookup(q).stock;
            long origLookupNs = System.nanoTime() - t0;

            t0 = System.nanoTime();
            for (String q : queries) sink += redesign.lookup(q).stock;
            long redLookupNs = System.nanoTime() - t0;

            // Measure 20 Code-Order Reports
            t0 = System.nanoTime();
            for (int r = 0; r < 20; r++) sink += orig.generateCodeOrderReport().size();
            long origReportNs = System.nanoTime() - t0;

            t0 = System.nanoTime();
            for (int r = 0; r < 20; r++) sink += redesign.generateCodeOrderReport().size();
            long redReportNs = System.nanoTime() - t0;

            if (sink == -1) System.out.print("");

            System.out.println("=========================================================================");
            System.out.printf("DESIGN CHALLENGE VALIDATION | Inventory Size: %,d medicines%n", n);
            System.out.println("=========================================================================");
            System.out.printf("Bulk Ingestion (%,d puts)    -> Original: %7.3f ms | Redesigned: %7.3f ms%n",
                    n, origInsertNs / 1e6, redInsertNs / 1e6);
            System.out.printf("100,000 Counter Lookups      -> Original: %7.3f ms | Redesigned: %7.3f ms%n",
                    origLookupNs / 1e6, redLookupNs / 1e6);
            System.out.printf("20 Code-Order Reports        -> Original: %7.3f ms | Redesigned: %7.3f ms (%.2fx faster)%n%n",
                    origReportNs / 1e6, redReportNs / 1e6, (double) origReportNs / Math.max(1, redReportNs));
        }
    }
}
