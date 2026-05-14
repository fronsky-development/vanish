<p align="center">
  <img src="https://img.shields.io/badge/Minecraft-1.20+-brightgreen?style=for-the-badge&logo=minecraft" alt="Minecraft 1.20+">
  <img src="https://img.shields.io/badge/Java-17+-orange?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 17+">
  <img src="https://img.shields.io/badge/Platform-Spigot%20%7C%20Paper-blue?style=for-the-badge" alt="Spigot / Paper">
  <img src="https://img.shields.io/badge/License-All%20Rights%20Reserved-red?style=for-the-badge" alt="All Rights Reserved">
</p>

<h1 align="center">Vanish</h1>

<p align="center">
  A modern, feature-rich vanish plugin for Minecraft servers.<br>
  Go completely invisible with full control over game behavior, visual indicators, and integrations.
</p>

<p align="center">
  <a href="#features">Features</a> •
  <a href="#installation">Installation</a> •
  <a href="#commands">Commands</a> •
  <a href="#configuration">Configuration</a> •
  <a href="API.md">API</a>
</p>

---

## Features

- **Complete Invisibility** - Vanish yourself or other players with `/vanish` (alias `/v`)
- **Silent Join & Quit** - Fake join/quit messages so no one knows you're online
- **Boss Bar Indicator** - Customizable visual reminder while vanished
- **Disabled Actions** - Block damage, hunger, mob targeting, death messages, item pickup, and more
- **Silent Containers** - Open chests, ender chests, shulker boxes, and barrels without sounds
- **Player Head GUI** - Browse and manage vanished players interactively
- **Per-Player Settings** - Individual sound and silent join preferences
- **Teleport** - `/vanish tp <player>` to reach anyone while invisible
- **Color Customization** - Change the plugin accent color via GUI or command
- **Public API** - Let other plugins check, toggle, and integrate with vanish state

### Integrations

| Plugin | Feature |
|--------|---------|
| [ProtocolLib](https://www.spigotmc.org/resources/protocollib.1997/) | Packet-level hiding, accurate player count, silent containers |
| [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) | `%vanish_...%` placeholders for scoreboards, tab lists, etc. |
| [Dynmap](https://www.spigotmc.org/resources/dynmap.274/) | Hide vanished players from the web map |

## Requirements

| | Minimum |
|-|---------|
| **Server** | Spigot or Paper **1.20+** |
| **Java** | **17** or newer |

ProtocolLib, PlaceholderAPI, and Dynmap are optional but recommended.

## Installation

1. Download the latest release from [CurseForge](https://www.curseforge.com/minecraft/bukkit-plugins/fronsky-vanish) or [build from source](#building-from-source).
2. Place the jar in your server's `plugins/` folder.
3. Restart the server.
4. Edit `plugins/Vanish/config.yml` and `plugins/Vanish/messages.yml` to your liking.

## Commands

All commands start with `/vanish` (alias `/v`). All permissions default to **OP**.

| Command | Description | Permission |
|---------|-------------|------------|
| `/vanish` | Toggle vanish for yourself | `vanish.cmd.vanish` |
| `/vanish <player>` | Toggle vanish for another player | `vanish.cmd.vanish.others` |
| `/vanish help` | Show help overview | `vanish.cmd.vanish.help` |
| `/vanish info` | Plugin info and build metadata | `vanish.cmd.vanish.info` |
| `/vanish reload` | Reload all config files | `vanish.cmd.vanish.reload` |
| `/vanish list` | List vanished players | `vanish.cmd.vanish.list` |
| `/vanish gui` | Open the vanish player GUI | `vanish.cmd.vanish.gui` |
| `/vanish tp <player>` | Teleport to a player | `vanish.cmd.vanish.tp` |
| `/vanish sound` | Toggle vanish sound effect | `vanish.cmd.vanish.sound` |
| `/vanish silent` | Toggle silent join mode | `vanish.cmd.vanish.silent` |
| `/vanish join` | Send a fake join message | `vanish.cmd.vanish.join` |
| `/vanish quit` | Send a fake quit message | `vanish.cmd.vanish.quit` |
| `/vanish color` | Change the plugin accent color | `vanish.cmd.vanish.color` |
| `/vanish permissions` | Show all permissions | `vanish.cmd.vanish.permissions` |
| `/vanish placeholders` | Show PlaceholderAPI placeholders | `vanish.cmd.vanish.placeholders` |

### Additional Permissions

| Permission | Description |
|------------|-------------|
| `vanish.*` | Full access to all features |
| `vanish.see` | See vanished players |
| `vanish.join` | Auto-vanish on join |

> **Tip:** Run `/vanish permissions` in-game for a full reference.

## Configuration

### config.yml

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

### messages.yml

All player-facing messages are customizable. Edit `plugins/Vanish/messages.yml` after first run.

### PlaceholderAPI

Requires [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/). Run `/vanish placeholders` in-game to see all available placeholders.

| Placeholder | Description |
|-------------|-------------|
| `%vanish_isvanished%` | Whether the player is vanished |
| `%vanish_status%` | Vanish status text |
| `%vanish_count%` | Number of vanished players |
| `%vanish_cansee%` | Can see vanished players |
| `%vanish_total_online%` | Online players minus vanished |
| `%vanish_color%` | Plugin accent color |
| `%vanish_silent%` | Silent join enabled |

## API

Vanish provides a public API for third-party plugins. See the **[full API documentation](API.md)** for details.

```java
import nl.fronsky.vanish.api.VanishAPI;

if (VanishAPI.isAvailable()) {
    boolean vanished = VanishAPI.isVanished(player);
    VanishAPI.toggleVanish(player);
    int count = VanishAPI.getVanishedPlayerCount();
}
```

> The API is covered by a dedicated exception in the [LICENSE](LICENSE). Third-party plugins may freely call the public API at runtime.

## Building from Source

Requires Git. No Gradle installation needed - the project includes the [Gradle Wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html).

```bash
git clone https://github.com/fronsky-development/vanish.git
cd vanish
./gradlew clean build
```

The output jar will be at `build/libs/Vanish-<version>.jar`.

## Compatibility

Compiled against Spigot API 1.20. Compatible with 1.21+ by avoiding NMS/CraftBukkit internals and relying on ProtocolLib for version-sensitive packet behavior.

## Issues

Found a bug? Open an issue on [GitHub](https://github.com/fronsky-development/vanish/issues) with your Minecraft version, server software, plugin version (`/vanish info`), steps to reproduce, and any console errors.

## License

Copyright &copy; 2025-2026 Fronsky. All Rights Reserved.

This is proprietary software. No part may be copied, modified, or distributed without permission. Use for AI/ML training or automated data mining is prohibited. See [LICENSE](LICENSE) for full terms.

<p align="center">
  Made with ❤️ by <a href="https://fronsky.nl">Fronsky</a>
</p>
