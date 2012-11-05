<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns:xs="http://www.w3.org/2001/XMLSchema"
   xmlns:wsdl="http://schemas.xmlsoap.org/wsdl/"
   xmlns:wsu="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd"
   xmlns:wsp="http://www.w3.org/ns/ws-policy"
   xmlns:wsam="http://www.w3.org/2007/05/addressing/metadata"
   xmlns:sp="http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702"
>

    <xsl:template name="print-x509-policy">
       <wsp:Policy wsu:Id="SecurityPolicy">
          <wsp:ExactlyOne>
             <wsp:All>
                <wsam:Addressing wsp:Optional="false"/>
                <sp:AsymmetricBinding>
                   <wsp:Policy>
                      <sp:InitiatorToken>
                         <wsp:Policy>
                            <sp:X509Token sp:IncludeToken="http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702/IncludeToken/AlwaysToRecipient">
                               <wsp:Policy>
                                  <sp:WssX509V3Token10/>
                               </wsp:Policy>
                            </sp:X509Token>
                         </wsp:Policy>
                      </sp:InitiatorToken>
                      <sp:RecipientToken>
                         <wsp:Policy>
                            <sp:X509Token sp:IncludeToken="http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702/IncludeToken/Never">
                               <wsp:Policy>
                                  <sp:WssX509V3Token10/>
                                  <sp:RequireIssuerSerialReference/>
                               </wsp:Policy>
                            </sp:X509Token>
                         </wsp:Policy>
                      </sp:RecipientToken>
                      <sp:Layout>
                         <wsp:Policy>
                              <xsl:choose>
                                  <xsl:when test="$security.header.layout = 'Lax'">
                                      <sp:Lax/>
                                  </xsl:when>
                                  <xsl:otherwise>
                                      <sp:Strict/>
                                  </xsl:otherwise>
                              </xsl:choose>
                         </wsp:Policy>
                      </sp:Layout>
                      <sp:IncludeTimestamp/>
                      <sp:OnlySignEntireHeadersAndBody/>
                      <sp:AlgorithmSuite>
                         <wsp:Policy>
                             <xsl:value-of select="$algorithm.suite"/>
                         </wsp:Policy>
                      </sp:AlgorithmSuite>
                   </wsp:Policy>
                </sp:AsymmetricBinding>
                   <wsp:Policy>
                      <sp:MustSupportRefIssuerSerial/>
                   </wsp:Policy>
                  <xsl:choose>
                      <xsl:when test="$ws.security.version = '1.0'">
                          <sp:Wss10/>
                      </xsl:when>
                      <xsl:otherwise>
                          <sp:Wss11 />
                      </xsl:otherwise>
                  </xsl:choose>
                  <xsl:if test="not($encrypt.signature = 0)">
                      <sp:EncryptSignature/>
                  </xsl:if>
                  <xsl:if test="not($encrypt.before.signing = 0)">
                      <sp:EncryptBeforeSigning/>
                  </xsl:if>
             </wsp:All>
          </wsp:ExactlyOne>
       </wsp:Policy>
    </xsl:template>

</xsl:stylesheet>
