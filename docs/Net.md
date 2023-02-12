## Networking

obtain free server port:
   
```java
int freeServerPort = Net.freeServerPort();
```

obtain free server port for any host address:

```java
int freeServerPort = Net.freeServerPort(hostAddress);
```

is localhost an IPv6 address:

```java
boolean isIpv6 = Net.localhostIsIPv6();
```

                              
create SSLContext to accept any SSL certificate:

```java
HttpsURLConnection connection = (HttpsURLConnection) URLConnections.urlConnectionOf(httpsUrl);
connection.setSSLSocketFactory(Net.acceptAllSSLContext().getSocketFactory());
```