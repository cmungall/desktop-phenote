package org.phenoscape.bridge;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import org.apache.xmlbeans.XmlException;
import org.junit.Test;
import org.obd.model.Graph;
import org.obd.query.impl.OBOSessionShard;
import org.phenoscape.io.NeXMLReader;
import org.phenoscape.model.DataSet;
import org.phenoscape.model.OntologyController;


public class OBDModelBridgeTest {

	@Test
	public void testLoad() throws XmlException, IOException, SQLException, ClassNotFoundException {
		OntologyController oc = new OntologyController();
		NeXMLReader reader = new NeXMLReader(new File("test/testfiles/Fang2003-nexml.xml"), oc.getOBOSession());
		DataSet ds = reader.getDataSet();
		OBDModelBridge bridge = new OBDModelBridge();
		bridge.translate(ds);
		Graph g = bridge.getGraph();

		//OBDSQLShard obdsql = new OBDSQLShard();
		//obdsql.connect("jdbc:postgresql://localhost:5432/obdtest");
		//obdsql.putGraph(g);
    OBOSessionShard s = new OBOSessionShard();
		s.putGraph(g);
	}
}
