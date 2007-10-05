package phenote.datamodel;

import org.apache.log4j.Logger;

import org.geneontology.util.ObjectUtil;
import org.geneontology.oboedit.annotation.datamodel.Annotation;
import org.geneontology.oboedit.annotation.datamodel.impl.AnnotationImpl;
import org.geneontology.oboedit.datamodel.LinkedObject;
import org.geneontology.oboedit.datamodel.OBOClass;
import org.geneontology.oboedit.datamodel.OBOProperty;
import org.geneontology.oboedit.datamodel.impl.DanglingClassImpl;
import org.geneontology.oboedit.util.TermUtil;

public class AnnotationCharacter extends AbstractCharacter {

  private static final Logger LOG = Logger.getLogger(AnnotationCharacter.class);

	protected static int idgen = 0;

  protected Annotation annotation;
  protected AnnotationMappingDriver driver;
  private CharFieldValue objectGenus;
  private CharFieldValue objectDifferentia;

  public AnnotationCharacter(AnnotationMappingDriver driver) {
    this("__temp__:" + idgen++, driver);
    annotation.setIsAnonymous(true);
  }

  public AnnotationCharacter(String id, AnnotationMappingDriver driver) {
    this(new AnnotationImpl(id), driver);
  }
  
  public AnnotationCharacter(Annotation annotation, AnnotationMappingDriver driver) {
    this.annotation = annotation;
    this.driver = driver;
  }

  /*
   * Extend this method to provide more field mappings. This should be all you
   * need to do for simple mappings
   */
  protected OBOProperty getPropertyForField(CharField cf) {
    return driver.getPropertyForField(cf);
  }


  protected void setPropertyValue(Annotation annotation,
                                  OBOProperty property, OBOClass value) {
    driver.setPropertyValue(annotation, property, value);
  }
  
  protected void setPropertyValue(Annotation annotation,
                                  OBOProperty property, String value) {
    driver.setPropertyValue(annotation, property, value);
  }

  protected OBOProperty getDefaultRelationship() {
    return driver.getDefaultRelationship();
  }

  protected boolean isSubjectField(CharField cf) {
    return driver.isSubjectField(cf);
  }
  
  protected boolean isObjectField(CharField cf) {
    return driver.isObjectField(cf);
  }

  private boolean isObjectGenusField(CharField cf) {
    return driver.isObjectGenusField(cf);
  }

  private boolean isObjectDifferentiaField(CharField cf) {
    return driver.isObjectDifferentiaField(cf);
  }

  protected void resetRelationshipField() {
    if (!ObjectUtil.equals(annotation.getRelationship(),getDefaultRelationship())) {
      if (driver.getAuditHistoryMode()) {
        driver.addHistoryItem(annotation
                              .getRelationshipChangeItem(getDefaultRelationship()));
      }
      if (driver.getChangeObjectsMode())
        annotation.setRelationship(getDefaultRelationship());
    }
  }
  

  public void setValue(CharField cf, CharFieldValue cfv) {
    // SUBJECT - if not a term(eg genotype), make a dangler??
    // Annotation only takes LinkedObjects not strings for subjects
    if (isSubjectField(cf)) {  //&& cfv.isTerm()) {
      OBOClass term = cfv.toTerm(); // if not term, makes dangler
      if (driver.getAuditHistoryMode())
        driver.addHistoryItem(annotation.getSubjectChangeItem(term));
      if (driver.getChangeObjectsMode())
        annotation.setSubject(term);
      resetRelationshipField();
    }
    // OBJECT GENUS -- should allow non terms to become danglers like above
    else if (isObjectGenusField(cf)) { // && cfv.isTerm()) {
      objectGenus = cfv;
      setObject();
//       if (driver.getAuditHistoryMode()) {
//         driver.addHistoryItem(annotation.getObjectChangeItem(cfv
//                                                              .getOboClass()));
//       }
//       if (driver.getChangeObjectsMode())
//         annotation.setObject(cfv.getOboClass());
//       resetRelationshipField();
    }
    // OBJECT DIFFERENTIA
    else if (isObjectDifferentiaField(cf)) {
      objectDifferentia = cfv;
      setObject();
    }
    // PROPERTIES (not subject or object)
    else {
      LOG.debug("setting charfield "+cf+" to val +"+cfv);
      OBOProperty prop = getPropertyForField(cf);
      if (prop != null) {
        if (cfv.isTerm()) {
          setPropertyValue(annotation, prop, cfv.getOboClass());
        } else {
          setPropertyValue(annotation, prop, cfv.getName());
        }
      }
    }
  }
  
  /** If have genus then set object, if just diff then do nothing - wait for genus */
  private void setObject() {
    // NO GENUS - return, wait for diff
    if (!hasObjectGenus()) return; // cant do object without genus

    // NO DIFF just set genus then
    if (!hasObjectDiff()) {
      setAnnotObject(objectGenus.toTerm());
    }

    // GENUS & DIFF - make post comp
    else {
      OBOProperty rel = driver.getPropertyForField(objectDifferentia.getCharField());
      OBOClass pc =
        OboUtil.makePostCompTerm(objectGenus.toTerm(),rel,objectDifferentia.toTerm());
      setAnnotObject(pc);
    }
  }

  private boolean hasObjectGenus() { return objectGenus!=null; }
  private boolean hasObjectDiff() { return objectDifferentia!=null; }


  private void setAnnotObject(OBOClass term) {
    if (driver.getAuditHistoryMode())
      driver.addHistoryItem(annotation.getObjectChangeItem(term));
    
    if (driver.getChangeObjectsMode())
      annotation.setObject(term);
    resetRelationshipField();
  }
  


  public CharFieldValue getValue(CharField cf) {
    CharFieldValue cfv = null;
    if (isSubjectField(cf)) {
      LinkedObject subject = annotation.getSubject();
      // For char fields that are free text, in obo as dangling class - just rip out
      // name
      if (subject!= null && !cf.isTerm())
        return new CharFieldValue(subject.getName(),this,cf);
      if (subject instanceof OBOClass) {
        cfv = new CharFieldValue((OBOClass) subject, this, cf);
      }
    }
    else if (isObjectGenusField(cf)) {
      cfv = objectGenus;
      // cold also see if diff or not and get from annot, below is if no diff
//       LinkedObject link = annotation.getObject();
//       if (link != null && TermUtil.isClass(link)) {
//         OBOClass postComp = TermUtil.getClass(link);
//         OBOClass genus = OboUtil.getGenusTerm(postComp); // if no pc just term
//         cfv = new CharFieldValue(genus,this,cf);
//       }
    }
    else if (isObjectDifferentiaField(cf)) {
      cfv = objectDifferentia;
//       LinkedObject link = annotation.getObject();
//       if (link != null && TermUtil.isClass(link)) {
//         OBOClass postComp = TermUtil.getClass(link);

//         OBOProperty rel = getPropertyForField(cf);

//         //OBOClass diff = OboUtil.getDifferentiaTerm(postComp);
//         // assumes only one diff
//         OBOClass diff = OboUtil.getDifferentiaTerm(postComp,rel);
//         if (diff != null)
//           cfv = new CharFieldValue(diff,this,cf);
//       }
    }
    else {
      OBOProperty prop = getPropertyForField(cf);
      LOG.debug("AC.getVal got prop "+prop+" for field "+cf);
      if (prop != null) {
        Object o = TermUtil.getPropValue(annotation, prop);
        LOG.debug("AC.getVal2 got propval "+o+" for field "+cf);
        
        if (o instanceof OBOClass) {
          cfv = getCharFieldValue((OBOClass) o, this, cf);
        } else if (o instanceof String) {
          cfv = getCharFieldValue((String) o, this, cf);
        }
      }
    }
    if (cfv == null)
      cfv = CharFieldValue.emptyValue(this, cf);
    return cfv;
  }

	protected CharFieldValue getCharFieldValue(OBOClass oboClass,
			CharacterI character, CharField field) {
		return driver.getCharFieldValue(oboClass, character, field);
	}

	protected CharFieldValue getCharFieldValue(String s, CharacterI character,
			CharField field) {
		return driver.getCharFieldValue(s, character, field);
	}

  public CharacterI cloneCharacter() {
    Annotation clone = (Annotation) annotation.clone();
    AnnotationCharacter a =  new AnnotationCharacter(clone, driver);
    a.objectGenus = objectGenus.cloneCharFieldValue();
    a.objectDifferentia = objectDifferentia.cloneCharFieldValue();
    return a;
  }
}
