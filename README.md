httpfile Spring
==================

Use Java interface and http file to access remote HTTP/GraphQL services.

# How to get started?

* Create index.http file in resources folder, such as `src/main/resources/httpbin.http`, and code as following:

```
### get my ip
//@name myIp
GET https://httpbin.org/ip

### Post test
//@name postTest
POST https://httpbin.org/post
Content-Type: application/json

{
  "hello": "{{nick}}"
}
```

* Create Java interface and bind with http file.

```java
import java.util.Map;

@HttpFile("httpbin.http")
public interface HttpBinService {
    @HttpRequestName("myIp")
    MyIp myIp();

    @HttpRequestName("postTest")
    PostResponse post(String nick);

    record MyIp(String origin) {
    }

    record PostResponse(String url, Map<String, String> headers, String data) {
    }
}
```

* Build proxy for HTTP request interface and execute request.

```
 HttpFileProxyFactory httpFileProxyFactory = new HttpFileProxyFactory(WebClientAdapter.forClient(WebClient.builder().build()));
 httpBinService = httpFileProxyFactory.createClient(HttpBinService.class);
 String ip = httpBinService.myIp().origin();
```

# References

* Spring 6.0 HTTP Interface: https://docs.spring.io/spring-framework/docs/6.0.0-M5/reference/html/integration.html#rest-http-interface
  