package de.flapdoodle.reflection;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ListTypeInfoTest {

	@Test
	void typeInfoMustCompareNestedTypes() {
		TypeInfo<List<String>> listOfStrings = TypeInfo.listOf(TypeInfo.of(String.class));

		assertThat(listOfStrings.isInstance(Arrays.asList("foo", "bar")))
			.isTrue();

		assertThat(listOfStrings.isInstance(Arrays.asList("foo", 2)))
			.isFalse();
	}

}