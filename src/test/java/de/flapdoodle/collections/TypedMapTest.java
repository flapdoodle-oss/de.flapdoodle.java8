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
package de.flapdoodle.collections;

import de.flapdoodle.reflection.TypeInfo;
import de.flapdoodle.types.Pair;
import org.assertj.core.util.Maps;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TypedMapTest {

	@Test
	void immutableToMutableAndBack() {
		ImmutableTypedMap<String> testee = TypedMap.<String>immutable()
			.add(TypeInfo.of(String.class), "foo", "bar")
			.add(Pair.typeInfo(String.class, Double.class),"bar", Pair.of("x", 2.0));

		MutableTypedMap<String> mutable = testee.asMutable();

		assertThat(mutable.keySet())
			.containsExactlyElementsOf(testee.keySet());

		TypedMap<String> copy = mutable.asImmutable();

		assertThat(copy).isEqualTo(testee);
		assertThat(copy.keySet())
			.containsExactlyElementsOf(testee.keySet());
	}

	@Nested
	class Immutable {
		@Test
		void addEntryToEmptyMap() {
			ImmutableTypedMap<String> testee = TypedMap.<String>immutable()
				.add(TypeInfo.of(Double.class), "foo", 123.0);

			assertThat(testee.get(TypeInfo.of(Double.class), "foo"))
				.isEqualTo(123.0);
		}

		@Test
		void addAllEntriesToEmptyMap() {
			ImmutableTypedMap<String> testee = TypedMap.<String>immutable()
				.add(TypeInfo.of(Double.class), "foo", 123.0)
				.addAll(TypeInfo.of(Integer.class), Maps.newHashMap("foo", 123));

			assertThat(testee.get(TypeInfo.of(Double.class), "foo"))
				.isEqualTo(123.0);
			assertThat(testee.get(TypeInfo.of(Integer.class), "foo"))
				.isEqualTo(123);
		}

		@Test
		void keyCollision() {
			assertThatThrownBy(() -> {
				TypedMap.<String>immutable()
					.add(TypeInfo.of(Double.class), "foo", 123.0)
					.add(TypeInfo.of(Double.class), "foo", 12.0);
			}).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("already set to 123.0");

			assertThatThrownBy(() -> {
				Map<String, Double> map = new LinkedHashMap<>();
				map.put("foo", 12.0);

				ImmutableTypedMap.<String>empty()
					.add(TypeInfo.of(Double.class), "foo", 123.0)
					.addAll(TypeInfo.of(Double.class), map);
			}).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("already set to 123.0");
		}
	}

	@Nested
	class Mutable {
		@Test
		void addEntryToEmptyMap() {
			MutableTypedMap<String> testee = TypedMap.<String>mutable();
			Double old = testee.put(TypeInfo.of(Double.class), "foo", 123.0);

			assertThat(old).isNull();
			assertThat(testee.get(TypeInfo.of(Double.class), "foo"))
				.isEqualTo(123.0);
		}

		@Test
		void keyCollision() {
			MutableTypedMap<String> testee = TypedMap.<String>mutable();

			testee.put(TypeInfo.of(Double.class), "foo", 123.0);
			assertThat(testee.put(TypeInfo.of(Double.class), "foo", 12.0)).isEqualTo(123.0);
		}
	}

}