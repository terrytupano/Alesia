package gui.list;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.io.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

//Demo - BasicDnD (The Java™ Tutorials > ... > Drag and Drop and Data Transfer)
//https://docs.oracle.com/javase/tutorial/uiswing/dnd/basicdemo.html
class ListItemTransferHandler extends TransferHandler {
	protected static final DataFlavor FLAVOR = new DataFlavor(List.class, "List of items");
	private final List<Integer> indices = new ArrayList<>();
	private int addIndex = -1; // Location where items were added
	private int addCount; // Number of items added.

// protected ListItemTransferHandler() {
//   super();
//   localObjectFlavor = new ActivationDataFlavor(
//       Object[].class, DataFlavor.javaJVMLocalObjectMimeType, "Array of items");
//   // localObjectFlavor = new DataFlavor(Object[].class, "Array of items");
// }

	@Override
	protected Transferable createTransferable(JComponent c) {
		JList<?> source = (JList<?>) c;
		c.getRootPane().getGlassPane().setVisible(true);
		for (int i : source.getSelectedIndices()) {
			indices.add(i);
		}
		// Object[] transferredObjects = source.getSelectedValuesList().toArray(new
		// Object[0]);
		// return new DataHandler(transferredObjects, FLAVOR.getMimeType());
		return new Transferable() {
			@Override
			public DataFlavor[] getTransferDataFlavors() {
				return new DataFlavor[] { FLAVOR };
			}

			@Override
			public boolean isDataFlavorSupported(DataFlavor flavor) {
				return Objects.equals(FLAVOR, flavor);
			}

			@Override
			public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
				if (isDataFlavorSupported(flavor)) {
					return source.getSelectedValuesList();
				} else {
					throw new UnsupportedFlavorException(flavor);
				}
			}
		};
	}

	@Override
	public boolean canImport(TransferHandler.TransferSupport info) {
		return info.isDrop() && info.isDataFlavorSupported(FLAVOR);
	}

	@Override
	public int getSourceActions(JComponent c) {
		// System.out.println("getSourceActions");
		c.getRootPane().getGlassPane().setCursor(DragSource.DefaultMoveDrop);
		// glassPane.setVisible(true);
		return TransferHandler.MOVE; // TransferHandler.COPY_OR_MOVE;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean importData(TransferHandler.TransferSupport info) {
		TransferHandler.DropLocation tdl = info.getDropLocation();
		if (!(tdl instanceof JList.DropLocation)) {
			return false;
		}
		JList.DropLocation dl = (JList.DropLocation) tdl;
		JList<?> target = (JList<?>) info.getComponent();
		DefaultListModel<Object> listModel = (DefaultListModel<Object>) target.getModel();
		// boolean insert = dl.isInsert();
		int max = listModel.getSize();
		int index = dl.getIndex();
		// index = index < 0 ? max : index; // If it is out of range, it is appended to
		// the end
		// index = Math.min(index, max);
		index = index >= 0 && index < max ? index : max;
		addIndex = index;
		try {
			// Object[] values = (Object[])
			// info.getTransferable().getTransferData(localObjectFlavor);
			List<?> values = (List<?>) info.getTransferable().getTransferData(FLAVOR);
			for (Object o : values) {
				int i = index++;
				listModel.add(i, o);
				target.addSelectionInterval(i, i);
			}
			addCount = values.size();
			return true;
		} catch (UnsupportedFlavorException | IOException ex) {
			return false;
		}
	}

	@Override
	protected void exportDone(JComponent c, Transferable data, int action) {
		// System.out.println("exportDone");
		Component glassPane = c.getRootPane().getGlassPane();
		// glassPane.setCursor(Cursor.getDefaultCursor());
		glassPane.setVisible(false);
		cleanup(c, action == TransferHandler.MOVE);
	}

	private void cleanup(JComponent c, boolean remove) {
		if (remove && !indices.isEmpty()) {
			// If we are moving items around in the same list, we
			// need to adjust the indices accordingly, since those
			// after the insertion point have moved.
			if (addCount > 0) {
				for (int i = 0; i < indices.size(); i++) {
					if (indices.get(i) >= addIndex) {
						indices.set(i, indices.get(i) + addCount);
					}
				}
			}
			JList<?> src = (JList<?>) c;
			DefaultListModel<?> model = (DefaultListModel<?>) src.getModel();
			for (int i = indices.size() - 1; i >= 0; i--) {
				model.remove(indices.get(i));
			}
		}
		indices.clear();
		addCount = 0;
		addIndex = -1;
	}
}
