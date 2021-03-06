package phenote.dataadapter.worm;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import phenote.dataadapter.DataAdapterEx;
import phenote.dataadapter.QueryableDataAdapterI;
import phenote.datamodel.CharFieldEnum;
import phenote.datamodel.CharFieldException;
import phenote.datamodel.CharFieldValue;
import phenote.datamodel.CharacterI;
import phenote.datamodel.CharacterIFactory;
import phenote.datamodel.CharacterList;
import phenote.datamodel.CharacterListI;
import phenote.datamodel.TermNotFoundException;
import phenote.edit.EditManager;






public class WormGoAdapter implements QueryableDataAdapterI {

  private List<String> queryableFields = new ArrayList<String>(2);
  private List<String> queryableGroups = new ArrayList<String>(2);

  public WormGoAdapter() { init(); }

  public List<String> getQueryableGroups() {
    return queryableGroups;
  }

  public String getCommitButtonLabel() {
    return "Commit To Worm DB";
  }


  public void delete(Connection c, Statement s) {
    List<CharacterI> l = EditManager.inst().getDeletedAnnotations();
    try {
      for (CharacterI chr : l) {
        String joinkey = chr.getValueString("PgdbId");
        if (joinkey == null) continue;
        String blank = "";
        if (joinkey.equals(blank)) { continue; }
        System.out.println("Delete "+joinkey+" end"); 
        String postgres_table = "gop_wbgene"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "gop_curator_evidence"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "gop_dbtype"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "gop_goid"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "gop_goinference"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "gop_goontology"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "gop_lastupdate"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "gop_paper_evidence"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "gop_person_evidence"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "gop_protein"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "gop_qualifier"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "gop_with"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "gop_comment"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
      }
    } catch (Exception e) {
      System.out.println("Could not delete character: " + e);
    }
  }

  private void updatePostgresVal(Connection c, String postgres_table, String postgres_value, String joinkey, String value) {
    PreparedStatement ps = null;	// intialize postgres insert 
    try { ps = c.prepareStatement("INSERT INTO "+postgres_table+"_hst VALUES (?, ?)"); ps.setString(1, joinkey); ps.setString(2, value); }
    catch (SQLException se) {
      System.out.println("We got an exception while preparing our insert: that probably means our SQL is invalid"); se.printStackTrace(); errorPopup("updatePostgresVal INSERT "+postgres_table+"_hst "); return; }
    try { ps.executeUpdate(); }
    catch (SQLException se) { System.out.println("We got an exception while executing a history insert in updatePostgresVal table "+postgres_table+" joinkey "+joinkey+" value "+value+" : possibly bad SQL, or check the connection."); se.printStackTrace(); errorPopup("updatePostgresVal ps.executeUpdate "+postgres_table+"_hst "); return; }
    
    String pgisnull = "null";
    String pgblank = "";
//System.out.println("pgblank "+pgblank+" postgres_value "+postgres_value+" end");
    if ( postgres_value.equals(pgisnull) ) {			// no values in postgres
//        System.out.println("INSERT INTO "+postgres_table+" VALUES "+joinkey+" and "+value+" with postgres_value "+postgres_value+" end");
        try { ps = c.prepareStatement("INSERT INTO "+postgres_table+" VALUES (?, ?)"); ps.setString(1, joinkey); ps.setString(2, value); }
        catch (SQLException se) {
          System.out.println("We got an exception while preparing our insert: that probably means our SQL is invalid"); se.printStackTrace(); errorPopup("updatePostgresVal INSERT "+postgres_table+" "); return; }
        try { ps.executeUpdate(); }
        catch (SQLException se) { System.out.println("We got an exception while executing an insert update in updatePostgresVal table "+postgres_table+" joinkey "+joinkey+" value "+value+" : possibly bad SQL, or check the connection."); se.printStackTrace(); errorPopup("updatePostgresVal ps.executeUpdate "+postgres_table+" "); return; } }
      else { 							// some value in postgres, update it
//        System.out.println("UPDATING "+postgres_table+" VALUES "+joinkey+" and "+value+" with postgres_value "+postgres_value+" end");
        try { ps = c.prepareStatement("UPDATE "+postgres_table+" SET "+postgres_table+" = ? WHERE joinkey = '"+joinkey+"'"); ps.setString(1, value); }
        catch (SQLException se) {
          System.out.println("We got an exception while preparing our insert: that probably means our SQL is invalid"); se.printStackTrace(); errorPopup("updatePostgresVal UPDATE "+postgres_table+" "); return; }
        try { ps.executeUpdate(); }
        catch (SQLException se) { System.out.println("We got an exception while executing a data update in updatePostgresVal table "+postgres_table+" joinkey "+joinkey+" value "+value+" : possibly bad SQL, or check the connection."); se.printStackTrace(); errorPopup("updatePostgresVal ps.executeUpdate "+postgres_table+" "); return; }
        try { ps = c.prepareStatement("UPDATE "+postgres_table+" SET gop_timestamp = CURRENT_TIMESTAMP WHERE joinkey = '"+joinkey+"'"); }
        catch (SQLException se) {
          System.out.println("We got an exception while preparing our insert: that probably means our SQL is invalid"); se.printStackTrace(); errorPopup("updatePostgresVal UPDATE timestamp "+postgres_table+" "); return; }
        try { ps.executeUpdate(); }
        catch (SQLException se) { System.out.println("We got an exception while executing a timestamp update in updatePostgresVal table "+postgres_table+" joinkey "+joinkey+" value "+value+" : possibly bad SQL, or check the connection."); se.printStackTrace(); errorPopup("updatePostgresVal ps.executeUpdate timestamp "+postgres_table+" "); return; } }
  } // private void updatePostgresVal(String postgres_table, String joinkey, int colI, String value)

  private String queryPostgresCharacterNull(Statement s, String postgres_table, String joinkey) {
    // see if the postgres value corresponding to a phenote cell has an entry at all (@row) ; returns ``null'' if no row
    String default_value = null;
    ResultSet rs = null;	// intialize postgres query result
    System.out.println("SELECT * FROM "+postgres_table+" WHERE joinkey = '"+joinkey+"' ORDER BY gop_timestamp"); 
    try { rs = s.executeQuery("SELECT * FROM "+postgres_table+" WHERE joinkey = '"+joinkey+"' ORDER BY gop_timestamp"); }
    catch (SQLException se) {
      System.out.println("We got an exception while executing our "+postgres_table+" joinkey: that probably means our term SQL is invalid"); se.printStackTrace(); errorPopup("queryPostgresCharacterNull SELECT "+postgres_table+" "); }
    try { while (rs.next()) { default_value = rs.getString(2); } }		// assign the new term value
    catch (SQLException se) {
      System.out.println("We got an exception while getting a queryPostgresCharacter "+postgres_table+" result:this shouldn't happen: we've done something really bad."); se.printStackTrace(); errorPopup("queryPostgresCharacterNull rs.next"); } 
    if (default_value == null) { default_value = "null"; }
    return default_value; 
  }

  private void updateNormalField(Connection c, Statement s, String joinkey, String postgres_table, String tag_name, String tag_value) {
//    String postgres_value = "No postgres value assigned";
//    String postgres_value = "";
//System.out.println("table "+postgres_table+" joinkey "+joinkey+" checking updateNormalField");
    String postgres_value = queryPostgresCharacterNull(s, postgres_table, joinkey);
    String pgisnull = "null";
    String pg_value = postgres_value;
    if (postgres_value.equals(pgisnull)) { pg_value = ""; }	// pg_value converts postgres null into blank to check against tag value
    if (pg_value.equals(tag_value)) { 
//System.out.println("table "+postgres_table+" joinkey "+joinkey+" has equal values");
    } else { updatePostgresVal(c, postgres_table, postgres_value, joinkey, tag_value); }	// pass the pgisnull string if pg value is null
    System.out.println( tag_name+" : "+tag_value+" end.");
  }

  private void updateListField(Connection c, Statement s, String joinkey, String postgres_table, String tag_name, CharacterI chr) {
    try {
      StringBuilder sb = new StringBuilder(); Integer loopcount = 0;
      CharFieldValue list = chr.getValue(chr.getCharFieldForName(tag_name));
      List<CharFieldValue> valList = list.getCharFieldValueList();
//      for (CharFieldValue kid : valList) { String kidId = kid.getID(); loopcount++; if (loopcount > 1) { sb.append(","); } sb.append("\"").append(kidId).append("\""); }
      for (CharFieldValue kid : valList) { String kidId = kid.getID(); loopcount++; if (loopcount > 1) { sb.append("|"); } sb.append(kidId); }
      if (sb != null) { String tag_value = sb.toString(); updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value); } }
    catch (Exception e) {
      System.out.println("Could not get terms from character in updateListField : " + e); e.printStackTrace(); }
  }

  public void errorPopup(String errType) {
    String errText = errType + " : invalid SQL problem";
    JOptionPane.showMessageDialog(null,errText,errText,JOptionPane.INFORMATION_MESSAGE);
  }

  public void commit(CharacterListI charList) {
    Connection c = connectToDB();
    Statement s = null;
    boolean bool = true;

    try { s = c.createStatement(); }
      catch (SQLException se) {
      System.out.println("We got an exception while creating a statement: that probably means we're no longer connected."); se.printStackTrace(); errorPopup("commit create statement"); return; }
    try { bool = s.execute("BEGIN WORK"); }
    catch (SQLException se) {
      System.out.println("We got an exception while executing our int_name query: that probably means our column SQL is invalid"); se.printStackTrace(); errorPopup("commit BEGIN WORK"); return ; }
    for (CharacterI chr : charList.getList()) {
////      System.out.println("Chr "+chr+" end");
      try {

//        String objname = chr.getValueString("Object Name");	// get the objname value from the character, currently could have a column number
        String pgdbid = chr.getValueString("PgdbId");		// get the postgres database ID for that character
        String joinkey = pgdbid;
//System.out.println("pgdbid "+pgdbid+" end");
        if ( (pgdbid == null) || (pgdbid == "") ) { 
////          Integer joinkeyInt = Integer.parseInt(pgdbid);	// this can't do anything since pgdbid must be blank
          Integer joinkeyInt = 0;
          ResultSet rs = null;
          try { rs = s.executeQuery("SELECT joinkey FROM gop_wbgene "); }	// everything must have a wbgene
          catch (SQLException se) {
            System.out.println("We got an exception while executing our gop_wbgene query: that probably means our column SQL is invalid"); se.printStackTrace(); errorPopup("commit SELECT wbgene"); return; }
          try { while (rs.next()) { if (rs.getInt(1) > joinkeyInt) { joinkeyInt = rs.getInt(1); } } joinkeyInt++; joinkey = Integer.toString(joinkeyInt); }
            // get the next highest number joinkey for that character
          catch (SQLException se) {
            System.out.println("We got an exception while getting a column/term joinkey "+joinkey+" result:this shouldn't happen: we've done something really bad."); 
            se.printStackTrace(); errorPopup("commit rs.next"); return; } }
System.out.println("joinkey "+joinkey+" end");
        chr.setValue("PgdbId",joinkey);					// assign the postgres database ID

        String postgres_table = "gop_wbgene"; String tag_name = "Gene"; String tag_value = "";
        if ( chr.hasValue(tag_name) ) { tag_value = chr.getTerm(tag_name).getID(); }
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
        postgres_table = "gop_goontology"; tag_name = "GO Ontology"; tag_value = "";
        if ( chr.hasValue(tag_name) ) { tag_value = chr.getTerm(tag_name).getID(); }
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
        postgres_table = "gop_goid"; tag_name = "GO Term"; tag_value = "";
        if ( chr.hasValue(tag_name) ) { tag_value = chr.getTerm(tag_name).getID(); }
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
        postgres_table = "gop_paper_evidence"; tag_name = "Paper"; tag_value = "";
        if ( chr.hasValue(tag_name) ) { tag_value = chr.getTerm(tag_name).getID(); }
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
        postgres_table = "gop_person_evidence"; tag_name = "Person"; 
        updateListField(c, s, joinkey, postgres_table, tag_name, chr);
        postgres_table = "gop_curator_evidence"; tag_name = "Curator"; tag_value = "";
        if ( chr.hasValue(tag_name) ) { tag_value = chr.getTerm(tag_name).getID(); }
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
        postgres_table = "gop_goinference"; tag_name = "GO Evidence"; tag_value = "";
        if ( chr.hasValue(tag_name) ) { tag_value = chr.getTerm(tag_name).getID(); }
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
        postgres_table = "gop_dbtype"; tag_name = "DB Object Type"; tag_value = "";
        if ( chr.hasValue(tag_name) ) { tag_value = chr.getTerm(tag_name).getID(); }
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
        postgres_table = "gop_protein"; tag_name = "Gene Product"; tag_value = chr.getValueString(tag_name);
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
        postgres_table = "gop_with"; tag_name = "with"; tag_value = chr.getValueString(tag_name);
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
        postgres_table = "gop_qualifier"; tag_name = "Qualifier"; tag_value = "";
        if ( chr.hasValue(tag_name) ) { tag_value = chr.getTerm(tag_name).getID(); }
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
        postgres_table = "gop_comment"; tag_name = "Comment"; tag_value = chr.getValueString(tag_name);
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
        postgres_table = "gop_lastupdate"; tag_name = "Last Updated"; tag_value = chr.getValueString(tag_name);
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);

//        postgres_table = "int_torvariation"; tag_name = "tor Variation"; tag_value = chr.getValueString(tag_name);
//        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
//        postgres_table = "int_tortransgene"; tag_name = "tor Transgene"; tag_value = chr.getValueString(tag_name);
//        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
//        postgres_table = "int_torremark"; tag_name = "tor Remark"; tag_value = chr.getValueString(tag_name);
//        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
//        postgres_table = "int_effected"; tag_name = "Effected"; 
//        updateListField(c, s, joinkey, postgres_table, tag_name, chr);
//        postgres_table = "int_tedvariation"; tag_name = "ted Variation"; tag_value = chr.getValueString(tag_name);
//        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
//        postgres_table = "int_tedtransgene"; tag_name = "ted Transgene"; tag_value = chr.getValueString(tag_name);
//        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
//        postgres_table = "int_tedremark"; tag_name = "ted Remark"; tag_value = chr.getValueString(tag_name);
//        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
//        postgres_table = "int_type"; tag_name = "Interaction Type"; tag_value = chr.getValueString(tag_name);
//        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
//        postgres_table = "int_phenotype"; tag_name = "Phenotype"; 
//        updateListField(c, s, joinkey, postgres_table, tag_name, chr);
//        postgres_table = "int_rnai"; tag_name = "RNAi"; tag_value = chr.getValueString(tag_name);
//        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
//        postgres_table = "int_remark"; tag_name = "Remark"; tag_value = chr.getValueString(tag_name);
//        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
//        postgres_table = "int_paper"; tag_name = "Pub"; tag_value = "";
//        if ( chr.hasValue(tag_name) ) { tag_value = chr.getTerm(tag_name).getID(); }
//        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
////System.out.println("paper tag_value "+tag_value+" for postgres");
//        postgres_table = "int_curator"; tag_name = "Curator"; tag_value = "";
//        if ( chr.hasValue(tag_name) ) { tag_value = chr.getTerm(tag_name).getID(); }
//        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
//        postgres_table = "int_person"; tag_name = "Person"; 
//        updateListField(c, s, joinkey, postgres_table, tag_name, chr);
//        postgres_table = "int_otherevi"; tag_name = "Other Evidence"; tag_value = chr.getValueString(tag_name);
//        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
      } catch (Exception e) {
        System.out.println("Could not get terms from character: " + e);
        e.printStackTrace(); // helpful for debugging
      }
    } // for (CharacterI chr : charList.getList())
    try { bool = s.execute("COMMIT WORK"); }
    catch (SQLException se) {
      System.out.println("We got an exception while executing our int_name query: that probably means our column SQL is invalid"); se.printStackTrace(); errorPopup("commit COMMIT WORK"); return ; }
    delete(c, s);
  } // public void commit(CharacterListI charList)

  // Returns the first substring in input that matches the pattern.  Returns null if no match found.
  // lifted from  http://www.exampledepot.com/egs/java.util.regex/Greedy.html?l=rel
  public static String find(String patternStr, CharSequence input) {
      Pattern pattern = Pattern.compile(patternStr);
      Matcher matcher = pattern.matcher(input);
//       System.out.println("Pattern "+pattern+" end");
      if (matcher.find()) { return matcher.group(1); }
      return null;
  }

  private void init() {
    // dont HAVE to use CharFieldEnum but it does enforce using same strings across different data adapters which is good to enforce  the worm config needs to have "Pub" and "Object Name"
//    queryableFields.add(CharFieldEnum.PUB.getName()); // "Pub"
    queryableFields.add("Gene");		// Gene
    queryableFields.add("Paper");		// Paper
    // should their be a check that the current char fields have pub & allele?
//    queryableFields.add("NBP Date"); 
//    queryableGroups.add("referenceMaker");		// populate reference obo for the main
    queryableGroups.add("default");			// default last
  }
  /** return true if data adapter can query for the char field */
  public boolean isFieldQueryable(String field) {
    return queryableFields.contains(field);
  }

  private Connection connectToDB() {
    System.out.println("Checking if Driver is registered with DriverManager.");	// open postgres database connection
    try {
      Class.forName("org.postgresql.Driver");
    } catch (ClassNotFoundException cnfe) {
      System.out.println("Couldn't find the driver!");
      System.out.println("Let's print a stack trace, and exit.");
      cnfe.printStackTrace();
      errorPopup("connectToDB"); return null;
    }
    System.out.println("Registered the driver ok, so let's make a connection.");
    Connection c = null;
    try {
      // The second and third arguments are the username and password, respectively. They should be whatever is necessary to connect to the database.
      c = DriverManager.getConnection("jdbc:postgresql://131.215.52.76:5432/testdb", "postgres", "");     // tazendra
    } catch (SQLException se) {
      System.out.println("Couldn't connect: print out a stack trace and exit.");
      se.printStackTrace();
      System.out.println("Couldn't connect: stack trace done.");
      String getMessage = se.getMessage();
      getMessage = getMessage + " \nYour IP is not recognized by the postgres database, please contact help@wormbase.org if you're a valid Phenotype WormBase curator.";
      JOptionPane.showMessageDialog(null,getMessage,"Postgres connection error",JOptionPane.INFORMATION_MESSAGE);
    }
    if (c != null)
      System.out.println("Hooray! We connected to the database!");
    else
      System.out.println("We should never get here.");
    return c; 
  } // private Connection connectToDB

  private String queryPostgresCharacter(Statement s, String postgres_table, String default_value, String joinkey) {		// get the value corresponding to a phenote cell from a postgres table by column
//System.out.println( "queryPostgresCharacter for "+postgres_table+" "+joinkey+" with default_value "+default_value+" end");
    ResultSet rs = null;	// intialize postgres query result
      // get the phenotype term in timestamp order where the allele and column number match
    try { rs = s.executeQuery("SELECT * FROM "+postgres_table+" WHERE joinkey = '"+joinkey+"' ORDER BY gop_timestamp"); }
    catch (SQLException se) {
      System.out.println("We got an exception while executing our "+postgres_table+" joinkey: that probably means our term SQL is invalid"); se.printStackTrace(); errorPopup("queryPostgresCharacter SELECT "+postgres_table+" "); }
    try { while (rs.next()) { default_value = rs.getString(2); } }		// assign the new term value
    catch (SQLException se) {
      System.out.println("We got an exception while getting a queryPostgresCharacter "+postgres_table+" result:this shouldn't happen: we've done something really bad."); se.printStackTrace(); errorPopup("queryPostgresCharacter rs.next "+postgres_table+" "); } 
    if (default_value == null) { default_value = ""; }
//System.out.println( "queryPostgresCharacter for "+postgres_table+" "+joinkey+" gives "+default_value+" end");
    return default_value; 
  } // private String queryPostgresCharacter(Statement s, String postgres_table, String default_value, String joinkey)

  private CharacterListI queryPostgresCharacterMainList(CharacterListI charList, Statement s, String joinkey) {
      // populate a phenote character based on postgres value by joinkey, then append to character list
    try {
      CharacterI c1 = CharacterIFactory.makeChar();				// create a new character for a phenote row

//System.out.println("set PgdbId to "+joinkey+" END");
      c1.setValue("PgdbId",joinkey);					// assign the allele and the column
      String postgres_table = "gop_wbgene"; String postgres_value = ""; 			// postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
      c1.setValue("Gene",postgres_value);					// assign the allele and the column

      postgres_table = "gop_goontology"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
      if (postgres_value == "") { } else { c1.setValue("GO Ontology",postgres_value); }					// assign the queried value
      postgres_table = "gop_goid"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
      if (postgres_value == "") { } else { c1.setValue("GO Term",postgres_value); }					// assign the queried value
      postgres_table = "gop_paper_evidence"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
      if (postgres_value == "") { } else { c1.setValue("Paper",postgres_value); }					// assign the queried value
      postgres_table = "gop_person_evidence"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
      if (postgres_value == "") { } else { 
        postgres_value = postgres_value.replaceAll("\\|", "\\\",\\\"");
        postgres_value = "\""+postgres_value+"\"";
        c1.setValue("Person",postgres_value); }					// assign the queried value
      postgres_table = "gop_curator_evidence"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
      if (postgres_value == "") { } else { c1.setValue("Curator",postgres_value); }					// assign the queried value
      postgres_table = "gop_goinference"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
      if (postgres_value == "") { } else { c1.setValue("GO Evidence",postgres_value); }					// assign the queried value
      postgres_table = "gop_dbtype"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
      if (postgres_value == "") { } else { c1.setValue("DB Object Type",postgres_value); }					// assign the queried value
      postgres_table = "gop_protein"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
      if (postgres_value == "") { } else { c1.setValue("Gene Product",postgres_value); }					// assign the queried value
      postgres_table = "gop_with"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
      if (postgres_value == "") { } else { c1.setValue("with",postgres_value); }					// assign the queried value
      postgres_table = "gop_qualifier"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
      if (postgres_value == "") { } else { c1.setValue("Qualifier",postgres_value); }					// assign the queried value
      postgres_table = "gop_comment"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
      if (postgres_value == "") { } else { c1.setValue("Comment",postgres_value); }					// assign the queried value
      postgres_table = "gop_lastupdate"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
      if (postgres_value == "") { } else { c1.setValue("Last Updated",postgres_value); }					// assign the queried value

//      postgres_table = "int_effector"; postgres_value = ""; // postgres_value = "No postgres value assigned";
//      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
//      if (postgres_value == "") { } else { 
//        postgres_value = postgres_value.replaceAll("\\|", "\\\",\\\"");
//        postgres_value = "\""+postgres_value+"\"";
//        c1.setValue("Effector",postgres_value); }					// assign the queried value
//      postgres_table = "int_torvariation"; postgres_value = ""; // postgres_value = "No postgres value assigned";
//      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
//      if (postgres_value == "") { } else { c1.setValue("tor Variation",postgres_value); }					// assign the queried value
//      postgres_table = "int_tortransgene"; postgres_value = ""; // postgres_value = "No postgres value assigned";
//      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
//      if (postgres_value == "") { } else { c1.setValue("tor Transgene",postgres_value); }					// assign the queried value
//      postgres_table = "int_torremark"; postgres_value = ""; // postgres_value = "No postgres value assigned";
//      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
//      if (postgres_value == "") { } else { c1.setValue("tor Remark",postgres_value); }					// assign the queried value
//
//      postgres_table = "int_effected"; postgres_value = ""; // postgres_value = "No postgres value assigned";
//      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
//      if (postgres_value == "") { } else { 
//        postgres_value = postgres_value.replaceAll("\\|", "\\\",\\\"");
//        postgres_value = "\""+postgres_value+"\"";
//        c1.setValue("Effected",postgres_value); }					// assign the queried value
//      postgres_table = "int_tedvariation"; postgres_value = ""; // postgres_value = "No postgres value assigned";
//      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
//      if (postgres_value == "") { } else { c1.setValue("ted Variation",postgres_value); }					// assign the queried value
//      postgres_table = "int_tedtransgene"; postgres_value = ""; // postgres_value = "No postgres value assigned";
//      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
//      if (postgres_value == "") { } else { c1.setValue("ted Transgene",postgres_value); }					// assign the queried value
//      postgres_table = "int_tedremark"; postgres_value = ""; // postgres_value = "No postgres value assigned";
//      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
//      if (postgres_value == "") { } else { c1.setValue("ted Remark",postgres_value); }					// assign the queried value
//
//      postgres_table = "int_type"; postgres_value = ""; // postgres_value = "No postgres value assigned";
//      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
//      if (postgres_value == "") { } else { c1.setValue("Type",postgres_value); }					// assign the queried value
//
//      postgres_value = ""; 								// postgres_value = "No postgres value assigned";
//      postgres_table = "int_phenotype";
//      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
////      String phenotype_match = find("(WBPhenotype[0-9]*)", postgres_value);  	// Find a WBPhenotype followed by any amount of digits
////      if (phenotype_match != null) { postgres_value = phenotype_match; }		// query for this, otherwise keep the default value
////      if (postgres_value == "No postgres value assigned") { } else { c1.setValue("Phenotype",postgres_value); }					// assign the queried value
//      if (postgres_value == "") { } else { 
//        postgres_value = postgres_value.replaceAll("\\|", "\\\",\\\"");
//        postgres_value = "\""+postgres_value+"\"";
////System.out.println("set Phenotype to "+postgres_value+" END");
//        c1.setValue("Phenotype",postgres_value); }					// assign the queried value
//
//      postgres_value = ""; 								// postgres_value = "No postgres value assigned";
//      postgres_table = "int_rnai";
//      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
//      String rnai_match = find("(WBRNAi[0-9]*)", postgres_value);  	// Find a WBPhenotype followed by any amount of digits
//      if (rnai_match != null) { postgres_value = rnai_match; }		// query for this, otherwise keep the default value
//      if (postgres_value == "") { } else { c1.setValue("RNAi",postgres_value); }					// assign the queried value
//
//      postgres_table = "int_remark"; postgres_value = ""; // postgres_value = "No postgres value assigned";
//      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
//      if (postgres_value == "") { } else { c1.setValue("Remark",postgres_value); }					// assign the queried value
//
//      postgres_table = "int_paper"; postgres_value = ""; // postgres_value = "No postgres value assigned";
//      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
//      if (postgres_value == "") { } else { c1.setValue("Pub",postgres_value); }					// assign the queried value
////System.out.println("set Pub to "+postgres_value+" END");
//      postgres_table = "int_person"; postgres_value = ""; // postgres_value = "No postgres value assigned";
//      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
//      if (postgres_value == "") { } else { 
//        postgres_value = postgres_value.replaceAll("\\|", "\\\",\\\"");
//        postgres_value = "\""+postgres_value+"\"";
////System.out.println("set Person to "+postgres_value+" END");
//        c1.setValue("Person",postgres_value); }				// assign the queried value
////System.out.println("set Person to "+postgres_value+" END");
//      postgres_table = "int_curator"; postgres_value = ""; // postgres_value = "No postgres value assigned";
//      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
//      if (postgres_value == "") { } else { c1.setValue("Curator",postgres_value); }					// assign the queried value
////System.out.println("set Curator to "+postgres_value+" END");
//      postgres_table = "int_otherevi"; postgres_value = ""; // postgres_value = "No postgres value assigned";
//      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
//      if (postgres_value == "") { } else { c1.setValue("Other Evidence",postgres_value); }					// assign the queried value

// WBInteraction0000074

      charList.add(c1);								// add the character to the character list
    }
    catch (TermNotFoundException e) {
      System.out.println("Term Not Found Exception, assigning characters in queryPostgresCharacterMainList "+e.getMessage()); }
    catch (CharFieldException e) {
      System.out.println("Char Field Exception, assigning characters "+e.getMessage()); }
    return charList; 
  } // private CharacterListI queryPostgresCharacterMainList(CharacterList charList, Statement s, String joinkey)


  public CharacterListI query(String group, String field, String query) throws DataAdapterEx {
//    String m = "Worm adapter query not yet implemented. field: "+field+" query: "+query;
//    JOptionPane.showMessageDialog(null,m,"Worm stub",JOptionPane.INFORMATION_MESSAGE);

    String nameString = "Gene";			// the query could be for Gene or Paper
    String pubString = "Paper";

    CharacterListI charList = new CharacterList();	// create the CharacterList that we will return

    Connection c = connectToDB();
    Statement s = null;
    try { s = c.createStatement(); }
      catch (SQLException se) { System.out.println("We got an exception while creating a statement: that probably means we're no longer connected."); se.printStackTrace(); errorPopup("query create statement"); }

    ResultSet rs = null;	// intialize postgres query result
    List<String> joinkeys = new ArrayList<String>(2);
    int foundResults = 0;

    if (field.equals(nameString)) {			// if querying the name, get name data
      try { rs = s.executeQuery("SELECT * FROM gop_wbgene WHERE gop_wbgene ~ '"+query+"' ORDER BY joinkey"); }	// find the substring name that matches the queried name  
      catch (SQLException se) { System.out.println("Exception while executing gop_wbgene nameString "+query+" query: that probably means our SQL is invalid"); se.printStackTrace(); errorPopup("query SELECT gop_wbgene"); }
    } else if (field.equals(pubString)) {						// if querying the publication, get paper data
      try { rs = s.executeQuery("SELECT * FROM gop_paper_evidence WHERE gop_paper_evidence ~ '"+query+"' ORDER BY joinkey"); }	// find the name that matches the queried name
      catch (SQLException se) { System.out.println("Exception while executing gop_paper_evidence pubString "+query+" query: that probably means our SQL is invalid"); se.printStackTrace(); errorPopup("query SELECT gop_paper_evidence"); }
    } else {
      throw new DataAdapterEx("Worm query of "+query+" of field "+field+" failed");
    }

    try {
      while (rs.next()) {
        joinkeys.add(rs.getString(1));			
        foundResults++; } }
    catch (SQLException se) {
      System.out.println("We got an exception while getting a query catch while rs.next tempname joinkey "+query+" result:this shouldn't happen: we've done something really bad."); 
      se.printStackTrace(); errorPopup("query rs.next"); }

      for (String joinkey : joinkeys) {
//        System.out.println("J "+joinkey+" List");
        charList = queryPostgresCharacterMainList(charList, s, joinkey); 
//        System.out.println("END "+joinkey+" List");
      }
    if (foundResults <= 0) { throw new DataAdapterEx("Worm query of "+query+" of field "+field+" has no match in postgres"); }	// if there is no match for the allele in postgres
      else { return charList; } 	// if there is a match

  } // public CharacterListI query(String field, String query) throws DataAdapterEx


} // public class WormInteractionAdapter implements QueryableDataAdapterI




// __END__

