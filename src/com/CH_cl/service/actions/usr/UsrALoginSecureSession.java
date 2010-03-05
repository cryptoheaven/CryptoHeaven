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

package com.CH_cl.service.actions.usr;

import com.CH_cl.util.GlobalSubProperties;

import java.io.File;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import com.CH_cl.service.actions.*;
import com.CH_cl.service.cache.FetchedDataCache;

import com.CH_co.service.engine.CommonSessionContext;
import com.CH_co.service.msg.MessageAction;
import com.CH_co.service.msg.dataSets.usr.*;
import com.CH_co.service.records.*;

import com.CH_co.cryptx.*;
import com.CH_co.util.*;

import com.CH_co.trace.Trace;

/** 
 * <b>Copyright</b> &copy; 2001-2010
 * <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
 * CryptoHeaven Development Team.
 * </a><br>All rights reserved.<p> 
 *
 * @author  Marcin Kurzawa
 * @version 
 */
public class UsrALoginSecureSession extends ClientMessageAction {

  /** Creates new UsrALoginSecureSession */
  public UsrALoginSecureSession() {
  }

  /** The action handler performs all actions related to the received message (reply),
      and optionally returns a request Message.  If there is no request, null is returned.
  */
  public MessageAction runAction() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UsrALoginSecureSession.class, "runAction()");

    // reply syntax:
    // <keyId> <encPrivateKey> <encSessionKeys> <serverVersion> <serverRelease> <serverBuild>
    // public KeyRecord keyRecord;
    // public byte[] encSessionKey;

    Usr_LoginSecSess_Rp reply = (Usr_LoginSecSess_Rp) getMsgDataSet();
    FetchedDataCache cache = getFetchedDataCache();

    Long keyId = reply.keyId;
    BASymCipherBlock encPrivateKey = reply.encPrivateKey;
    BAAsyCipherBlock encSessionKeys = reply.encSessionKeys;

    BAEncodedPassword encodedPassword = cache.getEncodedPassword();
    String keyPropertyName = null;
    String keyPropertyFileName = null;

    try {
      RSAPrivateKey privateKey = null;

      // Try the cache to find the key -- for subsequent logins.
      if (encPrivateKey == null && keyId != null) {
        KeyRecord keyRec = cache.getKeyRecord(keyId);
        if (keyRec != null)
          privateKey = keyRec.getPrivateKey();
      }

      if (privateKey == null) {
        // if the encrypted private key did not come, maybe its a new account, check cache
        if ((encPrivateKey == null || encPrivateKey.toByteArray() == null) && cache.getNewUserPrivateKey() != null) {
          privateKey = cache.getNewUserPrivateKey();
        }
        // maybe the encrypted private key is in the file
        else {
          // try the properties to find the key
          keyPropertyName = "Enc"+RSAPrivateKey.OBJECT_NAME+"_"+keyId;
          GlobalSubProperties keyProperties = new GlobalSubProperties(GlobalSubProperties.PROPERTY_EXTENSION_KEYS);
          String property = keyProperties.getProperty(keyPropertyName);
          keyPropertyFileName = keyProperties.getPropertiesFullFileName();

          if (property != null && property.length() > 0) {
            encPrivateKey = new BASymCipherBlock(ArrayUtils.toByteArray(property));
            // migrate private key filename change, so store file location if the properties...
            addPathToLastPrivKeyPaths(keyPropertyFileName);
          }
          // also try the alternate paths to key file
          else {
            String[] paths = GlobalProperties.getProperty("PrivKeyFilePaths", "").split("[\\|]+");
            int pathsIndex = paths[0].equals("") ? 1 : 0; // ignore leading delimited blanks
            StringBuffer keyPropBuffer = new StringBuffer();
            while (paths.length > pathsIndex) {
              String path = paths[pathsIndex++];
              if (path != null && path.length() > 0) {
                keyProperties = new GlobalSubProperties(new File(path), GlobalSubProperties.PROPERTY_EXTENSION_KEYS);
                property = keyProperties.getProperty(keyPropertyName);
                keyPropBuffer.append(", ");
                keyPropBuffer.append(keyProperties.getPropertiesFullFileName());
                if (property != null && property.length() > 0) {
                  encPrivateKey = new BASymCipherBlock(ArrayUtils.toByteArray(property));
                  break;
                }
              }
            }
            keyPropertyFileName += keyPropBuffer.toString();
          }
        }
      }

      // As last resort, let user specify the location of the key
      if (privateKey == null && encPrivateKey == null) {
        javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
        fc.setDialogTitle("Open Private Key file:");
        fc.setSelectedFile(null);
        int retVal = fc.showOpenDialog(null);
        if (retVal == javax.swing.JFileChooser.APPROVE_OPTION) {
          java.io.File file = fc.getSelectedFile();
          keyPropertyFileName = file.getAbsolutePath();
          keyPropertyName = "Enc"+RSAPrivateKey.OBJECT_NAME+"_"+keyId;
          GlobalSubProperties keyProperties = new GlobalSubProperties(file, GlobalSubProperties.PROPERTY_EXTENSION_KEYS);
          String property = keyProperties.getProperty(keyPropertyName);

          if (property != null && property.length() > 0) {
            encPrivateKey = new BASymCipherBlock(ArrayUtils.toByteArray(property));
            // remember the filename for next time -- TRIM the list to at most 5 paths
            addPathToLastPrivKeyPaths(file.getAbsolutePath());
          }
        }
      }

      // if we already have it because we just created a new account, then skip the decryption
      if (privateKey == null) {
        // decrypt the private key using the password
        if (encPrivateKey != null) {

          SymmetricSmallBlockCipher symCipher = new SymmetricSmallBlockCipher(encodedPassword);

          // see if our encoded password will decrypt the encrypted private key
          BASymPlainBlock privateKeyBA = null;
          try {
            privateKeyBA = symCipher.blockDecrypt(encPrivateKey);
          } catch (Throwable t) {
            if (trace != null) trace.exception(UsrALoginSecureSession.class, 40, t);
            String messageText = "Invalid credentials for the specified account! \n Program will terminate!";
            String title = "Critical Login Error";
            MessageDialog.showErrorDialog(null, messageText, title, true);
            Misc.systemExit(-101);
          }
          byte[] privateKeyBytes = privateKeyBA.toByteArray();
          privateKeyBA.clearContent();

          // create a private key from its bytes
          privateKey = RSAPrivateKey.bytesToObject(privateKeyBytes);
          for (int i=0; i<privateKeyBytes.length; i++) {
            privateKeyBytes[i] = 0;
          }

        }
        else {
          String message = 
              "<html>Private Key to decrypt session keys is not available! " +
              "<p>" +
              "Your key property file appears to be missing or corrupted.  " +
              "Could not find property field " + keyPropertyName + " to load your encrypted private key. " +
              "The key property file scanned is: <br>" + keyPropertyFileName +
              "<p>" +
              "Program will terminate!";
          MessageDialog.showErrorDialog(null, message, "Critical Login Error", true);
          Misc.systemExit(-102);
        }
      }

      // Asymmetrically decrypt the session key.
      // For security reasons, the block does not contain a digest.
      BASymmetricKey symmetricSessionKeys = new BASymmetricKey(encSessionKeys.decrypt(privateKey, false));

      CommonSessionContext sessionContext = getCommonContext();
      sessionContext.serverBuild = reply.serverBuild;

      byte[] incomingKey = new byte[32];
      byte[] outgoingKey = new byte[32];
      byte[] keys = symmetricSessionKeys.toByteArray();
      System.arraycopy(keys, 0, incomingKey, 0, 32);
      System.arraycopy(keys, 32, outgoingKey, 0, 32);
      sessionContext.setKeyMaterial(new BASymmetricKey(outgoingKey), new BASymmetricKey(incomingKey));
      sessionContext.secureStreams(); // <-- This will release blocked streamers waiting for completion of login sequence.
    } catch (InvalidKeyException e) {
      if (trace != null) trace.exception(UsrALoginSecureSession.class, 50, e);
      throw new SecurityException("Could not instantiate a SymmetricBulkCipher or failed while securing streams.");
    } catch (NoSuchAlgorithmException e) {
      if (trace != null) trace.exception(UsrALoginSecureSession.class, 60, e);
      throw new SecurityException("Could not instantiate a default AsymmetricBlockCipher");
    }
/*
    } catch (DigestException e) {
      if (trace != null) trace.exception(UsrALoginSecureSession.class, 70, e);
      throw new SecurityException("Could not decrypt private key with the password");
    }
*/

    // all ok
    getClientContext().login(true);

    if (trace != null) trace.exit(UsrALoginSecureSession.class);
    return null;
  }

  public static void addPathToLastPrivKeyPaths(String addPath) {
    String pathList = GlobalProperties.getProperty("PrivKeyFilePaths", "");
    String[] paths = pathList.split("[\\|]+");
    if (ArrayUtils.find(paths, addPath) < 0) {
      StringBuffer pathListSB = new StringBuffer(addPath);
      int countAdded = 1;
      for (int i=0; i<paths.length; i++) {
        if (paths[i].length() > 0) {
          pathListSB.append("|");
          pathListSB.append(paths[i]);
          countAdded ++;
          if (countAdded >= 5)
            break;
        }
      }
      GlobalProperties.setProperty("PrivKeyFilePaths", pathListSB.toString());
      GlobalProperties.store();
    }
  }
}