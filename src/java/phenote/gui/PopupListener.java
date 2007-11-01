 package phenote.gui;
 
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
 
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import java.awt.Point;
 
 
public class PopupListener extends MouseAdapter {
    JPopupMenu popup;
    
    int col; int row;
    Point p;
    public PopupListener(JPopupMenu popupMenu) {
      popup = popupMenu;
    }

    public void mousePressed(MouseEvent e) {
//      super.mousePressed(e);
      maybeShowPopup(e);
    }

    public void mouseReleased(MouseEvent e) {
//      super.mouseReleased(e);
      maybeShowPopup(e);
    }

    private void maybeShowPopup(MouseEvent e) {
//      String m="";
//      col=e.getX();
//      row=e.getY();
//       System.out.println("col="+col+" row= "+row);
//      System.out.println("button="+e.getButton());
//      System.out.println(e.paramString());
//      System.out.println("popuptrigger="+e.isPopupTrigger());
//      if(e.getButton()==MouseEvent.BUTTON3) {
      if (e.isPopupTrigger()) {
//        m = "popuptrigger!";
//      	System.out.println("component="+e.getComponent());
        popup.show(e.getComponent(),
            e.getX(), e.getY());
      }
//      else {
////        m="no trigger, its "+e.paramString()+"!";
//      }
    }
}