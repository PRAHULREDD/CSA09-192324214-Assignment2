# CSA09 – Programming in Java: Assignment 2 (CO2) Report

**Name:** Rahul Reddy  
**Register Number:** 192324214  
**Course:** CSA09 – Programming in Java  
**GitHub Repository:** `https://github.com/<your-username>/CSA09-192324214-Assignment2`

---

## Q1 – Building Text Under Load: String, StringBuilder or StringBuffer? (6 Marks)

### 1. Pre-Implementation Prediction & Reasoning
- **`String` (`+` operator in loop):** Because `java.lang.String` is immutable (`private final byte[] value`), concatenating $n$ fields in a loop creates a new `String` on every iteration and copies all previously accumulated characters. For $n$ fields of average length $L$, total characters copied scale as $L(1 + 2 + \dots + n) = O(n^2)$, causing severe execution slowdown and Young-Generation garbage collection overhead.
- **`StringBuilder`:** Uses a mutable, resizable internal `byte[]` buffer. Appending $n$ fields amortizes to $O(1)$ per append ($O(n)$ total) with zero synchronization overhead.
- **`StringBuffer`:** Identical resizable array algorithm as `StringBuilder`, but every `append()` acquires and releases an intrinsic monitor lock (`synchronized`), making it $O(n)$ yet measurably slower than `StringBuilder`.

### 2. Measured Experimental Results (Averaged over 10 Runs)

| Workload | `String (+)` Avg Time | Est. Intermediate Strings | `StringBuilder` Avg Time | `StringBuffer` Avg Time | Speedup (`Builder` vs `+`) |
| :--- | :---: | :---: | :---: | :---: | :---: |
| **W1:** 1 ticket $\times$ 8 fields | `0.040 ms` | `16` | `0.012 ms` | `0.011 ms` | **3.33x** |
| **W2:** 10,000 tickets $\times$ 50 fields | `73.237 ms` | `1,000,000` | `18.730 ms` | `19.885 ms` | **3.91x** |
| **W3:** 2,000 tickets $\times$ 200 fields | `186.848 ms` | `800,000` | `12.043 ms` | `11.738 ms` | **15.52x** |

*Comparison with Prediction:* Even though Workload 3 had **fewer total tickets** ($2,000$ vs $10,000$) and fewer intermediate objects ($800,000$ vs $1,000,000$) than Workload 2, `String (+)` took **2.55x longer** (`186.848 ms` vs `73.237 ms`), directly confirming the $O(n^2)$ quadratic growth with respect to field count $n$ ($200^2 / 50^2 = 16\times$ per ticket). Meanwhile, `StringBuilder` scaled linearly with total characters ($12.043\,\text{ms}$ vs $18.730\,\text{ms}$).

### 3. Design Decision: Multithreaded Booking Server
- **Per-Ticket Generation $\rightarrow$ `StringBuilder`:** Each ticket string is built locally inside a single worker thread's stack frame (thread-confined). Thread safety is unnecessary, so `StringBuilder` eliminates lock overhead.
- **Shared Booking Audit Log $\rightarrow$ `StringBuffer`:** Multiple booking threads concurrently append audit records to one shared buffer. Using `StringBuilder` across threads would cause race conditions, lost characters, or `ArrayIndexOutOfBoundsException` during buffer expansion; `StringBuffer` guarantees thread-safe atomic `append()`.

### 4. Alternative Concept & Big-O Complexity Analysis
- **Alternative (`String.join("|", fields)` / `StringJoiner`):** `String.join` pre-computes the exact total length of all elements and delimiter characters in one pass, allocates a single target array of exact size, and copies each string once—eliminating even `StringBuilder`'s doubling resizes.
- **Complexity Comparison:**
  - **Repeated `+`:** Time = $O(n^2)$ characters copied; Extra Space = $O(n^2)$ transient heap bytes across $2n$ intermediate `String` objects.
  - **`StringBuilder` / `StringJoiner`:** Time = $O(n)$ characters copied; Extra Space = $O(n)$ internal buffer bytes.

---

## Q2 – When Is ArrayList an Appropriate Engineering Choice? (4 Marks)

### 1. Operating Boundary
- **`ArrayList` is optimal when:** Operations are dominated by appending at the end (`add(e)` in amortized $O(1)$), random jumps to the $k$-th track (`get(k)` in $O(1)$), and full sequential playback iteration (contiguous backing array maximizes CPU cache hits).
- **Stop recommending `ArrayList` when:** Users frequently insert or remove tracks near the front (`index = 0`) or lower indices on large playlists ($n > 5,000$), as every edit forces `System.arraycopy()` to shift $O(n)$ elements.

### 2. Changed Condition: "Up Next" Queue (`ArrayDeque`)
- For an "Up Next" queue that frequently inserts at both ends (`addFirst`, `addLast`) and polls from the front (`removeFirst`), **`ArrayDeque` is the best choice**.
- Unlike `ArrayList` ($O(n)$ front shift), `ArrayDeque` uses a circular array with bitmasked `head` and `tail` pointers for amortized $O(1)$ operations at both ends. Unlike `LinkedList`, it avoids allocating a 24-byte `Node` wrapper per song and preserves cache locality.

### 3. Complexity & Memory Table (Including Plain Array Alternative)

| Operation | `ArrayList` | `LinkedList` | `ArrayDeque` | Plain Array (Manual Shift) |
| :--- | :--- | :--- | :--- | :--- |
| **Add at end** | $O(1)$ amortized | $O(1)$ | $O(1)$ amortized | $O(1)$ if capacity remains; $O(n)$ on manual resize |
| **Insert at front** | $O(n)$ | $O(1)$ | $O(1)$ amortized | $O(n)$ manual right-shift loop |
| **Insert in middle** | $O(n)$ | $O(n)$ seek + $O(1)$ link | Not supported | $O(n)$ manual right-shift loop |
| **Get $k$-th element** | $O(1)$ | $O(n)$ | Not supported | $O(1)$ |
| **Iterate whole list** | $O(n)$ | $O(n)$ | $O(n)$ | $O(n)$ |
| **Space per element** | ~4–8 B (array slot) | ~24 B (`Node` object + `prev`/`next`/`item`) | ~4–8 B (array slot) | ~4–8 B (array slot) |

---

## Q3 – Engineer the hashCode–equals Contract (6 Marks)

### 1. Predictions vs Observed Results
1. **Test 1 (Re-adding the exact same `Student` reference):** Predicted size = `2`, Observed = `2`. Because `s1 == s1`, `Object.hashCode()` returns the same identity hash, landing in the same bucket where `equals()` returns `true`.
2. **Test 2 (Scanning `new Student("23AID001", "Rahul")` twice as distinct instances):** Predicted size = `3` (bug), Observed = `3`. Two distinct `new` instances inherit distinct `Object.hashCode()` values, hash to different table buckets, and `HashSet` never compares them with `equals()`.
3. **Test 3 (Mutating `regNo` after insertion):** Predicted `contains(m)` = `false`, Observed = `false`. Mutating a hash-key field after insertion leaves the object stranded in its old bucket while `contains()` searches the new hash bucket.

### 2. Complete the Code Output
- **Before `TODO 1`:** `Unique students: 3`
- **After `TODO 1` (`Objects.hash(regNo)`):** `Unique students: 2`

### 3. Alternative Concept & Complexity
- **`TreeSet<Student>` with `Comparator.comparing(s -> s.regNo)`:** Deduplicates via tree comparisons (`compare == 0`) without `hashCode()`. Time per scan: $O(\log n)$; Space: ~40 bytes per Red-Black tree node.
- **`HashMap<String, Student>` keyed by `regNo`:** Uses `putIfAbsent(regNo, student)`. Time per scan: $O(1)$ average; Space: ~32 bytes per `Node` entry.

---

## Q4 – Choosing a Map for a Pharmacy Inventory (6 Marks)

### 1. Map Comparison & Deployment Choice
- **`HashMap`:** $O(1)$ `put`/`get`, arbitrary iteration order (requires $O(n \log n)$ sort for code-order report; cannot produce addition-order report).
- **`TreeMap`:** $O(\log n)$ `put`/`get`, naturally sorted code-order iteration in $O(n)$ (cannot produce addition-order report).
- **`LinkedHashMap`:** $O(1)$ `put`/`get`, naturally preserves insertion (addition) order in $O(n)$, and requires only one $O(n \log n)$ sort per evening for the code-order report.
- **Acceptability Criterion & Choice:** Counter lookup latency must be $< 10\,\mu\text{s}$ per query. `LinkedHashMap` achieves ~$0.18\,\mu\text{s}$ per lookup and natively satisfies the addition-order report, making it the strongest single-map choice. `Hashtable` is rejected due to legacy coarse-grained `synchronized` locks on every read operation.

### 2. Alternative Concept: Sorted Array + `Arrays.binarySearch()`
- **Lookup:** $O(\log n)$ binary search.
- **Insertion:** $O(n)$ due to shifting elements to maintain sorted order.
- **Code-Order Report:** $O(n)$ sequential scan.
- **Space:** Compact (~4 bytes per reference vs ~32–40 bytes per Map node), but impractical when new medicines are frequently added during the day.

---

## Q5 – Derive a Generic Ranking Component from a Principle (7 Marks)

### 1. Design & Correctness Proof
- Using an external `Comparator<? super T>` passed to `Leaderboard<T>` decouples ranking rules from domain classes (`Team`, `Player`, `QuizTeam`) and supports multiple ranking criteria.
- Chaining `Comparator.comparingInt((Team t) -> t.points).reversed().thenComparing(Comparator.comparingInt((Team t) -> t.goalDiff).reversed()).thenComparing(t -> t.name)` forms a lexicographic product of total orders, guaranteeing transitivity ($A > B \land B > C \implies A > C$) and consistency without integer subtraction overflow.
- **Observed Output:** `[Eagles(9,6), Bisons(9,4), Panthers(9,4), Cobras(6,1)]`
- **Why `Bisons(9,4)` ranks above `Panthers(9,4)`:** Both teams tie on points (`9`) and goal difference (`4`), so the third comparator breaks the tie alphabetically (`"Bisons"` precedes `"Panthers"`).
- **Faulty Subtraction Bug Proof:** Comparing `Integer.MAX_VALUE` against `-10` via `b.points - a.points` overflows `int` to `2147483639` (positive), falsely ranking the negative score ahead of `Integer.MAX_VALUE`.

### 2. Alternative Concept & Complexity
- **Single `Comparator` Chain:** $1 \times O(n \log n)$ sort, $O(n)$ auxiliary space for defensive list copy.
- **Three Sequential Stable Sorts (Name $\rightarrow$ GoalDiff $\rightarrow$ Points):** $3 \times O(n \log n) = O(n \log n)$ time with $3\times$ sorting passes and poor generic reusability across types with different field schemas.

---

## Q6 – Priority, Order and Fairness in a Queue (8 Marks)

### 1. Queue Design & Starvation Prevention
- A plain FIFO `Queue` ignores clinical urgency. `PriorityQueue<Patient>` backed by a binary min-heap orders patients by `severity` ascending, breaking ties using a monotonic `arrival` sequence number (`counter++`).
- Printing `PriorityQueue` directly exposes the internal array heap order rather than sorted order; repeated `poll()` extracts patients in exact treatment order (`Meena(S1) Kumar(S1) Lata(S2) Ravi(S3) John(S3) Asha(S5)`).
- **Fairness Rule (Aging):** To prevent indefinite starvation of S5 patients, effective priority is computed as $\text{effectiveSeverity}(t) = \max(1, \text{initialSeverity} - \lfloor \text{waitTime} / \Delta T \rfloor)$.
- **Deployment Decision (5,000 patients/day):** Even if all 5,000 patients queued simultaneously, $\log_2(5000) \approx 13$ comparisons per operation completes in $< 3\,\text{ms}$ total per day—well within hospital latency requirements.

### 2. Alternative Concept & Complexity
- **Sorted `LinkedList`:** `admit` = $O(n)$, `next` = $O(1)$.
- **Array of 5 FIFO Queues (`ArrayDeque<Patient>[5]`):** `admit` = $O(1)$, `next` = $O(1)$ (scans at most 5 buckets). Superior when priority levels are bounded to a small fixed integer range.

---

## Q7 – Day-4 Design Challenge: Diagnose, Redesign & Validate (13 Marks)

### A. Diagnose the Bottleneck (2 Marks)
In the Q4 `LinkedHashMap` solution, counter lookups are $O(1)$ and addition-order reports are $O(n)$, but generating the code-order report copies all $n$ keys into a new `ArrayList`, sorts them in $O(n \log n)$ time, and performs $n$ secondary hash lookups. As inventory size $n$ grows or reports are run frequently during audits, CPU time and temporary object allocations scale super-linearly ($O(n \log n)$).

### B. Define the Design Objective (2 Marks)
Eliminate report-time sorting completely so that **both** evening reports (code-order and addition-order) execute in **$O(n)$ time with zero sorting comparisons**, while preserving **$O(1)$ average lookup latency** for counter billing.

### C. Design the Revised Solution (4 Marks)
We introduce a dual-index repository (`RedesignedPharmacySystem`) encapsulating two synchronized maps:
1. `primaryIndex`: `LinkedHashMap<String, Medicine>` — handles $O(1)$ counter lookups and $O(n)$ addition-order reporting.
2. `sortedCodeIndex`: `TreeMap<String, Medicine>` — maintains medicine keys continuously sorted in a Red-Black tree during ingestion/updates ($O(\log n)$ per write), enabling $O(n)$ in-order traversal for code-order reports.

**Trade-off:** Increases per-medicine memory overhead by ~40 bytes (for the secondary `TreeMap.Entry` node) and shifts write complexity from $O(1)$ to $O(\log n)$, which is negligible because pharmacy stock updates occur far less frequently than counter lookups and report queries.

### D. Predict Before Implementing (1 Mark)
At $n = 100,000$ medicines, 100,000 lookups will remain virtually identical (~18–25 ms, $O(1)$), whereas generating 20 code-order reports will drop from $O(20 \cdot n \log n)$ to $O(20 \cdot n)$, yielding a **$3\times$ to $10\times$ speedup**.

### E. Implement and Validate the Redesign (2 Marks)

| Inventory Size ($n$) | Operation | Original (`LinkedHashMap`) | Redesigned (`LinkedHashMap` + `TreeMap`) | Comparison |
| :---: | :--- | :---: | :---: | :---: |
| **5,000** | Bulk Ingestion | `3.941 ms` | `11.916 ms` | $3.02\times$ write cost |
| **5,000** | 100,000 Lookups | `18.770 ms` | `21.483 ms` | ~$O(1)$ parity |
| **5,000** | 20 Code-Order Reports | `209.060 ms` | `52.534 ms` | **3.98x faster** |
| **25,000** | Bulk Ingestion | `26.791 ms` | `49.933 ms` | $1.86\times$ write cost |
| **25,000** | 100,000 Lookups | `31.311 ms` | `28.275 ms` | ~$O(1)$ parity |
| **25,000** | 20 Code-Order Reports | `507.518 ms` | `66.421 ms` | **7.64x faster** |
| **100,000** | Bulk Ingestion | `32.592 ms` | `189.179 ms` | $5.80\times$ write cost |
| **100,000** | 100,000 Lookups | `22.846 ms` | `18.451 ms` | ~$O(1)$ parity |
| **100,000** | 20 Code-Order Reports | `1,720.389 ms` | `179.054 ms` | **9.61x faster** |

### F. Defend the Final Design (2 Marks)
- **Objective Achieved:** Prediction confirmed—report-time sorting overhead is completely eliminated, delivering a **9.61x speedup** (`1,720.389 ms` $\rightarrow$ `179.054 ms`) at $n = 100,000$, while counter lookups remain $O(1)$ (`18.451 ms` for 100,000 queries).
- **When Original Is Still Preferable:** If the system is heavily write-bound (thousands of stock mutations per second) and sorted reports are generated rarely (e.g., once a month), or on memory-constrained embedded hardware where maintaining a second Red-Black tree index (~40 bytes/entry) is undesirable.
