package de.flapdoodle.net;

import org.immutables.value.Value;

import java.net.Proxy;

@FunctionalInterface
public interface ProxyFactory {
	Proxy create();

	@Value.Immutable
	abstract class HostnamePortProxyFactory implements ProxyFactory {
		@Value.Parameter
		public abstract String hostName();
		@Value.Parameter
		public abstract int port();

		@Override
		@Value.Auxiliary
		public Proxy create() {
			return Proxys.httpProxy(hostName(), port());
		}
	}

	static HostnamePortProxyFactory of(String hostName, int port) {
		return ImmutableHostnamePortProxyFactory.of(hostName, port);
	}
}
