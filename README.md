SpawnAuth works with AuthMe, nLogin, or OpenLogin to keep unauthenticated players away from the main world until they log in. It saves the player's original location before teleporting them to a temporary spawn and restores it after successful authentication.

The plugin is designed to replace the built-in spawn/limbo handling of these plugins, which can cause issues such as players getting stuck at spawn, spawning in the wrong place after death, or other authentication-related edge cases. It is recommended to disable those features when using SpawnAuth.

## Features

- Supports AuthMe, nLogin, and OpenLogin.
- Saves and restores the player's original location after login.
- Two spawn modes:
  - `vanilla` – teleports players to the configured world and scatters them around its spawn using the `respawn_radius` gamerule.
  - `fixed` – teleports players to a fixed location, usually in a dedicated void world.
- All dimensions (overworld, nether, end) use the same configured world — no per-dimension config.
- Optional automatic creation of a void world when using `fixed` mode.
