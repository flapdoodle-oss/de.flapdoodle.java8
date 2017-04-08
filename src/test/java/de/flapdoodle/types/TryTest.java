package de.flapdoodle.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Test;

public class TryTest {

	@Test(expected=IOException.class)
	public void functionShouldThrowMatchingException() throws IOException {
		Try.function(TryTest::functionThrowingIO).apply("fail");
	}
	
	@Test
	public void functionMayThrowExceptionButDoesNot() {
		expectNoException(() -> assertEquals("ok foo",Try.function(TryTest::functionThrowingIO).apply("foo")));
	}
	
	private static void expectNoException(ThrowingRunable<?> runable) {
		try {
			runable.run();
		} catch (Exception ex) {
			fail("got exception "+ex.getClass());
		}
	}
	
	private static String functionThrowingIO(String src) throws IOException {
		if (src.equals("fail")) {
			throw new IOException("should fail");
		}
		return "ok "+src;
	}
	
	private static String supplierThrowingIO() throws IOException {
		throw new IOException("should fail");
	}
	
	private static String supplierCouldThrowIO() throws IOException {
		if (false) {
			throw new IOException("should fail");
		}
		return "ok";
	}
	
	private static void consumerThrowingIO(String src) throws IOException {
		if (src.equals("fail")) {
			throw new IOException("should fail");
		}
	}

	private static void runableThrowingIO() throws IOException {
		throw new IOException("should fail");
	}
	
	private static void runableCouldThrowIO() throws IOException {
		if (false) {
			throw new IOException("should fail");
		}
	}
	
}
