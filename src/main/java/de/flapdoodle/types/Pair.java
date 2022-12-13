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

import org.immutables.value.Value;

import java.util.function.Function;

@Value.Immutable
public abstract class Pair<FIRST, SECOND> {
	@Value.Parameter
	public abstract FIRST first();
	@Value.Parameter
	public abstract SECOND second();

	public <T> Pair<T, SECOND> mapFirst(Function<FIRST, T> transformation) {
		return of(transformation.apply(first()), second());
	}

	public <T> Pair<FIRST, T> mapSecond(Function<SECOND, T> transformation) {
		return of(first(), transformation.apply(second()));
	}

	public <FIRST_MAPPED,SECOND_MAPPED> Pair<FIRST_MAPPED, SECOND_MAPPED> map(
		Function<FIRST, FIRST_MAPPED> first,
		Function<SECOND, SECOND_MAPPED> second
	) {
		return mapFirst(first).mapSecond(second);
	}

	public static <FIRST, SECOND> Pair<FIRST,SECOND> of(FIRST first, SECOND second) {
		return ImmutablePair.of(first, second);
	}
}
