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
