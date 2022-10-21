package com.affymetrix.igb.action;

import com.affymetrix.genometry.BioSeq;
import com.affymetrix.genometry.GenometryModel;
import com.affymetrix.genometry.event.GenericAction;
import com.affymetrix.genometry.event.SymSelectionEvent;
import com.affymetrix.genometry.event.SymSelectionListener;
import com.affymetrix.genometry.parsers.AnnotationWriter;
import com.affymetrix.genometry.parsers.FileTypeCategory;
import com.affymetrix.genometry.symmetry.RootSeqSymmetry;
import com.affymetrix.genometry.util.ErrorHandler;
import com.affymetrix.genometry.util.ExportFileModel;
import com.affymetrix.genometry.util.FileTracker;
import com.affymetrix.genometry.util.GFileChooser;
import com.affymetrix.genometry.util.UniFileFilter;
import static com.affymetrix.igb.IGBConstants.BUNDLE;
import com.affymetrix.igb.IgbServiceImpl;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.lorainelab.igb.genoviz.extensions.glyph.TierGlyph;
import java.awt.event.ActionEvent;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import javafx.stage.FileChooser;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import org.lorainelab.igb.javafx.FileChooserUtil;

public abstract class AbstractExportFileAction
        extends GenericAction implements SymSelectionListener {

    private static final long serialVersionUID = 1L;
    private static final GenometryModel gmodel = GenometryModel.getInstance();
    private final ExportFileModel model;
    private final Map<FileTypeCategory, UniFileFilter> preferredFilters;

    protected AbstractExportFileAction(
            String text,
            String tooltip,
            String iconPath, String largeIconPath,
            int mnemonic,
            Object extraInfo,
            boolean popup) {
        super(text, tooltip, iconPath, largeIconPath, mnemonic, extraInfo, popup);
        model = new ExportFileModel();
        preferredFilters = new EnumMap<>(FileTypeCategory.class);
    }

    /**
     * Override to enable or disable self based on tracks selected. Note that
     * this must match {@link #actionPerformed(ActionEvent)} which only works
     * when one track is selected.
     */
    @Override
    public void symSelectionChanged(SymSelectionEvent evt) {
        List<TierGlyph> answer = IgbServiceImpl.getInstance().getSelectedTierGlyphs();
        if (answer.size() != 1) {
            setEnabled(false);
        } else {
            Optional<FileTypeCategory> category = answer.get(0).getFileTypeCategory();
            setEnabled(answer.get(0).getInfo() != null && isExportable(category));
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        List<TierGlyph> current_tiers = IgbServiceImpl.getInstance().getSelectedTierGlyphs();
        if (current_tiers.size() > 1) {
            ErrorHandler.errorPanel(BUNDLE.getString("multTrackError"));
        } else if (current_tiers.isEmpty()) {
            ErrorHandler.errorPanel(BUNDLE.getString("noTrackError"));
        } else {
            TierGlyph current_tier = current_tiers.get(0);
            saveAsFile(current_tier);
        }
    }

    private void saveAsFile(TierGlyph atier) {
        RootSeqSymmetry rootSym = (RootSeqSymmetry) atier.getInfo();

        Optional<Map<UniFileFilter, AnnotationWriter>> filter2writers = model.getFilterToWriters(rootSym.getCategory());
        if (filter2writers.isPresent() && !filter2writers.get().isEmpty()) {
            File savedDir = FileTracker.DATA_DIR_TRACKER.getFile();
            List<FileChooser.ExtensionFilter> filters = Lists.newArrayList();
            Set<UniFileFilter> keySet = filter2writers.get().keySet();
            keySet.stream().forEach((filter) -> {
                filter.getExtensions().forEach(filterName -> {
                    FileChooser.ExtensionFilter extensionFilter
                             = new FileChooser.ExtensionFilter(filter.getDescription(), "*." + filterName);
                    filters.add(extensionFilter);
                });

            });
            //<Ivory Blakley> issue IGBF-1149, make defualt file type consistent
            Collections.sort(filters, new filterComparator());

            FileChooserUtil fcUtil = FileChooserUtil.build()
                    .setFileExtensionFilters(filters)
                    .setContext(savedDir);

            Optional<File> file = fcUtil.saveFilesFromFxChooser();

            if (file.isPresent()) {
                File selectedFile = file.get();
                Optional<BioSeq> aseq = gmodel.getSelectedSeq();

                try (DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(selectedFile)))) {

                    UniFileFilter swingSelectedFilter = null;

                    String selectedFileName = file.get().getName();
                    String extension = selectedFileName.substring(selectedFileName.lastIndexOf(".") + 1);
                    boolean isFound = false;
                    for (UniFileFilter key : filter2writers.get().keySet()) {
                        for (String ext : key.getExtensions()) {
                            if (ext.equals(extension)) {
                                swingSelectedFilter = key;
                                isFound = true;
                                break;
                            }
                        }
                        if (isFound) {
                            break;
                        }
                    }

                    preferredFilters.put(rootSym.getCategory(), swingSelectedFilter);
                    exportFile(filter2writers.get().get(swingSelectedFilter), dos, aseq.orElse(null), atier);
                } catch (Exception ex) {
                    ErrorHandler.errorPanel("Problem saving file", ex, Level.SEVERE);
                }
                FileTracker.DATA_DIR_TRACKER.setFile(selectedFile);
            }
        } else {
            ErrorHandler.errorPanel("not supported yet", "cannot export files of type "
                    + rootSym.getCategory().toString(), Level.WARNING);
        }
    }

    public boolean isExportable(Optional<FileTypeCategory> category) {
        if (category.isPresent()) {
            Optional<Map<UniFileFilter, AnnotationWriter>> filter2writers = model.getFilterToWriters(category.get());
            if (filter2writers.isPresent()) {
                return !filter2writers.get().isEmpty();
            }
        }
        return false;
    }

    protected abstract void exportFile(AnnotationWriter annotationWriter, DataOutputStream dos, BioSeq aseq, TierGlyph atier) throws java.io.IOException;

    // Sort the file type options before creating the pop-up, so the order of items in the pull down menu is consistent.
    // The first item is the default, so this makes the default consistent.  <Ivory Blakley> issue IGBF-1149
    private final static class filterComparator implements Comparator<FileChooser.ExtensionFilter> {
        @Override
        public int compare(FileChooser.ExtensionFilter left, FileChooser.ExtensionFilter right) {
            //sort alphabetically by file type (description) and then by file extention
            if (left.getDescription().compareTo(right.getDescription()) != 0){
                return left.getDescription().compareTo(right.getDescription());
            }
            return left.getExtensions().get(0).compareTo(right.getExtensions().get(0));
        }
    }
}
