package de.flapdoodle.net;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;

public class Proxys {

	public static Proxy httpProxy(String hostName, int port) {
		return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(hostName, port));
	}

	public static Proxy httpProxy(String hostName, int port, String proxyUser, String proxyPassword) {
		return new HttpProxyBasicAuth(new InetSocketAddress(hostName, port), proxyUser, proxyPassword);
	}

	private static class HttpProxyBasicAuth extends Proxy implements UseBasicAuth {
		private String proxyUser;
		private String proxyPassword;

		public HttpProxyBasicAuth(SocketAddress socketAddress, String proxyUser, String proxyPassword) {
			super(Proxy.Type.HTTP, socketAddress);

			this.proxyUser = proxyUser;
			this.proxyPassword = proxyPassword;
		}

		@Override
		public String proxyUser() {
			return proxyUser;
		}
		
		@Override
		public String proxyPassword() {
			return proxyPassword;
		}
	}

	interface UseBasicAuth {
		String proxyUser();
		String proxyPassword();
	}
}
