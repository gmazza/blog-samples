<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xslthl="http://xslthl.sf.net" xmlns:d="http://docbook.org/ns/docbook"
    xmlns:fo="http://www.w3.org/1999/XSL/Format"
    exclude-result-prefixes="xslthl" version="1.0">

    <xsl:import href="urn:docbkx:stylesheet"/>

    <xsl:param name="fop1.extensions">1</xsl:param>    
  
    <!-- Formatting source code listings, see here:
             http://www.sagehill.net/docbookxsl/ProgramListings.html#FormatListings
    -->
    <xsl:attribute-set name="monospace.verbatim.properties">
      <xsl:attribute name="font-family">monospace</xsl:attribute>
      <xsl:attribute name="font-size">9pt</xsl:attribute>
      <xsl:attribute name="keep-together.within-column">always</xsl:attribute>
    </xsl:attribute-set>

    <xsl:param name="shade.verbatim" select="1"/>

    <xsl:attribute-set name="shade.verbatim.style">
      <xsl:attribute name="background-color">#E0E0E0</xsl:attribute>
      <xsl:attribute name="border-width">0.5pt</xsl:attribute>
      <xsl:attribute name="border-style">solid</xsl:attribute>
      <xsl:attribute name="border-color">#575757</xsl:attribute>
      <xsl:attribute name="padding">3pt</xsl:attribute>
    </xsl:attribute-set>

</xsl:stylesheet>

