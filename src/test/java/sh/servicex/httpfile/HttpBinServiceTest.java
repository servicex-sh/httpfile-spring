package sh.servicex.httpfile;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.StreamUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import sh.servicex.httpfile.spring.HttpFileProxyFactory;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

public class HttpBinServiceTest {
    private static HttpBinService httpBinService;

    @BeforeAll
    public static void setUp() {
        HttpFileProxyFactory httpFileProxyFactory = new HttpFileProxyFactory(WebClientAdapter.forClient(WebClient.builder().build()));
        httpBinService = httpFileProxyFactory.createClient(HttpBinService.class);
    }

    @Test
    public void testHttpGetIp() throws Exception {
        System.out.println(httpBinService.myIp().origin());
    }

    @Test
    public void testHttpPostDemo() throws Exception {
        System.out.println(httpBinService.postTest("linux_china").data());
    }

    @Test
    public void testGraphqlDemo() throws Exception {
        System.out.println(httpBinService.graphqlTest("linux_china").data());
    }

    @Test
    public void testRequest() {
        for (Method method : HttpBinService.class.getMethods()) {
            final boolean matched = AnnotatedElementUtils.hasAnnotation(method, HttpRequestName.class);
            if (matched) {
                System.out.println(method.getName());
            }
        }
    }

    @Test
    public void testReadHttpFile() throws Exception {
        final String httpFileText = StreamUtils.copyToString(this.getClass().getResourceAsStream("/httpbin.http"), StandardCharsets.UTF_8);
        System.out.println(httpFileText);
    }
}
