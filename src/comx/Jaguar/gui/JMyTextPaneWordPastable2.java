/*
 * Copyright 2001-2011 by CryptoHeaven Development Team,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Development Team ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Development Team.
 */

package comx.Jaguar.gui;

import com.CH_co.util.HTML_utils;
import javax.swing.text.*;
import javax.swing.text.html.*;

import com.CH_guiLib.gui.JMyTextPaneWordPastable;

/**
 * <b>Copyright</b> &copy; 2001-2011
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
 * <b>$Revision: 1.1 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class JMyTextPaneWordPastable2 extends JMyTextPaneWordPastable {

  public void setText(String s) {
    super.setText(s);

    // fix the default font in the BODY tag
    Document doc = getDocument();
    if (doc instanceof MutableHTMLDocument) {
      MutableHTMLDocument mHtmlDoc = (MutableHTMLDocument) doc;
      Element elem = mHtmlDoc.getElementByTag(HTML.Tag.BODY);
      if (elem != null) {
        AttributeSet attribs = elem.getAttributes();
        java.util.Enumeration enm = attribs.getAttributeNames();
        boolean faceFound = false;
        while (enm.hasMoreElements()) {
          if ((""+enm.nextElement()).equalsIgnoreCase("face")) {
            faceFound = true;
            break;
          }
        }
        if (!faceFound) {
          SimpleAttributeSet as = new SimpleAttributeSet();
          as.addAttribute("face", HTML_utils.DEFAULT_FONTS);
          mHtmlDoc.addAttributes(elem, as);
        }
      }
    }
  }
}