# Changelog

All notable changes to Better Librarians are documented here.
The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).
Better Librarians is versioned semantically (`MAJOR.MINOR.PATCH`).
It runs on Minecraft 26.3 (Fabric + Forge + NeoForge, Java 25) and requires
[Mycel](https://github.com/MyCelium-GG/MyCel-Lib) (`my.celium.org`).

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
