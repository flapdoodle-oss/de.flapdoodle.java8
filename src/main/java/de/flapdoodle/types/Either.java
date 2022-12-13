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
import org.immutables.value.Value.Auxiliary;

import java.util.NoSuchElementException;
import java.util.function.Function;

public abstract class Either<L, R> {

	public abstract boolean isLeft();

	@Nullable
	public abstract L left();

	@Nullable
	public abstract R right();

	@Value.Immutable
	static abstract class Left<L, R> extends Either<L, R> {

		@Override
		@Value.Parameter
		@Nullable
		public abstract L left();

		@Auxiliary
		@Override
		public R right() {
			throw new NoSuchElementException("is left");
		}
		@Override
		public boolean isLeft() {
			return true;
		}
	}

	@Value.Immutable
	static abstract class Right<L, R> extends Either<L, R> {

		@Override
		@Value.Parameter
		@Nullable
		public abstract R right();

		@Auxiliary
		@Override
		public L left() {
			throw new NoSuchElementException("is right");
		}
		@Override
		public boolean isLeft() {
			return false;
		}
	}

	public <T> Either<T, R> mapLeft(Function<L, T> transformation) {
		return isLeft()
			? left(transformation.apply(left()))
			: (Either<T, R>) this;
	}

	public <T> Either<L, T> mapRight(Function<R, T> transformation) {
		return isLeft()
			? (Either<L, T>) this
			: right(transformation.apply(right()));
	}

	public <T> T map(Function<L, T> leftTransformation, Function<R, T> rightTransformation) {
		Either<T, T> mapped = mapLeft(leftTransformation).mapRight(rightTransformation);
		return mapped.isLeft() ? mapped.left() : mapped.right();
	}

	public static <L, R> Either<L, R> left(L left) {
		return ImmutableLeft.<L, R>of(left);
	}

	public static <L, R> Either<L, R> right(R right) {
		return ImmutableRight.<L, R>of(right);
	}
}
