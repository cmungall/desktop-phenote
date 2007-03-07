package phenote.gui;

import java.awt.GridBagConstraints;
import java.awt.Insets;

/** Convenience methods for making GridBagConstraints for GridBagLayout */

public class GridBagUtil {

  public static GridBagConstraints makeConstraint(int x,int y) {
    return makeConstraint(x,y,3,3,GridBagConstraints.WEST);
  }

  public static GridBagConstraints makeConstraint(int x,int y,int horizPad,int vertPad) {
    return makeConstraint(x,y,horizPad,vertPad,GridBagConstraints.WEST);
  }

  public static GridBagConstraints makeAnchorConstraint(int x, int y, int anchor) {
    return makeConstraint(x,y,3,3,anchor);
  }

  // defaults wieghty to 0 and fill to 0 - not great defaults really
  // well actually weighty means dont expand which is what you want for fields
  static GridBagConstraints makeConstraint(int x, int y, int horizPad, int vertPad,
                                           int anchor) {
    double weightY = 0; // 1??
    return makeConstraint(x,y,1,1,weightY,horizPad,vertPad,0,anchor);
  }

  public static GridBagConstraints makeWidthConstraint(int x, int y, int horPad,
                                                int verPad, int width) {
    double weightY = 0; // 1????
    return makeConstraint(x,y,width,1,weightY,horPad,verPad,0,GridBagConstraints.WEST);
  }

  /** Fils up vert & hor with weight 1 */
  public static GridBagConstraints makeFillingConstraint(int x, int y) {
    double weightY = 1;
    int fill = GridBagConstraints.BOTH;
    int anchor = GridBagConstraints.WEST;
    int pad = 3; // presumptious???
    return makeConstraint(x,y,1,1,weightY,pad,pad,fill,anchor);
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
