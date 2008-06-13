package org.phenoscape.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.bbop.dataadapter.DataAdapterException;
import org.obo.dataadapter.OBOAdapter;
import org.obo.dataadapter.OBOFileAdapter;
import org.obo.dataadapter.OBOMetaData;
import org.obo.datamodel.Namespace;
import org.obo.datamodel.OBOSession;
import org.oboedit.controller.SessionManager;

public class OntologyController {

  private final OBOFileAdapter fileAdapter;
  private final OBOMetaData metadata;
  
  private static String TTO = "http://obo.cvs.sourceforge.net/*checkout*/obo/obo/ontology/taxonomy/teleost_taxonomy.obo";
  //TODO get proper path for COLLECTION
  private static String COLLECTION = "http://obo.cvs.sourceforge.net/*checkout*/obo/obo/ontology/taxonomy/teleost_taxonomy.obo";
  private static String TAO = "http://obo.cvs.sourceforge.net/*checkout*/obo/obo/ontology/anatomy/gross_anatomy/animal_gross_anatomy/fish/teleost_anatomy.obo";
  private static String PATO = "http://obo.cvs.sourceforge.net/*checkout*/obo/obo/ontology/phenotype/quality.obo";
  private static String SPATIAL = "http://obo.cvs.sourceforge.net/*checkout*/obo/obo/ontology/anatomy/caro/spatial.obo";
  private static String UNIT = "http://obo.cvs.sourceforge.net/*checkout*/obo/obo/ontology/phenotype/unit.obo";
  private static String REL = "http://obo.cvs.sourceforge.net/*checkout*/obo/obo/ontology/OBO_REL/ro.obo";
  private static String REL_PROPOSED = "http://obo.cvs.sourceforge.net/*checkout*/obo/obo/ontology/OBO_REL/ro_proposed.obo";
  private static String[] URLS = {TTO, COLLECTION, TAO, PATO, SPATIAL, UNIT, REL, REL_PROPOSED};

  public OntologyController() {
    this.fileAdapter = new OBOFileAdapter();
    OBOFileAdapter.OBOAdapterConfiguration config = new OBOFileAdapter.OBOAdapterConfiguration();
    config.setReadPaths(Arrays.asList(URLS));
    config.setBasicSave(false);
    config.setAllowDangling(true);
    try {
      SessionManager.getManager().setSession(this.fileAdapter.doOperation(OBOAdapter.READ_ONTOLOGY, config, null));
    } catch (DataAdapterException e) {
      log().fatal("Failed to load ontologies", e);
    }
    this.metadata = this.fileAdapter.getMetaData();
  }

  public OBOSession getOBOSession() {
    return SessionManager.getManager().getSession();
  }

  public TermSet getTaxonTermSet() {
    return this.getTermSet(TTO);
  }
  
  public TermSet getCollectionTermSet() {
    return this.getTermSet(COLLECTION);
  }

  public TermSet getEntityTermSet() {
    return this.getTermSet(TAO, SPATIAL, PATO);
  }

  public TermSet getQualityTermSet() {
    return this.getTermSet(PATO, SPATIAL);
  }

  public TermSet getRelatedEntityTermSet() {
    return this.getEntityTermSet();
  }

  public TermSet getUnitTermSet() {
    return this.getTermSet(UNIT);
  }
  
  public TermSet getRelationsTermSet() {
    return this.getTermSet(REL, REL_PROPOSED);
  }
  
  private TermSet getTermSet(String... urls) {
    final Collection<Namespace> namespaces = new ArrayList<Namespace>();
    for (String url : urls) {
      namespaces.addAll(this.metadata.getNamespaces(url));
    }
    final TermSet terms =  new TermSet();
    terms.setOBOSession(this.getOBOSession());
    terms.setNamespaces(namespaces);
    return terms;
  }
  
  private Logger log() {
    return Logger.getLogger(this.getClass());
  }

}
