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

package com.CH_gui.dialog;

import com.CH_co.util.*;
import com.CH_co.trace.Trace;

import com.CH_gui.gui.*;
import com.CH_gui.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
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
 * <b>$Revision: 1.21 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class AboutSecurityDialog extends GeneralDialog implements VisualsSavable {

  private static final int DEFAULT_BUTTON_INDEX = 0;

  /** Creates new AboutSecurityDialog */
  public AboutSecurityDialog(Dialog parent) {
    super(parent, com.CH_gui.lang.Lang.rb.getString("title_About_Security"));
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AboutSecurityDialog.class, "AboutSecurityDialog(Dialog parent)");
    JButton[] buttons = createButtons();
    JPanel panel = createMainPanel();

    super.init(parent, buttons, panel, MiscGui.createLogoHeader(), DEFAULT_BUTTON_INDEX, DEFAULT_BUTTON_INDEX);

    if (trace != null) trace.exit(AboutSecurityDialog.class);
  }

  private JButton[] createButtons() {
    JButton[] buttons = new JButton[1];
    buttons[0] = new JMyButton(com.CH_gui.lang.Lang.rb.getString("button_Close"));
    buttons[0].setDefaultCapable(true);
    buttons[0].addActionListener(new OKActionListener());
    return buttons;
  }

  private JPanel createMainPanel() {
    JPanel panel = new JPanel();
    panel.setLayout(new GridBagLayout());

//    panel.add(new JMyLabel(Images.get(ImageNums.LOGO_BANNER_MAIN)), new GridBagConstraints(0, 0, 1, 1, 0, 0, 
//        GridBagConstraints.CENTER, GridBagConstraints.NONE, new MyInsets(0, 0, 5, 0), 0, 0));

    String aboutText = com.CH_gui.lang.Lang.rb.getString("text_CryptoHeaven_Security");

    if (!URLs.get(URLs.SERVICE_SOFTWARE_NAME).startsWith("CryptoHeaven")) {
      String preAboutText = java.text.MessageFormat.format(com.CH_gui.lang.Lang.rb.getString("text_Powered_by..."), new Object[] { URLs.get(URLs.SERVICE_SOFTWARE_NAME) });
      aboutText = "<p>"+preAboutText+"</p><br>" + aboutText;
    }

    JEditorPane jAboutArea = new HTML_ClickablePane(aboutText);
    jAboutArea.setEditable(false);
    jAboutArea.moveCaretPosition(0);
    jAboutArea.select(0, 0);

    KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
    Keymap aboutAreaMap = jAboutArea.getKeymap();
    aboutAreaMap.removeKeyStrokeBinding(enter);

    panel.add(new JScrollPane(jAboutArea), new GridBagConstraints(0, 2, 1, 1, 10, 10, 
        GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(5, 5, 5, 5), 0, 0));

    return panel;
  }


  private class OKActionListener implements ActionListener {
    public void actionPerformed (ActionEvent event) {
      closeDialog();
    }
  }

  /*******************************************************
  *** V i s u a l s S a v a b l e    interface methods ***
  *******************************************************/
  public static final String visualsClassKeyName = "AboutSecurityDialog";
  public String getVisualsClassKeyName() {
    return visualsClassKeyName;
  }
}