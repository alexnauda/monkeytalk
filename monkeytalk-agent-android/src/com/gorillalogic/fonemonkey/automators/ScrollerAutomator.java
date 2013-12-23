package com.gorillalogic.fonemonkey.automators;

import android.widget.ScrollView;

import com.gorillalogic.fonemonkey.Log;
import com.gorillalogic.monkeytalk.automators.AutomatorConstants;

public class ScrollerAutomator extends ViewAutomator {

	static String componentType = AutomatorConstants.TYPE_SCROLLER;

	static {
		Log.log("Initializing ScrollerAutomator");
	}

	@Override
	public String getComponentType() {
		return AutomatorConstants.TYPE_SCROLLER;
	}

	@Override
	public Class<?> getComponentClass() {
		return ScrollView.class;
	}

	public ScrollView getScrollView() {
		return (ScrollView) getComponent();
	}

	@Override
	public String play(final String action, final String... args) {

		if (action.equalsIgnoreCase(AutomatorConstants.ACTION_SCROLL)) {
			if (args.length < 2) {
				throw new IllegalArgumentException(AutomatorConstants.ACTION_SCROLL
						+ " requires an X and Y value");
			}
			scroll(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
			return null;
		}

		return super.play(action, args);
	}

}
