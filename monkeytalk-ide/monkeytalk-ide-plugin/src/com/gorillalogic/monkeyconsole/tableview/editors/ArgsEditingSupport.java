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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;

import com.gorillalogic.monkeyconsole.tableview.TableRow;
import com.gorillalogic.monkeytalk.api.meta.API;


public class ArgsEditingSupport extends EditingSupport {

	private final TableViewer viewer;

	public ArgsEditingSupport(TableViewer viewer) {
		super(viewer);
		this.viewer = viewer;
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		if(((TableRow) element).isComment()){
			return null;
		}
		final TableRow currentRow = (TableRow) element;
		Map<String,String> namevalPairs = new HashMap<String, String>();
		List<String> argNames = null;
		if(currentRow == null || currentRow.getComponentType() == null || currentRow.getAction() == null){
			argNames = new ArrayList<String>();
        } else {
        	if(null != API.getComponent(currentRow.getComponentType())){
        		if(null != API.getComponent(currentRow.getComponentType()).getAction(currentRow.getAction()))
                      argNames = API.getComponent(currentRow.getComponentType()).getAction(currentRow.getAction()).getArgNames();
        	}
        }
		if(argNames == null){
			argNames = new ArrayList<String>();	
			argNames.add("args");
		}
        int i = 0;
        if(argNames.size() == 1 && argNames.contains("args")){
        	namevalPairs.put("args", currentRow.getArgs().size() <= i ? "" : currentRow.getArgsAsString() );
        } else {
        for(String argName : argNames){
			namevalPairs.put(argName, currentRow.getArgs().size() <= i ? "" : currentRow.getArgs().get(i) );
			i++;
		}
        }
		return new ArgumentDialogCellEditor(viewer.getTable(), namevalPairs, currentRow);

	}

	@Override
	protected boolean canEdit(Object element) {
		return true;
	}

	@Override
	protected Object getValue(Object element) {
		return ((TableRow) element).getArgsAsString() != null ? ((TableRow) element).getArgsAsString() : "";
	}

	@Override
	protected void setValue(Object element, Object value) {
		((TableRow) element).setArgsAndModifiers((String)value);
		dataChanged();
		viewer.refresh();
	}
	public void dataChanged(){
		
	};
}