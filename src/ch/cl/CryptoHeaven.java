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

package ch.cl;

import com.CH_cl.service.ops.AutoUpdaterArgs;
import com.CH_cl.util.GlobalSubProperties;

import com.CH_co.cryptx.*;
import com.CH_co.service.records.*;
import com.CH_co.util.*;

import com.CH_gui.util.*;

import java.awt.*;
import javax.swing.*;

/** 
 * <b>Copyright</b> &copy; 2001-2011
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>
 *
 *
 * @author  Marcin Kurzawa
 * @version
 */
public class CryptoHeaven extends Object {

  private static class SingletonHolder_properties {
    private static final String[][] DEFAULT_PROPERTIES = new String[][] {
      //{"key",                       "value"},

      {"MenuTreeModel."+com.CH_gui.frame.AddressFrame.visualsClassKeyName+"_VS_Ver6", "Program Menus|-1|0|(|File|-10|70|(|Save|750|83|83|130|Save as Draft|764|-1|-1|-1|Separator|0|Select Recipients|751|82|82|130|Select Attachments|752|76|76|130|Voice Recording Panel|768|-1|-1|-1|Priority|-21|80|(|FYI|756|-1|49|130|Normal|757|-1|50|130|High|758|-1|51|130|)|Show Advanced|761|-1|-1|-1|Separator|0|Close|800|67|67|650|)|Edit|-23|69|(|Undo|759|85|90|130|Redo|760|82|89|130|Separator|0|Cut|753|88|88|130|Copy|754|67|67|130|Paste|755|86|86|130|)|Tools|-70|84|(|Spelling ...|765|-1|118|130|Edit user dictionary ...|766|-1|-1|-1|Spelling preferences ...|767|-1|-1|-1|Separator|0|Customize Toolbar ...|101|-1|-1|-1|Customize Menus ...|102|-1|-1|-1|)|Plugins|-100000|80|)|"},
      {"MenuTreeModel."+com.CH_gui.frame.AddressTableFrame.visualsClassKeyName+"_VS_Ver6", "Program Menus|-1|0|(|File|-10|70|(|Open|716|79|79|130|Share Address Book ...|312|-1|-1|-1|Download ...|400|68|68|650|Forward Address(es) ...|705|70|70|130|Separator|0|Print ...|723|-1|-1|-1|Separator|0|Close|800|67|67|650|)|Address|-20|65|(|New Address to Folder|700|77|77|650|New Address from Draft ...|718|-1|-1|-1|Separator|0|Compose to Address(es) ...|707|82|82|130|Reply to All ...|722|-1|82|195|Edit Address|715|80|80|130|Save Attachment(s) ...|706|84|84|130|Invite by Email|714|-1|-1|-1|Add Sender to Address Book|721|-1|-1|-1|Separator|0|Trace Address Access|713|-1|-1|-1|Address Properties|704|-1|-1|-1|)|Edit|-40|69|(|Add Star|730|-1|-1|-1|Remove Star|731|-1|-1|-1|Mark as Read|709|82|82|650|Mark as Unread|710|85|85|650|Mark All Read|711|65|65|650|Separator|0|Copy to Folder ...|701|-1|-1|-1|Move to Folder ...|702|-1|-1|-1|Delete ...|703|127|127|0|Revoke Address(es) ...|724|-1|-1|-1|)|View|-50|86|(|Clone Address View|712|-1|-1|-1|Separator|0|Search|727|-1|-1|-1|Split Top-Bottom|726|-1|-1|-1|Table Columns ...|1202|-1|-1|-1|Sort Table By|-51|83|(|Priority|1203|-1|-1|-1|Attachment|1204|-1|-1|-1|Flag|1205|-1|-1|-1|From|1206|-1|-1|-1|To|1207|-1|-1|-1|Name|1208|-1|-1|-1|Sent|1209|-1|-1|-1|Link ID|1210|-1|-1|-1|Data ID|1211|-1|-1|-1|Fetch count|1212|-1|-1|-1|Created|1213|-1|-1|-1|Updated|1214|-1|-1|-1|Delivered|1215|-1|-1|-1|Size on Disk|1216|-1|-1|-1|In reply to|1217|-1|-1|-1|Secure Lock|1218|-1|-1|-1|Email Address|1219|-1|-1|-1|Business Phone|1220|-1|-1|-1|Home Phone|1221|-1|-1|-1|Expiration|1222|-1|-1|-1|Password|1223|-1|-1|-1|Separator|0|Ascending|1200|-1|-1|-1|Descending|1201|-1|-1|-1|Separator|0|Group Mail by Conversation|717|-1|-1|-1|)|Separator|0|Refresh Addresses|708|-1|-1|-1|)|Tools|-70|84|(|Customize Toolbar ...|101|-1|-1|-1|Customize Menus ...|102|-1|-1|-1|)|Plugins|-100000|-1|)|"},
      {"MenuTreeModel."+com.CH_gui.frame.ChatTableFrame.visualsClassKeyName+"_VS_Ver6", "Program Menus|-1|0|(|File|-10|70|(|Send|750|83|83|130|Save as Draft|764|-1|-1|-1|Select Attachments|752|76|76|130|Priority|-21|80|(|FYI|756|-1|49|130|Normal|757|-1|50|130|High|758|-1|51|130|)|Separator|0|Open|716|79|79|130|Invite to the Conversation ...|312|-1|-1|-1|Download ...|400|68|68|650|Forward Message(s) ...|705|70|70|130|Separator|0|Print ...|723|-1|-1|-1|Separator|0|Close|800|67|67|650|)|Message|-20|77|(|New Message to Folder|700|77|77|650|New Message from Draft|718|-1|-1|-1|Separator|0|Reply to Sender ...|707|82|82|130|Reply to All ...|722|-1|82|195|Post Reply to Folder ...|715|80|80|130|Save Attachment(s) ...|706|84|84|130|Invite Sender|714|-1|-1|-1|Add Sender to Address Book|721|-1|-1|-1|Separator|0|Trace Message Access|713|-1|-1|-1|Message Properties|704|-1|-1|-1|)|Edit|-40|69|(|Undo|759|85|90|130|Redo|760|82|89|130|Separator|0|Cut|753|88|88|130|Copy|754|67|67|130|Paste|755|86|86|130|Separator|0|Add Star|730|-1|-1|-1|Remove Star|731|-1|-1|-1|Mark as Read|709|82|82|650|Mark as Unread|710|85|85|650|Mark All Read|711|65|65|650|Separator|0|Copy to Folder ...|701|-1|-1|-1|Move to Folder ...|702|-1|-1|-1|Delete ...|703|127|127|0|Revoke Message(s) ...|724|-1|-1|-1|)|View|-50|86|(|Clone Message View|712|-1|-1|-1|Separator|0|Search|727|-1|-1|-1|Table Columns ...|1202|-1|-1|-1|Sort Table By|-51|83|(|Priority|1203|-1|-1|-1|Attachment|1204|-1|-1|-1|Flag|1205|-1|-1|-1|From|1206|-1|-1|-1|To|1207|-1|-1|-1|Message|1208|-1|-1|-1|Sent|1209|-1|-1|-1|Link ID|1210|-1|-1|-1|Data ID|1211|-1|-1|-1|Fetch count|1212|-1|-1|-1|Created|1213|-1|-1|-1|Updated|1214|-1|-1|-1|Delivered|1215|-1|-1|-1|Size on Disk|1216|-1|-1|-1|In reply to|1217|-1|-1|-1|Secure Lock|1218|-1|-1|-1|Expiration|1222|-1|-1|-1|Password|1223|-1|-1|-1|Separator|0|Ascending|1200|-1|-1|-1|Descending|1201|-1|-1|-1|Separator|0|Group Chat Messages by Conversation|717|-1|-1|-1|)|Separator|0|Refresh Messages|708|-1|-1|-1|)|Tools|-70|84|(|Spelling ...|765|-1|118|130|Edit user dictionary ...|766|-1|-1|-1|Spelling preferences ...|767|-1|-1|-1|Separator|0|Customize Toolbar ...|101|-1|-1|-1|Customize Menus ...|102|-1|-1|-1|)|Plugins|-100000|-1|)|"},
      {"MenuTreeModel."+com.CH_gui.frame.ContactTableFrame.visualsClassKeyName+"_VS_Ver6", "Program Menus|-1|0|(|File|-10|70|(|New Message|503|78|77|130|Chat|508|72|72|130|Create Shared Space ...|510|-1|-1|-1|Separator|0|Find Friends and Associates ...|500|-1|-1|-1|Add to Address Book|511|-1|-1|-1|Create Group|512|-1|-1|-1|Separator|0|Close|800|67|67|650|)|Contacts|-30|78|(|Invite Friends and Associates ...|509|-1|-1|-1|Accept / Decline Contact(s) ...|501|-1|-1|-1|Delete Contact ...|502|-1|-1|-1|Separator|0|Show Other's Contacts|506|-1|-1|-1|Contact Properties|504|-1|-1|-1|)|View|-50|86|(|Clone Contact List View|507|-1|-1|-1|Separator|0|Contact Columns ...|524|-1|-1|-1|Sort Contacts By|-52|83|(|Direction|515|-1|-1|-1|Name|516|-1|-1|-1|Status|517|-1|-1|-1|Contact ID|518|-1|-1|-1|User ID|519|-1|-1|-1|Encryption|520|-1|-1|-1|Created|521|-1|-1|-1|Updated|522|-1|-1|-1|Permissions|523|-1|-1|-1|Separator|0|Ascending|513|-1|-1|-1|Descending|514|-1|-1|-1|)|Separator|0|Refresh Contacts|505|-1|-1|-1|)|Tools|-70|84|(|Customize Toolbar ...|101|-1|-1|-1|Customize Menus ...|102|-1|-1|-1|)|Plugins|-100000|-1|)|"},
      {"MenuTreeModel."+com.CH_gui.frame.FileTableFrame.visualsClassKeyName+"_VS_Ver6", "Program Menus|-1|0|(|File|-10|70|(|Open|716|79|79|130|Share Folder ...|312|-1|-1|-1|Download ...|400|68|68|650|Forward File ...|705|70|70|130|Separator|0|Trace File Access|713|-1|-1|-1|File Properties|404|-1|-1|-1|Separator|0|Close|800|67|67|650|)|Edit|-40|69|(|Add Star|730|-1|-1|-1|Remove Star|731|-1|-1|-1|Mark as Seen|709|82|82|650|Mark as Unseen|710|85|85|650|Mark All Seen|711|65|65|650|Separator|0|Copy to Folder ...|701|-1|-1|-1|Move to Folder ...|702|-1|-1|-1|Delete ...|703|127|127|0|)|View|-50|86|(|Clone File View|410|-1|-1|-1|Separator|0|Search|727|-1|-1|-1|Table Columns ...|1202|-1|-1|-1|Sort Table By|-51|83|(|Flag|1203|-1|-1|-1|File Name|1204|-1|-1|-1|Type|1205|-1|-1|-1|Size|1206|-1|-1|-1|Created|1207|-1|-1|-1|Updated|1208|-1|-1|-1|Link ID|1209|-1|-1|-1|Data ID|1210|-1|-1|-1|Separator|0|Ascending|1200|-1|-1|-1|Descending|1201|-1|-1|-1|)|Separator|0|Refresh Files|708|-1|-1|-1|)|Tools|-70|84|(|Customize Toolbar ...|101|-1|-1|-1|Customize Menus ...|102|-1|-1|-1|)|Plugins|-100000|-1|)|"},
      {"MenuTreeModel."+com.CH_gui.frame.FindUserFrame.visualsClassKeyName+"_VS_Ver6", "Program Menus|-1|0|(|File|-10|70|(|Add to Contact List ...|600|-1|-1|-1|Send Message ...|601|77|77|130|Invite Friends and Associates ...|1000|69|69|130|Separator|0|Close|800|67|67|650|)|View|-50|86|(|Table Columns ...|1202|-1|-1|-1|Sort Table By|-51|83|(|Username|1203|-1|-1|-1|User ID|1204|-1|-1|-1|Messaging|1205|-1|-1|-1|Email Address|1206|-1|-1|-1|Separator|0|Ascending|1200|-1|-1|-1|Descending|1201|-1|-1|-1|)|)|Tools|-70|84|(|Customize Toolbar ...|101|-1|-1|-1|Customize Menus ...|102|-1|-1|-1|)|Plugins|-100000|-1|)|"},
      {"MenuTreeModel."+com.CH_gui.frame.FolderTreeFrame.visualsClassKeyName+"_VS_Ver6", "Program Menus|-1|0|(|File|-10|70|(|New Folder ...|300|78|78|130|Share Folder ...|312|-1|-1|-1|New Message to Folder|700|77|77|650|Separator|0|Upload ...|303|85|85|130|Download Folder ...|304|68|68|130|Separator|0|Empty Recycle Bin Folder ...|314|-1|-1|-1|Transfer Folder Ownership|311|-1|-1|-1|Separator|0|Trace Folder Access|310|-1|-1|-1|Folder Properties and Sharing|305|-1|-1|-1|Separator|0|Close|800|67|67|650|)|Edit|-40|69|(|Move Folder ...|301|-1|-1|-1|Delete Folder ...|302|-1|-1|-1|)|View|-50|86|(|Clone Folder View|307|-1|-1|-1|Explore Folder|308|-1|-1|-1|Separator|0|Refresh Folders|306|-1|-1|-1|)|Tools|-70|84|(|Customize Toolbar ...|101|-1|-1|-1|Customize Menus ...|102|-1|-1|-1|)|Plugins|-100000|-1|)|"},
      {"MenuTreeModel."+com.CH_gui.frame.GroupTableFrame.visualsClassKeyName+"_VS_Ver6", "Program Menus|-1|0|(|File|-10|70|(|Invite to the Group|312|-1|-1|-1|Separator|0|Close|800|67|67|650|)|View|-50|86|(|Table Columns ...|1202|-1|-1|-1|Sort Table By|-51|83|(|Member|1203|-1|-1|-1|Add Members|1204|-1|-1|-1|Remove Members|1205|-1|-1|-1|Separator|0|Ascending|1200|-1|-1|-1|Descending|1201|-1|-1|-1|)|)|Tools|-70|84|(|Customize Toolbar ...|101|-1|-1|-1|Customize Menus ...|102|-1|-1|-1|)|Plugins|-100000|-1|)|"},
      {"MenuTreeModel."+com.CH_gui.frame.LocalFileTableFrame.visualsClassKeyName+"_VS_Ver6", "Program Menus|-1|0|(|File|-10|70|(|Upload File ...|902|85|85|650|Wipe File ...|703|127|127|0|Separator|0|Close|800|67|67|650|)|Edit|-40|69|View|-50|86|(|Clone File View|410|-1|-1|-1|Refresh Files|708|-1|-1|-1|)|Tools|-70|84|(|Customize Toolbar ...|101|-1|-1|-1|Customize Menus ...|102|-1|-1|-1|)|Plugins|-100000|-1|)|"},
      {"MenuTreeModel."+com.CH_gui.frame.MainFrame.visualsClassKeyName+"_VS_Ver6", "Program Menus|-1|0|(|File|-10|70|(|New Message|503|78|77|130|Chat|508|72|72|130|Open|716|79|79|130|Separator|0|New Folder ...|300|78|78|130|Create Shared Space ...|510|-1|-1|-1|Invite to the Conversation ...|312|-1|-1|-1|Add to Address Book|511|-1|-1|-1|Separator|0|Forward Message(s) ...|705|70|70|130|Upload File ...|902|85|85|650|Upload ...|303|85|85|130|Download ...|400|68|68|650|Download Folder ...|304|68|68|130|Separator|0|Print ...|723|-1|-1|-1|Separator|0|Import Address Book ...|212|-1|-1|-1|Switch Identity|209|-1|-1|-1|Separator|0|Folder Properties and Sharing|305|-1|-1|-1|File Properties|404|-1|-1|-1|Separator|0|Exit|200|88|88|650|)|Message|-20|77|(|New Message to Folder|700|77|77|650|New Message from Draft|718|-1|-1|-1|Separator|0|Reply to Sender ...|707|82|82|130|Reply to All ...|722|-1|82|195|Post Reply to Folder ...|715|80|80|130|Save Attachment(s) ...|706|84|84|130|Invite Sender|714|-1|-1|-1|Add Sender to Address Book|721|-1|-1|-1|Separator|0|Message Properties|704|-1|-1|-1|)|Chat|-22|67|(|Send|750|83|83|130|Save as Draft|764|-1|-1|-1|Select Attachments|752|76|76|130|Priority|-21|80|(|FYI|756|-1|49|130|Normal|757|-1|50|130|High|758|-1|51|130|)|Separator|0|Undo|759|85|90|130|Redo|760|82|89|130|Separator|0|Cut|753|88|88|130|Copy|754|67|67|130|Paste|755|86|86|130|)|Contacts|-30|78|(|Find Friends and Associates ...|500|-1|-1|-1|Invite Friends and Associates ...|509|-1|-1|-1|Create Group|512|-1|-1|-1|Accept / Decline Contact(s) ...|501|-1|-1|-1|Delete Contact ...|502|-1|-1|-1|Separator|0|Show Other's Contacts|506|-1|-1|-1|Contact Properties|504|-1|-1|-1|)|Edit|-40|69|(|Add Star|730|-1|-1|-1|Remove Star|731|-1|-1|-1|Mark as Read|709|82|82|650|Mark as Unread|710|85|85|650|Mark All Read|711|65|65|650|Separator|0|Move Folder ...|301|-1|-1|-1|Delete Folder ...|302|-1|-1|-1|Separator|0|Copy to Folder ...|701|-1|-1|-1|Move to Folder ...|702|-1|-1|-1|Delete ...|703|127|127|0|Revoke Message(s) ...|724|-1|-1|-1|Empty Folder ...|314|-1|-1|-1|)|View|-50|86|(|Clone Folder View|307|-1|-1|-1|Explore Folder|308|-1|-1|-1|Separator|0|Clone File View|410|-1|-1|-1|Clone Message View|712|-1|-1|-1|Clone Contact List View|507|-1|-1|-1|Separator|0|Search|727|-1|-1|-1|Split Left-Right|726|-1|-1|-1|Table Columns ...|1202|-1|-1|-1|Sort Table By|-51|83|(|Priority|1203|-1|-1|-1|Attachment|1204|-1|-1|-1|Flag|1205|-1|-1|-1|From|1206|-1|-1|-1|To|1207|-1|-1|-1|Message|1208|-1|-1|-1|Sent|1209|-1|-1|-1|Link ID|1210|-1|-1|-1|Data ID|1211|-1|-1|-1|Fetch count|1212|-1|-1|-1|Created|1213|-1|-1|-1|Updated|1214|-1|-1|-1|Delivered|1215|-1|-1|-1|Size on Disk|1216|-1|-1|-1|In reply to|1217|-1|-1|-1|Secure Lock|1218|-1|-1|-1|Email Address|1219|-1|-1|-1|Business Phone|1220|-1|-1|-1|Home Phone|1221|-1|-1|-1|Expiration|1222|-1|-1|-1|Password|1223|-1|-1|-1|Separator|0|Ascending|1200|-1|-1|-1|Descending|1201|-1|-1|-1|Separator|0|Group Chat Messages by Conversation|717|-1|-1|-1|)|Separator|0|Contact Columns ...|524|-1|-1|-1|Sort Contacts By|-52|83|(|Direction|515|-1|-1|-1|Name|516|-1|-1|-1|Status|517|-1|-1|-1|Contact ID|518|-1|-1|-1|User ID|519|-1|-1|-1|Encryption|520|-1|-1|-1|Created|521|-1|-1|-1|Updated|522|-1|-1|-1|Permissions|523|-1|-1|-1|Separator|0|Ascending|513|-1|-1|-1|Descending|514|-1|-1|-1|)|Separator|0|Refresh Folders|306|-1|-1|-1|Refresh Messages|708|-1|-1|-1|Refresh Contacts|505|-1|-1|-1|)|Tools|-70|84|(|Account Options|204|-1|-1|-1|Allowed Senders ...|214|-1|-1|-1|Change Username|208|-1|-1|-1|Change Password|202|-1|-1|-1|Setup Password Recovery|215|-1|-1|-1|Delete Account ...|211|-1|-1|-1|Separator|0|Manage User Accounts|210|-1|-1|-1|Separator|0|Spell Checker|-72|83|(|Spelling ...|765|-1|118|130|Edit user dictionary ...|766|-1|-1|-1|Spelling preferences ...|767|-1|-1|-1|)|Trace Folder Access|310|-1|-1|-1|Trace Message Access|713|-1|-1|-1|Transfer Folder Ownership|311|-1|-1|-1|Separator|0|Customize Toolbar ...|101|-1|-1|-1|Customize Menus ...|102|-1|-1|-1|Display Tool Tips|100|-1|-1|-1|)|Help|-100|72|(|General FAQ|205|-1|-1|-1|Quick Tour|206|-1|-1|-1|User's Guide|207|-1|-1|-1|Account Upgrade and Renewal|213|-1|-1|-1|Separator|0|Email Support|217|-1|-1|-1|Problem Reporting|216|-1|-1|-1|Separator|0|About CryptoHeaven|201|-1|-1|-1|)|Plugins|-100000|-1|)|"},
      {"MenuTreeModel."+com.CH_gui.frame.MessageFrame.visualsClassKeyName+"_VS_Ver6", "Program Menus|-1|0|(|File|-10|70|(|Send|750|83|83|130|Save as Draft|764|-1|-1|-1|Separator|0|Select Recipients|751|82|82|130|Select Attachments|752|76|76|130|Voice Recording Panel|768|-1|-1|-1|Priority|-21|80|(|FYI|756|-1|49|130|Normal|757|-1|50|130|High|758|-1|51|130|)|Show Advanced|761|-1|-1|-1|Separator|0|Close|800|67|67|650|)|Edit|-23|69|(|Undo|759|85|90|130|Redo|760|82|89|130|Separator|0|Cut|753|88|88|130|Copy|754|67|67|130|Paste|755|86|86|130|)|Tools|-70|84|(|Spelling ...|765|-1|118|130|Edit user dictionary ...|766|-1|-1|-1|Spelling preferences ...|767|-1|-1|-1|Separator|0|Customize Toolbar ...|101|-1|-1|-1|Customize Menus ...|102|-1|-1|-1|)|Plugins|-100000|-1|)|"},
      {"MenuTreeModel."+com.CH_gui.frame.MsgPreviewFrame.visualsClassKeyName+"_VS_Ver6", "Program Menus|-1|0|(|File|-2|70|(|Download ...|400|68|68|650|Forward Message(s) ...|705|70|70|130|Separator|0|Print ...|723|-1|-1|-1|Separator|0|Close|800|67|67|650|)|Message|-20|77|(|New Message from Draft|718|-1|-1|-1|Reply to Sender ...|707|82|82|130|Reply to All ...|722|-1|82|195|Save Attachment(s) ...|706|84|84|130|Invite Sender|714|-1|-1|-1|Add Sender to Address Book|721|-1|-1|-1|Separator|0|Trace Message Access|713|-1|-1|-1|Message Properties|704|-1|-1|-1|Separator|0|Separator|0|)|Edit|-40|69|(|Add Star|730|-1|-1|-1|Remove Star|731|-1|-1|-1|Mark as Read|709|82|82|650|Mark as Unread|710|85|85|650|Separator|0|Copy to Folder ...|701|-1|-1|-1|Move to Folder ...|702|-1|-1|-1|Delete ...|703|127|127|0|Revoke Message(s) ...|724|-1|-1|-1|)|Tools|-70|84|(|Customize Toolbar ...|101|-1|-1|-1|Customize Menus ...|102|-1|-1|-1|)|Plugins|-100000|-1|)|"},
      {"MenuTreeModel."+com.CH_gui.frame.MsgTableFrame.visualsClassKeyName+"_VS_Ver6", "Program Menus|-1|0|(|File|-10|70|(|Open|716|79|79|130|Share Folder ...|312|-1|-1|-1|Download ...|400|68|68|650|Forward Message(s) ...|705|70|70|130|Separator|0|Print ...|723|-1|-1|-1|Separator|0|Close|800|67|67|650|)|Message|-20|77|(|New Message to Folder|700|77|77|650|New Message from Draft|718|-1|-1|-1|Separator|0|Reply to Sender ...|707|82|82|130|Reply to All ...|722|-1|82|195|Post Reply to Folder ...|715|80|80|130|Save Attachment(s) ...|706|84|84|130|Invite Sender|714|-1|-1|-1|Add Sender to Address Book|721|-1|-1|-1|Separator|0|Trace Message Access|713|-1|-1|-1|Message Properties|704|-1|-1|-1|)|Edit|-40|69|(|Add Star|730|-1|-1|-1|Remove Star|731|-1|-1|-1|Mark as Read|709|82|82|650|Mark as Unread|710|85|85|650|Mark All Read|711|65|65|650|Separator|0|Copy to Folder ...|701|-1|-1|-1|Move to Folder ...|702|-1|-1|-1|Delete ...|703|127|127|0|Revoke Message(s) ...|724|-1|-1|-1|)|View|-50|86|(|Clone Message View|712|-1|-1|-1|Separator|0|Search|727|-1|-1|-1|Split Left-Right|726|-1|-1|-1|Table Columns ...|1202|-1|-1|-1|Sort Table By|-51|83|(|Priority|1203|-1|-1|-1|Attachment|1204|-1|-1|-1|Flag|1205|-1|-1|-1|From|1206|-1|-1|-1|To|1207|-1|-1|-1|Subject|1208|-1|-1|-1|Sent|1209|-1|-1|-1|Link ID|1210|-1|-1|-1|Data ID|1211|-1|-1|-1|Fetch count|1212|-1|-1|-1|Created|1213|-1|-1|-1|Updated|1214|-1|-1|-1|Delivered|1215|-1|-1|-1|Size on Disk|1216|-1|-1|-1|In reply to|1217|-1|-1|-1|Secure Lock|1218|-1|-1|-1|Expiration|1222|-1|-1|-1|Password|1223|-1|-1|-1|Separator|0|Ascending|1200|-1|-1|-1|Descending|1201|-1|-1|-1|Separator|0|Group Mail by Conversation|717|-1|-1|-1|)|Separator|0|Refresh Messages|708|-1|-1|-1|)|Tools|-70|84|(|Customize Toolbar ...|101|-1|-1|-1|Customize Menus ...|102|-1|-1|-1|)|Plugins|-100000|-1|)|"},
      {"MenuTreeModel."+com.CH_gui.frame.MsgTableStarterFrame.visualsClassKeyName+"_VS_Ver6", "Program Menus|-1|0|(|File|-10|70|(|Switch to Full Application|1400|-1|-1|-1|Separator|0|Share Folder ...|312|-1|-1|-1|Download ...|400|68|68|650|Print ...|723|-1|-1|-1|Separator|0|Close|800|67|67|650|)|Message|-20|77|(|Reply to Sender ...|707|82|82|130|Reply to All ...|722|-1|82|195|Save Attachment(s) ...|706|84|84|130|Separator|0|Message Properties|704|-1|-1|-1|)|Edit|-40|69|(|Add Star|730|-1|-1|-1|Remove Star|731|-1|-1|-1|Mark as Read|709|82|82|650|Mark as Unread|710|85|85|650|Mark All Read|711|65|65|650|Separator|0|Delete ...|703|127|127|0|)|View|-50|86|(|Search|727|-1|-1|-1|Split Left-Right|726|-1|-1|-1|Table Columns ...|1202|-1|-1|-1|Sort Table By|-51|83|(|Priority|1203|-1|-1|-1|Attachment|1204|-1|-1|-1|Flag|1205|-1|-1|-1|From|1206|-1|-1|-1|To|1207|-1|-1|-1|Subject|1208|-1|-1|-1|Sent|1209|-1|-1|-1|Link ID|1210|-1|-1|-1|Data ID|1211|-1|-1|-1|Fetch count|1212|-1|-1|-1|Created|1213|-1|-1|-1|Updated|1214|-1|-1|-1|Delivered|1215|-1|-1|-1|Size on Disk|1216|-1|-1|-1|In reply to|1217|-1|-1|-1|Secure Lock|1218|-1|-1|-1|Expiration|1222|-1|-1|-1|Password|1223|-1|-1|-1|Separator|0|Ascending|1200|-1|-1|-1|Descending|1201|-1|-1|-1|)|Separator|0|Refresh Messages|708|-1|-1|-1|)|Tools|-70|84|(|Trace Message Access|713|-1|-1|-1|Separator|0|Customize Toolbar ...|101|-1|-1|-1|Customize Menus ...|102|-1|-1|-1|)|Help|-100|72|(|General FAQ|205|-1|-1|-1|Quick Tour|206|-1|-1|-1|User's Guide|207|-1|-1|-1|Account Upgrade and Renewal|213|-1|-1|-1|Separator|0|About CryptoHeaven|201|-1|-1|-1|)|Plugins|-100000|-1|)|"},
      {"MenuTreeModel."+com.CH_gui.frame.PostTableFrame.visualsClassKeyName+"_VS_Ver6", "Program Menus|-1|0|(|File|-10|70|(|Open|716|79|79|130|Share Folder ...|312|-1|-1|-1|Download ...|400|68|68|650|Forward Message(s) ...|705|70|70|130|Separator|0|Print ...|723|-1|-1|-1|Separator|0|Close|800|67|67|650|)|Message|-20|77|(|New Message to Folder|700|77|77|650|New Message from Draft|718|-1|-1|-1|Separator|0|Reply to Sender ...|707|82|82|130|Reply to All ...|722|-1|82|195|Post Reply to Folder ...|715|80|80|130|Save Attachment(s) ...|706|84|84|130|Invite Sender|714|-1|-1|-1|Add Sender to Address Book|721|-1|-1|-1|Separator|0|Trace Message Access|713|-1|-1|-1|Message Properties|704|-1|-1|-1|)|Edit|-40|69|(|Add Star|730|-1|-1|-1|Remove Star|731|-1|-1|-1|Mark as Read|709|82|82|650|Mark as Unread|710|85|85|650|Mark All Read|711|65|65|650|Separator|0|Copy to Folder ...|701|-1|-1|-1|Move to Folder ...|702|-1|-1|-1|Delete ...|703|127|127|0|Revoke Message(s) ...|724|-1|-1|-1|)|View|-50|86|(|Clone Message View|712|-1|-1|-1|Separator|0|Search|727|-1|-1|-1|Table Columns ...|1202|-1|-1|-1|Sort Table By|-51|83|(|Priority|1203|-1|-1|-1|Attachment|1204|-1|-1|-1|Flag|1205|-1|-1|-1|From|1206|-1|-1|-1|To|1207|-1|-1|-1|Posting|1208|-1|-1|-1|Sent|1209|-1|-1|-1|Link ID|1210|-1|-1|-1|Data ID|1211|-1|-1|-1|Fetch count|1212|-1|-1|-1|Created|1213|-1|-1|-1|Updated|1214|-1|-1|-1|Delivered|1215|-1|-1|-1|Size on Disk|1216|-1|-1|-1|In reply to|1217|-1|-1|-1|Secure Lock|1218|-1|-1|-1|Expiration|1222|-1|-1|-1|Password|1223|-1|-1|-1|Separator|0|Ascending|1200|-1|-1|-1|Descending|1201|-1|-1|-1|Separator|0|Group Postings by Conversation|717|-1|-1|-1|)|Separator|0|Refresh Messages|708|-1|-1|-1|)|Tools|-70|84|(|Separator|0|Customize Toolbar ...|101|-1|-1|-1|Customize Menus ...|102|-1|-1|-1|)|Plugins|-100000|-1|)|"},
      {"MenuTreeModel."+com.CH_gui.frame.RecycleTableFrame.visualsClassKeyName+"_VS_Ver6", "Program Menus|-1|0|(|File|-10|70|(|Open|716|79|79|130|Share Folder ...|312|-1|-1|-1|Download ...|400|68|68|650|Forward ...|705|70|70|130|Separator|0|Trace Access|713|-1|-1|-1|Properties|404|-1|-1|-1|Separator|0|Close|800|67|67|650|)|Edit|-40|69|(|Move to Folder ...|702|-1|-1|-1|Delete ...|703|127|127|0|)|View|-50|86|(|Clone File View|410|-1|-1|-1|Clone Message View|712|-1|-1|-1|Separator|0|Search|727|-1|-1|-1|Table Columns ...|1202|-1|-1|-1|Sort Table By|-51|83|(|Flag|1203|-1|-1|-1|Name|1204|-1|-1|-1|From|1205|-1|-1|-1|Type|1206|-1|-1|-1|Size|1207|-1|-1|-1|Created|1208|-1|-1|-1|Deleted|1209|-1|-1|-1|Link ID|1210|-1|-1|-1|Data ID|1211|-1|-1|-1|Separator|0|Ascending|1200|-1|-1|-1|Descending|1201|-1|-1|-1|)|Separator|0|Refresh|708|-1|-1|-1|)|Tools|-70|84|(|Separator|0|Customize Toolbar ...|101|-1|-1|-1|Customize Menus ...|102|-1|-1|-1|)|Plugins|-100000|-1|)|"},
      {"MenuTreeModel."+com.CH_gui.frame.SubUserTableFrame.visualsClassKeyName+"_VS_Ver6", "Program Menus|-1|0|(|File|-10|70|(|Create New|1300|78|78|130|Edit Account|1301|69|69|130|Activate or Suspend ...|1307|-1|-1|-1|Password Reset ...|1308|-1|-1|-1|Delete Account ...|1302|-1|-1|-1|Separator|0|Manage Contacts|1306|-1|-1|-1|Send Message ...|1303|77|77|130|Separator|0|Close|800|67|67|650|)|View|-50|86|(|Clone Account List View|1305|-1|-1|-1|Separator|0|Table Columns ...|1202|-1|-1|-1|Sort Table By|-51|83|(|Username|1203|-1|-1|-1|User ID|1204|-1|-1|-1|Messaging|1205|-1|-1|-1|Email Address|1206|-1|-1|-1|Other Contact Address|1207|-1|-1|-1|Storage Limit|1208|-1|-1|-1|Storage Used|1209|-1|-1|-1|Last Login|1210|-1|-1|-1|Date Created|1211|-1|-1|-1|Status|1212|-1|-1|-1|Separator|0|Ascending|1200|-1|-1|-1|Descending|1201|-1|-1|-1|)|Separator|0|Refresh Accounts|1304|-1|-1|-1|)|Tools|-70|84|(|Customize Toolbar ...|101|-1|-1|-1|Customize Menus ...|102|-1|-1|-1|)|Plugins|-100000|-1|)|"},
      {"MenuTreeModel."+com.CH_gui.frame.WhiteListTableFrame.visualsClassKeyName+"_VS_Ver6", "Program Menus|-1|0|(|File|-10|70|(|Open|716|79|79|130|Share Address Book ...|312|-1|-1|-1|Download ...|400|68|68|650|Forward Address(es) ...|705|70|70|130|Separator|0|Print ...|723|-1|-1|-1|Separator|0|Close|800|67|67|650|)|Address|-20|65|(|New Address to Folder|700|77|77|650|New Address from Draft ...|718|-1|-1|-1|Separator|0|Compose to Address(es) ...|707|82|82|130|Reply to All ...|722|-1|82|195|Edit Address|715|80|80|130|Save Attachment(s) ...|706|84|84|130|Invite by Email|714|-1|-1|-1|Add Sender to Address Book|721|-1|-1|-1|Separator|0|Trace Address Access|713|-1|-1|-1|Address Properties|704|-1|-1|-1|)|Edit|-40|69|(|Add Star|730|-1|-1|-1|Remove Star|731|-1|-1|-1|Mark as Read|709|82|82|650|Mark as Unread|710|85|85|650|Mark All Read|711|65|65|650|Separator|0|Copy to Folder ...|701|-1|-1|-1|Move to Folder ...|702|-1|-1|-1|Delete ...|703|127|127|0|Revoke Address(es) ...|724|-1|-1|-1|)|View|-50|86|(|Clone Address View|712|-1|-1|-1|Separator|0|Search|727|-1|-1|-1|Split Top-Bottom|726|-1|-1|-1|Table Columns ...|1202|-1|-1|-1|Sort Table By|-51|83|(|Priority|1203|-1|-1|-1|Attachment|1204|-1|-1|-1|Flag|1205|-1|-1|-1|From|1206|-1|-1|-1|To|1207|-1|-1|-1|Name|1208|-1|-1|-1|Sent|1209|-1|-1|-1|Link ID|1210|-1|-1|-1|Data ID|1211|-1|-1|-1|Fetch count|1212|-1|-1|-1|Created|1213|-1|-1|-1|Updated|1214|-1|-1|-1|Delivered|1215|-1|-1|-1|Size on Disk|1216|-1|-1|-1|In reply to|1217|-1|-1|-1|Secure Lock|1218|-1|-1|-1|Email Address|1219|-1|-1|-1|Business Phone|1220|-1|-1|-1|Home Phone|1221|-1|-1|-1|Expiration|1222|-1|-1|-1|Password|1223|-1|-1|-1|Separator|0|Ascending|1200|-1|-1|-1|Descending|1201|-1|-1|-1|Separator|0|Group Mail by Conversation|717|-1|-1|-1|)|Separator|0|Refresh Addresses|708|-1|-1|-1|)|Tools|-70|84|(|Separator|0|Customize Toolbar ...|101|-1|-1|-1|Customize Menus ...|102|-1|-1|-1|)|Plugins|-100000|80|)|"},

      {"ToolBarModel."+com.CH_gui.frame.AddressFrame.visualsClassKeyName+"_VS_Ver6", "Save|750|Select Recipients|751|Select Attachments|752|Voice Recording Panel|768|Separator|0|Undo|759|Redo|760|Separator|0|Cut|753|Copy|754|Paste|755|"},
      {"ToolBarModel."+com.CH_gui.frame.AddressTableFrame.visualsClassKeyName+"_VS_Ver6", "Share Address Book ...|312|Separator|0|New Address to Folder|700|Compose to Address(es) ...|707|Forward Address(es) ...|705|Edit Address|715|Save Attachment(s) ...|706|Separator|0|Copy to Folder ...|701|Move to Folder ...|702|Delete ...|703|Separator|0|Previous|720|Next|719|Separator|0|Search|727|Print ...|723|"},
      {"ToolBarModel."+com.CH_gui.frame.ChatTableFrame.visualsClassKeyName+"_VS_Ver6", "Send|750|Select Attachments|752|Invite to the Conversation ...|312|Separator|0|New Message to Folder|700|Reply to Sender ...|707|Reply to All ...|722|Forward Message(s) ...|705|Post Reply to Folder ...|715|Save Attachment(s) ...|706|Separator|0|Move to Folder ...|702|Delete ...|703|Separator|0|Mark All Read|711|Search|727|Print ...|723|"},
      {"ToolBarModel."+com.CH_gui.frame.ContactTableFrame.visualsClassKeyName+"_VS_Ver6", "New Message|503|Chat|508|Separator|0|Find Friends and Associates ...|500|Accept / Decline Contact(s) ...|501|Delete Contact ...|502|Separator|0|Invite Friends and Associates ...|509|"},
      {"ToolBarModel."+com.CH_gui.frame.FileTableFrame.visualsClassKeyName+"_VS_Ver6", "Open|716|Share Folder ...|312|Download ...|400|Forward File ...|705|Separator|0|Copy to Folder ...|701|Move to Folder ...|702|Delete ...|703|Separator|0|Mark as Seen|709|Mark as Unseen|710|Mark All Seen|711|Search|727|"},
      {"ToolBarModel."+com.CH_gui.frame.FindUserFrame.visualsClassKeyName+"_VS_Ver6", "Add to Contact List ...|600|Send Message ...|601|Separator|0|Invite Friends and Associates ...|1000|"},
      {"ToolBarModel."+com.CH_gui.frame.FolderTreeFrame.visualsClassKeyName+"_VS_Ver6", "New Folder ...|300|Share Folder ...|312|Separator|0|Upload ...|303|Download Folder ...|304|Separator|0|New Message to Folder|700|Separator|0|Move Folder ...|301|Delete Folder ...|302|"},
      {"ToolBarModel."+com.CH_gui.frame.GroupTableFrame.visualsClassKeyName+"_VS_Ver6", "Invite to the Group|312|"},
      {"ToolBarModel."+com.CH_gui.frame.LocalFileTableFrame.visualsClassKeyName+"_VS_Ver6", "Upload File ...|902|Wipe File|703|"},
      {"ToolBarModel."+com.CH_gui.frame.MainFrame.visualsClassKeyName+"_VS_Ver6", "New Message|503|Chat|508|Open|716|Separator|0|Upload ...|303|Download ...|400|Download Folder ...|304|New Folder ...|300|Invite to the Conversation ...|312|Separator|0|New Message to Folder|700|Reply to Sender ...|707|Reply to All ...|722|Forward Message(s) ...|705|Save Attachment(s) ...|706|Separator|0|Move to Folder ...|702|Delete ...|703|Separator|0|Mark All Read|711|Search|727|Print ...|723|Separator|0|Find Friends and Associates ...|500|Invite Friends and Associates ...|509|"},
      {"ToolBarModel."+com.CH_gui.frame.MessageFrame.visualsClassKeyName+"_VS_Ver6", "Send|750|Select Recipients|751|Select Attachments|752|Voice Recording Panel|768|Separator|0|Undo|759|Redo|760|Separator|0|Cut|753|Copy|754|Paste|755|Separator|0|Spelling ...|765|"},
      {"ToolBarModel."+com.CH_gui.frame.MsgPreviewFrame.visualsClassKeyName+"_VS_Ver6", "Reply to Sender ...|707|Reply to All ...|722|Forward Message(s) ...|705|Save Attachment(s) ...|706|Add Sender to Address Book|721|Invite Sender|714|Separator|0|Copy to Folder ...|701|Move to Folder ...|702|Delete ...|703|Separator|0|Mark as Read|709|Mark as Unread|710|Separator|0|Previous|720|Next|719|Print ...|723|"},
      {"ToolBarModel."+com.CH_gui.frame.MsgTableFrame.visualsClassKeyName+"_VS_Ver6", "Share Folder ...|312|Separator|0|New Message to Folder|700|Reply to Sender ...|707|Reply to All ...|722|Forward Message(s) ...|705|Save Attachment(s) ...|706|Add Sender to Address Book|721|Invite Sender|714|Separator|0|Copy to Folder ...|701|Move to Folder ...|702|Delete ...|703|Separator|0|Mark as Read|709|Mark as Unread|710|Mark All Read|711|Search|727|Print ...|723|"},
      {"ToolBarModel."+com.CH_gui.frame.MsgTableStarterFrame.visualsClassKeyName+"_VS_Ver6", "Reply to Sender ...|707|Reply to All ...|722|Save Attachment(s) ...|706|Download ...|400|Separator|0|Delete ...|703|Separator|0|Mark as Read|709|Mark as Unread|710|Mark All Read|711|Separator|0|Search|727|Print ...|723|"},
      {"ToolBarModel."+com.CH_gui.frame.PostTableFrame.visualsClassKeyName+"_VS_Ver6", "Share Folder ...|312|Separator|0|New Message to Folder|700|Reply to Sender ...|707|Reply to All ...|722|Forward Message(s) ...|705|Save Attachment(s) ...|706|Add Sender to Address Book|721|Invite Sender|714|Separator|0|Copy to Folder ...|701|Move to Folder ...|702|Delete ...|703|Separator|0|Mark All Read|711|Search|727|Print ...|723|"},
      {"ToolBarModel."+com.CH_gui.frame.RecycleTableFrame.visualsClassKeyName+"_VS_Ver6", "Download ...|400|Forward ...|705|Separator|0|Move to Folder ...|702|Delete ...|703|Separator|0|Search|727|Empty Folder ...|1615|"},
      {"ToolBarModel."+com.CH_gui.frame.SubUserTableFrame.visualsClassKeyName+"_VS_Ver6", "Create New|1300|Edit Account|1301|Activate or Suspend ...|1307|Password Reset ...|1308|Delete Account ...|1302|Separator|0|Send Message ...|1303|Manage Contacts|1306|"},
      {"ToolBarModel."+com.CH_gui.frame.WhiteListTableFrame.visualsClassKeyName+"_VS_Ver6", "Share Address Book ...|312|Separator|0|New Address to Folder|700|Compose to Address(es) ...|707|Forward Address(es) ...|705|Edit Address|715|Separator|0|Copy to Folder ...|701|Move to Folder ...|702|Delete ...|703|Separator|0|Mark All Read|711|Separator|0|Previous|720|Next|719|Separator|0|Search|727|Print ...|723|"},

      {"ToolBarModel."+com.CH_gui.addressBook.AddressTableComponent.visualsClassKeyName+"_VS", "New Message|729|New Address to Folder|700|Compose to Address(es) ...|707|Invite by Email|714|Edit ...|715|Separator|0|Move ...|702|Delete ...|703|Separator|0|Search|727|Print ...|723|"},
      {"ToolBarModel."+com.CH_gui.chatTable.ChatTableComponent4Frame.visualsClassKeyName+"_VS", "Send|750|Select Attachments|752|Share Folder ...|312|Separator|0|New Message to Folder|700|Post Reply to Folder ...|715|Forward ...|705|Save Attachment(s) ...|706|Separator|0|Move ...|702|Delete ...|703|Separator|0|Mark All Read|711|Separator|0|Search|727|Print ...|723|"},
      {"ToolBarModel."+com.CH_gui.chatTable.ChatTableComponent.visualsClassKeyName+"_VS", "Send|750|Select Attachments|752|Separator|0|New Message to Folder|700|Post Reply to Folder ...|715|Forward ...|705|Save Attachment(s) ...|706|Separator|0|Move ...|702|Delete ...|703|Separator|0|Mark All Read|711|Separator|0|Search|727|Print ...|723|"},
      {"ToolBarModel."+com.CH_gui.contactTable.ContactTableComponent4Frame.visualsClassKeyName+"_VS_Ver1", "New Message|503|Chat|508|Separator|0|Create Shared Space ...|510|Create Group|512|Separator|0|Find Friends and Associates ...|500|Invite Friends and Associates ...|509|"},
      {"ToolBarModel."+com.CH_gui.contactTable.ContactTableComponent.visualsClassKeyName+"_VS_Ver1", "Chat|508|Create Shared Space ...|510|Find Friends and Associates ...|500|Invite Friends and Associates ...|509|"},
      {"ToolBarModel."+com.CH_gui.localFileTable.FileChooserComponent.visualsClassKeyName+"_Browse_VS", "Upload File ...|902|Wipe File|703|"},
      {"ToolBarModel."+com.CH_gui.fileTable.FileTableComponent.visualsClassKeyName+"_VS", "Open|716|Forward ...|705|Download ...|400|Separator|0|Move ...|702|Delete ...|703|Separator|0|Mark as Seen|709|Mark as Unseen|710|Separator|0|Search|727|"},
      {"ToolBarModel."+com.CH_gui.tree.FolderTreeComponent4Frame.visualsClassKeyName+"_VS", "New Message|315|New Folder ...|300|Share Folder ...|312|Separator|0|Upload ...|303|Download Folder ...|304|Separator|0|Move Folder ...|301|Delete Folder ...|302|"},
      {"ToolBarModel."+com.CH_gui.tree.FolderTreeComponent.visualsClassKeyName+"_VS", "New Folder ...|300|Share Folder ...|312|Upload ...|303|Download Folder ...|304|"},
      {"ToolBarModel."+com.CH_gui.groupTable.GroupTableComponent4Frame.visualsClassKeyName+"_VS", "Invite to the Group|312|Separator|0|New Message To Group|1501|New Message To Member|1502|"},
      {"ToolBarModel."+com.CH_gui.groupTable.GroupTableComponent.visualsClassKeyName+"_VS", "New Message To Group|1501|New Message To Member|1502|"},
      {"ToolBarModel."+com.CH_gui.msgTable.MsgDraftsTableComponent.visualsClassKeyName+"_VS", "New Message|729|New Message from Draft|718|Forward ...|705|Download ...|400|Save Attachment(s) ...|706|Separator|0|Delete ...|703|Separator|0|Search|727|Print ...|723|"},
      {"ToolBarModel."+com.CH_gui.msgTable.MsgInboxTableComponent.visualsClassKeyName+"_VS", "New Message|729|Reply to Sender ...|707|Forward ...|705|Download ...|400|Save Attachment(s) ...|706|Separator|0|Move ...|702|Delete ...|703|Separator|0|Mark as Unread|710|Separator|0|Search|727|Print ...|723|Add Sender to Address Book|721|Invite Sender|714|"},
      {"ToolBarModel."+com.CH_gui.msgTable.MsgSentTableComponent.visualsClassKeyName+"_VS", "New Message|729|New Message from Draft|718|Forward ...|705|Download ...|400|Save Attachment(s) ...|706|Separator|0|Move ...|702|Delete ...|703|Revoke ...|724|Separator|0|Search|727|Print ...|723|"},
      {"ToolBarModel."+com.CH_gui.msgTable.MsgSpamTableComponent.visualsClassKeyName+"_VS", "New Message|729|Reply to Sender ...|707|Forward ...|705|Download ...|400|Save Attachment(s) ...|706|Separator|0|Move ...|702|Delete ...|703|Separator|0|Mark All Read|711|Separator|0|Search|727|Print ...|723|"},
      {"ToolBarModel."+com.CH_gui.msgTable.MsgTableComponent.visualsClassKeyName+"_VS", "New Message|729|Reply to Sender ...|707|Forward ...|705|Download ...|400|Save Attachment(s) ...|706|Separator|0|Move ...|702|Delete ...|703|Separator|0|Mark as Unread|710|Separator|0|Search|727|Print ...|723|"},
      {"ToolBarModel."+com.CH_gui.postTable.PostTableComponent.visualsClassKeyName+"_VS", "New Message|729|New Message to Folder|700|Post Reply to Folder ...|715|Forward ...|705|Save Attachment(s) ...|706|Separator|0|Move ...|702|Delete ...|703|Separator|0|Mark All Read|711|Separator|0|Search|727|Print ...|723|"},
      {"ToolBarModel."+com.CH_gui.recycleTable.RecycleTableComponent.visualsClassKeyName+"_VS", "Open|716|Forward ...|705|Download ...|400|Separator|0|Move ...|702|Delete ...|703|Separator|0|Search|727|Empty Folder ...|1615|"},
      {"ToolBarModel."+com.CH_gui.userTable.SubUserTableComponent.visualsClassKeyName+"_VS", "Create New|1300|Edit Account|1301|Activate or Suspend ...|1307|Password Reset ...|1308|Delete Account ...|1302|Separator|0|Send Message ...|1303|Manage Contacts|1306|"},
      {"ToolBarModel."+com.CH_gui.userTable.UserActionTable.visualsClassKeyName+"_VS_Ver1", "Add to Contact List ...|600|Send Message ...|601|Separator|0|Invite Friends and Associates ...|1000|"},
      {"ToolBarModel."+com.CH_gui.addressBook.WhiteListTableComponent.visualsClassKeyName+"_VS", "New Message|729|New Address to Folder|700|Compose to Address(es) ...|707|Invite by Email|714|Edit ...|715|Separator|0|Move ...|702|Delete ...|703|Separator|0|Search|727|Print ...|723|"},

      {"ServerList", URLs.get(URLs.DEFAULT_SERVER_1)+" "+URLs.get(URLs.DEFAULT_SERVER_2)+" "+URLs.get(URLs.DEFAULT_SERVER_3)},
      {"ServerList_cryptoheaven.com_80", "role EngineServer ver 0 rel 0 host cryptoheaven.com port 82|role EngineServer ver 0 rel 0 host d1.cryptoheaven.com port 80|role EngineServer ver 0 rel 0 host cryptoheaven.com port 4383|role EngineServer ver 0 rel 0 host http://d3.cryptoheaven.com port 80|"},

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
      {com.CH_gui.frame.AddressFrame.visualsClassKeyName+"_VS_Ver6", "Dimension width 670 height 550 Location x 0 y 0"},
      {com.CH_gui.frame.AddressTableFrame.visualsClassKeyName+"_VS_Ver6", "Dimension width 770 height 735 Location x 0 y 0"},
      {com.CH_gui.frame.ChatTableFrame.visualsClassKeyName+"_VS_Ver6", "Dimension width 530 height 530 Location x 0 y 0"},
      {com.CH_gui.frame.ContactTableFrame.visualsClassKeyName+"_VS_Ver6", "Dimension width 333 height 392 Location x 0 y 0"},
      {com.CH_gui.frame.FileTableFrame.visualsClassKeyName+"_VS_Ver6", "Dimension width 770 height 535 Location x 0 y 0"},
      {com.CH_gui.frame.FindUserFrame.visualsClassKeyName+"_VS_Ver6", "Dimension width 500 height 600 Location x 0 y 0"},
      {com.CH_gui.frame.FolderTreeFrame.visualsClassKeyName+"_VS_Ver6", "Dimension width 385 height 427 Location x 0 y 0"},
      {com.CH_gui.frame.GroupTableFrame.visualsClassKeyName+"_VS_Ver6", "Dimension width 495 height 320 Location x 0 y 0"},
      {com.CH_gui.frame.LocalFileTableFrame.visualsClassKeyName+"_VS_Ver6", "Dimension width 500 height 500 Location x 0 y 0"},
      {com.CH_gui.frame.MainFrame.visualsClassKeyName+"_VS_Ver6", "Dimension width 1050 height 800 Location x 0 y 0"},
      {com.CH_gui.frame.MessageFrame.visualsClassKeyName+"_VS_Ver6", "Dimension width 670 height 550 Location x 0 y 0"},
      {com.CH_gui.frame.MsgPreviewFrame.visualsClassKeyName+"_VS_Ver6", "Dimension width 700 height 575 Location x 0 y 0"},
      {com.CH_gui.frame.MsgTableFrame.visualsClassKeyName+"_VS_Ver6", "Dimension width 800 height 735 Location x 0 y 0"},
      {com.CH_gui.frame.MsgTableStarterFrame.visualsClassKeyName+"_VS_Ver6", "Dimension width 600 height 600 Location x 0 y 0"},
      {com.CH_gui.frame.PostTableFrame.visualsClassKeyName+"_VS_Ver6", "Dimension width 770 height 735 Location x 0 y 0"},
      {com.CH_gui.frame.RecycleTableFrame.visualsClassKeyName+"_VS_Ver6", "Dimension width 770 height 535 Location x 0 y 0"},
      {com.CH_gui.frame.SubUserTableFrame.visualsClassKeyName+"_VS_Ver6", "Dimension width 800 height 365 Location x 0 y 0"},
      {com.CH_gui.frame.WhiteListTableFrame.visualsClassKeyName+"_VS_Ver6", "Dimension width 670 height 540 Location x 0 y 0"},

      {com.CH_gui.gui.JSplitPaneVS.visualsClassKeyName+"_"+com.CH_gui.table.TableComponent.visualsClassKeyName+"_"+com.CH_gui.chatTable.ChatTableComponent.visualsClassKeyName+"_VS", "Divider location 500 -1 90 0"},
      {com.CH_gui.gui.JSplitPaneVS.visualsClassKeyName+"_"+com.CH_gui.table.TableComponent.visualsClassKeyName+"_"+com.CH_gui.chatTable.ChatTableComponent4Frame.visualsClassKeyName+"_VS", "Divider location 300 -1 90 0"},
      {com.CH_gui.gui.JSplitPaneVS.visualsClassKeyName+"_"+com.CH_gui.table.TableComponent.visualsClassKeyName+"_"+com.CH_gui.msgTable.MsgDraftsTableComponent.visualsClassKeyName+"_VS", "Divider location 201 -1 40 0"},
      {com.CH_gui.gui.JSplitPaneVS.visualsClassKeyName+"_"+com.CH_gui.table.TableComponent.visualsClassKeyName+"_"+com.CH_gui.msgTable.MsgInboxTableComponent.visualsClassKeyName+"_VS", "Divider location 201 -1 40 0"},
      {com.CH_gui.gui.JSplitPaneVS.visualsClassKeyName+"_"+com.CH_gui.table.TableComponent.visualsClassKeyName+"_"+com.CH_gui.msgTable.MsgSentTableComponent.visualsClassKeyName+"_VS", "Divider location 201 -1 40 0"},
      {com.CH_gui.gui.JSplitPaneVS.visualsClassKeyName+"_"+com.CH_gui.table.TableComponent.visualsClassKeyName+"_"+com.CH_gui.msgTable.MsgSpamTableComponent.visualsClassKeyName+"_VS", "Divider location 201 -1 40 0"},
      {com.CH_gui.gui.JSplitPaneVS.visualsClassKeyName+"_"+com.CH_gui.table.TableComponent.visualsClassKeyName+"_"+com.CH_gui.msgTable.MsgTableComponent.visualsClassKeyName+"_VS", "Divider location 201 -1 40 0"},

      {com.CH_gui.gui.JSplitPaneVS.visualsClassKeyName+"_"+com.CH_gui.frame.ChatTableFrame.visualsClassKeyName+"_chat_VS","Divider location 320 -1 70 0"},
      {com.CH_gui.gui.JSplitPaneVS.visualsClassKeyName+"_"+com.CH_gui.table.TableComponent.visualsClassKeyName+"_chatComp_VS","Divider location 485 -1 70 0"},

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
      // useful for starting from chat link, main frame starter frame should be sized to some minimum dimension
      //{com.CH_gui.chatTable.ChatTableComponent4Frame.visualsClassKeyName+"_VS","Dimension width 510 height 500"},
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
  }

  private static boolean DEBUG = false;

  public static void initDefaultProperties() {
    GlobalProperties.initDefaultProperties(SingletonHolder_properties.DEFAULT_PROPERTIES);
  }

  private static void setDebug() {
    if (DEBUG) {
      Sounds.DEBUG__SUPPRESS_ALL_SOUNDS = true;
      KeyRecord.DEBUG__ALLOW_SHORT_KEYS = true;
      Misc.suppressAllGUI();
    }
  }

  public static void main(String[] args) {
    Misc.setIsRunningFromJNLP();

    // Controls of private label load
    boolean privateLabelLoaded = false;
    boolean skipSplashScreen = false;

    // Save original arguments incase we need to restart the application
    if (args != null)
      AutoUpdaterArgs.setOriginalArgs(args);

    if (args != null) {
      for (int i=0; i<args.length; i++) {
        if (args[i].equals("-version")) {
          System.out.println(GlobalProperties.PROGRAM_FULL_NAME);
          System.exit(0);
        } else if (args[i].equals("-no-splash")) {
          skipSplashScreen = true;
        } else if (args[i].equals("-localKeyChangePass")) {
          // Ability to recrypt locally stored keys for new name and password.
          String sKeyId = args[i+1];
          String oldName = args[i+2];
          String oldPass = args[i+3];
          String newName = args[i+4];
          String newPass = args[i+5];
          i += 5;

          try {
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
        } else if (args[i].equals("-privateLabelURL")) {
          String privateLabelURL = args[i+1];
          i++;
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

    JWindow splashWindow = null;
    if (!skipSplashScreen) {
      ImageIcon image = Images.get(ImageNums.LOGO_KEY_MAIN);
      // If for any reason there is no logo defined in private label customization, 
      // then skip the splash screen and allow the application to load.
      if (image != null) {
        splashWindow = new JWindow();
        Container c = splashWindow.getContentPane();
        JLabel splashImage = new JLabel(image);
        c.add(splashImage, BorderLayout.CENTER);
        splashImage.setPreferredSize(new Dimension(image.getIconWidth()+2, image.getIconHeight()+2));
        splashWindow.pack();
        MiscGui.setSuggestedWindowLocation(null, splashWindow);
        splashWindow.setVisible(true);
      }
    }

    // After splash screen is shown, continue with rest of initializations.
    initDefaultProperties();
    com.CH_gui.frame.MainFrameStarter.main(args, splashWindow);

    if (splashWindow != null && splashWindow.isShowing()) {
      splashWindow.setVisible(false);
      splashWindow.dispose();
    }
  }

}