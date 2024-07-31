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

import de.flapdoodle.checks.Preconditions;
import de.flapdoodle.types.Optionals;
import de.flapdoodle.types.ThrowingFunction;
import de.flapdoodle.types.ThrowingSupplier;

import java.io.*;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.*;
import java.util.Base64;
import java.util.Optional;

public class URLConnections {

	static final int BUFFER_LENGTH = 1024 * 8 * 8;

	public static URLConnection urlConnectionOf(URL url) throws IOException {
		return urlConnectionOf(url, Optional.empty());
	}

	public static URLConnection urlConnectionOf(URL url, Proxy proxy) throws IOException {
		return urlConnectionOf(url, Optional.of(proxy));
	}

	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	private static URLConnection urlConnectionOf(URL url, Optional<Proxy> proxy) throws IOException {
		URLConnection openConnection = Optionals.with(proxy)
			.map(url::openConnection)
			.orElseGet(url::openConnection);

		proxy.ifPresent(it -> {
			if (it instanceof Proxys.UseBasicAuth) {
				Proxys.UseBasicAuth withBasicAuth = (Proxys.UseBasicAuth) it;
				String authHeader = new String(Base64.getEncoder().encode((withBasicAuth.proxyUser() + ":" + withBasicAuth.proxyPassword()).getBytes()));
				openConnection.setRequestProperty("Proxy-Authorization", "Basic " + authHeader);

				if (url.getProtocol().equals("https")) {
					// the jdk creates a tunnel https connection proxy request with no way to add an header to this request
					// if you read this comment and know how to fix it, please open a PR:)
					throw new IllegalArgumentException("access of a https url over a proxy with proxy authorization is not supported");
				}
			}
		});

		return openConnection;
	}

	public static byte[] downloadIntoByteArray(URLConnection connection) throws IOException {
		return downloadIntoByteArray(connection, (url, bytesCopied, contentLength) -> {});
	}

	public static byte[] downloadIntoByteArray(URLConnection connection, DownloadCopyListener copyListener) throws IOException {
		ByteArrayOutputStream os=new ByteArrayOutputStream();
		downloadAndCopy(connection, () -> new BufferedOutputStream(os), copyListener);
		return os.toByteArray();
	}

	public static void downloadIntoFile(URLConnection connection, Path destination, DownloadCopyListener copyListener) throws IOException {
		downloadTo(connection, destination, c -> downloadIntoTempFile(c, copyListener));
	}

	public static Path downloadIntoTempFile(URLConnection connection) throws IOException {
		return downloadIntoTempFile(connection, (url, bytesCopied, contentLength) -> {});
	}

	public static Path downloadIntoTempFile(URLConnection connection, DownloadCopyListener copyListener) throws IOException {
		Path tempFile = java.nio.file.Files.createTempFile("download", "");
		boolean downloadSucceeded=false;
		try {
			downloadAndCopy(connection, () -> new BufferedOutputStream(Files.newOutputStream(tempFile.toFile().toPath())), copyListener);
			downloadSucceeded=true;
			return tempFile;
		} finally {
			if (!downloadSucceeded) {
				Files.delete(tempFile);
			}
		}
	}

	protected static <E extends Exception> void downloadTo(URLConnection connection, Path destination, ThrowingFunction<URLConnection, Path, E> urlToTempFile) throws IOException,E {
		Preconditions.checkArgument(!Files.exists(destination), "destination exists: %s",destination);
		Path tempFile = urlToTempFile.apply(connection);
		move(tempFile, destination);
	}

	//VisibleForTest
	protected static void move(Path tempFile, Path destination) throws IOException {
		try {
			Files.move(tempFile, destination, StandardCopyOption.ATOMIC_MOVE);
		} catch (AtomicMoveNotSupportedException ex) {
			Files.move(tempFile, destination, StandardCopyOption.REPLACE_EXISTING);
		}
	}

	private static <E extends Exception> void downloadAndCopy(URLConnection connection, ThrowingSupplier<BufferedOutputStream, E> output, DownloadCopyListener copyListener) throws IOException, E {
		long length = connection.getContentLengthLong();
		copyListener.downloaded(connection.getURL(), 0, length);
		try (BufferedInputStream bis = new BufferedInputStream(connection.getInputStream())) {
			try (BufferedOutputStream bos = output.get()) {
				byte[] buf = new byte[BUFFER_LENGTH];
				int read = 0;
				long readCount = 0;
				while ((read = bis.read(buf)) != -1) {
					bos.write(buf, 0, read);
					readCount = readCount + read;
					Preconditions.checkArgument(length==-1 || length>=readCount, "hmm.. readCount bigger than contentLength(more than we want to): %s > %s",readCount, length);
					copyListener.downloaded(connection.getURL(), readCount, length);
				}
				bos.flush();
				Preconditions.checkArgument(length==-1 || length==readCount, "hmm.. readCount smaller than contentLength(partial download?): %s > %s",readCount, length);
			}
		}
	}

	@FunctionalInterface
	public interface DownloadCopyListener {
		/**
		 * called on each transfered block
		 * @param url current url
		 * @param bytesCopied 0..n
		 * @param contentLength -1 if not set
		 */
		void downloaded(URL url, long bytesCopied, long contentLength);
	}
}
