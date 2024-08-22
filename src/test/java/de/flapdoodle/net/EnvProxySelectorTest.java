package de.flapdoodle.net;

import de.flapdoodle.types.Pair;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class EnvProxySelectorTest {
	@Test
	public void enableProxyIfEnvVarIsPresent() throws MalformedURLException {
		ProxySelector testee = EnvProxySelector.with(mapOf(
			entry("http_proxy", "http://proxy.net:1234"),
			entry("https_proxy", "http://proxy.net:2345"),
			entry("no_proxy", "localhost,127.0.*")
		));

		assertThat(testee.select(new URL("http://server:80/foo?bar")))
			.isPresent()
			.contains(ProxyFactory.of("proxy.net",1234));

		assertThat(testee.select(new URL("https://server:80/foo?bar")))
			.isPresent()
			.contains(ProxyFactory.of("proxy.net",2345));

		assertThat(testee.select(new URL("http://localhost:80/foo?bar")))
			.isEmpty();
		assertThat(testee.select(new URL("http://127.0.1.1:80/foo?bar")))
			.isEmpty();
	}

	@SafeVarargs
	private static Map<String, String> mapOf(Pair<String, String> ... entries) {
		LinkedHashMap<String, String> ret = new LinkedHashMap<>();
		for (Pair<String, String> entry : entries) {
			ret.put(entry.first(), entry.second());
		}
		return ret;
	}

	private static Pair<String, String> entry(String key, String value) {
		return Pair.of(key, value);
	}
}