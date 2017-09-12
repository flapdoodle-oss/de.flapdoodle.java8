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
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

public class OptionalsTest {

	@Test
	public void mapOptionalWithThrowingFunction() throws IOException {
		String asString = Optionals.with(Optional.of(2))
			.map(i -> asString(i))
			.get();
		assertEquals("2", asString);
	}

	@Test
	public void mapOptionalWithNormalFunction() {
		String asString = Optionals.with(Optional.of(2))
			.map(i -> ""+i)
			.get();
		assertEquals("2", asString);
	}

	@Test
	public void callConsumerIfPresent() {
		AtomicReference<Integer> ref=new AtomicReference<Integer>();
		Optionals.with(Optional.of(2))
			.ifPresent(ref::set)
			.ifAbsent(() -> {throw new RuntimeException();});

		assertEquals(Integer.valueOf(2), ref.get());
	}

	@Test
	public void callRunableIfAbsent() {
		AtomicReference<Integer> ref=new AtomicReference<Integer>();
		Optionals.with(Optional.empty())
			.ifPresent(x -> {throw new RuntimeException();})
			.ifAbsent(() -> ref.set((42)));

		assertEquals(Integer.valueOf(42), ref.get());
	}

	private String asString(Integer integer) throws IOException {
		return ""+integer;
	}
}
