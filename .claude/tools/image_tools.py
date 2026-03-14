#!/usr/bin/env python3
"""Image and asset generation tools for TradeGuru CLI toolkit.

Uses direct API calls to OpenAI (DALL-E) and Google (Gemini) via .env keys.
No external platform dependencies — runs fully local.
"""

import json
import os
import sys
import base64
import time
from pathlib import Path
from typing import Optional
from urllib.request import Request, urlopen
from urllib.error import URLError, HTTPError

from utils import (
    find_project_root, get_app_path, read_json, write_json,
    load_assets, save_asset_record, scan_xcassets,
    save_job, get_job, update_job, generate_job_id,
    load_env, get_env,
    success, error, info, warn
)


# --- Constants ---

VALID_SIZES = {
    "auto",         # let the model decide (gpt-image-1+)
    "1024x1024",    # square (all models)
    "1024x1536",    # portrait (gpt-image-1+)
    "1536x1024",    # landscape (gpt-image-1+)
    "1024x1792",    # tall portrait (dall-e-3)
    "1792x1024",    # wide landscape (dall-e-3)
    "256x256",      # small square (dall-e-2)
    "512x512",      # medium square (dall-e-2)
}
VALID_BACKGROUNDS = {"transparent", "opaque", "auto"}
VALID_DEVICES = {"iphone", "ipad"}
VALID_ICON_TYPES = {"icon", "tvos-icon", "visionos-icon", "imessage-icon"}

DEVICE_SIZES = {
    "iphone": {"width": 1320, "height": 2868, "display": "iPhone 6.9\""},
    "ipad": {"width": 2048, "height": 2732, "display": "iPad 13\""},
}


# --- API Helpers ---

def _ensure_env():
    """Load .env and validate required keys exist."""
    load_env(find_project_root())


def _openai_generate_image(prompt: str, size: str = "1024x1024", timeout: int = 120) -> dict:
    """Generate image via OpenAI DALL-E API. Returns {b64_data, mime_type}."""
    _ensure_env()
    api_key = get_env("OPENAI_API_KEY", required=True)
    model = get_env("IMAGE_GEN_MODEL", default="gpt-image-1")
    base_url = get_env("OPENAI_API_BASE", default="https://api.openai.com/v1")

    is_gpt_image = model.startswith("gpt-image")

    payload = {
        "model": model,
        "prompt": prompt,
        "n": 1,
        "size": size,
        "response_format": "b64_json",
    }

    # GPT image models support background parameter
    if is_gpt_image:
        bg = get_env("IMAGE_GEN_BACKGROUND", default="auto")
        if bg in VALID_BACKGROUNDS:
            payload["background"] = bg
        payload["output_format"] = "png"

    data = json.dumps(payload).encode("utf-8")
    req = Request(
        f"{base_url}/images/generations",
        data=data,
        headers={
            "Content-Type": "application/json",
            "Authorization": f"Bearer {api_key}",
        },
    )

    try:
        with urlopen(req, timeout=timeout) as resp:
            result = json.loads(resp.read().decode("utf-8"))
            b64 = result["data"][0]["b64_json"]
            return {"b64_data": b64, "mime_type": "image/png"}
    except HTTPError as e:
        body = e.read().decode("utf-8", errors="replace")
        error(f"OpenAI API error {e.code}: {body[:500]}")
        sys.exit(1)
    except URLError as e:
        error(f"Network error: {e.reason}")
        sys.exit(1)
    except (KeyError, IndexError) as e:
        error(f"Unexpected API response format: {e}")
        sys.exit(1)


def _openai_edit_image(prompt: str, input_images: list, timeout: int = 120) -> dict:
    """Edit image via OpenAI Images Edit API. Returns {b64_data, mime_type}.

    Falls back to generation if edit fails or no edit API is available.
    input_images: list of file paths to local images.
    """
    _ensure_env()
    api_key = get_env("OPENAI_API_KEY", required=True)
    base_url = get_env("OPENAI_API_BASE", default="https://api.openai.com/v1")

    # OpenAI edit API uses multipart/form-data, not JSON
    # Requires local image files (not URLs)
    if not input_images or not any(Path(p).exists() for p in input_images if not p.startswith("http")):
        warn("Edit mode: no local files found, falling back to generation with edit prompt.")
        return _openai_generate_image(prompt)

    import mimetypes

    image_path = None
    for p in input_images:
        if not p.startswith("http") and Path(p).exists():
            image_path = Path(p)
            break

    if not image_path:
        return _openai_generate_image(prompt)

    model = get_env("IMAGE_GEN_MODEL", default="gpt-image-1")

    # Build multipart form data
    boundary = f"----TradeGuruBoundary{int(time.time())}"
    body_parts = []

    body_parts.append(f"--{boundary}\r\nContent-Disposition: form-data; name=\"prompt\"\r\n\r\n{prompt}")
    body_parts.append(f"--{boundary}\r\nContent-Disposition: form-data; name=\"model\"\r\n\r\n{model}")
    body_parts.append(f"--{boundary}\r\nContent-Disposition: form-data; name=\"response_format\"\r\n\r\nb64_json")

    # gpt-image-1 supports more sizes; dall-e-2 edit is restricted to 1024x1024
    edit_size = "auto" if model.startswith("gpt-image") else "1024x1024"
    body_parts.append(f"--{boundary}\r\nContent-Disposition: form-data; name=\"size\"\r\n\r\n{edit_size}")

    # Add image file
    img_data = image_path.read_bytes()
    mime = mimetypes.guess_type(str(image_path))[0] or "image/png"
    body_parts.append(
        f"--{boundary}\r\nContent-Disposition: form-data; name=\"image\"; filename=\"{image_path.name}\"\r\n"
        f"Content-Type: {mime}\r\n\r\n"
    )

    # Assemble body
    body = b""
    for i, part in enumerate(body_parts):
        if i == len(body_parts) - 1:  # Image part - needs binary data appended
            body += part.encode("utf-8") + img_data + b"\r\n"
        else:
            body += part.encode("utf-8") + b"\r\n"
    body += f"--{boundary}--\r\n".encode("utf-8")

    req = Request(
        f"{base_url}/images/edits",
        data=body,
        headers={
            "Content-Type": f"multipart/form-data; boundary={boundary}",
            "Authorization": f"Bearer {api_key}",
        },
    )

    try:
        with urlopen(req, timeout=timeout) as resp:
            result = json.loads(resp.read().decode("utf-8"))
            b64 = result["data"][0]["b64_json"]
            return {"b64_data": b64, "mime_type": "image/png"}
    except HTTPError as e:
        body_text = e.read().decode("utf-8", errors="replace")
        warn(f"Edit API failed ({e.code}), falling back to generation.")
        return _openai_generate_image(prompt)
    except Exception as e:
        warn(f"Edit API failed ({e}), falling back to generation.")
        return _openai_generate_image(prompt)


def _save_base64_image(b64_data: str, mime_type: str, dest: Path) -> None:
    """Decode base64 image data and save to file."""
    dest.parent.mkdir(parents=True, exist_ok=True)
    img_bytes = base64.b64decode(b64_data)
    dest.write_bytes(img_bytes)
    size_kb = len(img_bytes) / 1024
    success(f"Saved image ({size_kb:.0f} KB): {dest}")


# =============================================================================
# TOOL: list_existing_image_assets
# =============================================================================

def list_existing_image_assets(app_path: Optional[str] = None) -> None:
    """List all existing image assets in the project.

    Scans .xcassets directories and the generated assets registry.

    Args:
        app_path: Path to app directory (auto-detected if omitted)
    """
    root = find_project_root()

    # Check generated assets registry
    registry_assets = load_assets(root)
    if registry_assets:
        info(f"Generated assets ({len(registry_assets)}):")
        for asset in registry_assets:
            status = "completed" if asset.get("url") else asset.get("status", "unknown")
            print(f"  {asset.get('name', '?'):30s}  {asset.get('size', '?'):12s}  [{status}]")
        print()

    # Scan xcassets in app directory
    try:
        app_dir = get_app_path(root, app_path)
        xcassets = scan_xcassets(app_dir)
        if xcassets:
            info(f"Asset catalog entries ({len(xcassets)}):")
            for asset in xcassets:
                files = ", ".join(asset["files"]) if asset["files"] else "(empty)"
                print(f"  {asset['name']:30s}  {asset['path']}")
                print(f"    Files: {files}")
            print()
    except SystemExit:
        pass  # No app found, skip xcassets scan

    if not registry_assets:
        info("No generated assets found. Use 'tradeguru generate-image-asset' to create some.")


# =============================================================================
# TOOL: confirm_image_generation
# =============================================================================

def confirm_image_generation(assets_json: str) -> None:
    """Interactive confirmation for proposed image assets.

    Args:
        assets_json: JSON string of array of {assetName, prompt} objects
    """
    try:
        assets = json.loads(assets_json)
    except json.JSONDecodeError:
        error("Invalid JSON. Expected: [{\"assetName\": \"...\", \"prompt\": \"...\"}]")
        sys.exit(1)

    if not assets:
        error("No assets provided.")
        sys.exit(1)

    root = find_project_root()

    # Check for duplicates
    existing = load_assets(root)
    existing_names = {a.get("name") for a in existing}

    info(f"Proposed image assets ({len(assets)}):\n")
    approved = []

    for i, asset in enumerate(assets, 1):
        name = asset.get("assetName", f"asset_{i}")
        prompt = asset.get("prompt", "")
        size = asset.get("size", "1024x1024")
        background = asset.get("background", "auto")

        duplicate = name in existing_names
        dup_tag = " [DUPLICATE]" if duplicate else ""

        print(f"  {i}. {name}{dup_tag}")
        print(f"     Prompt: {prompt[:80]}{'...' if len(prompt) > 80 else ''}")
        print(f"     Size: {size}  Background: {background}")

        if duplicate:
            warn(f"     Asset '{name}' already exists. Will skip.")
        else:
            approved.append(asset)
        print()

    if not approved:
        warn("No new assets to generate (all duplicates).")
        return

    # Interactive confirmation
    try:
        response = input(f"Generate {len(approved)} asset(s)? [Y/n]: ").strip().lower()
    except (EOFError, KeyboardInterrupt):
        response = "n"

    if response in ("", "y", "yes"):
        success(f"Approved {len(approved)} asset(s) for generation.")
        # Output approved assets as JSON for pipeline use
        print(json.dumps({"approved": approved}, indent=2))
    else:
        info("Generation cancelled.")


# =============================================================================
# TOOL: generate_image_asset
# =============================================================================

def generate_image_asset(
    prompt: str,
    asset_name: str,
    size: str = "1024x1024",
    background: str = "auto",
    input_images: Optional[str] = None,
    run_in_background: bool = False,
) -> None:
    """Generate a custom image asset using AI.

    Args:
        prompt: Detailed description of the desired image
        asset_name: Short snake_case identifier (e.g. "hero_banner")
        size: 1024x1024, 1024x1536, or 1536x1024
        background: transparent, opaque, or auto
        input_images: Comma-separated URLs for edit mode (optional)
        run_in_background: If True, save job and return ID immediately
    """
    if size not in VALID_SIZES:
        error(f"Invalid size: {size}. Use: {', '.join(sorted(VALID_SIZES))}")
        sys.exit(1)
    if background not in VALID_BACKGROUNDS:
        error(f"Invalid background: {background}. Use: {', '.join(VALID_BACKGROUNDS)}")
        sys.exit(1)

    root = find_project_root()

    if run_in_background:
        job_id = generate_job_id("img")
        save_job(root, job_id, "image_asset", {
            "prompt": prompt,
            "asset_name": asset_name,
            "size": size,
            "background": background,
        })
        success(f"Background job queued: {job_id}")

    # Use edit API if input images provided
    if input_images:
        image_paths = [p.strip() for p in input_images.split(",") if p.strip()]
        if image_paths:
            info(f"Edit mode: compositing with {len(image_paths)} input image(s)...")
            result = _openai_edit_image(prompt, image_paths, timeout=120)
            b64 = result["b64_data"]
            dest = root / "assets" / "generated" / f"{asset_name}.png"
            _save_base64_image(b64, "image/png", dest)
            record = {
                "name": asset_name, "size": size, "background": background,
                "prompt": prompt[:200], "path": str(dest.relative_to(root)),
                "generated_at": time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime()),
                "status": "completed", "mode": "edit",
            }
            save_asset_record(root, record)
            if run_in_background:
                update_job(root, job_id, {"status": "completed", "result_path": str(dest.relative_to(root))})
            success(f"Asset '{asset_name}' edited and saved.")
            return

    info(f"Generating image: {asset_name} ({size}, bg={background})...")
    result = _openai_generate_image(prompt, size=size, timeout=120)
    b64 = result["b64_data"]

    # Save to project assets
    dest = root / "assets" / "generated" / f"{asset_name}.png"
    _save_base64_image(b64, "image/png", dest)

    # Record in registry
    record = {
        "name": asset_name,
        "size": size,
        "background": background,
        "prompt": prompt[:200],
        "path": str(dest.relative_to(root)),
        "generated_at": time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime()),
        "status": "completed",
    }
    save_asset_record(root, record)

    if run_in_background:
        update_job(root, job_id, {
            "status": "completed",
            "result_path": str(dest.relative_to(root)),
        })

    success(f"Asset '{asset_name}' generated and saved.")


# =============================================================================
# TOOL: generate_image (image-gen skill)
# =============================================================================

def generate_image(
    prompt: str,
    size: str = "1024x1024",
    background: str = "auto",
    img_type: str = "asset",
    run_in_background: bool = False,
) -> None:
    """Generate an image using AI and return URL/path.

    Args:
        prompt: Description of the image to generate
        size: Image dimensions
        background: transparent, opaque, or auto
        img_type: "icon" or "asset"
        run_in_background: Queue as background job
    """
    if img_type == "icon":
        size = "1024x1024"
        background = "opaque"
        info("Icon mode: size=1024x1024, background=opaque")

    root = find_project_root()

    if run_in_background:
        job_id = generate_job_id("img")
        save_job(root, job_id, "generate_image", {
            "prompt": prompt, "size": size, "type": img_type,
        })
        success(f"Background job queued: {job_id}")

    info(f"Generating {img_type} image ({size})...")
    result = _openai_generate_image(prompt, size=size, timeout=120)
    b64 = result["b64_data"]

    # Save locally
    name = f"{img_type}_{int(time.time())}"
    dest = root / "assets" / "generated" / f"{name}.png"
    _save_base64_image(b64, "image/png", dest)

    record = {
        "name": name,
        "type": img_type,
        "size": size,
        "prompt": prompt[:200],
        "path": str(dest.relative_to(root)),
        "generated_at": time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime()),
        "status": "completed",
    }
    save_asset_record(root, record)

    if run_in_background:
        update_job(root, job_id, {"status": "completed", "result_path": str(dest.relative_to(root))})

    success(f"Image generated: {dest.relative_to(root)}")


# =============================================================================
# TOOL: generate_icon
# =============================================================================

def generate_icon(
    prompt: str,
    app_path: str,
    icon_type: str = "icon",
    run_in_background: bool = False,
) -> None:
    """Generate and save an app icon to the correct project location.

    Args:
        prompt: Description of the icon design
        app_path: App folder path (e.g. "ios")
        icon_type: icon, tvos-icon, visionos-icon, or imessage-icon
        run_in_background: Queue as background job
    """
    if icon_type not in VALID_ICON_TYPES:
        error(f"Invalid icon type: {icon_type}. Use: {', '.join(VALID_ICON_TYPES)}")
        sys.exit(1)

    root = find_project_root()
    app_dir = get_app_path(root, app_path)

    if run_in_background:
        job_id = generate_job_id("icon")
        save_job(root, job_id, "generate_icon", {
            "prompt": prompt, "app_path": app_path, "icon_type": icon_type,
        })
        success(f"Background job queued: {job_id}")

    info(f"Generating {icon_type} (1024x1024, opaque)...")
    result = _openai_generate_image(prompt, size="1024x1024", timeout=120)
    b64 = result["b64_data"]

    # Determine save location based on icon type and project type
    if icon_type == "icon":
        # Swift: Assets.xcassets/AppIcon.appiconset/icon.png
        icon_dirs = list(app_dir.rglob("AppIcon.appiconset"))
        if icon_dirs:
            dest = icon_dirs[0] / "icon.png"
        else:
            # Create the path
            dest = app_dir / "Assets.xcassets" / "AppIcon.appiconset" / "icon.png"
            dest.parent.mkdir(parents=True, exist_ok=True)
            # Create Contents.json
            write_json(dest.parent / "Contents.json", {
                "images": [
                    {"filename": "icon.png", "idiom": "universal", "platform": "ios", "size": "1024x1024"}
                ],
                "info": {"author": "xcode", "version": 1}
            })
    elif icon_type == "tvos-icon":
        dest = app_dir / "Assets.xcassets" / "TVAppIcon.appiconset" / "icon.png"
        dest.parent.mkdir(parents=True, exist_ok=True)
        write_json(dest.parent / "Contents.json", {
            "images": [{"filename": "icon.png", "idiom": "universal", "platform": "tvos", "size": "1024x1024"}],
            "info": {"author": "xcode", "version": 1}
        })
    elif icon_type == "visionos-icon":
        dest = app_dir / "Assets.xcassets" / "VisionAppIcon.appiconset" / "icon.png"
        dest.parent.mkdir(parents=True, exist_ok=True)
        write_json(dest.parent / "Contents.json", {
            "images": [{"filename": "icon.png", "idiom": "universal", "platform": "xros", "size": "1024x1024"}],
            "info": {"author": "xcode", "version": 1}
        })
    elif icon_type == "imessage-icon":
        dest = app_dir / "Assets.xcassets" / "iMessageAppIcon.appiconset" / "icon.png"
        dest.parent.mkdir(parents=True, exist_ok=True)
        write_json(dest.parent / "Contents.json", {
            "images": [{"filename": "icon.png", "idiom": "universal", "size": "1024x1024"}],
            "info": {"author": "xcode", "version": 1}
        })
    else:
        dest = app_dir / "Assets.xcassets" / "AppIcon.appiconset" / "icon.png"

    _save_base64_image(b64, "image/png", dest)

    # Update Contents.json if it exists
    contents_json = dest.parent / "Contents.json"
    if contents_json.exists():
        contents = read_json(contents_json)
        images = contents.get("images", [])
        # Update filename reference
        for img in images:
            if not img.get("filename"):
                img["filename"] = "icon.png"
        if not any(img.get("filename") == "icon.png" for img in images):
            platform_map = {"icon": "ios", "tvos-icon": "tvos", "visionos-icon": "xros"}
            platform_entry = {"filename": "icon.png", "idiom": "universal", "size": "1024x1024"}
            if icon_type in platform_map:
                platform_entry["platform"] = platform_map[icon_type]
            images.append(platform_entry)
        contents["images"] = images
        write_json(contents_json, contents)

    if run_in_background:
        update_job(root, job_id, {"status": "completed", "result_path": str(dest.relative_to(root))})

    success(f"App icon ({icon_type}) saved to: {dest.relative_to(root)}")


# =============================================================================
# TOOL: generate_screenshot
# =============================================================================

def generate_screenshot(
    prompt: str,
    app_path: str,
    device: str = "iphone",
    run_in_background: bool = False,
) -> None:
    """Generate an App Store screenshot mockup.

    Args:
        prompt: Description of the screenshot
        app_path: App folder path
        device: "iphone" or "ipad"
        run_in_background: Queue as background job
    """
    if device not in VALID_DEVICES:
        error(f"Invalid device: {device}. Use: iphone, ipad")
        sys.exit(1)

    device_info = DEVICE_SIZES[device]
    root = find_project_root()

    if run_in_background:
        job_id = generate_job_id("ss")
        save_job(root, job_id, "generate_screenshot", {
            "prompt": prompt, "app_path": app_path, "device": device,
        })
        success(f"Background job queued: {job_id}")

    # Use portrait size for screenshots
    size = "1024x1792" if device == "iphone" else "1792x1024"

    full_prompt = (
        f"App Store screenshot mockup for {device_info['display']}: "
        f"{prompt}. Marketing-style composition with device mockup, "
        f"styled background, and feature headline text."
    )

    info(f"Generating {device_info['display']} screenshot...")
    result = _openai_generate_image(full_prompt, size=size, timeout=120)
    b64 = result["b64_data"]

    # Save screenshot
    timestamp = int(time.time())
    dest = root / "assets" / "screenshots" / f"{device}_{timestamp}.png"
    _save_base64_image(b64, "image/png", dest)

    record = {
        "name": f"screenshot_{device}_{timestamp}",
        "type": "screenshot",
        "device": device,
        "device_display": device_info["display"],
        "target_resolution": f"{device_info['width']}x{device_info['height']}",
        "prompt": prompt[:200],
        "path": str(dest.relative_to(root)),
        "generated_at": time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime()),
        "status": "completed",
    }
    save_asset_record(root, record)

    if run_in_background:
        update_job(root, job_id, {"status": "completed", "result_path": str(dest.relative_to(root))})

    success(f"Screenshot saved: {dest.relative_to(root)}")
    info(f"Target resolution: {device_info['width']}x{device_info['height']} ({device_info['display']})")


# =============================================================================
# TOOL: wait_image_result
# =============================================================================

def wait_image_result(job_id: str) -> None:
    """Check status and retrieve result of a background image generation job.

    Args:
        job_id: The generation job ID from a background generation call
    """
    root = find_project_root()
    job = get_job(root, job_id)

    if not job:
        error(f"Job not found: {job_id}")
        # List available jobs
        jobs_data = read_json(root / ".claude" / "background_jobs.json")
        available = list(jobs_data.get("jobs", {}).keys())
        if available:
            info(f"Available jobs: {', '.join(available[-10:])}")
        sys.exit(1)

    status = job.get("status", "unknown")
    job_type = job.get("type", "unknown")

    if status == "completed":
        success(f"Job {job_id} completed.")
        result_path = job.get("result_path")
        if result_path:
            info(f"Result: {result_path}")
        print(json.dumps(job, indent=2, default=str))
    elif status == "failed":
        error(f"Job {job_id} failed.")
        print(json.dumps(job, indent=2, default=str))
        sys.exit(1)
    elif status == "pending":
        warn(f"Job {job_id} is still pending ({job_type}).")
        info("The job may need to be re-run. Background processing requires the CLI to stay active.")
        print(json.dumps(job, indent=2, default=str))
    else:
        info(f"Job {job_id} status: {status}")
        print(json.dumps(job, indent=2, default=str))
