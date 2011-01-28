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

package com.CH_gui.gui;

import com.CH_gui.util.HTML_ClickablePane;
import com.CH_co.util.URLs;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.net.URL;
import java.util.HashMap;
import javax.swing.JComponent;
import javax.swing.JPanel;

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
 * <b>$Revision: 1.20 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class Template extends Object {

  public static final int BACK_CONTACTS;
  public static final int BACK_GROUP;
  public static final int BACK_FILES;
  public static final int CATEGORY_MAIL;
  public static final int CATEGORY_FILE;
  public static final int CATEGORY_CHAT;
  public static final int CATEGORY_GROUP;
  public static final int EMPTY_ADDRESSES;
  public static final int EMPTY_WHITELIST;
  public static final int EMPTY_CHAT;
  public static final int EMPTY_CONTACTS;
  public static final int EMPTY_FILES;
  public static final int EMPTY_GROUP;
  public static final int EMPTY_MAIL;
  public static final int EMPTY_MAIL_SENT;
  public static final int EMPTY_MAIL_SPAM;
  public static final int EMPTY_MAIL_DRAFTS;
  public static final int EMPTY_POSTINGS;
  public static final int EMPTY_RECYCLE;
  public static final int EMPTY_USER_ACCOUNTS;
  public static final int FILTER_NO_RESULTS;
  public static final int KEY_GEN;
  public static final int NONE;

  public static String[] templates;

  public static final int SHOW_DELAY_FOR_FOLDER_TABLES = 500; // 0.5 sec.
  public static final int SHOW_DELAY_FOR_NON_FOLDER_TABLES = 30000; // 30 sec.


  static {
    int i = 0;
    templates = new String[23]; 

    BACK_CONTACTS = i; 
    templates[i] = "back-contacts.html"; i++;

    BACK_GROUP = i; 
    templates[i] = "back-group.html"; i++;

    BACK_FILES = i; 
    templates[i] = "back-files.html"; i++;

    CATEGORY_MAIL = i;
    templates[i] = "category-mail.html"; i++;

    CATEGORY_FILE = i;
    templates[i] = "category-file.html"; i++;

    CATEGORY_CHAT = i;
    templates[i] = "category-chat.html"; i++;

    CATEGORY_GROUP = i;
    templates[i] = "category-group.html"; i++;

    EMPTY_ADDRESSES = i; 
    templates[i] = "empty-addresses.html"; i++;

    EMPTY_WHITELIST = i; 
    templates[i] = "empty-whitelist.html"; i++;

    EMPTY_CHAT = i; 
    templates[i] = "empty-chat.html"; i++;

    EMPTY_CONTACTS = i; 
    templates[i] = "empty-contacts.html"; i++;

    EMPTY_GROUP = i; 
    templates[i] = "empty-group.html"; i++;

    EMPTY_FILES = i; 
    templates[i] = "empty-files.html"; i++;

    EMPTY_MAIL = i; 
    templates[i] = "empty-mail.html"; i++;

    EMPTY_MAIL_SENT = i; 
    templates[i] = "empty-mail-sent.html"; i++;

    EMPTY_MAIL_SPAM = i; 
    templates[i] = "empty-mail-spam.html"; i++;

    EMPTY_MAIL_DRAFTS = i; 
    templates[i] = "empty-mail-drafts.html"; i++;

    EMPTY_POSTINGS = i; 
    templates[i] = "empty-postings.html"; i++;

    EMPTY_RECYCLE = i; 
    templates[i] = "empty-recycle.html"; i++;

    EMPTY_USER_ACCOUNTS = i; 
    templates[i] = "empty-users"; i++; // NULL - not implemented - no such file

    FILTER_NO_RESULTS = i;
    templates[i] = "filter-no-results.html"; i++;

    KEY_GEN = i; 
    templates[i] = "key-gen.html"; i++;

    NONE = i; 
    templates[i] = "none"; // NULL - not implemented - no such file

  }


  /** Creates new Template */
  private Template() { // only static get methods
  }


  public static String get(int templateIndex) {
    // see if we have any replacement templates from PrivateLabel
    HashMap replacementTemplatesHM = URLs.replacementTemplatesHM;
    if (replacementTemplatesHM != null && replacementTemplatesHM.size() > 0) {
      for (int i=0; i<templates.length; i++) {
        String replacement = (String) replacementTemplatesHM.get(templates[i]);
        if (replacement != null)
          templates[i] = replacement;
      }
      // after applying the replacements, nullify them...
      URLs.replacementTemplatesHM = null;
    }
    return templates[templateIndex];
  }

  public static JComponent getTemplate(int templateIndex) {
    return getTemplate(get(templateIndex));
  }
  public static JComponent getTemplate(String templateName) {
    JComponent jTemplate = null;
    JComponent pane = null;
    if (templateName != null && templateName.length() > 0 && !templateName.equalsIgnoreCase("none")) {
      try {
        URL location = null;
        // If customized version and image is in a URL format, then load directly from URL
        if (URLs.hasPrivateLabelCustomization() && templateName.indexOf("://") >= 0) {
          location = new URL(templateName);
        } else {
          String fileName = "templates/" + templateName;
          location = URLs.getResourceURL(fileName);
        }
        HTML_ClickablePane htmlPane = HTML_ClickablePane.createNewAndLoading(location);
        htmlPane.setRegisteredLocalLauncher(new URLLauncherCHACTION(), URLLauncherCHACTION.ACTION_PATH);
        pane = htmlPane;
      } catch (Throwable t) {
      }
    }
    // back templates align bottom-right
    if (pane != null) {
      if (templateName.indexOf("back-") >= 0) {
        JPanel pTop = new JPanel();
        JPanel pLeft = new JPanel();
        pTop.setBackground(Color.white);
        pLeft.setBackground(Color.white);
        JPanel panel = new JPanel(new GridBagLayout());
        panel.add(pTop, new GridBagConstraints(0, 0, 2, 1, 10, 10, 
            GridBagConstraints.CENTER, GridBagConstraints.BOTH, new MyInsets(0, 0, 0, 0), 0, 0));
        panel.add(pLeft, new GridBagConstraints(0, 1, 1, 1, 10, 0, 
            GridBagConstraints.CENTER, GridBagConstraints.BOTH, new MyInsets(0, 0, 0, 0), 0, 0));
        panel.add(pane, new GridBagConstraints(1, 1, 1, 1, 0, 0, 
            GridBagConstraints.CENTER, GridBagConstraints.BOTH, new MyInsets(0, 0, 0, 0), 0, 0));
        jTemplate = panel;
      } else {
        jTemplate = pane;
      }
    }
    return jTemplate;
  }
}