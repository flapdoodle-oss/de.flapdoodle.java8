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

import de.flapdoodle.testdoc.Recorder;
import de.flapdoodle.testdoc.Recording;
import de.flapdoodle.testdoc.TabSize;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class URLConnectionsHowtoTest {
	@RegisterExtension
	static Recording recording = Recorder.with("URLConnections.md", TabSize.spaces(2))
		.renderTo("docs/URLConnections.md");

	@Test
	public void enableEnvVariableProxyUsage() {
		recording.output("envVar", URLConnections.USE_ENV_PROXY_SELECTOR);
	}

	@Test
	public void connectionAndDownloadToByteArray() throws IOException {
		String content="content";

		HttpServers.Listener listener = session -> {
			if (session.getUri().equals("/test")) {
				return Optional.of(HttpServers.response(200, "text/text", content.getBytes(StandardCharsets.UTF_8)));
			}
			return Optional.empty();
		};

		try (HttpServers.HttpServer server = new HttpServers.HttpServer(Net.freeServerPort(), listener)) {
			URL downloadUrl = server.urlOf("test");
			recording.begin();
			URLConnection connection = URLConnections.urlConnectionOf(downloadUrl);
			byte[] response = URLConnections.downloadIntoByteArray(connection, (url, bytesCopied, contentLength) -> {

			});
			recording.end();

			assertThat(response)
				.asString(StandardCharsets.UTF_8)
				.isEqualTo(content);
		}
	}

	@Test
	public void connectionAndDownloadToFile(@TempDir Path tempDir) throws IOException {
		String content="content";

		HttpServers.Listener listener = session -> {
			if (session.getUri().equals("/test")) {
				return Optional.of(HttpServers.response(200, "text/text", content.getBytes(StandardCharsets.UTF_8)));
			}
			return Optional.empty();
		};

		try (HttpServers.HttpServer server = new HttpServers.HttpServer(Net.freeServerPort(), listener)) {
			URL downloadUrl = server.urlOf("test");
			Path file = tempDir.resolve(UUID.randomUUID().toString());
			recording.begin();
			URLConnection connection = URLConnections.urlConnectionOf(downloadUrl);
			URLConnections.downloadIntoFile(connection, file, (url, bytesCopied, contentLength) -> {

			});
			recording.end();

			assertThat(file)
				.exists()
				.isRegularFile()
				.hasContent(content);
		}
	}
}
