package gui.jgoodies;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

import com.alee.laf.label.*;
import com.alee.laf.panel.*;
import com.alee.utils.*;

import core.*;

public class ListItem extends JPanel implements MouseListener {

	private Action action;
	private Color mouseOverColor = Color.LIGHT_GRAY.brighter();
	private Color lihgtColor;
	private WebLabel iconLabel, lineLabel;
	private JComponent rightComponent;
	private JPanel rightPanel;

	public ListItem() {
		setOpaque(true);
		this.lihgtColor = ColorUtils.intermediate(getBackground(), Color.WHITE, 0.7f);
		this.rightPanel = new WebPanel();
		BoxLayout layout = new BoxLayout(rightPanel, BoxLayout.Y_AXIS);
		rightPanel.setLayout(layout);
		rightPanel.setOpaque(true);
		setBackground(lihgtColor);
		this.lineLabel = new WebLabel();
		this.iconLabel = new WebLabel();
		iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
		iconLabel.setVerticalAlignment(SwingConstants.CENTER);
		iconLabel.setBorder(TUIUtils.STANDAR_EMPTY_BORDER);
		this.rightComponent = new WebLabel();
		setLayout(new BorderLayout());
		rightPanel.add(rightComponent);
		add(iconLabel, BorderLayout.WEST);
		add(lineLabel, BorderLayout.CENTER);
		add(rightPanel, BorderLayout.EAST);
	}

	public ListItem(Action action) {
		this();
		setAction(action);
	}

	public void setIcon(Icon icon) {
		iconLabel.setIcon(icon);
	}

	public void setAction(Action action) {
		setAction(action, 32, true);
	}

	public void setSmallAction(Action action) {
		setAction(action, 20, false);
	}

	private void setAction(Action action, int iconSize, boolean withText) {
		this.action = action;
		String message = withText ? action.getValue(javax.swing.Action.SHORT_DESCRIPTION).toString() : null;
		TUIUtils.overRideIcons(iconSize, TUIUtils.ACCENT_COLOR, action);
		String html = TStringUtils.getTitleText(action.getValue(javax.swing.Action.NAME).toString(), message);
		lineLabel.setText(html);
		setIcon((ImageIcon) action.getValue(Action.LARGE_ICON_KEY));
		setActionArrowComponent();
		setBorderVisible(true);
		addMouseListener(this);
	}

	public void setActionArrowComponent() {
		WebLabel label = new WebLabel(TUIUtils.getFontIcon('\uE315', 20, TUIUtils.ACCENT_COLOR));
		setRightComponent(label);
	}

	/**
	 * return an instance of {@link ListItem} special for input data in a form.
	 * 
	 * @param fieldName      - the field name of the input component
	 * @param rightComponent - the input component for "input data"
	 * 
	 * @return the item
	 */
	public static ListItem getItemForField(String fieldName, JComponent rightComponent) {
		String tit = TStringUtils.getString(fieldName);
		String msg = TStringUtils.getString(fieldName + ".tt");
		ListItem item = new ListItem();
		item.setLine(tit, msg);
		item.setBorderVisible(true);
		item.lineLabel.setBorder(TUIUtils.STANDAR_EMPTY_BORDER);
		item.setRightComponent(rightComponent);
		item.rightPanel.setBorder(TUIUtils.STANDAR_EMPTY_BORDER);
		item.iconLabel.setBorder(null);
		return item;
	}

	@Override
	public void setOpaque(boolean isOpaque) {
		super.setOpaque(isOpaque);
		if (rightPanel != null)
			rightPanel.setOpaque(isOpaque);
	}

	public void setBorderVisible(boolean visible) {
		Border border3 = new RoundedBorder(3, Color.LIGHT_GRAY);
		setBorder(border3);
	}

	public void setLine(String title, String message) {
		this.lineLabel.setText(TStringUtils.getTitleText(title, message));
	}

	public void setBackground(Color bg) {
		super.setBackground(bg);
		if (rightPanel != null)
			rightPanel.setBackground(bg);
	}

	public void setRightComponent(JComponent component) {
		rightPanel.removeAll();
		Box verticalBox = Box.createVerticalBox();
		verticalBox.add(Box.createVerticalGlue());
		component.setMaximumSize(component.getPreferredSize());
		verticalBox.add(component);
		verticalBox.add(Box.createVerticalGlue());
		rightPanel.add(verticalBox);
		this.rightComponent = component;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		setBackground(lihgtColor);
		ActionEvent event = new ActionEvent(this, 0, "terry");
		action.actionPerformed(event);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		setBackground(lihgtColor);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		setBackground(lihgtColor);
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		setBackground(mouseOverColor);
	}

	@Override
	public void mouseExited(MouseEvent e) {
		setBackground(lihgtColor);
	}

	private static class RoundedBorder implements Border {

		private int radius;
		private Color color;

		public RoundedBorder(int radius, Color color) {
			this.color = color;
			this.radius = radius;
		}

		@Override
		public Insets getBorderInsets(Component c) {
			return new Insets(this.radius + 1, this.radius + 1, this.radius + 2, this.radius);
		}

		@Override
		public boolean isBorderOpaque() {
			return true;
		}

		@Override
		public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
			g.setColor(color);
			g.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
		}
	}
}
