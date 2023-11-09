package de.flapdoodle.reflection;

import de.flapdoodle.checks.Preconditions;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
public abstract class ListTypeInfo<T> implements TypeInfo<List<T>> {
	@Value.Parameter
	public abstract TypeInfo<T> elements();

	@Override
	public List<T> cast(Object instance) {
		Preconditions.checkArgument(isInstance(instance), "type mismatch: %s is not a %s", instance, this);
		return (List<T>) instance;
	}

	@Override
	public boolean isInstance(Object instance) {
		return instance instanceof List && ((List<?>) instance).stream().allMatch(elements()::isInstance);
	}

	static <T> ListTypeInfo<T> of(TypeInfo<T> type) {
		return ImmutableListTypeInfo.of(type);
	}
}
