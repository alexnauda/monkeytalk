package com.gorillalogic.monkeyconsole.componentview.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

import com.gorillalogic.monkeyconsole.componentview.model.DeltaEvent;
import com.gorillalogic.monkeyconsole.componentview.model.IDeltaListener;
import com.gorillalogic.monkeyconsole.componentview.model.Model;
import com.gorillalogic.monkeyconsole.componentview.model.UIComponent;
import com.gorillalogic.monkeyconsole.componentview.model.UIContainer;


public class UIContainerContentProvider implements ITreeContentProvider, IDeltaListener {
	private static Object[] EMPTY_ARRAY = new Object[0];
	protected TreeViewer viewer;

	/*
	 * @see IContentProvider#dispose()
	 */
	public void dispose() {}

	/*
	 * @see IContentProvider#inputChanged(Viewer, Object, Object)
	 */
	/**
	* Notifies this content provider that the given viewer's input
	* has been switched to a different element.
	* <p>
	* A typical use for this method is registering the content provider as a listener
	* to changes on the new input (using model-specific means), and deregistering the viewer 
	* from the old input. In response to these change notifications, the content provider
	* propagates the changes to the viewer.
	* </p>
	*
	* @param viewer the viewer
	* @param oldInput the old input element, or <code>null</code> if the viewer
	*   did not previously have an input
	* @param newInput the new input element, or <code>null</code> if the viewer
	*   does not have an input
	*/
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = (TreeViewer)viewer;
		if(oldInput != null) {
			removeListenerFrom((UIContainer)oldInput);
		}
		if(newInput != null) {
			addListenerTo((UIContainer)newInput);
		}
	}
	
	/** Because the domain model does not have a richer
	 * listener model, recursively remove this listener
	 * from each child box of the given box. */
	protected void removeListenerFrom(UIContainer container) {
		container.removeListener(this);
		for (Iterator<UIComponent> iterator = container.getChildren().iterator(); iterator.hasNext();) {
			Model m=iterator.next();
			if (m instanceof UIContainer) {
				UIContainer aBox = (UIContainer) m;
				removeListenerFrom(aBox);
			}
		}
	}
	
	/** Because the domain model does not have a richer
	 * listener model, recursively add this listener
	 * to each child box of the given box. */
	protected void addListenerTo(UIContainer container) {
		container.addListener(this);
		for (Iterator iterator = container.getChildren().iterator(); iterator.hasNext();) {
			Object o=iterator.next();
			if (o instanceof UIContainer) {
				UIContainer childContainer = (UIContainer)o;
				addListenerTo(childContainer);
			}
		}
	}
	
	/*
	 * @see ITreeContentProvider#getChildren(Object)
	 */
	public Object[] getChildren(Object parentElement) {
		if(parentElement instanceof UIContainer) {
			UIContainer parent = (UIContainer)parentElement;
			return parent.getChildren().toArray();
		}
		return EMPTY_ARRAY;
	}
	
	/*
	 * @see ITreeContentProvider#getParent(Object)
	 */
	public Object getParent(Object element) {
		if(element instanceof Model) {
			return ((Model)element).getParent();
		}
		return null;
	}

	/*
	 * @see ITreeContentProvider#hasChildren(Object)
	 */
	public boolean hasChildren(Object element) {
		return getChildren(element).length > 0;
	}

	/*
	 * @see IStructuredContentProvider#getElements(Object)
	 */
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	/*
	 * @see IDeltaListener#add(DeltaEvent)
	 */
	public void add(DeltaEvent event) {
		Object movingBox = ((Model)event.receiver()).getParent();
		viewer.refresh(movingBox, false);
	}

	/*
	 * @see IDeltaListener#remove(DeltaEvent)
	 */
	public void remove(DeltaEvent event) {
		add(event);
	}

}
