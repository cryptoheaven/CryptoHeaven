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


package com.CH_gui.table;

import com.CH_cl.service.actions.ClientMessageAction;
import com.CH_cl.service.engine.*;
import com.CH_cl.service.cache.FetchedDataCache;
import com.CH_cl.service.cache.event.*;
import com.CH_cl.service.records.filters.*;

import com.CH_co.service.msg.*;
import com.CH_co.service.msg.dataSets.obj.*;
import com.CH_co.service.records.*;
import com.CH_co.service.records.filters.*;
import com.CH_co.trace.*;
import com.CH_co.util.*;

import com.CH_gui.action.*;
import com.CH_gui.actionGui.*;
import com.CH_gui.contactTable.*;
import com.CH_gui.frame.*;
import com.CH_gui.gui.*;
import com.CH_gui.list.*;
import com.CH_gui.menuing.*;
import com.CH_gui.msgs.*;
import com.CH_gui.msgTable.*;
import com.CH_gui.recycleTable.*;
import com.CH_gui.service.records.*;
import com.CH_gui.util.*;

import com.CH_guiLib.gui.*;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;

import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

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
 * <b>$Revision: 1.32 $</b>
 * @author  Marcin Kurzawa
 * @version
 */
public abstract class RecordTableComponent extends JPanel implements ToolBarProducerI, VisualsSavable, DisposableObj {

  private JLabel jTitleLabel;
  private JLabel jDescriptionLabel;
  private JTextField jFilterField;
  private JButton jFilterGoButton;
  private JCheckBox jFilterMsgBodyCheck;
  private JButton jFilterCloseButton;

  private JPanel jTopPanel;
  private JPanel jFilterPanel;
  private JPanel jTitlePanel;
  private JPanel jDescriptionPanel1;
  private JPanel jDescriptionPanel2;
  private JPanel jOfflinePanel;
  private JPanel jUtilityButtonPanel;
  private ToolBarModel toolBarModel;
  private int countTopPanels = 0;

  private boolean isDescriptionLabelShown = false;
  private boolean isParticipantComponentsListed = false;

  private RecordTableScrollPane recordTableScrollPane;
  private Object title; // Record or String
  private String description;
  private Long lastFolderId;

  private CardLayout cardLayout;
  private JPanel cardsPanel;
  private JComponent cards;
  private String emptyTemplateName;
  private String backTemplateName;
  private String categoryTemplateName;

  private JComponent mainTableComp;
  private JComponent mainEntryComp;
  private JComponent mainPreviewComp;
  private JPanel mainCardPanel;

  private boolean suppressVisualsSavable;
  private FolderShareListener folderShareListener;
  private ContactListener contactListener;

  /**
   * Creates new RecordTableComponent.
   * @param recordTableScrollPane is often an RecordActionTable which is a subclass of RecordTableScrollPane
   */
  public RecordTableComponent(RecordTableScrollPane recordTableScrollPane) {
    this(recordTableScrollPane, Template.get(Template.NONE), Template.get(Template.NONE));
  }
  public RecordTableComponent(RecordTableScrollPane recordTableScrollPane, String emptyTemplateName) {
    this(recordTableScrollPane, emptyTemplateName, Template.get(Template.NONE), Template.get(Template.NONE), false, false, false);
  }
  public RecordTableComponent(RecordTableScrollPane recordTableScrollPane, String emptyTemplateName, boolean suppressVisualsSavable) {
    this(recordTableScrollPane, emptyTemplateName, Template.get(Template.NONE), Template.get(Template.NONE), false, false, suppressVisualsSavable);
  }
  public RecordTableComponent(RecordTableScrollPane recordTableScrollPane, String emptyTemplateName, String backTemplateName) {
    this(recordTableScrollPane, emptyTemplateName, backTemplateName, Template.get(Template.NONE), false, false, false);
  }
  public RecordTableComponent(RecordTableScrollPane recordTableScrollPane, String emptyTemplateName, String backTemplateName, String categoryTemplateName) {
    this(recordTableScrollPane, emptyTemplateName, backTemplateName, categoryTemplateName, false, false, false);
  }
  public RecordTableComponent(RecordTableScrollPane recordTableScrollPane, String emptyTemplateName, String backTemplateName, String categoryTemplateName, boolean suppressToolbar, boolean suppressUtilityBar, boolean suppressVisualsSavable) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecordTableComponent.class, "RecordTableComponent(RecordTableScrollPane recordTableScrollPane, String emptyTemplateName, String backTemplateName, String categoryTemplateName, boolean suppressToolbar, boolean suppressUtilityBar, boolean suppressVisualsSavable)");
    if (trace != null) trace.args(recordTableScrollPane, emptyTemplateName, backTemplateName, categoryTemplateName);
    if (trace != null) trace.args(suppressToolbar);
    if (trace != null) trace.args(suppressUtilityBar);
    if (trace != null) trace.args(suppressVisualsSavable);

    this.recordTableScrollPane = recordTableScrollPane;
    this.emptyTemplateName = emptyTemplateName;
    this.backTemplateName = backTemplateName;
    this.categoryTemplateName = categoryTemplateName;
    this.suppressVisualsSavable = suppressVisualsSavable;

    jTitleLabel = new JMyLabel();
    jDescriptionLabel = new JMyLabel();
    jFilterField = new JMyTextField("", 15);
    jFilterField.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        setFilterNarrowing(jFilterField.getText(), jFilterMsgBodyCheck.isSelected());
        jFilterField.selectAll();
      }
    });
    jFilterGoButton = new JMyButton(Images.get(ImageNums.GO16));
    jFilterGoButton.setBorder(new EmptyBorder(0,0,0,0));
    jFilterGoButton.setBackground(Color.decode("0x"+MsgDataRecord.WARNING_BACKGROUND_COLOR));
    jFilterGoButton.setOpaque(true);
    jFilterGoButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    jFilterGoButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        setFilterNarrowing(jFilterField.getText(), jFilterMsgBodyCheck.isSelected());
        jFilterField.selectAll();
        jFilterField.requestFocus();
      }
    });
    jFilterMsgBodyCheck = new JMyCheckBox();
    jFilterMsgBodyCheck.setBackground(Color.decode("0x"+MsgDataRecord.WARNING_BACKGROUND_COLOR));
    jFilterMsgBodyCheck.setOpaque(true);
    RecordTableModel tableModel = recordTableScrollPane.getTableModel();
    if (tableModel instanceof MsgTableModel) {
      MsgTableModel msgTableModel = (MsgTableModel) tableModel;
      if (msgTableModel.isModeAddr())
        jFilterMsgBodyCheck.setText("include address details");
      else if (!msgTableModel.isModeMsgBody())
        jFilterMsgBodyCheck.setText("include message bodies");
    } else if (tableModel instanceof RecycleTableModel) {
      jFilterMsgBodyCheck.setText("include message bodies");
    }
    jFilterMsgBodyCheck.setVisible(jFilterMsgBodyCheck.getText().trim().length() > 0);
    jFilterMsgBodyCheck.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        setFilterNarrowing(jFilterField.getText(), jFilterMsgBodyCheck.isSelected());
        jFilterField.selectAll();
        jFilterField.requestFocus();
      }
    });
    jFilterCloseButton = new JMyButton(Images.get(ImageNums.X15));
    jFilterCloseButton.setBorder(new LineBorder(Color.darkGray, 1));
    jFilterCloseButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    jFilterCloseButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        ((RecordActionTable) RecordTableComponent.this.recordTableScrollPane).getFilterAction().putValue(Actions.STATE_CHECK, Boolean.FALSE);
        setFilterNarrowing(null, jFilterMsgBodyCheck.isSelected());
      }
    });

    jTopPanel = new JPanel(new GridBagLayout());
    jFilterPanel = new JPanel(new GridBagLayout());
    jFilterPanel.setVisible(false);
    //jFilterPanel.setBorder(new LineBorder(Color.darkGray, 1));
    jFilterPanel.setBackground(Color.decode("0x"+MsgDataRecord.WARNING_BACKGROUND_COLOR));
    jFilterPanel.add(new JMyLabel("Look for:"), new GridBagConstraints(0, 0, 1, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(3, 3, 3, 3), 0, 0));
    jFilterPanel.add(jFilterField, new GridBagConstraints(1, 0, 1, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(3, 3, 3, 3), 0, 0));
    jFilterPanel.add(jFilterGoButton, new GridBagConstraints(2, 0, 1, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(3, 3, 3, 3), 0, 0));
    jFilterPanel.add(jFilterMsgBodyCheck, new GridBagConstraints(4, 0, 1, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(3, 3, 3, 3), 0, 0));
    jFilterPanel.add(new JLabel(), new GridBagConstraints(5, 0, 1, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(0, 0, 0, 0), 0, 0));
    jFilterPanel.add(jFilterCloseButton, new GridBagConstraints(6, 0, 1, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(3, 3, 3, 3), 0, 0));
    jTitlePanel = new JPanel(new BorderLayout(0, 0));
    jTitlePanel.add(jTitleLabel, BorderLayout.CENTER);
    jDescriptionPanel1 = new JPanel(new BorderLayout(0, 0));
    jDescriptionPanel1.add(jDescriptionLabel, BorderLayout.CENTER);
    isDescriptionLabelShown = true;
    jDescriptionPanel2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    jDescriptionPanel2.setVisible(false);
    jOfflinePanel = new JPanel(new BorderLayout(0, 0));
    jOfflinePanel.setVisible(false);
    //jOfflinePanel.setBorder(new LineBorder(Color.BLACK, 1));
    jOfflinePanel.setBackground(Color.decode("0x"+MsgDataRecord.WARNING_BACKGROUND_COLOR));
    JMyLabel label = new JMyLabel("All participants are offline, new messages will be delivered when they sign in.");
    label.setBorder(new EmptyBorder(3, 3, 3, 3));
    jOfflinePanel.add(label, BorderLayout.CENTER);

    if (!suppressUtilityBar)
      jUtilityButtonPanel = new JPanel(new GridBagLayout());

    // If we don't have global from toolbars, we'll setup content toolbars
    if (!suppressToolbar)
      toolBarModel = initToolBarModel(MiscGui.getVisualsKeyName(this), null, null);

    init();

    /** If right mouse button is clicked then the popup is shown. */
    if (recordTableScrollPane instanceof ActionProducerI)
      addMouseListener(new PopupMouseAdapter(this, (ActionProducerI) recordTableScrollPane));

    // Listen on folder changes so we can adjust title and description
    FetchedDataCache.getSingleInstance().addFolderShareRecordListener(folderShareListener = new FolderShareListener());
    // Listen on Contact changes so we can adjust participants
    FetchedDataCache.getSingleInstance().addContactRecordListener(contactListener = new ContactListener());

    if (toolBarModel != null)
      toolBarModel.addComponentActions(this);

    if (trace != null) trace.exit(RecordTableComponent.class);
  }

  public void removeRecordListeners() {
    if (recordTableScrollPane != null) {
      recordTableScrollPane.removeRecordSelectionListeners();
    }
    if (folderShareListener != null) {
      FetchedDataCache.getSingleInstance().removeFolderShareRecordListener(folderShareListener);
      folderShareListener = null;
    }
    if (contactListener != null) {
      FetchedDataCache.getSingleInstance().removeContactRecordListener(contactListener);
      contactListener = null;
    }
  }

  public RecordActionTable getActionTable() {
    if (recordTableScrollPane instanceof RecordActionTable)
      return (RecordActionTable) recordTableScrollPane;
    return null;
  }

  public RecordTableScrollPane getRecordTableScrollPane() {
    return recordTableScrollPane;
  }

  public JPanel getTopPanel() {
    return jTopPanel;
  }

  /**
   * Setting the title overwrites the default title construction when initData() is called.
   * Setting it to 'null' resets the custom title setting.
   */
  public void setTitle(Object title) {
    this.title = title;
    if (title != null) {
      jTitleLabel.setText(ListRenderer.getRenderedText(title, false, false, false));
      jTitleLabel.setIcon(ListRenderer.getRenderedIcon(title));
      validate();
    }
    else if (lastFolderId != null)
      changeTitle(lastFolderId);
  }
  public void setTitleIcon(Icon icon) {
    jTitleLabel.setIcon(icon);
  }
//  /**
//   * Setting the description overwrites the default description construction when initData() is called.
//   * Setting it to 'null' resets the custom title setting.
//   */
//  public void setDescription(String description) {
//    this.description = description;
//    if (description != null) {
//      jDescriptionLabel.setText("<html>"+description+"</html>"); // html gives us multi-line label capability
//      jDescriptionLabel.revalidate();
//    } else if (lastFolderId != null) {
//      changeDescription(lastFolderId);
//    }
//  }
//  /**
//   * Setting the description overwrites the default description construction when initData() is called.
//   * Setting it to 'null' resets the custom title setting.
//   * Use this method if you want to control the GUI elements like alignment or icon positioning...
//   */
//  public void setDescription(JComponent jDescription) {
//    if (jDescription != null) {
//      if (jDescription instanceof JLabel) {
//        this.jDescriptionLabel = (JLabel) jDescription;
//        this.description = jDescriptionLabel.getText();
//      }
//      jDescriptionPanel.removeAll();
//      jDescriptionPanel.setLayout(new BorderLayout(0, 0));
//      jDescriptionPanel.add(jDescription, BorderLayout.CENTER);
//    } else {
//      setDescription((String) null);
//    }
//    this.jDescriptionPanel.revalidate();
//  }
  //public void setDescriptionParticipants(String prefix, String postfix, Folder)
  public void addEntryComponent(JSplitPane splitPane, JComponent entryComponent) {
    mainEntryComp = entryComponent;

    cards.setBorder(new EmptyBorder(0,0,0,0));
    mainEntryComp.setBorder(new EmptyBorder(0,0,0,0));

    // clear old content with no split pane to make room for new
    cardsPanel.removeAll();

    splitPane.setTopComponent(cards);
    splitPane.setBottomComponent(mainEntryComp);

    cardsPanel.add(BorderLayout.CENTER, splitPane);
  }
  public void addPreviewComponent(JSplitPane splitPane, JComponent previewComponent) {
    mainPreviewComp = previewComponent;

    mainTableComp.setBorder(new EmptyBorder(0,0,0,0));
    mainPreviewComp.setBorder(new EmptyBorder(0,0,0,0));

    // link the table with preview panel
    if (mainPreviewComp instanceof RecordSelectionListener)
      getActionTable().addRecordSelectionListener((RecordSelectionListener) mainPreviewComp);

    // clear old content with no split pane to make room for new
    mainCardPanel.removeAll();

    splitPane.setTopComponent(mainTableComp);
    splitPane.setBottomComponent(mainPreviewComp);

    mainCardPanel.add(BorderLayout.CENTER, splitPane);
  }
  public void addTopPanel(JComponent topPanel) {
    add(topPanel, new GridBagConstraints(0, countTopPanels, 1, 1, 10, 0,
        GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL, new MyInsets(0, 0, 0, 0), 0, 0));
    countTopPanels++;
  }
  public void addUtilityComponent(JComponent utilityComponent) {
    if (jUtilityButtonPanel != null) {
      int count = jUtilityButtonPanel.getComponentCount();
      jUtilityButtonPanel.add(utilityComponent, new GridBagConstraints(10-count, 0, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(0, 0, 0, 0), 0, 0));
      this.jUtilityButtonPanel.revalidate();
    }
  }

  private void init() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecordTableComponent.class, "init()");

    // So the split panes are not limited in movement, but must have at least visible header.
    setMinimumSize(new Dimension(0, 24));
    setLayout(new GridBagLayout());
    setBorder(new EmptyBorder(0,0,0,0));

    AbstractButton refreshButton = null;
    AbstractButton cloneButton = null;
    AbstractButton splitLayoutButton = null;
    AbstractButton filterButton = null;

    if (jUtilityButtonPanel != null && recordTableScrollPane instanceof RecordActionTable) {
      RecordActionTable actionTable = (RecordActionTable) recordTableScrollPane;
      Action refreshAction = actionTable.getRefreshAction();
      Action cloneAction = actionTable.getCloneAction();
      Action splitLayoutAction = actionTable.getSplitLayoutAction();
      Action filterAction = actionTable.getFilterAction();

      if (refreshAction != null)
        refreshButton = ActionUtilities.makeSmallComponentToolButton(refreshAction);
      if (cloneAction != null)
        cloneButton = ActionUtilities.makeSmallComponentToolButton(cloneAction);
      if (splitLayoutAction != null)
        splitLayoutButton = ActionUtilities.makeSmallComponentToolButton(splitLayoutAction);
      if (filterAction != null) {
        filterAction.addPropertyChangeListener(new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent evt) {
            String name = evt.getPropertyName();
            Object valueNew = evt.getNewValue();
            if (jFilterPanel != null && name.equalsIgnoreCase("state")) {
              boolean setVisible = ((Boolean) valueNew).booleanValue();
              jFilterPanel.setVisible(setVisible);
              if (!setVisible) {
                setFilterNarrowing(null, jFilterMsgBodyCheck.isSelected());
              } else {
                setFilterNarrowing(jFilterField.getText(), jFilterMsgBodyCheck.isSelected());
                jFilterField.requestFocus();
                jFilterField.selectAll();
              }
            }
          }
        });
        // skip Search Button in utility bar, it already is in the mail toolbar
        // filterButton = ActionUtilities.makeSmallComponentToolButton(filterAction);
      }
    }

    int posY = 0;
    if (toolBarModel == null) {
      jTopPanel.add(jFilterPanel, new GridBagConstraints(0, posY, 4, 1, 10, 0,
          GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new MyInsets(0, 0, 0, 0), 0, 0));
      posY ++;
    }
    jTopPanel.add(jTitlePanel, new GridBagConstraints(0, posY, 1, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(0, 3, 0, 5), 0, 0)); // 1 pixel smaller insets than description to accomodate "shared" folder icons that are larger
    jTopPanel.add(jDescriptionPanel1, new GridBagConstraints(1, posY, 1, 2, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(3, 5, 3, 5), 0, 0));
    JLabel minRowHeight = new JLabel(" ");
    jTopPanel.add(minRowHeight, new GridBagConstraints(2, posY, 1, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, new MyInsets(3, 0, 3, 0), 0, 0));
    if (jUtilityButtonPanel != null) {
      jTopPanel.add(jUtilityButtonPanel, new GridBagConstraints(3, posY, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.VERTICAL, new MyInsets(0, 0, 0, 0), 0, 0));
    }
    posY += 2;
    if (toolBarModel != null) {
      jTopPanel.add(toolBarModel.getToolBar(), new GridBagConstraints(0, posY, 4, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(0, 0, 0, 0), 0, 0));
      posY ++;
      jTopPanel.add(jFilterPanel, new GridBagConstraints(0, posY, 4, 1, 10, 0,
          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(0, 0, 0, 0), 0, 0));
      posY ++;
    }
    jTopPanel.add(jOfflinePanel, new GridBagConstraints(0, posY, 4, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(0, 0, 0, 0), 0, 0));
    posY ++;
    jTopPanel.add(jDescriptionPanel2, new GridBagConstraints(0, posY, 4, 1, 0, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(3, 0, 3, 0), 0, 0));
    posY ++;

    add(jTopPanel, new GridBagConstraints(0, countTopPanels, 1, 1, 10, 0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new MyInsets(0, 0, 0, 0), 0, 0));
    countTopPanels ++;

    if (refreshButton != null) {
      jUtilityButtonPanel.add(refreshButton, new GridBagConstraints(10, 0, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(0, 0, 0, 0), 0, 0));
    }
    if (cloneButton != null) {
      jUtilityButtonPanel.add(cloneButton, new GridBagConstraints(9, 0, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(0, 0, 0, 0), 0, 0));
    }
    if (splitLayoutButton != null) {
      jUtilityButtonPanel.add(splitLayoutButton, new GridBagConstraints(8, 0, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(0, 0, 0, 0), 0, 0));
    }
    if (filterButton != null) {
      jUtilityButtonPanel.add(filterButton, new GridBagConstraints(7, 0, 1, 1, 0, 0,
          GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(0, 0, 0, 0), 0, 0));
    }
    /*
    add(recordTableScrollPane, new GridBagConstraints(0, 1, 5, 1, 60, 60,
        GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new MyInsets(0, 0, 0, 0), 0, 0));
     */

    cards = new JPanel();
    cardLayout = new CardLayout();
    cards.setLayout(cardLayout);
    mainTableComp = recordTableScrollPane;
    final JComponent backTemplate = Template.getTemplate(backTemplateName);
    if (backTemplate != null) {
      FilledLayeredPane pane = new FilledLayeredPane();
      pane.add(backTemplate, JLayeredPane.DEFAULT_LAYER);
      pane.add(recordTableScrollPane, JLayeredPane.PALETTE_LAYER);
      recordTableScrollPane.setOpaque(false);
      recordTableScrollPane.setOpaqueTable(false);
      mainTableComp = pane;
    }
    mainCardPanel = new JPanel();
    mainCardPanel.setLayout(new BorderLayout(0,0));
    mainCardPanel.add(BorderLayout.CENTER, mainTableComp);
    cards.add(mainCardPanel, "table");
    JComponent emptyTemplate = Template.getTemplate(emptyTemplateName);
    if (emptyTemplate != null) {
      JScrollPane scrollPane = new JScrollPane(emptyTemplate);
      scrollPane.setBorder(new EmptyBorder(0,0,0,0));
      cards.add(scrollPane, "emptyTemplate");
    }
    // if Filter action exists, include empty filter results template
    JComponent emptyFilterResultsTemplate = null;
    if (isFilterable()) {
      emptyFilterResultsTemplate = Template.getTemplate(Template.FILTER_NO_RESULTS);
      JScrollPane scrollPane = new JScrollPane(emptyFilterResultsTemplate);
      scrollPane.setBorder(new EmptyBorder(0,0,0,0));
      cards.add(scrollPane, "emptyFilterResultsTemplate");
    }
    JComponent categoryTemplate = Template.getTemplate(categoryTemplateName);
    if (categoryTemplate != null) {
      JScrollPane scrollPane = new JScrollPane(categoryTemplate);
      scrollPane.setBorder(new EmptyBorder(0,0,0,0));
      cards.add(scrollPane, "categoryTemplate");
    }
//    if (emptyTemplate != null) {
//      JScrollPane emptyPane = new JScrollPane(emptyTemplate);
//      FilledLayeredPane pane = new FilledLayeredPane();
//      JComponent backTemplate2 = Template.getTemplate(backTemplateName);
//      if (backTemplate2 != null) {
//        backTemplate2.setOpaque(false);
//        pane.add(backTemplate2, JLayeredPane.DEFAULT_LAYER);
//      }
//      pane.add(emptyPane, JLayeredPane.PALETTE_LAYER);
//      emptyPane.setOpaque(false);
//      cards.add(pane, "template");
//    }
    // add main table as 10'th element leaving room for addon components
    cardsPanel = new JPanel();
    cardsPanel.setLayout(new BorderLayout(0,0));
    cardsPanel.add(BorderLayout.CENTER, cards);
    add(cardsPanel, new GridBagConstraints(0, 10, 1, 1, 20, 20,
        GridBagConstraints.WEST, GridBagConstraints.BOTH, new MyInsets(0, 0, 0, 0), 0, 0));
    // Since we want initially to show the table, wait SHOW_DELAY seconds and if it doesn't fill,
    // show the template.
    // If template exists for this table...
    if (emptyTemplate != null || categoryTemplate != null || emptyFilterResultsTemplate != null) {
      showTableOrDelayedTemplate();
      recordTableScrollPane.getTableModel().addTableModelListener(new TableModelListener() {
        public void tableChanged(TableModelEvent e) {
          showTableOrDelayedTemplate();
        }
      });

      recordTableScrollPane.getTableModel().addParentFolderListener(new ParentFolderListener() {
        public void parentFolderChanged(ParentFolderEvent e) {
          showTableOrDelayedTemplate();
        }
      });
    }

    recordTableScrollPane.setAreaComponent(this);
    restoreVisuals(GlobalProperties.getProperty(MiscGui.getVisualsKeyName(this), "Dimension width 160 height 180"));

    if (trace != null) trace.exit(RecordTableComponent.class);
  }


  private Timer timer = null;
  private final Object timerMonitor = new Object();
  private FolderPair evaluatedFolder = null;
  private void showTableOrDelayedTemplate() {
    FolderPair pfp = recordTableScrollPane.getTableModel().getParentFolderPair();
    int delay = pfp != null ? Template.SHOW_DELAY_FOR_FOLDER_TABLES : Template.SHOW_DELAY_FOR_NON_FOLDER_TABLES;
    showTableOrDelayedTemplate(delay);
  }
  private void showTableOrDelayedTemplate(int delayMillis) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecordTableComponent.class, "showTableOrDelayedTemplate(int delayMillis)");
    synchronized (timerMonitor) {
      boolean runTimer = false;
      RecordTableModel model = recordTableScrollPane.getTableModel();
      FolderPair pfp = model.getParentFolderPair();
      FolderRecord fr = pfp != null ? pfp.getFolderRecord() : null;
      FolderShareRecord fsr = pfp != null ? pfp.getFolderShareRecord() : null;
      if (fr != null && fr.isCategoryType()) {
        cardLayout.show(cards, "categoryTemplate");
      } else {
        int rowCount = model.getRowCount();
        if (rowCount > 0 || fsr == null || !model.isContentFetched(fsr.shareId)) {
          if (trace != null) trace.data(10, "1");
          if (timer != null) timer.stop();
          cardLayout.show(cards, "table");
        } else if (rowCount == 0 && fsr != null && model.isContentFetched(fsr.shareId) && (fr == null || fr.objectCount != null)) {
          if (trace != null) trace.data(20, "2");
          if (timer != null) timer.stop();
          if ((fr != null && fr.objectCount != null && fr.objectCount.longValue() == 0) || recordTableScrollPane.getTableModel().getFilterNarrowing() == null)
            cardLayout.show(cards, "emptyTemplate");
          else
            cardLayout.show(cards, "emptyFilterResultsTemplate");
        }
        if (rowCount == 0 &&
              (   (fsr != null && !model.isContentFetched(fsr.shareId)) ||
                  (fsr == null && model instanceof ContactTableModel)
              )
           )
        {
          runTimer = true;
        }
        if (fr != null && fr.objectCount == null) {
          runTimer = true;
        }
      }
      if (runTimer) {
        if (trace != null) trace.data(30, "timer involved");
        evaluatedFolder = pfp;
        if (timer != null) {
          if (trace != null) trace.data(40, "restarting timer");
          timer.setDelay(delayMillis);
          timer.restart();
        } else {
          if (trace != null) trace.data(50, "new timer needed");
          timer = new Timer(delayMillis, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
              Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(getClass(), "actionPerformed(ActionEvent event)");
              if (trace != null) trace.args(event);
              timer.stop();
              // Run the check in a threaded mode because we don't want to block the gui event thread which calls it.
              performEmptyTableCheck_Threaded();
              if (trace != null) trace.exit(getClass());
            } // actionPerformed()
          });
          timer.start();
        }
      }
    } // end synchronized
    if (trace != null) trace.exit(RecordTableComponent.class);
  } // end showTableOrDelayedTemplate()

  private void performEmptyTableCheck_Threaded() {
    Thread th = new ThreadTraced("Empty Table Checker") {
      public void runTraced() {
        performEmptyTableCheck();
      }
    };
    th.setDaemon(true);
    th.start();
  }
  private void performEmptyTableCheck() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecordTableComponent.class, "performEmptyTableCheck");
    RecordTableModel model = recordTableScrollPane.getTableModel();
    // If still no rows have filled
    FolderPair pfp = model.getParentFolderPair();
    FolderRecord fr = pfp != null ? pfp.getFolderRecord() : null;
    if (model.getRowCount() == 0 || (fr != null && fr.objectCount == null)) { // additional check for no count in case of file folder where folders are listed causing row count > 0 but files are not fetched
      if (trace != null) trace.data(10, "row count still zero");
      // Some tables with null Folder (contact list table) have templates too.
      boolean templateOk = evaluatedFolder == null && model instanceof ContactTableModel;
      templateOk = templateOk || (evaluatedFolder != null && evaluatedFolder == model.getParentFolderPair());
      if (trace != null) trace.data(20, templateOk);
      if (templateOk) {
        long countObjs = 0;
        ServerInterfaceLayer SIL = null;
        FetchedDataCache cache = null;
        boolean replyRecived = false;
        if (evaluatedFolder != null) {
          SIL = MainFrame.getServerInterfaceLayer();
          cache = SIL.getFetchedDataCache();
          // check if still logged in as the same user (no switch identity was in progress) and the folder is still in the cache (not deleted)
          Long myUID = cache.getMyUserId();
          if (myUID != null) {
            if (cache.getFolderRecord(evaluatedFolder.getFolderRecord().folderId) != null) {
              Set groupIDsSet = cache.getFolderGroupIDsSet(myUID);
              if (evaluatedFolder.getFolderShareRecord().isOwnedBy(myUID, groupIDsSet)) {

                ClientMessageAction msgAction = SIL.submitAndFetchReply(new MessageAction(CommandCodes.FLD_Q_GET_FOLDER_SIZE, new Obj_ID_Rq(evaluatedFolder.getFolderShareRecord().shareId)), 60000);

                // default to 1 element if the count times out and we don't know if its empty or not...
                if (msgAction == null) {
                  countObjs = 1;
                }

                DefaultReplyRunner.nonThreadedRun(SIL, msgAction);

                if (msgAction != null && msgAction.getActionCode() == CommandCodes.FLD_A_GET_FOLDER_SIZE) {
                  replyRecived = true;
                  Obj_IDPair_Co data = (Obj_IDPair_Co) msgAction.getMsgDataSet();
                  countObjs = data.objId_1.longValue();
                  evaluatedFolder.getFolderRecord().objectCount = new Long(countObjs);
                }
              }
            }
          }
        }

        boolean refresh = false;
        if (countObjs > 0 && model.getRowCount() == 0 && recordTableScrollPane.getTableModel().getFilterNarrowing() != null) {
          cardLayout.show(cards, "emptyFilterResultsTemplate");
        } else if (countObjs <= 0 && model.getRowCount() == 0) {
          cardLayout.show(cards, "emptyTemplate");
        } else if (model.getRowCount() == 0) {
          if (replyRecived)
            if (recordTableScrollPane.getTableModel().getFilterNarrowing() == null)
              refresh = true;
//        } else if (model instanceof FileTableModel) {
//          // in file folder there may be sub-directories but no files, so refresh if count of files is different - this is possible because all files are fetched at once (no batches)
//          FileLinkRecord[] links = cache.getFileLinkRecordsOwnerAndType(evaluatedFolder.getFolderRecord().folderId, new Short(Record.RECORD_TYPE_FOLDER));
//          if (links != null && links.length != countObjs) {
//            if (replyRecived)
//              if (recordTableScrollPane.getTableModel().getFilterNarrowing() == null)
//                refresh = true;
//          }
        }

        if (refresh) {
          RecordActionTable actionTable = (RecordActionTable) recordTableScrollPane;
          Action refreshAction = actionTable.getRefreshAction();
          if (refreshAction != null) {
            refreshAction.actionPerformed(new ActionEvent(RecordTableComponent.this, 0, "refresh"));
          }
        }
      } // end templateOk
    }
    if (trace != null) trace.exit(RecordTableComponent.class);
  }

  /**
   * This method should take care of setting the TableModel to display specified folders data.
   */
  public abstract void initDataModel(Long folderId);

  /**
   * Initializes to display specified folder's data.  Will call initDataModel(folderId);
   */
  public void initData(Long folderId) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecordTableComponent.class, "initData(Long folderId)");

    initDataModel(folderId);
    if (recordTableScrollPane instanceof RecordActionTable) {
      ((RecordActionTable) recordTableScrollPane).setEnabledActions();
      getActionTable().fireRecordSelectionChanged();
    }

    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    FolderRecord fRec = cache.getFolderRecord(folderId);
    if (fRec != null) {
      jTopPanel.setVisible(!fRec.isCategoryType());
    }
    jTopPanel.setVisible(true);
    if (title == null) {
      changeTitle(folderId);
    }
    if (description == null) {
      changeDescription(folderId);
    }

    if (trace != null) trace.exit(RecordTableComponent.class);
  }

  /**
   * Sets the title of the folder to reflect the description of the given folder.
   */
  private void changeTitle(Long folderId) {
    if (folderId == null) {
      jTitleLabel.setText(null);
      jTitleLabel.setIcon(null);
    }
    else {
      // remember the last folderId
      lastFolderId = folderId;

      // fetch folder title
      String titleName = "";
      Icon titleIcon = null;
      FetchedDataCache cache = FetchedDataCache.getSingleInstance();
      FolderRecord fRec = cache.getFolderRecord(folderId);
      FolderShareRecord sRec = cache.getFolderShareRecordMy(folderId, true);
      if (fRec != null && sRec != null) {
        FolderPair fPair = new FolderPair(sRec, fRec);
        titleName = ListRenderer.getRenderedText(fPair, false, false, false);
        titleIcon = ListRenderer.getRenderedIcon(fPair);
      }

      jTitleLabel.setText(titleName);
      jTitleLabel.setIcon(titleIcon);
    }
    jTitleLabel.revalidate();
    jTitleLabel.repaint();
  }
  /**
   * Sets the description of the folder to reflect the description of the given folder.
   */
  private void changeDescription(Long folderId) {
    boolean isParticipantsPanelMade = false;
    boolean isAnyOnline = false;
    if (folderId == null) {
      jDescriptionLabel.setText(null);
    } else {
      // remember the last folderId
      lastFolderId = folderId;

      // fetch folder description
      String desc = "";
      FetchedDataCache cache = FetchedDataCache.getSingleInstance();
      FolderShareRecord share = null;
      try {
        share = cache.getFolderShareRecordMy(folderId, true);
        if (share == null) {
          desc = "Folder has been REMOVED";
        } else {
          FolderRecord fRec = cache.getFolderRecord(folderId);
          if (fRec != null && fRec.isChatting() && fRec.numOfShares.shortValue() > 1) {
            // chatting folders use altered description or participant list if unmodified description
            String shareDesc = null;
            if (share != null)
              shareDesc = share.getFolderDesc();
            String originalDesc = com.CH_gui.lang.Lang.rb.getString("folderDesc_Chat_Log_common_beginning");
            if (shareDesc != null &&
                shareDesc.length() > 0 &&
                !shareDesc.startsWith(originalDesc)) {
              desc = shareDesc;
            } else {
              //String[] notes = FolderTree.getOwnerAndChatNote(fRec);
              //desc = "Participants: " + notes[0] + " " + notes[1];
              // correct the Chat Log title to ommit the participants...
              String name  = share.getFolderName();
              String defaultChatFolderName = com.CH_gui.lang.Lang.rb.getString("folderName_Chat_Log");
              if (name.startsWith(defaultChatFolderName))
                jTitleLabel.setText("Chat");
            }
          } else {
            desc = share.getFolderDesc();
          }
          // if still no description, make something generic
          if (desc == null || desc.length() == 0) {
            if (fRec != null) {
              if (fRec.numOfShares.shortValue() == 1) {
                desc = "Private " + fRec.getFolderType();
                if (fRec.isChatting() && fRec.numOfShares.shortValue() == 1)
                  desc += " archive.";
              } else if (fRec.isGroupType()) {
                if (fRec.ownerUserId.equals(cache.getMyUserId()))
                  desc = "Your shared " + fRec.getFolderType();
                else {
                  Record owner = MsgPanelUtils.convertUserIdToFamiliarUser(fRec.ownerUserId, true, false, true);
                  if (owner == null) {
                    owner = new UserRecord();
                    ((UserRecord) owner).userId = fRec.ownerUserId;
                  }
                  desc = "Shared " + fRec.getFolderType() + ", owner " + ListRenderer.getRenderedText(owner);
                }
              }
            }
          }

          if (fRec != null && fRec.numOfShares.shortValue() > 1 && !fRec.isGroupType()) {
            isParticipantsPanelMade = true;
            isParticipantComponentsListed = true;
            JPanel panel = null;
            // prep panels
            if (desc == null || desc.length() == 0) {
              jDescriptionPanel1.removeAll();
              jDescriptionPanel1.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
              jDescriptionPanel2.setVisible(false);
              panel = jDescriptionPanel1;
              isDescriptionLabelShown = false;
            } else {
              if (!isDescriptionLabelShown) {
                jDescriptionPanel1.removeAll();
                jDescriptionPanel1.setLayout(new BorderLayout(0, 0));
                jDescriptionPanel1.add(jDescriptionLabel, BorderLayout.CENTER);
                isDescriptionLabelShown = true;
              }
              jDescriptionPanel2.removeAll();
              jDescriptionPanel2.setVisible(true);
              panel = jDescriptionPanel2;
            }
            Record[] participants = getFolderParticipants(fRec);
            Record owner = participants[0];
            // sort the list, first active contacts, then other contacts, then groups, then users
            Arrays.sort(participants, new Comparator() {
              public int compare(Object o1, Object o2) {
                int rc = 0;
                rc = new Integer(getSortInstanceValue(o1)).compareTo(new Integer(getSortInstanceValue(o2)));
                if (rc == 0 && o1 instanceof ContactRecord && o2 instanceof ContactRecord) {
                  rc = -((ContactRecord) o1).status.compareTo(((ContactRecord) o2).status);
                }
                if (rc == 0) {
                  ListRenderer.getRenderedText(o1).compareTo(ListRenderer.getRenderedText(o2));
                }
                return rc;
              }
              private int getSortInstanceValue(Object o) {
                if (o instanceof ContactRecord)
                  return 1;
                else if (o instanceof FolderRecord)
                  return 2;
                else if (o instanceof UserRecord)
                  return 3;
                else
                  return 4;
              }
            });
            for (int i=0; i<participants.length; i++) {
              String text = ListRenderer.getRenderedText(participants[i]);
              if (participants[i] instanceof UserRecord) {
                UserRecord uRec = (UserRecord) participants[i];
                if (uRec.userId.equals(cache.getMyUserId())) {
                  text = "me";
                  continue; // skip myself in the list
                } else {
                  text = uRec.handle != null && uRec.handle.length() > 0 ? uRec.handle : uRec.shortInfo();
                }
              } else if (participants[i] instanceof ContactRecord) {
                ContactRecord cRec = (ContactRecord) participants[i];
                isAnyOnline |= cRec.isOnlineStatus();
              } else if (participants[i] instanceof FolderRecord) {
                FolderRecord fldRec = (FolderRecord) participants[i];
                if (fldRec.isGroupType()) {
                  isAnyOnline = true;
                }
              }
              // participants got sorted so compare using "equals" method
              if (participants[i] != null && participants[i].equals(owner))
                text = "[" + text + "]";
              JLabel label = new JMyLabel(text);
              label.setBorder(new EmptyBorder(0,3,0,3));
              label.setIconTextGap(2);
              if (participants[i] instanceof ContactRecord) {
                ContactRecord contactRecord = (ContactRecord) participants[i];
                label.setIcon(ContactRecUtil.getStatusIcon(contactRecord.status, contactRecord.ownerUserId));
              } else {
                label.setIcon(ListRenderer.getRenderedIcon(participants[i]));
              }
              panel.add(label);
            }
            jOfflinePanel.setVisible(fRec.isChatting() && !isAnyOnline);
          }
        } // end share != null
      } catch (Throwable t) {
        // if I'm not logged in, we can expect Exception, just note it in description...
        System.out.println(t.getMessage());
        t.printStackTrace();
        desc = "Folder could not be located.";
      }
      jDescriptionLabel.setText("<html>"+desc+"</html>"); // html gives us multi-line label capability
    }
    if (!isParticipantsPanelMade) {
      if (isParticipantComponentsListed) {
        isParticipantComponentsListed = false;
        jDescriptionPanel2.setVisible(false);
        jOfflinePanel.setVisible(false);
      }
      if (!isDescriptionLabelShown) {
        jDescriptionPanel1.removeAll();
        jDescriptionPanel1.setLayout(new BorderLayout(0, 0));
        jDescriptionPanel1.add(jDescriptionLabel, BorderLayout.CENTER);
        isDescriptionLabelShown = true;
      }
    }
    jDescriptionPanel1.revalidate();
    jDescriptionPanel1.repaint();
    if (jDescriptionPanel2.isVisible()) {
      jDescriptionPanel2.revalidate();
      jDescriptionPanel2.repaint();
    }
  }

  /**
   *
   * @param fRec
   * @return Array of all participants starting with folder owner
   */
  public Record[] getFolderParticipants(FolderRecord fRec) {
    FetchedDataCache cache = FetchedDataCache.getSingleInstance();
    Long ownerUserId = fRec.ownerUserId;
    ArrayList participantsL = new ArrayList();
    participantsL.add(MsgPanelUtils.convertUserIdToFamiliarUser(ownerUserId, true, true));
    FolderShareRecord[] allShares = cache.getFolderShareRecordsForFolder(fRec.folderId);
    for (int i=0; i<allShares.length; i++) {
      FolderShareRecord share = allShares[i];
      // all participants other than owner because he is already added
      if (share.isOwnedByGroup() || !share.isOwnedBy(ownerUserId, (Long[]) null)) {
        Record recipient = null;
        if (share.isOwnedByUser())
          recipient = MsgPanelUtils.convertUserIdToFamiliarUser(share.ownerUserId, true, true);
        else
          recipient = FetchedDataCache.getSingleInstance().getFolderRecord(share.ownerUserId);
        if (recipient != null)
          participantsL.add(recipient);
        else {
          if (share.isOwnedByUser()) {
            UserRecord usrRec = new UserRecord();
            usrRec.userId = share.ownerUserId;
            participantsL.add(usrRec);
          } else {
            FolderRecord fldRec = new FolderRecord();
            fldRec.folderId = share.ownerUserId;
            fldRec.folderType = new Short(FolderRecord.GROUP_FOLDER);
            participantsL.add(fldRec);
          }
        }
      }
    }
    Record[] participants = (Record[]) ArrayUtils.toArray(participantsL, Record.class);
    return participants;
  }

  /**
   * Folder Share listener to update title and description labels when they change.
   */
  private class FolderShareListener implements FolderShareRecordListener {
    public void folderShareRecordUpdated(FolderShareRecordEvent event) {
      // to prevent deadlocks, run in seperate thread
      javax.swing.SwingUtilities.invokeLater(new GUIUpdater(event));
    }
  }

  /**
   * Contacts listener to update participants list when they change.
   */
  private class ContactListener implements ContactRecordListener {
    public void contactRecordUpdated(ContactRecordEvent event) {
      // to prevent deadlocks, run in seperate thread
      javax.swing.SwingUtilities.invokeLater(new GUIUpdater(event));
    }
  }

  private class GUIUpdater implements Runnable {
    private RecordEvent event;
    public GUIUpdater(RecordEvent event) {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(GUIUpdater.class, "GUIUpdater(RecordEvent event)");
      this.event = event;
      if (trace != null) trace.exit(GUIUpdater.class);
    }
    public void run() {
      Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(GUIUpdater.class, "GUIUpdater.run()");
      if (recordTableScrollPane != null) {
        FolderPair folderPair = recordTableScrollPane.getTableModel().getParentFolderPair();
        if (folderPair != null) {
          FolderShareRecord parentShare = folderPair.getFolderShareRecord();
          if (parentShare != null) {
            if (event instanceof FolderShareRecordEvent) {
              FolderShareRecordEvent shareEvent = (FolderShareRecordEvent) event;
              FolderShareRecord[] shareRecords = shareEvent.getFolderShareRecords();
              for (int i=0; i<shareRecords.length; i++) {
                // If changing any of this folder's shares
                if (parentShare.folderId.equals(shareRecords[i].folderId)) {
                  if (event.getEventType() == RecordEvent.SET) {
                    changeTitle(shareRecords[i].folderId);
                  }
                  // participants might have been ADDED or REMOVED
                  changeDescription(shareRecords[i].folderId);
                  break;
                }
              }
            } else if (event instanceof ContactRecordEvent) {
              // contact status change, update description if we are listing participants
              if (isParticipantComponentsListed) {
                changeDescription(parentShare.folderId);
              }
            }
          }
        }
      }
      // Runnable, not a custom Thread -- DO NOT clear the trace stack as it is run by the AWT-EventQueue Thread.
      if (trace != null) trace.exit(GUIUpdater.class);
    }
  }

  /***********************************
  *** F i l t e r    M e t h o d s ***
  ***********************************/
  public boolean isFilterable() {
    return ((RecordActionTable) RecordTableComponent.this.recordTableScrollPane).getFilterAction() != null;
  }
//  public Object[] getFilterProperties() {
//    return new Object[] { getFilterString(), isFilterEnabled() };
//  }
//  public void setFilterProperties(Object[] filterProps) {
//    if (filterProps != null) {
//      setFilterString((String) filterProps[0]);
//      setFilterEnabled((Boolean) filterProps[1]);
//    }
//  }
  public void setFilterFromComponent(RecordTableComponent tableComp) {
    setFilterString(tableComp.getFilterString());
    setFilterIncludeMsgBodies(tableComp.getFilterIncludeMsgBodies());
    setFilterEnabled(tableComp.isFilterEnabled());
    RecordFilter filter = tableComp.recordTableScrollPane.getTableModel().getFilterNarrowing();
    if (filter instanceof TextSearchFilter) {
      RecordTableModel tableModel = recordTableScrollPane.getTableModel();
      boolean includingMsgBodies = getFilterIncludeMsgBodies(); // take default from GUI
      if (tableModel instanceof MsgTableModel) {
        if (((MsgTableModel) tableModel).isModeMsgBody())
          includingMsgBodies = true;
      }
      ((TextSearchFilter) filter).setIncludingMsgBodies(includingMsgBodies);
    }
    recordTableScrollPane.getTableModel().setFilterNarrowing(filter);
    showTableOrDelayedTemplate();
  }
  public boolean getFilterIncludeMsgBodies() {
    return jFilterMsgBodyCheck.isSelected();
  }
  public String getFilterString() {
    return jFilterField.getText().trim();
  }
  public void setFilterIncludeMsgBodies(boolean includeMsgBodies) {
    jFilterMsgBodyCheck.setSelected(includeMsgBodies);
  }
  public void setFilterString(String match) {
    jFilterField.setText(match);
  }
  public Boolean isFilterEnabled() {
    return Boolean.valueOf(jFilterPanel.isVisible());
  }
  public void setFilterEnabled(Boolean enableFilter) {
    if (enableFilter != null)
      ((RecordActionTable) RecordTableComponent.this.recordTableScrollPane).getFilterAction().putValue(Actions.STATE_CHECK, enableFilter);
  }
  public void setFilterNarrowing(String filterStr, boolean includeMsgBodies) {
    // Memorize current selection so we can attempt to restore it after filter is changed.
    List selectionL = recordTableScrollPane.getSelectedRecordsL();
    if (filterStr != null && filterStr.trim().length() > 0)
      recordTableScrollPane.getTableModel().setFilterNarrowing(new TextSearchFilter(filterStr, includeMsgBodies, recordTableScrollPane.getTableModel()));
    else
      recordTableScrollPane.getTableModel().setFilterNarrowing(null);
    // Try restoring selection
    boolean anySelected = false;
    if (selectionL != null) {
      for (int i=0; i<selectionL.size(); i++) {
        int row = recordTableScrollPane.getTableModel().getRowForObject(((Record) selectionL.get(i)).getId());
        if (row >= 0) {
          int viewRow = recordTableScrollPane.getJSortedTable().convertMyRowIndexToView(row);
          if (viewRow >= 0) {
            if (!anySelected) {
              recordTableScrollPane.getJSortedTable().getSelectionModel().setSelectionInterval(viewRow, viewRow);
              Rectangle rect = recordTableScrollPane.getJSortedTable().getCellRect(viewRow, 0, true);
              recordTableScrollPane.getJSortedTable().scrollRectToVisible(rect);
              anySelected = true;
            } else {
              recordTableScrollPane.getJSortedTable().getSelectionModel().addSelectionInterval(viewRow, viewRow);
            }
          }
        }
      }
    }
  }

  /***********************************************************
  *** T o o l B a r P r o d u c e r I    interface methods ***
  ***********************************************************/
  public ToolBarModel getToolBarModel() {
    return toolBarModel;
  }
  public String getToolBarTitle() {
    return jTitleLabel.getText();
  }
  public ToolBarModel initToolBarModel(String propertyKeyName, String toolBarName, Component sourceComponent) {
    if (!JActionFrame.ENABLE_FRAME_TOOLBARS && toolBarModel == null)
      toolBarModel = new ToolBarModel(propertyKeyName, toolBarName != null ? toolBarName : propertyKeyName, false);
    if (toolBarModel != null && sourceComponent != null)
      toolBarModel.addComponentActions(sourceComponent);
    return toolBarModel;
  }

  /*******************************************************
  *** V i s u a l s S a v a b l e    interface methods ***
  *******************************************************/

  public String getVisuals() {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecordTableComponent.class, "getVisuals()");

    String rc = null;

    if (!suppressVisualsSavable) {
      StringBuffer visuals = new StringBuffer();
      visuals.append("Dimension width ");
      Dimension dim = getSize();
      visuals.append(dim.width);
      visuals.append(" height ");
      visuals.append(dim.height);
      rc = visuals.toString();
    }

    if (trace != null) trace.exit(RecordTableComponent.class, rc);
    return rc;
  }

  public void restoreVisuals(String visuals) {
    Trace trace = null;  if (Trace.DEBUG) trace = Trace.entry(RecordTableComponent.class, "restoreVisuals(String visuals)");
    if (trace != null) trace.args(visuals);

    if (!suppressVisualsSavable) {
      try {
        StringTokenizer st = new StringTokenizer(visuals);
        st.nextToken();
        st.nextToken();
        int width = Integer.parseInt(st.nextToken());
        st.nextToken();
        int height = Integer.parseInt(st.nextToken());
        setPreferredSize(new Dimension(width, height));
      } catch (Throwable t) {
        if (trace != null) trace.exception(RecordTableComponent.class, 100, t);
      }
    }

    if (trace != null) trace.exit(RecordTableComponent.class);
  }
  public String getExtension() {
    return null;
  }
  public Integer getVisualsVersion() {
    return null;
  }
  public boolean isVisuallyTraversable() {
    return true;
  }

  /**
   * I N T E R F A C E   M E T H O D  ---   D i s p o s a b l e O b j  *****
   * Dispose the object and release resources to help in garbage collection.
  */
  public void disposeObj() {
    removeRecordListeners();
    if (recordTableScrollPane != null) {
      recordTableScrollPane.disposeObj();
    }
  }

}