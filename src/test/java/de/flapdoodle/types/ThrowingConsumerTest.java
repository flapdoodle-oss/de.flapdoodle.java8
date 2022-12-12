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
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ThrowingConsumerTest {

	private final AtomicReference<String> consumerCalledWith=new AtomicReference<>();
	private final AtomicReference<String> finallyCalledWith=new AtomicReference<>();

	@BeforeEach
	public void cleanConsumerCalledWithReference() {
		consumerCalledWith.set(null);
	}
	
	@Test
	public void throwsExpectedExeption() {
		assertThatThrownBy(() -> Try.consumer(this::consumerThrowingIO).accept("fail"))
			.isInstanceOf(IOException.class);
		assertThat(consumerCalledWith.get()).isEqualTo("fail");
	}

	@Test
	public void doNotThrowExeption() throws IOException {
		Try.consumer(this::consumerThrowingIO).accept("ok");
		assertEquals("ok", consumerCalledWith.get());
	}

	@Test
	public void doNotThrowWithMappedExeption() {
		Try.consumer(this::consumerThrowingIO)
				.mapException(RuntimeException::new)
				.accept("ok");
		assertEquals("ok", consumerCalledWith.get());
	}
	
	@Test
	public void doNotThrowWithFallback() {
		Try.consumer(this::consumerThrowingIO)
				.onCheckedException((ex,v) -> consumerCalledWith.set("fallback "+v))
				.accept("ok");
		assertEquals("ok", consumerCalledWith.get());
	}
	
	@Test
	public void mapExeption() {
		assertThatThrownBy(() -> Try.consumer(this::consumerThrowingIO)
			.mapException(IllegalArgumentException::new)
			.accept("fail"))
			.isInstanceOf(IllegalArgumentException.class);

		assertEquals("fail", consumerCalledWith.get());
	}
	
	@Test
	public void mapAsRuntimeExeption() {
		assertThatThrownBy(() -> Try.consumer(this::consumerThrowingIO)
			.mapException(RuntimeException::new)
			.accept("fail"))
			.isInstanceOf(RuntimeException.class);

		assertEquals("fail", consumerCalledWith.get());
	}
	
	@Test
	public void dontRemapRuntimeExeption() {
		assertThatThrownBy(() -> Try.consumer(this::consumerCouldThrowIOButThrowsRuntime)
			.mapException(IllegalArgumentException::new)
			.accept("noop"))
			.isInstanceOf(CustomRuntimeException.class);

		assertEquals("noop", consumerCalledWith.get());
	}

	@Test
	public void mapToUnchecked() {
		assertThatThrownBy(() -> Try.consumer(this::consumerThrowingIO)
			.mapToUncheckedException(RuntimeException::new)
			.accept("fail"))
			.isInstanceOf(RuntimeException.class);

		assertEquals("fail", consumerCalledWith.get());
	}

	@Test
	public void mapToUncheckedDontRemapRuntimeExeption() {
		assertThatThrownBy(() -> Try.consumer(this::consumerCouldThrowIOButThrowsRuntime)
			.mapToUncheckedException(IllegalArgumentException::new)
			.accept("noop"))
			.isInstanceOf(CustomRuntimeException.class);

		assertEquals("noop", consumerCalledWith.get());
	}

	@Test
	public void mapExceptionToFallback() {
		Try.consumer(this::consumerThrowingIO)
			.onCheckedException((ex,v) -> consumerCalledWith.set("fallback "+v))
			.accept("fail");
		assertEquals("fallback fail", consumerCalledWith.get());
	}

	@Test
	public void runFinallyIfExeptionIsThrown() {
		assertThatThrownBy(() -> Try.consumer(this::consumerThrowingIO)
			.andFinally(() -> finallyCalledWith.set("finally"))
			.accept("fail"))
			.isInstanceOf(IOException.class);
		assertThat(consumerCalledWith.get()).isEqualTo("fail");
		assertThat(finallyCalledWith.get()).isEqualTo("finally");
	}

	@Test
	public void runFinallyIfNoExeption() throws IOException {
		Try.consumer(this::consumerThrowingIO)
			.andFinally(() -> finallyCalledWith.set("finally"))
			.accept("ok");
		assertEquals("ok", consumerCalledWith.get());
		assertThat(finallyCalledWith.get()).isEqualTo("finally");
	}

	@Test
	public void runBothFinallyIfNoExeption() throws IOException {
		Try.consumer(this::consumerThrowingIO)
			.andFinally(() -> finallyCalledWith.set("finally"))
			.andFinally(() -> finallyCalledWith.set("second finally"))
			.accept("ok");
		assertEquals("ok", consumerCalledWith.get());
		assertThat(finallyCalledWith.get()).isEqualTo("second finally");
	}

	@Test
	public void doesNotMapExceptionToFallbackBecauseOfRuntimeException() {
		assertThatThrownBy(() -> Try.consumer(this::consumerCouldThrowIOButThrowsRuntime)
			.mapException(IllegalArgumentException::new)
			.onCheckedException((ex,v) -> {})
			.accept("noop"))
			.isInstanceOf(CustomRuntimeException.class);

		assertEquals("noop", consumerCalledWith.get());
	}
	
	protected void consumerThrowingIO(String src) throws IOException {
		consumerCalledWith.set(src);
		if (src.equals("fail")) {
			throw new IOException("should fail");
		}
	}

	private void consumerCouldThrowIOButThrowsRuntime(String src) throws IOException {
		consumerCalledWith.set(src);
		if (false) {
			throw new IOException("should fail");
		}
		throw new CustomRuntimeException();
	}


}
