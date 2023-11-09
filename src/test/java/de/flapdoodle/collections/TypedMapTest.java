package de.flapdoodle.collections;

import de.flapdoodle.reflection.TypeInfo;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TypedMapTest {

	@Nested
	class Immutable {
		@Test
		void addEntryToEmptyMap() {
			ImmutableTypedMap<String> testee = TypedMap.<String>empty()
				.add(TypeInfo.of(Double.class), "foo", 123.0);

			assertThat(testee.get(TypeInfo.of(Double.class), "foo"))
				.isEqualTo(123.0);
		}

		@Test
		void keyCollision() {
			assertThatThrownBy(() -> {
				TypedMap.<String>empty()
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