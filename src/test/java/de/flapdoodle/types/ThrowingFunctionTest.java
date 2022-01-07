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
import java.util.concurrent.atomic.AtomicReference;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class ThrowingFunctionTest {
	private final AtomicReference<String> functionCalledWith=new AtomicReference<>();
	private final AtomicReference<String> finallyCalledWith=new AtomicReference<>();

	@Test
	public void throwsExpectedExeption() {
		assertThatThrownBy(() -> Try.function(this::functionThrowingIO).apply("fail"))
			.isInstanceOf(IOException.class);

		assertThat(functionCalledWith.get()).isEqualTo("fail");
	}

	@Test
	public void doNotThrowExeption() throws IOException {
		assertThat(Try.function(this::functionThrowingIO).apply("ok")).isEqualTo("ok ok");

		assertThat(functionCalledWith.get()).isEqualTo("ok");
	}

	@Test
	public void doNotThrowWithMappedExeption() {
		assertThat(Try.function(this::functionThrowingIO)
				.mapCheckedException(RuntimeException::new)
				.apply("ok")).isEqualTo("ok ok");

		assertThat(functionCalledWith.get()).isEqualTo("ok");
	}
	
	@Test
	public void mapExeption() {
		assertThatThrownBy(() -> Try.function(this::functionThrowingIO)
			.mapCheckedException(IllegalArgumentException::new)
			.apply("fail"))
			.isInstanceOf(IllegalArgumentException.class);

		assertThat(functionCalledWith.get()).isEqualTo("fail");
	}
	
	@Test
	public void mapAsRuntimeExeption() {
		assertThatThrownBy(() -> Try.function(this::functionThrowingIO)
			.mapCheckedException(RuntimeException::new)
			.apply("fail"))
			.isInstanceOf(RuntimeException.class);

		assertThat(functionCalledWith.get()).isEqualTo("fail");
	}
	
	@Test
	public void dontRemapRuntimeExeption() {
		assertThatThrownBy(() -> Try.function(this::functionCouldThrowIOButThrowsRuntime)
			.mapCheckedException(IllegalArgumentException::new)
			.apply("noop"))
			.isInstanceOf(CustomRuntimeException.class);

		assertThat(functionCalledWith.get()).isEqualTo("noop");
	}

	@Test
	public void doNotThrowWithFallback() {
		assertThat(Try.function(this::functionThrowingIO)
			.onCheckedException((ex,v) -> "fallback "+v)
			.apply("ok")).isEqualTo("ok ok");

		assertThat(functionCalledWith.get()).isEqualTo("ok");
	}

	@Test
	public void mapExceptionToFallback() {
		assertThat(Try.function(this::functionThrowingIO)
			.onCheckedException((ex,v) -> "fallback "+v)
			.apply("fail"))
			.isEqualTo("fallback fail");

		assertThat(functionCalledWith.get()).isEqualTo("fail");
	}
	
	@Test
	public void doesNotMapExceptionToFallbackBecauseOfRuntimeException() {
		assertThatThrownBy(() -> Try.function(this::functionCouldThrowIOButThrowsRuntime)
			.mapCheckedException(IllegalArgumentException::new)
			.onCheckedException((ex,v) -> "fallback "+v)
			.apply("noop"))
			.isInstanceOf(CustomRuntimeException.class);
	}

	@Test
	public void doNotCallThrowWithOnCheckedException() {

		assertThat(Try.function(this::functionThrowingIO)
			.onCheckedException((ex,v) -> {})
			.apply("ok")).contains("ok ok");

		assertThat(functionCalledWith.get()).isEqualTo("ok");
	}

	@Test
	public void mapExceptionToOptionalEmpty() {
		assertThat(Try.function(this::functionThrowingIO)
			.onCheckedException((ex,v) -> {})
			.apply("fail"))
			.isEmpty();

		assertThat(functionCalledWith.get()).isEqualTo("fail");
	}

	@Test
	public void doesNotMapExceptionToOptionalEmptyBecauseOfRuntimeException() {
		assertThatThrownBy(() -> Try.function(this::functionCouldThrowIOButThrowsRuntime)
			.mapCheckedException(IllegalArgumentException::new)
			.onCheckedException((ex,v) -> {})
			.apply("noop"))
			.isInstanceOf(CustomRuntimeException.class);
	}

	@Test
	public void callFinallyIfThrowsExeption() {
		assertThatThrownBy(() -> Try.function(this::functionThrowingIO)
			.andFinally(() -> finallyCalledWith.set("finally"))
			.apply("fail"))
			.isInstanceOf(IOException.class);

		assertThat(functionCalledWith.get()).isEqualTo("fail");
		assertThat(finallyCalledWith.get()).isEqualTo("finally");
	}

	@Test
	public void callFinallyIfNoExeption() throws IOException {
		assertThat(Try.function(this::functionThrowingIO)
			.andFinally(() -> finallyCalledWith.set("finally"))
			.apply("ok")).isEqualTo("ok ok");

		assertThat(functionCalledWith.get()).isEqualTo("ok");
		assertThat(finallyCalledWith.get()).isEqualTo("finally");
	}

	@Test
	public void callAllFinallyIfNoExeption() throws IOException {
		assertThat(Try.function(this::functionThrowingIO)
			.andFinally(() -> finallyCalledWith.set("finally"))
			.andFinally(() -> finallyCalledWith.set("second finally"))
			.apply("ok")).isEqualTo("ok ok");

		assertThat(functionCalledWith.get()).isEqualTo("ok");
		assertThat(finallyCalledWith.get()).isEqualTo("second finally");
	}
	
	protected String functionThrowingIO(String src) throws IOException {
		functionCalledWith.set(src);
		if (src.equals("fail")) {
			throw new IOException("should fail");
		}
		return "ok "+src;
	}

	private String functionCouldThrowIOButThrowsRuntime(String src) throws IOException {
		functionCalledWith.set(src);
		if (false) {
			throw new IOException("should fail");
		}
		throw new CustomRuntimeException();
	}


}
