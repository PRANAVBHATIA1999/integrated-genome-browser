package com.affymetrix.igb.bookmarks;

import com.affymetrix.genometry.SeqSpan;
import com.affymetrix.genometry.util.ErrorHandler;
import com.affymetrix.common.PreferenceUtils;
import com.affymetrix.igb.bookmarks.model.Bookmark;
import com.google.common.base.Optional;
import java.util.logging.Level;
import javax.swing.ButtonGroup;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import org.apache.commons.lang3.StringUtils;

/**
 * This class is the implementation of bookmark editor panel.
 *
 * @author nick
 */
public class BookmarkEditor {

    private static BookmarkEditor singleton;
    private final JTextField nameField;
    private final JTextArea commentField;
    private final JRadioButton positionOnlyB;
    private final JRadioButton positionDataB;
    private final JOptionPane op;
    private SeqSpan span;
    private static final boolean defaultUseDefaultName = true;
   
    private static final String default_bookmark_type = "Position and Data";
    private static final String PREF_BOOKMARK_TYPE = "Bookmark type";

    private BookmarkEditor() {
        nameField = new JTextField(40);
        commentField = new JTextArea(5, 8);
        commentField.setLineWrap(true);
        commentField.setWrapStyleWord(true);
        positionOnlyB = PreferenceUtils.createRadioButton("Position Only",
                "Position Only", PREF_BOOKMARK_TYPE, default_bookmark_type);
        positionDataB = PreferenceUtils.createRadioButton("Position and Data",
                "Position and Data", PREF_BOOKMARK_TYPE, default_bookmark_type);
        
        ButtonGroup group = new ButtonGroup();
        group.add(positionOnlyB);
        group.add(positionDataB);
        JScrollPane scrollpane = new JScrollPane(commentField);
        scrollpane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        op = new JOptionPane(null, JOptionPane.PLAIN_MESSAGE,
                JOptionPane.CANCEL_OPTION, null, null);
        op.addPropertyChangeListener("value", evt -> addBookmark());
        op.setMessage(new Object[]{"",  "Name:", nameField,
            "Comment:", scrollpane, positionOnlyB, positionDataB});
    }

    private static BookmarkEditor getInstance() {
        if (singleton == null) {
            singleton = new BookmarkEditor();

        }

        return singleton;
    }

    /**
     * Used JDialog as display panel and initialized it.
     */
    private void initDialog(SeqSpan span) {
        this.span = span;
        setNameField();
        commentField.setText("");
        JDialog dialog = op.createDialog("Enter Bookmark Information...");
        dialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        dialog.setResizable(true);
        dialog.setAlwaysOnTop(false);
        dialog.setModal(false);
        dialog.setVisible(true);
        dialog.pack();
    }

    private void setNameField() 
    {
        nameField.setText(BookmarkController.getDefaultBookmarkName(span));      
    }

    private void addBookmark() {
        int result = JOptionPane.CANCEL_OPTION;

        if (op.getValue() != null && op.getValue() instanceof Integer) {
            result = (Integer) op.getValue();
        }

        if (result == JOptionPane.OK_OPTION) {
            Optional<Bookmark> currentBookmark = BookmarkController.getCurrentBookmark(
                    positionDataB.isSelected(), span);

            if (!currentBookmark.isPresent()) {
                ErrorHandler.errorPanel("Error", "Nothing to bookmark", Level.INFO);
                return;
            }
            Bookmark bookmark = currentBookmark.get();
            String name = nameField.getText();
            String comment = commentField.getText();

            if (StringUtils.isBlank(name)) {
                name = "IGB BOOKMARK";
            }
            bookmark.setName(name);
            bookmark.setComment(comment);
            BookmarkManagerView.getSingleton().insert(new BookmarkList(bookmark));
        }
    }

    /**
     * Activate the panel and complete adding a bookmark by user's operation.
     */
    public static void run(SeqSpan span) {
        getInstance().initDialog(span);
    }
}
