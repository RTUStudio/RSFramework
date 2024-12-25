# RSFramework

Framework for Bukkit RSPlugin (Bukkit/Velocity/Bungeecord)

- Send packets to a proxy and another server without developing a plugin for the proxy.
- Store data in a JSON-based structure with support for MySQL, MongoDB, and JSON files.
- A simple utility for managing custom items and blocks, compatible with tools like ItemsAdder, Oraxen, Nexo and
  MMOItems​

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

Spigot, Paper, Pufferfish, Purpur, Plazma, Arclight, Mohist
and Paper forks

## Supported Proxy

Velocity, Bungeecord, Waterfall and Bungeecord forks

## Supported Version

1.19.3 ~ 1.21.3

## JDK Version

- for 1.19.3 ~ 1.21.3, Java 21
- for Project Build, Java 21