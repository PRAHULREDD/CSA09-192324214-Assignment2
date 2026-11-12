# Q6 – Priority, Order and Fairness in a Queue

## Key Explanations
1. **How `arrival` preserves FIFO tie-breaking:** `PriorityQueue` is an unstable binary min-heap—it does not guarantee insertion order among equal elements. Assigning a monotonically increasing `long arrival = counter++` to every patient and chaining `.thenComparingLong(p -> p.arrival)` breaks severity ties strictly in arrival order.
2. **Why printing `PriorityQueue` directly does not show treatment order:** `toString()` iterates over the underlying `Object[] queue` array in level-order heap layout (where `queue[0]` is the minimum, but `queue[1]` and `queue[2]` are only heap-ordered relative to their parent, not sorted globally). Only repeated `poll()` extracts elements in full sorted order.
3. **Starvation & Fair Aging Rule:** Because `severity` 1–4 always preempts `severity` 5, a continuous stream of urgent arrivals starves S5 patients indefinitely. Defining $\text{effectiveSeverity}(t) = \max(1, \text{initialSeverity} - \lfloor (t - t_{\text{arrival}}) / \Delta T \rfloor)$ promotes long-waiting patients so every patient is guaranteed service.
4. **Deployment Decision (5,000 patients/day):** At $n \le 5,000$, $\log_2(5000) \approx 13$ heap comparisons per `add`/`poll` take $< 500\,\text{ns}$ ($< 2.5\,\text{ms}$ total for the entire day), easily meeting real-time hospital requirements.

## Alternative Concept & Complexity Analysis
- **Sorted `LinkedList`:** `admit` = $O(n)$ linear scan to find insertion point; `next` = $O(1)$ (`removeFirst`).
- **Array of 5 FIFO `ArrayDeque` Queues (Bucket Queue):** `admit` = $O(1)$ (`buckets[severity - 1].addLast(p)`); `next` = $O(k) = O(1)$ where $k=5$ severity levels. Fastest asymptotically when priority levels are small discrete integers ($1..5$).
