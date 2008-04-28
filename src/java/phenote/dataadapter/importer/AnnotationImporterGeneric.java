package phenote.dataadapter.importer;

import java.io.File;
import java.sql.SQLException;
import java.util.Collection;

import org.junit.Assert;
import org.obd.model.Graph;
import org.obd.model.bridge.OBOBridge;
import org.obd.query.Shard;
import org.obd.query.impl.OBDSQLShard;
import org.obo.annotation.datamodel.Annotation;
import org.obo.datamodel.IdentifiedObject;
import org.obo.datamodel.OBOSession;
import org.obo.util.AnnotationUtil;

import phenote.config.Config;
import phenote.dataadapter.delimited.DelimitedFileAdapter;
import phenote.datamodel.CharField;
import phenote.datamodel.CharFieldManager;
import phenote.datamodel.CharacterI;
import phenote.datamodel.CharacterListI;
import phenote.main.Phenote;


public class AnnotationImporterGeneric{
	
	private Shard shard;
	private CharacterListI clist;
	protected OBOSession session;
	
	public AnnotationImporterGeneric(){
		this.shard = null;
		this.clist = null;
	}
	
	public void importAnnotation(String configurationFile, String filePath) throws Exception{
		
		
		if (this.shard==null){
			throw new Exception("OBDSQLShard has not been configured. Call configureShard first.");
		}
		
		DelimitedFileAdapter ad = new DelimitedFileAdapter();
		File f = new File(filePath);
		if (!f.exists()){
			throw new Exception("File " + filePath + " does not exist.");
		}
		
		System.out.println("OUT");
		
		Phenote.resetAllSingletons();
		Config.inst().setConfigFile(configurationFile);
		Phenote phenote = Phenote.getPhenote();
                // rob - i think you need to commit this method
		//phenote.simpleInitOntologies();
		//phenote.initGui();
		System.out.println("OUT1");
		this.clist = ad.load(f);
		//this.testCharacterList(this.clist);
		System.out.println("OUT2");
		this.session = CharFieldManager.inst().getOboSession();
		System.out.println("OUT3");
		Graph g = OBOBridge.getAnnotationGraph(session);
		for (IdentifiedObject io : session.getObjects()) {
			if (io.isBuiltIn())
				continue;
			if (io.getNamespace() == null || io.isAnonymous()) {
				if (io instanceof Annotation) {
					// we should already have this
				}
				else {
					g.addNode(OBOBridge.obj2node(io));
				}
			}
		}
		System.out.println("OUT5");
		shard.putGraph(g);
		System.out.println("OUT6");
	}
	
	public void configureShard(String jdbcPath,String username,String password) throws Exception{
		try {
			this.shard = new OBDSQLShard();
			((OBDSQLShard)this.shard).connect(jdbcPath, username, password);
		} catch (SQLException e) {
			System.err.println("Configure Shard SQLException: " + e.getMessage());
			e.printStackTrace();
			throw e;
		} catch (ClassNotFoundException e) {
			System.err.println("Configure Shard ClassNotFoundExpection: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}
	
	
	private void testCharacterList(CharacterListI clist) {
		for (CharacterI c : clist.getList()) {
			System.out.println("character ="+c);
			for (CharField cf : c.getAllCharFields()) {
				System.out.println("  "+cf.getName()+" "+c.getValueString(cf));
			}

		}
	}
	
}