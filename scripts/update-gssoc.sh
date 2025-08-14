#!/bin/bash

# GSSoC Issue Tracker Update Script
# This script updates the README.md with current GSSoC issues

echo "🌟 Updating GSSoC Issue Tracker..."

# Check if Python is available
if ! command -v python3 &> /dev/null; then
    echo "❌ Python 3 is required but not installed."
    exit 1
fi

# Install requirements if needed
if [ -f "scripts/requirements.txt" ]; then
    echo "📦 Installing Python dependencies..."
    pip3 install -r scripts/requirements.txt
fi

# Run the updater script
echo "🔄 Running GSSoC issue tracker updater..."
python3 scripts/update-gssoc-issues.py

echo "✅ GSSoC issue tracker update completed!"