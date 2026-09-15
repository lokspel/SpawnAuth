# SpawnAuth

Authentication limbo and location restoration plugin for AuthMe, nLogin, OpeNLogin, and LoginSecurity

## » About

SpawnAuth keeps unauthenticated players away from the main world until they successfully log in or register. It saves their original location before moving them to a configured waiting spot and restores that location after authentication.

SpawnAuth is designed to replace the built-in spawn/limbo handling of authentication plugins, avoiding issues such as players getting stuck at spawn, spawning in the wrong location after death, or other authentication-related edge cases. Disable the corresponding spawn/limbo features in your authentication plugin when using SpawnAuth.

## » Dependencies

* **AuthMe** — optional
* **nLogin** — optional
* **OpeNLogin** — optional
* **LoginSecurity** — optional

At least one supported authentication plugin is required.

## » Features

* Supports **AuthMe**, **nLogin**, **OpeNLogin**, and **LoginSecurity**
* Saves and restores the player's original location
* Separate waiting spots for **login** and **registration**
* `vanilla`, `fixed`, and `disabled` waiting modes
* Works across overworld, nether, and end
* Optional automatic void world creation
* SQLite, MySQL, or in-memory storage
* Optional database cache
* Automatic in-memory fallback when the database is unavailable

## » Waiting Modes

### `vanilla`

Keeps the player near the server spawn in the overworld.

### `fixed`

Moves the player to a configured fixed location, typically in a dedicated void world.

### `disabled`

Does not move the player while they are unauthenticated.

After successful authentication, the player is always returned to the location they had before being moved.

## » Storage

SpawnAuth supports three storage modes:

* **SQLite** — local persistent storage
* **MySQL** — external persistent storage
* **Memory** — temporary in-memory storage only

The in-memory cache can also be enabled alongside a database. If the database becomes unavailable, SpawnAuth can fall back to the cache when `database.cache: true`; otherwise, persistence is disabled.

## » Notes

* The same configured world is used for all dimensions
* There is no separate configuration for overworld, nether, or end
* Disable the authentication plugin's built-in spawn/limbo handling when using SpawnAuth

Enjoy SpawnAuth!
