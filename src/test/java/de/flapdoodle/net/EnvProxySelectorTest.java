/*
 * Copyright (C) 2016
 *   Michael Mosmann <michael@mosmann.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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