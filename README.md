# Better Librarians

[![License](https://img.shields.io/github/license/MyCelium-GG/MyCel-Lib)](LICENSE)

Look at a librarian, see their books. A small title card lists every
enchanted book a villager trades — no trading GUI, no hovering required.
Rename villagers with colorful names floating above their heads. Client side,
no keybinds.

Built on [Mycel](https://github.com/MyCelium-GG/MyCel-Lib) (`my.celium.org`), the shared
core for the MyCelium ecosystem: configuration, platform abstraction and
diagnostics are reused.

| | |
|---|---|
| Mod id | `betterlibrarians` |
| Minecraft | **26.3** · Java **25** |
| Loaders | Fabric, Forge, NeoForge |

## Behavior

- Point your crosshair at a librarian: a centered title card shows their
  profession and level (e.g. `Librarian · Apprentice`) plus each enchanted
  book they sell (e.g. `Sharpness V`). Other professions show no card.
- Max-level books render in gold, lower levels in white.
- The client only learns a villager's trades when their trading screen is
  open, so each villager is catalogued the first time you trade with them.
- Catalogues and renames persist in `config/betterlibrarians_data.json`.

## Renaming (`/vname`)

Look at a villager and type:

- `/vname &aBob the &lBrave` — sets a floating name above their head
  (also shown on the title card). Removed with `/vname clear`.
- Full legacy codes: `&0`–`&9`, `&a`–`&f` colors, `&l` bold, `&o` italic,
  `&n` underline, `&m` strikethrough, `&k` obfuscated, `&r` reset,
  `&&` for a literal `&`.
- True RGB, any of `&#RRGGBB`, `{#RRGGBB}` or `&x&R&R&G&G&B&B`,
  e.g. `/vname &#ff55aaBobby`. Codes with no visible text are rejected.

Names render in-world above the villager (hidden through walls, fading past
48 blocks), client-side only — other players need the mod to see them too.

## Configuration (`config/betterlibrarians.json`)

| Key | Default | Meaning |
|---|---|---|
| `enabled` | `true` | Master switch (also toggleable live via `Mycel.setEnabled`). |
| `showLevel` | `true` | Append the level (`Sharpness V` vs `Sharpness`). |
| `highlightMaxLevel` | `true` | Gold for max-level books, white otherwise. |
| `maxBooks` | `6` | Maximum books listed on the card (1-12). |
| `showUncataloguedHint` | `true` | Hint for villagers whose books are still unknown. |

Reload live with `/mycel reload betterlibrarians`. A config screen is
available from the mod list on Forge and NeoForge.

## Building

JDK 25, then:

```sh
./gradlew build            # all loaders + tests
./gradlew :common:test     # unit tests only
```

Mycel itself comes from [JitPack](https://jitpack.io/#MyCelium-GG/MyCel-Lib)
as one universal jar for all loaders
(`com.github.MyCelium-GG:MyCel-Lib:1.4.0`). No local setup needed.
