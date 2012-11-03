<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns:xs="http://www.w3.org/2001/XMLSchema"
   xmlns:wsdl="http://schemas.xmlsoap.org/wsdl/"
   xmlns:wsu="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd"
   xmlns:wsp="http://www.w3.org/ns/ws-policy"
   xmlns:xalan="http://xml.apache.org/xslt">

   <xsl:output method="xml" indent="yes" xalan:indent-amount="3"/>
   <xsl:import href="properties.xsl"/>
   <xsl:import href="UTPolicies.xsl"/>

   <xsl:template match="wsdl:definitions">
       
       <!-- By itself, xsl:copy outputs only the element referred to.
            It needs additional coding as below to copy the
            element's attributes and descendant elements.
       -->
       <xsl:copy>
          <!-- output all attributes of the top-level element -->
          <xsl:for-each select="@*">
              <xsl:copy/>
          </xsl:for-each>

          <!-- output all non-binding child elements of the wsdl:definition -->
          <xsl:for-each select="./*[not(self::wsdl:binding)]">
              <xsl:copy-of select="."/>
          </xsl:for-each>

          <!-- add policy reference to wsdl:binding section -->
          <xsl:for-each select="./wsdl:binding">
              <xsl:apply-templates select="."/>
          </xsl:for-each>

          <!-- add the policy statement to the WSDL -->
          <xsl:choose>
          <xsl:when test="$security.method = 'UT'">
              <xsl:call-template name="print-ut-policy"/>
          </xsl:when>
          <xsl:when test="$security.method = 'X509'">
          </xsl:when>
          <xsl:otherwise>
          </xsl:otherwise>
          </xsl:choose>
       </xsl:copy>

   </xsl:template>

   <!--  Adds the PolicyReference to the wsdl:binding section -->
   <xsl:template match="wsdl:binding">
   	   <xsl:copy>
          <!-- output all (@*) attributes of the top-level element -->
          <xsl:for-each select="@*">
              <xsl:copy/>
          </xsl:for-each>
   		
          <wsp:PolicyReference URI="#SecurityPolicy" />

          <xsl:for-each select="./*">
              <xsl:copy-of select="."/>
          </xsl:for-each>

        </xsl:copy>
   </xsl:template>

</xsl:stylesheet>
