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
// Source File Name:   FileTextLexicon.java
package comx.Tiger.ssce;

import java.io.*;
import java.util.*;

// Referenced classes of package com.wintertree.ssce:
//      StreamTextLexicon, FileExistsException, ParameterException, LexiconUpdateException, 
//      WordException, UnsupportedException, FileFormatException, MemTextLexicon, 
//      SpellingSession
public class FileTextLexicon extends StreamTextLexicon {

  protected String fileName;
  protected long lastModified;

  public FileTextLexicon(String s) throws IOException, FileFormatException, LexiconUpdateException {
    lastModified = 0L;
    open(s);
  }

  public FileTextLexicon(String s, int i) throws IOException, FileExistsException {
    super(i);
    File file = new File(s);
    if (file.canRead()) {
      throw new FileExistsException(s);
    } else {
      fileName = s;
      save();
      return;
    }
  }

  public void addWord(String s) throws LexiconUpdateException {
    try {
      addWord(s, 105, "");
    } catch (ParameterException parameterexception) {
      throw new LexiconUpdateException(parameterexception.toString());
    }
  }

  public void addWord(String s, int i) throws LexiconUpdateException, ParameterException {
    if (i != 101 && i != 105) {
      throw new ParameterException(i + " action requires other word");
    }
    if (super.external && i != 105) {
      throw new LexiconUpdateException("Action " + i + " can't be used with external-format text lexicons");
    } else {
      addWord(s, i, "");
      return;
    }
  }

  public void addWord(String s, int i, String s1) throws LexiconUpdateException, ParameterException {
    try {
      syncFile();
    } catch (Exception exception) {
      throw new LexiconUpdateException(exception.toString());
    }
    super.addWord(s, i, s1);
    try {
      save();
    } catch (IOException ioexception) {
      try {
        super.deleteWord(s);
      } catch (WordException wordexception) {
      }
      throw new LexiconUpdateException(ioexception.toString());
    }
  }

  public void deleteWord(String s) throws LexiconUpdateException, WordException {
    try {
      syncFile();
    } catch (Exception exception) {
      throw new LexiconUpdateException(exception.toString());
    }
    StringBuffer stringbuffer = new StringBuffer();
    int i = findWord(s, true, stringbuffer);
    super.deleteWord(s);
    try {
      save();
    } catch (IOException ioexception) {
      try {
        super.addWord(s, i, stringbuffer.toString());
      } catch (ParameterException parameterexception) {
      }
      throw new LexiconUpdateException(ioexception.toString());
    }
  }

  public boolean equals(Object obj) {
    if (obj instanceof FileTextLexicon) {
      FileTextLexicon filetextlexicon = (FileTextLexicon) obj;
      return fileName.equals(filetextlexicon.fileName) && super.equals(obj);
    } else {
      return false;
    }
  }

  public String getFileName() {
    return fileName;
  }

  public int hashCode() {
    return fileName.hashCode();
  }

  public static boolean isFileTextLexicon(String s) {
    try {
      FileTextLexicon filetextlexicon = new FileTextLexicon(s);
      if (filetextlexicon != null)
        return true;
    } catch (Exception exception) {
      return false;
    }
    return true;
  }

  protected void save() throws IOException {
    try {
      if (fileName == null) {
        throw new UnsupportedException();
      }
//      try
//      {
//        int i = 0xa7d1fa59;
//        char c = '\213';
//        char c1 = '\214';
//        char c2 = '\u0287';
//        char c3 = '\u03E7';
//        char c4 = '\227';
//        char c5 = '\u012B';
//        int j = SpellingSession.getOption(Integer.valueOf(32));
//        if ((j & 0xff) == 162)
//        {
//          int k = (j & 0x1f00) >> 8;
//          int i1 = (j & 0x1ffe000) >> 13;
//          Date date = new Date();
//          Date date1 = (new GregorianCalendar(2000, 0, 1)).getTime();
//          Date date2 = new Date(date1.getTime() + (long)i1 * 0x5265c00L);
//          long l3 = System.currentTimeMillis();
//          long l5 = (long)SpellingSession.getOption(Integer.valueOf(16)) * 1000L;
//          int j2 = j & 0x7fffe000;
//          int k2 = 0;
//          for (int i3 = 0; i3 < 32; i3++)
//          {
//            k2 += j2 & 1;
//            j2 >>= 1;
//          }
//
//          if (k2 != k)
//            throw new Exception();
//          if (date.after(date2))
//          {
//            if (l3 > l5 + 0xdbba0L)
//            {
//              String as[] = {
//                "\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252", "\305\326\301\314\325\301\324\311\317\316\240\320\305\322\311\317\304\240\305\330\320\311\322\305\304", "\323\345\356\364\362\371\240\323\360\345\354\354\351\356\347\255\303\350\345\343\353\345\362\240\305\356\347\351\356\345\240\305\366\341\354\365\341\364\351\357\356\240\314\351\343\345\356\363\345", "\303\357\360\371\362\351\347\350\364\240\250\343\251\240\262\260\260\263\240\327\351\356\364\345\362\364\362\345\345\240\323\357\346\364\367\341\362\345\240\311\356\343\256", "\324\350\341\356\353\240\371\357\365\240\346\357\362\240\345\366\341\354\365\341\364\351\356\347\240\323\345\356\364\362\371\240\312\341\366\341\240\323\304\313\256", "\331\357\365\362\240\263\260\255\344\341\371\240\345\366\341\354\365\341\364\351\357\356\240\354\351\343\345\356\363\345\240\350\341\363\240\356\357\367\240\345\370\360\351\362\345\344\256", "\324\357\240\357\362\344\345\362\254\240\343\341\354\354\240\261\255\270\260\260\255\263\264\260\255\270\270\260\263\240\250\261\255\266\261\263\255\270\262\265\255\266\262\267\261\251\254\240\357\362\240\363\345\345", "\367\367\367\256\367\351\356\364\345\362\364\362\345\345\255\363\357\346\364\367\341\362\345\256\343\357\355\257\344\345\366\257\363\363\343\345\257\352\341\366\341\363\344\353\256\350\364\355\354", "\346\357\362\240\355\357\362\345\240\351\356\346\357\362\355\341\364\351\357\356\256", "\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252\252"
//              };
//              for (int j3 = 0; j3 < as.length; j3++)
//              {
//                for (int i4 = 0; i4 < as[j3].length(); i4++)
//                  System.out.print((char)(as[j3].charAt(i4) & 0x7f));
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
//            for (int k3 = 0; k3 < as1.length; k3++)
//            {
//              for (int j4 = 0; j4 < as1[k3].length(); j4++)
//                System.out.print((char)(as1[k3].charAt(j4) & 0x7f));
//
//              System.out.println();
//            }
//
//            SpellingSession.setOption(Integer.valueOf(16), Integer.valueOf((int)(l3 / 1000L)));
//          }
//        } else
//        {
//          long l1 = j ^ i;
//          long l2 = 0L;
//          for (int j1 = 0; j1 < 32; j1++)
//          {
//            long l4 = l1 >> j1 & 1L;
//            l2 |= l4 << 31 - j1;
//          }
//
//          int k1 = (int)(l2 / 10000L);
//          if (k1 != c && k1 != c1)
//            throw new Exception();
//          int i2 = (int)(l2 % 10000L);
//          if (k1 == c && i2 < c2 || i2 > c3)
//            throw new Exception();
//          if (k1 == c1 && (i2 < c4 || i2 > c5))
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
      OutputStream fileoutputstream = null;
      try {
        fileoutputstream = new BufferedOutputStream(new FileOutputStream(fileName), 32*1024);
        super.save(fileoutputstream);
      } finally {
        if (fileoutputstream != null)
          fileoutputstream.close();
      }
      File file = new File(fileName);
      lastModified = file.lastModified();
    } catch (UnsupportedException unsupportedexception) {
    }
  }

  protected void open(String s) throws IOException, FileFormatException, LexiconUpdateException {
    fileName = s;
    InputStream fileinputstream = null;
    try {
      fileinputstream = new BufferedInputStream(new FileInputStream(s), 32*1024);
      load(fileinputstream);
    } finally {
      if (fileinputstream != null)
        fileinputstream.close();
    }
  }

  public String toString() {
    return getClass().getName() + '(' + fileName + ')';
  }

  protected void syncFile() throws IOException, FileFormatException, LexiconUpdateException {
    File file = new File(fileName);
    long l = file.lastModified();
    if (l > lastModified) {
      FileInputStream fileinputstream = null;
      try {
        fileinputstream = new FileInputStream(fileName);
        load(fileinputstream);
        lastModified = l;
      } finally {
        if (fileinputstream != null)
          fileinputstream.close();
      }
    }
  }
}
