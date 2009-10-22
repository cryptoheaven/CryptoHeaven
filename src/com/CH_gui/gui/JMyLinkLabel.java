/*
 * Copyright 2001-2009 by CryptoHeaven Development Team,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Development Team ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Development Team.
 */

package com.CH_gui.gui;

import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import javax.swing.*;

import com.CH_co.gui.*;
import com.CH_co.util.*;

/**
 * <b>Copyright</b> &copy; 2001-2009
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>
 *
 * Class Description:
 *
 *
 * Class Details:
 *
 *
 * <b>$Revision: 1.7 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class JMyLinkLabel extends JMyLabel {

  private URL link;

  /** Creates new JMyLinkLabel */
  public JMyLinkLabel(String label, URL urlLink) {
    this(label, urlLink, "+0");
  }
  public JMyLinkLabel(String label, URL urlLink, String fontRelativeSize) {
    super("<html><body><font face='Arial, Verdana, Helvetica, sans-serif' size='"+fontRelativeSize+"'>"+(urlLink != null ? "<a href=\""+urlLink.toExternalForm()+"\">"+label+"</a>" : label)+"</font></body></html>");
    if (urlLink != null) {
      this.link = urlLink;
      setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      addMouseListener(new MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
          try {
            if (!e.isConsumed()) {
              BrowserLauncher.openURL(link.toExternalForm());
            }
          } catch (Throwable t) {
            MessageDialog.showErrorDialog(JMyLinkLabel.this, t.getMessage(), "URL error", true);
          }
        }
      });
    }
  }

  public void setText(String label) {
    setText(label, null, "+0");
  }
  public void setText(String label, URL urlLink) {
    setText(label, urlLink, "+0");
  }
  public void setText(String label, URL urlLink, String fontRelativeSize) {
    this.link = urlLink;
    super.setText("<html><body><font face='Arial, Verdana, Helvetica, sans-serif' size='"+fontRelativeSize+"'>"+(urlLink != null ? "<a href=\""+urlLink.toExternalForm()+"\">"+label+"</a>" : label)+"</font></body></html>");
  }
  public void setText(String label, URL urlLink, String fontRelativeSize, String bgColor) {
    this.link = urlLink;
    super.setText("<html><body><font face='Arial, Verdana, Helvetica, sans-serif' bgcolor='"+bgColor+"' size='"+fontRelativeSize+"'>"+(urlLink != null ? "<a href=\""+urlLink.toExternalForm()+"\">"+label+"</a>" : label)+"</font></body></html>");
  }

  public URL getLink() {
    return link;
  }

}