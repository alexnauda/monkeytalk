package com.gorillalogic.monkeyconsole.cloudview.ui;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;




public class UICloudContentProvider implements IStructuredContentProvider {
	  /**
	   * Returns the Person objects
	   */
	  public Object[] getElements(Object inputElement) {
	    return ((List) inputElement).toArray();
	  }

	  /**
	   * Disposes any created resources
	   */
	  public void dispose() {
	    // Do nothing
	  }

	  /**
	   * Called when the input changes
	   */
	  public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	    // Ignore
	  }
	}