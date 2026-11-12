# Q3 – Engineer the hashCode–equals Contract

## Complete the Code Output Comparison
- **Before completing `TODO 1` (`hashCode()` omitted):**
  `Unique students: 3`
  *Reason:* `HashSet` hashes each `Student` using `Object.hashCode()` (memory-address/identity-based). The two `new Student("192311001", "Arun")` instances produce different hash codes, map to different internal table buckets, and `equals()` is never even invoked across different buckets.
- **After completing `TODO 1` (`Objects.hash(regNo)`):**
  `Unique students: 2`
  *Reason:* Both `"192311001"` instances compute the identical hash code, land in the same bucket, and `equals()` confirms equality and suppresses the duplicate.

## Mutating a Field Used in `hashCode()` After Insertion
- If `regNo` is modified after `set.add(student)`, the object remains physically stored in the bucket corresponding to its **old** `hashCode()`. Subsequent `contains(student)` or `remove(student)` calls compute the **new** `hashCode()`, inspect a different bucket, and fail (`return false`), causing a logical memory leak. Declaring `regNo` as `private final` prevents this defect.

## Alternative Concept & Complexity Analysis
- **`TreeSet<Student>` with `Comparator` on `regNo`:** Uses Red-Black tree comparisons (`compareTo`/`compare`) instead of `hashCode()`/`equals()`. Time per scan: $O(\log n)$; Space: ~40 bytes per `TreeMap.Entry` node.
- **`HashMap<String, Student>` keyed by `regNo`:** Uses `String.hashCode()` directly via `putIfAbsent(regNo, student)`. Time per scan: $O(1)$ average; Space: ~32 bytes per `HashMap.Node` + `String` key reference.
