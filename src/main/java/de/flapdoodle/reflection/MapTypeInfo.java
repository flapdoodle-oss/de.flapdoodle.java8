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
package de.flapdoodle.reflection;

import de.flapdoodle.checks.Preconditions;
import org.immutables.value.Value;

import java.util.List;
import java.util.Map;

@Value.Immutable
public abstract class MapTypeInfo<K, V> implements TypeInfo<Map<K, V>> {
	@Value.Parameter
	public abstract TypeInfo<K> key();

	@Value.Parameter
	public abstract TypeInfo<V> value();

	@Override
	public Map<K, V> cast(Object instance) {
		Preconditions.checkArgument(isInstance(instance), "type mismatch: %s is not a %s", instance, this);
		return (Map<K, V>) instance;
	}

	@Override
	public boolean isInstance(Object instance) {
		return instance instanceof Map && ((Map<?, ?>) instance).entrySet().stream()
			.allMatch(entry -> key().isInstance(entry.getKey()) && value().isInstance(entry.getValue()));
	}

	@Override
	public boolean isAssignable(TypeInfo<?> other) {
		return other instanceof MapTypeInfo
			&& key().isAssignable(((MapTypeInfo<?, ?>) other).key())
			&& value().isAssignable(((MapTypeInfo<?, ?>) other).value());
	}
	static <K, V> MapTypeInfo<K, V> of(TypeInfo<K> key, TypeInfo<V> value) {
		return ImmutableMapTypeInfo.of(key, value);
	}
}
