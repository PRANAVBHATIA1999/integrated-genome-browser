package com.affymetrix.igb.bookmarks;

import com.affymetrix.genometry.util.PreferenceUtils;
import com.affymetrix.igb.shared.StyledJTable;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An Bookmark Properties UI class for IGB. It is generated by Netbeans GUI
 * Builder.
 *
 * @author nick
 */
public class BookmarkPropertiesGUI extends JFrame {

    private static final String TITLE = "Bookmark Properties";
    private static final Logger logger = LoggerFactory.getLogger(BookmarkPropertiesGUI.class);

    private final BookmarkPropertyTableModel propertyModel;
    private BookmarkList bookmarkList;

    public BookmarkPropertiesGUI(final TableModelListener listener) {
        propertyModel = new BookmarkPropertyTableModel();
        propertyModel.addTableModelListener(new TableModelListener() {

            /**
             * Any values changed in property table will trigger to update info
             * or data list table
             */
            @Override
            public void tableChanged(TableModelEvent e) {
                if (bookmarkList != null) {
                    Bookmark bm = (Bookmark) bookmarkList.getUserObject();
                    URL url = bm.getURL();
                    String url_base = bm.getURL().toExternalForm();
                    int index = url_base.indexOf('?');
                    if (index > 0) {
                        url_base = url_base.substring(0, index);
                    }

                    // record the modified time
                    ListMultimap<String, String> props = propertyModel.getValuesAsMap();
                    props.put(Bookmark.MODIFIED, BookmarkController.DATE_FORMAT.format(new Date()));

                    try {
                        String str = Bookmark.constructURL(url_base, props);
                        url = new URL(str);
                    } catch (MalformedURLException ex) {
                        logger.error("Malformed URL", ex);
                    } catch (UnsupportedEncodingException ex) {
                        logger.error("Malformed URL", ex);
                    }
                    bm.setURL(url);
                    listener.tableChanged(e);
                }
            }
        });

        Rectangle pos = PreferenceUtils.retrieveWindowLocation(TITLE, new Rectangle(400, 400));
        if (pos != null) {
            PreferenceUtils.setWindowSize(this, pos);
        }

        this.setTitle(TITLE);
        this.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent evt) {
                /**
                 * Writes the window location to the persistent preferences.
                 */
                PreferenceUtils.saveWindowLocation(BookmarkPropertiesGUI.this, TITLE);
            }
        });

        initComponents();
    }

    public synchronized void displayPanel(BookmarkList bl) {
        bookmarkList = bl;
        setTableFromBookmark(propertyModel, bl);
        this.setVisible(true);
    }

    private void setTableFromBookmark(BookmarkPropertyTableModel model, BookmarkList bl) {
        Bookmark bm = (Bookmark) bl.getUserObject();
        if (bm == null) {
            model.setValuesFromMap(ImmutableListMultimap.<String, String>builder().build());
        } else {
            URL url = bm.getURL();
            model.setValuesFromMap(Bookmark.parseParameters(url));
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        table =  new StyledJTable(propertyModel);

        setTitle("Bookmark Properties");

        jScrollPane1.setViewportView(table);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 414, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 408, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable table;
    // End of variables declaration//GEN-END:variables
}
