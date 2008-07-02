package org.phenoscape.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.obo.datamodel.Namespace;
import org.obo.datamodel.OBOClass;
import org.obo.datamodel.OBOSession;
import org.obo.query.QueryEngine;
import org.obo.query.impl.CategoryQuery;
import org.obo.query.impl.NamespaceQuery;

/**
 * A TermSet is used to define a collection of ontology terms.  Currently the collection 
 * can be defined using both OBO namespaces and categories (aka "slims" or "subsets").
 * @author Jim Balhoff
 */
public class TermSet {

  private OBOSession session;
  private Collection<Namespace> namespaces = new ArrayList<Namespace>();
  private Collection<String> categories = new ArrayList<String>();

  public OBOSession getOBOSession() {
    return this.session;
  }
  
  public void setOBOSession(OBOSession oboSession) {
    this.session = oboSession;
  }
  
  public Collection<Namespace> getNamespaces() {
    return this.namespaces;
  }

  public void setNamespaces(Collection<Namespace> namespaces) {
    this.namespaces = namespaces;
  }
  
  public Collection<String> getCategories() {
    return this.categories;
  }
  
  public void setCategories(Collection<String> categories) {
    this.categories = categories;
  }
  
  public Collection<OBOClass> getTerms() {
    final QueryEngine engine = new QueryEngine(this.getOBOSession());
    final List<String> namespaceIDs = new ArrayList<String>();
    for (Namespace ns : this.getNamespaces()) { namespaceIDs.add(ns.getID()); }
    final NamespaceQuery query = new NamespaceQuery(namespaceIDs);
    final Collection<OBOClass> termsInNamespaces = engine.query(query);
    if (this.hasAnyCategory()) {
      return engine.subquery(termsInNamespaces, new CategoryQuery(this.getCategories())).getResults();
    } else {
      return termsInNamespaces;
    }
  }
  
  private boolean hasAnyCategory() {
    return !this.getCategories().isEmpty();
  }
  
}
