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

package com.CH_co.util;

import java.net.URL;
import java.util.*;

import com.CH_co.nanoxml.*;

/**
 * <b>Copyright</b> &copy; 2001-2011
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
public class URLs extends Object {

  private static PrivateLabelI privateLabel;

  // Private Labels usually have custom settings stored in XML files on a web server
  private static String PRIVATE_LABEL_SETTINGS_URL = null;

  // optional in auto-update to redirect main jar distribution
  public static String FILENAME__MAIN_JAR = "CryptoHeaven.jar";

  public static int SERVICE_COMMUNITY_NAME;
  public static int SERVICE_NETWORK_NAME;
  public static int SERVICE_PROVIDER_NAME;
  public static int SERVICE_SOFTWARE_NAME;

  public static int SUPPORT_EMAIL;
  public static int SUPPORT_BODY;

  public static int DOMAIN;
  public static int DOMAIN_MAIL;
  public static int DOMAIN_WEB;
  public static int HOME_PAGE;

  public static int DEFAULT_SERVER__PROHIBIT_DATA_CONNECTIONS_1;
  public static int DEFAULT_SERVER_1;
  public static int DEFAULT_SERVER_2;
  public static int DEFAULT_SERVER_3;

  public static int ACTIVATION_CODE_DEFAULT;
  public static int ACTIVATION_CODE_FIELD_REMOVED;
  /**
   * @deprecated Server field is always initially hidden with option to show it
   */
  public static int SERVER_FIELD_REMOVED;
  public static int NEW_ACCOUNT_BUTTON;

  public static int ACTIVATION_CODE_PAGE;
  public static int CONNECTIVITY_PAGE;
  public static int DOWNLOAD_PAGE;
  public static int HELP_FAQ_PAGE;
  public static int HELP_QUICK_TOUR_PAGE;
  public static int HELP_USER_GUIDE_PAGE;
  public static int REPLY_PAGE;
  public static int SIGNUP_PAGE;
  public static int TELL_A_FRIEND_PAGE;

  public static int WELCOME_EMAIL_FROM;
  public static int WELCOME_EMAIL_SUBJECT;
  public static int WELCOME_EMAIL_BODY;

  public static int WELCOME_TEMPLATE;

  private static String[] customizationKeys;
  private static String[] customizationStrings;

  public static HashMap replacementTemplatesHM;


  static {
    Object privLabel = null;
    try {
      privLabel = Class.forName("com.CH_co.privateLabel.PrivateLabel").newInstance();
    } catch (Throwable t) { }
    if (privLabel != null && privLabel instanceof PrivateLabelI) {
      privateLabel = (PrivateLabelI) privLabel;
      try { PRIVATE_LABEL_SETTINGS_URL = privateLabel.getPrivateLabelURL(); } catch (Throwable t) { }
    }
    loadDefaults();
  }

  public static String getPrivateLabelSettingsURL() {
    return PRIVATE_LABEL_SETTINGS_URL;
  }

  public static boolean hasPrivateLabelCustomizationClass() {
    return privateLabel != null;
  }

  private static boolean hasPrivateLabelSettingsURL() {
    return PRIVATE_LABEL_SETTINGS_URL != null;
  }

  public static boolean hasPrivateLabelCustomization() {
    return hasPrivateLabelCustomizationClass() || hasPrivateLabelSettingsURL();
  }

  public static String get(int stringIndex, String nullValue) {
    String rc = customizationStrings[stringIndex];
    if (rc == null)
      rc = nullValue;
    return rc;
  }
  public static String get(int stringIndex) {
    return customizationStrings[stringIndex];
  }
  public static String[] getElements(int stringIndex) {
    String str = get(stringIndex);
    return getElements(str);
  }
  public static String[] getElements(String str) {
    String[] elements = null;
    if (str != null && str.length() > 0) {
      StringTokenizer st = new StringTokenizer(str, ",");
      elements = new String[st.countTokens()];
      for (int i=0; i<elements.length; i++) {
        elements[i] = st.nextToken();
      }
    } else if (str != null) {
      elements = new String[] { str };
    }
    return elements;
  }

  public static URL getResourceURL(String fileName) {
    URL location = new Object().getClass().getResource("/"+fileName);
    if (location == null) {
      location = URLs.class.getResource("/"+fileName);
    }
    return location;
  }

  private static void loadDefaults() {
    int i = 0;

    customizationKeys = new String[31];
    customizationStrings = new String[31];

    SERVICE_COMMUNITY_NAME = i;
    customizationKeys[i] = "SERVICE_COMMUNITY_NAME";
    customizationStrings[i] = "CryptoHeaven Community"; i++;

    SERVICE_NETWORK_NAME = i;
    customizationKeys[i] = "SERVICE_NETWORK_NAME";
    customizationStrings[i] = "CryptoHeaven Network"; i++;

    SERVICE_PROVIDER_NAME = i;
    customizationKeys[i] = "SERVICE_PROVIDER_NAME";
    customizationStrings[i] = "CryptoHeaven Development Team"; i++;

    SERVICE_SOFTWARE_NAME = i;
    customizationKeys[i] = "SERVICE_SOFTWARE_NAME";
    customizationStrings[i] = "CryptoHeaven"; i++;

    SUPPORT_EMAIL = i;
    customizationKeys[i] = "SUPPORT_EMAIL";
    customizationStrings[i] = "Support <support@cryptoheaven.com>"; i++;

    SUPPORT_BODY = i;
    customizationKeys[i] = "SUPPORT_BODY";
    customizationStrings[i] = "Technical Support Form\n\n"+
              "Please be as specific as possible when reporting any problems.\n\n"+
              "If the problem is reproducible, please list the steps required to reproduce it.\n\n"+
              "If the problem is not reproducible (only happened once, or occasionally for no apparent reason), please describe the circumstances in which it occurred and the symptoms observed: (note: it is much harder for us to fix non-reproducible bugs).\n\n"+
              "If the problem causes any error messages to appear, please copy the exact text displayed and paste it here.\n\n";
    i++;

    DOMAIN = i;
    customizationKeys[i] = "DOMAIN";
    customizationStrings[i] = "CryptoHeaven.com"; i++;

    DOMAIN_WEB = i;
    customizationKeys[i] = "DOMAIN_WEB";
    customizationStrings[i] = "www." + get(DOMAIN); i++;

    DOMAIN_MAIL = i;
    customizationKeys[i] = "DOMAIN_MAIL";
    customizationStrings[i] = get(DOMAIN)+",aprava.com,highvip.com,emailhosting.tv,MDemail.net,SecureMedical.net"; i++;

    HOME_PAGE = i;
    customizationKeys[i] = "HOME_PAGE";
    customizationStrings[i] = "http://" + get(DOMAIN_WEB); i++;


    DEFAULT_SERVER__PROHIBIT_DATA_CONNECTIONS_1 = i;
    customizationKeys[i] = "DEFAULT_SERVER__PROHIBIT_DATA_CONNECTIONS_1";
    customizationStrings[i] = get(DOMAIN); i++;

    DEFAULT_SERVER_1 = i;
    customizationKeys[i] = "DEFAULT_SERVER_1";
    customizationStrings[i] = get(DOMAIN); i++;

    DEFAULT_SERVER_2 = i;
    customizationKeys[i] = "DEFAULT_SERVER_2";
    customizationStrings[i] = "d1." + get(DOMAIN) + ":4383"; i++;

    DEFAULT_SERVER_3 = i;
    customizationKeys[i] = "DEFAULT_SERVER_3";
    customizationStrings[i] = "http://d3." + get(DOMAIN); i++;

    ACTIVATION_CODE_DEFAULT = i;
    customizationKeys[i] = "ACTIVATION_CODE_DEFAULT";
    customizationStrings[i] = ""; i++;

    ACTIVATION_CODE_FIELD_REMOVED = i;
    customizationKeys[i] = "ACTIVATION_CODE_FIELD_REMOVED";
    customizationStrings[i] = "false"; i++;

    SERVER_FIELD_REMOVED = i;
    customizationKeys[i] = "SERVER_FIELD_REMOVED";
    customizationStrings[i] = "true"; i++;

    NEW_ACCOUNT_BUTTON = i;
    customizationKeys[i] = "NEW_ACCOUNT_BUTTON"; // valid values are: enable, disable, remove
    customizationStrings[i] = "enable"; i++;

    ACTIVATION_CODE_PAGE = i;
    customizationKeys[i] = "ACTIVATION_CODE_PAGE";
    customizationStrings[i] = get(HOME_PAGE) + "/activation-code.htm"; i++;

    CONNECTIVITY_PAGE = i;
    customizationKeys[i] = "CONNECTIVITY_PAGE";
    customizationStrings[i] = get(HOME_PAGE) + "/Connectivity"; i++;

    DOWNLOAD_PAGE = i;
    customizationKeys[i] = "DOWNLOAD_PAGE";
    customizationStrings[i] = get(HOME_PAGE) + "/Download/Download.htm"; i++;

    HELP_FAQ_PAGE = i;
    customizationKeys[i] = "HELP_FAQ_PAGE";
    customizationStrings[i] = get(HOME_PAGE) + "/Support/FAQ.htm"; i++;

    HELP_QUICK_TOUR_PAGE = i;
    customizationKeys[i] = "HELP_QUICK_TOUR_PAGE";
    customizationStrings[i] = get(HOME_PAGE) + "/QuickTour/QuickTour.htm"; i++;

    HELP_USER_GUIDE_PAGE = i;
    customizationKeys[i] = "HELP_USER_GUIDE_PAGE";
    customizationStrings[i] = get(HOME_PAGE) + "/UserGuide/UserGuide.htm"; i++;

    REPLY_PAGE = i;
    customizationKeys[i] = "REPLY_PAGE";
    customizationStrings[i] = get(HOME_PAGE) + "/ch/web-mail.jsp?uId="; i++;

    SIGNUP_PAGE = i;
    customizationKeys[i] = "SIGNUP_PAGE";
    customizationStrings[i] = get(HOME_PAGE) + "/ch/signup.jsp"; i++;

    TELL_A_FRIEND_PAGE = i;
    customizationKeys[i] = "TELL_A_FRIEND_PAGE";
    customizationStrings[i] = get(HOME_PAGE) + "/tell-a-friend.htm"; i++;

    WELCOME_EMAIL_FROM = i;
    customizationKeys[i] = "WELCOME_EMAIL_FROM";
    customizationStrings[i] = null; i++;

    WELCOME_EMAIL_SUBJECT = i;
    customizationKeys[i] = "WELCOME_EMAIL_SUBJECT";
    customizationStrings[i] = "Welcome to " + customizationStrings[SERVICE_SOFTWARE_NAME]; i++;

    WELCOME_EMAIL_BODY = i;
    customizationKeys[i] = "WELCOME_EMAIL_BODY";
    String url = get(HOME_PAGE) + "/ch/welcome-email.jsp";
    customizationStrings[i] =
            "<html>\n"
            +"<body marginwidth=\"10\">\n"
            +"<div ALIGN=\"RIGHT\"><font size=\"-1\" face=\"Verdana, Arial, Helvetica, sans-serif\"><img src=\"/templates/images/Logo-color.png\" width=\"300\" height=\"38\" ALIGN=\"RIGHT\"></font></div>\n"
            +"<h3><font face=\"Verdana, Arial, Helvetica, sans-serif\">Welcome to CryptoHeaven!</font></h3>\n"
            +"<p><font size=\"-1\" face=\"Verdana, Arial, Helvetica, sans-serif\">Thank you for joining CryptoHeaven. You're just about to experience how easy secure email and secure online file management can be. Take a <a href=\"http://www.cryptoheaven.com/QuickTour/QuickTour.htm\">quick tour</a> and get started faster. So far more than 60 million emails and 80 million files have been securely stored on CryptoHeaven by private and professional users around the globe.</font></p>\n"
            +"<p><font size=\"-1\" face=\"Verdana, Arial, Helvetica, sans-serif\">With your CryptoHeaven account you can immediately:</font></p>\n"
            +"<ul>\n"
            +"  <li><font size=\"-1\" face=\"Verdana, Arial, Helvetica, sans-serif\">send private and highly encrypted email, \n"
            +"  </font>\n"
            +"  <li><font size=\"-1\" face=\"Verdana, Arial, Helvetica, sans-serif\">send, store, and share confidential documents, \n"
            +"  </font>\n"
            +"  <li><font size=\"-1\" face=\"Verdana, Arial, Helvetica, sans-serif\">engage in private and confidential chat, individually or in a group.\n"
            +"</font>\n"
            +"</ul>\n"
            +"<p><font size=\"-1\" face=\"Verdana, Arial, Helvetica, sans-serif\">Congratulations for taking charge of your email privacy and security, and trusting our technology to deliver on both promises. We are deeply honoured and truly appreciate it!</font></p>\n"
            +"<br>\n"
            +"<h3><font face=\"Verdana, Arial, Helvetica, sans-serif\">Privacy</font></h3>\n"
            +"<p><font size=\"-1\" face=\"Verdana, Arial, Helvetica, sans-serif\">\n"
            +"We take privacy very seriously. Your password never leaves your computer, meaning no one, not even we can access your data. If you lose your password, you will not be able to access your files anymore. Therefore, do not forget it, or write it down and keep it stored in a safe place.</font> \n"
            +"</p>\n"
            +"<br>\n"
            +"<h3><font face=\"Verdana, Arial, Helvetica, sans-serif\">Feedback</font></h3>\n"
            +"<p><font size=\"-1\" face=\"Verdana, Arial, Helvetica, sans-serif\">\n"
            +"Your satisfaction is very important to us. If you have any questions regarding our products and services we will be glad to answers them promptly. <a href=\"http://www.cryptoheaven.com/Support/Feedback.htm\">Feedback form.</a>\n"
            +"</font></p>\n"
            +"<br>\n"
            +"<h3><font face=\"Verdana, Arial, Helvetica, sans-serif\">We're Here To Help</font></h3>\n"
            +"<p><font face=\"Verdana, Arial, Helvetica, sans-serif\" size=\"-1\">Need help? The <a href=\"http://www.cryptoheaven.com/QuickTour/QuickTour.htm\">Quick Start Guide</a>, and \n"
            +"  <a href=\"http://www.cryptoheaven.com/UserGuide/UserGuide-Index.htm\">User's Manual</a> cover a wide range of common problems.  Feel free to <a href=\"mailto:13@cryptoheaven.com\">email</a> our support team with any question you may have.\n"
            +"</font></p>\n"
            +"<br>\n"
            +"<h3><font face=\"Verdana, Arial, Helvetica, sans-serif\">CryptoHeaven for your Business</font></h3>\n"
            +"<p><font size=\"-1\" face=\"Verdana, Arial, Helvetica, sans-serif\">CryptoHeaven is particularly well suited for businesses sending sensitive documents between corporate offices or between business partners. All your messages and documents can be accessed securely from any computer connected to the Internet like your home and office PC. Consider using CryptoHeaven for sending all your important documents and save a lot of time and money.</font></p>\n"
            +"<p><font size=\"-1\" face=\"Verdana, Arial, Helvetica, sans-serif\">Use CryptoHeaven technology to replace overnight deliveries or registered mail. There is no longer an excuse for late and expensive deliveries.  It only takes a minute to send your document, and it can be received right away - even around the globe.</font></p>\n"
            +"<p><font size=\"-1\" face=\"Verdana, Arial, Helvetica, sans-serif\">It is absurd to go through the expense and time cost of copying inherently digital material to a physical media and delivering it through a courier or mail. CryptoHeaven knows that, and so do you. CryptoHeaven technology offers you peace of mind, consider the following:</font></p>\n"
            +"<ul>\n"
            +"  <li><font size=\"-1\" face=\"Verdana, Arial, Helvetica, sans-serif\">Automatic end-to-end 256-bit encryption.\n"
            +"  </font>\n"
            +"  <li><font size=\"-1\" face=\"Verdana, Arial, Helvetica, sans-serif\">Secure transmission of files of any size or type to a single user or group of users.\n"
            +"  </font>\n"
            +"  <li><font size=\"-1\" face=\"Verdana, Arial, Helvetica, sans-serif\">Secure e-mail with unlimited size attachments.\n"
            +"  </font>\n"
            +"  <li><font size=\"-1\" face=\"Verdana, Arial, Helvetica, sans-serif\">Automatic recipient notification.\n"
            +"</font>\n"
            +"</ul>\n"
            +"<p><font size=\"-1\" face=\"Verdana, Arial, Helvetica, sans-serif\">CryptoHeaven is not just a very secure e-mail for sensitive message and document delivery.  Use CryptoHeaven for all your important communications.  All CryptoHeaven clients have access to the following services from an easy to use interface:</font></p>\n"
            +"<ul>\n"
            +"  <li><font size=\"-1\" face=\"Verdana, Arial, Helvetica, sans-serif\">Secure file sharing.\n"
            +"  </font>\n"
            +"  <li><font size=\"-1\" face=\"Verdana, Arial, Helvetica, sans-serif\">Secure online storage.\n"
            +"  </font>\n"
            +"  <li><font size=\"-1\" face=\"Verdana, Arial, Helvetica, sans-serif\">Secure instant messaging.\n"
            +"</font>\n"
            +"</ul>\n"
            +"<p><font size=\"-1\" face=\"Verdana, Arial, Helvetica, sans-serif\">Everyone knows the benefits of e-mail for sending casual messages. CryptoHeaven allows you to extend that convenience to sending sensitive business correspondence knowing it will not be intercepted - ever!</font></p>\n"
            +"<p><font size=\"-1\" face=\"Verdana, Arial, Helvetica, sans-serif\">Now you have the power to send any electronic document via CryptoHeaven to anyone, no matter what the content. You no longer have to worry about your messages ending up in your competitor's, news reporter's, government's or anyone else's hands.</font></p>\n"
            +"</font>\n"
            +"<p>&nbsp;</p>\n"
            +"<p><font size=\"-1\" face=\"Verdana, Arial, Helvetica, sans-serif\">Thank you for choosing CryptoHeaven,<br>\n"
            +"<br>\n"
            +"	Team CryptoHeaven<br>\n"
            +"	<a href=\"http://www.cryptoheaven.com\">http://www.cryptoheaven.com</a>\n"
            +"</p>\n"
            +"<p>&nbsp;</p>\n"
            +"<p>&nbsp;</p>\n"
            +"</body>\n"
            +"</html>";
    i++;
    // Java has problems with Print function for framesets, also reply/forward 
    // message quote doesn't always work with frameset.
    //customizationStrings[i] = "<html><FRAMESET cols=\"*\"><FRAME src=\""+url+"\" name=\"welcomeEmailFrame\"></FRAMESET></html>"; i++;

    WELCOME_TEMPLATE = i;
    customizationKeys[i] = "WELCOME_TEMPLATE";
    customizationStrings[i] = get(HOME_PAGE) + "/ch/LoginPage.jsp"; i++;
  }

  private static void loadPrivateLabelFromClassDefaults() {
    if (privateLabel != null) {
      try {
        if (privateLabel.getJarName() != null)
          FILENAME__MAIN_JAR = privateLabel.getJarName();
      } catch (Throwable t) { }
      try {
        if (privateLabel.getImage_LogoBanner() != null)
          ImageNums.setImageName(ImageNums.LOGO_BANNER_MAIN, privateLabel.getImage_LogoBanner());
      } catch (Throwable t) { }
      try {
        if (privateLabel.getImage_LogoKeyMain() != null)
          ImageNums.setImageName(ImageNums.LOGO_KEY_MAIN, privateLabel.getImage_LogoKeyMain());
      } catch (Throwable t) { }
      try {
        if (privateLabel.getImage_WindowPopup() != null)
          ImageNums.setImageName(ImageNums.WINDOW_POPUP, privateLabel.getImage_WindowPopup());
      } catch (Throwable t) { }
      try {
        if (privateLabel.getImage_FrameIcon() != null)
          ImageNums.setImageName(ImageNums.FRAME_LOCK32, privateLabel.getImage_FrameIcon());
      } catch (Throwable t) { }
      try {
        if (privateLabel.getActivationCodeDefault() != null)
          customizationStrings[ACTIVATION_CODE_DEFAULT] = privateLabel.getActivationCodeDefault();
      } catch (Throwable t) { }
      try {
        if (privateLabel.getActivationCodeFieldRemoved() != null)
          customizationStrings[ACTIVATION_CODE_FIELD_REMOVED] = privateLabel.getActivationCodeFieldRemoved();
      } catch (Throwable t) { }
      try {
        if (privateLabel.getServerFieldRemoved() != null)
          customizationStrings[SERVER_FIELD_REMOVED] = privateLabel.getServerFieldRemoved();
      } catch (Throwable t) { }
      try {
        if (privateLabel.getNewAccountButton() != null)
          customizationStrings[NEW_ACCOUNT_BUTTON] = privateLabel.getNewAccountButton();
      } catch (Throwable t) { }
      try {
        if (privateLabel.getServiceSoftwareName() != null)
          customizationStrings[SERVICE_SOFTWARE_NAME] = privateLabel.getServiceSoftwareName();
      } catch (Throwable t) { }
      try {
        if (privateLabel.getDomain() != null)
          customizationStrings[DOMAIN] = privateLabel.getDomain();
      } catch (Throwable t) { }
      try {
        if (privateLabel.getDomainWeb() != null)
          customizationStrings[DOMAIN_WEB] = privateLabel.getDomainWeb();
      } catch (Throwable t) { }
      try {
        if (privateLabel.getDomainMail() != null)
          customizationStrings[DOMAIN_MAIL] = privateLabel.getDomainMail();
      } catch (Throwable t) { }
      try {
        if (privateLabel.getHomePage() != null)
          customizationStrings[HOME_PAGE] = privateLabel.getHomePage();
      } catch (Throwable t) { }
      try {
        if (privateLabel.getServiceCommunityName() != null)
          customizationStrings[SERVICE_COMMUNITY_NAME] = privateLabel.getServiceCommunityName();
      } catch (Throwable t) { }
      try {
        if (privateLabel.getServiceNetworkName() != null)
          customizationStrings[SERVICE_NETWORK_NAME] = privateLabel.getServiceNetworkName();
      } catch (Throwable t) { }
      try {
        if (privateLabel.getServiceProviderName() != null)
          customizationStrings[SERVICE_PROVIDER_NAME] = privateLabel.getServiceProviderName();
      } catch (Throwable t) { }
      try {
        if (privateLabel.getSupportEmail() != null)
          customizationStrings[SUPPORT_EMAIL] = privateLabel.getSupportEmail();
      } catch (Throwable t) { }
      try {
        if (privateLabel.getSupportEmailBody() != null)
          customizationStrings[SUPPORT_BODY] = privateLabel.getSupportEmailBody();
      } catch (Throwable t) { }
      try {
        if (privateLabel.getWelcomeEmailFrom() != null)
          customizationStrings[WELCOME_EMAIL_FROM] = privateLabel.getWelcomeEmailFrom();
      } catch (Throwable t) { }
      try {
        if (privateLabel.getWelcomeEmailSubject() != null)
          customizationStrings[WELCOME_EMAIL_SUBJECT] = privateLabel.getWelcomeEmailSubject();
      } catch (Throwable t) { }
      try {
        if (privateLabel.getWelcomeEmailBody() != null)
          customizationStrings[WELCOME_EMAIL_BODY] = privateLabel.getWelcomeEmailBody();
      } catch (Throwable t) { }
      try {
        if (privateLabel.getWelcomeTemplate() != null)
          customizationStrings[WELCOME_TEMPLATE] = privateLabel.getWelcomeTemplate();
      } catch (Throwable t) { }
    }
  }

  public static void loadPrivateLabel(String url) {
    if (url == null || url.trim().length() == 0) {
      loadDefaults();
      // if no exception then set the new private label URL as default
      PRIVATE_LABEL_SETTINGS_URL = null;
    }
    // load static customization
    loadPrivateLabelFromClassDefaults();
    // load dynamic customization
    if (url != null && url.trim().length() > 0) {
      try {
        URL u = new URL(url);
        java.io.InputStream inStream = u.openStream();
        int ch = 0;
        StringBuffer sb = new StringBuffer();
        while ((ch = inStream.read()) >= 0)
          sb.append((char) ch);
        //System.out.println("***\nContent="+sb.toString());
        XMLElement xml = new XMLElement();
        xml.parseString(sb.toString());
        if (xml.getName().equals("XML_Private_Label")) {
          java.util.Enumeration e1 = xml.enumerateChildren();
          while (e1.hasMoreElements()) {
            XMLElement xml2 = (XMLElement) e1.nextElement();
            if (xml2.getName().equals("Customization")) {
              java.util.Enumeration e2 = xml2.enumerateChildren();
              while (e2.hasMoreElements()) {
                XMLElement xml3 = (XMLElement) e2.nextElement();
                if (xml3.getName().equals("Images")) {

                  java.util.Hashtable replacementHT = new java.util.Hashtable();
                  java.util.Enumeration e3 = xml3.enumerateChildren();

                  while (e3.hasMoreElements()) {
                    XMLElement xml4 = (XMLElement) e3.nextElement();
                    if (xml4.getName().equals("Img")) {
                      java.util.Enumeration e4 = xml4.enumerateChildren();
                      String name = null;
                      String source = null;
                      while (e4.hasMoreElements()) {
                        XMLElement xml5 = (XMLElement) e4.nextElement();
                        if (xml5.getName().equals("Name"))
                          name = xml5.getContent();
                        else if (xml5.getName().equals("Source")) {
                          source = xml5.getContent();
                          source = new URL(u, source).toExternalForm();
                        }
                      }
                      if (name != null && source != null) {
                        // Private-label replacable images assign directly to avoid clashing names if some of them point to the same image.
                        if (name.equalsIgnoreCase("LOGO_KEY_MAIN") || name.equalsIgnoreCase("LogoKey435_260"))
                          ImageNums.setImageName(ImageNums.LOGO_KEY_MAIN, source);
                        else if (name.equalsIgnoreCase("LOGO_BANNER_MAIN") || name.equalsIgnoreCase("LogoBanner435_80"))
                          ImageNums.setImageName(ImageNums.LOGO_BANNER_MAIN, source);
                        else if (name.equalsIgnoreCase("WINDOW_POPUP") || name.equalsIgnoreCase("WindowPopup"))
                          ImageNums.setImageName(ImageNums.WINDOW_POPUP, source);
                        else if (name.equalsIgnoreCase("FRAME_LOCK32") || name.equalsIgnoreCase("FrameLock32"))
                          ImageNums.setImageName(ImageNums.FRAME_LOCK32, source);
                        else
                          replacementHT.put(name, source);
                      }
                    }
                  }

                  if (replacementHT.size() > 0) {
                    ArrayList imagesL = ImageNums.getImageNames();
                    for (int i=0; i<imagesL.size(); i++) {
                      String replacement = (String) replacementHT.get(imagesL.get(i));
                      if (replacement != null) {
                        ImageNums.setImageName(i, replacement);
                      }
                    }
                    // First 4 Customization images
                    String customizationImageName = "customization-image";
                    for (int i=0; i<4; i++) {
                      String replacement = (String) replacementHT.get(customizationImageName+"-"+i);
                      if (replacement != null) {
                        ImageNums.setImageName(i, replacement);
                      }
                    }
                  }
                }

                if (xml3.getName().equals("Templates")) {

                  replacementTemplatesHM = new HashMap();
                  Enumeration e3 = xml3.enumerateChildren();

                  while (e3.hasMoreElements()) {
                    XMLElement xml4 = (XMLElement) e3.nextElement();
                    if (xml4.getName().equals("Template")) {
                      java.util.Enumeration e4 = xml4.enumerateChildren();
                      String name = null;
                      String source = null;
                      while (e4.hasMoreElements()) {
                        XMLElement xml5 = (XMLElement) e4.nextElement();
                        if (xml5.getName().equals("Name"))
                          name = xml5.getContent();
                        else if (xml5.getName().equals("Source")) {
                          source = xml5.getContent();
                          source = new URL(u, source).toExternalForm();
                        }
                      }
                      if (name != null && source != null) {
                        replacementTemplatesHM.put(name, source);
                      }
                    }
                  }
                }

                if (xml3.getName().equals("Strings") || xml3.getName().equals("Settings")) {

                  HashMap replacementHM = new HashMap();
                  Enumeration e3 = xml3.enumerateChildren();

                  while (e3.hasMoreElements()) {
                    XMLElement xml4 = (XMLElement) e3.nextElement();
                    if (xml4.getName().equals("string") || xml4.getName().equals("set")) {
                      java.util.Enumeration e4 = xml4.enumerateChildren();
                      String key = null;
                      String str= null;
                      while (e4.hasMoreElements()) {
                        XMLElement xml5 = (XMLElement) e4.nextElement();
                        if (xml5.getName().equals("key"))
                          key = xml5.getContent();
                        else if (xml5.getName().equals("str"))
                          str = xml5.getContent();
                      }
                      if (key != null && str != null) {
                        replacementHM.put(key, str);
                      }
                    }
                  }

                  if (replacementHM.size() > 0) {
                    String[] keys = com.CH_co.util.URLs.customizationKeys;
                    String[] strings = com.CH_co.util.URLs.customizationStrings;
                    for (int i=0; i<keys.length; i++) {
                      String replacement = (String) replacementHM.get(keys[i]);
                      if (replacement != null)
                        strings[i] = replacement;
                    }
                  }

                }
              }
            }
          }
        }
        // if no exception then set the new private label URL as default
        PRIVATE_LABEL_SETTINGS_URL = url;
        // load the default server list
      } catch (Throwable t) {
        System.out.println("Private Label NOT loaded, stack trace:");
        t.printStackTrace();
        loadDefaults();
        throw new IllegalArgumentException("Error loading private label " + url + ", error message is " + t.getMessage());
      }
    }
  }

  public static void main(String[] args) {
    //loadPrivateLabel("http://www.cryptoheaven.com/Support/CryptoHeavenPAD.xml");
    loadPrivateLabel("file://localhost/c:/PrivateLabel.xml");
  }

}