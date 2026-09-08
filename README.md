SpawnAuth works with AuthMe, nLogin, OpeNLogin, or LoginSecurity to keep unauthenticated players away from the main world until they log in. It saves the player's original location before moving them to a waiting spot and restores it after successful authentication.

The plugin is designed to replace the built-in spawn/limbo handling of these plugins, which can cause issues such as players getting stuck at spawn, spawning in the wrong place after death, or other authentication-related edge cases. It is recommended to disable those features when using SpawnAuth.

## Features

- Supports AuthMe, nLogin, OpeNLogin, and LoginSecurity.
- Saves and restores the player's original location after login or registration.
- Separate waiting spot for **login** (returning players) and **register** (new players):
  - `vanilla` – the player stays near the server spawn of the overworld.
  - `fixed` – the player is moved to a fixed point, usually in a dedicated void world.
  - `disabled` – the player is not moved at all.
- After authentication the player is always returned to the location they had before being moved, regardless of the mode.
- All dimensions (overworld, nether, end) use the same configured world — no per-dimension config.
- Optional automatic creation of a void world.