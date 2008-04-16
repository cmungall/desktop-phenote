package phenote.datamodel;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

import org.bbop.dataadapter.DataAdapterException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.obo.annotation.datamodel.Annotation;
import org.obo.dataadapter.OBDSQLDatabaseAdapter;
import org.obo.dataadapter.OBOAdapter;
import org.obo.dataadapter.OBOFileAdapter;
import org.obo.dataadapter.OBDSQLDatabaseAdapter.OBDSQLDatabaseAdapterConfiguration;
import org.obo.datamodel.IdentifiedObject;
import org.obo.datamodel.Instance;
import org.obo.datamodel.OBOSession;
import org.obo.test.AbstractAnnotationTest;
import org.obo.util.AnnotationUtil;

import phenote.config.Config;
import phenote.config.ConfigException;
import phenote.dataadapter.delimited.DelimitedFileAdapter;
import phenote.main.Phenote;

public abstract class AbstractAnnotationModelTest  {



	protected static OBOSession session;
	protected static String getConfigFileName() { return ""; }
	protected static String getDataFilePath() { return "";}

	String jdbcPath = "jdbc:postgresql://localhost:5432/obdtest";

	@BeforeClass public static void initialize() throws ConfigException {
		Phenote.resetAllSingletons();
		Config.inst().setConfigFile(getConfigFileName());
		Phenote phenote = Phenote.getPhenote();
		phenote.initOntologies();
		phenote.initGui();
		DelimitedFileAdapter ad = new DelimitedFileAdapter();
		CharacterListI clist = 
			ad.load(new File(getDataFilePath()));
		System.err.println("clist size: "+clist.size());
		session = CharFieldManager.inst().getOboSession();
	}

	@Before public void setup() {
	}

	@Test public void checkAnnotations()   {
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
