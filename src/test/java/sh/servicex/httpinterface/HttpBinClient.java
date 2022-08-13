package sh.servicex.httpinterface;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.Map;

@HttpExchange("https://httpbin.org")
public interface HttpBinClient {
    @GetExchange("/ip")
    MyIp myIp();

    @PostExchange("/post")
    PostResponse post(@RequestBody String body);

    public record MyIp(String origin) {
    }

    public record PostResponse(String url, Map<String, String> headers, String data) {
    }
}




