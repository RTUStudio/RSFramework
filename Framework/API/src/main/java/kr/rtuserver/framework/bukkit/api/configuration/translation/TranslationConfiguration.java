package kr.rtuserver.framework.bukkit.api.configuration.translation;

import com.google.common.io.Files;
import kr.rtuserver.cdi.LightDI;
import kr.rtuserver.framework.bukkit.api.RSPlugin;
import kr.rtuserver.framework.bukkit.api.core.Framework;
import kr.rtuserver.framework.bukkit.api.platform.FileResource;
import kr.rtuserver.protoweaver.api.proxy.ProxyPlayer;
import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Getter
public class TranslationConfiguration {

    private final RSPlugin plugin;
    private final TranslationType type;
    private final String defaultLocale;
    private final Map<String, Translation> map = new HashMap<>();
    private final Framework framework = LightDI.getBean(Framework.class);

    public TranslationConfiguration(RSPlugin plugin, TranslationType type, String defaultLocale) {
        this.plugin = plugin;
        this.type = type;
        this.defaultLocale = defaultLocale;
        reload();
    }

    public String get(String key) {
        Translation translation = map.get(defaultLocale);
        if (translation == null) return "";
        return translation.get(key);
    }

    public String get(String locale, String key) {
        Translation translation = map.get(locale);
        if (translation == null) return "";
        return translation.get(key);
    }

    @NotNull
    public String get(CommandSender sender, String key) {
        if (sender instanceof Player player) {
            Translation translation = map.getOrDefault(player.getLocale(), map.get(defaultLocale));
            if (translation == null) return "";
            return translation.get(key);
        }
        return get(key);
    }

    @NotNull
    public String get(ProxyPlayer player, String key) {
        if (player != null && player.locale() != null) {
            Translation translation = map.getOrDefault(player.locale().toString(), map.get(defaultLocale));
            if (translation == null) return "";
            return translation.get(key);
        }
        return get(key);
    }

    public String getCommon(String key) {
        return framework.getCommonTranslation().get(type, null, key);
    }

    public String getCommon(String locale, String key) {
        return framework.getCommonTranslation().get(type, locale, key);
    }

    public String getCommon(CommandSender sender, String key) {
        if (sender instanceof Player player) return getCommon(player.getLocale(), key);
        return getCommon(key);
    }

    public void reload() {
        File[] files = FileResource.createFolder(getPlugin().getDataFolder() + "/Translations/" + type.getName()).listFiles();
        if (files == null) return;
        Set<String> list = plugin.getLanguages();
        list.addAll(Arrays.stream(files).map(file -> Files.getNameWithoutExtension(file.getName())).toList());
        list.add(defaultLocale);
        for (String lang : list) map.put(lang, new Translation(plugin, type.getName(), lang));
    }

}