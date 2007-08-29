package phenote.datamodel;

import java.util.List;

import org.geneontology.oboedit.annotation.datamodel.Annotation;
import org.geneontology.oboedit.datamodel.HistoryItem;
import org.geneontology.oboedit.datamodel.OBOClass;
import org.geneontology.oboedit.datamodel.OBOProperty;

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

	public boolean isObjectField(CharField cf);
	
	public boolean getAuditHistoryMode();
	
	public boolean getChangeObjectsMode();
	
	public List<HistoryItem> popHistoryList();
	
	public void addHistoryItem(HistoryItem item);
}
