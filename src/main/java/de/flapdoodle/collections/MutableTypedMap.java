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
