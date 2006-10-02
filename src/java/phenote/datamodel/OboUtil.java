package phenote.datamodel;

import org.geneontology.oboedit.datamodel.OBOClass;
import org.geneontology.oboedit.datamodel.OBOProperty;
import org.geneontology.oboedit.datamodel.OBORestriction;
import org.geneontology.oboedit.datamodel.impl.OBOClassImpl;
import org.geneontology.oboedit.datamodel.impl.OBOPropertyImpl;
import org.geneontology.oboedit.datamodel.impl.OBORestrictionImpl;


// or should this go in datamodel?

public class OboUtil {

  public static OBOClass makePostCompTerm(OBOClass genus, OBOProperty rel,
                                          OBOClass diff) {
    String nm = pcString(genus.getName(),rel.getName(),diff.getName());
    String id = pcString(genus.getID(),rel.getName(),diff.getID());
    OBOClass postComp = new OBOClassImpl(nm,id);
    OBOProperty ISA = OBOProperty.IS_A;
    OBORestrictionImpl gRel = new OBORestrictionImpl(postComp,ISA,genus);
    gRel.setCompletes(true); // post comp flag
    postComp.addParent(gRel);
    OBORestrictionImpl dRel = new OBORestrictionImpl(postComp,rel,diff);
    dRel.setCompletes(true); // post comp
    postComp.addParent(dRel);
    return postComp;
  }
  private static String pcString(String g, String r, String d) {
    // for now hard wire to part_of
    return g+"^"+r+"("+d+")";
  }


}
