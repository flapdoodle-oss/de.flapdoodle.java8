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
package de.flapdoodle.compare;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.Function;

public class Comparators {
	
	public static <S,D> Comparator<S> matching(Function<S, Optional<D>> mapper, Comparator<D> comparator) {
		return (left, right) -> {
			Optional<D> matchLeft = mapper.apply(left);
			Optional<D> matchRight = mapper.apply(right);
			if (matchLeft.isPresent() && matchRight.isPresent()) {
				return comparator.compare(matchLeft.get(), matchRight.get());
			}
			if (matchLeft.isPresent()) {
				return -1;
			}
			if (matchRight.isPresent()) {
				return 1;
			}
			return 0;
		};
	}
}
