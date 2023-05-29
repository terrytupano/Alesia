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
import java.util.List;
import java.util.function.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import org.javalite.activejdbc.*;
import org.jdesktop.application.*;

import com.alee.laf.table.*;

import core.*;

public abstract class TUIListPanel extends TUIPanel implements TableModelListener, ListSelectionListener {

	public static final String MODEL_SELECTED = "ModelSelected";
	private JScrollPane scrollPane;
	private TAbstractTableModel tableModel;
	private WebTable table;
	private Map<String, Map<String, String>> referenceColumns;
	private String iconParameters;
	private List<String> tableColumns;
	private Map<String, ColumnMetadata> columnMetadata;
	private boolean enableRowSorter;

	public TUIListPanel() {
		this.tableColumns = new ArrayList<>();
		this.scrollPane = new JScrollPane();
		this.referenceColumns = new HashMap<>();
		scrollPane.getViewport().setBackground(Color.WHITE);
//		js_pane.getViewport().setBorder(null);
		setBodyComponent(scrollPane);
	}

	public void freshen() {

	}

	// TODO: test method to find out the fucking best solution to refresh the table
	// data
	public void refresh() {
		tableModel.fireTableDataChanged();
	}

	public String getIconParameters() {
		return iconParameters;
	}

	public void setMessage(String msgId, Object... msgData) {
		setMessage(msgId, false, msgData);
	}

	/**
	 * replace the {@link JComponent} set using the method
	 * {@link #setBodyComponent(JComponent)} and present a new component to display
	 * the selected message. If the msgId parameter is <code>null</code>, hide the
	 * message component and present the body component
	 * 
	 * @param msgId     - message id for text
	 * @param visibleTB - indicate if the toolbarpanel will be visible or not
	 *                  visible
	 * @param msgData   - Substitution data
	 * 
	 * @see UIComponentPanel#getToolBar()
	 */
	public void setMessage(String msgId, boolean visibleTB, Object... msgData) {
//		if (msgId == null) {
//			getToolBar().setVisible(true);
//			 remove(bodyMessageJComponent);
//			add(bodyJComponent, BorderLayout.CENTER);
//		} else {
//			TValidationMessage te = new TValidationMessage(msgId, msgData);
//			blkinfoLabel.setIcon(te.getIcon());
//			blkinfoLabel.setText(te.getMessage());
//			// blkinfoLabel.setVerticalTextPosition(JLabel.TOP);
//
//			toolBar.setVisible(visibleTB);
//			if (bodyJComponent != null)
//				remove(bodyJComponent);
//			add(bodyMessageJComponent, BorderLayout.CENTER);
//		}
//		repaint();
	}

	/**
	 * Return the current selected model in the table. If no element has been
	 * selected, return <code>null</code>
	 * 
	 * @return the selected {@link Model}
	 */
	public Model getModel() {
		Model model = null;
		int index = table.getSelectedRow();
		if (index > -1)
			model = tableModel.getModelAt(index);
		return model;
	}

	/**
	 * Return the current selected models
	 * 
	 * @return selected models
	 */
	public List<Model> getModels() {
		int[] indexes = table.getSelectedRows();
		List<Model> models = new ArrayList<>();
		for (int rc = 0; rc < indexes.length; rc++) {
			models.add(tableModel.getModelAt(indexes[rc]));
		}
		return models;
	}

	public TAbstractTableModel getTableModel() {
		return tableModel;
	}

	abstract public TUIFormPanel getTUIFormPanel(ApplicationAction action);

	public WebTable getTable() {
		return table;
	}

	abstract public void init();

	/**
	 * look for the model pass as argument and if the model is inside this list,
	 * select him.
	 * 
	 * @param model - the model to select
	 */
	public void selectModel(Model model) {
		int idx = tableModel.indexOf(model);
		setEnableActions("scope", "element", idx > -1);
		if (idx > -1) {
			table.getSelectionModel().setValueIsAdjusting(true);
			table.getSelectionModel().removeIndexInterval(0, tableModel.getRowCount());
			table.getSelectionModel().addSelectionInterval(idx, idx);
			table.getSelectionModel().setValueIsAdjusting(false);
		}
	}

	public void setEnableRowSorter(boolean enableRowSorter) {
		this.enableRowSorter = enableRowSorter;
	}

	public void setColumns(String... columns) {
		this.tableColumns = Arrays.asList(columns);
	}

	/**
	 * set the database parameter for this instance.
	 * 
	 * @param function - a {@link Function} that retrieve and filter the data
	 * @param columns  the columns metadata
	 */
	public void setDBParameters(Function<String, List<Model>> function, Map<String, ColumnMetadata> columnMetadata) {
		if (function != null || columnMetadata != null) {
			this.columnMetadata = columnMetadata;

			this.tableModel = new TAbstractTableModel(function, columnMetadata);
			tableModel.setReferenceColumn(referenceColumns);
			tableModel.addTableModelListener(this);

			TableColumnModel columnModel = getColumnModel();

			this.table = new WebTable(tableModel, columnModel);
			table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			table.setShowGrid(false);
//			table.optimizeColumnWidths(true);
			table.setOptimizeRowHeight(true);
			ListSelectionModel lsm = table.getSelectionModel();
			lsm.addListSelectionListener(this);

			// row sorter
			if (enableRowSorter) {
				TableRowSorter<TAbstractTableModel> sorter = new TableRowSorter<>(tableModel);
				table.setRowSorter(sorter);
				tableModel.setTableRowSorter(sorter);
			}

			// cellrenderer
//				TDefaultTableCellRenderer tdcr = new TDefaultTableCellRenderer();
//				setDefaultRenderer(tdcr);
//				setIconParameters(null);

			scrollPane.setViewportView(table);
			setEnableActions("scope", "element", false);
		} else {
//			 setMessage("ui.msg11");
		}
	}

	/**
	 * set all element in the table editable. this method perform auto save when
	 * some cell is edited
	 * 
	 * @param editable - <code>true</code> the table cell are editable and are auto
	 *                 saved editingStopped.
	 */
	public void setEditable(boolean editable) {
		table.setEditable(editable);
		tableModel.setCellEditable(editable);

		CellEditorListener listener = new CellEditorListener() {
			@Override
			public void editingStopped(ChangeEvent e) {
				getModel().save();
			}

			@Override
			public void editingCanceled(ChangeEvent e) {
				// do nothing
			}
		};

		for (int i = 0; i < table.getColumnCount(); i++) {
			CellEditor cellEditor = table.getCellEditor(0, i);
			cellEditor.addCellEditorListener(listener);
		}
	}

	/**
	 * set the cellRenderer for the {@link WebTable}. all know class inside of the
	 * table will be display using this renderer
	 * 
	 * @param tdcr instance of {@link TDefaultTableCellRenderer} public void
	 *             setDefaultRenderer(TDefaultTableCellRenderer tdcr) {
	 *             table.setDefaultRenderer(TSEntry.class, tdcr);
	 *             table.setDefaultRenderer(String.class, tdcr);
	 *             table.setDefaultRenderer(Date.class, tdcr);
	 *             table.setDefaultRenderer(Integer.class, tdcr);
	 *             table.setDefaultRenderer(Double.class, tdcr);
	 *             table.setDefaultRenderer(Long.class, tdcr); }
	 */

	/**
	 * Indica los parametros necesarios para presentar el icono que adorna la celda.
	 * los parametros descritos en forma parm;parm;...
	 * 
	 * @param column - Numero de la columna donde se desea presentar el icono (vista
	 *               tabla)
	 * @param icon   - nombre del archivo icono o prefijo (si se especifica la
	 *               columna valcol). si este valor es *, el nombre especificado en
	 *               <code>valcol</code> debe ser instancia de byte[] donde esta
	 *               almacenado el icono.
	 * @param valcol - nombre de la columna donde se obtendra el valor que sera
	 *               concatenado con el nombre especificado en parametro
	 *               <code>icon</code> para deterinar el nombre del archivo icono
	 *               (puede no especificarse). si <code>icon="*"</code> el campo
	 *               especificado aqui, contiene los byte[] para crear la imagen
	 *               <p>
	 *               ejemplo:
	 *               <li>0;user_;t_usroll: idica que se desa colocar en la columna 0
	 *               el icono cuyo nombre comienza con user_ y usar el valor de la
	 *               columna t_usroll para concaternarlo con el nombre del archivo
	 *               icono
	 *               <li>3;users4: colocar el icono llamado users4 en la 4ta columna
	 *               <li>0;*;userphoto: crea un icono usando los byte[] almacenados
	 *               en la columan userphoto y lo coloca en la columna 0
	 * 
	 * 
	 */
	public void setIconParameters(String ip) {
		TDefaultTableCellRenderer tdcr = (TDefaultTableCellRenderer) table.getDefaultRenderer(String.class);
		if (ip != null) {
			String[] col_ico_val = ip.split(";");
			String vc = (col_ico_val.length > 2) ? col_ico_val[2] : null;
			tdcr.setIconParameters(Integer.parseInt(col_ico_val[0]), col_ico_val[1], vc);
		} else {
//			tdcr.setIconParameters(0, "document", null);
		}
	}

	/**
	 * Associate a sublist of values for the internal value in this model. when the
	 * column value is request by JTable, the internal value is mapped whit this
	 * list to return the meaning of the value instead the value itselft
	 * 
	 * @param fieldName - column name
	 * 
	 * @param telist    array of TEntry
	 */
	public void setReferenceColumn(String fieldName, List<TSEntry> telist) {
		Map<String, String> ht = new HashMap<>();
		telist.forEach(te -> ht.put(te.getKey(), te.getValue()));
		referenceColumns.put(fieldName.toUpperCase(), ht);
	}

	@Override
	public void tableChanged(TableModelEvent e) {
		if (e.getSource() == tableModel) {
			table.tableChanged(e);
			// tJlist.ensureIndexIsVisible(sortableModel.getRowCount());
			setEnableActions("scope", "element", !table.getSelectionModel().isSelectionEmpty());
		}
	}

	public TableColumn getTableColumn(String columnName) {
		return fieldTableColumns.get(columnName);
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) {
			return;
		}
		setEnableActions("scope", "element", !((ListSelectionModel) e.getSource()).isSelectionEmpty());
		firePropertyChange(MODEL_SELECTED, null, getModel());
	}

	/** save the columns for further reference */
	private HashMap<String, TableColumn> fieldTableColumns = new HashMap<>();

	private TableColumnModel getColumnModel() {
		DefaultTableColumnModel columnModel = new DefaultTableColumnModel();
		Vector<String> tableCols = new Vector<>(columnMetadata.keySet());
		// show all columns in alphabetic order
		if (tableColumns.isEmpty()) {
			columnMetadata.keySet().forEach(k -> tableColumns.add(k));
			Collections.sort(tableColumns);
		}
		for (String field : tableColumns) {
//			int len = columnMetadata.get(field).getColumnSize();
//			TableColumn column = new TableColumn(tableCols.indexOf(field), len*10);
			TableColumn column = new TableColumn(tableCols.indexOf(field));
			String header = TStringUtils.getString(field);
			column.setHeaderValue(header);
			fieldTableColumns.put(field, column);
			columnModel.addColumn(column);
		}
		return columnModel;
	}

	public Map<String, ColumnMetadata> getColumnMetadata() {
		return columnMetadata;
	}

}
