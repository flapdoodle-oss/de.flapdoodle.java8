## URL Connections

download from an url connection into a byte array:

```java
${connectionAndDownloadToByteArray}
```

download from an url connection into a file:

```java
${connectionAndDownloadToFile}
```
                        
### Enable Env Variable HttpProxy Detection

set system property `${enableEnvVariableProxyUsage.envVar}` to true to enable `http_proxy`, `https_proxy` and `no_proxy` env variable support. 