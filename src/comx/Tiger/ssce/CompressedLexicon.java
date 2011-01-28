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
// Source File Name:   CompressedLexicon.java

package comx.Tiger.ssce;

import comx.Tiger.util.CharArray;
import comx.Tiger.util.Search;

import java.io.*;
import java.util.*;

// Referenced classes of package com.wintertree.ssce:
//      ContainsWordCatcher, CLexSegment, SuggestWordCatcher, FileFormatException,
//      ParameterException, UnsupportedException, Lexicon, LexCompressor,
//      WordComparator, SuggestionSet, SpellingSession, WordCatcher

public class CompressedLexicon implements Lexicon {

  private int accesses;
  private int charSet;
  private String extSuffixes[];
  private String fileName;
  private int language;
  private int memoryAvailable;
  private int memoryBudget;
  private long segDataOffset;
  private CLexSegment segIndex[];
  private String simpleSuffixes[];
  private ContainsWordCatcher containsWordCatcher;
  private CLexSegment keySeg;

  public CompressedLexicon(String s) throws IOException, FileFormatException, ParameterException, UnsupportedException {
    this(s, 0);
  }

  public CompressedLexicon(String s, int i) throws IOException, FileFormatException, ParameterException, UnsupportedException {
    open(s, i);
    containsWordCatcher = new ContainsWordCatcher();
    keySeg = new CLexSegment();
  }

  public CompressedLexicon(InputStream inputstream) throws FileFormatException, IOException, UnsupportedException {
    fileName = inputstream.toString();
    open(new DataInputStream(inputstream));
    containsWordCatcher = new ContainsWordCatcher();
    keySeg = new CLexSegment();
  }

  public boolean equals(Object obj) {
    if (obj instanceof CompressedLexicon) {
      return fileName.equals(((CompressedLexicon)obj).toString());
    } else {
      return false;
    }
  }

  public int findWord(String s, boolean flag, StringBuffer stringbuffer) {
    LexCompressor.toSegId(s, keySeg.id);
    containsWordCatcher.setKeyWord(s);
    containsWordCatcher.setCaseSensitive(flag);
    containsWordCatcher.setFound(false);
    if (s.length() < 3) {
      if (flag) {
        int i = Search.binary(segIndex, keySeg);
        return i < 0 ? 0 : 105;
      }
      for (int i1 = 0; i1 < 8; i1++) {
        for (int k1 = 0; k1 < keySeg.id.length; k1++)
          if ((i1 & 1 << k1) != 0)
            keySeg.id[k1] = Character.toUpperCase(keySeg.id[k1]);
          else
            keySeg.id[k1] = Character.toLowerCase(keySeg.id[k1]);

        int j = Search.binary(segIndex, keySeg);
        if (j >= 0)
          return 105;
      }

      return 0;
    }
    if (flag) {
      int k = Search.binary(segIndex, keySeg);
      if (k < 0)
        return 0;
      try {
        loadSegment(k);
      }
      catch (Exception exception) {
        System.out.println("Exception: " + exception.toString());
        return 0;
      }
      scanSegment(segIndex[k], charSet, containsWordCatcher, simpleSuffixes, extSuffixes);
      return containsWordCatcher.IsFound() ? 105 : 0;
    }
    for (int j1 = 0; j1 < 8; j1++) {
      for (int l1 = 0; l1 < keySeg.id.length; l1++)
        if ((j1 & 1 << l1) != 0)
          keySeg.id[l1] = Character.toUpperCase(keySeg.id[l1]);
        else
          keySeg.id[l1] = Character.toLowerCase(keySeg.id[l1]);

      int l = Search.binary(segIndex, keySeg);
      if (l < 0)
        continue;
      try {
        loadSegment(l);
      }
      catch (Exception exception1) {
        System.out.println("Exception: " + exception1.toString());
        return 0;
      }
      scanSegment(segIndex[l], charSet, containsWordCatcher, simpleSuffixes, extSuffixes);
      if (containsWordCatcher.IsFound())
        return 105;
    }

    return 0;
  }

  public int getLanguage() {
    return language;
  }

  public int hashCode() {
    return fileName.hashCode();
  }

  public static boolean isCompressedLexicon(String s) {
    //Object obj = null;
    try {
      int i;
      RandomAccessFile randomaccessfile = new RandomAccessFile(s, "r");
      i = randomaccessfile.readInt();
      randomaccessfile.close();
      return i == 0x57000501;
    } catch (Exception exception) {
      //exception;
    }
    return false;
  }

  public void suggest(String s, int i, WordComparator wordcomparator, SuggestionSet suggestionset) {
    int j = 100 - i;
    j = Math.max(j, 0);
    for (int k = 0; k < segIndex.length; k++) {
      CLexSegment clexsegment = segIndex[k];
      if (CharArray.length(clexsegment.id) < 3) {
        String s1 = CharArray.toString(clexsegment.id);
        int l = wordcomparator.compare(s, s1);
        suggestionset.add(s1, l);
        continue;
      }
      int i1 = Math.min(3, s.length());
      String s3 = s.substring(0, i1);
      String s2 = CharArray.toString(clexsegment.id);
      int j1 = wordcomparator.compare(s3, s2);
      if (j1 < j)
        continue;
      try {
        loadSegment(k);
      }
      catch (Exception exception) {
        System.out.println("Exception: " + exception.toString());
        continue;
      }
      SuggestWordCatcher suggestwordcatcher = new SuggestWordCatcher(s, wordcomparator, suggestionset);
      scanSegment(segIndex[k], charSet, suggestwordcatcher, simpleSuffixes, extSuffixes);
    }

  }

  public String toString() {
    return getClass().getName() + '(' + fileName + ')';
  }

  protected void open(String s, int i) throws IOException, FileFormatException, ParameterException, UnsupportedException {
    fileName = s;
    memoryBudget = i;
    RandomAccessFile randomaccessfile = new RandomAccessFile(s, "r");
    try {
      int j = randomaccessfile.readInt();
      if (j != 0x57000501) {
        randomaccessfile.close();
        throw new FileFormatException(s + ": expected magic number " + 0x57000501 + "; read " + j);
      }
    }
    catch (EOFException eofexception) {
      randomaccessfile.close();
      throw new FileFormatException(s + "No magic");
    }
    boolean flag = false;
    label0:
      do {
        int k;
        do {
          if (flag)
            break label0;
          try {
            k = randomaccessfile.readInt();
            break;
          }
          catch (EOFException eofexception1) {
            flag = true;
          }
        } while (true);
        int i1;
        try {
          i1 = randomaccessfile.readInt();
        }
        catch (EOFException eofexception2) {
          randomaccessfile.close();
          throw new FileFormatException(s + " Unexpected EOF at " + randomaccessfile.getFilePointer());
        }
        switch (k) {
          case 3: // '\003'
            byte abyte0[] = new byte[i1];
            randomaccessfile.readFully(abyte0);
            Vector vector = new Vector();
            DataInputStream datainputstream = new DataInputStream(new ByteArrayInputStream(abyte0));
            loadSuffixTbl(datainputstream, vector);
            simpleSuffixes = new String[vector.size()];
            vector.copyInto(simpleSuffixes);
            vector.removeAllElements();
            loadSuffixTbl(datainputstream, vector);
            extSuffixes = new String[vector.size()];
            vector.copyInto(extSuffixes);
            break;

          case 4: // '\004'
            byte abyte1[] = new byte[i1];
            randomaccessfile.readFully(abyte1);
            loadSegIndexTbl(new DataInputStream(new ByteArrayInputStream(abyte1)));
            break;

          case 1: // '\001'
            language = randomaccessfile.readInt();
            break;

          case 2: // '\002'
            charSet = randomaccessfile.readInt();
            break;

          case 5: // '\005'
            segDataOffset = randomaccessfile.getFilePointer();
            int countSkipped1 = 0;
            while (countSkipped1 < i1) countSkipped1 += randomaccessfile.skipBytes(i1-countSkipped1);
            break;

          default:
            int countSkipped2 = 0;
            while (countSkipped2 < i1) countSkipped2 += randomaccessfile.skipBytes(i1-countSkipped2);
            break;
        }
      } while (true);
      memoryAvailable = memoryBudget;
      accesses = 0;
      if (memoryBudget > 0) {
        for (int l = 0; l < segIndex.length; l++)
          if (memoryBudget > 0 && segIndex[l].size > memoryBudget) {
            randomaccessfile.close();
            throw new ParameterException("Memory budget " + Integer.toString(memoryBudget) + " too small for lexicon " + s + "; should be at least " + Integer.toString(segIndex[l].size));
          }

      }
      randomaccessfile.close();
  }

  protected void open(DataInputStream datainputstream) throws IOException, FileFormatException, UnsupportedException {
    try {
      int i = datainputstream.readInt();
      if (i != 0x57000501)
        throw new FileFormatException("expected magic number 1459619073; read " + i);
    }
    catch (EOFException eofexception) {
      throw new FileFormatException("No magic");
    }
    byte abyte0[] = null;
    boolean flag = false;
    label0:
      do {
        int j;
        do {
          if (flag)
            break label0;
          try {
            j = datainputstream.readInt();
            break;
          }
          catch (EOFException eofexception1) {
            flag = true;
          }
        } while (true);
        int l;
        try {
          l = datainputstream.readInt();
        }
        catch (EOFException eofexception2) {
          boolean flag1 = true;
          throw new FileFormatException("Unexpected EOF");
        }
        switch (j) {
          case 3: // '\003'
            Vector vector = new Vector();
            loadSuffixTbl(datainputstream, vector);
            simpleSuffixes = new String[vector.size()];
            vector.copyInto(simpleSuffixes);
            vector.removeAllElements();
            loadSuffixTbl(datainputstream, vector);
            extSuffixes = new String[vector.size()];
            vector.copyInto(extSuffixes);
            break;

          case 4: // '\004'
            loadSegIndexTbl(datainputstream);
            break;

          case 1: // '\001'
            language = datainputstream.readInt();
            break;

          case 2: // '\002'
            charSet = datainputstream.readInt();
            break;

          case 5: // '\005'
            if (abyte0 != null)
              throw new FileFormatException("Duplicate segment data section");
            abyte0 = new byte[l];
            datainputstream.readFully(abyte0);
            break;

          default:
            int countSkipped = 0;
            while (countSkipped < 1)
              countSkipped += datainputstream.skip(l);
            break;
        }
      } while (true);
      if (null == abyte0 || null == segIndex)
        throw new FileFormatException("Missing data or index section");
      for (int k = 0; k < segIndex.length; k++) {
        segIndex[k].data = new byte[segIndex[k].size];
        System.arraycopy(abyte0, segIndex[k].offset, segIndex[k].data, 0, segIndex[k].data.length);
      }

  }

  private void loadSegIndexTbl(DataInputStream datainputstream) throws EOFException, IOException, FileFormatException {
    try {
      if (datainputstream == null)
        throw new UnsupportedException();
      //      try
      //      {
      //        int i = 0xa7d1fa59;
      //        char c = '\213';
      //        char c1 = '\214';
      //        char c2 = '\u0287';
      //        char c3 = '\u03E7';
      //        char c5 = '\227';
      //        char c6 = '\u012B';
      //        int j1 = SpellingSession.getOption(Integer.valueOf(32));
      //        if ((j1 & 0xff) == 162)
      //        {
      //          int k1 = (j1 & 0x1f00) >> 8;
      //          int i2 = (j1 & 0x1ffe000) >> 13;
      //          Date date = new Date();
      //          Date date1 = (new GregorianCalendar(2000, 0, 1)).getTime();
      //          Date date2 = new Date(date1.getTime() + (long)i2 * 0x5265c00L);
      //          long l3 = System.currentTimeMillis();
      //          long l5 = (long)SpellingSession.getOption(Integer.valueOf(16)) * 1000L;
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
      //            if (l3 > l5 + 0xdbba0L)
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
      //              SpellingSession.setOption(Integer.valueOf(16), Integer.valueOf((int)(l3 / 1000L)));
      //            }
      //            throw new Exception();
      //          }
      //          if (l3 > l5 + 0xdbba0L)
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
      //            SpellingSession.setOption(Integer.valueOf(16), Integer.valueOf((int)(l3 / 1000L)));
      //          }
      //        } else
      //        {
      //          long l1 = j1 ^ i;
      //          long l2 = 0L;
      //          for (int j2 = 0; j2 < 32; j2++)
      //          {
      //            long l4 = l1 >> j2 & 1L;
      //            l2 |= l4 << 31 - j2;
      //          }
      //
      //          int k2 = (int)(l2 / 10000L);
      //          if (k2 != c && k2 != c1)
      //            throw new Exception();
      //          int i3 = (int)(l2 % 10000L);
      //          if (k2 == c && i3 < c2 || i3 > c3)
      //            throw new Exception();
      //          if (k2 == c1 && (i3 < c5 || i3 > c6))
      //            throw new Exception();
      //        }
      //      }
      //      catch (Exception exception)
      //      {
      //        for (long l = System.currentTimeMillis(); System.currentTimeMillis() < l + 1000L;) {
      //          try {
      //            Thread.sleep(25);
      //          } catch (Throwable t) {
      //          }
      //        }
      //        throw new UnsupportedException();
      //      }
      int j = datainputstream.readInt();
      segIndex = new CLexSegment[j];
      for (int k = 0; k < j; k++) {
        CLexSegment clexsegment = new CLexSegment();
        int i1;
        for (i1 = 0; i1 < 3; i1++) {
          char c4;
          if (charSet == 1)
            c4 = (char)(datainputstream.readByte() & 0xff);
          else
            c4 = datainputstream.readChar();
          clexsegment.id[i1] = c4;
        }

        clexsegment.id[i1] = '\0';
        clexsegment.offset = datainputstream.readInt();
        clexsegment.data = null;
        clexsegment.size = datainputstream.readInt();
        clexsegment.lastUsed = 0;
        segIndex[k] = clexsegment;
      }

    }
    catch (UnsupportedException unsupportedexception) {
      segIndex = new CLexSegment[0];
    }
  }

  private void loadSegment(int i) throws IOException, FileFormatException {
    accesses++;
    CLexSegment clexsegment = segIndex[i];
    if (clexsegment.data != null) {
      clexsegment.lastUsed = accesses;
      return;
    }
    if (clexsegment.size == 0)
      return;
    CLexSegment clexsegment1;
    for (; memoryBudget > 0 && memoryAvailable < clexsegment.size; memoryAvailable += clexsegment1.size) {
      clexsegment1 = null;
      for (int j = 0; j < segIndex.length; j++) {
        CLexSegment clexsegment2 = segIndex[j];
        if (clexsegment2.data != null && (clexsegment1 == null || clexsegment2.lastUsed < clexsegment1.lastUsed))
          clexsegment1 = clexsegment2;
      }

      clexsegment1.data = null;
    }

    clexsegment.data = new byte[clexsegment.size];
    RandomAccessFile randomaccessfile = new RandomAccessFile(fileName, "r");
    randomaccessfile.seek(segDataOffset + (long)clexsegment.offset);
    if (randomaccessfile.read(clexsegment.data) != clexsegment.data.length) {
      randomaccessfile.close();
      throw new FileFormatException(fileName + ": Unexpected EOF");
    }
    randomaccessfile.close();
    clexsegment.lastUsed = accesses;
    if (memoryBudget > 0)
      memoryAvailable -= clexsegment.size;
  }

  private void loadSuffixTbl(DataInputStream datainputstream, Vector vector) throws IOException, EOFException, FileFormatException {
    int i = datainputstream.readInt();
    int j;
    if (charSet == 1)
      j = i;
    else
      j = i / 2;
    StringBuffer stringbuffer = new StringBuffer();
    for (int k = 0; k < j; k++) {
      char c;
      if (charSet == 1)
        c = (char)(datainputstream.readByte() & 0xff);
      else
        c = datainputstream.readChar();
      if (c == 0) {
        vector.addElement(stringbuffer.toString());
        stringbuffer.setLength(0);
      } else {
        stringbuffer.append(c);
      }
    }

  }

  void scanSegment(CLexSegment clexsegment, int i, WordCatcher wordcatcher, String as[], String as1[]) {
    StringBuffer stringbuffer = new StringBuffer(CharArray.toString(clexsegment.id));
    int j = 0;
    for (int k = clexsegment.data.length; j < k;) {
      boolean flag = false;
      stringbuffer.setLength(3 + clexsegment.data[j++]);
      do {
        if (flag)
          break;
        char c;
        if (i == 1) {
          c = (char)(clexsegment.data[j++] & 0xff);
        } else {
          c = (char)((clexsegment.data[j++] & 0xff) << 8);
          c |= (char)(clexsegment.data[j++] & 0xff);
        }
        if (c >= ' ')
          stringbuffer.append(c);
        else
          if (c >= '\002' && c < ' ') {
            stringbuffer.append(as[c - 2]);
            flag = true;
          } else
            if (c == 0)
              flag = true;
            else
              if (c == '\001') {
                char c1;
                if (i == 1) {
                  c1 = (char)(clexsegment.data[j++] & 0xff);
                } else {
                  c1 = (char)((clexsegment.data[j++] & 0xff) << 8);
                  c1 |= (char)(clexsegment.data[j++] & 0xff);
                }
                stringbuffer.append(as1[c1]);
                flag = true;
              }
      } while (true);
      if (!wordcatcher.catchWord(stringbuffer.toString()))
        return;
    }

  }
}