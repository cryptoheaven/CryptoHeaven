/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.gui;

import com.CH_co.util.BrowserLauncher;
import com.CH_co.util.HTML_utils;
import com.CH_gui.util.MessageDialog;
import com.CH_gui.util.URLLauncher;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

/**
* Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
*
* <b>$Revision: 1.7 $</b>
*
* @author  Marcin Kurzawa
*/
public class JMyLinkLabel extends JMyLabel {

  private URL link;
  private URLLauncher launcher;

  /** Creates new JMyLinkLabel */
  public JMyLinkLabel(String label, URL urlLink) {
    this(label, urlLink, "+0");
  }
  public JMyLinkLabel(String label, URL urlLink, URLLauncher customLauncher) {
    this(label, urlLink, "+0", customLauncher);
  }
  public JMyLinkLabel(String label, URL urlLink, String fontRelativeSize) {
    this(label, urlLink, fontRelativeSize, null);
  }
  public JMyLinkLabel(String label, URL urlLink, String fontRelativeSize, URLLauncher customLauncher) {
    super("<html><body><font face="+HTML_utils.DEFAULT_FONTS_QUOTED+" size='"+fontRelativeSize+"'>"+(urlLink != null ? "<a href=\""+urlLink.toExternalForm()+"\">"+label+"</a>" : label)+"</font></body></html>");
    if (urlLink != null) {
      this.link = urlLink;
      this.launcher = customLauncher;
      setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      addMouseListener(new MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
          try {
            if (!e.isConsumed()) {
              if (launcher != null) {
                launcher.openURL(new URL(link.toExternalForm()), JMyLinkLabel.this);
              } else {
                BrowserLauncher.openURL(link.toExternalForm());
              }
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
    super.setText("<html><body><font face="+HTML_utils.DEFAULT_FONTS_QUOTED+" size='"+fontRelativeSize+"'>"+(urlLink != null ? "<a href=\""+urlLink.toExternalForm()+"\">"+label+"</a>" : label)+"</font></body></html>");
  }
  public void setText(String label, URL urlLink, String fontRelativeSize, String bgColor) {
    this.link = urlLink;
    super.setText("<html><body><font face="+HTML_utils.DEFAULT_FONTS_QUOTED+" bgcolor='"+bgColor+"' size='"+fontRelativeSize+"'>"+(urlLink != null ? "<a href=\""+urlLink.toExternalForm()+"\">"+label+"</a>" : label)+"</font></body></html>");
  }

  public URL getLink() {
    return link;
  }

}