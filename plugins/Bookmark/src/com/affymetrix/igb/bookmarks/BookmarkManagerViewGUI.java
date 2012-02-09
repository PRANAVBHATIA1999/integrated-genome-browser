package com.affymetrix.igb.bookmarks;

import com.affymetrix.common.CommonUtils;
import com.affymetrix.igb.bookmarks.action.AddFolderAction;
import com.affymetrix.igb.bookmarks.action.AddPositionBookmarkAction;
import com.affymetrix.igb.bookmarks.action.AddSeparatorAction;
import com.affymetrix.igb.bookmarks.action.BookmarkActionManager;
import com.affymetrix.igb.osgi.service.IGBService;
import com.affymetrix.igb.osgi.service.IGBTabPanel;
import java.awt.Rectangle;
import java.util.ResourceBundle;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

/**
 *
 * @author Nick & David
 */
public class BookmarkManagerViewGUI extends IGBTabPanel {

	private static final long serialVersionUID = 1L;
	private static BookmarkManagerViewGUI singleton;
	public static final ResourceBundle BUNDLE = ResourceBundle.getBundle("bookmark");
	private static final int TAB_POSITION = 9;
	public static BookmarkManagerView bmv;

	public static void init(IGBService _igbService) {
		singleton = new BookmarkManagerViewGUI(_igbService);
	}

	public static synchronized BookmarkManagerViewGUI getSingleton() {
		return singleton;
	}

	public BookmarkManagerView getBookmarkManagerView() {
		return bmv;
	}

	/** Creates new form BookMarkManagerViewGUI */
	public BookmarkManagerViewGUI(IGBService _igbService) {
		super(_igbService, BUNDLE.getString("bookmarksTab"), BUNDLE.getString("bookmarksTab"), false, TAB_POSITION);
		BookmarkManagerView.init(_igbService);
		bmv = BookmarkManagerView.getSingleton();
		initComponents();
	}

	@Override
	public TabState getDefaultState() {
		return TabState.COMPONENT_STATE_RIGHT_TAB;
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        splitPane = new javax.swing.JSplitPane();
        bmPanel = new javax.swing.JPanel();
        bottomPanel = new javax.swing.JPanel();
        backwardActionButton = bmv.backwardButton;
        forwardActionButton = bmv.forwardButton;
        bmScrollPane = new javax.swing.JScrollPane();
        bookmarkTree = bmv.tree;
        upPanel = new javax.swing.JPanel();
        addFolderButton = bmv.addFolderButton;
        addBookmarkActionButton = bmv.addBookmarkButton;
        removeBookmarkActionButton = bmv.deleteBookmarkButton;
        importButton = new javax.swing.JButton();
        exportButton = new javax.swing.JButton();
        addSeparator = bmv.addSeparatorButton;
        tabPane = new javax.swing.JTabbedPane();
        editorPanel = new javax.swing.JPanel();
        nameTextField = bmv.thing.name_text_field;
        nameLabel = new javax.swing.JLabel();
        undoNameButton = new javax.swing.JButton();
        redoNameButton = new javax.swing.JButton();
        cScrollPane = new javax.swing.JScrollPane();
        commentTextArea = bmv.thing.comment_text_area;
        commentLabel = new javax.swing.JLabel();
        propertiesPanel = new javax.swing.JPanel();
        pScrollPane = new javax.swing.JScrollPane();
        infoTable = bmv.thing.bookmarkData.getInfoTable();

        splitPane.setDividerLocation(400);
        splitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        backwardActionButton.setIcon(CommonUtils.getInstance().getIcon("images/backward.png"));
        backwardActionButton.setToolTipText("Click to go back (Previous visited bookmark)");
        backwardActionButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        backwardActionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backwardActionButtonActionPerformed(evt);
            }
        });

        forwardActionButton.setIcon(CommonUtils.getInstance().getIcon("images/forward.png"));
        forwardActionButton.setToolTipText("Click to go forward (Next visited bookmark)");
        forwardActionButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        forwardActionButton.setPreferredSize(new java.awt.Dimension(24, 24));
        forwardActionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                forwardActionButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout bottomPanelLayout = new org.jdesktop.layout.GroupLayout(bottomPanel);
        bottomPanel.setLayout(bottomPanelLayout);
        bottomPanelLayout.setHorizontalGroup(
            bottomPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(backwardActionButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE)
            .add(forwardActionButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE)
        );
        bottomPanelLayout.setVerticalGroup(
            bottomPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, bottomPanelLayout.createSequentialGroup()
                .add(0, 0, 0)
                .add(backwardActionButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(forwardActionButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        backwardActionButton.setBorder(null);
        forwardActionButton.setBorder(null);

        bmScrollPane.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                bmScrollPaneMouseDragged(evt);
            }
        });

        bookmarkTree.setModel(bmv.thing.def_tree_model);
        bmScrollPane.setViewportView(bookmarkTree);

        addFolderButton.setIcon(CommonUtils.getInstance().getIcon("images/addFolder.png"));
        addFolderButton.setToolTipText("Add a New Folder");
        addFolderButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        addFolderButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addFolderButtonActionPerformed(evt);
            }
        });

        addBookmarkActionButton.setIcon(CommonUtils.getInstance().getIcon("images/addBookmark.png"));
        addBookmarkActionButton.setToolTipText("Add a new Bookmark");
        addBookmarkActionButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        addBookmarkActionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addBookmarkActionButtonActionPerformed(evt);
            }
        });

        removeBookmarkActionButton.setIcon(CommonUtils.getInstance().getIcon("images/removeBookmark.png"));
        removeBookmarkActionButton.setToolTipText("Remove a bookmark");
        removeBookmarkActionButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        removeBookmarkActionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeBookmarkActionButtonActionPerformed(evt);
            }
        });

        importButton.setIcon(CommonUtils.getInstance().getIcon("images/import.png"));
        importButton.setToolTipText("Import");
        importButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        importButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importButtonActionPerformed(evt);
            }
        });

        exportButton.setIcon(CommonUtils.getInstance().getIcon("images/export.png"));
        exportButton.setToolTipText("Export");
        exportButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        exportButton.setPreferredSize(new java.awt.Dimension(2, 2));
        exportButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportButtonActionPerformed(evt);
            }
        });

        addSeparator.setIcon(CommonUtils.getInstance().getIcon("images/separator.png"));
        addSeparator.setToolTipText("Add new separator");
        addSeparator.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        addSeparator.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addSeparatorActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout upPanelLayout = new org.jdesktop.layout.GroupLayout(upPanel);
        upPanel.setLayout(upPanelLayout);
        upPanelLayout.setHorizontalGroup(
            upPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(addFolderButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE)
            .add(upPanelLayout.createSequentialGroup()
                .add(addSeparator, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE)
                .add(0, 0, 0))
            .add(upPanelLayout.createSequentialGroup()
                .add(addBookmarkActionButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE)
                .add(0, 0, 0))
            .add(upPanelLayout.createSequentialGroup()
                .add(upPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(removeBookmarkActionButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 28, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(importButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE)
                    .add(exportButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE))
                .add(0, 0, 0))
        );

        upPanelLayout.linkSize(new java.awt.Component[] {addBookmarkActionButton, addFolderButton, addSeparator, exportButton, importButton, removeBookmarkActionButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        upPanelLayout.setVerticalGroup(
            upPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(upPanelLayout.createSequentialGroup()
                .add(addFolderButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(4, 4, 4)
                .add(addSeparator, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(4, 4, 4)
                .add(addBookmarkActionButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(removeBookmarkActionButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(4, 4, 4)
                .add(importButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(4, 4, 4)
                .add(exportButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        upPanelLayout.linkSize(new java.awt.Component[] {addBookmarkActionButton, addFolderButton, addSeparator, importButton, removeBookmarkActionButton}, org.jdesktop.layout.GroupLayout.VERTICAL);

        addFolderButton.setBorder(null);
        addBookmarkActionButton.setBorder(null);
        removeBookmarkActionButton.setBorder(null);
        importButton.setBorder(null);
        exportButton.setBorder(null);
        addSeparator.setBorder(null);

        org.jdesktop.layout.GroupLayout bmPanelLayout = new org.jdesktop.layout.GroupLayout(bmPanel);
        bmPanel.setLayout(bmPanelLayout);
        bmPanelLayout.setHorizontalGroup(
            bmPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(bmPanelLayout.createSequentialGroup()
                .add(bmPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(upPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(bottomPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(1, 1, 1)
                .add(bmScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 191, Short.MAX_VALUE))
        );
        bmPanelLayout.setVerticalGroup(
            bmPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(bmPanelLayout.createSequentialGroup()
                .add(upPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 159, Short.MAX_VALUE)
                .add(bottomPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(15, 15, 15))
            .add(bmScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 398, Short.MAX_VALUE)
        );

        splitPane.setLeftComponent(bmPanel);

        tabPane.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                tabPaneStateChanged(evt);
            }
        });

        nameTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                nameTextFieldKeyReleased(evt);
            }
        });

        nameLabel.setText("Name:");

        undoNameButton.setIcon(CommonUtils.getInstance().getIcon("images/undo.png"));
        undoNameButton.setToolTipText("Undo");
        undoNameButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        undoNameButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                undoNameButtonActionPerformed(evt);
            }
        });

        redoNameButton.setIcon(CommonUtils.getInstance().getIcon("images/redo.png"));
        redoNameButton.setToolTipText("Redo");
        redoNameButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        redoNameButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                redoNameButtonActionPerformed(evt);
            }
        });

        commentTextArea.setColumns(20);
        commentTextArea.setLineWrap(true);
        commentTextArea.setRows(5);
        commentTextArea.setWrapStyleWord(true);
        commentTextArea.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                commentTextAreaKeyReleased(evt);
            }
        });
        cScrollPane.setViewportView(commentTextArea);

        commentLabel.setText("Comment:");

        org.jdesktop.layout.GroupLayout editorPanelLayout = new org.jdesktop.layout.GroupLayout(editorPanel);
        editorPanel.setLayout(editorPanelLayout);
        editorPanelLayout.setHorizontalGroup(
            editorPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(editorPanelLayout.createSequentialGroup()
                .add(commentLabel)
                .addContainerGap(134, Short.MAX_VALUE))
            .add(nameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 199, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, editorPanelLayout.createSequentialGroup()
                .add(nameLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 107, Short.MAX_VALUE)
                .add(undoNameButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(4, 4, 4)
                .add(redoNameButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .add(cScrollPane, 0, 0, Short.MAX_VALUE)
        );

        editorPanelLayout.linkSize(new java.awt.Component[] {redoNameButton, undoNameButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        editorPanelLayout.setVerticalGroup(
            editorPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(editorPanelLayout.createSequentialGroup()
                .add(editorPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(undoNameButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(redoNameButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(nameLabel))
                .add(0, 0, 0)
                .add(nameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(4, 4, 4)
                .add(commentLabel)
                .add(4, 4, 4)
                .add(cScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 27, Short.MAX_VALUE))
        );

        editorPanelLayout.linkSize(new java.awt.Component[] {redoNameButton, undoNameButton}, org.jdesktop.layout.GroupLayout.VERTICAL);

        undoNameButton.setBorder(null);
        redoNameButton.setBorder(null);

        tabPane.addTab("Comments", editorPanel);

        pScrollPane.setViewportView(infoTable);

        org.jdesktop.layout.GroupLayout propertiesPanelLayout = new org.jdesktop.layout.GroupLayout(propertiesPanel);
        propertiesPanel.setLayout(propertiesPanelLayout);
        propertiesPanelLayout.setHorizontalGroup(
            propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(pScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 199, Short.MAX_VALUE)
        );
        propertiesPanelLayout.setVerticalGroup(
            propertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, pScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 103, Short.MAX_VALUE)
        );

        tabPane.addTab("Infomation", propertiesPanel);

        splitPane.setRightComponent(tabPane);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(splitPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 224, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(splitPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 560, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

	private void exportButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportButtonActionPerformed
		bmv.export_action.actionPerformed(evt);
	}//GEN-LAST:event_exportButtonActionPerformed

	private void addFolderButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addFolderButtonActionPerformed
		AddFolderAction.getAction().actionPerformed(evt);
	}//GEN-LAST:event_addFolderButtonActionPerformed

	private void backwardActionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backwardActionButtonActionPerformed
		if (bmv.history_pointer > 0) {
			bmv.history_pointer--;
			TreePath path = bmv.bookmark_history.get(bmv.history_pointer);
			bmv.tree.setSelectionPath(path);
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
			if (node.getUserObject() instanceof Bookmark) {
				Bookmark bm = (Bookmark) node.getUserObject();
				BookmarkController.viewBookmark(bmv.thing.igbService, bm);
				backwardActionButton.setEnabled(bmv.history_pointer > 0);
				forwardActionButton.setEnabled(true);
			}
		}
	}//GEN-LAST:event_backwardActionButtonActionPerformed

	private void forwardActionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_forwardActionButtonActionPerformed
		if (bmv.history_pointer < bmv.bookmark_history.size() - 1) {
			bmv.history_pointer++;
			TreePath path = bmv.bookmark_history.get(bmv.history_pointer);
			bmv.tree.setSelectionPath(path);
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
			Bookmark bm = (Bookmark) node.getUserObject();
			BookmarkController.viewBookmark(bmv.thing.igbService, bm);
			forwardActionButton.setEnabled(bmv.history_pointer < bmv.bookmark_history.size() - 1);
			backwardActionButton.setEnabled(true);
		}
	}//GEN-LAST:event_forwardActionButtonActionPerformed

	private void bmScrollPaneMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bmScrollPaneMouseDragged
		Rectangle r = new Rectangle(evt.getX(), evt.getY(), 1, 1);
		scrollRectToVisible(r);
	}//GEN-LAST:event_bmScrollPaneMouseDragged

	private void addBookmarkActionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addBookmarkActionButtonActionPerformed
		AddPositionBookmarkAction.getAction().actionPerformed(evt);
}//GEN-LAST:event_addBookmarkActionButtonActionPerformed

	private void importButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importButtonActionPerformed
		bmv.import_action.actionPerformed(evt);
		BookmarkActionManager.getInstance().rebuildMenus();
}//GEN-LAST:event_importButtonActionPerformed

	private void removeBookmarkActionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeBookmarkActionButtonActionPerformed
		bmv.delete_action.actionPerformed(evt);
		BookmarkActionManager.getInstance().rebuildMenus();
	}//GEN-LAST:event_removeBookmarkActionButtonActionPerformed

	private void addSeparatorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addSeparatorActionPerformed
		AddSeparatorAction.getAction().actionPerformed(evt);
	}//GEN-LAST:event_addSeparatorActionPerformed

	private void undoNameButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_undoNameButtonActionPerformed
		try {
			bmv.thing.undoManager.undo();
			bmv.thing.updateBookmarkData();
		} catch (CannotUndoException ex) {
		}
	}//GEN-LAST:event_undoNameButtonActionPerformed

	private void redoNameButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_redoNameButtonActionPerformed
		try {
			bmv.thing.undoManager.redo();
			bmv.thing.updateBookmarkData();
		} catch (CannotRedoException ex) {
		}
	}//GEN-LAST:event_redoNameButtonActionPerformed

	private void nameTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_nameTextFieldKeyReleased
		bmv.thing.updateBookmarkData();
	}//GEN-LAST:event_nameTextFieldKeyReleased

	private void commentTextAreaKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_commentTextAreaKeyReleased
		bmv.thing.updateBookmarkData();
	}//GEN-LAST:event_commentTextAreaKeyReleased

	private void tabPaneStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tabPaneStateChanged
		if (tabPane.getSelectedIndex() == 1) {
			bmv.thing.updateInfoTable();
		}
	}//GEN-LAST:event_tabPaneStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addBookmarkActionButton;
    private javax.swing.JButton addFolderButton;
    private javax.swing.JButton addSeparator;
    private javax.swing.JButton backwardActionButton;
    private javax.swing.JPanel bmPanel;
    private javax.swing.JScrollPane bmScrollPane;
    private javax.swing.JTree bookmarkTree;
    private javax.swing.JPanel bottomPanel;
    private javax.swing.JScrollPane cScrollPane;
    private javax.swing.JLabel commentLabel;
    private javax.swing.JTextArea commentTextArea;
    private javax.swing.JPanel editorPanel;
    private javax.swing.JButton exportButton;
    private javax.swing.JButton forwardActionButton;
    private javax.swing.JButton importButton;
    private javax.swing.JTable infoTable;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JScrollPane pScrollPane;
    private javax.swing.JPanel propertiesPanel;
    private javax.swing.JButton redoNameButton;
    private javax.swing.JButton removeBookmarkActionButton;
    private javax.swing.JSplitPane splitPane;
    private javax.swing.JTabbedPane tabPane;
    private javax.swing.JButton undoNameButton;
    private javax.swing.JPanel upPanel;
    // End of variables declaration//GEN-END:variables
}
