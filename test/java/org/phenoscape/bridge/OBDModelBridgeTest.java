package org.phenoscape.bridge;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import org.apache.xmlbeans.XmlException;
import org.junit.Test;
import org.obd.model.Graph;
import org.obd.model.bridge.OBDXMLBridge;
import org.obd.model.bridge.OBOBridge;
import org.obd.model.bridge.OWLBridge;
import org.obd.query.impl.OBDSQLShard;
import org.obd.query.impl.RDFShard;
import org.phenoscape.io.NeXMLReader;
import org.phenoscape.model.DataSet;
import org.phenoscape.model.OntologyController;


public class OBDModelBridgeTest {

	@Test
	public void testLoad() throws XmlException, IOException, SQLException, ClassNotFoundException {
		OntologyController oc = new OntologyController();
		NeXMLReader reader = new NeXMLReader(new File("test/testfiles/Fang2003-nexml.xml"), oc.getOBOSession());
		DataSet ds = new DataSet();
		ds.getCharacters().addAll(reader.getCharacters());
		ds.getTaxa().addAll(reader.getTaxa());
		ds.setCurators(reader.getCuratorsText());
		ds.setPublication(reader.getPublicationText());
		ds.setPublicationNotes(reader.getPubNotesText());
		ds.setMatrix(reader.getMatrix());
		OBDModelBridge bridge = new OBDModelBridge();
		bridge.translate(ds);
		Graph g = bridge.getGraph();

		OBDSQLShard obdsql = new OBDSQLShard();
		obdsql.connect("jdbc:postgresql://localhost:5432/obdtest");
		obdsql.putGraph(g);

		System.out.println(OBDXMLBridge.toXML(g));
		System.out.println(OBOBridge.toOBOString(g));
		RDFShard rdfShard = new RDFShard();
		rdfShard.putGraph(g);
		
		//rdfShard.getModel();
		
		System.out.println(OWLBridge.toOWLString(g));
	}
}
