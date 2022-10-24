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
    private static final String ALPHABET_LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String ALPHANUMERIC = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
    private static final String HEXADECIMAL = "ABCDEF1234567890";

    public RequestContextVariableValueResolver(Properties properties) {
        this.resolver = placeholderName -> {
            if (placeholderName.equalsIgnoreCase("$uuid") || placeholderName.equalsIgnoreCase("$random.uuid")) {
                return UUID.randomUUID().toString();
            } else if (placeholderName.equalsIgnoreCase("$timestamp")) {
                return String.valueOf(System.currentTimeMillis() / 1000);
            } else if (placeholderName.equalsIgnoreCase("$randomInt") || placeholderName.equalsIgnoreCase("$random.integer")) {
                return String.valueOf(random.nextInt(0, 1000));
            } else if (placeholderName.equalsIgnoreCase("$random.float")) {
                return String.valueOf(new Random().nextFloat());
            } else if (placeholderName.equalsIgnoreCase("$random.alphabetic")) {
                Random r = new Random();
                final int alphabetLength = ALPHABET.length();
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < 16; i++) {
                    builder.append(ALPHABET.charAt(r.nextInt(alphabetLength)));
                }
                return builder.toString();
            } else if (placeholderName.equalsIgnoreCase("$random.alphanumeric")) {
                Random r = new Random();
                final int alphabetLength = ALPHANUMERIC.length();
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < 16; i++) {
                    builder.append(ALPHANUMERIC.charAt(r.nextInt(alphabetLength)));
                }
                return builder.toString();
            } else if (placeholderName.equalsIgnoreCase("$random.hexadecimal")) {
                Random r = new Random();
                final int alphabetLength = HEXADECIMAL.length();
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < 16; i++) {
                    builder.append(HEXADECIMAL.charAt(r.nextInt(alphabetLength)));
                }
                return builder.toString();
            } else if (placeholderName.equalsIgnoreCase("$random.email")) {
                Random r = new Random();
                final int alphabetLength = ALPHABET_LOWERCASE.length();
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < 16; i++) {
                    builder.append(ALPHABET_LOWERCASE.charAt(r.nextInt(alphabetLength)));
                }
                return builder.append("@example.com").toString();
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
