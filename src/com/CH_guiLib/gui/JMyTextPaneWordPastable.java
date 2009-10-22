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

package com.CH_guiLib.gui;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;

import com.CH_guiLib.util.*;

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
 * <b>$Revision: 1.3 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class JMyTextPaneWordPastable extends JMyTextPane {

  /**
   * This one can paste also from Word 2000 etc.
   * Strange tags and comments are removed.
   */
  public void paste() {
    Clipboard clipboard = getToolkit().getSystemClipboard();
    final Transferable content = clipboard.getContents(this);
    // Create a new transferable to filter the original one.
    Transferable newContent = new Transferable() {
      /*
       * Only return DataFlavors which mimeTypes are String
       */
      public DataFlavor[] getTransferDataFlavors() {
        DataFlavor[] flavors = content.getTransferDataFlavors();
        ArrayList myFlavorList = new ArrayList(flavors.length);
        for (int i = 0; i < flavors.length; i++) {
          DataFlavor flavor = flavors[i];
          String mimeType = flavor.getMimeType();
          if (mimeType.indexOf("String") >= 0) {
            myFlavorList.add(flavor);
          }
        }
        DataFlavor[] myFlavors = null;
        if (myFlavorList.size() > 0) {
          myFlavors = new DataFlavor[myFlavorList.size()];
          myFlavorList.toArray(myFlavors);
        } else {
          myFlavors = flavors;
        }
        return myFlavors;
      }
      /*
       * unchanged
       */
      public boolean isDataFlavorSupported(DataFlavor flavor) {
        return content.isDataFlavorSupported(flavor);
      }
      /*
       * transforms Strings that are of type HTML
       */
      public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        Object transferData = content.getTransferData(flavor);
        String mimeType = flavor.getMimeType();
        if (mimeType.indexOf("String") >= 0 && mimeType.indexOf("html") >= 0) {
          if (transferData instanceof String) {
            String data = (String) transferData;
            // extract body
            transferData = HTML_utils.clearHTMLheaderAndConditionForDisplay(data, true, true, true);
          }
        }
        return transferData;
      }
    };
    // set the new transferable to the clipboard
    clipboard.setContents(newContent, null);
    super.paste();
  }
  
}