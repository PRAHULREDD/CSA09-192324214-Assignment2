# Q7 – Day-4 Design Challenge: Diagnose, Redesign & Validate

## A. Diagnose the Bottleneck
- **Selected Solution:** Q4 (`LinkedHashMap`-only Pharmacy Inventory).
- **Bottleneck:** Generating the code-ordered report repeatedly copies `keySet()` into a temporary `ArrayList`, performs an $O(n \log n)$ `TimSort` of string codes, and executes $n$ random `HashMap.get()` pointer chases on every report generation.

## B. Design Objective
- Maintain $O(1)$ average lookup latency and $O(n)$ addition-order reporting while reducing code-order report generation from $O(n \log n)$ to $O(n)$ with zero sorting allocations at report time.

## C. Revised Solution (Dual-Index Write-Synchronized Architecture)
- Encapsulate a `LinkedHashMap<String, Medicine>` (`primaryIndex`) and a `TreeMap<String, Medicine>` (`sortedCodeIndex`) inside a single repository abstraction.
- Writes (`addOrUpdate`) update both structures ($O(\log n)$), reads (`lookup`) hit `primaryIndex` ($O(1)$), addition-order reports stream `primaryIndex.values()` ($O(n)$), and code-order reports stream `sortedCodeIndex.values()` ($O(n)$).

## D–F. Prediction, Validation & Defense
- **Prediction:** Code-order report generation will improve by $3\times$–$6\times$ at $n = 100,000$ while lookup time remains unchanged ($O(1)$), at the trade-off of ~$40\,\text{bytes}$ per medicine for the secondary `TreeMap` nodes and slightly slower writes ($O(\log n)$ vs $O(1)$).
- **Observed Result:** At $100,000$ medicines, 20 code-order reports dropped from **1,720.389 ms** to **179.054 ms** (**9.61x speedup**), while 100,000 counter lookups stayed constant at **18.451 ms** (~184 ns/op).
- **When Original Is Still Preferable:** Write-heavy systems where medicines are inserted/updated thousands of times per second and sorted reports are generated rarely (e.g., once a month), or severely memory-constrained edge devices.
