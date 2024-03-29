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
import fi.iki.elonen.NanoHTTPD;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

class URLConnectionsTest {

	@Nested
	class Downloads {
		@ParameterizedTest(name = "blocks: {0}")
		@ValueSource(ints = {0,1,4,20,100})
		public void downloadIntoByteArray(int blocks) throws IOException {
			String content=String.join("", Collections.nCopies(blocks, UUID.randomUUID().toString()));

			HttpServers.Listener listener = session -> {
				if (session.getUri().equals("/test")) {
					return Optional.of(HttpServers.response(200, "text/text", content.getBytes(StandardCharsets.UTF_8)));
				}
				return Optional.empty();
			};

			try (HttpServers.HttpServer server = new HttpServers.HttpServer(Net.freeServerPort(), listener)) {
				URLConnection connection = URLConnections.urlConnectionOf(server.urlOf("test"));
				byte[] response = URLConnections.downloadIntoByteArray(connection);

				assertThat(response)
					.asString(StandardCharsets.UTF_8)
					.isEqualTo(content);
			}
		}


		@ParameterizedTest(name = "blocks: {0}")
		@ValueSource(ints = {0,1,4,20,100})
		public void downloadIntoFile(int blocks, @TempDir Path tempDir) throws IOException {
			String content=String.join("", Collections.nCopies(blocks, UUID.randomUUID().toString()));
			Path tempFile = tempDir.resolve("tempFile");

			HttpServers.Listener listener = session -> {
				if (session.getUri().equals("/test")) {
					return Optional.of(HttpServers.response(200, "text/text", content.getBytes(StandardCharsets.UTF_8)));
				}
				return Optional.empty();
			};

			try (HttpServers.HttpServer server = new HttpServers.HttpServer(Net.freeServerPort(), listener)) {
				URLConnection connection = URLConnections.urlConnectionOf(server.urlOf("test"));
				URLConnections.downloadIntoFile(connection, tempFile, (url, bytesCopied, contentLength) -> {

				});

				assertThat(tempFile)
					.exists()
					.hasContent(content);
			}
		}

		@ParameterizedTest(name = "blocks: {0}")
		@ValueSource(ints = {0,1,4,20,100})
		public void downloadShouldBeMovedToDestinationOnSuccess(int blocks) throws IOException {
			int httpPort = Net.freeServerPort();
			String content=String.join("", Collections.nCopies(blocks, UUID.randomUUID().toString()));

			Path destination = Files.createTempFile("moveToThisFile", "");
			Files.delete(destination);

			try (HttpServers.HttpServer server = new HttpServers.HttpServer(httpPort, (session) -> Optional.empty())) {
				URLConnection connection = new URL("http://localhost:123/toLong?foo=bar").openConnection();
				URLConnections.downloadTo(connection, destination, url -> {
					Path downloadMock = Files.createTempFile("moveThis", "");
					Files.write(downloadMock, content.getBytes(StandardCharsets.UTF_8), StandardOpenOption.TRUNCATE_EXISTING);
					return downloadMock;
				});
			}

			assertThat(destination)
				.exists()
				.isRegularFile()
				.hasContent(content);
		}

		@ParameterizedTest(name = "blocks: {0}")
		@ValueSource(ints = {1,2,4})
		public void downloadShouldBeCompleteAndMatchContent(int blocks) throws IOException {
			int httpPort = Net.freeServerPort();
			String content=String.join("", Collections.nCopies(blocks*1000, UUID.randomUUID().toString()));
			long contentLengt = content.getBytes(StandardCharsets.UTF_8).length;

			HttpServers.Listener listener=(session) -> {
				if (session.getUri().equals("/download")) {
					return Optional.of(HttpServers.response(200, "text/text", content.getBytes(StandardCharsets.UTF_8)));
				}
				return Optional.empty();
			};

			List<Long> downloadSizes = new ArrayList<>();

			try (HttpServers.HttpServer server = new HttpServers.HttpServer(httpPort, listener)) {
				URLConnection connection = new URL("http://localhost:"+httpPort+"/download?foo=bar").openConnection();

				URLConnections.DownloadCopyListener copyListener=(url, bytesCopied, downloadContentLength) -> {
					downloadSizes.add(bytesCopied);
					assertThat(downloadContentLength).isEqualTo(contentLengt);
				};
				Path destination = URLConnections.downloadIntoTempFile(connection, copyListener);
				assertThat(destination)
					.exists()
					.isRegularFile()
					.hasContent(content);

				Files.delete(destination);
			}

			List<Long> downloadSizesMatchingFullDownload = downloadSizes.stream()
				.filter(l -> l == contentLengt)
				.collect(Collectors.toList());

			assertThat(downloadSizesMatchingFullDownload.size()).isEqualTo(1);

			List<Long> downloadSizesBiggerThanContentLength = downloadSizes.stream()
				.filter(l -> l > contentLengt)
				.collect(Collectors.toList());

			assertThat(downloadSizesBiggerThanContentLength)
				.isEmpty();
		}

		@ParameterizedTest(name = "blocks: {0}")
		@ValueSource(ints = {1,2,4})
		public void downloadWithoutContentLengthShouldWorkToo(int blocks) throws IOException {
			int httpPort = Net.freeServerPort();
			String content=String.join("", Collections.nCopies(blocks*1000, UUID.randomUUID().toString()));
			long contentLengt = content.getBytes(StandardCharsets.UTF_8).length;

			HttpServers.Listener listener=(session) -> {
				if (session.getUri().equals("/download")) {
					return Optional.of(HttpServers.chunkedResponse(200, "text/text", content.getBytes(StandardCharsets.UTF_8)));
				}
				return Optional.empty();
			};

			List<Long> downloadSizes = new ArrayList<>();

			try (HttpServers.HttpServer server = new HttpServers.HttpServer(httpPort, listener)) {
				URLConnection connection = new URL("http://localhost:"+httpPort+"/download?foo=bar").openConnection();

				URLConnections.DownloadCopyListener copyListener=(url, bytesCopied, downloadContentLength) -> {
					downloadSizes.add(bytesCopied);
					assertThat(downloadContentLength).isEqualTo(-1);
				};
				Path destination = URLConnections.downloadIntoTempFile(connection, copyListener);
				assertThat(destination)
					.exists()
						.isRegularFile()
							.hasContent(content);

				Files.delete(destination);
			}

			List<Long> downloadSizesMatchingFullDownload = downloadSizes.stream()
				.filter(l -> l == contentLengt)
				.collect(Collectors.toList());

			assertThat(downloadSizesMatchingFullDownload)
				.size()
				.isEqualTo(1);

			List<Long> downloadSizesBiggerThanContentLength = downloadSizes.stream()
				.filter(l -> l > contentLengt)
				.collect(Collectors.toList());

			assertThat(downloadSizesBiggerThanContentLength)
				.isEmpty();
		}


		@ParameterizedTest(name = "blocks: {0}")
		@ValueSource(ints = {1,2,4})
		public void downloadShouldFailIfContentLengthDoesNotMatch(int blocks) throws IOException {
			int httpPort = Net.freeServerPort();
			String content=String.join("", Collections.nCopies(blocks*1000, UUID.randomUUID().toString()));
			long contentLengt = content.getBytes(StandardCharsets.UTF_8).length;

			HttpServers.Listener listener=(session) -> {
				if (session.getUri().equals("/toShort")) {
					return Optional.of(HttpServers.response(200, "text/text", content.getBytes(StandardCharsets.UTF_8), content.getBytes(StandardCharsets.UTF_8).length*2));
				}
				if (session.getUri().equals("/toLong")) {
					return Optional.of(HttpServers.response(200, "text/text", content.getBytes(StandardCharsets.UTF_8), content.getBytes(StandardCharsets.UTF_8).length/2));
				}
				return Optional.empty();
			};

			try (HttpServers.HttpServer server = new HttpServers.HttpServer(httpPort, listener)) {

				try {
					URLConnection connection = new URL("http://localhost:"+httpPort+"/toShort?foo=bar").openConnection();
					URLConnections.downloadIntoTempFile(connection, (url, bytesCopied, downloadContentLength) -> {
					});
					fail("should not reach this");
				} catch (IllegalArgumentException iax) {
					assertThat(iax.getLocalizedMessage()).contains("partial");
				}

				// looks like URLConnection Impl does only read to content-length size if provided
				boolean weCanFakeNanoHttpToSendMoreStuffThanInContentLength=false;
				if (weCanFakeNanoHttpToSendMoreStuffThanInContentLength) {
					try {
						URLConnection connection = new URL("http://localhost:"+httpPort+"/toLong?foo=bar").openConnection();
						URLConnections.downloadIntoTempFile(connection, (url, bytesCopied, downloadContentLength) -> {
						});
						fail("should not reach this");
					} catch (IllegalArgumentException iax) {
						assertThat(iax.getLocalizedMessage()).contains("partial");
					}
				}

			}
		}

		@ParameterizedTest(name = "blocks: {0}")
		@ValueSource(ints = {1,2,4})
		public void shouldNotFailIfCalledTwice(int blocks) throws IOException {
			int httpPort = Net.freeServerPort();
			String content=String.join("", Collections.nCopies(blocks*1000, UUID.randomUUID().toString()));
			long contentLengt = content.getBytes(StandardCharsets.UTF_8).length;

			HttpServers.Listener listener=(session) -> {
				if (session.getUri().equals("/stuff")) {
					return Optional.of(HttpServers.response(200, "text/text", content.getBytes(StandardCharsets.UTF_8)));
				}
				return Optional.empty();
			};

			try (HttpServers.HttpServer server = new HttpServers.HttpServer(httpPort, listener)) {
				URLConnection connection = new URL("http://localhost:"+httpPort+"/stuff").openConnection();
				URLConnections.downloadIntoTempFile(connection, (url, bytesCopied, downloadContentLength) -> {
					assertThat(downloadContentLength).isEqualTo(contentLengt);
				});

				connection = new URL("http://localhost:"+httpPort+"/stuff").openConnection();
				URLConnections.downloadIntoTempFile(connection, (url, bytesCopied, downloadContentLength) -> {
					assertThat(downloadContentLength).isEqualTo(contentLengt);
				});
			}
		}


	}

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
				URLConnection connection = URLConnections.urlConnectionOf(server.urlOf("test"));
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
					Proxys.httpProxy(server.getHostname(), server.getListeningPort()));

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
					Proxys.httpProxy(server.getHostname(), server.getListeningPort(), username, password));

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
				HttpsURLConnection connection = (HttpsURLConnection) URLConnections.urlConnectionOf(server.urlOf("test"));
				connection.setSSLSocketFactory(Net.acceptAllSSLContext().getSocketFactory());
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
						Proxys.httpProxy(proxyServer.getHostname(), proxyServer.getListeningPort()));
					connection.setSSLSocketFactory(Net.acceptAllSSLContext().getSocketFactory());

					byte[] response = URLConnections.downloadIntoByteArray(connection);

					assertThat(response)
						.asString(StandardCharsets.UTF_8)
						.isEqualTo("dummy");
				}
			}
		}

		/**
		 * if the destination is just a http url, then the header set in ${@link URLConnections#urlConnectionOf(URL, Proxy)}
		 * is enough to let the request pass (if username and password matches)

		 * to tunnel a https connection through a proxy with proxy authentication someone
		 * has to ask for user and password or has to set the header to the proxy connection
		 *
		 * AFAIK this is not possible with the API available with java 8
		 */
		@SuppressWarnings("unchecked") @Test
		public void connectToHttpsServerWithProxyWithBasicAuth() throws IOException, NoSuchAlgorithmException, KeyManagementException {
			int port = Net.freeServerPort();

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
					assertThatThrownBy(() -> URLConnections.urlConnectionOf(httpsServer.urlOf("test"),
						Proxys.httpProxy(proxyServer.getHostname(), proxyServer.getListeningPort(), username, password)))
						.isInstanceOf(IllegalArgumentException.class)
						.hasMessageContaining("access of a https url over a proxy with proxy authorization is not supported");

					boolean urlConnectionOfHasNoCheckIfHttpsIsUsed=false;

					if (urlConnectionOfHasNoCheckIfHttpsIsUsed) {
						HttpsURLConnection connection = (HttpsURLConnection) URLConnections.urlConnectionOf(httpsServer.urlOf("test"),
							Proxys.httpProxy(proxyServer.getHostname(), proxyServer.getListeningPort(), username, password));
						connection.setSSLSocketFactory(Net.acceptAllSSLContext().getSocketFactory());

						assertThatThrownBy(() -> URLConnections.downloadIntoByteArray(connection))
							.isInstanceOf(IOException.class)
							.hasMessageContaining("Unable to tunnel through proxy. Proxy returns \"HTTP/1.0 407 Proxy Authorization Required\"");
					}
				}
			}
		}
	}
}