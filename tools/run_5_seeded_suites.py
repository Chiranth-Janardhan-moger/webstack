import random
import subprocess
import time
import sys

print("==================================================================")
print("=== EXECUTING 5 CONSECUTIVE TEST SUITE RUNS WITH RANDOM SEEDS ===")
print("==================================================================")

seeds = [random.randint(10000000, 999999999) for _ in range(5)]

for i, seed in enumerate(seeds, 1):
    print(f"\n--- RUN {i} of 5 (SEED: {seed}) ---")
    start = time.time()
    cmd = [
        ".\\gradlew.bat",
        "testDebugUnitTest",
        "--rerun-tasks",
        f"-Djunit.jupiter.execution.order.random.seed={seed}",
        f"-Dkotest.framework.random.seed={seed}"
    ]
    res = subprocess.run(cmd, shell=True)
    duration = time.time() - start
    print(f"Run {i} finished in {duration:.2f}s with Exit Code: {res.returncode}")
    if res.returncode != 0:
        print(f"FAILED on run {i} with seed {seed}")
        sys.exit(res.returncode)

print("\nAll 5 runs completed successfully with exit code 0!")
