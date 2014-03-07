/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package comx.Tiger.gui;

import com.CH_cl.service.engine.ServerInterfaceLayer;
import com.CH_cl.service.ops.UserOps;
import com.CH_gui.frame.MainFrame;
import com.CH_gui.gui.JMyButton;
import com.CH_gui.gui.JMyCheckBox;
import com.CH_gui.gui.JMyLabel;
import com.CH_gui.gui.MyInsets;
import com.CH_gui.util.GeneralDialog;
import com.CH_gui.util.NoObfuscateException;
import com.CH_guiLib.gui.JMyComboBox;
import comx.Tiger.ssce.PropSpellingSession;
import comx.Tiger.ssce.SpellingSession;
import comx.tig.en.SingleTigerSession;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Properties;
import javax.swing.*;

/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * Class Description:
 *
 * Interact with the user to set SpellingSession options.
 *
 * <b>$Revision: 1.4 $</b>
 *
 * @author Marcin Kurzawa
 */
public class JTigerOptionsDialog extends GeneralDialog {

  private ServerInterfaceLayer SIL;

  /**
   * Interact with the user to set SpellingSession options.
   *
   * @param parent The parent frame
   * @param session The spelling session whose options are to be set
   */
  public JTigerOptionsDialog(Frame parent, SpellingSession session, ServerInterfaceLayer SIL) {
    super(parent, "Spelling preferences");

    this.SIL = SIL;

    okBtn.setText("OK");
    okBtn.setActionCommand("OK");
    cancelBtn.setText("Cancel");
    cancelBtn.setActionCommand("Cancel");

    JPanel panel = new JPanel();
    panel.setLayout(new GridBagLayout());
    int posY = 0;

    initialLanguageChoice = ((PropSpellingSession) session).getProperties().getProperty(SingleTigerSession.PROPERTY__LANGUAGE_NAME);
    jLanguageCombo.setSelectedItem(initialLanguageChoice);
    panel.add(jLanguageLabel, new GridBagConstraints(0, posY, 1, 1, 1, 1,
            GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(10, 10, 2, 2), 0, 0));
    panel.add(jLanguageCombo, new GridBagConstraints(1, posY, 1, 1, 1, 1,
            GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(10, 2, 2, 10), 0, 0));
    panel.add(new JLabel(), new GridBagConstraints(2, posY, 1, 1, 10, 1,
            GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(0, 0, 0, 0), 0, 0));
    posY++;

    enableBackgroundCheckerCkb.setText("Enable as-you-type spell checking.");
    enableBackgroundCheckerCkb.setActionCommand("Enable as-you-type spell checking.");
    panel.add(enableBackgroundCheckerCkb, new GridBagConstraints(0, posY++, 3, 1, 10, 1,
            GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 10, 2, 10), 0, 0));

    caseSensitiveCkb.setText("Enable case sensitive checking.");
    caseSensitiveCkb.setActionCommand("Enable case sensitive checking.");
    panel.add(caseSensitiveCkb, new GridBagConstraints(0, posY++, 3, 1, 10, 1,
            GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 10, 2, 10), 0, 0));

    ignoreCapitalizedWordsCkb.setText("Ignore capitalized words (e.g., Canada).");
    ignoreCapitalizedWordsCkb.setActionCommand("Ignore capitalized words (e.g., Canada).");
    panel.add(ignoreCapitalizedWordsCkb, new GridBagConstraints(0, posY++, 3, 1, 10, 1,
            GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 10, 2, 10), 0, 0));

    ignoreAllCapsWordsCkb.setText("Ignore all-caps words (e.g., ASAP).");
    ignoreAllCapsWordsCkb.setActionCommand("Ignore all-caps words (e.g., ASAP).");
    panel.add(ignoreAllCapsWordsCkb, new GridBagConstraints(0, posY++, 3, 1, 10, 1,
            GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 10, 2, 10), 0, 0));

    ignoreWordsWithNumbersCkb.setText("Ignore words with numbers (e.g., Y2K).");
    ignoreWordsWithNumbersCkb.setActionCommand("Ignore words with numbers (e.g., Y2K).");
    panel.add(ignoreWordsWithNumbersCkb, new GridBagConstraints(0, posY++, 3, 1, 10, 1,
            GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 10, 2, 10), 0, 0));

    ignoreMixedCaseWordsCkb.setText("Ignore words with mixed case (e.g., SuperBase).");
    ignoreMixedCaseWordsCkb.setActionCommand("Ignore words with mixed case (e.g., SuperBase).");
    panel.add(ignoreMixedCaseWordsCkb, new GridBagConstraints(0, posY++, 3, 1, 10, 1,
            GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 10, 2, 10), 0, 0));

    ignoreDomainNamesCkb.setText("Ignore domain names (e.g., domain.com).");
    ignoreDomainNamesCkb.setActionCommand("Ignore domain names (e.g., domain.com).");
    panel.add(ignoreDomainNamesCkb, new GridBagConstraints(0, posY++, 3, 1, 10, 1,
            GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 10, 2, 10), 0, 0));

    reportDoubledWordsCkb.setText("Report doubled words (e.g., the the).");
    reportDoubledWordsCkb.setActionCommand("Report doubled words (e.g., the the).");
    panel.add(reportDoubledWordsCkb, new GridBagConstraints(0, posY++, 3, 1, 10, 1,
            GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 10, 2, 10), 0, 0));

    suggestSplitWordsCkb.setText("Suggest split words.");
    suggestSplitWordsCkb.setActionCommand("Suggest split words.");
    panel.add(suggestSplitWordsCkb, new GridBagConstraints(0, posY++, 3, 1, 10, 1,
            GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(2, 10, 10, 10), 0, 0));
    //}}
    // filler
    panel.add(new JLabel(), new GridBagConstraints(0, posY++, 3, 1, 10, 10,
            GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(0, 0, 0, 0), 0, 0));

    //{{REGISTER_LISTENERS
    SymAction lSymAction = new SymAction();
    okBtn.addActionListener(lSymAction);
    cancelBtn.addActionListener(lSymAction);
    //}}

    ssce = session;

    enableBackgroundCheckerCkb.setSelected(TigerBkgChecker.backgroundCheckEnabled);
    caseSensitiveCkb.setSelected(ssce.getOption(ssce.CASE_SENSITIVE_OPT));
    ignoreAllCapsWordsCkb.setSelected(ssce.getOption(ssce.IGNORE_ALL_CAPS_WORD_OPT));
    ignoreCapitalizedWordsCkb.setSelected(ssce.getOption(ssce.IGNORE_CAPPED_WORD_OPT));
    ignoreMixedCaseWordsCkb.setSelected(ssce.getOption(ssce.IGNORE_MIXED_CASE_OPT));
    ignoreWordsWithNumbersCkb.setSelected(ssce.getOption(ssce.IGNORE_MIXED_DIGITS_OPT));
    ignoreDomainNamesCkb.setSelected(ssce.getOption(ssce.IGNORE_DOMAIN_NAMES_OPT));
    reportDoubledWordsCkb.setSelected(ssce.getOption(ssce.REPORT_DOUBLED_WORD_OPT));
    suggestSplitWordsCkb.setSelected(ssce.getOption(ssce.SUGGEST_SPLIT_WORDS_OPT));

    // Create and show the dialog
    JButton[] buttons = new JButton[]{okBtn, cancelBtn};
    init(parent, buttons, panel, 0, 1);
  }

  public static JDialog buildOptionsDialog_reflection(Frame parent) throws NoObfuscateException {
    TigerPropSession speller = SingleTigerSession.getSingleInstance();
    return new JTigerOptionsDialog(parent, speller, MainFrame.getServerInterfaceLayer());
  }

  //{{DECLARE_CONTROLS
  String initialLanguageChoice = null;
  JLabel jLanguageLabel = new JMyLabel("Language:");
  JComboBox jLanguageCombo = new JMyComboBox(SingleTigerSession.languageNamesAvailable);
  JCheckBox enableBackgroundCheckerCkb = new JMyCheckBox();
  JCheckBox ignoreCapitalizedWordsCkb = new JMyCheckBox();
  JCheckBox ignoreAllCapsWordsCkb = new JMyCheckBox();
  JCheckBox ignoreWordsWithNumbersCkb = new JMyCheckBox();
  JCheckBox ignoreMixedCaseWordsCkb = new JMyCheckBox();
  JCheckBox ignoreDomainNamesCkb = new JMyCheckBox();
  JCheckBox reportDoubledWordsCkb = new JMyCheckBox();
  JCheckBox caseSensitiveCkb = new JMyCheckBox();
  JButton okBtn = new JMyButton();
  JButton cancelBtn = new JMyButton();
  JCheckBox suggestSplitWordsCkb = new JMyCheckBox();
  //}}
  /**
   * Spelling session being edited
   */
  protected SpellingSession ssce;

  class SymAction implements java.awt.event.ActionListener {

    public void actionPerformed(java.awt.event.ActionEvent event) {
      Object object = event.getSource();
      if (object == okBtn) {
        okBtn_actionPerformed(event);
      } else if (object == cancelBtn) {
        cancelBtn_actionPerformed(event);
      }
    }
  }

  void okBtn_actionPerformed(java.awt.event.ActionEvent event) {
    // Apply the option settings, then close.
    ssce.setOption(ssce.CASE_SENSITIVE_OPT, caseSensitiveCkb.isSelected());
    ssce.setOption(ssce.IGNORE_ALL_CAPS_WORD_OPT, ignoreAllCapsWordsCkb.isSelected());
    ssce.setOption(ssce.IGNORE_CAPPED_WORD_OPT, ignoreCapitalizedWordsCkb.isSelected());
    ssce.setOption(ssce.IGNORE_MIXED_CASE_OPT, ignoreMixedCaseWordsCkb.isSelected());
    ssce.setOption(ssce.IGNORE_MIXED_DIGITS_OPT, ignoreWordsWithNumbersCkb.isSelected());
    ssce.setOption(ssce.IGNORE_DOMAIN_NAMES_OPT, ignoreDomainNamesCkb.isSelected());
    ssce.setOption(ssce.REPORT_DOUBLED_WORD_OPT, reportDoubledWordsCkb.isSelected());
    ssce.setOption(ssce.SUGGEST_SPLIT_WORDS_OPT, suggestSplitWordsCkb.isSelected());

    TigerBkgChecker.backgroundCheckEnabled = enableBackgroundCheckerCkb.isSelected();

    if (ssce instanceof PropSpellingSession) {
      String newLanguageChoice = (String) jLanguageCombo.getSelectedItem();
      boolean newLangSettingSet = false;
      if (initialLanguageChoice == null || !initialLanguageChoice.equalsIgnoreCase(newLanguageChoice)) {
        newLangSettingSet = SingleTigerSession.loadLanguageLexicons((String) jLanguageCombo.getSelectedItem());
      }
      Properties properties = ((PropSpellingSession) ssce).getProperties();
      if (newLangSettingSet) {
        properties.setProperty(SingleTigerSession.PROPERTY__LANGUAGE_NAME, newLanguageChoice);
      }
      properties.setProperty(TigerBkgChecker.PROPERTY__BACKGROUND_CHECK_ENABLED, "" + TigerBkgChecker.backgroundCheckEnabled);
      UserOps.updateUserSettingsSpellingProperties(SIL, properties);
    }

    setVisible(false);
    dispose();
  }

  void cancelBtn_actionPerformed(java.awt.event.ActionEvent event) {
    // Close without saving.
    setVisible(false);
    dispose();
  }
}