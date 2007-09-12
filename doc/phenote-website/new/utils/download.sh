#!/bin/sh
cd /data/cluster/cjm/obo/website/cgi-bin && cvs update ontologies.txt mappings.txt;
cd /data/cluster/cjm/obo/website/utils &&\
 ./obo-downloader.pl --max-failures 8 --tar >& LOG &&\
 echo "XSLT" &&\
 xsltproc metadata-to-rdf.xsl obo-all/ontology_index.xml > obo-all/ontology_index.rdf &&\
 xsltproc metadata-to-download-html.xsl obo-all/ontology_index.xml > download.html &&\
 echo "stats html" &&\
 ./stats-to-tbl.pl obo-all/*/*.stats > stats.tbl &&\
 ./tbl2html --border 1 stats.tbl > stats.html &&\
 echo "archiving" &&\
 ./archive-file.pl --dir /data/public_ftp/pub/obo/archive obo-all/ontology_index.* stats.tbl obo-all-obo.tar.gz &&\
 mv download.html /users/cjm/public_html/obo-download/index.html &&\
 mv obo-all/ontology_index.* stats.* /users/cjm/public_html/obo-download/ &&\
 echo "copying targzs" &&\
 cp obo-all*tar.gz /data/public_ftp/pub/obo &&\
 cd /data/public_ftp/pub/obo &&\
 echo "untarring" &&\
 tar -zxvf obo-all.tar.gz &&\
 mail -s obo-download cjm@fruitfly.org < /data/cluster/cjm/obo/website/utils/LOG &&\
 echo "DONE!"
