/*
 * Copyright 2001-2010 by CryptoHeaven Development Team,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Development Team ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Development Team.
 */
package com.CH_co.util;

import com.CH_co.trace.Trace;
import javax.swing.text.html.*;
import javax.swing.text.*;

/**
 * <b>Copyright</b> &copy; 2001-2010
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
 * <b>$Revision: 1.24 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class HTML_EditorKit extends HTMLEditorKit {

  public ViewFactory getViewFactory() {
    return new HTMLFactoryX();
  }

  public class HTMLFactoryX extends HTMLFactory implements ViewFactory {
    public View create(Element elem) {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(HTMLFactoryX.class, "create(Element elem)");
      View view = null;
      //try { Thread.currentThread().sleep(50); } catch (Throwable t) {}
      Object o = elem.getAttributes().getAttribute(StyleConstants.NameAttribute);
      if (o instanceof HTML.Tag) {
        HTML.Tag kind = (HTML.Tag) o;
        if (kind == HTML.Tag.IMG) {
          try {
            if (trace != null) trace.data(10, "Creating our HTML_ImageView");
            view = new HTML_ImageView(elem);
          } catch (Throwable t) {
            if (trace != null) trace.exception(HTMLFactoryX.class, 100, t);
          }
        }
      }
      if (view == null) {
        //view = HTML_EditorKit.super.getViewFactory().create(elem);
        view = super.create(elem);
      }
      if (trace != null) trace.exit(HTMLFactoryX.class, view);
      return view;
    }
  }
}