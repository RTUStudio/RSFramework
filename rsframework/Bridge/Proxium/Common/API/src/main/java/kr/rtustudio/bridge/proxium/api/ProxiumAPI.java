package kr.rtustudio.bridge.proxium.api;

import kr.rtustudio.bridge.proxium.api.protocol.Protocol;
import kr.rtustudio.bridge.proxium.api.util.Event;
import lombok.NonNull;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ProxiumAPI {

    /**
     * {@link ProxiumAPI#load(Protocol)}가 호출될 때 발생하는 이벤트. 이 이벤트를 통해 {@link
     * Event.Cancelable#cancel()}을 호출하여 특정 프로토콜의 로딩을 취소할 수 있다. 단, 내부 핵심 Proxium 통신 프로토콜은 취소할 수 없다.
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

    /** {@link Protocol}이 메모리에 완전히 로드된 직후 발생하는 이벤트. */
    public static final Event<LoadedProtocol> PROTOCOL_LOADED =
            new Event<>(callbacks -> protocol -> callbacks.forEach(cb -> cb.trigger(protocol)));

    private static final ConcurrentHashMap<String, Protocol> loadedProtocols =
            new ConcurrentHashMap<>();

    /** 주어진 {@link Protocol}을 시스템에 로컬로 로드한다. 지정된 이름과 네임스페이스가 이미 존재하여 로드된 프로토콜인 경우 아무런 동작도 하지 않는다. */
    public static void load(@NonNull Protocol protocol) {
        if (loadedProtocols.containsKey(protocol.toString())) return;

        Event.Cancelable cancelable = new Event.Cancelable();
        PRE_PROTOCOL_LOADED.getInvoker().trigger(protocol, cancelable);
        if (cancelable.isCanceled()) return;

        loadedProtocols.put(protocol.toString(), protocol);
        PROTOCOL_LOADED.getInvoker().trigger(protocol);
    }

    /**
     * @return 현재 JVM 인스턴스에 로드된 모든 {@link Protocol} 객체들의 불변 리스트.
     */
    public static List<Protocol> getLoadedProtocols() {
        return loadedProtocols.values().stream().toList();
    }

    /**
     * 네임스페이스와 이름을 기반으로 로드된 {@link Protocol}을 가져온다.
     *
     * @param namespaceAndName 콜론("namespace:name")으로 결합된 프로토콜의 네임스페이스와 이름
     * @return 등록된 지정 {@link Protocol}. 해당 이름의 프로토콜이 없으면 null을 반환.
     */
    public static Protocol getLoadedProtocol(@NonNull String namespaceAndName) {
        return loadedProtocols.get(namespaceAndName);
    }

    /**
     * 네임스페이스와 이름을 기반으로 로드된 {@link Protocol}을 가져온다.
     *
     * @param namespace 프로토콜의 네임스페이스 (예: rsframework)
     * @param name 프로토콜의 이름 (예: internal)
     * @return 등록된 지정 {@link Protocol}. 해당 이름의 프로토콜이 없으면 null을 반환.
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
