package de.flapdoodle.collections;

import de.flapdoodle.checks.Preconditions;
import de.flapdoodle.reflection.TypeInfo;
import de.flapdoodle.types.Pair;
import org.immutables.value.Value;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class MutableTypedMap<K> implements TypedMap<K> {
	private final Map<Pair<K, ? extends TypeInfo<?>>, Object> map;

	protected MutableTypedMap() {
		this.map = new LinkedHashMap<>();
	}

	protected MutableTypedMap(Map<Pair<K, ? extends TypeInfo<?>>, Object> map) {
		this.map = new LinkedHashMap<>(map);
	}

	@Value.Auxiliary
	@Override
	public <T> T get(TypeInfo<T> type, K key) {
		return (T) map.get(Pair.of(key, type));
	}

	@Override
	public Set<Pair<K, ? extends TypeInfo<?>>> keySet() {
		return map.keySet();
	}

	@Value.Auxiliary
	public <T> T put(TypeInfo<T> type, K key, T value) {
		Preconditions.checkNotNull(value, "value is null");

		Pair<K, TypeInfo<T>> mapKey = Pair.of(key, type);
		return (T) map.put(mapKey, value);
	}

	public <T> void putAll(TypeInfo<T> type, Map<K, T> src) {
		src.forEach((key, value) -> put(type, key, value));
	}

	public TypedMap<K> asImmutable() {
		return GeneratedImmutableTypedMap.<K>builder()
			.putAllMap(map)
			.build();
	}

	public static <K> MutableTypedMap<K> empty() {
		return new MutableTypedMap<>();
	}
}
