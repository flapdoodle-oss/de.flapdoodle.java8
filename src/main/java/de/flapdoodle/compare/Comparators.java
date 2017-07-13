package de.flapdoodle.compare;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.Function;

public class Comparators {
	
	public static <S,D> ComparatorMatcher<S> matcher(Function<S, Optional<D>> mapper, Comparator<D> comparator) {
		return (left,right) -> {
			return mapper.apply(left)
					.flatMap(l -> mapper.apply(right)
							.map(r -> () -> comparator.compare(l, r)));
		};
	}
}
