# Contributing to Vanish

First off, thanks for taking the time to contribute! This repository is **public
and source-available**, and community input in the form of bug reports, ideas, and
pull requests is welcome.

> **Important — License notice**
> Vanish is **proprietary software** (see [LICENSE](LICENSE)). The source is public
> so you can read it, report issues, and propose changes, but it is **not** released
> under an open-source license. By contributing you agree to the
> [Contributor terms](#contributor-terms) below.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Ways to Contribute](#ways-to-contribute)
- [Reporting Bugs](#reporting-bugs)
- [Suggesting Features](#suggesting-features)
- [Development Setup](#development-setup)
- [Project Structure](#project-structure)
- [Coding Guidelines](#coding-guidelines)
- [Commit Messages](#commit-messages)
- [Pull Request Process](#pull-request-process)
- [Contributor Terms](#contributor-terms)

## Code of Conduct

This project and everyone participating in it is governed by our
[Code of Conduct](CODE_OF_CONDUCT.md). By participating, you are expected to uphold
this code. Please report unacceptable behavior to **support@fronsky.nl**.

## Ways to Contribute

- **Report bugs** using the [issue tracker](https://github.com/fronsky-development/vanish/issues).
- **Suggest features** or improvements.
- **Improve documentation** (README, API docs, wiki, code comments).
- **Submit pull requests** for open issues or approved feature requests.

If you plan to work on something non-trivial, please open an issue first so we can
discuss the approach before you invest time in it.

## Reporting Bugs

Before opening a new issue, please:

1. **Search existing issues** to avoid duplicates.
2. Make sure you are on the **latest version** (`/vanish info`).
3. Reproduce the bug on a clean server if possible.

When you open a [bug report](https://github.com/fronsky-development/vanish/issues/new/choose),
include:

- Minecraft version and server software (Spigot/Paper).
- Plugin version (`/vanish info`).
- Versions of relevant integrations (ProtocolLib, PlaceholderAPI, Dynmap).
- Clear steps to reproduce.
- Expected vs. actual behavior.
- Full console errors / stack traces (use a paste service for long logs).

## Suggesting Features

Open a [feature request](https://github.com/fronsky-development/vanish/issues/new/choose)
and describe:

- The problem your idea solves.
- How you imagine it working.
- Any alternatives you considered.

## Development Setup

**Requirements**

- **Java 17** or newer (JDK).
- **Git**.
- No Gradle install needed — the project ships with the [Gradle Wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html).

**Clone and build**

```bash
git clone https://github.com/fronsky-development/vanish.git
cd vanish
./gradlew clean build
```

On Windows use `gradlew.bat` instead of `./gradlew`.

The output jar is written to `build/libs/Vanish-<version>.jar`. Drop it into a test
server's `plugins/` folder to try your changes.

**Run tests**

```bash
./gradlew test
```

## Branching Strategy

This project uses a two-branch workflow:

- **`main`** — stable, released code. **Protected.** Only maintainers merge into it,
  and only from `dev`. Do **not** open contributor pull requests against `main`.
- **`dev`** — active development. **All contributions target this branch.**

As a contributor:

1. Make sure your fork's `dev` branch is up to date with `upstream/dev`.
2. Create your feature/fix branch **from `dev`**.
3. Open your pull request **against `dev`** (never `main`).

Maintainers periodically promote `dev` into `main` for releases.

## Project Structure

```
src/main/java/nl/fronsky/vanish/
  Main.java              # Plugin entry point
  api/                   # Public API (VanishAPI) — treat as stable, avoid breaking changes
  integrations/          # PlaceholderAPI and other integrations
  logic/                 # Framework: commands, files, logging, modules, tasks, utils
  module/                # Vanish feature module: commands, events, models, subcommands
src/main/resources/      # config.yml, messages.yml, players.yml, plugin.yml
```

The `logic/` package is a reusable framework; the `module/` package contains the
actual vanish behavior. Keep new vanish features inside `module/`.

## Coding Guidelines

- Target **Java 17**; do not use APIs newer than that.
- Match the existing code style. An [`.editorconfig`](.editorconfig) is provided —
  please enable EditorConfig support in your IDE.
- Avoid direct **NMS / CraftBukkit** internals. Use the Bukkit API or ProtocolLib so
  the plugin stays compatible across Minecraft versions.
- Treat integrations (ProtocolLib, PlaceholderAPI, Dynmap) as **optional** — guard
  their usage so the plugin still works when they are absent.
- Do **not** hard-code user-facing text. Add new strings to `messages.yml` and load
  them through the existing message system.
- Keep the **public API** (`nl.fronsky.vanish.api.VanishAPI`) backward compatible.
  Discuss any breaking API change in an issue first.
- Write or update tests where it makes sense.

## Commit Messages

Use clear, imperative commit messages. [Conventional Commits](https://www.conventionalcommits.org/)
is preferred but not required:

```
feat: add silent barrel support
fix: prevent NPE when ProtocolLib is missing
docs: clarify permission table in README
```

## Pull Request Process

1. **Fork** the repository and create a branch **from `dev`**
   (e.g. `feat/silent-barrels` or `fix/join-message`).
2. Make your changes in focused, logically grouped commits.
3. Ensure `./gradlew clean build` **and** `./gradlew test` pass locally.
4. Update documentation (README, API.md) when behavior changes.
5. Open a pull request **against `dev`** (not `main`) and fill in the PR template.
6. Link the related issue (e.g. `Closes #123`).
7. Be responsive to review feedback — maintainers may request changes.

> Pull requests that target `main` will be closed or asked to re-target `dev`.
> The `dev` → `main` promotion is handled by maintainers.

A maintainer will review your PR. Please note that, given the proprietary license,
maintainers make the final call on what is merged.

## Contributor Terms

Because Vanish is proprietary, contributions require clear licensing:

- You confirm that your contribution is **your own original work** and that you have
  the right to submit it.
- You grant Fronsky a **perpetual, worldwide, royalty-free license** to use, modify,
  sublicense, and distribute your contribution as part of Vanish, under the terms of
  the project [LICENSE](LICENSE).
- You understand the project is **not** open source and your contribution will be
  distributed under the proprietary Vanish license.

If you cannot agree to these terms, please do not submit a pull request.

---

Thanks again for helping improve Vanish! ❤️
