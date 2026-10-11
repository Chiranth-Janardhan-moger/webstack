#!/usr/bin/env python3
"""
tools/validate_netscape.py
Independent structural validator for Netscape bookmark files.
Checks:
- UTF-8 decoding
- First line DOCTYPE
- Balanced <DL> / </DL> tags
- Every <H3> followed by a <DL>
- Every HREF is http or https
- ADD_DATE attributes are 10 digits (epoch seconds)
- No unescaped entities or raw special characters
"""

import sys
import re
from bs4 import BeautifulSoup

def validate_netscape_file(file_path: str):
    errors = []

    # 1. UTF-8 decoding
    try:
        with open(file_path, "rb") as f:
            raw_bytes = f.read()
        raw_text = raw_bytes.decode("utf-8")
    except UnicodeDecodeError as e:
        return [f"Encoding error: file does not decode as UTF-8: {e}"]

    # 2. First line is DOCTYPE
    lines = raw_text.splitlines()
    if not lines or not lines[0].strip().startswith("<!DOCTYPE NETSCAPE-Bookmark-file-1>"):
        errors.append(f"Header error: First line is not DOCTYPE. Found: {lines[0] if lines else 'empty'}")

    # 3. DL tags balanced
    dl_open = len(re.findall(r"<DL\b", raw_text, re.IGNORECASE))
    dl_close = len(re.findall(r"</DL>", raw_text, re.IGNORECASE))
    if dl_open != dl_close:
        errors.append(f"Balance error: <DL> count ({dl_open}) != </DL> count ({dl_close})")

    # 4. Check no raw unescaped '&' in text
    # Any '&' must be part of an entity like &amp;, &lt;, &gt;, &quot;, &#39;, &#NN;, &#xHH;
    entity_regex = re.compile(r"&(#x[0-9a-fA-F]+|#[0-9]+|[a-zA-Z]+);")
    pos = 0
    while True:
        idx = raw_text.find("&", pos)
        if idx == -1:
            break
        # Check if matches entity
        m = entity_regex.match(raw_text, idx)
        if not m:
            snippet = raw_text[max(0, idx - 10):min(len(raw_text), idx + 15)].replace('\n', ' ')
            errors.append(f"Entity error: Raw unescaped '&' found near: '{snippet}'")
            break
        pos = m.end()

    # 5. Parse with BeautifulSoup html5lib / html.parser
    try:
        soup = BeautifulSoup(raw_text, "html.parser")
    except Exception as e:
        errors.append(f"HTML parse error: {e}")
        return errors

    # Check every H3 is followed by DL
    for h3 in soup.find_all("h3"):
        next_tag = h3.find_next_sibling()
        # Look forward for the next tag
        curr = h3.next_element
        found_dl = False
        while curr:
            if getattr(curr, 'name', None) == 'dl':
                found_dl = True
                break
            if getattr(curr, 'name', None) in ['h3', 'a']:
                break
            curr = curr.next_element
        if not found_dl:
            errors.append(f"Structure error: <H3> '{h3.text.strip()}' is not followed by <DL>")

    # Check every A tag
    for a in soup.find_all("a"):
        href = a.get("href")
        if not href:
            errors.append(f"Link error: <A> tag missing href: {a}")
        else:
            if not (href.startswith("http://") or href.startswith("https://")):
                errors.append(f"Scheme error: Link href is not http(s): '{href}'")

        add_date = a.get("add_date")
        if add_date is not None:
            if not (re.match(r"^\d{10}$", add_date) or (add_date == "0")):
                errors.append(f"Timestamp error: ADD_DATE '{add_date}' is not 10 digits (epoch seconds)")

    return errors

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python validate_netscape.py <file.html>", file=sys.stderr)
        sys.exit(1)

    file_to_check = sys.argv[1]
    errs = validate_netscape_file(file_to_check)
    if errs:
        print(f"Validation FAILED for {file_to_check} with {len(errs)} error(s):", file=sys.stderr)
        for err in errs:
            print(f"  - {err}", file=sys.stderr)
        sys.exit(1)
    else:
        print(f"Validation PASSED for {file_to_check}")
        sys.exit(0)
