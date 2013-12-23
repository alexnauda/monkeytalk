package com.gorillalogic.monkeyconsole.editors.utils.devicematrixtable.tree;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import com.gorillalogic.monkeyconsole.editors.utils.MonkeyTalkImagesEnum;

public class DeviceMatrixTreeViewLableProvider extends LabelProvider {
	@Override
	public String getText(Object element) {
		if (element instanceof AndroidVersion) {
			return ((AndroidVersion) element).getName();
		}
		return ((Resolution) element).getSummary();
	}

	@Override
	public Image getImage(Object element) {
		if (element instanceof AndroidVersion) {
			if (((AndroidVersion) element).getName().contains("OS")) {
				return MonkeyTalkImagesEnum.CONNECTIOSEMMULATOR.image.createImage();
			} else {
				return MonkeyTalkImagesEnum.CONNECTANDROIDEMULATOR.image.createImage();
			}
		}
		return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);
	}

}
