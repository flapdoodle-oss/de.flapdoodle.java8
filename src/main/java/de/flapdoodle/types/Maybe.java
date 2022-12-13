/**
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

import org.immutables.value.Value;

import java.util.function.Function;

public abstract class Maybe<T> {
	public abstract boolean hasSome();
	@Nullable
	public abstract T get();
	public abstract <R> Maybe<R> map(Function<T, R> mapping);

	public T getOrThrow(IllegalArgumentException e) {
		if (!hasSome()) throw e;
		return get();
	}

	@Value.Immutable
	public static abstract class Some<T> extends Maybe<T> {
		@Value.Parameter
		protected abstract @Nullable T value();

		@Override
		@Value.Auxiliary
		public boolean hasSome() {
			return true;
		}

		@Override
		public T get() {
			return value();
		}

		@Override
		public <R> Maybe<R> map(Function<T, R> mapping) {
			return Maybe.some(mapping.apply(value()));
		}
	}

	@Value.Immutable(singleton = true)
	public static abstract class None<T> extends Maybe<T> {

		@Override
		@Value.Auxiliary
		public boolean hasSome() {
			return false;
		}

		@Override
		public T get() {
			throw new IllegalArgumentException("is none");
		}

		@Override
		public <R> Maybe<R> map(Function<T, R> mapping) {
			return Maybe.none();
		}
	}

	public static <T> None<T> none() {
		return ImmutableNone.of();
	}

	public static <T> Some<T> some(@Nullable T value) {
		return ImmutableSome.of(value);
	}
}
