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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Before;
import org.junit.Test;

public class ThrowingConsumerTest {

	private final AtomicReference<String> consumerCalledWith=new AtomicReference();
	
	@Before
	public void cleanConsumerCalledWithReference() {
		consumerCalledWith.set(null);
	}
	
	@Test(expected = IOException.class)
	public void throwsExpectedExeption() throws IOException {
		Try.consumer(this::consumerThrowingIO).accept("fail");
	}

	@Test
	public void doNotThrowExeption() throws IOException {
		Try.consumer(this::consumerThrowingIO).accept("ok");
		assertEquals("ok", consumerCalledWith.get());
	}

	@Test
	public void doNotThrowWithMappedExeption() {
		Try.consumer(this::consumerThrowingIO)
				.mapCheckedException(RuntimeException::new)
				.accept("ok");
		assertEquals("ok", consumerCalledWith.get());
	}
	
	@Test
	public void doNotThrowWithFallback() {
		Try.consumer(this::consumerThrowingIO)
				.onCheckedException((ex,v) -> { consumerCalledWith.set("fallback "+v);})
				.accept("ok");
		assertEquals("ok", consumerCalledWith.get());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void mapExeption() {
		Try.consumer(this::consumerThrowingIO)
			.mapCheckedException(IllegalArgumentException::new)
			.accept("fail");
	}
	
	@Test(expected = RuntimeException.class)
	public void mapAsRuntimeExeption() {
		Try.consumer(this::consumerThrowingIO)
			.mapCheckedException(RuntimeException::new)
			.accept("fail");
	}
	
	@Test(expected = CustomRuntimeException.class)
	public void dontRemapRuntimeExeption() {
		Try.consumer(this::consumerCouldThrowIOButThrowsRuntime)
			.mapCheckedException(IllegalArgumentException::new)
			.accept("noop");
	}
	
	@Test
	public void mapExceptionToFallback() {
		Try.consumer(this::consumerThrowingIO)
			.onCheckedException((ex,v) -> { consumerCalledWith.set("fallback "+v);})
			.accept("fail");
		assertEquals("fallback fail", consumerCalledWith.get());
	}
	
	@Test(expected = CustomRuntimeException.class)
	public void doesNotMapExceptionToFallbackBecauseOfRuntimeException() {
		Try.consumer(this::consumerCouldThrowIOButThrowsRuntime)
			.mapCheckedException(IllegalArgumentException::new)
			.onCheckedException((ex,v) -> {})
			.accept("noop");
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
