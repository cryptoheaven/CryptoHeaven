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

package com.CH_co.util;


/**
 * <b>Copyright</b> &copy; 2001-2009
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>
 *
 * <b>$Revision: 1.0 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public interface PrivateLabelI {

  /**
   * @return default activation code
   */
  public String getActivationCodeDefault();

  /**
   * @return true/false
   */
  public String getActivationCodeFieldRemoved();

  /**
   * @return true/false
   * @deprecated Server field is always initially hidden with option to show it
   */
  public String getServerFieldRemoved();

  /**
   * @return preferred jar name
   */
  public String getJarName();

  /**
   * @return enable/disable/remove
   */
  public String getNewAccountButton();

  /**
   * @return label's XML setting file URL
   */
  public String getPrivateLabelURL();

  /**
   * @return Service / Software name
   */
  public String getServiceSoftwareName();

  public String getImage_LogoKeyMain();
  public String getImage_LogoBanner();
  public String getImage_WindowPopup();

  // Below this line are items loaded after login and not required for proper look of login dialog

  public String getDomain();
  public String getDomainWeb();
  public String getDomainMail();
  public String getHomePage();

  public String getServiceCommunityName();
  public String getServiceNetworkName();
  public String getServiceProviderName();

  public String getWelcomeEmailFrom();
  public String getWelcomeEmailSubject();
  public String getWelcomeEmailBody();
  public String getWelcomeTemplate();

}