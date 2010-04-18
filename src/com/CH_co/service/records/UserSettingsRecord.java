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

package com.CH_co.service.records;

import java.io.*;
import java.util.*;

import com.CH_co.cryptx.*;
import com.CH_co.nanoxml.*;
import com.CH_co.trace.Trace;
import com.CH_co.util.*;

/**
 * <b>Copyright</b> &copy; 2001-2010
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p>
 *
 * Class Description:
 *
 *
 * Class Details:
 *
 *
 * <b>$Revision: 1.9 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class UserSettingsRecord extends Record {

  public Long userId;
  public Long pubKeyId;                 // key for encryption of encSymKey
  private BAAsyCipherBlock encSymKey;   // asymmetrically encrypted symmetric key
  private BASymCipherBulk encText;      // compressed and encrypted XML settings text

  /** unwrapped data */
  private BASymmetricKey symKey;
  private XMLElement xmlText;

  /** cached data */
  public Vector sigListV;
  public Integer sigDefault;
  public Boolean sigAddToNew;
  public Boolean sigAddToReFwd;
  public Integer awayMinutes = new Integer(DEFAULT__AWAY_MINUTES);

  public Properties spellingProps;

  public static int DEFAULT__AWAY_MINUTES = 15;

  /** Creates new UserSettingsRecord */
  public UserSettingsRecord() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UserSettingsRecord.class, "UserSettingsRecord()");
    if (trace != null) trace.exit(UserSettingsRecord.class);
  }

  public int getIcon() {
    return ImageNums.IMAGE_NONE;
  }

  public Long getId() {
    return userId;
  }

  public void setEncSymKey        (BAAsyCipherBlock encSymKey)      { this.encSymKey        = encSymKey;        }
  public void setEncText          (BASymCipherBulk encText)         { this.encText          = encText;          }
  public void setSymKey           (BASymmetricKey symKey)           { this.symKey           = symKey;           }
  public void setXmlText          (XMLElement xmlText)              { this.xmlText          = xmlText;          }

  public BAAsyCipherBlock getEncSymKey()        { return encSymKey;       }
  public BASymCipherBulk  getEncText()          { return encText;         }
  public BASymmetricKey   getSymKey()           { return symKey;          }
  public XMLElement       getXmlText()          { return xmlText;         }

  public String getDefaultSig() {
    String sigText = null;
    if (sigDefault != null && sigDefault.intValue() >= 0 && sigListV != null && sigListV.size() > sigDefault.intValue()) {
      String data[] = (String[]) sigListV.elementAt(sigDefault.intValue());
      sigText = data[2];
    }
    return sigText;
  }

  public String getDefaultSigType() {
    String sigType = null;
    if (sigDefault != null && sigDefault.intValue() >= 0 && sigListV != null && sigListV.size() > sigDefault.intValue()) {
      String data[] = (String[]) sigListV.elementAt(sigDefault.intValue());
      sigType = data[1];
    }
    return sigType;
  }

  /**
   * Seals the <code> symKey </code> to <code> encSymKey </code>
   * using the sealant object which is the user's public key.
   */
  public void seal(KeyRecord publicKey) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UserSettingsRecord.class, "seal(KeyRecord publicKey)");
    if (symKey == null)
      throw new IllegalArgumentException("Symmetric keys must be present before sealing can take place!");

    try {
      // encrypt key
      AsymmetricBlockCipher asyCipher = new AsymmetricBlockCipher();
      BAAsyCipherBlock tempEncSymKey = asyCipher.blockEncrypt(publicKey.plainPublicKey, symKey.toByteArray());
      // compress and encrypt XML data
      SymmetricBulkCipher symCipher = new SymmetricBulkCipher(symKey);
      String xml = xmlText.toString();
      byte[] compressedXML = Misc.compress(xml);
      BASymCipherBulk tempEncXML = new BASymCipherBulk(symCipher.bulkEncrypt(compressedXML, 0, compressedXML.length));
      // set values when all done
      encSymKey = tempEncSymKey;
      encText = tempEncXML;
      userId = publicKey.ownerUserId;
      pubKeyId = publicKey.keyId;
      super.seal();
    } catch (Throwable t) {
      if (trace != null) trace.exception(UserSettingsRecord.class, 100, t);
      throw new SecurityException(t.getMessage());
    }

    if (trace != null) trace.exit(UserSettingsRecord.class);
  }


  /**
   * Unseals the <code> encSymKey </code> into <code> symKey </code>
   * using the unSealant object which is the user's private key.
   */
  public void unSeal(KeyRecord privateKey, StringBuffer errorBuffer) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UserSettingsRecord.class, "unSeal(KeyRecord privateKey, StringBuffer errorBuffer)");
    if (!privateKey.keyId.equals(pubKeyId))
      throw new IllegalArgumentException("Specified private key record cannot decrypt these symmetric key!");

    try {
      // decrypt key
      AsymmetricBlockCipher asyCipher = new AsymmetricBlockCipher();
      RSAPrivateKey rsaPrivateKey = privateKey.getPrivateKey();
      byte[] encSymKeyBytes = encSymKey.toByteArray();
      byte[] tempKeyBytes = asyCipher.blockDecrypt(rsaPrivateKey, encSymKeyBytes).toByteArray();
      BASymmetricKey tempKey = new BASymmetricKey(tempKeyBytes);
      // decrypt and uncompress XML data
      SymmetricBulkCipher symCipher = new SymmetricBulkCipher(tempKey);
      BASymPlainBulk compressedXML = symCipher.bulkDecrypt(encText);
      XMLElement tempXML = new XMLElement();
      try {
        tempXML.parseString(Misc.decompressStr(compressedXML.toByteArray()));
      } catch (Throwable th1) {
        errorBuffer.append("Invalid user settings detected.  This may include default email signatures or spelling preferences.  Please adjust your settings.");
      }
      // set values when all done
      symKey = tempKey;
      xmlText = tempXML;
      super.unSeal();
      try {
        parseSettings(xmlText);
      } catch (Throwable th1) {
        if (errorBuffer.length() == 0)
          errorBuffer.append("Invalid user settings detected.  ");
        errorBuffer.append("This may include default email signatures or spelling preferences.  Please validate your settings.");
      }
    } catch (Throwable t) {
      if (trace != null) trace.exception(UserSettingsRecord.class, 100, t);
      throw new SecurityException(t.getMessage());
    }

    if (trace != null) trace.exit(UserSettingsRecord.class);
  }

  private void resetSettings() {
    // reset old settings
    sigListV = new Vector();
    sigDefault = new Integer(-1);
    sigAddToNew = Boolean.FALSE;
    sigAddToReFwd = Boolean.FALSE;
    awayMinutes = new Integer(DEFAULT__AWAY_MINUTES);
  }

  private void parseSettings(XMLElement xmlText) {
    resetSettings();

    // parse new settings
    // look for "signatures" element
    Vector childrenV = xmlText.getChildren();
    for (int c=0; c<childrenV.size(); c++) {
      XMLElement child = (XMLElement) childrenV.elementAt(c);
      String childName = child.getNameSafe();

      if (childName.equalsIgnoreCase("signatures")) {
        // don't want this branch to affect anything else, try-catch everything
        try {
          sigAddToNew = Boolean.valueOf(("" + child.getAttribute("sign_new")).equalsIgnoreCase("true"));
          sigAddToReFwd = Boolean.valueOf(("" + child.getAttribute("sign_re_fwd")).equalsIgnoreCase("true"));
          Vector sigsV = child.getChildren();
          if (sigsV != null) {
            for (int i=0; i<sigsV.size(); i++) {
              XMLElement sig = (XMLElement) sigsV.elementAt(i);
              if (sig.getNameSafe().equalsIgnoreCase("sig")) {
                String isDefaultSig = "" + sig.getAttribute("default");
                if (isDefaultSig.equalsIgnoreCase("true"))
                  sigDefault = new Integer(i);
                String[] data = new String[3];
                Vector sigElementsV = sig.getChildren();
                for (int k=0; k<sigElementsV.size(); k++) {
                  XMLElement element = (XMLElement) sigElementsV.elementAt(k);
                  String elementName = element.getNameSafe();
                  String elementValue = element.getContent();
                  if (elementName.equalsIgnoreCase("name"))
                    data[0] = elementValue;
                  else if (elementName.equalsIgnoreCase("type"))
                    data[1] = elementValue;
                  else if (elementName.equalsIgnoreCase("value"))
                    data[2] = elementValue;
                }
                if (sigListV == null) sigListV = new Vector();
                sigListV.addElement(data);
              }
            }
          }
        } catch (Throwable t) {
        }
      } else if (childName.equalsIgnoreCase("online-status")) {
        // don't want this branch to affect anything else, try-catch everything
        try {
          Vector statusV = child.getChildren();
          if (statusV != null) {
            for (int i=0; i<statusV.size(); i++) {
              XMLElement status = (XMLElement) statusV.elementAt(i);
              if (status.getNameSafe().equalsIgnoreCase("away")) {
                String minutes = "" + status.getAttribute("minutes");
                if (minutes != null) {
                  try {
                    awayMinutes = Integer.valueOf(minutes);
                  } catch (Throwable t) {
                  }
                }
              }
            }
          }
        } catch (Throwable t) {
        }
      } else if (childName.equalsIgnoreCase("spell-check")) {
        try {
          Vector childV = child.getChildren();
          if (childV != null) {
            for (int i=0; i<childV.size(); i++) {
              XMLElement ch = (XMLElement) childV.elementAt(i);
              if (ch.getNameSafe().equalsIgnoreCase("properties")) {
                String props = ch.getContent();
                Properties properties = new Properties();
                properties.load(new ByteArrayInputStream(props.getBytes()));
                spellingProps = properties;
              }
            }
          }
        } catch (Throwable t) {
        }
      }
    }
  }

  public XMLElement makeXMLData() {
    XMLElement xmlData = new XMLElement();
    xmlData.setNameSafe("settings");
    {
      XMLElement xml = new XMLElement();
      xml.setNameSafe("signatures");
      xml.setAttribute("sign_new", sigAddToNew != null ? ""+sigAddToNew : "false");
      xml.setAttribute("sign_re_fwd", sigAddToReFwd != null ? ""+sigAddToReFwd : "false");
      for (int i=0; sigListV!=null && i<sigListV.size(); i++) {
        String[] data = (String[]) sigListV.elementAt(i);
        XMLElement sig = new XMLElement();
        sig.setNameSafe("sig");
        if (sigDefault != null && sigDefault.intValue() == i)
          sig.setAttribute("default", Boolean.TRUE);
        XMLElement name = new XMLElement();
        XMLElement type = new XMLElement();
        XMLElement value = new XMLElement();
        name.setNameSafe("name");
        name.setContent(data[0]);
        type.setNameSafe("type");
        type.setContent(data[1]);
        value.setNameSafe("value");
        value.setContent(data[2]);
        sig.addChild(name);
        sig.addChild(type);
        sig.addChild(value);
        xml.addChild(sig);
      }
      xmlData.addChild(xml);
    }
    {
      XMLElement xml = new XMLElement();
      xmlData.addChild(xml);
      xml.setNameSafe("online-status");
      XMLElement away = new XMLElement();
      xml.addChild(away);
      away.setNameSafe("away");
      away.setAttribute("minutes", awayMinutes);
    }
    {
      if (spellingProps != null) {
        XMLElement xml = new XMLElement();
        xmlData.addChild(xml);
        xml.setNameSafe("spell-check");
        XMLElement properties = new XMLElement();
        xml.addChild(properties);
        properties.setNameSafe("properties");
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        try {
          spellingProps.store(byteOut, "Spelling Properties");
          byteOut.flush();
        } catch (Throwable t) {
        }
        String props = new String(byteOut.toByteArray());
        properties.setContent(props);
      }
    }
    return xmlData;
  }

  public void merge(Record updated) {
    if (updated instanceof UserSettingsRecord) {
      UserSettingsRecord record = (UserSettingsRecord) updated;

      if (record.userId           != null) userId           = record.userId;
      if (record.pubKeyId         != null) pubKeyId         = record.pubKeyId;
      if (record.encSymKey        != null) encSymKey        = record.encSymKey;
      if (record.encText          != null) encText          = record.encText;

      // un-sealed data
      if (record.symKey           != null) symKey           = record.symKey;
      if (record.xmlText          != null) xmlText          = record.xmlText;

      // cached data
      if (record.sigListV         != null) sigListV         = record.sigListV;
      if (record.sigDefault       != null) sigDefault       = record.sigDefault;
      if (record.sigAddToNew      != null) sigAddToNew      = record.sigAddToNew;
      if (record.sigAddToReFwd    != null) sigAddToReFwd    = record.sigAddToReFwd;
      if (record.awayMinutes      != null) awayMinutes      = record.awayMinutes;
    }
    else
      super.mergeError(updated);
  }

  public void setId(Long id) {
    userId = id;
  }

  public String toString() {
    return "[UserSettingsRecord"
      + ": userId="         + userId
      + ", pubKeyId="       + pubKeyId
      + ", encSymKey="      + encSymKey
      + ", encText="        + encText
      + ", cached data >> "
      + ", sigListV="       + Misc.objToStr(sigListV)
      + ", sigDefault="     + sigDefault
      + ", sigAddToNew="    + sigAddToNew
      + ", sigAddToReFwd="  + sigAddToReFwd
      + ", awayMinutes="    + awayMinutes
      + ", spellingProps"   + spellingProps
      + ", un-sealed data >> "
      + ", symKey="         + symKey
      + ", xmlText="        + xmlText
      + "]";
  }

}