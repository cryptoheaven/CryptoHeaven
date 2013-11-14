/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.folder;

import com.CH_co.service.records.*;
import com.CH_co.trace.Trace;

import com.CH_gui.gui.*;
import com.CH_gui.util.*;
import com.CH_guiLib.gui.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

/** 
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * <b>$Revision: 1.19 $</b>
 *
 * @author  Marcin Kurzawa
 */
public class FolderAttributesPanel extends JPanel {

  private JCheckBox         jNumToKeepCheck = null;
  private JLabel            jNumToKeepLabel = null;
  private JTextField        jNumToKeep = null;
  private JCheckBox         jKeepAsOldAsCheck = null;
  private JLabel            jKeepAsOldAsLabel = null;
  private JTextField        jKeepAsOldAs = null;
  private JComboBox         jKeepAsOldAsCombo = null;

  private int[] keepAsOldAsMultipier;

  private Short originalNumToKeep;
  private Integer originalKeepAsOldAs;
  private boolean isEditable;

  private boolean numToKeepUsed;
  private boolean keepAsOldAsUsed;

  /** Creates new FolderAttributesPanel */
  public FolderAttributesPanel(boolean isEditable) {
    this(null, isEditable);
  }

  /** Creates new FolderAttributesPanel */
  public FolderAttributesPanel(FolderRecord initialDataRecord, boolean isEditable) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderAttributesPanel.class, "FolderAttributesPanel(FolderRecord folderRecord, boolean isEditable)");
    if (trace != null) trace.args(initialDataRecord);
    if (trace != null) trace.args(isEditable);
    this.isEditable = isEditable;
    originalNumToKeep = initialDataRecord != null ? initialDataRecord.numToKeep : null;
    originalKeepAsOldAs = initialDataRecord != null ? initialDataRecord.keepAsOldAs : null;
    initComponents(initialDataRecord, isEditable);
    initPanel();
    if (trace != null) trace.exit(FolderAttributesPanel.class);
  }

  private void initComponents(FolderRecord initialDataRecord, boolean isEditable) {
    jNumToKeepCheck = new JMyCheckBox();
    jNumToKeepLabel = new JMyLabel(com.CH_cl.lang.Lang.rb.getString("label_Automatically_purge_oldest_records..."));
    jNumToKeep = new JMyTextField("", 4);
    jNumToKeep.setEditable(isEditable);
    jNumToKeep.setMinimumSize(new Dimension(55, 18));
    jNumToKeep.addKeyListener(new KeyAdapter() {
      public void keyTyped(KeyEvent e) {
        numToKeepUsed = true;
      }
    });
    jKeepAsOldAsCheck = new JMyCheckBox();
    jKeepAsOldAsLabel = new JMyLabel(com.CH_cl.lang.Lang.rb.getString("label_Automatically_purge_records_which_are_older_than"));
    jKeepAsOldAs = new JMyTextField("", 4);
    jKeepAsOldAs.setEditable(isEditable);
    jKeepAsOldAs.setMinimumSize(new Dimension(55, 18));
    jKeepAsOldAs.addKeyListener(new KeyAdapter() {
      public void keyTyped(KeyEvent e) {
        keepAsOldAsUsed = true;
      }
    });
    jKeepAsOldAsCombo = new JMyComboBox(new String[] {com.CH_cl.lang.Lang.rb.getString("seconds"), com.CH_cl.lang.Lang.rb.getString("minutes"), com.CH_cl.lang.Lang.rb.getString("hours"), com.CH_cl.lang.Lang.rb.getString("days"), com.CH_cl.lang.Lang.rb.getString("weeks")});
    // combo editable is always false, but selecting values 'editing' is controlled by 'setEnabled()'
    jKeepAsOldAsCombo.setEditable(false);
    jKeepAsOldAsCombo.setEnabled(isEditable);
    jKeepAsOldAsCombo.setSelectedIndex(4);
    keepAsOldAsMultipier = new int[] { 1, 60, 60*60, 60*60*24, 60*60*24*7 };
    jKeepAsOldAsCombo.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        keepAsOldAsUsed = true;
      }
    });

    jNumToKeepCheck.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        boolean toEnable = jNumToKeepCheck.isSelected();
        jNumToKeepLabel.setEnabled(toEnable);
        jNumToKeep.setEnabled(toEnable);
        numToKeepUsed = true;
      }
    });
    jKeepAsOldAsCheck.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        boolean toEnable = jKeepAsOldAsCheck.isSelected();
        jKeepAsOldAsLabel.setEnabled(toEnable);
        jKeepAsOldAs.setEnabled(toEnable);
        jKeepAsOldAsCombo.setEnabled(toEnable && FolderAttributesPanel.this.isEditable);
        keepAsOldAsUsed = true;
      }
    });

    jNumToKeep.addKeyListener(new KeyAdapter() {
      public void keyReleased(KeyEvent e) {
        if (getNumToKeep() == null)
          MessageDialog.showErrorDialog(SwingUtilities.windowForComponent(FolderAttributesPanel.this), com.CH_cl.lang.Lang.rb.getString("msg_Invalid_value_specified.__Please_enter_an_integer_value_in_the_range_[0_-_32,767]."), com.CH_cl.lang.Lang.rb.getString("msgTitle_Invalid_Value"));
      }
    });
    jKeepAsOldAs.addKeyListener(new KeyAdapter() {
      public void keyReleased(KeyEvent e) {
        if (getKeepAsOldAs() == null)
          MessageDialog.showErrorDialog(SwingUtilities.windowForComponent(FolderAttributesPanel.this), com.CH_cl.lang.Lang.rb.getString("msg_Invalid_value_specified.__Value_specified_must_be_an_integer_in_the_range_of_[0_-_2,147,483,647]_seconds."), com.CH_cl.lang.Lang.rb.getString("msgTitle_Invalid_Value"));
      }
    });

    jNumToKeepCheck.setEnabled(isEditable);
    jKeepAsOldAsCheck.setEnabled(isEditable);

    setNumToKeep(initialDataRecord != null ? initialDataRecord.numToKeep : null);
    setKeepAsOldAs(initialDataRecord != null ? initialDataRecord.keepAsOldAs : null);
  }

  public void disableAttributesIfNotUsed() {
    if (!numToKeepUsed) {
      jNumToKeepCheck.setSelected(false);
      jNumToKeepLabel.setEnabled(false);
      jNumToKeep.setEnabled(false);
    }
    if (!keepAsOldAsUsed) {
      jKeepAsOldAsCheck.setSelected(false);
      jKeepAsOldAsLabel.setEnabled(false);
      jKeepAsOldAs.setEnabled(false);
      jKeepAsOldAsCombo.setEnabled(false);
    }
  }

  private void initPanel() {
    setLayout(new GridBagLayout());

    int posY = 0;

    if (jNumToKeep != null) {
      JPanel jNumToKeepPanel = new JPanel();

      jNumToKeepPanel.setLayout(new GridBagLayout());
      jNumToKeepPanel.add(jNumToKeepCheck, new GridBagConstraints(0, 0, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 1), 0, 0));
      jNumToKeepPanel.add(jNumToKeepLabel, new GridBagConstraints(1, 0, 1, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 1, 5, 5), 0, 0));
      jNumToKeepPanel.add(jNumToKeep, new GridBagConstraints(2, 0, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));

      add(jNumToKeepPanel, new GridBagConstraints(0, posY, 2, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(0, 0, 0, 0), 0, 0));
      posY ++;
    }

    if (jKeepAsOldAs != null) {
      JPanel jKeepAsOldAsPanel = new JPanel();

      jKeepAsOldAsPanel.setLayout(new GridBagLayout());
      jKeepAsOldAsPanel.add(jKeepAsOldAsCheck, new GridBagConstraints(0, 0, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 1), 0, 0));
      jKeepAsOldAsPanel.add(jKeepAsOldAsLabel, new GridBagConstraints(1, 0, 1, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 1, 5, 5), 0, 0));
      jKeepAsOldAsPanel.add(jKeepAsOldAs, new GridBagConstraints(2, 0, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
      jKeepAsOldAsCombo.setMaximumSize(new Dimension(77, 26));
      jKeepAsOldAsPanel.add(jKeepAsOldAsCombo, new GridBagConstraints(3, 0, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));

      add(jKeepAsOldAsPanel, new GridBagConstraints(0, posY, 2, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(0, 0, 0, 0), 0, 0));
      posY ++;
    }
  }

  public Short getNumToKeep() {
    Short numToKeep = null;
    try {
      String s = jNumToKeep.getText();
      if (jNumToKeepCheck.isSelected() && s.length() > 0)
        numToKeep = Short.valueOf(s);
      else
        numToKeep = new Short((short)0);
      if (numToKeep.shortValue() < 0)
        numToKeep = null;
    } catch (Throwable t) {
    }
    return numToKeep;
  }
  public Integer getKeepAsOldAs() {
    Integer keepAsOldAs = null;
    try {
      String s = jKeepAsOldAs.getText();
      if (jKeepAsOldAsCheck.isSelected() && s.length() > 0)
        keepAsOldAs = Integer.valueOf(s);
      else
        keepAsOldAs = new Integer(0);
      // apply multipier
      keepAsOldAs = new Integer(keepAsOldAs.intValue() * keepAsOldAsMultipier[jKeepAsOldAsCombo.getSelectedIndex()]);
      if (keepAsOldAs.intValue() < 0)
        keepAsOldAs = null;
    } catch (Throwable t) {
    }
    return keepAsOldAs;
  }

  public void setNumToKeep(Short numToKeep) {
    boolean numToKeepSet = numToKeep != null && numToKeep.shortValue() > 0;
    jNumToKeepCheck.setSelected(numToKeepSet);
    if (numToKeep != null) {
      if (numToKeep.shortValue() == 0)
        jNumToKeep.setText(null);
      else
        jNumToKeep.setText(numToKeep.toString());
    }
    jNumToKeepLabel.setEnabled(numToKeepSet);
    jNumToKeep.setEnabled(numToKeepSet);
  }

  public void setKeepAsOldAs(Integer keepAsOldAs) {
    boolean keepAsOldAsSet = keepAsOldAs != null && keepAsOldAs.intValue() > 0;
    jKeepAsOldAsCheck.setSelected(keepAsOldAsSet);
    if (keepAsOldAs != null) {
      if (keepAsOldAs.intValue() == 0)
        jKeepAsOldAs.setText(null);
      else {
        // Find highest interval from combo box, to fit with an even number.
        int keepValue = keepAsOldAs.intValue();
        int i=0;
        for (i=keepAsOldAsMultipier.length-1; i>=0; i--) {
          if (keepValue == ((keepValue/keepAsOldAsMultipier[i])*keepAsOldAsMultipier[i]))
            break;
        }
        jKeepAsOldAs.setText(""+(keepValue/keepAsOldAsMultipier[i]));
        if (jKeepAsOldAsCombo.getSelectedIndex() != i)
          jKeepAsOldAsCombo.setSelectedIndex(i);
      }
    }
    jKeepAsOldAsLabel.setEnabled(keepAsOldAsSet);
    jKeepAsOldAs.setEnabled(keepAsOldAsSet);
    jKeepAsOldAsCombo.setEnabled(keepAsOldAsSet && isEditable);
  }
}