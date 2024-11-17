package kr.rtuserver.framework.bukkit.api.config.impl;

import kr.rtuserver.framework.bukkit.api.RSPlugin;
import kr.rtuserver.framework.bukkit.api.config.RSConfiguration;
import kr.rtuserver.framework.bukkit.api.storage.StorageType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SettingConfiguration extends RSConfiguration {

    private boolean verbose = false;
    private boolean enablePlugin = true;
    private boolean motd = false;
    private String locale = "KR";
    private StorageType storage = StorageType.JSON;

    public SettingConfiguration(RSPlugin plugin) {
        super(plugin, "Setting.yml", 1);
        setup(this);
    }

    private void init() {
        verbose = getBoolean("verbose", verbose, """
                This is an option for developers
                Don't enable this option
                이 옵션은 개발자용입니다
                활성화하면 안됩니다""");
        enablePlugin = getBoolean("enablePlugin", enablePlugin, """
                Control Whether Enable This Plugin Functions
                If disabled, Event listener and Scheduler will be disabled
                플러그인의 기능을 활성화할지 설정합니다
                비활성화할시 이벤트 리스너와 스케듈러가 비활성화됩니다""");
        motd = getBoolean("motd", motd, """
                Control Whether Send Motd to OP
                If disabled, Not send
                Motd를 OP에게 보낼지 조정합니다
                비활성화할시 Motd를 보내지 않습니다""");
        locale = getString("locale", locale, """
                Message Locale, You can make new Locale File, Locale_KR.yml = KR
                Internal Locale: KR, EN (Locale file is automatically created when the configuration is loaded)
                메세지 언어, 새로운 언어 파일을 만들 수 있습니다, Locale_KR.yml = KR
                내장된 언어: KR, EN (구성을 불러올때 자동으로 파일이 생성됩니다)""");
        storage = StorageType.getType(getString("storage", storage.name(), """
                Data save format, Available format: JSON, MONGODB, MYSQL, MARIADB
                데이터 저장 포멧: 사용 가능한 포멧: JSON, MONGODB, MYSQL, MARIADB"""));
    }

    @Override
    public void reload() {
        final boolean previous = enablePlugin;
        super.reload();
        if (previous != enablePlugin) {
            if (enablePlugin) getPlugin().registerEvents();
            else getPlugin().unregisterEvents();
        }
    }
}
