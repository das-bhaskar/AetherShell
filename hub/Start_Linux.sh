#!/bin/bash
# Get the directory of the script
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$DIR"

clear
echo "------------------------------------------------"
echo "        AETHERSHELL HUB IS INITIALIZING         "
echo "------------------------------------------------"
# 2>/dev/null hides the JVM warnings
java -jar hub.jar 2>/dev/null

# Keep terminal open if the process ends
read -p "Press enter to close..."