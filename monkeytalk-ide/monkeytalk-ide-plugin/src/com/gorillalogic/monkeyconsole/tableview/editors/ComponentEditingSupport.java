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
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;

import com.gorillalogic.monkeyconsole.tableview.TableRow;
import com.gorillalogic.monkeytalk.api.meta.API;


public class ComponentEditingSupport extends EditingSupport {

	private final TableViewer viewer;
	private ComboBoxCellEditor editor;
	private String[] limitedComponentSet;
	
	public ComponentEditingSupport(TableViewer viewer, String [] limitedComponentSet) {
		super(viewer);
		this.viewer = viewer;
		this.limitedComponentSet = limitedComponentSet;
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		String[] componentSet;
		if(limitedComponentSet != null){
			componentSet = limitedComponentSet;
		} else {
			componentSet = API.getComponentTypes().toArray(new String[API.getComponentTypes().size()]);
		}
		editor =  new ComboBoxCellEditor(viewer.getTable(), componentSet){
			@Override
			protected Object doGetValue() {
				return ((CCombo)getControl()).getText();
			}

			@Override
			protected void doSetValue(Object value) {
				 ((CCombo)getControl()).setText((String)value);
			}
			
		};
		 
		return editor;
	}

	@Override
	protected boolean canEdit(Object element) {
		return true;
	}

/*
 * (non-Javadoc)
 * @see org.eclipse.jface.viewers.EditingSupport#getValue(java.lang.Object)
 * Gets the value from the row and initializes the editor value with this value
 */
	@Override
	protected Object getValue(Object element) {
	     if(((TableRow)element).isComment()){
	    	 return ((TableRow)element).getRawCommand();
	     }
		 return ((TableRow)element).getComponentType();
	}
	
/*
 * (non-Javadoc)
 * @see org.eclipse.jface.viewers.EditingSupport#setValue(java.lang.Object, java.lang.Object)
 * after the user commits the change it is set on the row in this method
 */
	@Override
	protected void setValue(Object element, Object value) {
		((TableRow) element).setComponentType((String)value);
		dataChanged();
		viewer.refresh();
	}
	
	public void dataChanged(){
		
	};
}