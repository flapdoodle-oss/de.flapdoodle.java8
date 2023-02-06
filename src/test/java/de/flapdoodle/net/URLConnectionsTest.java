package de.flapdoodle.net;

import de.flapdoodle.types.Pair;
import fi.iki.elonen.NanoHTTPD;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class URLConnectionsTest {

	@Nested
	class HttpConnections {
		
		@Test
		public void connectToHttpServer() throws IOException {
			HttpServers.Listener listener = session -> {
				if (session.getUri().equals("/test")) {
					return Optional.of(HttpServers.response(200, "text/text", "dummy".getBytes(StandardCharsets.UTF_8)));
				}
				return Optional.empty();
			};

			try (HttpServers.HttpServer server = new HttpServers.HttpServer(Net.freeServerPort(), listener)) {
				URLConnection connection = URLConnections.urlConnectionOf(server.urlOf("test"), Optional.empty());
				byte[] response = URLConnections.downloadIntoByteArray(connection);

				assertThat(response)
					.asString(StandardCharsets.UTF_8)
					.isEqualTo("dummy");
			}
		}

		@Test
		public void connectToHttpServerWithProxy() throws IOException {
			int port = Net.freeServerPort();

			HttpServers.Listener listener = session -> {
				if (session.getUri().equals("http://localhost:" + port + "/test")) {
					return Optional.of(HttpServers.response(200, "text/text", "dummy".getBytes(StandardCharsets.UTF_8)));
				}
				return Optional.empty();
			};

			try (HttpServers.HttpServer server = new HttpServers.HttpServer(port, listener)) {
				URLConnection connection = URLConnections.urlConnectionOf(server.urlOf("test"),
					Optional.of(Proxys.httpProxy(server.getHostname(), server.getListeningPort())));

				byte[] response = URLConnections.downloadIntoByteArray(connection);

				assertThat(response)
					.asString(StandardCharsets.UTF_8)
					.isEqualTo("dummy");
			}
		}

		@Test
		public void connectToHttpServerWithBasicAuthProxy() throws IOException {
			int port = Net.freeServerPort();

			String username = "user";
			String password = "passwd";
			String authHeader = new String(Base64.getEncoder().encode((username + ":" + password).getBytes()));

			HttpServers.Listener listener = session -> {
				String authorization = session.getHeaders().get("proxy-authorization");
				if (authorization == null || !authorization.equals("Basic " + authHeader)) {
					NanoHTTPD.Response response = HttpServers.response(401, "text/text", "protected".getBytes(StandardCharsets.UTF_8));
					response.addHeader("WWW-Authenticate", "Basic realm=\"Protected Area\", charset=\"UTF-8\"");
					return Optional.of(response);
				}

				if (session.getUri().equals("http://localhost:" + port + "/test")) {
					return Optional.of(HttpServers.response(200, "text/text", "dummy".getBytes(StandardCharsets.UTF_8)));
				}
				return Optional.empty();
			};

			try (HttpServers.HttpServer server = new HttpServers.HttpServer(port, listener)) {
				URLConnection connection = URLConnections.urlConnectionOf(server.urlOf("test"),
					Optional.of(Proxys.httpProxy(server.getHostname(), server.getListeningPort(), username, password)));

				byte[] response = URLConnections.downloadIntoByteArray(connection);

				assertThat(response)
					.asString(StandardCharsets.UTF_8)
					.isEqualTo("dummy");
			}
		}
	}

	@Nested
	class HttpsConnections {

		@Test
		public void connectToHttpsServer() throws IOException, NoSuchAlgorithmException, KeyManagementException {
			HttpServers.Listener listener = session -> {
				if (session.getUri().equals("/test")) {
					return Optional.of(HttpServers.response(200, "text/text", "dummy".getBytes(StandardCharsets.UTF_8)));
				}
				return Optional.empty();
			};

			try (HttpServers.HttpsServer server = new HttpServers.HttpsServer(Net.freeServerPort(), listener)) {
				HttpsURLConnection connection = (HttpsURLConnection) URLConnections.urlConnectionOf(server.urlOf("test"), Optional.empty());
				connection.setSSLSocketFactory(Net.acceptAllSSLSocketFactory());
				//connection.setAuthenticator(new Authenticator() {});

				byte[] response = URLConnections.downloadIntoByteArray(connection);

				assertThat(response)
					.asString(StandardCharsets.UTF_8)
					.isEqualTo("dummy");
			}
		}

		@Test
		public void connectToHttpsServerWithProxy() throws IOException, NoSuchAlgorithmException, KeyManagementException {
			int port = Net.freeServerPort();

			HttpServers.Listener listener = session -> {
				if (session.getUri().equals("/test")) {
					return Optional.of(HttpServers.response(200, "text/text", "dummy".getBytes(StandardCharsets.UTF_8)));
				}
				return Optional.empty();
			};

			try (HttpServers.HttpsServer httpsServer = new HttpServers.HttpsServer(port, listener)) {
				try (HttpServers.HttpsProxyServer proxyServer = new HttpServers.HttpsProxyServer(port + 1)) {
					HttpsURLConnection connection = (HttpsURLConnection) URLConnections.urlConnectionOf(httpsServer.urlOf("test"),
						Optional.of(Proxys.httpProxy(proxyServer.getHostname(), proxyServer.getListeningPort())));
					connection.setSSLSocketFactory(Net.acceptAllSSLSocketFactory());

					byte[] response = URLConnections.downloadIntoByteArray(connection);

					assertThat(response)
						.asString(StandardCharsets.UTF_8)
						.isEqualTo("dummy");
				}
			}
		}

		/**
		 * if the destination is just a http url, then the header set in ${@link URLConnections#urlConnectionOf(URL, Optional)}
		 * is enough to let the request pass (if username and password matches)

		 * to tunnel a https connection through a proxy with proxy authentication someone
		 * has to ask for user and password or has to set the header to the proxy connection
		 *
		 * AFAIK this is not possible with the API available with java 8
		 */
		@Test
		public void connectToHttpsServerWithProxyWithBasicAuth() throws IOException, NoSuchAlgorithmException, KeyManagementException {
			int port = true ? 12345 : Net.freeServerPort();

			String username = "user";
			String password = "passwd";
			String authHeader = new String(Base64.getEncoder().encode((username + ":" + password).getBytes()));

			HttpServers.Listener listener = session -> {
				if (session.getUri().equals("/test")) {
					return Optional.of(HttpServers.response(200, "text/text", "dummy".getBytes(StandardCharsets.UTF_8)));
				}
				return Optional.empty();
			};

			HttpServers.HttpsProxyServer.HttpsProxySessionListener proxyListener= session -> {
				if (!session.headers().containsKey("Proxy-Authorization")) {
					session.response(407, "Proxy Authorization Required",
								Pair.of("Proxy-Authenticate", "Basic realm\"Protected\"")
					);
				}
			};

			try (HttpServers.HttpsServer httpsServer = new HttpServers.HttpsServer(port, listener)) {
				try (HttpServers.HttpsProxyServer proxyServer = new HttpServers.HttpsProxyServer(port + 1, proxyListener)) {
					HttpsURLConnection connection = (HttpsURLConnection) URLConnections.urlConnectionOf(httpsServer.urlOf("test"),
						Optional.of(Proxys.httpProxy(proxyServer.getHostname(), proxyServer.getListeningPort(), username, password)));
					connection.setSSLSocketFactory(Net.acceptAllSSLSocketFactory());

					assertThatThrownBy(() -> URLConnections.downloadIntoByteArray(connection))
						.isInstanceOf(IOException.class)
						.hasMessageContaining("Unable to tunnel through proxy. Proxy returns \"HTTP/1.0 407 Proxy Authorization Required\"");
				}
			}
		}
	}
}