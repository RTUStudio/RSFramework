# RSFramework
Framework for Bukkit RSPlugin (Bukkit/Velocity/Bungeecord)
- Send packet to proxy and other server without developing proxy plugin
- Data Storage based by json/nosql structure with MySQL, MongoDB, JsonFile
- Simple utility for custom item and block (ItemsAdder, Oraxen, MMOItems, CustomModelData)

## How to Build?
1. `./gradlew build`

## How to Run Server and Proxy?

1. `./gradlew reobfJar`
2. Proxy
    - `./gradlew runVelocity -p ./Platform/Velocity`
    - `./gradlew runWaterfall -p ./Platform/Bungee`
3. `./gradlew runServer -p ./Platform/Bukkit`
4. Don't forget. You should setup config of Bukkit:`config/paper-global.yml` and Velocity:`velocity.toml`

## Supported MC Version
1.17.1 ~ 1.21.3

## JDK Version
- for 1.17.1 ~ 1.21.3, Java 21
- for Project Build, Java 21