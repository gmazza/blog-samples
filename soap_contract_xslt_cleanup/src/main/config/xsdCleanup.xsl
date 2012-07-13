<?xml version="1.0" encoding="UTF-8"?>

<!DOCTYPE stylesheet [
<!ENTITY ivSp "<xsl:text>    </xsl:text>">
<!ENTITY tab "<xsl:text>&#9;</xsl:text>">
<!ENTITY cr "<xsl:text>
</xsl:text>">
]>

<xsl:stylesheet version="1.0"
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns:xs="http://www.w3.org/2001/XMLSchema">

   <xsl:template match="xs:schema">
       &cr;
       <!-- By itself, xsl:copy outputs only the top-level xs:schema element.
            It needs the additional coding below to copy the xs:schema element's 
            attributes and descendant elements. 
       -->
       <xsl:copy> 
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
       </xsl:copy>

   </xsl:template>

   <!-- 
      Each immediate child of the top-level xs:schema is indented from the top-level item.
   -->
   <xsl:template match="xs:import | xs:simpleType | xs:complexType | xs:element">
       <!-- Unlike xsl:copy above, xsl:copy-of automatically copies the 
            element's attributes and its descendant elements. -->
       &ivSp;<xsl:copy-of select="."/>&cr;
   </xsl:template>
   
</xsl:stylesheet>

