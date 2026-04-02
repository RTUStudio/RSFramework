package kr.rtustudio.configurate.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Base class representing an individual section of a YAML configuration file. Use with
 * {@code @ConfigSerializable} to register as a Configurate object mapping target. When declared as
 * a non-static inner class, {@link
 * kr.rtustudio.configurate.model.mapping.InnerClassFieldDiscoverer} automatically creates
 * instances.
 *
 * <p>YAML 설정 파일의 개별 섹션을 나타내는 베이스 클래스. {@code @ConfigSerializable}과 함께 사용하여 Configurate 객체 매핑 대상으로
 * 등록한다. 비정적 내부 클래스로 선언하면 {@link kr.rtustudio.configurate.model.mapping.InnerClassFieldDiscoverer}가
 * 자동으로 인스턴스를 생성한다.
 *
 * <h2>Reload Safety</h2>
 *
 * <p>During configuration reload, Configurate's {@code ObjectMapper.Mutable.load()} reuses existing
 * collection objects by calling {@code clear()} followed by {@code addAll()} / {@code putAll()}.
 * Therefore, using <b>immutable collections</b> such as {@link java.util.List#of(Object[])} or
 * {@link java.util.Map#of()} as default values will cause {@link UnsupportedOperationException} on
 * reload. To prevent this, always use <b>mutable collections</b> for configuration field defaults.
 * The {@link #listOf(Object[])} and {@link #mapOf()} helper methods in this class safely create
 * mutable collections.
 *
 * <p>설정 리로드 시 Configurate의 {@code ObjectMapper.Mutable.load()}는 기존 컬렉션 객체를 재활용하여 {@code clear()} →
 * {@code addAll()} / {@code putAll()}로 내용을 갱신한다. 따라서 {@link java.util.List#of(Object[])}나 {@link
 * java.util.Map#of()} 등의 <b>불변 컬렉션</b>을 기본값으로 사용하면 리로드 시 {@link UnsupportedOperationException}이
 * 발생한다. 이를 방지하기 위해 설정 필드의 기본값에는 반드시 <b>가변 컬렉션</b>을 사용해야 하며, 이 클래스에서 제공하는 {@link #listOf(Object[])}
 * 및 {@link #mapOf()} 헬퍼 메서드를 사용하면 안전하게 가변 컬렉션을 생성할 수 있다.
 *
 * <h2>Usage</h2>
 *
 * <pre>{@code
 * @ConfigSerializable
 * public class WhitelistConfig extends ConfigurationPart {
 *
 *     // varargs: simple list initialization
 *     private List<String> commands = listOf("help", "spawn");
 *
 *     // Consumer: complex map initialization
 *     private Map<String, List<String>> groups = mapOf(map -> {
 *         map.put("default", listOf("help"));
 *         map.put("worldedit", listOf("/wand", "/copy"));
 *     });
 *
 *     // key-value: simple map initialization
 *     private Map<String, String> aliases = mapOf("h", "help", "s", "spawn");
 * }
 * }</pre>
 *
 * @see Configuration
 * @see kr.rtustudio.configurate.model.mapping.InnerClassFieldDiscoverer
 */
public abstract class ConfigurationPart {

    /**
     * Creates an object, applies initialization logic, and returns it.
     *
     * <p>객체를 생성한 뒤 초기화 로직을 적용하여 반환한다.
     *
     * @param object the object to initialize
     * @param consumer initialization logic to apply
     * @param <T> object type
     * @return the initialized object
     */
    public <T> T make(T object, Consumer<? super T> consumer) {
        consumer.accept(object);
        return object;
    }

    /**
     * Creates a mutable list with the given elements. Wraps in {@link ArrayList} to ensure safe
     * modification during configuration reload.
     *
     * <p>주어진 요소들로 가변 리스트를 생성하여 반환한다. 설정 리로드 시 안전하게 수정할 수 있도록 {@link ArrayList}로 래핑한다.
     *
     * @param elements elements to include in the list
     * @param <E> element type
     * @return a mutable list
     */
    @SafeVarargs
    public static <E> List<E> listOf(E... elements) {
        return new ArrayList<>(List.of(elements));
    }

    /**
     * Creates an empty mutable list, applies initialization logic, and returns it.
     *
     * <p>빈 가변 리스트를 생성한 뒤 초기화 로직을 적용하여 반환한다.
     *
     * @param consumer list initialization logic
     * @param <E> element type
     * @return the initialized mutable list
     */
    public static <E> List<E> listOf(Consumer<? super List<E>> consumer) {
        List<E> list = new ArrayList<>();
        consumer.accept(list);
        return list;
    }

    /**
     * Creates an empty mutable map, applies initialization logic, and returns it.
     *
     * <p>빈 가변 맵을 생성한 뒤 초기화 로직을 적용하여 반환한다.
     *
     * @param consumer map initialization logic
     * @param <K> key type
     * @param <V> value type
     * @return the initialized mutable map
     */
    public static <K, V> Map<K, V> mapOf(Consumer<? super Map<K, V>> consumer) {
        Map<K, V> map = new HashMap<>();
        consumer.accept(map);
        return map;
    }

    /**
     * Creates an empty mutable map. Wraps in {@link HashMap} to ensure safe modification during
     * configuration reload.
     *
     * <p>빈 가변 맵을 생성하여 반환한다. 설정 리로드 시 안전하게 수정할 수 있도록 {@link HashMap}으로 래핑한다.
     *
     * @param <K> key type
     * @param <V> value type
     * @return an empty mutable map
     */
    public static <K, V> Map<K, V> mapOf() {
        return new HashMap<>();
    }

    /**
     * Creates a mutable map containing a single entry.
     *
     * <p>단일 엔트리를 포함하는 가변 맵을 생성하여 반환한다.
     */
    public static <K, V> Map<K, V> mapOf(K k1, V v1) {
        return new HashMap<>(Map.of(k1, v1));
    }

    /**
     * Creates a mutable map containing two entries.
     *
     * <p>두 엔트리를 포함하는 가변 맵을 생성하여 반환한다.
     */
    public static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2) {
        return new HashMap<>(Map.of(k1, v1, k2, v2));
    }

    /**
     * Creates a mutable map containing three entries.
     *
     * <p>세 엔트리를 포함하는 가변 맵을 생성하여 반환한다.
     */
    public static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2, K k3, V v3) {
        return new HashMap<>(Map.of(k1, v1, k2, v2, k3, v3));
    }

    /**
     * Creates a mutable map containing four entries.
     *
     * <p>네 엔트리를 포함하는 가변 맵을 생성하여 반환한다.
     */
    public static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
        return new HashMap<>(Map.of(k1, v1, k2, v2, k3, v3, k4, v4));
    }

    /**
     * Creates a mutable map containing five entries.
     *
     * <p>다섯 엔트리를 포함하는 가변 맵을 생성하여 반환한다.
     */
    public static <K, V> Map<K, V> mapOf(
            K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
        return new HashMap<>(Map.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5));
    }
}
