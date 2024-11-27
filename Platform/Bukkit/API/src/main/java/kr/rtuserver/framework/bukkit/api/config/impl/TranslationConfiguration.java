package kr.rtuserver.framework.bukkit.api.config.impl;

import kr.rtuserver.cdi.LightDI;
import kr.rtuserver.framework.bukkit.api.RSPlugin;
import kr.rtuserver.framework.bukkit.api.core.Framework;
import kr.rtuserver.framework.bukkit.api.utility.platform.FileResource;
import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Getter
public class TranslationConfiguration {

    private final Translation defaultLocale;
    private final Map<String, Translation> map = new HashMap<>();
    private final Framework framework = LightDI.getBean(Framework.class);

    public TranslationConfiguration(RSPlugin plugin, String folder, String lang) {
        defaultLocale = new Translation(plugin, folder, lang);
        File[] list = FileResource.createFolder("Translations").listFiles();
        if (list == null) return;
        for (File file : list) {
            System.out.println("Load Translation: " + file.getName());
            map.put(file.getName(), new Translation(plugin, folder, file.getName()));
        }
    }

    public String get(String key) {
        return defaultLocale.get(key);
    }

    public String get(CommandSender sender, String key) {
        if (sender instanceof Player player) {
            System.out.println("Player Translation: " + player.getLocale());
            return map.getOrDefault(player.getLocale(), defaultLocale).get(key);
        }
        return defaultLocale.get(key);
    }

    public void reload() {
        defaultLocale.reload();
        map.values().forEach(Translation::reload);
    }
}