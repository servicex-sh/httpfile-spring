package sh.servicex.httpfile.parse;

import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class HttpFileRequest {
    private Integer index;
    private String name;
    private String comment;
    private List<String> tags;
    private HttpMethod method;
    private String requestLine;
    private List<HttpHeader> headers;
    private boolean bodyStarted = false;
    private List<String> bodyLines;
    private final List<String> requestLines = new ArrayList<>();
    private final List<Integer> lineNumbers = new ArrayList<>();
    private String body;
    private boolean variablesInBody = false;
    private String jsTestCode;
    private String redirectResponse;
    private HttpRequestTarget requestTarget;
    private List<String> preScriptLines;

    public HttpFileRequest() {
    }

    public HttpFileRequest(Integer index) {
        this.index = index;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public String getName() {
        return name == null ? "http" + index : name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public void addTag(String tag) {
        if (this.tags == null) {
            this.tags = new ArrayList<>();
        }
        this.tags.add(tag);
    }

    public boolean containsTag(String name) {
        if (this.tags != null && !this.tags.isEmpty()) {
            for (String tag : tags) {
                if (tag.equalsIgnoreCase(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    public String getRequestLine() {
        return requestLine;
    }

    public void setRequestLine(String requestLine) {
        this.requestLine = requestLine;
    }

    public void appendRequestLine(String requestPart) {
        if (requestLine == null) {
            requestLine = requestPart.trim();
        } else {
            requestLine = requestLine + requestPart.trim();
        }
    }

    public boolean isRequestStarted() {
        return requestLine != null;
    }

    public void addPreScriptLine(String line) {
        if (this.preScriptLines == null) {
            preScriptLines = new ArrayList<>();
        }
        this.preScriptLines.add(line);
    }

    public void addRequestLine(String line) {
        requestLines.add(line);
    }

    public String getRequestCode() {
        return String.join("\n", requestLines);
    }

    public HttpRequestTarget getRequestTarget() {
        if (requestTarget == null && method != null && requestLine != null) {
            requestTarget = HttpRequestTarget.valueOf(method.getName(), requestLine);
        }
        return requestTarget;
    }

    public void setRequestTarget(HttpRequestTarget requestTarget) {
        this.requestTarget = requestTarget;
    }

    @Nonnull
    public List<HttpHeader> getHeaders() {
        return headers == null ? Collections.emptyList() : headers;
    }

    public void setHeaders(List<HttpHeader> headers) {
        this.headers = headers;
    }

    public Map<String, String> getHeadersMap() {
        if (headers == null || headers.isEmpty()) {
            return Map.of();
        } else {
            return headers.stream().collect(Collectors.toMap(HttpHeader::getName, HttpHeader::getValue));
        }
    }

    @Nullable
    public String getHeader(String name) {
        if (this.headers != null) {
            for (HttpHeader header : headers) {
                if (header.getName().equalsIgnoreCase(name)) {
                    return header.getValue();
                }
            }
        }
        return null;
    }

    @Nonnull
    public String getHeader(String name, @Nonnull String defaultValue) {
        if (this.headers != null) {
            for (HttpHeader header : headers) {
                if (header.getName().equalsIgnoreCase(name)) {
                    return header.getValue();
                }
            }
        }
        return defaultValue;
    }

    public static String base64UserAndPassword(String headValue) {
        String token = headValue.substring(6).trim();
        if (token.contains(" ") || token.contains(":")) {
            String text = token.replace(" ", ":");
            return "Basic " + Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8));
        }
        return headValue;
    }

    public void addHttpHeader(String name, String value) {
        if (headers == null) {
            headers = new ArrayList<>();
        }
        if (name.equalsIgnoreCase("authorization")) {
            // Convert `username password` or `username:password` to Base64
            if (value.startsWith("Basic ")) {
                value = base64UserAndPassword(value);
            }
        } else if (name.equalsIgnoreCase("host") || name.equalsIgnoreCase("uri")) {
            HttpRequestTarget requestTarget = getRequestTarget();
            if (requestTarget == null) {
                requestTarget = HttpRequestTarget.valueOf("UNKNOWN", "/");
                this.setRequestTarget(requestTarget);
            }
            requestTarget.setHostOrUriHeader(name, value);
        }
        this.headers.add(new HttpHeader(name, value));
    }

    public void replaceHeader(String name, String value) {
        if (headers != null) {
            HttpHeader header = null;
            for (HttpHeader httpHeader : headers) {
                if (httpHeader.getName().equalsIgnoreCase(name)) {
                    header = httpHeader;
                    break;
                }
            }
            if (header != null) {
                this.headers.remove(header);
            }
            addHttpHeader(name, value);
        }
    }

    public List<String> getBodyLines() {
        return bodyLines;
    }

    public void addLineNumber(int lineNumber) {
        this.lineNumbers.add(lineNumber);
    }

    public boolean containsLineNumber(int lineNumber) {
        return this.lineNumbers.contains(lineNumber);
    }

    public void setBodyLines(List<String> bodyLines) {
        this.bodyLines = bodyLines;
    }

    public void addBodyLine(String line) {
        if (this.bodyLines == null) {
            bodyLines = new ArrayList<>();
        }
        this.bodyLines.add(line);
    }

    public String getRedirectResponse() {
        return this.redirectResponse;
    }

    @Nullable
    public String getJavaScriptTestCode() {
        return this.jsTestCode;
    }


    public String getBody() {
        return this.body;
    }

    public boolean isVariablesInBody() {
        return variablesInBody;
    }

    public String convertToDoubleQuoteString(String text) {
        String escapedText = StringUtils.replace(text, "\"", "\\\"");
        escapedText = StringUtils.replace(escapedText, "\n", "\\n");
        escapedText = StringUtils.replace(escapedText, "\r", "");
        return "\"" + escapedText + "\"";
    }

    public String wrapJsonValue(String value) {
        if (Objects.equals(value, "true") || Objects.equals(value, "false") || Objects.equals(value, "null")) {
            return value;
        } else if (value.startsWith("\"") || value.startsWith("{") || value.startsWith("[")) {
            return value;
        } else if (value.contains("\"")) {
            return convertToDoubleQuoteString(value);
        } else {
            try {
                Double.parseDouble(value);
                return value;
            } catch (Exception ignore) {
                return "\"" + value + "\"";
            }
        }
    }

    public boolean isFilled() {
        return method != null && requestLine != null;
    }


    public boolean isBodyEmpty() {
        return bodyLines == null || bodyLines.isEmpty();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isBodyStarted() {
        return bodyStarted;
    }

    public void setBodyStarted(boolean bodyStarted) {
        if (method != null) {
            this.bodyStarted = bodyStarted;
        }
    }

    public void cleanBody() throws Exception {
        if (bodyLines != null && !bodyLines.isEmpty()) {
            int offset = 0;
            boolean bodyFromExternal = false;
            if (bodyLines.get(0).startsWith("< ")) { // load body from an external file
                String firstLine = bodyLines.get(0);
                String fileName = firstLine.substring(2).trim();
                this.body = StreamUtils.copyToString(this.getClass().getResourceAsStream(fileName), StandardCharsets.UTF_8);
                bodyFromExternal = true;
                offset = 1;
            }
            List<String> lines = new ArrayList<>();
            for (String bodyLine : bodyLines.subList(offset, bodyLines.size())) {
                if (!bodyLine.startsWith("<>")) {
                    lines.add(bodyLine);
                }
            }
            // extract js code block
            int jsScriptStartOffset = lines.size();
            int jsScriptEndOffset = -1;
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.startsWith("> {%")) {
                    jsScriptStartOffset = i;
                }
                if (line.equals("%}") && i > jsScriptStartOffset) {
                    jsScriptEndOffset = i;
                    break;
                }
            }
            if (jsScriptEndOffset > 0) { // javascript test code found
                this.jsTestCode = String.join(System.lineSeparator(), lines.subList(jsScriptStartOffset + 1, jsScriptEndOffset));
                List<String> cleanLines = new ArrayList<>();
                cleanLines.addAll(lines.subList(0, jsScriptStartOffset));
                cleanLines.addAll(lines.subList(jsScriptEndOffset + 1, lines.size()));
                lines = cleanLines;
            }
            // extract js file '> /path/to/responseHandler.js'
            List<String> jsHandlerFiles = new ArrayList<>();
            for (String line : lines) {
                if (line.startsWith("> ") && line.endsWith(".js")) { // response redirect
                    jsHandlerFiles.add(line);
                }
            }
            if (!jsHandlerFiles.isEmpty()) {
                lines.removeAll(jsHandlerFiles);
                //read js block from files
            }
            //extract redirect response file
            for (String line : lines) {
                if (line.startsWith(">> ") || line.startsWith(">>! ")) { // response redirect
                    this.redirectResponse = line;
                }
            }
            if (this.redirectResponse != null) {
                lines.remove(this.redirectResponse);
            }
            if (!bodyFromExternal) { // process body from lines
                if (!lines.isEmpty()) {
                    //remove empty lines after body
                    while (lines.get(lines.size() - 1).isEmpty()) {
                        lines.remove(lines.size() - 1);
                        if (lines.isEmpty()) {
                            break;
                        }
                    }
                    if (!lines.isEmpty()) {
                        if (method.isGraphQLMethod()) {  // convert GraphQL to JSON
                            final int json_offset = lines.lastIndexOf("{");
                            String lastLine = lines.get(lines.size() - 1);
                            StringBuilder builder = new StringBuilder();
                            boolean variablesIncluded = false;
                            builder.append("{\"query\":\"");
                            if (json_offset > 0 && lastLine.endsWith("}")) {

                                String query = String.join("\n", lines.subList(0, json_offset));
                                String jsonText = String.join("\n", lines.subList(json_offset, lines.size()));
                                if (jsonText.contains("\"")) {
                                    variablesIncluded = true;
                                    query = StringUtils.replace(query, "\"", "\\\"");
                                    query = StringUtils.replace(query, "\n", "\\n");
                                    builder.append(query);
                                    builder.append("\",\"variables\":");
                                    builder.append(jsonText);
                                    builder.append("}");
                                }
                            }
                            if (!variablesIncluded) {
                                String bodyText = String.join("\n", lines);
                                bodyText = StringUtils.replace(bodyText, "\"", "\\\"");
                                bodyText = StringUtils.replace(bodyText, "\n", "\\n");
                                builder.append(bodyText);
                                builder.append("\"}");
                            }
                            this.body = builder.toString();
                        } else {
                            String content = String.join(System.lineSeparator(), lines);
                            String contentType = getHeader("Content-Type");
                            // Fix https://youtrack.jetbrains.com/issue/IDEA-281753/Support-formatting-for-POST-request-body-for-application-x-www-f
                            if (contentType != null && contentType.equalsIgnoreCase("application/x-www-form-urlencoded") && content.contains("\n")) {
                                content = StringUtils.replace(content, "\n", "");
                                content = StringUtils.replace(content, "\r", "");
                            }
                            this.body = content;
                        }
                    }
                }
            }
        }
        if (body != null && this.body.contains("{{")) {
            this.variablesInBody = true;
        }
    }
}

