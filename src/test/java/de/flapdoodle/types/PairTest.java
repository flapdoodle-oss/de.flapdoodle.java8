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