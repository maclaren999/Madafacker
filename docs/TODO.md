## UI main issues

1. [X] Tabs naming: write, my posts, inbox, account. The resources already exist.
2. [X] Remove rating buttons/icons in "my posts" and "inbox". We can return them redesigned if BE confirms the proper implementation.
   (at the moment, BE allows rating placement, but never sends any ratings with messages)
3. [Artem] My posts: implement replies displaying. For each post show total number and one most recent reply.
4. [Artem] Inbox(feed): polish replies UI styling.
5. [X] Account: remove coins; apply proper styling to buttons - outlined, transparent.
6. [Maria] Mode toggle with simple sinking sun animation.

## UI secondary issues

50. [] Tab swipe animation. Bolder font of the selected tab. Carousel or cascade effect when swiping.

## Logic

100. [X] Mode switching has to change content accordingly.
101. [Artem] Revise the notifications logic. (? test with BE)
    
## Bugs

300. [X] Context: *New user logged in with Google and end up on "Choose the nickname*. If user presses back button, the main screen is
     shown, but the User account has not been created. Fix: bring user back to 'AuthUiState.INITIAL'; forget the google account; let user
     choose new account.

## Docs

900. Revise the AppRequirements and DevStatus