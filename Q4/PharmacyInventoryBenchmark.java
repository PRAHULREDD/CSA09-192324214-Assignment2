import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

public class PharmacyInventoryBenchmark {

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

    private static List<Medicine> generateMedicines(int count) {
        List<Medicine> list = new ArrayList<>(count);
        Random rng = new Random(12345);
        for (int i = 0; i < count; i++) {
            String code = String.format("MED-%06d", rng.nextInt(count * 10));
            list.add(new Medicine(code, "Drug_" + i, 50 + (i % 200)));
        }
        return list;
    }

    private static void runBenchmark(int n) {
        System.out.println("=========================================================================");
        System.out.printf("PHARMACY INVENTORY SIZE: %,d medicines | 100,000 lookups%n", n);
        System.out.println("=========================================================================");

        List<Medicine> items = generateMedicines(n);
        String[] lookupKeys = new String[100_000];
        Random rng = new Random(99);
        for (int i = 0; i < lookupKeys.length; i++) {
            lookupKeys[i] = items.get(rng.nextInt(n)).code;
        }

        // Populate maps
        Map<String, Medicine> hashMap = new HashMap<>(n * 2);
        Map<String, Medicine> treeMap = new TreeMap<>();
        Map<String, Medicine> linkedHashMap = new LinkedHashMap<>(n * 2);

        for (Medicine m : items) {
            hashMap.put(m.code, m);
            treeMap.put(m.code, m);
            linkedHashMap.put(m.code, m);
        }

        // 1. Measure 100,000 lookups
        long sink = 0;
        long t0 = System.nanoTime();
        for (String k : lookupKeys) sink += hashMap.get(k).stock;
        long hmLookupNs = System.nanoTime() - t0;

        t0 = System.nanoTime();
        for (String k : lookupKeys) sink += treeMap.get(k).stock;
        long tmLookupNs = System.nanoTime() - t0;

        t0 = System.nanoTime();
        for (String k : lookupKeys) sink += linkedHashMap.get(k).stock;
        long lhmLookupNs = System.nanoTime() - t0;

        // 2. Evening Report A: Code-Order Report
        // HashMap requires extracting & sorting keys O(n log n)
        t0 = System.nanoTime();
        List<String> sortedKeys = new ArrayList<>(hashMap.keySet());
        sortedKeys.sort(Comparator.naturalOrder());
        for (String k : sortedKeys) sink += hashMap.get(k).stock;
        long hmCodeReportNs = System.nanoTime() - t0;

        // TreeMap is already in code order O(n)
        t0 = System.nanoTime();
        for (Medicine m : treeMap.values()) sink += m.stock;
        long tmCodeReportNs = System.nanoTime() - t0;

        // 3. Evening Report B: Addition-Order Report
        // LinkedHashMap is already in insertion order O(n)
        t0 = System.nanoTime();
        for (Medicine m : linkedHashMap.values()) sink += m.stock;
        long lhmAddReportNs = System.nanoTime() - t0;

        if (sink == -1) System.out.print("");

        System.out.printf("HashMap       -> 100k Lookups: %7.3f ms (%.1f ns/op) | Code Report (sorted): %7.3f ms%n",
                hmLookupNs / 1e6, hmLookupNs / 100_000.0, hmCodeReportNs / 1e6);
        System.out.printf("TreeMap       -> 100k Lookups: %7.3f ms (%.1f ns/op) | Code Report (native): %7.3f ms%n",
                tmLookupNs / 1e6, tmLookupNs / 100_000.0, tmCodeReportNs / 1e6);
        System.out.printf("LinkedHashMap -> 100k Lookups: %7.3f ms (%.1f ns/op) | Add-Order Report:     %7.3f ms%n%n",
                lhmLookupNs / 1e6, lhmLookupNs / 100_000.0, lhmAddReportNs / 1e6);
    }

    public static void main(String[] args) {
        // Verify CSV sample
        try (BufferedReader br = new BufferedReader(new FileReader("medicines_input.csv"))) {
            String line = br.readLine(); // header
            Map<String, Medicine> sample = new LinkedHashMap<>();
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                sample.put(p[0].trim(), new Medicine(p[0].trim(), p[1].trim(), Integer.parseInt(p[2].trim())));
            }
            System.out.println("Loaded sample medicines in addition order: " + sample.keySet() + "\n");
        } catch (IOException ignored) {
        }

        int[] sizes = {500, 5_000, 50_000};
        for (int n : sizes) {
            runBenchmark(n);
        }
    }
}
