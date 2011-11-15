package com.affymetrix.genoviz.swing;

import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.io.IOException;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

public class DragDropTree extends JTree implements DragSourceListener, DropTargetListener, DragGestureListener, Autoscroll {

	private final DragSource source;
	private TransferableTreeNode transferable;
	private DefaultMutableTreeNode oldNode;
	private final boolean DEBUG = false;
	private int margin = 12;

	public DragDropTree() {
		super();

		source = new DragSource();
		this.setDropTarget(new DropTarget(this, this));
		source.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_MOVE, this);
	}

	/*
	 * Drag Gesture Handler
	 */
	public void dragGestureRecognized(DragGestureEvent dge) {
		TreePath path = getSelectionPath();
		if ((path == null) || (path.getPathCount() <= 1)) {
			// We can't move the root node or an empty selection
			return;
		}
		oldNode = (DefaultMutableTreeNode) path.getLastPathComponent();
		transferable = new TransferableTreeNode(path);
		//source.startDrag(dge, DragSource.DefaultMoveNoDrop, transferable, this);

		// If you support dropping the node anywhere, you should probably
		// start with a valid move cursor:
		source.startDrag(dge, DragSource.DefaultMoveDrop, transferable, this);
	}

	/*
	 * Source Drag Event Handlers
	 */
	public void dragEnter(DragSourceDragEvent dsde) {
	}

	public void dragExit(DragSourceEvent dse) {
	}

	public void dragOver(DragSourceDragEvent dsde) {
	}

	public void dropActionChanged(DragSourceDragEvent dsde) {
		if (DEBUG) {
			System.out.println("Action: " + dsde.getDropAction());
			System.out.println("Target Action: " + dsde.getTargetActions());
			System.out.println("User Action: " + dsde.getUserAction());
		}
	}

	public void dragDropEnd(DragSourceDropEvent dsde) {
		if (DEBUG) {
			System.out.println("Drop Action: " + dsde.getDropAction());
		}

		if (dsde.getDropSuccess() && (dsde.getDropAction() == DnDConstants.ACTION_MOVE)) {
			((DefaultTreeModel) getModel()).removeNodeFromParent(oldNode);
		}

	}

	/*
	 * Target Drag Event Handlers
	 */
	public void dragEnter(DropTargetDragEvent dtde) {
		dtde.acceptDrag(dtde.getDropAction());
	}

	public void dragOver(DropTargetDragEvent dtde) {
		dtde.acceptDrag(dtde.getDropAction());
	}

	public void dragExit(DropTargetEvent dte) {
	}

	public void dropActionChanged(DropTargetDragEvent dtde) {
	}

	public void drop(DropTargetDropEvent dtde) {
		Point pt = dtde.getLocation();
		DropTargetContext dtc = dtde.getDropTargetContext();
		JTree tree = (JTree) dtc.getComponent();
		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
		
		TreePath parentPath = tree.getClosestPathForLocation(pt.x, pt.y);
		TreePath rootPath = tree.getPathForRow(0);
		TreePath dragedPath = tree.getSelectionPath();
		
		int parentRow = tree.getRowForPath(parentPath);
		int dragedRow = tree.getRowForPath(dragedPath);;

		DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) parentPath.getLastPathComponent();
		DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) rootPath.getLastPathComponent();
		DefaultMutableTreeNode dragNode;

		try {
			Transferable tr = dtde.getTransferable();
			DataFlavor[] flavors = tr.getTransferDataFlavors();
			for (int i = 0; i < flavors.length; i++) {
				if (tr.isDataFlavorSupported(flavors[i])) {
					dtde.acceptDrop(dtde.getDropAction());
					dragedPath = (TreePath) tr.getTransferData(flavors[i]);
					dragNode = (DefaultMutableTreeNode) dragedPath.getLastPathComponent();
				
					if (parentNode.isLeaf()) {
						MutableTreeNode actualparent = (MutableTreeNode) parentNode.getParent();
						int index = model.getIndexOfChild(actualparent, parentNode);
						model.insertNodeInto(dragNode, actualparent, index + 1);
					} else if (parentRow == dragedRow){
						model.insertNodeInto(dragNode, rootNode, rootNode.getChildCount());
					} else {
						model.insertNodeInto(dragNode, parentNode, 0);
					}

					//Keep the selection after moving node
					TreePath path = new TreePath(model.getPathToRoot(dragNode));
					tree.setSelectionPath(path);
					dtde.dropComplete(true);
					return;
				}
			}
			dtde.rejectDrop();
		} catch (Exception e) {
			e.printStackTrace();
			dtde.rejectDrop();
		}
	}

	public void autoscroll(Point p) {
		int realrow = getRowForLocation(p.x, p.y);
		Rectangle outer = getBounds();
		realrow = (p.y + outer.y <= margin ? realrow < 1 ? 0 : realrow - 1
				: realrow < getRowCount() - 1 ? realrow + 1 : realrow);
		scrollRowToVisible(realrow);
	}

	public Insets getAutoscrollInsets() {
		Rectangle outer = getBounds();
		Rectangle inner = getParent().getBounds();
		return new Insets(inner.y - outer.y + margin, inner.x - outer.x
				+ margin, outer.height - inner.height - inner.y + outer.y
				+ margin, outer.width - inner.width - inner.x + outer.x
				+ margin);
	}

	//TransferableTreeNode.java
	//A Transferable TreePath to be used with Drag & Drop applications.
	//
	class TransferableTreeNode implements Transferable {

		public DataFlavor TREE_PATH_FLAVOR = new DataFlavor(TreePath.class, "Tree Path");
		DataFlavor flavors[] = {TREE_PATH_FLAVOR};
		TreePath path;

		public TransferableTreeNode(TreePath tp) {
			path = tp;
		}

		public synchronized DataFlavor[] getTransferDataFlavors() {
			return flavors;
		}

		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return (flavor.getRepresentationClass() == TreePath.class);
		}

		public synchronized Object getTransferData(DataFlavor flavor)
				throws UnsupportedFlavorException, IOException {
			if (isDataFlavorSupported(flavor)) {
				return (Object) path;
			} else {
				throw new UnsupportedFlavorException(flavor);
			}
		}
	}
}
