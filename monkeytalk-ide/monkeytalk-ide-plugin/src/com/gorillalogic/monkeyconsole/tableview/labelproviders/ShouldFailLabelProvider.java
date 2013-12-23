package com.gorillalogic.monkeyconsole.tableview.labelproviders;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.gorillalogic.monkeyconsole.tableview.TableRow;

public class ShouldFailLabelProvider extends ColumnLabelProvider {
	private static final String CHECKED_KEY = "CHECKED";
	private static final String UNCHECK_KEY = "UNCHECKED";

	public ShouldFailLabelProvider(ColumnViewer viewer) {
		if (JFaceResources.getImageRegistry().getDescriptor(CHECKED_KEY) == null) {
			JFaceResources.getImageRegistry()
					.put(UNCHECK_KEY, makeShot(viewer.getControl(), false));
			JFaceResources.getImageRegistry().put(CHECKED_KEY, makeShot(viewer.getControl(), true));
		}
	}

	private Image makeShot(Control control, boolean type) {

		// Hopefully no platform uses exactly this color because we’ll make
		// it transparent in the image.

		Color greenScreen = new Color(control.getDisplay(), 222, 223, 224);

		Shell shell = new Shell(control.getShell(), SWT.NO_TRIM);
		// otherwise we have a default gray color
		shell.setBackground(greenScreen);
		// shell.setLayoutData(control.getLayoutData());
		Button button = new Button(shell, SWT.CHECK);
		button.setBackground(greenScreen);
		button.setSelection(type);
		// button.setLayoutData(control.getLayoutData());
		// otherwise an image is located in a corner
		button.setLocation(1, 1);
		Point bsize = button.computeSize(SWT.DEFAULT, SWT.DEFAULT);

		// otherwise an image is stretched by width
		bsize.x = Math.max(bsize.x - 1, bsize.y - 1);
		bsize.y = Math.max(bsize.x - 1, bsize.y - 1);
		button.setSize(bsize);
		shell.setSize(bsize);

		shell.open();
		GC gc = new GC(shell);
		Image image = new Image(control.getDisplay(), bsize.x, bsize.y);
		gc.copyArea(image, 0, 0);
		gc.dispose();
		shell.close();

		ImageData imageData = image.getImageData();
		imageData.transparentPixel = imageData.palette.getPixel(greenScreen.getRGB());

		return new Image(control.getDisplay(), imageData);

	}

	public Image getImage(Object element) {
		if (isChecked(element)) {
			return JFaceResources.getImageRegistry().getDescriptor(CHECKED_KEY).createImage();
		} else {
			return JFaceResources.getImageRegistry().getDescriptor(UNCHECK_KEY).createImage();
		}
	}

	@Override
	public String getText(Object element) {
		return "";
	}

	protected boolean isChecked(Object element) {
		return ((TableRow) element).shouldFail();
	}
}
