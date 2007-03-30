package phenote.datamodel;

import org.geneontology.oboedit.datamodel.OBOClass;
import org.geneontology.oboedit.datamodel.OBOProperty;
import org.geneontology.oboedit.datamodel.OBORestriction;
import org.geneontology.oboedit.datamodel.impl.OBOClassImpl;
import org.geneontology.oboedit.datamodel.impl.OBOPropertyImpl;
import org.geneontology.oboedit.datamodel.impl.OBORestrictionImpl;


/** changing this from static to non-static so can build post comp object from
    multiple rel-diffs */

public class OboUtil {

  private OBOClass postCompTerm;
  private String id;
  private String name;
  private boolean hasRelAndDiff=false;


  /** used by OntologyManager */
  public static OBOClass makePostCompTerm(OBOClass genus, OBOProperty rel,
                                          OBOClass diff) {
    String nm = pcString(genus.getName(),rel.getName(),diff.getName());
    String id = pcString(genus.getID(),rel.getName(),diff.getID());
    OBOClass postCompTerm = new OBOClassImpl(nm,id);
    OBOProperty ISA = OBOProperty.IS_A;
    OBORestrictionImpl gRel = new OBORestrictionImpl(postCompTerm,ISA,genus);
    gRel.setCompletes(true); // post comp flag
    postCompTerm.addParent(gRel);
    OBORestrictionImpl dRel = new OBORestrictionImpl(postCompTerm,rel,diff);
    dRel.setCompletes(true); // post comp
    postCompTerm.addParent(dRel);
    return postCompTerm;
  }

  public static OboUtil initPostCompTerm(OBOClass genus) {
    OboUtil ou = new OboUtil();
    ou.addGenus(genus);
    return ou;
  }

  private void addGenus(OBOClass genus) {
    id = genus.getID();
    name = genus.getName();
    postCompTerm = new OBOClassImpl(name,id);
    OBOProperty ISA = OBOProperty.IS_A;
    OBORestrictionImpl gRel = new OBORestrictionImpl(postCompTerm,ISA,genus);
    gRel.setCompletes(true); // post comp flag
    postCompTerm.addParent(gRel);
  }

  public void addRelDiff(OBOProperty rel,OBOClass diff) {
    OBORestrictionImpl dRel = new OBORestrictionImpl(postCompTerm,rel,diff);
    dRel.setCompletes(true); // post comp
    postCompTerm.addParent(dRel);
    name += relDiffString(rel.getName(),diff.getName());
    postCompTerm.setName(name);
    id += relDiffString(rel.getName(),diff.getID());
    // just for now
    ((OBOClassImpl)postCompTerm).setID(id);
    hasRelAndDiff = true;
  }

  public boolean hasRelAndDiff() { return hasRelAndDiff; }

  public OBOClass getPostCompTerm() { return postCompTerm; }

  private static String pcString(String g, String r, String d) {
    // for now hard wire to part_of
    return g + relDiffString(r,d);
  }

  private static String relDiffString(String r, String d) {
    return "^"+r+"("+d+")";
  }

}
