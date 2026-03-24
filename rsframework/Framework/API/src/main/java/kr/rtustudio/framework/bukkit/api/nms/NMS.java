package kr.rtustudio.framework.bukkit.api.nms;

/**
 * Version-specific implementations are injected at runtime.
 *
 * <p>버전별 NMS(Net Minecraft Server) 기능에 접근하는 인터페이스. 각 서버 버전에 맞는 구현체가 런타임에 주입된다.
 */
public interface NMS {

    /**
     * Returns item-related NMS features.
     *
     * <p>아이템 관련 NMS 기능을 반환한다.
     */
    Item getItem();

    /**
     * Returns biome-related NMS features.
     *
     * <p>바이옸 관련 NMS 기능을 반환한다.
     */
    Biome getBiome();

    /**
     * Returns command-related NMS features.
     *
     * <p>명령어 관련 NMS 기능을 반환한다.
     */
    Command getCommand();
}
