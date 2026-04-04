# 📡 Vanish API Documentation

> Developer guide for integrating with the Vanish plugin by Fronsky.

---

## Table of Contents

- [Overview](#overview)
- [Getting Started](#getting-started)
  - [Adding Vanish as a Dependency](#adding-vanish-as-a-dependency)
  - [plugin.yml Setup](#pluginyml-setup)
  - [Checking if Vanish is Available](#checking-if-vanish-is-available)
- [API Reference](#api-reference)
  - [VanishAPI](#vanishapi)
- [Usage Examples](#usage-examples)
  - [Check if a Player is Vanished](#check-if-a-player-is-vanished)
  - [Vanish / Unvanish a Player](#vanish--unvanish-a-player)
  - [Toggle Vanish](#toggle-vanish)
  - [Get All Vanished Players](#get-all-vanished-players)
  - [Check if a Player Can See Vanished Players](#check-if-a-player-can-see-vanished-players)
  - [Filter Vanished Players from Visibility](#filter-vanished-players-from-visibility)
- [Full Integration Example](#full-integration-example)
- [Best Practices](#best-practices)
- [FAQ](#faq)

---

## Overview

The **Vanish API** (`VanishAPI`) provides a simple, static API for third-party plugins to:

- ✅ Check if a player is vanished
- ✅ Programmatically vanish or unvanish players
- ✅ Toggle vanish state
- ✅ Get a list/count of all vanished players
- ✅ Check if a player has permission to see vanished players
- ✅ Verify that the Vanish plugin is loaded and available

All methods are **static** and can be called from anywhere without needing an instance.

---

## Getting Started

### Adding Vanish as a Dependency

#### Gradle (build.gradle)

Add the Vanish jar as a `compileOnly` dependency. You can use a local file reference or a flat directory.

**Option 1, local jar (flat directory):**

```groovy
repositories {
    flatDir {
        dirs 'libs'  // Place Vanish-3.0.1.jar in a 'libs' folder
    }
}

dependencies {
    compileOnly files('libs/Vanish-3.0.1.jar')
}
```

**Option 2, direct file reference:**

```groovy
dependencies {
    compileOnly files('/path/to/Vanish-3.0.1.jar')
}
```

#### Maven (pom.xml)

```xml
<dependency>
    <groupId>nl.fronsky</groupId>
    <artifactId>vanish</artifactId>
    <version>3.0.1</version>
    <scope>provided</scope>
    <systemPath>${project.basedir}/libs/Vanish-3.0.1.jar</systemPath>
</dependency>
```

> **Important:** Use `compileOnly` (Gradle) or `provided` scope (Maven). Vanish should **not** be shaded or bundled into your plugin. It must already be installed on the server.

---

### plugin.yml Setup

Add `Vanish` as a **soft dependency** in your plugin's `plugin.yml`:

```yaml
name: MyPlugin
version: 1.0.0
main: com.example.myplugin.Main
softdepend: [ 'Vanish' ]
```

Using `softdepend` ensures:
- Your plugin loads **after** Vanish (if Vanish is present).
- Your plugin still works **without** Vanish installed.

If your plugin **requires** Vanish to function, use `depend` instead:

```yaml
depend: [ 'Vanish' ]
```

---

### Checking if Vanish is Available

Always check if Vanish is loaded before calling API methods:

```java
import nl.fronsky.vanish.api.VanishAPI;

public class MyPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        if (VanishAPI.isAvailable()) {
            getLogger().info("Vanish integration enabled!");
        } else {
            getLogger().warning("Vanish not found. Vanish features disabled.");
        }
    }
}
```

Or check via Bukkit's plugin manager:

```java
boolean vanishInstalled = Bukkit.getPluginManager().getPlugin("Vanish") != null;
```

---

## API Reference

### VanishAPI

**Package:** `nl.fronsky.vanish.api`

All methods are `public static` and can be called directly on the class.

| Method                          | Return Type                | Description                                                |
|---------------------------------|----------------------------|------------------------------------------------------------|
| `isAvailable()`                 | `boolean`                  | Returns `true` if the Vanish plugin is loaded and ready    |
| `isVanished(Player player)`     | `boolean`                  | Check if a player is currently vanished                    |
| `isVanished(UUID uuid)`         | `boolean`                  | Check if a player is vanished by UUID                      |
| `vanishPlayer(Player player)`   | `boolean`                  | Vanish a player, returns `true` if successful              |
| `unvanishPlayer(Player player)` | `boolean`                  | Unvanish a player, returns `true` if successful            |
| `toggleVanish(Player player)`   | `boolean`                  | Toggle vanish, returns `true` if now vanished              |
| `getVanishedPlayers()`          | `Collection<VanishPlayer>` | Get all currently vanished players                         |
| `getVanishedPlayerUUIDs()`      | `Collection<UUID>`         | Get UUIDs of all vanished players                          |
| `getVanishedPlayerCount()`      | `int`                      | Get the number of vanished players                         |
| `canSeeVanished(Player player)` | `boolean`                  | Check if a player has permission to see vanished players   |

---

## Usage Examples

### Check if a Player is Vanished

```java
import nl.fronsky.vanish.api.VanishAPI;
import org.bukkit.entity.Player;

public boolean isPlayerHidden(Player player) {
    if (!VanishAPI.isAvailable()) {
        return false; // Vanish not installed, player is visible
    }
    return VanishAPI.isVanished(player);
}
```

You can also check by UUID (useful when the `Player` object isn't available):

```java
import java.util.UUID;

UUID playerUuid = UUID.fromString("...");
boolean vanished = VanishAPI.isVanished(playerUuid);
```

---

### Vanish / Unvanish a Player

```java
Player player = ...; // your player reference

// Vanish the player
boolean success = VanishAPI.vanishPlayer(player);
if (success) {
    getLogger().info(player.getName() + " has been vanished via API.");
}

// Unvanish the player
boolean revealed = VanishAPI.unvanishPlayer(player);
if (revealed) {
    getLogger().info(player.getName() + " is now visible via API.");
}
```

> **Note:** `vanishPlayer()` returns `false` if the player is already vanished. `unvanishPlayer()` returns `false` if the player is not vanished.

---

### Toggle Vanish

```java
boolean nowVanished = VanishAPI.toggleVanish(player);

if (nowVanished) {
    player.sendMessage("You are now invisible!");
} else {
    player.sendMessage("You are now visible!");
}
```

---

### Get All Vanished Players

```java
import nl.fronsky.vanish.module.models.VanishPlayer;
import java.util.Collection;

// Get VanishPlayer objects (includes extra methods)
Collection<VanishPlayer> vanishedPlayers = VanishAPI.getVanishedPlayers();
for (VanishPlayer vp : vanishedPlayers) {
    getLogger().info("Vanished: " + vp.getName());
}

// Or just get UUIDs
Collection<UUID> vanishedUUIDs = VanishAPI.getVanishedPlayerUUIDs();

// Or just the count
int count = VanishAPI.getVanishedPlayerCount();
getLogger().info("Currently vanished: " + count + " player(s)");
```

---

### Check if a Player Can See Vanished Players

```java
if (VanishAPI.canSeeVanished(player)) {
    // Show vanished players in your custom tab list, scoreboard, etc.
}
```

This checks for `vanish.see` or `vanish.*` permission.

---

### Filter Vanished Players from Visibility

A common use case: hiding vanished players from your plugin's player list, leaderboard, or custom GUI.

```java
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.List;
import java.util.stream.Collectors;

public List<Player> getVisiblePlayers(Player viewer) {
    boolean canSee = VanishAPI.isAvailable() && VanishAPI.canSeeVanished(viewer);

    return Bukkit.getOnlinePlayers().stream()
            .filter(p -> canSee || !VanishAPI.isAvailable() || !VanishAPI.isVanished(p))
            .collect(Collectors.toList());
}
```

---

## Full Integration Example

Below is a complete example of a plugin that integrates with Vanish:

```java
package com.example.myplugin;

import nl.fronsky.vanish.api.VanishAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class MyPlugin extends JavaPlugin implements Listener {

    private boolean vanishEnabled = false;

    @Override
    public void onEnable() {
        // Check if Vanish is available
        vanishEnabled = VanishAPI.isAvailable();

        if (vanishEnabled) {
            getLogger().info("Vanish detected! Hiding vanished players from features.");
        } else {
            getLogger().info("Vanish not found. All players will be visible.");
        }

        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Count visible players (excluding vanished)
        int visibleCount = getVisiblePlayerCount();
        player.sendMessage("Welcome! There are " + visibleCount + " players online.");
    }

    /**
     * Gets the number of online players, excluding vanished ones.
     */
    public int getVisiblePlayerCount() {
        int total = Bukkit.getOnlinePlayers().size();

        if (vanishEnabled && VanishAPI.isAvailable()) {
            total -= VanishAPI.getVanishedPlayerCount();
        }

        return Math.max(0, total);
    }

    /**
     * Checks if a target player should be visible to a viewer.
     */
    public boolean isVisibleTo(Player viewer, Player target) {
        if (!vanishEnabled || !VanishAPI.isAvailable()) {
            return true; // No vanish plugin, everyone is visible
        }

        if (!VanishAPI.isVanished(target)) {
            return true; // Target is not vanished
        }

        // Target is vanished, only show to players with vanish.see
        return VanishAPI.canSeeVanished(viewer);
    }
}
```

---

## Best Practices

### 1. Always check `isAvailable()` first

The Vanish plugin may not be installed. Always guard your API calls:

```java
if (VanishAPI.isAvailable()) {
    // Safe to call VanishAPI methods
}
```

### 2. Use `softdepend` in plugin.yml

Unless your plugin **requires** Vanish, use `softdepend` to make it optional. This way your plugin works on servers without Vanish installed.

### 3. Don't shade the Vanish jar

Add Vanish as `compileOnly` / `provided`. It is a runtime dependency provided by the server's plugin folder.

### 4. Handle null players

The API methods handle `null` gracefully (returning `false` or empty collections), but it's still good practice to null-check:

```java
Player player = Bukkit.getPlayer("Steve");
if (player != null && VanishAPI.isVanished(player)) {
    // ...
}
```

### 5. Cache the availability check

If you call `isAvailable()` frequently, cache it during `onEnable()`:

```java
private boolean vanishEnabled;

@Override
public void onEnable() {
    vanishEnabled = VanishAPI.isAvailable();
}
```

### 6. Don't store `VanishPlayer` references long-term

`VanishPlayer` objects may become stale when a player disconnects. Always get fresh references:

```java
// ✅ Good, fresh check
if (VanishAPI.isVanished(player)) { ... }

// ❌ Bad, stale reference
VanishPlayer vp = ...; // stored earlier
if (vp.getState() == State.HIDDEN) { ... } // may be outdated
```

---

## FAQ

### Q: Does calling `vanishPlayer()` trigger the same behavior as `/vanish`?

**Yes.** The API uses the same internal `VanishPlayer.hide()` method, so the player will:
- Become invisible to other players
- Get the Boss Bar indicator
- Have disabled actions applied (if configured)
- Trigger the `VisibilityChangeEvent`

### Q: Can I cancel a vanish event?

Not yet. Cancellable events (`PreVanishEvent`, `PreUnvanishEvent`) are planned for a future release.

### Q: Is the API thread-safe?

The vanished player map uses `ConcurrentHashMap` (as of v3.0.1), so read operations like `isVanished()` and `getVanishedPlayerCount()` are thread-safe. However, **mutating** operations (`vanishPlayer`, `unvanishPlayer`, `toggleVanish`) should always be called from the **main server thread**.

### Q: What happens if I call the API before Vanish is enabled?

`isAvailable()` returns `false`, and all other methods return safe defaults (`false`, `0`, empty collections). No exceptions will be thrown.

### Q: Where can I report API issues or request features?

Open an issue on [GitHub](https://github.com/fronsky-development/vanish/issues) with the `api` label.

---

<p align="center">
  Made with ❤️ by <a href="https://fronsky.nl">Fronsky</a>
</p>

