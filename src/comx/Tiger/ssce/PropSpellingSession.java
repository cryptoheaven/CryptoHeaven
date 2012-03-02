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
// Source File Name:   PropSpellingSession.java

package comx.Tiger.ssce;

import java.io.*;
import java.net.URL;
import java.util.*;

// Referenced classes of package com.wintertree.ssce:
//      SpellingSession, MemTextLexicon, FileTextLexicon, Lexicon, 
//      EnglishPhoneticComparator, TypographicalComparator, CompressedLexicon, StreamTextLexicon, 
//      WordComparator

public class PropSpellingSession extends SpellingSession
  implements Cloneable
{

  protected URL codeBase;
  /**
   * @deprecated Field comparator is deprecated
   */
  public WordComparator comparator;
  protected String mainLexPath;
  /**
   * @deprecated Field minSuggestDepth is deprecated
   */
  public int minSuggestDepth;
  protected Properties properties;
  protected String propertyPrefix;
  protected int tempLexiconX;
  /**
   * @deprecated Field userLexicons is deprecated
   */
  public FileTextLexicon userLexicons[];
  protected String userLexPath;

  public PropSpellingSession(Properties properties1, URL url, String s, String s1, String s2)
  {
    mainLexPath = null;
    propertyPrefix = "";
    userLexPath = null;
    properties = properties1;
    codeBase = url;
    propertyPrefix = s;
    mainLexPath = s1;
    userLexPath = s2;
    String s3 = "";
    if (s != null)
      s3 = s;
    Vector vector1 = new Vector();
    for (int i = 1; i < 99; i++)
    {
      String s4 = properties1.getProperty(s3 + "UserLexicon" + i);
      if (null == s4)
        break;
      StringTokenizer stringtokenizer = new StringTokenizer(s4, ",");
      String s7 = "";
      if (stringtokenizer.hasMoreTokens())
        s7 = stringtokenizer.nextToken();
      String s9 = "file";
      if (stringtokenizer.hasMoreTokens())
        s9 = stringtokenizer.nextToken();
      String s11 = "T";
      if (stringtokenizer.hasMoreTokens())
        s11 = stringtokenizer.nextToken();
      Lexicon lexicon = openLex(s7, s9, s11, s2);
      if (lexicon == null) {
        System.out.println("no user lexicon for " + s7 + ", " + s9 + ", " + s11 + ", " + s2 + ".");
        continue;
      }
      if (lexicon instanceof FileTextLexicon)
        vector1.addElement(lexicon);
    }

    userLexicons = null;
    if (!vector1.isEmpty())
    {
      userLexicons = new FileTextLexicon[vector1.size()];
      vector1.copyInto(userLexicons);
    }

    loadLanguageLexicons();

    setOptionsFromProperties(properties1, s3);
  }

  public PropSpellingSession(Properties properties1)
  {
    this(properties1, null, null, null, null);
  }

  public PropSpellingSession(Properties properties1, URL url)
  {
    this(properties1, url, null, null, null);
  }

  public void setOptionsFromProperties(Properties properties, String prefix) {
    String s3 = prefix != null ? prefix : "";
    String s6;
    if ((s6 = properties.getProperty(s3 + "CASE_SENSITIVE_OPT")) != null)
      setOption(1, Boolean.valueOf(s6).booleanValue());
    if ((s6 = properties.getProperty(s3 + "IGNORE_ALL_CAPS_WORD_OPT")) != null)
      setOption(2, Boolean.valueOf(s6).booleanValue());
    if ((s6 = properties.getProperty(s3 + "IGNORE_CAPPED_WORD_OPT")) != null)
      setOption(4, Boolean.valueOf(s6).booleanValue());
    if ((s6 = properties.getProperty(s3 + "IGNORE_MIXED_CASE_OPT")) != null)
      setOption(8, Boolean.valueOf(s6).booleanValue());
    if ((s6 = properties.getProperty(s3 + "IGNORE_MIXED_DIGITS_OPT")) != null)
      setOption(16, Boolean.valueOf(s6).booleanValue());
    if ((s6 = properties.getProperty(s3 + "IGNORE_NON_ALPHA_WORD_OPT")) != null)
      setOption(32, Boolean.valueOf(s6).booleanValue());
    if ((s6 = properties.getProperty(s3 + "REPORT_DOUBLED_WORD_OPT")) != null)
      setOption(64, Boolean.valueOf(s6).booleanValue());
    if ((s6 = properties.getProperty(s3 + "REPORT_MIXED_CASE_OPT")) != null)
      setOption(128, Boolean.valueOf(s6).booleanValue());
    if ((s6 = properties.getProperty(s3 + "REPORT_MIXED_DIGITS_OPT")) != null)
      setOption(256, Boolean.valueOf(s6).booleanValue());
    if ((s6 = properties.getProperty(s3 + "REPORT_UNCAPPED_OPT")) != null)
      setOption(1024, Boolean.valueOf(s6).booleanValue());
    if ((s6 = properties.getProperty(s3 + "SPLIT_CONTRACTED_WORDS_OPT")) != null)
      setOption(2048, Boolean.valueOf(s6).booleanValue());
    if ((s6 = properties.getProperty(s3 + "SPLIT_HYPHENATED_WORDS_OPT")) != null)
      setOption(4096, Boolean.valueOf(s6).booleanValue());
    if ((s6 = properties.getProperty(s3 + "SPLIT_WORDS_OPT")) != null)
      setOption(8192, Boolean.valueOf(s6).booleanValue());
    if ((s6 = properties.getProperty(s3 + "STRIP_POSSESSIVES_OPT")) != null)
      setOption(16384, Boolean.valueOf(s6).booleanValue());
    if ((s6 = properties.getProperty(s3 + "SUGGEST_SPLIT_WORDS_OPT")) != null)
      setOption(32768, Boolean.valueOf(s6).booleanValue());
    if ((s6 = properties.getProperty(s3 + "IGNORE_DOMAIN_NAMES_OPT")) != null)
      setOption(0x10000, Boolean.valueOf(s6).booleanValue());
    if ((s6 = properties.getProperty(s3 + "ALLOW_ACCENTED_CAPS_OPT")) != null)
      setOption(0x20000, Boolean.valueOf(s6).booleanValue());
    minSuggestDepth = 50;
    if ((s6 = properties.getProperty(s3 + "MinSuggestDepth")) != null)
      minSuggestDepth = Integer.valueOf(s6).intValue();
    if ((s6 = properties.getProperty(s3 + "Suggestions")) != null && s6.equalsIgnoreCase("Phonetic"))
      comparator = new EnglishPhoneticComparator();
    else
      comparator = new TypographicalComparator();
  }
  public void loadLanguageLexicons() {
    Vector vector = new Vector();

    // retain old temporary lexicon, only change the main language lexicons
    Lexicon tempLexicon = null;
    try {
      tempLexicon = getTempLexicon();
    } catch (Throwable t) {
    }
    if (tempLexicon != null)
      vector.addElement(tempLexicon);
    else
      vector.addElement(new MemTextLexicon());
    tempLexiconX = 0;
    Lexicon[] userLexicons = getUserLexicons();
    // retain all user lexicons
    for (int i=0; userLexicons!=null && i<userLexicons.length; i++)
      vector.addElement(userLexicons[i]);

    String s3 = "";
    if (propertyPrefix != null)
      s3 = propertyPrefix;
    String s1 = mainLexPath;
    for (int j = 1; j < 99; j++)
    {
      String prop = s3 + "MainLexicon" + j;
      String s5 = properties.getProperty(prop);
      if (null == s5)
        break;
      StringTokenizer stringtokenizer1 = new StringTokenizer(s5, ",");
      String s8 = "";
      if (stringtokenizer1.hasMoreTokens())
        s8 = stringtokenizer1.nextToken();
      String s10 = "file";
      if (stringtokenizer1.hasMoreTokens())
        s10 = stringtokenizer1.nextToken();
      String s12 = null;
      if (stringtokenizer1.hasMoreTokens())
        s12 = stringtokenizer1.nextToken();
      Lexicon lexicon1 = openLex(s8, s10, s12, s1);
      if (lexicon1 == null)
        System.out.println("no main lexicon for " + s8 + ", " + s10 + ", " + s12 + ", " + s1 + ".");
      if (lexicon1 != null) {
        vector.addElement(lexicon1);
      }
    }

    Lexicon alexicon[] = new Lexicon[vector.size()];
    vector.copyInto(alexicon);
    setLexicons(alexicon);
  }

  public Object clone()
  {
    PropSpellingSession propspellingsession = (PropSpellingSession)super.clone();
    propspellingsession.codeBase = codeBase;
    propspellingsession.comparator = comparator;
    propspellingsession.mainLexPath = mainLexPath;
    propspellingsession.minSuggestDepth = minSuggestDepth;
    propspellingsession.tempLexiconX = tempLexiconX;
    if (properties != null)
      propspellingsession.properties = new Properties(properties);
    else
      propspellingsession.properties = null;
    propspellingsession.propertyPrefix = propertyPrefix;
    if (userLexicons != null)
    {
      propspellingsession.userLexicons = new FileTextLexicon[userLexicons.length];
      System.arraycopy(userLexicons, 0, propspellingsession.userLexicons, 0, propspellingsession.userLexicons.length);
    } else
    {
      propspellingsession.userLexicons = null;
    }
    propspellingsession.userLexPath = userLexPath;
    return propspellingsession;
  }

  public WordComparator getComparator()
  {
    return comparator;
  }

  public int getMinSuggestDepth()
  {
    return minSuggestDepth;
  }

  public Properties getProperties()
  {
    properties.put("CASE_SENSITIVE_OPT", String.valueOf(getOption(1)));
    properties.put("IGNORE_ALL_CAPS_WORD_OPT", String.valueOf(getOption(2)));
    properties.put("IGNORE_CAPPED_WORD_OPT", String.valueOf(getOption(4)));
    properties.put("IGNORE_MIXED_CASE_OPT", String.valueOf(getOption(8)));
    properties.put("IGNORE_MIXED_DIGITS_OPT", String.valueOf(getOption(16)));
    properties.put("IGNORE_NON_ALPHA_WORD_OPT", String.valueOf(getOption(32)));
    properties.put("REPORT_DOUBLED_WORD_OPT", String.valueOf(getOption(64)));
    properties.put("REPORT_MIXED_CASE_OPT", String.valueOf(getOption(128)));
    properties.put("REPORT_MIXED_DIGITS_OPT", String.valueOf(getOption(256)));
    properties.put("REPORT_UNCAPPED_OPT", String.valueOf(getOption(1024)));
    properties.put("SPLIT_CONTRACTED_WORDS_OPT", String.valueOf(getOption(2048)));
    properties.put("SPLIT_HYPHENATED_WORDS_OPT", String.valueOf(getOption(4096)));
    properties.put("SPLIT_WORDS_OPT", String.valueOf(getOption(8192)));
    properties.put("STRIP_POSSESSIVES_OPT", String.valueOf(getOption(16384)));
    properties.put("SUGGEST_SPLIT_WORDS_OPT", String.valueOf(getOption(32768)));
    properties.put("IGNORE_DOMAIN_NAMES_OPT", String.valueOf(getOption(0x10000)));
    properties.put("ALLOW_ACCENTED_CAPS_OPT", String.valueOf(getOption(0x20000)));
    properties.put("MinSuggestDepth", String.valueOf(minSuggestDepth));
    if (comparator instanceof EnglishPhoneticComparator)
      properties.put("Comparator", "Phonetic");
    else
      properties.put("Comparator", "Typographical");
    return properties;
  }

  public MemTextLexicon getTempLexicon()
  {
    Lexicon alexicon[] = getLexicons();
    return (MemTextLexicon)alexicon[tempLexiconX];
  }

  public FileTextLexicon[] getUserLexicons()
  {
    return userLexicons;
  }

  public void setComparator(WordComparator wordcomparator)
  {
    comparator = wordcomparator;
  }

  public void setMinSuggestDepth(int i)
  {
    minSuggestDepth = i;
  }

  public MemTextLexicon setTempLexicon(MemTextLexicon memtextlexicon)
  {
    Lexicon alexicon[] = getLexicons();
    MemTextLexicon memtextlexicon1 = (MemTextLexicon)alexicon[tempLexiconX];
    alexicon[tempLexiconX] = memtextlexicon;
    return memtextlexicon1;
  }

  protected Lexicon openLex(String s, String s1, String s2, String s3)
  {
    Object obj = null;
    try
    {
      if (s1.equalsIgnoreCase("file"))
      {
        if (s3 != null && s.indexOf(File.separator) < 0)
        {
          if (!s3.endsWith(File.separator))
            s3 = s3 + File.separator;
          s = s3 + s;
        }
        if (s2 == null)
          if (CompressedLexicon.isCompressedLexicon(s))
            s2 = "c";
          else
            s2 = "t";
        if (s2.equalsIgnoreCase("c"))
          obj = new CompressedLexicon(s, 0);
        else
          obj = new FileTextLexicon(s);
      } else
      if (s1.equalsIgnoreCase("resource"))
      {
        java.io.InputStream inputstream = PropSpellingSession.class.getResourceAsStream(s);
        if (null == inputstream)
          throw new Exception();
        if (s2.equalsIgnoreCase("t"))
          obj = new StreamTextLexicon(inputstream);
        else
          obj = new CompressedLexicon(inputstream);
      } else
      if (s1.equalsIgnoreCase("url"))
      {
        URL url;
        if (codeBase == null)
          url = new URL(s);
        else
          url = new URL(codeBase + s);
        java.io.InputStream inputstream1 = url.openStream();
        if (null == inputstream1)
          throw new Exception();
        if (s2.equalsIgnoreCase("t"))
          obj = new StreamTextLexicon(inputstream1);
        else
          obj = new CompressedLexicon(inputstream1);
      } else
      if (s1.equalsIgnoreCase("stream"))
      {
        if (s3 != null && s.indexOf(File.separator) < 0)
        {
          if (!s3.endsWith(File.separator))
            s3 = s3 + File.separator;
          s = s3 + s;
        }
        FileInputStream fileinputstream = new FileInputStream(s);
        if (fileinputstream == null)
          throw new Exception();
        if (s2.equalsIgnoreCase("c"))
          obj = new CompressedLexicon(fileinputstream);
        else
          obj = new StreamTextLexicon(fileinputstream);
      }
    }
    catch (Exception exception)
    {
      exception.printStackTrace();
      System.err.println("Can't open " + s + "(" + s1 + "," + s2 + "):");
      if (exception.getMessage() != null)
        System.err.println("  " + exception);
    }
    return ((Lexicon) (obj));
  }
}