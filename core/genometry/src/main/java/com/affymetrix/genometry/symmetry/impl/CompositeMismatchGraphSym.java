package com.affymetrix.genometry.symmetry.impl;

import com.affymetrix.genometry.BioSeq;
import java.util.Arrays;

/**
 *
 * @author hiralv
 */
public class CompositeMismatchGraphSym extends MisMatchGraphSym {

    public CompositeMismatchGraphSym(String id, BioSeq seq) {
        super(null, null, null, null, null, null, null, null, id, seq);
    }

    /**
     * Overriding addChild() to only accept GraphSym children, integrates x and
     * y coord arrays of child into composite's coord arrays (and nulls old ones
     * out for gc). Assumes that slices can abut but do _not_ overlap "abut" in
     * this case means that (sliceA.span.max == sliceB.span.min) since these are
     * half-open half-closed intervals, this is not actually overlap but
     * abutment...
     *
     */
    @Override
    public void addChild(SeqSymmetry sym) {
        if (!(sym instanceof MisMatchGraphSym)) {
            throw new RuntimeException("only MisMatchGraphSym can be added as children to CompositeGraphSym!");
        }
        MisMatchGraphSym slice = (MisMatchGraphSym) sym;

        if (slice.getPointCount() > 0) {
            if (this.getPointCount() == 0) { // first GraphSym child, so just set xcoords and ycoords
                int[] slice_xcoords = slice.getGraphXCoords();
                float[] slice_ycoords = slice.getGraphYCoords();
                int[] slice_wcoords = slice.getGraphWidthCoords();	// may be null
                int[][] slice_rcoords = slice.getAllResidues();
                slice.nullCoords();
                setCoords(slice_xcoords, slice_ycoords, slice_wcoords);
                setAllResidues(slice_rcoords[0], slice_rcoords[1], slice_rcoords[2], slice_rcoords[3], slice_rcoords[4]);
            } else {
                createNewCoords(slice);
            }
        }
    }

    private void createNewCoords(MisMatchGraphSym slice) {
        int slice_min = slice.getMinXCoord();
        int slice_index = this.determineBegIndex(slice_min);

        int coordSize = this.getPointCount();
        int sliceSize = slice.getPointCount();

        int[] slice_xcoords = slice.getGraphXCoords();
        float[] slice_ycoords = slice.getGraphYCoords();
        int[] slice_wcoords = slice.getGraphWidthCoords();
        int[][] slice_rcoords = slice.getAllResidues();
        slice.nullCoords();	// get rid of old coords

        int[] old_xcoords = this.getGraphXCoords();
        int[] new_xcoords = copyIntCoords(coordSize, sliceSize, slice_index, old_xcoords, slice_xcoords);

        float[] old_ycoords = this.getGraphYCoords();
        float[] new_ycoords = copyFloatCoords(coordSize, sliceSize, slice_index, old_ycoords, slice_ycoords);

        int[] old_wcoords = this.getGraphWidthCoords();	// may be null
        int[] new_wcoords = copyIntCoords(coordSize, sliceSize, slice_index, old_wcoords, slice_wcoords);

        int[][] old_rcoords = this.getAllResidues();
        int[] new_acoords = copyIntCoords(coordSize, sliceSize, slice_index, old_rcoords[0], slice_rcoords[0]);
        int[] new_tcoords = copyIntCoords(coordSize, sliceSize, slice_index, old_rcoords[1], slice_rcoords[1]);
        int[] new_gcoords = copyIntCoords(coordSize, sliceSize, slice_index, old_rcoords[2], slice_rcoords[2]);
//		int[] new_ccoords = copyIntCoords(coordSize, sliceSize, slice_index, old_rcoords[3], slice_rcoords[3]);
        int[] new_ncoords = copyIntCoords(coordSize, sliceSize, slice_index, old_rcoords[4], slice_rcoords[4]);

        setCoords(new_xcoords, new_ycoords, new_wcoords);
        setAllResidues(new_acoords, new_tcoords, new_gcoords, new_ncoords, new_ncoords);
    }

    private static int[] copyIntCoords(int coordSize, int sliceSize, int slice_index, int[] old_coords, int[] slice_coords) {
        int[] new_coords = new int[coordSize + sliceSize];
        int new_index = 0;

        //    old coord array entries up to "A-1"
        if (old_coords == null) {
            Arrays.fill(new_coords, new_index, new_index + slice_index, 0);
        } else {
            System.arraycopy(old_coords, 0, new_coords, new_index, slice_index);
        }
        new_index += slice_index;

        //    all of slice_coords entries
        if (slice_coords == null) {
            Arrays.fill(new_coords, new_index, new_index + sliceSize, 0);
        } else {
            System.arraycopy(slice_coords, 0, new_coords, new_index, sliceSize);
        }
        new_index += sliceSize;

        //    old coord array entries from "A" to end of old coord array
        if (old_coords == null) {
            Arrays.fill(new_coords, new_index, sliceSize + coordSize, 0);
        } else {
            System.arraycopy(old_coords, slice_index, new_coords, new_index, coordSize - slice_index);
        }

        return new_coords;
    }

    private static float[] copyFloatCoords(int coordSize, int sliceSize, int slice_index, float[] old_coords, float[] slice_coords) {
        float[] new_coords = new float[coordSize + sliceSize];
        int new_index = 0;
        //    old coord array entries up to "A-1"
        System.arraycopy(old_coords, 0, new_coords, new_index, slice_index);
        new_index += slice_index;

        //    all of slice_coords entries
        System.arraycopy(slice_coords, 0, new_coords, new_index, sliceSize);
        new_index += sliceSize;

        //    old coord array entries from "A" to end of old coord array
        System.arraycopy(old_coords, slice_index, new_coords, new_index, coordSize - slice_index);
        return new_coords;
    }
}
