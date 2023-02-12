/**
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

import javax.net.ssl.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Net {
	private static final Logger logger=Logger.getLogger(Net.class.getName());

	private static final String NO_LOCALHOST_ERROR_MESSAGE = "We could not detect if localhost is IPv4 or IPv6. " +
		"Sometimes there is no entry for localhost. " +
		"If 'ping localhost' does not work, it could help to add the right entry in your hosts configuration file.";
	private static final int IPV4_LENGTH = 4;

	public static boolean localhostIsIPv6() throws UnknownHostException {
		try {
			InetAddress addr = getLocalHost();
			byte[] ipAddr = addr.getAddress();
			return ipAddr.length > IPV4_LENGTH;
		} catch (UnknownHostException ux) {
			logger.log(Level.SEVERE, NO_LOCALHOST_ERROR_MESSAGE, ux);
			throw ux;
		}
	}

	public static int freeServerPort() throws IOException {
		return freeServerPort(getLocalHost());
	}

	public static int freeServerPort(InetAddress hostAddress) throws IOException {
		try(ServerSocket socket = new ServerSocket(0,0,hostAddress)) {
			return socket.getLocalPort();
		}
	}

	public static InetAddress getLocalHost() throws UnknownHostException {
		InetAddress ret = InetAddress.getLocalHost();
		// see https://www.linuxtopia.org/online_books/linux_system_administration/debian_linux_guides/debian_linux_reference_guide/ch-gateway.en_009.html
		// call to getLocalHost() can give 127.0.1.1 which is not the same as localhost and will lead to trouble
		// if used to connect services
		if (!ret.isLoopbackAddress() || ret.getHostAddress().equals("127.0.1.1")) {
			ret = localHostByName();
		}
		return ret;
	}

	public static InetAddress localHostByName() throws UnknownHostException {
		InetAddress ret;
		ret = InetAddress.getByName("localhost");
		if (!ret.isLoopbackAddress()) {
			throw new IllegalArgumentException(ret.getHostAddress()+" is not a loopback address");
		}
		return ret;
	}

	public static SSLContext acceptAllSSLContext() throws NoSuchAlgorithmException, KeyManagementException {
		TrustManager[] trustAllCerts = new TrustManager[]{
			new X509ExtendedTrustManager() {
				public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket) {}
				public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket) {}
				public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine) {}
				public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine) {}
				public void checkClientTrusted(X509Certificate[] chain, String authType) {}
				public void checkServerTrusted(X509Certificate[] chain, String authType) {}
				public X509Certificate[] getAcceptedIssuers() {
					return new X509Certificate[0];
				}
			}
		};

		SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext.init(null, trustAllCerts, null);
		return sslContext;
	}

}
