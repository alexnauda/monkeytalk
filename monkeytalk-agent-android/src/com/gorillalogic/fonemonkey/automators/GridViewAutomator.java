package com.gorillalogic.fonemonkey.automators;

import android.widget.GridView;

import com.gorillalogic.fonemonkey.Log;
import com.gorillalogic.monkeytalk.automators.AutomatorConstants;

public class GridViewAutomator extends AdapterViewAutomator {

	private static String componentType = AutomatorConstants.TYPE_GRID;
	private static Class<?> componentClass = GridView.class;

	static {
		Log.log("Initializing GridViewAutomator");
	}

	@Override
	public Class<?> getComponentClass() {
		return componentClass;
	}

	@Override
	public String getComponentType() {
		return componentType;
	}

	private GridView getGridView() {
		return (GridView) getComponent();
	}

	@Override
	public String play(String action, String... args) {
		// GridView supports all actions implemented by AdapterViewAutomator.
		return super.play(action, args);
	}

	/**
	 * Scroll to the given position (0-based)
	 * 
	 * @see ListVieAutomator.scrollToPosition
	 * */
	/** Use smooth scrolling to make things look cool. */
	// Can't be place in AdapterViewAutomator because smoothScrollToPosition is implemented at a
	// lower level than the parent class.
	@Override
	protected void scrollToPosition(final int position) {

		AutomationManager.runOnUIThread(new Runnable() {
			public void run() {
				// Using setSelection instead of smoothScrollToPosition (like in ListViewAutomator)
				// because in some applications sSTP() was not properly scrolling all the way down.
				// setSelection appears to be working properly and scroll okay.
				getGridView().setSelection(position);
			}
		});

		// loop while we wait for the scroll to complete
		for (int j = 0; j < 100; j++) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException ex) {
				// ignore
			}

			if (position >= getGridView().getFirstVisiblePosition()
					&& position <= getGridView().getLastVisiblePosition()) {

				// sleep some extra time to guarantee we are fully scrolled
				try {
					Thread.sleep(500);
				} catch (InterruptedException ex) {
					// ignore
				}
				break;
			}
		}
	}

}
