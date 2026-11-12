# Q2 – When Is ArrayList an Appropriate Engineering Choice?

## Operating Boundary & Recommendation
- **Use `ArrayList` when:** The playlist is append-heavy (`add` at end), frequently jumps to arbitrary $k$-th songs (`get(k)` in $O(1)$), or iterates sequentially over contiguous memory (CPU L1/L2 cache locality).
- **Stop recommending `ArrayList` when:** Users frequently insert or delete at the front (`index = 0`) or lower indices in large playlists ($n > 5,000$), triggering $O(n)$ `System.arraycopy()` element shifts on every edit.

## Changed Condition ("Up Next" Double-Ended Queue)
- When songs are frequently added at both ends (`addFirst`, `addLast`) and consumed from the front (`removeFirst`), **`ArrayDeque` is strictly superior** to both `ArrayList` and `LinkedList`:
  - Amortized $O(1)$ at both head and tail using circular `head`/`tail` bitmasking (`(head - 1) & (elements.length - 1)`).
  - Zero per-node wrapper allocation overhead (~4 bytes/slot vs ~24 bytes/node in `LinkedList`).
  - Contiguous array storage yields superior cache locality during iteration.

## Complexity & Memory Comparison Table

| Operation | `ArrayList` | `LinkedList` | `ArrayDeque` | Plain Array (Manual Shift) |
| :--- | :--- | :--- | :--- | :--- |
| **Add at end** | $O(1)$ amortized | $O(1)$ | $O(1)$ amortized | $O(1)$ if space, $O(n)$ resize |
| **Insert at front** | $O(n)$ | $O(1)$ | $O(1)$ amortized | $O(n)$ |
| **Insert in middle** | $O(n)$ | $O(n)$ traversal + $O(1)$ link | Not supported | $O(n)$ |
| **Get $k$-th element** | $O(1)$ | $O(n)$ | Not supported | $O(1)$ |
| **Iterate whole list** | $O(n)$ (cache-friendly) | $O(n)$ (pointer-chasing) | $O(n)$ (cache-friendly) | $O(n)$ (cache-friendly) |
| **Space per element** | ~4–8 bytes (object ref) | ~24 bytes (`Node` + `prev` + `next` + `item`) | ~4–8 bytes (object ref) | ~4–8 bytes (object ref) |
