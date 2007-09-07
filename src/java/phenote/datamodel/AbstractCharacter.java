package phenote.datamodel;

import java.util.List;

import org.geneontology.oboedit.datamodel.OBOClass;

public abstract class AbstractCharacter implements CharacterI {

  /** Returns CharFieldValue created */
  public CharFieldValue setValue(String fieldString, String valueString)
    throws CharFieldException, TermNotFoundException {
    CharField cf = getCharFieldForName(fieldString);
    return setValue(cf, valueString);
  }

  /** if term field, string should be id, obo class will be searched for, if class
      not found then dangler is created. if free text field just uses string of
      course. the dangler makes termNotFoundEx irrelevant - take out? or will there
      be a no dangler mode? probably not right? */
  public CharFieldValue setValue(CharField cf, String s) throws TermNotFoundException {
    CharFieldValue cfv = cf.makeValue(this, s);
    setValue(cf, cfv);
    return cfv;
  }


  public List<CharField> getAllCharFields() {
    return OntologyManager.inst().getCharFieldList();
  }
  
  public CharField getCharFieldForName(String fieldName)
    throws CharFieldException {
    return OntologyManager.inst().getCharFieldForName(fieldName);
  }

  protected OBOClass getTerm(CharField cf) {
    if (!hasValue(cf))
      return null; // ?? exception?
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

  /** check if both are null in addition to .equals() */
  protected boolean eq(CharFieldValue c1, CharFieldValue c2) {
    if (c1 == null && c2 == null)
      return true;
    if (c2 == null)
      return false;
    return c1.equals(c2);
  }

  /**
   * According to the CharacterI documentation, the methods below are garbage
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


	public void setGenotype(String gt) {
		try {
			setValue(getGenotypeField(), gt);
		} catch (CharFieldException x) {
			throw new RuntimeException(x);
		} catch (TermNotFoundException e) {
		} // doesnt happen for free text field
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

}
