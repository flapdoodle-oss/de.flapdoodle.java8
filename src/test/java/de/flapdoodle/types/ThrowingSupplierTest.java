package de.flapdoodle.types;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

public class ThrowingSupplierTest {

	@Test(expected = IOException.class)
	public void throwsExpectedExeption() throws IOException {
		Try.supplier(ThrowingSupplierTest::supplierThrowingIO).get();
	}

	@Test
	public void doNotThrowExeption() throws IOException {
		assertEquals("ok", Try.supplier(ThrowingSupplierTest::supplierCouldThrowIO).get());
	}

	@Test
	public void doNotThrowWithMappedExeption() {
		assertEquals("ok", Try.supplier(ThrowingSupplierTest::supplierCouldThrowIO)
				.mapCheckedException(RuntimeException::new)
				.get());
	}
	
	@Test
	public void doNotThrowWithMappedExeptionAndFallback() {
		assertEquals("ok", Try.supplier(ThrowingSupplierTest::supplierCouldThrowIO)
				.mapCheckedException(RuntimeException::new)
				.onCheckedException(ex -> "fallback")
				.get());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void mapExeption() {
		Try.supplier(ThrowingSupplierTest::supplierThrowingIO)
			.mapCheckedException(IllegalArgumentException::new)
			.get();
	}
	
	@Test(expected = RuntimeException.class)
	public void mapAsRuntimeExeption() {
		Try.supplier(ThrowingSupplierTest::supplierThrowingIO)
			.mapCheckedException(RuntimeException::new)
			.get();
	}
	
	@Test(expected = CustomRuntimeException.class)
	public void dontRemapRuntimeExeption() {
		Try.supplier(ThrowingSupplierTest::supplierCouldThrowIOButThrowsRuntime)
			.mapCheckedException(IllegalArgumentException::new)
			.get();
	}
	
	@Test
	public void mapExceptionToFallback() {
		assertEquals("fallback", Try.supplier(ThrowingSupplierTest::supplierThrowingIO)
			.onCheckedException(ex -> "fallback")
			.get());
	}
	
	@Test(expected = CustomRuntimeException.class)
	public void doesNotMapExceptionToFallbackBecauseOfRuntimeException() {
		Try.supplier(ThrowingSupplierTest::supplierCouldThrowIOButThrowsRuntime)
			.mapCheckedException(IllegalArgumentException::new)
			.onCheckedException(ex -> "fallback")
			.get();
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
	
	private static String supplierCouldThrowIOButThrowsRuntime() throws IOException {
		if (false) {
			throw new IOException("should fail");
		}
		throw new CustomRuntimeException();
	}


}
