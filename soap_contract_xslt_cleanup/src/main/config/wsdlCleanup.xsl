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

   <xsl:template match="wsdl:definitions">
       &cr;
       <!-- By itself, xsl:copy outputs only the element referred to.
            It needs additional coding as below to copy the
            element's attributes and descendant elements.
       -->
       <xsl:copy>
          <!-- output all attributes of the top-level element -->
          <xsl:for-each select="@*">
              <xsl:copy/>
          </xsl:for-each>&cr;

          <xsl:for-each select="./wsdl:types">
              &ivSp;<wsdl:types>&cr;
                  <xsl:apply-templates select="./xs:schema"/>
              &ivSp;</wsdl:types>&cr;
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
              &ivSp;<xsl:copy-of select="."/>&cr;
          </xsl:for-each>

          <xsl:for-each select="./wsdl:service">
              &ivSp;<xsl:copy-of select="."/>&cr;
          </xsl:for-each>

       </xsl:copy>

   </xsl:template>

   <!--
      Each immediate child of the top-level element is indented four spaces
      from the top-level item.  May need to convert original WSDL from tabs
      to spaces first.
   -->
   <xsl:template match="wsdl:message">
       &ivSp;<xsl:copy-of select="."/>&cr;
   </xsl:template>

   <xsl:template match="wsdl:portType | wsdl:binding">
   	&ivSp;<xsl:copy>
          <!-- output all (@*) attributes of the top-level element -->
          <xsl:for-each select="@*">
              <xsl:copy/>
          </xsl:for-each>&cr;
   		
          <xsl:for-each select="./*[not(self::wsdl:operation)]">
              &ivSp;&ivSp;<xsl:copy-of select="."/>&cr;
          </xsl:for-each>

          <xsl:for-each select="./wsdl:operation">
              <xsl:sort select="@name"/>
              <xsl:apply-templates select="."/>
          </xsl:for-each>&ivSp;

        </xsl:copy>&cr;
   </xsl:template>

   <xsl:template match="wsdl:operation">
       &ivSp;&ivSp;<xsl:copy-of select="."/>&cr;
   </xsl:template>

   <xsl:template match="xs:schema">

       &ivSp;&ivSp;<xsl:copy> 
          <!-- output all (@*) attributes of the top-level xs:schema element -->
          <xsl:for-each select="@*"> 
              <xsl:copy/>
          </xsl:for-each>&cr;
          
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
       &ivSp;&ivSp;</xsl:copy>&cr;

   </xsl:template>

   <xsl:template match="xs:import | xs:simpleType | xs:complexType | xs:element">
       <!-- Unlike xsl:copy above, xsl:copy-of automatically copies the 
            element's attributes and its descendant elements. -->
       &ivSp;&ivSp;&ivSp;<xsl:copy-of select="."/>&cr;
   </xsl:template>

</xsl:stylesheet>

