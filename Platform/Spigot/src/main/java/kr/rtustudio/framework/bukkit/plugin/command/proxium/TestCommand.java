package kr.rtustudio.framework.bukkit.plugin.command.proxium;

import kr.rtustudio.bridge.BridgeChannel;
import kr.rtustudio.bridge.proxium.api.Proxium;
import kr.rtustudio.bridge.proxium.api.protocol.internal.BroadcastMessage;
import kr.rtustudio.framework.bukkit.api.command.CommandArgs;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import kr.rtustudio.framework.bukkit.plugin.RSFramework;

import java.time.Duration;
import java.util.logging.Logger;

import org.bukkit.permissions.PermissionDefault;

/**
 * Proxium RPC / Pub-Sub 통합 테스트 콘솔 명령어.
 *
 * <p>사용법: {@code proxium test}
 */
public class TestCommand extends RSCommand<RSFramework> {

    public TestCommand(RSFramework plugin) {
        super(plugin, "test", PermissionDefault.OP);
    }

    @Override
    protected Result execute(CommandArgs data) {
        Proxium proxium = getFramework().getBridge(Proxium.class);
        Logger log = getPlugin().getLogger();

        if (proxium == null || !proxium.isLoaded()) {
            notifier.announce(message.get(player(), "test.not-loaded"));
            return Result.FAILURE;
        }

        String serverName = proxium.getServer();
        if (serverName == null) {
            notifier.announce(message.get(player(), "test.not-connected"));
            return Result.FAILURE;
        }

        log.info("[Test] ═══════════════════════════════════════");
        log.info("[Test] Starting Proxium integration tests");
        log.info("[Test] Server: " + serverName);
        log.info("[Test] ═══════════════════════════════════════");

        // ─── 1. Pub/Sub Test ───
        log.info("[Test] [1/2] Pub/Sub: Publishing BroadcastMessage...");
        try {
            proxium.publish(
                    BridgeChannel.AUDIENCE,
                    new BroadcastMessage("<green>[Test] Pub/Sub OK!</green>"));
            log.info("[Test] [1/2] Pub/Sub: ✅ Message published successfully");
        } catch (Exception e) {
            log.severe("[Test] [1/2] Pub/Sub: ❌ Failed — " + e.getMessage());
        }

        // ─── 2. RPC Test ───
        // Use BroadcastMessage (already pre-registered on AUDIENCE channel)
        // as the RPC request/response type. Handler key = channel + type.
        log.info("[Test] [2/2] RPC: Sending BroadcastMessage request → " + serverName + "...");

        // Register responder for BroadcastMessage on INTERNAL channel
        // (types already registered; responseHandlers map is separate from subscriptions)
        proxium.respond(BridgeChannel.INTERNAL)
                .on(
                        BroadcastMessage.class,
                        (sender, req) -> {
                            log.info(
                                    "[Test] [2/2] RPC: Responder received request from '"
                                            + sender
                                            + "' (msg="
                                            + req.message()
                                            + ")");
                            return new BroadcastMessage("[Test] Pong from " + serverName);
                        })
                .error(e -> log.severe("[Test] [2/2] RPC Responder error: " + e.type()));
        // Send request to self (through proxy)
        long sendTime = System.currentTimeMillis();
        proxium.request(
                        serverName,
                        BridgeChannel.INTERNAL,
                        new BroadcastMessage("[Test] Ping"),
                        Duration.ofSeconds(5))
                .on(
                        BroadcastMessage.class,
                        (sender, resp) -> {
                            long rtt = System.currentTimeMillis() - sendTime;
                            log.info(
                                    "[Test] [2/2] RPC: ✅ Got response from '"
                                            + sender
                                            + "' (msg="
                                            + resp.message()
                                            + ", RTT="
                                            + rtt
                                            + "ms)");
                            log.info("[Test] ═══════════════════════════════════════");
                            log.info("[Test] All tests completed!");
                            log.info("[Test] ═══════════════════════════════════════");
                        })
                .error(
                        e -> {
                            log.severe("[Test] [2/2] RPC: ❌ Failed — " + e.type());
                            if (e.getCause() != null) {
                                log.severe(
                                        "[Test] [2/2] RPC: Cause — " + e.getCause().getMessage());
                            }
                        });

        return Result.SUCCESS;
    }
}
