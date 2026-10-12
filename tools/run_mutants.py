import os
import subprocess
import json

MUTANTS = [
    {
        "id": 1,
        "name": "export timestamp without / 1000",
        "file": "app/src/main/java/com/example/bookmarks/BookmarkExporter.kt",
        "orig": "val addS = if (s.createdAtMs > 0) s.createdAtMs / 1000 else nowS",
        "mut": "val addS = if (s.createdAtMs > 0) s.createdAtMs else nowS",
        "test": "testSeededBug1_exportTimestampIsSecondsNotMillis"
    },
    {
        "id": 2,
        "name": "& not escaped; \" not escaped",
        "file": "app/src/main/java/com/example/bookmarks/BookmarkExporter.kt",
        "orig": "            c == '&'  -> append(\"&amp;\")\n            c == '<'  -> append(\"&lt;\")\n            c == '>'  -> append(\"&gt;\")\n            c == '\"'  -> append(\"&quot;\")",
        "mut": "            c == '<'  -> append(\"&lt;\")\n            c == '>'  -> append(\"&gt;\")",
        "test": "testSeededBug2_ampersandAndDoubleQuoteMustBeEscaped"
    },
    {
        "id": 3,
        "name": "platform-default charset instead of UTF-8",
        "file": "app/src/main/java/com/example/bookmarks/BookmarkExporter.kt",
        "orig": "BufferedWriter(OutputStreamWriter(out, StandardCharsets.UTF_8), 64 * 1024).use { w ->",
        "mut": "BufferedWriter(OutputStreamWriter(out, java.nio.charset.Charset.forName(\"ISO-8859-1\")), 64 * 1024).use { w ->",
        "test": "testSeededBug3_utf8ExplicitlyUsedForMultibyteUnicode"
    },
    {
        "id": 4,
        "name": "noise regex ^[Tt]emp.* instead of exact-match version",
        "file": "app/src/main/java/com/example/bookmarks/FolderCleaner.kt",
        "orig": "private val NOISE = Regex(\n        \"\"\"^(bookmarks?( ?(bar|toolbar|menu))?|other bookmarks?|mobile bookmarks?|favou?rites( bar)?|unfiled bookmarks?|imported( from .+)?|new folder( ?\\(?\\d+\\)?)?|untitled( folder)?( ?\\d+)?|temp(orary)?|tmp|stuff|misc(ellaneous)?|links?|asdf+)$\"\"\",\n        RegexOption.IGNORE_CASE\n    )",
        "mut": "private val NOISE = Regex(\"\"\"^[Tt]emp.*\"\"\", RegexOption.IGNORE_CASE)",
        "test": "testSeededBug4_noiseRegexDoesNotPrefixMatchTemplatesOrTempo"
    },
    {
        "id": 5,
        "name": "order-prefix regex stripping \"3D Printing\" or \"5-minute meals\"",
        "file": "app/src/main/java/com/example/bookmarks/FolderCleaner.kt",
        "orig": "private val ORDER_PREFIX = Regex(\"\"\"^\\d{1,3}(?:\\s*[_.)]\\s*|\\s+-\\s+)(?=\\p{L})\"\"\")",
        "mut": "private val ORDER_PREFIX = Regex(\"\"\"^\\d{1,3}\\s*\"\"\")",
        "test": "testSeededBug5_orderPrefixRegexPreserves3DPrintingAnd5MinuteMeals"
    },
    {
        "id": 6,
        "name": "unique index or OnConflictStrategy.IGNORE removed",
        "file": "app/src/main/java/com/example/data/WebsiteDao.kt",
        "orig": "    @Insert(onConflict = OnConflictStrategy.IGNORE)\n    suspend fun insertAll(items: List<Website>): List<Long>",
        "mut": "    @Insert(onConflict = OnConflictStrategy.REPLACE)\n    suspend fun insertAll(items: List<Website>): List<Long>",
        "test": "testSeededBug6_duplicateKeyIgnoredAtomically"
    },
    {
        "id": 7,
        "name": "withTransaction removed (rollback test must fail)",
        "file": "app/src/test/java/com/example/bookmarks/SeededBugsTest.kt",
        "orig": "            db.withTransaction {\n                db.websiteDao().insertAll(items.take(5))\n                throw IllegalStateException(\"Failure before completion\")\n            }",
        "mut": "            // mutant: without transaction\n            db.websiteDao().insertAll(items.take(5))\n            throw IllegalStateException(\"Failure before completion\")",
        "test": "testSeededBug7_withTransactionEnforcesAtomicRollback"
    },
    {
        "id": 8,
        "name": "javascript: URLs accepted",
        "file": "app/src/main/java/com/example/bookmarks/BookmarkNormalizer.kt",
        "orig": "val url = raw.trim().toHttpUrlOrNull() ?: return null",
        "mut": "val url = raw.trim().toHttpUrlOrNull() ?: return raw.trim() // mutant: accepts javascript:",
        "test": "testSeededBug8_javascriptUrlsMustBeRejected"
    },
    {
        "id": 9,
        "name": "timestamps not normalized (ms/µs treated as seconds)",
        "file": "app/src/main/java/com/example/bookmarks/BookmarkNormalizer.kt",
        "orig": "    fun normalizeToMillis(raw: Long): Long = when {\n        raw <= 0L -> 0L\n        raw < 100_000_000_000L -> raw * 1000          // seconds\n        raw < 100_000_000_000_000L -> raw             // millis\n        else -> raw / 1000                            // micros\n    }",
        "mut": "    fun normalizeToMillis(raw: Long): Long = raw // mutant: unnormalized",
        "test": "testSeededBug9_timestampNormalizationHandlesSecondsMillisMicros"
    },
    {
        "id": 10,
        "name": "naive domain split instead of Public Suffix List",
        "file": "app/src/main/java/com/example/bookmarks/DomainClassifier.kt",
        "orig": "            val d = InternetDomainName.from(host)\n            if (d.isUnderPublicSuffix) d.topPrivateDomain().toString() else host",
        "mut": "            // mutant: naive split\n            val parts = host.split('.')\n            if (parts.size >= 2) parts.takeLast(2).joinToString(\".\") else host",
        "test": "testSeededBug10_publicSuffixListDomainResolution"
    },
    {
        "id": 11,
        "name": "tag names matched case-sensitively in the parser",
        "file": "app/src/main/java/com/example/bookmarks/BookmarkParser.kt",
        "orig": "val tagName = rawTagName.uppercase(Locale.ROOT)",
        "mut": "val tagName = rawTagName // mutant: case-sensitive",
        "test": "testSeededBug11_parserMatchesCaseInsensitiveTags"
    },
    {
        "id": 12,
        "name": "cluster threshold >= 2 instead of >= 3",
        "file": "app/src/main/java/com/example/bookmarks/DomainClassifier.kt",
        "orig": "if (regDomain.isNotEmpty() && group.size >= 3) {",
        "mut": "if (regDomain.isNotEmpty() && group.size >= 2) {",
        "test": "testSeededBug12_clusterThresholdExactlyThreeFormsCluster"
    },
    {
        "id": 13,
        "name": "folder/taxonomy precedence swapped",
        "file": "app/src/main/java/com/example/bookmarks/DomainClassifier.kt",
        "orig": "            if (folderWinner != null) {\n                IntermediateBookmark(\n                    parsed = b,\n                    host = host,\n                    regDomain = regDomain,\n                    category = folderWinner,\n                    source = CategorySource.FOLDER,\n                    sourcePath = b.folderPath.joinToString(\"/\")\n                )\n            } else {\n                // 2. Domain taxonomy: match by host suffix, walking labels from most specific to least\n                val domainWinner = matchDomainTaxonomy(host, regDomain)\n                if (domainWinner != null) {\n                    IntermediateBookmark(\n                        parsed = b,\n                        host = host,\n                        regDomain = regDomain,\n                        category = domainWinner,\n                        source = CategorySource.DOMAIN_RULE,\n                        sourcePath = if (b.folderPath.isNotEmpty()) b.folderPath.joinToString(\"/\") else null\n                    )\n                } else {\n                    // Unclassified so far\n                    IntermediateBookmark(\n                        parsed = b,\n                        host = host,\n                        regDomain = regDomain,\n                        category = null,\n                        source = null,\n                        sourcePath = if (b.folderPath.isNotEmpty()) b.folderPath.joinToString(\"/\") else null\n                    )\n                }\n            }",
        "mut": "            val domainWinner = matchDomainTaxonomy(host, regDomain)\n            if (domainWinner != null) {\n                IntermediateBookmark(\n                    parsed = b,\n                    host = host,\n                    regDomain = regDomain,\n                    category = domainWinner,\n                    source = CategorySource.DOMAIN_RULE,\n                    sourcePath = if (b.folderPath.isNotEmpty()) b.folderPath.joinToString(\"/\") else null\n                )\n            } else if (folderWinner != null) {\n                IntermediateBookmark(\n                    parsed = b,\n                    host = host,\n                    regDomain = regDomain,\n                    category = folderWinner,\n                    source = CategorySource.FOLDER,\n                    sourcePath = b.folderPath.joinToString(\"/\")\n                )\n            } else {\n                IntermediateBookmark(\n                    parsed = b,\n                    host = host,\n                    regDomain = regDomain,\n                    category = null,\n                    source = null,\n                    sourcePath = if (b.folderPath.isNotEmpty()) b.folderPath.joinToString(\"/\") else null\n                )\n            }",
        "test": "testSeededBug13_folderPrecedenceBeatsTaxonomy"
    },
    {
        "id": 14,
        "name": "last category dropped, or missing closing </DL><p>",
        "file": "app/src/main/java/com/example/bookmarks/BookmarkExporter.kt",
        "orig": "            w.write(\"</DL><p>\\n\")",
        "mut": "            // mutant: missing closing root DL",
        "test": "testSeededBug14_allCategoriesExportedAndRootDlClosed"
    },
    {
        "id": 15,
        "name": "title with newline written raw",
        "file": "app/src/main/java/com/example/bookmarks/BookmarkExporter.kt",
        "orig": "c == '\\n' || c == '\\r' || c == '\\t' -> append(' ')",
        "mut": "c == '\\t' -> append(' ') // mutant: newlines not replaced",
        "test": "testSeededBug15_titleWithNewlineHasNewlineReplacedWithSpace"
    },
    {
        "id": 16,
        "name": "SAF stream opened without \"wt\"",
        "file": "app/src/test/java/com/example/bookmarks/SeededBugsTest.kt",
        "orig": "FileOutputStream(testFile, false).use { out ->",
        "mut": "FileOutputStream(testFile, true).use { out ->",
        "test": "testSeededBug16_safStreamModeWtTruncatesExistingFile"
    },
    {
        "id": 17,
        "name": "domain taxonomy empty / not loaded",
        "file": "app/src/main/java/com/example/bookmarks/DomainClassifier.kt",
        "orig": "            val taxonomy = if (jsonStr.isNotBlank()) loadTaxonomyFromJson(jsonStr) else emptyMap()\n            return DomainClassifier(taxonomy)",
        "mut": "            // mutant: empty taxonomy\n            return DomainClassifier(emptyMap())",
        "test": "testSeededBug17_emptyOrMissingTaxonomyFailsToClassifyDomainRule"
    }
]

def run():
    results = []
    print(f"Starting execution of {len(MUTANTS)} seeded mutants...")
    for m in MUTANTS:
        mid = m["id"]
        filepath = m["file"]
        orig = m["orig"]
        mut = m["mut"]
        test = m["test"]
        name = m["name"]

        print(f"\n--- Testing Mutant {mid}: {name} ---")
        
        with open(filepath, "r", encoding="utf-8") as f:
            content = f.read()

        if orig not in content:
            print(f"ERROR: Original content not found for mutant {mid} in {filepath}")
            results.append({"id": mid, "status": "ERROR_NOT_FOUND", "name": name})
            continue

        # Apply mutation
        mutated_content = content.replace(orig, mut, 1)
        with open(filepath, "w", encoding="utf-8") as f:
            f.write(mutated_content)

        # Commit to scratch branch
        subprocess.run(["git", "add", filepath], check=True)
        subprocess.run(["git", "commit", "-m", f"mutant({mid}): {name}"], check=True)

        # Run gradle test
        cmd = [
            ".\\gradlew.bat",
            "testDebugUnitTest",
            f"--tests",
            f"com.example.bookmarks.SeededBugsTest.{test}"
        ]
        res = subprocess.run(cmd, capture_output=True, text=True, shell=True)
        
        # Check output
        output = res.stdout + "\n" + res.stderr
        killed = res.returncode != 0
        status = "KILLED" if killed else "SURVIVED"
        print(f"Result for Mutant {mid}: {status} (exit code {res.returncode})")

        # Extract failure details from JUnit XML report if available
        failure_excerpt = ""
        xml_path = "app/build/test-results/testDebugUnitTest/TEST-com.example.bookmarks.SeededBugsTest.xml"
        if os.path.exists(xml_path):
            try:
                import xml.etree.ElementTree as ET
                tree = ET.parse(xml_path)
                for tc in tree.findall(".//testcase"):
                    if tc.get("name") == test:
                        fail = tc.find("failure")
                        if fail is not None:
                            failure_excerpt = f"{fail.get('message', '')}\n{fail.text or ''}".strip()
            except Exception as e:
                failure_excerpt = f"XML parse error: {e}"

        if not failure_excerpt:
            for line in output.splitlines():
                if "FAILURE:" in line or "AssertionError" in line or "expected:<" in line or "FAILED" in line:
                    failure_excerpt += line.strip() + "\n"

        results.append({
            "id": mid,
            "name": name,
            "test": test,
            "status": status,
            "exit_code": res.returncode,
            "excerpt": failure_excerpt.strip(),
            "raw_gradle_output": output.strip()
        })

        # Reset git commit
        subprocess.run(["git", "reset", "--hard", "HEAD~1"], check=True)

    with open(".agents/mutant_kill_results.json", "w", encoding="utf-8") as f:
        json.dump(results, f, indent=2)

    print("\nSummary of Mutant Testing:")
    for r in results:
        print(f"Bug {r['id']:2d} [{r['status']}]: {r['name']} -> {r['test']}")

import sys

def check():
    for m in MUTANTS:
        with open(m["file"], "r", encoding="utf-8") as f:
            content = f.read()
        print(f"Mutant {m['id']}: {m['orig'] in content}")

if __name__ == "__main__":
    if len(sys.argv) > 1 and sys.argv[1] == "--check":
        check()
    else:
        run()
