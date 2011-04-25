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
package com.CH_gui.util;

import com.CH_co.trace.Trace;
import com.CH_co.util.CallbackI;

import java.util.Enumeration;
import javax.swing.text.html.*;
import javax.swing.text.*;

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
 * <b>$Revision: 1.24 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class HTML_EditorKit extends HTMLEditorKit {

  private boolean alwaysUseCustomImageView = false;
  private boolean isDisplayRemoteImages = false;
  private CallbackI remoteImageBlockedCallback;

  public HTML_EditorKit() {
    super();
  }
  public HTML_EditorKit(boolean alwaysUseCustomImageView) {
    super();
    this.alwaysUseCustomImageView = alwaysUseCustomImageView;
  }

  public void setDisplayRemoteImages(boolean enable) {
    isDisplayRemoteImages = enable;
  }

  public void registerRemoteImageBlockedCallback(CallbackI remoteImageBlockedCallback) {
    this.remoteImageBlockedCallback = remoteImageBlockedCallback;
  }

  public ViewFactory getViewFactory() {
    return new HTMLFactoryX();
  }

  public class HTMLFactoryX extends HTMLFactory implements ViewFactory {
    public View create(Element elem) {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(HTMLFactoryX.class, "create(Element elem)");
      View view = null;
      Object o = elem.getAttributes().getAttribute(StyleConstants.NameAttribute);
      if (o instanceof HTML.Tag) {
        HTML.Tag kind = (HTML.Tag) o;
        if (kind == HTML.Tag.IMG) {
          try {
            if (trace != null) trace.data(10, "Creating our HTML_ImageView");
            if (alwaysUseCustomImageView) {
              view = new HTML_ImageView(elem);
            } else {
              AttributeSet attribSet = elem.getAttributes();
              Enumeration enm = attribSet.getAttributeNames();
              while (enm.hasMoreElements()) {
                Object attribName = enm.nextElement();
                Object attrib = attribSet.getAttribute(attribName);
                if (attribName instanceof HTML.Attribute && ((HTML.Attribute) attribName).equals(HTML.Attribute.SRC)) {
                  if (attrib instanceof String) {
                    String aValue = (String) attrib;
                    boolean isLocalBlocked = false;
                    Boolean isRemoteBlocked = null;
                    if (aValue.startsWith("cid:"))
                      isLocalBlocked = true;
                    else if (aValue.startsWith("http://") || aValue.startsWith("https://")) {
                      if (isDisplayRemoteImages)
                        isRemoteBlocked = Boolean.FALSE;
                      else
                        isRemoteBlocked = Boolean.TRUE;
                    }
                    if (isRemoteBlocked != null && remoteImageBlockedCallback != null) {
                      remoteImageBlockedCallback.callback(isRemoteBlocked);
                    }
                    if (isLocalBlocked || (isRemoteBlocked != null && isRemoteBlocked.booleanValue())) {
                      view = new javax.swing.text.ParagraphView(elem);
                    }
                  }
                }
              }
            }
          } catch (Throwable t) {
            if (trace != null) trace.exception(HTMLFactoryX.class, 100, t);
          }
        }
      }
      if (view == null) {
        view = super.create(elem);
      }
      if (trace != null) trace.exit(HTMLFactoryX.class, view);
      return view;
    }
  }
}