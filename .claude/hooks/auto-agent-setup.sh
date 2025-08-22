#!/bin/bash
# Suggests agent-architect for new projects

if [ ! -f ".claude/VERSION" ]; then
    echo "
╔═══════════════════════════════════════════════════════════════╗
║                 🤖 Welcome to Claude Agents!                  ║
╟───────────────────────────────────────────────────────────────╢
║  Set up project-specific agents:                              ║
║  > Use agent-architect to analyze this project                ║
╚═══════════════════════════════════════════════════════════════╝
"
fi