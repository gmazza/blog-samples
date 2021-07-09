package net.glenmazza.sflistener.cometd;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class AccessTokenProvider implements Function<Boolean, String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccessTokenProvider.class);

    private final Supplier<String> accessTokenSupplier;
    private String accessToken;

    public AccessTokenProvider(String privateKeyForConnectedApp, String url, String username, String jwtAudience, String clientId) {
        this.accessTokenSupplier = () -> {
            try {
                return getAccessToken(privateKeyForConnectedApp,
                        new URL(url + "/services/oauth2/token"),
                        username,
                        jwtAudience,
                        clientId);
            } catch (Exception e) {
                throw new RuntimeException("Unable to connect to Salesforce.  Make sure necessary config fields provided", e);
            }
        };
    }

    @Override
    public String apply(Boolean reAuth) {
        if (reAuth) {
            try {
                accessToken = accessTokenSupplier.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return accessToken;
    }

    private static String getAccessToken(String privateKeyString, URL loginEndpoint, String username, String audience,
                                         String clientId) {

        try {
            RSAPrivateKey privateKey = readPrivateKey(privateKeyString);
            Algorithm algorithm = Algorithm.RSA256(null, privateKey);

            String token = JWT.create()
                    .withIssuer(clientId)
                    .withAudience(audience)
                    .withSubject(username)
                    .withExpiresAt(new Date(System.currentTimeMillis() + 300000))
                    .sign(algorithm);

            HttpURLConnection connection = (HttpURLConnection) loginEndpoint.openConnection();

            Map<String, String> params = new HashMap<>();

            params.put("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer");
            params.put("assertion", token);
            params.put("client_id", clientId);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            try (OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream())) {

                String payload = params.entrySet().stream()
                        .map(entry -> URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8) + "=" +
                                URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                        .collect(Collectors.joining("&"));
                LOGGER.info("OAuth payload: " + payload);
                osw.append(payload);
                osw.close();
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    ObjectMapper mapper = new ObjectMapper();
                    MapType mapType = mapper.getTypeFactory().constructMapType(HashMap.class, String.class, String.class);
                    Map<String, String> data = mapper.readValue(connection.getInputStream(), mapType);
                    return data.get("access_token");
                } else {
                    String responseText = IOUtils.toString(connection.getInputStream());
                    throw new RuntimeException("Response code: " + responseCode + "; Response text: " + responseText);
                }
            } catch (IOException e) {
                String responseText = IOUtils.toString(connection.getErrorStream());
                throw new RuntimeException(responseText, e);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to obtain a token", e);
        }
    }

    // Adapted from code at https://www.baeldung.com/java-read-pem-file-keys
    // other option: https://stackoverflow.com/a/49753179
    private static RSAPrivateKey readPrivateKey(String pemEncodedString) throws GeneralSecurityException {
        String privateKeyPEM = pemEncodedString
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replaceAll(System.lineSeparator(), "")
                .replace("-----END PRIVATE KEY-----", "");

        byte[] encoded = Base64.decodeBase64(privateKeyPEM);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
        return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
    }

}
