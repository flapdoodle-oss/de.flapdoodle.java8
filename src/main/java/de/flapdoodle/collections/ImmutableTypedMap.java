/*
 * Copyright (C) 2016
 *   Michael Mosmann <michael@mosmann.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.flapdoodle.collections;

import de.flapdoodle.checks.Preconditions;
import de.flapdoodle.reflection.TypeInfo;
import de.flapdoodle.types.Pair;
import org.immutables.value.Value;

import java.util.Map;
import java.util.Set;

@Value.Immutable
@Value.Style(typeImmutable = "Generated*")
public abstract class ImmutableTypedMap<K> implements TypedMap<K> {
	protected abstract Map<Pair<K, ? extends TypeInfo<?>>, Object> map();

	@Override
	@Value.Auxiliary
	public <T> T get(TypeInfo<T> type, K key) {
		return (T) map().get(Pair.of(key, type));
	}

	@Override
	@Value.Lazy
	public Set<Pair<K, ? extends TypeInfo<?>>> keySet() {
		return map().keySet();
	}

	@Value.Auxiliary
	public MutableTypedMap<K> asMutable() {
		return new MutableTypedMap<>(map());
	}

	@Value.Auxiliary
	public <T> ImmutableTypedMap<K> add(TypeInfo<T> type, K key, T value) {
		Preconditions.checkNotNull(value, "value is null");

		Pair<K, TypeInfo<T>> mapKey = Pair.of(key, type);
		Preconditions.checkArgument(!map().containsKey(mapKey), "key %s:%s already set to %s", key, type, Preconditions.lazy(() -> map().get(mapKey)));

		return GeneratedImmutableTypedMap.<K>builder()
			.from(this)
			.putMap(mapKey, value)
			.build();
	}

	@Value.Auxiliary
	public <T> ImmutableTypedMap<K> addAll(TypeInfo<T> type, Map<K, T> map) {
		GeneratedImmutableTypedMap.Builder<K> builder = GeneratedImmutableTypedMap.<K>builder()
			.from(this);

		map.forEach((key, value) -> {
			Preconditions.checkNotNull(value, "value is null");

			Pair<K, TypeInfo<T>> mapKey = Pair.of(key, type);
			Preconditions.checkArgument(!map().containsKey(mapKey), "key %s:%s already set to %s", key, type, Preconditions.lazy(() -> map().get(mapKey)));
			builder.putMap(mapKey, value);
		});

		return builder.build();
	}

	public static <K> ImmutableTypedMap<K> empty() {
		return GeneratedImmutableTypedMap.<K>builder().build();
	}

	public static <K, T> ImmutableTypedMap<K> of(TypeInfo<T> type, K key, T value) {
		return ImmutableTypedMap.<K>empty()
			.add(type, key, value);
	}

	public static <K, T> ImmutableTypedMap<K> of(TypeInfo<T> type, Map<K, T> map) {
		return ImmutableTypedMap.<K>empty()
			.addAll(type, map);
	}

	public static <K> ImmutableTypedMap<K> copyOf(TypedMap<K> src) {
		if (src instanceof ImmutableTypedMap) {
			return (ImmutableTypedMap<K>) src;
		}

		GeneratedImmutableTypedMap.Builder<K> builder = GeneratedImmutableTypedMap.builder();
		src.keySet().forEach(key -> builder.putMap(key, src.get(key.second(), key.first())));
		return builder.build();
	}
}
