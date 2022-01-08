package de.flapdoodle.streams;

import de.flapdoodle.types.Pair;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ZipStreamTest {

	@Test
	public void mergeTwoStreamsOfSameLenght() {
		List<Pair<String, Integer>> result = Streams.zipStreamOf(Stream.of("A", "B", "C"), Stream.of(1, 2, 3), Pair::of)
			.collect(Collectors.toList());

		assertThat(result)
			.containsExactly(Pair.of("A", 1), Pair.of("B", 2), Pair.of("C", 3));
	}

	@Test
	public void mergeTwoStreamsOfDifferntLenght() {
		List<Pair<String, Integer>> result = Streams.zipStreamOf(Stream.of("A", "B", "C", "D"), Stream.of(1, 2, 3), Pair::of)
			.collect(Collectors.toList());

		assertThat(result)
			.containsExactly(Pair.of("A", 1), Pair.of("B", 2), Pair.of("C", 3));
	}
}