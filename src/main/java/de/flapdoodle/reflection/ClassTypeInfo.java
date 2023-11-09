package de.flapdoodle.reflection;

import org.immutables.value.Value;

@Value.Immutable
abstract class ClassTypeInfo<T> implements TypeInfo<T> {
	@Value.Parameter
	public abstract Class<T> type();

	@Override
	public T cast(Object instance) {
		return type().cast(instance);
	}

	@Override
	public boolean isInstance(Object instance) {
		return type().isInstance(instance);
	}

	static <T> ClassTypeInfo<T> of(Class<T> type) {
		return ImmutableClassTypeInfo.of(type);
	}
}
