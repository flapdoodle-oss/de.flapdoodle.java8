package de.flapdoodle.net;

import java.net.URI;
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
