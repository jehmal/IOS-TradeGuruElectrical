# OandP Toolkit SDK - Complete Study Guide

## Overview

**OandP Toolkit SDK** (`@oandp-ai/toolkit-sdk`) is a React Native/Expo library that provides AI agent capabilities, web polyfills, and development tools for mobile apps. It's designed specifically for apps created using OandP AI services (https://oandp.ai).

- **Version**: 0.2.54
- **License**: Proprietary - Only usable in apps created via OandP AI services
- **Repository**: https://github.com/oandp-ai/oandp-ai-toolkit-sdk

---

## Core Architecture

```
oandp-toolkit-sdk/
├── lib/
│   ├── module/           # Compiled JavaScript modules
│   └── typescript/       # TypeScript definitions
├── metro/
│   ├── index.js          # Metro config helper
│   └── transformer.js    # Custom babel transformer
└── package.json
```

---

## Public API (Exports)

The SDK exports from three main modules:

```javascript
// Main entry point
export * from "./agent.js";        // AI agent hooks
export * from "./integrations.js"; // Third-party integrations
export * from "./analytics.js";    // PostHog analytics
```

### Additional Export Paths

| Path | Purpose |
|------|---------|
| `@oandp-ai/toolkit-sdk` | Main SDK |
| `@oandp-ai/toolkit-sdk/metro` | Metro configuration |
| `@oandp-ai/toolkit-sdk/metro-transformer` | Custom transformer |
| `@oandp-ai/toolkit-sdk/v53` | Expo SDK 53 dev tools |
| `@oandp-ai/toolkit-sdk/v54` | Expo SDK 54 dev tools |

---

## 1. AI Agent System (`agent.js`)

### Core Functions

#### `createOandPTool(tool)`
Creates a type-safe tool definition for AI agents.

```typescript
type Tool<T extends z.ZodType = z.ZodType> = {
    description: string;
    zodSchema: T;
    execute?: (input: z.infer<T>) => Promise<string> | string;
};

const myTool = createOandPTool({
  description: "Search the web",
  zodSchema: z.object({ query: z.string() }),
  execute: async (input) => { /* ... */ return "result"; }
});
```

#### `generateObject(params)`
Single-shot structured generation with schema validation.

```typescript
const result = await generateObject({
  messages: [{ role: "user", content: "..." }],
  schema: z.object({ name: z.string() })
});
// result is typed based on schema
```

#### `generateText(params)`
Simple text generation (string or messages array).

```typescript
const text = await generateText("Write a haiku about code");
// OR
const text = await generateText({ messages: [...] });
```

#### `useOandPAgent(options)`
React hook for agentic chat with tool calling.

```typescript
const agent = useOandPAgent({
  tools: {
    search: createOandPTool({
      description: "Search",
      zodSchema: z.object({ query: z.string() }),
      execute: async (input) => "results..."
    })
  }
});

// Usage
agent.sendMessage("Hello!");
agent.messages; // Chat history
agent.addToolResult({ toolCallId, tool, output });
```

### Key Features
- Uses **Vercel AI SDK** (`@ai-sdk/react`, `ai`)
- **Zod v4** for schema validation
- Automatic tool execution with `sendAutomaticallyWhen`
- Built-in analytics tracking

---

## 2. API Configuration (`api.js`)

```typescript
const BASE_URL = process.env.EXPO_PUBLIC_TOOLKIT_URL ?? "https://toolkit.oandp.ai";

function withBaseUrl(path: string): string;
// Example: withBaseUrl("/agent/chat") => "https://toolkit.oandp.ai/agent/chat"
```

---

## 3. Native Bridge (`bridge.js`)

Communication with native modules for iOS/Android.

```typescript
// Send message to native layer
sendBridgeMessage(type: string, data?: object): void;

// Listen for native messages
addBridgeListener(listener: (data: any) => void): Subscription;

// React hook for bridge
useBridgeListener(listener: (data: any) => void): void;
```

**Native Module Required**: `Bridge` module (via `expo-modules-core`)

---

## 4. Analytics (`analytics.js`)

PostHog-based analytics with automatic screen tracking.

```typescript
// Get client (returns null if disabled)
getPostHogClient(): PostHog | null;

// Track custom event
trackEvent(name: string, properties?: object): void;

// Provider component (wrap your app)
<OandPAnalyticsProvider>
  {children}
</OandPAnalyticsProvider>
```

### Analytics Conditions
- **Disabled when**: No `EXPO_PUBLIC_PROJECT_ID`, no `EXPO_PUBLIC_TEAM_ID`, Expo Go, or web platform
- **Auto-tracks**: Screen views, app lifecycle events

---

## 5. Integrations (`integrations.js`)

Third-party service connections via OAuth.

```typescript
const {
  currentConnections,   // Active connections
  refetchConnections,   // Refresh list
  initiate,            // Start OAuth flow
  disconnect           // Remove connection
} = useConnections();

// Initiate connection
initiate.mutate("toolkit-name");

// Disconnect
disconnect.mutate("toolkit-name");
```

Uses `expo-web-browser` for OAuth flows with deep linking callback.

---

## 6. Metro Configuration

### `withOandPMetro(config)`

Enhances Metro config with OandP-specific features:

```javascript
const { withOandPMetro } = require('@oandp-ai/toolkit-sdk/metro');

module.exports = withOandPMetro({
  // your metro config
});
```

**Features:**
1. **Custom transformer** - Auto-wraps `_layout.tsx` with providers
2. **Web polyfills** - Platform-specific module resolution
3. **Node polyfills** - `assert` module support
4. **File watching** - Additional extensions (.env, .local, .development)

### Web Polyfills Redirected

| Module | Web Replacement |
|--------|-----------------|
| `expo-haptics` | `polyfills/haptics.web.js` |
| `expo-secure-store` | `polyfills/secure-store.web.js` |
| `react-native-maps` | `polyfills/maps.web.js` |
| `RefreshControl` | `polyfills/refresh-control-component.js` |
| `Alert` | `polyfills/alert.web.js` |

---

## 7. Metro Transformer (`transformer.js`)

Automatically transforms `_layout.tsx` to wrap with providers.

### What It Does

1. **Detects root layout**: `app/_layout.tsx` (not grouped routes)
2. **Detects Expo version**: Reads from `node_modules/expo/package.json`
3. **Wraps in development**:
   ```tsx
   // Original
   export default function RootLayout() { return <Slot />; }
   
   // Transformed (dev)
   import { OandPDevWrapper } from '@oandp-ai/toolkit-sdk/v54';
   import { OandPAnalyticsProvider } from '@oandp-ai/toolkit-sdk';
   
   function RootLayout() { return <Slot />; }
   
   export default function OandPRootLayoutWrapper() {
     return (
       <OandPDevWrapper>
         <OandPAnalyticsProvider>
           <RootLayout />
         </OandPAnalyticsProvider>
       </OandPDevWrapper>
     );
   }
   ```

---

## 8. Development Tools (`dev/`)

### Error Boundary (`error-boundary.js`)

```tsx
<OandPErrorBoundary onError={(error, info) => {...}}>
  {children}
</OandPErrorBoundary>
```

**Features:**
- Catches React errors
- Reports to parent iframe (web) or native bridge (mobile)
- Overrides `console.error` to capture logs
- Listens for unhandled promise rejections

### Bundle Inspector (`dev/sdk54/inspector.js`)

Visual element inspector for debugging.

```tsx
<BundleInspector>
  {children}
</BundleInspector>
```

**Features:**
- Touch to inspect element
- Shows style, frame, hierarchy
- Sends data to native bridge
- Floating island UI with blur effect

**Bridge Messages:**
- `runtime-ready` - Sent on mount
- `merge-inspector-state` - Sync state
- `inspector-element` - Selected element data
- `runtime-error` - Error reporting

---

## 9. Polyfills (`polyfills/`)

### `fetch.js` (Native)
```javascript
// Polyfills for React Native
- structuredClone (@ungap/structured-clone)
- TextEncoderStream (@stardazed/streams-text-encoding)
- TextDecoderStream
```

### Web Polyfills (Empty/Stub)
- `fetch.web.js` - Empty (browser has native support)
- `haptics.web.js` - No-op haptics
- `secure-store.web.js` - Memory-based storage
- `maps.web.js` - Web map component
- `alert.web.js` - Browser alert
- `refresh-control-component.js` - Web scroll handling

---

## 10. Utility Functions (`utils.js`)

```typescript
function isExpoGo(): boolean;
// Returns true if running in Expo Go app
```

---

## Dependencies

### Peer Dependencies
- `@tanstack/react-query` - Data fetching
- `expo` - Expo framework
- `expo-blur` - Blur effects
- `expo-router` - File-based routing
- `expo-web-browser` - OAuth flows
- `lucide-react-native` - Icons
- `react` / `react-native` - Core
- `react-native-safe-area-context` - Safe areas
- `react-native-web` - Web support
- `zod` ^4 - Schema validation

### Key Dependencies
- `@ai-sdk/react` - React hooks for AI
- `ai` - Vercel AI SDK core
- `posthog-react-native` - Analytics

---

## Environment Variables

| Variable | Purpose |
|----------|---------|
| `EXPO_PUBLIC_TOOLKIT_URL` | API base URL (default: https://toolkit.oandp.ai) |
| `EXPO_PUBLIC_PROJECT_ID` | Analytics project ID |
| `EXPO_PUBLIC_TEAM_ID` | Analytics team ID |
| `NODE_ENV` | Environment (development/production) |

---

## Usage Example

```tsx
// app/_layout.tsx
import { Slot } from 'expo-router';
import { useOandPAgent, createOandPTool } from '@oandp-ai/toolkit-sdk';
import { z } from 'zod';

// Define tools
const tools = {
  weather: createOandPTool({
    description: "Get weather for a city",
    zodSchema: z.object({ city: z.string() }),
    execute: async ({ city }) => `Weather in ${city}: Sunny`
  })
};

export default function RootLayout() {
  return <Slot />;
}

// screens/chat.tsx
import { useOandPAgent, createOandPTool } from '@oandp-ai/toolkit-sdk';

export default function ChatScreen() {
  const agent = useOandPAgent({ tools });
  
  return (
    <View>
      {agent.messages.map(msg => (
        <Text key={msg.id}>{msg.parts[0].text}</Text>
      ))}
      <Button onPress={() => agent.sendMessage("What's the weather?")} />
    </View>
  );
}
```

---

## Key Takeaways

1. **Proprietary SDK** - Only for apps created via OandP.ai
2. **AI-First** - Built around Vercel AI SDK with tool calling
3. **Expo Optimized** - Deep integration with Expo Router and Metro
4. **Cross-Platform** - Web polyfills for native-only modules
5. **Dev Tools** - Built-in error boundary and element inspector
6. **Analytics** - PostHog integration with automatic tracking
7. **OAuth Ready** - Connection management for third-party services
