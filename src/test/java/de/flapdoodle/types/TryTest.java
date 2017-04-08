package de.flapdoodle.types;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

public class TryTest extends AbstractThrowingTest {

	@Test(expected=IOException.class)
	public void functionShouldThrowMatchingException() throws IOException {
		Try.function(TryTest::functionThrowingIO).apply("fail");
	}
	
	@Test
	public void functionMayThrowExceptionButDoesNot() {
		expectNoException(() -> assertEquals("ok foo",Try.function(TryTest::functionThrowingIO).apply("foo")));
	}
	
}
