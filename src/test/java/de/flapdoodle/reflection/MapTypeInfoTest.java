package de.flapdoodle.reflection;

import org.assertj.core.util.Maps;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MapTypeInfoTest {

	@Test
	void typeInfoMustCompareNestedTypes() {
		TypeInfo<Map<String, Integer>> testee = TypeInfo.mapOf(
			TypeInfo.of(String.class),
			TypeInfo.of(Integer.class)
		);

		assertThat(testee.isInstance(Maps.newHashMap("foo", 2)))
			.isTrue();

		assertThat(testee.isInstance(Maps.newHashMap("foo", 2.0)))
			.isFalse();
	}

}