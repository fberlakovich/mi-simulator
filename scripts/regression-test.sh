#!/bin/bash
#
# Regression test script for MI Simulator
# Compares CLI output between master branch and current branch
#

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
WORK_DIR=$(mktemp -d)
PROGRAMS_DIR="$PROJECT_DIR/src/test/resources/programs"

echo "=== MI Simulator Regression Test ==="
echo "Work directory: $WORK_DIR"
echo ""

# Cleanup on exit
cleanup() {
    rm -rf "$WORK_DIR"
}
trap cleanup EXIT

# Save current branch
CURRENT_BRANCH=$(git -C "$PROJECT_DIR" rev-parse --abbrev-ref HEAD)
echo "Current branch: $CURRENT_BRANCH"

# Build current branch JAR
echo ""
echo "=== Building current branch ($CURRENT_BRANCH) ==="
cd "$PROJECT_DIR"
./gradlew cli-jar -q
cp build/libs/mi-simulator-cli-*.jar "$WORK_DIR/cli-current.jar"

# Checkout master and build
echo ""
echo "=== Building master branch ==="
git stash -q || true
git checkout master -q
./gradlew cli-jar -q
cp build/libs/mi-simulator-cli-*.jar "$WORK_DIR/cli-master.jar"

# Return to current branch
git checkout "$CURRENT_BRANCH" -q
git stash pop -q 2>/dev/null || true

echo ""
echo "=== Running regression tests ==="

PASS=0
FAIL=0
FAILED_TESTS=""

# Find all .mi program files
for program in "$PROGRAMS_DIR"/*.mi; do
    if [ ! -f "$program" ]; then
        continue
    fi

    name=$(basename "$program" .mi)

    # Check for state file
    state_file=""
    if [ -f "$PROGRAMS_DIR/${name}.state" ]; then
        state_file="$PROGRAMS_DIR/${name}.state"
    fi

    # Run on master
    if [ -n "$state_file" ]; then
        java -jar "$WORK_DIR/cli-master.jar" "$program" -state "$state_file" > "$WORK_DIR/master-$name.out" 2>&1 || true
    else
        java -jar "$WORK_DIR/cli-master.jar" "$program" > "$WORK_DIR/master-$name.out" 2>&1 || true
    fi

    # Run on current
    if [ -n "$state_file" ]; then
        java -jar "$WORK_DIR/cli-current.jar" "$program" -state "$state_file" > "$WORK_DIR/current-$name.out" 2>&1 || true
    else
        java -jar "$WORK_DIR/cli-current.jar" "$program" > "$WORK_DIR/current-$name.out" 2>&1 || true
    fi

    # Compare outputs
    if diff -q "$WORK_DIR/master-$name.out" "$WORK_DIR/current-$name.out" > /dev/null 2>&1; then
        echo "  PASS: $name"
        ((PASS++))
    else
        echo "  FAIL: $name"
        ((FAIL++))
        FAILED_TESTS="$FAILED_TESTS $name"

        # Save diff for review
        diff -u "$WORK_DIR/master-$name.out" "$WORK_DIR/current-$name.out" > "$WORK_DIR/diff-$name.txt" 2>&1 || true
    fi
done

# Test with -hex flag
echo ""
echo "=== Testing -hex flag ==="
for program in "$PROGRAMS_DIR"/*.mi; do
    if [ ! -f "$program" ]; then
        continue
    fi

    name=$(basename "$program" .mi)

    java -jar "$WORK_DIR/cli-master.jar" "$program" -hex > "$WORK_DIR/master-$name-hex.out" 2>&1 || true
    java -jar "$WORK_DIR/cli-current.jar" "$program" -hex > "$WORK_DIR/current-$name-hex.out" 2>&1 || true

    if diff -q "$WORK_DIR/master-$name-hex.out" "$WORK_DIR/current-$name-hex.out" > /dev/null 2>&1; then
        echo "  PASS: $name -hex"
        ((PASS++))
    else
        echo "  FAIL: $name -hex"
        ((FAIL++))
        FAILED_TESTS="$FAILED_TESTS ${name}-hex"
        diff -u "$WORK_DIR/master-$name-hex.out" "$WORK_DIR/current-$name-hex.out" > "$WORK_DIR/diff-$name-hex.txt" 2>&1 || true
    fi
done

# Test with -quiet flag
echo ""
echo "=== Testing -quiet flag ==="
for program in "$PROGRAMS_DIR"/*.mi; do
    if [ ! -f "$program" ]; then
        continue
    fi

    name=$(basename "$program" .mi)

    java -jar "$WORK_DIR/cli-master.jar" "$program" -quiet > "$WORK_DIR/master-$name-quiet.out" 2>&1 || true
    java -jar "$WORK_DIR/cli-current.jar" "$program" -quiet > "$WORK_DIR/current-$name-quiet.out" 2>&1 || true

    if diff -q "$WORK_DIR/master-$name-quiet.out" "$WORK_DIR/current-$name-quiet.out" > /dev/null 2>&1; then
        echo "  PASS: $name -quiet"
        ((PASS++))
    else
        echo "  FAIL: $name -quiet"
        ((FAIL++))
        FAILED_TESTS="$FAILED_TESTS ${name}-quiet"
        diff -u "$WORK_DIR/master-$name-quiet.out" "$WORK_DIR/current-$name-quiet.out" > "$WORK_DIR/diff-$name-quiet.txt" 2>&1 || true
    fi
done

echo ""
echo "=== Summary ==="
echo "Passed: $PASS"
echo "Failed: $FAIL"

if [ $FAIL -gt 0 ]; then
    echo ""
    echo "Failed tests:$FAILED_TESTS"
    echo ""
    echo "Diff files saved in: $WORK_DIR"
    echo "Review with: ls $WORK_DIR/diff-*.txt"

    # Don't cleanup so user can inspect diffs
    trap - EXIT
    exit 1
fi

echo ""
echo "All regression tests passed!"
exit 0