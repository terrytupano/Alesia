package gui.list;

import java.awt.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

class ListItemListCellRenderer<E extends IconListItem> implements ListCellRenderer<E> {
	private final JPanel renderer = new JPanel(new BorderLayout());
	private final JLabel icon = new JLabel((Icon) null, SwingConstants.CENTER);
	private final JLabel label = new JLabel("", SwingConstants.CENTER);
	// private final Border dotBorder = new DotBorder(2, 2, 2, 2);
	// private final Border empBorder = BorderFactory.createEmptyBorder(2, 2, 2, 2);
	private final Border focusBorder = UIManager.getBorder("List.focusCellHighlightBorder");
	private final Border noFocusBorder; // = UIManager.getBorder("List.noFocusBorder");

	protected ListItemListCellRenderer() {
		Border b = UIManager.getBorder("List.noFocusBorder");
		if (Objects.isNull(b)) { // Nimbus???
			Insets i = focusBorder.getBorderInsets(label);
			b = BorderFactory.createEmptyBorder(i.top, i.left, i.bottom, i.right);
		}
		noFocusBorder = b;
		icon.setOpaque(false);
		label.setForeground(renderer.getForeground());
		label.setBackground(renderer.getBackground());
		label.setBorder(noFocusBorder);

		renderer.setOpaque(false);
		renderer.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		renderer.add(icon);
		renderer.add(label, BorderLayout.SOUTH);
	}

	@Override
	public Component getListCellRendererComponent(JList<? extends E> list, E value, int index, boolean isSelected,
			boolean cellHasFocus) {
		label.setText(value.title);
		label.setBorder(cellHasFocus ? focusBorder : noFocusBorder);
		if (isSelected) {
			icon.setIcon(value.selectedIcon);
			label.setForeground(list.getSelectionForeground());
			label.setBackground(list.getSelectionBackground());
			label.setOpaque(true);
		} else {
			icon.setIcon(value.icon);
			label.setForeground(list.getForeground());
			label.setBackground(list.getBackground());
			label.setOpaque(false);
		}
		return renderer;
	}
}