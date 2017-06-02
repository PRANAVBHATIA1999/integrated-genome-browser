package com.affymetrix.igb.action;

import com.affymetrix.genometry.BioSeq;
import com.affymetrix.genometry.GenomeVersion;
import com.affymetrix.genometry.GenometryModel;
import com.affymetrix.genometry.event.GenericActionHolder;
import com.affymetrix.genometry.parsers.AnnotationWriter;
import com.affymetrix.genometry.parsers.FileTypeCategory;
import com.affymetrix.genometry.symmetry.RootSeqSymmetry;
import com.affymetrix.genometry.symmetry.impl.GraphSym;
import com.affymetrix.genometry.symmetry.impl.SeqSymmetry;
import com.affymetrix.genometry.symmetry.impl.TypeContainerAnnot;
import com.affymetrix.genometry.util.ExportFileModel;
import com.affymetrix.genometry.util.FileTypeCategoryUtils;
import static com.affymetrix.igb.IGBConstants.BUNDLE;
import com.affymetrix.igb.IgbServiceImpl;
import org.lorainelab.igb.genoviz.extensions.glyph.TierGlyph;
import java.awt.event.KeyEvent;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ExportFileAction
        extends AbstractExportFileAction {

    private static final long serialVersionUID = 1L;
    private static final ExportFileAction ACTION = new ExportFileAction();

    static {
        GenericActionHolder.getInstance().addGenericAction(ACTION);
        ACTION.setEnabled(false);
        GenometryModel.getInstance().addSymSelectionListener(ACTION);
    }

    public static ExportFileAction getAction() {
        return ACTION;
    }

    private ExportFileAction() {
        super(BUNDLE.getString("saveTrackAction"), null,
                "16x16/actions/save.png",
                "22x22/actions/save.png",
                KeyEvent.VK_UNDEFINED, null, true);
        putValue(SHORT_DESCRIPTION, BUNDLE.getString("saveTrackActionTooltip"));
        this.ordinal = -9007100;
        setKeyStrokeBinding("ctrl S");
    }

    @Override
    protected void exportFile(AnnotationWriter annotationWriter, DataOutputStream dos, BioSeq aseq, TierGlyph atier) throws java.io.IOException {
        List<SeqSymmetry> syms = new ArrayList<>();
        RootSeqSymmetry rootSeqSymmetry = (RootSeqSymmetry) atier.getInfo();
        FileTypeCategory category = rootSeqSymmetry.getCategory();
        if (FileTypeCategoryUtils.isFileTypeCategoryContainer(category) && (rootSeqSymmetry instanceof TypeContainerAnnot)) {
            GenomeVersion genomeVersion = aseq.getGenomeVersion();
            List<BioSeq> seql = genomeVersion.getSeqList();
            for (BioSeq aseql : seql) {
                RootSeqSymmetry rootSym = aseql.getAnnotation(((TypeContainerAnnot) atier.getInfo()).getType());
                if (rootSym != null) {
                    syms = new ArrayList<>();
                    ExportFileModel.collectSyms(rootSym, syms, atier.getAnnotStyle().getGlyphDepth());
                    annotationWriter.writeAnnotations(syms, aseql, "", dos);
                }
            }
        } else {
            if (category.equals(FileTypeCategory.Graph)) {
                List<List<SeqSymmetry>> myList = new ArrayList<List<SeqSymmetry>>();
                myList.add(new ArrayList<>());
                aseq.getGenomeVersion().getSeqList().forEach(seq -> {
                    myList.get(0).addAll(seq.getAnnotations(Pattern.compile(".*")).stream().filter(s -> s instanceof GraphSym).collect(Collectors.toList()));
                });
                annotationWriter.writeAnnotations(myList.get(0), aseq, aseq.getGenomeVersion().getName(), dos);
            } else {
                syms.add(rootSeqSymmetry);
                annotationWriter.writeAnnotations(syms, aseq, "", dos);
            }
        }
    }
}
