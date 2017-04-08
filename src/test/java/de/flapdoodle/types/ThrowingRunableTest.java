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

import java.io.IOException;

import org.junit.Test;

public class ThrowingRunableTest {

	@Test(expected = IOException.class)
	public void throwsExpectedExeption() throws IOException {
		Try.runable(ThrowingRunableTest::runableThrowingIO).run();
	}

	@Test
	public void doNotThrowExeption() throws IOException {
		Try.runable(ThrowingRunableTest::runableCouldThrowIO).run();
	}

	@Test
	public void doNotThrowWithMappedExeption() throws IOException {
		Try.runable(ThrowingRunableTest::runableCouldThrowIO)
			.mapCheckedException(RuntimeException::new)
			.run();
	}

	@Test(expected = IllegalArgumentException.class)
	public void mapExeption() {
		Try.runable(ThrowingRunableTest::runableThrowingIO)
			.mapCheckedException(IllegalArgumentException::new)
			.run();
	}
	
	@Test(expected = RuntimeException.class)
	public void mapAsRuntimeExeption() {
		Try.runable(ThrowingRunableTest::runableThrowingIO)
			.mapCheckedException(RuntimeException::new)
			.run();
	}
	
	@Test(expected = CustomRuntimeException.class)
	public void dontRemapRuntimeExeption() {
		Try.runable(ThrowingRunableTest::runableCouldThrowIOButThrowsRuntime)
			.mapCheckedException(IllegalArgumentException::new)
			.run();
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

}
