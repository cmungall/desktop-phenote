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






public class WormInteractionAdapter implements QueryableDataAdapterI {

  private List<String> queryableFields = new ArrayList<String>(2);
  private List<String> queryableGroups = new ArrayList<String>(2);

  public WormInteractionAdapter() { init(); }

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
        String postgres_table = "int_name"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
//        postgres_table = "int_effector"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
//        postgres_table = "int_torvariation"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
//        postgres_table = "int_tortransgene"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
//        postgres_table = "int_effected"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
//        postgres_table = "int_tedvariation"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
//        postgres_table = "int_tedtransgene"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "int_geneone"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "int_variationone"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "int_transgeneone"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "int_genetwo"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "int_variationtwo"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "int_transgenetwo"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "int_geneextra"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "int_variationextra"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "int_transgeneextra"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "int_type"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "int_phenotype"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "int_rnai"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "int_remark"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "int_paper"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "int_curator"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "int_person"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "int_otherevi"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
        postgres_table = "int_treatment"; updateNormalField(c, s, joinkey, postgres_table, postgres_table, blank);
      }
    } catch (Exception e) {
      System.out.println("Could not delete character: " + e);
    }
  }

  private void updatePostgresVal(Connection c, String postgres_table, String postgres_value, String joinkey, String value) {
    PreparedStatement ps = null;	// intialize postgres insert 
    try { ps = c.prepareStatement("INSERT INTO "+postgres_table+"_hst VALUES (?, ?)"); ps.setString(1, joinkey); ps.setString(2, value); }
    catch (SQLException se) {
      System.out.println("We got an exception while preparing our insert: that probably means our SQL is invalid"); se.printStackTrace(); errorPopup("updatePostgresVal INSERT"); return; }
    try { ps.executeUpdate(); }
    catch (SQLException se) { System.out.println("We got an exception while executing a history insert in updatePostgresVal table "+postgres_table+" joinkey "+joinkey+" value "+value+" : possibly bad SQL, or check the connection."); se.printStackTrace(); errorPopup("updatePostgresVal ps.executeUpdate"); return; }
    
    String pgisnull = "null";
    String pgblank = "";
//System.out.println("pgblank "+pgblank+" postgres_value "+postgres_value+" end");
    if ( postgres_value.equals(pgisnull) ) {			// no values in postgres
//        System.out.println("INSERT INTO "+postgres_table+" VALUES "+joinkey+" and "+value+" with postgres_value "+postgres_value+" end");
        try { ps = c.prepareStatement("INSERT INTO "+postgres_table+" VALUES (?, ?)"); ps.setString(1, joinkey); ps.setString(2, value); }
        catch (SQLException se) {
          System.out.println("We got an exception while preparing our insert: that probably means our SQL is invalid"); se.printStackTrace(); errorPopup("updatePostgresVal INSERT "+postgres_table+" "); return; }
        try { ps.executeUpdate(); }
        catch (SQLException se) { System.out.println("We got an exception while executing an insert update in updatePostgresVal table "+postgres_table+" joinkey "+joinkey+" value "+value+" : possibly bad SQL, or check the connection."); se.printStackTrace(); errorPopup("updatePostgresVal ps.executeUpdate()"); return; } }
      else { 							// some value in postgres, update it
//        System.out.println("UPDATING "+postgres_table+" VALUES "+joinkey+" and "+value+" with postgres_value "+postgres_value+" end");
        try { ps = c.prepareStatement("UPDATE "+postgres_table+" SET "+postgres_table+" = ? WHERE joinkey = '"+joinkey+"'"); ps.setString(1, value); }
        catch (SQLException se) {
          System.out.println("We got an exception while preparing our insert: that probably means our SQL is invalid"); se.printStackTrace(); errorPopup("updatePostgresVal UPDATE "+postgres_table+" "); return; }
        try { ps.executeUpdate(); }
        catch (SQLException se) { System.out.println("We got an exception while executing a data update in updatePostgresVal table "+postgres_table+" joinkey "+joinkey+" value "+value+" : possibly bad SQL, or check the connection."); se.printStackTrace(); errorPopup("updatePostgresVal ps.executeUpdate()"); return; }
        try { ps = c.prepareStatement("UPDATE "+postgres_table+" SET int_timestamp = CURRENT_TIMESTAMP WHERE joinkey = '"+joinkey+"'"); }
        catch (SQLException se) {
          System.out.println("We got an exception while preparing our insert: that probably means our SQL is invalid"); se.printStackTrace(); errorPopup("updatePostgresVal UPDATE timestamp "+postgres_table+" "); return; }
        try { ps.executeUpdate(); }
        catch (SQLException se) { System.out.println("We got an exception while executing a timestamp update in updatePostgresVal table "+postgres_table+" joinkey "+joinkey+" value "+value+" : possibly bad SQL, or check the connection."); se.printStackTrace(); errorPopup("updatePostgresVal ps.executeUpdate()"); return; } }
  } // private void updatePostgresVal(String postgres_table, String joinkey, int colI, String value)

  private String queryPostgresCharacterNull(Statement s, String postgres_table, String joinkey) {
    // see if the postgres value corresponding to a phenote cell has an entry at all (@row) ; returns ``null'' if no row
    String default_value = null;
    ResultSet rs = null;	// intialize postgres query result
    try { rs = s.executeQuery("SELECT * FROM "+postgres_table+" WHERE joinkey = '"+joinkey+"' ORDER BY int_timestamp"); }
    catch (SQLException se) {
      System.out.println("We got an exception while executing our "+postgres_table+" joinkey: that probably means our term SQL is invalid"); se.printStackTrace(); errorPopup("queryPostgresCharacterNull SELECT "+postgres_table+" "); }
    try { while (rs.next()) { default_value = rs.getString(2); } }		// assign the new term value
    catch (SQLException se) {
      System.out.println("We got an exception while getting a queryPostgresCharacter "+postgres_table+" result:this shouldn't happen: we've done something really bad."); se.printStackTrace(); errorPopup("queryPostgresCharacterNull rs.getString"); } 
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
      System.out.println("We got an exception while creating a statement: that probably means we're no longer connected."); se.printStackTrace(); errorPopup("commit createStatement"); return ; }
    try { bool = s.execute("BEGIN WORK"); }
    catch (SQLException se) {
      System.out.println("We got an exception while executing our int_name query: that probably means our column SQL is invalid"); se.printStackTrace(); errorPopup("commit BEGIN WORK"); return ; }
    for (CharacterI chr : charList.getList()) {
//      System.out.println("Chr "+chr+" end");
      try {
        String objname = chr.getValueString("Interaction ID");	// get the objname value from the character, currently could have a column number
        String pgdbid = chr.getValueString("PgdbId");		// get the postgres database ID for that character
        String joinkey = pgdbid;
//System.out.println("pgdbid "+pgdbid+" end");
        if ( (pgdbid == null) || (pgdbid == "") ) { 
//          Integer joinkeyInt = Integer.parseInt(pgdbid);	// this can't do anything since pgdbid must be blank
          Integer joinkeyInt = 0;
          ResultSet rs = null;
          try { rs = s.executeQuery("SELECT joinkey FROM int_curator "); }
          catch (SQLException se) {
            System.out.println("We got an exception while executing our int_name query: that probably means our column SQL is invalid"); se.printStackTrace(); errorPopup("commit SELECT int_name"); return ; }
          try { while (rs.next()) { if (rs.getInt(1) > joinkeyInt) { joinkeyInt = rs.getInt(1); } } joinkeyInt++; joinkey = Integer.toString(joinkeyInt); }
            // get the next highest number joinkey for that character
          catch (SQLException se) {
            System.out.println("We got an exception while getting a column/term joinkey "+joinkey+" result:this shouldn't happen: we've done something really bad."); 
            se.printStackTrace(); errorPopup("commit rs.next"); return ; } }
//System.out.println("joinkey "+joinkey+" end");
        chr.setValue("PgdbId",joinkey);					// assign the postgres database ID

        String postgres_table = "int_name"; String tag_name = "Name"; String tag_value = chr.getValueString(tag_name);
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
//        postgres_table = "int_effector"; tag_name = "Effector"; tag_value = "";
        postgres_table = "int_geneone"; tag_name = "Effector"; tag_value = "";
        if ( chr.hasValue(tag_name) ) { tag_value = chr.getTerm(tag_name).getID(); }
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
//        postgres_table = "int_torvariation"; tag_name = "tor Variation"; tag_value = chr.getValueString(tag_name);
        postgres_table = "int_variationone"; tag_name = "tor Variation"; tag_value = chr.getValueString(tag_name);
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
//        postgres_table = "int_tortransgene"; tag_name = "tor Transgene"; tag_value = chr.getValueString(tag_name);
        postgres_table = "int_transgeneone"; tag_name = "tor Transgene"; tag_value = chr.getValueString(tag_name);
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
//        postgres_table = "int_effected"; tag_name = "Effected"; tag_value = "";
        postgres_table = "int_genetwo"; tag_name = "Effected"; tag_value = "";
        if ( chr.hasValue(tag_name) ) { tag_value = chr.getTerm(tag_name).getID(); }
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
//        postgres_table = "int_tedvariation"; tag_name = "ted Variation"; tag_value = chr.getValueString(tag_name);
        postgres_table = "int_variationtwo"; tag_name = "ted Variation"; tag_value = chr.getValueString(tag_name);
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
//        postgres_table = "int_tedtransgene"; tag_name = "ted Transgene"; tag_value = chr.getValueString(tag_name);
        postgres_table = "int_transgenetwo"; tag_name = "ted Transgene"; tag_value = chr.getValueString(tag_name);
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
        postgres_table = "int_geneextra"; tag_name = "Gene Extra"; tag_value = "";
        updateListField(c, s, joinkey, postgres_table, tag_name, chr);
        postgres_table = "int_variationextra"; tag_name = "Variation Extra"; tag_value = chr.getValueString(tag_name);
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
        postgres_table = "int_transgeneextra"; tag_name = "Transgene Extra"; tag_value = chr.getValueString(tag_name);
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
        postgres_table = "int_type"; tag_name = "Type"; tag_value = chr.getValueString(tag_name);
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
        postgres_table = "int_phenotype"; tag_name = "Phenotype"; 
        updateListField(c, s, joinkey, postgres_table, tag_name, chr);
        postgres_table = "int_rnai"; tag_name = "RNAi"; tag_value = chr.getValueString(tag_name);
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
        postgres_table = "int_remark"; tag_name = "Remark"; tag_value = chr.getValueString(tag_name);
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
        postgres_table = "int_paper"; tag_name = "Pub"; tag_value = "";
        if ( chr.hasValue(tag_name) ) { tag_value = chr.getTerm(tag_name).getID(); }
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
//System.out.println("paper tag_value "+tag_value+" for postgres");
        postgres_table = "int_curator"; tag_name = "Curator"; tag_value = "";
        if ( chr.hasValue(tag_name) ) { tag_value = chr.getTerm(tag_name).getID(); }
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
        postgres_table = "int_person"; tag_name = "Person"; 
        updateListField(c, s, joinkey, postgres_table, tag_name, chr);
        postgres_table = "int_otherevi"; tag_name = "Other Evidence"; tag_value = chr.getValueString(tag_name);
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
        postgres_table = "int_treatment"; tag_name = "Treatment"; tag_value = chr.getValueString(tag_name);
        updateNormalField(c, s, joinkey, postgres_table, tag_name, tag_value);
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
    // dont HAVE to use CharFieldEnum but it does enforce using same strings across different data adapters which is good to enforce  the worm config needs to have "Pub" and "Interaction ID"
    queryableFields.add(CharFieldEnum.PUB.getName()); // "Pub"
    queryableFields.add("Interaction ID"); // "Interaction ID"
    queryableFields.add("Remark"); // "Interaction ID"
    queryableFields.add("Phenotype"); // "Phenotype"
    queryableFields.add("Effector"); // "Effector"
    queryableFields.add("Effected"); // "Effected"
    queryableFields.add("tor Variation"); // "tor Variation"
    queryableFields.add("ted Variation"); // "ted Variation"
    // queryableFields.add(CharFieldEnum.PHENOTYPE.getName()); // "Phenotype"	not a CharFieldEnum, what is it  2008 06 19
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
      errorPopup("connectToDB failure");
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
    try { rs = s.executeQuery("SELECT * FROM "+postgres_table+" WHERE joinkey = '"+joinkey+"' ORDER BY int_timestamp"); }
    catch (SQLException se) {
      System.out.println("We got an exception while executing our "+postgres_table+" joinkey: that probably means our term SQL is invalid"); se.printStackTrace(); errorPopup("queryPostgresCharacter SELECT"); }
    try { while (rs.next()) { default_value = rs.getString(2); } }		// assign the new term value
    catch (SQLException se) {
      System.out.println("We got an exception while getting a queryPostgresCharacter "+postgres_table+" result:this shouldn't happen: we've done something really bad."); se.printStackTrace(); errorPopup("queryPostgresCharacter rs.next()"); }
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
      String postgres_table = "int_name"; String postgres_value = ""; 			// postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
      c1.setValue("Interaction ID",postgres_value);					// assign the allele and the column

//      postgres_table = "int_effector"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_table = "int_geneone"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
      if (postgres_value == "") { } else { c1.setValue("Effector",postgres_value); }					// assign the queried value
//      if (postgres_value == "") { } else { 
//        postgres_value = postgres_value.replaceAll("\\|", "\\\",\\\"");
//        postgres_value = "\""+postgres_value+"\"";
//        c1.setValue("Effector",postgres_value); }					// assign the queried value
//      postgres_table = "int_torvariation"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_table = "int_variationone"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
      if (postgres_value == "") { } else { c1.setValue("tor Variation",postgres_value); }					// assign the queried value
//      postgres_table = "int_tortransgene"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_table = "int_transgeneone"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
      if (postgres_value == "") { } else { c1.setValue("tor Transgene",postgres_value); }					// assign the queried value
//      postgres_table = "int_effected"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_table = "int_genetwo"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
      if (postgres_value == "") { } else { c1.setValue("Effected",postgres_value); }					// assign the queried value
//      if (postgres_value == "") { } else { 
//        postgres_value = postgres_value.replaceAll("\\|", "\\\",\\\"");
//        postgres_value = "\""+postgres_value+"\"";
//        c1.setValue("Effected",postgres_value); }					// assign the queried value
      postgres_table = "int_variationtwo"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
      if (postgres_value == "") { } else { c1.setValue("ted Variation",postgres_value); }					// assign the queried value
//      postgres_table = "int_tedtransgene"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_table = "int_transgenetwo"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
      if (postgres_value == "") { } else { c1.setValue("ted Transgene",postgres_value); }					// assign the queried value

      postgres_table = "int_geneextra"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
      if (postgres_value == "") { } else { postgres_value = postgres_value.replaceAll("\\|", "\\\",\\\""); postgres_value = "\""+postgres_value+"\""; c1.setValue("Gene Extra",postgres_value); }
      postgres_table = "int_variationextra"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
      if (postgres_value == "") { } else { c1.setValue("Variation Extra",postgres_value); }					// assign the queried value
      postgres_table = "int_transgeneextra"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
      if (postgres_value == "") { } else { c1.setValue("Transgene Extra",postgres_value); }					// assign the queried value

      postgres_table = "int_type"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
      if (postgres_value == "") { } else { c1.setValue("Type",postgres_value); }					// assign the queried value

      postgres_value = ""; 								// postgres_value = "No postgres value assigned";
      postgres_table = "int_phenotype";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
//      String phenotype_match = find("(WBPhenotype[0-9]*)", postgres_value);  	// Find a WBPhenotype followed by any amount of digits
//      if (phenotype_match != null) { postgres_value = phenotype_match; }		// query for this, otherwise keep the default value
//      if (postgres_value == "No postgres value assigned") { } else { c1.setValue("Phenotype",postgres_value); }					// assign the queried value
      if (postgres_value == "") { } else { 
        postgres_value = postgres_value.replaceAll("\\|", "\\\",\\\"");
        postgres_value = "\""+postgres_value+"\"";
//System.out.println("set Phenotype to "+postgres_value+" END");
        c1.setValue("Phenotype",postgres_value); }					// assign the queried value

      postgres_value = ""; 								// postgres_value = "No postgres value assigned";
      postgres_table = "int_rnai";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
      String rnai_match = find("(WBRNAi[0-9]*)", postgres_value);  	// Find a WBPhenotype followed by any amount of digits
      if (rnai_match != null) { postgres_value = rnai_match; }		// query for this, otherwise keep the default value
      if (postgres_value == "") { } else { c1.setValue("RNAi",postgres_value); }					// assign the queried value

      postgres_table = "int_remark"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
      if (postgres_value == "") { } else { c1.setValue("Remark",postgres_value); }					// assign the queried value

      postgres_table = "int_paper"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
      if (postgres_value == "") { } else { c1.setValue("Pub",postgres_value); }					// assign the queried value
//System.out.println("set Pub to "+postgres_value+" END");
      postgres_table = "int_person"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
      if (postgres_value == "") { } else { 
        postgres_value = postgres_value.replaceAll("\\|", "\\\",\\\"");
        postgres_value = "\""+postgres_value+"\"";
//System.out.println("set Person to "+postgres_value+" END");
        c1.setValue("Person",postgres_value); }				// assign the queried value
//System.out.println("set Person to "+postgres_value+" END");
      postgres_table = "int_curator"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
      if (postgres_value == "") { } else { c1.setValue("Curator",postgres_value); }					// assign the queried value
//System.out.println("set Curator to "+postgres_value+" END");
      postgres_table = "int_otherevi"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
      if (postgres_value == "") { } else { c1.setValue("Other Evidence",postgres_value); }					// assign the queried value
      postgres_table = "int_treatment"; postgres_value = ""; // postgres_value = "No postgres value assigned";
      postgres_value = queryPostgresCharacter(s, postgres_table, postgres_value, joinkey);
      if (postgres_value == "") { } else { c1.setValue("Treatment",postgres_value); }					// assign the queried value

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

    String nameString = "Interaction ID"; String pubString = "Pub"; String remString = "Remark"; String phenString = "Phenotype";
//    String effectorString = "Effector"; String effectedString = "Effected"; String tedvString = "ted Variation"; String torvString = "tor Variation";
    String geneoneString = "Effector"; String genetwoString = "Effected"; String vtwoString = "ted Variation"; String voneString = "tor Variation";

    CharacterListI charList = new CharacterList();	// create the CharacterList that we will return

    Connection c = connectToDB();
    Statement s = null;
    try { s = c.createStatement(); }
      catch (SQLException se) { System.out.println("We got an exception while creating a statement: that probably means we're no longer connected."); se.printStackTrace(); errorPopup("query cannot create statement"); }

    ResultSet rs = null;	// intialize postgres query result
    List<String> joinkeys = new ArrayList<String>(2);
    int foundResults = 0;

    if (field.equals(nameString)) {			// if querying the name, get name data
      try { rs = s.executeQuery("SELECT * FROM int_name WHERE int_name ~ '"+query+"' ORDER BY joinkey"); }	// find the substring name that matches the queried name  
//      catch (SQLException se) { System.out.println("Exception while executing int_name nameString "+query+" query: that probably means our SQL is invalid"); se.printStackTrace(); System.exit(1); }
      catch (SQLException se) { System.out.println("Exception while executing int_name nameString "+query+" query: that probably means our SQL is invalid"); se.printStackTrace(); errorPopup("query int_name"); }
    } else if (field.equals(remString)) {						// if querying the remark, get paper data
      try { rs = s.executeQuery("SELECT * FROM int_remark WHERE int_remark ~ '"+query+"' ORDER BY joinkey"); }	// find the name that matches the queried remark
      catch (SQLException se) { System.out.println("Exception while executing int_remark remString "+query+" query: that probably means our SQL is invalid"); se.printStackTrace(); errorPopup("query int_remark"); }
//    } else if (field.equals(effectorString)) {						// if querying the effector, get paper data
//      try { rs = s.executeQuery("SELECT * FROM int_effector WHERE int_effector ~ '"+query+"' ORDER BY joinkey"); }	// find the name that matches the queried effector
//      catch (SQLException se) { System.out.println("Exception while executing int_effector effectorString "+query+" query: that probably means our SQL is invalid"); se.printStackTrace(); errorPopup("query int_effector"); }
    } else if (field.equals(geneoneString)) {						// if querying the geneone, get paper data
      try { rs = s.executeQuery("SELECT * FROM int_geneone WHERE int_geneone ~ '"+query+"' ORDER BY joinkey"); }	// find the name that matches the queried geneone
      catch (SQLException se) { System.out.println("Exception while executing int_geneone geneoneString "+query+" query: that probably means our SQL is invalid"); se.printStackTrace(); errorPopup("query int_geneone"); }
//    } else if (field.equals(effectedString)) {						// if querying the effected, get paper data
//      try { rs = s.executeQuery("SELECT * FROM int_effected WHERE int_effected ~ '"+query+"' ORDER BY joinkey"); }	// find the name that matches the queried effected
//      catch (SQLException se) { System.out.println("Exception while executing int_effected effectedString "+query+" query: that probably means our SQL is invalid"); se.printStackTrace(); errorPopup("query int_effected"); }
    } else if (field.equals(genetwoString)) {						// if querying the genetwo, get paper data
      try { rs = s.executeQuery("SELECT * FROM int_genetwo WHERE int_genetwo ~ '"+query+"' ORDER BY joinkey"); }	// find the name that matches the queried genetwo
      catch (SQLException se) { System.out.println("Exception while executing int_genetwo genetwoString "+query+" query: that probably means our SQL is invalid"); se.printStackTrace(); errorPopup("query int_genetwo"); }
//    } else if (field.equals(torvString)) {						// if querying the torvariation, get paper data
//      try { rs = s.executeQuery("SELECT * FROM int_torvariation WHERE int_torvariation ~ '"+query+"' ORDER BY joinkey"); }	// find the name that matches the queried torvariation
//      catch (SQLException se) { System.out.println("Exception while executing int_torvariation torvString "+query+" query: that probably means our SQL is invalid"); se.printStackTrace(); errorPopup("query int_torvariation"); }
    } else if (field.equals(voneString)) {						// if querying the variationone, get paper data
      try { rs = s.executeQuery("SELECT * FROM int_variationone WHERE int_variationone ~ '"+query+"' ORDER BY joinkey"); }	// find the name that matches the queried variationone
      catch (SQLException se) { System.out.println("Exception while executing int_variationone voneString "+query+" query: that probably means our SQL is invalid"); se.printStackTrace(); errorPopup("query int_variationone"); }
//    } else if (field.equals(tedvString)) {						// if querying the tedvariation, get paper data
//      try { rs = s.executeQuery("SELECT * FROM int_tedvariation WHERE int_tedvariation ~ '"+query+"' ORDER BY joinkey"); }	// find the name that matches the queried tedvariation
//      catch (SQLException se) { System.out.println("Exception while executing int_tedvariation tedvString "+query+" query: that probably means our SQL is invalid"); se.printStackTrace(); errorPopup("query int_tedvariation"); }
    } else if (field.equals(vtwoString)) {						// if querying the variationtwo, get paper data
      try { rs = s.executeQuery("SELECT * FROM int_variationtwo WHERE int_variationtwo ~ '"+query+"' ORDER BY joinkey"); }	// find the name that matches the queried variationtwo
      catch (SQLException se) { System.out.println("Exception while executing int_variationtwo vtwoString "+query+" query: that probably means our SQL is invalid"); se.printStackTrace(); errorPopup("query int_variationtwo"); }
    } else if (field.equals(pubString)) {						// if querying the publication, get paper data
      try { rs = s.executeQuery("SELECT * FROM int_paper WHERE int_paper ~ '"+query+"' ORDER BY joinkey"); }	// find the name that matches the queried name
      catch (SQLException se) { System.out.println("Exception while executing int_paper pubString "+query+" query: that probably means our SQL is invalid"); se.printStackTrace(); errorPopup("query int_paper"); }
    } else if (field.equals(phenString)) {						// if querying the phenotype, get paper data
      try { rs = s.executeQuery("SELECT * FROM int_phenotype WHERE int_phenotype ~ '"+query+"' ORDER BY joinkey"); }	// find the name that matches the queried name
      catch (SQLException se) { System.out.println("Exception while executing int_phenotype phenString "+query+" query: that probably means our SQL is invalid"); se.printStackTrace(); errorPopup("query int_phenotype"); }
//      String tag_name = "Phenotype"; StringBuilder sb = new StringBuilder(); Integer loopcount = 0;
//      CharFieldValue list = chr.getValue(chr.getCharFieldForName(tag_name));
//      List<CharFieldValue> valList = list.getCharFieldValueList();
//      for (CharFieldValue kid : valList) { String kidId = kid.getID(); loopcount++; if (loopcount > 1) { sb.append("' OR int_phenotype ~'"); } sb.append(kidId); }
//      if (sb == null) { System.out.println("No input phenotypes found in list for phenString query"); }
//      else {
//        query = sb.toString(); 
//        try { rs = s.executeQuery("SELECT * FROM int_phenotype WHERE int_phenotype ~ '"+query+"' ORDER BY joinkey"); 
//              System.out.println("SELECT * FROM int_phenotype WHERE int_phenotype ~ '"+query+"' ORDER BY joinkey"); }	// find the name that matches the queried phenotype
//      catch (SQLException se) { System.out.println("Exception while executing int_phenotype phenString "+query+" query: that probably means our SQL is invalid"); se.printStackTrace(); System.exit(1); } }
    } else {
      throw new DataAdapterEx("Worm query of "+query+" of field "+field+" failed");
    }

    try {
      while (rs.next()) {
        joinkeys.add(rs.getString(1));			
        foundResults++; } }
    catch (SQLException se) {
      System.out.println("We got an exception while getting a query catch while rs.next tempname joinkey "+query+" result:this shouldn't happen: we've done something really bad."); 
      se.printStackTrace(); errorPopup("query cannot get values"); }

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

//  private String queryPostgresPapAll(Statement s, String postgres_table, String joinkey) {
//    StringBuilder sb = new StringBuilder();
//    ResultSet rs = null;	// intialize postgres query result
////    System.out.println("SELECT * FROM "+postgres_table+"_hst WHERE joinkey = '"+joinkey+"' ORDER BY app_timestamp"); 
//    try { rs = s.executeQuery("SELECT * FROM "+postgres_table+"_hst WHERE joinkey = '"+joinkey+"' ORDER BY app_timestamp"); }
//    catch (SQLException se) {
//      System.out.println("We got an exception while executing our "+postgres_table+"_hst query: that probably means our term SQL is invalid"); se.printStackTrace(); System.exit(1); }
//    try { while (rs.next()) { sb.append(rs.getString(2)).append(" -- "); } }		// append the new term value
//    catch (SQLException se) {
//      System.out.println("We got an exception while getting a queryPostgresPapAll"+postgres_table+"_hst result:this shouldn't happen: we've done something really bad."); se.printStackTrace(); System.exit(1); } 
//    String pap_hist = sb.toString();
////    System.out.println("pap_hist "+pap_hist+" is not null"); 
////    if (pap_hist == null) { pap_hist = "postgres value is null"; }
////    if (pap_hist == "") { pap_hist = "postgres value is blank"; }
//    if (pap_hist == null) { pap_hist = ""; }
//    return pap_hist; 
//  }
//
//  private String queryPostgresPap(Statement s, String postgres_table, String joinkey) {
//    String pap_latest = "";
//    ResultSet rs = null;	// intialize postgres query result
////    System.out.println("SELECT * FROM "+postgres_table+"_hst WHERE joinkey = '"+joinkey+"' ORDER BY app_timestamp"); 
//    try { rs = s.executeQuery("SELECT * FROM "+postgres_table+"_hst WHERE joinkey = '"+joinkey+"' ORDER BY app_timestamp"); }
//    catch (SQLException se) {
//      System.out.println("We got an exception while executing our "+postgres_table+"_hst query: that probably means our term SQL is invalid"); se.printStackTrace(); System.exit(1); }
//    try { while (rs.next()) { pap_latest = rs.getString(2); } }		// append the new term value
//    catch (SQLException se) {
//      System.out.println("We got an exception while getting a queryPostgresPap"+postgres_table+"_hst result:this shouldn't happen: we've done something really bad."); se.printStackTrace(); System.exit(1); } 
////System.out.println("pap_latest "+pap_latest+" for joinkey "+joinkey+" end");
//    if (pap_latest == null) { pap_latest = ""; }
//    return pap_latest; 
//  }

//  private String queryPostgresCharacterDate(Statement s, String postgres_table, String default_value, String joinkey) {
//    ResultSet rs = null;	// intialize postgres query result
//    try { rs = s.executeQuery("SELECT * FROM "+postgres_table+" WHERE joinkey = '"+joinkey+"' ORDER BY app_timestamp"); }
//    catch (SQLException se) {
//      System.out.println("We got an exception while executing our "+postgres_table+" query: that probably means our term SQL is invalid"); se.printStackTrace(); System.exit(1); }
//    try { while (rs.next()) { default_value = rs.getString(3); } }		// assign the new term value
//    catch (SQLException se) {
//      System.out.println("We got an exception while getting a queryPostgresCharacterDate "+postgres_table+" result:this shouldn't happen: we've done something really bad."); se.printStackTrace(); System.exit(1); }
////    if (default_value == null) { default_value = "postgres value is null"; }
////    if (default_value == "") { default_value = "postgres value is blank"; }
//    if (default_value == null) { default_value = ""; }
//    String date_match = find("([0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9] [0-9][0-9]:[0-9][0-9])", default_value);		// Find a WBPaper followed by any amount of digits
//    if (date_match != null) { default_value = date_match; } 						// query for this, otherwise keep the default value
//    return default_value; 
//  }

//  private void insertPostgresHistVal(Connection c, Statement s, String postgres_table, String joinkey, String value) {
//    Integer hasMatch = 0;
//    ResultSet rs = null;
//    System.out.println("SELECT joinkey FROM "+postgres_table+"_hst WHERE joinkey = '"+joinkey+"' AND "+postgres_table+"_hst = '"+value+"' "); 
//    try { rs = s.executeQuery("SELECT joinkey FROM "+postgres_table+"_hst WHERE joinkey = '"+joinkey+"' AND "+postgres_table+"_hst = '"+value+"' "); }
//    catch (SQLException se) {
//      System.out.println("We got an exception while executing our "+postgres_table+" query: that probably means our column SQL is invalid"); se.printStackTrace(); System.exit(1); }
//    try { if (rs.next()) { hasMatch++; } }
//      // get the next highest number joinkey for that character
//    catch (SQLException se) {
//      System.out.println("We got an exception while getting a insertPostgresHistVal joinkey "+joinkey+" result:this shouldn't happen: we've done something really bad."); 
//      se.printStackTrace(); System.exit(1); }
//    if (hasMatch == 0) {
//      PreparedStatement ps = null;	// intialize postgres insert 
//      System.out.println("INSERT INTO "+postgres_table+"_hst VALUES "+joinkey+" and "+value+" end");
//      try { ps = c.prepareStatement("INSERT INTO "+postgres_table+"_hst VALUES (?, ?)"); ps.setString(1, joinkey); ps.setString(2, value); }
//      catch (SQLException se) {
//        System.out.println("We got an exception while preparing our insert: that probably means our SQL is invalid"); se.printStackTrace(); System.exit(1); }
//      try { ps.executeUpdate(); } 	// write to app_tempname, which is not what we really want, but we need to figure out the pubchunk thing to see what we're going to do
//      catch (SQLException se) { System.out.println("We got an exception while executing a history insert in insertPostgresVal table "+postgres_table+" joinkey "+joinkey+" value "+value+" : possibly bad SQL, or check the connection."); se.printStackTrace(); System.exit(1); } } }
    



