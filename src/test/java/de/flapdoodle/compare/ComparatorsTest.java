package de.flapdoodle.compare;

import static org.junit.Assert.assertEquals;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import de.flapdoodle.types.Types;

public class ComparatorsTest {

	@Test
	public void simpleTest() {
		Comparator<Object> comparator = Comparators.matcher(Types.ifInstance(String.class), String::compareTo)
			.or(Comparators.matcher(Types.ifInstance(Integer.class), Integer::compareTo))
			.or(Comparators.matcher(Types.ifInstance(Long.class), Long::compareTo))
			.asComparator();
		
		String result = Stream.of(0L, 0,"foo",12,"bar",1,12L,3.0)
			.sorted(comparator)
			.map(v -> v.toString()+"("+v.getClass().getSimpleName()+")")
			.collect(Collectors.joining(", "));
		
		assertEquals("bar(String), foo(String), 0(Integer), 1(Integer), 12(Integer), 0(Long), 12(Long), 3.0(Double)", result);
	}

}
