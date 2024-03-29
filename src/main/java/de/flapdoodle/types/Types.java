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
package de.flapdoodle.types;

import java.util.Optional;
import java.util.function.Function;

public abstract class Types {

	/**
	 * see {@link de.flapdoodle.reflection.TypeInfo}
	 */
	@Deprecated
	public static <T> Optional<T> ifInstance(Object value, Class<T> type) {
		if (type.isInstance(value)) {
			return Optional.of(type.cast (value));
		}
		return Optional.empty();
	}

	/**
	 * see {@link de.flapdoodle.reflection.TypeInfo}
	 */
	@Deprecated
	public static <S,D> Function<S, Optional<D>> ifInstance(Class<D> type) {
		return (s) -> ifInstance(s, type);
	}
}
