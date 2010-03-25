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

package com.CH_co.util;

import java.util.*;

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
 * <b>$Revision: 1.18 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class ImageNums extends Object {

  public static final int LOGO_KEY_MAIN;
  public static final int LOGO_BANNER_MAIN;
  public static final int WINDOW_POPUP;

  public static final int KEYBOARD;
  public static final int KEY_LENGTH32;
  public static final int PRIME_CERTEINTY32;
  public static final int STORE_REMOTE32;

  public static final int TRANSPARENT16;
  //public static final int WAIT16;
  public static final int STATUS_WAITING16;
  public static final int STOPWATCH16;
  public static final int STOPWATCH_ALERT16;
  public static final int STOPWATCH_ALERT24;
  public static final int STOPWATCH_WARN16;
  public static final int WARNING16;

  //public static final int COMP_NET_LARGE;
  //public static final int COMP_NET_SMALL;
//  public static final int COMP_NET16;
//  public static final int COMP_NET32;
  //public static final int COMP_STATION_LARGE;
  //public static final int COMP_STATION_SMALL;
  public static final int MY_COMPUTER16;
  //public static final int MY_COMPUTER32;
  //public static final int CONTACT_OPEN_SMALL;
  //public static final int CONTACT_CLOSED_SMALL;
  //public static final int DESKTOP_LARGE;
  //public static final int DESKTOP_SMALL;

  public static final int SHARE_HAND_L;
  public static final int FLD_ADDR_OPEN16;
  public static final int FLD_ADDR_CLOSED16;
  public static final int FLD_ADDR_OPEN_SHARED16;
  public static final int FLD_ADDR_CLOSED_SHARED16;
  public static final int FLD_ADDR_CLOSED_SHARED24;
  public static final int FLD_CHAT16;
  public static final int FLD_CHAT_OPEN16;
  public static final int FLD_CHAT_CLOSED16;
  public static final int FLD_CHAT_CLOSED_SHARED16;
  public static final int FLD_CHAT_CLOSED_SHARED24;
  public static final int FLD_CNT_OPEN16;
  public static final int FLD_CNT_CLOSED16;
  public static final int FLD_FILES16;
  public static final int FLD_GROUPS16;
  public static final int FLD_KEY_OPEN16;
  public static final int FLD_KEY_CLOSED16;

  public static final int FLD_MAIL18_12;

  public static final int FLD_MAIL_OPEN16;
  public static final int FLD_MAIL_CLOSED16;
  public static final int FLD_MAIL_OPEN_SHARED16;
  public static final int FLD_MAIL_CLOSED_SHARED16;

  public static final int FLD_MAIL_DRAFT_OPEN16;
  public static final int FLD_MAIL_DRAFT_CLOSED16;
  public static final int FLD_MAIL_DRAFT_OPEN_SHARED16;
  public static final int FLD_MAIL_DRAFT_CLOSED_SHARED16;

  public static final int FLD_MAIL_INBOX_OPEN16;
  public static final int FLD_MAIL_INBOX_CLOSED16;
  public static final int FLD_MAIL_INBOX_OPEN_SHARED16;
  public static final int FLD_MAIL_INBOX_CLOSED_SHARED16;

  public static final int FLD_MAIL_JUNK_OPEN16;
  public static final int FLD_MAIL_JUNK_CLOSED16;
  public static final int FLD_MAIL_JUNK_OPEN_SHARED16;
  public static final int FLD_MAIL_JUNK_CLOSED_SHARED16;

  public static final int FLD_MAIL_SENT_OPEN16;
  public static final int FLD_MAIL_SENT_CLOSED16;
  public static final int FLD_MAIL_SENT_OPEN_SHARED16;
  public static final int FLD_MAIL_SENT_CLOSED_SHARED16;

  public static final int FLD_MAIL_POST_OPEN16;
  public static final int FLD_MAIL_POST_CLOSED16;
  public static final int FLD_MAIL_POST_OPEN_SHARED16;
  public static final int FLD_MAIL_POST_CLOSED_SHARED16;

  public static final int FLD_CLEAR16;
  public static final int FLD_CLEAR48;
  public static final int FLD_RECYCLE48;
  public static final int FLD_RECYCLE_CLEAR16;
  public static final int FLD_RECYCLE_CLEAR24;
  public static final int FLD_RECYCLE_CLEAR48;
  public static final int FLD_RECYCLE_EMPTY16;
  public static final int FLD_RECYCLE_EMPTY_SHARED16;

  public static final int FLD_OPEN16;
  public static final int FLD_CLOSED16;
  public static final int FLD_OPEN_SHARED16;
  public static final int FLD_CLOSED_SHARED16;
  public static final int FLD_CLOSED_SHARED24;

  //public static final int FOLDER24;
  public static final int FOLDER32;
  public static final int FOLDER_SHARED32;
  public static final int FOLDER_NEW16;
  public static final int FOLDER_NEW24;
  public static final int FOLDER_NEW_SHARED16;
  public static final int FOLDER_NEW_SHARED24;
  public static final int FOLDER_MOVE16;
  public static final int FOLDER_MOVE24;
  public static final int FOLDER_DELETE16;
  public static final int FOLDER_DELETE24;
//  public static final int FILE_TYPE_TXT;
//  public static final int FILE_TYPE_DOC;
//  public static final int FILE_TYPE_TIF;
//  public static final int FILE_TYPE_ZIP;
//  public static final int FILE_TYPE_ACE;
//  public static final int FILE_TYPE_RAR;
//  public static final int FILE_TYPE_JPG;
//  public static final int FILE_TYPE_GIF;
//  public static final int FILE_TYPE_CLASS;
//  public static final int FILE_TYPE_PSP;
//  public static final int FILE_TYPE_INI;
//  public static final int FILE_TYPE_JAVA;
//  public static final int FILE_TYPE_JAR;
//  public static final int FILE_TYPE_JSP;
//  public static final int FILE_TYPE_ASP;
//  public static final int FILE_TYPE_PSD;
//  public static final int FILE_TYPE_BMP;
//  public static final int FILE_TYPE_FLA;
//  public static final int FILE_TYPE_XLS;
//  public static final int FILE_TYPE_HTML;
//  public static final int FILE_TYPE_OBD;
//  public static final int FILE_TYPE_PPT;
//  public static final int FILE_TYPE_SHTML;
//  public static final int FILE_TYPE_WAV;
//  public static final int FILE_TYPE_BAT;
//  public static final int FILE_TYPE_EXE;
//  public static final int FILE_TYPE_DLL;
//  public static final int FILE_TYPE_SKR;
//  public static final int FILE_TYPE_ASC;
//  public static final int FILE_TYPE_PKR;
//  public static final int FILE_TYPE_EML;
//  public static final int FILE_TYPE_PDF;
//  public static final int FILE_TYPE_MDB;
//  public static final int FILE_TYPE_MOV;
//  public static final int FILE_TYPE_MP1;
//  public static final int FILE_TYPE_MPEG;
//  public static final int FILE_TYPE_PNG;
//  public static final int FILE_TYPE_PIC;
//  public static final int FILE_TYPE_PCX;
//  public static final int FILE_TYPE_PCD;
//  public static final int FILE_TYPE_SWF;
//  public static final int FILE_TYPE_IFF;
//  public static final int FILE_TYPE_XML;
//  public static final int FILE_TYPE_XSL;
  public static final int FILE_TYPE_OTHER;

  public static final int CLONE_ADDR16;
  public static final int CLONE_ADDR24;
  public static final int CLONE_FILE16;
  public static final int CLONE_FILE24;
  public static final int CLONE_FOLDER16;
  public static final int CLONE_FOLDER24;
  public static final int CLONE_GROUP16;
  public static final int CLONE_GROUP24;
  public static final int CLONE_MSG16;
  public static final int CLONE_MSG24;
  public static final int CLONE_CONTACT16;
  public static final int CLONE_CONTACT24;
  public static final int CLONE16;
  public static final int CLONE24;

  public static final int SPLIT_LEFT_RIGHT16;
  public static final int SPLIT_TOP_BOTTOM16;

  public static final int ARROW_LEFT16;
  public static final int ARROW_LEFT24;
  public static final int ARROW_RIGHT16;
  public static final int ARROW_DOUBLE16;
  public static final int ARROW_DOUBLE24;
  public static final int HANDSHAKE16;
  public static final int HANDSHAKE24;

  public static final int REPLY16;
  public static final int REPLY24;
  public static final int REPLY_TO_ALL16;
  public static final int REPLY_TO_ALL24;
  public static final int REPLY_TO_MSG16;
  public static final int REPLY_TO_MSG24;
  public static final int REPLY_ARROW16;
  public static final int FORWARD16;
  public static final int FORWARD24;
  public static final int FORWARD_FILE16;
  public static final int FORWARD_FILE24;

  //public static final int PRINT;

  public static final int PRIORITY_HIGH_SMALL;
  public static final int PRIORITY_LOW_SMALL;
  public static final int PRIORITY_HIGH_12;
  public static final int PRIORITY_LOW_12;
  public static final int PRIORITY_SMALL;
  public static final int LOCK_CLOSED_BLACK_SMALL;
  public static final int LOCK_CLOSED_SMALL;
  public static final int LOCK_CLOSED_WEB_SMALL;
  public static final int LOCK_OPEN_SMALL;
  public static final int LOCK_OPEN_WEB_SMALL;
  public static final int FLAG_BLANK_SMALL;
  public static final int FLAG_BLANK24;
  public static final int FLAG_BLANK_DOUBLE16;
  public static final int FLAG_BLANK_DOUBLE24;
  public static final int FLAG_GRAY_SMALL;
  public static final int FLAG_GREEN_SMALL;
  public static final int FLAG_GREEN24;
  public static final int FLAG_RED_SMALL;
  public static final int FLAG_YELLOW_SMALL;
  public static final int FLAG_GREEN_12;
  public static final int FLAG_RED_12;
  public static final int FLAG_YELLOW_12;

  public static final int TRACE_HISTORY12_13;
  public static final int TRACE_HISTORY13;
  public static final int TRACE_PRIVILEGE12_13;
  public static final int TRACE_PRIVILEGE13;

//  public static final int OK;
//  public static final int CANCEL;

  public static final int DELETE16;
  public static final int DELETE24;
  public static final int ADD14;
  public static final int GO16;
  public static final int X15;
  public static final int GO_NEXT16;
  public static final int GO_NEXT24;
  public static final int GO_PREV16;
  public static final int GO_PREV24;
  public static final int EMAIL_SYMBOL_SMALL;

  public static final int PRINT16;
  public static final int PRINT24;
  public static final int TOOLS16;
  public static final int TOOLS24;
  public static final int TOOLS_FIX16;
  public static final int TOOLS_FIX32;
  public static final int ATTACH16;
  public static final int ATTACH_14x12;
  public static final int ATTACH24;
  public static final int ATTACH_SMALL;
  public static final int DETACH16;
  public static final int DETACH24;

  //public static final int FOLDER_DOCUMENTS_LARGE;
  //public static final int FOLDER_DOCUMENTS_SMALL;

  //public static final int GLOBE_LARGE;
  //public static final int GLOBE_SMALL;
  public static final int KEY16;
  public static final int SEAL8_15;
  //public static final int KEY_TOOL;
  public static final int FRAME_LOCK32;
  //public static final int LOCK_CLOSED16;
  //public static final int LOCK_CLOSED_SMALL;
  //public static final int LOCK_OPEN_SMALL;
  //public static final int MAIL01_LARGE;
  //public static final int MAIL01_SMALL;
  //public static final int MAIL_FOLDER_SMALL;
  public static final int MAIL_CERT32;
  public static final int MAIL_UNREAD16;
  public static final int MAIL_READ16;

  public static final int MAIL_RPY16;
  public static final int MAIL_RPYFWD16;
  public static final int MAIL_FWD16;

  public static final int MAIL_COMPOSE16;
  public static final int MAIL_COMPOSE24;
  public static final int MAIL_COMPOSE_FROM_DRAFT16;
  public static final int MAIL_COMPOSE_FROM_DRAFT24;
  public static final int MAIL_COMPOSE_TO_FOLDER16;
  public static final int MAIL_COMPOSE_TO_FOLDER24;
  public static final int MAIL_COMPOSE_TO_MEMBER16;
  public static final int MAIL_COMPOSE_TO_MEMBER24;
  public static final int MAIL_SEND16;
  public static final int MAIL_SEND24;
  public static final int MAIL_SEND_INVITE_16;
  public static final int MAIL_SEND_INVITE_24;
  public static final int MAIL_SEND_INVITE_32;
  public static final int MAIL_RECIPIENTS16;
  public static final int MAIL_RECIPIENTS24;
  public static final int MAIL_COPY16;
  public static final int MAIL_COPY24;
  public static final int MAIL_DELETE16;
  public static final int MAIL_DELETE24;
  public static final int MAIL_MOVE16;
  public static final int MAIL_MOVE24;
  public static final int SAVE16;
  public static final int SAVE24;
  public static final int CHAT16;
  public static final int CHAT24;
  //public static final int NETWORK_FOLDER16;
//  public static final int NETWORK_FOLDER32;
  //public static final int NOTE_LARGE;
  //public static final int PEOPLE_ONE_SMALL;
  public static final int PERSON_SMALL;
  public static final int PEOPLE16;
  public static final int PEOPLE24;
  public static final int PEOPLE32;
  public static final int USER_FIND16;
  public static final int USER_FIND24;
  public static final int USER_FIND32;
  public static final int USER_NEW16;
  public static final int USER_NEW24;
  public static final int USER_EDIT16;
  public static final int USER_EDIT24;
  public static final int USER_ACTIVATE16;
  public static final int USER_ACTIVATE24;
  public static final int USER_PASS_RESET16;
  public static final int USER_PASS_RESET24;
  public static final int USER_DELETE16;
  public static final int USER_DELETE24;
  public static final int USER_MANAGE16;
  public static final int USER_MANAGE24;
  public static final int ADDRESS16;
  public static final int ADDRESS32;
  public static final int ADDRESS_ADD16;
  public static final int ADDRESS_ADD24;
  public static final int ADDRESS_COPY16;
  public static final int ADDRESS_COPY24;
  public static final int ADDRESS_DELETE16;
  public static final int ADDRESS_DELETE24;
  public static final int ADDRESS_EDIT16;
  public static final int ADDRESS_EDIT24;
  public static final int ADDRESS_MOVE16;
  public static final int ADDRESS_MOVE24;
  public static final int ADDRESS_SAVE16;
  public static final int ADDRESS_SAVE24;
  public static final int CONTACT16;
  public static final int CONTACT32;
  public static final int CONTACT_ADD16;
  public static final int CONTACT_ADD24;
  public static final int CONTACT_CHECK16;
  public static final int CONTACT_CHECK24;
  public static final int CONTACT_DELETE16;
  public static final int CONTACT_DELETE24;
  public static final int CONTACT_NEW32;
  public static final int GROUP_ADD16;
  public static final int GROUP_ADD24;
  public static final int MEMBER_ADD16;
  public static final int MEMBER_ADD24;
  //public static final int POST_FOLDER01_SMALL;
  //public static final int POST_READ_SMALL;
  //public static final int POST_UNREAD_SMALL;
  public static final int POSTING16;
  public static final int POSTING_CERT32;

  public static final int ARROW_DROP_DOWN_5_3;
  public static final int ORDER_ASCENDING;
  public static final int ORDER_ASCENDING2;
  public static final int ORDER_DESCENDING;
  public static final int ORDER_DESCENDING2;

  public static final int INFO16;
  public static final int ANIM_GLOBE_FIRST16;
  public static final int ANIM_GLOBE16;
  public static final int ANIM_KEY;
  public static final int ANIM_LOCK;
//  public static final int ANIM_PHONE;
  public static final int ANIM_TRANSFER;

  public static final int ANIM_TRANSFER_STOP;
  public static final int LOCK_OPENED;

  public static final int PERSON32;
  public static final int HOME32;
  public static final int PHONE32;
  public static final int MAIL32;
  public static final int WEB32;

  //public static final int DOT_GRAY16;
  //public static final int DOT_RED16;
  //public static final int DOT_GREEN16;
  //public static final int DOT_GREEN_RED16;
  //public static final int DOT_BLUE12;
  //public static final int DOT_GREEN12;
  //public static final int DOT_RED12;
//  public static final int CHECK_BLUE16;
//  //public static final int CHECK_GREEN16;
//  public static final int PERSON_AVAILABLE16;
//  public static final int PERSON_DND16;
//  public static final int PERSON_NOT_AVAILABLE16;
  public static final int STATUS_AWAY16;
  public static final int STATUS_DND16;
  public static final int STATUS_INVISIBLE16;
  public static final int STATUS_NA16;
  public static final int STATUS_OFFLINE16;
  public static final int STATUS_ONLINE16;
  public static final int STATUS_QUESTION16;
  public static final int LIGHT_ON_SMALL;
  public static final int LIGHT_ON_12;
  public static final int LIGHT_OFF_SMALL;
  public static final int LIGHT_GREEN_SMALL;
  public static final int LIGHT_X_SMALL;

  public static final int FILE32; // little modification of ADD24
  public static final int FILE_LOCKED32; // little modification of ADD24
  public static final int FILE_MOVE16;
  public static final int FILE_MOVE24;
  public static final int FILE_REMOVE16;
  public static final int FILE_REMOVE24;
  public static final int FILE_REMOVE48;
  public static final int FILE_REPLACE32;
  public static final int IMPORT_FILE16;
  public static final int IMPORT_FILE24;
  public static final int IMPORT_FOLDER16;
  public static final int IMPORT_FOLDER24;

  //public static final int ADD16;
  //public static final int ADD24;
  public static final int SELECT_ALL16;
  public static final int COPY16;
  public static final int COPY24;
  public static final int CUT16;
  public static final int CUT24;
  //public static final int FILE16; // little modification of ADD16
  //public static final int FILE24; // little modification of ADD24
  public static final int EXPORT16;
  public static final int EXPORT24;
  public static final int FIND16;
  public static final int FIND24;
  //public static final int IMPORT16;
  //public static final int IMPORT24;
  //public static final int NEW16;
  //public static final int NEW24;
  public static final int PASTE16;
  public static final int PASTE24;
  public static final int SPELL16;
  public static final int SPELL24;
  public static final int UNDO16;
  public static final int UNDO24;
  public static final int REDO16;
  public static final int REDO24;
  public static final int REFRESH16;
  public static final int REFRESH24;
  //public static final int SEARCH16;
  //public static final int SEARCH24;

  public static final int AUTO_RESPONDER32;
  public static final int SIGNATURE32;
  public static final int SHIELD32;

  public static final int HTML_IMAGE_DELAYED;
  public static final int HTML_IMAGE_FAILED;
  public static final int RESIZE_DRAG_SE; // South-East direction

  public static final int RING_BELL;
  public static final int VOLUME16;
  public static final int VOLUME24;
  public static final int RECORD16;
  public static final int RECORD24;
  public static final int PLAY16;
  public static final int PAUSE16;
  public static final int PLAY_PAUSE16;
  public static final int STOP16;

  // emotions
  public static final int EM_ARROW_LEFT;
  public static final int EM_ARROW_RIGHT;
  public static final int EM_BALL;
  public static final int EM_BAT;
  public static final int EM_BEER;
  public static final int EM_BUDDY;
  public static final int EM_CAMERA;
  public static final int EM_CAR;
  public static final int EM_COCTAIL;
  public static final int EM_COFFEE;
  public static final int EM_FACE_ANGRY;
  public static final int EM_FACE_TEETH;
  public static final int EM_FACE_CONFUSED;
  public static final int EM_FACE_CRY;
  public static final int EM_FACE_DEVIL;
  public static final int EM_FACE_DONT_TELL;
  public static final int EM_FACE_NERD;
  public static final int EM_FACE_RED;
  public static final int EM_FACE_SAD;
  public static final int EM_FACE_SARCASTIC;
  public static final int EM_FACE_SECRET;
  public static final int EM_FACE_SHADES;
  public static final int EM_FACE_SICK;
  public static final int EM_FACE_SMILE1;
  public static final int EM_FACE_SMILE2;
  public static final int EM_FACE_SMILE3;
  public static final int EM_FACE_STRAIGHT;
  public static final int EM_FACE_SURPRISED;
  public static final int EM_FACE_THINKING;
  public static final int EM_FACE_WEE;
  public static final int EM_GIFT;
  public static final int EM_HEAD_DUMB;
  public static final int EM_HEAD_TALK;
  public static final int EM_HEART;
  public static final int EM_HEARTBREAK;
  public static final int EM_KISS;
  public static final int EM_LETTER;
  public static final int EM_MAIL;
  public static final int EM_MEN;
  public static final int EM_PALMS;
  public static final int EM_PARTY;
  public static final int EM_PHONE;
  public static final int EM_PIZZA;
  public static final int EM_PLAIN;
  public static final int EM_ROSE;
  public static final int EM_THUMB_DOWN;
  public static final int EM_THUMB_UP;
  public static final int EM_WOMAN;
  public static final int EM_FLAG_RED;
  public static final int EM_FLAG_GREEN;

  public static final int NUMBER_OF_IMAGES;

  private static final String[] images;
  private static final int[] emotions;

  private static final HashSet unUsedIconNames = new HashSet();
  private static final HashSet usedIconNames = new HashSet();

  private static final HashMap imageCodesHM1 = new HashMap();
  private static final HashMap imageCodesHM2 = new HashMap();
  private static final HashMap imageCodesHM3 = new HashMap();
  private static final HashMap imageCodesHM4 = new HashMap();
  private static final HashMap imageCodesHM5 = new HashMap();

  static {
    int i = 0;
    images = new String[321+50]; // plus emotions


    /******************************************************************
     * First 3 indexed images are permanent and used in customizations.
     ******************************************************************/
    LOGO_KEY_MAIN = i;
    images[i] = "LogoKey435_260"; i++;
    LOGO_BANNER_MAIN = i;
    images[i] = "LogoBanner435_80"; i++;
    WINDOW_POPUP = i;
    images[i] = "WindowPopup"; i++;
    /******************************************************************
     * First 3 indexed images are permanent and used in customizations.
     ******************************************************************/


    KEYBOARD = i;
    images[i] = "keyboard"; i++;

    KEY_LENGTH32 = i;
    images[i] = "KeyLength32"; i++;

    PRIME_CERTEINTY32 = i;
    images[i] = "PrimeCerteinty32"; i++;

    STORE_REMOTE32 = i;
    images[i] = "StoreRemote32"; i++;

    TRANSPARENT16 = i;
    images[i] = "Transparent16"; i++;

    //WAIT16 = i;
    //images[i] = "Wait16"; i++;

    STOPWATCH16 = i;
    images[i] = "Stopwatch16"; i++;

    STOPWATCH_ALERT16 = i;
    images[i] = "StopwatchAlert16"; i++;

    STOPWATCH_ALERT24 = i;
    images[i] = "StopwatchAlert24"; i++;

    STOPWATCH_WARN16 = i;
    images[i] = "StopwatchWarn16"; i++;

    WARNING16 = i;
    images[i] = "Warning16"; i++;


    //COMP_NET_LARGE = i;
    //images[i] = "comp_net_large"; i++;

    //COMP_NET_SMALL = i;
    //images[i] = "comp_net_small"; i++;

//    COMP_NET16 = i;
//    images[i] = "CompNet16"; i++;

//    COMP_NET32 = i;
//    images[i] = "CompNet32"; i++;


    //COMP_STATION_LARGE = i;
    //images[i] = "comp_station_large"; i++;

    //COMP_STATION_SMALL = i;
    //images[i] = "comp_station_small"; i++;


    MY_COMPUTER16 = i;
    images[i] = "MyComputer16"; i++;

    //MY_COMPUTER32= i;
    //images[i] = "MyComputer32"; i++;


    //DESKTOP_LARGE = i;
    //images[i] = "desktop_large"; i++;

    //DESKTOP_SMALL = i;
    //images[i] = "desktop_small"; i++;

    //CONTACT_OPEN_SMALL = i;
    //images[i] = "contact_open_small"; i++;

    //CONTACT_CLOSED_SMALL = i;
    //images[i] = "contact_closed_small"; i++;

    SHARE_HAND_L = i;
    images[i] = "ShareHand_L"; i++;

    FLD_ADDR_OPEN16 = i;
    images[i] = "FldAddrOpen16"; i++;

    FLD_ADDR_CLOSED16 = i;
    images[i] = "FldAddrClosed16"; i++;

    FLD_ADDR_OPEN_SHARED16 = i;
    images[i] = "FldAddrOpen16"; i++;
    //images[i] = "FldAddrOpenShared16"; i++;

    FLD_ADDR_CLOSED_SHARED16 = i;
    images[i] = "FldAddrClosed16"; i++;
    //images[i] = "FldAddrClosedShared16"; i++;

    FLD_ADDR_CLOSED_SHARED24 = i;
    images[i] = "FldAddrClosedShared24"; i++;

    FLD_CHAT16 = i;
    images[i] = "Chat16"; i++;

    FLD_CHAT_OPEN16 = i;
    images[i] = "FldChatClosed16"; i++;

    FLD_CHAT_CLOSED16 = i;
    images[i] = "FldChatClosed16"; i++;

    FLD_CHAT_CLOSED_SHARED16 = i;
    images[i] = "FldChatClosed16"; i++;

    FLD_CHAT_CLOSED_SHARED24 = i;
    images[i] = "FldChatClosedShared24"; i++;

    FLD_CNT_OPEN16 = i;
    images[i] = "FldCntOpen16"; i++;

    FLD_CNT_CLOSED16 = i;
    images[i] = "FldCntClosed16"; i++;

    FLD_FILES16 = i;
    images[i] = "FldFiles16"; i++;

    FLD_GROUPS16 = i;
    images[i] = "FldGroups16"; i++;

    FLD_KEY_OPEN16 = i;
    images[i] = "FldKeyOpen16"; i++;

    FLD_KEY_CLOSED16 = i;
    images[i] = "FldKeyClosed16"; i++;


    FLD_MAIL18_12 = i;
    images[i] = "FldMail18_12"; i++;


    FLD_MAIL_OPEN16 = i;
    images[i] = "FldMailOpen16"; i++;

    FLD_MAIL_CLOSED16 = i;
    images[i] = "FldMailClosed16"; i++;

    FLD_MAIL_OPEN_SHARED16 = i;
    images[i] = "FldMailOpen16"; i++;

    FLD_MAIL_CLOSED_SHARED16 = i;
    images[i] = "FldMailClosed16"; i++;


    FLD_MAIL_DRAFT_OPEN16 = i;
    images[i] = "FldMailDraftOpen16"; i++;

    FLD_MAIL_DRAFT_CLOSED16 = i;
    images[i] = "FldMailDraftClosed16"; i++;

    FLD_MAIL_DRAFT_OPEN_SHARED16 = i;
    images[i] = "FldMailDraftOpen16"; i++;

    FLD_MAIL_DRAFT_CLOSED_SHARED16 = i;
    images[i] = "FldMailDraftClosed16"; i++;


    FLD_MAIL_INBOX_OPEN16 = i;
    images[i] = "FldMailInboxOpen16"; i++;

    FLD_MAIL_INBOX_CLOSED16 = i;
    images[i] = "FldMailInboxClosed16"; i++;

    FLD_MAIL_INBOX_OPEN_SHARED16 = i;
    images[i] = "FldMailInboxOpen16"; i++;

    FLD_MAIL_INBOX_CLOSED_SHARED16 = i;
    images[i] = "FldMailInboxClosed16"; i++;


    FLD_MAIL_JUNK_OPEN16 = i;
    images[i] = "FldMailJunkOpen16"; i++;

    FLD_MAIL_JUNK_CLOSED16 = i;
    images[i] = "FldMailJunkClosed16"; i++;

    FLD_MAIL_JUNK_OPEN_SHARED16 = i;
    images[i] = "FldMailJunkOpen16"; i++;

    FLD_MAIL_JUNK_CLOSED_SHARED16 = i;
    images[i] = "FldMailJunkClosed16"; i++;


    FLD_MAIL_SENT_OPEN16 = i;
    images[i] = "FldMailSentOpen16"; i++;

    FLD_MAIL_SENT_CLOSED16 = i;
    images[i] = "FldMailSentClosed16"; i++;

    FLD_MAIL_SENT_OPEN_SHARED16 = i;
    images[i] = "FldMailSentOpen16"; i++;

    FLD_MAIL_SENT_CLOSED_SHARED16 = i;
    images[i] = "FldMailSentClosed16"; i++;


    FLD_MAIL_POST_OPEN16 = i;
    images[i] = "FldMailPostOpen16"; i++;

    FLD_MAIL_POST_CLOSED16 = i;
    images[i] = "FldMailPostClosed16"; i++;

    FLD_MAIL_POST_OPEN_SHARED16 = i;
    images[i] = "FldMailPostOpen16"; i++;

    FLD_MAIL_POST_CLOSED_SHARED16 = i;
    images[i] = "FldMailPostClosed16"; i++;


    FLD_CLEAR16 = i;
    images[i] = "FldClear16"; i++;

    FLD_CLEAR48 = i;
    images[i] = "FldClear48"; i++;

    FLD_RECYCLE_CLEAR16 = i;
    images[i] = "FldRecycleClear16"; i++;

    FLD_RECYCLE_CLEAR24 = i;
    images[i] = "FldRecycleClear24"; i++;

    FLD_RECYCLE48 = i;
    images[i] = "FldRecycle48"; i++;

    FLD_RECYCLE_CLEAR48 = i;
    images[i] = "FldRecycleClear48"; i++;

    FLD_RECYCLE_EMPTY16 = i;
    images[i] = "FldRecycleEmpty16"; i++;

    FLD_RECYCLE_EMPTY_SHARED16 = i;
    images[i] = "FldRecycleEmpty16"; i++;

    FLD_OPEN16 = i;
    images[i] = "FldOpen16"; i++;

    FLD_CLOSED16 = i;
    images[i] = "FldClosed16"; i++;

    FLD_OPEN_SHARED16 = i;
    images[i] = "FldOpen16"; i++;

    FLD_CLOSED_SHARED16 = i;
    images[i] = "FldClosed16"; i++;
    //images[i] = "FldClosedShared16"; i++; // special pre-rendered version for MiscGui types

    FLD_CLOSED_SHARED24 = i;
    images[i] = "FldClosedShared24"; i++;

    //FOLDER24 = i;
    //images[i] = "Folder24"; i++;

    FOLDER32 = i;
    images[i] = "Folder32"; i++;

    FOLDER_SHARED32 = i;
    images[i] = "FolderShared32"; i++;

    FOLDER_NEW16 = i;
    images[i] = "FolderNew16"; i++;

    FOLDER_NEW24 = i;
    images[i] = "FolderNew24"; i++;

    FOLDER_NEW_SHARED16 = i;
    images[i] = "FolderNew16"; i++;
    //images[i] = "FolderNewShared16"; i++;

    FOLDER_NEW_SHARED24 = i;
    images[i] = "FolderNewShared24"; i++;

    FOLDER_MOVE16 = i;
    images[i] = "FolderMove16"; i++;

    FOLDER_MOVE24 = i;
    images[i] = "FolderMove24"; i++;

    FOLDER_DELETE16 = i;
    images[i] = "FolderDelete16"; i++;

    FOLDER_DELETE24 = i;
    images[i] = "FolderDelete24"; i++;

//    FILE_TYPE_TXT = i;
//    images[i] = "file_type_txt"; i++;
//    FILE_TYPE_DOC = i;
//    images[i] = "file_type_doc"; i++;
//    FILE_TYPE_TIF = i;
//    images[i] = "file_type_tif"; i++;
//    FILE_TYPE_ZIP = i;
//    images[i] = "file_type_zip"; i++;
//    FILE_TYPE_ACE = i;
//    images[i] = "file_type_ace"; i++;
//    FILE_TYPE_RAR = i;
//    images[i] = "file_type_rar"; i++;
//    FILE_TYPE_INI = i;
//    images[i] = "file_type_ini"; i++;
//    FILE_TYPE_JAVA = i;
//    images[i] = "file_type_java"; i++;
//    FILE_TYPE_JAR = i;
//    images[i] = "file_type_jar"; i++;
//    FILE_TYPE_JSP = i;
//    images[i] = "file_type_jsp"; i++;
//    FILE_TYPE_ASP = i;
//    images[i] = "file_type_asp"; i++;
//    FILE_TYPE_JPG = i;
//    images[i] = "file_type_jpg"; i++;
//    FILE_TYPE_GIF = i;
//    images[i] = "file_type_gif"; i++;
//    FILE_TYPE_CLASS = i;
//    images[i] = "file_type_class"; i++;
//    FILE_TYPE_PSP = i;
//    images[i] = "file_type_psp"; i++;
//    FILE_TYPE_PSD = i;
//    images[i] = "file_type_psd"; i++;
//    FILE_TYPE_BMP = i;
//    images[i] = "file_type_bmp"; i++;
//    FILE_TYPE_FLA = i;
//    images[i] = "file_type_fla"; i++;
//    FILE_TYPE_XLS = i;
//    images[i] = "file_type_xls"; i++;
//    FILE_TYPE_HTML = i;
//    images[i] = "file_type_html"; i++;
//    FILE_TYPE_OBD = i;
//    images[i] = "file_type_obd"; i++;
//    FILE_TYPE_PPT = i;
//    images[i] = "file_type_ppt"; i++;
//    FILE_TYPE_SHTML = i;
//    images[i] = "file_type_shtml"; i++;
//    FILE_TYPE_WAV = i;
//    images[i] = "file_type_wav"; i++;
//    FILE_TYPE_BAT = i;
//    images[i] = "file_type_bat"; i++;
//    FILE_TYPE_EXE = i;
//    images[i] = "file_type_exe"; i++;
//    FILE_TYPE_DLL = i;
//    images[i] = "file_type_dll"; i++;
//    FILE_TYPE_SKR = i;
//    images[i] = "file_type_skr"; i++;
//    FILE_TYPE_ASC = i;
//    images[i] = "file_type_asc"; i++;
//    FILE_TYPE_PKR = i;
//    images[i] = "file_type_pkr"; i++;
//    FILE_TYPE_EML = i;
//    images[i] = "file_type_eml"; i++;
//    FILE_TYPE_PDF = i;
//    images[i] = "file_type_pdf"; i++;
//    FILE_TYPE_MDB = i;
//    images[i] = "file_type_mdb"; i++;
//    FILE_TYPE_MOV = i;
//    images[i] = "file_type_mov"; i++;
//    FILE_TYPE_MP1 = i;
//    images[i] = "file_type_mp1"; i++;
//    FILE_TYPE_MPEG = i;
//    images[i] = "file_type_mpeg"; i++;
//    FILE_TYPE_PNG = i;
//    images[i] = "file_type_png"; i++;
//    FILE_TYPE_PIC = i;
//    images[i] = "file_type_pic"; i++;
//    FILE_TYPE_PCX = i;
//    images[i] = "file_type_pcx"; i++;
//    FILE_TYPE_PCD = i;
//    images[i] = "file_type_pcd"; i++;
//    FILE_TYPE_SWF = i;
//    images[i] = "file_type_swf"; i++;
//    FILE_TYPE_IFF = i;
//    images[i] = "file_type_iff"; i++;
//    FILE_TYPE_XML = i;
//    images[i] = "file_type_xml"; i++;
//    FILE_TYPE_XSL = i;
//    images[i] = "file_type_xsl"; i++;
    FILE_TYPE_OTHER = i;
    images[i] = "file_type_other"; i++;


    CLONE_ADDR16 = i;
    images[i] = "CloneAddr16"; i++;
    CLONE_ADDR24 = i;
    images[i] = "CloneAddr24"; i++;
    CLONE_FILE16 = i;
    images[i] = "CloneFile16"; i++;
    CLONE_FILE24 = i;
    images[i] = "CloneFile24"; i++;
    CLONE_FOLDER16 = i;
    images[i] = "CloneFolder16"; i++;
    CLONE_FOLDER24 = i;
    images[i] = "CloneFolder24"; i++;
    CLONE_GROUP16 = i;
    images[i] = "CloneGroup16"; i++;
    CLONE_GROUP24 = i;
    images[i] = "CloneGroup24"; i++;
    CLONE_MSG16 = i;
    images[i] = "CloneMsg16"; i++;
    CLONE_MSG24 = i;
    images[i] = "CloneMsg24"; i++;
    CLONE_CONTACT16 = i;
    images[i] = "CloneContact16"; i++;
    CLONE_CONTACT24 = i;
    images[i] = "CloneContact24"; i++;
    CLONE16 = i;
    images[i] = "Clone16"; i++;
    CLONE24 = i;
    images[i] = "Clone24"; i++;


    SPLIT_LEFT_RIGHT16 = i;
    images[i] = "SplitLeftRight16"; i++;
    SPLIT_TOP_BOTTOM16 = i;
    images[i] = "SplitTopBottom16"; i++;


    ARROW_LEFT16 = i;
    images[i] = "ArrowLeft16"; i++;
    ARROW_LEFT24 = i;
    images[i] = "ArrowLeft24"; i++;
    ARROW_RIGHT16 = i;
    images[i] = "ArrowRight16"; i++;
    ARROW_DOUBLE16 = i;
    images[i] = "ArrowDouble16"; i++;
    ARROW_DOUBLE24 = i;
    images[i] = "ArrowDouble24"; i++;

    HANDSHAKE16 = i;
    images[i] = "Handshake16"; i++;

    HANDSHAKE24 = i;
    images[i] = "Handshake24"; i++;


    REPLY16 = i;
    images[i] = "Reply16"; i++;

    REPLY24 = i;
    images[i] = "Reply24"; i++;

    REPLY_TO_ALL16 = i;
    images[i] = "ReplyToAll16"; i++;

    REPLY_TO_ALL24 = i;
    images[i] = "ReplyToAll24"; i++;

    REPLY_TO_MSG16 = i;
    images[i] = "ReplyToMsg16"; i++;

    REPLY_TO_MSG24 = i;
    images[i] = "ReplyToMsg24"; i++;

    REPLY_ARROW16 = i;
    images[i] = "ReplyArrow16"; i++;

    FORWARD16 = i;
    images[i] = "Forward16"; i++;

    FORWARD24 = i;
    images[i] = "Forward24"; i++;

    FORWARD_FILE16 = i;
    images[i] = "ForwardFile16"; i++;

    FORWARD_FILE24 = i;
    images[i] = "ForwardFile24"; i++;


    //PRINT = i;
    //images[i] = "print"; i++;


    PRIORITY_HIGH_SMALL = i;
    images[i] = "priority-high-small"; i++;

    PRIORITY_LOW_SMALL = i;
    images[i] = "priority-low-small"; i++;

    PRIORITY_HIGH_12 = i;
    images[i] = "priority-high-12"; i++;

    PRIORITY_LOW_12 = i;
    images[i] = "priority-low-12"; i++;

    PRIORITY_SMALL = i;
    images[i] = "priority-small"; i++;

    LOCK_CLOSED_BLACK_SMALL = i;
    images[i] = "lock-closed-black-small"; i++;

    LOCK_CLOSED_SMALL = i;
    images[i] = "lock-closed-small"; i++;

    LOCK_CLOSED_WEB_SMALL = i;
    images[i] = "lock-closed-web-small"; i++;

    LOCK_OPEN_SMALL = i;
    images[i] = "lock-open-small"; i++;

    LOCK_OPEN_WEB_SMALL = i;
    images[i] = "lock-open-web-small"; i++;

    FLAG_BLANK_SMALL = i;
    images[i] = "flag-blank-small"; i++;

    FLAG_BLANK24 = i;
    images[i] = "flag-blank-24"; i++;

    FLAG_BLANK_DOUBLE16 = i;
    images[i] = "flag-blank-double-16"; i++;

    FLAG_BLANK_DOUBLE24 = i;
    images[i] = "flag-blank-double-24"; i++;

    FLAG_GRAY_SMALL = i;
    images[i] = "flag-gray-small"; i++;

    FLAG_GREEN_SMALL = i;
    images[i] = "flag-green-small"; i++;

    FLAG_GREEN24 = i;
    images[i] = "flag-green-24"; i++;

    FLAG_RED_SMALL = i;
    images[i] = "flag-red-small"; i++;

    FLAG_YELLOW_SMALL = i;
    images[i] = "flag-yellow-small"; i++;

    FLAG_GREEN_12 = i;
    images[i] = "flag-green-12"; i++;

    FLAG_RED_12 = i;
    images[i] = "flag-red-12"; i++;

    FLAG_YELLOW_12 = i;
    images[i] = "flag-yellow-12"; i++;

    TRACE_HISTORY12_13 = i;
    images[i] = "TraceHist12_13"; i++;

    TRACE_HISTORY13= i;
    images[i] = "TraceHist13"; i++;

    TRACE_PRIVILEGE12_13 = i;
    images[i] = "TracePriv12_13"; i++;

    TRACE_PRIVILEGE13= i;
    images[i] = "TracePriv13"; i++;


//    OK = i;
//    images[i] = "OK"; i++;
//
//    CANCEL = i;
//    images[i] = "Cancel"; i++;


    DELETE16 = i;
    images[i] = "Delete16"; i++;

    DELETE24 = i;
    images[i] = "Delete24"; i++;

    ADD14 = i;
    images[i] = "add14"; i++;

    GO16 = i;
    images[i] = "Go16"; i++;

    X15 = i;
    images[i] = "X15"; i++;

    GO_NEXT16 = i;
    images[i] = "GoNext16"; i++;
    GO_NEXT24 = i;
    images[i] = "GoNext24"; i++;
    GO_PREV16 = i;
    images[i] = "GoPrev16"; i++;
    GO_PREV24 = i;
    images[i] = "GoPrev24"; i++;

    EMAIL_SYMBOL_SMALL = i;
    images[i] = "email-symbol-small"; i++;


    PRINT16 = i;
    images[i] = "Print16"; i++;

    PRINT24 = i;
    images[i] = "Print24"; i++;

    TOOLS16 = i;
    images[i] = "Tools16"; i++;

    TOOLS24 = i;
    images[i] = "Tools24"; i++;

    TOOLS_FIX16 = i;
    images[i] = "ToolsFix16"; i++;

    TOOLS_FIX32 = i;
    images[i] = "ToolsFix32"; i++;

    ATTACH16 = i;
    images[i] = "attach-small"; i++;

    ATTACH_14x12 = i;
    images[i] = "attach-14x12"; i++;

    ATTACH24 = i;
    images[i] = "Attach24"; i++;

    ATTACH_SMALL = i;
    images[i] = "attach-small-black"; i++;

    DETACH16 = i;
    images[i] = "Detach16"; i++;

    DETACH24 = i;
    images[i] = "Detach24"; i++;

    //FOLDER_DOCUMENTS_LARGE = i;
    //images[i] = "folder_documents_large"; i++;

    //FOLDER_DOCUMENTS_SMALL = i;
    //images[i] = "folder_documents_small"; i++;



    //GLOBE_LARGE = i;
    //images[i] = "globe_large"; i++;

    //GLOBE_SMALL = i;
    //images[i] = "globe_small"; i++;

    KEY16 = i;
    images[i] = "key16"; i++;

    SEAL8_15 = i;
    images[i] = "Seal8_15"; i++;

    //KEY_TOOL = i;
    //images[i] = "key_tool"; i++;

    FRAME_LOCK32 = i;
    images[i] = "FrameLock32"; i++;

    //LOCK_CLOSED16 = i;
    //images[i] = "lock_closed16"; i++;

    //LOCK_CLOSED_SMALL = i;
    //images[i] = "lock_closed_small"; i++;

    //LOCK_OPEN_SMALL = i;
    //images[i] = "lock_open_small"; i++;


    MAIL_CERT32 = i;
    images[i] = "MailCert32"; i++;

    MAIL_UNREAD16 = i;
    images[i] = "MailUnread16"; i++;

    MAIL_READ16 = i;
    images[i] = "MailRead16"; i++;

    MAIL_RPY16 = i;
    images[i] = "MailRpy16"; i++;

    MAIL_RPYFWD16 = i;
    images[i] = "MailRpyFwd16"; i++;

    MAIL_FWD16 = i;
    images[i] = "MailFwd16"; i++;

    MAIL_COMPOSE16 = i;
    images[i] = "ComposeMail16"; i++;

    MAIL_COMPOSE24 = i;
    images[i] = "ComposeMail24"; i++;

    MAIL_COMPOSE_FROM_DRAFT16 = i;
    images[i] = "ComposeMailFromDraft16"; i++;

    MAIL_COMPOSE_FROM_DRAFT24 = i;
    images[i] = "ComposeMailFromDraft24"; i++;

    MAIL_COMPOSE_TO_FOLDER16 = i;
    images[i] = "ComposeMailToFolder16"; i++;

    MAIL_COMPOSE_TO_FOLDER24 = i;
    images[i] = "ComposeMailToFolder24"; i++;

    MAIL_COMPOSE_TO_MEMBER16 = i;
    images[i] = "ComposeMailToMember16"; i++;

    MAIL_COMPOSE_TO_MEMBER24 = i;
    images[i] = "ComposeMailToMember24"; i++;

    MAIL_SEND16 = i;
    //images[i] = "mail_send16"; i++;
    images[i] = "SendMail16"; i++;

    MAIL_SEND24 = i;
    //images[i] = "mail_send24"; i++;
    images[i] = "SendMail24"; i++;


    MAIL_SEND_INVITE_16 = i;
    images[i] = "mail-send-invite16"; i++;

    MAIL_SEND_INVITE_24 = i;
    images[i] = "mail-send-invite24"; i++;

    MAIL_SEND_INVITE_32 = i;
    images[i] = "mail-send-invite32"; i++;


    MAIL_RECIPIENTS16 = i;
    images[i] = "AddressBook16"; i++;

    MAIL_RECIPIENTS24 = i;
    images[i] = "AddressBook24"; i++;

    MAIL_COPY16 = i;
    images[i] = "MailCopy16"; i++;

    MAIL_COPY24 = i;
    images[i] = "MailCopy24"; i++;

    MAIL_DELETE16 = i;
    images[i] = "MailDelete16"; i++;

    MAIL_DELETE24 = i;
    images[i] = "MailDelete24"; i++;

    MAIL_MOVE16 = i;
    images[i] = "MailMove16"; i++;

    MAIL_MOVE24 = i;
    images[i] = "MailMove24"; i++;

    SAVE16 = i;
    images[i] = "Save16"; i++;

    SAVE24 = i;
    images[i] = "Save24"; i++;

    CHAT16 = i;
    images[i] = "Chat16"; i++;

    CHAT24 = i;
    images[i] = "Chat24"; i++;


//    NETWORK_FOLDER32 = i;
//    images[i] = "NetworkFolder32"; i++;

    //NOTE_LARGE = i;
    //images[i] = "note_large"; i++;

    //PEOPLE_ONE_SMALL = i;
    //images[i] = "people_one_small"; i++;
    PERSON_SMALL = i;
    images[i] = "person-small"; i++;

    PEOPLE16 = i;
    images[i] = "People16"; i++;

    PEOPLE24 = i;
    images[i] = "People24"; i++;

    PEOPLE32 = i;
    images[i] = "People32"; i++;


    USER_FIND16 = i;
    images[i] = "UserFind16"; i++;

    USER_FIND24 = i;
    images[i] = "UserFind24"; i++;

    USER_FIND32 = i;
    images[i] = "UserFind32"; i++;

    USER_NEW16 = i;
    images[i] = "UserNew16"; i++;

    USER_NEW24 = i;
    images[i] = "UserNew24"; i++;

    USER_EDIT16 = i;
    images[i] = "UserEdit16"; i++;

    USER_EDIT24 = i;
    images[i] = "UserEdit24"; i++;

    USER_ACTIVATE16 = i;
    images[i] = "UserActivate16"; i++;

    USER_ACTIVATE24 = i;
    images[i] = "UserActivate24"; i++;

    USER_PASS_RESET16 = i;
    images[i] = "UserPassReset16"; i++;

    USER_PASS_RESET24 = i;
    images[i] = "UserPassReset24"; i++;

    USER_DELETE16 = i;
    images[i] = "UserDelete16"; i++;

    USER_DELETE24 = i;
    images[i] = "UserDelete24"; i++;

    USER_MANAGE16 = i;
    images[i] = "UserManage16"; i++;

    USER_MANAGE24 = i;
    images[i] = "UserManage24"; i++;


    ADDRESS16 = i;
    images[i] = "Address16"; i++;

    ADDRESS32 = i;
    images[i] = "Address32"; i++;

    ADDRESS_ADD16 = i;
    images[i] = "AddressAdd16"; i++;

    ADDRESS_ADD24 = i;
    images[i] = "AddressAdd24"; i++;

    ADDRESS_COPY16 = i;
    images[i] = "AddressCopy16"; i++;

    ADDRESS_COPY24 = i;
    images[i] = "AddressCopy24"; i++;

    ADDRESS_DELETE16 = i;
    images[i] = "AddressDelete16"; i++;

    ADDRESS_DELETE24 = i;
    images[i] = "AddressDelete24"; i++;

    ADDRESS_EDIT16 = i;
    images[i] = "AddressEdit16"; i++;

    ADDRESS_EDIT24 = i;
    images[i] = "AddressEdit24"; i++;

    ADDRESS_MOVE16 = i;
    images[i] = "AddressMove16"; i++;

    ADDRESS_MOVE24 = i;
    images[i] = "AddressMove24"; i++;

    ADDRESS_SAVE16 = i;
    images[i] = "AddressSave16"; i++;

    ADDRESS_SAVE24 = i;
    images[i] = "AddressSave24"; i++;

    CONTACT16 = i;
    images[i] = "Contact16"; i++;

    CONTACT32 = i;
    images[i] = "Contact32"; i++;

    CONTACT_ADD16 = i;
    images[i] = "ContactAdd16"; i++;

    CONTACT_ADD24 = i;
    images[i] = "ContactAdd24"; i++;

    CONTACT_CHECK16 = i;
    images[i] = "ContactCheck16"; i++;

    CONTACT_CHECK24 = i;
    images[i] = "ContactCheck24"; i++;

    CONTACT_DELETE16 = i;
    images[i] = "ContactDelete16"; i++;

    CONTACT_DELETE24 = i;
    images[i] = "ContactDelete24"; i++;

    CONTACT_NEW32 = i;
    images[i] = "ContactNew32"; i++;

    GROUP_ADD16 = i;
    images[i] = "GroupAdd16"; i++;

    GROUP_ADD24 = i;
    images[i] = "GroupAdd24"; i++;

    MEMBER_ADD16 = i;
    images[i] = "MemberAdd16"; i++;

    MEMBER_ADD24 = i;
    images[i] = "MemberAdd24"; i++;

    //POST_FOLDER01_SMALL = i;
    //images[i] = "post_folder01_small"; i++;

    //POST_READ_SMALL = i;
    //images[i] = "post_read_small"; i++;

    //POST_UNREAD_SMALL = i;
    //images[i] = "post_unread_small"; i++;

    POSTING16 = i;
    images[i] = "Posting16"; i++;

    POSTING_CERT32 = i;
    images[i] = "PostingCert32"; i++;

    ARROW_DROP_DOWN_5_3 = i;
    images[i] = "ArrowDropDown_5_3"; i++;

    ORDER_ASCENDING = i;
    images[i] = "order_ascending"; i++;
    ORDER_ASCENDING2 = i;
    images[i] = "order_ascending2"; i++;

    ORDER_DESCENDING = i;
    images[i] = "order_descending"; i++;
    ORDER_DESCENDING2 = i;
    images[i] = "order_descending2"; i++;

    INFO16 = i;
    images[i] = "Info16"; i++;

    ANIM_GLOBE_FIRST16 = i;
    images[i] = "AnimGlobeFirst16"; i++;
    ANIM_GLOBE16 = i;
    images[i] = "AnimGlobe16"; i++;
    ANIM_KEY = i;
    images[i] = "anim_key"; i++;
    ANIM_LOCK = i;
    images[i] = "anim_lock"; i++;
//    ANIM_PHONE = i;
//    images[i] = "anim_phone"; i++;
    ANIM_TRANSFER = i;
    images[i] = "anim_transfer"; i++;
    ANIM_TRANSFER_STOP = i;
    images[i] = "anim_transfer_stop"; i++;
    LOCK_OPENED = i;
    images[i] = "lock_opened"; i++;

    PERSON32 = i;
    images[i] = "Person32"; i++;
    HOME32 = i;
    images[i] = "Home32"; i++;
    PHONE32 = i;
    images[i] = "Phone32"; i++;
    MAIL32 = i;
    images[i] = "Mail32"; i++;
    WEB32 = i;
    images[i] = "Web32"; i++;

    //DOT_GRAY16 = i;
    //images[i] = "dot_gray16"; i++;
    //DOT_RED16 = i;
    //images[i] = "dot_red16"; i++;
    //DOT_GREEN16 = i;
    //images[i] = "dot_green16"; i++;
    //DOT_GREEN_RED16 = i;
    //images[i] = "dot_green_red16"; i++;
    //DOT_BLUE12 = i;
    //images[i] = "DotBlue12"; i++;
    //DOT_GREEN12 = i;
    //images[i] = "DotGreen12"; i++;
    //DOT_RED12 = i;
    //images[i] = "DotRed12"; i++;
//    CHECK_BLUE16 = i;
//    images[i] = "CheckBlue16"; i++;
//    //CHECK_GREEN16 = i;
//    //images[i] = "CheckGreen16"; i++;
//    PERSON_AVAILABLE16 = i;
//    images[i] = "PersonAvailable16"; i++;
//    PERSON_DND16 = i;
//    images[i] = "PersonDND16"; i++;
//    PERSON_NOT_AVAILABLE16 = i;
//    images[i] = "PersonNotAvailable16"; i++;
    STATUS_AWAY16 = i;
    images[i] = "StatusAway16"; i++;
    STATUS_DND16 = i;
    images[i] = "StatusDND16"; i++;
    STATUS_INVISIBLE16 = i;
    images[i] = "StatusOffline16"; i++;
    STATUS_NA16 = i;
    images[i] = "StatusNA16"; i++;
    STATUS_OFFLINE16 = i;
    images[i] = "StatusOffline16"; i++;
    STATUS_ONLINE16 = i;
    images[i] = "StatusOnline16"; i++;
    STATUS_QUESTION16 = i;
    images[i] = "StatusQuestion16"; i++;
    STATUS_WAITING16 = i;
    images[i] = "StatusWaiting16"; i++;
    LIGHT_ON_SMALL = i;
    images[i] = "light-on-small"; i++;
    LIGHT_ON_12 = i;
    images[i] = "light-on-12"; i++;
    LIGHT_OFF_SMALL = i;
    images[i] = "light-off-small"; i++;
    LIGHT_GREEN_SMALL = i;
    images[i] = "light-green-small"; i++;
    LIGHT_X_SMALL = i;
    images[i] = "light-x-small"; i++;

    FILE32 = i; // little modification of ADD24
    images[i] = "File32"; i++;

    FILE_LOCKED32 = i; // little modification of ADD24
    images[i] = "FileLocked32"; i++;

    FILE_MOVE16 = i;
    images[i] = "FileMove16"; i++;

    FILE_MOVE24 = i;
    images[i] = "FileMove24"; i++;

    FILE_REMOVE16 = i;
    images[i] = "FileRemove16"; i++;

    FILE_REMOVE24 = i;
    images[i] = "FileRemove24"; i++;

    FILE_REMOVE48 = i;
    images[i] = "FileRemove48"; i++;

    FILE_REPLACE32 = i;
    images[i] = "FileReplace32"; i++;

    IMPORT_FILE16 = i;
    images[i] = "ImportFile16"; i++;

    IMPORT_FILE24 = i;
    images[i] = "ImportFile24"; i++;

    IMPORT_FOLDER16 = i;
    images[i] = "ImportFolder16"; i++;

    IMPORT_FOLDER24 = i;
    images[i] = "ImportFolder24"; i++;


    // straight from SUN without ANY modifications
    //ADD16 = i;
    //images[i] = "Add16"; i++;

    //ADD24 = i;
    //images[i] = "Add24"; i++;

    SELECT_ALL16 = i;
    images[i] = "SelectAll16"; i++;

    COPY16 = i;
    images[i] = "Copy16"; i++;

    COPY24 = i;
    images[i] = "Copy24"; i++;

    CUT16 = i;
    images[i] = "Cut16"; i++;

    CUT24 = i;
    images[i] = "Cut24"; i++;

    EXPORT16 = i;
    images[i] = "Export16"; i++;

    EXPORT24 = i;
    images[i] = "Export24"; i++;

    //FILE16 = i; // little modification of ADD16
    //images[i] = "File16"; i++;

    //FILE24 = i; // little modification of ADD24
    //images[i] = "File24"; i++;

    FIND16 = i;
    images[i] = "Find16"; i++;

    FIND24 = i;
    images[i] = "Find24"; i++;

    //IMPORT16 = i;
    //images[i] = "Import16"; i++;

    //IMPORT24 = i;
    //images[i] = "Import24"; i++;

    //NEW16 = i;
    //images[i] = "New16"; i++;

    //NEW24 = i;
    //images[i] = "New24"; i++;

    PASTE16 = i;
    images[i] = "Paste16"; i++;

    PASTE24 = i;
    images[i] = "Paste24"; i++;

    SPELL16 = i;
    images[i] = "SpellCheck16"; i++;

    SPELL24 = i;
    images[i] = "SpellCheck24"; i++;

    UNDO16 = i;
    images[i] = "Undo16"; i++;

    UNDO24 = i;
    images[i] = "Undo24"; i++;

    REDO16 = i;
    images[i] = "Redo16"; i++;

    REDO24 = i;
    images[i] = "Redo24"; i++;

    REFRESH16 = i;
    images[i] = "Refresh16"; i++;

    REFRESH24 = i;
    images[i] = "Refresh24"; i++;

    //SEARCH16 = i;
    //images[i] = "Search16"; i++;

    //SEARCH24 = i;
    //images[i] = "Search24"; i++;

    AUTO_RESPONDER32 = i;
    images[i] = "auto-responder-32"; i++;

    SIGNATURE32 = i;
    images[i] = "Signature32"; i++;

    SHIELD32 = i;
    images[i] = "Shield32"; i++;

    HTML_IMAGE_DELAYED = i;
    images[i] = "image-delayed"; i++;

    HTML_IMAGE_FAILED = i;
    images[i] = "image-failed"; i++;

    RESIZE_DRAG_SE = i;
    images[i] = "resize-drag-se"; i++;


    RING_BELL = i;
    images[i] = "Bell16"; i++;

    VOLUME16 = i;
    images[i] = "Volume16"; i++;

    VOLUME24 = i;
    images[i] = "Volume24"; i++;

    RECORD16 = i;
    images[i] = "Record16"; i++;

    RECORD24 = i;
    images[i] = "Record24"; i++;

    PLAY16 = i;
    images[i] = "Play16"; i++;

    PAUSE16 = i;
    images[i] = "Pause16"; i++;

    PLAY_PAUSE16 = i;
    images[i] = "PlayPause16"; i++;

    STOP16 = i;
    images[i] = "Stop16"; i++;

    EM_ARROW_LEFT     = i;    images[i] = "emotions/arrow-left";     i++;
    EM_ARROW_RIGHT    = i;    images[i] = "emotions/arrow-right";    i++;
    EM_BALL           = i;    images[i] = "emotions/ball";           i++;
    EM_BAT            = i;    images[i] = "emotions/bat";            i++;
    EM_BEER           = i;    images[i] = "emotions/beer";           i++;
    EM_BUDDY          = i;    images[i] = "emotions/buddy";          i++;
    EM_CAMERA         = i;    images[i] = "emotions/camera";         i++;
    EM_CAR            = i;    images[i] = "emotions/car";            i++;
    EM_COCTAIL        = i;    images[i] = "emotions/coctail";        i++;
    EM_COFFEE         = i;    images[i] = "emotions/coffee";         i++;
    EM_FACE_ANGRY     = i;    images[i] = "emotions/face-angry";     i++;
    EM_FACE_TEETH     = i;    images[i] = "emotions/face-teeth";     i++;
    EM_FACE_CONFUSED  = i;    images[i] = "emotions/face-confused";  i++;
    EM_FACE_CRY       = i;    images[i] = "emotions/face-cry";       i++;
    EM_FACE_DEVIL     = i;    images[i] = "emotions/face-devil";     i++;
    EM_FACE_DONT_TELL = i;    images[i] = "emotions/face-dont-tell"; i++;
    EM_FACE_NERD      = i;    images[i] = "emotions/face-nerd";      i++;
    EM_FACE_RED       = i;    images[i] = "emotions/face-red";       i++;
    EM_FACE_SAD       = i;    images[i] = "emotions/face-sad";       i++;
    EM_FACE_SARCASTIC = i;    images[i] = "emotions/face-sarcastic"; i++;
    EM_FACE_SECRET    = i;    images[i] = "emotions/face-secret";    i++;
    EM_FACE_SHADES    = i;    images[i] = "emotions/face-shades";    i++;
    EM_FACE_SICK      = i;    images[i] = "emotions/face-sick";      i++;
    EM_FACE_SMILE1    = i;    images[i] = "emotions/face-smile1";    i++;
    EM_FACE_SMILE2    = i;    images[i] = "emotions/face-smile2";    i++;
    EM_FACE_SMILE3    = i;    images[i] = "emotions/face-smile3";    i++;
    EM_FACE_STRAIGHT  = i;    images[i] = "emotions/face-straight";  i++;
    EM_FACE_SURPRISED = i;    images[i] = "emotions/face-surprised"; i++;
    EM_FACE_THINKING  = i;    images[i] = "emotions/face-thinking";  i++;
    EM_FACE_WEE       = i;    images[i] = "emotions/face-wee";       i++;
    EM_FLAG_GREEN     = i;    images[i] = "emotions/flag-green";     i++;
    EM_FLAG_RED       = i;    images[i] = "emotions/flag-red";       i++;
    EM_GIFT           = i;    images[i] = "emotions/gift";           i++;
    EM_HEAD_DUMB      = i;    images[i] = "emotions/head-dumb";      i++;
    EM_HEAD_TALK      = i;    images[i] = "emotions/head-talk";      i++;
    EM_HEART          = i;    images[i] = "emotions/heart";          i++;
    EM_HEARTBREAK     = i;    images[i] = "emotions/heartbreak";     i++;
    EM_KISS           = i;    images[i] = "emotions/kiss";           i++;
    EM_LETTER         = i;    images[i] = "emotions/letter";         i++;
    EM_MAIL           = i;    images[i] = "emotions/mail";           i++;
    EM_MEN            = i;    images[i] = "emotions/men";            i++;
    EM_PALMS          = i;    images[i] = "emotions/palms";          i++;
    EM_PARTY          = i;    images[i] = "emotions/party";          i++;
    EM_PHONE          = i;    images[i] = "emotions/phone";          i++;
    EM_PIZZA          = i;    images[i] = "emotions/pizza";          i++;
    EM_PLAIN          = i;    images[i] = "emotions/plain";          i++;
    EM_ROSE           = i;    images[i] = "emotions/rose";           i++;
    EM_THUMB_DOWN     = i;    images[i] = "emotions/thumb-down";     i++;
    EM_THUMB_UP       = i;    images[i] = "emotions/thumb-up";       i++;
    EM_WOMAN          = i;    images[i] = "emotions/woman";          i++;

    NUMBER_OF_IMAGES = i;

    for (int k=0; k<NUMBER_OF_IMAGES; k++) {
      unUsedIconNames.add(images[k]);
      imageCodesHM1.put(images[k], Integer.valueOf(k));
      imageCodesHM2.put(images[k]+".png", Integer.valueOf(k));
      imageCodesHM3.put("images/"+images[k]+".png", Integer.valueOf(k));
      imageCodesHM4.put(images[k]+".gif", Integer.valueOf(k));
      imageCodesHM5.put("images/"+images[k]+".gif", Integer.valueOf(k));
    }

    emotions = new int[]
      {
        EM_FACE_SMILE1, EM_FACE_SAD, EM_FACE_STRAIGHT, EM_FACE_CONFUSED, EM_FACE_SMILE2, EM_FACE_SMILE3, EM_FACE_SURPRISED, EM_FACE_WEE, EM_FACE_THINKING, EM_FACE_SARCASTIC,
        EM_FACE_SHADES, EM_FACE_NERD, EM_FACE_DONT_TELL, EM_FACE_CRY, EM_FACE_RED, EM_FACE_SICK, EM_FACE_SECRET, EM_FACE_TEETH, EM_FACE_ANGRY, EM_FACE_DEVIL,
        EM_ROSE, EM_KISS, EM_HEART, EM_HEARTBREAK, EM_COCTAIL, EM_BEER, EM_COFFEE, EM_GIFT, EM_PARTY, EM_PIZZA,
        EM_CAR, EM_PLAIN, EM_BALL, EM_PALMS, EM_CAMERA, EM_PHONE, EM_BUDDY, EM_WOMAN, EM_MEN, EM_BAT,
        EM_THUMB_UP, EM_THUMB_DOWN, EM_ARROW_RIGHT, EM_ARROW_LEFT, EM_MAIL, EM_LETTER, EM_HEAD_TALK, EM_HEAD_DUMB, EM_FLAG_RED, EM_FLAG_GREEN
      };
  }

  public static Integer getImageCode(String imageName) {
    Integer imageCode = null;
    if (imageCode == null)
      imageCode = (Integer) ImageNums.imageCodesHM1.get(imageName);
    if (imageCode == null)
      imageCode = (Integer) ImageNums.imageCodesHM2.get(imageName);
    if (imageCode == null)
      imageCode = (Integer) ImageNums.imageCodesHM3.get(imageName);
    if (imageCode == null)
      imageCode = (Integer) ImageNums.imageCodesHM4.get(imageName);
    if (imageCode == null)
      imageCode = (Integer) ImageNums.imageCodesHM5.get(imageName);
    return imageCode;
  }

  public static String getImageName(int imageCode) {
    return images[imageCode];
  }
  public static ArrayList getImageNames() {
    return new ArrayList(Arrays.asList(images));
  }
  public static int[] getEmoticonCodes() {
    return emotions;
  }

  public static void setImageName(int imageCode, String name) {
    images[imageCode] = name;
  }

  public static ArrayList getUnusedImageNames() {
    synchronized (unUsedIconNames) {
      return new ArrayList(unUsedIconNames);
    }
  }

  public static void setUsedIcon(int imageCode) {
    synchronized (unUsedIconNames) {
      String imageName = images[imageCode];
      if (unUsedIconNames.remove(imageName))
        usedIconNames.add(imageName);
    }
  }

}