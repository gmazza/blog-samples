<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns:xs="http://www.w3.org/2001/XMLSchema"
   xmlns:wsdl="http://schemas.xmlsoap.org/wsdl/"
   xmlns:wsu="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd"
   xmlns:wsp="http://www.w3.org/ns/ws-policy"
   xmlns:xalan="http://xml.apache.org/xslt">

   <xsl:output method="xml" indent="yes" xalan:indent-amount="3"/>

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

          <xsl:for-each select="./wsdl:types">
              <wsdl:types>
                  <xsl:apply-templates select="./xs:schema"/>
              </wsdl:types>
          </xsl:for-each>

          <xsl:for-each select="./wsdl:message">
              <xsl:sort select="@name"/>
              <xsl:apply-templates select="."/>
          </xsl:for-each>

          <xsl:for-each select="./wsdl:portType">
              <xsl:apply-templates select="."/>
          </xsl:for-each>

          <xsl:for-each select="./wsdl:binding">
              <xsl:sort select="@name"/>
              <xsl:apply-templates select="."/>
          </xsl:for-each>

          <xsl:for-each select="./wsp:Policy">
              <!--if wish to sort: xsl:sort select="@wsu:Id"/-->
              <xsl:copy-of select="."/>
          </xsl:for-each>

          <xsl:for-each select="./wsdl:service">
              <xsl:copy-of select="."/>
          </xsl:for-each>

       </xsl:copy>

   </xsl:template>

   <!--
      Each immediate child of the top-level element is indented four spaces
      from the top-level item.  May need to convert original WSDL from tabs
      to spaces first.
   -->
   <xsl:template match="wsdl:message">
       <xsl:copy-of select="."/>
   </xsl:template>

   <xsl:template match="wsdl:portType | wsdl:binding">
   	   <xsl:copy>
          <!-- output all (@*) attributes of the top-level element -->
          <xsl:for-each select="@*">
              <xsl:copy/>
          </xsl:for-each>
   		
          <xsl:for-each select="./*[not(self::wsdl:operation)]">
              <xsl:copy-of select="."/>
          </xsl:for-each>

          <xsl:for-each select="./wsdl:operation">
              <xsl:sort select="@name"/>
              <xsl:apply-templates select="."/>
          </xsl:for-each>

        </xsl:copy>
   </xsl:template>

   <xsl:template match="wsdl:operation">
       <xsl:copy-of select="."/>
   </xsl:template>

   <xsl:template match="xs:schema">

       <xsl:copy> 
          <!-- output all (@*) attributes of the top-level xs:schema element -->
          <xsl:for-each select="@*"> 
              <xsl:copy/>
          </xsl:for-each>
          
          <xsl:for-each select="./xs:import">
              <xsl:apply-templates select="."/>
          </xsl:for-each>
      
          <xsl:for-each select="./xs:simpleType">
              <!-- sort the simpleTypes by their name attribute first -->
              <xsl:sort select="@name"/> 
              <xsl:apply-templates select="."/>
          </xsl:for-each>
      
          <xsl:for-each select="./xs:complexType">
              <xsl:sort select="@name"/>
              <xsl:apply-templates select="."/>
          </xsl:for-each>

          <xsl:for-each select="./xs:element">
              <xsl:sort select="@name"/>
              <xsl:apply-templates select="."/>
          </xsl:for-each>
       </xsl:copy>

   </xsl:template>

   <xsl:template match="xs:import | xs:simpleType | xs:complexType | xs:element">
       <!-- Unlike xsl:copy above, xsl:copy-of automatically copies the 
            element's attributes and its descendant elements. -->
       <xsl:copy-of select="."/>
   </xsl:template>

</xsl:stylesheet>
