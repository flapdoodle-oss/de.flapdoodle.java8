package de.flapdoodle.types;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PairTest {

	@Test
	public void nullIsNotAllowed() {
		Assertions.assertThatThrownBy(() -> Pair.of(null,"")).isInstanceOf(NullPointerException.class);
		Assertions.assertThatThrownBy(() -> Pair.of(null,null)).isInstanceOf(NullPointerException.class);
		Assertions.assertThatThrownBy(() -> Pair.of("",null)).isInstanceOf(NullPointerException.class);
	}

	@Test
	public void pairMustMatchSourceValues() {
		Pair<Integer, String> testee = Pair.of(1, "second");
		assertThat(testee.first()).isEqualTo(1);
		assertThat(testee.second()).isEqualTo("second");
	}

	@Test
	public void mapMustMapValues() {
		Pair<Integer, String> testee = Pair.of(1, "second")
			.mapFirst(it -> it + 1)
			.mapSecond(it -> "<"+it+">");
		
		assertThat(testee.first()).isEqualTo(2);
		assertThat(testee.second()).isEqualTo("<second>");
	}
}