package phenote.datamodel;

import java.awt.datatransfer.DataFlavor;
import java.util.List;

import org.apache.log4j.Logger;

import org.obo.annotation.datamodel.Annotation;
import org.obo.datamodel.OBOClass;
import org.obo.datamodel.OBOProperty;

public abstract class AbstractCharacter implements CharacterI {

  /** Returns CharFieldValue created */
  public CharFieldValue setValue(String fieldString, String valueString)
    throws CharFieldException, TermNotFoundException {
    CharField cf = getCharFieldForName(fieldString);
    return setValue(cf, valueString);
  }

  /** if term field, string should be id, obo class will be searched for, if class
      not found then dangler is created. if free text field just uses string of
      course. the dangler makes termNotFound Ex irrelevant - take out? or will there
      be a no dangler mode? probably not right?
      CharFieldEx thrown if improper date for date field */
  public CharFieldValue setValue(CharField cf, String s) throws CharFieldException {
    return setValue(cf,s,null); // null danglerName
  }

  /** if term field, string should be id, obo class will be searched for, if class
      not found then dangler is created. if free text field just uses string of
      course. the dangler makes termNotFound Ex irrelevant - take out? or will there
      be a no dangler mode? probably not right?
      CharFieldEx thrown if improper date for date field
      if cf is a term, can optionally pass in non null danglerName, if term ends up
      being dangler (not found in ontology) will set dangler id to s and dangler name
      to danglerName
  */
  public CharFieldValue setValue(CharField cf, String s,String danglerName)
    throws CharFieldException {
    CharFieldValue cfv = cf.makeValue(this, s, danglerName);
    setValue(cf, cfv);
    return cfv;
  }


  public List<CharField> getAllCharFields() {
    return CharFieldManager.inst().getCharFieldList();
  }
  
  public CharField getCharFieldForName(String fieldName)
    throws CharFieldException {
    return CharFieldManager.inst().getCharFieldForName(fieldName);
  }

  /** Throws char field exception if char field is not a term, thus lists of terms
      throw an exception as they are not technically a single term */
  protected OBOClass getTerm(CharField cf) throws CharFieldException {
    if (cf.isList())
      throw new CharFieldException("Cant call getTerm on list for field "+cf.getName());
    if (!cf.isTerm())
      throw new CharFieldException("Cant call getTerm on Field "+cf.getName()+" not a term");
    if (!hasValue(cf))
      throw new CharFieldException("Char "+toString()+" has no value");//return null;
    return getValue(cf).getOboClass();
  }


	public OBOClass getTerm(String field) throws CharFieldException {
		CharField cf = getCharFieldForName(field);
		return getTerm(cf);
	}

  protected void setValue(CharField cf, OBOClass term) {
    setValue(new CharFieldValue(term, this, cf));
  }

	public String getValueString(CharField cf) {
		CharFieldValue cfv = getValue(cf);
		if (cfv.getName() == null)
			return ""; // ?? ex?
		return cfv.getName();
	}

	public String getValueString(String field) throws CharFieldException {
		CharField cf = getCharFieldForName(field); // throws ex
		if (!hasValue(cf))
			return null; // ?? exception? ""?
		return getValue(cf).getName();
	}


	public boolean hasNoContent() {
		for (CharField cf : getAllCharFields()) {
			if (hasValue(cf))
				return false;
		}
		return true;
	}

	public boolean hasValue(CharField cf) {
		if (getValue(cf) == null)
			return false;
		return !getValue(cf).isEmpty();
	}

	public boolean hasValue(String fieldName) {
		try {
			return hasValue(getCharFieldForName(fieldName));
		} catch (CharFieldException e) { // throws exception if doesnt have
			return false;
		}
	}
  
  protected void setValue(CharFieldValue cfv) {
    setValue(cfv.getCharField(), cfv);
  }

  // this is just used for testing at this point
  public boolean equals(CharacterI ch) {
    for (CharField cf : getAllCharFields()) {
      if (!eq(getValue(cf), ch.getValue(cf)))
        return false;
    }
    return true;
  }

  /** by default check if single char field val from field is equal,
      eventually should get hip to potential lists of values */
  public boolean fieldEquals(CharacterI c, CharField cf) {
    CharFieldValue v1 = getValue(cf);
    CharFieldValue v2 = c.getValue(cf);
    if (v1==null && v2==null) return true;
    if (v1==null || v2==null) return false;
    return v1.equals(v2);
  }

  /** check if both are null in addition to .equals() */
  protected boolean eq(CharFieldValue c1, CharFieldValue c2) {
    if (c1 == null && c2 == null)
      return true;
    if (c2 == null)
      return false;
    return c1.equals(c2);
  }

  /** Whether or not datamodel has obo annotations, returns getOboAnnotation !=
      null, which is only true for AnnotationCharacter */
  public boolean hasOboAnnotation() { return getOboAnnotation() != null; }

  /** Return null by default - AnnotationCharacter overrides */
  public Annotation getOboAnnotation() { return null; }
  
  /** throws CharacterEx if not implemented (Character doesnt implement)
      OBOAnnotation implements */
  //public abstractvoid addComparison(OBOProperty r,CharacterI c)throwsCharacterEx;

  public boolean hasAnnotId() { return getAnnotId() != null; }
  public String getAnnotId() {
    if (!CharFieldManager.inst().hasAutoAnnotField()) return null;
    CharField idField = CharFieldManager.inst().getAutoAnnotField();
    return getValueString(idField); // returns null if empty
  }

  //  TRANSFERABLE INTERFACE for drag & drop 

  /** Returns an object which represents the data to be transferred.*/
  public Object getTransferData(DataFlavor flavor) {
    return this;
  }
    
  /** Returns an array of DataFlavor objects indicating the flavors the data
      can be provided in. */
  public DataFlavor[] getTransferDataFlavors() {
    return new DataFlavor[] { CHAR_FLAVOR };
  }
  
  /** Returns whether or not the specified data flavor is supported for this object*/
  public boolean isDataFlavorSupported(DataFlavor flavor) {
    return flavor == CHAR_FLAVOR;
  }

  /**
   * According to the CharacterI documentation, the methods below are garbage
   They are here for backward compatibility - still used in nexus & phenoxml adapter
   but they should be upgraded to new generic way at some point
   need to keep in for now
   */
  
  public boolean hasPub() {
    return getPub() != null && !getPub().equals("");
  }


	public void setEntity(OBOClass e) {
		try {
			setValue(new CharFieldValue(e, this, getEntityField()));
		} catch (CharFieldException x) {
			throw new RuntimeException(x);
		}
	}


  public boolean hasGeneticContext() {
    return getGeneticContext() != null && !getGeneticContext().equals("");
  }
  
  public void setGeneticContext(OBOClass gc) {
    try {
      setValue(getGenConField(), gc);
    } catch (CharFieldException e) {
      throw new RuntimeException(e);
    }
  }

  public OBOClass getEntity() {
    try {
      return getValue(getEntityField()).getTerm();
    } catch (CharFieldException e) {
      return null;
    } // ??
  }

	protected CharField getEntityField() throws CharFieldException {
		return getCharFieldForName(CharFieldEnum.ENTITY.getName());
	}

	protected CharField getGenConField() throws CharFieldException {
		return getCharFieldForName(CharFieldEnum.GENETIC_CONTEXT.getName());
	}

  protected CharField getPubField() throws CharFieldException {
    return getCharFieldForName(CharFieldEnum.PUB.getName());
  }

	protected CharField getQualField() throws CharFieldException {
		return getCharFieldForName(CharFieldEnum.QUALITY.getName());
	}

	public OBOClass getGeneticContext() {
		try {
			return getTerm(getGenConField());
		} catch (CharFieldException e) {
			return null;
		} // ??
	}


  // still used by phenoxml - phase out
  public void setGenotype(String gt) {
    try {
      setValue(getGenotypeField(), gt);
    } catch (CharFieldException x) {
      //throw new RuntimeException(x); // runtime??
      log().error("Failed to set genotype "+x);
    } 
  }

  public String getGenotype() {
    try {
      return getValueString(getGenotypeField());
    } catch (CharFieldException e) {
      return null;
    } // ??
  }

  protected CharField getGenotypeField() throws CharFieldException {
    return getCharFieldForName(CharFieldEnum.GENOTYPE.getName());
  }

  public String getPub() {
    try {
      return getValueString(getPubField());
    } catch (CharFieldException e) {
      return null;
    } // ?? ""?
  }

	public void setPub(String p) {
		try {
			setValue(new CharFieldValue(p, this, getPubField()));
		} catch (CharFieldException x) {
			throw new RuntimeException(x);
		}
	}
	public OBOClass getQuality() {
		// return quality;
		try {
			return getValue(getQualField()).getTerm();
		} catch (CharFieldException e) {
			return null;
		} // ??
	}


	public void setQuality(OBOClass q) {
		try {
			setValue(new CharFieldValue(q, this, getQualField()));
		} catch (CharFieldException e) {
			throw new RuntimeException(e);
		}
	}

  private Logger log;
  private Logger log() {
    if (log == null) log = Logger.getLogger(getClass());
    return log;
  }
}
