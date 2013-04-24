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

package com.CH_gui.usrs;

import com.CH_cl.service.actions.ClientMessageAction;
import com.CH_cl.service.actions.usr.UsrAGetHandles;
import com.CH_cl.service.engine.DefaultReplyRunner;
import com.CH_cl.service.engine.ServerInterfaceLayer;
import com.CH_co.service.msg.CommandCodes;
import com.CH_co.service.msg.MessageAction;
import com.CH_co.service.msg.dataSets.usr.Usr_Search_Rq;
import com.CH_co.service.msg.dataSets.usr.Usr_UsrHandles_Rp;
import com.CH_co.service.records.RecordUtils;
import com.CH_co.service.records.UserRecord;
import com.CH_co.trace.ThreadTraced;
import com.CH_co.trace.Trace;
import com.CH_co.util.ImageNums;
import com.CH_co.util.NotificationCenter;
import com.CH_co.util.URLs;
import com.CH_gui.actionGui.JActionButton;
import com.CH_gui.actionGui.JActionFrame;
import com.CH_gui.frame.MainFrame;
import com.CH_gui.gui.JMyButton;
import com.CH_gui.gui.JMyLabel;
import com.CH_gui.gui.MyInsets;
import com.CH_gui.list.StringHighlighter;
import com.CH_gui.list.StringHighlighterI;
import com.CH_gui.menuing.ToolBarModel;
import com.CH_gui.table.RecordTableCellRenderer;
import com.CH_gui.userTable.PassRecoveryUserActionTable;
import com.CH_gui.userTable.UserActionTable;
import com.CH_gui.util.Images;
import com.CH_gui.util.MessageDialog;
import com.CH_gui.util.MiscGui;
import com.CH_gui.util.ToolBarProducerI;
import com.CH_guiLib.gui.JMyRadioButton;
import com.CH_guiLib.gui.JMyTextField;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.StringTokenizer;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.TableCellRenderer;

/** 
* <b>Copyright</b> &copy; 2001-2013
* <a href="http://www.CryptoHeaven.com/DevelopmentTeam/">
* CryptoHeaven Corp.
* </a><br>All rights reserved.<p>
*
* Class Description:
*
*
* Class Details:
*
*
* <b>$Revision: 1.33 $</b>
* @author  Marcin Kurzawa
* @version
*/
public class UserSearchPanel extends JPanel implements ToolBarProducerI {

  private boolean suppressToolBar;
  private ServerInterfaceLayer SIL;

  JTextField jSearchField;
  public UserActionTable userActionTable;
  public EmailInvitationPanel emailInvitationPanel;
  private ToolBarModel toolBarModel;

  JRadioButton jRadioExact;
  JRadioButton jRadioNoCase;
  JRadioButton jRadioPartial;
  JRadioButton jRadioPhonetic;

  JButton jSearch;

  private String[] lastSearchStrings;
  private static int matchBITS =
      StringHighlighter.MATCH_STRING__EXACT |
      StringHighlighter.MATCH_STRING__TRIMMED |
      StringHighlighter.MATCH_STRING__NO_CASE |
      StringHighlighter.MATCH_STRING__LEADING_TOKENS |
      StringHighlighter.MATCH_STRING__SEQUENCED_TOKENS |
      StringHighlighter.MATCH_STRING__CONTAINS;

  /** Creates new UserSearchPanel */
  public UserSearchPanel(boolean suppressToolBar, boolean withUserActions, boolean withInviteActions, String customSearchHeader, String searchString, boolean isPassRecoveryMode) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(UserSearchPanel.class, "UserSearchPanel(boolean suppressToolBar, boolean withUserActions, boolean withInviteActions, String customSearchHeader, String searchString, boolean isPassRecoveryMode)");
    if (trace != null) trace.args(suppressToolBar);
    if (trace != null) trace.args(withUserActions);
    if (trace != null) trace.args(withInviteActions);
    if (trace != null) trace.args(customSearchHeader);
    if (trace != null) trace.args(searchString);
    if (trace != null) trace.args(isPassRecoveryMode);

    this.suppressToolBar = suppressToolBar;
    SIL = MainFrame.getServerInterfaceLayer();
    createPanel(withUserActions, withInviteActions, customSearchHeader, isPassRecoveryMode);

    if (toolBarModel != null)
      toolBarModel.addComponentActions(this);

    if (searchString != null && searchString.length() > 0) {
      jSearchField.setText(searchString);
      searchAction(true);
    }

    if (trace != null) trace.exit(UserSearchPanel.class);
  }

  public JButton getSearchButton() {
    return jSearch;
  }

  public UserActionTable getUserActionTable() {
    return userActionTable;
  }

  /**
  * @return create and return 'Search' button
  */
  private JButton createSearchButton() {
    jSearch = new JMyButton(com.CH_cl.lang.Lang.rb.getString("button_Search"));
    jSearch.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        // search action
        searchAction();
      }
    });
    return jSearch;
  }


  /**
  * Create the dialog's main panel.
  */
  private void createPanel(boolean withUserActions, boolean withInviteActions, String customSearchHeader, boolean isPassRecoveryMode) {
    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new GridBagLayout());
    if (!isPassRecoveryMode) {
      mainPanel.setBorder(new LineBorder(mainPanel.getBackground().darker(), 1, true));
    }

    JPanel invitePanel = null;
    if (!isPassRecoveryMode) {
      invitePanel = new JPanel();
      invitePanel.setLayout(new GridBagLayout());
      invitePanel.setBorder(new LineBorder(invitePanel.getBackground().darker(), 1, true));
    }

    // Overall, we have a border layout with two panels insede, the main, and invitation button.
    setLayout(new BorderLayout(0, 1));
    add(mainPanel, BorderLayout.CENTER);
    if (invitePanel != null) {
      add(invitePanel, BorderLayout.SOUTH);
    }

    // create radio buttons first
    jRadioExact = new JMyRadioButton(com.CH_cl.lang.Lang.rb.getString("searchMatch_Exact"));
    jRadioNoCase = new JMyRadioButton(com.CH_cl.lang.Lang.rb.getString("searchMatch_Ignore_Case"));
    jRadioNoCase.setSelected(true);
    jRadioPartial = new JMyRadioButton(com.CH_cl.lang.Lang.rb.getString("searchMatch_Partial"));
    jRadioPhonetic = new JMyRadioButton(com.CH_cl.lang.Lang.rb.getString("searchMatch_Phonetic"));

    ButtonGroup group = new ButtonGroup();
    group.add(jRadioExact); group.add(jRadioNoCase); group.add(jRadioPartial); group.add(jRadioPhonetic);

    jSearchField = new JMyTextField();

    if (isPassRecoveryMode) {
      userActionTable = new PassRecoveryUserActionTable();
      userActionTable.getJSortedTable().getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    } else {
      userActionTable = new UserActionTable();
    }
    TableCellRenderer renderer = userActionTable.getJSortedTable().getDefaultRenderer(Object.class);
    if (renderer instanceof RecordTableCellRenderer) {
      RecordTableCellRenderer recordTableRenderer = (RecordTableCellRenderer) renderer;
      recordTableRenderer.setStringHighlighter(new StringHighlighterI() {
        public Object[] getExcludedObjs() {
          return null;
        }
        public int getHighlightMatch() {
          return matchBITS;
        }
        public String getHighlightStr() {
          return null;
        }
        public String[] getHighlightStrs() {
          if (lastSearchStrings != null && lastSearchStrings.length > 0)
            return lastSearchStrings;
          return null;
        }
        public boolean hasHighlightingStr() {
          return (lastSearchStrings != null && lastSearchStrings.length > 0);
        }
        public boolean alwaysArmorInHTML() {
          return true;
        }
        public boolean includePreTags() {
          return true;
        }
      });
    }

    jSearchField.setColumns(15);

    int posY = 0;

    if (!suppressToolBar && !JActionFrame.ENABLE_FRAME_TOOLBARS && !isPassRecoveryMode) {
      JToolBar toolBar = initToolBarModel(MiscGui.getVisualsKeyName(userActionTable), null, userActionTable).getToolBar();
      mainPanel.add(toolBar, new GridBagConstraints(0, posY, 7, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(0, 0, 0, 0), 0, 0));
      posY ++;
    }

    if (isPassRecoveryMode) {
      mainPanel.add(MiscGui.createLogoHeader(), new GridBagConstraints(0, posY, 7, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(0, 0, 0, 0), 0, 0));
      posY ++;
    }

    String introStr = customSearchHeader;
    if (introStr == null)
      introStr = java.text.MessageFormat.format(com.CH_cl.lang.Lang.rb.getString("label_Search_the_world_wide__SERVICE_COMMUNITY_NAME__for_your_Friends_and_Associates_by_specifying_their_username,_email_address,_or_ID."), new Object[] { URLs.get(URLs.SERVICE_COMMUNITY_NAME) });
    JLabel intro = new JMyLabel(introStr);
    intro.setIcon(Images.get(ImageNums.USER_FIND32));
    mainPanel.add(intro, new GridBagConstraints(0, posY, 7, 1, 0, 0,
      GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(10, 10, 10, 10), 0, 0));
    posY ++;

    mainPanel.add(new JMyLabel("Search for:"), new GridBagConstraints(0, posY, 2, 1, 0, 0,
      GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(5, 5, 0, 5), 0, 0));
    mainPanel.add(jSearchField, new GridBagConstraints(2, posY, 5, 1, 10, 0,
      GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(5, 5, 0, 5), 0, 0));
    posY ++;

    mainPanel.add(new JMyLabel(com.CH_cl.lang.Lang.rb.getString("label_Match")), new GridBagConstraints(0, posY, 2, 1, 0, 0,
      GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(0, 5, 8, 5), 0, 0));
    mainPanel.add(jRadioExact, new GridBagConstraints(2, posY, 1, 1, 10, 0,
      GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(0, 2, 8, 2), 0, 0));
    mainPanel.add(jRadioNoCase, new GridBagConstraints(3, posY, 1, 1, 10, 0,
      GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(0, 2, 8, 2), 0, 0));
    mainPanel.add(jRadioPartial, new GridBagConstraints(4, posY, 1, 1, 10, 0,
      GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(0, 2, 8, 2), 0, 0));
    mainPanel.add(jRadioPhonetic, new GridBagConstraints(5, posY, 1, 1, 10, 0,
      GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(0, 2, 8, 2), 0, 0));
    // Search button
    mainPanel.add(createSearchButton(), new GridBagConstraints(6, posY, 1, 2, 0, 0,
      GridBagConstraints.EAST, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;
    posY ++; // search button takes 2 vertical lines

    if (isPassRecoveryMode) {
      mainPanel.add(new JMyLabel("Please select your account from the search results below:"), new GridBagConstraints(0, posY, 7, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(5, 5, 5, 5), 0, 0));
      posY ++;
    }

    mainPanel.add(userActionTable, new GridBagConstraints(0, posY, 7, 1, 10, 10,
      GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new MyInsets(5, 5, 5, 5), 0, 0));
    posY ++;

    if (withUserActions) {
      Action[] actions = userActionTable.getUserActions();
      if (actions != null && actions.length > 0) {
        JButton[] actionButtons = new JButton[actions.length];
        for (int i=0; i<actions.length; i++) {
          actionButtons[i] = new JActionButton(actions[i], false, null, false);
//          actionButtons[i].setText((String) actions[i].getValue(Actions.NAME));
//          actionButtons[i].setHorizontalTextPosition(JButton.RIGHT);
//          actionButtons[i].setVerticalTextPosition(JButton.CENTER);
        }
        mainPanel.add(MiscGui.createButtonPanel(actionButtons), new GridBagConstraints(0, posY, 7, 1, 0, 0,
          GridBagConstraints.NORTHEAST, GridBagConstraints.NONE, new MyInsets(0, 0, 0, 0), 0, 0));
        posY ++;
      }
    }
    if (invitePanel != null) {
      emailInvitationPanel = new EmailInvitationPanel(jSearchField, withInviteActions);
      invitePanel.add(emailInvitationPanel, new GridBagConstraints(0, 0, 1, 1, 10, 0,
          GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new MyInsets(0, 0, 0, 0), 0, 0));
    }

    if (isPassRecoveryMode)
      setPreferredSize(new Dimension(439, 435));
  }

  private void searchAction() {
    searchAction(false);
  }
  private void searchAction(boolean broaderSearchIfNoResults) {
    boolean inputValid = true;
    String sId = null;
    String searchStr = jSearchField.getText().trim();
    StringBuffer searchTextSB = new StringBuffer();

    StringTokenizer st = new StringTokenizer(searchStr, ",;");
    lastSearchStrings = new String[st.countTokens()];
    int tokenIndex = 0;
    while (st.hasMoreTokens()) {
      String token = st.nextToken().trim();
      lastSearchStrings[tokenIndex] = token;
      boolean isNumeric = false;
      // first numeric value will be our USER-ID search number
      if (sId == null) {
        try {
          Long id = Long.valueOf(token);
          sId = token;
          isNumeric = true;
        } catch (NumberFormatException e) {
        }
      }
      if (!isNumeric) {
        searchTextSB.append(token);
        searchTextSB.append(";");
      }
      tokenIndex ++;
    }
    searchStr = searchTextSB.toString();

    Long uID = null;
    if (sId != null && sId.length() > 0) {
      try {
        uID = Long.valueOf(sId);
      } catch (NumberFormatException e) {
        inputValid = false;
      }
    }

    // no input at all
    if (uID == null && (searchStr == null || searchStr.length() == 0)) {
      inputValid = false;
    }

    // if input is valid
    if (inputValid) {
      Usr_Search_Rq request = new Usr_Search_Rq();
      request.handle = searchStr;
      if (searchStr == null || searchStr.length() == 0) {
        request.handleMode = 0;
      } else if (jRadioExact.isSelected()) {
        request.handleMode = 1;
      } else if (jRadioPartial.isSelected()) {
        request.handleMode = 2;
      } else if (jRadioPhonetic.isSelected()) {
        request.handleMode = 3;
      } else if (jRadioNoCase.isSelected()) {
        request.handleMode = 4;
      }

      request.userId = uID;
      if (uID == null) {
        request.idMode = 0;
      } else if (jRadioExact.isSelected()) {
        request.idMode = 1;
      } else if (jRadioPartial.isSelected() || jRadioPhonetic.isSelected() || jRadioNoCase.isSelected()) {
        request.idMode = 2;
      }
      request.includeEmailRecords = true;

      new UserSearchRunner(request, broaderSearchIfNoResults).start();
    }
  }


  /***********************************************************
  *** T o o l B a r P r o d u c e r I    interface methods ***
  ***********************************************************/
  public ToolBarModel getToolBarModel() {
    return toolBarModel;
  }
  public String getToolBarTitle() {
    return "User Search Toolbar";
  }
  public ToolBarModel initToolBarModel(String propertyKeyName, String toolBarName, Component sourceComponent) {
    if (!suppressToolBar && !JActionFrame.ENABLE_FRAME_TOOLBARS && toolBarModel == null)
      toolBarModel = new ToolBarModel(propertyKeyName, toolBarName != null ? toolBarName : getToolBarTitle(), false);
    if (toolBarModel != null && sourceComponent != null)
      toolBarModel.addComponentActions(sourceComponent);
    return toolBarModel;
  }


  private class UserSearchRunner extends ThreadTraced {
    private Usr_Search_Rq request;
    private boolean broaderSearchIfNoResults;
    private UserSearchRunner(Usr_Search_Rq request, boolean broaderSearchIfNoResults) {
      super("UserSearchRunner");
      this.request = request;
      this.broaderSearchIfNoResults = broaderSearchIfNoResults;
      setDaemon(true);
    }

    public void runTraced() {
      runTraced(broaderSearchIfNoResults);
    }
    private void runTraced(final boolean broaderSearchIfNoResults) {
      MessageAction msgAction = new MessageAction(CommandCodes.USR_Q_SEARCH, request);
      final ClientMessageAction replyAction = SIL.submitAndFetchReply(msgAction, 30000);

      DefaultReplyRunner.nonThreadedRun(SIL, replyAction);

      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          if (UserSearchPanel.this.isVisible()) {
            boolean querySatisfied = false;
            if (replyAction instanceof UsrAGetHandles) {
              UsrAGetHandles handlesAction = (UsrAGetHandles) replyAction;
              Usr_UsrHandles_Rp handlesSet = (Usr_UsrHandles_Rp) handlesAction.getMsgDataSet();
              UserRecord[] uRecords = handlesSet.userRecords;
              boolean isResultTruncated = uRecords != null && uRecords.length >= 10;
              // get merged records
              uRecords = SIL.getFetchedDataCache().getUserRecords(RecordUtils.getIDs(uRecords));
              userActionTable.getTableModel().setData(uRecords);
              userActionTable.setEnabledActions();
              if (uRecords != null && uRecords.length > 0) {
                querySatisfied = true;
                userActionTable.getJSortedTable().getSelectionModel().setSelectionInterval(0, 0);
              }
              if (jSearchField.getText().length() > 0)
                jSearchField.selectAll();
              if (isResultTruncated)
                MessageDialog.showInfoDialog(UserSearchPanel.this, "Displaying first few hits only.  Please narrow down your search.", "Search too broad.", false);
            }

            if (!querySatisfied) {
              if (broaderSearchIfNoResults && (request.idMode == 1 || request.handleMode ==  4)) {
                if (request.idMode == 1) {
                  jRadioPartial.setSelected(true);
                  request.idMode = 2;
                }
                if (request.handleMode == 4) {
                  jRadioPartial.setSelected(true);
                  request.handleMode = 2;
                }
                runTraced(false);
              } else {
                if (emailInvitationPanel != null && request.handle != null && request.handle.length() > 0) {
                  String line1 = "No user accounts found to match your search.";
                  String line2 = "Would you like to send an email invitation now?";
                  JPanel msgPanel = new JPanel(new GridLayout(2, 1, 10, 10));
                  msgPanel.add(new JMyLabel(line1));
                  msgPanel.add(new JMyLabel(line2));
                  String title = com.CH_cl.lang.Lang.rb.getString("msgTitle_No_users_found");
                  ActionListener yesAction = new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                      emailInvitationPanel.getActions()[EmailInvitationPanel.SEND_EMAIL_INVITAION_ACTION].actionPerformed(null);
                    }
                  };
                  MessageDialog.showDialogYesNo(UserSearchPanel.this, msgPanel, title, NotificationCenter.QUESTION_MESSAGE, false, yesAction, null);
                } else {
                  String messageText = com.CH_cl.lang.Lang.rb.getString("msg_No_users_found_to_satisfy_your_search_criteria.");
                  String title = com.CH_cl.lang.Lang.rb.getString("msgTitle_No_users_found");
                  MessageDialog.showInfoDialog(UserSearchPanel.this, messageText, title, false);
                }
              }
            }
          }
        }
      });
    }
  }

}