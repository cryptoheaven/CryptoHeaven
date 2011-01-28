/*
 * Copyright 2001-2011 by CryptoHeaven Development Team,
 * Mississauga, Ontario, Canada.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CryptoHeaven Development Team ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CryptoHeaven Development Team.
 */

package com.CH_gui.dialog;

import com.CH_co.trace.*;
import com.CH_co.util.*;
import com.CH_cl.service.actions.*;
import com.CH_cl.service.actions.file.*;
import com.CH_cl.service.engine.*;
import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.records.*;

import com.CH_co.service.records.*;
import com.CH_co.service.msg.*;
import com.CH_co.service.msg.dataSets.file.*;
import com.CH_co.service.msg.dataSets.obj.*;

import com.CH_gui.file.FileUtilities;
import com.CH_gui.frame.MainFrame;
import com.CH_gui.gui.*;
import com.CH_gui.service.records.RecordUtilsGui;
import com.CH_gui.util.*;
import com.CH_guiLib.gui.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.border.*;
import javax.swing.*;
import javax.swing.event.*;

/** 
 * <b>Copyright</b> &copy; 2001-2011
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
 * <b>$Revision: 1.30 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public class FilePropertiesDialog extends GeneralDialog implements VisualsSavable {

  private static final int DEFAULT_OK_INDEX = 0;
  private static final int DEFAULT_CANCEL_INDEX = 2;

  private FileLinkRecord fileLink;
  private FileDataRecord fileData; // file attributes for the transcript

  // General -- File Link page
  private JTextField jFileName;
  private JTextArea jFileDesc;

  // Data -- File Data page
  private JLabel jSizeOnDisk;
  private JLabel jDataCreated;
  private JLabel jDataUpdated;
  private JLabel jDataUser;
  private JLabel jDataKeyID;
  private JLabel jDataKeyInfo;
  private JTextField jDataOriginalDigest;
  private JTextField jDataEncryptedDigest;
  private JLabel jVerifyOK;

  private JButton jOk;
  private JButton jTranscript;

  private ServerInterfaceLayer serverInterfaceLayer;

  private static String FETCHING_DATA = com.CH_gui.lang.Lang.rb.getString("Fetching_Data...");

  private DocumentChangeListener documentChangeListener;


  /** Creates new FilePropertiesDialog */
  public FilePropertiesDialog(Frame owner, FileLinkRecord fileLink) {
    super(owner, java.text.MessageFormat.format(com.CH_gui.lang.Lang.rb.getString("title_OBJECT_-_File_Properties"), new Object[] {fileLink.getFileName()}));
    constructDialog(owner, fileLink);
  }
  public FilePropertiesDialog(Dialog owner, FileLinkRecord fileLink) {
    super(owner, java.text.MessageFormat.format(com.CH_gui.lang.Lang.rb.getString("title_OBJECT_-_File_Properties"), new Object[] {fileLink.getFileName()}));
    constructDialog(owner, fileLink);
  }
  private void constructDialog(Component owner, FileLinkRecord fileLink) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(FilePropertiesDialog.class, "FilePropertiesDialog()");

    this.fileLink = fileLink;
    serverInterfaceLayer = MainFrame.getServerInterfaceLayer();

    JButton[] buttons = createButtons();
    JComponent mainComponent = createTabbedPane();
    jFileName.addHierarchyListener(new InitialFocusRequestor());

    init(owner, buttons, mainComponent, DEFAULT_OK_INDEX, DEFAULT_CANCEL_INDEX);
    fetchData();

    if (trace != null) trace.exit(FilePropertiesDialog.class);
  }

  private JButton[] createButtons() {
    JButton[] buttons = new JButton[3];

    buttons[0] = new JMyButton(com.CH_gui.lang.Lang.rb.getString("button_OK"));
    buttons[0].addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        pressedOK();
      }
    });
    jOk = buttons[0];
    jOk.setEnabled(false);

    buttons[1] = new JMyButton(com.CH_gui.lang.Lang.rb.getString("button_Transcript"));
    buttons[1].addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        pressedTranscript();
      }
    });
    jTranscript = buttons[1];
    jTranscript.setEnabled(false);

    buttons[2] = new JMyButton(com.CH_gui.lang.Lang.rb.getString("button_Cancel"));
    buttons[2].addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        pressedCancel();
      }
    });

    return buttons;
  }

  private JTabbedPane createTabbedPane() {
    JTabbedPane pane = new JMyTabbedPane();
    pane.addTab(com.CH_gui.lang.Lang.rb.getString("tab_General"), createLinkPanel());
    pane.addTab(com.CH_gui.lang.Lang.rb.getString("tab_Data"), createDataPanel());
    return pane;
  }

  private JPanel createLinkPanel() {
    JPanel panel = new JPanel();
    panel.setBorder(new EmptyBorder(10, 10, 10, 10));

    panel.setLayout(new GridBagLayout());

    jFileName = new JMyTextField(fileLink.getFileName());
    documentChangeListener = new DocumentChangeListener();
    jFileName.getDocument().addDocumentListener(documentChangeListener);

    panel.add(new JMyLabel(Images.get(ImageNums.FILE_LOCKED32)), new GridBagConstraints(0, 0, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    panel.add(jFileName, new GridBagConstraints(1, 0, 1, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));


    // separator
    panel.add(new JSeparator(), new GridBagConstraints(0, 1, 2, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));


    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_File_Link_ID")), new GridBagConstraints(0, 2, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    panel.add(new JMyLabel(fileLink.fileLinkId.toString()), new GridBagConstraints(1, 2, 1, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));

    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_File_Type")), new GridBagConstraints(0, 3, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    JLabel jFileType = new JMyLabel(fileLink.getFileType(), RecordUtilsGui.getIcon(fileLink), JLabel.LEFT);
    panel.add(jFileType, new GridBagConstraints(1, 3, 1, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));

    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Location")), new GridBagConstraints(0, 4, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));

    Record locationRecord = FileLinkRecUtil.getLocationRecord(fileLink);
    JLabel jLocationRecord = null;
    if (locationRecord != null) {
      if (locationRecord instanceof FolderPair) {
        FolderPair pair = (FolderPair) locationRecord;
        jLocationRecord = new JMyLabel(pair.getMyName(), RecordUtilsGui.getIcon(pair), JLabel.LEFT);
      }
      else
        jLocationRecord = new JMyLabel(locationRecord.toString());
    }
    else
      jLocationRecord = new JMyLabel(com.CH_gui.lang.Lang.rb.getString("unknown"));
    panel.add(jLocationRecord, new GridBagConstraints(1, 4, 1, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));

    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Original_Size")), new GridBagConstraints(0, 5, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    long size = fileLink.origSize.longValue();
    String oSize = Misc.getFormattedSize(size, 3, 2);
    if (size >= 1000)
      oSize += " (" + Misc.getFormattedSize(size, 10, 10) + ")";
    panel.add(new JMyLabel(oSize), new GridBagConstraints(1, 5, 1, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));


    // seperator
    panel.add(new JSeparator(), new GridBagConstraints(0, 6, 2, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));


    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Link_Created")), new GridBagConstraints(0, 7, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    //SimpleDateFormat dateFormat = new SimpleDateFormat("MMMMM dd, yyyyy, HH:mm:ss.SSS");
    //String dateCreated = dateFormat.format(fileLink.recordCreated);
    panel.add(new JMyLabel(Misc.getFormattedTimestamp(fileLink.recordCreated)), new GridBagConstraints(1, 7, 1, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));

    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Link_Updated")), new GridBagConstraints(0, 8, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    //String dateUpdated = fileLink.recordUpdated != null ? dateFormat.format(fileLink.recordUpdated) : "";
    String dateUpdated = Misc.getFormattedTimestamp(fileLink.recordUpdated);
    panel.add(new JMyLabel(dateUpdated), new GridBagConstraints(1, 8, 1, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));


    // separator
    panel.add(new JSeparator(), new GridBagConstraints(0, 9, 2, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));


    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Encryption")), new GridBagConstraints(0, 10, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    panel.add(new JMyLabel("AES(256)"), new GridBagConstraints(1, 10, 1, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));


    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Comment")), new GridBagConstraints(0, 11, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    String desc = fileLink.getFileDesc();
    jFileDesc = new JMyTextArea(desc != null ? desc : "", 3, 20);
    jFileDesc.setWrapStyleWord(true);
    jFileDesc.setLineWrap(true);
    jFileDesc.getDocument().addDocumentListener(documentChangeListener);
    panel.add(new JScrollPane(jFileDesc), new GridBagConstraints(1, 11, 1, 3, 10, 10,
          GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(5, 5, 5, 5), 0, 0));


    return panel;
  }

  private JPanel createDataPanel() {
    JPanel panel = new JPanel();
    panel.setBorder(new EmptyBorder(10, 10, 10, 10));

    panel.setLayout(new GridBagLayout());

    int posY = 0;
    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_File_Data_ID")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    panel.add(new JMyLabel(fileLink.fileId.toString()), new GridBagConstraints(1, posY, 1, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Size_on_Disk")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    jSizeOnDisk = new JMyLabel(FETCHING_DATA);
    panel.add(jSizeOnDisk, new GridBagConstraints(1, posY, 1, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;



    // separator
    panel.add(new JSeparator(), new GridBagConstraints(0, posY, 2, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;


    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Signing_User")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    jDataUser = new JMyLabel(FETCHING_DATA);
    panel.add(jDataUser, new GridBagConstraints(1, posY, 1, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Signing_Key_ID")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    jDataKeyID = new JMyLabel(FETCHING_DATA);
    panel.add(jDataKeyID, new GridBagConstraints(1, posY, 1, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Signing_Key_Info")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    jDataKeyInfo = new JMyLabel(FETCHING_DATA);
    panel.add(jDataKeyInfo, new GridBagConstraints(1, posY, 1, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;



    // separator
    panel.add(new JSeparator(), new GridBagConstraints(0, posY, 2, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;



    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Verification")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    jVerifyOK = new JMyLabel(FETCHING_DATA);
    panel.add(jVerifyOK, new GridBagConstraints(1, posY, 1, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;


    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Digest_of_Original_Data_(SHA-256)")), new GridBagConstraints(0, posY, 2, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;
    jDataOriginalDigest = new JMyTextField(FETCHING_DATA, 32);
    jDataOriginalDigest.setEditable(false);
    panel.add(jDataOriginalDigest, new GridBagConstraints(0, posY, 2, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Digest_of_Encrypted_Data_(SHA-256)")), new GridBagConstraints(0, posY, 2, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;
    jDataEncryptedDigest = new JMyTextField(FETCHING_DATA, 32);
    jDataEncryptedDigest.setEditable(false);
    panel.add(jDataEncryptedDigest, new GridBagConstraints(0, posY, 2, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;



    // separator
    panel.add(new JSeparator(), new GridBagConstraints(0, posY, 2, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;


    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Data_Uploaded")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    jDataCreated = new JMyLabel(FETCHING_DATA);
    panel.add(jDataCreated, new GridBagConstraints(1, posY, 1, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    panel.add(new JMyLabel(com.CH_gui.lang.Lang.rb.getString("label_Record_Updated")), new GridBagConstraints(0, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    jDataUpdated = new JMyLabel(FETCHING_DATA);
    panel.add(jDataUpdated, new GridBagConstraints(1, posY, 1, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;


    // empty component for stretching only so that the rest stays on the top
    panel.add(new JMyLabel(), new GridBagConstraints(0, posY, 2, 1, 10, 10,
          GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(5, 5, 5, 5), 0, 0));

    return panel;
  }

  private void setEnabledButtons() {
    // see if Name or Comment has changed
    String newName = jFileName.getText().trim();
    String newDesc = jFileDesc.getText().trim();
    String oldDesc = fileLink.getFileDesc();
    oldDesc = oldDesc != null ? oldDesc : "";
    if (newName != null && newName.length() > 0 &&
       (!newName.equals(fileLink.getFileName()) || !newDesc.equals(oldDesc))
    ) {
      jOk.setEnabled(true);
    } else {
      jOk.setEnabled(false);
    }
  }

  private void pressedOK() {
    // see if we need to update File Name or Comment
    String newName = jFileName.getText().trim();
    String newDesc = jFileDesc.getText().trim();
    String oldDesc = fileLink.getFileDesc();
    oldDesc = oldDesc != null ? oldDesc : "";
    if (!newName.equals(fileLink.getFileName()) || !newDesc.equals(oldDesc)) {
      FileLinkRecord newFileLink = (FileLinkRecord) fileLink.clone();
      newFileLink.setFileName(newName);
      if (newDesc.length() > 0)
        newFileLink.setFileDesc(newDesc);
      else {
        newFileLink.setFileDesc(null);
        fileLink.setFileDesc(null);
      }

      fileLink.setFileName(newName + "^");

      FileUtilities.renameFile(newName, newFileLink);
      closeDialog();
    }

  }

  private void pressedCancel() {
    closeDialog();
  }


  private void pressedTranscript() {
    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    KeyRecord kRec = cache.getKeyRecord(fileData.getSigningKeyId());
    String RSA = kRec.plainPublicKey.shortInfo().toUpperCase();
    FolderShareRecord shareRec = cache.getFolderShareRecordMy(fileLink.ownerObjId, true);
    UserRecord userRec = cache.getUserRecord();
    KeyRecord myKeyRec = cache.getKeyRecordMyCurrent();
    StringBuffer sb = new StringBuffer();
    sb.append("--- BEGIN RECEIVED FILE ATTRIBUTES");

    sb.append("\n--- BEGIN AES(256) ENCRYPTED FILE NAME\n\n");
    sb.append(ArrayUtils.breakLines(fileLink.getEncFileName().getHexContent(), 80));
    sb.append("\n\n--- END AES(256) ENCRYPTED FILE NAME");

    sb.append("\n\n--- BEGIN AES(256) ENCRYPTED FILE DESCRIPTION\n\n");
    sb.append(ArrayUtils.breakLines(fileLink.getEncFileDesc() != null ? fileLink.getEncFileDesc().getHexContent() : "", 80));
    sb.append("\n\n--- END AES(256) ENCRYPTED FILE DESCRIPTION");

    sb.append("\n\n--- BEGIN AES(256) ENCRYPTED SHA-256 DIGEST OF AES(256) ENCRYPTED FILE\n\n");
    sb.append(ArrayUtils.breakLines(ArrayUtils.spreadString(fileData.getEncEncDataDigest().getHexContent(), 4, ' '), 80));
    sb.append("\n\n--- END AES(256) ENCRYPTED SHA-256 DIGEST OF AES(256) ENCRYPTED FILE");

    sb.append("\n\n--- BEGIN AES(256) ENCRYPTED SHA-256 DIGEST OF PLAIN FILE\n\n");
    sb.append(ArrayUtils.breakLines(ArrayUtils.spreadString(fileData.getEncOrigDataDigest().getHexContent(), 4, ' '), 80));
    sb.append("\n\n--- END AES(256) ENCRYPTED SHA-256 DIGEST OF PLAIN FILE");

    sb.append("\n\n--- BEGIN AES(256) ENCRYPTED "+RSA+" SIGNED SHA-256 DIGEST OF PLAIN FILE\n\n");
    sb.append(ArrayUtils.breakLines(fileData.getEncSignedOrigDigest().getHexContent(), 80));
    sb.append("\n\n--- END AES(256) ENCRYPTED "+RSA+" SIGNED SHA-256 DIGEST OF PLAIN FILE");

    sb.append("\n\n--- BEGIN "+RSA+" PUBLIC PLAIN SIGNING KEY\n\n");
    sb.append(ArrayUtils.breakLines(ArrayUtils.toString(kRec.plainPublicKey.objectToBytes()), 80));
    sb.append("\n\n--- END "+RSA+" PUBLIC PLAIN SIGNING KEY");

    sb.append("\n\n--- BEGIN AES(256) ENCRYPTED FILE AES(256) KEY\n\n");
    sb.append(ArrayUtils.breakLines(ArrayUtils.spreadString(fileLink.getEncSymmetricKey().getHexContent(), 4, ' '), 80));
    sb.append("\n\n--- END AES(256) ENCRYPTED FILE AES(256) KEY");

    sb.append("\n\n--- BEGIN AES(256) ENCRYPTED FOLDER AES(256) KEY\n\n");
    sb.append(ArrayUtils.breakLines(ArrayUtils.spreadString(shareRec.getEncSymmetricKey().getHexContent(), 4, ' '), 80));
    sb.append("\n\n--- END AES(256) ENCRYPTED FOLDER AES(256) KEY");

    sb.append("\n\n--- BEGIN "+RSA+" ENCRYPTED SUPER FOLDER AND CONTACT AES(256) KEYS\n\n");
    sb.append(ArrayUtils.breakLines(userRec.getEncSymKeys().getHexContent(), 80));
    sb.append("\n\n--- END "+RSA+" ENCRYPTED SUPER FOLDER AND CONTACT AES(256) KEYS");

    sb.append("\n\n--- BEGIN AES(256) PASS-CODE ENCRYPTED "+RSA+" PRIVATE KEY\n\n");
    sb.append(ArrayUtils.breakLines(myKeyRec.getEncPrivateKey().getHexContent(), 80));
    sb.append("\n\n--- END AES(256) PASS-CODE ENCRYPTED "+RSA+" PRIVATE KEY");

    sb.append("\n--- END RECEIVED FILE ATTRIBUTES");


    sb.append("\n\n--- BEGIN COMPUTED FILE ATTRIBUTES");
/*
    sb.append("\n--- BEGIN FILE AES(256) KEY\n\n");
    sb.append(ArrayUtils.breakLines(ArrayUtils.spreadString(fileLink.getSymmetricKey().getHexContent(), 4, ' '), 80));
    sb.append("\n\n--- END FILE AES(256) KEY");
*/
    sb.append("\n--- BEGIN PLAIN FILE NAME\n\n");
    sb.append(fileLink.getFileName());
    sb.append("\n\n--- END PLAIN FOLDER NAME");

    sb.append("\n\n--- BEGIN PLAIN FILE DESCRIPTION\n\n");
    sb.append(fileLink.getFileDesc() != null ? fileLink.getFileDesc() : "");
    sb.append("\n\n--- END PLAIN FILE DESCRIPTION");

    sb.append("\n\n--- BEGIN SHA-256 DIGEST OF AES(256) ENCRYPTED FILE\n\n");
    sb.append(ArrayUtils.breakLines(ArrayUtils.spreadString(fileData.getEncDataDigest().getHexContent(), 4, ' '), 80));
    sb.append("\n\n--- END SHA-256 DIGEST OF AES(256) ENCRYPTED FILE");

    sb.append("\n\n--- BEGIN "+RSA+" SIGNED SHA-256 DIGEST OF PLAIN FILE\n\n");
    sb.append(ArrayUtils.breakLines(fileData.getSignedOrigDigest().getHexContent(), 80));
    sb.append("\n\n--- END "+RSA+" SIGNED SHA-256 DIGEST OF PLAIN FILE");

    sb.append("\n\n--- BEGIN SHA-256 DIGEST OF PLAIN FILE\n\n");
    sb.append(ArrayUtils.breakLines(ArrayUtils.spreadString(fileData.getOrigDataDigest().getHexContent(), 4, ' '), 80));
    sb.append("\n\n--- END SHA-256 DIGEST OF PLAIN FILE");
    sb.append("\n--- END COMPUTED FILE ATTRIBUTES");

    JTextArea textArea = new JMyTextArea(sb.toString());
    textArea.setEditable(false);
    textArea.setCaretPosition(0);
    textArea.setRows(35);
    textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

    JButton jClose = new JMyButton("Close");
    jClose.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Dialog d = (Dialog) SwingUtilities.windowForComponent((Component) e.getSource());
        d.dispose();
      }
    });
    new GeneralDialog(this, com.CH_gui.lang.Lang.rb.getString("title_File_Transcript"), new JButton[] { jClose }, -1, 0, new JScrollPane(textArea));
  }


  private void fetchData() {
    Thread th = new ThreadTraced("File Properties Data Fetcher") {
      public void runTraced() {
        FetchedDataCache cache = FetchedDataCache.getSingleInstance();

        Obj_IDs_Co request = new Obj_IDs_Co();
        request.IDs = new Long[2][];
        request.IDs[0] = new Long[] { fileLink.fileLinkId };
        request.IDs[1] = new Long[] { cache.getFolderShareRecordMy(fileLink.ownerObjId, true).shareId };

        MessageAction msgAction = new MessageAction(CommandCodes.FILE_Q_GET_FILES_DATA_ATTRIBUTES, request);
        ClientMessageAction replyMsg = serverInterfaceLayer.submitAndFetchReply(msgAction, 30000);
        DefaultReplyRunner.nonThreadedRun(serverInterfaceLayer, replyMsg);

        if (replyMsg instanceof FileAGetFilesDataAttr) {
          File_GetAttr_Rp reply = (File_GetAttr_Rp) replyMsg.getMsgDataSet();
          fileData = reply.fileDataRecords[0];
          jTranscript.setEnabled(true);

          // get Signing key
          Long keyId = fileData.getSigningKeyId();
          KeyRecord kRec = cache.getKeyRecord(keyId);
          if (kRec == null) {
            serverInterfaceLayer.submitAndWait(new MessageAction(CommandCodes.KEY_Q_GET_PUBLIC_KEYS_FOR_USERS, new Obj_IDList_Co(keyId)), 60000);
            kRec = cache.getKeyRecord(keyId);
          }

          // get Signing user
          UserRecord uRec = null;
          if (kRec != null) {
            Long userId = kRec.ownerUserId;
            uRec = cache.getUserRecord(userId);
            if (uRec == null) {
              serverInterfaceLayer.submitAndWait(new MessageAction(CommandCodes.USR_Q_GET_HANDLES, new Obj_IDList_Co(userId)), 30000);
              uRec = cache.getUserRecord(userId);
            }
          }

          //long size = fileData.getEncSize().longValue();
          long size = fileData.recordSize.longValue();
          String oSize = Misc.getFormattedSize(size, 3, 2);
          if (size >= 1000)
            oSize += " (" + Misc.getFormattedSize(size, 10, 10) + ")";
          jSizeOnDisk.setText(oSize);
          jDataCreated.setText(Misc.getFormattedTimestamp(fileData.fileCreated));
          jDataUpdated.setText(Misc.getFormattedTimestamp(fileData.fileUpdated));

          if (uRec != null) {
            jDataUser.setText(uRec.shortInfo());
            jDataUser.setIcon(RecordUtilsGui.getIcon(uRec));
          } else {
            jDataUser.setText(com.CH_gui.lang.Lang.rb.getString("label_Unknown_User_Account"));
            jDataUser.setIcon(Images.get(ImageNums.PERSON_SMALL));
          }

          jDataKeyID.setText(fileData.getSigningKeyId().toString());

          if (kRec != null) {
            jDataKeyInfo.setText(kRec.plainPublicKey.shortInfo());
            jDataKeyInfo.setIcon(RecordUtilsGui.getIcon(kRec));
          }
          else {
            jDataKeyInfo.setText(com.CH_gui.lang.Lang.rb.getString("Key_is_not_available"));
            jDataKeyInfo.setIcon(Images.get(ImageNums.KEY16));
          }

          // since we were able to decrypt the digests, they verified OK
          if (fileData.isVerifiedPlainDigest()) {
            jVerifyOK.setIcon(Images.get(ImageNums.SEAL8_15));
            jVerifyOK.setText(com.CH_gui.lang.Lang.rb.getString("Digest_signatures_verified."));
          } else {
            jVerifyOK.setText(com.CH_gui.lang.Lang.rb.getString("Digest_signatures_could_not_be_verified."));
            jVerifyOK.setIcon(Images.get(ImageNums.PRIORITY_HIGH_SMALL));
          }

          jDataOriginalDigest.setText(fileData.getOrigDataDigest().getHexContent());
          jDataEncryptedDigest.setText(fileData.getEncDataDigest().getHexContent());
        }
      }
    };
    th.setDaemon(true);
    th.start();
  }


  public void closeDialog() {
    if (documentChangeListener != null) {
      if (jFileName != null)
        jFileName.getDocument().removeDocumentListener(documentChangeListener);
      if (jFileDesc != null)
        jFileDesc.getDocument().removeDocumentListener(documentChangeListener);
      documentChangeListener = null;
    }
    super.closeDialog();
  }


  private class DocumentChangeListener implements DocumentListener {
    public void changedUpdate(DocumentEvent e) {
      setEnabledButtons();
    }
    public void insertUpdate(DocumentEvent e) {
      setEnabledButtons();
    }
    public void removeUpdate(DocumentEvent e) {
      setEnabledButtons();
    }
  }

  /*******************************************************
  *** V i s u a l s S a v a b l e    interface methods ***
  *******************************************************/
  public static final String visualsClassKeyName = "FilePropertiesDialog";
  public String getVisualsClassKeyName() {
    return visualsClassKeyName;
  }
}