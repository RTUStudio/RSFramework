package kr.rtuserver.framework.bukkit.api.configuration.serializer.collection.map;

import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.AnnotatedType;
import java.util.*;

/**
 * Serializer for Map<Object[], Object>.
 * <p>
 * This class flattens nested YAML/Configuration structures into
 * path arrays like {["a", "b", 1] = "value"} and restores them back.
 * The key order is preserved using LinkedHashMap, so the original
 * YAML order will not be lost.
 * <p>
 * (Map<Object[], Object> 직렬화 클래스.
 * YAML/Configurate 구조를 {["a", "b", 1] = "value"} 식으로 평탄화하고,
 * 다시 복원할 수 있도록 한다.
 * 또한 LinkedHashMap을 사용하여 키 순서를 보존하기 때문에
 * YAML에 적힌 원래 순서가 흐트러지지 않는다.)
 */
public class FlattenedMapSerializer implements TypeSerializer.Annotated<Map<Object[], Object>> {

    public static final TypeToken<Map<Object[], Object>> TYPE = new TypeToken<Map<Object[], Object>>() {
    };
    private static final Logger LOGGER = LoggerFactory.getLogger(FlattenedMapSerializer.class);

    private final boolean clearInvalids;

    public FlattenedMapSerializer(final boolean clearInvalids) {
        this.clearInvalids = clearInvalids;
    }

    @Override
    public Map<Object[], Object> deserialize(final AnnotatedType annotatedType, final ConfigurationNode node) throws SerializationException {
        final Map<Object[], Object> result = new LinkedHashMap<>();
        if (!node.isMap()) {
            return result;
            // if the node is not a map, just return empty (맵이 아니면 그냥 빈 값 반환)
        }

        // iterate children with their order preserved (자식 노드 순서 그대로 순회)
        for (Map.Entry<Object, ? extends ConfigurationNode> entry : new LinkedHashMap<>(node.childrenMap()).entrySet()) {
            Object rawKey = entry.getKey();
            ConfigurationNode child = entry.getValue();
            this.collectFlattened(result, new ArrayList<>(List.of(rawKey)), child);
        }
        return result;
    }

    private void collectFlattened(Map<Object[], Object> result, List<Object> path, ConfigurationNode node) {
        if (node.isMap()) {
            // if this node has children, keep going deeper (노드가 맵이면 자식들로 재귀 탐색)
            for (Map.Entry<Object, ? extends ConfigurationNode> entry : new LinkedHashMap<>(node.childrenMap()).entrySet()) {
                List<Object> newPath = new ArrayList<>(path);
                newPath.add(entry.getKey());
                collectFlattened(result, newPath, entry.getValue());
            }
        } else {
            // leaf node → store into the map (리프 노드 → 결과 맵에 넣기)
            result.put(path.toArray(), node.raw());
        }
    }

    @Override
    public void serialize(final AnnotatedType annotatedType, @Nullable final Map<Object[], Object> obj, final ConfigurationNode node) throws SerializationException {
        if (obj == null || obj.isEmpty()) {
            node.set(Collections.emptyMap());
            return;
            // nothing to save → clear (저장할 게 없으면 그냥 비움)
        }

        if (clearInvalids) {
            node.raw(new LinkedHashMap<>());
            // reset node with LinkedHashMap to enforce order (순서 보장 위해 노드 초기화)
        }

        // write each path-value back into node (경로-값을 다시 노드에 기록)
        for (Map.Entry<Object[], Object> entry : obj.entrySet()) {
            Object[] path = entry.getKey();
            Object value = entry.getValue();

            ConfigurationNode current = node;
            for (int i = 0; i < path.length; i++) {
                Object segment = path[i];
                if (i == path.length - 1) {
                    // last element → assign value (마지막 경로면 값 저장)
                    current.node(segment).set(value);
                } else {
                    // intermediate node → must also be LinkedHashMap (중간 노드도 LinkedHashMap으로 유지)
                    ConfigurationNode child = current.node(segment);
                    if (!child.isMap()) {
                        child.raw(new LinkedHashMap<>());
                    }
                    current = child;
                }
            }
        }
    }

    @Override
    public @Nullable Map<Object[], Object> emptyValue(final AnnotatedType specificType, final ConfigurationOptions options) {
        // return empty LinkedHashMap as default (기본값은 빈 LinkedHashMap)
        return new LinkedHashMap<>();
    }
}
