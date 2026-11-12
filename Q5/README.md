# Q5 – Derive a Generic Ranking Component from a Principle

## Design Decisions & Correctness Inquiry
1. **`Comparator` vs `Comparable`:** External `Comparator<? super T>` objects are superior because `Leaderboard<T>` can rank arbitrary entities (players, colleges, quiz teams) without forcing domain classes to implement `Comparable`, and allows multiple ranking rules for the same type.
2. **Why `Bisons(9,4)` ranks above `Panthers(9,4)`:** Both teams tie on `points` (`9 == 9`) and tie on `goalDiff` (`4 == 4`). The third comparator stage (`thenComparing(t -> t.name)`) compares `"Bisons"` vs `"Panthers"` lexicographically (`"Bisons".compareTo("Panthers") < 0`), placing `Bisons` ahead of `Panthers`.
3. **Strict Weak Ordering Proof:** Each stage delegates to `Integer.compare` (which avoids subtraction overflow) and `String.compareTo`. Lexicographic composition of total orders preserves irreflexivity, asymmetry, and transitivity.
4. **Wildcards (`List<? extends T>` & `Comparator<? super T>`):** Follows PECS (Producer Extends, Consumer Super)—`List<? extends T>` lets callers bulk-load subtypes into `Leaderboard<T>`, while `Comparator<? super T>` lets a supertype comparator rank subtypes.

## Alternative Concept & Complexity Analysis
- **Single `Comparator` Chain (`copy.sort(order)`):** 1 TimSort pass = $O(n \log n)$ comparisons, $O(n)$ extra space for the defensive copy.
- **Three Sequential Stable Sorts (by name, then goalDiff, then points):** 3 TimSort passes = $3 \times O(n \log n) = O(n \log n)$ time, higher constant factor and poor reusability for players/colleges whose fields differ.
