package de.flapdoodle.types;

import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EitherTest {
	@Test
	void eitherLeft() {
		Either<String, Integer> result = Either.left("x");

		assertThat(result.isLeft()).isTrue();
		assertThat(result.left()).isEqualTo("x");
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

}