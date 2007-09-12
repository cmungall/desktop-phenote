<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">

  <xsl:output method="html"/>

  <xsl:template match="/">
    <html>
      <head profile="http://www.berkeleybop.org/ontologies/obo-all/ontology_index.rdf">
        <title>OBO Download Matrix</title> 
        <link href="stylesheet.css" rel="stylesheet" type="text/css" media="screen"/>
        <link rel="meta" type="application/rdf+xml" href="obo-all/ontology_index.rdf" />
        <link rel="meta" type="application/rdf+xml" title="FOAF" href="http://www.berkeleybop.org/content/people/cjm/chris-mungall-foaf.rdf"/>
        <meta http-equiv="content-type" content="text/html; charset=iso-8859-1"/>
        <meta name="description" content="The ontologies available from the Open Biomedical Ontologies project."/>
        <meta name="keywords" content="ontology, ontologies, owl, obo, biological, biomedical, terminology, rdf, download, open-source"/>
        <meta name="dc.creator" content="Chris Mungall"/>
        <style type="text/css">
          <xsl:comment>
.metadata p
{
        font-size: 75%;
}
.item
{
        font-size: 125%;
}
li
{	
	padding: 0;
	margin: 0;
}
.formatlist li
{
        font-size: 80%;
        margin-left: 8px;
}
.caveats li
{
            list-style: disc;
            margin: 8px;
}
.warning
{
            color: #f00;
            margin-left: 24px;
}

#dhtmltooltip
{
        font-size: 80%;
            position: absolute;
            width: 150px;
            border: 2px solid black;
            padding: 2px;
            background-color: lightyellow;
            visibility: hidden;
            z-index: 100;
            /*Remove below line to remove shadow. Below line should always appear last within this CSS*/
            filter: progid:DXImageTransform.Microsoft.Shadow(color=gray,direction=135);
}
          </xsl:comment>
        </style>

      </head>
      <body>
        <div id="dhtmltooltip"></div>

        <script type="text/javascript">
          <xsl:text>
<![CDATA[
/***********************************************
* Cool DHTML tooltip script- © Dynamic Drive DHTML code library (www.dynamicdrive.com)
* This notice MUST stay intact for legal use
* Visit Dynamic Drive at http://www.dynamicdrive.com/ for full source code
***********************************************/

var offsetxpoint=-60 //Customize x offset of tooltip
var offsetypoint=20 //Customize y offset of tooltip
var ie=document.all
var ns6=document.getElementById && !document.all
var enabletip=false
if (ie||ns6)
var tipobj=document.all? document.all["dhtmltooltip"] : document.getElementById? document.getElementById("dhtmltooltip") : ""

function ietruebody(){
return (document.compatMode && document.compatMode!="BackCompat")? document.documentElement : document.body
}

function ddrivetip(thetext, thecolor, thewidth){
if (ns6||ie){
if (typeof thewidth!="undefined") tipobj.style.width=thewidth+"px"
if (typeof thecolor!="undefined" && thecolor!="") tipobj.style.backgroundColor=thecolor
tipobj.innerHTML=thetext
enabletip=true
return false
}
}

function positiontip(e){
if (enabletip){
var curX=(ns6)?e.pageX : event.clientX+ietruebody().scrollLeft;
var curY=(ns6)?e.pageY : event.clientY+ietruebody().scrollTop;
//Find out how close the mouse is to the corner of the window
var rightedge=ie&&!window.opera? ietruebody().clientWidth-event.clientX-offsetxpoint : window.innerWidth-e.clientX-offsetxpoint-20
var bottomedge=ie&&!window.opera? ietruebody().clientHeight-event.clientY-offsetypoint : window.innerHeight-e.clientY-offsetypoint-20

var leftedge=(offsetxpoint < 0)? offsetxpoint*(-1) : -1000

//if the horizontal distance isn't enough to accomodate the width of the context menu
if (rightedge<tipobj.offsetWidth)
//move the horizontal position of the menu to the left by it's width
tipobj.style.left=ie? ietruebody().scrollLeft+event.clientX-tipobj.offsetWidth+"px" : window.pageXOffset+e.clientX-tipobj.offsetWidth+"px"
else if (curX<leftedge)
tipobj.style.left="5px"
else
//position the horizontal position of the menu where the mouse is positioned
tipobj.style.left=curX+offsetxpoint+"px"

//same concept with the vertical position
if (bottomedge<tipobj.offsetHeight)
tipobj.style.top=ie? ietruebody().scrollTop+event.clientY-tipobj.offsetHeight-offsetypoint+"px" : window.pageYOffset+e.clientY-tipobj.offsetHeight-offsetypoint+"px"
else
tipobj.style.top=curY+offsetypoint+"px"
tipobj.style.visibility="visible"
}
}

function hideddrivetip(){
if (ns6||ie){
enabletip=false
tipobj.style.visibility="hidden"
tipobj.style.left="-1000px"
tipobj.style.backgroundColor=''
tipobj.style.width=''
}
}

document.onmousemove=positiontip
  ]]>
          </xsl:text>
        </script>
        <div class="header">
          <ul>
            <li>
              <a href="http://obo.sourceforge.net/main.html">Main</a>
            </li>
            <li>
              <a href="http://obo.sourceforge.net/crit.html">Criteria</a>
            </li>
            <li>
              <a class="active" href="#">Ontologies</a>
            </li>
            <li>
              <a href="http://obo.sourceforge.net/browse.html">Browse</a>
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
          <h1>OBO Download Matrix</h1>
          <p>
            This page is for downloading OBO ontologies in a variety
            of formats, and for downloading reports from each of the
            ontologies. The data below is derived automatically from
            the primary sources, available from the main OBO
            website. The status of this page is EXPERIMENTAL. See
            below for caveats.
          </p>
          <p>
            This page will be retired when the current functionality
            is subsumed by the BioPortal resource developed by the <a
            href="http://www.ncbo.us">NCBO</a>
          </p>
          <ul>
            <li><a href="#ontologies">Ontologies</a></li>
            <li><a href="#logical_definitions">Logical Definitions</a></li>
            <li><a href="#mappings">Mappings</a></li>
            <li><a href="#formats">Format Guide</a></li>
            <li><a href="#metadata">Metadata</a></li>
            <li><a href="#reports">Reports</a></li>
            <li><a href="#download">FTP Archive</a></li>
            <li><a href="#statistics">Statistics</a></li>
          </ul>
          
          <a name="ontologies"/>
          <h2>Ontologies</h2>
          
          <div class="tab">
            <table>
              <tr>
                <th style="width: 25%">
                  <a href="table.cgi?title">Ontology</a>
                </th>
                <th style="width: 10%">
                  <a href="table.cgi?namespace">ID Prefix</a>
                </th>
                <th style="width: 65%">
                  <a href="table.cgi?ontology">Files</a>
                </th>
              </tr>
              <xsl:apply-templates select="obo_metadata/ont[not(type) or type='ontologies']">
                <xsl:sort select="@id"/>
              </xsl:apply-templates>
            </table>
          </div>

          <a name="logical_definitions"/>
          <h2>Logical definitions (cross-products)</h2>

          <p>
            The following are supplementary to the above ontologies - they enhance existing ontologies with <i>logical definitions</i> (also known colloquially in the GO as <i>cross-products</i>). For more information, see <a href="http://www.bioontology.org/wiki/index.php/XP:Main_Page">Cross-Products</a>. These files are generated using the <a href="http://obo.cvs.sourceforge.net/obo/obo/website/cgi-bin/mappings.txt?view=log">Mappings Metadata File</a>
          </p>

          <div class="tab">
            <table>
              <tr>
                <th style="width: 25%">
                  <a href="table.cgi?title">Ontology</a>
                </th>
                <th style="width: 10%">
                  <a href="table.cgi?namespace">ID Prefix</a>
                </th>
                <th style="width: 65%">
                  <a href="table.cgi?ontology">Files</a>
                </th>
              </tr>
              <xsl:apply-templates select="obo_metadata/ont[type='logical_definitions']">
                <xsl:sort select="@id"/>
              </xsl:apply-templates>
            </table>
          </div>

          <a name="mappings"/>
          <h2>Mappings</h2>

          <p>
            Mappings between two ontologies
          </p>

          <div class="tab">
            <table>
              <tr>
                <th style="width: 25%">
                  <a href="table.cgi?title">Ontology</a>
                </th>
                <th style="width: 10%">
                  <a href="table.cgi?namespace">ID Prefix</a>
                </th>
                <th style="width: 65%">
                  <a href="table.cgi?ontology">Files</a>
                </th>
              </tr>
              <xsl:apply-templates select="obo_metadata/ont[type='mappings' or type='bridge' or type='metadata']">
                <xsl:sort select="@id"/>
              </xsl:apply-templates>
            </table>
          </div>

          <div class="metadata">
            Export started on:
            <xsl:value-of select="obo_metadata/time_started"/>
            Completed on:
            <xsl:value-of select="obo_metadata/time_completed"/>
          </div>
          <div class="content">

            <a name="formats"/>
            <h2>Formats</h2>
            <p>
              The following formats are generated. See below for
              caveats. All indexed ontologies can be downloaded
              en-masse for any one particular format, see download
              section below. The format metadata spec can be found <a
              rel="meta" type="application/rdf+xml"
              href="http://purl.org/obo/metadata#">Here</a>
            </p>
            <ul class="formatlist">
              <li>
                <div class="item">obo</div> -- The <a href="http://www.geneontology.org/GO.format.obo-1_2.shtml">Obo text</a> format. If this is not the source format, this is generated using flat2obo (part of obo-edit)
              </li>
              <li>
                <div class="item">obo_xml</div> -- The <a href="http://www.godatabase.org/dev/xml/dtd/obo-xml.dtd">Obo XML</a> format. Generated using <a href="http://www.godatabase.org/dev/go-perl/doc/go-perl-doc.html">go-perl</a>
              </li>
              <li>
                <div class="item">go_ont</div> -- The old <i>deprecated</i> go DAG format. Generated using <a href="http://www.godatabase.org/dev/go-perl/doc/go-perl-doc.html">go-perl</a>
              </li>
              <li>
                <div class="item">owl</div> -- See <a href="http://www.w3.org/TR/owl-features">W3 OWL page</a> for details. Generated from obo-xml using <a href="http://www.godatabase.org/dev/xml/xsl/oboxml_to_owl.xsl">oboxml_to_owl.xsl</a>. See <a href="http://xrl.us/oboinowl">OboInOwl</a> for details of mapping. All obo files converted to owl are made available using the <a href="http://purl.org/obo/">http://purl.org/obo/</a> URI scheme; for example <a href="http://purl.org/obo/GO">http://purl.org/obo/GO</a>
              </li>
              <li>
                <div class="item">chadoxml</div> -- See <a href="http://www.gmod.org/chado_xml_doc">Chado-XML</a> pages. This format can be loaded into a Chado-schema database using a generic loader such as <a href="http://search.cpan.org/~cmungall/DBIx-DBStag">DBStag</a>.
              </li>
              <li>
                <div class="item">godb_prestore</div> -- This format can be loaded into a <a href="http://www.godatabase.org/dev/sql/doc/godb-sql-doc.html">GO DB schema</a> database using a generic loader such as <a href="http://search.cpan.org/~cmungall/DBIx-DBStag">DBStag</a>.
              </li>
              <li>
                <div class="item">go_rdf</div> -- <a href="http://www.geneontology.org/GO.format.shtml#XML">GO RDF XML</a> format. May soon be deprecated in favour of the OWL export.
              </li>
              <li>
                <div class="item">tbl</div> -- Simple table-based format
              </li>
              <li>
                <div class="item">prolog</div> -- Prolog
                database. Can be reasoned over using a prolog
                engine. See <a
                href="http://www.berkeleybop.org/obol">obol</a> and <a href="http://www.blipkit.org">blip</a>.
              </li>
              <li>
                <div class="item">owl-classified-by-pellet</div> -- result of running the OWL through the peller classifier. Potentially includes missing subclasses
              </li>
              <li>
                <div class="item">obo-classified-by-oboedit</div> -- result of running the obo through the oboedit reasoner. Potentially includes missing is_a and relationship links. For best results, see the logical definitions files.
              </li>
            </ul>
            <a name="reports"/>
            <h2>Reports</h2>
            <p>
              The following reports are generated from each of the ontologies that are indexable
            </p>
            <ul class="formatlist">
              <li>
                <div class="item">validation_report</div> -- reports
                redundant relationships between classes. This report
                will be extended to report inconsistences obtained by
                using a reasoning engine
              </li>
              <li>
                <div class="item">error_report</div> -- reports any
                syntax errors
              </li>
              <li>
                <div class="item">stats</div> -- Ontology statistics
              </li>
            </ul>
            <a name="download"/>
            <h2>Download</h2>
            <p>
              All the above are available for download on an ftp site:
              <ul>
                <li>
                  <a
                    href="ftp://ftp.fruitfly.org/pub/obo">ftp://ftp.fruitfly.org/pub/obo</a>
              
                </li>
              </ul>
            </p>
            <p>
              Compressed tar files are available for the entire
              download matrix, and also on a per-format basis. No
              historic archive is maintained, beyond what is provided
              by the projects managing the individual ontologies
            </p>
            <a name="metadata"/>
            <h2>Metadata</h2>
            <p>
              Metadata derived from the above ontologies can be
              downloaded as an xml file here:
              <ul>
                <li>
                  <a rel="meta" href="obo-all/ontology_index.xml">obo-all/ontology_index.xml</a> -- XML
              
                </li>
                <li>
                  <a rel="meta" type="application/rdf+xml" href="obo-all/ontology_index.rdf">obo-all/ontology_index.rdf</a> -- RDF
              
                </li>
              </ul>
            </p>
            <a name="statistics"/>
            <h2>Statistics</h2>
            <p>
              Statistics on each indexable ontology is performed as
              part of the creation of this resource. Individual
              statistics files are available, per-ontology, above.

              <ul>
                <li>
                  <a href="stats.tbl">stats.tbl</a> -- tab delimited text
                </li>
                <li>
                  <a href="stats.html">stats.html</a> -- DHTML table (click on columns to sort)
                </li>
              </ul>
            </p>
            <p>
              The tab-delimited
              version can be used with unix tools such as sort and
              grep, or imported into an application like Excel.
            </p>
            <p>
              You can also download stats from previous versions in the
              <a href="ftp://ftp.fruitfly.org/pub/obo/archive">archive</a>
            </p>
            <h3>Alignment</h3>
            <p>
              You can download a basic all-vs-all text alignment of
              classes in the indexed OBO files above. This is based on
              text matching using names and synonyms.
              <ul>
                <li>
                  <a
                    href="ftp://ftp.fruitfly.org/pub/obo/reports">ftp://ftp.fruitfly.org/pub/obo/reports</a>
              
                </li>
              </ul>
            </p>
            <p>
              This file is updated irregularly and may be out of sync
              with the above files
            </p>
            <h2>Browsing and querying</h2>
            <p>
              As yet there is no way to browse all the ontologies as a
              whole. In future we may provide this using a tool such
              as AmiGO.
            </p>
            <a name="caveats"/>
            <h2>Caveats</h2>
            <ul class="caveats">
              <li>
                This web page should be considered alpha. The contents
                may change in the future. The URL is not stable, and
                caution should be exercised when linking to it
              </li>
              <li>
                Not every obo ontology is indexed. Currently we index
                (deprecated) GO and Obo formatted files. There may be
                problems with GO formatted files. This is a deprecated
                format (note: everyone has now switched to obo or
                another suported format). Ontology maintainers should
                switch to Obo
                format. 
              </li>
              <li>
                Many ontologies reside on sourceforge. Sometimes an
                ontology will not download correctly because of a
                sourceforge glitch. If this happens, the ontology will
                be omitted from the list above, or the ontology from
                the previous week will be used. Eventually we would
                like a more robust way of handling this
              </li>
              <li>
                Some ontologies such as evoc are supplied as a bundle
                of separate ontologies. Unfortunately, we do not yet
                index these. We also do not index ontologies that are
                not available as a simple one-file download from a URL
              </li>
              <li>
                We endeavour to produce this on a weekly basis. System
                problems may prevent us from doing this. The
                production of these files is automated, unexpected
                problems may occur. We hope to make this a more
                reliable process in the future
              </li>
              <li>
                This resource is derived automatically from
                information submitted to the obo site
                maintainers. This site is not a substitute for any
                data or formats made available by the individual
                ontology providers. The purpose of this site is to
                make available as much of OBO as possible in a uniform
                fashion for a variety of formats. For any serious
                analysis, you should perform the file format
                conversion yourself using the ontology provided
                directly by the ontology provider. See main OBO site
                for obtaining this information.
              </li>
              <li>
                The MeSH file should be treated with caution. The
                source mesh.obo file treats links in MeSH as <a
                href="http://obo.sourceforge.net/relationship/#OBO_REL:is_a">is_a</a>
                relations, which is incorrect. This means that the
                resulting OWL file uses owl:subClassOf, which is also
                incorrect
              </li>
              <li>
                The NCBI Taxonomy transformation treats this resource
                as an ontology of organism types. The taxonomic rank
                (species, genus, order, family, phylum, etc)
                is treated as a term property_value. These are
                translated to annotationProperties in the owl
                version. See note below on term property_values with oboedit.
                <p>
                  Also note that due to the presence of {} characters
                  in the NCBI Taxonomy you will need oboedit1.1 or
                  oboedit1.100-beta12 or higher, due to a bug in previous versions.
                </p>
              </li>
              <li>
                In the Obo generated from OWL, owl
                AnnotationProperties are converted into oboedit class
                properties. You will need
                OBO-Edit 1.003-beta1 to be able to load the ontology
                without errors. A quick hack is also to simply run
                <pre>
                  grep -v ^property_value: in.obo > out.obo
                </pre>
                This will also lose property values for instances, but
                these are not currently displayed in oboedit
                anyway. (will be visible in a future version)
              </li>
              <li>
                This page is semantic-web friendly! Try viewing it with <a href="http://simile.mit.edu/wiki/Piggy_Bank">Piggy Bank <img src="http://simile.mit.edu/mediawiki/images/8/89/Piggybank_button.png" alt="picture of a piggy"/></a>
            </li>
            </ul>
            <h2>How this site works</h2>
            <p>
              This site is generated using the tools available in the
              obo sourceforge cvs, under <a
              href="http://obo.cvs.sourceforge.net/obo/obo/website/utils/">obo/website/utils</a>
            </p>

          </div>

        </div>
        <div style="display:none">
          <xsl:copy-of select="document('obo-all/ontology_index.rdf')" />
        </div>
        <!--
             TODO: figure out how to do this elegantly..
        -->
      </body>
    </html>
  </xsl:template>

  <xsl:template mode="copy-all" match="@*|node()">
    <xsl:copy><xsl:copy-of select="@*|node()"/><xsl:apply-templates mode="copy-all"/></xsl:copy>
  </xsl:template>


  <xsl:template match="ont">
    <a name="{@id}"/>
    <tr>
      <td class="tableOne">
        <xsl:value-of select="@id"/>
        <xsl:text>:</xsl:text>
        <a>
          <xsl:attribute name="onmouseover">
            <xsl:text>ddrivetip('</xsl:text>
            <xsl:text>Classes:</xsl:text>
            <xsl:value-of select="stats/@number_of_classes"/>
            <xsl:text>Class links:</xsl:text>
            <xsl:value-of select="stats/@number_of_relationships"/>
            <xsl:text>Status:</xsl:text>
            <xsl:value-of select="status"/>
            <xsl:text>','yellow', 300)</xsl:text>
          </xsl:attribute>
          <xsl:attribute name="onmouseout">
            <xsl:text>hideddrivetip()</xsl:text>
          </xsl:attribute>
          <xsl:attribute name="href">
            <xsl:text>http://obo.sourceforge.net/cgi-bin/detail.cgi?</xsl:text>
            <xsl:value-of select="@id"/>
          </xsl:attribute>
          <xsl:value-of select="title"/>
        </a>
      </td>

      <!-- namespace is really the db prefix -->
      <td class="tableOne">
        <xsl:value-of select="namespace"/>
      </td>

      <td class="tableOne">
        <xsl:if test="problem">
          <xsl:text>Could not index: </xsl:text>
          <xsl:value-of select="problem"/>
        </xsl:if>
        <xsl:for-each select="export">
          <xsl:if test="not(@problem) and number(@size)>0">
            <a class="tip">
              <xsl:attribute name="onmouseover">
                <xsl:text>ddrivetip('</xsl:text>
                <xsl:text>Build time:</xsl:text>
                <xsl:value-of select="@time_taken_to_generate"/>
                <xsl:text>s</xsl:text>
                <xsl:text> File size:</xsl:text>
                <xsl:value-of select="@size"/>
                <xsl:text> bytes</xsl:text>
                <xsl:text>','yellow', 300)</xsl:text>
              </xsl:attribute>
              <xsl:attribute name="onmouseout">
                <xsl:text>hideddrivetip()</xsl:text>
              </xsl:attribute>
              <xsl:if test="@format='owl'">
                <xsl:attribute name="rel">
                  <xsl:text>meta</xsl:text>
                </xsl:attribute>
                <xsl:attribute name="type">
                  <xsl:text>application/rdf+xml</xsl:text>
                </xsl:attribute>
              </xsl:if>
              <xsl:attribute name="tag">
                <xsl:text>ontology</xsl:text>
              </xsl:attribute>
              <xsl:attribute name="href">
                <xsl:value-of select="@path"/>
              </xsl:attribute>
              <xsl:value-of select="@format"/>
            </a>
            <xsl:text>[</xsl:text>
            <xsl:value-of select="floor(@size div 1024)"/>
            <xsl:text> kb]</xsl:text>
            <xsl:text> -- </xsl:text>
          </xsl:if>
        </xsl:for-each>
      </td>

    </tr>
  </xsl:template>

</xsl:stylesheet>

