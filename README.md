SpawnAuth works with AuthMe or nLogin to keep unauthenticated players away from the main world until they log in. It saves the player's original location before teleporting them to a temporary spawn and restores it after successful authentication.

The plugin is designed to replace the built-in spawn/limbo handling of AuthMe and nLogin, which can cause issues such as players getting stuck at spawn, spawning in the wrong place after death, or other authentication-related edge cases. It is recommended to disable those features when using SpawnAuth.

## Features

- Supports AuthMe and nLogin.
- Saves and restores the player's original location after login.
- Two spawn modes:
  - `vanilla` – teleports players to the configured world for their current dimension and uses the world's `respawnRadius` gamerule to spread players around spawn.
  - `fixed` – teleports players to a fixed location, usually in a dedicated void world.
- Supports separate worlds for the Overworld, Nether, and End.
- Optional fallback to the Overworld if a dimension is not configured.
- Optional automatic creation of a void world when using `fixed` mode.
