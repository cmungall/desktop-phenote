package phenote.datamodel;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

import org.bbop.dataadapter.DataAdapterException;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.obo.annotation.datamodel.Annotation;
import org.obo.dataadapter.OBDSQLDatabaseAdapter;
import org.obo.dataadapter.OBOAdapter;
import org.obo.dataadapter.OBOFileAdapter;
import org.obo.dataadapter.OBDSQLDatabaseAdapter.OBDSQLDatabaseAdapterConfiguration;
import org.obo.datamodel.IdentifiedObject;
import org.obo.datamodel.Instance;
import org.obo.datamodel.OBOSession;
import org.obo.util.AnnotationUtil;

/**
 * This test is "disabled" at the moment because it has not been maintained 
 * and its subclass tests don't work.  Also, not sure if we want a test class hierarchy.  
 * Update -- made this non-abstract and into more of a util class (don't think abstract is 
 * a good way to go with tests).  These tests need attention.
 */
public class AnnotationModelTestUtil  {

	protected static String getConfigFileName() { return ""; }
	protected static String getDataFilePath() { return "";}
	private final OBOSession session;

	String jdbcPath = "jdbc:postgresql://localhost:5432/obdtest";
	
	/**
	 * This constructor keeps the Ant JUnit runner happy. This class 
	 * shouldn't run tests and is just used by other tests.
	 */
	public AnnotationModelTestUtil() {
	  this.session = null;
	}
	
	public AnnotationModelTestUtil(OBOSession session) {
	  this.session = session;
	}

	@Ignore @Test
	public void checkAnnotations()   {
		Collection<Annotation> annots = AnnotationUtil.getAnnotations(session);
		System.err.println("# annots = "+annots.size());
		for (Annotation annot : annots) {
			System.err.println("annot: "+annot+":: "+annot.getSubject()+" -"+annot.getRelationship()+"-> "+annot.getObject().getID());

		}
		Assert.assertTrue(annots.size() > 0);
	}

	public void writeToDatabase() throws DataAdapterException, IOException {
		OBDSQLDatabaseAdapterConfiguration wconfig = 
			new OBDSQLDatabaseAdapter.OBDSQLDatabaseAdapterConfiguration();
		wconfig.setSaveImplied(false);

		wconfig.setWritePath(jdbcPath);
		wconfig.setAnnotationMode(OBDSQLDatabaseAdapter.
				OBDSQLDatabaseAdapterConfiguration.AnnotationMode.ANNOTATIONS_ONLY);
		OBDSQLDatabaseAdapter wadapter = new OBDSQLDatabaseAdapter();
		//ReasonedLinkDatabase reasoner = rf.createReasoner();
		//reasoner.setLinkDatabase(linkDatabase);
		//wadapter.setReasoner(reasoner);
		//reasoner.recache();

		wadapter.doOperation(OBOAdapter.WRITE_ONTOLOGY, wconfig, session);
	}	

	// lifted from OE. Can we reuse from test
	public File writeTempOBOFile() throws IOException, DataAdapterException {

		OBOFileAdapter adapter = new OBOFileAdapter();
		OBOFileAdapter.OBOAdapterConfiguration config = new OBOFileAdapter.OBOAdapterConfiguration();
		File outFile = File.createTempFile("foo", "bar");
		//outFile.deleteOnExit();
		config.setWritePath(outFile.getAbsolutePath());
		adapter.doOperation(OBOAdapter.WRITE_ONTOLOGY, config, session);
		return outFile;
	}

	public void testForAnnotation(String su, String ob) {
		Assert.assertTrue(getFirstAnnotation(su,ob) != null);
	}
	
	public void testForAnnotationAssignedBy(String su, String ob, String by)  {
		Assert.assertTrue(getFirstAnnotation(su,ob).getAssignedBy().getID().equals(by));
	}
	
	public void testForAnnotationPublication(String su, String ob, String pubId)  {
		Assert.assertTrue(getFirstAnnotationWithPublication(su,ob,pubId) != null);
	}
	
	public void testForAnnotationWithEvidenceCode(String su, String ob, String code)  {
		Assert.assertTrue(getFirstAnnotationWithEvidenceCode(su,ob,code) != null);
	}
	
	public Annotation getFirstAnnotationWithPublication(String su, String ob, String pubId)  {
		for (Annotation annot : getAllAnnotations(su, ob))
			for (IdentifiedObject pub : annot.getSources())
				if (pub.getID().equals(pubId))
					return annot;
		return null;
	}
	
	public Annotation getFirstAnnotationWithEvidenceCode(String su, String ob, String code)  {
		for (Annotation annot : getAllAnnotations(su, ob))
			for (IdentifiedObject ev : annot.getEvidence())
				if (((Instance)ev).getType().getID().equals(code))
					return annot;
		return null;
	}
	
	public Annotation getFirstAnnotation(String su, String ob) {
		IdentifiedObject io = session.getObject(su);
		if (io != null) {
			Collection<Annotation> annots = getAnnotationsForSubject(io);
			for (Annotation annot : annots) {
				if (ob.equals(annot.getObject().getID())) {
					return annot;
				}
			}
		}
		return null;
	}
	
	public Collection<Annotation> getAllAnnotations(String su, String ob) {
		IdentifiedObject io = session.getObject(su);
		HashSet<Annotation> matches = new HashSet<Annotation>();
		if (io != null) {
			Collection<Annotation> annots = getAnnotationsForSubject(io);
			for (Annotation annot : annots) {
				if (ob.equals(annot.getObject().getID())) {
					matches.add(annot);
				}
			}
		}
		return matches;
	}
		
	public Collection<Annotation> getAnnotationsForSubject(String id) {
		return getAnnotationsForSubject(session.getObject(id));
	}
	public void writeAnnot(Annotation annot) {
		
		System.err.println("annot: "+annot+":: "+annot.getSubject()+" -"+annot.getRelationship()+"-> "+annot.getObject().getID());
		System.err.println(annot.getNamespace());
	}

	public Collection<Annotation> getAnnotationsForSubject(IdentifiedObject su) {
		Collection<Annotation> annots = new LinkedList<Annotation>();
		for (IdentifiedObject io : session.getObjects()) {
			if (io instanceof Annotation) {
				Annotation annot = (Annotation)io;
				if (su.equals(annot.getSubject())) {
					annots.add(annot);
				}
			}
		}
		return annots;
	}

}
