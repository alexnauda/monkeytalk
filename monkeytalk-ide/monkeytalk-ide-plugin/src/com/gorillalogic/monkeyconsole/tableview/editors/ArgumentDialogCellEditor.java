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


import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.gorillalogic.monkeyconsole.tableview.TableRow;



public class ArgumentDialogCellEditor
    extends DialogCellEditor {

    Map<String,String> nameValPairs = new HashMap<String,String>();
    TableRow currentRow;
    private Text textField;


    protected ArgumentDialogCellEditor(Composite parent, Map<String,String> nameValPairs, TableRow currentRow) {
        super(parent);
    	this.nameValPairs = nameValPairs;
    	this.currentRow = currentRow;
    	

    }

    /**
     * @see org.eclipse.jface.viewers.DialogCellEditor#openDialogBox(Control)
     */
    protected Object openDialogBox(Control cellEditorWindow) {
        ArgumentEditorDialog ftDialog = new ArgumentEditorDialog(cellEditorWindow.getShell(), nameValPairs, currentRow);
        String value = (String) getValue();
        int fData = ftDialog.open();
        if (fData == 0) {
            value = ftDialog.getComposedValue();
        } else {
         	currentRow.getArgsAsString();
        }
        return value;
    }
    





    protected Control createContents(Composite cell) {
        textField = new Text(cell, SWT.NONE);
        textField.setFont(cell.getFont());
        textField.setBackground(cell.getBackground());
        textField.addFocusListener(new FocusAdapter() {
                public void focusLost(FocusEvent event) {
                     setValueToModel();
                }
            });

        textField.addListener(SWT.Traverse,new Listener() {
   	   public void handleEvent(Event e) {
   		   ArgumentDialogCellEditor.this.getControl().notifyListeners(SWT.Traverse, e);
   	   }
   	  });
        textField.addKeyListener(new KeyAdapter() {
                public void keyPressed(KeyEvent event) {
                    keyReleaseOccured(event);
                }
            });

        return textField;
    }

    protected void keyReleaseOccured(KeyEvent keyEvent) {
        if (keyEvent.keyCode == SWT.CR || keyEvent.keyCode == SWT.KEYPAD_CR) { // Enter key
            setValueToModel();
        }
        ///
        
        
        ///
        super.keyReleaseOccured(keyEvent);
    }

    protected void setValueToModel() {
         String newValue = textField.getText();
        boolean newValidState = isCorrect(newValue);
        if (newValidState) {
            markDirty();
            doSetValue(newValue);
            currentRow.setArgsAndModifiers(newValue);
        } else {
            // try to insert the current value into the error message.
            setErrorMessage(MessageFormat.format(getErrorMessage(), new Object[] { newValue.toString() }));
        }
    }

    protected void updateContents(Object value) {
        if (textField == null) {
            return;
        }

        String text = "";
        if (value != null) {
            text = value.toString();
        }
        textField.setText(text);
        
    }

    protected void doSetFocus() {
        // Overridden to set focus to the Text widget instead of the Button.
        textField.setFocus();
        textField.selectAll();
    }




    protected String getDialogInitialValue() {
        Object value = getValue();
        if (value == null) {
            return null;
        } else {
            return value.toString();
        }
    }
}

