package phenote.gui;

import java.awt.GridBagConstraints;
import java.awt.Insets;

/** Convenience methods for making GridBagConstraints for GridBagLayout */

public class GridBagUtil {

  public static GridBagConstraints makeConstraint(int x,int y,int horizPad,int vertPad) {
    return makeConstraint(x,y,horizPad,vertPad,GridBagConstraints.WEST);
  }
  static GridBagConstraints makeConstraint(int x, int y, int horizPad, int vertPad,
                                           int anchor) {
    return makeConstraint(x,y,1,1,0,horizPad,vertPad,0,anchor);
  }

  public static GridBagConstraints makeWidthConstraint(int x, int y, int horPad,
                                                int verPad, int width) {
    return makeConstraint(x,y,width,1,0,horPad,verPad,0,GridBagConstraints.WEST);
  }

  private static GridBagConstraints makeConstraint(int x, int y, int width,
                                           int height, double weighty,
                                           int horizPad, int vertPad,
                                           int fill, int anchor) {
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = x;
    gbc.gridy = y;
    gbc.gridwidth = width;
    gbc.gridheight = height;
    gbc.weightx = 1.0;
    gbc.weighty = weighty;
    gbc.anchor = anchor;
    gbc.fill = fill;
    gbc.insets = new Insets(vertPad, horizPad, vertPad, horizPad);
    return gbc;
  }
  

}
