package de.flapdoodle.collections;

import de.flapdoodle.reflection.TypeInfo;
import de.flapdoodle.types.Pair;
import org.immutables.value.Value;

import java.util.Set;

public interface TypedMap<K> {
	<T> T get(TypeInfo<T> type, K key);

	Set<Pair<K, ? extends TypeInfo<?>>> keySet();

	static <K> ImmutableTypedMap<K> empty() {
		return ImmutableTypedMap.empty();
	}

	static <K> MutableTypedMap<K> mutable() {
		return new MutableTypedMap<>();
	}
}
