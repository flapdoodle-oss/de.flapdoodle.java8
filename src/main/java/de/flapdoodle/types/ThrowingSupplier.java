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

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface ThrowingSupplier<T, E extends Exception> {
	T get() throws E;
	
	default <N extends Exception> ThrowingSupplier<T, N> mapCheckedException(Function<Exception, N> exceptionMapper) {
		return () -> {
			try {
				return this.get();
			} catch (Exception e) {
				if (e instanceof RuntimeException) {
					throw (RuntimeException) e;
				}
				throw exceptionMapper.apply(e);
			}
		};
	}

	default ThrowingSupplier<T, E> andFinally(Runnable runnable) {
		return () -> {
			try {
				return this.get();
			} finally {
				runnable.run();
			}
		};
	}

	default Supplier<T> fallbackTo(Function<Exception, T> exceptionToFallback) {
		return () -> {
			try {
				return this.get();
			}
			catch (Exception e) {
				if (e instanceof RuntimeException) {
					throw (RuntimeException) e;
				}
				return exceptionToFallback.apply(e);
			}
		};
	}

	default Supplier<Optional<T>> onCheckedException(Consumer<Exception> onException) {
		return () -> {
			try {
				return Optional.of(this.get());
			}
			catch (Exception e) {
				if (e instanceof RuntimeException) {
					throw (RuntimeException) e;
				}
				onException.accept(e);
				return Optional.empty();
			}
		};
	}
}
