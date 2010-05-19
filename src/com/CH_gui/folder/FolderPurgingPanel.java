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

package com.CH_gui.folder;

import com.CH_gui.util.Images;
import com.CH_gui.gui.JMyLabel;
import com.CH_gui.gui.MyInsets;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

import com.CH_cl.service.cache.FetchedDataCache;

import com.CH_co.gui.*;
import com.CH_co.service.records.*;
import com.CH_co.util.*;
import com.CH_co.trace.Trace;

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
 * <b>$Revision: 1.13 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class FolderPurgingPanel extends JPanel {

  // Purging page
  private JLabel jPurgeMsg;
  private JLabel jPurgeWarning;
  public FolderAttributesPanel folderAttributesPanel;

  private FolderPair folderPair;
  private FetchedDataCache cache;

  public boolean amIOwner;
  public boolean isSharableType;
  public boolean isFolderCreationMode;

  /**
   * Creates new FolderPurgingPanel
   * Folder Creation mode
   */
  public FolderPurgingPanel() {
    this(null, true);
  }
  /**
   * Creates new FolderPurgingPanel
   * Folder Editing mode
   */
  public FolderPurgingPanel(FolderPair folderPair) {
    this(folderPair, false);
  }
  /** Creates new FolderPurgingPanel */
  private FolderPurgingPanel(FolderPair folderPair, boolean isFolderCreationMode) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderPurgingPanel.class, "FolderPurgingPanel(FolderPair folderPair, boolean isFolderCreationMode)");
    if (trace != null) trace.args(folderPair);
    if (trace != null) trace.args(isFolderCreationMode);

    this.cache = FetchedDataCache.getSingleInstance();
    this.folderPair = folderPair;

    this.isFolderCreationMode = isFolderCreationMode;
    FolderRecord folderRecord = isFolderCreationMode ? null : folderPair.getFolderRecord();
    this.isSharableType = isFolderCreationMode ? true : folderRecord.isSharableType();
    this.amIOwner = isFolderCreationMode ? true : folderRecord.ownerUserId.equals(cache.getMyUserId());

    createPurgingPanel();

    // set multi line html label after init() (after pack()) because it messes up the sizing of the dialog
    setPurgeMsg();

    if (trace != null) trace.exit(FolderPurgingPanel.class);
  }

  private void createPurgingPanel() {
    setBorder(new EmptyBorder(10, 10, 10, 10));
    setLayout(new GridBagLayout());

    int posY = 0;
    add(new JMyLabel(Images.get(ImageNums.FLD_RECYCLE48)), new GridBagConstraints(0, posY, 1, 2, 0, 0,
          GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    jPurgeMsg = new JMyLabel();
    add(jPurgeMsg, new GridBagConstraints(1, posY, 1, 1, 10, 0,
          GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    jPurgeWarning = new JMyLabel("Purged items are deleted permanently.");
    jPurgeWarning.setIcon(Images.get(ImageNums.WARNING16));
    add(jPurgeWarning, new GridBagConstraints(1, posY, 1, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    // separator
    add(new JSeparator(), new GridBagConstraints(0, posY, 2, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    boolean isEditable = amIOwner && isSharableType;
    if (isFolderCreationMode)
      folderAttributesPanel = new FolderAttributesPanel(isEditable);
    else
      folderAttributesPanel = new FolderAttributesPanel(folderPair.getFolderRecord(), isEditable);
    add(folderAttributesPanel, new GridBagConstraints(0, posY, 2, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    // dummy
    add(new JMyLabel(), new GridBagConstraints(0, posY, 2, 1, 10, 10,
          GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(0, 0, 0, 0), 0, 0));
    posY ++;

  }


  private void setPurgeMsg() {
    String purgeMsg;

    if (!amIOwner)
      purgeMsg = com.CH_gui.lang.Lang.rb.getString("label_You_have_been_granted_access_to_this_folder_and_cannot_alter_auto_purging_settings.");
    else if (isSharableType)
      purgeMsg = com.CH_gui.lang.Lang.rb.getString("label_You_can_use_folder_auto_purging_settings_to_limit_the_number_of_records_it_contains...");
    else
      purgeMsg = com.CH_gui.lang.Lang.rb.getString("label_Folders_of_this_type_do_not_support_auto_purging.");

    jPurgeMsg.setText(purgeMsg);
  }

}