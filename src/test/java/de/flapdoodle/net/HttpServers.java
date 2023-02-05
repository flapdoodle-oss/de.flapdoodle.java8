package de.flapdoodle.net;

import de.flapdoodle.types.Pair;
import fi.iki.elonen.NanoHTTPD;

import java.io.*;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HttpServers {

	public static NanoHTTPD.Response response(int status, String mimeType, byte[] data) {
		return response(status, mimeType, data, data.length);
	}

	public static NanoHTTPD.Response response(int status, String mimeType, byte[] data, int contentLength) {
		NanoHTTPD.Response ret = NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.lookup(status), mimeType, new ByteArrayInputStream(data), data.length);
		ret.addHeader("content-length", "" + contentLength);
		return ret;

	}

	public static NanoHTTPD.Response chunkedResponse(int status, String mimeType, byte[] data) {
		return NanoHTTPD.newChunkedResponse(NanoHTTPD.Response.Status.lookup(status), mimeType, new ByteArrayInputStream(data));
	}

	public static NanoHTTPD.Response rawResponse(byte[] data) {
		return response(200, "text/text", data);
	}

	@FunctionalInterface
	public interface Listener {
		Optional<NanoHTTPD.Response> serve(NanoHTTPD.IHTTPSession session);
	}

	static class HttpServer extends NanoHTTPD implements AutoCloseable {

		private final Listener listener;

		public HttpServer(int port, Listener listener) throws IOException {
			super("localhost", port);
			this.listener = listener;
			start(NanoHTTPD.SOCKET_READ_TIMEOUT, true);
		}

		@Override
		public void close() {
			this.stop();
		}

		public String serverUrl() {
			return "http://" + getHostname() + ":" + getListeningPort();
		}

		public URL urlOf(String resourceName) throws MalformedURLException {
			return new URL(serverUrl() + "/" + resourceName);
		}

		@Override
		public Response serve(IHTTPSession session) {
			Optional<Response> response = listener.serve(session);
			return response
				.orElseGet(() -> super.serve(session));
		}
	}

	static class HttpsServer extends NanoHTTPD implements AutoCloseable {

		private final Listener listener;

		public HttpsServer(int port, Listener listener) throws IOException {
			super("localhost", port);

			// keytool -genkey -keyalg RSA -alias selfsigned -keystore keystore.jks -storepass password -validity 360 -keysize 2048 -ext SAN=DNS:localhost,IP:127.0.0.1  -validity 9999
			makeSecure(NanoHTTPD.makeSSLSocketFactory("/localhost-keystore.jks", "password".toCharArray()), null);

			this.listener = listener;
			start(NanoHTTPD.SOCKET_READ_TIMEOUT, true);
		}

		@Override
		public void close() {
			this.stop();
		}

		public String serverUrl() {
			return "https://" + getHostname() + ":" + getListeningPort();
		}

		public URL urlOf(String resourceName) throws MalformedURLException {
			return new URL(serverUrl() + "/" + resourceName);
		}

		@Override
		public Response serve(IHTTPSession session) {
			Optional<Response> response = listener.serve(session);
			return response
				.orElseGet(() -> super.serve(session));
		}
	}

	public static class HttpsProxyServer implements AutoCloseable {

		private final int port;
		private final ServerThread server;

		public HttpsProxyServer(int port) throws IOException {
			this.port = port;
			this.server = new ServerThread(port);
			this.server.start();
		}

		public String getHostname() {
			return "localhost";
		}

		public int getListeningPort() {
			return port;
		}

		@Override
		public void close() {
			this.server.close();
		}

		static class ServerThread extends Thread {

			private final int port;
			private final ServerSocket serverSocket;
			private AtomicBoolean running=new AtomicBoolean(true);

			private ServerThread(int port) throws IOException {
				this.port = port;
				this.serverSocket = new ServerSocket(port);
			}

			@Override
			public void run() {
				Socket socket;
				try {
					while (running.get() && (socket = serverSocket.accept()) != null) {
						WorkerThread worker = new WorkerThread(socket);
						worker.start();
					}
				} catch (IOException iox) {
					if (running.get()) throw new RuntimeException(iox);
				}
			}

			public void close() {
				try {
					serverSocket.close();
				}
				catch (IOException e) {
					throw new RuntimeException(e);
				} finally {
					try {
						running.set(false);
						join();
					}
					catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
			}
		}

		static class WorkerThread extends Thread {
			private static final Pattern CONNECT_PATTERN = Pattern.compile("CONNECT (?<host>.+):(?<port>.+) HTTP/(?<httpVersion>1\\.[01])\r\n", Pattern.CASE_INSENSITIVE);
			private static final Pattern HEADER_PATTERN = Pattern.compile("(?<key>.+):(?<value>.+)\r\n");

			private final Socket client;
			private final OutputStream clientOutputStream;
			private final InputStream clientInputStream;

			public WorkerThread(Socket client) throws IOException {
				this.client = client;
				this.clientOutputStream = client.getOutputStream();
				this.clientInputStream = client.getInputStream();
			}

			@Override
			public void run() {
				try {
					String line = readLine(client.getInputStream());
					Matcher matcher = CONNECT_PATTERN.matcher(line);
					if (matcher.matches()) {
						String host = matcher.group("host");
						String port = matcher.group("port");

						Map<String, String> headers=new LinkedHashMap<>();
						String headerLine;
						do {
							headerLine = readLine(clientInputStream);
							Matcher header = HEADER_PATTERN.matcher(headerLine);
							if (header.matches()) {
								headers.put(header.group("key"), header.group("value"));
							}
						} while (!headerLine.equals("\r\n"));

						headers.forEach((key,val) -> System.out.println(key+": "+val));

						Socket destination = new Socket(host, Integer.parseInt(port));
						response(200, "Connection established");
						InputStream serverInputStream = destination.getInputStream();
						OutputStream serverOutputStream = destination.getOutputStream();

						Thread clientToServer=new Thread(() -> {
							try {
								pipeData(clientInputStream, serverOutputStream);
							}
							catch (IOException e) {
								throw new RuntimeException(e);
							}
						});
						clientToServer.start();

						Thread serverToClient=new Thread(() -> {
							try {
								pipeData(serverInputStream, clientOutputStream);
							}
							catch (IOException e) {
								throw new RuntimeException(e);
							}
						});
						serverToClient.start();

						try {
							clientToServer.join();
							serverToClient.join();
						}
						catch (InterruptedException e) {
							throw new RuntimeException(e);
						}

						destination.close();
						System.out.println("close connection");
					} else {
						response(502, "Bad Gateway");
					}
				}
				catch (IOException e) {
					throw new RuntimeException(e);
				}
			}

			private void pipeData(InputStream inputStream, OutputStream outputStream) throws IOException {
				byte[] buffer = new byte[4096];
				int read;
				do {
					read = inputStream.read(buffer);
					if (read > 0) {
						outputStream.write(buffer, 0, read);
						if (inputStream.available() < 1) {
							outputStream.flush();
						}
					}
				} while (read >= 0);
			}

			private void response(int statusCode, String statusCodeLabel) throws IOException {
				response(new OutputStreamWriter(clientOutputStream), statusCode, statusCodeLabel, Pair.of("Proxy-agent","MockProxy/0.1"));
			}

			private static void response(OutputStreamWriter os, int statusCode, String statusCodeLabel, Pair<String, String> ... header) throws IOException {
				Stream<String> firstLine = Stream.of("HTTP/1.0 " + statusCode + " " + statusCodeLabel);
				Stream<String> headerStream = Arrays.stream(header).map(it -> it.first() + ": " + it.second());
				response(os, Stream.concat(firstLine, headerStream)
					.collect(Collectors.toList()));
			}

			private static void response(OutputStreamWriter os, Collection<String> lines) throws IOException {
				for (String line : lines) {
					os.write(line);
					os.write("\r\n");
				}
				os.write("\r\n");
				os.flush();
			}

			private static String readLine(InputStream is) throws IOException {
				ByteArrayOutputStream os=new ByteArrayOutputStream();
				int next;

				boolean lastOneWasCR = false;
				boolean done = false;

				while (!done && (next = is.read()) != -1) {
					os.write(next);

					switch (next) {
						case '\r':
							lastOneWasCR=true;
							break;
						case '\n':
							if (lastOneWasCR) {
								done=true;
							}
							break;
						default:
							lastOneWasCR=false;
							break;
					}
				}

				return os.toString("ISO-8859-1");
			}
		}
	}
}
