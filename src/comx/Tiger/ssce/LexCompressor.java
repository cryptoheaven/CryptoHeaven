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
// Source File Name:   LexCompressor.java

package comx.Tiger.ssce;

import comx.Tiger.util.*;

import java.io.*;
import java.util.*;

// Referenced classes of package com.wintertree.ssce:
//      SegIndexNode, WordException, UnsupportedException, SpellingSession

public class LexCompressor
{

  public boolean debug;
  private Vector extSuffixes;
  private RandomAccessFile lexFile;
  private long segDataOffset;
  private Vector segIndex;
  private Vector simpleSuffixes;
  public static final int SEG_ID_LEN = 3;
  public static final int FILE_MAGIC = 0x57000501;
  public static final int SIZEOF_CHAR = 2;
  public static final int SIZEOF_INT = 4;
  public static final int LANGUAGE_ID_SECTION = 1;
  public static final int CHARSET_SECTION = 2;
  public static final int SUFFIX_TBL_1_SECTION = 3;
  public static final int SEGMENT_INDEX_1_SECTION = 4;
  public static final int SEGMENT_DATA_1_SECTION = 5;
  public static final int LATIN1_CHARSET = 1;
  public static final int UNICODE_CHARSET = 2;
  public static final int END_OF_WORD = 0;
  public static final int EXT_SUFFIX = 1;
  public static final int FIRST_SIMPLE_SUFFIX = 2;
  public static final int LAST_SIMPLE_SUFFIX = 31;
  public static final int MAX_SIMPLE_SUFFIX = 30;
  public static final int MAX_EXT_SUFFIX = 65535;
  private static final String COMMENT_CHAR = "#";

  public LexCompressor(String s, String s1, int i)
    throws IOException
  {
    lexFile = new RandomAccessFile(s, "rw");
    lexFile.writeInt(0x57000501);
    lexFile.writeInt(1);
    lexFile.writeInt(4);
    lexFile.writeInt(i);
    lexFile.writeInt(2);
    lexFile.writeInt(4);
    lexFile.writeInt(2);
    if (s1 != null)
      loadSuffixes(s1);
    saveSuffixes();
    lexFile.writeInt(5);
    lexFile.writeInt(0);
    segDataOffset = lexFile.getFilePointer();
    segIndex = new Vector();
  }

  public void compressFile(String s)
    throws IOException, WordException
  {
    SegIndexNode segindexnode = new SegIndexNode();
    DataInputStream datainputstream = new DataInputStream(new FileInputStream(s));
    char c = datainputstream.readChar();
    datainputstream.close();
    BufferedReader bufferedreader;
    if (c == '\uFEFF' || c == '\uFFFE')
      bufferedreader = new BufferedReader(new InputStreamReader(new FileInputStream(s), "Unicode"));
    else
      bufferedreader = new BufferedReader(new InputStreamReader(new FileInputStream(s)));
    Vector vector = new Vector();
    boolean flag = false;
    do
    {
      if (flag)
        break;
      String s1 = bufferedreader.readLine();
      if (s1 == null)
      {
        flag = true;
        bufferedreader.close();
      } else
      if (s1.length() != 0 && !s1.startsWith("#"))
      {
        if (s1.length() > 63)
          throw new WordException(s1 + ": too long");
        for (int i = 0; i < s1.length(); i++)
          if (!UniCharacter.isPrintable(s1.charAt(i)))
            throw new WordException(s1 + ": contains invalid character(s)");

        vector.addElement(s1);
      }
    } while (true);
    bufferedreader.close();
    String as[] = new String[vector.size()];
    vector.copyInto(as);
    vector = null;
    Sort.ascending(as);
    StringBuffer astringbuffer[] = new StringBuffer[2];
    astringbuffer[0] = new StringBuffer();
    astringbuffer[1] = new StringBuffer();
    StringBuffer stringbuffer = astringbuffer[0];
    StringBuffer stringbuffer1 = astringbuffer[1];
    char ac[] = new char[4];
    for (int j = 0; j < as.length; j++)
    {
      String s2 = as[j];
      if (debug)
        System.out.println(s2);
      stringbuffer.setLength(0);
      stringbuffer.append(s2);
      toSegId(stringbuffer.toString(), ac);
      if (cmpSegIds(ac, segindexnode.id) != 0)
      {
        if (segindexnode.id[0] != 0)
        {
          segindexnode.size = (int)lexFile.getFilePointer() - segindexnode.offset;
          try
          {
            addSegIndexNode(segindexnode);
          }
          catch (WordException wordexception1)
          {
            throw new WordException(stringbuffer.toString() + ": " + wordexception1);
          }
        }
        segindexnode = new SegIndexNode();
        toSegId(stringbuffer.toString(), segindexnode.id);
        segindexnode.offset = (int)lexFile.getFilePointer();
        segindexnode.size = 0;
        stringbuffer1.setLength(0);
      }
      byte byte0 = 0;
      for (int k = 3; k < stringbuffer1.length() && stringbuffer.charAt(k) == stringbuffer1.charAt(k); k++)
        byte0++;

      lexFile.writeByte(byte0);
      boolean flag1 = false;
      for (int l = 3 + byte0; l < stringbuffer.length(); l++)
      {
        int i1 = Search.binary(simpleSuffixes, stringbuffer.toString().substring(l));
        if (i1 >= 0)
        {
          lexFile.writeChar(2 + i1);
          l = stringbuffer.length();
          flag1 = true;
        }
        if (!flag1)
        {
          int j1 = Search.binary(extSuffixes, stringbuffer.toString().substring(l));
          if (j1 >= 0)
          {
            lexFile.writeChar(1);
            lexFile.writeChar((char)j1);
            l = stringbuffer.length();
            flag1 = true;
          }
        }
        if (!flag1)
          lexFile.writeChar(stringbuffer.charAt(l));
      }

      if (!flag1)
        lexFile.writeChar(0);
      StringBuffer stringbuffer2 = stringbuffer;
      stringbuffer = stringbuffer1;
      stringbuffer1 = stringbuffer2;
    }

    if (segindexnode.id[0] != 0)
    {
      segindexnode.size = (int)lexFile.getFilePointer() - segindexnode.offset;
      try
      {
        addSegIndexNode(segindexnode);
      }
      catch (WordException wordexception)
      {
        throw new WordException(stringbuffer.toString() + ": " + wordexception);
      }
    }
  }

  public void end()
    throws IOException
  {
    try
    {
      if (lexFile == null)
        throw new UnsupportedException();
//      try
//      {
//        int i = 0xa7d1fa59;
//        char c = '\213';
//        char c1 = '\214';
//        char c2 = '\u0287';
//        char c3 = '\u03E7';
//        char c4 = '\227';
//        char c5 = '\u012B';
//        int j1 = SpellingSession.getOption(Integer.valueOf(32));
//        if ((j1 & 0xff) == 162)
//        {
//          int k1 = (j1 & 0x1f00) >> 8;
//          int i2 = (j1 & 0x1ffe000) >> 13;
//          Date date = new Date();
//          Date date1 = (new GregorianCalendar(2000, 0, 1)).getTime();
//          Date date2 = new Date(date1.getTime() + (long)i2 * 0x5265c00L);
//          long l4 = System.currentTimeMillis();
//          long l6 = (long)SpellingSession.getOption(Integer.valueOf(16)) * 1000L;
//          int j3 = j1 & 0x7fffe000;
//          int k3 = 0;
//          for (int i4 = 0; i4 < 32; i4++)
//          {
//            k3 += j3 & 1;
//            j3 >>= 1;
//          }
//
//          if (k3 != k1)
//            throw new Exception();
//          if (date.after(date2))
//          {
//            if (l4 > l6 + 0xdbba0L)
//            {
//              String as[] = {
//                "\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252", "\305\326\301\314\325\301\324\311\317\316\240\320\305\322\311\317\304\240\305\330\320\311\322\305\304", "\323\345\356\364\362\371\240\323\360\345\354\354\351\356\347\255\303\350\345\343\353\345\362\240\305\356\347\351\356\345\240\305\366\341\354\365\341\364\351\357\356\240\314\351\343\345\356\363\345", "\303\357\360\371\362\351\347\350\364\240\250\343\251\240\262\260\260\263\240\327\351\356\364\345\362\364\362\345\345\240\323\357\346\364\367\341\362\345\240\311\356\343\256", "\324\350\341\356\353\240\371\357\365\240\346\357\362\240\345\366\341\354\365\341\364\351\356\347\240\323\345\356\364\362\371\240\312\341\366\341\240\323\304\313\256", "\331\357\365\362\240\263\260\255\344\341\371\240\345\366\341\354\365\341\364\351\357\356\240\354\351\343\345\356\363\345\240\350\341\363\240\356\357\367\240\345\370\360\351\362\345\344\256", "\324\357\240\357\362\344\345\362\254\240\343\341\354\354\240\261\255\270\260\260\255\263\264\260\255\270\270\260\263\240\250\261\255\266\261\263\255\270\262\265\255\266\262\267\261\251\254\240\357\362\240\363\345\345", "\367\367\367\256\367\351\356\364\345\362\364\362\345\345\255\363\357\346\364\367\341\362\345\256\343\357\355\257\344\345\366\257\363\363\343\345\257\352\341\366\341\363\344\353\256\350\364\355\354", "\346\357\362\240\355\357\362\345\240\351\356\346\357\362\355\341\364\351\357\356\256", "\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252"
//              };
//              for (int j4 = 0; j4 < as.length; j4++)
//              {
//                for (int i5 = 0; i5 < as[j4].length(); i5++)
//                  System.out.print((char)(as[j4].charAt(i5) & 0x7f));
//
//                System.out.println();
//              }
//
//              SpellingSession.setOption(Integer.valueOf(16), Integer.valueOf((int)(l4 / 1000L)));
//            }
//            throw new Exception();
//          }
//          if (l4 > l6 + 0xdbba0L)
//          {
//            String as1[] = {
//              "\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252", "\323\345\356\364\362\371\240\323\360\345\354\354\351\356\347\255\303\350\345\343\353\345\362\240\305\356\347\351\356\345\240\305\366\341\354\365\341\364\351\357\356\240\314\351\343\345\356\363\345", "\303\357\360\371\362\351\347\350\364\240\250\343\251\240\262\260\260\263\240\327\351\356\364\345\362\364\362\345\345\240\323\357\346\364\367\341\362\345\240\311\356\343\256", "\306\317\322\240\305\326\301\314\325\301\324\311\317\316\240\317\316\314\331\240\255\240\316\317\324\240\306\317\322\240\320\322\317\304\325\303\324\311\317\316\240\325\323\305", "\324\350\341\356\353\240\371\357\365\240\346\357\362\240\345\366\341\354\365\341\364\351\356\347\240\323\345\356\364\362\371\240\312\341\366\341\240\323\304\313\241", "\324\357\240\357\362\344\345\362\254\240\343\341\354\354\240\261\255\270\260\260\255\263\264\260\255\270\270\260\263\240\250\261\255\266\261\263\255\270\262\265\255\266\262\267\261\251\254\240\357\362\240\363\345\345", "\367\367\367\256\367\351\356\364\345\362\364\362\345\345\255\363\357\346\364\367\341\362\345\256\343\357\355\257\344\345\366\257\363\363\343\345\257\352\341\366\341\363\344\353\256\350\364\355\354", "\346\357\362\240\355\357\362\345\240\351\356\346\357\362\355\341\364\351\357\356\256", "\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252"
//            };
//            for (int k4 = 0; k4 < as1.length; k4++)
//            {
//              for (int j5 = 0; j5 < as1[k4].length(); j5++)
//                System.out.print((char)(as1[k4].charAt(j5) & 0x7f));
//
//              System.out.println();
//            }
//
//            SpellingSession.setOption(Integer.valueOf(16), Integer.valueOf((int)(l4 / 1000L)));
//          }
//        } else
//        {
//          long l2 = j1 ^ i;
//          long l3 = 0L;
//          for (int j2 = 0; j2 < 32; j2++)
//          {
//            long l5 = l2 >> j2 & 1L;
//            l3 |= l5 << 31 - j2;
//          }
//
//          int k2 = (int)(l3 / 10000L);
//          if (k2 != c && k2 != c1)
//            throw new Exception();
//          int i3 = (int)(l3 % 10000L);
//          if (k2 == c && i3 < c2 || i3 > c3)
//            throw new Exception();
//          if (k2 == c1 && (i3 < c4 || i3 > c5))
//            throw new Exception();
//        }
//      }
//      catch (Exception exception)
//      {
//        for (long l1 = System.currentTimeMillis(); System.currentTimeMillis() < l1 + 1000L;) {
//          try {
//            Thread.sleep(25);
//          } catch (Throwable t) {
//          }
//        }
//        throw new UnsupportedException();
//      }
      long l = lexFile.getFilePointer();
      lexFile.seek(segDataOffset - 4L);
      lexFile.writeInt((int)(l - segDataOffset));
      lexFile.seek(l);
      lexFile.writeInt(4);
      int j = 4 + segIndex.size() * 14;
      lexFile.writeInt(j);
      lexFile.writeInt(segIndex.size());
      SegIndexNode segindexnode;
      for (Enumeration enumeration = segIndex.elements(); enumeration.hasMoreElements(); lexFile.writeInt(segindexnode.size))
      {
        segindexnode = (SegIndexNode)enumeration.nextElement();
        for (int k = 0; k < 3; k++)
          if (k < CharArray.length(segindexnode.id))
            lexFile.writeChar(segindexnode.id[k]);
          else
            lexFile.writeChar(0);

        int i1 = (int)((long)segindexnode.offset - segDataOffset);
        lexFile.writeInt(i1);
      }

      lexFile.close();
    }
    catch (UnsupportedException unsupportedexception) { }
  }

  public static void toSegId(String s, char ac[])
  {
    int i = Math.min(s.length(), 3);
    s.getChars(0, i, ac, 0);
    if (i < 3)
      ac[i] = '\0';
  }

  private void addSegIndexNode(SegIndexNode segindexnode)
    throws WordException
  {
    int i = 0;
    int j = 0;
    do
    {
      if (j >= segIndex.size())
        break;
      SegIndexNode segindexnode1 = (SegIndexNode)segIndex.elementAt(j);
      i = cmpSegIds(segindexnode.id, segindexnode1.id);
      if (i <= 0)
        break;
      j++;
    } while (true);
    if (i == 0 && segIndex.size() > 0)
    {
      throw new WordException(new String(segindexnode.id) + " out of sequence");
    } else
    {
      segIndex.insertElementAt(segindexnode, j);
      return;
    }
  }

  private int cmpSegIds(char ac[], char ac1[])
  {
    return CharArray.compare(ac, ac1);
  }

  private void loadSuffixes(String s)
    throws IOException
  {
    DataInputStream datainputstream = new DataInputStream(new FileInputStream(s));
    char c = '\0';
    try
    {
      c = datainputstream.readChar();
    }
    catch (EOFException eofexception) { }
    datainputstream.close();
    BufferedReader bufferedreader;
    if (c == '\uFEFF' || c == '\uFFFE')
      bufferedreader = new BufferedReader(new InputStreamReader(new FileInputStream(s), "Unicode"));
    else
      bufferedreader = new BufferedReader(new InputStreamReader(new FileInputStream(s)));
    simpleSuffixes = new Vector();
    String s1;
    for (int i = 0; i < 30 && (s1 = bufferedreader.readLine()) != null; i++)
      if (!s1.startsWith("#"))
        simpleSuffixes.addElement(s1);

    extSuffixes = new Vector();
    String s2;
    for (int j = 0; j < 65535 && (s2 = bufferedreader.readLine()) != null; j++)
      if (!s2.startsWith("#"))
        extSuffixes.addElement(s2);

    bufferedreader.close();
  }

  private void saveSuffixes()
    throws IOException
  {
    lexFile.writeInt(3);
    int i = 0;
    for (Enumeration enumeration = simpleSuffixes.elements(); enumeration.hasMoreElements();)
    {
      String s = (String)enumeration.nextElement();
      i += s.length() * 2 + 2;
    }

    int j = 0;
    for (Enumeration enumeration1 = extSuffixes.elements(); enumeration1.hasMoreElements();)
    {
      String s1 = (String)enumeration1.nextElement();
      j += s1.length() * 2 + 2;
    }

    lexFile.writeInt(4 + i + 4 + j);
    lexFile.writeInt(i);
    for (Enumeration enumeration2 = simpleSuffixes.elements(); enumeration2.hasMoreElements(); lexFile.writeChar(0))
      lexFile.writeChars((String)enumeration2.nextElement());

    lexFile.writeInt(j);
    for (Enumeration enumeration3 = extSuffixes.elements(); enumeration3.hasMoreElements(); lexFile.writeChar(0))
      lexFile.writeChars((String)enumeration3.nextElement());

  }
}
