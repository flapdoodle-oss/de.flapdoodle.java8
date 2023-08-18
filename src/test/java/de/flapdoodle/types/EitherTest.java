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

import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.*;

class EitherTest {
	@Test
	void eitherLeft() {
		Either<String, Integer> result = Either.left("x");

		assertThat(result.isLeft()).isTrue();
		assertThat(result.left()).isEqualTo("x");
		assertThatThrownBy(result::right).isInstanceOf(NoSuchElementException.class);
	}

	@Test
	void eitherLeftWithNull() {
		Either<String, Integer> result = Either.left(null);

		assertThat(result.isLeft()).isTrue();
		assertThat(result.left()).isNull();
		assertThatThrownBy(result::right).isInstanceOf(NoSuchElementException.class);
	}

	@Test
	void eitherRight() {
		Either<String, Integer> result = Either.right(2);

		assertThat(result.isLeft()).isFalse();
		assertThat(result.right()).isEqualTo(2);
		assertThatThrownBy(result::left).isInstanceOf(NoSuchElementException.class);
	}

	@Test
	void eitherRightWithNull() {
		Either<String, Integer> result = Either.right(null);

		assertThat(result.isLeft()).isFalse();
		assertThat(result.right()).isNull();
		assertThatThrownBy(result::left).isInstanceOf(NoSuchElementException.class);
	}

	@Test
	void mapEitherLeft() {
		Either<String, Integer> result = Either.<String, Integer>left("x")
			.mapLeft(it -> "left "+it)
			.mapRight(it -> it + 2);

		assertThat(result.isLeft()).isTrue();
		assertThat(result.left()).isEqualTo("left x");
		assertThatThrownBy(result::right).isInstanceOf(NoSuchElementException.class);
	}

	@Test
	void mapEitherRight() {
		Either<String, Integer> result = Either.<String, Integer>right(2)
			.mapLeft(it -> "left "+it)
			.mapRight(it -> it + 2);

		assertThat(result.isLeft()).isFalse();
		assertThat(result.right()).isEqualTo(4);
		assertThatThrownBy(result::left).isInstanceOf(NoSuchElementException.class);
	}

	@Test
	void mapMustExtractOneValue() {
		String result = Either.<Integer, Integer>left(2)
			.map(String::valueOf, String::valueOf);

		assertThat(result).isEqualTo("2");
	}
}