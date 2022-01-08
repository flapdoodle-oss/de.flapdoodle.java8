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

import de.flapdoodle.checks.Preconditions;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public abstract class Optionals {

	private Optionals() {
		// no instance
	}
	
	public static <S,D, E extends Exception> Optional<D> map(Optional<S> source, ThrowingFunction<? super S, ? extends D, E> mapper) throws E {
		return source.isPresent()
				? Optional.of(mapper.apply(source.get()))
				: Optional.empty();
	}

	public static <D, E extends Exception> D orElseGet(Optional<D> current, ThrowingSupplier<? extends D, E> supplier) throws E {
		return current.isPresent()
				? current.get()
				: supplier.get();
	}

	public static <T> Stream<T> streamOf(Optional<T> src) {
		return src.isPresent() ? Stream.of(src.get()) : Stream.empty();
	}

	public static <T> Wrapper<T> with(Optional<T> wrapped) {
		return new Wrapper<>(wrapped);
	}

	public final static class Wrapper<T> {

		private final Optional<T> wrapped;

		public Wrapper(Optional<T> src) {
			this.wrapped = Preconditions.checkNotNull(src,"src is null");
		}

		public T get() {
			return wrapped.get();
		}

		public boolean isPresent() {
			return wrapped.isPresent();
		}

		public <E extends Exception> Wrapper<T> ifPresent(ThrowingConsumer<? super T, E> consumer) throws E {
			if (wrapped.isPresent()) {
				consumer.accept(wrapped.get());
			}
			return this;
		}

		public <E extends Exception> Wrapper<T> ifAbsent(ThrowingRunnable<E> runable) throws E {
			if (!wrapped.isPresent()) {
				runable.run();
			}
			return this;
		}

		public Wrapper<T> filter(Predicate<? super T> predicate) {
			return with(wrapped.filter(predicate));
		}

		public <U, E extends Exception> Wrapper<U> map(ThrowingFunction<? super T, ? extends U, E> mapper) throws E {
			return with(Optionals.map(wrapped, mapper));
		}

		public <U, E extends Exception> Wrapper<U> flatMap(ThrowingFunction<? super T, Optional<U>, E> mapper) throws E {
			Optional<Optional<U>> mapped = Optionals.map(wrapped, mapper);
			return with(mapped.isPresent() ? mapped.get() : Optional.empty());
		}

		public T orElse(T other) {
			return wrapped.orElse(other);
		}

		public <E extends Exception> T orElseGet(ThrowingSupplier<? extends T, E> supplier) throws E {
			return Optionals.orElseGet(wrapped, supplier);
		}

		public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
			return wrapped.orElseThrow(exceptionSupplier);
		}

		public Stream<T> stream() {
			return streamOf(wrapped);
		}

		@Override public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Wrapper<?> wrapper = (Wrapper<?>) o;
			return wrapped.equals(wrapper.wrapped);
		}

		@Override public int hashCode() {
			return wrapped.hashCode();
		}
		@Override
		public String toString() {
			return "Wrapped("+wrapped.toString()+")";
		}


	}
}
