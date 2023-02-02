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

import static org.assertj.core.api.Assertions.*;

class MaybeTest {
	@Test
	void none() {
		Maybe<String> testee = Maybe.none();

		assertThat(testee.hasSome()).isFalse();
		assertThatThrownBy(testee::get)
			.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> testee.getOrThrow(new IllegalArgumentException("custom")))
			.hasMessage("custom");
	}

	@Test
	void mapNoneToNone() {
		Maybe<String> testee = Maybe.none();
		Maybe<String> mapped = testee.map(s -> fail("must not be called"));
		assertThat(mapped.hasSome()).isFalse();
	}

	@Test
	void some() {
		Maybe<String> testee = Maybe.some("value");

		assertThat(testee.hasSome()).isTrue();
		assertThat(testee.get())
			.isEqualTo("value");
		assertThat(testee.getOrThrow(new IllegalArgumentException("custom")))
			.isEqualTo("value");
	}

	@Test
	void someIsNullable() {
		Maybe<String> testee = Maybe.some(null);

		assertThat(testee.hasSome()).isTrue();
		assertThat(testee.get())
			.isNull();
		assertThat(testee.getOrThrow(new IllegalArgumentException("custom")))
			.isNull();
	}

	@Test
	void mapSomeToSome() {
		Maybe<String> testee = Maybe.some("value");
		Maybe<String> mapped = testee.map(s -> "["+s+"]");
		assertThat(mapped.get()).isEqualTo("[value]");
	}

	@Test
	void mapSomeNull() {
		Maybe<String> testee = Maybe.some(null);
		Maybe<String> mapped = testee.map(s -> "["+s+"]");
		assertThat(mapped.get()).isEqualTo("[null]");
	}

}