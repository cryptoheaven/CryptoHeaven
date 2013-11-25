/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package comx.tig.en;

import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_co.service.records.UserSettingsRecord;
import com.CH_co.trace.TraceProperties;
import com.CH_co.util.ArrayUtils;
import com.CH_co.util.URLs;
import com.CH_gui.util.NoObfuscateException;
import com.CH_gui.util.SpellCheckerWrapper;
import comx.Tiger.gui.TigerBkgChecker;
import comx.Tiger.gui.TigerPropSession;
import comx.Tiger.ssce.Lexicon;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Properties;
import java.util.Vector;

/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * Class Description:
 *
 * Single instance of a spelling session.
 *
 * <b>$Revision: 1.4 $</b>
 *
 * @author Marcin Kurzawa
 */
public class SingleTigerSession extends Object {

  private static TigerPropSession singleInstance;
  private static final Object monitorObj = new Object();
  private static Properties defaultProperties = new Properties();
  private static String[][] DEFAULT_PROPERTIES;
  public static String PROPERTY__LANGUAGE_NAME = "LanguageName";
  public static String PROPERTY__MAX_AVAIL_LANGS = SpellCheckerWrapper.PROPERTY__MAX_AVAIL_LANGS;
  /**
   * 'languageNameSets' are in direct corelation with 'languageFiles' and
   * 'languageProperties', do not change their order.
   */
  private static final String[][] languageNameSets = {
    {"English (Canada)", "en", "CA"}, // top language, used if user's locale can't be matched to any supported lang/country
    {"English (U.K.)", "en", "GB"},
    {"English (U.S.)", "en", "US"},
    {"American Legal", "american", "legal"},
    {"American Medical", "american", "medical"},
    {"British Legal", "british", "legal"},
    {"British Medical", "british", "medical"},
    {"Brazilian", "pt", "BR"},
    {"Danish", "da", "DK"},
    {"Dutch", "nl", "NL"},
    {"Finnish", "fi", "FI"},
    {"French", "fr", "FR"},
    {"German", "de", "DE"},
    {"Italian", "it", "IT"},
    {"Norwegian", "no", "NO"},
    {"Portugese", "pt", "PT"},
    {"Spanish", "es", "ES"},
    {"Swedish", "sv", "SE"},};
  private static String defaultLanguageName = "";
  private static String[] languageNames;
  public static String[] languageNamesAvailable;
  private static String[][] languageFiles = {
    { // English (Canada)
      "/comx/tig/i/ca.t,resource,t", // *.t = text
      "/comx/tig/i/ca2.c,resource,c", // *.c = compressed
      "/comx/tig/en/corr.t,resource,t", // corr.t = auto correction word list
      "/comx/tig/en/tech.t,resource,t",},
    { // English (U.K.)
      "/comx/tig/i/br.t,resource,t",
      "/comx/tig/i/br2.c,resource,c",
      "/comx/tig/en/corr.t,resource,t",
      "/comx/tig/en/tech.t,resource,t",},
    { // English (U.S.)
      "/comx/tig/en/am.t,resource,t",
      "/comx/tig/en/am2.c,resource,c",
      "/comx/tig/en/corr.t,resource,t",
      "/comx/tig/en/tech.t,resource,t",},
    { // English (U.S) Legal
      "/comx/tig/i/la.t,resource,t",
      "/comx/tig/i/la2.c,resource,c",
      "/comx/tig/en/am.t,resource,t",
      "/comx/tig/en/am2.c,resource,c",
      "/comx/tig/en/corr.t,resource,t",},
    { // English (U.S) Medical
      "/comx/tig/i/ma.t,resource,t",
      "/comx/tig/i/ma2.c,resource,c",
      "/comx/tig/en/am.t,resource,t",
      "/comx/tig/en/am2.c,resource,c",
      "/comx/tig/en/corr.t,resource,t",},
    { // English (U.K) Legal
      "/comx/tig/i/lb.t,resource,t",
      "/comx/tig/i/lb2.c,resource,c",
      "/comx/tig/i/br.t,resource,t",
      "/comx/tig/i/br2.c,resource,c",
      "/comx/tig/en/corr.t,resource,t",},
    { // English (U.K) Medical
      "/comx/tig/i/mb.t,resource,t",
      "/comx/tig/i/mb2.c,resource,c",
      "/comx/tig/i/br.t,resource,t",
      "/comx/tig/i/br2.c,resource,c",
      "/comx/tig/en/corr.t,resource,t",},
    { // Brazilian
      "/comx/tig/i/pb.ut,resource,t",
      "/comx/tig/i/pb.t,resource,t",
      "/comx/tig/i/pb2.c,resource,c",},
    { // Danish
      "/comx/tig/i/da.ut,resource,t",
      "/comx/tig/i/da.t,resource,t",
      "/comx/tig/i/da2.c,resource,c",},
    { // Dutch
      "/comx/tig/i/du.ut,resource,t",
      "/comx/tig/i/du.t,resource,t",
      "/comx/tig/i/du2.c,resource,c",},
    { // Finnish
      "/comx/tig/i/fi.ut,resource,t",
      "/comx/tig/i/fi.t,resource,t",
      "/comx/tig/i/fi2.c,resource,c",},
    { // French
      "/comx/tig/i/fr.ut,resource,t",
      "/comx/tig/i/fr.t,resource,t",
      "/comx/tig/i/fr2.c,resource,c",},
    { // German
      "/comx/tig/i/ge.t,resource,t",
      "/comx/tig/i/ge2.c,resource,c",},
    { // Italian
      "/comx/tig/i/it.ut,resource,t",
      "/comx/tig/i/it.t,resource,t",
      "/comx/tig/i/it2.c,resource,c",},
    { // Norwegian
      "/comx/tig/i/nb.ut,resource,t",
      "/comx/tig/i/nb.t,resource,t",
      "/comx/tig/i/nb2.c,resource,c",},
    { // Portugese
      "/comx/tig/i/po.ut,resource,t",
      "/comx/tig/i/po.t,resource,t",
      "/comx/tig/i/po2.c,resource,c",},
    { // Spanish
      "/comx/tig/i/sp.ut,resource,t",
      "/comx/tig/i/sp.t,resource,t",
      "/comx/tig/i/sp2.c,resource,c",},
    { // Swedish
      "/comx/tig/i/sw.ut,resource,t",
      "/comx/tig/i/sw.t,resource,t",
      "/comx/tig/i/sw2.c,resource,c",},};
  private static String[][][] languageProperties = {
    { // English (Canada)
      {"SPLIT_WORDS_OPT", "false"},
      {"SPLIT_CONTRACTED_WORDS_OPT", "false"},},
    { // English (U.K.)
      {"SPLIT_WORDS_OPT", "false"},
      {"SPLIT_CONTRACTED_WORDS_OPT", "false"},},
    { // English (U.S.)
      {"SPLIT_WORDS_OPT", "false"},
      {"SPLIT_CONTRACTED_WORDS_OPT", "false"},},
    { // English (U.S) Legal
      {"SPLIT_WORDS_OPT", "false"},
      {"SPLIT_CONTRACTED_WORDS_OPT", "false"},},
    { // English (U.S) Medical
      {"SPLIT_WORDS_OPT", "false"},
      {"SPLIT_CONTRACTED_WORDS_OPT", "false"},},
    { // English (U.K) Legal
      {"SPLIT_WORDS_OPT", "false"},
      {"SPLIT_CONTRACTED_WORDS_OPT", "false"},},
    { // English (U.K) Medical
      {"SPLIT_WORDS_OPT", "false"},
      {"SPLIT_CONTRACTED_WORDS_OPT", "false"},},
    { // Brazilian
      {"SPLIT_WORDS_OPT", "false"},
      {"SPLIT_CONTRACTED_WORDS_OPT", "false"},},
    { // Danish
      {"SPLIT_WORDS_OPT", "false"},
      {"SPLIT_CONTRACTED_WORDS_OPT", "false"},},
    { // Dutch
      {"SPLIT_WORDS_OPT", "false"},
      {"SPLIT_CONTRACTED_WORDS_OPT", "false"},},
    { // Finnish
      {"SPLIT_WORDS_OPT", "false"},
      {"SPLIT_CONTRACTED_WORDS_OPT", "true"},},
    { // French
      {"SPLIT_WORDS_OPT", "false"},
      {"SPLIT_CONTRACTED_WORDS_OPT", "true"},},
    { // German
      {"SPLIT_WORDS_OPT", "true"},
      {"SPLIT_CONTRACTED_WORDS_OPT", "false"},},
    { // Italian
      {"SPLIT_WORDS_OPT", "false"},
      {"SPLIT_CONTRACTED_WORDS_OPT", "true"},},
    { // Norwegian
      {"SPLIT_WORDS_OPT", "false"},
      {"SPLIT_CONTRACTED_WORDS_OPT", "false"},},
    { // Portugese
      {"SPLIT_WORDS_OPT", "false"},
      {"SPLIT_CONTRACTED_WORDS_OPT", "false"},},
    { // Spanish
      {"SPLIT_WORDS_OPT", "false"},
      {"SPLIT_CONTRACTED_WORDS_OPT", "false"},},
    { // Swedish
      {"SPLIT_WORDS_OPT", "true"},
      {"SPLIT_CONTRACTED_WORDS_OPT", "false"},},};

  static {
    try {
      // load displayable array of languages
      languageNames = new String[languageNameSets.length];
      for (int i = 0; i < languageNameSets.length; i++) {
        languageNames[i] = languageNameSets[i][0];
      }
    } catch (Throwable t) {
    }

    try {
      // load default language based on current locale
      Locale locale = Locale.getDefault();
      String lang = locale.getLanguage();
      String country = locale.getCountry();
      for (int i = 0; i < languageNameSets.length; i++) {
        if (languageNameSets[i][1].equalsIgnoreCase(lang)) {
          defaultLanguageName = languageNameSets[i][0];
        }
        if (languageNameSets[i][2].equalsIgnoreCase(country)) {
          break;
        }
      }
    } catch (Throwable t) {
    }

    try {
      // load default set of properties
      DEFAULT_PROPERTIES = new String[][]{
        {"ALLOW_ACCENTED_CAPS_OPT", "true"},
        {"CASE_SENSITIVE_OPT", "true"},
        {"Comparator", "Typographical"},
        {"IGNORE_ALL_CAPS_WORD_OPT", "false"},
        {"IGNORE_CAPPED_WORD_OPT", "false"},
        {"IGNORE_DOMAIN_NAMES_OPT", "true"},
        {"IGNORE_MIXED_CASE_OPT", "true"},
        {"IGNORE_MIXED_DIGITS_OPT", "false"},
        {"IGNORE_NON_ALPHA_WORD_OPT", "true"},
        {"MinSuggestDepth", "20"},
        {"REPORT_DOUBLED_WORD_OPT", "true"},
        {"REPORT_MIXED_CASE_OPT", "false"},
        {"REPORT_MIXED_DIGITS_OPT", "false"},
        {"REPORT_UNCAPPED_OPT", "true"},
        {"SPLIT_CONTRACTED_WORDS_OPT", "false"},
        {"SPLIT_HYPHENATED_WORDS_OPT", "true"},
        {"SPLIT_WORDS_OPT", "false"},
        {"STRIP_POSSESSIVES_OPT", "true"},
        {"SUGGEST_SPLIT_WORDS_OPT", "true"},
        {"Suggestions", "typographical"},
        {"UserLexiconFilePostfix", "dictionaries/user.txt"},
        {PROPERTY__LANGUAGE_NAME, defaultLanguageName},};
    } catch (Throwable t) {
    }

    try {
      initialLoad();
    } catch (Throwable t) {
    }
    try {
      initialAvailability();
    } catch (Throwable t) {
    }
  }

  private static void initialLoad() {
    // always load default properties first
    for (int i = 0; DEFAULT_PROPERTIES != null && i < DEFAULT_PROPERTIES.length; i++) {
      if (defaultProperties.getProperty(DEFAULT_PROPERTIES[i][0]) == null) {
        defaultProperties.setProperty(DEFAULT_PROPERTIES[i][0], DEFAULT_PROPERTIES[i][1]);
      }
    }
  }

  private static void initialAvailability() {
    Vector languageNamesAvailableV = new Vector();
    for (int i = 0; i < languageFiles.length; i++) {
      String filename = languageFiles[i][0].substring(1, languageFiles[i][0].indexOf(',')); // strip '/' from begining
      boolean available = URLs.getResourceURL(filename) != null;
      if (available) {
        languageNamesAvailableV.addElement(languageNames[i]);
      }
    }
    if (languageNamesAvailableV.size() == 0) {
      languageNamesAvailableV.addElement("(no dictionary available)");
    }
    languageNamesAvailable = new String[languageNamesAvailableV.size()];
    languageNamesAvailableV.toArray(languageNamesAvailable);
  }

  public static TigerPropSession getSingleInstance_reflection() throws NoObfuscateException {
    return getSingleInstance();
  }
  public static TigerPropSession getSingleInstance() {
    synchronized (monitorObj) {
      if (singleInstance == null) {
        // Create a spelling session.
        try {
          FetchedDataCache cache = FetchedDataCache.getSingleInstance();
          UserSettingsRecord userSettings = cache.getMyUserSettingsRecord();
          Properties properties = (Properties) defaultProperties.clone();
          // load custom properties
          Properties customProps = userSettings != null && userSettings.spellingProps != null ? userSettings.spellingProps : new Properties();
          Enumeration enm = customProps.keys();
          while (enm.hasMoreElements()) {
            String key = (String) enm.nextElement();
            properties.setProperty(key, customProps.getProperty(key));
          }
          String userLexFilePostfix = properties.getProperty("UserLexiconFilePostfix");
          String userLexFilename = TraceProperties.getPropertiesFullPathName() + userLexFilePostfix;
          properties.setProperty("UserLexicon1", userLexFilename + ",file,t");
          File userLexFile = new File(userLexFilename);
          if (!userLexFile.exists()) {
            try {
              int pathSeperator = userLexFilename.lastIndexOf('/');
              if (pathSeperator < 0) {
                pathSeperator = userLexFilename.lastIndexOf('\\');
              }
              if (pathSeperator > 0) {
                new File(userLexFilename.substring(0, pathSeperator)).mkdirs();
              }
              userLexFile.createNewFile();
              OutputStream fOut = new BufferedOutputStream(new FileOutputStream(userLexFile), 1 * 1024);
              fOut.write("#LID 24941".getBytes());
              fOut.flush();
              fOut.close();
            } catch (Throwable t) {
            }
          }
          // Set the current lexicons as defaults may be blank
          setLanguageLexiconProperties(properties, properties.getProperty(PROPERTY__LANGUAGE_NAME));
          singleInstance = new TigerPropSession(properties);
          try {
            if (defaultLanguageName != null && defaultLanguageName.length() > 0) {
              TigerBkgChecker.backgroundCheckEnabled = Boolean.valueOf(properties.getProperty(TigerBkgChecker.PROPERTY__BACKGROUND_CHECK_ENABLED, "" + TigerBkgChecker.backgroundCheckEnabled)).booleanValue();
            } else {
              TigerBkgChecker.backgroundCheckEnabled = false;
            }
            int countLanguageLexicons = countLanguageLexicons(singleInstance);
            if (countLanguageLexicons < 1) {
              TigerBkgChecker.backgroundCheckEnabled = false;
            }
          } catch (Throwable t) {
            t.printStackTrace();
          }
        } catch (Throwable t) {
          t.printStackTrace();
        }
      }
    }
    return singleInstance;
  }

  /**
   * @return true iff specified dictionary properties were set.
   */
  public static boolean setLanguageLexiconProperties(Properties props, String name) {
    int langIndex = -1;
    // find language in available set ...
    if (languageNamesAvailable.length > 0) {
      langIndex = ArrayUtils.find(languageNamesAvailable, name);
    }
    // map available to raw index
    if (langIndex >= 0) {
      langIndex = ArrayUtils.find(languageNames, languageNamesAvailable[langIndex]);
    }
    boolean specifiedLangFound = langIndex >= 0;
    // if specified language not found, take the first one
    if (langIndex < 0 && languageNamesAvailable.length > 0) {
      langIndex = ArrayUtils.find(languageNames, languageNamesAvailable[0]);
    }
    Enumeration enm = props.keys();
    while (enm.hasMoreElements()) {
      String key = (String) enm.nextElement();
      if (key.startsWith("MainLexicon")) {
        props.remove(key);
      }
    }
    if (langIndex >= 0) {
      // set the Lexicons
      for (int i = 0; i < languageFiles[langIndex].length; i++) {
        String key = "MainLexicon" + (i + 1);
        String value = languageFiles[langIndex][i];
        props.setProperty(key, value);
      }
      // set the changing properties
      for (int i = 0; i < languageProperties[langIndex].length; i++) {
        String key = languageProperties[langIndex][i][0];
        String value = languageProperties[langIndex][i][1];
        props.setProperty(key, value);
      }
    }
    return specifiedLangFound;
  }

  public static int countLanguageLexicons_reflection() throws NoObfuscateException {
    TigerPropSession speller = SingleTigerSession.getSingleInstance();
    return countLanguageLexicons(speller);
  }

  public static int countLanguageLexicons(TigerPropSession session) {
    Lexicon[] lexicons = session.getLexicons();
    Lexicon[] userLex = session.getUserLexicons();
    Lexicon tempLex = session.getTempLexicon();
    int countUserLex = userLex != null ? userLex.length : 0;
    int countTempLex = tempLex != null ? 1 : 0;
    return lexicons.length - countUserLex - countTempLex;
  }

  public static String[] getAvailableLanguages_reflection() throws NoObfuscateException {
    return getAvailableLanguages();
  }

  public static String[] getAvailableLanguages() {
    String[] langs = null;
    try {
      langs = (String[]) languageNamesAvailable.clone();
    } catch (Throwable t) {
    }
    return langs;
  }

  /**
   * @return true iff the exact language specified was found, else (or if
   * default was loaded) return false;
   */
  public static boolean loadLanguageLexicons(String name) {
    TigerPropSession session = getSingleInstance();
    Properties props = session.getProperties();
    boolean rc = setLanguageLexiconProperties(props, name);
    session.loadLanguageLexicons();
    return rc;
  }
}