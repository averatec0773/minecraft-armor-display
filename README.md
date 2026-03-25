# Armor Display

A client-side Fabric mod for Minecraft 1.21.4 that displays your total numeric armor value above the armor bar.

Useful on any server where armor values differ from vanilla — including RPG servers, custom gear servers, or any server that modifies armor through plugins or datapacks.

## Features

- Displays total armor value above the HUD armor bar
- When armor exceeds the vanilla cap of 30, shows `30 (Actual: X)` to reflect both effective and real values
- Reads server-synced armor attributes (e.g. bonus armor from plugins or gear systems)
- Dynamically adjusts position for multi-row health bars and absorption hearts
- Configurable text color (White / Yellow / Cyan / Green)
- Toggle display on/off in-game
- Rebindable config menu key (default: `N`)

## Requirements

- Minecraft 1.21.4
- Fabric Loader ≥ 0.16.0
- Fabric API

## Usage

Press `N` (default) to open the settings menu.

## Building

```bash
./gradlew build
```

Output jar is in `build/libs/`.

## License

MIT — by averatec0773
