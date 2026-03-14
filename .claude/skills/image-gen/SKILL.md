---
name: image-gen
description: Generate images, app icons, and App Store screenshots. You must read this skill before generating any image.
---

## Image Generation

You just unlocked the `generateImage`, `generateIcon`, and `generateScreenshot` tools.

Don't forget to explore the app's code to match the image with the app design and idea.

### `generateImage`

Generates images using AI and uploads to R2. Returns an image URL.

- `prompt`: describe the image (generate) or what to change (edit with inputImages)
- `size`: `"1024x1024"`, `"1024x1536"`, or `"1536x1024"`
- `background`: `"transparent"`, `"opaque"`, or `"auto"`
- `type`: `"icon"` for app icons, `"asset"` for other images
- `runInBackground`: set `true` to schedule in background, then use `waitImageResult` with the returned `generationId`

For **icons**: use `type: "icon"`, size is auto-set to 1024x1024, background to opaque. The generated image is returned as a URL but NOT saved to the project -- use `generateIcon` to generate and save the icon into the app.

For **assets**: use `type: "asset"` for decorative images, backgrounds, patterns, design elements, etc.

### `generateIcon`

Generates and saves an app icon into the correct project asset structure. Requires an `appPath`.

- `appPath`: which app folder (e.g. `"expo"`, `"ios"`)
- `prompt`: describe the icon design
- `iconType`: `"icon"` (main app icon), `"tvos-icon"`, `"visionos-icon"`, or `"imessage-icon"`
- `runInBackground`: set `true` to schedule in background

For Swift apps: saves into `Assets.xcassets/AppIcon.appiconset/icon.png` for all Xcode targets.
For React Native apps: saves into `assets/images/icon.png` and adaptive icon variants.

tvOS/visionOS/iMessage icon types read the existing app icon from disk and transform it -- generate the main icon first with `iconType: "icon"`, then call again with `"tvos-icon"`, `"visionos-icon"`, or `"imessage-icon"` to populate platform-specific assets.

### `generateScreenshot`

Generates App Store screenshot mockups. Requires an `appPath`.

- `appPath`: which app folder (e.g. `"expo"`, `"ios"`)
- `prompt`: describe the screenshot (generate) or what to change (edit with inputImages)
- `device`: `"iphone"` (automatically resized to iPhone 6.9" 1320x2868) or `"ipad"` (automatically resized to iPad 13" 2048x2732)
- `runInBackground`: set `true` to schedule in background

### How to generate great App Store screenshots

The goal is to create stunning, high-converting App Store screenshots that make users want to download the app. Think of these as marketing assets -- they should highlight the app's best features, look polished and professional, and stand out in the App Store.

1. Read the app's code to understand the design, colors, layout, and most impressive screens
2. Write a detailed prompt -- describe the screen content, UI elements, colors, background, and overall composition. The prompt should produce a visually striking image that sells the app
3. Generate with `generateScreenshot`

Unless the user specifically asks for a plain fullscreen screenshot, default to a **marketing-style composition**: a device mockup showing the app screen, surrounded by a styled background with a headline, subtitle, or feature callout text. Think App Store featured screenshots -- they almost never show a raw screen. Instead, they use gradients or solid color backgrounds, place the phone/tablet at an angle or centered, and add short punchy text like "Track your habits", "Beautiful dark mode", or "Plan your day". Decide what text and composition fits best based on the app's purpose and design.

Screenshots work best when the model has a real app screenshot as reference. If the user did NOT already attach a screenshot, use the `requestAsset` tool to ask them to provide one before generating. Set `mimeFilter: "image/*"`, `minFiles: 1`, `maxFiles: 5`. Once they attach screenshots, use the "edit" prompt type and pass the screenshot URLs in `inputImages`.

The size and background are set automatically -- you only need to provide the prompt and device type.
