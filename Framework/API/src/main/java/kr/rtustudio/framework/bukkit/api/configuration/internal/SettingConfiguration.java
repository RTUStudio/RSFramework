package kr.rtustudio.framework.bukkit.api.configuration.internal;

import kr.rtustudio.framework.bukkit.api.RSPlugin;
import kr.rtustudio.framework.bukkit.api.configuration.ConfigPath;
import kr.rtustudio.framework.bukkit.api.configuration.RSConfiguration;
import lombok.Getter;
import lombok.Setter;

/**
 * 플러그인 공통 설정({@code Config/Setting.yml})을 관리하는 클래스입니다.
 *
 * <p>디버그 모드, 리스너 활성화, MOTD, 접두사, 로케일 등의 기본 설정을 포함합니다.
 */
@Getter
@Setter
@SuppressWarnings("unused")
public class SettingConfiguration extends RSConfiguration.Wrapper<RSPlugin> {

    private boolean verbose = false;
    private boolean listener = true;
    private boolean welcome = true;
    private String prefix = "";
    private String locale;

    public SettingConfiguration(RSPlugin plugin) {
        super(plugin, ConfigPath.of("Setting"));
        this.locale = plugin.getLanguages().getFirst();
        setup(this);
    }

    private void init() {
        verbose =
                getBoolean(
                        "verbose",
                        verbose,
                        """
                                Debug target
                                디버그 옵션입니다""");
        listener =
                getBoolean(
                        "listener",
                        listener,
                        """
                                When disabled, event listeners will be deactivated
                                비활성화할시 이벤트 리스너가 비활성화됩니다""");
        welcome =
                getBoolean(
                        "welcome",
                        welcome,
                        """
                                Controls whether to send MOTD to all players
                                If disabled, MOTD will only be sent to operators
                                MOTD 메세지를 플레이어에게 전송할지 조정합니다
                                비활성화할시 관리자에게만 전송합니다""");
        prefix =
                getString(
                        "prefix",
                        prefix,
                        """
                                Plugin message prefix. If left empty, the default prefix will be used.
                                시스템 메세지의 접두사입니다. 비워두면 내장 접두사를 사용합니다.""");
        locale =
                getString(
                        "locale",
                        locale,
                        """
                                Default locale for messages and commands. Custom locale files can be created.
                                Built-in locales: en_us, ko_kr
                                메세지 및 명령어 기본 언어, 새로운 언어 파일을 만들 수 있습니다
                                내장된 언어: en_us, ko_kr""");
    }

    @Override
    public void reload() {
        final boolean previous = listener;
        super.reload();
        if (previous != listener) {
            if (listener) getPlugin().registerEvents();
            else getPlugin().unregisterEvents();
        }
    }
}
