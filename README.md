<p align="center">
  <img src="https://img.shields.io/badge/version-3.0.1-blue?style=for-the-badge" alt="Version">
  <img src="https://img.shields.io/badge/Minecraft-1.20--1.21+-green?style=for-the-badge" alt="Minecraft">
  <img src="https://img.shields.io/badge/Java-17-orange?style=for-the-badge" alt="Java">
  <img src="https://img.shields.io/badge/license-All%20Rights%20Reserved-red?style=for-the-badge" alt="License">
</p>

# Vanish by Fronsky

**Vanish** is a feature-rich, modern vanish plugin for Minecraft (Spigot/Paper) servers. It allows staff members to become completely invisible to other players, with full control over game behavior, visual indicators, and third-party integrations.

**Website:** [fronsky.nl/projects/vanish](https://fronsky.nl/projects/vanish)
**Contact:** [support@fronsky.nl](mailto:support@fronsky.nl)

## Table of Contents

- [API](#api)
- [Features](#features)
- [Requirements](#requirements)
- [Installation](#installation)
- [Building from Source](#building-from-source)
- [Commands](#commands)
- [Permissions](#permissions)
- [PlaceholderAPI Placeholders](#placeholderapi-placeholders)
- [Configuration](#configuration)
- [Version Compatibility](#version-compatibility)
- [Reporting Issues](#reporting-issues)
- [License](#license)

## API

Vanish provides a public API for third-party plugin developers. See the **[full API documentation](API.md)** for integration guides, examples, and best practices.

### Quick Start

```java
import nl.fronsky.vanish.api.VanishAPI;

// Check if a player is vanished
boolean vanished = VanishAPI.isVanished(player);

// Toggle vanish
VanishAPI.toggleVanish(player);

// Get vanished player count
int count = VanishAPI.getVanishedPlayerCount();
```

> **Note:** The API is covered by a dedicated exception in the [LICENSE](LICENSE). Third-party plugins may freely call the public API at runtime, provided the Vanish plugin itself is not modified, redistributed, or bundled.

## Features

- **Complete Invisibility** — Vanish yourself or other players with a single command.
- **Silent Join/Quit** — Optionally fake join and quit messages for vanished players.
- **Boss Bar Indicator** — Visual reminder that you are vanished, with customizable color.
- **Configurable Disabled Actions** — Block damage, hunger, mob targeting, death messages, pressure plates, container sounds, and more for vanished players.
- **Silent Containers** — Open chests, ender chests, shulker boxes, and barrels without any sound or animation visible to other players.
- **Player Head GUI** — See and manage online vanished players via an interactive GUI.
- **Per-Player Settings** — Toggle sound effects and silent join individually.
- **Fake Join/Quit Messages** — Simulate join and quit messages while vanished.
- **Teleport** — Teleport to any player while vanished.
- **Color Customization** — Change the plugin accent color (Boss Bar, messages).
- **Debug Mode** — Extra logging for troubleshooting.
- **Public API** — For other plugins to check, toggle, and interact with vanish state.
- **Build Metadata** — `/vanish info` shows a unique build ID generated at build time.

### Optional Integrations

| Integration        | Feature                                                                   |
|--------------------|---------------------------------------------------------------------------|
| **ProtocolLib**    | Packet-level hiding, accurate server list player count, silent containers |
| **PlaceholderAPI** | `%vanish_...%` placeholders for use in scoreboards, tab lists, etc.       |
| **Dynmap**         | Hide vanished players from the Dynmap web map                             |

## Requirements

### Runtime

| Requirement                     | Version   |
|---------------------------------|-----------|
| Minecraft Server (Spigot/Paper) | **1.20+** |
| Java                            | **17**    |

### Optional Plugins

| Plugin                                                                    | Version | Purpose                             |
|---------------------------------------------------------------------------|---------|-------------------------------------|
| [ProtocolLib](https://www.spigotmc.org/resources/protocollib.1997/)       | 5.4.0   | Packet-level features (recommended) |
| [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) | 2.12.2  | Placeholder support                 |
| [Dynmap](https://www.spigotmc.org/resources/dynmap.274/)                  | Any     | Web map hiding                      |

## Installation

1. Download the latest release from [fronsky.nl](https://fronsky.nl/projects/vanish) or build from source.
2. Place the `.jar` file in your server's `plugins/` directory.
3. *(Optional)* Install ProtocolLib, PlaceholderAPI, and/or Dynmap for extra features.
4. Restart the server.
5. Configure settings in `plugins/Vanish/config.yml` and `plugins/Vanish/messages.yml`.

## Building from Source

This project uses the [Gradle Wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html), so no global Gradle installation is required.

```bash
# Clone the repository
git clone https://github.com/fronsky-development/vanish.git
cd vanish

# Build the plugin
./gradlew clean build
```

The output `.jar` file will be at:

```
build/libs/Vanish-3.0.1.jar
```

> **Note:** Each build generates a `vanish-build.properties` resource embedded in the jar. The `/vanish info` command reads this to display a build ID that is unique per build and stable at runtime.

## Commands

| Command                | Description                                | Permission                       |
|------------------------|--------------------------------------------|----------------------------------|
| `/vanish` (alias `/v`) | Toggle vanish for yourself                 | `vanish.cmd.vanish`              |
| `/vanish <player>`     | Toggle vanish for another player           | `vanish.cmd.vanish.others`       |
| `/vanish help`         | Show help overview                         | `vanish.cmd.vanish.help`         |
| `/vanish info`         | Show plugin info and build metadata        | `vanish.cmd.vanish.info`         |
| `/vanish reload`       | Reload all configuration files             | `vanish.cmd.vanish.reload`       |
| `/vanish list`         | List currently vanished players            | `vanish.cmd.vanish.list`         |
| `/vanish gui`          | Open the vanish player GUI                 | `vanish.cmd.vanish.gui`          |
| `/vanish tp <player>`  | Teleport to a player                       | `vanish.cmd.vanish.tp`           |
| `/vanish sound`        | Toggle vanish sound effect                 | `vanish.cmd.vanish.sound`        |
| `/vanish silent`       | Toggle silent join mode                    | `vanish.cmd.vanish.silent`       |
| `/vanish join`         | Send a fake join message                   | `vanish.cmd.vanish.join`         |
| `/vanish quit`         | Send a fake quit message                   | `vanish.cmd.vanish.quit`         |
| `/vanish color`        | Change the plugin accent color             | `vanish.cmd.vanish.color`        |
| `/vanish permissions`  | Show permission overview                   | `vanish.cmd.vanish.permissions`  |
| `/vanish placeholders` | Show available PlaceholderAPI placeholders | `vanish.cmd.vanish.placeholders` |

## Permissions

All permissions default to **OP only**.

| Permission                       | Description                        |
|----------------------------------|------------------------------------|
| `vanish.*`                       | Full access to all Vanish features |
| `vanish.cmd.vanish`              | Use `/vanish` command              |
| `vanish.cmd.vanish.others`       | Toggle vanish for other players    |
| `vanish.cmd.vanish.gui`          | Open vanish GUI                    |
| `vanish.cmd.vanish.sound`        | Configure vanish sound             |
| `vanish.cmd.vanish.silent`       | Configure silent join              |
| `vanish.cmd.vanish.list`         | List vanished players              |
| `vanish.cmd.vanish.join`         | Send a fake join message           |
| `vanish.cmd.vanish.quit`         | Send a fake quit message           |
| `vanish.cmd.vanish.tp`           | Teleport to a player               |
| `vanish.cmd.vanish.color`        | Change plugin color                |
| `vanish.cmd.vanish.reload`       | Reload plugin configuration        |
| `vanish.cmd.vanish.info`         | Show plugin info                   |
| `vanish.cmd.vanish.help`         | Show help                          |
| `vanish.cmd.vanish.permissions`  | Show permissions overview          |
| `vanish.cmd.vanish.placeholders` | Show placeholders overview         |
| `vanish.see`                     | See vanished players               |
| `vanish.join`                    | Auto-vanish on join                |

Use `/vanish permissions` in-game for a quick reference.

## PlaceholderAPI Placeholders

> Requires [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/)

| Placeholder             | Description                                               |
|-------------------------|-----------------------------------------------------------|
| `%vanish_isvanished%`   | Whether the player is currently vanished (`true`/`false`) |
| `%vanish_status%`       | Vanish status text                                        |
| `%vanish_count%`        | Number of currently vanished players                      |
| `%vanish_cansee%`       | Whether the player can see vanished players               |
| `%vanish_total_online%` | Total online players minus vanished                       |
| `%vanish_color%`        | Current plugin accent color                               |
| `%vanish_silent%`       | Whether the player has silent join enabled                |

Use `/vanish placeholders` in-game to see the full list.

## Configuration

### `config.yml`

```yaml
debug-mode: false

sound-enable: true
sound: 'AMBIENT_CAVE'

disabled-actions:
  damage: true
  hunger: true
  mob-target: true
  silent-chest: true
  silent-ender-chest: true
  pressure-plates: true
  death-messages: true
  player-push: true
  pickup-items: true

plugin-color: 'BLUE'
```

### `messages.yml`

All player-facing messages can be customized. Keys match the internal `Language` enum names in lowercase (e.g., `you_vanished`, `fake_join`, `fake_quit`).

### `players.yml`

Per-player settings are stored automatically (e.g., silent mode preference). Do not edit manually unless you know what you are doing.

## Version Compatibility

The plugin is compiled against **Spigot API 1.20.x** and maintains compatibility with **1.21+** by:

- Avoiding NMS and CraftBukkit usage
- Using stable, non-deprecated Spigot APIs
- Relying on ProtocolLib for version-sensitive packet behavior

## Reporting Issues

Found a bug or have a suggestion? Please open an issue on [GitHub Issues](https://github.com/fronsky-development/vanish/issues) with:

1. **Minecraft version** and **server software** (Spigot/Paper)
2. **Plugin version** (run `/vanish info`)
3. **Steps to reproduce** the issue
4. **Console errors** (if any)

## License

This project is proprietary software. All rights reserved.

```
Copyright © 2025-2026 Fronsky. All Rights Reserved.
```

No part of this software may be copied, modified, distributed, or used without
prior written permission from the copyright holder. Use of this software for
training machine learning or AI models, and automated scraping or data mining of
this repository, are strictly prohibited. See the [LICENSE](LICENSE) file for
full details.

<p align="center">
  Made with ❤️ by <a href="https://fronsky.nl">Fronsky</a>
</p>
