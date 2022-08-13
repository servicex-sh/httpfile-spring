package sh.servicex.httpfile.parse;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@SuppressWarnings("unused")
public class HttpRequestTarget {
    private String method;
    private String requestLine;
    private String host;
    private String pathAbsolute;
    private int port;
    private String schema;
    private String requestUri;

    public String getRequestLine() {
        return requestLine;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPathAbsolute() {
        return pathAbsolute;
    }

    public void setPathAbsolute(String pathAbsolute) {
        this.pathAbsolute = pathAbsolute;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String toUriText() {
        if (requestUri != null && !requestUri.startsWith("/")) {
            return requestUri;
        }
        StringBuilder builder = new StringBuilder();
        builder.append(schema).append("://");
        builder.append(host);
        if (port > 0) {
            builder.append(":").append(port);
        }
        if (pathAbsolute != null) {
            final String encodedPath = URLEncoder.encode(pathAbsolute, StandardCharsets.UTF_8).replaceAll("%2F", "/");
            if (encodedPath.startsWith("/")) {
                builder.append(encodedPath);
            } else {
                builder.append("/").append(encodedPath);
            }
        }
        return builder.toString();
    }

    public void setHostOrUriHeader(String headerName, String headerValue) {
        if (this.requestUri == null || requestUri.startsWith("/")) {
            if (headerValue.contains("://")) { // URI
                final URI uri = URI.create(headerValue);
                if (pathAbsolute == null) {
                    this.pathAbsolute = host;
                }
                this.host = uri.getHost();
                this.schema = uri.getScheme();
                this.port = uri.getPort();
                final String rawPath = uri.getRawPath();
                if (rawPath != null && !rawPath.equals("/")) {
                    if (pathAbsolute == null) {
                        this.pathAbsolute = rawPath;
                    } else {
                        if (!(rawPath.endsWith("/") || this.pathAbsolute.startsWith("/"))) {
                            this.pathAbsolute = rawPath + "/" + this.pathAbsolute;
                        } else {
                            this.pathAbsolute = rawPath + this.pathAbsolute;
                        }
                    }
                }
            } else if (headerValue.contains(":")) { // host and port
                final String[] parts = headerValue.split(":", 2);
                if (pathAbsolute == null) {
                    this.pathAbsolute = host;
                }
                this.host = parts[0];
                this.port = Integer.parseInt(parts[1]);
            } else {  // host only
                if (pathAbsolute == null) {
                    this.pathAbsolute = host;
                }
                this.host = headerValue;
            }
        }
    }

    public static HttpRequestTarget valueOf(String method, String requestLine) {
        final HttpRequestTarget requestTarget = new HttpRequestTarget();
        requestTarget.method = method;
        requestTarget.requestLine = requestLine;
        String requestUri = requestLine;
        if (requestLine.contains(" HTTP/")) {  // request line with protocol `GET /index.html HTTP/1.1`
            requestUri = requestLine.substring(0, requestLine.lastIndexOf(" "));
            final String protocol = requestLine.substring(requestLine.lastIndexOf(" ") + 1);
            if (protocol.contains("HTTPS")) {
                requestTarget.schema = "https://";
            }
        }
        requestTarget.requestUri = requestUri;
        return requestTarget;
    }
}
