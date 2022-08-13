package sh.servicex.httpfile.spring;

import org.junit.jupiter.api.Test;

import java.util.Properties;

public class RequestContextVariableValueResolverTest {
    @Test
    public void testReplaceVariables() {
        Properties properties = new Properties();
        properties.setProperty("host", "httpbin.org");
        properties.setProperty("$uuid", "xxx-yyy");
        RequestContextVariableValueResolver helper = new RequestContextVariableValueResolver(properties);
        final String target = helper.resolveStringValue("Hello {{host}} {{$uuid}}, age: {{age}}");
        System.out.println(target);
    }
}
