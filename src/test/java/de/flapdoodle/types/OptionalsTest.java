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

import org.junit.Test;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class OptionalsTest {

	@Test
	public void streamOfOptional() {
		assertThat(Optionals.streamOf(Optional.of("one")).collect(Collectors.toList()))
			.containsExactly("one");
		assertThat(Optionals.streamOf(Optional.empty()).collect(Collectors.toList()))
			.isEmpty();
	}

	@Test
	public void basicDelegatesMustWork() throws IOException {
		Optionals.Wrapper<Integer> testee = Optionals.with(Optional.of(2));
		AtomicReference<Integer> onIfPresent=new AtomicReference<>();
		AtomicBoolean onIfAbsent=new AtomicBoolean(false);

		assertThat(testee.isPresent()).isTrue();
		assertThat(testee.get()).isEqualTo(2);
		assertThat(testee.ifPresent(onIfPresent::set))
			.isEqualTo(testee);
		assertThat(testee.ifAbsent(() -> onIfAbsent.set(true)))
			.isEqualTo(testee);

		assertThat(onIfPresent.get()).isEqualTo(2);
		assertThat(onIfAbsent.get()).isFalse();

		assertThat(testee.orElse(4)).isEqualTo(2);
		assertThat(testee.orElseGet(() -> 4)).isEqualTo(2);

		assertThat(testee.orElseThrow(() -> new IOException("not thrown"))).isEqualTo(2);
		assertThat(testee.stream().collect(Collectors.toList())).containsExactly(2);

		assertThat(testee.toString()).isEqualTo("Wrapped(Optional[2])");
	}

	@Test
	public void equalsHashCodeMustWorkAsExpected() {
		Optionals.Wrapper<String> presentString = Optionals.with(Optional.of("one"));
		Optionals.Wrapper<String> samePresentString = Optionals.with(Optional.of("one"));
		Optionals.Wrapper<String> otherPresentString = Optionals.with(Optional.of("other"));

		Optionals.Wrapper<String> absentString = Optionals.with(Optional.empty());
		Optionals.Wrapper<String> otherAbsentString = Optionals.with(Optional.empty());

		assertThat(presentString).isEqualTo(presentString);
		assertThat(presentString.hashCode()).isEqualTo(presentString.hashCode());
		assertThat(presentString.hashCode()).isEqualTo(Optional.of("one").hashCode());

		assertThat(presentString).isEqualTo(samePresentString);
		assertThat(presentString.hashCode()).isEqualTo(samePresentString.hashCode());

		assertThat(presentString).isNotEqualTo(otherPresentString);
		assertThat(presentString).isNotEqualTo(absentString);

		assertThat(absentString).isEqualTo(absentString);
		assertThat(absentString.hashCode()).isEqualTo(absentString.hashCode());
		assertThat(absentString).isEqualTo(otherAbsentString);
		assertThat(absentString.hashCode()).isEqualTo(otherAbsentString.hashCode());

		assertThat(presentString).isNotEqualTo(Optional.of("one"));
		assertThat(presentString).isNotEqualTo(null);
	}

	@Test
	public void mapOptionalWithThrowingFunction() throws IOException {
		assertThat(Optionals.with(Optional.of(2))
			.map(i -> asString(i))
			.get()).isEqualTo("2");
	}

	@Test
	public void filterOptional() {
		assertThat(Optionals.with(Optional.of(2))
			.filter(it -> it == 2)
			.get()).isEqualTo(2);
	}

	@Test
	public void flapMap() throws IOException {
		assertThat(Optionals.with(Optional.of(2))
			.flatMap(it -> Optional.of(asString(it)))
			.get()).isEqualTo("2");
	}
	@Test
	public void mapEmptyWithThrowingFunction() throws IOException {
		assertThat(Optionals.with(Optional.<Integer>empty())
			.map(i -> asString(i)).isPresent()).isFalse();
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

	@Test
	public void fallBackIfEmpty() throws IOException {
		String result = Optionals.orElseGet(Optional.empty(), (ThrowingSupplier<String, IOException>) () -> "fallback");
		assertThat(result).isEqualTo("fallback");
	}

	@Test
	public void fontFallBackIfPresent() throws IOException {
		String result = Optionals.orElseGet(Optional.of("present"), (ThrowingSupplier<String, IOException>) () -> {
			throw new IOException("fail");
		});
		assertThat(result).isEqualTo("present");
	}

	private String asString(Integer integer) throws IOException {
		return ""+integer;
	}
}
