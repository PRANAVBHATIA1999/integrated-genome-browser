package com.affymetrix.genometryImpl.operator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.affymetrix.genometryImpl.BioSeq;
import com.affymetrix.genometryImpl.parsers.FileTypeCategory;
import com.affymetrix.genometryImpl.symmetry.GraphSym;
import com.affymetrix.genometryImpl.symmetry.SeqSymmetry;
import com.affymetrix.genometryImpl.util.GraphSymUtils;

public abstract class AbstractGraphOperator implements Operator {

	/**
	 * performs a given graph operation on a given set of graphs and returns the
	 * resulting graph note - there can be a mix of widthless (no wCoords) and
	 * width graphs, if all input graphs are widthless, the result is also
	 * widthless, otherwise all widthless graphs will be treated as if they have
	 * width of 1.
	 *
	 * @param aseq the BioSeq to use
	 * @param graphs the selected graphs to use as the operands of the operation
	 * @return the graph result of the operation
	 */
	public SeqSymmetry operate(BioSeq aseq, List<SeqSymmetry> symList) {
		// get the x, y, and w (width) coordinates of the graphs int Lists
		ArrayList<ArrayList<Integer>> xCoords = new ArrayList<ArrayList<Integer>>();
		ArrayList<ArrayList<Integer>> wCoords = new ArrayList<ArrayList<Integer>>();
		ArrayList<ArrayList<Float>> yCoords = new ArrayList<ArrayList<Float>>();
		boolean hasWidthGraphs = false;
		int[] index = new int[symList.size()];
		ArrayList<String> labels = new ArrayList<String>();
		for (int i = 0; i < symList.size(); i++) {
			GraphSym graph = (GraphSym) symList.get(i);
			index[i] = 0;
			int[] xArray = graph.getGraphXCoords();
			ArrayList<Integer> xCoordList = new ArrayList<Integer>();
			for (int j = 0; j < xArray.length; j++) {
				xCoordList.add(xArray[j]);
			}
			xCoords.add(xCoordList);
			ArrayList<Integer> wCoordList = null;
			int[] wArray = graph.getGraphWidthCoords();
			if (wArray != null) {
				hasWidthGraphs = true;
				wCoordList = new ArrayList<Integer>();
				for (int j = 0; j < wArray.length; j++) {
					wCoordList.add(wArray[j]);
				}
			}
			wCoords.add(wCoordList);
			float[] yArray = graph.copyGraphYCoords();
			ArrayList<Float> yCoordList = new ArrayList<Float>();
			for (int j = 0; j < yArray.length; j++) {
				yCoordList.add(yArray[j]);
			}
			yCoords.add(yCoordList);
			labels.add(graph.getID());
		}
		List<Integer> xList = new ArrayList<Integer>();
		List<Integer> wList = new ArrayList<Integer>();
		List<Float> yList = new ArrayList<Float>();
		// find the minimum x of all graphs to start with
		int spanBeginX = Integer.MAX_VALUE;
		for (int i = 0; i < symList.size(); i++) {
			spanBeginX = Math.min(spanBeginX, xCoords.get(i).get(0));
		}
		// loop through finding the next x values by searching through all the x coords,
		// and applying the operation on all the graphs
		boolean lastWidth0 = false;
		int spanEndX = 0;
		while (spanBeginX < Integer.MAX_VALUE) {
			// find the next x value, the minimum of all x, x + w that is greater than the current x
			spanEndX = Integer.MAX_VALUE;
			for (int i = 0; i < symList.size(); i++) {
				int graphIndex = index[i];
				if (graphIndex < xCoords.get(i).size()) {
					int startX = xCoords.get(i).get(graphIndex);
					int endX = startX + getWidth(wCoords.get(i), graphIndex, hasWidthGraphs);
					if (startX == endX && startX < spanEndX) { // widthless (width == 0) coordinate
						spanEndX = startX;
					} else if (startX > spanBeginX && startX < spanEndX) {
						spanEndX = startX;
					} else if (endX > spanBeginX && endX < spanEndX) {
						spanEndX = endX;
					}
				}
			}
			if (lastWidth0) {
				spanBeginX = spanEndX;
			}
			// now that we have currentX and nextX (the start and end of the span)
			// we get each y coord as an operand
			List<Float> operands = new ArrayList<Float>();
			for (int i = 0; i < symList.size(); i++) {
				float value = 0;
				int graphIndex = index[i];
				if (graphIndex < xCoords.get(i).size()) {
					int startX = xCoords.get(i).get(graphIndex);
					int endX = startX + getWidth(wCoords.get(i), graphIndex, hasWidthGraphs);
					if (spanBeginX >= startX && spanEndX <= endX) {
						value = yCoords.get(i).get(graphIndex);
					}
				}
				operands.add(value);
			}
			// now we have the operands, actually perform the operation
			float currentY = operate(operands);
			// add the span and result - x, y, w - to the result graph
			xList.add(spanBeginX);
			wList.add(spanEndX - spanBeginX);
			yList.add(currentY);
			// now go through all graphs, and increment the index if necessary
			for (int i = 0; i < symList.size(); i++) {
				int graphIndex = index[i];
				if (graphIndex < xCoords.get(i).size()) {
					int startX = xCoords.get(i).get(graphIndex);
					int endX = startX + getWidth(wCoords.get(i), graphIndex, hasWidthGraphs);
					if (endX <= spanEndX) {
						index[i]++;
					}
				}
			}
			// we are done for this span, move the end of span to the beginning
			lastWidth0 = spanEndX == spanBeginX;
			spanBeginX = spanEndX;
		}
		// get the display name for the result graph
		String symbol = getSymbol();
		String separator = (symbol == null) ? ", " : " " + symbol + " ";
		String newname = createName(aseq, symList, separator);
		// create the new graph from the results
		int[] x = intListToArray(xList);
		int[] w = intListToArray(wList);
		float[] y = floatListToArray(yList);
		if (x.length == 0) { // if no data, just create a dummy zero span
			x = new int[]{xCoords.get(0).get(0)};
			y = new float[]{0};
			w = new int[]{1};
		}
//		if (!graphA.hasWidth()) {
//			newsym = new GraphSym(graphA.getXCoords(), newY, newname, aseq);
//		} else {
//			newsym = new GraphIntervalSym(graphA.getXCoords(), graphA.getWCoords(), newY, newname, aseq);
//		}
		GraphSym newsym = new GraphSym(x, w, y, newname, aseq);

		newsym.setGraphName(newname);
		newsym.getGraphState().setGraphStyle(((GraphSym) symList.get(0)).getGraphState().getGraphStyle());
		newsym.getGraphState().setHeatMap(((GraphSym) symList.get(0)).getGraphState().getHeatMap());
		return newsym;
	}

	protected String createName(BioSeq aseq, List<SeqSymmetry> symList, String separator) {
		String newname =
				getDisplay().toLowerCase() + ": " + (symList.size() == 2 ? "(" + symList.get(0).getID() + ")" + separator + "(" + symList.get(1).getID() + ")"
				: "(..." + symList.size() + ")");
		newname = GraphSymUtils.getUniqueGraphID(newname, aseq);
		return newname;
	}

	protected abstract String getSymbol();

	protected abstract float operate(List<Float> operands);

	private static int getWidth(ArrayList<Integer> widths, int index, boolean hasWidthGraphs) {
		int width = 0;
		if (widths == null) {
			if (hasWidthGraphs) {
				width = 1;
			}
		} else {
			width = widths.get(index);
		}
		return width;
	}

	public static int[] intListToArray(List<Integer> list) {
		int[] array = new int[list.size()];
		for (int i = 0; i < list.size(); i++) {
			array[i] = list.get(i);
		}
		return array;
	}

	public static float[] floatListToArray(List<Float> list) {
		float[] array = new float[list.size()];
		for (int i = 0; i < list.size(); i++) {
			array[i] = list.get(i);
		}
		return array;
	}

	@Override
	public boolean supportsTwoTrack() {
		return false;
	}

	@Override
	public FileTypeCategory getOutputCategory() {
		return FileTypeCategory.Graph;
	}

	@Override
	public int getOperandCountMin(FileTypeCategory category) {
		return category == FileTypeCategory.Graph ? 2 : 0;
	}

	@Override
	public int getOperandCountMax(FileTypeCategory category) {
		return category == FileTypeCategory.Graph ? Integer.MAX_VALUE : 0;
	}

	@Override
	public Map<String, Class<?>> getParameters() {
		return null;
	}

	@Override
	public boolean setParameters(Map<String, Object> parms) {
		return false;
	}

	public static boolean isGraphOperator(Operator operator) {
		for (FileTypeCategory category : FileTypeCategory.values()) {
			if (category == FileTypeCategory.Graph) {
				if (operator.getOperandCountMin(category) < 2) {
					return false;
				}
			} else {
				if (operator.getOperandCountMax(category) > 0) {
					return false;
				}
			}
		}
		return true;
	}
}
