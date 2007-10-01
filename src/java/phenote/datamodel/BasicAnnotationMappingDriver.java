package phenote.datamodel;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.geneontology.oboedit.annotation.datamodel.Annotation;
import org.geneontology.oboedit.annotation.datamodel.AnnotationOntology;
import org.geneontology.oboedit.datamodel.Datatype;
import org.geneontology.oboedit.datamodel.IdentifiedObject;
import org.geneontology.oboedit.datamodel.OBOClass;
import org.geneontology.oboedit.datamodel.OBOProperty;
import org.geneontology.oboedit.datamodel.Value;
import org.geneontology.oboedit.datamodel.impl.DatatypeValueImpl;
import org.geneontology.oboedit.datamodel.impl.OBOPropertyImpl;
import org.geneontology.oboedit.history.AddPropertyValueHistoryItem;
import org.geneontology.oboedit.history.DeletePropertyValueHistoryItem;
import org.geneontology.oboedit.history.HistoryItem;

/** Maps ENTITY to subject and QUALITY to object 
 but is this proper? shouldnt genotype be subject? */

public class BasicAnnotationMappingDriver implements AnnotationMappingDriver {

	protected boolean auditHistoryMode = false;
	protected boolean changeObjectsMode = true;
	protected List<HistoryItem> historyItems;

  /**
   * We probably want to create a special static ontology that extends
   * AnnotationOntology to contain these properties. This is here just to keep
   * the example simple
   */
  protected static final OBOProperty GENOTYPE_REL = new OBOPropertyImpl(
    "oban:has_genotype", "has_genotype");
  protected static final OBOProperty HAS_QUALITY_REL = new OBOPropertyImpl(
    "pato:has_quality", "has_quality");
  
  public OBOProperty getPropertyForField(CharField cf) {
    // First check if configged/in char field
    // if (cf.hasOboRelation()) return cf.getOboRelation();
    if (CharFieldEnum.PUB.equals(cf)) {
      return AnnotationOntology.EVIDENCE_REL();
    } else if (CharFieldEnum.GENOTYPE.equals(cf)) {
      return GENOTYPE_REL;
    }
    //return null;
    // since not configged nor in java/mapper create one on fly - better
    // than returning null which causes failure - should pring error 
    // message though(?) - info, error might be annoying
    // LOG.info("No obo relationship found for "+cf);
    return new OBOPropertyImpl("PHENOTE_MAPPING_REL:"+cf.getTag(),cf.getTag());
  }

  /** retrieves prop from obo session by id, if not there then creates one */
  protected OBOProperty getRelation(String id, String name) {
    OBOProperty p = OntologyManager.inst().getRelation(id);
    if (p != null) return p;
    return new OBOPropertyImpl(id,name);
  }


	public List<HistoryItem> popHistoryList() {
		List<HistoryItem> out = historyItems;
		historyItems = new LinkedList<HistoryItem>();
		return out;
	}
	
  public CharFieldValue getCharFieldValue(OBOClass oboclass,
                                          CharacterI character, CharField field) {
    return new CharFieldValue(oboclass, character, field);
  }
  
	public CharFieldValue getCharFieldValue(String s, CharacterI character,
			CharField field) {
		return new CharFieldValue(s, character, field);
	}

  public void setPropertyValue(Annotation annotation, OBOProperty property,
                               OBOClass value) {
    clearProperty(annotation, property);
    if (changeObjectsMode)
      annotation.addPropertyValue(property, value);
    if (auditHistoryMode) {
      historyItems.add(new AddPropertyValueHistoryItem(
                         annotation.getID(), property.getID(), value.getType()
                         .getID(), value.getID()));
    }
  }

	public void setPropertyValue(Annotation annotation, OBOProperty property,
			String value) {
		clearProperty(annotation, property);
		if (changeObjectsMode)
			annotation.addPropertyValue(property, new DatatypeValueImpl(
					Datatype.STRING, value));
		if (auditHistoryMode) {
			historyItems.add(new AddPropertyValueHistoryItem(
					annotation.getID(), property.getID(), Datatype.STRING
							.getID(), value));
		}
	}

	public void clearProperty(Annotation annotation, OBOProperty property) {
		Collection<Value> values = new LinkedList<Value>(annotation
				.getValues(property));
		for (Value v : values) {
			if (auditHistoryMode) {
				String stringVal = null;
				if (v.getType() instanceof Datatype) {
					stringVal = ((Datatype) v.getType()).getString(v);
				} else if (v instanceof IdentifiedObject) {
					stringVal = ((IdentifiedObject) v).getID();
				}
				if (stringVal != null)
					historyItems.add(new DeletePropertyValueHistoryItem(
							annotation.getID(), property.getID(), v.getType()
									.getID(), stringVal));
			}
			if (changeObjectsMode)
				annotation.removePropertyValue(property, v);
		}
	}

  public OBOProperty getDefaultRelationship() {
    return HAS_QUALITY_REL;
  }

  public boolean isSubjectField(CharField cf) {
    return CharFieldEnum.ENTITY.equals(cf);
  }

  // phase out? for isObjectGenusField?
  public boolean isObjectField(CharField cf) {
    return isObjectGenusField(cf);//CharFieldEnum.QUALITY.getName().equals(cf);
  }
  public boolean isObjectGenusField(CharField cf) {
    return CharFieldEnum.QUALITY.equals(cf);
  }

  public boolean isObjectDifferentiaField(CharField cf) {
    return false; // basic - no object differentia
  }

	public boolean getAuditHistoryMode() {
		return auditHistoryMode;
	}

	public void setAuditHistoryMode(boolean auditHistoryMode) {
		this.auditHistoryMode = auditHistoryMode;
		if (auditHistoryMode && historyItems == null)
			historyItems = new LinkedList<HistoryItem>();
		else if (!auditHistoryMode)
			historyItems = null;
	}

	public boolean getChangeObjectsMode() {
		return changeObjectsMode;
	}

	public void setChangeObjectsMode(boolean changeObjectsMode) {
		this.changeObjectsMode = changeObjectsMode;
	}

	public void addHistoryItem(HistoryItem item) {
		historyItems.add(item);		
	}
}
