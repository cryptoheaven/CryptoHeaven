/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of CryptoHeaven
 * Corp. ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the license
 * agreement you entered into with CryptoHeaven Corp.
 */
package com.CH_gui.util;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringBufferInputStream;
import java.io.StringReader;
import java.util.ArrayList;

/**
 * Copyright 2001-2014 CryptoHeaven Corp. All Rights Reserved.
 *
 * @author Marcin Kurzawa
 */
public class HtmlSelection implements Transferable {

  private static ArrayList htmlFlavors = new ArrayList();

  static {
    try {
      htmlFlavors.add(new DataFlavor("text/html;class=java.lang.String"));
      htmlFlavors.add(new DataFlavor("text/html;class=java.io.Reader"));
      htmlFlavors.add(new DataFlavor("text/html;charset=unicode;class=java.io.InputStream"));
      htmlFlavors.add(new DataFlavor("text/plain;class=java.lang.String"));
      htmlFlavors.add(new DataFlavor("text/plain;class=java.io.Reader"));
      htmlFlavors.add(new DataFlavor("text/plain;charset=unicode;class=java.io.InputStream"));
    } catch (ClassNotFoundException ex) {
      ex.printStackTrace();
    }
  }
  private String html;
  private String plain;

  public HtmlSelection(String html, String plain) {
    this.html = html;
    this.plain = plain;
  }

  public DataFlavor[] getTransferDataFlavors() {
    return (DataFlavor[]) htmlFlavors.toArray(new DataFlavor[htmlFlavors.size()]);
  }

  public boolean isDataFlavorSupported(DataFlavor flavor) {
    return htmlFlavors.contains(flavor);
  }

  public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
    boolean isPlain = flavor.getMimeType().startsWith("text/plain") || flavor.getMimeType().startsWith("plain/text");
    if (String.class.equals(flavor.getRepresentationClass())) {
      return isPlain ? plain : html;
    } else if (Reader.class.equals(flavor.getRepresentationClass())) {
      return new StringReader(isPlain ? plain : html);
    } else if (InputStream.class.equals(flavor.getRepresentationClass())) {
      return new StringBufferInputStream(isPlain ? plain : html);
    }
    throw new UnsupportedFlavorException(flavor);
  }
}