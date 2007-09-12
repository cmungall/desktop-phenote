PELLET = java  -mx1024m -jar $(HOME)/src/pellet/lib/pellet.jar

SOURCE_DIR = ../../../ontology/OBO_REL

all: index.html ro.owl ro.pro ro.obo ro_bfo_bridge.obo ro_bfo_bridge.owl

index.html: ro.html
	cp $< $@

clean:
	rm ro.obo-xml ro.obo index.html ro.html ro_bfo_bridge.obo ro_bfo_bridge.owl

%.obo: $(SOURCE_DIR)/%.obo
	cp $< $@

%.obo-xml: $(SOURCE_DIR)/%.obo relationship.xsl
	go2obo_xml $< > $@.tmp && mv $@.tmp $@
.PRECIOUS: %.obo-xml

%.owl: %.obo-xml
	go2owl $< > $@.tmp && mv $@.tmp $@
.PRECIOUS: %.owl

%.pellet: %.owl
	$(PELLET) -if $< > $@.tmp && mv $@.tmp $@


%.pro: %.obo-xml
	go2prolog $< > $@.tmp && mv $@.tmp $@

%.html: %.obo-xml
	xsltproc ./relationship.xsl $< > $@.tmp && mv $@.tmp $@

#SF_USER = cmungall
#WWW_DIR = $(SF_USER)\@shell.sf.net:/home/groups/o/ob/obo/htdocs/relationship/

update_site: all
	scp index.html ro.* ro_bfo_bridge.* $(WWW_DIR)
