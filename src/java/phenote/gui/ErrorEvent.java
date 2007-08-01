package phenote.gui;

import java.util.EventObject;

public class ErrorEvent extends EventObject {

  private String message;

  public ErrorEvent(Object source,String m) {
    super(source);
    message = m;
  }

  public String getMsg() { return message; }

}
