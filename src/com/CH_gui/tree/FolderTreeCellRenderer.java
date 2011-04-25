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

package com.CH_gui.tree;

import com.CH_cl.service.cache.*;

import com.CH_co.trace.Trace;
import com.CH_co.tree.*;
import com.CH_co.util.*;
import com.CH_co.service.records.*;

import com.CH_gui.gui.*;
import com.CH_gui.util.Images;

import java.awt.*;
import javax.swing.*;

/** 
 * <b>Copyright</b> &copy; 2001-2011
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>
 *
 * Class Description:
 *  This class overrides the default renderer to manage the icons properly.
 *
 * Class Details:
 *  Icons used are taken from Icon Repository.
 *
 * <b>$Revision: 1.22 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class FolderTreeCellRenderer extends MyDefaultTreeCellRenderer {

  /** Creates new FolderTreeCellRenderer */
  public FolderTreeCellRenderer() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FolderTreeCellRenderer.class, "FolderTreeCellRenderer()");
    setLeafIcon(Images.get(ImageNums.FLD_CLOSED16));
    setOpenIcon(Images.get(ImageNums.FLD_CLOSED16));
    setClosedIcon(Images.get(ImageNums.FLD_CLOSED16));
    if (trace != null) trace.exit(FolderTreeCellRenderer.class);
  }

  /**
    * Configures the renderer based on the passed in components.
    * The value is set from messaging value with toString().
    * The foreground color is set based on the selection and the icon
    * is set based on selected.
    */
  public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                boolean selected,
                                                boolean expanded,
                                                boolean leaf, int row,
                                                boolean hasFocus)
  {
    super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
    setToolTipText(null);

    Icon icon = null;

    // use a bold face if label is ment to be html with bold due to bug in windows skin in jre 1.4.1 and prior where
    // bold folders would show blank
    String text = getText();
    Font font = getFont();
    if (text != null && text.startsWith("<html><b>")) {
      setText(text.substring("<html><b>".length()));
      if (!font.isBold())
        setFont(font.deriveFont(Font.BOLD));
    } else {
      if (!font.isPlain())
        setFont(font.deriveFont(Font.PLAIN));
    }

    // root
    if (value == tree.getModel().getRoot()) {
      //icon = Images.get(ImageNums.COMP_NET16); // root of the tree is always hidden from view
    // folders
    } else {
      FolderPair folderPair = ((FolderTreeNode) value).getFolderObject();
      FolderRecord fRec = folderPair.getFolderRecord();

      if (fRec.folderType.shortValue() == FolderRecord.MESSAGE_FOLDER) {
        UserRecord myUserRec = FetchedDataCache.getSingleInstance().getUserRecord();
        icon = Images.get(fRec.getIcon(selected, myUserRec));
      } else {
        icon = Images.get(fRec.getIcon(selected, null));
      }

      // Render update count so the user is notified that folder content changed while he wasn't looking at it.
      String toolTip = fRec.getCachedToolTip();
      if (toolTip == null) {
        int updateCount = fRec.getUpdateCount();
        if (!selected && updateCount > 0) {
          String times = updateCount > 1 ? com.CH_gui.lang.Lang.rb.getString("times") : com.CH_gui.lang.Lang.rb.getString("time");
          toolTip = java.text.MessageFormat.format(com.CH_gui.lang.Lang.rb.getString("folderTip_This_folder_was_updated_NUMBER-OF_TIMES_while_you_were_not_viewing_its_content..."), new Object[] {new Integer(updateCount), times});
        } else {
          String[] notes = FolderTree.getOwnerAndChatNote(fRec);
          String ownerNote = notes[0];
          String chatNote = notes[1];

          if (ownerNote.length() > 0 && chatNote.length() == 0) {
            //toolTip = "<html>The user <b>" + ownerNote + "</b> is the primary owner of this folder.";
            toolTip = java.text.MessageFormat.format(com.CH_gui.lang.Lang.rb.getString("folderTip_The_user_USER_is_the_primary_owner_of_this_folder."), new Object[] {ownerNote});
          } else if (ownerNote.length() == 0 && chatNote.length() > 0) {
            //toolTip = "<html>You are sharing this chatting folder with <b>" + chatNote + "</b>.";
            toolTip = java.text.MessageFormat.format(com.CH_gui.lang.Lang.rb.getString("folderTip_You_are_sharing_this_chatting_folder_with_USER."), new Object[] {chatNote});
          } else if (ownerNote.length() > 0 && chatNote.length() > 0) {
            //toolTip = "<html>The user <b>" + ownerNote+ "</b> is the primary owner of this chatting folder. <br>Other participants are <b>" + chatNote + "</b>.";
            toolTip = java.text.MessageFormat.format(com.CH_gui.lang.Lang.rb.getString("folderTip_The_user_USER_is_the_primary_owner_of_this_chatting_folder._Other_participants_are_OTHER-USERS."), new Object[] {ownerNote, chatNote});
          }
        }
        fRec.setCachedToolTip(toolTip == null ? "" : toolTip);
      }
      setToolTipText(toolTip != null && toolTip.length() > 0 ? toolTip : null);

    } // end if folders

    if (icon != null)
      setIcon(icon);

    return this;
  }
}