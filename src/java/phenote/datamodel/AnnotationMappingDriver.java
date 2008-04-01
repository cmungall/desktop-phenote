package phenote.datamodel;

import java.util.List;

import org.obo.annotation.datamodel.Annotation;
import org.obo.datamodel.OBOClass;
import org.obo.datamodel.OBOProperty;
import org.obo.history.HistoryItem;

public interface AnnotationMappingDriver {
	public OBOProperty getPropertyForField(CharField cf);

	public CharFieldValue getCharFieldValue(OBOClass oboclass,
			CharacterI character, CharField field);

	public CharFieldValue getCharFieldValue(String s, CharacterI character,
			CharField field);
	
	public void setPropertyValue(Annotation annotation, OBOProperty property,
			OBOClass value);
	
	public void setPropertyValue(Annotation annotation, OBOProperty property,
			String value);

	public void clearProperty(Annotation annotation, OBOProperty property);
	
	public OBOProperty getDefaultRelationship();
	
	public boolean isSubjectField(CharField cf);

  // replace with isObjectGenusField?
  public boolean isObjectField(CharField cf);
  public boolean isObjectGenusField(CharField cf);
  public boolean isObjectDifferentiaField(CharField cf);
	
	public boolean getAuditHistoryMode();
	
	public boolean getChangeObjectsMode();
	
	public List<HistoryItem> popHistoryList();
	
	public void addHistoryItem(HistoryItem item);
}
