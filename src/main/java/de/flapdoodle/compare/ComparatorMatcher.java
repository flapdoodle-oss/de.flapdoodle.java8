package de.flapdoodle.compare;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface ComparatorMatcher<T> {
	
	Optional<Match<T>> match(T left, T right);
	
	default Combined<T> or(ComparatorMatcher<T> other) {
		return () -> Arrays.asList(this,other);
	}
	
	interface Match<T> {
		int compare();
	}

	interface Combined<T> {
		Collection<ComparatorMatcher<T>> all();
		
		default Combined<T> or(ComparatorMatcher<T> other) {
			return () -> Stream.concat(all().stream(), Stream.of(other))
					.collect(Collectors.toList());
		}
		
		default Comparator<T> asComparator() {
			return (left, right) -> {
				Optional<Match<T>> match = all().stream()
					.map(m -> m.match(left, right))
					.filter(Optional::isPresent)
					.map(Optional::get)
					.findFirst();
				if (match.isPresent()) {
					return match.get().compare();
				}
				
				return all().stream()
					.map(m -> m.match(left, left).isPresent() 
							? OptionalInt.of(-1) 
							: m.match(right, right).isPresent() 
								? OptionalInt.of(1) : OptionalInt.empty())
					.filter(OptionalInt::isPresent)
					.map(OptionalInt::getAsInt)
					.findFirst()
					.orElse(0);
			};
		}
	}
	
}
