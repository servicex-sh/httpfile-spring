package sh.servicex.httpfile.spring;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.ReactiveAdapterRegistry;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.Assert;
import org.springframework.util.StreamUtils;
import org.springframework.web.service.invoker.HttpClientAdapter;
import sh.servicex.httpfile.HttpFile;
import sh.servicex.httpfile.HttpRequestName;
import sh.servicex.httpfile.parse.HttpFileParser;
import sh.servicex.httpfile.parse.HttpFileRequest;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class HttpFileProxyFactory {
    private final HttpClientAdapter clientAdapter;
    private Duration blockTimeout = Duration.ofSeconds(5);
    private ReactiveAdapterRegistry reactiveAdapterRegistry = ReactiveAdapterRegistry.getSharedInstance();

    private HttpFileProxyFactory(HttpClientAdapter clientAdapter) {
        Assert.notNull(clientAdapter, "HttpClientAdapter is required");
        this.clientAdapter = clientAdapter;
    }

    public static HttpFileProxyFactory.Builder builder(HttpClientAdapter clientAdapter) {
        return new HttpFileProxyFactory.Builder().clientAdapter(clientAdapter);
    }

    public <S> S createClient(Class<S> serviceType) {
        return createClient(serviceType, Collections.emptyMap());
    }

    public <S> S createClient(Class<S> serviceType, Map<String, String> globalContext) {
        // parse http file
        try {
            final HttpFile httpFileAnnotation = AnnotatedElementUtils.getMergedAnnotation(serviceType, HttpFile.class);
            String httpFilePath = httpFileAnnotation.value();
            if (!httpFilePath.startsWith("/")) {
                httpFilePath = "/" + httpFilePath;
            }
            final String httpFileText = StreamUtils.copyToString(this.getClass().getResourceAsStream(httpFilePath), StandardCharsets.UTF_8);
            final Map<String, HttpFileRequest> httpFileRequestMap = HttpFileParser.parse(httpFileText).stream().collect(Collectors.toMap(HttpFileRequest::getName, Function.identity()));
            List<HttpRequestStub> httpServiceMethods = MethodIntrospector.selectMethods(serviceType, this::isHttpRequestMethod).stream().map(method -> createHttpServiceMethod(serviceType, method, httpFileRequestMap, globalContext)).toList();
            return ProxyFactory.getProxy(serviceType, new HttpServiceMethodInterceptor(httpServiceMethods));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse http file for  " + serviceType.getCanonicalName(), e);
        }
    }


    private boolean isHttpRequestMethod(Method method) {
        return AnnotatedElementUtils.hasAnnotation(method, HttpRequestName.class);
    }

    private <S> HttpRequestStub createHttpServiceMethod(Class<S> serviceType, Method method, Map<String, HttpFileRequest> httpFileRequestMap, Map<String, String> globalContext) {
        final String requestName = method.getAnnotation(HttpRequestName.class).value();
        final HttpFileRequest httpFileRequest = httpFileRequestMap.get(requestName);
        return new HttpRequestStub(globalContext, method, serviceType, this.clientAdapter, this.reactiveAdapterRegistry, blockTimeout, httpFileRequest);
    }


    public static final class Builder {
        private HttpClientAdapter clientAdapter;
        private Duration blockTimeout = Duration.ofSeconds(5);

        private Builder() {

        }

        public Builder clientAdapter(HttpClientAdapter clientAdapter) {
            this.clientAdapter = clientAdapter;
            return this;
        }

        public Builder blockTimeout(Duration blockTimeout) {
            this.blockTimeout = blockTimeout;
            return this;
        }

        public HttpFileProxyFactory build() {
            Assert.notNull(this.clientAdapter, "HttpClientAdapter is required");
            final HttpFileProxyFactory proxyFactory = new HttpFileProxyFactory(this.clientAdapter);
            proxyFactory.blockTimeout = this.blockTimeout;
            return proxyFactory;
        }

    }
}
