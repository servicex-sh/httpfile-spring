package sh.servicex.httpfile.spring;

import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.util.StringValueResolver;

import javax.annotation.Nonnull;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;

/**
 * request context variable value resolver. if variable not found, return empty string.
 * Internal variables: $uuid, $timestamp, $randomInt
 *
 * @author linux_china
 */
public class RequestContextVariableValueResolver implements StringValueResolver {
    private static final PropertyPlaceholderHelper helper = new PropertyPlaceholderHelper("{{", "}}");
    private static final Random random = new Random();
    private final PropertyPlaceholderHelper.PlaceholderResolver resolver;

    public RequestContextVariableValueResolver(Properties properties) {
        this.resolver = placeholderName -> {
            if (placeholderName.equalsIgnoreCase("$uuid")) {
                return UUID.randomUUID().toString();
            } else if (placeholderName.equalsIgnoreCase("$timestamp")) {
                return String.valueOf(System.currentTimeMillis() / 1000);
            } else if (placeholderName.equalsIgnoreCase("$randomInt")) {
                return String.valueOf(random.nextInt(0, 1000));
            } else {
                return properties.getProperty(placeholderName, "");
            }
        };
    }

    @Nonnull
    @Override
    public String resolveStringValue(@Nonnull String strVal) {
        return helper.replacePlaceholders(strVal, resolver);
    }
}
