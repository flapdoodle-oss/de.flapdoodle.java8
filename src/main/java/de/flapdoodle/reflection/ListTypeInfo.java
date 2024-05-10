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

@Value.Immutable
public abstract class ListTypeInfo<T> implements TypeInfo<List<T>> {
	@Value.Parameter
	public abstract TypeInfo<T> elements();

	@Override
	public List<T> cast(Object instance) {
		Preconditions.checkArgument(isInstance(instance), "type mismatch: %s is not a %s", instance, this);
		return (List<T>) instance;
	}

	@Override
	public boolean isInstance(Object instance) {
		return instance instanceof List && ((List<?>) instance).stream().allMatch(elements()::isInstance);
	}

	@Override
	public boolean isAssignable(TypeInfo<?> other) {
		return other instanceof ListTypeInfo && elements().isAssignable(((ListTypeInfo<?>) other).elements());
	}
	
	static <T> ListTypeInfo<T> of(TypeInfo<T> type) {
		return ImmutableListTypeInfo.of(type);
	}
}
