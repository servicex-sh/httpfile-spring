package sh.servicex.httpfile.parse;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * httpfile parser
 *
 * @author linux_china
 */
public class HttpFileParser {
    public static List<HttpFileRequest> splitRequests(String httpFileCode) {
        List<HttpFileRequest> requests = new ArrayList<>();
        try {
            final BufferedReader bufferedReader = new BufferedReader(new StringReader(httpFileCode));
            List<String> lines = bufferedReader.lines().toList();
            int index = 1;
            int lineNumber = 1;
            //remove shebang
            if (lines.get(0).startsWith("#!/usr/bin/env")) {
                lines = lines.subList(1, lines.size());
                lineNumber++;
            }
            HttpFileRequest httpRequest = new HttpFileRequest(index);
            for (String rawLine : lines) {
                String line = rawLine.trim();
                //noinspection StatementWithEmptyBody
                if (!httpRequest.isFilled() && line.isEmpty()) {  // ignore empty lines before http request

                } else if (line.startsWith("###")) { // comment for httpRequest or new HttpRequest separator
                    String comment = line.substring(3).trim();
                    if (!httpRequest.isFilled()) { // fill information for current httpRequest
                        httpRequest.setComment(line.substring(3).trim());
                    } else {  // start new httpRequest
                        requests.add(httpRequest);
                        index = index + 1;
                        httpRequest = new HttpFileRequest(index);
                        httpRequest.setComment(comment);
                    }
                } else if (!httpRequest.isRequestStarted()) {
                    if ((line.startsWith("#") || line.startsWith("//"))) { //comment
                        String comment = (line.startsWith("#") ? line.substring(1) : line.substring(2)).trim();
                        if (comment.startsWith("@")) { // tag for httpRequest
                            String tag = comment.substring(1);
                            String[] parts = tag.split("[=\\s]+", 2);
                            if (parts[0].equals("name") && parts.length > 1) {
                                httpRequest.setName(parts[1].trim());
                            } else if (parts[0].equals("mock") && parts.length > 1) {
                                httpRequest.setMockResult(parts[1].trim());
                            }
                            httpRequest.addTag(tag);
                        }
                    } else if (HttpMethod.isRequestLine(line)) {   // normal comment
                        int position = line.indexOf(' ');
                        final String method = line.substring(0, position);
                        httpRequest.setMethod(HttpMethod.valueOf(method));
                        httpRequest.setRequestLine(line.substring(position + 1));
                        httpRequest.addRequestLine(rawLine);
                    } else {
                        httpRequest.addPreScriptLine(line);
                    }
                } else {
                    httpRequest.addRequestLine(rawLine);
                }
                httpRequest.addLineNumber(lineNumber);
                lineNumber++;
            }
            if (httpRequest.isFilled()) {  //add last httpRequest
                requests.add(httpRequest);
            }
        } catch (Exception ignore) {
        }
        return requests;
    }

    public static void parse(HttpFileRequest httpRequest) {
        try {
            final BufferedReader bufferedReader = new BufferedReader(new StringReader(httpRequest.getRequestCode()));
            List<String> lines = bufferedReader.lines().toList();
            int offset = 0;
            String requestLine = null;
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (!line.isEmpty() && HttpMethod.isRequestLine(line)) {
                    requestLine = lines.get(i);
                    offset = i;
                }
            }
            // reset request line
            if (requestLine != null) {
                int position = requestLine.indexOf(' ');
                final String method = requestLine.substring(0, position);
                httpRequest.setMethod(HttpMethod.valueOf(method));
                httpRequest.setRequestLine(requestLine.substring(position + 1));
            }
            for (String rawLine : lines.subList(offset + 1, lines.size())) {
                String line = rawLine.trim();
                if (!httpRequest.isBodyStarted()) {
                    if ((rawLine.startsWith("  ") || rawLine.startsWith("\t"))) { // append request line parts in multi lines
                        httpRequest.appendRequestLine(line);
                    } else if (line.indexOf(':') > 0 && !httpRequest.isBodyStarted()) { //http request headers parse: body should be empty
                        int position = line.indexOf(':');
                        final String name = line.substring(0, position).trim();
                        if (name.contains(" ")) {
                            httpRequest.addBodyLine(rawLine);
                            httpRequest.setBodyStarted(true);
                        } else {
                            httpRequest.addHttpHeader(name, line.substring(position + 1).trim());
                        }
                    } else {
                        if (!line.isEmpty()) { // ignore lines between headers and body
                            httpRequest.addBodyLine(rawLine);
                        } else {
                            httpRequest.setBodyStarted(true);
                        }
                    }
                } else {  // parse httpRequest body
                    httpRequest.addBodyLine(rawLine);
                }
            }
        } catch (Exception ignore) {

        }
    }

    public static List<HttpFileRequest> parse(String httpFileCode) {
        final List<HttpFileRequest> requests = splitRequests(httpFileCode);
        for (HttpFileRequest request : requests) {
            try {
                parse(request);
                request.cleanBody();
            } catch (Exception ignore) {
            }
        }
        return requests;
    }

}
