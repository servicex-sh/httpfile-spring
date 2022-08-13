package sh.servicex.httpfile;

import java.util.Map;

@HttpFile("httpbin.http")
public interface HttpBinService {
    @HttpRequestName("myIp")
    MyIp myIp();

    @HttpRequestName("postTest")
    PostResponse post(String nick);

    public record MyIp(String origin) {
    }

    public record PostResponse(String url, Map<String, String> headers, String data) {
    }
}




