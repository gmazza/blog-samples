This is the source code for this tutorial: http://www.jroller.com/gmazza/entry/metro_sts_tutorial, which explains
both UsernameToken and X.509 authentication options between the WSC and the STS.  Be sure to read the tutorial,
including any warnings and disclaimers given within it.

This example has been configured for X.509 authentication between the WSC and the STS.  To use UsernameToken instead:

1.) Rename the DoubleItSTSServiceUT.txt file to DoubleItSTSService.wsdl and place in the
sts-war/src/main/webapp/WEB-INF/wsdl folder.  *Remove* the (X.509) WSDL already there --
important to place it outside of the project else Metro will try to parse that file as well.

2.) In client/src/main/resources, DoubleItSTSService.xml, comment out the sc1:KeyStore element and activate the 
sc1:CallbackHandlerConfiguration element.

Also note the STS's included key/truststore has the client's public cert in it in order for X.509 auth to work.  This
is unnecessary for UsernameToken authentication.

