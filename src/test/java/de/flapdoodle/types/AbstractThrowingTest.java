package de.flapdoodle.types;

import static org.junit.Assert.fail;

import java.io.IOException;

public abstract class AbstractThrowingTest {

	protected static void expectNoException(ThrowingRunable<?> runable) {
		try {
			runable.run();
		} catch (Exception ex) {
			fail("got exception "+ex.getClass());
		}
	}

	protected static String functionThrowingIO(String src) throws IOException {
		if (src.equals("fail")) {
			throw new IOException("should fail");
		}
		return "ok "+src;
	}

	protected static void consumerThrowingIO(String src) throws IOException {
		if (src.equals("fail")) {
			throw new IOException("should fail");
		}
	}

}
