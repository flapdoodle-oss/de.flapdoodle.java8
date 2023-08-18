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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ThrowingRunnableTest {

	@Test
	public void throwsExpectedExeption() {
		CountingThrowingRunable<IOException> runable = countCalls(ThrowingRunnableTest::runableThrowingIO);

		Assertions.assertThatThrownBy(Try.runable(runable)::run)
			.isInstanceOf(IOException.class);

		assertThat(runable.numberOfCalls()).isEqualTo(1);
	}

	@Test
	public void doNotThrowExeption() throws IOException {
		CountingThrowingRunable<IOException> runable = countCalls(ThrowingRunnableTest::runableCouldThrowIO);

		Try.runable(runable).run();

		assertThat(runable.numberOfCalls()).isEqualTo(1);
	}

	@Test
	public void doNotThrowWithMappedExeption() {
		CountingThrowingRunable<IOException> runable = countCalls(ThrowingRunnableTest::runableCouldThrowIO);

		Try.runable(runable)
			.mapException(RuntimeException::new)
			.run();

		assertThat(runable.numberOfCalls()).isEqualTo(1);
	}

	@Test
	public void mapExeption() {
		CountingThrowingRunable<IOException> runable = countCalls(ThrowingRunnableTest::runableThrowingIO);

		assertThatThrownBy(Try.runable(runable)
			.mapException(IllegalArgumentException::new)
			::run)
			.isInstanceOf(IllegalArgumentException.class);

		assertThat(runable.numberOfCalls()).isEqualTo(1);
	}

	@Test
	public void mapAsRuntimeExeption() {
		CountingThrowingRunable<IOException> runable = countCalls(ThrowingRunnableTest::runableThrowingIO);

		assertThatThrownBy(Try.runable(runable)
			.mapException(RuntimeException::new)
			::run)
			.isInstanceOf(RuntimeException.class);

		assertThat(runable.numberOfCalls()).isEqualTo(1);
	}

	@Test
	public void dontRemapRuntimeExeption() {
		CountingThrowingRunable<IOException> runable = countCalls(ThrowingRunnableTest::runableCouldThrowIOButThrowsRuntime);

		assertThatThrownBy(Try.runable(runable)
			.mapException(IllegalArgumentException::new)
			::run)
			.isInstanceOf(CustomRuntimeException.class);

		assertThat(runable.numberOfCalls()).isEqualTo(1);
	}

	@Test
	public void mapToUnchecked() {
		CountingThrowingRunable<IOException> runable = countCalls(ThrowingRunnableTest::runableThrowingIO);

		assertThatThrownBy(Try.runable(runable)
			.mapToUncheckedException(RuntimeException::new)
			::run)
			.isInstanceOf(RuntimeException.class);

		assertThat(runable.numberOfCalls()).isEqualTo(1);
	}

	@Test
	public void mapToUncheckedDontRemapRuntimeExeption() {
		CountingThrowingRunable<IOException> runable = countCalls(ThrowingRunnableTest::runableCouldThrowIOButThrowsRuntime);

		assertThatThrownBy(Try.runable(runable)
			.mapToUncheckedException(IllegalArgumentException::new)
			::run)
			.isInstanceOf(CustomRuntimeException.class);

		assertThat(runable.numberOfCalls()).isEqualTo(1);
	}

	@Test
	public void callOnFinallyWithoutException() throws IOException {
		AtomicInteger finallyCounter = new AtomicInteger();

		CountingThrowingRunable<IOException> runable = countCalls(ThrowingRunnableTest::runableCouldThrowIO);

		Try.runable(runable)
			.andFinally(finallyCounter::incrementAndGet)
			.run();

		assertThat(runable.numberOfCalls()).isEqualTo(1);
		assertThat(finallyCounter.get()).isEqualTo(1);
	}

	@Test
	public void callOnFinallyWithException() {
		AtomicInteger finallyCounter = new AtomicInteger();

		CountingThrowingRunable<IOException> runable = countCalls(ThrowingRunnableTest::runableThrowingIO);

		assertThatThrownBy(Try.runable(runable)
			.andFinally(finallyCounter::incrementAndGet)
			::run)
			.isInstanceOf(IOException.class);

		assertThat(runable.numberOfCalls()).isEqualTo(1);
		assertThat(finallyCounter.get()).isEqualTo(1);
	}

	@Test
	public void callBothOnFinallyWithException() {
		AtomicInteger finallyCounter = new AtomicInteger();

		CountingThrowingRunable<IOException> runable = countCalls(ThrowingRunnableTest::runableThrowingIO);

		assertThatThrownBy(Try.runable(runable)
			.andFinally(finallyCounter::incrementAndGet)
			.andFinally(finallyCounter::incrementAndGet)
			::run)
			.isInstanceOf(IOException.class);

		assertThat(runable.numberOfCalls()).isEqualTo(1);
		assertThat(finallyCounter.get()).isEqualTo(2);
	}

	protected static void runableThrowingIO() throws IOException {
		throw new IOException("should fail");
	}

	protected static void runableCouldThrowIO() throws IOException {
		if (false) {
			throw new IOException("should fail");
		}
	}

	protected static void runableCouldThrowIOButThrowsRuntime() throws IOException {
		if (false) {
			throw new IOException("should fail");
		}
		throw new CustomRuntimeException();
	}

	private static <E extends Exception> CountingThrowingRunable<E> countCalls(ThrowingRunnable<E> delegate) {
		AtomicInteger counter = new AtomicInteger();
		return new CountingThrowingRunable<E>() {
			@Override public int numberOfCalls() {
				return counter.get();
			}

			@Override public void run() throws E {
				counter.incrementAndGet();
				delegate.run();
			}
		};
	}

	interface CountingThrowingRunable<E extends Exception> extends ThrowingRunnable<E> {
		int numberOfCalls();
	}
}
