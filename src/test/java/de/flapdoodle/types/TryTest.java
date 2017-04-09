package de.flapdoodle.types;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class TryTest {

	@Test
	public void compilerWillChoseMatchingSuperExceptionType() throws A {
		assertNotNull(Try.supplier(TryTest::throwsAaAndAb).get());
	}
	
	@Test
	public void compilerWillChoseExceptionIfNoMatchingSuperType() throws Exception {
		assertNotNull(Try.supplier(TryTest::throwsAaAndB).get());
	}
	
	private static String throwsAaAndAb() throws AA, AB {
		return "";
	}
	
	private static String throwsAaAndB() throws AA, B {
		return "";
	}
	
	static class A extends Exception {
		
	}
	
	static class AA extends A {
		
	}
	
	static class AB extends A {
		
	}
	
	static class B extends Exception {
		
	}
}
