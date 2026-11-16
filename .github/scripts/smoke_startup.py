#!/usr/bin/env python3
"""Boot temporary Paper/Rebar/Pylon with Gradle's runServer task.

This does NOT replace live gameplay verification. It checks that all three
plugins load, Rebar sees item/recipe registration, and startup has no errors.
"""
import os
import queue
import re
import signal
import subprocess
import sys
import threading
import time

DEADLINE_SECONDS = 360
LOADED_MARKER = "Loaded unique three-tier talismans"
RECIPE_MARKER = "Recipe registration audit: 33/33"
STOP_MARKERS = ("Done (", "Done!")
FATAL = (
    "Could not load plugin 'Rebar_Relics'",
    "Error occurred while enabling Rebar_Relics",
    "Error occurred while enabling Rebar",
    "Error occurred while enabling Pylon",
    "Failed to load recipe with key 'rebar_relics:",
    "Failed to load recipe",
    "Rebar Relics recipe registration incomplete",
    "Failed to load plugin 'Rebar_Relics'",
    "NoClassDefFoundError",
    "NoSuchMethodError",
    "UnknownDependencyException",
    "ExceptionInInitializerError",
)

def main():
    # Explicitly accept Mojang's EULA for this throwaway automated server.
    # This writes only to the CI checkout, not any player's server directory.
    from pathlib import Path
    run_dir = Path("run")
    run_dir.mkdir(exist_ok=True)
    (run_dir / "eula.txt").write_text("eula=true\n", encoding="utf-8")
    command = ["gradle", "--no-daemon", "runServer"]
    print("Starting disposable Paper server:", " ".join(command), flush=True)
    process = subprocess.Popen(
        command,
        stdin=subprocess.PIPE,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
        text=True,
        encoding="utf-8",
        errors="replace",
        bufsize=1,
        start_new_session=True,
    )
    incoming = queue.Queue()
    def capture():
        try:
            for line in process.stdout:
                incoming.put(line)
        finally:
            incoming.put(None)
    threading.Thread(target=capture, daemon=True).start()

    loaded = False
    started = False
    recipes = False
    fatal_lines = []
    deadline = time.monotonic() + DEADLINE_SECONDS
    with open("smoke-startup.log", "w", encoding="utf-8") as log:
        while time.monotonic() < deadline:
            try:
                line = incoming.get(timeout=1)
            except queue.Empty:
                if process.poll() is not None:
                    break
                continue
            if line is None:
                break
            sys.stdout.write(line)
            log.write(line)
            log.flush()
            if LOADED_MARKER in line:
                loaded = True
            if RECIPE_MARKER in line:
                recipes = True
            if "Done (" in line and "For help, type" in line:
                started = True
            elif re.search(r"Done \([\d.]+s\)!.*help", line):
                started = True
            if any(marker.lower() in line.lower() for marker in FATAL):
                fatal_lines.append(line.strip())
            if loaded and started and recipes:
                print("Disposable Paper startup markers reached.", flush=True)
                break

    if process.poll() is None:
        try:
            process.stdin.write("stop\n")
            process.stdin.flush()
            process.wait(timeout=15)
        except (BrokenPipeError, OSError, subprocess.TimeoutExpired):
            pass
    if process.poll() is None:
        os.killpg(process.pid, signal.SIGTERM)
        try:
            process.wait(timeout=10)
        except subprocess.TimeoutExpired:
            os.killpg(process.pid, signal.SIGKILL)
            process.wait(timeout=5)

    if not loaded or not started or not recipes or fatal_lines:
        print("Startup smoke FAILED: loaded=", loaded, "started=", started, "recipes=", recipes,
              "fatal=", fatal_lines[:15], file=sys.stderr)
        return 1
    print("Startup smoke PASSED (not an in-game behavior test).")
    return 0

if __name__ == "__main__":
    sys.exit(main())
