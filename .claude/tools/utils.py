#!/usr/bin/env python3
"""Shared utilities for TradeGuru CLI toolkit."""

import json
import os
import sys
import hashlib
import time
from pathlib import Path
from typing import Any, Optional


# --- Environment Loading ---

_env_loaded = False

def load_env(root: Optional[Path] = None) -> dict:
    """Load .env file from project root. Returns dict of loaded vars."""
    global _env_loaded
    if _env_loaded:
        return {}

    search_paths = []
    if root:
        search_paths.append(root / ".env")
    search_paths.append(Path.cwd() / ".env")
    # Also check parent dirs
    current = Path.cwd()
    for parent in [current, *current.parents]:
        candidate = parent / ".env"
        if candidate.exists():
            search_paths.insert(0, candidate)
            break

    loaded = {}
    for env_path in search_paths:
        if env_path.exists():
            for line in env_path.read_text(encoding="utf-8").splitlines():
                line = line.strip()
                if not line or line.startswith("#"):
                    continue
                if "=" in line:
                    key, _, value = line.partition("=")
                    key = key.strip()
                    value = value.strip().strip("'\"")
                    if key and key not in os.environ:
                        os.environ[key] = value
                        loaded[key] = value
            _env_loaded = True
            break

    return loaded


def get_env(key: str, required: bool = False, default: Optional[str] = None) -> Optional[str]:
    """Get environment variable with optional requirement check."""
    value = os.environ.get(key, default)
    if required and not value:
        error(f"Missing required environment variable: {key}")
        error(f"Add it to your .env file or export it: export {key}=your_value")
        error(f"See .env.example for all required variables.")
        sys.exit(1)
    return value


# --- Console Output ---

def success(msg: str) -> None:
    print(f"\033[32m✓\033[0m {msg}")

def error(msg: str) -> None:
    print(f"\033[31m✗\033[0m {msg}", file=sys.stderr)

def info(msg: str) -> None:
    print(f"\033[34m→\033[0m {msg}")

def warn(msg: str) -> None:
    print(f"\033[33m!\033[0m {msg}")


# --- Project Root ---

def find_project_root(start: Optional[str] = None) -> Path:
    """Walk up from start (or cwd) to find project root (contains rork.json or .claude/)."""
    current = Path(start or os.getcwd()).resolve()
    # Priority 1: rork.json
    for parent in [current, *current.parents]:
        if (parent / "rork.json").exists():
            return parent
    # Priority 2: .claude/ directory (but not ~/.claude)
    home = Path.home()
    for parent in [current, *current.parents]:
        if (parent / ".claude").exists() and parent != home:
            return parent
    print(f"\033[33m!\033[0m No project root found. Using current directory: {current}", file=sys.stderr)
    return current


def get_app_path(root: Path, app_path: Optional[str] = None) -> Path:
    """Resolve app path. Auto-detect if only one Swift app exists."""
    if app_path:
        p = root / app_path
        if p.exists():
            return p
        error(f"App path not found: {p}")
        sys.exit(1)

    # Auto-detect: look for directories containing .xcodeproj or Package.swift
    candidates = []
    for child in root.iterdir():
        if child.is_dir() and not child.name.startswith("."):
            if list(child.glob("*.xcodeproj")) or (child / "Package.swift").exists():
                candidates.append(child)

    if len(candidates) == 1:
        return candidates[0]
    elif len(candidates) == 0:
        error("No Swift app found in project root.")
        sys.exit(1)
    else:
        error(f"Multiple apps found: {[c.name for c in candidates]}. Specify --app-path.")
        sys.exit(1)


# --- JSON Helpers ---

def read_json(path: Path) -> dict:
    """Read and parse a JSON file."""
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except FileNotFoundError:
        return {}
    except json.JSONDecodeError as e:
        error(f"Invalid JSON in {path}: {e}")
        return {}


def write_json(path: Path, data: Any, indent: int = 2) -> None:
    """Write data as formatted JSON."""
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=indent, ensure_ascii=False) + "\n", encoding="utf-8")


# --- Asset Tracking ---

ASSETS_REGISTRY = ".claude/generated_assets.json"

def get_assets_registry(root: Path) -> Path:
    return root / ASSETS_REGISTRY


def load_assets(root: Path) -> list:
    """Load the generated assets registry."""
    reg = get_assets_registry(root)
    data = read_json(reg)
    return data.get("assets", [])


def save_asset_record(root: Path, record: dict) -> None:
    """Append an asset record to the registry."""
    reg = get_assets_registry(root)
    data = read_json(reg)
    if "assets" not in data:
        data["assets"] = []
    data["assets"].append(record)
    write_json(reg, data)


# --- Background Job Tracking ---

JOBS_FILE = ".claude/background_jobs.json"

def get_jobs_file(root: Path) -> Path:
    return root / JOBS_FILE


def save_job(root: Path, job_id: str, job_type: str, params: dict) -> None:
    """Save a background job for later retrieval."""
    jobs_path = get_jobs_file(root)
    data = read_json(jobs_path)
    if "jobs" not in data:
        data["jobs"] = {}
    data["jobs"][job_id] = {
        "type": job_type,
        "params": params,
        "status": "pending",
        "created_at": time.time(),
    }
    write_json(jobs_path, data)


def get_job(root: Path, job_id: str) -> Optional[dict]:
    """Retrieve a background job by ID."""
    data = read_json(get_jobs_file(root))
    return data.get("jobs", {}).get(job_id)


def update_job(root: Path, job_id: str, updates: dict) -> None:
    """Update a background job's status/result."""
    jobs_path = get_jobs_file(root)
    data = read_json(jobs_path)
    if job_id in data.get("jobs", {}):
        data["jobs"][job_id].update(updates)
        write_json(jobs_path, data)


def generate_job_id(prefix: str = "gen") -> str:
    """Generate a unique job ID."""
    return f"{prefix}_{hashlib.sha256(f'{time.time()}'.encode()).hexdigest()[:12]}"


# --- Asset Catalog Scanning ---

def scan_xcassets(app_dir: Path) -> list:
    """Scan .xcassets directories for existing image assets."""
    assets = []
    for xcassets in app_dir.rglob("*.xcassets"):
        for asset_dir in list(xcassets.rglob("*.imageset")) + list(xcassets.rglob("*.appiconset")):
            contents_json = asset_dir / "Contents.json"
            if contents_json.exists():
                contents = read_json(contents_json)
                images = [
                    img.get("filename")
                    for img in contents.get("images", [])
                    if img.get("filename")
                ]
                assets.append({
                    "name": asset_dir.stem,
                    "type": "icon" if asset_dir.suffix == ".appiconset" else "image",
                    "path": str(asset_dir.relative_to(app_dir)),
                    "files": images,
                })
    return assets
