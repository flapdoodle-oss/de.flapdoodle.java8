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

public abstract class Try {

	public static <T, R, E extends Exception> ThrowingFunction<T, R, E> function(ThrowingFunction<T, R, E> function) {
		return function;
	}
	
	public static <T, E extends Exception> ThrowingSupplier<T, E> supplier(ThrowingSupplier<T, E> supplier) {
		return supplier;
	}
	
	public static <T, E extends Exception> ThrowingConsumer<T, E> consumer(ThrowingConsumer<T, E> consumer) {
		return consumer;
	}

	public static <E extends Exception> ThrowingRunable<E> runable(ThrowingRunable<E> runable) {
		return runable;
	}

	public static <T, R> R apply(ThrowingFunction<T, R, ? extends Exception> function, T value) {
		return function.mapCheckedException(RuntimeException::new).apply(value);
	}
	
	public static <T> T get(ThrowingSupplier<T, ? extends Exception> supplier) {
		return supplier.mapCheckedException(RuntimeException::new).get();
	}
	
	public static <T> void accept(ThrowingConsumer<T, ? extends Exception> consumer, T value) {
		consumer.mapCheckedException(RuntimeException::new).accept(value);
	}
	
	public static void run(ThrowingRunable<? extends Exception> runable) {
		runable.mapCheckedException(RuntimeException::new).run();
	}
}
