package de.flapdoodle.types;

import java.io.IOException;

import org.junit.Test;

public class ThrowingRunableTest {

	@Test(expected = IOException.class)
	public void throwsExpectedExeption() throws IOException {
		Try.runable(ThrowingRunableTest::runableThrowingIO).run();
	}

	@Test
	public void doNotThrowExeption() throws IOException {
		Try.runable(ThrowingRunableTest::runableCouldThrowIO).run();
	}

	@Test(expected = IllegalArgumentException.class)
	public void mapExeption() {
		Try.runable(ThrowingRunableTest::runableThrowingIO)
			.mapException(ex -> new IllegalArgumentException(ex))
			.run();
	}
	
	@Test(expected = RuntimeException.class)
	public void mapAsRuntimeExeption() {
		Try.runable(ThrowingRunableTest::runableThrowingIO)
			.mapToRuntimeException()
			.run();
	}
	
	@Test(expected = CustomRuntimeException.class)
	public void dontRemapRuntimeExeption() {
		Try.runable(ThrowingRunableTest::runableCouldThrowIOButThrowsRuntime)
			.mapException(ex -> new IllegalArgumentException(ex))
			.run();
	}
	
	protected static void runableThrowingIO() throws IOException {
		throw new IOException("should fail");
	}

	protected static void runableCouldThrowIO() throws IOException {
		if (false) {
			throw new IOException("should fail");
		}
	}

	protected static void runableCouldThrowIOButThrowsRuntime() throws IOException {
		if (false) {
			throw new IOException("should fail");
		}
		throw new CustomRuntimeException();
	}

	static class CustomRuntimeException extends RuntimeException {
		
	}

}
