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
// Source File Name:   StreamTextLexicon.java

package comx.Tiger.ssce;

import java.io.*;
import java.util.*;

// Referenced classes of package com.wintertree.ssce:
//      MemTextLexicon, ParameterException, UnsupportedException, FileFormatException,
//      LexiconUpdateException, SpellingSession

public class StreamTextLexicon extends MemTextLexicon {

  protected boolean external;
  private static final String bigBOM = "\uFEFF";
  private static final String headerId = "#LID";

  public StreamTextLexicon() {
  }

  public StreamTextLexicon(InputStream inputstream) throws IOException, FileFormatException, LexiconUpdateException {
    external = true;
    load(inputstream);
  }

  public StreamTextLexicon(int i) {
    super(i);
    external = false;
  }

  public void addWord(String s, int i, String s1) throws ParameterException, LexiconUpdateException {
    if (external && i != 105)
      throw new ParameterException("Action " + i + " can't be used with external-format lexicons");
    if (!external) {
      for (int j = 0; j < s.length(); j++)
        if (s.charAt(j) == '\t')
          throw new ParameterException(s + ": contains invalid character(s)");

    }
    super.addWord(s, i, s1);
  }

  public void load(InputStream inputstream) throws IOException, FileFormatException {
    byte abyte0[];
    String s;
    BufferedReader bufferedreader;
    String s1;
    if (inputstream == null)
      throw new UnsupportedException();
    //    try
    //    {
    //      int i = 0xa7d1fa59;
    //      char c = '\213';
    //      char c1 = '\214';
    //      char c2 = '\u0287';
    //      char c3 = '\u03E7';
    //      char c4 = '\227';
    //      char c5 = '\u012B';
    //      int i1 = SpellingSession.getOption(new Integer(32));
    //      if ((i1 & 0xff) == 162)
    //      {
    //        int j1 = (i1 & 0x1f00) >> 8;
    //        int k1 = (i1 & 0x1ffe000) >> 13;
    //        Date date = new Date();
    //        Date date1 = (new GregorianCalendar(2000, 0, 1)).getTime();
    //        Date date2 = new Date(date1.getTime() + (long)k1 * 0x5265c00L);
    //        long l3 = System.currentTimeMillis();
    //        long l5 = (long)SpellingSession.getOption(new Integer(16)) * 1000L;
    //        int j3 = i1 & 0x7fffe000;
    //        int k3 = 0;
    //        for (int i4 = 0; i4 < 32; i4++)
    //        {
    //          k3 += j3 & 1;
    //          j3 >>= 1;
    //        }
    //
    //        if (k3 != j1)
    //          throw new Exception();
    //        if (date.after(date2))
    //        {
    //          if (l3 > l5 + 0xdbba0L)
    //          {
    //            String as[] = {
    //              "\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252", "\305\326\301\314\325\301\324\311\317\316\240\320\305\322\311\317\304\240\305\330\320\311\322\305\304", "\323\345\356\364\362\371\240\323\360\345\354\354\351\356\347\255\303\350\345\343\353\345\362\240\305\356\347\351\356\345\240\305\366\341\354\365\341\364\351\357\356\240\314\351\343\345\356\363\345", "\303\357\360\371\362\351\347\350\364\240\250\343\251\240\262\260\260\263\240\327\351\356\364\345\362\364\362\345\345\240\323\357\346\364\367\341\362\345\240\311\356\343\256", "\324\350\341\356\353\240\371\357\365\240\346\357\362\240\345\366\341\354\365\341\364\351\356\347\240\323\345\356\364\362\371\240\312\341\366\341\240\323\304\313\256", "\331\357\365\362\240\263\260\255\344\341\371\240\345\366\341\354\365\341\364\351\357\356\240\354\351\343\345\356\363\345\240\350\341\363\240\356\357\367\240\345\370\360\351\362\345\344\256", "\324\357\240\357\362\344\345\362\254\240\343\341\354\354\240\261\255\270\260\260\255\263\264\260\255\270\270\260\263\240\250\261\255\266\261\263\255\270\262\265\255\266\262\267\261\251\254\240\357\362\240\363\345\345", "\367\367\367\256\367\351\356\364\345\362\364\362\345\345\255\363\357\346\364\367\341\362\345\256\343\357\355\257\344\345\366\257\363\363\343\345\257\352\341\366\341\363\344\353\256\350\364\355\354", "\346\357\362\240\355\357\362\345\240\351\356\346\357\362\355\341\364\351\357\356\256", "\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252"
    //            };
    //            for (int j4 = 0; j4 < as.length; j4++)
    //            {
    //              for (int i5 = 0; i5 < as[j4].length(); i5++)
    //                System.out.print((char)(as[j4].charAt(i5) & 0x7f));
    //
    //              System.out.println();
    //            }
    //
    //            SpellingSession.setOption(new Integer(16), new Integer((int)(l3 / 1000L)));
    //          }
    //          throw new Exception();
    //        }
    //        if (l3 > l5 + 0xdbba0L)
    //        {
    //          String as1[] = {
    //            "\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252", "\323\345\356\364\362\371\240\323\360\345\354\354\351\356\347\255\303\350\345\343\353\345\362\240\305\356\347\351\356\345\240\305\366\341\354\365\341\364\351\357\356\240\314\351\343\345\356\363\345", "\303\357\360\371\362\351\347\350\364\240\250\343\251\240\262\260\260\263\240\327\351\356\364\345\362\364\362\345\345\240\323\357\346\364\367\341\362\345\240\311\356\343\256", "\306\317\322\240\305\326\301\314\325\301\324\311\317\316\240\317\316\314\331\240\255\240\316\317\324\240\306\317\322\240\320\322\317\304\325\303\324\311\317\316\240\325\323\305", "\324\350\341\356\353\240\371\357\365\240\346\357\362\240\345\366\341\354\365\341\364\351\356\347\240\323\345\356\364\362\371\240\312\341\366\341\240\323\304\313\241", "\324\357\240\357\362\344\345\362\254\240\343\341\354\354\240\261\255\270\260\260\255\263\264\260\255\270\270\260\263\240\250\261\255\266\261\263\255\270\262\265\255\266\262\267\261\251\254\240\357\362\240\363\345\345", "\367\367\367\256\367\351\356\364\345\362\364\362\345\345\255\363\357\346\364\367\341\362\345\256\343\357\355\257\344\345\366\257\363\363\343\345\257\352\341\366\341\363\344\353\256\350\364\355\354", "\346\357\362\240\355\357\362\345\240\351\356\346\357\362\355\341\364\351\357\356\256", "\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252"
    //          };
    //          for (int k4 = 0; k4 < as1.length; k4++)
    //          {
    //            for (int j5 = 0; j5 < as1[k4].length(); j5++)
    //              System.out.print((char)(as1[k4].charAt(j5) & 0x7f));
    //
    //            System.out.println();
    //          }
    //
    //          SpellingSession.setOption(new Integer(16), new Integer((int)(l3 / 1000L)));
    //        }
    //      } else
    //      {
    //        long l1 = i1 ^ i;
    //        long l2 = 0L;
    //        for (int j2 = 0; j2 < 32; j2++)
    //        {
    //          long l4 = l1 >> j2 & 1L;
    //          l2 |= l4 << 31 - j2;
    //        }
    //
    //        int k2 = (int)(l2 / 10000L);
    //        if (k2 != c && k2 != c1)
    //          throw new Exception();
    //        int i3 = (int)(l2 % 10000L);
    //        if (k2 == c && i3 < c2 || i3 > c3)
    //          throw new Exception();
    //        if (k2 == c1 && (i3 < c4 || i3 > c5))
    //          throw new Exception();
    //      }
    //    }
    //    catch (Exception exception)
    //    {
    //      for (long l = System.currentTimeMillis(); System.currentTimeMillis() < l + 1000L;) {
    //        try {
    //          Thread.sleep(25);
    //        } catch (Throwable t) {
    //        }
    //      }
    //      throw new UnsupportedException();
    //    }
    abyte0 = null;
    boolean flag = false;
    do {
      if (flag)
        break;
      try {
        int j = inputstream.available();
        if (0 == j)
          j = 1024;
        byte abyte1[] = new byte[j];
        int k = inputstream.read(abyte1);
        if (k == -1)
          flag = true;
        if (k > 0)
          if (abyte0 != null) {
            byte abyte2[] = new byte[abyte0.length + k];
            System.arraycopy(abyte0, 0, abyte2, 0, abyte0.length);
            System.arraycopy(abyte1, 0, abyte2, abyte0.length, k);
            abyte0 = abyte2;
          } else {
            abyte0 = new byte[k];
            System.arraycopy(abyte1, 0, abyte0, 0, k);
          }
      }
      catch (Exception exception1) {
        flag = true;
      }
    } while (true);
    s = null;
    if (abyte0.length > 1 && (abyte0[0] == -2 && abyte0[1] == -1 || abyte0[0] == 0))
      s = "UnicodeBig";
    ByteArrayInputStream bytearrayinputstream = new ByteArrayInputStream(abyte0);
    if (s == null)
      bufferedreader = new BufferedReader(new InputStreamReader(bytearrayinputstream));
    else
      bufferedreader = new BufferedReader(new InputStreamReader(bytearrayinputstream, s));
    s1 = null;
    try {
      s1 = bufferedreader.readLine();
    }
    catch (Exception exception2) { }
    if (s1 == null) {
      external = true;
      return;
    }
    try {
      if (s1.length() >= "\uFEFF".length() && s1.startsWith("\uFEFF"))
        s1 = s1.substring("\uFEFF".length());
      if (s1.startsWith("#LID")) {
        parseHeaderLine(s1);
        external = false;
      } else {
        external = true;
      }
      if (external) {
        bufferedreader.close();
        ByteArrayInputStream bytearrayinputstream1 = new ByteArrayInputStream(abyte0);
        if (s == null)
          bufferedreader = new BufferedReader(new InputStreamReader(bytearrayinputstream1));
        else
          bufferedreader = new BufferedReader(new InputStreamReader(bytearrayinputstream1, s));
      }
      while ((s1 = bufferedreader.readLine()) != null) {
        char c6 = 'i';
        String s3 = "";
        int i2 = s1.indexOf('\t');
        String s2;
        if (i2 >= 0) {
          s2 = s1.substring(0, i2);
          if (++i2 < s1.length())
            c6 = s1.charAt(i2);
          else
            throw new FileFormatException("Missing action in " + s1);
          if (++i2 < s1.length())
            s3 = s1.substring(i2);
        } else {
          s2 = s1;
        }
        try {
          super.addWord(s2, c6, s3);
        }
        catch (LexiconUpdateException lexiconupdateexception) {
          throw new FileFormatException("Can't add " + s1 + " to lexicon");
        }
        catch (ParameterException parameterexception) {
          throw new FileFormatException("Unrecognized action in " + s1);
        }
      }
    }
    catch (UnsupportedException unsupportedexception) { }
    return;
  }

  public void save(OutputStream outputstream) throws IOException {
    String s = null;
    Object obj = words();
    label0:
      do {
        if (((Enumeration) (obj)).hasMoreElements() && s == null) {
          String s1 = (String)((Enumeration) (obj)).nextElement();
          StringBuffer stringbuffer = new StringBuffer();
          int i = 0;
          do {
            if (i >= s1.length() || s != null)
              break;
            if (s1.charAt(i) > '\377') {
              s = "UnicodeBig";
              break;
            }
            i++;
          } while (true);
          i = 0;
          do {
            if (i >= stringbuffer.length() || s != null)
              continue label0;
            if (stringbuffer.charAt(i) > '\377') {
              s = "UnicodeBig";
              continue label0;
            }
            i++;
          } while (true);
        }
        if (s == null)
          obj = new PrintWriter(new OutputStreamWriter(outputstream), true);
        else
          obj = new PrintWriter(new OutputStreamWriter(outputstream, s), true);
        if (!external)
          ((PrintWriter) (obj)).println("#LID " + super.language);
        for (Enumeration enumeration = words(); enumeration.hasMoreElements();) {
          String s2 = (String)enumeration.nextElement();
          if (external) {
            ((PrintWriter) (obj)).println(s2);
          } else {
            StringBuffer stringbuffer1 = new StringBuffer();
            int j = findWord(s2, true, stringbuffer1);
            ((PrintWriter) (obj)).println(s2 + "\t" + (char)j + stringbuffer1.toString());
          }
        }

        return;
      } while (true);
  }

  protected void parseHeaderLine(String s)
  throws FileFormatException {
    StringTokenizer stringtokenizer = new StringTokenizer(s);
    if (!stringtokenizer.hasMoreTokens())
      throw new FileFormatException(s);
    String s1 = stringtokenizer.nextToken();
    if (!s1.equalsIgnoreCase("#LID"))
      throw new FileFormatException(s);
    if (!stringtokenizer.hasMoreTokens())
      throw new FileFormatException(s);
    s1 = stringtokenizer.nextToken();
    super.language = Integer.valueOf(s1).intValue();
    switch (super.language) {
      case 1033:
        super.language = 24941;
        break;

      case 2057:
        super.language = 25202;
        break;

      case 1027:
        super.language = 29539;
        break;

      case 1029:
        super.language = 25459;
        break;

      case 1030:
        super.language = 25697;
        break;

      case 1043:
        super.language = 25717;
        break;

      case 1035:
        super.language = 26217;
        break;

      case 1036:
        super.language = 26226;
        break;

      case 1031:
        super.language = 26469;
        break;

      case 1038:
        super.language = 26741;
        break;

      case 1040:
        super.language = 26996;
        break;

      case 1044:
        super.language = 28258;
        break;

      case 2068:
        super.language = 28270;
        break;

      case 1045:
        super.language = 28780;
        break;

      case 1046:
        super.language = 28770;
        break;

      case 2070:
        super.language = 28783;
        break;

      case 1049:
        super.language = 29301;
        break;

      case 1034:
        super.language = 29552;
        break;

      case 1053:
        super.language = 29559;
        break;
    }
  }
}