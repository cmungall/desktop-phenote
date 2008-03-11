package phenote.edit;


public class AutoAnnotIdGenerator {

  private static int currentIdNumber = 1;

  public static String getNewId() {
    String id = "_:"+currentIdNumber;
    currentIdNumber++;
    return id;
  }

}
