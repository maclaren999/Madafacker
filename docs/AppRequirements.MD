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

Establish four primary tabs in the top navigation tabs to support the Write‑focused flow and clear separation of content:

| Position | Purpose                                        | Label    | Icon |
|----------|------------------------------------------------|----------|------|
| **1.**   | Compose and view your last 3 sent messages     | Write    | ✍️   |
| **2.**   | Sent messages + their replies (tap for thread) | My Posts | 💬   |
| **3.**   | Review messages you’ve received and reply      | Inbox    | 📥   |
| **4.**   | Account settings and profile                   | Account  | 👤   |

### 4.3  Account & Settings

* Google sign‑in links messages across devices.
* Language + Locale selector (default phone locale). #NON-MVP

### 4.4 Error & Informational States

* **Send rejection (moderation / rate limit):** Keep the draft in the TextField, disable input while sending, and show the server-provided error inline; no background retry/queue.
* **Nickname already taken:** Inline field error prompting a new name.
* **Moderation rejection (Shine):** Inline message suggests switching to Shadow mode when wording is rejected.
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
* **Crash Reporting:** ✅ **IMPLEMENTED** - Firebase Crashlytics integrated for real‑time crash and ANR monitoring with custom keys and user
  identification.
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
   exceeds the threshold (e.g., 0.6), reject with an HTTP 4xx error that includes a user-facing message (and optional error code) so the client can show inline feedback.
2. **Client-Side Quick Check:** Use **Firebase ML Kit Profanity Filter** in the Android app to flag disallowed words instantly, helping users self-correct before sending.
3. **Custom Blocklist (Optional):** Maintain a small server-hosted JSON list of brand‑ or culture‑specific banned terms; screen messages against it post‑API check.

This hybrid solution gives robust, free moderation out of the box, minimizes false positives through client prompts, and keeps your server code simple.

## 7  Technical Architecture (High‑Level)


flowchart TD
  Mobile(Android) -- REST/JSON --> API(Gateway)
API --> Node
Node -- ORM --> DB
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
  • `wasSent` (boolean, always true)
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

| Filtered Mode | Unfiltered Mode |
|---------------|-----------------|
| **Shine**     | **Shadow**      |

*(Locked for MVP)*

## 11  Resolved Decisions

1. **Maximum daily sends per user:** Limited to 10 messages per day in Phase 1.
2. **Shadow mode content filtering:** Explicit illegal content will still be filtered starting with Phase 2.
3. **Economy mechanics (coin sink/inflation control):** Deferred to Phase 2 backlog.
4. **Translation vs. locale restriction:** ML-driven auto‑detect deferred to Phase 2.
5. **Tablet / foldable layouts:** Not a priority for MVP.

## 12. Adaptive Notification System

### 12.1 Feature Overview

The notification system delivers incoming messages via silent push notifications with generic placeholder content to maintain privacy and
intrigue. The system adapts notification frequency based on user engagement patterns while respecting timezone and sleep schedules.

**Core Principles:**
- Privacy-first: Actual message content hidden until app is opened
- Engagement-driven: Frequency adapts based on user behavior
- Mode-aware: Different presentation for Shine vs Shadow modes
- Silent delivery: No sound/vibration, visible in notification tray only

### 12.2 Notification Content & Placeholders

**Shine Mode Placeholders (Supportive/Empathetic):**
- "Someone shared a thought with you ✨"
- "A stranger left you something to consider 💭"
- "Fresh perspective from the universe 🌟"
- "You've got a random message waiting 🎲"
- "Someone reached out to you ☀️"
- "A soul dropped a line for you 📝"
- "Something meaningful just arrived 🌸"
- "A human moment awaits ⚡"

**Shadow Mode Placeholders (Edgy/Mysterious):**
- "Someone shared their unfiltered thoughts 🌙"
- "A wild message appeared from the shadows 🎪"
- "Raw thoughts from a stranger 🔮"
- "Someone dropped their guard for you 🎯"
- "Unfiltered wisdom just landed 🌊"
- "A stranger's honest take awaits 🎭"
- "Something real just surfaced 🌑"
- "Truth from the underground 🔥"

**Visual Styling:**
- **Shine Mode:** Warm colors (gold/yellow accent), sun icon
- **Shadow Mode:** Cool colors (purple/blue accent), moon icon
- **Notification Icon:** Mode-specific small icon in status bar

### 12.3 Frequency Management System

**Base Frequency:** 1-4 notifications per day (configurable via Firebase Remote Config)

**Engagement-Based Adaptation:**
```
High Engagement (>80% open rate): 3-4 notifications/day
Medium Engagement (40-80% open rate): 2-3 notifications/day
Low Engagement (10-40% open rate): 1-2 notifications/day
Very Low Engagement (<10% open rate): 1 notification/day
```

**Timing Constraints:**
- **Nighttime Blackout:** No notifications between 10 PM - 8 AM (user's timezone)
- **Minimum Interval:** 3 hours between notifications
- **Maximum Interval:** 18 hours (to ensure daily engagement)

**Dormant User Re-engagement:**
- After 7 days of ignored notifications: Send special feedback prompt
- Placeholder: "Why are you ignoring me? 🥺 Tap to let us know"
- Links to in-app feedback form with detailed options

### 12.4 User Feedback Tracking

**Tracked Events:**
- `notification_received` - FCM delivery confirmed
- `notification_opened` - User taps notification
- `notification_dismissed` - User swipes away notification
- `notification_ignored` - Unread after 12 hours
- `notification_feedback_prompt_sent` - Special re-engagement notification sent
- `notification_feedback_submitted` - User completed feedback form
- `feedback_submitted` - User submitted feedback via Account Tab (✅ Implemented)
- `message_rated` - User rated message (dislike/like/superlike)
- `message_replied` - User sent reply to message

**Client-Side Logic:**
- Track engagement metrics locally using DataStore
- Calculate engagement rate over rolling 7-day window
- Use Firebase Remote Config for frequency parameters
- All events sent to Firebase Analytics

### 12.5 UI/UX Behavior

**Notification Tray:**
- Mode-specific icon and accent color
- Generic placeholder text only (randomly selected by client)
- Silent delivery (no sound/vibration)
- Notifications remain up to 12 hours if not opened

**Deep Link Behavior:**
- Notification tap → Navigate to Inbox tab
- Target message highlighted with existing highlighting system (most recent unread)
- Users can tap any message to enter "Replying" state

**Interactive Message States:**

- **Default State:** Simple MessageCard with tap-to-interact functionality
- **Replying State:** Enhanced interactive card triggered by tapping, featuring:
  - **User's Previous Replies:** Display existing replies from current user in condensed format
  - **Rating Buttons:** Three options (Dislike, Like, Superlike) with visual feedback
  - **Reply Input:** Text field with "Tap to reply to this message..." placeholder
  - **Action Buttons:** Send Reply button
  - **Database Integration:** Fetches user replies by matching authorId with current user ID

**Rating Interaction:**
- After selection, button is highlighted with background color
- Visual feedback confirms rating was recorded
- Rating options:
  - Dislike (👎)
  - Like (👍)
  - Superlike (❤️)
- Rating analytics event sent only after 10 seconds (or when screen closed) in case if user changes mind

**Simplified Analytics:**
- Direct Firebase Analytics integration (no local storage)
- Real-time event tracking for immediate insights
- Backend analytics via BigQuery export from Firebase

### 12.6 Technical Implementation

**Firebase Messaging Service Updates:**
```kotlin
// Enhanced notification data payload
{
  "messageId": "msg_123",
  "mode": "shine|shadow",
  "actualContent": "hidden_until_opened",
  "timestamp": "ISO8601"
}
```

**Notification Channel Configuration:**
```kotlin
NotificationChannel(
  channelId,
  "Message Notifications",
  NotificationManager.IMPORTANCE_DEFAULT
).apply {
  setSound(null, null) // Silent
  enableVibration(false)
  setShowBadge(true)
}
```

**Remote Config Parameters:**
- `notification_frequency_base`: Default daily frequency
- `engagement_thresholds`: JSON object with engagement brackets
- `nighttime_start_hour`: Blackout period start (default: 22)
- `nighttime_end_hour`: Blackout period end (default: 8)
- `placeholder_messages_shine`: Array of Shine mode placeholders
- `placeholder_messages_shadow`: Array of Shadow mode placeholders

### 12.7 Simplified Analytics Integration

**Direct Firebase Analytics Events (No Local Storage):**
```kotlin
// Real-time event tracking
"notification_received" -> {
  "message_id": "msg_123",
  "mode": "shine|shadow",
  "user_timezone": "UTC_offset"
}

"notification_opened" -> {
  "message_id": "msg_123",
  "mode": "shine|shadow",
  "time_to_open": "seconds"
}

"notification_dismissed" -> {
  "message_id": "msg_123",
  "mode": "shine|shadow",
  "time_in_tray": "seconds"
}

"notification_ignored" -> {
  "message_id": "msg_123",
  "mode": "shine|shadow",
  "hours_ignored": 24
}

"message_rated" -> {
  "message_id": "msg_123",
  "rating": "dislike|like|superlike",
  "mode": "shine|shadow",
  "via_notification": "boolean"
}

"message_replied" -> {
  "message_id": "msg_123",
  "mode": "shine|shadow",
  "via_notification": "boolean",
  "reply_length": "characters"
}
```

**Backend Analytics via BigQuery:**
- Firebase Analytics automatically exports to BigQuery
- Backend queries notification data using SQL
- Real-time dashboard capabilities
- No custom API endpoints needed for analytics data

### 12.8 Configuration Management

**Firebase Remote Config Setup:**
- A/B test different placeholder sets
- Experiment with frequency algorithms
- Adjust timing constraints by region
- Test engagement threshold brackets

### 12.9 Feedback Form Design

**Account Tab Feedback Form (✅ Implemented):**

- **Star Rating System:** 1-5 stars (optional)
- **Text Feedback:** Free-form text input, 500 character limit (optional)
- **Form Validation:** Requires either rating OR text feedback
- **Firebase Analytics Integration:**
  - Event: `feedback_submitted`
  - Parameters: `rating`, `feedback_length`, `user_id`, `timestamp`, `has_rating`, `has_text`
- **UI/UX:** Material Design dialog with loading states and success feedback
- **Accessibility:** Follows existing app patterns and MVVM architecture

**Dormant User Feedback Options:**

- **Frequency Issues:**
  - "Too many notifications"
  - "Too few notifications"
  - "Bad timing (wrong hours)"
- **Content Issues:**
  - "Messages aren't interesting"
  - "Wrong content mode for me"
  - "Messages feel repetitive"
- **Technical Issues:**
  - "App is too slow"
  - "Notifications don't work properly"
  - "Hard to use interface"
- **Personal Reasons:**
  - "Too busy right now"
  - "Lost interest in the app"
  - "Privacy concerns"
- **Open Input Field:** "Tell us more..." (optional, 200 char limit)

**Form Behavior:**

- Multiple selection allowed
- Submit triggers `notification_feedback_submitted` event
- Results inform frequency algorithm adjustments
- Anonymous feedback stored for product improvement

## 15 Recent Updates

### 15.1 MessageCard Refactor (v1.1)

**Overview:**
Completed refactor to consolidate message interaction functionality into a single MessageCard component with multiple states, replacing the
separate HighlightedMessageCard component.

**Key Changes:**

- **New "Replying" State:** Added interactive state to MessageCard triggered by tapping
- **Unified Component:** Replaced HighlightedMessageCard with enhanced MessageCard functionality
- **Database Integration:** Added methods to fetch user's existing replies for messages
- **State Management:** Implemented replyingMessageId tracking similar to existing highlightedMessageId
- **Preserved Highlighting:** Maintained existing logic for highlighting last unread and notification-driven messages

**Technical Implementation:**

- Added `getRepliesByParentIdAndAuthor()` to DAO and repository layers
- Enhanced MainScreenContract with replying state management methods
- Updated InboxTab to use unified MessageCard with conditional rendering
- Maintained simple MessageCard styling as base with interactive elements in Replying state
- Integrated with existing reply creation and analytics systems

**User Experience:**

- Tap any message to enter interactive "Replying" state
- View existing user replies in condensed format
- Rate messages and compose replies in unified interface
- Close interaction to return to simple card view
- Seamless integration with notification highlighting system

### 15.2 Theme Mode Integration (v1.1)

**Overview:**
Integrated app theme directly with the Mode system, making dark/light theme automatically follow the current mode selection (SHINE/SHADOW).

**Key Changes:**

- **Direct Mode Dependency:** Theme now directly uses `currentMode` from PreferenceManager
- **Automatic Theme Switching:** SHADOW mode = dark theme, SHINE mode = light theme
- **Reactive Updates:** Theme changes immediately when user toggles between modes
- **Dependency Injection:** MainActivity now injects PreferenceManager for theme access

**Technical Implementation:**

- Modified `MadafakerTheme` to accept `Mode` parameter instead of `darkTheme: Boolean`
- Updated `MainActivity` to inject `PreferenceManager` and observe `currentMode`
- Theme automatically recomposes when mode changes via `collectAsState()`
- Maintained fallback to system theme when mode is null

**User Experience:**

- SHINE mode automatically applies light theme
- SHADOW mode automatically applies dark theme
- Instant theme switching when toggling modes
- Consistent visual experience aligned with content filtering mode

---

### 15.3 Mode Toggle Tip Behavior (v1.2)

- The tooltip for switching Shine/Shadow now appears only on the first app launch after a user signs
  in.
- Logout clears the hint flag so the next successful login surfaces the tip once, then it stays
  hidden until the next login.
- Preference reset happens alongside auth token storage to guarantee the hint for each new session.




