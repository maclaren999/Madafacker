## UI main issues

1. [X] Tabs naming: write, my posts, inbox, account. The resources already exist.
2. [X] Remove rating buttons/icons in "my posts" and "inbox". We can return them redesigned if BE confirms the proper implementation.
   (at the moment, BE allows rating placement, but never sends any ratings with messages)
3. [X] My posts: implement replies displaying. For each post show total number and one most recent reply.
4. [X] Inbox(feed): polish replies UI styling.
5. [X] Account: remove coins; apply proper styling to buttons - outlined, transparent.
6. [Maria] Mode toggle with simple sinking sun animation.
7. [] "Tip": show new user how to change modes by tapping on the sun.
8. [] ! My posts: impl expanded state for selected post. User taps -> post with replies is drawn on blurred background. (check Figma
   prototype)
9. [] Likes: impl dislike, like and superlike buttons(similar to Netflix). For 'my posts' they have to be small, with numbers, as they are
   non clickable. For 'inbox' they have to be bigger, clickable.
10. [X] My posts: scroll glitch.
11. [X] Bug: after sending reply -> open Write tab -> inappropriate "reply send" snackbar is shown.
    Fixed: moved reply success snackbar to InboxTab only.

## UI secondary issues

51. [X] Recent messages: hide section if no messages yet.
52. [] Tabs swipe animation: titles slide horizontally, underline graphics stays in place.
53. [] Improve button "Send reply" styling/states.
54. [] Change error theme color
55. [X] Lock portrait orientation only in activities.

## Logic

100. [X] Mode switching has to change content accordingly.
101. [Artem] Revise the notifications logic. (? test with BE)
102. [pending-BE] Moderation: Construct a flow when BE rejects a message sending (e.g., due to inappropriate content). Show proper error
     message to user.
    1) Client will wait for HTTP response from BE. If BE rejects the message, client shows appropriate error message to user.
    2) BE targets implementation of RFC 7807 (Problem Details for HTTP APIs) to give back a payload with error code and meaningful message.
     3) [X] Disable the offline sending queue for now. Because it adds too much complexity.
     4) [pending] Handle BE error -> map to user-friendly message -> show under write TextField.
    
## Bugs

300. [X] Context: *New user logged in with Google and end up on "Choose the nickname*. If user presses back button, the main screen is
     shown, but the User account has not been created. Fix: bring user back to 'AuthUiState.INITIAL'; forget the google account; let user
     choose new account.
301. [X] Logged in user. No internet connection. User opens the app (cold start) the sees Welcome AuthScreen instead of MainScreen.
302. [X] Messages arrangement by dates my be unstable. Messages mix up sometimes.
303. [X] Replies are not shown under messages in Inbox. After sending a reply, it is not displayed under the message.
304. [] Replies are not shown for incoming message. (check outcoming messages too)
305. [X] The Auth state/navigation logic - FIXED with V2.1 Optimistic Auth.
     Note: Firebase Auth doesn't persist session across cold starts (known issue with Credential Manager flow).
     Workaround: Use `awaitInitialization()` to wait for Firebase, then restore session with stored Google ID token.
     See docs/AUTH_SYSTEM_V2.md for details.

## Docs

900. [X] Revise the AppRequirements and DevStatus
901. [X] Create a Privacy Policy document. Add To Welcome screen and Account tab.