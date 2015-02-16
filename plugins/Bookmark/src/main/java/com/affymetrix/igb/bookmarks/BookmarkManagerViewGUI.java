package com.affymetrix.igb.bookmarks;

import com.affymetrix.common.CommonUtils;
import com.affymetrix.igb.bookmarks.action.AddBookmarkAction;
import com.affymetrix.igb.bookmarks.action.AddFolderAction;
import com.affymetrix.igb.bookmarks.action.AddSeparatorAction;
import com.lorainelab.igb.service.api.IgbService;
import com.lorainelab.igb.service.api.IgbTabPanel;
import com.affymetrix.igb.swing.jide.StyledJTable;
import java.awt.Rectangle;
import java.util.ResourceBundle;
import javax.swing.JTabbedPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

/**
 * An Bookmark Manager View UI class for IGB. It is generated by Netbeans GUI
 * Builder.
 *
 * @author Nick & David
 */
public class BookmarkManagerViewGUI extends IgbTabPanel {

    private static final long serialVersionUID = 1L;
    private static BookmarkManagerViewGUI singleton;
    public static final ResourceBundle BUNDLE = ResourceBundle.getBundle("bookmark");
    private static final int TAB_POSITION = 8;
    public static BookmarkManagerView bmv;

    public static void init(IgbService _igbService) {
        singleton = new BookmarkManagerViewGUI(_igbService);
    }

    public static synchronized BookmarkManagerViewGUI getSingleton() {
        return singleton;
    }

    public BookmarkManagerView getBookmarkManagerView() {
        return bmv;
    }

    /**
     * Creates new form BookMarkManagerViewGUI
     */
    public BookmarkManagerViewGUI(IgbService _igbService) {
        super(BUNDLE.getString("bookmarksTab"),
                BUNDLE.getString("bookmarksTab"), BUNDLE.getString("bookmarksTooltip"), false, TAB_POSITION);
        BookmarkManagerView.init(_igbService);
        bmv = BookmarkManagerView.getSingleton();

        initComponents();
    }

    @Override
    public TabState getDefaultTabState() {
        return TabState.COMPONENT_STATE_RIGHT_TAB;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
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
        addSeparator = bmv.addSeparatorButton;
        tabPane = new JTabbedPane();
        commentPanel = new javax.swing.JPanel();
        nameTextField = bmv.thing.name_text_field;
        nameLabel = new javax.swing.JLabel();
        undoNameButton = new javax.swing.JButton();
        redoNameButton = new javax.swing.JButton();
        cScrollPane = new javax.swing.JScrollPane();
        commentTextArea = bmv.thing.comment_text_area;
        commentLabel = new javax.swing.JLabel();
        informationPanel = new javax.swing.JPanel();
        iScrollPane = new javax.swing.JScrollPane();
        infoTable = new StyledJTable(bmv.thing.infoModel);
        datalistPanel = new javax.swing.JPanel();
        dScrollPane = new javax.swing.JScrollPane();
        dataTable = new StyledJTable(bmv.thing.datalistModel);

        splitPane.setDividerLocation(400);
        splitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        backwardActionButton.setIcon(CommonUtils.getInstance().getIcon("22x22/actions/go-previous.png"));
        backwardActionButton.setToolTipText("Click to go back (Previous visited bookmark)");
        backwardActionButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        backwardActionButton.addActionListener(this::backwardActionButtonActionPerformed);

        forwardActionButton.setIcon(CommonUtils.getInstance().getIcon("22x22/actions/go-next.png"));
        forwardActionButton.setToolTipText("Click to go forward (Next visited bookmark)");
        forwardActionButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        forwardActionButton.setPreferredSize(new java.awt.Dimension(24, 24));
        forwardActionButton.addActionListener(this::forwardActionButtonActionPerformed);

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

        bookmarkTree.setModel(bmv.tree_model);
        bmScrollPane.setViewportView(bookmarkTree);

        addFolderButton.setIcon(CommonUtils.getInstance().getIcon("22x22/actions/folder-new.png"));
        addFolderButton.setToolTipText("Add a New Folder");
        addFolderButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        addFolderButton.addActionListener(this::addFolderButtonActionPerformed);

        addBookmarkActionButton.setIcon(CommonUtils.getInstance().getIcon("22x22/actions/bookmark-new.png"));
        addBookmarkActionButton.setToolTipText("Add a new Bookmark");
        addBookmarkActionButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        addBookmarkActionButton.addActionListener(this::addBookmarkActionButtonActionPerformed);

        removeBookmarkActionButton.setIcon(CommonUtils.getInstance().getIcon("16x16/actions/delete_bookmark.png"));
        removeBookmarkActionButton.setToolTipText("Remove a bookmark");
        removeBookmarkActionButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        removeBookmarkActionButton.addActionListener(this::removeBookmarkActionButtonActionPerformed);

        addSeparator.setIcon(CommonUtils.getInstance().getIcon("22x22/actions/list-remove.png"));
        addSeparator.setToolTipText("Add new separator");
        addSeparator.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        addSeparator.addActionListener(this::addSeparatorActionPerformed);

        org.jdesktop.layout.GroupLayout upPanelLayout = new org.jdesktop.layout.GroupLayout(upPanel);
        upPanel.setLayout(upPanelLayout);
        upPanelLayout.setHorizontalGroup(
            upPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(addFolderButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(addSeparator, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(addBookmarkActionButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(removeBookmarkActionButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 28, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );

        upPanelLayout.linkSize(new java.awt.Component[] {addBookmarkActionButton, addFolderButton, addSeparator, removeBookmarkActionButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        upPanelLayout.setVerticalGroup(
            upPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(upPanelLayout.createSequentialGroup()
                .add(addBookmarkActionButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(addFolderButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(addSeparator, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(removeBookmarkActionButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        upPanelLayout.linkSize(new java.awt.Component[] {addBookmarkActionButton, addFolderButton, addSeparator, removeBookmarkActionButton}, org.jdesktop.layout.GroupLayout.VERTICAL);

        addFolderButton.setBorder(null);
        addBookmarkActionButton.setBorder(null);
        removeBookmarkActionButton.setBorder(null);
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
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(bottomPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(15, 15, 15))
            .add(bmScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 398, Short.MAX_VALUE)
        );

        splitPane.setLeftComponent(bmPanel);

        nameLabel.setText("Name:");

        undoNameButton.setIcon(CommonUtils.getInstance().getIcon("22x22/actions/edit-undo.png"));
        undoNameButton.setToolTipText("Undo");
        undoNameButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        undoNameButton.addActionListener(this::undoNameButtonActionPerformed);

        redoNameButton.setIcon(CommonUtils.getInstance().getIcon("22x22/actions/edit-redo.png"));
        redoNameButton.setToolTipText("Redo");
        redoNameButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        redoNameButton.addActionListener(this::redoNameButtonActionPerformed);

        commentTextArea.setColumns(20);
        commentTextArea.setLineWrap(true);
        commentTextArea.setRows(5);
        commentTextArea.setWrapStyleWord(true);
        cScrollPane.setViewportView(commentTextArea);

        commentLabel.setText("Comment:");

        org.jdesktop.layout.GroupLayout commentPanelLayout = new org.jdesktop.layout.GroupLayout(commentPanel);
        commentPanel.setLayout(commentPanelLayout);
        commentPanelLayout.setHorizontalGroup(
            commentPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(commentPanelLayout.createSequentialGroup()
                .add(commentLabel)
                .addContainerGap(134, Short.MAX_VALUE))
            .add(nameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 199, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, commentPanelLayout.createSequentialGroup()
                .add(nameLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 107, Short.MAX_VALUE)
                .add(undoNameButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(4, 4, 4)
                .add(redoNameButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .add(cScrollPane, 0, 0, Short.MAX_VALUE)
        );

        commentPanelLayout.linkSize(new java.awt.Component[] {redoNameButton, undoNameButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        commentPanelLayout.setVerticalGroup(
            commentPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(commentPanelLayout.createSequentialGroup()
                .add(commentPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
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

        commentPanelLayout.linkSize(new java.awt.Component[] {redoNameButton, undoNameButton}, org.jdesktop.layout.GroupLayout.VERTICAL);

        undoNameButton.setBorder(null);
        redoNameButton.setBorder(null);

        tabPane.addTab("Notes", commentPanel);

        iScrollPane.setViewportView(infoTable);

        org.jdesktop.layout.GroupLayout informationPanelLayout = new org.jdesktop.layout.GroupLayout(informationPanel);
        informationPanel.setLayout(informationPanelLayout);
        informationPanelLayout.setHorizontalGroup(
            informationPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, iScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 199, Short.MAX_VALUE)
        );
        informationPanelLayout.setVerticalGroup(
            informationPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(iScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 103, Short.MAX_VALUE)
        );

        tabPane.addTab("Location", informationPanel);

        dScrollPane.setViewportView(dataTable);

        org.jdesktop.layout.GroupLayout datalistPanelLayout = new org.jdesktop.layout.GroupLayout(datalistPanel);
        datalistPanel.setLayout(datalistPanelLayout);
        datalistPanelLayout.setHorizontalGroup(
            datalistPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, dScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 199, Short.MAX_VALUE)
        );
        datalistPanelLayout.setVerticalGroup(
            datalistPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(dScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 103, Short.MAX_VALUE)
        );

        tabPane.addTab("Details", datalistPanel);

        splitPane.setRightComponent(tabPane);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(splitPane)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(splitPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 560, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

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
                    BookmarkController.viewBookmark(bmv.igbService, bm);
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
                BookmarkController.viewBookmark(bmv.igbService, bm);
                forwardActionButton.setEnabled(bmv.history_pointer < bmv.bookmark_history.size() - 1);
                backwardActionButton.setEnabled(true);
            }
	}//GEN-LAST:event_forwardActionButtonActionPerformed

	private void bmScrollPaneMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bmScrollPaneMouseDragged
            Rectangle r = new Rectangle(evt.getX(), evt.getY(), 1, 1);
            scrollRectToVisible(new Rectangle(evt.getX(), evt.getY(), 1, 1));
	}//GEN-LAST:event_bmScrollPaneMouseDragged

	private void addBookmarkActionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addBookmarkActionButtonActionPerformed
            AddBookmarkAction.getAction().actionPerformed(evt);
}//GEN-LAST:event_addBookmarkActionButtonActionPerformed

	private void removeBookmarkActionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeBookmarkActionButtonActionPerformed
            bmv.deleteAction();
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
    private javax.swing.JPanel commentPanel;
    private javax.swing.JTextArea commentTextArea;
    private javax.swing.JScrollPane dScrollPane;
    private javax.swing.JTable dataTable;
    private javax.swing.JPanel datalistPanel;
    private javax.swing.JButton forwardActionButton;
    private javax.swing.JScrollPane iScrollPane;
    private javax.swing.JTable infoTable;
    private javax.swing.JPanel informationPanel;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JButton redoNameButton;
    private javax.swing.JButton removeBookmarkActionButton;
    private javax.swing.JSplitPane splitPane;
    private javax.swing.JTabbedPane tabPane;
    private javax.swing.JButton undoNameButton;
    private javax.swing.JPanel upPanel;
    // End of variables declaration//GEN-END:variables

}
