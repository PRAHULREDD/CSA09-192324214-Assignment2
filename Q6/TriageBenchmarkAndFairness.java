import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

public class TriageBenchmarkAndFairness {

    static class FairPatient {
        final String name;
        final int initialSeverity;
        final long arrivalTick;

        FairPatient(String name, int initialSeverity, long arrivalTick) {
            this.name = name;
            this.initialSeverity = initialSeverity;
            this.arrivalTick = arrivalTick;
        }

        // Dynamic effective severity: improves by 1 level every 10 ticks waited (minimum 1)
        int effectiveSeverity(long currentTick) {
            long waited = Math.max(0, currentTick - arrivalTick);
            int boost = (int) (waited / 10);
            return Math.max(1, initialSeverity - boost);
        }
    }

    public static void main(String[] args) {
        // 1. Hand-verified 8-patient correctness check & raw heap order vs poll order
        Triage er = new Triage();
        er.admit("P1_Arjun", 4);
        er.admit("P2_Bhavna", 2);
        er.admit("P3_Charan", 1);
        er.admit("P4_Deepa", 2);
        er.admit("P5_Eshan", 5);
        er.admit("P6_Farah", 1);
        er.admit("P7_Gokul", 3);
        er.admit("P8_Harini", 4);

        System.out.print("Treatment Order (8 patients): ");
        Patient next;
        while ((next = er.next()) != null) {
            System.out.print(next + " ");
        }
        System.out.println("\n");

        // 2. Growth Investigation across 3 queue sizes
        int[] sizes = {1_000, 10_000, 100_000};
        Random rng = new Random(7);
        for (int n : sizes) {
            PriorityQueue<Patient> pq = new PriorityQueue<>(
                    Comparator.comparingInt((Patient p) -> p.severity)
                              .thenComparingLong(p -> p.arrival));

            long t0 = System.nanoTime();
            for (int i = 0; i < n; i++) {
                pq.add(new Patient("Pat" + i, 1 + rng.nextInt(5), i));
            }
            long addNs = System.nanoTime() - t0;

            t0 = System.nanoTime();
            while (!pq.isEmpty()) {
                pq.poll();
            }
            long pollNs = System.nanoTime() - t0;

            System.out.printf("Queue Size: %,7d | Total Add: %7.3f ms (%.1f ns/op) | Total Poll: %7.3f ms (%.1f ns/op)%n",
                    n, addNs / 1e6, addNs / (double) n, pollNs / 1e6, pollNs / (double) n);
        }

        // 3. Anti-Starvation Aging Demonstration
        System.out.println("\n--- Anti-Starvation Aging Simulation (1 severity boost per 10 ticks) ---");
        List<FairPatient> waiting = new ArrayList<>();
        waiting.add(new FairPatient("MinorCase_Asha", 5, 0));   // arrives at tick 0
        waiting.add(new FairPatient("Urgent_Ravi", 2, 25));     // arrives at tick 25
        waiting.add(new FairPatient("Moderate_Kiran", 3, 30));  // arrives at tick 30

        long currentTick = 35; // Asha has waited 35 ticks -> boost = 3 -> effective severity = 5 - 3 = 2!
        waiting.sort(Comparator.comparingInt((FairPatient p) -> p.effectiveSeverity(currentTick))
                               .thenComparingLong(p -> p.arrivalTick));
        for (FairPatient fp : waiting) {
            System.out.printf("%s | Initial: S%d | Waited: %d ticks | Effective: S%d%n",
                    fp.name, fp.initialSeverity, (currentTick - fp.arrivalTick), fp.effectiveSeverity(currentTick));
        }
    }
}
