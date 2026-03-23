package kr.rtustudio.bridge.proxium.api;

import kr.rtustudio.bridge.proxium.api.protocol.Protocol;
import kr.rtustudio.bridge.proxium.api.util.Event;
import lombok.NonNull;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages protocol loading and provides lifecycle events for the Proxium system.
 *
 * <p>Proxium 시스템의 프로토콜 로딩을 관리하고 생명주기 이벤트를 제공한다.
 */
public class ProxiumAPI {

    /**
     * Event fired when {@link ProxiumAPI#load(Protocol)} is called. Protocol loading can be
     * cancelled via {@link Event.Cancelable#cancel()}, except for core Proxium protocols.
     *
     * <p>{@link ProxiumAPI#load(Protocol)}가 호출될 때 발생하는 이벤트. {@link Event.Cancelable#cancel()}을 호출하여
     * 특정 프로토콜의 로딩을 취소할 수 있다. 단, 내부 핵심 Proxium 통신 프로토콜은 취소할 수 없다.
     */
    public static final Event<PreLoadedProtocol> PRE_PROTOCOL_LOADED =
            new Event<>(
                    callbacks ->
                            (protocol, cancelable) -> {
                                for (PreLoadedProtocol callback : callbacks) {
                                    if (cancelable.isCanceled()) {
                                        if (!protocol.getChannel().isProxium()) break;
                                    }
                                    callback.trigger(protocol, cancelable);
                                }
                            });

    /**
     * Event fired after a {@link Protocol} is fully loaded into memory.
     *
     * <p>{@link Protocol}이 메모리에 완전히 로드된 직후 발생하는 이벤트.
     */
    public static final Event<LoadedProtocol> PROTOCOL_LOADED =
            new Event<>(callbacks -> protocol -> callbacks.forEach(cb -> cb.trigger(protocol)));

    private static final ConcurrentHashMap<String, Protocol> loadedProtocols =
            new ConcurrentHashMap<>();

    /**
     * Loads the given {@link Protocol} into the system. No-op if a protocol with the same namespace
     * and name is already loaded.
     *
     * <p>주어진 {@link Protocol}을 시스템에 로컬로 로드한다. 동일 이름/네임스페이스가 존재하면 무시한다.
     */
    public static void load(@NonNull Protocol protocol) {
        if (loadedProtocols.containsKey(protocol.toString())) return;

        Event.Cancelable cancelable = new Event.Cancelable();
        PRE_PROTOCOL_LOADED.getInvoker().trigger(protocol, cancelable);
        if (cancelable.isCanceled()) return;

        loadedProtocols.put(protocol.toString(), protocol);
        PROTOCOL_LOADED.getInvoker().trigger(protocol);
    }

    /**
     * Returns an immutable list of all protocols loaded in the current JVM.
     *
     * <p>현재 JVM 인스턴스에 로드된 모든 {@link Protocol} 객체들의 불변 리스트를 반환한다.
     *
     * @return immutable list of loaded protocols
     */
    public static List<Protocol> getLoadedProtocols() {
        return loadedProtocols.values().stream().toList();
    }

    /**
     * Retrieves a loaded protocol by namespace and name.
     *
     * <p>네임스페이스와 이름을 기반으로 로드된 {@link Protocol}을 가져온다.
     *
     * @param namespaceAndName colon-joined namespace and name (e.g. {@code "rsframework:internal"})
     * @return the protocol, or {@code null} if not found
     */
    public static Protocol getLoadedProtocol(@NonNull String namespaceAndName) {
        return loadedProtocols.get(namespaceAndName);
    }

    /**
     * Retrieves a loaded protocol by separate namespace and name.
     *
     * <p>네임스페이스와 이름을 기반으로 로드된 {@link Protocol}을 가져온다.
     *
     * @param namespace protocol namespace (e.g. {@code "rsframework"})
     * @param name protocol name (e.g. {@code "internal"})
     * @return the protocol, or {@code null} if not found
     */
    public static Protocol getLoadedProtocol(@NonNull String namespace, @NonNull String name) {
        return getLoadedProtocol(namespace + ":" + name);
    }

    @FunctionalInterface
    public interface PreLoadedProtocol {
        void trigger(Protocol protocol, Event.Cancelable cancelable);
    }

    @FunctionalInterface
    public interface LoadedProtocol {
        void trigger(Protocol protocol);
    }
}
