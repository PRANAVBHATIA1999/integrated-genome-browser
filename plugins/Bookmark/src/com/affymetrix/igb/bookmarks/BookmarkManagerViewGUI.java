package com.affymetrix.igb.bookmarks;

import com.affymetrix.common.CommonUtils;
import com.affymetrix.igb.bookmarks.action.AddFolderAction;
import com.affymetrix.igb.bookmarks.action.AddPositionAndDataBookmarkAction;
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
 * @author lorainelab
 */
public class BookmarkManagerViewGUI extends IGBTabPanel {

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

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        bookmarkTree = bmv.tree;
        PropertiesPanel = new javax.swing.JPanel();
        nameTextField = bmv.thing.name_text_field;
        nameLabel = new javax.swing.JLabel();
        undoNameButton = new javax.swing.JButton();
        redoNameButton = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        commentTextArea = bmv.thing.comment_text_area;
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        backwardActionButton = bmv.backwardButton;
        forwardActionButton = bmv.forwardButton;
        jPanel3 = new javax.swing.JPanel();
        addFolderButton = bmv.addFolderButton;
        addBookmarkActionButton = bmv.addBookmarkButton;
        removeBookmarkActionButton = new javax.swing.JButton();
        importButton = new javax.swing.JButton();
        exportButton = new javax.swing.JButton();
        addSeparator = bmv.addSeparatorButton;
        addDataAndBookmarkAction = bmv.addDataAndPositionBookmarkButton;

        jScrollPane1.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                jScrollPane1MouseDragged(evt);
            }
        });

        bookmarkTree.setModel(bmv.thing.def_tree_model);
        jScrollPane1.setViewportView(bookmarkTree);

        nameTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nameTextFieldActionPerformed(evt);
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
        commentTextArea.setRows(5);
        commentTextArea.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                commentTextAreaFocusLost(evt);
            }
        });
        jScrollPane2.setViewportView(commentTextArea);

        jLabel1.setText("Comment:");

        org.jdesktop.layout.GroupLayout PropertiesPanelLayout = new org.jdesktop.layout.GroupLayout(PropertiesPanel);
        PropertiesPanel.setLayout(PropertiesPanelLayout);
        PropertiesPanelLayout.setHorizontalGroup(
            PropertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(PropertiesPanelLayout.createSequentialGroup()
                .add(jLabel1)
                .addContainerGap(145, Short.MAX_VALUE))
            .add(jScrollPane2, 0, 0, Short.MAX_VALUE)
            .add(nameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 210, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, PropertiesPanelLayout.createSequentialGroup()
                .add(nameLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 118, Short.MAX_VALUE)
                .add(undoNameButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(4, 4, 4)
                .add(redoNameButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        PropertiesPanelLayout.linkSize(new java.awt.Component[] {redoNameButton, undoNameButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        PropertiesPanelLayout.setVerticalGroup(
            PropertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(PropertiesPanelLayout.createSequentialGroup()
                .add(PropertiesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(undoNameButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(redoNameButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(nameLabel))
                .add(0, 0, 0)
                .add(nameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(4, 4, 4)
                .add(jLabel1)
                .add(4, 4, 4)
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 130, Short.MAX_VALUE))
        );

        PropertiesPanelLayout.linkSize(new java.awt.Component[] {redoNameButton, undoNameButton}, org.jdesktop.layout.GroupLayout.VERTICAL);

        undoNameButton.setBorder(null);
        redoNameButton.setBorder(null);

        backwardActionButton.setIcon(CommonUtils.getInstance().getIcon("images/backward.png"));
        backwardActionButton.setToolTipText("Return to Previously visited Bookmark");
        backwardActionButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        backwardActionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backwardActionButtonActionPerformed(evt);
            }
        });

        forwardActionButton.setIcon(CommonUtils.getInstance().getIcon("images/forward.png"));
        forwardActionButton.setToolTipText("Return from a backward action ");
        forwardActionButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        forwardActionButton.setPreferredSize(new java.awt.Dimension(24, 24));
        forwardActionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                forwardActionButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(backwardActionButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE)
            .add(forwardActionButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel2Layout.createSequentialGroup()
                .add(0, 0, 0)
                .add(backwardActionButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(forwardActionButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        backwardActionButton.setBorder(null);
        forwardActionButton.setBorder(null);

        addFolderButton.setIcon(CommonUtils.getInstance().getIcon("images/addFolder.png"));
        addFolderButton.setToolTipText("Add a New Folder");
        addFolderButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        addFolderButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addFolderButtonActionPerformed(evt);
            }
        });

        addBookmarkActionButton.setIcon(CommonUtils.getInstance().getIcon("images/addBookmark.png"));
        addBookmarkActionButton.setToolTipText("Add a New Position Bookmark");
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

        addDataAndBookmarkAction.setIcon(CommonUtils.getInstance().getIcon("images/addPositionDataBookmark.png"));
        addDataAndBookmarkAction.setToolTipText("Add a New Position and Data Bookmark");
        addDataAndBookmarkAction.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        addDataAndBookmarkAction.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addDataAndBookmarkActionActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(addFolderButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE)
            .add(jPanel3Layout.createSequentialGroup()
                .add(addSeparator, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE)
                .add(0, 0, 0))
            .add(jPanel3Layout.createSequentialGroup()
                .add(addBookmarkActionButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE)
                .add(0, 0, 0))
            .add(jPanel3Layout.createSequentialGroup()
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(removeBookmarkActionButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 28, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(importButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE)
                    .add(exportButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE))
                .add(0, 0, 0))
            .add(jPanel3Layout.createSequentialGroup()
                .add(addDataAndBookmarkAction, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE)
                .add(0, 0, 0))
        );

        jPanel3Layout.linkSize(new java.awt.Component[] {addBookmarkActionButton, addDataAndBookmarkAction, addFolderButton, addSeparator, exportButton, importButton, removeBookmarkActionButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .add(addFolderButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(4, 4, 4)
                .add(addSeparator, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(4, 4, 4)
                .add(addBookmarkActionButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(addDataAndBookmarkAction, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(removeBookmarkActionButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(4, 4, 4)
                .add(importButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(4, 4, 4)
                .add(exportButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        jPanel3Layout.linkSize(new java.awt.Component[] {addBookmarkActionButton, addDataAndBookmarkAction, addFolderButton, addSeparator, importButton, removeBookmarkActionButton}, org.jdesktop.layout.GroupLayout.VERTICAL);

        addFolderButton.setBorder(null);
        addBookmarkActionButton.setBorder(null);
        removeBookmarkActionButton.setBorder(null);
        importButton.setBorder(null);
        exportButton.setBorder(null);
        addSeparator.setBorder(null);
        addDataAndBookmarkAction.setBorder(null);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(PropertiesPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jPanel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(1, 1, 1)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 181, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(jPanel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 130, Short.MAX_VALUE)
                        .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 383, Short.MAX_VALUE))
                .add(1, 1, 1)
                .add(PropertiesPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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

	private void jScrollPane1MouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jScrollPane1MouseDragged
		Rectangle r = new Rectangle(evt.getX(), evt.getY(), 1, 1);
		scrollRectToVisible(r);
	}//GEN-LAST:event_jScrollPane1MouseDragged

	private void addBookmarkActionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addBookmarkActionButtonActionPerformed
		AddPositionBookmarkAction.getAction().actionPerformed(evt);
}//GEN-LAST:event_addBookmarkActionButtonActionPerformed

	private void nameTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nameTextFieldActionPerformed
		bmv.thing.updateBookmarkData();
	}//GEN-LAST:event_nameTextFieldActionPerformed

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
		} catch (CannotUndoException ex) {
		}
	}//GEN-LAST:event_undoNameButtonActionPerformed

	private void redoNameButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_redoNameButtonActionPerformed
		try {
			bmv.thing.undoManager.redo();
		} catch (CannotRedoException ex) {
		}
	}//GEN-LAST:event_redoNameButtonActionPerformed

	private void commentTextAreaFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_commentTextAreaFocusLost
		bmv.thing.updateBookmarkData();
	}//GEN-LAST:event_commentTextAreaFocusLost

	private void addDataAndBookmarkActionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addDataAndBookmarkActionActionPerformed
		AddPositionAndDataBookmarkAction.getAction().actionPerformed(evt);
	}//GEN-LAST:event_addDataAndBookmarkActionActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel PropertiesPanel;
    private javax.swing.JButton addBookmarkActionButton;
    private javax.swing.JButton addDataAndBookmarkAction;
    private javax.swing.JButton addFolderButton;
    private javax.swing.JButton addSeparator;
    private javax.swing.JButton backwardActionButton;
    private javax.swing.JTree bookmarkTree;
    private javax.swing.JTextArea commentTextArea;
    private javax.swing.JButton exportButton;
    private javax.swing.JButton forwardActionButton;
    private javax.swing.JButton importButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JButton redoNameButton;
    private javax.swing.JButton removeBookmarkActionButton;
    private javax.swing.JButton undoNameButton;
    // End of variables declaration//GEN-END:variables
}
