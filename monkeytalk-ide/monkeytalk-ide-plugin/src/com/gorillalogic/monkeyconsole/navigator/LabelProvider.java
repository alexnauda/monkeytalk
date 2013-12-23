package com.gorillalogic.monkeyconsole.navigator;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

public class LabelProvider implements ILabelProvider {

	@Override
	public void addListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getText(Object element) {
		String text = "";
		if (CustomProjectParent.class.isInstance(element)) {
			text = ((CustomProjectParent)element).getProjectName();
		}
		return text;
	}

	public Image getImage(Object element) {
        System.out.println("LabelProvider.getImage: " + element.getClass().getName());
        Image image = null;

        if (CustomProjectParent.class.isInstance(element)) {
            image = ((CustomProjectParent)element).getImage();
        }
        // else ignore the element

        return image;
    }
}
