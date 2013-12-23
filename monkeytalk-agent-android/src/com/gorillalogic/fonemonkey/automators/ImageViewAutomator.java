package com.gorillalogic.fonemonkey.automators;

import android.widget.ImageView;

import com.gorillalogic.fonemonkey.Log;
import com.gorillalogic.monkeytalk.automators.AutomatorConstants;

public class ImageViewAutomator extends ViewAutomator {
	private static Class<?> componentClass = ImageView.class;
	static {
		Log.log("Initializing ImageViewAutomator");
	}

	@Override
	public String getComponentType() {
		return AutomatorConstants.TYPE_IMAGE;
	}

	@Override
	public Class<?> getComponentClass() {
		return componentClass;
	}
}
