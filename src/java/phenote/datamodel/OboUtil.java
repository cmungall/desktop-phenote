package phenote.datamodel;

import org.obo.datamodel.Link;
import org.obo.datamodel.LinkedObject;
import org.obo.datamodel.OBOClass;
import org.obo.datamodel.OBOProperty;
import org.obo.datamodel.OBORestriction;
import org.obo.datamodel.impl.OBOClassImpl;
import org.obo.datamodel.impl.OBORestrictionImpl;
import org.obo.util.TermUtil;


/** changing this from static to non-static so can build post comp object from
    multiple rel-diffs */

public class OboUtil {

  private OBOClass postCompTerm;
  private String id;
  private String name;
  private boolean hasRelAndDiff=false;


//   /** used by OntologyManager */
//   public static OBOClass makePostCompTerm(OBOClass genus, OBOProperty rel,
//                                           OBOClass diff) {
//     String nm = pcString(genus.getName(),rel.getName(),diff.getName());
//     String id = pcString(genus.getID(),rel.getID(),diff.getID());
//     OBOClass postCompTerm = new OBOClassImpl(nm,id);
//     OBOProperty ISA = OBOProperty.IS_A;
//     OBORestrictionImpl gRel = new OBORestrictionImpl(postCompTerm,ISA,genus);
//     gRel.setCompletes(true); // post comp flag
//     postCompTerm.addParent(gRel);
//     OBORestrictionImpl dRel = new OBORestrictionImpl(postCompTerm,rel,diff);
//     dRel.setCompletes(true); // post comp
//     postCompTerm.addParent(dRel);
//     return postCompTerm;
//   }

  public static OBOClass makePostCompTerm(OBOClass genus, OBOProperty rel,
                                          OBOClass diff) {
    OboUtil u = initPostCompTerm(genus);
    u.addRelDiff(rel,diff);
    return u.getPostCompTerm();
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
    id += relDiffString(rel.getID(),diff.getID());
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

  /** for non post comp returns term itself */
  public static OBOClass getGenusTerm(OBOClass term) {
    if (isPostCompTerm(term)) {
      for (Object o : term.getParents()) {
        OBORestriction r = (OBORestriction)o;
        if (r.completes() && r.getType().equals(OBOProperty.IS_A))
          return (OBOClass)r.getParent(); // check downcast?
      }
      // error msg?
    }
    return term;
  }

  public static boolean isPostCompTerm(OBOClass term) {
    for (Link l : term.getParents()) {
      if (isLinkToDiff(l))
        return true;
    }
    return false;
  }

  public static boolean isLinkToDiff(Link l) {
    if (!isOboRestriction(l)) return false;
    return isLinkToDiff(getOboRestriction(l));
  }

  public static boolean isLinkToDiff(OBORestriction r) {
    // i guess there is a case where isa is completes - but why?
    return r.completes() && !r.getType().equals(OBOProperty.IS_A);
  }

  public static boolean isOboRestriction(Link l) {
    return l instanceof OBORestriction;
  }

  public static OBORestriction getOboRestriction(Link l) {
    if (!isOboRestriction(l)) return null;
    return (OBORestriction)l;
  }

  /** Assumes theres only one term with diffRel, returns the 1st one it finds
      null if none found - ex? */
  public static OBOClass getDifferentiaTerm(OBOClass postComp,
                                            OBOProperty diffRel) {
    if (!isPostCompTerm(postComp)) return null; // ex?
    // loop thru parents looking for diffRel
    for (Link l : postComp.getParents()) {
      OBOProperty rel = l.getType();
      if (rel.equals(diffRel)) {
        LinkedObject lo = l.getParent();
        if (TermUtil.isClass(lo))
          return TermUtil.getClass(lo);
      }
    }
    return null; // diff not found - OboEx?
  }

}
