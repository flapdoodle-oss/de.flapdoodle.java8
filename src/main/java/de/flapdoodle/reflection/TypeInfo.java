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

import java.util.List;

public interface TypeInfo<T> {
	@Value.Auxiliary
	T cast(Object instance);

	@Value.Auxiliary
	boolean isInstance(Object instance);

	static <T> TypeInfo<T> of(Class<T> type) {
		return ClassTypeInfo.of(type);
	}

	static <T> TypeInfo<List<T>> listOf(TypeInfo<T> type) {
		return ListTypeInfo.of(type);
	}
}
