# CSA09 – Programming in Java: Assignment 2 (CO2)

- **Student Name:** Rahul Reddy
- **Register Number:** 192324214
- **Course:** CSA09 – Programming in Java (CO2: Strings, Regular Expressions & Java Collections Framework)
- **Java Version:** Java 17 / Java 21 (JDK 17+ recommended)

---

## Repository Structure

```text
CSA09-192324214-Assignment2/
├── README.md                  # Repository overview, compile/run steps & summary of results
├── docs/
│   └── Assignment2_Report.md  # Complete written analysis, predictions, Big-O derivations & Q7 Design Challenge
├── Q1/                        # String vs StringBuilder vs StringBuffer (Railway e-Ticket)
│   ├── TicketBenchmark.java
│   ├── sample_tickets.csv
│   └── README.md
├── Q2/                        # ArrayList vs LinkedList vs ArrayDeque (Music Playlist Editor)
│   ├── PlaylistBenchmark.java
│   ├── playlist_input.txt
│   └── README.md
├── Q3/                        # hashCode-equals Contract (Event Attendance De-duplication)
│   ├── Attendance.java
│   ├── AttendanceInquiry.java
│   ├── gate_scans.csv
│   └── README.md
├── Q4/                        # HashMap vs TreeMap vs LinkedHashMap (Pharmacy Stock Lookup)
│   ├── PharmacyInventoryBenchmark.java
│   ├── medicines_input.csv
│   └── README.md
├── Q5/                        # Generics, Comparable & Comparator (Sports Leaderboard)
│   ├── LeaderboardDemo.java
│   ├── LeaderboardTestSuite.java
│   ├── teams_input.csv
│   └── README.md
├── Q6/                        # PriorityQueue & Fairness (Hospital Emergency Triage)
│   ├── Triage.java
│   ├── TriageBenchmarkAndFairness.java
│   ├── patients_input.csv
│   └── README.md
└── Q7/                        # Day-4 Design Challenge (Diagnose, Redesign & Validate)
    ├── PharmacyRedesignValidation.java
    └── README.md
```

---

## How to Compile and Run

Open a terminal in the repository root directory and run each task using `javac` and `java`:

### Q1 – Railway e-Ticket Generation
```bash
cd Q1
javac TicketBenchmark.java
java TicketBenchmark
```

### Q2 – Music Playlist Editor
```bash
cd Q2
javac PlaylistBenchmark.java
java PlaylistBenchmark
```

### Q3 – Event Attendance De-duplication
```bash
cd Q3
javac Attendance.java AttendanceInquiry.java
java Attendance
java AttendanceInquiry
```

### Q4 – Hospital Pharmacy Stock Lookup
```bash
cd Q4
javac PharmacyInventoryBenchmark.java
java PharmacyInventoryBenchmark
```

### Q5 – Inter-College Sports Leaderboard
```bash
cd Q5
javac LeaderboardDemo.java LeaderboardTestSuite.java
java LeaderboardDemo
java LeaderboardTestSuite
```

### Q6 – Hospital Emergency Triage
```bash
cd Q6
javac Triage.java TriageBenchmarkAndFairness.java
java Triage
java TriageBenchmarkAndFairness
```

### Q7 – Day-4 Design Challenge (Hybrid Indexed Pharmacy Inventory)
```bash
cd Q7
javac PharmacyRedesignValidation.java
java PharmacyRedesignValidation
```

---

## Summary of Results

| Task | Core Topic | Key Experimental Finding |
| :--- | :--- | :--- |
| **Q1** | `String` vs `StringBuilder` vs `StringBuffer` | Repeated `+` exhibits $O(n^2)$ character copying (`186.85 ms` vs `12.04 ms` on $2,000 \times 200$); `StringBuilder` achieves $O(n)$ linear scaling without synchronization overhead. |
| **Q2** | `ArrayList` vs `LinkedList` vs `ArrayDeque` | `ArrayList` dominates $O(1)$ random access and cache-friendly iteration; `ArrayDeque` outperforms both for double-ended "Up Next" queue operations. |
| **Q3** | `hashCode()` & `equals()` Contract | Without overriding `hashCode()`, equal `Student` objects land in distinct hash buckets, causing silent duplicates (`size = 3` instead of `2`). |
| **Q4** | `HashMap` vs `TreeMap` vs `LinkedHashMap` | `HashMap` gives $O(1)$ lookups; `TreeMap` maintains sorted code order ($O(\log n)$); `LinkedHashMap` preserves insertion order ($O(1)$). |
| **Q5** | Generics, `Comparable` & `Comparator` | Null-safe `Comparator.comparing(...).thenComparing(...)` enforces strict weak ordering (transitivity & consistency with `equals`). |
| **Q6** | `PriorityQueue` & Starvation Prevention | Binary min-heap provides $O(\log n)$ `add`/`poll`; aging mechanism prevents indefinite starvation of low-severity patients. |
| **Q7** | Diagnose, Redesign & Validate | Replaced full $O(n \log n)$ evening re-sorts of `LinkedHashMap` with a write-synchronized `LinkedHashMap` + `TreeMap` dual-index architecture (**9.61x faster** at $100,000$ medicines). |

---

## Git Commit Guide (Progressive Commits)
```bash
git init
git add README.md docs/
git commit -m "chore: initialize repository structure and documentation"
git add Q1/ && git commit -m "feat(Q1): implement String vs StringBuilder vs StringBuffer benchmark"
git add Q2/ && git commit -m "feat(Q2): implement ArrayList vs LinkedList vs ArrayDeque playlist benchmark"
git add Q3/ && git commit -m "feat(Q3): implement Student hashCode-equals inquiry and complete Attendance"
git add Q4/ && git commit -m "feat(Q4): implement Pharmacy Map lookup and report benchmark"
git add Q5/ && git commit -m "feat(Q5): implement generic Leaderboard and Comparator test suite"
git add Q6/ && git commit -m "feat(Q6): implement Hospital Triage PriorityQueue and aging fairness"
git add Q7/ && git commit -m "feat(Q7): implement Day-4 Design Challenge diagnosis and dual-index redesign"
```
