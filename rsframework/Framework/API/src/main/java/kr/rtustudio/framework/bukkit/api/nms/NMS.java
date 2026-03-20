package kr.rtustudio.framework.bukkit.api.nms;

/**
 * 버전별 NMS(Net Minecraft Server) 기능에 접근하는 인터페이스입니다.
 *
 * <p>각 서버 버전에 맞는 구현체가 런타임에 주입됩니다.
 */
public interface NMS {

    /** 아이템 관련 NMS 기능을 반환한다. */
    Item getItem();

    /** 바이옴 관련 NMS 기능을 반환한다. */
    Biome getBiome();

    /** 명령어 관련 NMS 기능을 반환한다. */
    Command getCommand();
}
