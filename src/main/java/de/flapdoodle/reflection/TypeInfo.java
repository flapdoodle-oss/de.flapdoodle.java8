package de.flapdoodle.reflection;

import org.immutables.value.Value;

import java.util.List;

public interface TypeInfo<T> {
	@Value.Auxiliary
	T cast(Object instance);

	@Value.Auxiliary
	boolean isInstance(Object instance);

	static <T> TypeInfo<T> of(Class<T> type) {
		return ClassTypeInfo.of(type);
	}

	static <T> TypeInfo<List<T>> listOf(TypeInfo<T> type) {
		return ListTypeInfo.of(type);
	}
}
