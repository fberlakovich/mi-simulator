#!/bin/bash
#
# Run GUI tests in Docker container with Xvfb
# This allows running GUI tests without stealing focus on the host
#

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

echo "=== Building Docker image for GUI tests ==="
docker build -t mi-simulator-test -f "$PROJECT_DIR/Dockerfile.test" "$PROJECT_DIR"

echo ""
echo "=== Running GUI tests in Docker ==="
# Use 4GB container memory to accommodate 1GB JVM heap
docker run --rm -m 4g mi-simulator-test --tests "gui.GuiRegressionTest"

echo ""
echo "=== GUI tests completed ==="
