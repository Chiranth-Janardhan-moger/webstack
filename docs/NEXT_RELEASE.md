# Next Release Migration Plan: Dropping saved_websites

## Overview
In database schema version 3 (release 1.1.2), the active bookmarks table was renamed and restructured to `websites` with a unique index on `dedupeKey`. The legacy version 2 table, `saved_websites`, was preserved intact as a fail-safe backup during the `MIGRATION_2_3` transition.

This document outlines the roadmap and technical requirements for removing `saved_websites` in release 1.2.0 (database schema version 4).

## Current State (Schema v3)
- Active table: `websites` (contains all active bookmarks with canonical dedupe keys).
- Legacy table: `saved_websites` (read-only copy retained from schema v2).
- Verification status: Verified in `MIGRATION_2_3` that `websites count + dropped duplicates == saved_websites count`.
- UI dependency: Zero UI components read or write to `saved_websites`.

## Target Release: v1.2.0 (Schema v4)

### 1. Database Version Bump
- Increment Room `@Database(version = 4)`.

### 2. Migration Definition (MIGRATION_3_4)
- Create `MIGRATION_3_4` as an `androidx.room.migration.Migration(3, 4)`:
  - Execute: `DROP TABLE IF EXISTS saved_websites;`
  - Reclaim unused storage space.

### 3. Upgrade Paths and Chain Handling
- **Path A (v3 -> v4)**: Applies `MIGRATION_3_4`. Drops `saved_websites`. `websites` table is completely untouched.
- **Path B (v2 -> v4 direct upgrade)**:
  - Room sequentially executes `MIGRATION_2_3` (copies and re-keys from `saved_websites` to `websites`), followed immediately by `MIGRATION_3_4` (drops `saved_websites`).
  - Data integrity is preserved because `MIGRATION_2_3` finishes and commits before `MIGRATION_3_4` executes.

### 4. Verification Requirements
- Instrumented test verifying direct migration from a real v3 database to v4.
- Instrumented test verifying multi-hop migration from a real v2 database directly to v4.
- Assert that `saved_websites` does not appear in `sqlite_master`.
- Assert that `SELECT COUNT(*) FROM websites` matches pre-migration counts.
