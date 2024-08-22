package de.flapdoodle.net;

import de.flapdoodle.checks.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.quote;

public class EnvProxySelector implements ProxySelector {
	private static final Logger logger= LoggerFactory.getLogger(EnvProxySelector.class.getName());

	private final ProxySelector httpProxySelector;
	private final ProxySelector httpsProxySelector;

	private EnvProxySelector(ProxySelector httpProxySelector, ProxySelector httpsProxySelector) {
		logger.info("http proxy selector {}", httpProxySelector);
		logger.info("https proxy selector {}", httpsProxySelector);
		this.httpProxySelector = httpProxySelector;
		this.httpsProxySelector = httpsProxySelector;
	}

	@Override
	public Optional<ProxyFactory> select(URL url) {
		Preconditions.checkNotNull(url, "url is null");
		String protocol = url.getProtocol();

		if (protocol.equals("http")) {
			return httpProxySelector.select(url);
		}
		if (protocol.equals("https")) {
			return httpsProxySelector.select(url);
		}

		return Optional.empty();
	}

	private static Optional<Pattern> noProxy(String noProxy) {
		return noProxy != null
			? toPattern(noProxy)
			: Optional.empty();
	}

	private static ProxySelector proxy(String proxyUrl, Optional<Pattern> noProxyPattern) {
		if (proxyUrl != null) {
			URI proxyUri = URI.create(proxyUrl);
			String proxyHostName = proxyUri.getHost();
			int proxyPort = proxyUri.getPort();

			Preconditions.checkArgument(proxyUri.getUserInfo() == null,"UserInfo not supported: %s", proxyUri.getUserInfo());

			return new HostnameBasesProxySelector(proxyHostName, proxyPort, noProxyPattern);
		}
		return ProxySelector.noProxy();
	}

	static class HostnameBasesProxySelector implements ProxySelector {

		private final String proxyHostName;
		private final int proxyPort;
		private final Optional<Pattern> noProxyPattern;

		private HostnameBasesProxySelector(String proxyHostName, int proxyPort, Optional<Pattern> noProxyPattern) {
			this.proxyHostName = proxyHostName;
			this.proxyPort = proxyPort;
			this.noProxyPattern = noProxyPattern;
		}

		@Override
		public String toString() {
			return "HostnameBasesProxySelector{" +
				"proxyHostName='" + proxyHostName + '\'' +
				", proxyPort=" + proxyPort +
				", noProxyPattern=" + noProxyPattern +
				'}';
		}
		
		@Override
		public Optional<ProxyFactory> select(URL url) {
			Preconditions.checkNotNull(url, "url is null");

			if (useProxy(noProxyPattern, url)) {
				return Optional.of(ProxyFactory.of(proxyHostName, proxyPort));
			}
			return Optional.empty();
		}

		/**
		 * Adaptation of sun.net.spi.DefaultProxySelector
		 */
		private boolean useProxy(Optional<Pattern> noProxyPattern, URL url) {
			if (noProxyPattern.isPresent()) {
				String host = url.getHost();
				if (host == null) {
					// This is a hack to ensure backward compatibility in two
					// cases: 1. hostnames contain non-ascii characters,
					// internationalized domain names. in which case, URI will
					// return null, see BugID 4957669; 2. Some hostnames can
					// contain '_' chars even though it's not supposed to be
					// legal, in which case URI will return null for getHost,
					// but not for getAuthority() See BugID 4913253
					String auth = url.getAuthority();
					if (auth != null) {
						int i;
						i = auth.indexOf('@');
						if (i >= 0) {
							auth = auth.substring(i+1);
						}
						i = auth.lastIndexOf(':');
						if (i >= 0) {
							auth = auth.substring(0,i);
						}
						host = auth;
					}
				}
				return host==null || !noProxyPattern.get().matcher(host.toLowerCase()).matches();
			}
			return true;
		}
	}

	/**
	 * Adaptation of sun.net.spi.DefaultProxySelector
	 * 
	 * @param mask non-null mask
	 * @return {@link java.util.regex.Pattern} corresponding to this mask
	 * or {@code null} in case mask should not match anything
	 */
	static Optional<Pattern> toPattern(String mask) {
		boolean disjunctionEmpty = true;
		StringJoiner joiner = new StringJoiner("|");
		for (String disjunct : mask.split(",")) {
			if (disjunct.isEmpty())
				continue;
			disjunctionEmpty = false;
			String regex = disjunctToRegex(disjunct.toLowerCase());
			joiner.add(regex);
		}
		return !disjunctionEmpty
			? Optional.of(Pattern.compile(joiner.toString()))
			: Optional.empty();
	}

	/**
	 * @param disjunct non-null mask disjunct
	 * @return java regex string corresponding to this mask
	 */
	static String disjunctToRegex(String disjunct) {
		String regex;
		if (disjunct.startsWith("*") && disjunct.endsWith("*")) {
			regex = ".*" + quote(disjunct.substring(1, disjunct.length() - 1)) + ".*";
		} else if (disjunct.startsWith("*")) {
			regex = ".*" + quote(disjunct.substring(1));
		} else if (disjunct.endsWith("*")) {
			regex = quote(disjunct.substring(0, disjunct.length() - 1)) + ".*";
		} else {
			regex = quote(disjunct);
		}
		return regex;
	}

	public static ProxySelector with(Map<String, String> env) {
		String http_proxy = env.get("http_proxy");
		String https_proxy = env.get("https_proxy");
		String no_proxy = env.get("no_proxy");

		if (http_proxy != null || https_proxy != null) {
			Optional<Pattern> noProxyPattern = noProxy(no_proxy);

			return new EnvProxySelector(
				proxy(http_proxy, noProxyPattern),
				proxy(https_proxy, noProxyPattern)
			);
		}

		return ProxySelector.noProxy();
	}
}
