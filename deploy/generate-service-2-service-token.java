import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

public class GenerateServiceToken {

    /* HOW TO USE
        java generate-service-2-service-token.java --secret=<jwt_secret> --days=365
    */

    private static final String SYSTEM_GUID = "00000000-0000-0000-0000-000000000000";
    private static final String SYSTEM_SID = "00000000-0000-0000-0000-000000000001";
    private static final String EMAIL = "service2service@gmail.com";
    private static final String STATUS = "DEFAULT";
    private static final String ROLE = "SERVICE";
    private static final long DEFAULT_EXPIRATION_DAYS = 365;

    public static void main(String[] args) throws Exception {
        String secret = System.getenv("JWT_SECRET");
        long expirationDays = DEFAULT_EXPIRATION_DAYS;

        for (String arg : args) {
            if (arg.startsWith("--secret=")) {
                secret = arg.substring("--secret=".length());
            } else if (arg.startsWith("--days=")) {
                expirationDays = Long.parseLong(arg.substring("--days=".length()));
            }
        }

        if (secret == null || secret.isBlank()) {
            System.err.println("JWT_SECRET not setup. Set environment variable JWT_SECRET or use flag --secret=<jwt_secret>");
            System.exit(1);
        }

        long nowSeconds = Instant.now().getEpochSecond();
        long expSeconds = nowSeconds + expirationDays * 24 * 60 * 60;

        String algorithm = algorithmFor(secret);

        String header = "{\"alg\":\"" + algorithm + "\",\"typ\":\"JWT\"}";
        String payload = "{"
                + "\"sub\":\"" + SYSTEM_GUID + "\","
                + "\"email\":\"" + EMAIL + "\","
                + "\"roles\":[\"" + ROLE + "\"],"
                + "\"status\":\"" + STATUS + "\","
                + "\"sid\":\"" + SYSTEM_SID + "\","
                + "\"iat\":" + nowSeconds + ","
                + "\"exp\":" + expSeconds
                + "}";

        String headerB64 = base64Url(header.getBytes(StandardCharsets.UTF_8));
        String payloadB64 = base64Url(payload.getBytes(StandardCharsets.UTF_8));
        String signingInput = headerB64 + "." + payloadB64;

        byte[] signature = sign(signingInput, secret, algorithm);
        String token = signingInput + "." + base64Url(signature);

        System.err.println("Algorithm: " + algorithm);
        System.err.println("Expires:   " + Instant.ofEpochSecond(expSeconds));
        System.err.println();
        System.out.println(token);
    }

    private static String algorithmFor(String secret) {
        int bitLength = secret.getBytes(StandardCharsets.UTF_8).length * 8;
        if (bitLength >= 512)
            return "HS512";
        if (bitLength >= 384)
            return "HS384";
        if (bitLength >= 256)
            return "HS256";
        throw new IllegalArgumentException("JWT_SECRET too short: required at least 256 bits (32 bytes), now " + bitLength + " bits");
    }

    private static byte[] sign(String data, String secret, String algorithm) throws Exception {
        String macAlgorithm = switch (algorithm) {
            case "HS256" -> "HmacSHA256";
            case "HS384" -> "HmacSHA384";
            case "HS512" -> "HmacSHA512";
            default -> throw new IllegalStateException("Unsupported algorithm: " + algorithm);
        };

        Mac mac = Mac.getInstance(macAlgorithm);
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), macAlgorithm));
        return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    }

    private static String base64Url(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }
}