package com.gorillalogic.monkeyconsole.componentview.ui;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import com.gorillalogic.monkeyconsole.componentview.model.UIComponent;
import com.gorillalogic.monkeyconsole.componentview.model.UIContainer;
import com.gorillalogic.monkeyconsole.editors.utils.MonkeyTalkImagesEnum;


public class UIContainerLabelProvider extends LabelProvider {	
	private Map imageCache = new HashMap(11);
	
	/*
	 * @see ILabelProvider#getImage(Object)
	 */
	public Image getImage(Object element) {
		ImageDescriptor descriptor = null;
		if (element instanceof UIContainer) {
			descriptor = MonkeyTalkImagesEnum.MOVINGBOX.image;
		} else if (element instanceof UIComponent) {
			descriptor = MonkeyTalkImagesEnum.BOOK.image;
		} else {
			throw unknownElement(element);
		}

		//obtain the cached image corresponding to the descriptor
		Image image = (Image)imageCache.get(descriptor);
		if (image == null) {
			image = descriptor.createImage();
			imageCache.put(descriptor, image);
		}
		return image;
	}

	/*
	 * @see ILabelProvider#getText(Object)
	 */
	public String getText(Object element) {
		String result;
		if (element instanceof UIComponent) {
			UIComponent component = ((UIComponent)element);
			if (component.getLabelString() == null) {
				result="root";
			} else {
				result=component.getLabelString();
			}
		}   else {
			throw unknownElement(element);
		}
		return result;
	}

	public void dispose() {
		for (Iterator i = imageCache.values().iterator(); i.hasNext();) {
			((Image) i.next()).dispose();
		}
		imageCache.clear();
	}

	protected RuntimeException unknownElement(Object element) {
		return new RuntimeException("Unknown type of element in tree of type " + element.getClass().getName());
	}

}
