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

# GraphQL over HTTP support

You can call GraphQL over HTTP services in http file, and request as following:

```
### graphql test
//@name graphqlTest
GRAPHQL https://localhost:8787/graphql

query {
   welcome(name : "{{nick}}" )
}
```

Then create method API and GraphqlResponse record:

```java

@HttpFile("httpbin.http")
public interface HttpBinService {

    @HttpRequestName("graphqlTest")
    GraphqlResponse graphqlTest(String nick);

    record GraphqlResponse(Map<String, Object> data, Map<String, Object> extensions, List<Object> errors) {
    }
}
```

# Global variables support

* `$uuid`, `$timestamp` and `$randomInt`
* `$random` object: `$random.integer`, `$random.float`, `$random.alphabetic`, `$random.alphanumeric`, `$random.hexadecimal` and `$random.email`

# References

* Spring 6.0 HTTP Interface: https://docs.spring.io/spring-framework/docs/6.0.0-RC2/reference/html/integration.html#rest-http-interface
  