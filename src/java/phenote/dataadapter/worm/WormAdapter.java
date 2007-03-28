package phenote.dataadapter.worm;

// add http://jdbc.postgresql.org/download/postgresql-8.2-504.jdbc4.jar to trunk/jars/ directory

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import phenote.datamodel.CharFieldEnum;
import phenote.datamodel.CharacterListI;
import phenote.dataadapter.DataAdapterEx;
import phenote.dataadapter.QueryableDataAdapterI;

// import phenote.datamodel.CharacterI;
import phenote.datamodel.CharacterList;
import phenote.datamodel.Character;
import phenote.datamodel.CharFieldException;
import phenote.datamodel.TermNotFoundException;
import phenote.datamodel.CharField;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;





public class WormAdapter implements QueryableDataAdapterI {

  private List<String> queryableFields = new ArrayList<String>(2);

  public WormAdapter() { init(); }

  private void init() {
    // dont HAVE to use CharFieldEnum but it does enforce using same strings
    // across different data adapters which is good to enforce
    // the worm config needs to have "Pub" and "Allele"
    queryableFields.add(CharFieldEnum.PUB.getName()); // "Pub"
    queryableFields.add(CharFieldEnum.ALLELE.getName()); // "Allele"
    // should their be a check that the current char fields have pub & allele?
  }

  /** return true if data adapter can query for the char field */
  public boolean isFieldQueryable(String field) {
    return queryableFields.contains(field);
  }
  public CharacterListI query(String field, String query) throws DataAdapterEx {
//    String m = "Worm adapter query not yet implemented. field: "+field+" query: "+query;
//    JOptionPane.showMessageDialog(null,m,"Worm stub",JOptionPane.INFORMATION_MESSAGE);

    String alleleString = "Allele";			// the query could be for Allele or Pub
    String pubString = "Pub";
//    System.out.println("Querying field "+field+" query "+query+" end");
//    if (field.equals(alleleString)) { System.out.println("Yes Allele"); } else { System.out.println("Not Allele"); }

    CharacterListI charList = new CharacterList();	// create the CharacterList that we will return

    if (field.equals(alleleString)) {			// if querying the allele, get allele data
      System.out.println("Checking if Driver is registered with DriverManager.");	// open postgres database connection
      try {
        Class.forName("org.postgresql.Driver");
      } catch (ClassNotFoundException cnfe) {
        System.out.println("Couldn't find the driver!");
        System.out.println("Let's print a stack trace, and exit.");
        cnfe.printStackTrace();
        System.exit(1);
      }
      System.out.println("Registered the driver ok, so let's make a connection.");
      Connection c = null;
      try {
        // The second and third arguments are the username and password,
        // respectively. They should be whatever is necessary to connect
        // to the database.
        // c = DriverManager.getConnection("jdbc:postgresql://andiamo.caltech.edu/testdb", "postgres", "");
        // c = DriverManager.getConnection("jdbc:postgresql://131.215.52.86:5432/testdb", "postgres", "");  // andiamo, if postgres is running there
        c = DriverManager.getConnection("jdbc:postgresql://131.215.52.76:5432/testdb", "postgres", "");     // tazendra
        // c = DriverManager.getConnection("jdbc:postgresql://localhost/testdb", "postgres", "");           // with /usr/local/pgsql/data/postgresql.conf set to localhost
        //c = DriverManager.getConnection("jdbc:postgresql://localhost/booktown", "username", "password");        // sample
      } catch (SQLException se) {
        System.out.println("Couldn't connect: print out a stack trace and exit.");
        se.printStackTrace();
        System.exit(1);
      }
      if (c != null)
        System.out.println("Hooray! We connected to the database!");
      else
        System.out.println("We should never get here.");
      Statement s = null;
      try {
        s = c.createStatement();
      } catch (SQLException se) {
        System.out.println("We got an exception while creating a statement:" +
                           "that probably means we're no longer connected.");
        se.printStackTrace();
        System.exit(1);
      } // end opening postgres database connection


      ResultSet rs = null;	// intialize postgres query result
      try { rs = s.executeQuery("SELECT * FROM alp_tempname WHERE alp_tempname = '"+query+"'");	}	// find the allele that matches the queried allele
      catch (SQLException se) {
        System.out.println("We got an exception while executing our query: that probably means our SQL is invalid"); se.printStackTrace(); System.exit(1); }
      int foundAllele = 0;
      try {
        while (rs.next()) {
            System.out.println(rs.getString(1) + "\t" + rs.getString(2) + "\t" + rs.getString(3));	// comment out later
            foundAllele++; } }		// if there's a result in postgres we have found an allele
      catch (SQLException se) {
        System.out.println("We got an exception while getting a result:this shouldn't happen: we've done something really bad."); se.printStackTrace(); System.exit(1); }

      if (foundAllele <= 0) { throw new DataAdapterEx("Worm query of "+query+" of field "+field+" has no match in postgres"); }	// if there is no match for the allele in postgres
      else {		// if there is a match
        try {
          int boxes = 0;	// the number of boxes for that allele
          rs = null;		// init result from postgres query
          try { rs = s.executeQuery("SELECT alp_box FROM alp_curator WHERE joinkey = '"+query+"' ORDER BY alp_box DESC"); }	// grab the highest number box for that allele
          catch (SQLException se) {
            System.out.println("We got an exception while executing our query:" + "that probably means our SQL is invalid"); se.printStackTrace(); System.exit(1); }
          try { rs.next(); { boxes = rs.getInt(1); } }		// assign the highest number box for that allele to the number of boxes
          catch (SQLException se) {
            System.out.println("We got an exception while getting a result:this " + "shouldn't happen: we've done something really bad."); se.printStackTrace(); System.exit(1); }
          System.out.println("We have "+boxes+" boxes");	// comment out later
          for (int boxI=1; boxI<boxes+1; boxI++) {		// for each of those boxes
            String paper = "No Paper assigned";			// initialize paper value
            rs = null;
	      // get the papers in timestamp order where the allele and box number match
            try { rs = s.executeQuery("SELECT * FROM alp_paper WHERE joinkey = '"+query+"' AND alp_box='"+boxI+"' ORDER BY alp_timestamp"); }
            catch (SQLException se) {
              System.out.println("We got an exception while executing our query:" + "that probably means our pap SQL is invalid"); se.printStackTrace(); System.exit(1); }
            try { while (rs.next()) { paper = rs.getString(3); } } 	// assign the paper
            catch (SQLException se) {
              System.out.println("We got an exception while getting a result:this " + "shouldn't happen: we've done something really bad."); se.printStackTrace(); System.exit(1); }

            String curator = "No Curator assigned";
            rs = null;
            try { rs = s.executeQuery("SELECT * FROM alp_curator WHERE joinkey = '"+query+"' AND alp_box='"+boxI+"' ORDER BY alp_timestamp"); }
            catch (SQLException se) {
              System.out.println("We got an exception while executing our query:" + "that probably means our cur SQL is invalid"); se.printStackTrace(); System.exit(1); }
            try { while (rs.next()) { curator = rs.getString(3); } } 
            catch (SQLException se) {
              System.out.println("We got an exception while getting a result:this " + "shouldn't happen: we've done something really bad."); se.printStackTrace(); System.exit(1); }

            String person = "No Person assigned";
            rs = null;
            try { rs = s.executeQuery("SELECT * FROM alp_person WHERE joinkey = '"+query+"' AND alp_box='"+boxI+"' ORDER BY alp_timestamp"); }
            catch (SQLException se) {
              System.out.println("We got an exception while executing our query:" + "that probably means our per SQL is invalid"); se.printStackTrace(); System.exit(1); }
            try { while (rs.next()) { person = rs.getString(3); } } 
            catch (SQLException se) {
              System.out.println("We got an exception while getting a result:this " + "shouldn't happen: we've done something really bad."); se.printStackTrace(); System.exit(1); }

            String phen_text_remark = "No Phenotype Text Remark assigned";
            rs = null;
            try { rs = s.executeQuery("SELECT * FROM alp_phenotype WHERE joinkey = '"+query+"' AND alp_box='"+boxI+"' ORDER BY alp_timestamp"); }
            catch (SQLException se) {
              System.out.println("We got an exception while executing our query:" + "that probably means our ptr SQL is invalid"); se.printStackTrace(); System.exit(1); }
            try { while (rs.next()) { phen_text_remark = rs.getString(3); } } 
            catch (SQLException se) {
              System.out.println("We got an exception while getting a result:this " + "shouldn't happen: we've done something really bad."); se.printStackTrace(); System.exit(1); }

            String remark = "No Other Remark assigned";
            rs = null;
            try { rs = s.executeQuery("SELECT * FROM alp_remark WHERE joinkey = '"+query+"' AND alp_box='"+boxI+"' ORDER BY alp_timestamp"); }
            catch (SQLException se) {
              System.out.println("We got an exception while executing our query:" + "that probably means our orem SQL is invalid"); se.printStackTrace(); System.exit(1); }
            try { while (rs.next()) { remark = rs.getString(3); } } 
            catch (SQLException se) {
              System.out.println("We got an exception while getting a result:this " + "shouldn't happen: we've done something really bad."); se.printStackTrace(); System.exit(1); }

            String genetic_interaction = "No Genetic Interaction assigned";
            rs = null;
            try { rs = s.executeQuery("SELECT * FROM alp_intx_desc WHERE joinkey = '"+query+"' AND alp_box='"+boxI+"' ORDER BY alp_timestamp"); }
            catch (SQLException se) {
              System.out.println("We got an exception while executing our query:" + "that probably means our gint SQL is invalid"); se.printStackTrace(); System.exit(1); }
            try { while (rs.next()) { genetic_interaction = rs.getString(3); } } 
            catch (SQLException se) {
              System.out.println("We got an exception while getting a result:this " + "shouldn't happen: we've done something really bad."); se.printStackTrace(); System.exit(1); }

            int columns = 0;		// the number of columns in that box
            rs = null;
	      // grab the highest number column for that box and that allele
            try { rs = s.executeQuery("SELECT alp_column FROM alp_term WHERE joinkey = '"+query+"' AND alp_box = '"+boxI+"' ORDER BY alp_column DESC"); }
            catch (SQLException se) {
              System.out.println("We got an exception while executing our query:" + "that probably means our column SQL is invalid"); se.printStackTrace(); System.exit(1); }
            try { rs.next(); { columns = rs.getInt(1); } }		// assign the highest number column for that box and that allele to the number of columns
            catch (SQLException se) {
              System.out.println("We got an exception while getting a result:this " + "shouldn't happen: we've done something really bad."); se.printStackTrace(); System.exit(1); }

            System.out.println("We have "+columns+" columns in box "+boxI+".");		// comment out later
            for (int colI=1; colI<columns+1; colI++) {					// for each of those columns
              Character c1 = new Character();						// create a new character for a phenote row

              String phenotype = "No Phenotype assigned";
              rs = null;
                // get the phenotype term in timestamp order where the allele and box and column number match
              try { rs = s.executeQuery("SELECT * FROM alp_term WHERE joinkey = '"+query+"' AND alp_box='"+boxI+"' AND alp_column='"+colI+"' ORDER BY alp_timestamp"); }
              catch (SQLException se) {
                System.out.println("We got an exception while executing our query:" + "that probably means our term SQL is invalid"); se.printStackTrace(); System.exit(1); }
              try { while (rs.next()) { phenotype = rs.getString(4); } }		// assign the phenotype term value
              catch (SQLException se) {
                System.out.println("We got an exception while getting a result:this " + "shouldn't happen: we've done something really bad."); se.printStackTrace(); System.exit(1); }
 
              String phen_remark = "No Phenotype Remark assigned";
              rs = null;
              try { rs = s.executeQuery("SELECT * FROM alp_phen_remark WHERE joinkey = '"+query+"' AND alp_box='"+boxI+"' AND alp_column='"+colI+"' ORDER BY alp_timestamp"); }
              catch (SQLException se) {
                System.out.println("We got an exception while executing our query:" + "that probably means our phen_rem SQL is invalid"); se.printStackTrace(); System.exit(1); }
              try { while (rs.next()) { phen_remark = rs.getString(4); } } 
              catch (SQLException se) {
                System.out.println("We got an exception while getting a result:this " + "shouldn't happen: we've done something really bad."); se.printStackTrace(); System.exit(1); }
 
              String anatomy = "No anatomy assigned";
              rs = null;
              try { rs = s.executeQuery("SELECT * FROM alp_anat_term WHERE joinkey = '"+query+"' AND alp_box='"+boxI+"' AND alp_column='"+colI+"' ORDER BY alp_timestamp"); }
              catch (SQLException se) {
                System.out.println("We got an exception while executing our query:" + "that probably means our anaterm SQL is invalid"); se.printStackTrace(); System.exit(1); }
              try { while (rs.next()) { anatomy = rs.getString(4); } } 
              catch (SQLException se) {
                System.out.println("We got an exception while getting a result:this " + "shouldn't happen: we've done something really bad."); se.printStackTrace(); System.exit(1); }
 
              String stage = "No Stage assigned";
              rs = null;
              try { rs = s.executeQuery("SELECT * FROM alp_lifestage WHERE joinkey = '"+query+"' AND alp_box='"+boxI+"' AND alp_column='"+colI+"' ORDER BY alp_timestamp"); }
              catch (SQLException se) {
                System.out.println("We got an exception while executing our query:" + "that probably means our stage SQL is invalid"); se.printStackTrace(); System.exit(1); }
              try { while (rs.next()) { stage = rs.getString(4); } } 
              catch (SQLException se) {
                System.out.println("We got an exception while getting a result:this " + "shouldn't happen: we've done something really bad."); se.printStackTrace(); System.exit(1); }

              c1.setValue("Allele",query);						// assign the allele
              c1.setValue("Pub",paper);
              c1.setValue("Curator",curator);
              c1.setValue("Person",person);
              c1.setValue("Phenotype Text Remark",phen_text_remark);
              c1.setValue("Other Remark",remark);
              c1.setValue("Genetic Interaction",genetic_interaction);
              c1.setValue("Phenotype",phenotype);
              c1.setValue("Phenotype Remark",phen_remark);
//               c1.setValue("Anatomy",anatomy);
              c1.setValue("Stage",stage);
              charList.add(c1);								// add the character to the character list
              System.out.println("Added to charList term "+phenotype+" column "+colI+" box "+boxI+".");		// comment out later
            } // for (int colI=1; colI<columns; colI++)
          } // for (int boxI=1; boxI<boxes; boxI++)
        }
        catch (TermNotFoundException e) {} // do nothing - try next char field
        catch (CharFieldException e) {} // do nothing - try next char field
        return charList; 
      } // end -- if there's an allele found in postgres

    } else if (field.equals(pubString)) {						// if querying the publication, get paper data
      System.out.println("Checking if Driver is registered with DriverManager.");	// open postgres database connection
      try { Class.forName("org.postgresql.Driver"); } 
      catch (ClassNotFoundException cnfe) {
        System.out.println("Couldn't find the driver!"); System.out.println("Let's print a stack trace, and exit."); cnfe.printStackTrace(); System.exit(1); }
      System.out.println("Registered the driver ok, so let's make a connection.");
      Connection c = null;
      try { c = DriverManager.getConnection("jdbc:postgresql://131.215.52.76:5432/testdb", "postgres", ""); }     // tazendra
      catch (SQLException se) { System.out.println("Couldn't connect: print out a stack trace and exit."); se.printStackTrace(); System.exit(1); }
      if (c != null) System.out.println("Hooray! We connected to the database!");
        else System.out.println("We should never get here.");
      Statement s = null;
      try { s = c.createStatement(); }
       catch (SQLException se) {
        System.out.println("We got an exception while creating a statement:" + "that probably means we're no longer connected."); se.printStackTrace(); System.exit(1); }

      ResultSet rs = null;				// initialize result of query
      try { rs = s.executeQuery("SELECT DISTINCT(joinkey) FROM alp_paper WHERE alp_paper ~ '"+query+"' ORDER BY joinkey;"); }	// get the alleles from a paper
      catch (SQLException se) {
        System.out.println("We got an exception while executing our query:" + "that probably means our SQL is invalid"); se.printStackTrace(); System.exit(1); }
      String allele = "No Allele assigned for Publication "+query+".";			// initialize allele in each phenote character Row
      try { while (rs.next()) {								// while there's data in postgres
        allele = rs.getString(1); 							// get the allele from postgres
        try {
          Character c1 = new Character(); 						// make a new character
          c1.setValue("Allele",allele); 						// assign the allele
          charList.add(c1);								// add the character to the character list
        }
        catch (TermNotFoundException e) {} // do nothing - try next char field
        catch (CharFieldException e) {} // do nothing - try next char field
      } }
      catch (SQLException se) {
        System.out.println("We got an exception while getting a result:this " + "shouldn't happen: we've done something really bad."); se.printStackTrace(); System.exit(1); }
      return charList; 

//    for (CharacterI chr : charList.getList()) {
//      System.out.println("Chr "+chr+" Phenoset "+phenoset+" end");
//        
//      // builds Phenoset from characters
//      addCharAndGenotypeToPhenoset(chr,phenoset);
//    }

    
    } else {
      // if query has failed...
      throw new DataAdapterEx("Worm query of "+query+" of field "+field+" failed");
    }

  } // public CharacterListI query(String field, String query) throws DataAdapterEx
} // public class WormAdapter implements QueryableDataAdapterI
