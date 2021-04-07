package net.glenmazza.sfoauth2client.salesforce;

import org.slf4j.Logger;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.Signature;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;

/**
 * Constructs a RequestEntity for making an access token request using the JWT Bearer Grant method.
 * Logic largely as https://help.salesforce.com/articleView?id=remoteaccess_oauth_jwt_flow.htm&type=0#create_token
 */
public class OAuth2SalesforceJwtBearerGrantRequestEntityConverter implements Converter<OAuth2SalesforceJwtBearerGrantRequest, RequestEntity<?>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth2SalesforceJwtBearerGrantRequestEntityConverter.class);

    /**
     * Returns the {@link RequestEntity} used for the Access Token Request.
     *
     * @param grantRequest the Salesforce JWT grant request, holder for all information necessary to create JWT
     * @return the {@link RequestEntity} used for the Access Token Request
     */
    @Override
    public RequestEntity<?> convert(OAuth2SalesforceJwtBearerGrantRequest grantRequest) {
        ClientRegistration clientRegistration = grantRequest.getClientRegistration();

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        final MediaType contentType = MediaType.valueOf(APPLICATION_FORM_URLENCODED_VALUE + ";charset=UTF-8");
        headers.setContentType(contentType);

        URI uri = UriComponentsBuilder.fromUriString(clientRegistration.getProviderDetails().getTokenUri())
                .build()
                .toUri();

        MultiValueMap<String, String> formParameters = new LinkedMultiValueMap<>();
        formParameters.add(OAuth2ParameterNames.GRANT_TYPE, OAuth2SalesforceJwtBearerGrantRequest.BEARER_GRANT_TYPE);

        formParameters.add("assertion", generateAssertion(grantRequest));
        return new RequestEntity<>(formParameters, headers, HttpMethod.POST, uri);
    }

    private static String generateAssertion(OAuth2SalesforceJwtBearerGrantRequest grantRequest) {
        String header = "{\"alg\":\"RS256\"}";
        String claimTemplate = "'{'\"iss\": \"{0}\", \"sub\": \"{1}\", \"aud\": \"{2}\", \"exp\": \"{3}\", \"jti\": \"{4}\"'}'";

        StringBuilder token = new StringBuilder();

        //Encode the JWT Header and add it to our string to sign
        token.append(Base64.encodeBase64URLSafeString(header.getBytes(StandardCharsets.UTF_8)));

        //Separate with a period
        token.append(".");

        //Create the JWT Claims Object (https://tinyurl.com/ydeehqcv)
        String[] claimArray = new String[5];

        // iss = consumer key (client_id) of connected app
        claimArray[0] = grantRequest.getClientRegistration().getClientId();

        // sub = username having access into the Salesforce sandbox
        claimArray[1] = grantRequest.getUsername();

        // aud = audience (acceptable values: https://tinyurl.com/ydeehqcv)
        claimArray[2] = grantRequest.getAudience();

        // exp = expiration in seconds (max 300)
        claimArray[3] = Long.toString((System.currentTimeMillis() / 1000) + 300);

        // jti = unique value to guard against replay attacks
        claimArray[4] = UUID.randomUUID().toString();

        MessageFormat claims;
        claims = new MessageFormat(claimTemplate);
        String payload = claims.format(claimArray);

        //Add the encoded claims object
        token.append(Base64.encodeBase64URLSafeString(payload.getBytes(StandardCharsets.UTF_8)));

        //Sign the JWT Header + "." + JWT Claims Object
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(grantRequest.getPrivateKey());
            signature.update(token.toString().getBytes(StandardCharsets.UTF_8));
            String signedPayload = Base64.encodeBase64URLSafeString(signature.sign());

            //Separate with a period
            token.append(".");

            //Add the encoded signature
            token.append(signedPayload);

            return token.toString();

        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

}
