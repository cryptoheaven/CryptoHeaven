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

package com.CH_gui.dialog;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

import com.CH_co.gui.*;
import com.CH_co.service.records.*;
import com.CH_co.util.*;

import com.CH_guiLib.gui.*;

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
 * <b>$Revision: 1.21 $</b>
 * @author  Marcin Kurzawa
 * @version 
 */
public class KeyGenerationOptionsDialog extends GeneralDialog {

  private static final int DEFAULT_BUTTON_INDEX = 0;
  private static final int DEFAULT_CANCEL_BUTTON_INDEX = 1;

  private int keyLength;
  private int certainty;
  private Boolean storeRemoteFlag;
  private boolean ok;

  private JSlider jKeyLength;
  private JSlider jCertainty;
  private JCheckBox jStoreRemoteFlag;

  private JTextField jLength;
  private JTextField jCert;

  /** Creates new KeyGenerationOptionsDialog */
  public KeyGenerationOptionsDialog(Dialog parent, int keyLength, int certainty, Boolean storeRemoteFlag) {
    super(parent, com.CH_gui.lang.Lang.rb.getString("title_Key_Generation_Advanced_Options"));
    init(parent, keyLength, certainty, storeRemoteFlag);
  }
  public KeyGenerationOptionsDialog(Frame parent, int keyLength, int certainty, Boolean storeRemoteFlag) {
    super(parent, com.CH_gui.lang.Lang.rb.getString("title_Key_Generation_Advanced_Options"));
    init(parent, keyLength, certainty, storeRemoteFlag);
  }

  private void init(Component parent, int keyLength, int certainty, Boolean storeRemoteFlag) {

    this.keyLength = keyLength;
    this.certainty = certainty;
    this.storeRemoteFlag = storeRemoteFlag;

    JButton[] buttons = createButtons();
    JPanel panel = createMainPanel();

    this.setModal(true);
    super.init(parent, buttons, new JScrollPane(panel), DEFAULT_BUTTON_INDEX, DEFAULT_CANCEL_BUTTON_INDEX);
  }

  public int getKeyLength() {
    return keyLength;
  }

  public int getCertainty() {
    return certainty;
  }

  public Boolean getStoreRemoteFlag() {
    return storeRemoteFlag;
  }

  public boolean isOK() {
    return ok;
  }

  /* Create three buttons: "OK", "Cancel" */
  private JButton[] createButtons() {

    JButton[] buttons = new JButton[2];
    buttons[0] = new JMyButton(com.CH_gui.lang.Lang.rb.getString("button_OK"));
    buttons[0].setDefaultCapable(true);
    buttons[0] .addActionListener(new OKActionListener());

    buttons[1] = new JMyButton(com.CH_gui.lang.Lang.rb.getString("button_Cancel"));
    buttons[1].addActionListener(new CancelActionListener());

    return buttons;
  }

  private JPanel createMainPanel() {
    JPanel panel = new JPanel();

    //panel.setBorder(BorderFactory.createEtchedBorder());
    panel.setLayout(new GridBagLayout());

    if (KeyRecord.DEBUG__ALLOW_SHORT_KEYS) {
      jKeyLength = new JSlider(KeyRecord.DEBUG__SHORTEST_KEY, KeyRecord.MAX__KEY_LENGTH, keyLength);
      jCertainty = new JSlider(KeyRecord.DEBUG__MIN_CERTAINTY, KeyRecord.MAX__CERTAINTY, certainty);
    }
    else {
      jKeyLength = new JSlider(KeyRecord.MIN__KEY_LENGTH, KeyRecord.MAX__KEY_LENGTH, keyLength);
      jCertainty = new JSlider(KeyRecord.MIN__CERTAINTY, KeyRecord.MAX__CERTAINTY, certainty);
    }

    jKeyLength.addChangeListener(new KeyLengthListener());
    jCertainty.addChangeListener(new CertaintyListener());
    
    jKeyLength.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent me){jKeyLength.setValue(jKeyLength.getMinimum()+((jKeyLength.getMaximum()-jKeyLength.getMinimum())*me.getX())/jKeyLength.getWidth());}
    });
    jCertainty.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent me){jCertainty.setValue(jCertainty.getMinimum()+((jCertainty.getMaximum()-jCertainty.getMinimum())*me.getX())/jCertainty.getWidth());}
    });

    jKeyLength.setPaintLabels(true);
    jCertainty.setPaintLabels(true);
    jKeyLength.setPaintTicks(true);
    jCertainty.setPaintTicks(true);
    jKeyLength.setPaintTrack(true);
    jCertainty.setPaintTrack(true);
    /*
    jKeyLength.setMajorTickSpacing(256);
    jCertainty.setMajorTickSpacing(32);
     */
    //jKeyLength.setExtent(128);
    //jCertainty.setExtent(16);
    //jKeyLength.setSnapToTicks(false);
    //jCertainty.setSnapToTicks(false);

    jKeyLength.setMinorTickSpacing(128);
    jKeyLength.setMajorTickSpacing(512);
    jCertainty.setMinorTickSpacing(4);
    jCertainty.setMajorTickSpacing(32);

    Hashtable htKey = new Hashtable();
    htKey.put(new Integer(1024), new JMyLabel("1024"));
    htKey.put(new Integer(1536), new JMyLabel("1536"));
    htKey.put(new Integer(2048), new JMyLabel("2048"));
    htKey.put(new Integer(2560), new JMyLabel("2560"));
    htKey.put(new Integer(3072), new JMyLabel("3072"));
    htKey.put(new Integer(3584), new JMyLabel("3584"));
    htKey.put(new Integer(4096), new JMyLabel("4096"));

    Hashtable htCert = new Hashtable();
    htCert.put(new Integer(128), new JMyLabel("128"));
    htCert.put(new Integer(192), new JMyLabel("192"));
    htCert.put(new Integer(256), new JMyLabel("256"));
    htCert.put(new Integer(160), new JMyLabel("160"));
    htCert.put(new Integer(224), new JMyLabel("224"));

    if (KeyRecord.DEBUG__ALLOW_SHORT_KEYS) {
      htKey.put(new Integer(KeyRecord.DEBUG__SHORTEST_KEY), new JMyLabel(""+KeyRecord.DEBUG__SHORTEST_KEY));
      htCert.put(new Integer(KeyRecord.DEBUG__MIN_CERTAINTY), new JMyLabel(""+KeyRecord.DEBUG__MIN_CERTAINTY));
    }

    jKeyLength.setLabelTable(htKey);
    jCertainty.setLabelTable(htCert);

    //jKeyLength.setPreferredSize(new Dimension(200, 30));
    //jCertainty.setPreferredSize(new Dimension(200, 30));

    jLength = new JMyTextField("" + keyLength, 4);
    jLength.setEditable(false);
    jCert = new JMyTextField("" + certainty, 4);
    jCert.setEditable(false);

    if (storeRemoteFlag != null)
      jStoreRemoteFlag = new JMyCheckBox(com.CH_gui.lang.Lang.rb.getString("check_Store_encrypted_Private_Key_on_the_server."), storeRemoteFlag.booleanValue());

    int posY = 0;

    String keyStr = com.CH_gui.lang.Lang.rb.getString("text_Longer_keys_will_provide_higher_level_of_security");
    panel.add(new JMyLabel(Images.get(ImageNums.KEY_LENGTH32)), new GridBagConstraints(0, posY, 1, 2, 0, 0, 
        GridBagConstraints.NORTH, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));

    panel.add(new JMyLabel(keyStr), new GridBagConstraints(1, posY, 3, 1, 5, 0, 
        GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_RSA_Key_Length")), new GridBagConstraints(1, posY, 1, 1, 0, 0, 
        GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));

    panel.add(jKeyLength, new GridBagConstraints(2, posY, 1, 1, 8, 0, 
        GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));

    panel.add(jLength, new GridBagConstraints(3, posY, 1, 1, 1, 0, 
        GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    panel.add(new JSeparator(), new GridBagConstraints(0, posY, 4, 1, 10, 0, 
        GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;



    String certaintyStr = com.CH_gui.lang.Lang.rb.getString("text_The_strength_of_the_generated_key_pair");
    panel.add(new JMyLabel(Images.get(ImageNums.PRIME_CERTEINTY32)), new GridBagConstraints(0, posY, 1, 2, 0, 0, 
        GridBagConstraints.NORTH, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));

    panel.add(new JMyLabel(certaintyStr), new GridBagConstraints(1, posY, 3, 1, 5, 0, 
        GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Certainty")), new GridBagConstraints(1, posY, 1, 1, 0, 0, 
        GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));

    panel.add(jCertainty, new GridBagConstraints(2, posY, 1, 1, 8, 0, 
        GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));

    panel.add(jCert, new GridBagConstraints(3, posY, 1, 1, 1, 0, 
        GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    if (jStoreRemoteFlag != null) {
      panel.add(new JSeparator(), new GridBagConstraints(0, posY, 4, 1, 10, 0, 
          GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
      posY ++;

      String storeStr = com.CH_gui.lang.Lang.rb.getString("text_The_strength_of_the_overall_system");
      panel.add(new JMyLabel(Images.get(ImageNums.STORE_REMOTE32)), new GridBagConstraints(0, posY, 1, 2, 0, 0, 
          GridBagConstraints.NORTH, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));

      panel.add(new JMyLabel(storeStr), new GridBagConstraints(1, posY, 3, 1, 5, 0, 
          GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
      posY ++;

      panel.add(jStoreRemoteFlag, new GridBagConstraints(1, posY, 3, 1, 0, 0, 
          GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
      posY ++;

      panel.setPreferredSize(new Dimension(515, 520));
    } else {
      panel.setPreferredSize(new Dimension(515, 320));
    }

    panel.add(new JMyLabel(), new GridBagConstraints(0, posY, 4, 1, 10, 10, 
        GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new MyInsets(5, 5, 5, 5), 0, 0));

    return panel;
  }


  private class CancelActionListener implements ActionListener {
    public void actionPerformed (ActionEvent event) {
      ok = false;
      closeDialog();
    }
  }

  private class OKActionListener implements ActionListener {
    public void actionPerformed (ActionEvent event) {
      keyLength = jKeyLength.getValue();
      certainty = jCertainty.getValue();
      if (jStoreRemoteFlag != null)
        storeRemoteFlag = Boolean.valueOf(jStoreRemoteFlag.isSelected());
      ok = true;
      closeDialog();
    }
  }

  private class KeyLengthListener implements ChangeListener {
    public void stateChanged(ChangeEvent e) {
      if (jKeyLength != null && jLength != null)
        jLength.setText(""+jKeyLength.getValue());
    }
  }

  private class CertaintyListener implements ChangeListener {
    public void stateChanged(ChangeEvent e) {
      if (jCertainty != null && jCert != null)
        jCert.setText(""+jCertainty.getValue());
    }
  }

//  /*******************************************************
//  *** V i s u a l s S a v a b l e    interface methods ***
//  *******************************************************/
//  public static final String visualsClassKeyName = "KeyGenerationOptionsDialog";
//  public String getVisualsClassKeyName() {
//    return visualsClassKeyName;
//  }
}