# Vanish API

> Developer guide for integrating with the Vanish plugin.

## Table of Contents

- [Getting Started](#getting-started)
- [API Reference](#api-reference)
- [Usage Examples](#usage-examples)
- [Full Integration Example](#full-integration-example)
- [Best Practices](#best-practices)
- [FAQ](#faq)

## Getting Started

### Add as Dependency

Add Vanish as a **compile-only** dependency. Do not shade or bundle it.

**Gradle:**

```groovy
dependencies {
    compileOnly files('libs/Vanish-<version>.jar')
}
```

**Maven:**

```xml
<dependency>
    <groupId>nl.fronsky</groupId>
    <artifactId>vanish</artifactId>
    <version><!-- version --></version>
    <scope>provided</scope>
    <systemPath>${project.basedir}/libs/Vanish-<version>.jar</systemPath>
</dependency>
```

### plugin.yml

Add Vanish as a soft dependency so your plugin loads after it (but still works without it):

```yaml
softdepend: [ 'Vanish' ]
```

Use `depend: [ 'Vanish' ]` instead if your plugin requires Vanish to function.

### Check Availability

```java
import nl.fronsky.vanish.api.VanishAPI;

@Override
public void onEnable() {
    if (VanishAPI.isAvailable()) {
        getLogger().info("Vanish integration enabled!");
    }
}
```

## API Reference

**Package:** `nl.fronsky.vanish.api.VanishAPI`

All methods are `public static`.

| Method | Returns | Description |
|--------|---------|-------------|
| `isAvailable()` | `boolean` | Whether the Vanish plugin is loaded and ready |
| `isVanished(Player)` | `boolean` | Check if a player is vanished |
| `isVanished(UUID)` | `boolean` | Check if a player is vanished by UUID |
| `vanishPlayer(Player)` | `boolean` | Vanish a player (`false` if already vanished) |
| `unvanishPlayer(Player)` | `boolean` | Unvanish a player (`false` if not vanished) |
| `toggleVanish(Player)` | `boolean` | Toggle vanish; returns `true` if now vanished |
| `getVanishedPlayers()` | `Collection<VanishPlayer>` | All currently vanished players |
| `getVanishedPlayerUUIDs()` | `Collection<UUID>` | UUIDs of all vanished players |
| `getVanishedPlayerCount()` | `int` | Number of vanished players |
| `canSeeVanished(Player)` | `boolean` | Whether the player has `vanish.see` permission |

## Usage Examples

### Check Vanish State

```java
// By player
boolean vanished = VanishAPI.isVanished(player);

// By UUID (useful when Player object is unavailable)
boolean vanished = VanishAPI.isVanished(uuid);
```

### Vanish, Unvanish, or Toggle

```java
VanishAPI.vanishPlayer(player);    // returns false if already vanished
VanishAPI.unvanishPlayer(player);  // returns false if not vanished

boolean nowVanished = VanishAPI.toggleVanish(player);
```

### Get Vanished Players

```java
Collection<VanishPlayer> players = VanishAPI.getVanishedPlayers();
Collection<UUID> uuids = VanishAPI.getVanishedPlayerUUIDs();
int count = VanishAPI.getVanishedPlayerCount();
```

### Filter Vanished Players from Visibility

```java
public List<Player> getVisiblePlayers(Player viewer) {
    boolean canSee = VanishAPI.isAvailable() && VanishAPI.canSeeVanished(viewer);

    return Bukkit.getOnlinePlayers().stream()
            .filter(p -> canSee || !VanishAPI.isAvailable() || !VanishAPI.isVanished(p))
            .collect(Collectors.toList());
}
```

## Full Integration Example

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
        vanishEnabled = VanishAPI.isAvailable();
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        int visible = getVisiblePlayerCount();
        event.getPlayer().sendMessage("Welcome! There are " + visible + " players online.");
    }

    public int getVisiblePlayerCount() {
        int total = Bukkit.getOnlinePlayers().size();
        if (vanishEnabled && VanishAPI.isAvailable()) {
            total -= VanishAPI.getVanishedPlayerCount();
        }
        return Math.max(0, total);
    }

    public boolean isVisibleTo(Player viewer, Player target) {
        if (!vanishEnabled || !VanishAPI.isAvailable()) return true;
        if (!VanishAPI.isVanished(target)) return true;
        return VanishAPI.canSeeVanished(viewer);
    }
}
```

## Best Practices

1. **Always check `isAvailable()` first** - Vanish may not be installed. Guard all API calls.
2. **Use `softdepend`** - Unless your plugin requires Vanish, keep it optional.
3. **Never shade the jar** - Use `compileOnly` / `provided` scope only.
4. **Don't store `VanishPlayer` references** - They become stale on disconnect. Always use fresh API calls.
5. **Call mutating methods on the main thread** - `vanishPlayer()`, `unvanishPlayer()`, and `toggleVanish()` must be called from the main server thread. Read operations are thread-safe.

## FAQ

**Does `vanishPlayer()` work the same as `/vanish`?**
Yes. It uses the same internal logic - the player becomes invisible, gets the Boss Bar indicator, and has disabled actions applied.

**Can I cancel a vanish event?**
Not yet. Cancellable events (`PreVanishEvent`, `PreUnvanishEvent`) are planned for a future release.

**What happens if Vanish isn't loaded?**
`isAvailable()` returns `false`. All other methods return safe defaults (`false`, `0`, empty collections). No exceptions are thrown.

**Where do I report API issues?**
Open an issue on [GitHub](https://github.com/fronsky-development/vanish/issues) with the `api` label.

<p align="center">
  Made with ❤️ by <a href="https://fronsky.nl">Fronsky</a>
</p>
