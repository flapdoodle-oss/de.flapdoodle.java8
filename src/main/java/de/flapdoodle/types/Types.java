package de.flapdoodle.types;

import java.util.Optional;
import java.util.function.Function;

public class Types {

	public static <T> Optional<T> ifInstance(Object value, Class<T> type) {
		if (type.isInstance(value)) {
			return Optional.of((T) value);
		}
		return Optional.empty();
	}
	
	public static <S,D> Function<S, Optional<D>> ifInstance(Class<D> type) {
		return (s) -> ifInstance(s, type);
	}
}
