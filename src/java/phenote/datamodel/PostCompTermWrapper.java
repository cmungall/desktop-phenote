package phenote.datamodel;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.obo.datamodel.OBOClass;
import org.obo.datamodel.OBOProperty;
import org.obo.datamodel.OBORestriction;
import org.obo.datamodel.impl.OBOClassImpl;
import org.obo.datamodel.impl.OBORestrictionImpl;


/** changing this from static to non-static so can build post comp object from
    multiple rel-diffs */

public class PostCompTermWrapper {

  private static final Logger LOG = Logger.getLogger(PostCompTermWrapper.class);
  //private OBOClass postCompTerm;
  private boolean hasRelAndDiff=false;
  private OBOClass genus;
  private ArrayList<OBORestrictionImpl> diffs = 
	  new ArrayList<OBORestrictionImpl>();



  public void addGenus(OBOClass genus) {
  	if (genus==null) {
  	  LOG.error("Genus is null, cant add to postcomp");
  	  return;
  	}
  	LOG.info("setting genus: "+genus);
  	this.genus = genus;
   }

  public void addRelDiff(OBOProperty rel,OBOClass diff) {
    if (genus==null){
      LOG.error("cant add rel diff, post comp genus term is null");
      return;
    }
    OBORestrictionImpl dRel = new OBORestrictionImpl(null,rel,diff);
    dRel.setCompletes(true); // post comp
    diffs.add(dRel);
    hasRelAndDiff = true;
  }

  public boolean hasRelAndDiff() { return hasRelAndDiff; }

  public OBOClass getPostCompTerm() { 
	  String id = genus.getID();
	  String name = genus.getName();
	  for (OBORestriction diff : diffs) {
		  OBOProperty rel = diff.getType();
		  id += OboUtil.relDiffString(rel.getID(),diff.getParent().getID());
		  name += OboUtil.relDiffString(rel.getName(),diff.getParent().getName());
	  }
	  OBOClass postCompTerm = new OBOClassImpl(name,id);
	  for (OBORestriction diff : diffs) {
	  	diff.setChild(postCompTerm);
	  }
	  OBORestrictionImpl gRel = 
		  new OBORestrictionImpl(postCompTerm,OBOProperty.IS_A,genus);
	  gRel.setCompletes(true); // post comp flag
	  postCompTerm.addParent(gRel);
	  
	  for (OBORestriction diff : diffs) {
		  postCompTerm.addParent(diff);		  
	  }
	  return postCompTerm; 
  }

}
