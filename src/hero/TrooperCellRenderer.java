package hero;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import core.*;
import datasource.*;
import gui.jgoodies.*;

public  class TrooperCellRenderer extends DefaultListCellRenderer {

	protected ListItem listItem;
	protected String titlePattern, messagePattern;

	public TrooperCellRenderer() {
		this.titlePattern = "<chair>. <trooper>";
		this.messagePattern = "<description>";
		this.listItem = new ListItem();
		listItem.setIcon(TResources.getIcon("robot.png", 25));
		listItem.setActionArrowComponent();
		listItem.setOpaque(false);
	}
	
	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
			boolean cellHasFocus) {
//		Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		TrooperParameter model = (TrooperParameter) value;
		@SuppressWarnings("static-access")
		Set<String> attributes = model.attributeNames();
		String titleTxt = titlePattern;
		String msgTxt = messagePattern;
		for (String att : attributes) {
			String val = model.getString(att); 
			titleTxt = titleTxt.replace("<"+att+">", val == null ? "null": val);
			msgTxt = msgTxt.replace("<"+att+">", val == null ? "null": val);
		}
		listItem.setLine(titleTxt, msgTxt);
		return listItem;
	}	
}
