<?xml version="1.0" encoding="UTF-8"?>

<!DOCTYPE stylesheet [
<!ENTITY space "<xsl:text> </xsl:text>">
<!ENTITY ivSp "<xsl:text>    </xsl:text>">
<!ENTITY tab "<xsl:text>&#9;</xsl:text>">
<!ENTITY cr "<xsl:text>
</xsl:text>">
]>

<xsl:stylesheet version="1.0"
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns:xs="http://www.w3.org/2001/XMLSchema"
   xmlns:wsdl="http://schemas.xmlsoap.org/wsdl/"
   xmlns:wsu="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd"
   xmlns:wsp="http://www.w3.org/ns/ws-policy">

      <xsl:template name="print-ut-policy">
          <wsp:Policy wsu:Id="SecurityPolicy" 
                 xmlns:wsp="http://schemas.xmlsoap.org/ws/2004/09/policy" 
                 xmlns:sp="http://schemas.xmlsoap.org/ws/2005/07/securitypolicy" 
                 xmlns:wsaw="http://www.w3.org/2005/08/addressing">
              <wsp:ExactlyOne>
                 <wsp:All>
                    <wsaw:UsingAddressing
                       xmlns:wsaw="http://www.w3.org/2006/05/addressing/wsdl"
                       wsp:Optional="true" />
                    <sp:TransportBinding>
                       <wsp:Policy>
                          <sp:TransportToken>
                             <wsp:Policy>
                                <sp:HttpsToken
                                   RequireClientCertificate="false" />
                             </wsp:Policy>
                          </sp:TransportToken>
                          <sp:Layout>
                             <wsp:Policy>
                                <sp:Lax />
                             </wsp:Policy>
                          </sp:Layout>
                          <sp:IncludeTimestamp/>
                          <sp:AlgorithmSuite>
                             <wsp:Policy>
                                <sp:Basic128 />
                             </wsp:Policy>
                          </sp:AlgorithmSuite>
                       </wsp:Policy>
                    </sp:TransportBinding>
                    <sp:SignedSupportingTokens>
                       <wsp:Policy>
                          <sp:UsernameToken
                             sp:IncludeToken="http://schemas.xmlsoap.org/ws/2005/07/securitypolicy/IncludeToken/AlwaysToRecipient">
                             <wsp:Policy>
                                <sp:WssUsernameToken10 />
                             </wsp:Policy>
                          </sp:UsernameToken>
                       </wsp:Policy>
                    </sp:SignedSupportingTokens>
                    <sp:Wss11 />
                 </wsp:All>
              </wsp:ExactlyOne>
           </wsp:Policy>
        </xsl:template>

</xsl:stylesheet>
