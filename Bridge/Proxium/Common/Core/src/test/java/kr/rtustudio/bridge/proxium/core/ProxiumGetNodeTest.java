package kr.rtustudio.bridge.proxium.core;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import kr.rtustudio.bridge.BridgeOptions;
import kr.rtustudio.bridge.proxium.api.ProxiumNode;
import kr.rtustudio.bridge.proxium.api.ProxiumPipeline;
import kr.rtustudio.bridge.proxium.api.protocol.internal.ServerList;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ProxiumGetNodeTest {

    private final ProxiumPipeline pipeline = mock(ProxiumPipeline.class);

    // ── MutableProxyPlayer Tests ──

    @Nested
    @DisplayName("MutableProxyPlayer.getNode()")
    class MutableProxyPlayerNodeTest {

        @Test
        @DisplayName("getNode() returns node set via constructor")
        void getNode_returnsConstructorNode() {
            ProxiumNode node = new ProxiumNode("Lobby-1", "127.0.0.1", 25565);
            MutableProxyPlayer player =
                    new MutableProxyPlayer(pipeline, UUID.randomUUID(), "TestPlayer", node);

            assertNotNull(player.getNode(), "getNode() should not be null after construction");
            assertEquals("Lobby-1", player.getNode().name());
            assertEquals("Lobby-1", player.getServer());
        }

        @Test
        @DisplayName("getNode() returns null when constructed with null node")
        void getNode_returnsNullWhenConstructedWithNull() {
            MutableProxyPlayer player =
                    new MutableProxyPlayer(pipeline, UUID.randomUUID(), "TestPlayer", null);

            assertNull(player.getNode());
            assertNull(player.getServer());
        }

        @Test
        @DisplayName("setNode() updates the node correctly")
        void setNode_updatesNode() {
            ProxiumNode initial = new ProxiumNode("Lobby-1", "127.0.0.1", 25565);
            ProxiumNode updated = new ProxiumNode("Survival-1", "127.0.0.1", 25566);
            MutableProxyPlayer player =
                    new MutableProxyPlayer(pipeline, UUID.randomUUID(), "TestPlayer", initial);

            player.setNode(updated);

            assertEquals("Survival-1", player.getNode().name());
            assertEquals("Survival-1", player.getServer());
        }

        @Test
        @DisplayName("setNode(null) clears the node")
        void setNode_nullClearsNode() {
            ProxiumNode initial = new ProxiumNode("Lobby-1", "127.0.0.1", 25565);
            MutableProxyPlayer player =
                    new MutableProxyPlayer(pipeline, UUID.randomUUID(), "TestPlayer", initial);

            player.setNode(null);

            assertNull(player.getNode());
            assertNull(player.getServer());
        }
    }

    // ── ServerList + knownServers Tests ──

    @Nested
    @DisplayName("ProxiumServer.getNode() with ServerList")
    class ProxiumServerGetNodeTest {

        private TestProxiumServer server;

        @BeforeEach
        void setUp() {
            server = new TestProxiumServer();
        }

        @Test
        @DisplayName("getNode returns null when knownServers is empty")
        void getNode_emptyReturnsNull() {
            assertNull(server.getNode("Lobby-1"));
            assertNull(server.getNode(""));
        }

        @Test
        @DisplayName("getNode returns local node by name")
        void getNode_returnsLocalNode() {
            ProxiumNode localNode = new ProxiumNode("Local-1", "127.0.0.1", 25565);
            server.setNode(localNode);

            assertEquals(localNode, server.getNode("Local-1"));
        }

        @Test
        @DisplayName("getNode returns null for non-matching names when knownServers empty")
        void getNode_nonMatchingReturnsNull() {
            ProxiumNode localNode = new ProxiumNode("Local-1", "127.0.0.1", 25565);
            server.setNode(localNode);

            assertNull(server.getNode("Other-Server"));
            assertNull(server.getNode(""));
        }

        @Test
        @DisplayName("handleServerList populates knownServers")
        void handleServerList_populatesKnownServers() {
            Map<String, ProxiumNode> servers =
                    Map.of(
                            "Lobby-1", new ProxiumNode("Lobby-1", "10.0.0.1", 25565),
                            "Survival-1", new ProxiumNode("Survival-1", "10.0.0.2", 25566),
                            "Creative-1", new ProxiumNode("Creative-1", "10.0.0.3", 25567));

            server.handleServerList(new ServerList(servers));

            assertNotNull(server.getNode("Lobby-1"));
            assertEquals("10.0.0.1", server.getNode("Lobby-1").host());
            assertNotNull(server.getNode("Survival-1"));
            assertNotNull(server.getNode("Creative-1"));
        }

        @Test
        @DisplayName("handleServerList with empty string key still works")
        void handleServerList_emptyStringKey() {
            // edge case: empty string key should not crash, but won't be useful
            assertNull(server.getNode(""));
        }

        @Test
        @DisplayName("handleServerList replaces previous entries")
        void handleServerList_replacesOldEntries() {
            server.handleServerList(
                    new ServerList(
                            Map.of("Lobby-1", new ProxiumNode("Lobby-1", "10.0.0.1", 25565))));

            assertNotNull(server.getNode("Lobby-1"));

            // second update without Lobby-1
            server.handleServerList(
                    new ServerList(
                            Map.of(
                                    "Survival-1",
                                    new ProxiumNode("Survival-1", "10.0.0.2", 25566))));

            assertNull(
                    server.getNode("Lobby-1"),
                    "Lobby-1 should be removed after server list update");
            assertNotNull(server.getNode("Survival-1"));
        }

        @Test
        @DisplayName("getNode prefers knownServers over local node")
        void getNode_prefersKnownServers() {
            ProxiumNode localNode = new ProxiumNode("Local-1", "127.0.0.1", 25565);
            ProxiumNode remoteLocal = new ProxiumNode("Local-1", "10.0.0.99", 9999);
            server.setNode(localNode);
            server.handleServerList(new ServerList(Map.of("Local-1", remoteLocal)));

            // knownServers entry takes priority
            ProxiumNode result = server.getNode("Local-1");
            assertNotNull(result);
            assertEquals("10.0.0.99", result.host());
        }
    }

    // ── Proxium#request RPC getNode validation Tests ──

    @Nested
    @DisplayName("Proxium#request getNode validation")
    class RequestGetNodeTest {

        private TestProxiumServer server;

        @BeforeEach
        void setUp() {
            server = new TestProxiumServer();
        }

        @Test
        @DisplayName("request throws when target server unknown")
        void request_throwsForUnknownServer() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> server.request("Unknown-Server", null, new Object()));
        }

        @Test
        @DisplayName("getNode succeeds after ServerList received")
        void getNode_succeedsAfterServerListReceived() {
            server.handleServerList(
                    new ServerList(
                            Map.of("Target-1", new ProxiumNode("Target-1", "10.0.0.5", 25570))));

            ProxiumNode node = server.getNode("Target-1");
            assertNotNull(node, "getNode should find server after ServerList sync");
            assertEquals("Target-1", node.name());
        }
    }

    // ── Test stub ──

    /**
     * Minimal concrete ProxiumServer for testing getNode / handleServerList. Does not require a
     * real connection.
     */
    private static class TestProxiumServer extends ProxiumServer {

        TestProxiumServer() {
            super(
                    new BridgeOptions(TestProxiumServer.class.getClassLoader()),
                    new StubProxiumConfig());
        }

        @Override
        public boolean isLoaded() {
            return true;
        }
    }
}
