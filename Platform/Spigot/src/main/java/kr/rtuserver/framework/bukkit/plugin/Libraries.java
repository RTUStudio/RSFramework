package kr.rtuserver.framework.bukkit.plugin;

import kr.rtuserver.framework.bukkit.api.RSPlugin;

import org.jetbrains.annotations.NotNull;

import com.alessiodp.libby.BukkitLibraryManager;
import com.alessiodp.libby.Library;

class Libraries {

    private final BukkitLibraryManager manager;

    Libraries(RSPlugin plugin) {
        this.manager = new BukkitLibraryManager(plugin, "Libraries");
        manager.addMavenCentral();
        manager.addJitPack();
        manager.addRepository("https://repo.papermc.io");
    }

    public void load(@NotNull String dependency) {
        String[] split = dependency.split(":");
        String groupId = split[0];
        String artifactId = split[1];
        String version = split[2];
        Library lib =
                Library.builder().groupId(groupId).artifactId(artifactId).version(version).build();
        manager.loadLibrary(lib);
    }

    public void load(
            @NotNull String dependency, @NotNull String pattern, @NotNull String relocatedPattern) {
        String[] split = dependency.split(":");
        String groupId = split[0];
        String artifactId = split[1];
        String version = split[2];
        Library lib =
                Library.builder()
                        .groupId(groupId)
                        .artifactId(artifactId)
                        .version(version)
                        .relocate(pattern, relocatedPattern)
                        .build();
        manager.loadLibrary(lib);
    }
}
