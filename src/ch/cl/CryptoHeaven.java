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

package ch.cl;

import com.CH_cl.service.ops.AutoUpdater;
import com.CH_cl.util.GlobalSubProperties;

import com.CH_co.cryptx.*;
import com.CH_co.service.records.*;
import com.CH_co.util.*;

import java.awt.*;
import javax.swing.*;

/** 
 * <b>Copyright</b> &copy; 2001-2009
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team. 
 * </a><br>All rights reserved.<p>
 *
 *
 * @author  Marcin Kurzawa
 * @version 
 */
public class CryptoHeaven extends Object {

  private static boolean DEBUG = false;

  static {
    try {
      String[][] DEFAULT_PROPERTIES = new String[][] {
          //{"key",                       "value"},
          {"MenuTreeModel."+com.CH_gui.frame.AddressFrame.visualsClassKeyName+"_VS_Ver3",        "Program Menus|-1|0|(|File|-10|70|(|Send|750|83|83|2|Save as Draft|764|-1|-1|-1|Separator|0|Select Recipients|751|82|82|2|Select Attachments|752|76|76|2|Voice Recording Panel|768|-1|-1|-1|Priority|-21|80|(|FYI|756|-1|49|2|Normal|757|-1|50|2|High|758|-1|51|2|)|All Headers|761|-1|-1|-1|Separator|0|Print ...|723|-1|-1|-1|Separator|0|Close|800|67|67|10|)|Edit|-23|69|(|Undo|759|85|90|2|Redo|760|82|89|2|Separator|0|Cut|753|88|88|2|Copy|754|67|67|2|Paste|755|86|86|2|)|Tools|-70|84|(|Spelling ...|765|-1|118|130|Edit user dictionary ...|766|-1|-1|-1|Spelling options ...|767|-1|-1|-1|Separator|0|Customize Toolbar ...|101|-1|-1|-1|)|Plugins|-100000|80|)|"},
          {"MenuTreeModel."+com.CH_gui.frame.ContactTableFrame.visualsClassKeyName+"_VS_Ver3",   "Program Menus|-1|0|(|File|-10|70|(|New Message|503|78|77|2|Chat|508|72|72|2|Create Shared Space|510|-1|-1|-1|Separator|0|Find Friends and Associates ...|500|-1|-1|-1|Add to Address Book|511|-1|-1|-1|Create Group|512|-1|-1|-1|Separator|0|Close|800|67|67|10|)|Contacts|-30|78|(|Invite Friends and Associates ...|509|-1|-1|-1|Accept / Decline Contact ...|501|-1|-1|-1|Delete Contact(s)|502|-1|-1|-1|Separator|0|Show Other's Contacts|506|-1|-1|-1|Contact Properties|504|-1|-1|-1|)|View|-50|86|(|Clone Contact List View|507|-1|-1|-1|Separator|0|Refresh Contacts|505|-1|-1|-1|Separator|0|Table Columns ...|1202|-1|-1|-1|Sort Table By|-51|83|(|col_1|1203|-1|-1|-1|col_2|1204|-1|-1|-1|col_3|1205|-1|-1|-1|col_4|1206|-1|-1|-1|col_5|1207|-1|-1|-1|col_6|1208|-1|-1|-1|col_7|1209|-1|-1|-1|col_8|1210|-1|-1|-1|col_9|1211|-1|-1|-1|col_10|1212|-1|-1|-1|col_11|1213|-1|-1|-1|col_12|1214|-1|-1|-1|col_13|1215|-1|-1|-1|col_14|1216|-1|-1|-1|col_15|1217|-1|-1|-1|col_16|1218|-1|-1|-1|col_16|1219|-1|-1|-1|col_16|1220|-1|-1|-1|col_16|1221|-1|-1|-1|col_16|1222|-1|-1|-1|Separator|0|Ascending|1200|-1|-1|-1|Descending|1201|-1|-1|-1|Separator|0|Group Messages by Conversation|717|-1|-1|-1|)|Separator|0|Contact Columns ...|524|-1|-1|-1|Sort Contacts By|-52|83|(|Direction|515|-1|-1|-1|Name|516|-1|-1|-1|Status|517|-1|-1|-1|Contact ID|518|-1|-1|-1|User ID|519|-1|-1|-1|Encryption|520|-1|-1|-1|Created|521|-1|-1|-1|Updated|522|-1|-1|-1|Permissions|523|-1|-1|-1|Separator|0|Ascending|513|-1|-1|-1|Descending|514|-1|-1|-1|)|Separator|0|)|Tools|-70|84|(|Customize Toolbar ...|101|-1|-1|-1|)|Help|-100|72|(|About "+URLs.get(URLs.SERVICE_SOFTWARE_NAME)+"|201|-1|-1|-1|)|Plugins|-100000|80|)|"},
          {"MenuTreeModel."+com.CH_gui.frame.FolderTreeFrame.visualsClassKeyName+"_VS_Ver3",     "Program Menus|-1|0|(|File|-10|70|(|New Folder ...|300|78|78|130|Share Folder ...|312|-1|-1|-1|New Message to Folder|700|77|77|650|Separator|0|Upload To Folder ...|303|85|85|130|Download Folder ...|304|68|68|130|Separator|0|Folder Properties and Sharing|305|-1|-1|-1|Separator|0|Close|800|67|67|650|)|Edit|-40|69|(|Move Folder ...|301|-1|-1|-1|Delete Folder ...|302|-1|-1|-1|Separator|0|Empty Folder ...|314|-1|-1|-1|)|View|-50|86|(|Clone Folder View|307|-1|-1|-1|Explore Folder|308|-1|-1|-1|Separator|0|Refresh Folders|306|-1|-1|-1|)|Tools|-70|84|(|Trace Folder Access|310|-1|-1|-1|Transfer Folder Ownership|311|-1|-1|-1|Separator|0|Customize Toolbar ...|101|-1|-1|-1|)|Plugins|-100000|80|)|"},
          {"MenuTreeModel."+com.CH_gui.frame.MessageFrame.visualsClassKeyName+"_VS_Ver3",        "Program Menus|-1|0|(|File|-10|70|(|Send|750|83|83|2|Save as Draft|764|-1|-1|-1|Separator|0|Select Recipients|751|82|82|2|Select Attachments|752|76|76|2|Voice Recording Panel|768|-1|-1|-1|Priority|-21|80|(|FYI|756|-1|49|2|Normal|757|-1|50|2|High|758|-1|51|2|)|All Headers|761|-1|-1|-1|Separator|0|Print ...|723|-1|-1|-1|Separator|0|Close|800|67|67|10|)|Edit|-23|69|(|Undo|759|85|90|2|Redo|760|82|89|2|Separator|0|Cut|753|88|88|2|Copy|754|67|67|2|Paste|755|86|86|2|)|Tools|-70|84|(|Spelling ...|765|-1|118|130|Edit user dictionary ...|766|-1|-1|-1|Spelling options ...|767|-1|-1|-1|Separator|0|Customize Toolbar ...|101|-1|-1|-1|)|Plugins|-100000|80|)|"},

          {"ServerList", URLs.get(URLs.DEFAULT_SERVER_1)+" "+URLs.get(URLs.DEFAULT_SERVER_2)},
          {"ServerList_cryptoheaven.com_80", "role EngineServer ver 0 rel 0 host cryptoheaven.com port 82|role EngineServer ver 0 rel 0 host d1.cryptoheaven.com port 80|role EngineServer ver 0 rel 0 host cryptoheaven.com port 4383|role EngineServer ver 0 rel 0 host http://d3.cryptoheaven.com port 80|"},

          {"ToolBarModel."+com.CH_gui.frame.AddressFrame.visualsClassKeyName+"_VS_Ver3",         "Send|750|Select Recipients|751|Select Attachments|752|Record|768|Separator|0|Undo|759|Redo|760|Separator|0|Cut|753|Copy|754|Paste|755|"},
          {"ToolBarModel."+com.CH_gui.frame.AddressTableFrame.visualsClassKeyName+"_VS_Ver3",    "Share Address Book ...|312|Separator|0|New Address to Folder|700|Compose to Address(es) ...|707|Forward Address(es) ...|705|Edit Address|715|Save Attachment(s) ...|706|Find|727|Print ...|723|Separator|0|Copy Address(es) ...|701|Move Address(es) ...|702|Delete Address(es) ...|703|Separator|0|Previous|720|Next|719|"},
          {"ToolBarModel."+com.CH_gui.frame.ChatTableFrame.visualsClassKeyName+"_VS_Ver3",       "Send|750|Open|716|Invite to the Conversation ...|312|Separator|0|Select Attachments|752|Record|768|Separator|0|Reply to Sender ...|707|Reply to All ...|722|Forward Message(s) ...|705|Post Reply to Folder ...|715|Save Attachment(s) ...|706|Find|727|Print ...|723|Separator|0|Mark as Read|709|Mark as Unread|710|Mark All Read|711|"},
          {"ToolBarModel."+com.CH_gui.frame.ContactTableFrame.visualsClassKeyName+"_VS_Ver3",    "New Message|503|Chat|508|Separator|0|Find Friends and Associates ...|500|Accept / Decline Contact(s) ...|501|Delete Contact ...|502|Separator|0|Invite Friends and Associates ...|509|"},
          {"ToolBarModel."+com.CH_gui.frame.GroupTableFrame.visualsClassKeyName+"_VS_Ver3",      "Add Member|312|"},
          {"ToolBarModel."+com.CH_gui.frame.FileTableFrame.visualsClassKeyName+"_VS_Ver3",       "Open|716|Share Folder ...|312|Separator|0|Download File ...|400|Forward File ...|705|Find|727|Separator|0|Copy File ...|701|Move File ...|702|Delete File ...|703|Separator|0|Mark as Seen|709|Mark as Unseen|710|Mark All Seen|711|"},
          {"ToolBarModel."+com.CH_gui.frame.FindUserFrame.visualsClassKeyName+"_VS_Ver3",        "Add to Contact List ...|600|Send Message ...|601|Separator|0|Invite Friends and Associates ...|1000|"},
          {"ToolBarModel."+com.CH_gui.frame.FolderTreeFrame.visualsClassKeyName+"_VS_Ver3",      "New Folder ...|300|Share Folder ...|312|Separator|0|Upload To Folder ...|303|Download Folder ...|304|Separator|0|New Message to Folder|700|Separator|0|Move Folder ...|301|Delete Folder ...|302|"},
          {"ToolBarModel."+com.CH_gui.frame.LocalFileTableFrame.visualsClassKeyName+"_VS_Ver3",  "Upload File ...|902|Wipe File ...|703|"},
          {"ToolBarModel."+com.CH_gui.frame.MainFrame.visualsClassKeyName+"_VS_Ver3",            "Open|716|New Folder ...|300|Share Folder ...|312|Separator|0|Upload To Folder ...|303|Download File ...|400|Download Folder ...|304|Separator|0|New Message|503|New Message to Folder|700|Reply to Sender ...|707|Reply to All ...|722|Forward Message(s) ...|705|Save Attachment(s) ...|706|Find|727|Print ...|723|Separator|0|Chat|508|Find Friends and Associates ...|500|Invite Friends and Associates ...|509|"},
          {"ToolBarModel."+com.CH_gui.frame.MessageFrame.visualsClassKeyName+"_VS_Ver3",         "Send|750|Select Recipients|751|Select Attachments|752|Record|768|Separator|0|Undo|759|Redo|760|Separator|0|Cut|753|Copy|754|Paste|755|Separator|0|Spelling ...|765|"},
          {"ToolBarModel."+com.CH_gui.frame.MsgPreviewFrame.visualsClassKeyName+"_VS_Ver3",      "Reply to Sender ...|707|Reply to All ...|722|Forward Message(s) ...|705|Save Attachment(s) ...|706|Separator|0|Add Sender to Address Book|721|Invite Sender|714|Print ...|723|Separator|0|Copy Message(s) ...|701|Move Message(s) ...|702|Delete Message(s) ...|703|Separator|0|Mark as Read|709|Mark as Unread|710|Separator|0|Previous|720|Next|719|"},
          {"ToolBarModel."+com.CH_gui.frame.MsgTableFrame.visualsClassKeyName+"_VS_Ver3",        "Share Folder ...|312|Separator|0|New Message to Folder|700|Reply to Sender ...|707|Reply to All ...|722|Forward Message(s) ...|705|Save Attachment(s) ...|706|Separator|0|Add Sender to Address Book|721|Invite Sender|714|Find|727|Print ...|723|Separator|0|Copy Message(s) ...|701|Move Message(s) ...|702|Delete Message(s) ...|703|Separator|0|Mark as Read|709|Mark as Unread|710|Mark All Read|711|"},
          {"ToolBarModel."+com.CH_gui.frame.PostTableFrame.visualsClassKeyName+"_VS_Ver3",       "Share Folder ...|312|Separator|0|New Message to Folder|700|Reply to Sender ...|707|Reply to All ...|722|Forward Message(s) ...|705|Save Attachment(s) ...|706|Add Sender to Address Book|721|Invite Sender|714|Find|727|Print ...|723|Separator|0|Copy Message(s) ...|701|Move Message(s) ...|702|Delete Message(s) ...|703|Separator|0|Mark as Read|709|Mark as Unread|710|Mark All Read|711|"},
          {"ToolBarModel."+com.CH_gui.frame.RecycleTableFrame.visualsClassKeyName+"_VS_Ver3",    "Download File ...|400|Forward ...|705|Find|727|Separator|0|Move ...|702|Delete ...|703|Separator|0|Empty Folder ...|1615|"},
          {"ToolBarModel."+com.CH_gui.frame.SubUserTableFrame.visualsClassKeyName+"_VS_Ver3",    "Create New|1300|Edit Account|1301|Activate or Suspend|1307|Password Reset|1308|Delete Account ...|1302|Separator|0|Send Message ...|1303|Manage Contacts|1306|"},
          {"ToolBarModel."+com.CH_gui.frame.WhiteListTableFrame.visualsClassKeyName+"_VS_Ver3",  "Share Address Book ...|312|Separator|0|Compose to Address(es) ...|707|Print ...|723|Find|727|Separator|0|Move Address(es) ...|702|Delete Address(es) ...|703|Separator|0|Mark All Read|711|Separator|0|Previous|720|Next|719|"},

          // Dialogs
          {com.CH_gui.dialog.AboutDialog.visualsClassKeyName+"_VS",            "Dimension width 437 height 580"},
          {com.CH_gui.dialog.AboutSecurityDialog.visualsClassKeyName+"_VS",    "Dimension width 437 height 580"},
          {com.CH_gui.dialog.FolderPropertiesDialog.visualsClassKeyName+"_VS", "Dimension width 410 height 490"},
          //{com.CH_gui.dialog.InviteByEmailDialog.visualsClassKeyName+"_VS",    "Dimension width 375 height 540"},
          {com.CH_gui.dialog.LicenseDialog.visualsClassKeyName+"_VS",          "Dimension width 437 height 580"},
          {com.CH_gui.dialog.Move_NewFld_Dialog.visualsClassKeyName+"_VS",     "Dimension width 410 height 490"},
          {com.CH_gui.dialog.MsgPropertiesDialog.visualsClassKeyName+"_VS",    "Dimension width 525 height 645"},
          {com.CH_gui.dialog.TraceRecordDialog.visualsClassKeyName+"_VS",      "Dimension width 450 height 320"},
          {com.CH_gui.dialog.UserSelectorDialog.visualsClassKeyName+"_VS",     "Dimension width 500 height 600"},

          //{com.CH_gui.msgs.MsgPreviewPanel.visualsClassKeyName+"_VS","Dimension width 700 height 274"},

          // Frames
          {com.CH_gui.frame.AddressFrame.visualsClassKeyName+"_VS_Ver3","Dimension width 670 height 550 Location x 0 y 0"},
          {com.CH_gui.frame.AddressTableFrame.visualsClassKeyName+"_VS_Ver3","Dimension width 770 height 735 Location x 0 y 0"},
          {com.CH_gui.frame.ChatTableFrame.visualsClassKeyName+"_VS_Ver3","Dimension width 625 height 515 Location x 0 y 0"},
          {com.CH_gui.frame.ContactTableFrame.visualsClassKeyName+"_VS_Ver3","Dimension width 275 height 392 Location x 0 y 0"},
          {com.CH_gui.frame.FileTableFrame.visualsClassKeyName+"_VS_Ver3","Dimension width 770 height 535 Location x 0 y 0"},
          {com.CH_gui.frame.FindUserFrame.visualsClassKeyName+"_VS_Ver3","Dimension width 500 height 600 Location x 0 y 0"},
          {com.CH_gui.frame.FolderTreeFrame.visualsClassKeyName+"_VS_Ver3","Dimension width 385 height 427 Location x 0 y 0"},
          {com.CH_gui.frame.GroupTableFrame.visualsClassKeyName+"_VS_Ver3","Dimension width 495 height 320 Location x 0 y 0"},
          {com.CH_gui.frame.LocalFileTableFrame.visualsClassKeyName+"_VS_Ver3","Dimension width 500 height 500 Location x 0 y 0"},
          {com.CH_gui.frame.MainFrame.visualsClassKeyName+"_VS_Ver3","Dimension width 880 height 640 Location x 0 y 0"},
          {com.CH_gui.frame.MessageFrame.visualsClassKeyName+"_VS_Ver3","Dimension width 670 height 550 Location x 0 y 0"},
          {com.CH_gui.frame.MsgPreviewFrame.visualsClassKeyName+"_VS_Ver3","Dimension width 700 height 575 Location x 0 y 0"},
          {com.CH_gui.frame.MsgTableFrame.visualsClassKeyName+"_VS_Ver3","Dimension width 800 height 735 Location x 0 y 0"},
          {com.CH_gui.frame.PostTableFrame.visualsClassKeyName+"_VS_Ver3","Dimension width 770 height 735 Location x 0 y 0"},
          {com.CH_gui.frame.RecycleTableFrame.visualsClassKeyName+"_VS_Ver3","Dimension width 770 height 535 Location x 0 y 0"},
          {com.CH_gui.frame.SubUserTableFrame.visualsClassKeyName+"_VS_Ver3","Dimension width 800 height 365 Location x 0 y 0"},
          {com.CH_gui.frame.WhiteListTableFrame.visualsClassKeyName+"_VS_Ver3","Dimension width 670 height 540 Location x 0 y 0"},

//          {com.CH_cl.util.JSplitPaneVS.visualsClassKeyName+"_"+com.CH_gui.dialog.RecordChooserDialog.visualsClassKeyName+"_hSplit1_VS","Divider location 164 181 0"},
//          {com.CH_cl.util.JSplitPaneVS.visualsClassKeyName+"_"+com.CH_gui.dialog.RecordChooserDialog.visualsClassKeyName+"_hSplit2_VS","Divider location 567 585 100"},
//          {com.CH_cl.util.JSplitPaneVS.visualsClassKeyName+"_"+com.CH_gui.frame.AddressTableFrame.visualsClassKeyName+"_addr_VS","Divider location 280 280 90"},
//          {com.CH_cl.util.JSplitPaneVS.visualsClassKeyName+"_"+com.CH_gui.frame.WhiteListTableFrame.visualsClassKeyName+"_addr_VS","Divider location 150 150 90"},
//          {com.CH_cl.util.JSplitPaneVS.visualsClassKeyName+"_"+com.CH_gui.frame.ChatTableFrame.visualsClassKeyName+"_chat_VS","Divider location 265 265 90"},
//          {com.CH_cl.util.JSplitPaneVS.visualsClassKeyName+"_"+com.CH_gui.frame.MainFrame.visualsClassKeyName+"_hSplit_VS","Divider location 235 235 20"},
//          {com.CH_cl.util.JSplitPaneVS.visualsClassKeyName+"_"+com.CH_gui.frame.MainFrame.visualsClassKeyName+"_vSplit_VS","Divider location 297 297 80"},
//          {com.CH_cl.util.JSplitPaneVS.visualsClassKeyName+"_"+com.CH_gui.frame.MsgTableFrame.visualsClassKeyName+"_msg_VS","Divider location 206 206 50"},
//          {com.CH_cl.util.JSplitPaneVS.visualsClassKeyName+"_"+com.CH_gui.table.TableComponent.visualsClassKeyName+"_addr_VS","Divider location 206 206 90"},
//          {com.CH_cl.util.JSplitPaneVS.visualsClassKeyName+"_"+com.CH_gui.table.TableComponent.visualsClassKeyName+"_chatComp_VS","Divider location 340 340 90"},
//          {com.CH_cl.util.JSplitPaneVS.visualsClassKeyName+"_"+com.CH_gui.table.TableComponent.visualsClassKeyName+"_msg_VS","Divider location 206 206 50"},
//          {com.CH_cl.util.JSplitPaneVS.visualsClassKeyName+"_"+com.CH_gui.table.TableComponent.visualsClassKeyName+"_sentMsg_VS","Divider location 206 206 50"},

//          {com.CH_gui.addressBook.AddressTableComponent.visualsClassKeyName+"_VS","Dimension width 700 height 205"},
//          {com.CH_gui.addressBook.WhiteListTableComponent.visualsClassKeyName+"_VS","Dimension width 662 height 449"},
//          {com.CH_gui.chatTable.ChatTableComponent.visualsClassKeyName+"_VS","Dimension width 700 height 339"},
//          {com.CH_gui.contactTable.ContactTableComponent.visualsClassKeyName+"_VS","Dimension width 232 height 183"},
//          {com.CH_gui.fileTable.FileTableComponent.visualsClassKeyName+"_VS","Dimension width 702 height 486"},
//          {com.CH_gui.keyTable.KeyTableComponent.visualsClassKeyName+"_VS","Dimension width 702 height 486"},
//          {com.CH_gui.localFileTable.FileChooserComponent.visualsClassKeyName+"_Browse_VS","Dimension width 702 height 486"},
//          {com.CH_gui.msgTable.MsgDraftsTableComponent.visualsClassKeyName+"_VS","Dimension width 700 height 205"},
//          {com.CH_gui.msgTable.MsgSentTableComponent.visualsClassKeyName+"_VS","Dimension width 700 height 205"},
//          {com.CH_gui.msgTable.MsgTableComponent.visualsClassKeyName+"_VS","Dimension width 700 height 205"},
//          {com.CH_gui.postTable.PostTableComponent.visualsClassKeyName+"_VS","Dimension width 702 height 486"},
//          {com.CH_gui.table.TableComponent.visualsClassKeyName+"_Browse_VS","Dimension width 702 height 486"},
//          {com.CH_gui.traceTable.TraceTableComponent.visualsClassKeyName+"_VS","Dimension width 412 height 180"},
//          {com.CH_gui.tree.FolderTreeComponent.visualsClassKeyName+"_VS","Dimension width 232 height 296"},

        };

      // set initial default properties if they were not loaded from file
      for (int i=0; i<DEFAULT_PROPERTIES.length; i++) {
        if (GlobalProperties.getProperty(DEFAULT_PROPERTIES[i][0]) == null)
          GlobalProperties.setProperty(DEFAULT_PROPERTIES[i][0], DEFAULT_PROPERTIES[i][1]);
      }
    } catch (Throwable t) {
    }
  }

  /*
   * List all environment properties in alphabetical order...
  static {
    Properties props = System.getProperties();
    Enumeration keyEnum = props.keys();
    TreeMap propMap = new TreeMap();
    while (keyEnum.hasMoreElements()) {
      String key = (String) keyEnum.nextElement();
      String value = props.getProperty(key);
      propMap.put(key, value);
    }
    Set keySet = propMap.keySet();
    Iterator keyIter = keySet.iterator();
    while (keyIter.hasNext()) {
      String key = (String) keyIter.next();
      String value = (String) propMap.get(key);
      System.out.println(key + "=" + value);
    }
  }
   */

  /*
   * List all UI defaults
  static {
    UIDefaults uiDefaults = UIManager.getDefaults();
    Enumeration keyEnum = uiDefaults.keys();
    while (keyEnum.hasMoreElements()) {
      Object key = keyEnum.nextElement();
      Object value = uiDefaults.get(key);
      System.out.println(key + "=" + value);
    }
  }
   */

  /*
  static {
    try {
      String laf = UIManager.getLookAndFeel().getID();
      if (laf.equalsIgnoreCase("Metal")) {
        String osName = System.getProperty("os.name");
        // check that Windows
        if (osName.toLowerCase().startsWith("windows")) {
          // overwrite checks
          boolean isWindowsVersionIgnore = true;
          boolean isJvmVersionIgnore = true;
          boolean isWindowsVersionOk = false;
          boolean isJvmVersionOk = false;
          if (!isWindowsVersionIgnore) {
            // check that Windows is at least version 5.1 (XP or higher)
            String osVersion = System.getProperty("os.version");
            java.util.StringTokenizer st = new java.util.StringTokenizer(osVersion, ". _");
            int osMajor = 0;
            int osMinor = 0;
            if (st.hasMoreTokens()) osMajor = Integer.parseInt(st.nextToken());
            if (st.hasMoreTokens()) osMinor = Integer.parseInt(st.nextToken());
            isWindowsVersionOk = osMajor > 5 || (osMajor == 5 && osMinor >= 1);
          }
          if (!isJvmVersionIgnore) {
            // check that java version is at least 1.4.2
            String verStr = System.getProperty("java.version");
            int jvmMajor = 0;
            int jvmMinor = 0;
            int jvmRel = 0;
            java.util.StringTokenizer st = new java.util.StringTokenizer(verStr, ". _");
            if (st.hasMoreTokens()) jvmMajor = Integer.parseInt(st.nextToken());
            if (st.hasMoreTokens()) jvmMinor = Integer.parseInt(st.nextToken());
            if (st.hasMoreTokens()) jvmRel = Integer.parseInt(st.nextToken());
            isJvmVersionOk = jvmMajor > 1 || (jvmMajor == 1 && (jvmMinor > 4 || (jvmMinor == 4 && jvmRel >= 2)));
          }
          if ((isWindowsVersionIgnore || isWindowsVersionOk) && (isJvmVersionIgnore || isJvmVersionOk)) {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
          }
        }
      }
    } catch (Throwable t) {
    }
  }
   */

  private static void setDebug() {
    if (DEBUG) {
      Sounds.DEBUG__SUPPRESS_ALL_SOUNDS = true;
      KeyRecord.DEBUG__ALLOW_SHORT_KEYS = true;
      MiscGui.suppressAllGUI();
    }
  }

  public static void main(String[] args) {
    // Controls of private label load
    boolean privateLabelLoaded = false;

    // Save original arguments incase we need to restart the application
    if (args != null)
      AutoUpdater.setOriginalArgs(args);

    if (args != null && args.length == 1 && args[0].equals("-version")) {
      System.out.println(GlobalProperties.PROGRAM_FULL_NAME);
      System.exit(0);
    }

    // Ability to recrypt locally stored keys for new name and password.
    if (args != null && args.length > 0) {
      if (args[0].equals("-localKeyChangePass")) {
        try {
          String sKeyId = args[1];
          String oldName = args[2];
          String oldPass = args[3];
          String newName = args[4];
          String newPass = args[5];

          // Check if the encrypted private part of the key is stored remotely... if so we will need to send an update.
          String keyPropertyName = "Enc" + RSAPrivateKey.OBJECT_NAME + "_" + sKeyId;
          GlobalSubProperties keyProperties = new GlobalSubProperties(GlobalSubProperties.PROPERTY_EXTENSION_KEYS);
          String oldProperty = keyProperties.getProperty(keyPropertyName);
          if (oldProperty == null || oldProperty.length() == 0) {
            throw new IllegalArgumentException("Key not found.");
          } else {
            BASymCipherBlock oldEncPrivateKey = new BASymCipherBlock(ArrayUtils.toByteArray(oldProperty));
            BAEncodedPassword oldPasscode = UserRecord.getBAEncodedPassword(oldPass.toCharArray(), oldName);
            BAEncodedPassword newPasscode = UserRecord.getBAEncodedPassword(newPass.toCharArray(), newName);

            SymmetricSmallBlockCipher symCipher = new SymmetricSmallBlockCipher(oldPasscode);
            // see if our encoded password will decrypt the encrypted private key
            BASymPlainBlock privateKeyBA =  symCipher.blockDecrypt(oldEncPrivateKey);
            symCipher = new SymmetricSmallBlockCipher(newPasscode);
            BASymCipherBlock newEncPrivateKey = symCipher.blockEncrypt(privateKeyBA);
            keyProperties.setProperty(keyPropertyName, newEncPrivateKey.getHexContent());
            keyProperties.store();
          }

        } catch (Throwable t) {
          System.out.println("Error: " + t.getMessage());
          System.out.println("Arguments: -localKeyChangePass <localKeyId> <old name> <old password> <new name> <new password>");
          System.out.println("Example: -localKeyChangePass 56789 \"John Blank\" oldpass \"John New\" newpass");
        }
        System.exit(0);
      } else if (args[0].equals("-privateLabelURL")) {
        String privateLabelURL = args[1];
        try {
          URLs.loadPrivateLabel(privateLabelURL);
          privateLabelLoaded = true;
        } catch (Throwable t) {
          System.out.println("Error loading private label: " + privateLabelURL);
          System.out.println("Arguments: -privateLabelURL <URL-of-xml-file>");
          System.out.println("Example: -privateLabelURL http://mycompany.com/my-cryptoheaven.xml");
        }
      }
    }

    if (!privateLabelLoaded && URLs.hasPrivateLabelCustomizationClass()) {
      String privateLabelURL = URLs.getPrivateLabelSettingsURL();
      try {
        URLs.loadPrivateLabel(privateLabelURL);
        privateLabelLoaded = true;
      } catch (Throwable t) {
        System.out.println("Error loading private label: " + privateLabelURL);
      }
    }

    setDebug();
    JWindow splashWindow = new JWindow();
    Container c = splashWindow.getContentPane();
    JLabel splashImage = new JLabel();
    splashImage.setIcon(Images.get(ImageNums.LOGO_KEY_MAIN));
    c.add(splashImage, BorderLayout.CENTER);
    splashWindow.pack();
    MiscGui.setSuggestedWindowLocation(null, splashWindow);
    splashWindow.setVisible(true);

    com.CH_gui.frame.MainFrameStarter.main(args, splashWindow);

    if (splashWindow.isShowing()) {
      splashWindow.setVisible(false);
      splashWindow.dispose();
    }
  }

}