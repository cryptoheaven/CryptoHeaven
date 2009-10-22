/*
 * Copyright 2001-2009 by CryptoHeaven Development Team,
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

import com.CH_cl.service.ops.DownloadUtilities;
import java.awt.*;
import javax.swing.*;
import java.io.*;

import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import java.security.DigestInputStream;
import java.security.DigestOutputStream;

import java.sql.Timestamp;
import java.util.Arrays;

import com.CH_co.trace.Trace;
import com.CH_co.io.*;
import com.CH_co.util.*;
import com.CH_co.monitor.ProgMonitor;

import com.CH_co.cryptx.*;
import java.util.Random;

/**
 * <b>Copyright</b> &copy; 2001-2009
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
 * <b>$Revision: 1.29 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class FileDataRecord extends Record {

  public Long fileId;
  private BASymCipherBulk encOrigDataDigest;    // BADigestBlock symmetrically encrypted
  private BASymCipherBulk encSignedOrigDigest;  // BAAsyCipherBlock symmetrically encrypted
  private BASymCipherBulk encEncDataDigest;     // BADigestBlock symmetrically encrypted
  private Long signingKeyId;
  public Timestamp fileCreated;
  public Timestamp fileUpdated;
  private Long encSize;
  public Long recordSize;

  // If encrypted file is specified it will be sent, if not, the plain file will be encrypted before sending
  private File encDataFile;
  private File plainDataFile;
  public Boolean fileDownloaded; // mark if the file was downloaded or already existing
  private boolean autoRemovePlainFile = false; // auto deletion of plain file on finalization

  // unwrapped variables
  private BADigestBlock origDataDigest;
  private BAAsyCipherBlock signedOrigDigest;
  private BADigestBlock encDataDigest;

  // flag to determine if digest was verified OK
  private boolean isVerifiedPlainDigest;

  // Database will read serialized file (by FileUtils.serializeFile) from this stream.
  public InputStream fileSource;

  // Database will write serialized file to this stream.
  public OutputStream fileDest; // either DataOutputStream2 or FileOutputStream

  public static final String TEMP_ENCRYPTED_FILE_PREFIX = "ch-tmp-enc-";
  public static final String TEMP_PLAIN_FILE_PREFIX = "ch-tmp-pln-";

  /** Creates new FileDataRecord */
  public FileDataRecord() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileDataRecord.class, "FileDataRecord()");
    if (trace != null) trace.exit(FileDataRecord.class);
  }

  public Long getId() {
    return fileId;
  }

  public Icon getIcon() {
    return null;
  }

  public void setEncOrigDataDigest  (BASymCipherBulk  encOrigDataDigest   ) { this.encOrigDataDigest    = encOrigDataDigest;  }
  public void setEncSignedOrigDigest(BASymCipherBulk  encSignedOrigDigest ) { this.encSignedOrigDigest  = encSignedOrigDigest;}
  public void setEncEncDataDigest   (BASymCipherBulk  encEncDataDigest    ) { this.encEncDataDigest     = encEncDataDigest;   }
//  public void setOrigDataDigest     (BADigestBlock    origDataDigest      ) { this.origDataDigest       = origDataDigest;     }
//  public void setSignedOrigDigest   (BAAsyCipherBlock signedOrigDigest    ) { this.signedOrigDigest     = signedOrigDigest;   }
//  public void setEncDataDigest      (BADigestBlock    encDataDigest       ) { this.encDataDigest        = encDataDigest;      }
//  public void setSignedEncDigest    (BAAsyCipherBlock signedEncDigest     ) { this.signedEncDigest      = signedEncDigest;    }
  public void setEncSize            (Long             encSize             ) { this.encSize              = encSize;            }
  public void setPlainDataFile      (File             plainDataFile       ) { this.plainDataFile        = plainDataFile;      }
  public void setEncDataFile        (File             encDataFile         ) { this.encDataFile          = encDataFile;        }
  public void setSigningKeyId       (Long             signingKeyId        ) { this.signingKeyId         = signingKeyId;       }

  public BASymCipherBulk    getEncOrigDataDigest()    { return encOrigDataDigest;   }
  public BASymCipherBulk    getEncSignedOrigDigest()  { return encSignedOrigDigest; }
  public BASymCipherBulk    getEncEncDataDigest()     { return encEncDataDigest;    }
  public BADigestBlock      getOrigDataDigest()       { return origDataDigest;      }
  public BAAsyCipherBlock   getSignedOrigDigest()     { return signedOrigDigest;    }
  public BADigestBlock      getEncDataDigest()        { return encDataDigest;       }
  public Long               getEncSize()              { return encSize;             }
  public File               getPlainDataFile()        { return plainDataFile;       }
  public File               getEncDataFile()          { return encDataFile;         }
  public Long               getSigningKeyId()         { return signingKeyId;        }

  public boolean isVerifiedPlainDigest() {
    return isVerifiedPlainDigest;
  }


  /**
   * Seals the <code> plainDataFile </code> into
   * <code> origDataDigest, signedOrigDigest, encDataDigest, signedEncDigest, encDataFile, encSize </code> .
   * using the sealant object which are <code> signingKeyRecord, symmetricKey </code> .
   * Hash of the data is distributed privately in encrypted form between trusted individuals,
   * its not published publically, it need not be long.
   * @param signingKeyRecord the asymmetric key used to produce signed digests.
   * @param symmetricKey key matreial used for symmetric encryption of the file.
   */
  public void seal(KeyRecord signingKeyRecord, BASymmetricKey symmetricKey, ProgMonitor progressMonitor) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileDataRecord.class, "seal()");

    int oldPriority = Thread.currentThread().getPriority();
    Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

    super.seal();

    File tempFile = null;
    FileOutputStream tempFileOut = null;

    try {

      long plainDataFileLength = plainDataFile.length();
      FileInputStream fileIn = new FileInputStream(plainDataFile);
      //SpeedLimitedInputStream speedIn = new SpeedLimitedInputStream(fileIn, SpeedLimiter.DEFAULT_THROUGHPUT*2, false);
      BufferedInputStream bufFileIn = new BufferedInputStream(fileIn, 1024*32);

      // progress interruptible stream
      InterruptibleInputStream interIn = new InterruptibleInputStream(bufFileIn);
      if (progressMonitor != null) progressMonitor.setInterrupt(interIn);

      DigestInputStream dFileIn = new DigestInputStream(interIn, new SHA256());
      //DigestInputStream dFileIn = new DigestInputStream(interIn, MessageDigest.getInstance("SHA-1"));

      // create a temporary file for the encrypted data
      tempFile = File.createTempFile(TEMP_ENCRYPTED_FILE_PREFIX, null);
      tempFileOut = new FileOutputStream(tempFile);
      BufferedOutputStream bufFileOut = new BufferedOutputStream(tempFileOut, 1024*32);
      DigestOutputStream dFileOut = new DigestOutputStream(bufFileOut, new SHA256());
      //DigestOutputStream dFileOut = new DigestOutputStream(bufFileOut, MessageDigest.getInstance("SHA-1"));
      BlockCipherOutputStream cipherOut = new BlockCipherOutputStream(dFileOut, symmetricKey);
      GZIPOutputStream gzipOut = new GZIPOutputStream(cipherOut);

      if (progressMonitor != null) {
        progressMonitor.setCurrentStatus("Compressing and Encrypting File ...");
        progressMonitor.setFileNameSource(getPlainDataFile().getAbsolutePath());
        progressMonitor.setFileNameDestination(tempFile.getAbsolutePath());
        progressMonitor.setTransferSize(plainDataFileLength);
        progressMonitor.nextTask();
      }

      // move data from the digest input stream to GZIP file output stream
      FileUtils.moveDataEOF(dFileIn, gzipOut, progressMonitor); 
      // FileUtils.moveData(new DataInputStream(dFileIn), gzipOut, plainDataFileLength, progressMonitor);

      if (progressMonitor != null) {
        progressMonitor.setInterrupt(null);
        progressMonitor.doneTransfer();
      }

      dFileIn.close();

      gzipOut.finish();
      gzipOut.flush();
      gzipOut.close();

      // create the original data digest
      origDataDigest = new BADigestBlock(dFileIn.getMessageDigest().digest());
      if (trace != null) trace.data(30, "origDataDigest", ArrayUtils.toString(origDataDigest.toByteArray()));
      // sign the original data digest
      if (progressMonitor != null) progressMonitor.setCurrentStatus("Signing Encrypted File ...");
      // random filling will cause even the same 'origDataDigest' to look different in 'signedOrigDigest' form!
      signedOrigDigest = new BAAsyPlainBlock(origDataDigest).signBlock(signingKeyRecord.getPrivateKey());
      if (trace != null) trace.data(40, "signedOrigDigest", ArrayUtils.toString(signedOrigDigest.toByteArray()));

      // create the encrypted data digest
      encDataDigest = new BADigestBlock(dFileOut.getMessageDigest().digest());
      if (trace != null) trace.data(50, "encDataDigest", ArrayUtils.toString(encDataDigest.toByteArray()));

      // set the encrypted versions of digests and signed digests
      SymmetricBulkCipher symCipher = new SymmetricBulkCipher(symmetricKey);
      encOrigDataDigest = symCipher.bulkEncrypt(origDataDigest);
      encSignedOrigDigest = symCipher.bulkEncrypt(signedOrigDigest);
      encEncDataDigest = symCipher.bulkEncrypt(encDataDigest);

      if (progressMonitor != null) progressMonitor.setCurrentStatus("Signing Encrypted File ... signed.");

      signingKeyId = signingKeyRecord.keyId;
      encSize = new Long(tempFile.length());

      // Remember the newly created encrypted file.
      encDataFile = tempFile;

    } catch (Throwable t) {
      if (trace != null) trace.exception(FileDataRecord.class, 100, t);

      // update the job status to KILLED
      if (progressMonitor != null) progressMonitor.jobKilled();

      if (progressMonitor != null && !progressMonitor.isCancelled()) {
        String inFileName = "unknown";
        if (plainDataFile != null)
          inFileName = plainDataFile.getAbsolutePath();

        String outFileName = "unknown";
        if (tempFile != null)
          outFileName = tempFile.getAbsolutePath();

        String msg = "Exception occurred while encrypting the file " + inFileName + "  The destination file " + outFileName + " was not completely written.  This error is not recoverable, the output file will be erased.  Please check destination folder for sufficient free space and write access permissions.  Exception message is: " + t.getMessage();
        String title = "Error Uploading File";
        MessageDialog.showErrorDialog(null, msg, title, false);
      }

      // clean up temporary file
      try {
        if (tempFileOut != null)
          tempFileOut.close();
      } catch (Throwable th) { }
      try {
        if (tempFile != null)
          CleanupAgent.wipeOrDelete(tempFile);
      } catch (Throwable th) { }

      throw new IllegalStateException(t.getMessage());
    } finally {
      Thread.currentThread().setPriority(oldPriority);
    }

    if (trace != null) trace.exit(FileDataRecord.class);
  }


  /**
   * Unseals the <code> encDataFile </code> into <code> plainDataFile </code>
   * using the unSealant object which are <code> verifyingKeyRecord, symmetricKey </code> .
   * Hash of the data is distributed privately in encrypted form between trusted individuals,
   * its not published publically, it need not be long.
   * @param verifyingKeyRecord Key Record used to verify the signed digests, if null record will not be verified, only uncrypted!
   * @param symmetricKey key used to symmetricaly decrypt the encrypted file data
   * @param destinationDirectory Directory of the plain file, if null then temporary file will be assigned (destinationFileName is irrelevant in that case).
   */
  public void unSeal(KeyRecord verifyingKeyRecord, BASymmetricKey symmetricKey,
                     File destinationDirectory, String destinationFileName,
                     ProgMonitor progressMonitor, Long originalSize)
  {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileDataRecord.class, "unSeal(KeyRecord verifyingKeyRecord, BASymmetricKey symmetricKey, File destinationDirectory, String destinationFileName, ProgMonitor progressMonitor, Long originalSize)");
    if (trace != null) trace.args(verifyingKeyRecord, symmetricKey, destinationDirectory, destinationFileName, progressMonitor, originalSize);


    int oldPriority = Thread.currentThread().getPriority();
    Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

    super.unSeal();

    File destinationFile = null;
    boolean destinationFileCreated = false;
    FileInputStream encFileIn = null;
    FileOutputStream fileOut = null;

    try {

      if (encDataFile.length() != encSize.longValue()) {
        long len1 = encDataFile.length();
        long len2 = encSize.longValue();
        throw new IllegalArgumentException("Specified encrypted file length ("+len1+" bytes) does not match the actual file length ("+len2+" bytes).  (File ID " + fileId + ")");
      }
      if (verifyingKeyRecord != null && !verifyingKeyRecord.keyId.equals(signingKeyId)) {
        throw new IllegalArgumentException("Wrong verifying key specified.");
      }

      // create a temporary file for plain data
      if (destinationDirectory != null) {
        destinationFile = new File(destinationDirectory, destinationFileName);
        // If file goes to temp dir, make sure its unique name and added to cleanup
        if (destinationDirectory.equals(DownloadUtilities.getDefaultTempDir())) {
          while (destinationFile.exists()) {
            Random rnd = new Random();
            int r = rnd.nextInt(99999);
            destinationFile = new File(destinationDirectory, r+"-"+destinationFileName);
          }
          GlobalProperties.addTempFileToCleanup(destinationFile);
        }
        if (destinationFile.exists()) {
          ConfirmFileReplaceDialog d = null;
          if (!MiscGui.isAllGUIsuppressed())
            d = new ConfirmFileReplaceDialog(destinationFile, originalSize, this);
          // if GUI is suppressed, then default is to REPLACE the file.
          if (d != null) {
            if (d.isRename())
              destinationFile = d.getRenamdFile();
            else if (!d.isReplace()) {
              // File already exists so mark the link to it and
              plainDataFile = destinationFile;
              destinationFile = null;
            }
          }
        }
      } else {
        // get extension
        String ext = null;
        int index = destinationFileName != null ? destinationFileName.lastIndexOf('.') : -1;
        if (index > 0) ext = destinationFileName.substring(index); // includes the dot '.' in front

        destinationFile = File.createTempFile(TEMP_PLAIN_FILE_PREFIX, ext);
        // temp plain files should be cleaned up
        GlobalProperties.addTempFileToCleanup(destinationFile);
      }

      if (destinationFile != null) {

        encFileIn = new FileInputStream(encDataFile);
        DigestInputStream dEncFileIn = new DigestInputStream(encFileIn, new SHA256());
        //DigestInputStream dEncFileIn = new DigestInputStream(encFileIn, MessageDigest.getInstance("SHA-1"));
        // buffer should suck all through digest input stream even if original file is 0 length and is treated differently by GZIP sucker
        //SpeedLimitedInputStream encSpeedIn = new SpeedLimitedInputStream(dEncFileIn, SpeedLimiter.DEFAULT_THROUGHPUT*2, false);
        BufferedInputStream encBufFileIn = new BufferedInputStream(dEncFileIn, 1024*32);
        // progress interruptible stream
        InterruptibleInputStream interEncIn = new InterruptibleInputStream(encBufFileIn);
        if (progressMonitor != null) progressMonitor.setInterrupt(interEncIn);

        BlockCipherInputStream cipherIn = new BlockCipherInputStream(interEncIn, symmetricKey);
        GZIPInputStream gzipIn = new GZIPInputStream(cipherIn);

        // Open the destination file to write to it.
        fileOut = new FileOutputStream(destinationFile);
        destinationFileCreated = true;
        BufferedOutputStream bufFileOut = new BufferedOutputStream(fileOut, 1024*32);
        DigestOutputStream dFileOut = new DigestOutputStream(bufFileOut, new SHA256());
        //DigestOutputStream dFileOut = new DigestOutputStream(bufFileOut, MessageDigest.getInstance("SHA-1"));

        if (progressMonitor != null) {
          progressMonitor.setCurrentStatus("Decrypting and Uncompressing File ...");
          progressMonitor.setFileNameSource(getEncDataFile().getAbsolutePath());
          progressMonitor.setFileNameDestination(destinationFile.getAbsolutePath());
          progressMonitor.setTransferSize(originalSize.longValue());
          progressMonitor.nextTask();
        }

        // move data from the GZIP input to file output
        //FileUtils.moveDataEOF(gzipIn, dFileOut, progressMonitor); // <== bad because BlockCipherInputStream DOES NOT support EOF marker
        FileUtils.moveData(new DataInputStream(gzipIn), dFileOut, originalSize.longValue(), progressMonitor);
        fileDownloaded = Boolean.TRUE;

        if (progressMonitor != null) {
          progressMonitor.setInterrupt(null);
          progressMonitor.doneTransfer();
        }

        gzipIn.close();
        dFileOut.close();

        // Remember the newly created plain file.
        plainDataFile = destinationFile;

        // create the encrypted data digest
        byte[] encDigest = dEncFileIn.getMessageDigest().digest();
        if (trace != null) trace.data(30, "encDigest", ArrayUtils.toString(encDigest));

        // create the plain data digest
        byte[] plainDigest = dFileOut.getMessageDigest().digest();
        if (trace != null) trace.data(40, "plainDigest", ArrayUtils.toString(plainDigest));


        // unseal the attributes and verify the signatures
        unSeal(verifyingKeyRecord, symmetricKey);


        // verify the plain data digest
        BAAsyPlainBlock verifiedPlainDigest = null;
        if (verifyingKeyRecord != null) {
          verifiedPlainDigest = signedOrigDigest.verifySignature(verifyingKeyRecord.plainPublicKey);
          if (trace != null) trace.data(60, "verifiedPlainDigest", ArrayUtils.toString(verifiedPlainDigest.toByteArray()));
        }


        String errorMsg = "";
        //if (!Arrays.equals(encDigest, verifiedEncDigest.toByteArray()) || !Arrays.equals(plainDigest, verifiedPlainDigest.toByteArray())) {
        if (!Arrays.equals(encDigest, encDataDigest.toByteArray()))
        {
          errorMsg += "\n  Computed Encrypted Data Digest : " + ArrayUtils.toString(encDigest) +
                      "\n  Expected Encrypted Data Digest : " + ArrayUtils.toString(encDataDigest.toByteArray());
        }
        if (!Arrays.equals(plainDigest, origDataDigest.toByteArray())) {
          errorMsg += "\n  Computed Plain Data Digest : " + ArrayUtils.toString(plainDigest) +
                      "\n  Expected Plain Data Digest : " + ArrayUtils.toString(origDataDigest.toByteArray());
        }
        if (errorMsg.length() > 0) {
          errorMsg =  "File data digests are invalid.  Data might have been tampered with. " +
                      "\n" +
                      "\n  Encrypted File : " + encDataFile.getAbsolutePath() +
                      "\n  Plain File : " + destinationFile.getAbsolutePath() +
                      "\n" + errorMsg;
        }
        String keyMsg = "";
        if (!origDataDigest.equals(verifiedPlainDigest)) {
          String keyErrorMsg = null;
          boolean skipErrorMsg = false;
          keyErrorMsg += "\n" +
                         "\nSpecified Signing Key cannot verify the Signed Digest of Plain Data." +
                         "\n";
          if (verifyingKeyRecord != null && verifiedPlainDigest != null) {
            keyErrorMsg += "Specifically, signing key " + verifyingKeyRecord.verboseInfo() + " verifies the " +
                           "signed digest of plain data " + ArrayUtils.toString(signedOrigDigest.toByteArray()) + " " +
                           "to the value " + ArrayUtils.toString(verifiedPlainDigest.toByteArray()) + " which differs " +
                           "from the expected value " + ArrayUtils.toString(origDataDigest.toByteArray());
          }
          else {
            // we will skip error messages due to missing user key, user might have been deleted and his key as well...
            skipErrorMsg = true;
            keyErrorMsg += "Specifically, signing key is not available in the system.  " +
                           "This could be caused by removal of the account from which the file was signed, " +
                           "or an unexpected system error.";
          }
          if (!skipErrorMsg) {
            keyMsg += keyErrorMsg;
          }
        }
        errorMsg = errorMsg + keyMsg;

        if (errorMsg.length() > 0) {
          errorMsg =  "Error occurred while veryfying integrity of the file: " +
                      "\n  " + destinationFile.getAbsolutePath() +
                      "\n" +
                      errorMsg +
                      "\n" +
                      "\n" +
                      "\nWould you like to permanently remove this file?";
          if (trace != null) trace.data(80, errorMsg);

          // show error dialog
          String title = "File Integrity Check FAILED";
          boolean option = MessageDialog.showDialogYesNo(null, errorMsg, title, MessageDialog.ERROR_MESSAGE);

          if (option == true) {
            // cleanup encrypted and plain file that may be partial
            if (encDataFile != null) {
              CleanupAgent.wipeOrDelete(encDataFile);
              encDataFile = null;
            }
            if (plainDataFile != null) {
              CleanupAgent.wipeOrDelete(plainDataFile);
              plainDataFile = null;
            }
          }

        }
      } // end if destinationFile != null

    } catch (Throwable t) {
      if (trace != null) trace.exception(FileDataRecord.class, 100, t);

      // cleanup encrypted and plain file that may be partial
      try {
        if (encFileIn != null) 
          encFileIn.close();
      } catch (Throwable th) { }
      try {
        if (encDataFile != null) {
          CleanupAgent.wipeOrDelete(encDataFile);
          encDataFile = null;
        }
      } catch (Throwable th) { }

      try {
        if (fileOut != null) 
          fileOut.close();
      } catch (Throwable th) { }
      try {
        if (destinationFile != null) {
          // destinationFile is created first, so delete than and make sure class variable plainDataFile is cleared.
          if (destinationFileCreated)
            CleanupAgent.wipeOrDelete(destinationFile);
          destinationFile = null;
          plainDataFile = null;
        }
      } catch (Throwable th) { }

      throw new IllegalStateException(t.getMessage());
    } finally {
      Thread.currentThread().setPriority(oldPriority);
    }

    if (trace != null) trace.exit(FileDataRecord.class);
  }

  /**
   * Unseals the attributes only, no data files.
   */
  public void unSeal(KeyRecord verifyingKeyRecord, BASymmetricKey symmetricKey) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FileDataRecord.class, "unSeal(KeyRecord verifyingKeyRecord, BASymmetricKey symmetricKey)");
    if (trace != null) trace.args(verifyingKeyRecord);
    if (trace != null) trace.args(symmetricKey);

    try { 
      SymmetricBulkCipher symCipher = new SymmetricBulkCipher(symmetricKey);
      BASymPlainBulk tempOrigDataDigest = symCipher.bulkDecrypt(encOrigDataDigest);
      BASymPlainBulk tempSignedOrigDigest = symCipher.bulkDecrypt(encSignedOrigDigest);
      BASymPlainBulk tempEncDataDigest = symCipher.bulkDecrypt(encEncDataDigest);

      origDataDigest = new BADigestBlock(tempOrigDataDigest.toByteArray());
      signedOrigDigest = new BAAsyCipherBlock(tempSignedOrigDigest.toByteArray());
      encDataDigest = new BADigestBlock(tempEncDataDigest.toByteArray());

      // verify the plain data digest
      if (verifyingKeyRecord != null) {
        BAAsyPlainBlock verifiedPlainDigest = signedOrigDigest.verifySignature(verifyingKeyRecord.plainPublicKey);
        if (trace != null) trace.data(60, "verifiedPlainDigest", ArrayUtils.toString(verifiedPlainDigest.toByteArray()));
        isVerifiedPlainDigest = verifiedPlainDigest.equals(origDataDigest);
        if (!isVerifiedPlainDigest) {
          throw new SecurityException("Digest of the plain data is not equal to the digest obtained from verification of the signed digest of plain data.");
        }
      } else {
        // skip exception, not availability of key doesn't mean file is bad, used could have been deleted, and his key with him
        //throw new SecurityException("Digest of the plain data cannot be verified because the signing key is not available.");
      }
    } catch (Throwable t) {
      if (trace != null) trace.exception(FileDataRecord.class, 100, t);
      String title = "Digest Verification FAILED";
      MessageDialog.showErrorDialog(null, t.getMessage(), title, false);
    }

    if (trace != null) trace.exit(FileDataRecord.class);
  }


  public static long getFileEncSizeSum(FileDataRecord[] files) {
    long sum = 0;
    for (int i=0; files!=null && i<files.length; i++) {
      sum += files[i].encSize.longValue();
    }
    return sum;
  }

  public static long getRecordSizeSum(FileDataRecord[] files) {
    long sum = 0;
    for (int i=0; files!=null && i<files.length; i++) {
      sum += files[i].recordSize.longValue();
    }
    return sum;
  }


  public void merge(Record updated) {
    if (updated instanceof FileDataRecord) {
      FileDataRecord record = (FileDataRecord) updated;
      if (record.fileId            != null) fileId           = record.fileId;

      if (record.encOrigDataDigest != null) encOrigDataDigest = record.encOrigDataDigest;
      if (record.encSignedOrigDigest != null) encSignedOrigDigest = record.encSignedOrigDigest;
      if (record.encEncDataDigest != null) encEncDataDigest = record.encEncDataDigest;

      if (record.encSize           != null) encSize          = record.encSize;
      if (record.encDataFile       != null) encDataFile      = record.encDataFile;
      if (record.plainDataFile     != null) plainDataFile    = record.plainDataFile;
      if (record.fileDownloaded    != null) fileDownloaded   = record.fileDownloaded;

      if (record.origDataDigest    != null) origDataDigest   = record.origDataDigest;
      if (record.signedOrigDigest  != null) signedOrigDigest = record.signedOrigDigest;
      if (record.encDataDigest     != null) encDataDigest    = record.encDataDigest;
      if (record.signingKeyId      != null) signingKeyId     = record.signingKeyId;
      if (record.fileCreated       != null) fileCreated      = record.fileCreated;
      if (record.fileUpdated       != null) fileUpdated      = record.fileUpdated;
      if (record.recordSize        != null) recordSize       = record.recordSize;

      // After fetching, it is unSealed and later on merged into the cache,
      // so copy the unSealed flag too, but don't reset it if fetched data 
      // was not unSealed and cached copy might be already unSealed.
      if (updated.isUnSealed())
        setUnSealed(true);
    }
    else
      super.mergeError(updated);
  }


  public String toString() {
    return "[FileDataRecord"
    + ": fileId="           + fileId
    + ", encSize="          + encSize
    + ", encDataFile="      + encDataFile
    + ", plainDataFile="    + plainDataFile
    + ", fileDownloaded="   + fileDownloaded
    + ", origDataDigest="   + origDataDigest
    + ", signedOrigDigest=" + signedOrigDigest
    + ", encDataDigest="    + encDataDigest
    + ", signingKeyId="     + signingKeyId
    + ", fileCreated="      + fileCreated
    + ", fileUpdated="      + fileUpdated
    + ", recordSize="       + recordSize
    + "]";
  }


  public String toStringLongFormat() {
    return "FileDataRecord"
    + "\n: fileId="           + fileId
    + "\n, encSize="          + encSize
    + "\n, encDataFile="      + encDataFile
    + "\n, plainDataFile="    + plainDataFile
    + "\n, fileDownloaded="   + fileDownloaded
    + "\n, origDataDigest="   + origDataDigest
    + "\n, signedOrigDigest=" + signedOrigDigest
    + "\n, encDataDigest="    + encDataDigest
    + "\n, signingKeyId="     + signingKeyId
    + "\n, fileCreated="      + fileCreated
    + "\n, fileUpdated="      + fileUpdated
    + "\n, recordSize="       + recordSize;
  }

  /**
   * When record is being finalized, and auto remove flag is set,
   * then plain file will be deleted.
   */
  public void setAutoRemovePlainFile(boolean remove) {
    autoRemovePlainFile = remove;
  }

  public void cleanupEncFile() {
    try {
      if (encDataFile != null) {
        CleanupAgent.wipeOrDelete(encDataFile);
      }
    } catch (Throwable t) { }
  }

  public void finalize() throws Throwable {
    cleanupEncFile();
    // see if plain file needs to be deleted
    try {
      if (plainDataFile != null) {
        if (autoRemovePlainFile) {
          CleanupAgent.wipeOrDelete(plainDataFile);
        }
      }
    } catch (Throwable t) { }
    super.finalize();
  }

  public void setId(Long id) {
    fileId = id;
  }

}