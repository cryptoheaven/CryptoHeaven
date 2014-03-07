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

import com.CH_co.trace.Trace;

import java.awt.*;
import java.awt.geom.*;

/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.3 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class JMyLinkLikeLabel extends JMyLabel {

  private Object description;

  /** Creates new JMyLinkLikeLabel */
  public JMyLinkLikeLabel(String label) {
    this(label, 0);
  }

  /** Creates new JMyLinkLikeLabel */
  public JMyLinkLikeLabel(String label, int fontRelativeSize) {
    super(label, (float) fontRelativeSize);
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(JMyLinkLikeLabel.class, "JMyLinkLikeLabel()");
    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    setForeground(Color.blue);
    setText(label);
    if (trace != null) trace.exit(JMyLinkLikeLabel.class);
  }

  public void setDescription(Object o) {
    description = o;
  }

  public Object getDescription() {
    return description;
  }

  public void setText(String text) {
    setOpaque(false);
    super.setText(text);
  }

  public void setText(String text, Color bg) {
    setOpaque(true);
    setBackground(bg);
    super.setText(text);
  }

  public void paint(Graphics g) {
    super.paint(g);
    //if (drawUnderline) {
      Color underline = getForeground();

      // really all this size stuff below only needs to be recalculated if font or text changes
      Rectangle2D textBounds =  getFontMetrics(getFont()).getStringBounds(getText(), g);

      Insets i = getInsets();
      //this layout stuff assumes the icon is to the left, or null
      int y = (i != null ? i.top : 0) + getHeight()/2 + (int)(textBounds.getHeight()/2);
      int w = (int)textBounds.getWidth();
      int x = (i != null ? i.left : 0) + (getIcon() == null ? 0 : getIcon().getIconWidth() + getIconTextGap());

      g.setColor(underline);
      g.drawLine(x, y, x + w, y);
    //}
  }

}