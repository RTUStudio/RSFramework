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

    private static final int RPC_ITERATIONS = 5;

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

        String serverName = proxium.getName();
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

        // ─── 2. RPC Test (5 iterations) ───
        log.info(
                "[Test] [2/2] RPC: Running "
                        + RPC_ITERATIONS
                        + " iterations → "
                        + serverName
                        + "...");

        // Register responder once
        proxium.respond(BridgeChannel.INTERNAL)
                .on(
                        BroadcastMessage.class,
                        (sender, req) -> new BroadcastMessage("[Test] Pong from " + serverName))
                .error(e -> log.severe("[Test] RPC Responder error: " + e.type()));

        // Run iterations sequentially using CountDownLatch chaining
        long[] rtts = new long[RPC_ITERATIONS];
        runRpcIteration(proxium, serverName, log, rtts, 0);

        return Result.SUCCESS;
    }

    private void runRpcIteration(
            Proxium proxium, String serverName, Logger log, long[] rtts, int index) {
        if (index >= RPC_ITERATIONS) {
            // All iterations complete — print summary
            long min = Long.MAX_VALUE, max = 0, sum = 0;
            for (long rtt : rtts) {
                min = Math.min(min, rtt);
                max = Math.max(max, rtt);
                sum += rtt;
            }
            log.info("[Test] ───────────────────────────────────────");
            log.info(
                    "[Test] RPC Summary ("
                            + RPC_ITERATIONS
                            + " calls): min="
                            + min
                            + "ms, max="
                            + max
                            + "ms, avg="
                            + (sum / RPC_ITERATIONS)
                            + "ms");
            log.info("[Test] ═══════════════════════════════════════");
            log.info("[Test] All tests completed!");
            log.info("[Test] ═══════════════════════════════════════");
            return;
        }

        long sendTime = System.currentTimeMillis();
        proxium.request(
                        serverName,
                        BridgeChannel.INTERNAL,
                        new BroadcastMessage("[Test] Ping #" + (index + 1)),
                        Duration.ofSeconds(5))
                .on(
                        BroadcastMessage.class,
                        (sender, resp) -> {
                            long rtt = System.currentTimeMillis() - sendTime;
                            rtts[index] = rtt;
                            log.info("[Test] [2/2] RPC #" + (index + 1) + ": ✅ RTT=" + rtt + "ms");
                            // Chain next iteration
                            runRpcIteration(proxium, serverName, log, rtts, index + 1);
                        })
                .error(
                        e -> {
                            log.severe(
                                    "[Test] [2/2] RPC #"
                                            + (index + 1)
                                            + ": ❌ Failed — "
                                            + e.type());
                            if (e.getCause() != null) {
                                log.severe("[Test] Cause — " + e.getCause().getMessage());
                            }
                        });
    }
}
