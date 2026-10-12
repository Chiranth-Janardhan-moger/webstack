import xml.etree.ElementTree as ET
import glob
import os

files = glob.glob('app/build/test-results/testDebugUnitTest/**/TEST-*.xml', recursive=True)
total_tests = 0
total_failures = 0
total_errors = 0
total_skipped = 0

suites = []

for f in sorted(files):
    tree = ET.parse(f)
    root = tree.getroot()
    name = root.attrib.get('name', os.path.basename(f))
    tests = int(root.attrib.get('tests', 0))
    failures = int(root.attrib.get('failures', 0))
    errors = int(root.attrib.get('errors', 0))
    skipped = int(root.attrib.get('skipped', 0))
    time = float(root.attrib.get('time', 0.0))
    
    total_tests += tests
    total_failures += failures
    total_errors += errors
    total_skipped += skipped
    
    suites.append((name.split('.')[-1], tests, failures, errors, time))

print('========================================================================================')
print(f'| {"Suite Name":<35} | {"Tests":<7} | {"Failures":<8} | {"Errors":<6} | {"Time (s)":<8} |')
print('========================================================================================')
for name, t, f, e, tm in sorted(suites, key=lambda x: -x[1]):
    print(f'| {name:<35} | {t:<7} | {f:<8} | {e:<6} | {tm:<8.3f} |')
print('========================================================================================')
print(f'| {"TOTAL":<35} | {total_tests:<7} | {total_failures:<8} | {total_errors:<6} |')
print('========================================================================================')
