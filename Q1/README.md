# Q1 – Building Text Under Load: String, StringBuilder or StringBuffer?

## Pre-Implementation Prediction
- **String (`+`):** Will degrade quadratically ($O(n^2)$ characters copied per ticket) as the number of fields $n$ grows, because `java.lang.String` is immutable. Every `+` allocates a new `String` (or `StringBuilder` + `toString()` per iteration) and copies all prior characters.
- **StringBuilder:** Will scale linearly ($O(n)$) with minimal heap allocations because it mutates an internal resizable `byte[]` buffer without synchronization locks.
- **StringBuffer:** Will also scale linearly ($O(n)$) but run slightly slower than `StringBuilder` due to `synchronized` method lock acquisition/release overhead on every `append()`.

## Design Decision (Multithreaded Booking Server)
1. **Per-Ticket Text Construction -> `StringBuilder`:** Each e-ticket is constructed locally within a single request-handling thread (thread-confined stack variable). No external thread shares that builder instance, so synchronization is wasted overhead.
2. **Shared Booking Audit Log -> `StringBuffer` (or concurrent log queue):** Because multiple worker threads concurrently append log lines to a single shared buffer, thread-safety is mandatory to prevent interleaved characters or `ArrayIndexOutOfBoundsException`. `StringBuffer` provides intrinsic monitor synchronization on `append()`.

## Alternative Concept & Complexity Analysis
- **`String.join("|", fields)` / `StringJoiner`:** Internally uses `StringBuilder` (or pre-computes exact total character length in Java 9+ `String.join` to allocate the target `byte[]` once with zero resizing).
- **Time Complexity:** Repeated `+` copies $1 + 2 + \dots + n = \frac{n(n+1)}{2} = O(n^2)$ characters. `StringBuilder` copies each character once into its buffer and once into the final `String` = $O(n)$.
- **Space Complexity:** Repeated `+` allocates $O(n)$ intermediate `String` objects totaling $O(n^2)$ bytes of short-lived garbage; `StringBuilder` uses $O(n)$ auxiliary buffer space.
