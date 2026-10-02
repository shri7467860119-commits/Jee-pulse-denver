# Security Specification — JEE Pulse

## 1. Data Invariants

1. **Authentication & Ownership Invariant**: Every write and read operation requires an authenticated Google Sign-In user (`request.auth != null`). Private user collections (`/test_attempts/{attemptId}`, `/study_tasks/{taskId}`, `/bookmarks/{bookmarkId}`) strictly require `resource.data.userId == request.auth.uid` for reads/deletes and `request.resource.data.userId == request.auth.uid` for writes.
2. **Leaderboard Ownership & Public Visibility Invariant**: Each document in `/leaderboard/{userId}` must have `documentId == request.auth.uid` and `userId == request.auth.uid` on create/update/delete. List queries on `/leaderboard` must explicitly filter by `resource.data.visibility == 'public'`. Zero PII (no email, phone, or address) is stored in `/leaderboard`.
3. **Immutable Metadata Invariant**: `userId` and `createdAt` are immortal fields and cannot be mutated during any `update` operation (`incoming().userId == existing().userId && incoming().createdAt == existing().createdAt`).
4. **Temporal Integrity Invariant**: All timestamp fields (`createdAt`, `updatedAt`) must be Firestore `timestamp` objects (`is timestamp`) and must satisfy `<= request.time`.
5. **Path & Size Bound Invariant**: All document IDs must satisfy `isValidId(id)` (`^[a-zA-Z0-9_\-]+$`, length <= 128). Every string and list field has strict minimum/maximum size bounds.

## 2. The "Dirty Dozen" Payloads

1. **Unauthenticated Read/Write**: Caller with `request.auth == null` attempts `get`, `list`, or `create` on `/test_attempts` or `/leaderboard`. -> **REJECTED** (`isSignedIn()`).
2. **Cross-User Test Attempt Read**: Authenticated user Alice (`uid: "alice_123"`) attempts to `get` or `list` Bob's (`uid: "bob_456"`) document in `/test_attempts`. -> **REJECTED** (`existing().userId == request.auth.uid`).
3. **Identity Spoofing on Create**: Alice attempts to create a `/test_attempts` or `/leaderboard` document with `userId: "bob_456"`. -> **REJECTED** (`incoming().userId == request.auth.uid`).
4. **Shadow Update / Ghost Field Injection**: Alice updates `/study_tasks/{taskId}` or `/leaderboard/{userId}` including an undeclared field `"isAdmin": true` or `"verified": true`. -> **REJECTED** (`incoming().keys().hasOnly(...)` and `affectedKeys().hasOnly(...)`).
5. **Owner Reassignment on Update**: Alice attempts to change `userId` on an existing `/bookmarks/{bookmarkId}` document to `"bob_456"`. -> **REJECTED** (`incoming().userId == existing().userId` and `userId` excluded from `affectedKeys().hasOnly(...)`).
6. **CreatedAt Tampering on Update**: Alice attempts to backdate `createdAt` during an `update` on `/study_tasks/{taskId}`. -> **REJECTED** (`incoming().createdAt == existing().createdAt`).
7. **Future Timestamp Injection**: Alice sends `createdAt` or `updatedAt` set 1 hour in the future (`> request.time`). -> **REJECTED** (`incoming().createdAt <= request.time`).
8. **Value Poisoning on Update**: Alice updates `completed` in `/study_tasks/{taskId}` with a string `"yes"` instead of a boolean, or sets `xpPoints` in `/leaderboard/{userId}` to `-500` or `"999999"`. -> **REJECTED** (`isValidStudyTask(incoming())` / `isValidLeaderboardProfile(incoming())`).
9. **Unbounded Array / Denial of Wallet**: Alice submits `weakTopics` with 50 items (exceeding max 20) or `displayName` with 5000 characters. -> **REJECTED** (`size() <= 20` and `size() <= 60`).
10. **ID Poisoning**: Alice attempts to create a document with ID containing spaces or special characters (`"bad/id$123"`). -> **REJECTED** (`isValidId()`).
11. **Unfiltered List Query Scraping**: Alice executes `db.collection("test_attempts").get()` without `.where("userId", "==", "alice_123")`. -> **REJECTED** (`allow list` checks `existing().userId == request.auth.uid`).
12. **Invalid Enum Injection**: Alice creates a `/study_tasks/{taskId}` document with `subject: "History"` or `priorityLevel: "CRITICAL"`. -> **REJECTED** (enum allowlist validation).

## 3. Conflict & Red Team Audit Summary

- **Identity Spoofing**: Blocked across all 4 collections via `incoming().userId == request.auth.uid` on create and `incoming().userId == existing().userId` on update.
- **Shadow Updates**: Blocked via strict `hasOnly()` on create and `affectedKeys().hasOnly()` on update.
- **Value Poisoning**: Blocked because every `allow update` begins with `isSignedIn() && isValidId(...) && isValid[Entity](incoming())`.
- **PII Blanket Check**: No PII is stored in any collection; `/leaderboard` stores only public gamification stats and requires `visibility == 'public'`.
