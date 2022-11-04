package sh.servicex.httpfile.spring;

import org.reactivestreams.Publisher;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.ReactiveAdapter;
import org.springframework.core.ReactiveAdapterRegistry;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.web.service.invoker.HttpClientAdapter;
import org.springframework.web.service.invoker.HttpRequestValues;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import sh.servicex.httpfile.parse.HttpFileRequest;
import sh.servicex.httpfile.parse.HttpHeader;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;

public class HttpRequestStub {
    private final Map<String, String> globalContext;
    private final Method method;
    private final Class<?> containingClass;
    private final HttpClientAdapter clientAdapter;
    private HttpMethod httpMethod;
    @Nullable
    private URI uri;
    @Nullable
    private String uriTemplate;
    @Nullable
    private final String body;
    private final boolean variablesInBody;
    private final HttpFileRequest httpFileRequest;
    private final ResponseFunction responseFunction;


    public HttpRequestStub(Map<String, String> globalContext, Method method, Class<?> containingClass, HttpClientAdapter clientAdapter,
                           ReactiveAdapterRegistry reactiveRegistry, Duration blockTimeout,
                           HttpFileRequest httpFileRequest) {
        this.containingClass = containingClass;
        this.globalContext = globalContext;
        this.method = method;
        this.clientAdapter = clientAdapter;
        this.responseFunction = ResponseFunction.create(clientAdapter, method, reactiveRegistry, blockTimeout);
        String uriText = httpFileRequest.getRequestTarget().toUriText();
        if (uriText.contains("{{")) {
            this.uriTemplate = uriText.replaceAll("\\{\\{", "{").replaceAll("}}", "}");
        } else {
            this.uri = URI.create(uriText);
        }
        String methodName = httpFileRequest.getMethod().getName();
        if (httpFileRequest.getMethod().isGraphQLMethod()) {
            methodName = "POST";
        }
        this.httpMethod = HttpMethod.valueOf(methodName);
        this.httpFileRequest = httpFileRequest;
        this.body = httpFileRequest.getBody();
        this.variablesInBody = httpFileRequest.isVariablesInBody();
    }

    public Method getMethod() {
        return method;
    }

    @Nullable
    public Object invoke(Object[] arguments) {
        Properties properties = new Properties();
        if (!globalContext.isEmpty()) {
            properties.putAll(globalContext);
        }
        final Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            String name = parameters[i].getName();
            String value = arguments[i] == null ? "" : arguments[i].toString();
            properties.setProperty(name, value);
        }
        RequestContextVariableValueResolver variableValueResolver = new RequestContextVariableValueResolver(properties);
        final HttpRequestValues.Builder requestValues = HttpRequestValues.builder();
        //set request method
        requestValues.setHttpMethod(this.httpMethod);
        //set request uri
        if (this.uri != null) {
            requestValues.setUri(this.uri);
        } else {
            //noinspection ConstantConditions
            requestValues.setUriTemplate(this.uriTemplate);
            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                requestValues.setUriVariable((String) entry.getKey(), (String) entry.getValue());
            }
        }
        // set http headers
        fillHeaders(requestValues, variableValueResolver);
        // set request body
        if (this.body != null) {
            if (this.variablesInBody) {
                requestValues.setBodyValue(variableValueResolver.resolveStringValue(this.body));
            } else {
                requestValues.setBodyValue(this.body);
            }
        }
        return this.responseFunction.execute(requestValues.build());
    }

    public void fillHeaders(HttpRequestValues.Builder requestValues, RequestContextVariableValueResolver variableValueResolver) {
        final List<HttpHeader> headers = this.httpFileRequest.getHeaders();
        if (!headers.isEmpty()) {
            for (HttpHeader header : headers) {
                String name = header.getName();
                if (header.isVariablesIncluded()) {
                    String resolvedValue = variableValueResolver.resolveStringValue(header.getValue());
                    if (name.equalsIgnoreCase("authorization")) {
                        // Convert `username password` or `username:password` to Base64
                        if (resolvedValue.startsWith("Basic ")) {
                            resolvedValue = HttpFileRequest.base64UserAndPassword(resolvedValue);
                        }
                    }
                    requestValues.addHeader(name, resolvedValue);
                } else {
                    requestValues.addHeader(name, header.getValue());
                }
            }
        }
        if (httpFileRequest.getMethod().isGraphQLMethod()) {
            requestValues.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        }
    }

    /**
     * Code from HttpServiceMethod
     *
     * @see org.springframework.web.service.invoker.HttpServiceMethod
     */
    private record ResponseFunction(
            Function<HttpRequestValues, Publisher<?>> responseFunction,
            @Nullable ReactiveAdapter returnTypeAdapter,
            boolean blockForOptional, Duration blockTimeout) {

        @Nullable
        public Object execute(HttpRequestValues requestValues) {

            Publisher<?> responsePublisher = this.responseFunction.apply(requestValues);

            if (this.returnTypeAdapter != null) {
                return this.returnTypeAdapter.fromPublisher(responsePublisher);
            }

            return (this.blockForOptional ?
                    ((Mono<?>) responsePublisher).blockOptional(this.blockTimeout) :
                    ((Mono<?>) responsePublisher).block(this.blockTimeout));
        }


        /**
         * Create the {@code ResponseFunction} that matches the method's return type.
         */
        public static ResponseFunction create(
                HttpClientAdapter client, Method method, ReactiveAdapterRegistry reactiveRegistry,
                Duration blockTimeout) {

            MethodParameter returnParam = new MethodParameter(method, -1);
            Class<?> returnType = returnParam.getParameterType();
            ReactiveAdapter reactiveAdapter = reactiveRegistry.getAdapter(returnType);

            MethodParameter actualParam = (reactiveAdapter != null ? returnParam.nested() : returnParam.nestedIfOptional());
            Class<?> actualType = actualParam.getNestedParameterType();

            Function<HttpRequestValues, Publisher<?>> responseFunction;
            if (actualType.equals(void.class) || actualType.equals(Void.class)) {
                responseFunction = client::requestToVoid;
            } else if (reactiveAdapter != null && reactiveAdapter.isNoValue()) {
                responseFunction = client::requestToVoid;
            } else if (actualType.equals(HttpHeaders.class)) {
                responseFunction = client::requestToHeaders;
            } else if (actualType.equals(ResponseEntity.class)) {
                MethodParameter bodyParam = actualParam.nested();
                Class<?> bodyType = bodyParam.getNestedParameterType();
                if (bodyType.equals(Void.class)) {
                    responseFunction = client::requestToBodilessEntity;
                } else {
                    ReactiveAdapter bodyAdapter = reactiveRegistry.getAdapter(bodyType);
                    responseFunction = initResponseEntityFunction(client, bodyParam, bodyAdapter);
                }
            } else {
                responseFunction = initBodyFunction(client, actualParam, reactiveAdapter);
            }

            boolean blockForOptional = returnType.equals(Optional.class);
            return new ResponseFunction(responseFunction, reactiveAdapter, blockForOptional, blockTimeout);
        }

        @SuppressWarnings("ConstantConditions")
        private static Function<HttpRequestValues, Publisher<?>> initResponseEntityFunction(
                HttpClientAdapter client, MethodParameter methodParam, @Nullable ReactiveAdapter reactiveAdapter) {

            if (reactiveAdapter == null) {
                return request -> client.requestToEntity(
                        request, ParameterizedTypeReference.forType(methodParam.getNestedGenericParameterType()));
            }

            Assert.isTrue(reactiveAdapter.isMultiValue(),
                    "ResponseEntity body must be a concrete value or a multi-value Publisher");

            ParameterizedTypeReference<?> bodyType =
                    ParameterizedTypeReference.forType(methodParam.nested().getNestedGenericParameterType());

            // Shortcut for Flux
            if (reactiveAdapter.getReactiveType().equals(Flux.class)) {
                return request -> client.requestToEntityFlux(request, bodyType);
            }

            return request -> client.requestToEntityFlux(request, bodyType)
                    .map(entity -> {
                        Object body = reactiveAdapter.fromPublisher(entity.getBody());
                        return new ResponseEntity<>(body, entity.getHeaders(), entity.getStatusCode());
                    });
        }

        private static Function<HttpRequestValues, Publisher<?>> initBodyFunction(
                HttpClientAdapter client, MethodParameter methodParam, @Nullable ReactiveAdapter reactiveAdapter) {

            ParameterizedTypeReference<?> bodyType =
                    ParameterizedTypeReference.forType(methodParam.getNestedGenericParameterType());

            return (reactiveAdapter != null && reactiveAdapter.isMultiValue() ?
                    request -> client.requestToBodyFlux(request, bodyType) :
                    request -> client.requestToBody(request, bodyType));
        }

    }
}
