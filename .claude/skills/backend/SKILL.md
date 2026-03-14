---
name: backend
description: Stack is Hono and tRPC. Read when building APIs or server-side logic. Read SETUP-BACKEND.md to setup backend.
---

## Do You Really Need Backend?

Most apps can reach $1M ARR without a backend—unless they're social apps or multiplayer games.

**Before enabling backend, consider:**

- AI features are built-in; no backend needed for that
- Backend adds complexity and maintenance burden
- Apps like Cal AI, Umax, QUITTR were built without backend

If the user asks for backend but doesn't need it, politely suggest alternatives.

**To enable backend:** read `SETUP-BACKEND.md`

---

## Stack Overview

- **Server:** Node.js with [Hono](https://hono.dev/)
- **API:** [tRPC](https://trpc.io/)
- **Entry Point:** `backend/hono.ts`

### Entry Point

`backend/hono.ts` is the main file that gets deployed. Without it, the backend won't deploy.

The exported by default Hono() is mounted at `/api`. So for example tRPC should be mounted at /trpc
then it will be available at `/api/trpc/...`

---

## Structuring tRPC Procedures

Create a separate file for each endpoint:

```
backend/trpc/example/hi/route.ts
```

```ts
export const hiProcedure = protectedProcedure.query(() => {
  /* ... */
});
```

Import and register it in `backend/trpc/app-router.ts`.

---

## Client Usage

Two ways to call the backend from `@/lib/trpc`:

| Method       | Use Case         | Context                   |
| ------------ | ---------------- | ------------------------- |
| `trpc`       | React components | Returns React tRPC client |
| `trpcClient` | Non-React files  | Pre-initialized client    |

**In React:**

```ts
const hiQuery = trpc.example.hi.useQuery();
```

**Outside React:**

```ts
const data = await trpcClient.example.hi.query();
```
