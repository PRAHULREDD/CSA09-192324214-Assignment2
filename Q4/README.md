# Q4 – Choosing a Map for a Pharmacy Inventory

## Pre-Implementation Comparison
- **`HashMap`:** `put` $O(1)$ avg, `get` $O(1)$ avg, iteration $O(n)$ in arbitrary hash-bucket order. Requires external $O(n \log n)$ sort for code-order report; cannot reconstruct addition order without extra timestamps.
- **`TreeMap`:** `put` $O(\log n)$, `get` $O(\log n)$, iteration $O(n)$ **naturally in sorted medicine code order**. Cannot produce addition-order report without auxiliary structures.
- **`LinkedHashMap`:** `put` $O(1)$ avg, `get` $O(1)$ avg, iteration $O(n)$ **naturally in insertion (addition) order**. Requires external $O(n \log n)$ sort for code-order report.

## Scalability & Deployment Decision
- **Acceptability Criterion:** Single medicine lookup must complete in $< 10\,\mu\text{s}$ ($10,000\,\text{ns}$) so interactive counter billing feels instantaneous. All three maps easily satisfy this ($< 300\,\text{ns}$ per lookup).
- **Recommended Single Map:** **`LinkedHashMap`** — it delivers $O(1)$ counter lookups and natively produces the evening addition-order report in $O(n)$ with zero sorting, while the once-per-evening code-order report costs a negligible $O(n \log n)$ sort on a few thousand keys.
- **Why `Hashtable` is avoided:** Every method in legacy `Hashtable` is coarse-grained `synchronized`, creating lock contention even for read-only lookups, and it prohibits `null` keys/values.

## Alternative Concept: Sorted Array + `Arrays.binarySearch()`
- **Lookup:** $O(\log n)$ via binary search (cache-friendly, low memory).
- **Insertion:** $O(n)$ because maintaining sorted order requires shifting up to $n$ elements on every new medicine.
- **Code-Order Report:** $O(n)$ direct linear scan.
- **Space:** Minimal (~4 bytes per reference), much smaller than `TreeMap` (~40 bytes/node) or `HashMap` (~32 bytes/node).
