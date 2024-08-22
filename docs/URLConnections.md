## URL Connections

download from an url connection into a byte array:

```java
URLConnection connection = URLConnections.urlConnectionOf(downloadUrl);
byte[] response = URLConnections.downloadIntoByteArray(connection, (url, bytesCopied, contentLength) -> {

});
```

download from an url connection into a file:

```java
URLConnection connection = URLConnections.urlConnectionOf(downloadUrl);
URLConnections.downloadIntoFile(connection, file, (url, bytesCopied, contentLength) -> {

});
```
                        
### Enable Env Variable HttpProxy Detection

set system property `de.flapdoodle.net.useEnvProxySelector` to true to enable `http_proxy`, `https_proxy` and `no_proxy` env variable support. 