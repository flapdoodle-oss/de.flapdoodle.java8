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
}