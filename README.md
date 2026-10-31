# Better Librarians

[![License](https://img.shields.io/github/license/MyCelium-GG/MyCel-Lib)](LICENSE)

Look at a librarian, see their books and prices on a small card, with no
trading screen. Search every librarian you have traded with to find who sells
Mending, cheapest first, and make them glow. Rename villagers with colorful
names floating above their heads. Client side.

Built on [Mycel](https://github.com/MyCelium-GG/MyCel-Lib) (`my.celium.org`), the shared
core for the MyCelium ecosystem: configuration, platform abstraction and
diagnostics are reused.

| | |
|---|---|
| Mod id | `betterlibrarians` |
| Minecraft | **26.3**, **26.2**, **26.1** (26.1-26.1.2) · Java **25** |
| Loaders | Fabric, Forge, NeoForge |

## Behavior

- Point your crosshair at a librarian: a card shows their profession and
  level (e.g. `Librarian · Apprentice`) plus each enchanted book they sell
  (e.g. `Sharpness V`) and its emerald price when you last traded. Other
  professions show no card.
- Max-level books render in gold, lower levels in white.
- The client only learns a villager's trades when their trading screen is
  open, so each villager is catalogued the first time you trade with them.
- Catalogues and renames persist in `config/betterlibrarians_data.json`.

## The librarian list

Press **K** (rebindable under Controls, Gameplay) or run `/librarians`. Every
librarian you have traded with in this world or server is listed, nearest
first, with distance and direction. Type a book to see who sells it, cheapest
first. **Find** makes a nearby librarian glow for 30 seconds, and **Forget**
removes one from the list. Librarians catalogued before this version show their
price and place after you next trade with them.

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
(the version is `mycel_version` in `gradle.properties`). No local setup needed.
