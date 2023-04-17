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

import java.util.*;
import java.util.function.*;

import javax.swing.table.*;

import org.javalite.activejdbc.*;

public class TAbstractTableModel extends AbstractTableModel {

	private List<Model> lazyList;
	private TableRowSorter<TAbstractTableModel> tableRowSorter;
	private boolean allowCellEdit;
	private Map<String, Map<String,String>> referenceColumns;
	private Vector<String> attributeNames;
	private Function<String, List<Model>> function;

	/**
	 * new instance
	 * 
	 * @param function - the function that will process and retrive the result
	 * @param colums - column information for this list
	 */
	public TAbstractTableModel(Function<String, List<Model>> function, Map<String, ColumnMetadata> colums) {
		setDBParameters(function, colums);
	}

	/**
	 * set the need parameter for this instance.
	 * 
	 * @param function - the function that process and retrive the result
	 * @param colums - column information for this list
	 */
	public void setDBParameters(Function<String, List<Model>> function, Map<String, ColumnMetadata> colums) {
		this.function = function;
		this.lazyList = function.apply(null);
		this.referenceColumns = new HashMap<>();
		this.attributeNames = new Vector<>(colums.keySet());
	}
	/**
	 * Return the indix of the model pass as argument. if the model is not in the list, return -1
	 * 
	 * @param model - {@link Model} to find
	 * 
	 * @return index of the model
	 */
	public int indexOf(Model model) {
		return lazyList.indexOf(model);
	}

	public void freshen() {
		if (lazyList == null) {
			return;
		}
		int bef_rc = lazyList.size();
		lazyList = function.apply(null);
		int aft_rc = lazyList.size();
		tableRowSorter.allRowsChanged();
		if (aft_rc == 0) {
			return;
		}
		if (bef_rc != aft_rc) {
			fireTableDataChanged();
		} else {
			try {
				tableRowSorter.allRowsChanged();
				fireTableRowsUpdated(0, aft_rc - 1);
			} catch (Exception e) {
				// temporal
				fireTableDataChanged();
				System.out.println("--------");
				// e.printStackTrace();
			}
		}
	}

	@Override
	public Class<?> getColumnClass(int idx) {
		String atn = attributeNames.get(idx);
		Model m = lazyList.get(0);
		Object obj = m.get(atn);
//		if (obj == null)
//			System.out.println("TAbstractTableModel.getColumnClass()");
		return obj == null ? Object.class : obj.getClass();
	}

	@Override
	public int getColumnCount() {
		return attributeNames.size();
	}

	@Override
	public String getColumnName(int col) {
		return attributeNames.elementAt(col);
	}

	/**
	 * return the Record found in <code>row</code> position
	 * 
	 * @param row - row
	 * 
	 * @return Record
	 */
	public Model getModelAt(int row) {
		int row1 = tableRowSorter == null ? row : tableRowSorter.convertRowIndexToModel(row);
		return lazyList.get(row1);
	}

	@Override
	public int getRowCount() {
		return lazyList.size();
	}

	@Override
	public Object getValueAt(int row, int col) {
		Model model = lazyList.get(row);
		String atn = attributeNames.elementAt(col);
		Object rv = model.get(atn);

		// check the reference by column
		Map<String,String> map = referenceColumns.get(atn);
		if (map != null) {
			Object rv1 = map.get(rv);
			// 180311: rv.tostring to allow from number, bool to key from TEntrys ( that are generaly created from
			// propeties files) only if previous request is null
			rv1 = (rv1 == null) ? map.get(rv.toString()) : rv1;
			// 180416: if no reference found, remark the value (NOTE: temp: not fully tested. wath about numbers or
			// tentry columns??
			rv = (rv1 == null) ? "<html><FONT COLOR='#FF0000'><i>" + rv + "</html>" : rv1;
		}
		return rv;
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		// TODO: Check later.
		// String atn = attributeNames.get(column);
		// Model m = lazyList.get(row);
		// String keys[] = m.getCompositeKeys();
		boolean ispk = false;
		// for (String k : keys) {
		// ispk = atn.equals(k) ? true : ispk;
		// }
		// cell is editable iff allow cell edit and is not a keyfield
		return allowCellEdit && !ispk;
	}

	public void setCellEditable(boolean ce) {
		allowCellEdit = ce;
	}

	/**
	 * Asociate a sublist of values for the internal value in this model. when the column value is request by JTable,
	 * the internal value is mapped whit this list to return the meaning of the value instead the value itselft
	 * 
	 * @param refc - an instance of {@link Hashtable}
	 */
	public void setReferenceColumn(Map<String, Map<String,String>> colums) {
		referenceColumns = colums;
	}

	public void setTableRowSorter(TableRowSorter<TAbstractTableModel> trs) {
		this.tableRowSorter = trs;
	}
}
