package com.gorillalogic.monkeyconsole.editors.utils.devicematrixtable.tree;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.json.JSONException;

import com.gorillalogic.monkeyconsole.editors.utils.CloudServiceException;

public class DeviceMatrixTreeViewContentProvider implements ITreeContentProvider {
	private DeviceMatrixTreeViewModel model;

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		model = (DeviceMatrixTreeViewModel) newInput;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		try {
			return model.getCategories().toArray();
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		} catch (CloudServiceException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof AndroidVersion) {
			return ((AndroidVersion) parentElement).getTodos().toArray();
		}
		return null;
	}

	@Override
	public Object getParent(Object element) {
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof AndroidVersion) {
			return ((AndroidVersion) element).getTodos().size() > 0;
		}
		return false;
	}
}
