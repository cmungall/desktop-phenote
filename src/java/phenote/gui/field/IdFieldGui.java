package phenote.gui.field;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

import phenote.datamodel.CharField;
import phenote.gui.selection.IDSelectionEvent;
import phenote.gui.selection.IDSelectionListener;

/** for now this is just a free text field that checks if theres a ":" present 
    eventually may get more savvy with option of prepending an id prefix and
    using OBOInstance instead of String */

//NEED TO ADD IN THE SELECTION MANAGER HERE AND selectID()

class IdFieldGui extends FreeTextField implements IDSelectionListener {

  private final static String ERR = "Input string must have a colon(':') for ID field";

  private InputVerifier idVerifier;//=new IdInputVerifier(); //init after getInVer!

  IdFieldGui(CharField cf) { 
  	super(cf); }

  protected boolean passesConstraints(String input) {
    //new Throwable().printStackTrace();
    //if (true) return true;
    return input.contains(":");
  }

  protected String getConstraintFailureMsg() {
    return ERR+" for field "+getLabel()+" value "+getText();
  }

  protected InputVerifier getInputVerifier() {
    if (idVerifier==null) idVerifier = new IdInputVerifier();
    return idVerifier;
  }

  private class IdInputVerifier extends InputVerifier {
    /** Calls verify(input) to ensure that the input is valid. */
    public boolean shouldYieldFocus(JComponent input) {
      if (!verify(input)) {
        fireErrorEvent(getConstraintFailureMsg());
//         try { SwingUtilities.invokeLater(new ErrMsgThread()); } // invokeAndWait
//         catch (Exception e) { log().error("err msg thread ex "+e); }
        // this causes row selection to not go through
        //JOptionPane.showMessageDialog(null,getConstraintFailureMsg(),"ID Input Error",
        //                            JOptionPane.ERROR_MESSAGE); 
      }
      return verify(input); // for now - should put up error message on failure
    }
    
    private class ErrMsgThread implements Runnable {
      public void run() {
        JOptionPane.showMessageDialog(null,getConstraintFailureMsg(),"ID Input Error",
                                      JOptionPane.ERROR_MESSAGE); 
      }
    }
    
    public  boolean verify(JComponent input) {
      //new Throwable().printStackTrace();
      //System.out.println("verify: "+getText()+" "+getText().contains(":"));
      if (getText() == null || getText().trim().equals("")) return true;
      return getText().contains(":");
    }
  }
  
  //add to compile 
  public void IDSelected(IDSelectionEvent e) {}


}
