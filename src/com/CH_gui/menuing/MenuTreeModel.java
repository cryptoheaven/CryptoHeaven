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

package com.CH_gui.menuing;

import java.util.*;

import java.awt.Event;
import java.awt.event.KeyEvent;

import javax.swing.*;
import javax.swing.tree.*;

import com.CH_gui.action.Actions;
import com.CH_gui.actionGui.*;
import com.CH_gui.gui.*;
import com.CH_guiLib.gui.*;

import com.CH_co.trace.Trace;
import com.CH_co.util.*;

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
 * <b>$Revision: 1.47 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class MenuTreeModel extends Object {

  public static final int PLUGINS_ID = -100000;

  /**
   * JMenus(s) do not have accelerator specified.
   * JSeparator(s) do not have mnemonic and accelerator specified.
   *
   * Format of menu items:
   *    for all JMenuItem(s) where action id is +ve
   *      <menu item name>|<actionId>|<mnemonic>|<key code>|<modifiers>|
   *    for Separator(s) where action id = 0
   *      <menu item name>|<actionId>|
   *    for JMenu(s) where action id is -ve
   *      <menu item name>|<actionId>|<mnemonic>|
   *
   * Submenus are groupped with "(|" ... ")|"
   */
  private static String EMPTY_MENU_SEQUENCE =
    "Program Menus|-1|0|"+
      "(|"+
        // Main Frame
        "File|-10|"+KeyEvent.VK_F+"|"+
          "(|"+
            // Message Table Starter Frame
            "Switch to Full Application|1400|-1|-1|-1|"+
            // File Table
            "Open|716|"+KeyEvent.VK_O+"|"+KeyEvent.VK_O+"|"+Event.CTRL_MASK+"|"+
            // Accounts Management Frame
            "Create New ...|1300|"+KeyEvent.VK_N+"|"+KeyEvent.VK_N+"|"+Event.CTRL_MASK+"|"+
            "Edit Account ...|1301|"+KeyEvent.VK_E+"|"+KeyEvent.VK_E+"|"+Event.CTRL_MASK+"|"+
            "Activate or Suspend ...|1307|-1|-1|-1|"+
            "Password Reset ...|1308|-1|-1|-1|"+
            "Delete Account ...|1302|-1|-1|-1|"+
            "Separator|0|"+
            "Manage Contacts ...|1306|-1|-1|-1|"+
            "Message User ...|1303|"+KeyEvent.VK_M+"|"+KeyEvent.VK_M+"|"+Event.CTRL_MASK+"|"+
            // Find User Frame
            "Add to Contact List ...|600|-1|-1|-1|"+
            "Message User(s) ...|601|"+KeyEvent.VK_M+"|"+KeyEvent.VK_M+"|"+Event.CTRL_MASK+"|"+
            "Compose Email Invitation ...|1000|-1|-1|-1|"+
            "Separator|0|"+
            // Main Frame
            "New Folder ...|300|"+KeyEvent.VK_N+"|"+KeyEvent.VK_N+"|"+Event.CTRL_MASK+"|"+
            "Create Shared Space ...|510|-1|-1|-1|"+
            "Share Folder ...|312|-1|-1|-1|"+
            "New Message|503|"+KeyEvent.VK_N+"|"+KeyEvent.VK_M+"|"+Event.CTRL_MASK+"|"+
            "New Address|511|-1|-1|-1|"+
            "Separator|0|"+
            "Upload File ...|902|"+KeyEvent.VK_U+"|"+KeyEvent.VK_U+"|"+(Event.CTRL_MASK|Event.ALT_MASK)+"|"+
            "Upload ...|303|"+KeyEvent.VK_U+"|"+KeyEvent.VK_U+"|"+Event.CTRL_MASK+"|"+
            "Download File(s) ...|400|"+KeyEvent.VK_D+"|"+KeyEvent.VK_D+"|"+(Event.CTRL_MASK|Event.ALT_MASK)+"|"+
            "Download Folder(s) ...|304|"+KeyEvent.VK_D+"|"+KeyEvent.VK_D+"|"+Event.CTRL_MASK+"|"+
            "Separator|0|"+
            "Forward File To ...|705|"+KeyEvent.VK_F+"|"+KeyEvent.VK_F+"|"+Event.CTRL_MASK+"|"+
            "Separator|0|"+
            "Print ...|723|-1|-1|-1|"+
            "Separator|0|"+
            "Import Address Book ...|212|-1|-1|-1|"+
            "Switch Identity|209|-1|-1|-1|"+
            "Separator|0|"+
            "Folder Properties and Sharing|305|-1|-1|-1|"+
            "File Properties|404|-1|-1|-1|"+
            "Separator|0|"+
            // All Closable Frames
            "Close|800|"+KeyEvent.VK_C+"|"+KeyEvent.VK_C+"|"+(Event.CTRL_MASK|Event.ALT_MASK)+"|"+
            // Main Frame
            "Exit|200|"+KeyEvent.VK_X+"|"+KeyEvent.VK_X+"|"+(Event.CTRL_MASK|Event.ALT_MASK)+"|"+
          ")|"+
        // Message Frame
        "Message|-20|"+KeyEvent.VK_M+"|"+
          "(|"+
            "New Message to Folder|700|"+KeyEvent.VK_M+"|"+KeyEvent.VK_M+"|"+(Event.CTRL_MASK|Event.ALT_MASK)+"|"+
            "New Message from Draft|718|-1|-1|-1|"+
            "Separator|0|"+
            "Reply to Sender ...|707|"+KeyEvent.VK_R+"|"+KeyEvent.VK_R+"|"+Event.CTRL_MASK+"|"+
            "Reply to All ...|722|-1|"+KeyEvent.VK_R+"|"+(Event.CTRL_MASK+Event.SHIFT_MASK)+"|"+
            "Post Reply to Folder ...|715|"+KeyEvent.VK_P+"|"+KeyEvent.VK_P+"|"+Event.CTRL_MASK+"|"+
            "Save Attachment(s) ...|706|"+KeyEvent.VK_T+"|"+KeyEvent.VK_T+"|"+Event.CTRL_MASK+"|"+
            "Invite Sender ...|714|-1|-1|-1|"+
            "Add Sender to Address Book ...|721|-1|-1|-1|"+
            "Separator|0|"+
            "Previous|720|-1|-1|-1|"+
            "Next|719|-1|-1|-1|"+
            "Separator|0|"+
            "Message Properties|704|-1|-1|-1|"+
          ")|"+
        // Compose Message Frame
        "Chat|-22|"+KeyEvent.VK_C+"|"+
          "(|"+
            "Chat|508|"+KeyEvent.VK_H+"|"+KeyEvent.VK_H+"|"+Event.CTRL_MASK+"|"+
            "Send|750|"+KeyEvent.VK_S+"|"+KeyEvent.VK_S+"|"+Event.CTRL_MASK+"|"+
            "Save as Draft|764|-1|-1|-1|"+
            "Select Recipients|751|"+KeyEvent.VK_R+"|"+KeyEvent.VK_R+"|"+Event.CTRL_MASK+"|"+
            "Select Attachments|752|"+KeyEvent.VK_L+"|"+KeyEvent.VK_L+"|"+Event.CTRL_MASK+"|"+
            "Voice Recording Panel|768|-1|-1|-1|"+
            "Priority|-21|"+KeyEvent.VK_P+"|"+
              "(|"+
                "FYI|756|-1|"+KeyEvent.VK_1+"|"+Event.CTRL_MASK+"|"+
                "Normal|757|-1|"+KeyEvent.VK_2+"|"+Event.CTRL_MASK+"|"+
                "High|758|-1|"+KeyEvent.VK_3+"|"+Event.CTRL_MASK+"|"+
              ")|"+
            "All Headers|761|-1|-1|-1|"+
            "Separator|0|"+
            "Undo|759|"+KeyEvent.VK_U+"|"+KeyEvent.VK_Z+"|"+Event.CTRL_MASK+"|"+
            "Redo|760|"+KeyEvent.VK_R+"|"+KeyEvent.VK_Y+"|"+Event.CTRL_MASK+"|"+
            "Separator|0|"+
            "Cut|753|"+KeyEvent.VK_X+"|"+KeyEvent.VK_X+"|"+Event.CTRL_MASK+"|"+
            "Copy|754|"+KeyEvent.VK_C+"|"+KeyEvent.VK_C+"|"+Event.CTRL_MASK+"|"+
            "Paste|755|"+KeyEvent.VK_V+"|"+KeyEvent.VK_V+"|"+Event.CTRL_MASK+"|"+
          ")|"+
        "Contacts|-30|"+KeyEvent.VK_N+"|"+
          "(|"+
            "Find Friends and Associates ...|500|-1|-1|-1|"+
            "Invite Friends and Associates ...|509|-1|-1|-1|"+
            "Create Group|512|-1|-1|-1|"+
            "Accept / Decline Contact ...|501|-1|-1|-1|"+
            "Delete Contact ...|502|-1|-1|-1|"+
            "Separator|0|"+
            "Show Other's Contacts|506|-1|-1|-1|"+
            "Contact Properties|504|-1|-1|-1|"+
          ")|"+
        "Edit|-40|"+KeyEvent.VK_E+"|"+
          "(|"+
            "Mark as Seen|709|"+KeyEvent.VK_R+"|"+KeyEvent.VK_R+"|"+(Event.CTRL_MASK|Event.ALT_MASK)+"|"+
            "Mark as Unseen|710|"+KeyEvent.VK_U+"|"+KeyEvent.VK_U+"|"+(Event.CTRL_MASK|Event.ALT_MASK)+"|"+
            "Mark All Seen|711|"+KeyEvent.VK_A+"|"+KeyEvent.VK_A+"|"+(Event.CTRL_MASK|Event.ALT_MASK)+"|"+
            "Separator|0|"+
            "Move Folder ...|301|-1|-1|-1|"+
            "Delete Folder(s)|302|-1|-1|-1|"+
            "Separator|0|"+
            "Copy Message(s) ...|701|-1|-1|-1|"+
            "Move Message(s) ...|702|-1|-1|-1|"+
            "Delete Message(s)|703|"+KeyEvent.VK_DELETE+"|"+KeyEvent.VK_DELETE+"|0|"+
            "Revoke Message(s)|724|-1|-1|-1|"+
            "Empty Recycle Bin ...|314|-1|-1|-1|"+
            "Empty Folder ...|1615|-1|-1|-1|"+
          ")|"+
        "View|-50|"+KeyEvent.VK_V+"|"+
          "(|"+
            "Clone Folder View|307|-1|-1|-1|"+
            "Explore Folder|308|-1|-1|-1|"+
            "Separator|0|"+
            "Clone File View|410|-1|-1|-1|"+
            "Clone Message View|712|-1|-1|-1|"+
            "Clone Contact List View|507|-1|-1|-1|"+
            "Clone Account List View|1305|-1|-1|-1|"+
            "Separator|0|"+
            "Find|727|-1|-1|-1|"+
            "Split Top-Bottom|726|-1|-1|-1|"+
            "Table Columns ...|1202|-1|-1|-1|"+
            "Sort Table By|-51|"+KeyEvent.VK_S+"|"+
              "(|"+
                "col_1|1203|-1|-1|-1|col_2|1204|-1|-1|-1|col_3|1205|-1|-1|-1|col_4|1206|-1|-1|-1|col_5|1207|-1|-1|-1|col_6|1208|-1|-1|-1|col_7|1209|-1|-1|-1|col_8|1210|-1|-1|-1|col_9|1211|-1|-1|-1|col_10|1212|-1|-1|-1|col_11|1213|-1|-1|-1|col_12|1214|-1|-1|-1|col_13|1215|-1|-1|-1|col_14|1216|-1|-1|-1|col_15|1217|-1|-1|-1|col_16|1218|-1|-1|-1|col_17|1219|-1|-1|-1|col_18|1220|-1|-1|-1|col_19|1221|-1|-1|-1|col_20|1222|-1|-1|-1|col_21|1223|-1|-1|-1|col_22|1224|-1|-1|-1|col_23|1225|-1|-1|-1|col_24|1226|-1|-1|-1|col_25|1227|-1|-1|-1|col_26|1228|-1|-1|-1|"+
                "Separator|0|"+
                "Ascending|1200|-1|-1|-1|"+
                "Descending|1201|-1|-1|-1|"+
                "Separator|0|"+
                "Group Messages by Conversation|717|-1|-1|-1|"+
              ")|"+
            "Separator|0|"+
            "Contact Columns ...|524|-1|-1|-1|"+
            "Sort Contacts By|-52|"+KeyEvent.VK_S+"|"+
              "(|"+
                "Direction|515|-1|-1|-1|Name|516|-1|-1|-1|Status|517|-1|-1|-1|Contact ID|518|-1|-1|-1|User ID|519|-1|-1|-1|Encryption|520|-1|-1|-1|Created|521|-1|-1|-1|Updated|522|-1|-1|-1|Permissions|523|-1|-1|-1|"+
                "Separator|0|"+
                "Ascending|513|-1|-1|-1|"+
                "Descending|514|-1|-1|-1|"+
              ")|"+
            "Separator|0|"+
            "Refresh Folders|306|-1|-1|-1|"+
            "Refresh Messages|708|-1|-1|-1|"+
            "Refresh Contacts|505|-1|-1|-1|"+
            "Refresh Accounts|1304|-1|-1|-1|"+
          ")|"+
        "Tools|-70|"+KeyEvent.VK_T+"|"+
          "(|"+
            "Account Options|204|-1|-1|-1|"+
            "Allowed Senders|214|-1|-1|-1|"+
            "Change Username|208|-1|-1|-1|"+
            "Change Password|202|-1|-1|-1|"+
            "Setup Password Recovery|215|-1|-1|-1|"+
            "Delete Account|211|-1|-1|-1|"+
            "Connection Options|203|-1|-1|-1|"+
            "Separator|0|"+
            "Create Sub-Accounts|210|-1|-1|-1|"+
            "Separator|0|"+
            "Trace Folder Access|310|-1|-1|-1|"+
            "Trace Message Access|713|-1|-1|-1|"+
            "Transfer Folder Ownership|311|-1|-1|-1|"+
            "Separator|0|"+
            "Spell Checker|-72|"+KeyEvent.VK_S+"|"+
              "(|"+
                "Spelling ...|765|-1|"+KeyEvent.VK_F7+"|"+Event.CTRL_MASK+"|" +
                "Edit user dictionary|766|-1|-1|-1|" +
                "Spelling options|767|-1|-1|-1|" +
              ")|"+
            "Separator|0|"+
            "Customize Toolbar ...|101|-1|-1|-1|"+
            "Customize Menu ...|102|-1|-1|-1|"+
            "Tool Tips|100|-1|-1|-1|"+
            "Look and Feel|-71|"+KeyEvent.VK_L+"|"+
              "(|"+
                "Look_a|110|"+KeyEvent.VK_1+"|"+KeyEvent.VK_1+"|"+(Event.CTRL_MASK|Event.SHIFT_MASK|Event.ALT_MASK)+"|"+
                "Look_b|111|"+KeyEvent.VK_2+"|"+KeyEvent.VK_2+"|"+(Event.CTRL_MASK|Event.SHIFT_MASK|Event.ALT_MASK)+"|"+
                "Look_c|112|"+KeyEvent.VK_3+"|"+KeyEvent.VK_3+"|"+(Event.CTRL_MASK|Event.SHIFT_MASK|Event.ALT_MASK)+"|"+
                "Look_d|113|"+KeyEvent.VK_4+"|"+KeyEvent.VK_4+"|"+(Event.CTRL_MASK|Event.SHIFT_MASK|Event.ALT_MASK)+"|"+
                "Look_e|114|"+KeyEvent.VK_5+"|"+KeyEvent.VK_5+"|"+(Event.CTRL_MASK|Event.SHIFT_MASK|Event.ALT_MASK)+"|"+
                "Look_f|115|"+KeyEvent.VK_6+"|"+KeyEvent.VK_6+"|"+(Event.CTRL_MASK|Event.SHIFT_MASK|Event.ALT_MASK)+"|"+
                "Look_g|116|"+KeyEvent.VK_7+"|"+KeyEvent.VK_7+"|"+(Event.CTRL_MASK|Event.SHIFT_MASK|Event.ALT_MASK)+"|"+
                "Look_h|117|"+KeyEvent.VK_8+"|"+KeyEvent.VK_8+"|"+(Event.CTRL_MASK|Event.SHIFT_MASK|Event.ALT_MASK)+"|"+
                "Look_i|118|"+KeyEvent.VK_9+"|"+KeyEvent.VK_9+"|"+(Event.CTRL_MASK|Event.SHIFT_MASK|Event.ALT_MASK)+"|"+
              ")|"+
            "Separator|0|"+
          ")|"+
        "Help|-100|"+KeyEvent.VK_H+"|"+
          "(|"+
          "General FAQ|205|-1|-1|-1|"+
          "Quick Tour|206|-1|-1|-1|"+
          "User's Guide|207|-1|-1|-1|"+
          "Account Upgrade and Renewal|213|-1|-1|-1|"+
          "Separator|0|"+
          "Email Support|217|-1|-1|-1|"+
          "Bug Reporting|216|-1|-1|-1|"+
          "Separator|0|"+
          "About "+URLs.get(URLs.SERVICE_SOFTWARE_NAME)+"|201|-1|-1|-1|"+
          ")|"+
        "Plugins|"+PLUGINS_ID+"|"+KeyEvent.VK_P+"|"+
      ")|";

  private static String EMPTY_MENU_POPUP_SEQUENCE =
    "Program Menus|-1|0|"+
      "(|"+
        // Main Frame
        "File|-10|-1|"+
          "(|"+
            // Accounts Management Frame
            "Create New ...|1300|"+KeyEvent.VK_N+"|"+KeyEvent.VK_N+"|"+Event.CTRL_MASK+"|"+
            "Edit Account ...|1301|"+KeyEvent.VK_E+"|"+KeyEvent.VK_E+"|"+Event.CTRL_MASK+"|"+
            "Activate or Suspend ...|1307|-1|-1|-1|"+
            "Password Reset ...|1308|-1|-1|-1|"+
            "Delete Account ...|1302|-1|-1|-1|"+
            "Separator|0|"+
            "Manage Contacts ...|1306|-1|-1|-1|"+
            "Message User ...|1303|"+KeyEvent.VK_M+"|"+KeyEvent.VK_M+"|"+Event.CTRL_MASK+"|"+
            // Find User Frame
            "Add to Contact List ...|600|-1|-1|-1|"+
            "Message User(s) ...|601|-1|-1|-1|"+
            "Compose Email Invitation ...|1000|"+KeyEvent.VK_E+"|"+KeyEvent.VK_E+"|"+Event.CTRL_MASK+"|"+
            "Separator|0|"+
            // Main Frame
            "New Message|503|-1|-1|-1|"+
            "Chat|508|-1|-1|-1|"+
            "New Folder ...|300|-1|-1|-1|"+
            "Create Shared Space ...|510|-1|-1|-1|"+
            //"Separator|0|"+
            "Upload File ...|902|-1|-1|-1|"+
            "Upload ...|303|-1|-1|-1|"+
            //"Separator|0|"+
            "Open|716|-1|-1|-1|"+
            "Download File(s) ...|400|-1|-1|-1|"+
            "Download Folder(s) ...|304|-1|-1|-1|"+
            //"Separator|0|"+
            // <<< Message Menu - part a
            "New Message to Folder|700|-1|-1|-1|"+
            "New Message from Draft|718|-1|-1|-1|"+
            "Reply to Sender ...|707|-1|-1|-1|"+
            "Reply to All ...|722|-1|-1|-1|"+
            // >>> Message Menu - part a
            "Forward File To ...|705|-1|-1|-1|"+
            // <<< Message Menu - part b
            "Post Reply to Folder ...|715|-1|-1|-1|"+
            "Save Attachment(s) ...|706|-1|-1|-1|"+
            "New Message to Group...|1501|-1|-1|-1|"+ // new message to the selected Group
            "New Message to Member...|1502|-1|-1|-1|"+ // new message to the selected Member
            "Share Folder ...|312|-1|-1|-1|"+
            "Share Folder ...|313|-1|-1|-1|"+
            "Invite Sender  ...|714|-1|-1|-1|"+
            "Add Sender to Address Book ...|721|-1|-1|-1|"+
            "Separator|0|"+
            "Previous|720|-1|-1|-1|"+
            "Next|719|-1|-1|-1|"+
            // >>> Message Menu - part a
          ")|"+
        // Message Frame
        "Message|-20|-1|"+
          "(|"+
            //"Forward Message To ...|705|-1|-1|-1|"+
          ")|"+
        // Compose Message Frame
        "Message|-22|-1|"+
          "(|"+
            "Send|750|-1|-1|-1|"+
            "Save as Draft|764|-1|-1|-1|"+
            "Select Recipients|751|-1|-1|-1|"+
            "Select Attachments|752|-1|-1|-1|"+
            "Voice Recording Panel|768|-1|-1|-1|"+
            "Priority|-21|-1|"+
              "(|"+
                "FYI|756|-1|-1|-1|"+
                "Normal|757|-1|-1|-1|"+
                "High|758|-1|-1|-1|"+
              ")|"+
            "All Headers|761|-1|-1|-1|"+
            "Separator|0|"+
            "Undo|759|-1|-1|-1|"+
            "Redo|760|-1|-1|-1|"+
            "Separator|0|"+
            "Cut|753|-1|-1|-1|"+
            "Copy|754|-1|-1|-1|"+
            "Paste|755|-1|-1|-1|"+
          ")|"+
        "Contacts|-30|-1|"+
          "(|"+
            "Find Friends and Associates ...|500|-1|-1|-1|"+
            "Invite Friends and Associates ...|509|-1|-1|-1|"+
            "Add to Address Book|511|-1|-1|-1|"+
            "Create Group|512|-1|-1|-1|"+
            "Accept / Decline Contact ...|501|-1|-1|-1|"+
            "Delete Contact ...|502|-1|-1|-1|"+
          ")|"+
        "Edit|-40|-1|"+
          "(|"+
            "Mark as Seen|709|-1|-1|-1|"+
            "Mark as Unseen|710|-1|-1|-1|"+
            "Mark All Seen|711|-1|-1|-1|"+
            "Separator|0|"+
            "Move Folder ...|301|-1|-1|-1|"+
            "Delete Folder(s)|302|-1|-1|-1|"+
            //"Separator|0|"+
            "Copy Message(s) ...|701|-1|-1|-1|"+
            "Move Message(s) ...|702|-1|-1|-1|"+
            "Delete Message(s) ...|703|"+KeyEvent.VK_DELETE+"|"+KeyEvent.VK_DELETE+"|0|"+
            "Revoke Message(s)|724|-1|-1|-1|"+
            "Empty Recycle Bin ...|314|-1|-1|-1|"+
            "Empty Folder ...|1615|-1|-1|-1|"+
            "Print ...|723|-1|-1|-1|"+
          ")|"+
        "View|-50|-1|"+
          "(|"+
            "Clone Folder View|307|-1|-1|-1|"+
            "Explore Folder|308|-1|-1|-1|"+
            "Clone File View|410|-1|-1|-1|"+
            "Clone Message View|712|-1|-1|-1|"+
            "Show Other's Contacts|506|-1|-1|-1|"+
            "Clone Contact List View|507|-1|-1|-1|"+
            "Clone Account List View|1305|-1|-1|-1|"+
            "Find|727|-1|-1|-1|"+
            "Split Top-Bottom|726|-1|-1|-1|"+
            "Table Columns ...|1202|-1|-1|-1|"+
            "Sort Table By|-51|"+KeyEvent.VK_S+"|"+
              "(|"+
                "col_1|1203|-1|-1|-1|col_2|1204|-1|-1|-1|col_3|1205|-1|-1|-1|col_4|1206|-1|-1|-1|col_5|1207|-1|-1|-1|col_6|1208|-1|-1|-1|col_7|1209|-1|-1|-1|col_8|1210|-1|-1|-1|col_9|1211|-1|-1|-1|col_10|1212|-1|-1|-1|col_11|1213|-1|-1|-1|col_12|1214|-1|-1|-1|col_13|1215|-1|-1|-1|col_14|1216|-1|-1|-1|col_15|1217|-1|-1|-1|col_16|1218|-1|-1|-1|col_17|1219|-1|-1|-1|col_18|1220|-1|-1|-1|col_19|1221|-1|-1|-1|col_20|1222|-1|-1|-1|col_21|1223|-1|-1|-1|col_22|1224|-1|-1|-1|col_23|1225|-1|-1|-1|col_24|1226|-1|-1|-1|col_25|1227|-1|-1|-1|col_26|1228|-1|-1|-1|"+
                "Separator|0|"+
                "Ascending|1200|-1|-1|-1|"+
                "Descending|1201|-1|-1|-1|"+
                "Separator|0|"+
                "Group Messages by Conversation|717|-1|-1|-1|"+
              ")|"+
            "Contact Columns ...|524|-1|-1|-1|"+
            "Sort Contacts By|-52|"+KeyEvent.VK_S+"|"+
              "(|"+
                "Direction|515|-1|-1|-1|Name|516|-1|-1|-1|Status|517|-1|-1|-1|Contact ID|518|-1|-1|-1|User ID|519|-1|-1|-1|Encryption|520|-1|-1|-1|Created|521|-1|-1|-1|Updated|522|-1|-1|-1|Permissions|523|-1|-1|-1|"+
                "Separator|0|"+
                "Ascending|513|-1|-1|-1|"+
                "Descending|514|-1|-1|-1|"+
              ")|"+
            "Refresh Folders|306|-1|-1|-1|"+
            "Refresh Messages|708|-1|-1|-1|"+
            "Refresh Contacts|505|-1|-1|-1|"+
            "Refresh Accounts|1304|-1|-1|-1|"+
          ")|"+
        "Properties|-60|-1|"+
          "(|"+
            "Trace Folder Access|310|-1|-1|-1|"+
            "Trace Message Access|713|-1|-1|-1|" +
            "Transfer Folder Ownership|311|-1|-1|-1|" +
            "Folder Properties and Sharing|305|-1|-1|-1|"+
            "File Properties|404|-1|-1|-1|"+
            "Message Properties|704|-1|-1|-1|"+
            "Contact Properties|504|-1|-1|-1|"+
          ")|"+
        "Tools|-70|-1|"+
          "(|"+
            "Account Options|204|-1|-1|-1|"+
            "Allowed Senders|214|-1|-1|-1|"+
            "Change Username|208|-1|-1|-1|"+
            "Change Password|202|-1|-1|-1|"+
            "Setup Password Recovery|215|-1|-1|-1|"+
            "Delete Account|211|-1|-1|-1|"+
            "Connection Options|203|-1|-1|-1|"+
            "Separator|0|"+
            "Create Sub-Accounts|210|-1|-1|-1|"+
            "Separator|0|"+
            "Spell Checker|-72|-1|"+
              "(|"+
                "Spelling ...|765|-1|-1|-1|" +
                "Edit user dictionary|766|-1|-1|-1|" +
                "Spelling options|767|-1|-1|-1|" +
              ")|"+
            "Separator|0|"+
            "Customize Toolbar ...|101|-1|-1|-1|"+
            "Customize Menu ...|102|-1|-1|-1|"+
            "Tool Tips|100|-1|-1|-1|"+
            "Look and Feel|-71|-1|"+
              "(|"+
                "Look_a|110|-1|-1|-1|"+
                "Look_b|111|-1|-1|-1|"+
                "Look_c|112|-1|-1|-1|"+
                "Look_d|113|-1|-1|-1|"+
                "Look_e|114|-1|-1|-1|"+
                "Look_f|115|-1|-1|-1|"+
                "Look_g|116|-1|-1|-1|"+
                "Look_h|117|-1|-1|-1|"+
                "Look_i|118|-1|-1|-1|"+
              ")|"+
            "Separator|0|"+
          ")|"+
        "Help|-100|-1|"+
          "(|"+
          "General FAQ|205|-1|-1|-1|"+
          "Quick Tour|206|-1|-1|-1|"+
          "User's Guide|207|-1|-1|-1|"+
          "Account Upgrade and Renewal|213|-1|-1|-1|"+
          "Email Support|217|-1|-1|-1|"+
          "Bug Reporting|216|-1|-1|-1|"+
          "Separator|0|"+
          "About "+URLs.get(URLs.SERVICE_SOFTWARE_NAME)+"|201|-1|-1|-1|"+
          ")|"+
        "File|-200|-1|"+
          "(|"+
            "Import Address Book ...|212|-1|-1|-1|"+
            "Switch Identity|209|-1|-1|-1|"+
            // All Closable Frames
            "Close|800|-1|-1|-1|"+
            // Main Frame
            "Exit|200|-1|-1|-1|"+
          ")|"+
        "Plugins|"+PLUGINS_ID+"|-1|"+
      ")|";



  private String menuPropertyName;
  /** GUI reflection of the model */
  private JMenuBar jMenuBar;

  private DefaultTreeModel treeModel;
  private DefaultTreeModel treeModelPopup;
  private HashMap treeModelHM;
  private HashMap treeModelPopupHM;

  private boolean isInitialized = false;

  /** Creates new MenuTreeModel */
  public MenuTreeModel() {
    this("defaultMenuTreeModel");
  }
  /** Creates new MenuTreeModel */
  public MenuTreeModel(String menuPropertyName) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MenuTreeModel.class, "MenuTreeModel(String menuPropertyName)");
    if (trace != null) trace.args(menuPropertyName);

    this.menuPropertyName = menuPropertyName;

    /** GUI placeholder for this model. */
    jMenuBar = new JMenuBar();

    if (trace != null) trace.exit(MenuTreeModel.class);
  }

  private void initialize() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MenuTreeModel.class, "initialize()");
    if (trace != null) trace.args(menuPropertyName);

    if (!isInitialized) {
      isInitialized = true;

      Object[] _treeModelPopup = buildModel("PopupTreeModel."+menuPropertyName, EMPTY_MENU_POPUP_SEQUENCE);
      // First build the popup menu model, then overwrite hot keys with real menu tree model.
      treeModelPopup = (DefaultTreeModel) _treeModelPopup[0];
      treeModelPopupHM = (HashMap) _treeModelPopup[1];

      Object[] _treeModel = buildModel("MenuTreeModel."+menuPropertyName, EMPTY_MENU_SEQUENCE);
      treeModel = (DefaultTreeModel) _treeModel[0];
      treeModelHM = (HashMap) _treeModel[1];
    }

    if (trace != null) trace.exit(MenuTreeModel.class);
  }

  private static Object[] buildModel(String propertyName, String emptySequence) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MenuTreeModel.class, "initialize(String propertyName, String emptySequence)");
    if (trace != null) trace.args(propertyName, emptySequence);

    String menuSequence = GlobalProperties.getProperty(propertyName);
    if (trace != null) trace.data(10, menuSequence);
    if (menuSequence == null || menuSequence.length() == 0)
      menuSequence = emptySequence;
    Object[] modelSet = null;
    try {
      modelSet = buildMenuTreeModel(null, null, Arrays.asList(menuSequence.split("[\\|]+")).iterator());
    } catch (Exception e1) {
      try {
        // we have corrupted properties -- reset them to defaults so that user doesn't have to do it manually
        GlobalProperties.resetMyAndGlobalProperties();
        menuSequence = GlobalProperties.getProperty(propertyName);
        if (menuSequence == null || menuSequence.length() == 0)
          menuSequence = emptySequence;
        if (trace != null) trace.data(20, menuSequence);
        modelSet = buildMenuTreeModel(null, null, Arrays.asList(menuSequence.split("[\\|]+")).iterator());
      } catch (Exception e2) {
        // failed again with reset properties - this is probably programming bug
        e2.printStackTrace();
        // last resort is the empty sequence
        modelSet = buildMenuTreeModel(null, null, Arrays.asList(emptySequence.split("[\\|]+")).iterator());
      }
    }

    if (trace != null) trace.exit(MenuTreeModel.class, modelSet);
    return modelSet;
  }

  /**
   * Wipes out entire model.
   */
  public synchronized void clear() {
    if (isInitialized) {
      clear(treeModel, treeModelHM);
      clear(treeModelPopup, treeModelPopupHM);
    }
    if (jMenuBar!= null) {
      MiscGui.removeAllComponentsAndListeners(jMenuBar);
      jMenuBar = null;
    }
  }
  private static void clear(DefaultTreeModel treeModel, Map treeModelHM) {
    if (treeModel != null) {
      DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
      Enumeration e = root.depthFirstEnumeration();
      while (e.hasMoreElements()) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
        MenuActionItem menuActionItem = (MenuActionItem) node.getUserObject();
        menuActionItem.setAction(null);
        menuActionItem.setButtonGroup(null);
        menuActionItem.setGUIButton(null);
        node.setUserObject(null);
        // Additional call to detach children in this depth first sequence
        node.removeAllChildren();
      }
      root = null;
      treeModelHM.clear();
    }
  }


  public synchronized DefaultTreeModel getTreeModel() {
    if (!isInitialized) initialize();
    return treeModel;
  }


  /**
   * Rebuild is currently unused.  Coded for purposes of MenuEditor, but it has been disabled.
   */
  public synchronized void rebuildMenuBar(DefaultTreeModel treeModel) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MenuTreeModel.class, "rebuildMenuBar(DefaultTreeModel treeModel)");
    if (trace != null) trace.args(treeModel);

    if (!isInitialized) initialize();

    this.treeModel = treeModel;
    treeModelHM.clear();
    jMenuBar.removeAll();
    DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
    Enumeration e = root.depthFirstEnumeration();

    ArrayList actionsL = new ArrayList();
    // rebuild the HashMap of nodes and hide the nodes so we can re-ad them...
    while (e.hasMoreElements()) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
      MenuActionItem menuActionItem = (MenuActionItem) node.getUserObject();
      treeModelHM.put(menuActionItem.getActionId(), node);
      menuActionItem.setShowing(false);
      if (menuActionItem.getAction() != null)
        actionsL.add(menuActionItem.getAction());
    }
    if (actionsL.size() > 0) {
      Action[] actions = new Action[actionsL.size()];
      actionsL.toArray(actions);
      addActions(actions);
    }

    if (trace != null) trace.exit(MenuTreeModel.class);
  }


  /**
   * Adds actions to the model, if actions are not registered prior, they
   * will appear in the "Plugins" menu.  GUI automatically reflects the changes.
   */
  public synchronized void addActions(Action[] actionArray) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MenuTreeModel.class, "addActions(Action[] actionArray)");
    if (trace != null) trace.args(actionArray);

    if (actionArray != null && actionArray.length > 0) {
      if (!isInitialized) initialize();

      boolean anyAdded = false;
      // Insert new actions into the tree -- and based on the position, to the menu too.
      for (int i=0; i<actionArray.length; i++) {
        Action action = actionArray[i];
        // see if the action is to be included in the menu
        Boolean include = (Boolean) action.getValue(Actions.IN_MENU);
        if (include == null || include.equals(Boolean.TRUE)) {
          addAction(action, treeModel, treeModelHM, true);
          anyAdded = true;
        }
        // see if the action is to be included in the popups or regulated by IN_POPUP_SHOW_DEACTIVATED variable flag
        include = (Boolean) action.getValue(Actions.IN_POPUP);
        if (include == null || include.equals(Boolean.TRUE) || action.getValue(Actions.IN_POPUP_SHOW_DEACTIVATED) != null) {
          addAction(action, treeModelPopup, treeModelPopupHM, false);
          anyAdded = true;
        }
      }
      if (anyAdded) {
        addSeparatorsToMenu((DefaultMutableTreeNode) treeModel.getRoot());
        jMenuBar.revalidate();
        jMenuBar.repaint();
      }
    }
    if (trace != null) trace.exit(MenuTreeModel.class);
  }
  private void addAction(Action action, DefaultTreeModel treeModel, Map treeModelHM, boolean ensureVisibility) {
    Integer actionId = (Integer) action.getValue(Actions.ACTION_ID);
    // Attempt to find the node starting from the root.
    //DefaultMutableTreeNode node = findNode(actionId, (DefaultMutableTreeNode) modelSet.getRoot());
    DefaultMutableTreeNode node = findNode(actionId, treeModelHM);
    MenuActionItem menuNode = null;
    if (node != null) {
      menuNode = (MenuActionItem) node.getUserObject();
      // fill in the real action
      menuNode.setAction(action);
      // see if parent menu name changed
      String parentName = (String) action.getValue(Actions.PARENT_NAME);
      if (parentName != null) {
        TreeNode parent = node.getParent();
        if (parent instanceof DefaultMutableTreeNode) {
          DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) parent;
          MenuActionItem parentItem = (MenuActionItem) parentNode.getUserObject();
          if (parentItem != null && !parentItem.getName().equals(parentName)) {
            parentItem.setName(parentName);
            Integer parentMnemonic = (Integer) action.getValue(Actions.PARENT_MNEMONIC);
            if (parentMnemonic != null)
              parentItem.setMnemonic(parentMnemonic);
            parentItem.updateGUIButtonName(true);
            //jMenuBar.revalidate();
            //rebuildMenuBar(); // this is too much!
          }
        }
      }
    } else {
      // if no assigned node, throw it into 'Not Assigned' (-100,000)
      //node = findNode(new Integer(-100000), (DefaultMutableTreeNode) modelSet.getRoot());
      node = findNode(new Integer(-100000), treeModelHM);
      if (node != null) {
        menuNode = new MenuActionItem(action);
        DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(menuNode);
        node.add(newNode);
        treeModelHM.put(menuNode.getActionId(), newNode);
        node = newNode;
      } else {
        System.out.println("Adding action but no parent node!  Action is "+action);
      }
    }
    if (ensureVisibility) {
      ensureNodeIsVisible(node);
    }
  }

  /**
   * @return node from the tree carrying the specified actionId
   */
  private static MenuActionItem findMenuActionItem(Integer actionId, Map treeModelHM) {
    MenuActionItem item = null;
    DefaultMutableTreeNode node = (DefaultMutableTreeNode) treeModelHM.get(actionId);
    if (node != null)
      item = (MenuActionItem) node.getUserObject();
    return item;
  }
  private static DefaultMutableTreeNode findNode(Integer actionId, Map treeModelHM) {
    return (DefaultMutableTreeNode) treeModelHM.get(actionId);
  }

//  /**
//   * @return node from the tree carrying the specified actionId
//   */
//  private static DefaultMutableTreeNode findNode(Integer actionId, DefaultMutableTreeNode sourceSubTree) {
//    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MenuTreeModel.class, "findNode(Integer actionId, DefaultMutableTreeNode sourceSubTree)");
//    if (trace != null) trace.args(actionId);
//
//    DefaultMutableTreeNode foundNode = null;
//
//    Enumeration enum = sourceSubTree.breadthFirstEnumeration();
//    while (enum.hasMoreElements()) {
//      DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) enum.nextElement();
//      MenuActionItem menuNode = (MenuActionItem) treeNode.getUserObject();
//      if (actionId.equals(menuNode.getActionId())) {
//        foundNode = treeNode;
//        break;
//      }
//    }
//
//    if (trace != null) trace.exit(MenuTreeModel.class, foundNode);
//    return foundNode;
//  }

  /**
   * Ensures that specified node is showing in the menu (because the action is available)
   * @return true if some node was made visible.
   */
  private boolean ensureNodeIsVisible(DefaultMutableTreeNode node) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MenuTreeModel.class, "ensureNodeIsVisible(DefaultMutableTreeNode node)");
    if (trace != null) trace.args(node);

    // ensure we are not adding a few of the same nodes (if ActionProducerI mistakenly created it)
    if (isNodeVisible(node)) {
      if (trace != null) trace.exit(MenuTreeModel.class, false);
      return false;
    }

    TreeNode[] treeNodes = node.getPath();
    JMenu currentMenu = null;

    // insert the sub-menus in the path to the action (not the action item) if don't already exist...
    for (int i=1; i<treeNodes.length-1; i++) {
      DefaultMutableTreeNode n = (DefaultMutableTreeNode) treeNodes[i];

      MenuActionItem menuNode = (MenuActionItem) n.getUserObject();
      if (trace != null) trace.data(10, "menuNode", menuNode);

      int visibleBefore = countVisibleBeforeChild((DefaultMutableTreeNode) treeNodes[i-1], n);

      if (trace != null) trace.data(20, "currentMenu", currentMenu);
      if (menuNode.isShowing()) {
        if (currentMenu == null)
          currentMenu = jMenuBar.getMenu(visibleBefore);
        else
          currentMenu = (JMenu) currentMenu.getItem(visibleBefore);
      }
      else {
        if (trace != null) trace.data(25, "creating new submenu");
        JMenu newMenu = createSubMenu(menuNode);
        if (currentMenu == null)
          jMenuBar.add(newMenu, visibleBefore);
        else {
          currentMenu.insert(newMenu, visibleBefore);
        }
        currentMenu = newMenu;
      }
      if (trace != null) trace.data(30, "currentMenu", currentMenu);
      menuNode.setShowing(true);
    }

    // insert the action itself
    DefaultMutableTreeNode insertionNode = (DefaultMutableTreeNode) treeNodes[treeNodes.length-1];
    MenuActionItem insertionMenuItem = (MenuActionItem) insertionNode.getUserObject();
    if (trace != null) trace.data(50, insertionMenuItem);

    int visibleBefore = countVisibleBeforeChild((DefaultMutableTreeNode) treeNodes[treeNodes.length-2], insertionNode);
    if (insertionMenuItem.isActionItem()) {
//      // If buttton group exists, look and clear a similar action button from the group.
//      ButtonGroup group = (ButtonGroup) insertionMenuItem.getAction().getValue(Actions.BUTTON_GROUP);
//      if (group != null) {
//        Enumeration buttons = group.getElements();
//        while (buttons.hasMoreElements()) {
//          AbstractButton button = (AbstractButton) buttons.nextElement();
//          String text = button.getText();
//          if (text != null && text.equals(insertionMenuItem.getName())) {
//            group.remove(button);
//            break;
//          }
//        }
//      }
      JMenuItem jMenuItem = null;
      if (!insertionMenuItem.isGUIButtonSet())
        jMenuItem = convertActionToMenuItem(insertionMenuItem);
      else
        jMenuItem = (JMenuItem) insertionMenuItem.getGUIButton();
      currentMenu.insert(jMenuItem, visibleBefore);
    }
    else
      currentMenu.insertSeparator(visibleBefore);

    insertionMenuItem.setShowing(true);

    if (trace != null) trace.exit(MenuTreeModel.class, true);
    return true;
  }

  private static JMenu createSubMenu(MenuActionItem menuNode) {
    JMenu jMenu = new JMyMenu(menuNode.getName());
    if (menuNode.getMnemonic() != null)
      jMenu.setMnemonic(menuNode.getMnemonic().intValue());
    menuNode.setGUIButton(jMenu);
    return jMenu;
  }

  /**
   * @return true if the menu tree user node object specifies that given node isShowing.
   */
  private static boolean isNodeVisible(DefaultMutableTreeNode node) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MenuTreeModel.class, "isNodeVisible(DefaultMutableTreeNode node)");
    if (trace != null) trace.args(node);

    MenuActionItem menuNode = (MenuActionItem) node.getUserObject();

    if (trace != null) trace.exit(MenuTreeModel.class, menuNode.isShowing());
    return menuNode.isShowing();
  }

  /**
   * If childNode is NULL, all visible children are counted.
   * @return number of sibling menus that are visible in the sequence before this child node is encountered in the tree.
   */
  private static int countVisibleBeforeChild(DefaultMutableTreeNode parentNode, DefaultMutableTreeNode childNode) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MenuTreeModel.class, "countVisibleBeforeChild(DefaultMutableTreeNode parentNode, DefaultMutableTreeNode childNode)");
    if (trace != null) trace.args(parentNode);
    if (trace != null) trace.args(childNode);

    int childCount = parentNode.getChildCount();
    int countVisible = 0;
    for (int i=0; i<childCount; i++) {
      DefaultMutableTreeNode n = (DefaultMutableTreeNode) parentNode.getChildAt(i);
      if (n == childNode)
        break;
      MenuActionItem menuNode = (MenuActionItem) n.getUserObject();
      if (menuNode.isShowing())
        countVisible ++;
    }

    if (trace != null) trace.exit(MenuTreeModel.class, countVisible);
    return countVisible;
  }


  private void addSeparatorsToMenu(DefaultMutableTreeNode node) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MenuTreeModel.class, "addSeparatorsToMenu(DefaultMutableTreeNode node)");

    DefaultMutableTreeNode lastHidenSeparatorAfterShownItem = null;

    Enumeration children = node.children();
    int state = 0; // 0 - looking for shown menu node, 1 - looking for hiden seperator, 2 - looking for another shown menu node
    while (children.hasMoreElements()) {
      DefaultMutableTreeNode n = (DefaultMutableTreeNode) children.nextElement();
      MenuActionItem item = (MenuActionItem) n.getUserObject();

      if (state == 0) {
        if (item.isShowing()) {
          if (item.isActionItem()) {
            state = 1;
          }
        }
      } else if (state == 1) {
        if (!item.isShowing()) {
          if (!item.isActionItem()) {
            lastHidenSeparatorAfterShownItem = n;
            state = 2;
          }
        } else if (!item.isActionItem()) {
          state = 0;
        }
      } else if (state == 2) {
        if (item.isShowing()) {
          if (item.isActionItem()) {
            ensureNodeIsVisible(lastHidenSeparatorAfterShownItem);
            state = 1;
          } else {
            state = 0;
          }
        }
      }
      // make this recursive!
      if (item.isShowing() && n.getChildCount() > 1)
        addSeparatorsToMenu(n);
    } // end while

    if (trace != null) trace.exit(MenuTreeModel.class);
  } // end addSeparatorsToMenu();


  /**
   * Removes actions from the model, if some JMenus are left with no menu items,
   * they are removed too.
   */
  public synchronized void removeActions(Action[] actionArray) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MenuTreeModel.class, "removeActions(Actions[] actionArray)");
    if (trace != null) trace.args(actionArray);

    if (actionArray != null && actionArray.length > 0) {
      if (!isInitialized) initialize();

      // Remove existing action from the tree -- and based on their visible position(s), from the menu(s) too.
      for (int i=0; i<actionArray.length; i++) {
        Action action = actionArray[i];
        Integer actionId = (Integer) action.getValue(Actions.ACTION_ID);
        // Attempt to find the node starting from the root.
        //DefaultMutableTreeNode node = findNode(actionId, (DefaultMutableTreeNode) modelSet.getRoot());
        DefaultMutableTreeNode node = findNode(actionId, treeModelHM);
        if (node != null) {
          ensureNodeIsInvisible(node);
        }
      }

      removeExtraSeparatorsFromMenu((DefaultMutableTreeNode) treeModel.getRoot());
      jMenuBar.revalidate();
      jMenuBar.repaint();
    }

    if (trace != null) trace.exit(MenuTreeModel.class);
  }

  /**
   * Hides the node by setting isShowing false and removing it from the jMenuBar.
   * If the parent JMenu or JMenuBar does not contain any more items, it will
   * be removed too.
   * A recursive process that starts at child most node after finding all GUI menus on the way.
   * @return true if a node was made invisible
   */
  private boolean ensureNodeIsInvisible(DefaultMutableTreeNode node) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MenuTreeModel.class, "ensurenodeIsInvisible(DefaultMutableTreeNode node)");
    if (trace != null) trace.args(node);

    // ensure we are not hidding a node that is already not visible.
    if (!isNodeVisible(node)) {
      if (trace != null) trace.exit(MenuTreeModel.class, false);
      return false;
    }

    ensureNodePathIsInvisible(node.getPath(), 0, null);

    if (trace != null) trace.exit(MenuTreeModel.class, true);
    return true;
  }

  /**
   * Makes the last node in the path invisible and propegates to all parents
   * that have no more visible children to hide them too.
   */
  private void ensureNodePathIsInvisible(TreeNode[] nodePath, int index, JComponent parentMenu) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MenuTreeModel.class, "ensureNodePathIsInvisible(TreeNode[] nodePath, int index, JComponent parentMenu)");
    if (trace != null) trace.args(nodePath);
    if (trace != null) trace.args(index);
    if (trace != null) trace.args(parentMenu);

    // child of parentMenu
    JComponent menuComponent = null;
    // n'th child of parentMenu
    int menuComponentIndex = 0;

    // if this is the JMenuBar
    if (index == 0)
      menuComponent = jMenuBar;
    // else if this is an intermediate JMenu or the final JMenuItem
    else {
      menuComponentIndex = countVisibleBeforeChild((DefaultMutableTreeNode) nodePath[index-1], (DefaultMutableTreeNode) nodePath[index]);
      if (parentMenu instanceof JMenuBar)
        menuComponent = ((JMenuBar) parentMenu).getMenu(menuComponentIndex);
      else
        menuComponent = ((JMenu) parentMenu).getItem(menuComponentIndex);
    }

    // go deep into the node path to start processing from the end of the array
    if (index + 1 < nodePath.length)
      ensureNodePathIsInvisible(nodePath, index + 1, menuComponent);

    // Process the last node first .. till the beginning of the node path...
    // if menuComponent is JMenuItem, hide it from parentMenu
    if (index == nodePath.length-1) {
      if (trace != null) trace.data(40, "removing from parent=", parentMenu);
      if (trace != null) trace.data(41, "removing component=", menuComponent);
      // mark data item invisible
      DefaultMutableTreeNode tNode = (DefaultMutableTreeNode) nodePath[index];
      MenuActionItem menuItem = (MenuActionItem) tNode.getUserObject();
      // see if menu item is removable
      Action action = menuItem.getAction();
      Boolean removable = (Boolean) (action != null ? action.getValue(Actions.REMOVABLE_MENU) : null);
      Boolean disabable = (Boolean) (action != null ? action.getValue(Actions.DISABABLE) : null);
      if (action != null && (disabable == null || disabable.equals(Boolean.TRUE)))
        action.setEnabled(false);
      if (menuItem.isMenu() || menuItem.isSeparator() || (removable != null && removable.equals(Boolean.TRUE))) {
        menuItem.setShowing(false);
        menuItem.setGUIButton(null);
        parentMenu.remove(menuComponentIndex);
      }
      // Don't remove the Action item because it messes up the Menu Editor and rebuild action
//      if (disabable == null || disabable.equals(Boolean.TRUE))
//        menuItem.setAction(null);
    }
    // if menuComponent is JMenu and it has no children, hide it from parentMenu
    else if (index > 0) {
      int componentCount = 0;
      if (menuComponent instanceof JMenu)
        componentCount = ((JMenu) menuComponent).getItemCount();
      else if (menuComponent instanceof JMenuBar)
        componentCount = ((JMenuBar) menuComponent).getMenuCount();
      if (componentCount == 0) {
        // remove gui component from parent menu
        if (trace != null) trace.data(50, "removing from parent=", parentMenu);
        if (trace != null) trace.data(51, "removing component=", menuComponent);
        // mark data item invisible
        DefaultMutableTreeNode tNode = (DefaultMutableTreeNode) nodePath[index];
        MenuActionItem menuItem = (MenuActionItem) tNode.getUserObject();
        // see if menu item is removable
        Action action = menuItem.getAction();
        Boolean removable = (Boolean) (action != null ? action.getValue(Actions.REMOVABLE_MENU) : null);
        Boolean disabable = (Boolean) (action != null ? action.getValue(Actions.DISABABLE) : null);
        if (action != null && (disabable == null || disabable.equals(Boolean.TRUE)))
          action.setEnabled(false);
        if (menuItem.isMenu() || menuItem.isSeparator() || (removable != null && removable.equals(Boolean.TRUE))) {
          menuItem.setShowing(false);
          menuItem.setGUIButton(null);
          parentMenu.remove(menuComponent);
        }
        // Don't remove the Action item because it messes up the Menu Editor and rebuild action
//        if (disabable == null || disabable.equals(Boolean.TRUE))
//          menuItem.setAction(null);
      }
    }
    // if menuComponent is JMenuBar, just leave it

    if (trace != null) trace.exit(MenuTreeModel.class);
  }

  /**
   * Removes extra separators (if any) from the menu, called after menu item removal.
   */
  private void removeExtraSeparatorsFromMenu(DefaultMutableTreeNode node) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MenuTreeModel.class, "removeExtraSeparatorsFromMenu(DefaultMutableTreeNode node)");
    if (trace != null) trace.args(node);

    ArrayList visibleNodesL = new ArrayList();
    int childCount = node.getChildCount();
    for (int j=0; j<childCount; j++) {
      DefaultMutableTreeNode n = (DefaultMutableTreeNode) node.getChildAt(j);
      MenuActionItem menuNode = (MenuActionItem) n.getUserObject();
      if (menuNode.isShowing()) {
        visibleNodesL.add(n);
      }
    }

    // recursive bottom-up, because removing the deapest child can cause removing of parent path
    for (int k=0; k<visibleNodesL.size(); k++) {
      DefaultMutableTreeNode n = (DefaultMutableTreeNode) visibleNodesL.get(k);
      // make this recursive
      if (n.getChildCount() > 1) {
        removeExtraSeparatorsFromMenu(n);
      }
    }

    // Reconstruct visibleNodesL because some objects might have become invisible in the above recursive call.
    visibleNodesL = new ArrayList();
    childCount = node.getChildCount();
    for (int x=0; x<childCount; x++) {
      DefaultMutableTreeNode n = (DefaultMutableTreeNode) node.getChildAt(x);
      MenuActionItem menuNode = (MenuActionItem) n.getUserObject();
      if (menuNode.isShowing()) {
        visibleNodesL.add(n);
      }
    }

    int i = 0;
    while (i<visibleNodesL.size()) {
      if (i >= 0 && i < visibleNodesL.size()) {
        DefaultMutableTreeNode n = (DefaultMutableTreeNode) visibleNodesL.get(i);
        MenuActionItem menuNode = (MenuActionItem) n.getUserObject();
        if (menuNode.isSeparator()) {
          if ((i == 0) || // leading seperator
              (i == visibleNodesL.size()-1) || // trailing seperator
              (i < visibleNodesL.size()-1 && ((MenuActionItem) ((DefaultMutableTreeNode) visibleNodesL.get(i+1)).getUserObject()).isSeparator())) // if next is also a seperator (two seperators in a row)
          {
            ensureNodeIsInvisible(n);
            visibleNodesL.remove(i);
            continue;
          }
        }
      }
      i++;
    }

    if (trace != null) trace.exit(MenuTreeModel.class);
  }

  /**
   * @return the presenter of this model.
   */
  public synchronized JMenuBar getMenuBar() {
    return jMenuBar;
  }

  /***************************************************************************/
  /*            P o p u p    M e n u    F u n c t i o n s    (begin)         */
  /***************************************************************************/

  /**
   * Generates a popup menu with a structure reflecting the menu system
   * with all GUI representations of actions cloned and ButtonGroups made for
   * the radio buttons.
   * All parent most actions are reduced to first JMenuItem representation so that
   * popup does not contain inner JMenus without any items at the root.
   * Items from different JMenus are separated by JSeparator.
   */
  public synchronized JPopupMenu generatePopup(Action[] popupActions) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MenuTreeModel.class, "generatePopup(Action[] popupActions)");
    if (trace != null) trace.args(popupActions);

    JPopupMenu jPopup = null;
    if (popupActions != null && popupActions.length > 0) {
      if (!isInitialized) initialize();

      // Create an identical tree model only with the specified leafs.
      Object[] _treeModelPopupCopy = copyTreeModel(treeModelPopup, popupActions, true);
      DefaultTreeModel treeModelPopupCopy = (DefaultTreeModel) _treeModelPopupCopy[0];
      HashMap treeModelPopupCopyHM = (HashMap) _treeModelPopupCopy[1];

      // Cut down the branch levels that don't have any concrete Actions, just bunch of JMenu(s)
      slashAwayMenuLevels(treeModelPopupCopy);

      // Replace ButtonGroups with new groups keeping the groupped actions together.
      replaceButtonGroups(treeModelPopupCopy);

      // create a gui reflection of the popup tree model
      jPopup = createPopupMenuFromModel(treeModelPopupCopy);

      // dissasemble the tree copy and hashmap
      DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModelPopupCopy.getRoot();
      Enumeration enm = root.depthFirstEnumeration();
      while (enm.hasMoreElements()) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) enm.nextElement();
        node.setUserObject(null);
        DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
        if (parent != null)
          parent.remove(node);
      }
      treeModelPopupCopyHM.clear();
    }

    if (trace != null) trace.exit(MenuTreeModel.class, jPopup);
    return jPopup;
  }

  /**
   * Create a new tree structure that resembles the specified tree model
   * containing only the specified filtered nodes and their parent paths
   * and assign the same user objects as in the original tree.
   * @return newly created mirror of the specified tree.
   */
  private static Object[] copyTreeModel(DefaultTreeModel treeModel, Action[] keepActions, boolean forPopup) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MenuTreeModel.class, "copyTreeModel(DefaultTreeModel treeModel, Action[] keepActions, boolean forPopup)");
    if (trace != null) trace.args(treeModel);
    if (trace != null) trace.args(keepActions);
    if (trace != null) trace.args(forPopup);

    DefaultMutableTreeNode treeRoot = (DefaultMutableTreeNode) treeModel.getRoot();
    DefaultMutableTreeNode copiedRoot = copyNode(treeRoot);
    List keepActionList = Arrays.asList(keepActions);
    HashMap copiedTreeModelHM = new HashMap();
    copyTreeNodes(copiedRoot, copiedTreeModelHM, treeRoot, keepActionList, forPopup);
    DefaultTreeModel copiedTreeModel = new DefaultTreeModel(copiedRoot);

    Object[] rc = new Object[] { copiedTreeModel, copiedTreeModelHM };
    if (trace != null) trace.exit(MenuTreeModel.class, rc);
    return rc;
  }
  private static void copyTreeNodes(DefaultMutableTreeNode newRoot, Map newTreeModelHM, DefaultMutableTreeNode treeNode, List keepActionList, boolean forPopup) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MenuTreeModel.class, "copyTreeNodes(DefaultMutableTreeNode newRoot, HashMap newTreeModelHM, DefaultMutableTreeNode treeNode, List keepActionList, boolean forPopup)");
    if (trace != null) trace.args(newRoot);
    if (trace != null) trace.args(newTreeModelHM);
    if (trace != null) trace.args(treeNode);
    if (trace != null) trace.args(keepActionList);
    if (trace != null) trace.args(forPopup);

    boolean separatorOk = false;
    boolean separatorPending = false;
    DefaultMutableTreeNode separatorNode = null;

    if (trace != null) trace.data(10, "treeNode.getChildCount()", treeNode.getChildCount());

    Enumeration enm = treeNode.children();
    while (enm.hasMoreElements()) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) enm.nextElement();
      MenuActionItem menuActionItem = (MenuActionItem) node.getUserObject();
      Action action = menuActionItem.getAction();
      if (menuActionItem.isSeparator()) {
        if (separatorOk) {
          separatorPending = true;
          separatorNode = node;
        }
      } else if (action != null && keepActionList.contains(action)) {
        boolean suppressAction = false;
        if (forPopup) {
          Boolean includeInPopup = (Boolean) action.getValue(Actions.IN_POPUP);
          Boolean includeDeactivated = (Boolean) action.getValue(Actions.IN_POPUP_SHOW_DEACTIVATED);
          if (includeInPopup != null && includeInPopup.equals(Boolean.FALSE)) {
            suppressAction = true;
          } else if (includeDeactivated == null || includeDeactivated.equals(Boolean.TRUE) || action.isEnabled()) {
            suppressAction = false;
          } else {
            suppressAction = true;
          }
        }
        if (!suppressAction) {
          if (separatorPending) {
            TreeNode[] nodePath = separatorNode.getPath();
            addPathToRoot(nodePath, newRoot, newTreeModelHM);
            separatorPending = false;
            separatorNode = null;
          }
          TreeNode[] nodePath = node.getPath();
          addPathToRoot(nodePath, newRoot, newTreeModelHM);
          separatorOk = true;
        }
      }

      // go next level down to copy next layer
      if (node.getChildCount() > 0) {
        copyTreeNodes(newRoot, newTreeModelHM, node, keepActionList, forPopup);
      }
    } // end while

    if (trace != null) trace.exit(MenuTreeModel.class);
  } // end copyTreeNodes
  private static DefaultMutableTreeNode copyNode(DefaultMutableTreeNode node) {
    MenuActionItem item = (MenuActionItem) node.getUserObject();
    if (item != null) {
      item = (MenuActionItem) item.clone();
    }
    return new DefaultMutableTreeNode(item);
  }
  private static void addPathToRoot(TreeNode[] nodePath, DefaultMutableTreeNode rootNode, Map treeModelHM) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MenuTreeModel.class, "addPathToRoot(TreeNode[] nodePath, DefaultMutableTreeNode rootNode, Map treeModelHM)");
    if (trace != null) trace.args(nodePath);
    if (trace != null) trace.args(rootNode);
    if (trace != null) trace.args(treeModelHM);

    DefaultMutableTreeNode currentNode = rootNode;
    for (int i=0; i<nodePath.length; i++) {

      if (currentNode == null && i==0)
        currentNode = rootNode;
      else {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) nodePath[i];
        MenuActionItem menuActionItem = (MenuActionItem) node.getUserObject();
        Integer actionId = menuActionItem.getActionId();
        DefaultMutableTreeNode n = findNode(actionId, treeModelHM);
        if (n != null && !menuActionItem.isSeparator())
          currentNode = n;
        else {
          DefaultMutableTreeNode newNode = copyNode(node);
          currentNode.add(newNode);
          treeModelHM.put(actionId, newNode);
          currentNode = newNode;
        }
      }
    } // end for

    if (trace != null) trace.exit(MenuTreeModel.class);
  }
  /**
   * Compresses the tree to eliminate empty leading menu branches.  If parent menu level
   * contains only JMenus then their items are put one level higher and separeted by JSeparator.
   */
  private static void slashAwayMenuLevels(DefaultTreeModel treeModel) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MenuTreeModel.class, "slashAwayMenuLevels(DefaultTreeModel treeModel)");

    DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
    Enumeration allNodes = root.breadthFirstEnumeration();

    while (allNodes.hasMoreElements()) {

      DefaultMutableTreeNode mergedParentCandidate = (DefaultMutableTreeNode) allNodes.nextElement();
      int firstLevelChildCount = mergedParentCandidate.getChildCount();
      if (firstLevelChildCount > 0) {

        if (areAllChildrenJMenuOrJSeparator(mergedParentCandidate)) {
          // if only 1 child, upgrade his user object 1 up
          if (firstLevelChildCount == 1) {
            DefaultMutableTreeNode firstLevelChild = (DefaultMutableTreeNode) mergedParentCandidate.getChildAt(0);
            mergedParentCandidate.setUserObject(firstLevelChild.getUserObject());
          }

          // For every child of mergedParentCandidate take its children and store them in a list.
          // Separate items when jumping accross to another branch.
          Enumeration firstLevelChildren = mergedParentCandidate.children();
          ArrayList mergedChildren = new ArrayList();
          while (firstLevelChildren.hasMoreElements()) {
            DefaultMutableTreeNode firstLevelChild = (DefaultMutableTreeNode) firstLevelChildren.nextElement();

            boolean added = false;
            boolean separatorPending = false;
            boolean separatorOk = false;
            DefaultMutableTreeNode separatorNode = null;

            Enumeration secondLevelChildren = firstLevelChild.children();
            while (secondLevelChildren.hasMoreElements()) {
              DefaultMutableTreeNode secondLevelChildNode = (DefaultMutableTreeNode) secondLevelChildren.nextElement();
              MenuActionItem menuItem = (MenuActionItem) secondLevelChildNode.getUserObject();
              if (menuItem.isSeparator()) {
                if (separatorOk) {
                  separatorPending = true;
                  separatorNode = secondLevelChildNode;
                }
              } else {
                if (separatorPending) {
                  separatorPending = false;
                  mergedChildren.add(separatorNode);
                  separatorNode = null;
                }
                mergedChildren.add(secondLevelChildNode);
                added = true;
                separatorOk = true;
              }
            }
            firstLevelChild.removeAllChildren();

            // when done with a branch, insert separator
            if (added) {
              mergedChildren.add(new DefaultMutableTreeNode(new MenuActionItem(MenuActionItem.STR_SEPARATOR, 0, -1, -1, -1)));
            }
          }
          mergedParentCandidate.removeAllChildren();

          // insert the children from the list
          for (int i=0; i<mergedChildren.size(); i++) {
            mergedParentCandidate.add( (DefaultMutableTreeNode) mergedChildren.get(i) );
          }

          // Must restart from the beginning on the modified tree because
          // some enumerations are invalidated after structural changes.
          slashAwayMenuLevels(treeModel);
          break;
        }
      }
    } // end while
    if (trace != null) trace.exit(MenuTreeModel.class);
  } // end slashAwayMenuBranches()
  private static boolean areAllChildrenJMenuOrJSeparator(DefaultMutableTreeNode parent) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MenuTreeModel.class, "areAllChildrenJMenuOrJSeparator(DefaultMutableTreeNode parent)");
    // determine if all children are JMenu(s)
    boolean allJMenusOrJSeparators = true;
    int childCount = parent.getChildCount();
    if (childCount == 0)
      allJMenusOrJSeparators = false;
    else {
      for (int i=0; i<childCount; i++) {
        DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) parent.getChildAt(i);
        MenuActionItem menuActionItem = (MenuActionItem) childNode.getUserObject();
        if ( !menuActionItem.isMenu() && !menuActionItem.isSeparator() ) {
          allJMenusOrJSeparators = false;
          break;
        }
      }
    }

    if (trace != null) trace.exit(MenuTreeModel.class, allJMenusOrJSeparators);
    return allJMenusOrJSeparators;
  }
  private static void replaceButtonGroups(DefaultTreeModel treeModel) {
    // Go through the tree and store all unique groups in a hashmap
    // where a key is the old group and value is the new replacement group.
    HashMap groupsHM = null;
    Enumeration allNodes = ((DefaultMutableTreeNode) treeModel.getRoot()).depthFirstEnumeration();
    while (allNodes.hasMoreElements()) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) allNodes.nextElement();
      MenuActionItem menuItem = (MenuActionItem) node.getUserObject();
      Action action = menuItem.getAction();
      if (action != null) {
        ButtonGroup buttonGroup = (ButtonGroup) action.getValue(Actions.BUTTON_GROUP);
        if (buttonGroup != null) {
          // laizly create lookup
          if (groupsHM == null) groupsHM = new HashMap();

          ButtonGroup newButtonGroup = (ButtonGroup) groupsHM.get(buttonGroup);

          // if the first item with that group, create a replacement group
          if (newButtonGroup == null) {
            newButtonGroup = new ButtonGroup();
            groupsHM.put(buttonGroup, newButtonGroup);
          }

          // replace the old group with a new one
          action.putValue(Actions.BUTTON_GROUP, newButtonGroup);
        }
      }
    }
    // help garbage collector
    if (groupsHM != null)
      groupsHM.clear();
  }
  private static JPopupMenu createPopupMenuFromModel(DefaultTreeModel copiedTreeModel) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MenuTreeModel.class, "createPopupMenuFromModel(DefaultTreeModel copiedTreeModel)");
    if (trace != null) trace.args(copiedTreeModel);
    JPopupMenu jPopup = (JPopupMenu) createMenuFromModel((DefaultMutableTreeNode) copiedTreeModel.getRoot(), true);
    if (trace != null) trace.exit(MenuTreeModel.class, jPopup);
    return jPopup;
  }
  private static JComponent createMenuFromModel(DefaultMutableTreeNode node, boolean createPopup) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MenuTreeModel.class, "createMenuFromModel(DefaultMutableTreeNode node, boolean createPopup)");
    if (trace != null) trace.args(node);
    if (trace != null) trace.args(createPopup);

    JComponent jComponent = null;
    if (createPopup) {
      jComponent = new JMyPopupMenu();
    } else {
      jComponent = new JMyMenu(((MenuActionItem) node.getUserObject()).getName());
    }

    boolean separatorPending = false;
    boolean separatorOk = false;

    Enumeration firstChildren = node.children();
    while (firstChildren.hasMoreElements()) {
      DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) firstChildren.nextElement();
      MenuActionItem menuItem = (MenuActionItem) childNode.getUserObject();
      if (menuItem.isSeparator()) {
        if (separatorOk) {
          separatorPending = true;
        }
      } else {
        // only allow separators if we already have a regular item
        separatorOk = true;
        // skip the last and repetitive separators -- add only if there is more items
        if (separatorPending) {
          separatorPending = false;
          if (createPopup)
            ((JPopupMenu) jComponent).addSeparator();
          else
            ((JMenu) jComponent).addSeparator();
        }

        JMenuItem jMenuItem = null;
        if (menuItem.isMenu()) {
          jMenuItem = (JMenu) createMenuFromModel(childNode, false);
        } else {
          jMenuItem = convertActionToMenuItem(menuItem);
        }

        if (createPopup)
          ((JPopupMenu) jComponent).add(jMenuItem);
        else
          ((JMenu) jComponent).add(jMenuItem);
      }
    }

    if (trace != null) trace.exit(MenuTreeModel.class, jComponent);
    return jComponent;
  }
  /***************************************************************************/
  /*            P o p u p    M e n u    F u n c t i o n s    (end)           */
  /***************************************************************************/



  /**
   * Converts an Action to a JMenuItem if possible.  Only JActionCheckBoxMenuItem,
   * JActionRadioButtonMenuItem, and JMenuItem are converted here.
   * return NULL if object could not be created, an object otherwise
   */
  private static JMenuItem convertActionToMenuItem(MenuActionItem menuActionItem) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(MenuTreeModel.class, "convertActionToMenuItem(MenuActionItem menuActionItem)");
    if (trace != null) trace.args(menuActionItem);

    JMenuItem menuItem = null;

    Action action = menuActionItem.getAction();
    Boolean state = (Boolean) action.getValue(Actions.STATE_CHECK);
    if (state != null) {
      menuItem = new JActionCheckBoxMenuItem(action);
    } else {
      Boolean selected = (Boolean) action.getValue(Actions.SELECTED_RADIO);
      if (selected != null) {
        menuItem = new JActionRadioButtonMenuItem(action);
      } else {
        menuItem = new JActionMenuItem(action);
      }
    }
    menuActionItem.setGUIButton(menuItem);

    if (trace != null) trace.exit(MenuTreeModel.class, menuItem);
    return menuItem;
  }


  /***************************************/
  /* Private helpers for externalization */
  /***************************************/

  /**
   * Creates a tree node structure from a string source.
   * @return newly created tree model.
   */
  private static Object[] buildMenuTreeModel(DefaultMutableTreeNode parentNode, Map parentHM, Iterator strs) {
    DefaultTreeModel newTreeModel = null;
    Map newTreeModelHT = null;
    DefaultMutableTreeNode prevNode = null;

    if (parentHM != null)
      newTreeModelHT = parentHM;
    else
      newTreeModelHT = new HashMap();

    while (strs.hasNext()) {
      String nodeName = (String) strs.next();
      if (nodeName.equals("(")) {
        if (prevNode != null)
          buildMenuTreeModel(prevNode, newTreeModelHT, strs);
      } else if (nodeName.equals(")")) {
        break;
      } else {
        int actionId = Integer.parseInt((String) strs.next());
        int mnemonic = 0;

        // mnemonic is not defined for Seperator(s)
        if (actionId != 0)
          mnemonic = Integer.parseInt((String) strs.next());

        int keyCode = 0;
        int mask = 0;

        // accelerator is not defined for JMenu(s) or Seperator(s) -- only defined for JMenuItem(s)
        if (actionId > 0) {
          keyCode = Integer.parseInt((String) strs.next());
          mask = Integer.parseInt((String) strs.next());
        }

        Object userObject = new MenuActionItem(nodeName, actionId, mnemonic, keyCode, mask);
        prevNode = new DefaultMutableTreeNode(userObject);
        if (parentNode == null)
          newTreeModel = new DefaultTreeModel(prevNode);
        else {
          parentNode.add(prevNode);
          newTreeModelHT.put(new Integer(actionId), prevNode);
        }
      }
    }
    return newTreeModel != null ? new Object[] { newTreeModel, newTreeModelHT } : null;
  }

  /**
   * Writes the current tree node structure to the StringBuffer.
   */
  private static void dissasambleMenuTreeModel(DefaultMutableTreeNode node, StringBuffer sb) {
    // print out this node
    MenuActionItem menuNode = (MenuActionItem) node.getUserObject();
    boolean isJMenu = menuNode.getActionId().intValue() < 0 ? true : false;
    sb.append(menuNode.getName().replace('|', '_'));
    sb.append("|");

    int actionId = menuNode.getActionId().intValue();
    sb.append(actionId);
    sb.append("|");

    // mnemonic is not defined for Seperator(s)
    if (actionId != 0) {
      if (menuNode.getMnemonic() != null)
        sb.append(menuNode.getMnemonic());
      else
        sb.append("-1");
      sb.append("|");
    }

    // accelerator is not defined for JMenu(s) or Seperator(s) -- only defined for JMenuItem(s)
    if (actionId > 0) {
      KeyStroke keyStroke = menuNode.getKeyStroke();
      if (keyStroke != null) {
        sb.append(keyStroke.getKeyCode());
        sb.append("|");
        sb.append(keyStroke.getModifiers());
        sb.append("|");
      }
      else
        sb.append("-1|-1|");
    }

    // print out its children
    if (node.getChildCount() > 0) {
      // signal that its the child now...
      sb.append("(|");
      Enumeration children = node.children();
      while (children.hasMoreElements()) {
        DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) children.nextElement();
        dissasambleMenuTreeModel(childNode, sb);
      }
      sb.append(")|");
    }
  }


  public synchronized void putMenuProperties() {
    if (isInitialized) {
      StringBuffer sb = new StringBuffer();
      dissasambleMenuTreeModel((DefaultMutableTreeNode) treeModel.getRoot(), sb);
      GlobalProperties.setProperty("MenuTreeModel."+menuPropertyName, sb.toString());
    }
  }

  public synchronized void printMenus() {
    if (isInitialized) {
      StringBuffer strBuf = new StringBuffer();
      dissasambleMenuTreeModel((DefaultMutableTreeNode) treeModel.getRoot(), strBuf);
      System.out.println(strBuf.toString());
    }
  }

}