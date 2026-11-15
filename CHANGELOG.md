# Changelog

All notable changes to Better Librarians are documented here.
The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).
Better Librarians is versioned semantically (`MAJOR.MINOR.PATCH`).
It runs on Minecraft 26.3, 26.2 and 26.1 (Fabric + Forge + NeoForge, Java 25) and requires
[Mycel](https://github.com/MyCelium-GG/MyCel-Lib) (`my.celium.org`).

## [1.3.0] - 2026-11-15

### Added
- Restock timer: the look-at card and the trading screen show which trades are sold out and when the villager restocks next, for example "Restocks in 4:10", "Restocks any moment now" or "Restocked twice today, next in 12:30". Exact when the server has Better Librarians too (always in singleplayer); on other servers an estimate from the time of day and the stock you last saw.
- Warnings when a villager can't restock: it has no workstation, or it stands too far from it during work hours (a trading hall cell built away from the lectern).
- The restock timer works for every working villager (farmers, clerics, smiths and so on), not only librarians. Turn that off with `restockAllJobs`.
- The librarian list shows the selected librarian's stock.
- New settings `restockTimer` and `restockAllJobs`.

### Changed
- Book prices on the card and in the list now update when they change (demand, reputation), not only when a villager gets new trades.
- The hint for unknown librarians now says "Open its trades once to see its books": opening the trading screen is enough.
- Installed without Mycel, the game still starts: Better Librarians stays off and says in chat that Mycel is missing, with a download link.
- Optional on servers: a server with the mod sends players who have it the stock of the villagers near them, once a second. It only reads; trades are never changed.

## [1.2.2] - 2026-11-04

### Changed
- Built for Mycel 1.6.1: chat messages get the new MyCelium look, and Mycel can share anonymous stats if you agree. What is sent: https://wiki.mycelium-gg.org/privacy#mycel-stats

### Added
- Anonymous stats (how many librarians get catalogued and which features get used) so we know what to improve. They follow your answer on Mycel's stats screen; nothing else is asked. What is sent: https://wiki.mycelium-gg.org/privacy#stats-betterlibrarians

## [1.2.1] - 2026-11-02

### Changed
- Settings screen: on/off settings now show their real state when it opens (fixed in Mycel).
- Built against the newest Fabric API, Forge and NeoForge, including the first stable NeoForge for 26.3.

## [1.2.0] - 2026-10-31

### Added

- A librarian list on **K** or `/librarians`: everyone you have traded with in
  this world or server, with distance and direction. Search a book to see who
  sells it, cheapest first. Find makes a nearby one glow for 30 seconds.
- The card and the list show each book's emerald price from your last trade.

### Changed

- The card has a new look with book and emerald icons.

### Fixed

- In a trading hall, books could be saved under a different villager standing
  closer than the one you traded with. They now go to the villager you are
  looking at. Librarians affected keep the wrong books until you trade with them
  again.

## [1.1.0] - 2026-10-28

### Added

- Minecraft 26.2 and 26.1 (26.1, 26.1.1, 26.1.2) support. Each release now
  has jars for 26.3, 26.2 and 26.1 on Fabric, Forge and NeoForge; pick the
  ones named for your Minecraft.

### Changed

- Requires Mycel 1.5.1 (for the same Minecraft version).

## [1.0.5] - 2026-10-26

### Added

- An icon in the mod list on Fabric, Forge and NeoForge.

### Changed

- Requires Mycel 1.4.1.

## [1.0.4] - 2026-10-25

### Changed

- Requires Mycel 1.4.0. Operators now get one chat line per session when a
  MyCelium mod has an update, and `/mycel mods` marks it.

### Fixed

- A villager data file that could not be read was replaced by an empty one
  on the next rename, losing every name and catalogue. It is now kept as
  `betterlibrarians_data.json.broken`, and saves are atomic, so a crash
  mid-save can no longer corrupt the file.

## [1.0.3] - 2026-10-23

### Changed

- Requires Mycel 1.3.1. The look-at card uses the MyCelium wiki colors
  (red title, orange max-level books, wiki greys) instead of legacy yellow
  and grey.

## [1.0.2] - 2026-10-19

### Changed

- Updated Mycel lib to 1.3.0: Mycel's chat messages now use the MyCelium wiki's
  red style.
- Checks for new versions through the MyCelium wiki (one log line when an
  update is out; turn off with `enableUpdateChecker` in `config/mycel.json`).

## [1.0.1] - 2026-10-10

### Changed

- Updated Mycel lib to 1.2.1.

## [1.0.0] - 2026-10-10

### Added

- Initial release.
- Crosshair title card for librarians: profession and level
  (e.g. `Librarian · Apprentice`) plus each enchanted book they sell
  (e.g. `Sharpness V`). Other professions show no card.
- Max-level books render in gold, lower levels in white.
- Villagers are catalogued the first time you open their trading screen
  (the client only learns trades then); catalogues and renames persist in
  `config/betterlibrarians_data.json` across restarts.
- `/vname` client command: look at a villager and rename it with a floating
  in-world name (also shown on the title card); `/vname clear` removes it.
  Full legacy codes (`&0`–`&9`, `&a`–`&f`, `&l`, `&o`, `&n`, `&m`, `&k`,
  `&r`, `&&`) plus true RGB (`&#RRGGBB`, `{#RRGGBB}`, `&x&R&R&G&G&B&B`).
  Names hide through walls and fade past 48 blocks.
- Config (`config/betterlibrarians.json`): `enabled`, `showLevel`,
  `highlightMaxLevel`, `maxBooks` (1-12), `showUncataloguedHint`.
  Reload live with `/mycel reload betterlibrarians`; config screen available
  from the mod list on Forge and NeoForge.
- Client-side only, no keybinds. Built on Mycel 1.2.1
  (`com.github.MyCelium-GG:MyCel-Lib:1.2.1` via JitPack).
