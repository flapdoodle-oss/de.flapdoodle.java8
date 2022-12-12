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
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface ThrowingFunction<T,R,E extends Exception>  {
	
	R apply(T t) throws E;
	
	default <N extends Exception> ThrowingFunction<T, R, N> mapException(Function<Exception, N> exceptionMapper) {
		return value -> {
			try {
				return this.apply(value);
			} catch (Exception e) {
				if (e instanceof RuntimeException) {
					throw (RuntimeException) e;
				}
				throw exceptionMapper.apply(e);
			}
		};
	}

	default Function<T, R> mapToUncheckedException(Function<Exception, RuntimeException> exceptionMapper) {
		return mapException(exceptionMapper)::apply;
	}

	default ThrowingFunction<T, R, E> andFinally(Runnable runnable) {
		return value -> {
			try {
				return this.apply(value);
			} finally {
				runnable.run();
			}
		};
	}

	default Function<T, R> fallbackTo(BiFunction<Exception,T , R> exceptionToFallback) {
		return value -> {
			try {
				return this.apply(value);
			}
			catch (Exception e) {
				if (e instanceof RuntimeException) {
					throw (RuntimeException) e;
				}
				return exceptionToFallback.apply(e, value);
			}
		};
	}

	default Function<T, Optional<R>> onCheckedException(BiConsumer<Exception, T> onException) {
		return value -> {
			try {
				return Optional.of(this.apply(value));
			}
			catch (Exception e) {
				if (e instanceof RuntimeException) {
					throw (RuntimeException) e;
				}
				onException.accept(e, value);
				return Optional.empty();
			}
		};
	}
}
