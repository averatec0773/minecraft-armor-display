# Armor Display

A client-side Fabric mod for Minecraft 1.21.4 that displays your total numeric armor value above the armor bar.

Designed for RPG servers where armor commonly exceeds the vanilla 30-point cap.

## Features

- Displays total armor value above the HUD armor bar
- When armor exceeds the vanilla cap of 30, shows `30 (Actual: X)` to reflect both effective and real values
- Correctly reads set bonus armor (赋灵) synced by RPG server plugins
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
