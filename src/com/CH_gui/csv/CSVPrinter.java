/*
 * Write files in comma separated value format.
 * Copyright (C) 2001,2002 Stephen Ostermiller
 * http://ostermiller.org/contact.pl?regarding=Java+Utilities
 * Copyright (C) 2003 Pierre Dittgen <pierre dot dittgen at pass-tech dot fr>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * See COPYING.TXT for details.
 */

package com.CH_gui.csv;
import java.io.*;

/**
 * Print values as a comma separated list.
 * More information about this class is available from <a target="_top" href=
 * "http://ostermiller.org/utils/CSVLexer.html">ostermiller.org</a>.
 *
 * @author Stephen Ostermiller http://ostermiller.org/contact.pl?regarding=Java+Utilities
 * @author Pierre Dittgen <pierre dot dittgen at pass-tech dot fr>
 * @since ostermillerutils 1.00.00
 */
public class CSVPrinter implements CSVPrint {

  /**
   * Delimiter character written.
   *
   * @since ostermillerutils 1.02.18
   */
  protected char delimiterChar = ',';

  /**
   * Quoting character written.
   *
   * @since ostermillerutils 1.02.18
   */
  protected char quoteChar = '"';

  /**
   * The place that the values get written.
   *
   * @since ostermillerutils 1.00.00
   */
  protected PrintWriter out;

  /**
   * True iff we just began a new line.
   *
   * @since ostermillerutils 1.00.00
   */
  protected boolean newLine = true;

  /**
   * Character used to start comments. (Default is '#')
   *
   * @since ostermillerutils 1.00.00
   */
  protected char commentStart = '#';

  /**
   * Change this printer so that it uses a new delimiter.
   *
   * @param newDelimiter The new delimiter character to use.
   * @throws BadDelimeterException if the character cannot be used as a delimiter.
   *
   * @author Pierre Dittgen <pierre dot dittgen at pass-tech dot fr>
   * @since ostermillerutils 1.02.18
   */
  public void changeDelimiter(char newDelimiter) throws BadDelimeterException {
    if (newDelimiter == '\n' || newDelimiter == '\r' ||
    newDelimiter == delimiterChar || newDelimiter == quoteChar){
      throw new BadDelimeterException();
    }
    delimiterChar = newDelimiter;
  }

  /**
   * Change this printer so that it uses a new character for quoting.
   *
   * @param newQuote The new character to use for quoting.
   * @throws BadQuoteException if the character cannot be used as a quote.
   *
   * @author Pierre Dittgen <pierre dot dittgen at pass-tech dot fr>
   * @since ostermillerutils 1.02.18
   */
  public void changeQuote(char newQuote) throws BadQuoteException {
    if (newQuote == '\n' || newQuote == '\r' ||
    newQuote == delimiterChar || newQuote == quoteChar){
      throw new BadQuoteException();
    }
    quoteChar = newQuote;
  }

  /**
   * Create a printer that will print values to the given
   * stream.	 Character to byte conversion is done using
   * the default character encoding.	Comments will be
   * written using the default comment character '#'.
   *
   * @param out stream to which to print.
   *
   * @since ostermillerutils 1.00.00
   */
  public CSVPrinter(OutputStream out){
    this.out = new PrintWriter(out);
  }

  /**
   * Create a printer that will print values to the given
   * stream.	Comments will be
   * written using the default comment character '#'.
   *
   * @param out stream to which to print.
   *
   * @since ostermillerutils 1.00.00
   */
  public CSVPrinter(Writer out){
    if (out instanceof PrintWriter){
      this.out = (PrintWriter)out;
    } else {
      this.out = new PrintWriter(out);
    }
  }

  /**
   * Create a printer that will print values to the given
   * stream.	 Character to byte conversion is done using
   * the default character encoding.
   *
   * @param out stream to which to print.
   * @param commentStart Character used to start comments.
   *
   * @since ostermillerutils 1.00.00
   */
  public CSVPrinter(OutputStream out, char commentStart){
    this(out);
    this.commentStart = commentStart;
  }

  /**
   * Create a printer that will print values to the given
   * stream.
   *
   * @param out stream to which to print.
   * @param commentStart Character used to start comments.
   *
   * @since ostermillerutils 1.00.00
   */
  public CSVPrinter(Writer out, char commentStart){
    this(out);
    this.commentStart = commentStart;
  }

  /**
   * Print the string as the last value on the line.	The value
   * will be quoted if needed.
   *
   * @param value value to be outputted.
   *
   * @since ostermillerutils 1.00.00
   */
  public void println(String value){
    print(value);
    out.println();
    out.flush();
    newLine = true;
  }

  /**
   * Output a blank line.
   *
   * @since ostermillerutils 1.00.00
   */
  public void println(){
    out.println();
    out.flush();
    newLine = true;
  }

  /**
   * Print a single line of comma separated values.
   * The values will be quoted if needed.  Quotes and
   * newLine characters will be escaped.
   *
   * @param values values to be outputted.
   *
   * @since ostermillerutils 1.00.00
   */
  public void println(String[] values){
    for (int i=0; i<values.length; i++){
      print(values[i]);
    }
    out.println();
    out.flush();
    newLine = true;
  }

  /**
   * Print several lines of comma separated values.
   * The values will be quoted if needed.  Quotes and
   * newLine characters will be escaped.
   *
   * @param values values to be outputted.
   *
   * @since ostermillerutils 1.00.00
   */
  public void println(String[][] values){
    for (int i=0; i<values.length; i++){
      println(values[i]);
    }
    if (values.length == 0){
      out.println();
    }
    out.flush();
    newLine = true;
  }

  /**
   * Put a comment among the comma separated values.
   * Comments will always begin on a new line and occupy a
   * least one full line. The character specified to star
   * comments and a space will be inserted at the beginning of
   * each new line in the comment.
   *
   * @param comment the comment to output.
   *
   * @since ostermillerutils 1.00.00
   */
  public void printlnComment(String comment){
    if (!newLine){
      out.println();
    }
    out.print(commentStart);
    out.print(' ');
    for (int i=0; i<comment.length(); i++){
      char c = comment.charAt(i);
      switch (c){
        case '\r': {
          if (i+1 < comment.length() && comment.charAt(i+1) == '\n'){
            i++;
          }
        } //break intentionally excluded.
        case '\n': {
          out.println();
          out.print(commentStart);
          out.print(' ');
        } break;
        default: {
          out.print(c);
        } break;
      }
    }
    out.println();
    out.flush();
    newLine = true;
  }

  /**
   * Print the string as the next value on the line.	The value
   * will be quoted if needed.
   *
   * @param value value to be outputted.
   *
   * @since ostermillerutils 1.00.00
   */
  public void print(String value){
    boolean quote = false;
    if (value.length() > 0){
      char c = value.charAt(0);
      if (newLine && (c<'0' || (c>'9' && c<'A') || (c>'Z' && c<'a') || (c>'z'))){
        quote = true;
      }
      if (c==' ' || c=='\f' || c=='\t'){
        quote = true;
      }
      for (int i=0; i<value.length(); i++){
        c = value.charAt(i);
        if (c==quoteChar || c==delimiterChar || c=='\n' || c=='\r'){
          quote = true;
        }
      }
      if (c==' ' || c=='\f' || c=='\t'){
        quote = true;
      }
    } else if (newLine) {
      // always quote an empty token that is the firs
      // on the line, as it may be the only thing on the
      // line.  If it were not quoted in that case,
      // an empty line has no tokens.
      quote = true;
    }
    if (newLine){
      newLine = false;
    } else {
      out.print(delimiterChar);
    }
    if (quote){
      out.print(escapeAndQuote(value));
    } else {
      out.print(value);
    }
    out.flush();
  }

  /**
   * Enclose the value in quotes and escape the quote
   * and comma characters that are inside.
   *
   * @param value needs to be escaped and quoted
   * @return the value, escaped and quoted.
   *
   * @since ostermillerutils 1.00.00
   */
  private String escapeAndQuote(String value){
    int count = 2;
    for (int i=0; i<value.length(); i++){
      char c = value.charAt(i);
      switch(c){
        case '\n': case '\r': case '\\': {
          count ++;
        } break;
        default: {
          if (c == quoteChar){
            count++;
          }
        } break;
      }
    }
    StringBuffer sb = new StringBuffer(value.length() + count);
    sb.append(quoteChar);
    for (int i=0; i<value.length(); i++){
      char c = value.charAt(i);
      switch(c){
        case '\n': {
          sb.append("\\n");
        } break;
        case '\r': {
          sb.append("\\r");
        } break;
        case '\\': {
          sb.append("\\\\");
        } break;
        default: {
          if (c == quoteChar){
            sb.append("\\" + quoteChar);
          } else {
            sb.append(c);
          }
        }
      }
    }
    sb.append(quoteChar);
    return (sb.toString());
  }

  /**
   * Write some test data to the given file.
   *
   * @param args First argument is the file name.  System.out used if no filename given.
   *
   * @since ostermillerutils 1.00.00
   */
  private static void main(String[] args) {
    OutputStream out;
    try {
      if (args.length > 0){
        File f = new File(args[0]);
        if (!f.exists()){
          f.createNewFile();
          if (f.canWrite()){
            out = new FileOutputStream(f);
          } else {
            throw new IOException("Could not open " + args[0]);
          }
        } else {
          throw new IOException("File already exists: " + args[0]);
        }
      } else {
        out = System.out;
      }
      CSVPrinter p  = new CSVPrinter(out);
      p.print("unquoted");
      p.print("un\\quoted");
      p.print("escaped\"quote");
      p.print("escaped\"quote\\");
      p.println("comma,comma");
      p.print("!quoted");
      p.print("!unquoted");
      p.print(" quoted");
      p.print("quoted ");
      p.printlnComment("A comment.");
      p.print("one");
      p.print("");
      p.print("");
      p.print("");
      p.print("");
      p.printlnComment("Multi\nLine\rComment\r\nto test line breaks\r");
      p.println("two");
      p.printlnComment("Comment after explicit new line.");
      p.print("\nthree\nline\n");
      p.println("\ttab");
    } catch (IOException e){
      System.out.println(e.getMessage());
    }
  }
}