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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicReference;

public class TryTest {

	@Test
	public void compilerWillChoseMatchingSuperExceptionType() throws A {
		assertThat(Try.supplier(TryTest::canThrowAaAndAb).get()).isEqualTo("aa+ab");
	}

	@Test
	public void compilerWillChoseExceptionIfNoMatchingSuperType() throws Exception {
		assertThat(Try.supplier(TryTest::canThrowsAaAndB).get()).isEqualTo("aa+b");
	}

	@Test
	public void getOnThrowingSupplierMustProvideValue() {
		assertThat(Try.get(TryTest::canThrowAaAndAb)).isEqualTo("aa+ab");
	}

	@Test
	public void applyMustCallDelegate() {
		assertThat(Try.<String, String>apply(it -> it+" called","function")).isEqualTo("function called");
	}

	@Test
	public void acceptMustCallDelegate() {
		AtomicReference<String> callValue=new AtomicReference<>();
		Try.accept(it -> callValue.set(it),"consumer");
		assertThat(callValue.get()).isEqualTo("consumer");
	}

	@Test
	public void runMustCallDelegate() {
		AtomicReference<String> callValue=new AtomicReference<>();
		Try.run(() -> callValue.set("runnable"));
		assertThat(callValue.get()).isEqualTo("runnable");
	}

	private static String canThrowAaAndAb() throws AA, AB {
		return "aa+ab";
	}
	
	private static String canThrowsAaAndB() throws AA, B {
		return "aa+b";
	}
	
	static class A extends Exception {
		
	}
	
	static class AA extends A {
		
	}
	
	static class AB extends A {
		
	}
	
	static class B extends Exception {
		
	}
}
