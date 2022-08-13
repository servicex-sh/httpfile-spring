package sh.servicex.httpfile;

import java.util.List;
import java.util.Map;

@HttpFile("httpbin.http")
public interface HttpBinService {
    @HttpRequestName("myIp")
    MyIp myIp();

    @HttpRequestName("postTest")
    PostResponse postTest(String nick);

    @HttpRequestName("graphqlTest")
    GraphqlResponse graphqlTest(String nick);

    record MyIp(String origin) {
    }

    record PostResponse(String url, Map<String, String> headers, String data) {
    }

    record GraphqlResponse(Map<String, Object> data, Map<String, Object> extensions, List<Object> errors) {
    }
}




