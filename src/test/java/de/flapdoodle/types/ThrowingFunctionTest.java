package de.flapdoodle.types;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

public class ThrowingFunctionTest {

	@Test(expected = IOException.class)
	public void throwsExpectedExeption() throws IOException {
		Try.function(ThrowingFunctionTest::functionThrowingIO).apply("fail");
	}

	@Test
	public void doNotThrowExeption() throws IOException {
		assertEquals("ok ok", Try.function(ThrowingFunctionTest::functionThrowingIO).apply("ok"));
	}

	@Test
	public void doNotThrowWithMappedExeption() {
		assertEquals("ok ok", Try.function(ThrowingFunctionTest::functionThrowingIO)
				.mapCheckedException(RuntimeException::new)
				.apply("ok"));
	}
	
	@Test
	public void doNotThrowWithFallback() {
		assertEquals("ok ok", Try.function(ThrowingFunctionTest::functionThrowingIO)
				.onCheckedException((ex,v) -> "fallback "+v)
				.apply("ok"));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void mapExeption() {
		Try.function(ThrowingFunctionTest::functionThrowingIO)
			.mapCheckedException(IllegalArgumentException::new)
			.apply("fail");
	}
	
	@Test(expected = RuntimeException.class)
	public void mapAsRuntimeExeption() {
		Try.function(ThrowingFunctionTest::functionThrowingIO)
			.mapCheckedException(RuntimeException::new)
			.apply("fail");
	}
	
	@Test(expected = CustomRuntimeException.class)
	public void dontRemapRuntimeExeption() {
		Try.function(ThrowingFunctionTest::functionCouldThrowIOButThrowsRuntime)
			.mapCheckedException(IllegalArgumentException::new)
			.apply("noop");
	}
	
	@Test
	public void mapExceptionToFallback() {
		assertEquals("fallback fail", Try.function(ThrowingFunctionTest::functionThrowingIO)
			.onCheckedException((ex,v) -> "fallback "+v)
			.apply("fail"));
	}
	
	@Test(expected = CustomRuntimeException.class)
	public void doesNotMapExceptionToFallbackBecauseOfRuntimeException() {
		Try.function(ThrowingFunctionTest::functionCouldThrowIOButThrowsRuntime)
			.mapCheckedException(IllegalArgumentException::new)
			.onCheckedException((ex,v) -> "fallback "+v)
			.apply("noop");
	}
	
	protected static String functionThrowingIO(String src) throws IOException {
		if (src.equals("fail")) {
			throw new IOException("should fail");
		}
		return "ok "+src;
	}

	private static String functionCouldThrowIOButThrowsRuntime(String src) throws IOException {
		if (false) {
			throw new IOException("should fail");
		}
		throw new CustomRuntimeException();
	}


}
