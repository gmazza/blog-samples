<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xslthl="http://xslthl.sf.net" xmlns:d="http://docbook.org/ns/docbook"
    exclude-result-prefixes="xslthl" version="1.0">

    <xsl:template match="d:caption">
        <div>
            <xsl:apply-templates select="."
                mode="common.html.attributes" />
            <xsl:if
                test="@align = 'right' or @align = 'left' or @align='center'">
                <xsl:attribute name="align"><xsl:value-of
                    select="@align" /></xsl:attribute>
            </xsl:if>
            <strong>
                <em>
                    <xsl:apply-templates />
                </em>
            </strong>
        </div>
    </xsl:template>

</xsl:stylesheet>

