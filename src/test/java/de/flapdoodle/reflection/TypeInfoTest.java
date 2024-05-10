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
package de.flapdoodle.reflection;

import de.flapdoodle.types.Pair;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TypeInfoTest {

	@Test
	void typeInfoMustCompareNestedTypes() {
		assertThat(TypeInfo.of(String.class))
			.isEqualTo(TypeInfo.of(String.class));

		assertThat(Pair.typeInfo(String.class, Integer.class))
			.isEqualTo(Pair.typeInfo(String.class, Integer.class));

		assertThat(Pair.typeInfo(String.class, Integer.class))
			.isNotEqualTo(Pair.typeInfo(String.class, Double.class));
	}

	@Test
	void castIfTypMatchesShouldSucceed() {
		TypeInfo<Pair<String, Integer>> testee = Pair.typeInfo(String.class, Integer.class);

		assertThat(testee.isInstance(Pair.of("foo", 123))).isTrue();
		assertThat(testee.isInstance(Pair.of("foo", 123.0))).isFalse();

		Pair<String, Integer> casted = testee.cast((Object) Pair.of("foo", 123));
		assertThat(casted).isNotNull();
	}

	@Test
	void checkWrappedInstance() {
		TypeInfo<List<Pair<String, Integer>>> testee = TypeInfo.listOf(Pair.typeInfo(String.class, Integer.class));

		assertThat(testee.isInstance(Arrays.asList(
			Pair.of("foo", 123),
			Pair.of("bar", 123.0)
		))).isFalse();

		assertThat(testee.isInstance(Arrays.asList(
			Pair.of("foo", 123),
			Pair.of("bar", 12)
		))).isTrue();
	}

	@Test
	void checkAssignable() {
		TypeInfo<List<Pair<String, Integer>>> testee = TypeInfo.listOf(Pair.typeInfo(String.class, Integer.class));
		TypeInfo<List<Pair<String, Object>>> other = TypeInfo.listOf(Pair.typeInfo(String.class, Object.class));

		assertThat(testee.isAssignable(other)).isFalse();
		assertThat(other.isAssignable(testee)).isTrue();
	}
}