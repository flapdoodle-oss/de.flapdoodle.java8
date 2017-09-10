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

import static org.junit.Assert.assertEquals;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import de.flapdoodle.types.Types;

public class ComparatorsTest {

	@Test
	public void simpleSample() {
		Comparator<Object> comparator = Comparators.matching(Types.ifInstance(String.class), String::compareTo)
			.thenComparing(Comparators.matching(Types.ifInstance(Integer.class), Integer::compareTo))
			.thenComparing(Comparators.matching(Types.ifInstance(Long.class), Long::compareTo));
		
		String result = Stream.of(0L, 0,"foo",12,"bar",1,12L,3.0)
			.sorted(comparator)
			.map(v -> v.toString()+"("+v.getClass().getSimpleName()+")")
			.collect(Collectors.joining(", "));
		
		assertEquals("bar(String), foo(String), 0(Integer), 1(Integer), 12(Integer), 0(Long), 12(Long), 3.0(Double)", result);
	}

	@Test
	public void advanceSample() {
		Comparator<String> comparator = Comparators.matching(matchNumber("(?<number>[0-9]+) m", "number"), Integer::compareTo)
			.thenComparing(Comparators.matching(matchNumber("(?<number>[0-9]+) L", "number"), Integer::compareTo))
			.thenComparing(Comparators.matching(matchRegex("(?<all>[0-9]+)", "all"), String::compareTo));
		
		String result = Stream.of("1","2","20 m","100 m","14 L","3 L")
			.sorted(comparator)
			.collect(Collectors.joining(", "));
		
		assertEquals("20 m, 100 m, 3 L, 14 L, 1, 2", result);
	}

	public static <S,OA,OE> Function<S, Optional<OE>> onResult(Function<S, Optional<OA>> src, Function<OA, OE> mapper) {
		return src.andThen(os -> os.map(mapper));
	}
	
	private static Function<String, Optional<Integer>> matchNumber(String regex, String groupName) {
		return onResult(matchRegex(regex, groupName), Integer::valueOf);
	}
	
	private static Function<String, Optional<String>> matchRegex(String regex, String groupName) {
		return matchRegex(Pattern.compile(regex), groupName);
	}
	
	private static Function<String, Optional<String>> matchRegex(Pattern pattern,String groupName) {
		return s -> {
			Matcher matcher = pattern.matcher(s);
			return matcher.matches() 
					? Optional.of(matcher.group(groupName)) 
					: Optional.empty();
		};
	}
}
