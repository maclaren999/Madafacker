TODO: Make draft message work

# Mobile Random Message Social App – Requirements  (v1.0)

---

## 1  Product Vision

Deliver a light‑weight, low‑friction social experience where users broadcast a single line of text to strangers and receive unexpected
thoughts in return. The app features two distinct content filtering modes:

1. Shine Mode - Strict content filtering that removes abuse, hate speech, harassment, sexism, racism and other harmful content to maintain a
   positive environment
2. Shadow Mode - Light content filtering that only removes illegal content (drug trafficking, exploitation of minors, explicit violence)
   while allowing more controversial expression

The app balances these contrasting approaches while rewarding creativity and respectful engagement.

## 2  Business Goals

1. **Daily Delight:** Drive habitual, micro‑time engagement (30‑60 s sessions).
2. **Viral Growth Loop:** Push‑notification delivery of messages → ratings → wider distribution → screenshots/shared quotes.
3. **Positive Net‑Promoter Score:** Minimise harassment via opt‑in “Shine” mode and rate‑gated exposure in “Shadow” mode.
4. **Monetisation (Phase 2):** Sponsored prompts, coin packs for additional replies, themed UI skins.

## 3  Personas & Use‑Cases

| Persona                | Motivation                                  | Key Flows                                               |
|------------------------|---------------------------------------------|---------------------------------------------------------|
| **The Support‑Seeker** | Wants to brighten someone’s day & feel good | Writes in *Shine*, rates messages, builds streaks       |
| **The Ranter**         | Needs a safe outlet for spicy takes         | Writes in *Shadow*, hunts witty lines                   |
| **The Lurker**         | Prefers reading & rating                    | Receives notifications, rarely writes                   |
| **The Connector**      | Enjoys making new friends and networking    | Engages actively, replies to messages, shares content   |
| **The Critic**         | Provides thoughtful feedback and ratings    | Rates messages carefully, reports inappropriate content |
| **The Creator**        | Loves crafting unique and creative lines    | Writes frequently, experiments with styles and tones    |

## 4  MVP Feature Set

### 4.1 Onboarding & Tutorial Sequence

* **Multi‑Page Intro:** A three‑panel carousel explaining core mechanics:

  1. **Random Broadcast:** “Write a short line. We'll deliver it randomly to active users.” Diagram of dots moving between devices.
  2. **Mode Toggle:** “Drag or tap the sun icon to switch modes.” Demo animation where the sun sinks and the moon emerges.
  3. **Receive & Rate:** “When messages arrive, read and rate them.” Includes swipe gesture visual.
* **Auto‑Advance & Skip:** Panels auto‑advance after 2–3 seconds; “Skip” becomes available after the first panel.
* \*\*Set Nickname Prompt: Immediately after intro, show auth page.

  * Footer contains a **Privacy Policy** link.
  * Push‑notification permission request fires once nickname is confirmed.

### 4.1.1 New Authentication Flow (Updated Implementation)

**State 1 - Initial/Unauthenticated:**

* Display welcome message explaining the app concept: "Share random thoughts with strangers and discover unexpected perspectives"
* Show "Log in with Google" button as the primary action
* Hide all other UI elements until Google authentication is complete

**State 2 - Post-Google Authentication:**

* If user already exists in backend: Navigate directly to main screen
* If user is new (not registered in backend):
  * Show nickname input field with real-time validation
  * Show "Create Account" button (enabled only when nickname is valid)
  * Validate nickname availability via `/api/user/check-name-availability`
  * Send `createUser` API request when submitted
  * Navigate to main screen after successful user creation

**Technical Implementation:**

* Single Google Sign-In flow for both new and existing users
* Backend user existence determined via `/api/user/current` endpoint after Google authentication
* Follows API contract specified in section 9 for user creation and validation
* Push notification permission request fires after successful authentication

### 4.2 UI Navigation Tabs

Establish four primary tabs in the bottom navigation bar to support the Write‑focused flow and clear separation of content:

| Position | Purpose                                        | Label    | Icon |
|----------|------------------------------------------------|----------|------|
| **1.**   | Compose and view your last 3 sent messages     | Write    | ✍️   |
| **2.**   | Sent messages + their replies (tap for thread) | My Posts | 💬   |
| **3.**   | Review messages you’ve received and reply      | Inbox    | 📥   |
| **4.**   | Account settings and profile                   | Account  | 👤   |

### 4.3  Account & Settings

* Google sign‑in links messages across devices.
* Avatar & 80‑char bio.
* Language + Locale selector (default phone locale).

### 4.4 Error & Informational States

* **Offline send failure:** Silently display a small progress icon on the message and do the Background retry via WorkManager.
* **Nickname already taken:** Inline field error prompting a new name.
* **Moderation rejection (Shine):** Dialog suggests switching to Shadow mode if the user wants uncensored wording.
* **Push permission denied:** Snackbar outlining that pushes are essential to the experience and promising no spam, plus a button to open system settings.
* **No messages yet:** Empty‑state copy: “No messages yet.”

**Spam/Flood Control:** Backend enforces a **30‑second** minimum interval between sends; returns HTTP 429 with a friendly error.


## 5  Future (Post‑MVP) Backlog

1. **Iterative Distribution:** ≥ 4★ average after first 10 ratings ⇒ resend to ×10 users.
2. **Adaptive Notifications:** Behaviour‑based quota (1‑4 per day).
3. **Locale Hints:** Keyboard language ≠ selected locale ⇒ prompt switch.
4. **Public Inbox Experiment:** Top 50 daily messages in read‑only inbox.
5. **Moderation Dashboard:** Admin web panel, real‑time abuse reports.
6. **Report / Block Abuse:** In‑app “Report” action on any message (Phase 2)

## 6  Non‑Functional Requirements

* **Latency:** <300 ms API round‑trip for message send.
* **Scalability:** Use topic‑based FCM for batched sends.
* **Legal:** Comply with GDPR/CCPA.
* **Analytics:** Instrument Firebase Analytics to track key events (app*open, send\_message, receive\_notification, open\_notification,
  dismiss\_*notification, rate\_message, toggle\_mode) and funnel metrics for ongoing product improvement.
* **Crash Reporting:** Integrate Firebase Crashlytics for real‑time crash and ANR monitoring.
* **Spam/Flood Control:** 30‑second minimum send interval enforced server‑side.
* **Kotlin Multiplatform Preparedness:** Structure the Android codebase (business logic, network layer) to facilitate a future migration to Kotlin Multiplatform (KMP) for shared modules across mobile platforms.
  Avoid Java libs - use Kotlin-based libs if possible.

### 6.1 Content Filtering for Shine Mode

To automatically moderate user-generated text in **Shine** mode with a lightweight integration, adopt a hybrid of server-side ML and client-side filtering:

| Tool / Service                       | Platform         | Key Features                                                              | Integration Effort                                                            |
| ------------------------------------ | ---------------- | ------------------------------------------------------------------------- | ----------------------------------------------------------------------------- |
| **OpenAI Moderation API**            | Backend          | Multidimensional policy checks: abuse, hate, sexual, self-harm, profanity | Single REST call to `/moderations`; free and unlimited usage (no token quota) |
| **Firebase ML Kit Profanity Filter** | Mobile (Android) | On-device simple profanity blacklist; instant feedback                    | Add ML Kit SDK + Profanity Extension                                          |

**Recommended MVP Approach:**

1. **Server-Side Enforcement:** For every Shine‑mode submission, call the **OpenAI Moderation API**. If any category (e.g., profanity, hate)
   exceeds the threshold (e.g., 0.6), reject with a user-friendly error: “Please keep it positive or switch to Shadow mode!”
2. **Client-Side Quick Check:** Use **Firebase ML Kit Profanity Filter** in the Android app to flag disallowed words instantly, helping users self-correct before sending.
3. **Custom Blocklist (Optional):** Maintain a small server-hosted JSON list of brand‑ or culture‑specific banned terms; screen messages against it post‑API check.

This hybrid solution gives robust, free moderation out of the box, minimizes false positives through client prompts, and keeps your server code simple.

## 7  Technical Architecture (High‑Level)


flowchart TD
  Mobile(Android) -- REST/JSON --> API(Gateway)
  API --> Node(NestJS Service Cluster)
  Node -- ORM --> DB(PostgreSQL)
  Node --> FCM(Push)
  Cron(Scheduled Jobs) --> Node


* **Security:** Bearer `token` header for user JWT; `x-api-key` for cron jobs.
* **Data Storage:** Users, Messages, Replies (see §8).
* **Push Delivery:** Outgoing messages are delivered via Firebase Cloud Messaging (FCM) to **randomly selected** active users within the same mode.

## 8  Data Model Summary

| Entity          | Key Fields                                                                    | Relations                 |
|-----------------|-------------------------------------------------------------------------------|---------------------------|
| **User**        | id String, name, registrationToken?, coins, createdAt, updatedAt              | 1‑\* Message, 1‑\* Reply  |
| **Message**     | id, body, mode, isPublic, authorId, createdAt, localState, tempId?, needsSync | *‑1 User, 1‑* Reply       |
| **Reply**       | id, body, mode, isPublic, parentId?, authorId, createdAt, updatedAt           | \*‑1 Message, self‑nested |
| **UnsentDraft** | body, mode, timestamp *(Client-only)*                                         | N/A                       |

## 9  API Contract Coverage (Postman)

Below are the canonical endpoints as documented in Postman. Note that server currently uses `mode` values `light` or `dark` (to be renamed to `shine`/`shadow` via migration).

### User Endpoints

* **Create user**
  `POST /api/user`
  • **Request Body** (JSON):
  • `name` (string, required): User’s nickname.
  • `registrationToken` (string, optional): FCM registration token for push notifications.
  • **Response** (201 Created, JSON):
  • `id` (string)
  • `name` (string)
  • `updatedAt` (string)
  • `createdAt` (string)

* **Get current user**
  `GET /api/user/current`
  • **Headers**: `token: <user-token>`
  • **Response** (200 OK, JSON):
  • `id` (string)
  • `name` (string)
  • `createdAt` (string)
  • `updatedAt` (string)

* **Update current user**
  `PATCH /api/user/current`
  • **Headers**: `token: <user-token>`
  • **Request Body** (JSON, optional fields):
  • `name` (string)
  • `registrationToken` (string)
  • **Response** (200 OK, JSON): Updated user object (same fields as Get current user).

* **Check name availability**
  `GET /api/user/check-name-availability?name=<string>`
  • **Response** (200 OK, JSON):
  • `nameIsAvailable` (boolean)

### Message Endpoints

* **Create message**
  `POST /api/message`
  • **Headers**: `token: <user-token>`
  • **Request Body** (JSON):
  • `body` (string, required)
  • `mode` (string, required): `"light"` or `"dark"`
  • **Response** (201 Created, JSON):
  • `id` (string)
  • `public` (boolean, always true)
  • `wasSent` (boolean, false if queued)
  • `body` (string)
  • `mode` (string)
  • `authorId` (string)
  • `updatedAt` (string)
  • `createdAt` (string)
  • `parentId` (null)

* **Rate message**
  `PATCH /api/message/{id}/rate`
  • **Headers**: `token: <user-token>`
  • **Request Body** (JSON):
  • `rating` (string): `"dislike"`, `"like"`, or `"superlike"`
  • **Response** (200 OK, no body)

* **Get incoming messages**
  `GET /api/message/current/incoming`
  • **Headers**: `token: <user-token>`
  • **Response** (200 OK, JSON Array of messages): Fields per message:
  • `id`, `body`, `mode`, `public`, `wasSent`, `createdAt`, `updatedAt`, `authorId`, `parentId` (string or null)
  • `replies` (array of reply objects, may be omitted if empty)

* **Get outcoming messages**
  `GET /api/message/current/outcoming`
  • **Headers**: `token: <user-token>`
  • **Response** (200 OK, JSON Array of messages): Same schema as incoming, but ordered by user’s own authored messages.

### Reply Endpoints

> Note: In MVP, coin-based reply creation is disabled. Replies still use `public` flag for privacy.

* **Create reply**
  `POST /api/reply`
  • **Headers**: `token: <user-token>`
  • **Request Body** (JSON):
  • `body` (string, required)
  • `public` (boolean, required)
  • `parentId` (string, required): ID of parent message or reply
  • **Response** (201 Created, JSON):
  • `id`, `wasSent` (boolean), `body`, `public`, `mode`, `parentId`, `authorId`, `updatedAt`, `createdAt`

* **Update reply**
  `PATCH /api/reply`
  • **Headers**: `token: <user-token>`
  • **Request Body** (JSON):
  • `id` (string, required)
  • `public` (boolean, optional)
  • **Response** (200 OK, JSON): Updated reply object with full fields.

* **Get reply by ID**
  `GET /api/reply/{id}`
  • **Headers**: `token: <user-token>`
  • **Response** (200 OK, JSON): Reply object, including nested `replies` array.

### Cron Job

* **Send messages job**
  `POST /api/cron/send-messages`
  • **Headers**: `x-api-key: <cron-key>`
  • **Response**: 200 OK on success.

## 10  Naming – Modes

| Positive Mode | Unfiltered Mode |
| ------------- | --------------- |
| **Shine**     | **Shadow**      |

*(Locked for MVP)*

## 11  Resolved Decisions

1. **Maximum daily sends per user:** Limited to 10 messages per day in Phase 1.
2. **Shadow mode content filtering:** Explicit illegal content will still be filtered starting with Phase 2.
3. **Economy mechanics (coin sink/inflation control):** Deferred to Phase 2 backlog.
4. **Translation vs. locale restriction:** ML-driven auto‑detect deferred to Phase 2.
5. **Tablet / foldable layouts:** Not a priority for MVP.

---
