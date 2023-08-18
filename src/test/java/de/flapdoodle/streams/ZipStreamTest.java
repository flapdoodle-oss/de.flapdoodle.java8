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
package de.flapdoodle.streams;

import de.flapdoodle.types.Pair;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Spliterator;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ZipStreamTest {

	@Test
	public void checkDelegates() {
		Supplier<Stream<Pair<String, Integer>>> testee = () -> Streams.zipStreamOf(
			Stream.of("A", "B", "C", "D").sorted().parallel(),
			Stream.of(1, 2, 3).distinct(),
			Pair::of
		);

		assertThat(testee.get()
			.spliterator().estimateSize())
			.isEqualTo(3);
		
		assertThat(testee.get()
			.spliterator().trySplit())
			.isNull();

		assertThat(testee.get()
			.spliterator().hasCharacteristics(Spliterator.DISTINCT)).isFalse();

		assertThat(testee.get()
			.spliterator().hasCharacteristics(Spliterator.SORTED)).isFalse();

		assertThat(testee.get()
			.spliterator().characteristics()).isEqualTo(Spliterator.ORDERED);

	}

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