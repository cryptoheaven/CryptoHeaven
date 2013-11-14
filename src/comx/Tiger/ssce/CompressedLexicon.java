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
import java.io.*;
import java.util.Vector;

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
      return fileName.equals(((CompressedLexicon) obj).toString());
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
        for (int k1 = 0; k1 < keySeg.id.length; k1++) {
          if ((i1 & 1 << k1) != 0) {
            keySeg.id[k1] = Character.toUpperCase(keySeg.id[k1]);
          } else {
            keySeg.id[k1] = Character.toLowerCase(keySeg.id[k1]);
          }
        }

        int j = Search.binary(segIndex, keySeg);
        if (j >= 0) {
          return 105;
        }
      }

      return 0;
    }
    if (flag) {
      int k = Search.binary(segIndex, keySeg);
      if (k < 0) {
        return 0;
      }
      try {
        loadSegment(k);
      } catch (Exception exception) {
        System.out.println("Exception: " + exception.toString());
        return 0;
      }
      scanSegment(segIndex[k], charSet, containsWordCatcher, simpleSuffixes, extSuffixes);
      return containsWordCatcher.IsFound() ? 105 : 0;
    }
    for (int j1 = 0; j1 < 8; j1++) {
      for (int l1 = 0; l1 < keySeg.id.length; l1++) {
        if ((j1 & 1 << l1) != 0) {
          keySeg.id[l1] = Character.toUpperCase(keySeg.id[l1]);
        } else {
          keySeg.id[l1] = Character.toLowerCase(keySeg.id[l1]);
        }
      }

      int l = Search.binary(segIndex, keySeg);
      if (l < 0) {
        continue;
      }
      try {
        loadSegment(l);
      } catch (Exception exception1) {
        System.out.println("Exception: " + exception1.toString());
        return 0;
      }
      scanSegment(segIndex[l], charSet, containsWordCatcher, simpleSuffixes, extSuffixes);
      if (containsWordCatcher.IsFound()) {
        return 105;
      }
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
      if (j1 < j) {
        continue;
      }
      try {
        loadSegment(k);
      } catch (Exception exception) {
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
    } catch (EOFException eofexception) {
      randomaccessfile.close();
      throw new FileFormatException(s + "No magic");
    }
    boolean flag = false;
    label0:
    do {
      int k;
      do {
        if (flag) {
          break label0;
        }
        try {
          k = randomaccessfile.readInt();
          break;
        } catch (EOFException eofexception1) {
          flag = true;
        }
      } while (true);
      int i1;
      try {
        i1 = randomaccessfile.readInt();
      } catch (EOFException eofexception2) {
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
          while (countSkipped1 < i1) {
            countSkipped1 += randomaccessfile.skipBytes(i1 - countSkipped1);
          }
          break;

        default:
          int countSkipped2 = 0;
          while (countSkipped2 < i1) {
            countSkipped2 += randomaccessfile.skipBytes(i1 - countSkipped2);
          }
          break;
      }
    } while (true);
    memoryAvailable = memoryBudget;
    accesses = 0;
    if (memoryBudget > 0) {
      for (int l = 0; l < segIndex.length; l++) {
        if (memoryBudget > 0 && segIndex[l].size > memoryBudget) {
          randomaccessfile.close();
          throw new ParameterException("Memory budget " + Integer.toString(memoryBudget) + " too small for lexicon " + s + "; should be at least " + Integer.toString(segIndex[l].size));
        }
      }

    }
    randomaccessfile.close();
  }

  protected void open(DataInputStream datainputstream) throws IOException, FileFormatException, UnsupportedException {
    try {
      int i = datainputstream.readInt();
      if (i != 0x57000501) {
        throw new FileFormatException("expected magic number 1459619073; read " + i);
      }
    } catch (EOFException eofexception) {
      throw new FileFormatException("No magic");
    }
    byte abyte0[] = null;
    boolean flag = false;
    label0:
    do {
      int j;
      do {
        if (flag) {
          break label0;
        }
        try {
          j = datainputstream.readInt();
          break;
        } catch (EOFException eofexception1) {
          flag = true;
        }
      } while (true);
      int l;
      try {
        l = datainputstream.readInt();
      } catch (EOFException eofexception2) {
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
          if (abyte0 != null) {
            throw new FileFormatException("Duplicate segment data section");
          }
          abyte0 = new byte[l];
          datainputstream.readFully(abyte0);
          break;

        default:
          int countSkipped = 0;
          while (countSkipped < 1) {
            countSkipped += datainputstream.skip(l);
          }
          break;
      }
    } while (true);
    if (null == abyte0 || null == segIndex) {
      throw new FileFormatException("Missing data or index section");
    }
    for (int k = 0; k < segIndex.length; k++) {
      segIndex[k].data = new byte[segIndex[k].size];
      System.arraycopy(abyte0, segIndex[k].offset, segIndex[k].data, 0, segIndex[k].data.length);
    }

  }

  private void loadSegIndexTbl(DataInputStream datainputstream) throws EOFException, IOException, FileFormatException {
    try {
      if (datainputstream == null) {
        throw new UnsupportedException();
      }
      int j = datainputstream.readInt();
      segIndex = new CLexSegment[j];
      for (int k = 0; k < j; k++) {
        CLexSegment clexsegment = new CLexSegment();
        int i1;
        for (i1 = 0; i1 < 3; i1++) {
          char c4;
          if (charSet == 1) {
            c4 = (char) (datainputstream.readByte() & 0xff);
          } else {
            c4 = datainputstream.readChar();
          }
          clexsegment.id[i1] = c4;
        }

        clexsegment.id[i1] = '\0';
        clexsegment.offset = datainputstream.readInt();
        clexsegment.data = null;
        clexsegment.size = datainputstream.readInt();
        clexsegment.lastUsed = 0;
        segIndex[k] = clexsegment;
      }

    } catch (UnsupportedException unsupportedexception) {
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
    if (clexsegment.size == 0) {
      return;
    }
    CLexSegment clexsegment1;
    for (; memoryBudget > 0 && memoryAvailable < clexsegment.size; memoryAvailable += clexsegment1.size) {
      clexsegment1 = null;
      for (int j = 0; j < segIndex.length; j++) {
        CLexSegment clexsegment2 = segIndex[j];
        if (clexsegment2.data != null && (clexsegment1 == null || clexsegment2.lastUsed < clexsegment1.lastUsed)) {
          clexsegment1 = clexsegment2;
        }
      }

      clexsegment1.data = null;
    }

    clexsegment.data = new byte[clexsegment.size];
    RandomAccessFile randomaccessfile = new RandomAccessFile(fileName, "r");
    randomaccessfile.seek(segDataOffset + (long) clexsegment.offset);
    if (randomaccessfile.read(clexsegment.data) != clexsegment.data.length) {
      randomaccessfile.close();
      throw new FileFormatException(fileName + ": Unexpected EOF");
    }
    randomaccessfile.close();
    clexsegment.lastUsed = accesses;
    if (memoryBudget > 0) {
      memoryAvailable -= clexsegment.size;
    }
  }

  private void loadSuffixTbl(DataInputStream datainputstream, Vector vector) throws IOException, EOFException, FileFormatException {
    int i = datainputstream.readInt();
    int j;
    if (charSet == 1) {
      j = i;
    } else {
      j = i / 2;
    }
    StringBuffer stringbuffer = new StringBuffer();
    for (int k = 0; k < j; k++) {
      char c;
      if (charSet == 1) {
        c = (char) (datainputstream.readByte() & 0xff);
      } else {
        c = datainputstream.readChar();
      }
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
        if (flag) {
          break;
        }
        char c;
        if (i == 1) {
          c = (char) (clexsegment.data[j++] & 0xff);
        } else {
          c = (char) ((clexsegment.data[j++] & 0xff) << 8);
          c |= (char) (clexsegment.data[j++] & 0xff);
        }
        if (c >= ' ') {
          stringbuffer.append(c);
        } else if (c >= '\002' && c < ' ') {
          stringbuffer.append(as[c - 2]);
          flag = true;
        } else if (c == 0) {
          flag = true;
        } else if (c == '\001') {
          char c1;
          if (i == 1) {
            c1 = (char) (clexsegment.data[j++] & 0xff);
          } else {
            c1 = (char) ((clexsegment.data[j++] & 0xff) << 8);
            c1 |= (char) (clexsegment.data[j++] & 0xff);
          }
          stringbuffer.append(as1[c1]);
          flag = true;
        }
      } while (true);
      if (!wordcatcher.catchWord(stringbuffer.toString())) {
        return;
      }
    }

  }
}