<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:fo="http://www.w3.org/1999/XSL/Format" version="1.0">

    <xsl:template match="request">
        <!-- fo:root is the top element of any printed item (document or book, etc.)-->
        <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
            <fo:layout-master-set>
                <fo:simple-page-master master-name="portraitMaster" 
                    page-width="8.5in" 
                    page-height="11.0in"
                    margin-top="0.6in" 
                    margin-bottom="0.6in"
                    margin-left="0.6in" 
                    margin-right="0.6in">
                    <fo:region-body margin-top="1.0in" margin-bottom="0.0in"/>
                    <fo:region-before extent="1.7in"/>
                    <fo:region-after extent="0.6in"/>
                </fo:simple-page-master>
            </fo:layout-master-set>

            <fo:page-sequence master-reference="portraitMaster">
                <!-- Header -->
                <fo:static-content flow-name="xsl-region-before">
                    <fo:block text-align="center">Double It Response</fo:block>
                </fo:static-content>
          
                <fo:flow flow-name="xsl-region-body">
                   <fo:block text-align="center"><xsl:value-of select="."/> 
                         doubled is <xsl:value-of select=". * 2"/></fo:block>
              </fo:flow>                          
           </fo:page-sequence>
        </fo:root>
  </xsl:template>
</xsl:stylesheet>

