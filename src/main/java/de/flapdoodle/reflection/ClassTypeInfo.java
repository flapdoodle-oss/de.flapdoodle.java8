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

import org.immutables.value.Value;

@Value.Immutable
public abstract class ClassTypeInfo<T> implements TypeInfo<T> {
	@Value.Parameter
	public abstract Class<T> type();

	@Override
	public T cast(Object instance) {
		return type().cast(instance);
	}

	@Override
	public boolean isInstance(Object instance) {
		return type().isInstance(instance);
	}

	@Override
	public boolean isAssignable(TypeInfo<?> other) {
		return other instanceof ClassTypeInfo && type().isAssignableFrom(((ClassTypeInfo<?>) other).type());
	}
	static <T> ClassTypeInfo<T> of(Class<T> type) {
		return ImmutableClassTypeInfo.of(type);
	}
}
