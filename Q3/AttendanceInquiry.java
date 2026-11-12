import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

public class AttendanceInquiry {

    // Broken Student (overrides equals, omits hashCode)
    static class BrokenStudent {
        String regNo;
        String name;

        BrokenStudent(String regNo, String name) {
            this.regNo = regNo;
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof BrokenStudent)) return false;
            return regNo.equals(((BrokenStudent) o).regNo);
        }

        @Override
        public String toString() {
            return regNo + "(" + name + ")";
        }
    }

    // Mutable Student (to test mutating key field after insertion)
    static class MutableStudent {
        String regNo;
        String name;

        MutableStudent(String regNo, String name) {
            this.regNo = regNo;
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof MutableStudent)) return false;
            return Objects.equals(regNo, ((MutableStudent) o).regNo);
        }

        @Override
        public int hashCode() {
            return Objects.hash(regNo);
        }
    }

    public static void main(String[] args) {
        // Test 1: Scan set where broken HashSet appears to work (re-inserting exact same reference)
        Set<BrokenStudent> test1 = new HashSet<>();
        BrokenStudent s1 = new BrokenStudent("23AID001", "Rahul");
        BrokenStudent s2 = new BrokenStudent("23AID002", "Sneha");
        test1.add(s1);
        test1.add(s2);
        test1.add(s1); // Same object reference -> same Object.hashCode() -> bucket matches -> equals() true
        System.out.println("Test 1 (Same Reference Re-scan) | Predicted: 2 | Observed: " + test1.size());

        // Test 2: Scan set exposing duplicates (new object instance at Gate 3 with same regNo)
        Set<BrokenStudent> test2 = new HashSet<>();
        test2.add(new BrokenStudent("23AID001", "Rahul"));
        test2.add(new BrokenStudent("23AID002", "Sneha"));
        test2.add(new BrokenStudent("23AID001", "Rahul")); // Distinct identity hash -> different bucket -> duplicate!
        System.out.println("Test 2 (New Instance at Gate 3)  | Predicted: 3 (Bug!) | Observed: " + test2.size());

        // Test 3: Mutating regNo after insertion into HashSet
        Set<MutableStudent> test3 = new HashSet<>();
        MutableStudent m = new MutableStudent("23AID010", "Vikram");
        test3.add(m);
        m.regNo = "23AID999"; // Hash bucket now mismatches stored bucket!
        System.out.println("Test 3 (Mutated regNo) contains m? | Predicted: false | Observed: " + test3.contains(m));

        // Alternative Concept 1: TreeSet<Student> with Comparator on regNo
        Set<Student> treeSet = new TreeSet<>(Comparator.comparing(st -> st.toString().split(" ")[0]));
        treeSet.add(new Student("192311001", "Arun"));
        treeSet.add(new Student("192311002", "Divya"));
        treeSet.add(new Student("192311001", "Arun"));
        System.out.println("Alternative TreeSet size: " + treeSet.size());

        // Alternative Concept 2: HashMap<String, Student> keyed by regNo
        Map<String, Student> mapByRegNo = new HashMap<>();
        mapByRegNo.putIfAbsent("192311001", new Student("192311001", "Arun"));
        mapByRegNo.putIfAbsent("192311002", new Student("192311002", "Divya"));
        mapByRegNo.putIfAbsent("192311001", new Student("192311001", "Arun"));
        System.out.println("Alternative HashMap size: " + mapByRegNo.size());
    }
}
