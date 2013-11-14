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

import java.io.*;

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
      if (filetextlexicon != null) {
        return true;
      }
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
      OutputStream fileoutputstream = null;
      try {
        fileoutputstream = new BufferedOutputStream(new FileOutputStream(fileName), 32 * 1024);
        super.save(fileoutputstream);
      } finally {
        if (fileoutputstream != null) {
          fileoutputstream.close();
        }
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
      fileinputstream = new BufferedInputStream(new FileInputStream(s), 32 * 1024);
      load(fileinputstream);
    } finally {
      if (fileinputstream != null) {
        fileinputstream.close();
      }
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
        if (fileinputstream != null) {
          fileinputstream.close();
        }
      }
    }
  }
}