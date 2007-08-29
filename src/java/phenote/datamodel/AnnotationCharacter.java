package phenote.datamodel;

import org.geneontology.oboedit.annotation.datamodel.Annotation;
import org.geneontology.oboedit.annotation.datamodel.impl.AnnotationImpl;
import org.geneontology.oboedit.datamodel.LinkedObject;
import org.geneontology.oboedit.datamodel.OBOClass;
import org.geneontology.oboedit.datamodel.OBOProperty;
import org.geneontology.oboedit.datamodel.TermUtil;

public class AnnotationCharacter extends AbstractCharacter {

	protected static int idgen = 0;

	protected Annotation annotation;
	protected AnnotationMappingDriver driver;

	/*
	 * Extend this method to provide more field mappings. This should be all you
	 * need to do for simple mappings
	 */
	protected OBOProperty getPropertyForField(CharField cf) {
		return driver.getPropertyForField(cf);
	}

	protected CharFieldValue getCharFieldValue(OBOClass oboClass,
			CharacterI character, CharField field) {
		return driver.getCharFieldValue(oboClass, character, field);
	}

	protected CharFieldValue getCharFieldValue(String s, CharacterI character,
			CharField field) {
		return driver.getCharFieldValue(s, character, field);
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

	protected void resetRelationshipField() {
		if (!annotation.getRelationship().equals(getDefaultRelationship())) {
			if (driver.getAuditHistoryMode()) {
				driver.addHistoryItem(annotation
						.getRelationshipChangeItem(getDefaultRelationship()));
			}
			if (driver.getChangeObjectsMode())
				annotation.setRelationship(getDefaultRelationship());
		}
	}

	public void setValue(CharField cf, CharFieldValue cfv) {
		if (isSubjectField(cf) && cfv.isTerm()) {
			if (driver.getAuditHistoryMode()) {
				driver.addHistoryItem(annotation.getSubjectChangeItem(cfv
						.getOboClass()));
			}
			if (driver.getChangeObjectsMode())
				annotation.setSubject(cfv.getOboClass());
			resetRelationshipField();
		} else if (isObjectField(cf) && cfv.isTerm()) {
			if (driver.getAuditHistoryMode()) {
				driver.addHistoryItem(annotation.getObjectChangeItem(cfv
						.getOboClass()));
			}
			if (driver.getChangeObjectsMode())
				annotation.setObject(cfv.getOboClass());
			resetRelationshipField();
		} else {
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

	public CharFieldValue getValue(CharField cf) {
		CharFieldValue cfv = null;
		if (isSubjectField(cf)) {
			LinkedObject subject = annotation.getSubject();
			if (subject instanceof OBOClass) {
				cfv = new CharFieldValue((OBOClass) subject, this, cf);
			}
		} else if (isObjectField(cf)) {
			LinkedObject object = annotation.getObject();
			if (object instanceof OBOClass) {
				cfv = new CharFieldValue((OBOClass) object, this, cf);
			}
		} else {
			OBOProperty prop = getPropertyForField(cf);
			if (prop != null) {
				Object o = TermUtil.getPropValue(annotation, prop);
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

	public CharacterI cloneCharacter() {
		Annotation clone = (Annotation) annotation.clone();
		return new AnnotationCharacter(clone, driver);
	}
}
