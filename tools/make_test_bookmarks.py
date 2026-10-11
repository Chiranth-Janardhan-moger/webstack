#!/usr/bin/env python3
"""
tools/make_test_bookmarks.py
Generates testdata/e2e/bookmarks.html and testdata/e2e/expected.json.
Contains approx 200 links with rich edge cases matching Section 8.1.
Answer key is hand-computed according to specification rules.
"""

import os
import json

def build_test_data():
    out_dir = os.path.join(os.path.dirname(os.path.dirname(__file__)), "testdata", "e2e")
    os.makedirs(out_dir, exist_ok=True)
    html_file = os.path.join(out_dir, "bookmarks.html")
    json_file = os.path.join(out_dir, "expected.json")

    # Links definitions:
    # Each entry: (url, title, folder_path, expected_category, is_unsupported, is_duplicate_of)
    links = []

    # 1. Unsupported schemes (5 links)
    links.append(("javascript:alert('xss')", "Bookmarklet Alert", ["Bookmarks bar"], None, True, None))
    links.append(("chrome://settings", "Chrome Settings", ["Bookmarks bar"], None, True, None))
    links.append(("about:blank", "About Blank", ["Bookmarks bar"], None, True, None))
    links.append(("place:type=6&sort=14", "Firefox Place URI", ["Bookmarks bar"], None, True, None))
    links.append(("file:///C:/Users/test/doc.pdf", "Local PDF", ["Bookmarks bar"], None, True, None))

    # 2. Duplicate testing on Linux kernel (4 links -> 1 valid, 3 duplicates)
    # Primary link:
    links.append(("https://github.com/torvalds/linux", "Linux Kernel", ["Bookmarks bar", "Development"], "Development", False, None))
    # Same URL with trailing slash and www:
    links.append(("https://www.github.com/torvalds/linux/", "Linux Kernel with WWW and Slash", ["Bookmarks bar", "Development"], "Development", False, "https://github.com/torvalds/linux"))
    # Same URL with utm tracking:
    links.append(("https://github.com/torvalds/linux?utm_source=newsletter&utm_medium=email", "Linux Kernel Tracking", ["Bookmarks bar", "Development"], "Development", False, "https://github.com/torvalds/linux"))
    # Same URL with fragment:
    links.append(("https://github.com/torvalds/linux#readme", "Linux Kernel Fragment", ["Bookmarks bar", "Development"], "Development", False, "https://github.com/torvalds/linux"))

    # 3. HTTP vs HTTPS stay distinct (2 valid links)
    links.append(("http://example.com/doc", "Example HTTP Doc", ["Bookmarks bar"], "Uncategorized", False, None))
    links.append(("https://example.com/doc", "Example HTTPS Doc", ["Bookmarks bar"], "Uncategorized", False, None))

    # 4. Folder cleaning test cases
    links.append(("https://example.org/print1", "3D Printer Guide", ["Bookmarks bar", "3D Printing"], "3D Printing", False, None))
    links.append(("https://example.org/meal1", "Quick Pasta", ["Bookmarks bar", "5-minute meals"], "5-minute meals", False, None))
    links.append(("https://example.org/template1", "Doc Template", ["Bookmarks bar", "Templates"], "Templates", False, None))
    links.append(("https://example.org/tempo1", "Metronome", ["Bookmarks bar", "Tempo"], "Tempo", False, None))
    links.append(("https://example.org/tempctl1", "HVAC Thermostat", ["Bookmarks bar", "Temperature Control"], "Temperature Control", False, None))
    links.append(("https://example.org/taxes1", "IRS Forms", ["Bookmarks bar", "2024 Taxes"], "2024 Taxes", False, None))
    links.append(("https://example.org/cpu1", "Specs", ["Bookmarks bar", "1.5 GHz CPUs"], "1.5 GHz CPUs", False, None))

    # Order prefix stripping: "01_Tech" -> "Tech", "1. Code" -> "Code", "02 - Design" -> "Design", "3) Tools" -> "Tools"
    links.append(("https://tech.example.com/1", "Tech 1", ["Bookmarks bar", "01_Tech"], "Tech", False, None))
    links.append(("https://code.example.com/1", "Code 1", ["Bookmarks bar", "1. Code"], "Code", False, None))
    links.append(("https://design.example.com/1", "Design 1", ["Bookmarks bar", "02 - Design"], "Design", False, None))
    links.append(("https://tools.example.com/1", "Tools 1", ["Bookmarks bar", "3) Tools"], "Tools", False, None))

    # Nested folder Work/Projects/ML -> leaf ML wins
    links.append(("https://ml.example.com/paper", "Attention Is All You Need", ["Bookmarks bar", "Work", "Projects", "ML"], "ML", False, None))

    # Deepest non-noise folder: leaf is "temp" (noise), parent is "Research"
    links.append(("https://deep.example.com/data", "Deep Data", ["Bookmarks bar", "Research", "temp"], "Research", False, None))

    # Noise folders fallback to taxonomy: "New folder" (noise), host is github.com -> "Development"
    links.append(("https://github.com/android/architecture-samples", "Architecture Samples", ["Bookmarks bar", "New folder"], "Development", False, None))

    # Second-level label matching for Amazon & eBay in noise folders:
    links.append(("https://www.amazon.co.uk/dp/B08N5WRWNW", "Amazon UK Kindle", ["Other bookmarks"], "Shopping", False, None))
    links.append(("https://amazon.com/dp/B07FZ8S74R", "Amazon US Echo", ["Other bookmarks"], "Shopping", False, None))
    links.append(("https://www.ebay.de/itm/123456789", "eBay DE Item", ["Other bookmarks"], "Shopping", False, None))
    links.append(("https://ebay.com/itm/987654321", "eBay US Item", ["Other bookmarks"], "Shopping", False, None))

    # Public Suffix List test cases:
    # bbc.co.uk (single link, unknown domain, size 1 < 3 -> Uncategorized)
    links.append(("https://www.bbc.co.uk/news", "BBC World News", ["Other bookmarks"], "Uncategorized", False, None))
    # alice.github.io (single link, unknown domain, size 1 < 3 -> Uncategorized)
    links.append(("https://alice.github.io/blog/post-1", "Alice Tech Notes", ["Other bookmarks"], "Uncategorized", False, None))

    # Unknown domain with >= 3 links -> cluster named from first label title-cased: "Clusterhub"
    for i in range(1, 5):
        links.append((f"https://sub.clusterhub.org/article/{i}", f"Cluster Hub Article {i}", ["Other bookmarks"], "Clusterhub", False, None))

    # Unknown domain with exactly 2 links -> does NOT cluster -> "Uncategorized"
    links.append(("https://twolinks.net/page1", "Two Links Page 1", ["Other bookmarks"], "Uncategorized", False, None))
    links.append(("https://twolinks.net/page2", "Two Links Page 2", ["Other bookmarks"], "Uncategorized", False, None))

    # Special character titles (single links < 3 -> Uncategorized)
    links.append(("https://unicode1.org/entities", "C++ &amp; Java &lt;Guide&gt;", ["Other bookmarks"], "Uncategorized", False, None))
    links.append(("https://unicode2.org/quotes", "Quotes: &#39;Single&#39; &amp; &quot;Double&quot;", ["Other bookmarks"], "Uncategorized", False, None))
    links.append(("https://unicode3.org/emoji", "Rocket &#x1F680; and Smile &#x1F600;", ["Other bookmarks"], "Uncategorized", False, None))
    links.append(("https://unicode4.org/japanese", "日本語のプログラミング", ["Other bookmarks"], "Uncategorized", False, None))
    links.append(("https://unicode5.org/arabic", "دليل المطورين العربي", ["Other bookmarks"], "Uncategorized", False, None))
    links.append(("https://unicode6.org/multiline", "First Line\nSecond Line\nThird Line", ["Other bookmarks"], "Uncategorized", False, None))

    # Bulk links in Development folder (~40 links)
    for i in range(1, 41):
        links.append((f"https://devsite{i}.org/repo/project-{i}", f"Dev Project {i}", ["Bookmarks bar", "Development"], "Development", False, None))

    # Bulk links in Mobile bookmarks (~120 links from known taxonomy domains)
    taxonomy_domains = [
        ("github.com", "Development"),
        ("gitlab.com", "Development"),
        ("stackoverflow.com", "Development"),
        ("developer.android.com", "Development"),
        ("npmjs.com", "Development"),
        ("dev.to", "Development"),
        ("news.ycombinator.com", "Development"),
        ("medium.com", "Reading"),
        ("substack.com", "Reading"),
        ("arxiv.org", "Reading"),
        ("wikipedia.org", "Reading"),
        ("youtube.com", "Media"),
        ("vimeo.com", "Media"),
        ("spotify.com", "Media"),
        ("netflix.com", "Media"),
        ("reddit.com", "Social"),
        ("x.com", "Social"),
        ("linkedin.com", "Social")
    ]

    mobile_count = 0
    while mobile_count < 120:
        for domain, expected_cat in taxonomy_domains:
            if mobile_count >= 120:
                break
            mobile_count += 1
            idx = mobile_count
            links.append((
                f"https://{domain}/item/{idx}",
                f"{domain} Item {idx}",
                ["Mobile bookmarks"],
                expected_cat,
                False,
                None
            ))

    # Generate HTML content
    # Mimic Chrome export with BOM, CRLF, uppercase tags, DT/p tags, comments, and base64 icon
    bom = "\ufeff"
    header = (
        "<!DOCTYPE NETSCAPE-Bookmark-file-1>\r\n"
        "<!-- This is an automatically generated file.\r\n"
        "     It will be read and overwritten.\r\n"
        "     DO NOT EDIT! -->\r\n"
        "<META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=UTF-8\">\r\n"
        "<TITLE>Bookmarks</TITLE>\r\n"
        "<H1>Bookmarks</H1>\r\n"
        "<DL><p>\r\n"
    )

    # Let's organize links into tree structure
    # Generate tree
    root_nodes = {}
    for item in links:
        path = item[2]
        curr = root_nodes
        for folder in path:
            if folder not in curr:
                curr[folder] = {"_links": [], "_children": {}}
            curr = curr[folder]["_children"]
        # Add to lowest folder _links
        curr_folder = root_nodes
        for folder in path[:-1]:
            curr_folder = curr_folder[folder]["_children"]
        curr_folder[path[-1]]["_links"].append(item)

    lines = []
    lines.append(bom + header)

    # 100 KB base64 icon string for the first link to test memory/ignoring ICON attribute
    large_base64_icon = "A" * (100 * 1024)

    def write_folder(folder_name, data, indent_level):
        indent = "    " * indent_level
        # Check if browser root
        attrs = ""
        if folder_name == "Bookmarks bar":
            attrs = ' PERSONAL_TOOLBAR_FOLDER="true"'
        elif folder_name == "Mobile bookmarks":
            attrs = ' UNFILED_BOOKMARKS_FOLDER="true"'

        lines.append(f'{indent}<DT><H3 ADD_DATE="1710000000" LAST_MODIFIED="1710000000"{attrs}>{folder_name}</H3>\r\n')
        lines.append(f'{indent}<DL><p>\r\n')

        # Links
        for idx, (url, title, _, _, _, _) in enumerate(data["_links"]):
            link_indent = indent + "    "
            icon_attr = f' ICON="data:image/png;base64,{large_base64_icon}"' if idx == 0 and folder_name == "Bookmarks bar" else ""
            # Escape HTML characters in title and URL as standard HTML
            esc_url = url.replace("&", "&amp;").replace('"', "&quot;")
            esc_title = title.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace('"', "&quot;")
            lines.append(f'{link_indent}<DT><A HREF="{esc_url}" ADD_DATE="1710000000"{icon_attr}>{esc_title}</A>\r\n')

        # Empty folder edge case
        if folder_name == "Templates":
            lines.append(f'{indent}    <DT><H3>Empty Subfolder</H3>\r\n{indent}    <DL><p>\r\n{indent}    </DL><p>\r\n')

        # Children
        for child_name, child_data in data["_children"].items():
            write_folder(child_name, child_data, indent_level + 1)

        lines.append(f'{indent}</DL><p>\r\n')

    for root_folder, data in root_nodes.items():
        write_folder(root_folder, data, 1)

    lines.append("</DL><p>\r\n")

    full_html = "".join(lines)
    with open(html_file, "w", encoding="utf-8", newline="") as f:
        f.write(full_html)

    # Now calculate expected.json answer key completely by rules
    total_found = len(links)
    unsupported_links = [l for l in links if l[4]]
    valid_links = [l for l in links if not l[4]]

    # Compute expected duplicates
    unique_links = []
    seen_keys = set()
    dup_count = 0

    for l in valid_links:
        dup_of = l[5]
        if dup_of is not None:
            dup_count += 1
        else:
            unique_links.append(l)

    # Build links map: key -> {url, title, category}
    expected_links_map = {}
    category_counts = {}
    for l in unique_links:
        url, title, _, cat, _, _ = l
        # Clean title matching decoder
        cleaned_title = title.replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", '"').replace("&#39;", "'")
        expected_links_map[url] = {
            "url": url,
            "title": cleaned_title,
            "category": cat
        }
        category_counts[cat] = category_counts.get(cat, 0) + 1

    expected_data = {
        "totalFound": total_found,
        "unsupportedCount": len(unsupported_links),
        "internalDuplicatesCount": dup_count,
        "uniqueInsertedCount": len(unique_links),
        "categoryCounts": category_counts,
        "links": expected_links_map
    }

    with open(json_file, "w", encoding="utf-8") as f:
        json.dump(expected_data, f, indent=2, ensure_ascii=False)

    print(f"Generated {html_file} with {total_found} links.")
    print(f"Generated {json_file}: {len(unique_links)} unique, {dup_count} duplicates, {len(unsupported_links)} unsupported.")

if __name__ == "__main__":
    build_test_data()
