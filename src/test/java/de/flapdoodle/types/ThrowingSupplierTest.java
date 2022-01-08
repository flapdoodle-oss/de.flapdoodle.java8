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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class ThrowingSupplierTest {

	@Test
	public void throwsExpectedExeption() {
		CountingThrowingSupplier<String, IOException> supplier = countCalls(ThrowingSupplierTest::supplierThrowingIO);

		assertThatThrownBy(Try.supplier(supplier)::get)
			.isInstanceOf(IOException.class);

		assertThat(supplier.numberOfCalls()).isEqualTo(1);
	}

	@Test
	public void doNotThrowExeption() throws IOException {
		CountingThrowingSupplier<String, IOException> supplier = countCalls(ThrowingSupplierTest::supplierCouldThrowIO);
		
		assertThat(Try.supplier(supplier).get()).isEqualTo("ok");
		assertThat(supplier.numberOfCalls()).isEqualTo(1);
	}

	@Test
	public void doNotThrowWithMappedExeption() {
		CountingThrowingSupplier<String, IOException> supplier = countCalls(ThrowingSupplierTest::supplierCouldThrowIO);

		assertThat(Try.supplier(supplier)
			.mapCheckedException(RuntimeException::new)
			.get()).isEqualTo("ok");
		
		assertThat(supplier.numberOfCalls()).isEqualTo(1);
	}
	
	@Test
	public void mapExeption() {
		CountingThrowingSupplier<String, IOException> supplier = countCalls(ThrowingSupplierTest::supplierThrowingIO);

		assertThatThrownBy(Try.supplier(supplier)
			.mapCheckedException(IllegalArgumentException::new)
			::get)
			.isInstanceOf(IllegalArgumentException.class);

		assertThat(supplier.numberOfCalls()).isEqualTo(1);
	}
	
	@Test
	public void mapAsRuntimeExeption() {
		CountingThrowingSupplier<String, IOException> supplier = countCalls(ThrowingSupplierTest::supplierThrowingIO);

		assertThatThrownBy(Try.supplier(supplier)
			.mapCheckedException(RuntimeException::new)
			::get)
			.isInstanceOf(RuntimeException.class);

		assertThat(supplier.numberOfCalls()).isEqualTo(1);
	}
	
	@Test
	public void dontRemapRuntimeExeption() {
		CountingThrowingSupplier<String, IOException> supplier = countCalls(ThrowingSupplierTest::supplierCouldThrowIOButThrowsRuntime);

		assertThatThrownBy(Try.supplier(supplier)
			.mapCheckedException(IllegalArgumentException::new)
			::get)
			.isInstanceOf(CustomRuntimeException.class);

		assertThat(supplier.numberOfCalls()).isEqualTo(1);
	}

	@Test
	public void doNotThrowWithMappedExeptionAndFallback() {
		CountingThrowingSupplier<String, IOException> supplier = countCalls(ThrowingSupplierTest::supplierCouldThrowIO);

		assertThat(Try.supplier(supplier)
			.mapCheckedException(RuntimeException::new)
			.onCheckedException(ex -> "fallback")
			.get()).isEqualTo("ok");

		assertThat(supplier.numberOfCalls()).isEqualTo(1);
	}

	@Test
	public void dontCallOnCheckedExceptionIfNoError() {
		CountingThrowingSupplier<String, IOException> supplier = countCalls(ThrowingSupplierTest::supplierCouldThrowIO);

		assertThat(Try.supplier(supplier)
			.onCheckedException(ex -> {
				if (true) throw new RuntimeException("not called");
				return "";
			})
			.get())
			.isEqualTo("ok");

		assertThat(supplier.numberOfCalls()).isEqualTo(1);
	}

	@Test
	public void mapExceptionToFallback() {
		CountingThrowingSupplier<String, IOException> supplier = countCalls(ThrowingSupplierTest::supplierThrowingIO);

		assertThat(Try.supplier(supplier)
			.onCheckedException(ex -> "fallback")
			.get())
			.isEqualTo("fallback");

		assertThat(supplier.numberOfCalls()).isEqualTo(1);
	}

	@Test
	public void dontCallOnCheckedExceptionConsumerIfNoError() {
		CountingThrowingSupplier<String, IOException> supplier = countCalls(ThrowingSupplierTest::supplierCouldThrowIO);

		assertThat(Try.supplier(supplier)
			.onCheckedException(ex -> {
				if (true) throw new RuntimeException("not called");
			})
			.get())
			.contains("ok");

		assertThat(supplier.numberOfCalls()).isEqualTo(1);
	}

	@Test
	public void dontCallOnCheckedExceptionIfExceptionIsRuntime() {
		CountingThrowingSupplier<String, IOException> supplier = countCalls(ThrowingSupplierTest::supplierThrowingIO);
		AtomicReference<Exception> onCheckedException=new AtomicReference<>();

		assertThatThrownBy(Try.supplier(supplier)
			.mapCheckedException(RuntimeException::new)
			.onCheckedException(onCheckedException::set)
			::get)
			.isInstanceOf(RuntimeException.class);

		assertThat(supplier.numberOfCalls()).isEqualTo(1);
	}

	@Test
	public void supplierReturnsEmptyIfExceptionIsThrown() {
		CountingThrowingSupplier<String, IOException> supplier = countCalls(ThrowingSupplierTest::supplierThrowingIO);
		AtomicReference<Exception> onCheckedException=new AtomicReference<>();

		assertThat(Try.supplier(supplier)
			.onCheckedException(onCheckedException::set)
			.get())
			.isEmpty();

		assertThat(supplier.numberOfCalls()).isEqualTo(1);
		assertThat(onCheckedException.get()).isInstanceOf(IOException.class);
	}
	@Test
	public void doesNotMapExceptionToFallbackBecauseOfRuntimeException() {
		CountingThrowingSupplier<String, IOException> supplier = countCalls(ThrowingSupplierTest::supplierCouldThrowIOButThrowsRuntime);

		assertThatThrownBy(Try.supplier(supplier)
			.mapCheckedException(IllegalArgumentException::new)
			.onCheckedException(ex -> "fallback")
			::get)
			.isInstanceOf(CustomRuntimeException.class);

		assertThat(supplier.numberOfCalls()).isEqualTo(1);
	}

	@Test
	public void callFinallyIfThrowsExeption() {
		AtomicInteger finallyCounter = new AtomicInteger();

		CountingThrowingSupplier<String, IOException> supplier = countCalls(ThrowingSupplierTest::supplierThrowingIO);

		assertThatThrownBy(Try.supplier(supplier)
			.andFinally(finallyCounter::incrementAndGet)::get)
			.isInstanceOf(IOException.class);

		assertThat(supplier.numberOfCalls()).isEqualTo(1);
		assertThat(finallyCounter.get()).isEqualTo(1);
	}

	@Test
	public void callFinallyIfNoExeption() throws IOException {
		AtomicInteger finallyCounter = new AtomicInteger();

		CountingThrowingSupplier<String, IOException> supplier = countCalls(ThrowingSupplierTest::supplierCouldThrowIO);

		assertThat(Try.supplier(supplier)
			.andFinally(finallyCounter::incrementAndGet)
			.get()).isEqualTo("ok");

		assertThat(supplier.numberOfCalls()).isEqualTo(1);
		assertThat(finallyCounter.get()).isEqualTo(1);
	}

	@Test
	public void callAllFinally() throws IOException {
		AtomicInteger finallyCounter = new AtomicInteger();

		CountingThrowingSupplier<String, IOException> supplier = countCalls(ThrowingSupplierTest::supplierCouldThrowIO);

		assertThat(Try.supplier(supplier)
			.andFinally(finallyCounter::incrementAndGet)
			.andFinally(finallyCounter::incrementAndGet)
			.get()).isEqualTo("ok");

		assertThat(supplier.numberOfCalls()).isEqualTo(1);
		assertThat(finallyCounter.get()).isEqualTo(2);
	}

	private static String supplierThrowingIO() throws IOException {
		throw new IOException("should fail");
	}

	private static String supplierCouldThrowIO() throws IOException {
		if (false) {
			throw new IOException("should fail");
		}
		return "ok";
	}
	
	private static String supplierCouldThrowIOButThrowsRuntime() throws IOException {
		if (false) {
			throw new IOException("should fail");
		}
		throw new CustomRuntimeException();
	}

	private static <T, E extends Exception> CountingThrowingSupplier<T, E> countCalls(ThrowingSupplier<T, E> delegate) {
		AtomicInteger counter = new AtomicInteger();
		return new CountingThrowingSupplier<T, E>() {
			@Override public int numberOfCalls() {
				return counter.get();
			}

			@Override
			public T get() throws E {
				counter.incrementAndGet();
				return delegate.get();
			}
		};
	}

	interface CountingThrowingSupplier<T, E extends Exception> extends ThrowingSupplier<T, E> {
		int numberOfCalls();
	}

}
