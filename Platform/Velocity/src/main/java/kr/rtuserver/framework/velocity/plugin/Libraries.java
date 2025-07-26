package kr.rtuserver.framework.velocity.plugin;

import com.alessiodp.libby.Library;
import com.alessiodp.libby.VelocityLibraryManager;
import com.velocitypowered.api.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.nio.file.Path;

class Libraries {

    private final VelocityLibraryManager<RSFramework> manager;

    Libraries(@NotNull RSFramework plugin, @NotNull Logger logger, @NotNull Path dataDirectory, @NotNull PluginManager pluginManager) {
        this.manager = new VelocityLibraryManager<>(plugin, logger, dataDirectory, pluginManager, "Libraries");
        manager.addMavenCentral();
        manager.addJitPack();
        manager.addRepository("https://repo.papermc.io");
    }

    public void load(@NotNull String dependency) {
        String[] split = dependency.split(":");
        String groupId = split[0];
        String artifactId = split[1];
        String version = split[2];
        Library lib = Library.builder()
                .groupId(groupId)
                .artifactId(artifactId)
                .version(version)
                .build();
        manager.loadLibrary(lib);
    }

    public void load(@NotNull String dependency, @NotNull String pattern, @NotNull String relocatedPattern) {
        String[] split = dependency.split(":");
        String groupId = split[0];
        String artifactId = split[1];
        String version = split[2];
        Library lib = Library.builder()
                .groupId(groupId)
                .artifactId(artifactId)
                .version(version)
                .relocate(pattern, relocatedPattern)
                .build();
        manager.loadLibrary(lib);
    }

}
