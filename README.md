# RSFramework

Framework Bukkit Plugin for RSPlugin (Spigot/Paper/Folia/Velocity/Bungee/Hybrid)

- Send packets to a proxy and another server without developing a plugin for the proxy.
- Store data in a JSON-based structure with support for MySQL, MariaDB, MongoDB, and JSON files.
- A simple utility for managing custom items and blocks, compatible with tools like ItemsAdder, Oraxen, Nexo, MMOItems
  and EcoItems

## How to Build?

1. `./gradlew build`

## How to Run Server and Proxy?

1. `./gradlew reobfJar`
2. Proxy
    - `./gradlew runVelocity -p ./Platform/Velocity`
    - `./gradlew runWaterfall -p ./Platform/Bungee`
3. `./gradlew runServer -p ./Platform/Bukkit`
4. Don't forget. You should setup config of Bukkit:`config/paper-global.yml` and Velocity:`velocity.toml`

## Supported Bukkit

Spigot, Paper, Folia, Arclight, Mohist
and Forks

## Supported Proxy

Velocity, Bungeecord, Waterfall and Forks

## Supported  Version

1.17.1 ~ 1.21.7

## JDK Version

- for 1.17.1 ~ 1.21.7, Java 21
- for Project Build, Java 21
