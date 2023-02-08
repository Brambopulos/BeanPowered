# BeanPowered
 A spoof Steam app.

Made for a certain class at a certain university. Keeping it vague so as to keep it from being found by future students ;)

Currently implemented functionality:

STANDARD USERS:
~~~~~
- Purchase game from store
- Leave review on game (played for over X hours)
- Take survey in order to gain money
- Add playtime to games
- Request refund on game (played for less than X hours)
- View and modify library
- Request to greenlight games. Absurdly long and goofy ToS included (created with thanks to GPT3).
~~~~~

ADMIN USERS:
~~~~~
- Purchase game from store
- Leave review on game
- Remove a game from store (long press listing in Store Activity for dialog)
- Delete user reviews on game (long press review in GameCard Activity).
- Approve or deny greenlight requests. Short press to open approval dialog
- Approve or deny refund requests. Short press to open approval dialog
- Add, remove, or modify user attributes. Long press for user modification dialog
~~~~~

LIBRARY
~~~~~
- Reviews are left in a quantity of stars. Multiple reviews are averaged into one star value. Reviews are listed with rating and text explanation
- Games that are greenlit by admins will appear in library. All money made by purchase of a game goes to the user who published it.
- Games can be searched via RecyclerView or by searchbox, which indexes by game title and publisher
~~~~~

KNOWN ISSUES
~~~~~
- Dark mode is messed up on most Android devices
~~~~~
