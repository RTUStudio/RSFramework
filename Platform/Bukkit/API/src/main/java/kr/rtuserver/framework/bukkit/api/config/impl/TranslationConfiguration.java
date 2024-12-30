package kr.rtuserver.framework.bukkit.api.config.impl;

import kr.rtuserver.cdi.LightDI;
import kr.rtuserver.framework.bukkit.api.RSPlugin;
import kr.rtuserver.framework.bukkit.api.core.Framework;
import kr.rtuserver.framework.bukkit.api.utility.platform.FileResource;
import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Getter
public class TranslationConfiguration {

    private final RSPlugin plugin;
    private final String folder;
    private final String defaultLocale;
    private final Map<String, Translation> map = new HashMap<>();
    private final Framework framework = LightDI.getBean(Framework.class);

    public TranslationConfiguration(RSPlugin plugin, String folder, String defaultLocale) {
        this.plugin = plugin;
        this.folder = folder;
        this.defaultLocale = defaultLocale;
        reload();
    }

    public String get(String key) {
        return map.get(defaultLocale).get(key);
    }

    public String get(CommandSender sender, String key) {
        if (sender == null) return get(key);
        if (sender instanceof Player player) {
            return map.getOrDefault(player.getLocale(), map.get(defaultLocale)).get(key);
        }
        return get(key);
    }

    public void reload() {
        File[] files = FileResource.createFolder(getPlugin().getDataFolder() + "/Translations").listFiles();
        if (files == null) return;
        Set<String> list = plugin.getLanguages();
        list.addAll(Arrays.stream(files).map(File::getName).toList());
        list.add(defaultLocale);
        for (String file : list) map.put(file, new Translation(plugin, folder, file));
    }
}