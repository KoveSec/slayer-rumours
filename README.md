# Slayer Rumours

RuneLite Plugin Hub plugin that adds **Hunter Rumours-style preferred locations** for Old School RuneScape Slayer tasks. It detects your current assignment, remembers where you like to kill each task, and can optionally ask peer Hub plugins to path there or open an Inventory Setup.

This does **not** replace the built-in Slayer plugin. Core Slayer still owns the task counter, infobox, and its own highlights. Slayer Rumours sits beside it.

## Features (MVP)

- Detects the active Slayer task from **game state** (Slayer varps + cache DB tables) and **chat** (assignment / completion lines).
- Bundled `tasks.json` catalog of ~30 common tasks with aliases, NPC ids/name patterns, and one or more locations.
- Sidebar **Preferred locations** panel:
  - search tasks and locations
  - per-task location `JComboBox`
  - per-task **Inventory Setup name** editor (primary setup-mapping UX)
  - validates setup names via Inventory Setups `get-setups` when that plugin is present
- Optional **Shortest Path** via `PluginMessage` only (`shortestpath` / `path` / `clear`, `WorldPoint` target). Only clears paths this plugin set.
- World map pin on the preferred location while a task is active.
- On-task NPC highlight through core `NpcOverlayService.registerHighlighter` (no Better NPC Highlight, no reflection).
- Optional **Inventory Setups** via `PluginMessage` only (`inventory-setups` / `view` / `get-setups` / `clear`).
- Task strip overlay: name, remaining, location, plus **Nav** and **Setup** overlay actions.
- Soft-fails if Shortest Path or Inventory Setups is missing or disabled.

## Peer plugins

| Plugin | Namespace | Messages | Required? |
| --- | --- | --- | --- |
| [Shortest Path](https://github.com/Skretzo/shortest-path) | `shortestpath` | `path` (`start`, `target`), `clear` | No |
| [Inventory Setups](https://github.com/dillydill123/inventory-setups) | `inventory-setups` | `get-setups`, `view`, `clear`, listens for `setups-changed` | No |

Cross-plugin traffic uses `PluginMessage` with JDK-visible types only (`String`, `WorldPoint`, `Collection<String>`). There is no `@PluginDependency` on Hub peers, no reflection, and no JNI.

## Config

Group: `slayer-rumours`

| Key | Default | Purpose |
| --- | --- | --- |
| `showTaskStrip` | true | Overlay with task / remaining / location |
| `showSidebar` | true | Preferred locations + setup mapping panel |
| `showWorldMapPin` | true | Pin while a task is active |
| `useShortestPath` | **true** | Allow Nav / owned path updates |
| `autoPathOnAssign` | **false** | Request a path when a new task is assigned |
| `autoOpenSetupOnAssign` | **false** | Open the mapped Inventory Setup on assign |
| highlight options | outline on | Hull / tile / outline + color |

Preferred location keys: `pref.loc.<taskId>`  
Inventory Setup map: `setup.map` (JSON object of task id → setup name)

## Hub-safe scope

This plugin follows [example-plugin `AGENTS.md`](https://github.com/runelite/example-plugin/blob/master/AGENTS.md) and Plugin Hub rules:

- Java 11, `build=standard`
- No reflection, JNI, Kotlin, dynamic classloading, or input injection
- No combat prediction, prayer indicators, projectile landing, or auto stand-here
- Clean `shutDown()`: overlay removed, highlighter unregistered, owned Shortest Path cleared, world map pin removed

## Location coordinates

The bundled `src/main/resources/com/slayerrumeurs/tasks.json` coordinates are **wiki-approximate**. They need **in-game verification** (stand on the tile, confirm x/y/plane) before a Plugin Hub submission. Treat them as a starting catalog, not a finished nav dataset.

## Future (not in this MVP)

V2 may add kill / loot / supplies metrics. That work is intentionally out of scope here.

## Development

```bash
./gradlew test
./gradlew run
```

`./gradlew run` starts a developer RuneLite client with this plugin loaded. Follow [Using Jagex Accounts](https://github.com/runelite/runelite/wiki/Using-Jagex-Accounts) to log in.

### What to test in-game

1. Get a catalogued Slayer task (or check a slayer gem) and confirm the task strip shows name, remaining, and preferred location.
2. Change the location in the sidebar and confirm the world map pin moves.
3. With Shortest Path installed, use overlay **Nav** (and, if enabled, auto-path on a new assign). Disable Shortest Path and confirm Nav soft-fails.
4. Map an Inventory Setup name in the sidebar; confirm validation when Inventory Setups is installed; use overlay **Setup**.
5. Finish or cancel the task and confirm the pin, highlight, and any owned path are cleared.
6. Disable the plugin and confirm overlays / highlights / owned paths are gone.

## License

BSD 2-Clause. See `LICENSE`.
