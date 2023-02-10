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

import de.flapdoodle.testdoc.Recorder;
import de.flapdoodle.testdoc.Recording;
import de.flapdoodle.testdoc.TabSize;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class NetHowtoTest {
	@RegisterExtension
	static Recording recording = Recorder.with("Net.md", TabSize.spaces(2));

	@Test
	public void freeServerPort() throws IOException {
		recording.begin();
		int freeServerPort = Net.freeServerPort();
		recording.end();
		assertThat(freeServerPort)
			.isGreaterThan(0);
	}

	@Test
	public void freeServerPortForHostAddress() throws IOException {
		InetAddress hostAddress = Net.getLocalHost();
		recording.begin();
		int freeServerPort = Net.freeServerPort(hostAddress);
		recording.end();
		assertThat(freeServerPort)
			.isGreaterThan(0);
	}

	@Test
	public void acceptAnySSLCertificate() throws IOException, NoSuchAlgorithmException, KeyManagementException {
		HttpServers.Listener listener = session -> {
			if (session.getUri().equals("/test")) {
				return Optional.of(HttpServers.response(200, "text/text", "dummy".getBytes(StandardCharsets.UTF_8)));
			}
			return Optional.empty();
		};

		try (HttpServers.HttpsServer server = new HttpServers.HttpsServer(Net.freeServerPort(), listener)) {
			URL httpsUrl = server.urlOf("test");
			recording.begin();
			HttpsURLConnection connection = (HttpsURLConnection) URLConnections.urlConnectionOf(httpsUrl);
			connection.setSSLSocketFactory(Net.acceptAllSSLContext().getSocketFactory());
			recording.end();
			//connection.setAuthenticator(new Authenticator() {});

			byte[] response = URLConnections.downloadIntoByteArray(connection);

			assertThat(response)
				.asString(StandardCharsets.UTF_8)
				.isEqualTo("dummy");
		}
	}

}