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

    public static List<HttpFileRequest> parse(String httpFileCode) {
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
                } else if (!httpRequest.isBodyStarted()) {
                    if ((line.startsWith("#") || line.startsWith("//"))) { //comment
                        String comment = (line.startsWith("#") ? line.substring(1) : line.substring(2)).trim();
                        if (comment.startsWith("@")) { // tag for httpRequest
                            String tag = comment.substring(1);
                            String[] parts = tag.split("[=\\s]+", 2);
                            if (parts[0].equals("name") && parts.length > 1) {
                                httpRequest.setName(parts[1].trim());
                            }
                            httpRequest.addTag(tag);
                        } else {   // normal comment
                            if (httpRequest.getComment() == null) {
                                httpRequest.setComment(comment);
                            }
                        }
                    } else if (HttpMethod.isRequestLine(line)) {  // request line parse
                        int position = line.indexOf(' ');
                        final String method = line.substring(0, position);
                        httpRequest.setMethod(HttpMethod.valueOf(method));
                        httpRequest.setRequestLine(line.substring(position + 1));
                    } else if ((rawLine.startsWith("  ") || rawLine.startsWith("\t"))) { // append request line parts in multi lines
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
                httpRequest.addLineNumber(lineNumber);
                lineNumber++;
            }
            if (httpRequest.isFilled()) {  //add last httpRequest
                requests.add(httpRequest);
            }
        } catch (Exception ignore) {
        }
        for (HttpFileRequest request : requests) {
            try {
                request.cleanBody();
            } catch (Exception ignore) {
            }
        }
        return requests;
    }

}
