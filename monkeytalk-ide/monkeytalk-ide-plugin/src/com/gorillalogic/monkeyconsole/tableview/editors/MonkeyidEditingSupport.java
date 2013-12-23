/*  MonkeyTalk - a cross-platform functional testing tool
    Copyright (C) 2012 Gorilla Logic, Inc.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>. */
package com.gorillalogic.monkeyconsole.tableview.editors;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;

import com.gorillalogic.monkeyconsole.tableview.TableRow;


public class MonkeyidEditingSupport extends EditingSupport {

	private final TableViewer viewer;

	public MonkeyidEditingSupport(TableViewer viewer) {
		super(viewer);
		this.viewer = viewer;
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		if(((TableRow) element).isComment()){
			return null;
		}
		return new TextCellEditor(viewer.getTable());
	}

	@Override
	protected boolean canEdit(Object element) {
		return true;
	}

	@Override
	protected Object getValue(Object element) {
		return ((TableRow) element).getMonkeyId();
	}

	@Override
	protected void setValue(Object element, Object value) {
		((TableRow) element).setMonkeyId((String)value);
		dataChanged();
		viewer.refresh();
	}
	public void dataChanged(){
		
	};
}