/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.gui;

import com.CH_gui.util.MiscGui;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.IllegalComponentStateException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Locale;
import javax.accessibility.*;
import javax.swing.Icon;
import javax.swing.UIManager;

/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.4 $</b>
 *
 * @author Marcin Kurzawa
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

    public Locale getLocale() throws IllegalComponentStateException {
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

    private void readObject(ObjectInputStream objectinputstream) throws ClassNotFoundException, IOException {
      objectinputstream.defaultReadObject();
    }

    private void writeObject(ObjectOutputStream objectoutputstream) throws IOException {
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

    int iHeight = getIconHeight();
    int iWidth = getIconWidth();

    int xCenter = iWidth / 2;
    int xLeft = xCenter - width / 2;
    int xRight = xLeft + width;
    int xMid = xLeft + width / 2;

    int yCenter = iHeight / 2;
    int yTop = yCenter - height / 2;
    int yBottom = yTop + height;

    g.translate(0, j);
    g.fillPolygon(new int[]{xLeft, xRight, xMid}, new int[]{yTop, yTop, yBottom}, 3);
    g.translate(0, -j);
  }

  public int getIconWidth() {
    return width + 2 * 3;
  }

  public int getIconHeight() {
//    int i = height;
//    //int i = diameter + 1;
//    return i <= 10 ? 10 : i;
    int i = height + 2 * 2;
    return i <= 10 ? 10 : i;
  }

  public AccessibleContext getAccessibleContext() {
    if (accessibleContext == null) {
      accessibleContext = new AccessibleEllipsisIcon();
    }
    return accessibleContext;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String s) {
    description = s;
  }
}