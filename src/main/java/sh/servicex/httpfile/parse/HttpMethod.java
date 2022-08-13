package sh.servicex.httpfile.parse;

import java.util.List;

@SuppressWarnings("unused")
public class HttpMethod {
    public static final List<String> HTTP_METHODS = List.of("GET", "HEAD", "POST", "PUT", "DELETE", "CONNECT", "OPTION", "TRACE", "PATCH");
    public static final List<String> GRAPHQL_METHODS = List.of("GRAPHQL");
    private String name;

    public HttpMethod() {
    }

    public HttpMethod(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static boolean isRequestLine(String line) {
        final int offset = line.indexOf(' ');
        String method;
        if (offset < 0) {
            method = line;
        } else {
            method = line.substring(0, offset);
        }
        return HTTP_METHODS.contains(method) || GRAPHQL_METHODS.contains(method);
    }

    public static HttpMethod valueOf(String methodName) {
        return new HttpMethod(methodName);
    }

    public boolean isHttpMethod() {
        return HTTP_METHODS.contains(this.name);
    }

    public boolean isGraphQLMethod() {
        return GRAPHQL_METHODS.contains(name);
    }

}
