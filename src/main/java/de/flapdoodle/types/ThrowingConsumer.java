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

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface ThrowingConsumer<T, E extends Exception> {
	void accept(T value) throws E;

	default <N extends Exception> ThrowingConsumer<T, N> mapException(Function<Exception, N> exceptionMapper) {
		return value -> {
			try {
				this.accept(value);
			} catch (Exception e) {
				if (e instanceof RuntimeException) {
					throw (RuntimeException) e;
				}
				throw exceptionMapper.apply(e);
			}
		};
	}

	/**
	 * @see ThrowingConsumer#mapException(Function)
	 */
	@Deprecated
	default <N extends Exception> ThrowingConsumer<T, N> mapCheckedException(Function<Exception, N> exceptionMapper) {
		return mapException(exceptionMapper);
	}

	default Consumer<T> mapToUncheckedException(Function<Exception, RuntimeException> exceptionMapper) {
		return mapException(exceptionMapper)::accept;
	}

	default ThrowingConsumer<T, E> andFinally(Runnable runnable) {
		return value -> {
			try {
				this.accept(value);
			} finally {
				runnable.run();
			}
		};
	}

	default Consumer<T> onCheckedException(BiConsumer<Exception,T > exceptionToFallback) {
		return value -> {
			try {
				this.accept(value);
			}
			catch (Exception e) {
				if (e instanceof RuntimeException) {
					throw (RuntimeException) e;
				}
				exceptionToFallback.accept(e, value);
			}
		};
	}
}
