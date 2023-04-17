/*******************************************************************************
 * Copyright (C) 2017 terry.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     terry - initial API and implementation
 ******************************************************************************/
package gui;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import com.alee.extended.link.*;
import com.alee.laf.panel.*;
import com.alee.laf.scroll.*;
import com.alee.managers.style.*;
import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import core.*;
import gui.jgoodies.*;

/**
 * presenta la ventana con informacion sobre la aplicacion
 * 
 */
public class AboutPanel extends WebPanel {

	public AboutPanel() {
		super(StyleId.panelTransparent);
		setLayout(new BorderLayout());
		add(getaboutPanel(), BorderLayout.CENTER);
	}

	private JPanel getaboutPanel() {
		JLabel jl = new JLabel(TStringUtils.getString("name"));
		jl.setFont(TUIUtils.H1_Font);
		UrlLinkAction ua = new UrlLinkAction(TStringUtils.getString("homepage"));

		FormLayout layout = new FormLayout("fill:100dlu:grow",
				"pref, pref, 6dlu, pref, 6dlu, pref, 6dlu, pref, fill:100dlu:grow");
		WebPanel jp = new WebPanel();
		DefaultFormBuilder builder = new DefaultFormBuilder(layout, jp).border(Borders.DLU2);

		builder.append(jl);
		builder.append(new JLabel(TStringUtils.getString("title")));
		builder.nextLine(2);
		builder.append(new WebLink(ua));
		builder.nextLine(2);
		builder.append(TStringUtils.getString("about.version")+ TStringUtils.getString("version"));
		builder.nextLine(2);
		builder.append(new JLabel(TStringUtils.getString("about.msg2")));
		builder.append(getOpenSourcePanel());

		return builder.getPanel();
	}

	private WebScrollPane getOpenSourcePanel() {
		// componentes
		Vector<String> lines = createLines();
//		WebPanel jp = new WebPanel(new VerticalFlowLayout());
		WebPanel jp = new WebPanel(new GridLayout(0,1));
		for (String string : lines) {
			String[] lins = string.split(";");
			ListItemView i = new ListItemView(ListItemView.TRIPLE_LINE);
			i.setPrimaryText(lins[0]);
			i.setSecondaryText(lins[1]);
			i.setTertiaryText(lins[2]);
			i.buildView();
			jp.add(i);
		}
		// WebScrollPane sp = new WebScrollPane(StyleId.scrollpaneHovering);
		WebScrollPane sp = new WebScrollPane(jp);
		 sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		// sp.setViewportView(jp);
		return sp;
	}

	/**
	 * crea animacion de lineas
	 * 
	 */
	private Vector<String> createLines() {
		Vector<String> lines = new Vector<>();
		lines.add("Forms framework;Build better screens faster;Copyright (c) 2001-2004 JGoodies Karsten Lentzsch");
		lines.add("iReport 0.5.0;Desing tool for JasperReport;(c) 2002 Giulio Toffol");
		lines.add(
				"JFreeChart ;A free chart library for the Java(tm) platform;Copyright 2000-2005, by Object Refinery Limited and Contributors.");
		lines.add("JSmooth;A VM wrapper toolkit for Windows;Copyright (C) 2003 Rodrigo Reyes");
		lines.add(
				"Looks;Free high-fidelity Windows and multi-platform appearance;Copyright (c) 2001-2004 JGoodies Karsten Lentzsch");
		lines.add("Eclipse;IDE for software development;(c) Copyright Eclipse contributors and others 2000, 2005.");
		lines.add("HSQL ;100% Java Database;Copyright � 2001 - 2005 HSQL Development Group.");
		lines.add("JasperReport ;Open source reporting tool;Copyright (C) 2001-2005 JasperSoft Corporation.");
		lines.add("MySQL Connector/J 3.1.7;JDBC level 4 for MySQL DB;Copyright (c) 2003 MySQL AB");
		lines.add("Animation 1.1.3;Time-based real-time animations;Copyright (c) 2001-2004 JGoodies Karsten Lentzsch");
		lines.add("SQLylog v4.04;Manager for MySQL DB;(c) 2002 - 2005 Webyog SoftWork Pvt. Ltd.");
		lines.add("MySQL;Database Management System;Copyright (c) 2003 MySQL AB");
		lines.add("L2FProd;Common Components;Copyright 2005 L2FProd.com");
		return lines;
	}

	private JPanel getPropertyPanel() {
		Properties properties = System.getProperties();
		Enumeration<Object> e = properties.keys();
		Vector<Object> columns = new Vector<>(2);
		columns.add(TStringUtils.getString("property.name"));
		columns.add(TStringUtils.getString("property.value"));
		Vector<Vector<String>> rows = new Vector<>();
		while (e.hasMoreElements()) {
			Vector<String> row = new Vector<>(2);
			String k = (String) e.nextElement();
			row.add(k);
			row.add(properties.getProperty(k));
			rows.add(row);
		}
		JTable jt = new JTable(rows, columns);
		TUIUtils.fixTableColumn(jt, new int[]{170, 300});
		jt.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		jt.setEnabled(false);
		JScrollPane jsp = new JScrollPane(jt);
		JPanel pb = new JPanel(new BorderLayout());
		pb.add(jsp, BorderLayout.CENTER);
		return pb;
	}
}
