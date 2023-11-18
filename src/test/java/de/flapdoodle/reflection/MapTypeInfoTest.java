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