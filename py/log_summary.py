#!/usr/bin/env python3
"""log_summary.py - quick metadata scan for AdvantageKit .wpilog logs.

AdvantageKit's .wpilog files are a binary format; fully parsing them (telemetry,
state machines, motor currents, timing, etc.) is best done with a .wpilog-aware
tool, e.g.:

  * the community MCP server "artemis" (GabrielNakamoto/artemis) - reads .wpilog
    (apollo) and live NT4, so an AI agent can analyze logs and tune in closed loop
  * replaying with:  .\\gradlew.bat replayWatch   (AdvantageScope GUI)

This script is a thin helper for the "find the log" step of the closed-loop
workflow (edit -> simulateJava -> read newest log -> verify). It locates the
newest .wpilog and prints basic info for quick triage.

Usage:
    python py/log_summary.py                 # newest sim log
    python py/log_summary.py --log logs/sim/akit_26-03-13_23-09-41.wpilog
    python py/log_summary.py --live          # show robot logs too (logs/)
    python py/log_summary.py --table <log>   # print a text/hex preview header
"""

import argparse
import glob
import os
import sys
import time


def newest_log(dirs, pattern="*.wpilog"):
    candidates = []
    for d in dirs:
        if os.path.isdir(d):
            candidates += glob.glob(os.path.join(d, pattern))
    if not candidates:
        return None
    return max(candidates, key=os.path.getmtime)


def preview(path, nbytes=64):
    with open(path, "rb") as handle:
        head = handle.read(nbytes)
    return head.hex(" ")


def main():
    ap = argparse.ArgumentParser(description=__doc__)
    ap.add_argument("--log", help="explicit path to a .wpilog file")
    ap.add_argument("--dir", nargs="*", default=["logs/sim", "logs"],
                    help="dirs to search for the newest log (default: logs/sim logs)")
    ap.add_argument("--hex", type=int, default=64,
                    help="print a hex preview of the first N bytes (default 64)")
    args = ap.parse_args()

    dirs = list(args.dir)
    path = args.log or newest_log(dirs)
    if not path:
        print("No .wpilog file found in:", args.dir)
        sys.exit(1)

    stat = os.stat(path)
    print(f"LOG     : {os.path.abspath(path)}")
    print(f"SIZE    : {stat.st_size:,} bytes")
    print(f"MODIFIED: {time.ctime(stat.st_mtime)}")

    if args.hex:
        print(f"HEAD    : {preview(path, args.hex)}")

    base = path.lower()
    if "_sim" in base or os.sep + "sim" + os.sep in path.replace("\\", os.sep):
        print("SOURCE  : simulator (AdvantageKit) -> use replayWatch or an MCP/.wpilog reader for analysis")
    else:
        print("SOURCE  : robot log -> use replayWatch or an MCP/.wpilog reader for analysis")

    print("HINT    : for full analysis, point a .wpilog-aware tool (e.g. artemis MCP) at this file.")
    sys.exit(0)


if __name__ == "__main__":
    main()