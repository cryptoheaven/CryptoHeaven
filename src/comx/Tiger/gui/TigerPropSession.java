/*
 * Copyright 2001-2013 by CryptoHeaven Corp.,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */

package comx.Tiger.gui;

import comx.tig.en.SingleTigerSession;
import comx.Tiger.ssce.*;
import java.util.*;

/**
 * <b>Copyright</b> &copy; 2001-2013
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Corp.
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
public class TigerPropSession extends PropSpellingSession {

  /** Creates new TigerPropSession */
  public TigerPropSession(Properties properties) {
    super(properties);
  }

  public int countUserLexicons() {
    FileTextLexicon[] userLexicons = getUserLexicons();
    return userLexicons != null ? userLexicons.length : 0;
  }

  public void setOptionsFromProperties(Properties properties, String prefix) {
    super.setOptionsFromProperties(properties, prefix);
    try {
      Properties sessionProperties = getProperties();
      String initialLanguageChoice = sessionProperties.getProperty(SingleTigerSession.PROPERTY__LANGUAGE_NAME);
      String newLanguageChoice = properties.getProperty(SingleTigerSession.PROPERTY__LANGUAGE_NAME);
      if (newLanguageChoice != null) {
        if (initialLanguageChoice == null || !initialLanguageChoice.equalsIgnoreCase(newLanguageChoice)) {
          if (SingleTigerSession.loadLanguageLexicons(newLanguageChoice)) {
            sessionProperties.setProperty(SingleTigerSession.PROPERTY__LANGUAGE_NAME, newLanguageChoice);
          }
        }
      }
      TigerBkgChecker.backgroundCheckEnabled = Boolean.valueOf(properties.getProperty(TigerBkgChecker.PROPERTY__BACKGROUND_CHECK_ENABLED, ""+TigerBkgChecker.backgroundCheckEnabled)).booleanValue();
      int countLanguageLexicons = SingleTigerSession.countLanguageLexicons(this);
      if (countLanguageLexicons < 1) TigerBkgChecker.backgroundCheckEnabled = false;
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }
}