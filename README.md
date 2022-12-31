httpfile Spring
==================

Use Java interface and http file to access remote HTTP/GraphQL services.

# Why Java http interface bound with http file?

* http file is easy to write and test
* If you have http files already, why not reuse them?

# How to get started?

* Add `httpfile-spring` dependency in pom.xml

```xml

<dependency>
    <groupId>org.mvnsearch</groupId>
    <artifactId>httpfile-spring</artifactId>
    <version>1.0.1</version>
</dependency>
```

* Create http file in resources folder, such as `src/main/resources/httpbin.http`, and code as following:

```
### get my ip
#@name myIp
GET https://httpbin.org/ip

### Post test
#@name postTest
POST https://httpbin.org/post
Content-Type: application/json

{
  "hello": "{{nick}}"
}
```

* Create Java interface and bind with http file: method param names should be same with variables in http file.

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
 WebClient webClient = WebClient.builder().build();
 HttpFileProxyFactory httpFileProxyFactory = HttpFileProxyFactory.builder(WebClientAdapter.forClient(webClient)).build();
 httpBinService = httpFileProxyFactory.createClient(HttpBinService.class);
 String ip = httpBinService.myIp().origin();
```

# GraphQL over HTTP support

You can call GraphQL over HTTP services in http file, and request as following:

```
### graphql test
#@name graphqlTest
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

# FAQ

### Global context variables support

Some variables, such as `host`, `token`, shared by multi requests, and these variables could be global.
You can build HTTP service interface with global context as following:

```
Map<String, String> globalContext = new HashMap<>();
globalContext.put("host", "httpbin.org");
//...
httpBinService = httpFileProxyFactory.createClient(HttpBinService.class, globalContext);
```

### How to mock http request?

You can mock http request by using `#@mock` tag in http file, such as:

```
### get my ip
#@name myIp
#@mock {"origin":"127.0.0.1"}
GET https://{{host}}/ip
```

**Attention**: mock feature is disabled by default, and you should enable with following code:

```
Map<String, String> globalContext = new HashMap<>();
globalContext.put("http.mock", "true");
//
//...
httpBinService = httpFileProxyFactory.createClient(HttpBinService.class, globalContext);
```

或者通过System Properties进行设置，这样就可以通过命令行参数进行设置。

```
System.getProperties().put("http.mock","true");
```

如果使用Spring Boot的话，你可以通过properties key进行设置，如下:

```
@Value("${http.mock:false}")
private String httpMock;

Map<String, String> globalContext = new HashMap<>();
globalContext.put("http.mock", httpMock);
```

# References

* Spring 6.0 HTTP Interface: https://docs.spring.io/spring-framework/docs/6.0.0/reference/html/integration.html#rest-http-interface
* JetBrains HTTP Client: https://www.jetbrains.com/help/idea/http-client-in-product-code-editor.html
  