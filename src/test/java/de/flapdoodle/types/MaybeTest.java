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