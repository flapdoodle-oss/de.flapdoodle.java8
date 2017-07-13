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
