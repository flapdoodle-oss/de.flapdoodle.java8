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

import org.slf4j.Logger;

import java.net.Proxy;
import java.net.URL;
import java.util.Optional;

@FunctionalInterface
public interface ProxySelector {
	Optional<ProxyFactory> select(URL url);

	static ProxySelector noProxy() {
		return new NoProxy();
	}

	static ProxySelector envVariableProxySelector() {
		return EnvProxySelector.with(System.getenv());
	}

	class NoProxy implements ProxySelector {
		@Override
		public Optional<ProxyFactory> select(URL url) {
			return Optional.empty();
		}

		@Override
		public String toString() {
			return getClass().getSimpleName();
		}
	}
}
