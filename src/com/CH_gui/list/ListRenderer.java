/*
* Copyright 2001-2012 by CryptoHeaven Corp.,
* Mississauga, Ontario, Canada.
* All rights reserved.
*
* This software is the confidential and proprietary information
* of CryptoHeaven Corp. ("Confidential Information").  You
* shall not disclose such Confidential Information and shall use
* it only in accordance with the terms of the license agreement
* you entered into with CryptoHeaven Corp.
*/

package com.CH_gui.list;

import com.CH_cl.service.cache.CacheEmlUtils;
import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.cache.TextRenderer;
import com.CH_cl.service.records.EmailAddressRecord;
import com.CH_co.service.records.*;
import com.CH_co.trace.Trace;
import com.CH_co.util.ArrayUtils;
import com.CH_co.util.ImageNums;
import com.CH_co.util.Misc;
import com.CH_gui.action.Actions;
import com.CH_gui.menuing.MenuActionItem;
import com.CH_gui.msgs.MsgPanelUtils;
import com.CH_gui.service.records.RecordUtilsGui;
import com.CH_gui.util.FileTypesIcons;
import com.CH_gui.util.Images;
import com.CH_guiLib.gui.MyDefaultListCellRenderer;
import java.awt.Component;
import java.io.File;
import javax.swing.*;

/** 
* <b>Copyright</b> &copy; 2001-2012
* <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
* CryptoHeaven Corp.
* </a><br>All rights reserved.<p>
*
* @author  Marcin Kurzawa
* @version
*/
public class ListRenderer implements ListCellRenderer, Cloneable {

  private static String STR_UNKNOWN = com.CH_cl.lang.Lang.rb.getString("unknown");

  private DefaultListCellRenderer defaultRenderer = new MyDefaultListCellRenderer();
  private boolean withFileSizes;
  private boolean withFolderParticipants;
  private boolean withFullEmailAddresses;
  private boolean withUploadPendingNote;
  private boolean forSendReceive;
  private StringHighlighterI stringHighlighter;

  private boolean suppressSelectionRendering;

  public ListRenderer() {
  }
  public ListRenderer(boolean withFileSizes, boolean withFolderParticipants, boolean withFullEmailAddresses) {
    this(withFileSizes, withFolderParticipants, withFullEmailAddresses, false, false, null);
  }
  public ListRenderer(boolean withFileSizes, boolean withFolderParticipants, boolean withFullEmailAddresses, boolean withUploadPendingNote, boolean forSendReceive) {
    this(withFileSizes, withFolderParticipants, withFullEmailAddresses, withUploadPendingNote, forSendReceive, null);
  }
  public ListRenderer(boolean withFileSizes, boolean withFolderParticipants, boolean withFullEmailAddresses, boolean withUploadPendingNote, boolean forSendReceive, StringHighlighterI stringHighlighter) {
    this.withFileSizes = withFileSizes;
    this.withFolderParticipants = withFolderParticipants;
    this.withFullEmailAddresses = withFullEmailAddresses;
    this.withUploadPendingNote = withUploadPendingNote;
    this.forSendReceive = forSendReceive;
    this.stringHighlighter = stringHighlighter;
  }

  public StringHighlighterI getStringHighlighter() {
    return stringHighlighter;
  }
  public boolean isWithFileSizes() {
    return withFileSizes;
  }
  public boolean isWithFolderParticipants() {
    return withFolderParticipants;
  }
  public boolean isWithFullEmailAddresses() {
    return withFullEmailAddresses;
  }
  public boolean isWithUploadPendingNote() {
    return withUploadPendingNote;
  }
  public void setStringHighlighter(StringHighlighterI stringHighlighter) {
    this.stringHighlighter = stringHighlighter;
  }
  public void setSuppressSelectionRendering(boolean isSuppressed) {
    this.suppressSelectionRendering = isSuppressed;
  }
  public void setWithFileSizes(boolean withFileSizes) {
    this.withFileSizes = withFileSizes;
  }
  public void setWithFolderParticipants(boolean withFolderParticipants) {
    this.withFolderParticipants = withFolderParticipants;
  }
  public void setWithFullEmailAddresses(boolean withFullEmailAddresses) {
    this.withFullEmailAddresses = withFullEmailAddresses;
  }
  public void setWithUploadPendingNote(boolean withUploadPendingNote) {
    this.withUploadPendingNote = withUploadPendingNote;
  }
  public String getRenderedTextApplySettings(Object value) {
    return getRenderedText(value, withFileSizes, withFolderParticipants, withFullEmailAddresses, withUploadPendingNote, stringHighlighter);
  }
  public static String getRenderedText(Object value) {
    return getRenderedText(value, false, false, false, false, null);
  }
  public static String getRenderedText(Object value, boolean includeFileSizes, boolean includeFolderParticipants, boolean includeFullEmailAddress) {
    return getRenderedText(value, includeFileSizes, includeFolderParticipants, includeFullEmailAddress, false, null);
  }
  public static String getRenderedText(Object value, boolean includeFileSizes, boolean includeFolderParticipants, boolean includeFullEmailAddress, StringHighlighterI stringHighlighter) {
    return getRenderedText(value, includeFileSizes, includeFolderParticipants, includeFullEmailAddress, false, stringHighlighter);
  }
  public static String getRenderedText(Object value, boolean includeFileSizes, boolean includeFolderParticipants, boolean includeFullEmailAddress, boolean includeUploadPendingNote) {
    return getRenderedText(value, includeFileSizes, includeFolderParticipants, includeFullEmailAddress, includeUploadPendingNote, null);
  }
  public static String getRenderedText(Object value, boolean includeFileSizes, boolean includeFolderParticipants, boolean includeFullEmailAddress, boolean includeUploadPendingNote, StringHighlighterI stringHighlighter) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ListRenderer.class, "getRenderedText()");
    String label = null;

    // get label and icon
    if (value instanceof List_Viewable) {
      List_Viewable v = (List_Viewable) value;
      label = v.getLabel();
    }
    else if (value instanceof JLabel) {
      JLabel v = (JLabel) value;
      label = v.getText();
    }
    else if (value instanceof AbstractButton) {
      AbstractButton v = (AbstractButton) value;
      label = v.getText();
    }
    else if (value instanceof JSeparator) {
      label = MenuActionItem.STR_SEPARATOR;
    }
    else if (value instanceof MsgLinkRecord || value instanceof MsgDataRecord) {
      MsgDataRecord mData = null;
      if (value instanceof MsgLinkRecord) {
        MsgLinkRecord mLink = (MsgLinkRecord) value;
        mData = FetchedDataCache.getSingleInstance().getMsgDataRecord(mLink.msgId);
      } else {
        mData = (MsgDataRecord) value;
      }
      if (mData != null) {
        if (mData.isTypeMessage()) {
          String subject = mData.getSubject();
          if (subject == null || subject.length() == 0) {
            if (mData.getText() != null) {
              subject = "Body: " + MsgPanelUtils.extractPlainFromHtml(mData.getText());
              if (subject.length() > 40)
                subject = subject.substring(0, 40) + " ...";
              subject = ArrayUtils.replaceKeyWords(subject,
                new String[][] {
                  {"\n\r", " "},
                  {"\r\n", " "},
                  {"\r", " "},
                  {"\n", " "},
              });
            } else {
              subject = "(No subject, Message ID " + mData.msgId + ")";
            }
          }
          label = subject;
          if (includeFileSizes && mData.recordSize != null)
            label += "   (" + Misc.getFormattedSize(mData.recordSize.intValue(), 4, 3) + ")";
        }
      }
    }
    else if (value instanceof ObjectsProviderUpdaterI) {
      label = value.toString();
    }
    else if (value instanceof InvEmlRecord) {
      InvEmlRecord rec = (InvEmlRecord) value;
      String msg = rec.msg != null && rec.msg.length() > 0 ? "<FONT size='-2' COLOR=#777777>"+rec.msg+"</FONT>" : "";
      label = "<html><body>"+rec.emailAddr+" "+msg+"</body></html>";
    }

    if (label == null) {
      label = TextRenderer.getRenderedText(value, includeFileSizes, includeFolderParticipants, includeFullEmailAddress, includeUploadPendingNote);
    }

    if (label == null) {
      label = STR_UNKNOWN;
      if (trace != null) trace.info(100, value, Misc.getStack(new Throwable()));
    }

    if (stringHighlighter != null && !(value instanceof ObjectsProviderUpdaterI)) {
      String[] visualsReturnBuffer = new String[1];
      if (stringHighlighter.hasHighlightingStr()) {
        int match = StringHighlighter.matchStrings(label, stringHighlighter, true, visualsReturnBuffer);
        if (match != StringHighlighter.MATCH_STRING__NO_MATCH) {
          label = visualsReturnBuffer[0];
        }
      }
    }

    if (trace != null) trace.exit(ListRenderer.class, label);
    return label;
  }


  public static Icon getRenderedIcon(Object value) {
    return getRenderedIcon(value, false);
  }

  public static Icon getRenderedIcon(Object value, boolean forSendReceive) {
    Icon icon = null;

    // get label and icon
    if (value instanceof List_Viewable) {
      List_Viewable v = (List_Viewable) value;
      icon = v.getIcon();
    }
    else if (value instanceof JLabel) {
      JLabel v = (JLabel) value;
      icon = v.getIcon();
    }
    else if (value instanceof AbstractButton) {
      AbstractButton v = (AbstractButton) value;
      icon = v.getIcon();
    }
    else if (value instanceof JSeparator) {
    }
    else if (value instanceof MsgLinkRecord) {
      MsgLinkRecord mLink = (MsgLinkRecord) value;
      FetchedDataCache cache = FetchedDataCache.getSingleInstance();
      MsgDataRecord mData = cache.getMsgDataRecord(mLink.msgId);
      if (mData != null) {
        if (mData.isTypeAddress())
          icon = Images.get(ImageNums.ADDRESS16);
      }
      if (icon == null && mLink.ownerObjType.shortValue() == Record.RECORD_TYPE_FOLDER) {
        FolderRecord fRec = FetchedDataCache.getSingleInstance().getFolderRecord(mLink.ownerObjId);
        if (fRec != null && fRec.folderType != null) {
          if (fRec.folderType.shortValue() == FolderRecord.POSTING_FOLDER || fRec.folderType.shortValue() == FolderRecord.CHATTING_FOLDER)
            icon = Images.get(ImageNums.POSTING16);
        }
      }
      if (icon == null) {
        icon = Images.get(mLink.getIcon());
      }
    }
    else if (value instanceof FolderPair || value instanceof FolderRecord) {
      FolderRecord fRec = null;
      if (value instanceof FolderPair) {
        fRec = ((FolderPair) value).getFolderRecord();
      } else {
        fRec = (FolderRecord) value;
      }
      FetchedDataCache cache = FetchedDataCache.getSingleInstance();
      UserRecord uRec = cache.getUserRecord();
      icon = Images.get(fRec.getIcon(false, uRec, forSendReceive));
    }
    else if (value instanceof Record) {
      Record rec = (Record) value;
      icon = RecordUtilsGui.getIcon(rec);
    }
    else if (value instanceof File) {
      File file = (File) value;
      icon = FileTypesIcons.getFileIcon(file);
    }

    if (icon == null)
      icon = Images.get(ImageNums.TRANSPARENT16);
    return icon;
  }

  public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
    // Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(ListRenderer.class, "getListCellRendererComponent");

    JLabel renderer = (JLabel)defaultRenderer.getListCellRendererComponent(list, value, index, suppressSelectionRendering ? false:isSelected, cellHasFocus);
    String label = null;
    Icon icon = null;
    String toolTip = null;

    if (forSendReceive) {
      // just for display convert any EmailAddressRecord to familiar Address Book entry
      if (value instanceof EmailAddressRecord) {
        value = CacheEmlUtils.convertToFamiliarEmailRecord(((EmailAddressRecord) value).address);
      }
    }
    label = getRenderedText(value, withFileSizes, withFolderParticipants, withFullEmailAddresses, withUploadPendingNote, stringHighlighter);
    icon = getRenderedIcon(value, forSendReceive);

    // get tool tip
    if (value instanceof MenuActionItem) {
      MenuActionItem v = (MenuActionItem) value;
      Action a = v.getAction();
      if (a != null)
        toolTip = (String) a.getValue(Actions.TOOL_TIP);
    } else if (value instanceof Action) {
      Action v = (Action) value;
      toolTip = (String) v.getValue(Actions.TOOL_TIP);
    } else if (value instanceof JComponent) {
      JComponent v = (JComponent) value;
      toolTip = v.getToolTipText();
    }

    renderer.setText(label);
    renderer.setIcon(icon);
    renderer.setToolTipText(toolTip);

    // if (trace != null) trace.exit(ListRenderer.class, renderer);
    return renderer;
  }

  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
    }
    return null;
  }
}