package sh.servicex.httpfile.parse;

import org.junit.jupiter.api.Test;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * httpfile parser
 *
 * @author linux_china
 */
public class HttpFileParserTest {

    @Test
    public void testParse() throws Exception {
        final String httpfileContent = StreamUtils.copyToString(this.getClass().getClassLoader().getResourceAsStream("httpbin.http"), StandardCharsets.UTF_8);
        final List<HttpFileRequest> requests = HttpFileParser.parse(httpfileContent);
        for (HttpFileRequest request : requests) {
            System.out.println(request.getName());
        }
    }
}
