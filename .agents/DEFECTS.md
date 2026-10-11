# Defects Log: Bookmark Import and Export Engine

## 1. Defect Summary

| ID | Description | Root Cause | Resolution | Verification |
|---|---|---|---|---|
| DEF-01 | JUnit 4 `InvalidTestClassError` in `SeededBugsTest` | Test method `testSeededBug16` used expression body `= runBlocking { ... testFile.delete() }`, returning `Boolean` instead of `void`. | Converted method to block body returning `Unit`. | `SeededBugsTest` executed and passed all 16 tests. |
| DEF-02 | `testdata` path resolution failure in `EndToEndBookmarkTest` | Gradle test runner executes with `user.dir` pointing to `:app` subfolder rather than repository root. | Resolved `projectRoot` checking for existence of `testdata` in current or parent directory. | `EndToEndBookmarkTest` located test files and executed cleanly. |
| DEF-03 | Blank title comparison mismatch in `BookmarkPropertyTest` | Random title generator produced all-whitespace titles. `BookmarkExporter` correctly applied spec rule `s.title.ifBlank { s.url }`, but property test comparison did not mirror this fallback. | Updated test normalization helper to evaluate `title.ifBlank { url }`. | 10,000 property test cases round-trip passed with 100% multiset equality. |
| DEF-04 | Domain clustering collision in synthetic E2E fixture | `make_test_bookmarks.py` added 6 unicode links under `unicode.example.com` in a noise folder. Guava PSL correctly resolved registrable domain to `example.com`, aggregating with 2 root `example.com/doc` links (8 >= 3 threshold) and clustering them into "Example" rather than "Uncategorized". | Updated `make_test_bookmarks.py` to assign distinct domains (`unicode1.org` .. `unicode6.org`) for character set testing. | `EndToEndBookmarkTest` verified against `expected.json`. |
| DEF-05 | Multiline title comparison mismatch in E2E round-trip | Input HTML contained `First Line\nSecond Line\nThird Line`. Netscape export spec mandates normalizing whitespace `[\n\r\t]` to space. Re-import in step 5 thus had space-normalized title. | Updated E2E test assertion to compare against whitespace-normalized baseline. | `EndToEndBookmarkTest` passed with 0 failures. |
