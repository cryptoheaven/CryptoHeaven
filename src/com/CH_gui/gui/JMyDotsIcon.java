/*
 * Copyright 2001-2012 by CryptoHeaven Corp.,
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
 * <b>Copyright</b> &copy; 2001-2012
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
public class JMyDotsIcon implements Icon, Serializable, Accessible {

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


  protected int diameter;
  protected String description;
  protected AccessibleEllipsisIcon accessibleContext;

  public JMyDotsIcon() {
    this(2);
  }

  public JMyDotsIcon(int i) {
    accessibleContext = null;
    diameter = i;
    description = null;
  }

  public void paintIcon(Component component, Graphics g, int i, int j) {
    if (component.isEnabled()) {
      g.setColor(UIManager.getColor("Button.foreground"));
      drawDots(g, i, j);
    } else {
      Color color = component.getBackground();
      g.setColor(color.brighter());
      drawDots(g, i + 1, j + 1);
      g.setColor(color.darker());
      drawDots(g, i, j);
    }
  }

  protected void drawDots(Graphics g, int i, int j) {
    MiscGui.setPaintPrefs(g);
    int k = diameter + 1;
    int l = 3;
    int i1 = getIconHeight() / 2 - k / 2;
    g.translate(i, j);
    g.fillOval(l, i1, k, k);
    l += k + 2;
    g.fillOval(l, i1, k, k);
    l += k + 2;
    g.fillOval(l, i1, k, k);
    g.translate(-i, -j);
  }

  public int getIconWidth() {
    return (diameter + 1) * 3 + 9 + 2;
  }

  public int getIconHeight() {
    int i = diameter + 1;
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