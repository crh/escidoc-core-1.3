<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema">
	<xsl:output method="xml"/>
	<xsl:include href="commonSoapRest.xsl"/>
	<xsl:template match="/">
		<chapter>
			<title>List of elements and attributes</title>
			<xsl:for-each select="//xs:element[@name='organizational-unit']/xs:annotation/xs:documentation/para">
				<para>
					<xsl:value-of select="."/>
				</para>
			</xsl:for-each>
			<xsl:apply-templates select="//xs:element[@name='organizational-unit']"/>
		</chapter>
	</xsl:template>
</xsl:stylesheet>
