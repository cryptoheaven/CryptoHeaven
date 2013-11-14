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
import java.util.Enumeration;
import java.util.StringTokenizer;

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
    if (external && i != 105) {
      throw new ParameterException("Action " + i + " can't be used with external-format lexicons");
    }
    if (!external) {
      for (int j = 0; j < s.length(); j++) {
        if (s.charAt(j) == '\t') {
          throw new ParameterException(s + ": contains invalid character(s)");
        }
      }

    }
    super.addWord(s, i, s1);
  }

  public void load(InputStream inputstream) throws IOException, FileFormatException {
    byte abyte0[];
    String s;
    BufferedReader bufferedreader;
    String s1;
    if (inputstream == null) {
      throw new UnsupportedException();
    }
    abyte0 = null;
    boolean flag = false;
    do {
      if (flag) {
        break;
      }
      try {
        int j = inputstream.available();
        if (0 == j) {
          j = 1024;
        }
        byte abyte1[] = new byte[j];
        int k = inputstream.read(abyte1);
        if (k == -1) {
          flag = true;
        }
        if (k > 0) {
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
      } catch (Exception exception1) {
        flag = true;
      }
    } while (true);
    s = null;
    if (abyte0.length > 1 && (abyte0[0] == -2 && abyte0[1] == -1 || abyte0[0] == 0)) {
      s = "UnicodeBig";
    }
    ByteArrayInputStream bytearrayinputstream = new ByteArrayInputStream(abyte0);
    if (s == null) {
      bufferedreader = new BufferedReader(new InputStreamReader(bytearrayinputstream));
    } else {
      bufferedreader = new BufferedReader(new InputStreamReader(bytearrayinputstream, s));
    }
    s1 = null;
    try {
      s1 = bufferedreader.readLine();
    } catch (Exception exception2) {
    }
    if (s1 == null) {
      external = true;
      return;
    }
    try {
      if (s1.length() >= "\uFEFF".length() && s1.startsWith("\uFEFF")) {
        s1 = s1.substring("\uFEFF".length());
      }
      if (s1.startsWith("#LID")) {
        parseHeaderLine(s1);
        external = false;
      } else {
        external = true;
      }
      if (external) {
        bufferedreader.close();
        ByteArrayInputStream bytearrayinputstream1 = new ByteArrayInputStream(abyte0);
        if (s == null) {
          bufferedreader = new BufferedReader(new InputStreamReader(bytearrayinputstream1));
        } else {
          bufferedreader = new BufferedReader(new InputStreamReader(bytearrayinputstream1, s));
        }
      }
      while ((s1 = bufferedreader.readLine()) != null) {
        char c6 = 'i';
        String s3 = "";
        int i2 = s1.indexOf('\t');
        String s2;
        if (i2 >= 0) {
          s2 = s1.substring(0, i2);
          if (++i2 < s1.length()) {
            c6 = s1.charAt(i2);
          } else {
            throw new FileFormatException("Missing action in " + s1);
          }
          if (++i2 < s1.length()) {
            s3 = s1.substring(i2);
          }
        } else {
          s2 = s1;
        }
        try {
          super.addWord(s2, c6, s3);
        } catch (LexiconUpdateException lexiconupdateexception) {
          throw new FileFormatException("Can't add " + s1 + " to lexicon");
        } catch (ParameterException parameterexception) {
          throw new FileFormatException("Unrecognized action in " + s1);
        }
      }
    } catch (UnsupportedException unsupportedexception) {
    }
    return;
  }

  public void save(OutputStream outputstream) throws IOException {
    String s = null;
    Object obj = words();
    label0:
    do {
      if (((Enumeration) (obj)).hasMoreElements() && s == null) {
        String s1 = (String) ((Enumeration) (obj)).nextElement();
        StringBuffer stringbuffer = new StringBuffer();
        int i = 0;
        do {
          if (i >= s1.length()) {
            break;
          }
          if (s1.charAt(i) > '\377') {
            s = "UnicodeBig";
            break;
          }
          i++;
        } while (true);
        i = 0;
        do {
          if (i >= stringbuffer.length() || s != null) {
            continue label0;
          }
          if (stringbuffer.charAt(i) > '\377') {
            s = "UnicodeBig";
            continue label0;
          }
          i++;
        } while (true);
      }
      if (s == null) {
        obj = new PrintWriter(new OutputStreamWriter(outputstream), true);
      } else {
        obj = new PrintWriter(new OutputStreamWriter(outputstream, s), true);
      }
      if (!external) {
        ((PrintWriter) (obj)).println("#LID " + super.language);
      }
      for (Enumeration enumeration = words(); enumeration.hasMoreElements();) {
        String s2 = (String) enumeration.nextElement();
        if (external) {
          ((PrintWriter) (obj)).println(s2);
        } else {
          StringBuffer stringbuffer1 = new StringBuffer();
          int j = findWord(s2, true, stringbuffer1);
          ((PrintWriter) (obj)).println(s2 + "\t" + (char) j + stringbuffer1.toString());
        }
      }

      return;
    } while (true);
  }

  protected void parseHeaderLine(String s) throws FileFormatException {
    StringTokenizer stringtokenizer = new StringTokenizer(s);
    if (!stringtokenizer.hasMoreTokens()) {
      throw new FileFormatException(s);
    }
    String s1 = stringtokenizer.nextToken();
    if (!s1.equalsIgnoreCase("#LID")) {
      throw new FileFormatException(s);
    }
    if (!stringtokenizer.hasMoreTokens()) {
      throw new FileFormatException(s);
    }
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