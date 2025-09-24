package kr.rtustudio.framework.bungee.plugin;

import net.md_5.bungee.api.plugin.Plugin;

import org.jetbrains.annotations.NotNull;

import com.alessiodp.libby.BungeeLibraryManager;
import com.alessiodp.libby.Library;

class Libraries {

    private final BungeeLibraryManager manager;

    Libraries(Plugin plugin) {
        this.manager = new BungeeLibraryManager(plugin, "Libraries");
        manager.addMavenCentral();
        manager.addJitPack();
        manager.addRepository("https://repo.papermc.io/repository/maven-public/");
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
