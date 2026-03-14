#!/usr/bin/env python3
"""Project creation and Swift project tools for TradeGuru CLI toolkit."""

import json
import os
import subprocess
import sys
from pathlib import Path
from typing import Optional

from utils import (
    find_project_root, get_app_path, read_json, write_json,
    success, error, info, warn
)


# =============================================================================
# TOOL: create_app
# =============================================================================

def _content_view_swift(app_name: str) -> str:
    return f'''\
import SwiftUI

struct ContentView: View {{
    var body: some View {{
        NavigationStack {{
            VStack {{
                Image(systemName: "chart.line.uptrend.xyaxis")
                    .imageScale(.large)
                    .foregroundStyle(.tint)
                Text("Welcome")
                    .font(.title)
            }}
            .padding()
            .navigationTitle("{app_name}")
        }}
    }}
}}

#Preview {{
    ContentView()
}}
'''

def _app_swift(pascal_name: str) -> str:
    return f'''\
import SwiftUI

@main
struct {pascal_name}App: App {{
    var body: some Scene {{
        WindowGroup {{
            ContentView()
        }}
    }}
}}
'''

def _package_swift(pascal_name: str) -> str:
    return f'''\
// swift-tools-version: 6.0
import PackageDescription

let package = Package(
    name: "{pascal_name}",
    platforms: [.iOS(.v18)],
    products: [
        .library(name: "{pascal_name}", targets: ["{pascal_name}"]),
    ],
    dependencies: [],
    targets: [
        .target(name: "{pascal_name}", dependencies: [], path: "{pascal_name}"),
    ]
)
'''


def to_pascal(name: str) -> str:
    """Convert a name to PascalCase."""
    return "".join(word.capitalize() for word in name.replace("-", " ").replace("_", " ").split())


def create_app(name: str, framework: str, path: str) -> None:
    """Create a new app project scaffold.

    Args:
        name: User-friendly app name (e.g. "TradeGuru")
        framework: "swift" | "react-native" | "web" | "kotlin"
        path: Lowercase folder name (e.g. "ios")
    """
    root = find_project_root()
    app_dir = root / path

    if app_dir.exists() and any(app_dir.iterdir()):
        error(f"Directory '{path}' already exists and is not empty.")
        sys.exit(1)

    pascal_name = to_pascal(name)

    if framework == "swift":
        _create_swift_app(root, app_dir, name, pascal_name)
    elif framework == "react-native":
        _create_rn_app(root, app_dir, name)
    elif framework == "web":
        _create_web_app(root, app_dir, name)
    elif framework == "kotlin":
        _create_kotlin_app(root, app_dir, name)
    else:
        error(f"Unknown framework: {framework}. Use: swift, react-native, web, kotlin")
        sys.exit(1)

    # Update or create rork.json manifest
    manifest_path = root / "rork.json"
    manifest = read_json(manifest_path)
    if "apps" not in manifest:
        manifest["apps"] = []
    manifest["apps"].append({
        "name": name,
        "framework": framework,
        "path": path,
    })
    write_json(manifest_path, manifest)

    success(f"Created {framework} app '{name}' at ./{path}/")
    info(f"Manifest updated: rork.json")


def _create_swift_app(root: Path, app_dir: Path, name: str, pascal_name: str) -> None:
    """Scaffold a Swift/SwiftUI iOS project."""
    src_dir = app_dir / pascal_name
    src_dir.mkdir(parents=True, exist_ok=True)

    # Create source files
    (src_dir / "ContentView.swift").write_text(_content_view_swift(name), encoding="utf-8")
    (src_dir / f"{pascal_name}App.swift").write_text(_app_swift(pascal_name), encoding="utf-8")

    # Create Assets.xcassets
    assets_dir = src_dir / "Assets.xcassets"
    assets_dir.mkdir(parents=True, exist_ok=True)
    write_json(assets_dir / "Contents.json", {
        "info": {"author": "xcode", "version": 1}
    })

    # Create AppIcon.appiconset
    icon_dir = assets_dir / "AppIcon.appiconset"
    icon_dir.mkdir(parents=True, exist_ok=True)
    write_json(icon_dir / "Contents.json", {
        "images": [{"idiom": "universal", "platform": "ios", "size": "1024x1024"}],
        "info": {"author": "xcode", "version": 1}
    })

    # Create AccentColor
    accent_dir = assets_dir / "AccentColor.colorset"
    accent_dir.mkdir(parents=True, exist_ok=True)
    write_json(accent_dir / "Contents.json", {
        "colors": [{"idiom": "universal"}],
        "info": {"author": "xcode", "version": 1}
    })

    # Create Package.swift
    (app_dir / "Package.swift").write_text(_package_swift(pascal_name), encoding="utf-8")

    # Create Preview Content
    preview_dir = src_dir / "Preview Content"
    preview_dir.mkdir(parents=True, exist_ok=True)
    write_json(preview_dir / "Contents.json", {
        "info": {"author": "xcode", "version": 1}
    })

    # Create .entitlements file
    entitlements = f'''\
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict/>
</plist>
'''
    (src_dir / f"{pascal_name}.entitlements").write_text(entitlements, encoding="utf-8")

    info(f"Swift project structure created at {app_dir}/")


def _create_rn_app(root: Path, app_dir: Path, name: str) -> None:
    """Scaffold a React Native / Expo project."""
    app_dir.mkdir(parents=True, exist_ok=True)
    (app_dir / "app").mkdir(exist_ok=True)
    (app_dir / "components").mkdir(exist_ok=True)
    (app_dir / "constants").mkdir(exist_ok=True)
    (app_dir / "hooks").mkdir(exist_ok=True)
    (app_dir / "assets" / "images").mkdir(parents=True, exist_ok=True)

    pkg = {
        "name": name.lower().replace(" ", "-"),
        "version": "1.0.0",
        "main": "expo-router/entry",
        "scripts": {
            "start": "expo start",
            "ios": "expo run:ios",
            "android": "expo run:android",
        },
    }
    write_json(app_dir / "package.json", pkg)

    app_json = {
        "expo": {
            "name": name,
            "slug": name.lower().replace(" ", "-"),
            "version": "1.0.0",
            "scheme": name.lower().replace(" ", ""),
            "platforms": ["ios", "android"],
        }
    }
    write_json(app_dir / "app.json", app_json)
    info(f"React Native (Expo) project structure created at {app_dir}/")


def _create_web_app(root: Path, app_dir: Path, name: str) -> None:
    """Scaffold a basic web project."""
    app_dir.mkdir(parents=True, exist_ok=True)
    (app_dir / "src").mkdir(exist_ok=True)
    (app_dir / "public").mkdir(exist_ok=True)

    index_html = f'''\
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>{name}</title>
</head>
<body>
    <div id="app"></div>
    <script type="module" src="/src/main.js"></script>
</body>
</html>
'''
    (app_dir / "index.html").write_text(index_html, encoding="utf-8")
    info(f"Web project structure created at {app_dir}/")


def _create_kotlin_app(root: Path, app_dir: Path, name: str) -> None:
    """Scaffold a Kotlin/Android project."""
    app_dir.mkdir(parents=True, exist_ok=True)
    src_dir = app_dir / "app" / "src" / "main" / "java"
    src_dir.mkdir(parents=True, exist_ok=True)
    (app_dir / "app" / "src" / "main" / "res").mkdir(parents=True, exist_ok=True)
    info(f"Kotlin project structure created at {app_dir}/")


# =============================================================================
# TOOL: swift_build
# =============================================================================

def swift_build(app_path: Optional[str] = None) -> None:
    """Run a Swift build and report results.

    Args:
        app_path: Path to the Swift app (auto-detected if only one exists)
    """
    root = find_project_root()
    app_dir = get_app_path(root, app_path)

    # Find .xcodeproj
    xcodeprojs = list(app_dir.glob("*.xcodeproj"))

    if xcodeprojs:
        # Use xcodebuild
        proj = xcodeprojs[0]
        info(f"Building with xcodebuild: {proj.name}")
        cmd = [
            "xcodebuild",
            "-project", str(proj),
            "-scheme", proj.stem,
            "-destination", "generic/platform=iOS Simulator",
            "-quiet",
            "build",
        ]
    elif (app_dir / "Package.swift").exists():
        # Use swift build
        info(f"Building with swift build: {app_dir}")
        cmd = ["swift", "build", "--package-path", str(app_dir)]
    else:
        error(f"No .xcodeproj or Package.swift found in {app_dir}")
        sys.exit(1)

    result = subprocess.run(cmd, capture_output=True, text=True, cwd=str(app_dir))

    if result.returncode == 0:
        success("Build succeeded.")
        if result.stdout.strip():
            print(result.stdout[-500:] if len(result.stdout) > 500 else result.stdout)
    else:
        error("Build failed.")
        # Parse and display compiler errors
        stderr = result.stderr or result.stdout or ""
        errors = _parse_swift_errors(stderr)
        if errors:
            print(f"\n{len(errors)} error(s) found:\n")
            for i, err in enumerate(errors[:10], 1):
                print(f"  {i}. {err['file']}:{err['line']}:{err['col']}: {err['message']}")
            if len(errors) > 10:
                warn(f"... and {len(errors) - 10} more. Fix the first error and rebuild.")
        else:
            # Raw output fallback
            print(stderr[-2000:] if len(stderr) > 2000 else stderr)
        sys.exit(1)


def _parse_swift_errors(output: str) -> list:
    """Parse Swift compiler error output into structured records."""
    import re
    errors = []
    pattern = re.compile(r"^(.+\.swift):(\d+):(\d+):\s*(error|warning):\s*(.+)$", re.MULTILINE)
    for match in pattern.finditer(output):
        errors.append({
            "file": match.group(1),
            "line": int(match.group(2)),
            "col": int(match.group(3)),
            "severity": match.group(4),
            "message": match.group(5).strip(),
        })
    return errors


# =============================================================================
# TOOL: swift_install
# =============================================================================

def swift_install(packages: list, path: Optional[str] = None) -> None:
    """Add Swift Package Manager dependencies.

    Args:
        packages: List of dicts with {url, version, products}
        path: Path to the Swift app
    """
    root = find_project_root()
    app_dir = get_app_path(root, path)
    package_swift = app_dir / "Package.swift"

    if not package_swift.exists():
        error(f"No Package.swift found at {app_dir}")
        sys.exit(1)

    content = package_swift.read_text(encoding="utf-8")

    for pkg in packages:
        url = pkg.get("url", "")
        version = pkg.get("version", "1.0.0")
        products = pkg.get("products", [])

        if not url.endswith(".git"):
            warn(f"Package URL should end with .git: {url}")

        # Extract package name from URL
        pkg_name = url.rstrip("/").rsplit("/", 1)[-1].replace(".git", "")

        # Add to dependencies array
        dep_entry = f'.package(url: "{url}", from: "{version}")'
        if dep_entry not in content:
            content = content.replace(
                "dependencies: [",
                f"dependencies: [\n        {dep_entry},",
                1
            )
            info(f"Added dependency: {pkg_name} >= {version}")

        # Add products to target dependencies
        for product in products:
            product_entry = f'.product(name: "{product}", package: "{pkg_name}")'
            if product_entry not in content:
                # Find the target block's dependencies array using regex
                import re
                # Match: .target(name: "...", dependencies: [...],
                target_dep_pattern = re.compile(
                    r'(\.target\([^)]*dependencies:\s*\[)([^\]]*?)(\])',
                    re.DOTALL
                )
                match = target_dep_pattern.search(content)
                if match:
                    existing_deps = match.group(2).strip()
                    if existing_deps:
                        new_deps = f"{existing_deps}, {product_entry}"
                    else:
                        new_deps = product_entry
                    content = content[:match.start(2)] + new_deps + content[match.end(2):]
                    info(f"  Linked product: {product}")
                else:
                    warn(f"  Could not find target dependencies block to link {product}")

    package_swift.write_text(content, encoding="utf-8")
    success(f"Updated Package.swift with {len(packages)} package(s).")
    info("Run 'tradeguru swift-build' to resolve dependencies.")


# =============================================================================
# TOOL: swift_add_target
# =============================================================================

EXTENSION_POINT_IDS = {
    "share-extension": "com.apple.share-services",
    "notification-content": "com.apple.usernotifications.content-extension",
    "app-intent": "com.apple.intents-service",
}

TARGET_TEMPLATES = {
    "widget": {
        "suffix": "Widget",
        "import": "WidgetKit",
        "template": '''\
import WidgetKit
import SwiftUI

struct {name}Entry: TimelineEntry {{
    let date: Date
}}

struct {name}Provider: TimelineProvider {{
    func placeholder(in context: Context) -> {name}Entry {{
        {name}Entry(date: Date())
    }}

    func snapshot(in context: Context) async -> {name}Entry {{
        {name}Entry(date: Date())
    }}

    func timeline(in context: Context) async -> Timeline<{name}Entry> {{
        let entry = {name}Entry(date: Date())
        return Timeline(entries: [entry], policy: .atEnd)
    }}
}}

struct {name}View: View {{
    var entry: {name}Entry

    var body: some View {{
        Text("Hello, Widget!")
    }}
}}

@main
struct {name}: Widget {{
    let kind: String = "{name}"

    var body: some WidgetConfiguration {{
        StaticConfiguration(kind: kind, provider: {name}Provider()) {{ entry in
            {name}View(entry: entry)
        }}
        .configurationDisplayName("{display_name}")
        .description("A helpful widget.")
        .supportedFamilies([.systemSmall, .systemMedium])
    }}
}}
''',
    },
    "share-extension": {
        "suffix": "ShareExtension",
        "template": '''\
import UIKit
import Social

class ShareViewController: SLComposeServiceViewController {{
    override func isContentValid() -> Bool {{
        return true
    }}

    override func didSelectPost() {{
        self.extensionContext?.completeRequest(returningItems: [], completionHandler: nil)
    }}
}}
''',
    },
}


def swift_add_target(name: str, target_type: str, path: Optional[str] = None) -> None:
    """Add a new Xcode target scaffold.

    Args:
        name: Target name in PascalCase
        target_type: widget, share-extension, notification-content, etc.
        path: Path to the Swift app
    """
    root = find_project_root()
    app_dir = get_app_path(root, path)

    template = TARGET_TEMPLATES.get(target_type)
    if not template:
        # Generic target scaffold
        target_dir = app_dir / name
        target_dir.mkdir(parents=True, exist_ok=True)

        generic_swift = f'''\
import Foundation

// {name} - {target_type} target
// Implement your {target_type} logic here.
'''
        (target_dir / f"{name}.swift").write_text(generic_swift, encoding="utf-8")

        # Info.plist for extension targets
        if "extension" in target_type or target_type in EXTENSION_POINT_IDS:
            ext_point = EXTENSION_POINT_IDS.get(target_type, "com.apple.share-services")
            plist = f'''\
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>NSExtension</key>
    <dict>
        <key>NSExtensionPointIdentifier</key>
        <string>{ext_point}</string>
    </dict>
</dict>
</plist>
'''
            (target_dir / "Info.plist").write_text(plist, encoding="utf-8")

        success(f"Created generic target scaffold: {name} ({target_type})")
        warn(f"You'll need to manually add this target to the Xcode project.")
        return

    target_dir = app_dir / name
    target_dir.mkdir(parents=True, exist_ok=True)

    # Render template
    display_name = " ".join(
        word for word in name.replace("Widget", "").replace("Extension", "").split()
    ) or name

    swift_content = template["template"].format(
        name=name,
        display_name=display_name,
    )

    filename = f"{name}.swift"
    (target_dir / filename).write_text(swift_content, encoding="utf-8")

    # Create Assets.xcassets for the target
    assets_dir = target_dir / "Assets.xcassets"
    assets_dir.mkdir(parents=True, exist_ok=True)
    write_json(assets_dir / "Contents.json", {
        "info": {"author": "xcode", "version": 1}
    })

    success(f"Created {target_type} target: {name}")
    info(f"  Directory: {target_dir.relative_to(root)}")
    info(f"  Main file: {filename}")
    info("Run 'tradeguru swift-build' to verify.")
