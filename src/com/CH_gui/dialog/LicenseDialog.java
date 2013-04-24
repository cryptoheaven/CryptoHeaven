/*
 * Copyright 2001-2013 by CryptoHeaven Corp.,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */

package com.CH_gui.dialog;

import com.CH_co.trace.Trace;
import com.CH_co.util.URLs;
import com.CH_gui.gui.JMyButton;
import com.CH_gui.gui.JMyTextArea;
import com.CH_gui.gui.MyInsets;
import com.CH_gui.util.GeneralDialog;
import com.CH_gui.util.MiscGui;
import com.CH_gui.util.VisualsSavable;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.swing.*;
import javax.swing.text.Keymap;

/** 
 * <b>Copyright</b> &copy; 2001-2013
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Corp.
 * </a><br>All rights reserved.<p>
 *
 * Class Description: 
 *
 *
 * Class Details:
 *
 *
 * <b>$Revision: 1.26 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class LicenseDialog extends GeneralDialog implements VisualsSavable {

  private static final int DEFAULT_BUTTON_INDEX = 0;

  /** Creates new LicenseDialog */
  public LicenseDialog(Dialog parent) {
    super(parent, com.CH_cl.lang.Lang.rb.getString("title_License"));
    init(parent);
  }
  public LicenseDialog(Frame parent) {
    super(parent, com.CH_cl.lang.Lang.rb.getString("title_License"));
    init(parent);
  }
  private void init(Component parent) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(LicenseDialog.class, "init(Component parent)");
    JButton[] buttons = createButtons();
    JPanel panel = createMainPanel();

    super.init(parent, buttons, panel, MiscGui.createLogoHeader(), DEFAULT_BUTTON_INDEX, DEFAULT_BUTTON_INDEX);

    if (trace != null) trace.exit(LicenseDialog.class);
  }

  private JButton[] createButtons() {
    JButton[] buttons = new JButton[1];
    buttons[0] = new JMyButton(com.CH_cl.lang.Lang.rb.getString("button_Close"));
    buttons[0].setDefaultCapable(true);
    buttons[0].addActionListener(new OKActionListener());
    return buttons;
  }

  private JPanel createMainPanel() {
    JPanel panel = new JPanel();
    panel.setLayout(new GridBagLayout());

//    panel.add(new JMyLabel(Images.get(ImageNums.LOGO_BANNER_MAIN)), new GridBagConstraints(0, 0, 1, 1, 0, 0, 
//        GridBagConstraints.CENTER, GridBagConstraints.NONE, new MyInsets(0, 0, 5, 0), 0, 0));

    JTextArea jAboutArea = new JMyTextArea(getLicenseText());
    jAboutArea.setWrapStyleWord(true);
    jAboutArea.setLineWrap(true);
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

  /**
   * Fetches license text from resource file.
   * @return
   */
  private String getLicenseText() {
    String licenseText = "";
    try {
      InputStream inStream = URLs.getResourceURL("ch/cl/License.txt").openStream();
      BufferedReader reader = new BufferedReader(new InputStreamReader(inStream));
      StringBuffer sb = new StringBuffer();
      String line = null;
      while ((line = reader.readLine()) != null) {
        sb.append(line);
        sb.append("\n");
      }
      licenseText = sb.toString();
    } catch (Throwable t) {
      licenseText = java.text.MessageFormat.format(com.CH_cl.lang.Lang.rb.getString("text_Copyright_(c)__CryptoHeaven_Development_Team.__All_rights_reserved."), new Object[] { URLs.get(URLs.HOME_PAGE) });
    }

    if (!URLs.get(URLs.SERVICE_SOFTWARE_NAME).startsWith("CryptoHeaven")) {
      String preAboutText = java.text.MessageFormat.format(com.CH_cl.lang.Lang.rb.getString("text_Powered_by..."), new Object[] { URLs.get(URLs.SERVICE_SOFTWARE_NAME) });
      licenseText = preAboutText+"\n\n" + licenseText;
    }
    return licenseText;
  }

  private class OKActionListener implements ActionListener {
    public void actionPerformed (ActionEvent event) {
      closeDialog();
    }
  }

  /*******************************************************
  *** V i s u a l s S a v a b l e    interface methods ***
  *******************************************************/
  public static final String visualsClassKeyName = "LicenseDialog";
  public String getVisualsClassKeyName() {
    return visualsClassKeyName;
  }
}