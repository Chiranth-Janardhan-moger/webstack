# Performance Benchmarks: Bookmark Import and Export Engine

All benchmarks were executed on an AMD Ryzen / Windows test environment via Robolectric unit test execution with in-memory SQLite and local file systems.

---

## 1. Measured Benchmarks

| Operation | Scale | Measured Time | Throughput | Evidence Source |
|---|---|---|---|---|
| Fuzz / Resiliency Parsing | 10,000 corrupted files | 0.431 s | ~23,200 inputs/sec | `TEST-com.example.bookmarks.BookmarkPropertyTest.xml` |
| Round-Trip Export, Parse & Classify | 10,000 bookmarks | 0.638 s | ~15,670 bookmarks/sec | `TEST-com.example.bookmarks.BookmarkPropertyTest.xml` |
| Bulk Database Import (Room SQLite) | 10,000 rows (chunked 500) | 0.901 s | ~11,100 inserts/sec | `TEST-com.example.bookmarks.BookmarkRoomTransactionTest.xml` |
| Complete Real-File Pipeline | 64 links (65 KB HTML with base64 favicons) | 0.366 s | 1 complete cycle | `TEST-com.example.bookmarks.RealBookmarksTest.xml` |
| Bulk Room Import (Scale) | 50,000 rows (chunked 500) | 9.713 s | ~5,147 inserts/sec | `TEST-com.example.bookmarks.BookmarkScaleBenchmarkTest.xml` |
| Streaming Export (Scale) | 50,000 links | 0.656 s | ~76,219 bookmarks/sec | `TEST-com.example.bookmarks.BookmarkScaleBenchmarkTest.xml` |
| Streaming Parse (Scale) | 50,000 links | 1.235 s | ~40,485 bookmarks/sec | `TEST-com.example.bookmarks.BookmarkScaleBenchmarkTest.xml` |
| Full 50k Room Round-Trip | 50,000 links (import + export + parse) | 11.604 s | ~4,308 complete cycles/sec | `TEST-com.example.bookmarks.BookmarkScaleBenchmarkTest.xml` |
| 30 MB Embedded Base64 Favicon File | 30.81 MB (1,000 links with ~30 KB data URIs) | 1.788 s | ~17.2 MB/s streaming throughput | `TEST-com.example.bookmarks.BookmarkScaleBenchmarkTest.xml` |

---

## 2. Resource Utilization & Architectural Constraints

1. **Streaming Memory Ceiling**:
   - Both `BookmarkParser` and `BookmarkExporter` use `64 * 1024` (64 KB) character buffers (`BufferedReader` and `BufferedWriter`).
   - The entire HTML file is never loaded into a single string.
   - Tag tokenization is capped at 1 MB per tag (`MAX_TAG_SIZE = 1024 * 1024`). Excessive tags trigger resynchronization at the next `<` without crashing or allocating unbounded memory.
   - Favicons embedded in `ICON="data:image/png;base64,..."` attributes are ignored during tokenization and not retained in memory.

2. **Atomic Room Batching**:
   - Persistence is wrapped in `db.withTransaction { ... }` so the entire batch runs inside one SQLite transaction.
   - Chunks of 500 items are passed to `dao.insertAll(chunk)` for progress notification and memory pacing.
   - If an exception occurs at any point, all chunks are rolled back, leaving 0 rows committed.

3. **Heap Measurement Methodology**:
   - Heap allocation was measured before and after the 50,000 entity Room database import and round-trip lifecycle using `Runtime.getRuntime()`:
     ```kotlin
     System.gc()
     val heapBefore = runtime.totalMemory() - runtime.freeMemory()
     // ... execute 50,000 entity import, export, and parse ...
     System.gc()
     val heapAfter = runtime.totalMemory() - runtime.freeMemory()
     val heapDeltaMB = (heapAfter - heapBefore).toDouble() / (1024 * 1024)
     ```
   - Measured Heap Delta for 50,000 persistent items: **2.24 MB**.
   - Measured Heap Delta for 30 MB HTML file with base64 icons: **0.00 MB** (streaming tokenizer drops base64 icon data during lexing without object retention).
