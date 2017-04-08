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
	Archimedes Trajano (trajano@github),
	Kevin D. Keck (kdkeck@github),
	Ben McCann (benmccann@github)
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
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public abstract class Optionals {

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
	
	public static <T> Wrapper<T> wrap(Optional<T> wrapped) {
		return new Wrapper<>(wrapped);
	}
	
	public static class Wrapper<T> {
		
		private final Optional<T> wrapped;

		public Wrapper(Optional<T> wrapped) {
			this.wrapped = wrapped;
		}

		public T get() {
			return wrapped.get();
		}

		public boolean isPresent() {
			return wrapped.isPresent();
		}

		public <E extends Exception> void ifPresent(Consumer<? super T> consumer) {
			wrapped.ifPresent(consumer);
		}
		
		public <E extends Exception> void ifPresent(ThrowingConsumer<? super T, E> consumer) throws E {
			if (wrapped.isPresent()) {
				consumer.accept(wrapped.get());
			}
		}

		public Wrapper<T> filter(Predicate<? super T> predicate) {
			return wrap(wrapped.filter(predicate));
		}

		public <U, E extends Exception> Wrapper<U> map(ThrowingFunction<? super T, ? extends U, E> mapper) throws E {
			return wrap(Optionals.map(wrapped, mapper));
		}

		public <U, E extends Exception> Wrapper<U> flatMap(ThrowingFunction<? super T, Optional<U>, E> mapper) throws E {
			Optional<Optional<U>> mapped = Optionals.map(wrapped, mapper);
			return wrap(mapped.isPresent() ? mapped.get() : Optional.empty());
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

		@Override
		public boolean equals(Object obj) {
			return obj.getClass() == this.getClass() && wrapped.equals(obj);
		}

		@Override
		public int hashCode() {
			return wrapped.hashCode();
		}

		@Override
		public String toString() {
			return "Wrapped("+wrapped.toString()+")";
		}
		
		
	}
}
