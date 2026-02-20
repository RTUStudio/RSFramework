package kr.rtustudio.framework.bukkit.api.configuration.internal.translation;

import kr.rtustudio.broker.protoweaver.api.proxy.ProxyPlayer;
import kr.rtustudio.cdi.LightDI;
import kr.rtustudio.framework.bukkit.api.RSPlugin;
import kr.rtustudio.framework.bukkit.api.core.Framework;
import kr.rtustudio.framework.bukkit.api.platform.FileResource;
import lombok.Getter;

import java.io.File;
import java.util.*;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import com.google.common.io.Files;

@Getter
public class TranslationConfiguration {

    private final RSPlugin plugin;
    private final TranslationType type;
    private final String defaultLocale;
    private final Map<String, Translation> translations = new HashMap<>();
    private final Framework framework = LightDI.getBean(Framework.class);

    public TranslationConfiguration(RSPlugin plugin, TranslationType type, String defaultLocale) {
        this.plugin = plugin;
        this.type = type;
        this.defaultLocale = defaultLocale;
        reload();
    }

    private Translation resolveTranslation(String locale) {
        if (locale == null) return translations.get(defaultLocale);
        return translations.getOrDefault(locale, translations.get(defaultLocale));
    }

    private String resolveLocale(CommandSender sender) {
        return sender instanceof Player player ? player.getLocale() : null;
    }

    private String resolveLocale(ProxyPlayer player) {
        return player != null && player.locale() != null ? player.locale().toString() : null;
    }

    @NotNull
    public String get(String key) {
        Translation translation = resolveTranslation(null);
        if (translation == null) return "";
        return translation.get(key);
    }

    @NotNull
    public String get(String locale, String key) {
        Translation translation = resolveTranslation(locale);
        if (translation == null) return "";
        return translation.get(key);
    }

    @NotNull
    public String get(CommandSender sender, String key) {
        Translation translation = resolveTranslation(resolveLocale(sender));
        if (translation == null) return "";
        return translation.get(key);
    }

    @NotNull
    public String get(ProxyPlayer player, String key) {
        Translation translation = resolveTranslation(resolveLocale(player));
        if (translation == null) return "";
        return translation.get(key);
    }

    @NotNull
    public List<String> getList(String key) {
        Translation translation = resolveTranslation(null);
        if (translation == null) return List.of();
        return translation.getList(key);
    }

    @NotNull
    public List<String> getList(String locale, String key) {
        Translation translation = resolveTranslation(locale);
        if (translation == null) return List.of();
        return translation.getList(key);
    }

    @NotNull
    public List<String> getList(CommandSender sender, String key) {
        Translation translation = resolveTranslation(resolveLocale(sender));
        if (translation == null) return List.of();
        return translation.getList(key);
    }

    @NotNull
    public List<String> getList(ProxyPlayer player, String key) {
        Translation translation = resolveTranslation(resolveLocale(player));
        if (translation == null) return List.of();
        return translation.getList(key);
    }

    @NotNull
    public String getCommon(String key) {
        return framework.getCommonTranslation().get(type, null, key);
    }

    @NotNull
    public String getCommon(String locale, String key) {
        return framework.getCommonTranslation().get(type, locale, key);
    }

    @NotNull
    public String getCommon(CommandSender sender, String key) {
        if (sender instanceof Player player) return getCommon(player.getLocale(), key);
        return getCommon(key);
    }

    @NotNull
    public String getCommon(ProxyPlayer player, String key) {
        if (player != null && player.locale() != null)
            return getCommon(player.locale().toString(), key);
        return getCommon(key);
    }

    @NotNull
    public List<String> getCommonList(String key) {
        return framework.getCommonTranslation().getList(type, null, key);
    }

    @NotNull
    public List<String> getCommonList(String locale, String key) {
        return framework.getCommonTranslation().getList(type, locale, key);
    }

    @NotNull
    public List<String> getCommonList(CommandSender sender, String key) {
        if (sender instanceof Player player) return getCommonList(player.getLocale(), key);
        return getCommonList(key);
    }

    @NotNull
    public List<String> getCommonList(ProxyPlayer player, String key) {
        if (player != null && player.locale() != null)
            return getCommonList(player.locale().toString(), key);
        return getCommonList(key);
    }

    public void reload() {
        File[] files =
                FileResource.createFolder(
                                getPlugin().getDataFolder() + "/Translations/" + type.getName())
                        .listFiles();
        if (files == null) return;
        Set<String> list = plugin.getLanguages();
        list.addAll(
                Arrays.stream(files)
                        .map(file -> Files.getNameWithoutExtension(file.getName()))
                        .toList());
        list.add(defaultLocale);
        for (String lang : list)
            this.translations.put(lang, new Translation(plugin, type.getName(), lang));
    }
}
