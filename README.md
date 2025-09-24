# RSFramework

**RSFramework** is a Bukkit plugin framework for **RSPlugin**, supporting Spigot, Paper, Folia, Velocity, Bungee, and hybrid setups.

---

## ✨ Features

* Send packets between a proxy and a server **without developing a separate plugin** for the proxy.
* Flexible **JSON-based data storage** with support for:

    * MySQL / MariaDB / MongoDB / JSON files
* Manage custom items and blocks, compatible with:

    * **ItemsAdder, Oraxen, Nexo, MMOItems, EcoItems**

---

## 🏗️ Build

Build the project using Gradle:

```bash
./gradlew build
```

---

## 🚀 Running the Server and Proxy

### 1. Reobfuscate the JAR

```bash
./gradlew reobfJar
```

### 2. Start a Proxy

| Proxy Type       | Command                                        |
| ---------------- | ---------------------------------------------- |
| Velocity         | `./gradlew runVelocity -p ./Platform/Velocity` |
| Waterfall/Bungee | `./gradlew runWaterfall -p ./Platform/Bungee`  |

### 3. Start the Bukkit/Spigot Server

```bash
./gradlew runServer -p ./Framework
```

> **Important:** Configure before running:
>
> * Bukkit/Paper: `config/paper-global.yml`
> * Velocity: `velocity.toml`

---

## 🧩 Supported Platforms

| Type   | Platforms / Forks                                |
| ------ | ----------------------------------------------- |
| Bukkit | Spigot, Paper, Folia, Arclight, Mohist, and forks |
| Proxy  | Velocity, BungeeCord, Waterfall, and forks      |

---

## 📦 Supported Minecraft Versions

1.20.1 → 1.21.8

---

## ☕ JDK Requirement

* Java 21 is required for **all supported Minecraft versions** and for building the project.
