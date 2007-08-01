package phenote.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;

public class GuiUtil {


  public static void doBlinker(Component c) {
    GuiUtil g = new GuiUtil(); // is this silly?
    g.doBlinkerInst(c); // ??
  }

  private void doBlinkerInst(Component c) {
    Blinker b = new Blinker(c);
    Timer timer = new Timer(300,b); // 300 ms??
    b.setTimer(timer);
    timer.start();
  }
 
  private class Blinker implements ActionListener {
    private int counter = 0;
    private Timer timer;

    private Component comp;
    private Blinker(Component c) { comp = c; } 

    public void actionPerformed(ActionEvent e) {
      Color c = Color.RED;
      if (++counter % 2 == 0) c = Color.WHITE; 
      comp.setBackground(c);
      if (counter == 4) timer.stop();
    }
    private void setTimer(Timer t) { timer = t; }
  }

}
