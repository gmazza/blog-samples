# marketoclient
OAuth2 client with associated helper classes for making API calls to Marketo.
Major functionality around Leads, Activities, and Bulk Extracts supported.

See for background:
https://glenmazza.net/blog/entry/java-client-for-marketo

Test cases in the integration test folder provide examples of this library's use.

Note, due to apparent inconsistencies between refreshed tokens provided by Marketo and
their expiration times, usage of this library requires Spring Retry to be enabled for the application:
https://www.baeldung.com/spring-retry#enabling-spring-retry
i.e., add @EnableRetry to a configuration class.

That, or have the application embedding this library coded to trap MarketoAccessTokenExpiredException 
exceptions.
