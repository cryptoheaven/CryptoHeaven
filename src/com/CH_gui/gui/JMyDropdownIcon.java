/*
 * Copyright 2001-2013 by CryptoHeaven Corp.,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */

package com.CH_gui.gui;

import java.awt.*;
import java.io.*;
import java.util.Locale;
import javax.accessibility.*;
import javax.swing.Icon;
import javax.swing.UIManager;

import com.CH_gui.util.MiscGui;

/**
 * <b>Copyright</b> &copy; 2001-2013
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Corp.
 * </a><br>All rights reserved.<p>
 *
 * Class Description:
 *
 *
 * Class Details:
 *
 *
 * <b>$Revision: 1.4 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class JMyDropdownIcon implements Icon, Serializable, Accessible {

  protected class AccessibleEllipsisIcon extends AccessibleContext implements AccessibleIcon, Serializable {


    public AccessibleRole getAccessibleRole() {
      return AccessibleRole.ICON;
    }

    public AccessibleStateSet getAccessibleStateSet() {
      return null;
    }

    public Accessible getAccessibleParent() {
      return null;
    }

    public int getAccessibleIndexInParent() {
      return -1;
    }

    public int getAccessibleChildrenCount() {
      return 0;
    }

    public Accessible getAccessibleChild(int i) {
      return null;
    }

    public Locale getLocale()
    throws IllegalComponentStateException {
      return null;
    }

    public String getAccessibleIconDescription() {
      return getDescription();
    }

    public void setAccessibleIconDescription(String s) {
      setDescription(s);
    }

    public int getAccessibleIconHeight() {
      return getIconHeight();
    }

    public int getAccessibleIconWidth() {
      return getIconWidth();
    }

    private void readObject(ObjectInputStream objectinputstream)
    throws ClassNotFoundException, IOException {
      objectinputstream.defaultReadObject();
    }

    private void writeObject(ObjectOutputStream objectoutputstream)
    throws IOException {
      objectoutputstream.defaultWriteObject();
    }

    protected AccessibleEllipsisIcon() {
    }
  }


  protected int width;
  protected int height;
  protected String description;
  protected AccessibleEllipsisIcon accessibleContext;

  public JMyDropdownIcon() {
    this(5, 3);
  }

  public JMyDropdownIcon(int width, int height) {
    accessibleContext = null;
    this.width = width;
    this.height = height;
    description = null;
  }

  public void paintIcon(Component component, Graphics g, int i, int j) {
    if (component.isEnabled()) {
      g.setColor(UIManager.getColor("Button.foreground"));
      drawArrow(g, i, j);
    } else {
      Color color = component.getBackground();
      g.setColor(color.brighter());
      drawArrow(g, i + 1, j + 1);
      g.setColor(color.darker());
      drawArrow(g, i, j);
    }
  }

  protected void drawArrow(Graphics g, int i, int j) {
    MiscGui.setPaintPrefs(g);

//    System.out.println("i="+i);
//    System.out.println("j="+j);

    int iHeight = getIconHeight();
    int iWidth = getIconWidth();

//    System.out.println("iHeight="+iHeight);
//    System.out.println("iWidth="+iWidth);

    int xCenter = iWidth/2;
    int xLeft = xCenter - width/2;
    int xRight = xLeft + width;
    int xMid = xLeft + width/2;

//    System.out.println("xCenter="+xCenter);
//    System.out.println("xLeft="+xLeft);
//    System.out.println("xRight="+xRight);
//    System.out.println("xMid="+xMid);

    int yCenter = iHeight/2;
    int yTop = yCenter - height/2;
    int yBottom = yTop + height;

//    System.out.println("yCenter="+yCenter);
//    System.out.println("yTop="+yTop);
//    System.out.println("yBottom="+yBottom);

    g.translate(0,  j);
    g.fillPolygon(new int[] { xLeft, xRight, xMid }, new int[] { yTop, yTop, yBottom }, 3);
    g.translate(0,  -j);
  }

  public int getIconWidth() {
    return width + 2*3;
  }

  public int getIconHeight() {
//    int i = height;
//    //int i = diameter + 1;
//    return i <= 10 ? 10 : i;
    int i = height + 2*2;
    return i <= 10 ? 10 : i;
  }

  public AccessibleContext getAccessibleContext() {
    if (accessibleContext == null)
      accessibleContext = new AccessibleEllipsisIcon();
    return accessibleContext;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String s) {
    description = s;
  }
}