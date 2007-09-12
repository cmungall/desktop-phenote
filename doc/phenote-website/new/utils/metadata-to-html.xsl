<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">

  <xsl:output method="html"/>

  <xsl:template match="/">
    <html>
      <head>
        <title>OBO Ontologies</title> 
        <link href="stylesheet.css" rel="stylesheet" type="text/css" media="screen"/>
        <meta http-equiv="content-type" content="text/html; charset=iso-8859-1"/>
        <meta name="description" content="The ontologies available from the Open Biomedical Ontologies project."/>
        <script src="/obfuscate.js" type="text/javascript"></script>
      </head>
      <body>

        <div class="header">
          <ul>
            <li>
              <a href="/main.html">Main</a>
            </li>
            <li>
              <a href="/crit.html">Criteria</a>
            </li>
            <li>
              <a class="active" href="#">Ontologies</a>
            </li>
            <li>
              <a href="/browse.html">Browse</a>
            </li>
            <li>
              <a href="http://sourceforge.net/projects/obo">Project</a>
            </li>
            <li>
              <a href="http://cvs.sourceforge.net/cgi-bin/viewcvs.cgi/obo/obo/ontology/">CVS</a>
            </li>
            <li>
              <a href="http://lists.sourceforge.net/lists/listinfo/obo-discuss">Subscribe</a>
            </li>
            <li>
              <a href="mailto:obo-discuss@lists.sourceforge.net">Contact</a>
            </li>
          </ul>
        </div>
        <div class="content">
          <h1>OBO Ontologies</h1>
          <p>
            These are the ontologies currently lodged within OBO. Click on the column heading to sort the table and click on the ontology name for further information on the ontology.
          </p>

          <p>
            A subset of the OBO ontologies have the tag <b>Gene Ontology Consortium core ontology</b>. This designates a subset of ontologies that are intended to be used in combination with each other for the generation of compound ontologies using cross-products.
          </p>
        
          <p>
          
            <a href="table.cgi?core">Show only Gene Ontology Consortium core ontologies</a>
            
          </p>
          
          <div class="tab">
            <table>
              <tr>
                <th style="width: 30%">
                  <a href="table.cgi?title">Domain</a>
                </th>
                <th style="width: 10%">
                  <a href="table.cgi?namespace">Prefix</a>
                </th>
                <th style="width: 50%">
                  <a href="table.cgi?ontology">Files</a>
                </th>
                <th style="width: 10%">
                  <a href="table.cgi?c">Core</a>
                </th>
              </tr>
              <xsl:apply-templates select="obo_metadata/ont">
                <xsl:sort select="title"/>
              </xsl:apply-templates>
            </table>
          </div>
        </div>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="ont">
    <tr>
      <td class="tableOne">
        <a>
          <xsl:attribute name="href">
            <xsl:text>detail.cgi?</xsl:text>
            <xsl:value-of select="@id"/>
          </xsl:attribute>
          <xsl:value-of select="title"/>
        </a>
      </td>
      <td class="tableOne">
        <xsl:value-of select="namespace"/>
      </td>
      <td class="tableOne">
        <xsl:for-each select="export">
          <xsl:if test="not(@problem)">
            <a>
              <xsl:attribute name="href">
                <xsl:value-of select="@path"/>
              </xsl:attribute>
              <xsl:value-of select="@format"/>
            </a>
            <xsl:text> | </xsl:text>
          </xsl:if>
        </xsl:for-each>
      </td>
    </tr>
  </xsl:template>

</xsl:stylesheet>

