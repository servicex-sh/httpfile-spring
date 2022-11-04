package sh.servicex.httpinterface;

import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

public class HttpBinClientTest {
    @Test
    public void testClient() throws Exception {
        WebClient webClient = WebClient.builder().build();
        final HttpServiceProxyFactory httpServiceProxyFactory = HttpServiceProxyFactory.builder(WebClientAdapter.forClient(webClient)).build();
        httpServiceProxyFactory.afterPropertiesSet();
        HttpBinClient httpBinClient = httpServiceProxyFactory.createClient(HttpBinClient.class);
        System.out.println(httpBinClient.myIp().origin());
        System.out.println(httpBinClient.post("Hello").data());
    }
}
