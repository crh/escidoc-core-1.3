<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~ CDDL HEADER START
  ~
  ~ The contents of this file are subject to the terms of the Common Development and Distribution License, Version 1.0
  ~ only (the "License"). You may not use this file except in compliance with the License.
  ~
  ~ You can obtain a copy of the license at license/ESCIDOC.LICENSE or http://www.escidoc.de/license. See the License
  ~ for the specific language governing permissions and limitations under the License.
  ~
  ~ When distributing Covered Code, include this CDDL HEADER in each file and include the License file at
  ~ license/ESCIDOC.LICENSE. If applicable, add the following below this CDDL HEADER, with the fields enclosed by
  ~ brackets "[]" replaced with your own identifying information: Portions Copyright [yyyy] [name of copyright owner]
  ~
  ~ CDDL HEADER END
  ~
  ~ Copyright 2006-2011 Fachinformationszentrum Karlsruhe Gesellschaft fuer wissenschaftlich-technische Information mbH
  ~ and Max-Planck-Gesellschaft zur Foerderung der Wissenschaft e.V. All rights reserved. Use is subject to license
  ~ terms.
  -->
<xsl:stylesheet version="1.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:xalan="http://xml.apache.org/xalan"
		xmlns:lastdate-helper="xalan://de.escidoc.sb.gsearch.xslt.LastdateHelper"
		xmlns:string-helper="xalan://de.escidoc.sb.gsearch.xslt.StringHelper"
		xmlns:sortfield-helper="xalan://de.escidoc.sb.gsearch.xslt.SortFieldHelper"
		xmlns:escidoc-core-accessor="xalan://de.escidoc.sb.gsearch.xslt.EscidocCoreAccessor" 
		extension-element-prefixes="lastdate-helper string-helper sortfield-helper escidoc-core-accessor">
	<xsl:output method="xml" indent="yes" encoding="UTF-8"/>
	
    <!-- Include stylesheet that writes important fields for gsearch -->
    <xsl:include href="index/gsearchAttributes.xslt"/>
    
	<!-- Parameters that get passed while calling this stylesheet-transformation -->
	<xsl:param name="SUPPORTED_MIMETYPES"/>

    <!-- Store Fields for Scan-Operation-->
    <xsl:variable name="STORE_FOR_SCAN">YES</xsl:variable>

	<xsl:variable name="CONTEXTNAME">escidoc</xsl:variable>
	<xsl:variable name="COMPONENT_CONTEXTNAME">escidoc.component</xsl:variable>
    <xsl:variable name="SORTCONTEXTPREFIX">sort</xsl:variable>

	<!-- Paths to Metadata -->
	<xsl:variable name="ITEM_METADATAPATH" select="/*[local-name()='item']/*[local-name()='md-records']/*[local-name()='md-record'][@name='escidoc']"/>
	<xsl:variable name="CONTAINER_METADATAPATH" select="/*[local-name()='container']/*[local-name()='md-records']/*[local-name()='md-record'][@name='escidoc']"/>
	<xsl:variable name="COMPONENT_METADATAPATH" select="/*[local-name()='item']/*[local-name()='components']/*[local-name()='component']/*[local-name()='md-records']/*[local-name()='md-record'][@name='escidoc']"/>
	
	<!-- Paths to Properties -->
	<xsl:variable name="ITEM_PROPERTIESPATH" select="/*[local-name()='item']/*[local-name()='properties']"/>
	<xsl:variable name="CONTAINER_PROPERTIESPATH" select="/*[local-name()='container']/*[local-name()='properties']"/>
	<xsl:variable name="COMPONENT_PROPERTIESPATH" select="/*[local-name()='item']/*[local-name()='components']/*[local-name()='component']/*[local-name()='properties']"/>
	<xsl:variable name="CONTENT_MODEL_SPECIFIC_PATH" select="/*[local-name()='item']/*[local-name()='properties']/*[local-name()='content-model-specific']"/>

    <!-- Paths to Components -->
    <xsl:variable name="COMPONENT_PATH" select="/*[local-name()='item']/*[local-name()='components']/*[local-name()='component']"/>

    <!-- Paths to Content-Relations -->
    <xsl:variable name="CONTENT_RELATIONS_PATH" select="/*/*[local-name()='relations']/*[local-name()='relation']"/>

	<!-- COMPONENT TYPES THAT DONT GET INDEXED -->
	<xsl:variable name="NON_SUPPORTED_COMPONENT_TYPES"> correspondence copyright-transfer-agreement </xsl:variable>
	
	<xsl:template match="/">
		<xsl:variable name="type">
			<xsl:for-each select="*">
				<xsl:if test="position() = 1">
					<xsl:value-of select="local-name()"/>
				</xsl:if>
			</xsl:for-each>
		</xsl:variable>
        <IndexDocument> 
        <!-- Call this template immediately after opening IndexDocument-element! -->
        <xsl:call-template name="processGsearchAttributes"/>
		<xsl:choose>
			<xsl:when test="$type='item'">
				<xsl:call-template name="processItem"/>
			</xsl:when>
			<xsl:when test="$type='container'">
				<xsl:call-template name="processContainer"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="processContainer"/>
			</xsl:otherwise>
		</xsl:choose>
        </IndexDocument> 
	</xsl:template>

    <!-- WRITE THE XML THAT GETS RETURNED BY THE SEARCH -->
    <xsl:template name="writeSearchXmlItem">
        <xsl:copy-of select="/*[local-name()='item']"/>
    </xsl:template>

    <!-- WRITE THE XML THAT GETS RETURNED BY THE SEARCH -->
    <xsl:template name="writeSearchXmlContainer">
        <xsl:copy-of select="/*[local-name()='container']"/>
    </xsl:template>

	<xsl:template name="processItem">
		<xsl:call-template name="writeIndexField">
			<xsl:with-param name="context" select="$CONTEXTNAME"/>
			<xsl:with-param name="fieldname">objecttype</xsl:with-param>
			<xsl:with-param name="fieldvalue">item</xsl:with-param>
			<xsl:with-param name="indextype">UN_TOKENIZED</xsl:with-param>
			<xsl:with-param name="store" select="$STORE_FOR_SCAN"/>
		</xsl:call-template>
		<xsl:call-template name="writeIndexField">
			<xsl:with-param name="context" select="$CONTEXTNAME"/>
			<xsl:with-param name="fieldname">objid</xsl:with-param>
			<xsl:with-param name="fieldvalue" select="string-helper:removeVersionIdentifier(string-helper:getSubstringAfterLast(/*[local-name()='item']/@*[local-name()='href'], '/'))"/>
			<xsl:with-param name="indextype">UN_TOKENIZED</xsl:with-param>
			<xsl:with-param name="store" select="$STORE_FOR_SCAN"/>
		</xsl:call-template>
		<IndexField IFname="xml_representation" index="NO" store="YES" termVector="NO">
			<xsl:text disable-output-escaping="yes">
				&lt;![CDATA[
			</xsl:text>
				<xsl:call-template name="writeSearchXmlItem"/>
			<xsl:text disable-output-escaping="yes">
				]]&gt;
			</xsl:text>
		</IndexField>
		
		<!-- PROPERTIES -->
		<xsl:call-template name="processProperties">
			<xsl:with-param name="path" select="$ITEM_PROPERTIESPATH"/>
		</xsl:call-template>
 			
        <!-- INDEX CONTENT-RELATIONS -->
        <xsl:call-template name="processContentRelations">
            <xsl:with-param name="path" select="$CONTENT_RELATIONS_PATH"/>
            <xsl:with-param name="context" select="$CONTEXTNAME"/>
        </xsl:call-template>
            
 			<!-- CONTENT_MODEL_SPECIFIC -->
		<!-- xsl:call-template name="processContentModelSpecific" /-->
		
		<!-- ESCIDOC METADATA -->
		<xsl:call-template name="processMetadata">
			<xsl:with-param name="path" select="$ITEM_METADATAPATH"/>
		</xsl:call-template>
		
		<!-- COMPONENT METADATA -->
		<xsl:for-each select="$COMPONENT_METADATAPATH">
			<xsl:call-template name="processElementTree">
				<xsl:with-param name="path"/>
				<xsl:with-param name="context" select="$COMPONENT_CONTEXTNAME"/>
                <xsl:with-param name="indexAttributes">no</xsl:with-param>
				<xsl:with-param name="nametype">element</xsl:with-param>
			</xsl:call-template>
 			</xsl:for-each>
		
		<!-- COMPONENT PROPERTIES -->
		<xsl:for-each select="$COMPONENT_PROPERTIESPATH">
			<xsl:call-template name="processElementTree">
				<xsl:with-param name="path"/>
				<xsl:with-param name="context" select="$COMPONENT_CONTEXTNAME"/>
                <xsl:with-param name="indexAttributes">no</xsl:with-param>
				<xsl:with-param name="nametype">element</xsl:with-param>
			</xsl:call-template>
 			</xsl:for-each>
		
		<!-- FULLTEXTS -->
        <xsl:call-template name="processComponents">
            <xsl:with-param name="num" select="0"/>
            <xsl:with-param name="components" select="$COMPONENT_PATH"/>
            <xsl:with-param name="matchNum" select="1"/>
        </xsl:call-template>
		
		<!-- SORT FIELDS -->
		<xsl:for-each select="xalan:nodeset($sortfields)/sortfield">
			<xsl:if test="./@type='item'">
				<xsl:call-template name="writeSortField">
					<xsl:with-param name="context" select="$CONTEXTNAME"/>
					<xsl:with-param name="fieldname" select="./@name"/>
					<xsl:with-param name="fieldvalue" select="./@path"/>
				</xsl:call-template>
			</xsl:if>
 			</xsl:for-each>
 			
 		<!-- USER DEFINED INDEXES -->
		<xsl:call-template name="writeUserdefinedIndexes" />
	</xsl:template>

	<xsl:template name="processContainer">
		<xsl:call-template name="writeIndexField">
			<xsl:with-param name="context" select="$CONTEXTNAME"/>
			<xsl:with-param name="fieldname">objecttype</xsl:with-param>
			<xsl:with-param name="fieldvalue">container</xsl:with-param>
			<xsl:with-param name="indextype">UN_TOKENIZED</xsl:with-param>
			<xsl:with-param name="store" select="$STORE_FOR_SCAN"/>
		</xsl:call-template>
		<xsl:call-template name="writeIndexField">
			<xsl:with-param name="context" select="$CONTEXTNAME"/>
			<xsl:with-param name="fieldname">objid</xsl:with-param>
			<xsl:with-param name="fieldvalue" select="string-helper:removeVersionIdentifier(string-helper:getSubstringAfterLast(/*[local-name()='container']/@*[local-name()='href'], '/'))"/>
			<xsl:with-param name="indextype">UN_TOKENIZED</xsl:with-param>
			<xsl:with-param name="store" select="$STORE_FOR_SCAN"/>
		</xsl:call-template>
		<IndexField IFname="xml_representation" index="NO" store="YES" termVector="NO">
			<xsl:text disable-output-escaping="yes">
				&lt;![CDATA[
			</xsl:text>
				<xsl:call-template name="writeSearchXmlContainer"/>
			<xsl:text disable-output-escaping="yes">
				]]&gt;
			</xsl:text>
		</IndexField>
		
		<!-- PROPERTIES -->
		<xsl:call-template name="processProperties">
			<xsl:with-param name="path" select="$CONTAINER_PROPERTIESPATH"/>
		</xsl:call-template>
 			
        <!-- INDEX CONTENT-RELATIONS -->
        <xsl:call-template name="processContentRelations">
            <xsl:with-param name="path" select="$CONTENT_RELATIONS_PATH"/>
            <xsl:with-param name="context" select="$CONTEXTNAME"/>
        </xsl:call-template>
            
 			<!-- CONTENT_MODEL_SPECIFIC -->
		<!-- xsl:call-template name="processContentModelSpecific" / -->
		
		<!-- ESCIDOC METADATA -->
		<xsl:call-template name="processMetadata">
			<xsl:with-param name="path" select="$CONTAINER_METADATAPATH"/>
		</xsl:call-template>
		
		<!-- SORT FIELDS -->
		<xsl:for-each select="xalan:nodeset($sortfields)/sortfield">
			<xsl:if test="./@type='container'">
				<xsl:call-template name="writeSortField">
					<xsl:with-param name="context" select="$CONTEXTNAME"/>
					<xsl:with-param name="fieldname" select="./@name"/>
					<xsl:with-param name="fieldvalue" select="./@path"/>
				</xsl:call-template>
			</xsl:if>
 			</xsl:for-each>

 			<!-- USER DEFINED INDEXES -->
		<xsl:call-template name="writeUserdefinedIndexes" />
	</xsl:template>

	<!-- RECURSIVE ITERATION OF ELEMENTS -->
	<!-- ITERATE ALL ELEMENTS AND WRITE ELEMENT-NAME AND ELEMENT-VALUE -->
    <xsl:template name="processElementTree">
        <!-- name of index-field -->
        <xsl:param name="path"/>
        <!-- prefix for index-name -->
        <xsl:param name="context"/>
        <!-- if 'yes', also write attributes as index-fields -->
        <xsl:param name="indexAttributes"/>
        <!-- nametype defines if paths are used for indexnames or elementname only -->
        <!-- can be 'path' or 'element' -->
        <!-- eg first-name or publication.creator.person.first-name -->
        <xsl:param name="nametype"/>
        <xsl:if test="string(text()) and normalize-space(text())!=''">
            <xsl:call-template name="writeIndexField">
                <xsl:with-param name="context" select="$context"/>
                <xsl:with-param name="fieldname" select="$path"/>
                <xsl:with-param name="fieldvalue" select="text()"/>
                <xsl:with-param name="indextype">TOKENIZED</xsl:with-param>
                <xsl:with-param name="store" select="$STORE_FOR_SCAN"/>
            </xsl:call-template>
            <xsl:call-template name="writeIndexField">
                <xsl:with-param name="context" select="$context"/>
                <xsl:with-param name="fieldname">metadata</xsl:with-param>
                <xsl:with-param name="fieldvalue" select="text()"/>
                <xsl:with-param name="indextype">TOKENIZED</xsl:with-param>
                <xsl:with-param name="store" select="$STORE_FOR_SCAN"/>
            </xsl:call-template>
        </xsl:if>
        <xsl:if test="$indexAttributes='yes'">
            <!-- ITERATE ALL ATTRIBUTES AND WRITE ELEMENT-NAME, ATTRIBUTE-NAME AND ATTRIBUTE-VALUE -->
            <xsl:for-each select="@*">
                <xsl:if test="string(.) and normalize-space(.)!=''
                        and string($path) and normalize-space($path)!=''">
                    <xsl:call-template name="writeIndexField">
                        <xsl:with-param name="context" select="$context"/>
                        <xsl:with-param name="fieldname" select="concat($path,'.',local-name())"/>
                        <xsl:with-param name="fieldvalue" select="."/>
                        <xsl:with-param name="indextype">TOKENIZED</xsl:with-param>
                        <xsl:with-param name="store" select="$STORE_FOR_SCAN"/>
                    </xsl:call-template>
                    <!-- ADDITIONALLY WRITE VALUE IN metadata-index -->
                    <xsl:call-template name="writeIndexField">
                        <xsl:with-param name="context" select="$CONTEXTNAME"/>
                        <xsl:with-param name="fieldname">metadata</xsl:with-param>
                        <xsl:with-param name="fieldvalue" select="."/>
                        <xsl:with-param name="indextype">TOKENIZED</xsl:with-param>
                        <xsl:with-param name="store" select="$STORE_FOR_SCAN"/>
                    </xsl:call-template>
                </xsl:if>
            </xsl:for-each>
        </xsl:if>
        <xsl:for-each select="./*">
            <xsl:variable name="fieldname">
                <xsl:choose>
                    <xsl:when test="$nametype='element'">
                            <xsl:value-of select="local-name()"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:choose>
                            <xsl:when test="string($path) and normalize-space($path)!=''">
                                <xsl:value-of select="concat($path,'.',local-name())"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="local-name()"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:variable>
            <xsl:call-template name="processElementTree">
                <xsl:with-param name="context" select="$context"/>
                <xsl:with-param name="indexAttributes" select="$indexAttributes"/>
                <xsl:with-param name="path" select="$fieldname"/>
                <xsl:with-param name="nametype" select="$nametype"/>
            </xsl:call-template>
        </xsl:for-each>
    </xsl:template>

	<!-- PROCESS METADATA -->
	<xsl:template name="processMetadata">
  		<xsl:param name="path"/>
		<xsl:for-each select="$path">
			<IndexField IFname="xml_metadata" index="NO" store="YES" termVector="NO">
				<xsl:text disable-output-escaping="yes">
					&lt;![CDATA[
				</xsl:text>
					<xsl:copy-of select="."/>
				<xsl:text disable-output-escaping="yes">
					]]&gt;
				</xsl:text>
			</IndexField>
			<xsl:call-template name="processElementTree">
				<xsl:with-param name="path"/>
				<xsl:with-param name="context" select="$CONTEXTNAME"/>
                <xsl:with-param name="indexAttributes">no</xsl:with-param>
				<xsl:with-param name="nametype">element</xsl:with-param>
			</xsl:call-template>
  		</xsl:for-each>
	</xsl:template>

    <!-- PROCESS ALL PROPERTIES -->
    <xsl:template name="processProperties">
        <xsl:param name="path"/>
        <xsl:for-each select="$path">
            <xsl:call-template name="processElementTree">
                <xsl:with-param name="path"/>
                <xsl:with-param name="context" select="$CONTEXTNAME"/>
                <xsl:with-param name="indexAttributes">no</xsl:with-param>
                <xsl:with-param name="nametype">path</xsl:with-param>
            </xsl:call-template>
        </xsl:for-each>
    </xsl:template>

    <!-- PROCESS CONTENT-RELATIONS -->
    <xsl:template name="processContentRelations">
        <xsl:param name="path"/>
        <xsl:param name="context"/>
        <xsl:for-each select="$path">
            <xsl:call-template name="writeIndexField">
                <xsl:with-param name="context" select="$context"/>
                <xsl:with-param name="fieldname">content-relation</xsl:with-param>
                <xsl:with-param name="fieldvalue" select="concat(./@predicate, ' ', string-helper:getSubstringAfterLast(./@*[local-name()='href'], '/'))"/>
                <xsl:with-param name="indextype">TOKENIZED</xsl:with-param>
                <xsl:with-param name="store" select="$STORE_FOR_SCAN"/>
            </xsl:call-template>
        </xsl:for-each>
    </xsl:template>

	<!-- PROCESS ELEMENT CONTENT_MODEL_SPECIFIC -->
	<!-- xsl:template name="processContentModelSpecific">
		<xsl:for-each select="$CONTENT_MODEL_SPECIFIC_PATH">
			<xsl:call-template name="processElementTree">
				<xsl:with-param name="path">content-model-specific</xsl:with-param>
				<xsl:with-param name="context" select="$CONTEXTNAME"/>
                <xsl:with-param name="indexAttributes">no</xsl:with-param>
				<xsl:with-param name="nametype">path</xsl:with-param>
			</xsl:call-template>
  		</xsl:for-each>
	</xsl:template -->

	<!-- RECURSIVE ITERATION FOR COMPONENTS (FULLTEXTS) -->
	<!-- STORE EVERYTHING IN FIELD fulltext FOR SEARCH-->
	<!-- STORE EACH FULLTEXT IN SEPARATE FIELD stored_fulltext<n> FOR HIGHLIGHTING -->
	<!-- ADDITIONALLY STORE HREF OF COMPONENT IN SEPARATE FIELD stored_filename<n> FOR HIGHLIGHTING THE LOCATION OF THE FULLTEXT-->
	<!-- ONLY INDEX FULLTEXTS IF MIME_TYPE IS text/xml, application/xml, text/plain, application/msword or application/pdf -->
	<!-- ONLY INDEX FULLTEXTS IF component-type IS NOT correspondence OR copyright transfer agreement-->
	<xsl:template name="processComponents" xmlns:xlink="http://www.w3.org/1999/xlink">
  		<xsl:param name="num"/>
  		<xsl:param name="components"/>
  		<xsl:param name="matchNum"/>
		<xsl:variable name="component-type" select="$components[$num]/*[local-name()='properties']/*[local-name()='content-category']"/>
		<!-- xsl:variable name="visibility" select="$components[$num]/*[local-name()='properties']/*[local-name()='visibility']"/ -->
  		<xsl:variable name="mime-type">
			<xsl:value-of select="$components[$num]/*[local-name()='properties']/*[local-name()='mime-type']"/>
		</xsl:variable>
  		<xsl:choose>
  			<xsl:when test="string($mime-type) 
  							and contains($SUPPORTED_MIMETYPES,$mime-type)
  							and string($component-type)
  							and contains($NON_SUPPORTED_COMPONENT_TYPES,concat(' ',$component-type,' '))=false">
  				<!-- INDEX FULLTEXT -->
				<IndexField index="TOKENIZED" store="YES" termVector="NO">
					<xsl:attribute name="dsId">
						<xsl:value-of select="$components[$num]/*[local-name()='content']/@xlink:href"/>
					</xsl:attribute>
					<xsl:attribute name="IFname">
						<xsl:value-of select="concat($CONTEXTNAME,'.fulltext')"/>
					</xsl:attribute>
                    <xsl:attribute name="store">
                        <xsl:value-of select="$STORE_FOR_SCAN"/>
                    </xsl:attribute>
				</IndexField>

				<!-- SEPERATELY STORE EACH FULLTEXT IN DIFFERENT FIELD FOR HIGHLIGHTING -->
    			<IndexField index="NO" store="YES" termVector="NO">
					<xsl:attribute name="dsId">
						<xsl:value-of select="$components[$num]/*[local-name()='content']/@xlink:href"/>
					</xsl:attribute>
					<xsl:attribute name="IFname">
						<xsl:value-of select="concat('stored_fulltext',$matchNum)"/>
					</xsl:attribute>
				</IndexField>

				<!-- SEPERATELY STORE FILENAME FOR EACH FULLTEXT FOR HIGHLIGHTING -->
    			<IndexField index="NO" store="YES" termVector="NO">
					<xsl:attribute name="IFname">
						<xsl:value-of select="concat('stored_filename',$matchNum)"/>
					</xsl:attribute>
					<xsl:value-of select="$components[$num]/*[local-name()='content']/@xlink:href"/>
				</IndexField>

				<xsl:choose>
					<xsl:when test="$components[$num + 1]">
						<xsl:call-template name="processComponents">
							<xsl:with-param name="num" select="$num + 1"/>
							<xsl:with-param name="components" select="$components"/>
							<xsl:with-param name="matchNum" select="$matchNum + 1"/>
						</xsl:call-template>
					</xsl:when>
				</xsl:choose>
			</xsl:when>
			<xsl:otherwise>
				<xsl:choose>
					<xsl:when test="$components[$num + 1]">
						<xsl:call-template name="processComponents">
							<xsl:with-param name="num" select="$num + 1"/>
							<xsl:with-param name="components" select="$components"/>
							<xsl:with-param name="matchNum" select="$matchNum"/>
						</xsl:call-template>
					</xsl:when>
				</xsl:choose>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!--  WRITE INDEXFIELD -->
	<xsl:template name="writeIndexField">
  		<xsl:param name="context"/>
  		<xsl:param name="fieldname"/>
  		<xsl:param name="fieldvalue"/>
  		<xsl:param name="indextype"/>
  		<xsl:param name="store"/>
		<xsl:if test="string($fieldvalue) and normalize-space($fieldvalue)!=''">
			<IndexField termVector="NO">
				<xsl:attribute name="index">
                	<xsl:value-of select="$indextype"/>
				</xsl:attribute>
				<xsl:attribute name="store">
					<xsl:value-of select="$store"/>
				</xsl:attribute>
				<xsl:attribute name="IFname">
					<xsl:value-of select="concat($context,'.',$fieldname)"/>
				</xsl:attribute>
                <xsl:value-of select="$fieldvalue"/>
			</IndexField>
            <xsl:call-template name="writeSortField">
                <xsl:with-param name="context" select="$context"/>
                <xsl:with-param name="fieldname" select="$fieldname"/>
                <xsl:with-param name="fieldvalue" select="$fieldvalue"/>
            </xsl:call-template>
		</xsl:if>
  	</xsl:template>
  		
    <!--  WRITE SORTFIELD -->
    <xsl:template name="writeSortField">
        <xsl:param name="context"/>
        <xsl:param name="fieldname"/>
        <xsl:param name="fieldvalue"/>
        <xsl:if test="string($fieldvalue) 
                    and normalize-space($fieldvalue)!=''
                    and sortfield-helper:checkSortField(concat($SORTCONTEXTPREFIX,'.',$context,'.',$fieldname)) = false()">
            <IndexField termVector="NO" index="UN_TOKENIZED" store="NO">
                <xsl:attribute name="IFname">
                    <xsl:value-of select="concat($SORTCONTEXTPREFIX,'.',$context,'.',$fieldname)"/>
                </xsl:attribute>
                <xsl:value-of select="string-helper:getNormalizedString($fieldvalue)"/>
            </IndexField>
        </xsl:if>
    </xsl:template>
        
	<!-- WRITE USERDEFINED INDEX -->
	<xsl:template name="writeUserdefinedIndexes">
		<xsl:for-each select="xalan:nodeset($userdefined-indexes)/userdefined-index">
			<xsl:variable name="index-name" select="./@name"/>
			<xsl:variable name="context" select="./@context"/>
			<xsl:for-each select="./element">
		    	<xsl:if test="string(.) and normalize-space(.)!=''">
                    <xsl:call-template name="writeIndexField">
                        <xsl:with-param name="context" select="$context"/>
                        <xsl:with-param name="fieldname" select="$index-name"/>
                        <xsl:with-param name="fieldvalue" select="."/>
                        <xsl:with-param name="indextype" select="./@index"/>
                        <xsl:with-param name="store" select="$STORE_FOR_SCAN"/>
                    </xsl:call-template>
				</xsl:if>
			</xsl:for-each>
  		</xsl:for-each>
  	</xsl:template>
  		
	<!-- SORTFIELDS -->
	<xsl:variable name="sortfields">
		<!-- sortfield type="item" name="most-recent-date">
				<xsl:attribute name="path">
					<xsl:value-of select="lastdate-helper:getLastDate($ITEM_METADATAPATH//*[local-name()='created'],$ITEM_METADATAPATH//*[local-name()='modified'],$ITEM_METADATAPATH//*[local-name()='dateSubmitted'],$ITEM_METADATAPATH//*[local-name()='dateAccepted'],$ITEM_METADATAPATH//*[local-name()='issued'],//*[local-name()='last-revision']/*[local-name()='date'])"/>
				</xsl:attribute>
		</sortfield>
		<sortfield type="container" name="most-recent-date">
				<xsl:attribute name="path">
					<xsl:value-of select="lastdate-helper:getLastDate($CONTAINER_METADATAPATH//*[local-name()='created'],$CONTAINER_METADATAPATH//*[local-name()='modified'],$CONTAINER_METADATAPATH//*[local-name()='dateSubmitted'],$CONTAINER_METADATAPATH//*[local-name()='dateAccepted'],$CONTAINER_METADATAPATH//*[local-name()='issued'],//*[local-name()='last-revision']/*[local-name()='date'])"/>
				</xsl:attribute>
		</sortfield -->
	</xsl:variable>
	
	<!-- USER DEFINED INDEX FIELDS -->
	<xsl:variable name="userdefined-indexes" xmlns:xlink="http://www.w3.org/1999/xlink">
		<userdefined-index name="metadata">
			<xsl:attribute name="context">
				<xsl:value-of select="$CONTEXTNAME"/>
			</xsl:attribute>
			<xsl:for-each select="$ITEM_METADATAPATH//*[local-name()='identifier']">
				<xsl:variable name="idtype" select="string-helper:getSubstringAfterLast(./@*[local-name()='type'],':')" />
				<xsl:if test="string($idtype) 
						and normalize-space($idtype)!=''">
					<element index="TOKENIZED">
						<xsl:value-of select="concat($idtype,':',.)"/>
					</element>
					<element index="TOKENIZED">
						<xsl:value-of select="concat($idtype,' ',.)"/>
					</element>
				</xsl:if>
			</xsl:for-each>
            <xsl:for-each select="$CONTAINER_METADATAPATH//*[local-name()='identifier']">
                <xsl:variable name="idtype" select="string-helper:getSubstringAfterLast(./@*[local-name()='type'],':')" />
                <xsl:if test="string($idtype) 
                        and normalize-space($idtype)!=''">
                    <element index="TOKENIZED">
                        <xsl:value-of select="concat($idtype,':',.)"/>
                    </element>
                    <element index="TOKENIZED">
                        <xsl:value-of select="concat($idtype,' ',.)"/>
                    </element>
                </xsl:if>
            </xsl:for-each>
			<element index="TOKENIZED">
				<xsl:value-of select="string-helper:removeVersionIdentifier(string-helper:getSubstringAfterLast(/*[local-name()='item']/@*[local-name()='href'], '/'))"/>
			</element>
			<element index="TOKENIZED">
				<xsl:value-of select="$ITEM_PROPERTIESPATH/*[local-name()='pid']"/>
			</element>
			<element index="TOKENIZED">
				<xsl:value-of select="$ITEM_PROPERTIESPATH/*[local-name()='latest-release']/*[local-name()='pid']"/>
			</element>
			<element index="TOKENIZED">
				<xsl:value-of select="string-helper:removeVersionIdentifier(string-helper:getSubstringAfterLast(/*[local-name()='container']/@*[local-name()='href'], '/'))"/>
			</element>
			<element index="TOKENIZED">
				<xsl:value-of select="$CONTAINER_PROPERTIESPATH/*[local-name()='pid']"/>
			</element>
			<element index="TOKENIZED">
				<xsl:value-of select="$CONTAINER_PROPERTIESPATH/*[local-name()='latest-release']/*[local-name()='pid']"/>
			</element>
			<xsl:for-each select="$COMPONENT_PATH">
				<element index="TOKENIZED">
					<xsl:value-of select="./*[local-name()='properties']/*[local-name()='pid']"/>
				</element>
			</xsl:for-each>
		</userdefined-index>
        <userdefined-index name="context.objid">
            <xsl:attribute name="context">
                <xsl:value-of select="$CONTEXTNAME"/>
            </xsl:attribute>
            <element index="TOKENIZED">
                <xsl:value-of select="string-helper:getSubstringAfterLast($ITEM_PROPERTIESPATH/*[local-name()='context']/@*[local-name()='href'], '/')"/>
            </element>
            <element index="TOKENIZED">
                <xsl:value-of select="string-helper:getSubstringAfterLast($CONTAINER_PROPERTIESPATH/*[local-name()='context']/@*[local-name()='href'], '/')"/>
            </element>
        </userdefined-index>
        <userdefined-index name="content-model.objid">
            <xsl:attribute name="context">
                <xsl:value-of select="$CONTEXTNAME"/>
            </xsl:attribute>
            <element index="TOKENIZED">
                <xsl:value-of select="string-helper:getSubstringAfterLast($ITEM_PROPERTIESPATH/*[local-name()='content-model']/@*[local-name()='href'], '/')"/>
            </element>
            <element index="TOKENIZED">
                <xsl:value-of select="string-helper:getSubstringAfterLast($CONTAINER_PROPERTIESPATH/*[local-name()='content-model']/@*[local-name()='href'], '/')"/>
            </element>
        </userdefined-index>
        <userdefined-index name="publication.type">
            <xsl:attribute name="context">
                <xsl:value-of select="$CONTEXTNAME"/>
            </xsl:attribute>
            <element index="TOKENIZED">
                <xsl:value-of select="$ITEM_METADATAPATH/*[local-name()='publication']/@type"/>
            </element>
            <element index="TOKENIZED">
                <xsl:value-of select="$CONTAINER_METADATAPATH/*[local-name()='publication']/@type"/>
            </element>
        </userdefined-index>
		<userdefined-index name="most-recent-date">
			<xsl:attribute name="context">
				<xsl:value-of select="$CONTEXTNAME"/>
			</xsl:attribute>
			<element index="TOKENIZED">
					<xsl:value-of select="lastdate-helper:getLastDate($ITEM_METADATAPATH//*[local-name()='created'],$ITEM_METADATAPATH//*[local-name()='modified'],$ITEM_METADATAPATH//*[local-name()='dateSubmitted'],$ITEM_METADATAPATH//*[local-name()='dateAccepted'],$ITEM_METADATAPATH//*[local-name()='issued'],$ITEM_METADATAPATH//*[local-name()='published-online'])"/>
			</element>
			<element index="TOKENIZED">
					<xsl:value-of select="lastdate-helper:getLastDate($CONTAINER_METADATAPATH//*[local-name()='created'],$CONTAINER_METADATAPATH//*[local-name()='modified'],$CONTAINER_METADATAPATH//*[local-name()='dateSubmitted'],$CONTAINER_METADATAPATH//*[local-name()='dateAccepted'],$CONTAINER_METADATAPATH//*[local-name()='issued'],$CONTAINER_METADATAPATH//*[local-name()='published-online'])"/>
			</element>
		</userdefined-index>
		<userdefined-index name="most-recent-date.status">
			<xsl:attribute name="context">
				<xsl:value-of select="$CONTEXTNAME"/>
			</xsl:attribute>
			<element index="TOKENIZED">
					<xsl:value-of select="lastdate-helper:getLastDateElement($CONTEXTNAME, $ITEM_METADATAPATH//*[local-name()='created'],$ITEM_METADATAPATH//*[local-name()='modified'],$ITEM_METADATAPATH//*[local-name()='dateSubmitted'],$ITEM_METADATAPATH//*[local-name()='dateAccepted'],$ITEM_METADATAPATH//*[local-name()='issued'],$ITEM_METADATAPATH//*[local-name()='published-online'])"/>
			</element>
			<element index="TOKENIZED">
					<xsl:value-of select="lastdate-helper:getLastDateElement($CONTEXTNAME, $CONTAINER_METADATAPATH//*[local-name()='created'],$CONTAINER_METADATAPATH//*[local-name()='modified'],$CONTAINER_METADATAPATH//*[local-name()='dateSubmitted'],$CONTAINER_METADATAPATH//*[local-name()='dateAccepted'],$CONTAINER_METADATAPATH//*[local-name()='issued'],$CONTAINER_METADATAPATH//*[local-name()='published-online'])"/>
			</element>
		</userdefined-index>
        <userdefined-index name="creator.role">
            <xsl:attribute name="context">
                <xsl:value-of select="$CONTEXTNAME"/>
            </xsl:attribute>
            <xsl:for-each select="$ITEM_METADATAPATH//*[local-name()='creator']/@role">
                <element index="TOKENIZED">
                    <xsl:value-of select="."/>
                </element>
            </xsl:for-each>
            <xsl:for-each select="$CONTAINER_METADATAPATH//*[local-name()='creator']/@role">
                <element index="TOKENIZED">
                    <xsl:value-of select="."/>
                </element>
            </xsl:for-each>
        </userdefined-index>
		<userdefined-index name="content-model.name">
			<xsl:attribute name="context">
				<xsl:value-of select="$CONTEXTNAME"/>
			</xsl:attribute>
			<element index="TOKENIZED">
                <xsl:variable name="objectId" select="string-helper:getSubstringAfterLast($ITEM_PROPERTIESPATH/*[local-name()='content-model']/@*[local-name()='href'], '/')"/>
                <xsl:if test="string($objectId) and normalize-space($objectId)!=''">
                    <xsl:value-of select="escidoc-core-accessor:getObjectAttribute(
                        concat('/cmm/content-model/',$objectId),'/content-model/properties/name','','','false','false')"/>
                </xsl:if>
			</element>
            <element index="TOKENIZED">
                <xsl:variable name="objectId" select="string-helper:getSubstringAfterLast($CONTAINER_PROPERTIESPATH/*[local-name()='content-model']/@*[local-name()='href'], '/')"/>
                <xsl:if test="string($objectId) and normalize-space($objectId)!=''">
                    <xsl:value-of select="escidoc-core-accessor:getObjectAttribute(
                        concat('/cmm/content-model/',$objectId),'/content-model/properties/name','','','false','false')"/>
                </xsl:if>
            </element>
		</userdefined-index>
		<userdefined-index name="context.name">
			<xsl:attribute name="context">
				<xsl:value-of select="$CONTEXTNAME"/>
			</xsl:attribute>
			<element index="TOKENIZED">
                <xsl:variable name="objectId" select="string-helper:getSubstringAfterLast($ITEM_PROPERTIESPATH/*[local-name()='context']/@*[local-name()='href'], '/')"/>
                <xsl:if test="string($objectId) and normalize-space($objectId)!=''">
                    <xsl:value-of select="escidoc-core-accessor:getObjectAttribute(
                        concat('/ir/context/',$objectId,'/properties'),'/properties/name','','','false','false')"/>
                </xsl:if>
			</element>
            <element index="TOKENIZED">
                <xsl:variable name="objectId" select="string-helper:getSubstringAfterLast($CONTAINER_PROPERTIESPATH/*[local-name()='context']/@*[local-name()='href'], '/')"/>
                <xsl:if test="string($objectId) and normalize-space($objectId)!=''">
                    <xsl:value-of select="escidoc-core-accessor:getObjectAttribute(
                        concat('/ir/context/',$objectId,'/properties'),'/properties/name','','','false','false')"/>
                </xsl:if>
            </element>
		</userdefined-index>
		<userdefined-index name="created-by.name">
			<xsl:attribute name="context">
				<xsl:value-of select="$CONTEXTNAME"/>
			</xsl:attribute>
			<element index="TOKENIZED">
                <xsl:variable name="objectId" select="string-helper:getSubstringAfterLast($ITEM_PROPERTIESPATH/*[local-name()='created-by']/@*[local-name()='href'], '/')"/>
                <xsl:if test="string($objectId) and normalize-space($objectId)!=''">
                    <xsl:value-of select="escidoc-core-accessor:getObjectAttribute(
                        concat('/aa/user-account/',$objectId),'/user-account/properties/name','','','false','false')"/>
                </xsl:if>
			</element>
			<element index="TOKENIZED">
                <xsl:variable name="objectId" select="string-helper:getSubstringAfterLast($CONTAINER_PROPERTIESPATH/*[local-name()='created-by']/@*[local-name()='href'], '/')"/>
                <xsl:if test="string($objectId) and normalize-space($objectId)!=''">
                    <xsl:value-of select="escidoc-core-accessor:getObjectAttribute(
                        concat('/aa/user-account/',$objectId),'/user-account/properties/name','','','false','false')"/>
                </xsl:if>
			</element>
		</userdefined-index>
		<userdefined-index name="component.created-by.name">
			<xsl:attribute name="context">
				<xsl:value-of select="$CONTEXTNAME"/>
			</xsl:attribute>
			<xsl:for-each select="$COMPONENT_PROPERTIESPATH/*[local-name()='created-by']/@*[local-name()='href']">
				<element index="TOKENIZED">
                    <xsl:variable name="objectId" select="normalize-space(string-helper:getSubstringAfterLast(., '/'))"/>
                    <xsl:if test="string($objectId) and normalize-space($objectId)!=''">
                        <xsl:value-of select="escidoc-core-accessor:getObjectAttribute(
                            concat('/aa/user-account/',$objectId),'/user-account/properties/name','','','false','false')"/>
                    </xsl:if>
				</element>
  			</xsl:for-each>
		</userdefined-index>
        <userdefined-index name="last-modification-date">
            <xsl:attribute name="context">
                <xsl:value-of select="$CONTEXTNAME"/>
            </xsl:attribute>
            <element index="TOKENIZED">
                <xsl:value-of select="/*[local-name()='item']/@last-modification-date"/>
            </element>
            <element index="TOKENIZED">
                <xsl:value-of select="/*[local-name()='container']/@last-modification-date"/>
            </element>
        </userdefined-index>
		<userdefined-index name="member-count">
			<xsl:variable name="type">
				<xsl:for-each select="*">
					<xsl:if test="position() = 1">
						<xsl:value-of select="local-name()"/>
					</xsl:if>
				</xsl:for-each>
			</xsl:variable>
			<xsl:attribute name="context">
				<xsl:value-of select="$CONTEXTNAME"/>
			</xsl:attribute>
			<element index="UN_TOKENIZED">
				<xsl:if test="$type='container'">
					<xsl:value-of select="count(/*[local-name()='container']/*[local-name()='struct-map']/*)"/>
				</xsl:if>
			</element>
		</userdefined-index>
        <userdefined-index name="type">
            <xsl:attribute name="context">
                <xsl:value-of select="$CONTEXTNAME"/>
            </xsl:attribute>
            <element index="TOKENIZED">
                <xsl:value-of select="$ITEM_METADATAPATH/*/@type"/>
            </element>
            <element index="TOKENIZED">
                <xsl:value-of select="$CONTAINER_METADATAPATH/*/@type"/>
            </element>
        </userdefined-index>
        <userdefined-index name="source.type">
            <xsl:attribute name="context">
                <xsl:value-of select="$CONTEXTNAME"/>
            </xsl:attribute>
            <xsl:for-each select="$ITEM_METADATAPATH//*[local-name()='source']/@type">
                <element index="TOKENIZED">
                    <xsl:value-of select="."/>
                </element>
            </xsl:for-each>
            <xsl:for-each select="$CONTAINER_METADATAPATH//*[local-name()='source']/@type">
                <element index="TOKENIZED">
                    <xsl:value-of select="."/>
                </element>
            </xsl:for-each>
        </userdefined-index>
		<userdefined-index name="any-title">
			<xsl:attribute name="context">
				<xsl:value-of select="$CONTEXTNAME"/>
			</xsl:attribute>
			<xsl:variable name="fields">
				<xsl:copy-of select="$ITEM_METADATAPATH//*[local-name()='title']"/>
				<xsl:copy-of select="$ITEM_METADATAPATH//*[local-name()='alternative']"/>
				<xsl:copy-of select="/*[local-name()='item']/@xlink:title"/>
				<xsl:copy-of select="$CONTAINER_METADATAPATH//*[local-name()='title']"/>
				<xsl:copy-of select="$CONTAINER_METADATAPATH//*[local-name()='alternative']"/>
				<xsl:copy-of select="/*[local-name()='container']/@xlink:title"/>
			</xsl:variable>
			<xsl:for-each select="xalan:nodeset($fields)/*">
				<xsl:variable name="name" select="name()"/>
                <element index="TOKENIZED">
                    <xsl:value-of select="."/>
                </element>
  			</xsl:for-each>
		</userdefined-index>
		<userdefined-index name="title">
			<xsl:attribute name="context">
				<xsl:value-of select="$CONTEXTNAME"/>
			</xsl:attribute>
			<xsl:variable name="fields">
				<xsl:copy-of select="/*[local-name()='item']/@xlink:title"/>
				<xsl:copy-of select="/*[local-name()='container']/@xlink:title"/>
			</xsl:variable>
			<xsl:for-each select="xalan:nodeset($fields)/*">
				<xsl:variable name="name" select="name()"/>
                <element index="TOKENIZED">
                    <xsl:value-of select="."/>
                </element>
  			</xsl:for-each>
		</userdefined-index>
		<userdefined-index name="any-topic">
			<xsl:attribute name="context">
				<xsl:value-of select="$CONTEXTNAME"/>
			</xsl:attribute>
			<xsl:variable name="fields">
				<xsl:copy-of select="$ITEM_METADATAPATH//*[local-name()='title']"/>
				<xsl:copy-of select="$ITEM_METADATAPATH//*[local-name()='alternative']"/>
				<xsl:copy-of select="$ITEM_METADATAPATH//*[local-name()='tableOfContents']"/>
				<xsl:copy-of select="$ITEM_METADATAPATH//*[local-name()='abstract']"/>
				<xsl:copy-of select="$ITEM_METADATAPATH//*[local-name()='subject']"/>
				<xsl:copy-of select="$CONTAINER_METADATAPATH//*[local-name()='title']"/>
				<xsl:copy-of select="$CONTAINER_METADATAPATH//*[local-name()='alternative']"/>
				<xsl:copy-of select="$CONTAINER_METADATAPATH//*[local-name()='tableOfContents']"/>
				<xsl:copy-of select="$CONTAINER_METADATAPATH//*[local-name()='abstract']"/>
				<xsl:copy-of select="$CONTAINER_METADATAPATH//*[local-name()='subject']"/>
			</xsl:variable>
			<xsl:for-each select="xalan:nodeset($fields)/*">
				<xsl:variable name="name" select="name()"/>
                <element index="TOKENIZED">
                    <xsl:value-of select="."/>
                </element>
  			</xsl:for-each>
		</userdefined-index>
		<userdefined-index name="any-persons">
			<xsl:attribute name="context">
				<xsl:value-of select="$CONTEXTNAME"/>
			</xsl:attribute>
			<xsl:for-each select="$ITEM_METADATAPATH//*[local-name()='creator']">
				<xsl:if test="string(./*[local-name()='person']) 
						and normalize-space(./*[local-name()='person'])!=''">
					<element index="TOKENIZED">
						<xsl:value-of select="./*[local-name()='person']/*[local-name()='given-name']"/>
					</element>
					<element index="TOKENIZED">
						<xsl:value-of select="./*[local-name()='person']/*[local-name()='family-name']"/>
					</element>
					<element index="TOKENIZED">
						<xsl:value-of select="./*[local-name()='person']/*[local-name()='complete-name']"/>
					</element>
				</xsl:if>
			</xsl:for-each>
			<xsl:for-each select="$CONTAINER_METADATAPATH//*[local-name()='creator']">
				<xsl:if test="string(./*[local-name()='person']) 
						and normalize-space(./*[local-name()='person'])!=''">
					<element index="TOKENIZED">
						<xsl:value-of select="./*[local-name()='person']/*[local-name()='given-name']"/>
					</element>
					<element index="TOKENIZED">
						<xsl:value-of select="./*[local-name()='person']/*[local-name()='family-name']"/>
					</element>
					<element index="TOKENIZED">
						<xsl:value-of select="./*[local-name()='person']/*[local-name()='complete-name']"/>
					</element>
				</xsl:if>
			</xsl:for-each>
		</userdefined-index>
		<userdefined-index name="any-organizations">
			<xsl:attribute name="context">
				<xsl:value-of select="$CONTEXTNAME"/>
			</xsl:attribute>
			<xsl:for-each select="$ITEM_METADATAPATH//*[local-name()='organization-name']">
				<element index="TOKENIZED">
					<xsl:value-of select="."/>
				</element>
			</xsl:for-each>
			<xsl:for-each select="$CONTAINER_METADATAPATH//*[local-name()='organization-name']">
				<element index="TOKENIZED">
					<xsl:value-of select="."/>
				</element>
			</xsl:for-each>
		</userdefined-index>
		<userdefined-index name="any-organization-pids">
			<xsl:attribute name="context">
				<xsl:value-of select="$CONTEXTNAME"/>
			</xsl:attribute>
			<xsl:for-each select="$ITEM_METADATAPATH//*[local-name()='creator']//*[local-name()='organization']/*[local-name()='identifier']">
				<element index="TOKENIZED">
                    <xsl:variable name="objectId" select="normalize-space(.)"/>
                    <xsl:if test="string($objectId) and normalize-space($objectId)!=''">
                        <xsl:value-of select="escidoc-core-accessor:getObjectAttribute(
                            concat('/oum/organizational-unit/',$objectId,'/resources/path-list'),'/organizational-unit-path-list/organizational-unit-path/organizational-unit-ref','href','http://www.w3.org/1999/xlink','false','true')"/>
                    </xsl:if>
				</element>
			</xsl:for-each>
			<xsl:for-each select="$CONTAINER_METADATAPATH//*[local-name()='creator']//*[local-name()='organization']/*[local-name()='identifier']">
				<element index="TOKENIZED">
                    <xsl:variable name="objectId" select="normalize-space(.)"/>
                    <xsl:if test="string($objectId) and normalize-space($objectId)!=''">
                        <xsl:value-of select="escidoc-core-accessor:getObjectAttribute(
                            concat('/oum/organizational-unit/',$objectId,'/resources/path-list'),'/organizational-unit-path-list/organizational-unit-path/organizational-unit-ref','href','http://www.w3.org/1999/xlink','false','true')"/>
                    </xsl:if>
				</element>
			</xsl:for-each>
		</userdefined-index>
		<userdefined-index name="any-genre">
			<xsl:attribute name="context">
				<xsl:value-of select="$CONTEXTNAME"/>
			</xsl:attribute>
			<xsl:for-each select="$ITEM_METADATAPATH//*">
				<element index="TOKENIZED">
					<xsl:value-of select="./@type"/>
				</element>
			</xsl:for-each>
			<xsl:for-each select="$CONTAINER_METADATAPATH//*">
				<element index="TOKENIZED">
					<xsl:value-of select="./@type"/>
				</element>
			</xsl:for-each>
		</userdefined-index>
		<userdefined-index name="any-dates">
			<xsl:attribute name="context">
				<xsl:value-of select="$CONTEXTNAME"/>
			</xsl:attribute>
			<xsl:for-each select="$ITEM_METADATAPATH//*">
				<element index="TOKENIZED">
					<xsl:value-of select="./*[local-name()='created']"/>
				</element>
				<element index="TOKENIZED">
					<xsl:value-of select="./*[local-name()='modified']"/>
				</element>
				<element index="TOKENIZED">
					<xsl:value-of select="./*[local-name()='dateSubmitted']"/>
				</element>
				<element index="TOKENIZED">
					<xsl:value-of select="./*[local-name()='dateAccepted']"/>
				</element>
				<element index="TOKENIZED">
					<xsl:value-of select="./*[local-name()='issued']"/>
				</element>
				<element index="TOKENIZED">
					<xsl:value-of select="./*[local-name()='published-online']"/>
				</element>
			</xsl:for-each>
			<xsl:for-each select="$CONTAINER_METADATAPATH//*">
				<element index="TOKENIZED">
					<xsl:value-of select="./*[local-name()='created']"/>
				</element>
				<element index="TOKENIZED">
					<xsl:value-of select="./*[local-name()='modified']"/>
				</element>
				<element index="TOKENIZED">
					<xsl:value-of select="./*[local-name()='dateSubmitted']"/>
				</element>
				<element index="TOKENIZED">
					<xsl:value-of select="./*[local-name()='dateAccepted']"/>
				</element>
				<element index="TOKENIZED">
					<xsl:value-of select="./*[local-name()='issued']"/>
				</element>
				<element index="TOKENIZED">
					<xsl:value-of select="./*[local-name()='published-online']"/>
				</element>
			</xsl:for-each>
		</userdefined-index>
		<userdefined-index name="any-event">
			<xsl:attribute name="context">
				<xsl:value-of select="$CONTEXTNAME"/>
			</xsl:attribute>
			<xsl:variable name="fields">
				<xsl:copy-of select="$ITEM_METADATAPATH//*[local-name()='event']/*[local-name()='title']"/>
				<xsl:copy-of select="$ITEM_METADATAPATH//*[local-name()='event']/*[local-name()='alternative']"/>
				<xsl:copy-of select="$ITEM_METADATAPATH//*[local-name()='event']/*[local-name()='place']"/>
				<xsl:copy-of select="$CONTAINER_METADATAPATH//*[local-name()='event']/*[local-name()='title']"/>
				<xsl:copy-of select="$CONTAINER_METADATAPATH//*[local-name()='event']/*[local-name()='alternative']"/>
				<xsl:copy-of select="$CONTAINER_METADATAPATH//*[local-name()='event']/*[local-name()='place']"/>
			</xsl:variable>
			<xsl:for-each select="xalan:nodeset($fields)/*">
				<xsl:variable name="name" select="name()"/>
                <element index="TOKENIZED">
                    <xsl:value-of select="."/>
                </element>
  			</xsl:for-each>
		</userdefined-index>
		<userdefined-index name="any-source">
			<xsl:attribute name="context">
				<xsl:value-of select="$CONTEXTNAME"/>
			</xsl:attribute>
			<xsl:variable name="fields">
				<xsl:copy-of select="$ITEM_METADATAPATH//*[local-name()='source']/*[local-name()='title']"/>
				<xsl:copy-of select="$ITEM_METADATAPATH//*[local-name()='source']/*[local-name()='alternative']"/>
				<xsl:copy-of select="$CONTAINER_METADATAPATH//*[local-name()='source']/*[local-name()='title']"/>
				<xsl:copy-of select="$CONTAINER_METADATAPATH//*[local-name()='source']/*[local-name()='alternative']"/>
			</xsl:variable>
			<xsl:for-each select="xalan:nodeset($fields)/*">
				<xsl:variable name="name" select="name()"/>
                <element index="TOKENIZED">
                    <xsl:value-of select="."/>
                </element>
  			</xsl:for-each>
		</userdefined-index>
		<userdefined-index name="any-identifier">
			<xsl:attribute name="context">
				<xsl:value-of select="$CONTEXTNAME"/>
			</xsl:attribute>
			<element index="TOKENIZED">
				<xsl:value-of select="string-helper:removeVersionIdentifier(string-helper:getSubstringAfterLast(/*[local-name()='item']/@*[local-name()='href'], '/'))"/>
			</element>
			<element index="TOKENIZED">
				<xsl:value-of select="$ITEM_PROPERTIESPATH/*[local-name()='pid']"/>
			</element>
			<element index="TOKENIZED">
				<xsl:value-of select="$ITEM_PROPERTIESPATH/*[local-name()='latest-release']/*[local-name()='pid']"/>
			</element>
			<element index="TOKENIZED">
				<xsl:value-of select="string-helper:removeVersionIdentifier(string-helper:getSubstringAfterLast(/*[local-name()='container']/@*[local-name()='href'], '/'))"/>
			</element>
			<element index="TOKENIZED">
				<xsl:value-of select="$CONTAINER_PROPERTIESPATH/*[local-name()='pid']"/>
			</element>
			<element index="TOKENIZED">
				<xsl:value-of select="$CONTAINER_PROPERTIESPATH/*[local-name()='latest-release']/*[local-name()='pid']"/>
			</element>
			<xsl:for-each select="$COMPONENT_PATH">
				<element index="TOKENIZED">
					<xsl:value-of select="./*[local-name()='properties']/*[local-name()='pid']"/>
				</element>
			</xsl:for-each>
			<xsl:for-each select="$ITEM_METADATAPATH//*[local-name()='identifier']">
				<xsl:variable name="idtype" select="string-helper:getSubstringAfterLast(./@*[local-name()='type'],':')" />
				<xsl:if test="string($idtype) 
						and normalize-space($idtype)!=''">
					<element index="TOKENIZED">
						<xsl:value-of select="concat($idtype,':',.)"/>
					</element>
					<element index="TOKENIZED">
						<xsl:value-of select="concat($idtype,' ',.)"/>
					</element>
				</xsl:if>
			</xsl:for-each>
            <xsl:for-each select="$CONTAINER_METADATAPATH//*[local-name()='identifier']">
                <xsl:variable name="idtype" select="string-helper:getSubstringAfterLast(./@*[local-name()='type'],':')" />
                <xsl:if test="string($idtype) 
                        and normalize-space($idtype)!=''">
                    <element index="TOKENIZED">
                        <xsl:value-of select="concat($idtype,':',.)"/>
                    </element>
                    <element index="TOKENIZED">
                        <xsl:value-of select="concat($idtype,' ',.)"/>
                    </element>
                </xsl:if>
            </xsl:for-each>
		</userdefined-index>
	</xsl:variable>

</xsl:stylesheet>	
