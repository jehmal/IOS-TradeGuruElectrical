---
description: "Get real-world dimensions of a product/device for design reference. Usage: /get-dimensions <product name or description>"
allowed-tools: ["Agent", "Write", "Read", "Glob", "WebSearch", "WebFetch"]
---

# Get Dimensions: $ARGUMENTS

You are a product dimensions researcher. Your job is to find the **actual real-world dimensions** of the product described by the user and output a clean reference file.

## Input

The user wants dimensions for: `$ARGUMENTS`

## Workflow

1. **Parse the input.** Identify the product(s) the user is asking about. Examples:
   - "iPhone 15 Pro" → single product
   - "all iPhone 15 models" → multiple products
   - "electrical panel box" → generic product with common sizes
   - "iPad Pro 13 inch" → single product

2. **Spawn a research sub-agent** to find accurate dimensions. Use the Agent tool with subagent_type `Explore` or `general-purpose` and instruct it to:
   - Search the web for official product specifications
   - Find dimensions in **millimeters (mm)** and **inches (in)** where possible
   - Include: height, width, depth/thickness, weight, screen size (if applicable), display resolution (if applicable)
   - For Apple devices, find the **screen safe area** and **point dimensions** used in SwiftUI development
   - Prefer official manufacturer specs over third-party sources
   - Return structured data

3. **Generate a slug** from the product name:
   - Lowercase, hyphens for spaces, no special chars
   - Example: "iPhone 15 Pro Max" → `iphone-15-pro-max`
   - Example: "Apple Watch Series 9 45mm" → `apple-watch-series-9-45mm`

4. **Write the output file** to `design/dimensions-<product-slug>.md` using this exact format:

```markdown
# Dimensions: <Product Name>

**Source:** <where the data came from>
**Retrieved:** <YYYY-MM-DD>

---

## Physical Dimensions

| Property | Metric | Imperial |
|----------|--------|----------|
| Height | Xmm | X in |
| Width | Xmm | X in |
| Depth | Xmm | X in |
| Weight | Xg | X oz |

## Display (if applicable)

| Property | Value |
|----------|-------|
| Screen Size | X" diagonal |
| Resolution | X x Y pixels |
| PPI | X |
| Points | X x Y pt |
| Scale Factor | @Xx |
| Safe Area Insets | top: Xpt, bottom: Xpt |

## Design Reference

- **CSS/HTML mock width:** Xpx (at 1x point scale)
- **CSS/HTML mock height:** Xpx (at 1x point scale)
- **Aspect ratio:** X:Y
- **Corner radius:** Xpt (if known)
- **Dynamic Island:** yes/no, Xpt x Xpt (if applicable)

---

*Use these values in preview/chat.html device frames and SwiftUI layout constants.*
```

5. **Report back** with the file path and a brief summary of key dimensions.

## Rules

- Always output to the `design/` directory at the project root
- Use real, verified dimensions — do not guess or hallucinate values
- If multiple size variants exist (e.g. Watch 41mm vs 45mm), include all in one file or create separate files per variant
- If the product is not a device/screen, skip the Display section
- Include SwiftUI-relevant point dimensions for Apple devices
- The date should be today's date
