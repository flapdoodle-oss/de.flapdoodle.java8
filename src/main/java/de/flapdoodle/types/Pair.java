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

	public static <FIRST, SECOND> Pair<FIRST,SECOND> of(FIRST first, SECOND second) {
		return ImmutablePair.of(first, second);
	}
}
