package kr.rtuserver.framework.bukkit.api.configuration.paper.configuration.type;

import kr.rtuserver.framework.bukkit.api.configuration.paper.configuration.serializer.EngineModeSerializer;
import org.spongepowered.configurate.serialize.ScalarSerializer;

public enum EngineMode {

    HIDE(1, "hide ores"), OBFUSCATE(2, "obfuscate"), OBFUSCATE_LAYER(3, "obfuscate layer");

    public static final ScalarSerializer<EngineMode> SERIALIZER = new EngineModeSerializer();

    private final int id;
    private final String description;

    EngineMode(int id, String description) {
        this.id = id;
        this.description = description;
    }

    public static EngineMode valueOf(int id) {
        for (EngineMode engineMode : values()) {
            if (engineMode.getId() == id) {
                return engineMode;
            }
        }

        throw new IllegalArgumentException("No enum constant with id " + id);
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }
}
