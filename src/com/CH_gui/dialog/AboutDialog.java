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

package com.CH_gui.dialog;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;

import com.CH_co.gui.*;
import com.CH_co.util.*;
import com.CH_co.trace.Trace;

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
 * <b>$Revision: 1.24 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class AboutDialog extends GeneralDialog implements VisualsSavable {

  private static final int DEFAULT_BUTTON_INDEX = 2;

  /** Creates new AboutDialog */
  public AboutDialog(Frame frame) {
    super(frame, java.text.MessageFormat.format(com.CH_gui.lang.Lang.rb.getString("title_About__SERVICE_SOFTWARE_NAME"), 
      new Object[] {  URLs.get(URLs.SERVICE_SOFTWARE_NAME), 
                      GlobalProperties.PROGRAM_VERSION_STR, 
                      new Short(GlobalProperties.PROGRAM_BUILD_NUMBER)
                   }
          ));
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AboutDialog.class, "AboutDialog(Frame frame)");

    JButton[] buttons = createButtons();
    JPanel panel = createMainPanel();

    if (GlobalProperties.PROGRAM_BUILD_DATE != null)
      setTitle(getTitle() + "  " + GlobalProperties.PROGRAM_BUILD_DATE);

    super.init(frame, buttons, panel, new JMyLabel(Images.get(ImageNums.LOGO_BANNER_MAIN)), DEFAULT_BUTTON_INDEX, DEFAULT_BUTTON_INDEX);

    if (trace != null) trace.exit(AboutDialog.class);
  }

  private JButton[] createButtons() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AboutDialog.class, "createButtons()");
    JButton[] buttons = new JButton[3];

    buttons[0] = new JMyButton(com.CH_gui.lang.Lang.rb.getString("button_License"));
    buttons[0].addActionListener(new LicenseActionListener());

    buttons[1] = new JMyButton(com.CH_gui.lang.Lang.rb.getString("button_About_Security"));
    buttons[1].addActionListener(new SecurityActionListener());

    buttons[2] = new JMyButton(com.CH_gui.lang.Lang.rb.getString("button_Close"));
    buttons[2].setDefaultCapable(true);
    buttons[2].addActionListener(new OKActionListener());

    if (trace != null) trace.exit(AboutDialog.class, buttons);
    return buttons;
  }

  private JPanel createMainPanel() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(AboutDialog.class, "createMainPanel()");
    JPanel panel = new JPanel();
    panel.setLayout(new GridBagLayout());

//    panel.add(new JMyLabel(Images.get(ImageNums.LOGO_BANNER_MAIN)), new GridBagConstraints(0, 0, 1, 1, 0, 0, 
//        GridBagConstraints.CENTER, GridBagConstraints.NONE, new MyInsets(0, 0, 5, 0), 0, 0));

    String aboutText = com.CH_gui.lang.Lang.rb.getString("text_CryptoHeaven_software_is_developed");

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

    if (trace != null) trace.exit(AboutDialog.class, panel);
    return panel;
  }


  private class OKActionListener implements ActionListener {
    public void actionPerformed(ActionEvent event) {
      closeDialog();
    }
  }

  private class SecurityActionListener implements ActionListener {
    public void actionPerformed(ActionEvent event) {
      new AboutSecurityDialog(AboutDialog.this);
    }
  }

  private class LicenseActionListener implements ActionListener {
    public void actionPerformed(ActionEvent event) {
      new LicenseDialog(AboutDialog.this);
    }
  }

  /*******************************************************
  *** V i s u a l s S a v a b l e    interface methods ***
  *******************************************************/
  public static final String visualsClassKeyName = "AboutDialog";
  public String getVisualsClassKeyName() {
    return visualsClassKeyName;
  }
}