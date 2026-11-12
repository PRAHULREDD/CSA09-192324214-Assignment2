import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class TicketBenchmark {

    static class Workload {
        final String name;
        final int ticketCount;
        final int fieldsPerTicket;

        Workload(String name, int ticketCount, int fieldsPerTicket) {
            this.name = name;
            this.ticketCount = ticketCount;
            this.fieldsPerTicket = fieldsPerTicket;
        }
    }

    // Approach 1: String concatenation using '+' inside loop
    public static String buildWithPlus(String[] fields) {
        String ticket = "";
        for (int i = 0; i < fields.length; i++) {
            ticket = ticket + fields[i] + "|";
        }
        return ticket;
    }

    // Approach 2: StringBuilder (unsynchronized mutable buffer)
    public static String buildWithStringBuilder(String[] fields) {
        StringBuilder sb = new StringBuilder(fields.length * 16);
        for (int i = 0; i < fields.length; i++) {
            sb.append(fields[i]).append('|');
        }
        return sb.toString();
    }

    // Approach 3: StringBuffer (synchronized mutable buffer)
    public static String buildWithStringBuffer(String[] fields) {
        StringBuffer sbf = new StringBuffer(fields.length * 16);
        for (int i = 0; i < fields.length; i++) {
            sbf.append(fields[i]).append('|');
        }
        return sbf.toString();
    }

    // Alternative Concept: String.join
    public static String buildWithStringJoin(String[] fields) {
        return String.join("|", fields) + "|";
    }

    // Alternative Concept: StringJoiner
    public static String buildWithStringJoiner(String[] fields) {
        StringJoiner joiner = new StringJoiner("|", "", "|");
        for (String field : fields) {
            joiner.add(field);
        }
        return joiner.toString();
    }

    private static String[][] generateTickets(int count, int fieldsPerTicket) {
        String[][] tickets = new String[count][fieldsPerTicket];
        for (int t = 0; t < count; t++) {
            for (int f = 0; f < fieldsPerTicket; f++) {
                tickets[t][f] = "F" + f + ":PNR" + (100000 + t);
            }
        }
        return tickets;
    }

    private static long getUsedMemoryBytes() {
        System.gc();
        try {
            Thread.sleep(20);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
        Runtime rt = Runtime.getRuntime();
        return rt.totalMemory() - rt.freeMemory();
    }

    private static void runWorkload(Workload w, int runs) {
        System.out.println("=================================================================");
        System.out.printf("WORKLOAD: %s (%d tickets x %d fields)%n", w.name, w.ticketCount, w.fieldsPerTicket);
        System.out.println("=================================================================");

        String[][] data = generateTickets(w.ticketCount, w.fieldsPerTicket);

        // Warmup JVM JIT
        for (int i = 0; i < Math.min(200, w.ticketCount); i++) {
            buildWithPlus(data[i]);
            buildWithStringBuilder(data[i]);
            buildWithStringBuffer(data[i]);
        }

        // Measure String '+'
        long memBeforePlus = getUsedMemoryBytes();
        long totalPlusNs = 0;
        int sink = 0;
        for (int r = 0; r < runs; r++) {
            long start = System.nanoTime();
            for (int t = 0; t < w.ticketCount; t++) {
                sink ^= buildWithPlus(data[t]).length();
            }
            totalPlusNs += (System.nanoTime() - start);
        }
        long memAfterPlus = getUsedMemoryBytes();
        long estIntermediateStrings = (long) w.ticketCount * (2L * w.fieldsPerTicket);

        // Measure StringBuilder
        long memBeforeSb = getUsedMemoryBytes();
        long totalSbNs = 0;
        for (int r = 0; r < runs; r++) {
            long start = System.nanoTime();
            for (int t = 0; t < w.ticketCount; t++) {
                sink ^= buildWithStringBuilder(data[t]).length();
            }
            totalSbNs += (System.nanoTime() - start);
        }
        long memAfterSb = getUsedMemoryBytes();

        // Measure StringBuffer
        long memBeforeSbf = getUsedMemoryBytes();
        long totalSbfNs = 0;
        for (int r = 0; r < runs; r++) {
            long start = System.nanoTime();
            for (int t = 0; t < w.ticketCount; t++) {
                sink ^= buildWithStringBuffer(data[t]).length();
            }
            totalSbfNs += (System.nanoTime() - start);
        }
        long memAfterSbf = getUsedMemoryBytes();

        if (sink == 42) System.out.print(""); // prevent dead-code elimination

        System.out.printf("%-15s | Avg Time (ms) | Est. Intermediate Strings | Delta Mem (KB)%n", "Approach");
        System.out.println("-----------------------------------------------------------------");
        System.out.printf("%-15s | %13.3f | %25d | %14.2f%n",
                "String (+)", (totalPlusNs / (double) runs) / 1_000_000.0,
                estIntermediateStrings, Math.max(0, memAfterPlus - memBeforePlus) / 1024.0);
        System.out.printf("%-15s | %13.3f | %25d | %14.2f%n",
                "StringBuilder", (totalSbNs / (double) runs) / 1_000_000.0,
                0L, Math.max(0, memAfterSb - memBeforeSb) / 1024.0);
        System.out.printf("%-15s | %13.3f | %25d | %14.2f%n%n",
                "StringBuffer", (totalSbfNs / (double) runs) / 1_000_000.0,
                0L, Math.max(0, memAfterSbf - memBeforeSbf) / 1024.0);
    }

    public static void main(String[] args) {
        List<String> sampleFields = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("sample_tickets.csv"))) {
            String line = br.readLine(); // skip header
            if ((line = br.readLine()) != null) {
                for (String token : line.split(",")) {
                    sampleFields.add(token.trim());
                }
            }
        } catch (IOException e) {
            sampleFields.add("PNR:4512890123");
            sampleFields.add("PASSENGER:Rahul Reddy");
            sampleFields.add("TRAIN:12622 Tamil Nadu Exp");
            sampleFields.add("COACH:B2");
            sampleFields.add("BERTH:41/LB");
            sampleFields.add("FARE:INR 1845.00");
        }
        System.out.println("Sample Built e-Ticket: " + buildWithStringBuilder(sampleFields.toArray(new String[0])));
        System.out.println();

        Workload[] workloads = {
            new Workload("Workload 1: Single Short Ticket", 1, 8),
            new Workload("Workload 2: Bulk Standard Tickets", 10_000, 50),
            new Workload("Workload 3: Heavy Group Manifesto (Custom)", 2_000, 200)
        };

        for (Workload w : workloads) {
            runWorkload(w, 10);
        }
    }
}
