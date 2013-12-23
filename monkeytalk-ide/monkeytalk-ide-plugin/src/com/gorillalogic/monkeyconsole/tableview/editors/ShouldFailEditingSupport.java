package com.gorillalogic.monkeyconsole.tableview.editors;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;

import com.gorillalogic.monkeyconsole.tableview.TableRow;

public class ShouldFailEditingSupport extends EditingSupport {
	private final TableViewer viewer;

	public ShouldFailEditingSupport(TableViewer viewer) {
		super(viewer);
		this.viewer = viewer;
	}

	@Override
	protected boolean canEdit(Object element) {
		return true;
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		CheckboxCellEditor editor = new CheckboxCellEditor(null, SWT.CHECK | SWT.READ_ONLY);
		editor.getLayoutData().horizontalAlignment = SWT.CENTER;
		return editor;
	}

	@Override
	protected Object getValue(Object element) {
		if (((TableRow) element).isComment())
			return "";

		Boolean should_fail = ((TableRow) element).shouldFail();
		return should_fail;
	}

	@Override
	protected void setValue(Object element, Object value) {
		boolean checkState = (Boolean) value;
		if (checkState) {
			((TableRow) element).setModifier("shouldfail", "true");
		} else {
			((TableRow) element).setModifier("shouldfail", null);
		}
		dataChanged();
		viewer.refresh();

		getViewer().update(element, null);
	}

	public void dataChanged() {

	};
}
