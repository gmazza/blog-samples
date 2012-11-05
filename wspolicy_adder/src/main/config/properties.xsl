<xsl:stylesheet version="1.0"
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<!-- Choose UT, X509 -->
<xsl:param name="security.method">UT</xsl:param>

<!-- Options used for either UT or X509 type -->
<!-- Choose 1.0, 1.1 -->
<xsl:param name="ws.security.version">1.1</xsl:param>
<!-- Any of 16 values defined here: http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702/ws-securitypolicy-1.2-spec-os.html#_Toc161826556 -->
<xsl:param name="algorithm.suite">Basic256</xsl:param>
<!-- Choose Strict, Lax -->
<xsl:param name="security.header.layout">Strict</xsl:param>

<!-- Options used for X509 type only -->
<xsl:param name="encrypt.signature" select="1"/>
<xsl:param name="encrypt.before.signing" select="1"/>

</xsl:stylesheet>
