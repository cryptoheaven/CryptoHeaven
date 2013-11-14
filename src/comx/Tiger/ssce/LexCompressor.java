/**
 * Copyright 2001-2013 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Corp. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Corp.
 */
package comx.Tiger.ssce;

import comx.Tiger.util.CharArray;
import comx.Tiger.util.Search;
import comx.Tiger.util.Sort;
import comx.Tiger.util.UniCharacter;
import java.io.*;
import java.util.Enumeration;
import java.util.Vector;

public class LexCompressor {

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

  public LexCompressor(String s, String s1, int i) throws IOException {
    lexFile = new RandomAccessFile(s, "rw");
    lexFile.writeInt(0x57000501);
    lexFile.writeInt(1);
    lexFile.writeInt(4);
    lexFile.writeInt(i);
    lexFile.writeInt(2);
    lexFile.writeInt(4);
    lexFile.writeInt(2);
    if (s1 != null) {
      loadSuffixes(s1);
    }
    saveSuffixes();
    lexFile.writeInt(5);
    lexFile.writeInt(0);
    segDataOffset = lexFile.getFilePointer();
    segIndex = new Vector();
  }

  public void compressFile(String s) throws IOException, WordException {
    SegIndexNode segindexnode = new SegIndexNode();
    DataInputStream datainputstream = new DataInputStream(new FileInputStream(s));
    char c = datainputstream.readChar();
    datainputstream.close();
    BufferedReader bufferedreader;
    if (c == '\uFEFF' || c == '\uFFFE') {
      bufferedreader = new BufferedReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(s), 32 * 1024), "Unicode"));
    } else {
      bufferedreader = new BufferedReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(s), 32 * 1024)));
    }
    Vector vector = new Vector();
    boolean flag = false;
    do {
      if (flag) {
        break;
      }
      String s1 = bufferedreader.readLine();
      if (s1 == null) {
        flag = true;
        bufferedreader.close();
      } else if (s1.length() != 0 && !s1.startsWith("#")) {
        if (s1.length() > 63) {
          throw new WordException(s1 + ": too long");
        }
        for (int i = 0; i < s1.length(); i++) {
          if (!UniCharacter.isPrintable(s1.charAt(i))) {
            throw new WordException(s1 + ": contains invalid character(s)");
          }
        }

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
    for (int j = 0; j < as.length; j++) {
      String s2 = as[j];
      if (debug) {
        System.out.println(s2);
      }
      stringbuffer.setLength(0);
      stringbuffer.append(s2);
      toSegId(stringbuffer.toString(), ac);
      if (cmpSegIds(ac, segindexnode.id) != 0) {
        if (segindexnode.id[0] != 0) {
          segindexnode.size = (int) lexFile.getFilePointer() - segindexnode.offset;
          try {
            addSegIndexNode(segindexnode);
          } catch (WordException wordexception1) {
            throw new WordException(stringbuffer.toString() + ": " + wordexception1);
          }
        }
        segindexnode = new SegIndexNode();
        toSegId(stringbuffer.toString(), segindexnode.id);
        segindexnode.offset = (int) lexFile.getFilePointer();
        segindexnode.size = 0;
        stringbuffer1.setLength(0);
      }
      byte byte0 = 0;
      for (int k = 3; k < stringbuffer1.length() && stringbuffer.charAt(k) == stringbuffer1.charAt(k); k++) {
        byte0++;
      }

      lexFile.writeByte(byte0);
      boolean flag1 = false;
      for (int l = 3 + byte0; l < stringbuffer.length(); l++) {
        int i1 = Search.binary(simpleSuffixes, stringbuffer.toString().substring(l));
        if (i1 >= 0) {
          lexFile.writeChar(2 + i1);
          l = stringbuffer.length();
          flag1 = true;
        }
        if (!flag1) {
          int j1 = Search.binary(extSuffixes, stringbuffer.toString().substring(l));
          if (j1 >= 0) {
            lexFile.writeChar(1);
            lexFile.writeChar((char) j1);
            l = stringbuffer.length();
            flag1 = true;
          }
        }
        if (!flag1) {
          lexFile.writeChar(stringbuffer.charAt(l));
        }
      }

      if (!flag1) {
        lexFile.writeChar(0);
      }
      StringBuffer stringbuffer2 = stringbuffer;
      stringbuffer = stringbuffer1;
      stringbuffer1 = stringbuffer2;
    }

    if (segindexnode.id[0] != 0) {
      segindexnode.size = (int) lexFile.getFilePointer() - segindexnode.offset;
      try {
        addSegIndexNode(segindexnode);
      } catch (WordException wordexception) {
        throw new WordException(stringbuffer.toString() + ": " + wordexception);
      }
    }
  }

  public void end() throws IOException {
    try {
      if (lexFile == null) {
        throw new UnsupportedException();
      }
      long l = lexFile.getFilePointer();
      lexFile.seek(segDataOffset - 4L);
      lexFile.writeInt((int) (l - segDataOffset));
      lexFile.seek(l);
      lexFile.writeInt(4);
      int j = 4 + segIndex.size() * 14;
      lexFile.writeInt(j);
      lexFile.writeInt(segIndex.size());
      SegIndexNode segindexnode;
      for (Enumeration enumeration = segIndex.elements(); enumeration.hasMoreElements(); lexFile.writeInt(segindexnode.size)) {
        segindexnode = (SegIndexNode) enumeration.nextElement();
        for (int k = 0; k < 3; k++) {
          if (k < CharArray.length(segindexnode.id)) {
            lexFile.writeChar(segindexnode.id[k]);
          } else {
            lexFile.writeChar(0);
          }
        }

        int i1 = (int) ((long) segindexnode.offset - segDataOffset);
        lexFile.writeInt(i1);
      }

      lexFile.close();
    } catch (UnsupportedException unsupportedexception) {
    }
  }

  public static void toSegId(String s, char ac[]) {
    int i = Math.min(s.length(), 3);
    s.getChars(0, i, ac, 0);
    if (i < 3) {
      ac[i] = '\0';
    }
  }

  private void addSegIndexNode(SegIndexNode segindexnode) throws WordException {
    int i = 0;
    int j = 0;
    do {
      if (j >= segIndex.size()) {
        break;
      }
      SegIndexNode segindexnode1 = (SegIndexNode) segIndex.elementAt(j);
      i = cmpSegIds(segindexnode.id, segindexnode1.id);
      if (i <= 0) {
        break;
      }
      j++;
    } while (true);
    if (i == 0 && segIndex.size() > 0) {
      throw new WordException(new String(segindexnode.id) + " out of sequence");
    } else {
      segIndex.insertElementAt(segindexnode, j);
      return;
    }
  }

  private int cmpSegIds(char ac[], char ac1[]) {
    return CharArray.compare(ac, ac1);
  }

  private void loadSuffixes(String s) throws IOException {
    DataInputStream datainputstream = new DataInputStream(new FileInputStream(s));
    char c = '\0';
    try {
      c = datainputstream.readChar();
    } catch (EOFException eofexception) {
    }
    datainputstream.close();
    BufferedReader bufferedreader;
    if (c == '\uFEFF' || c == '\uFFFE') {
      bufferedreader = new BufferedReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(s), 32 * 1024), "Unicode"));
    } else {
      bufferedreader = new BufferedReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(s), 32 * 1024)));
    }
    simpleSuffixes = new Vector();
    String s1;
    for (int i = 0; i < 30 && (s1 = bufferedreader.readLine()) != null; i++) {
      if (!s1.startsWith("#")) {
        simpleSuffixes.addElement(s1);
      }
    }

    extSuffixes = new Vector();
    String s2;
    for (int j = 0; j < 65535 && (s2 = bufferedreader.readLine()) != null; j++) {
      if (!s2.startsWith("#")) {
        extSuffixes.addElement(s2);
      }
    }

    bufferedreader.close();
  }

  private void saveSuffixes() throws IOException {
    lexFile.writeInt(3);
    int i = 0;
    for (Enumeration enumeration = simpleSuffixes.elements(); enumeration.hasMoreElements();) {
      String s = (String) enumeration.nextElement();
      i += s.length() * 2 + 2;
    }

    int j = 0;
    for (Enumeration enumeration1 = extSuffixes.elements(); enumeration1.hasMoreElements();) {
      String s1 = (String) enumeration1.nextElement();
      j += s1.length() * 2 + 2;
    }

    lexFile.writeInt(4 + i + 4 + j);
    lexFile.writeInt(i);
    for (Enumeration enumeration2 = simpleSuffixes.elements(); enumeration2.hasMoreElements(); lexFile.writeChar(0)) {
      lexFile.writeChars((String) enumeration2.nextElement());
    }

    lexFile.writeInt(j);
    for (Enumeration enumeration3 = extSuffixes.elements(); enumeration3.hasMoreElements(); lexFile.writeChar(0)) {
      lexFile.writeChars((String) enumeration3.nextElement());
    }

  }
}