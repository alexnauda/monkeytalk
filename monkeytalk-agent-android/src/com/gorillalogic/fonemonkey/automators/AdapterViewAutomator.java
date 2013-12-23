/*  MonkeyTalk - a cross-platform functional testing tool
    Copyright (C) 2012 Gorilla Logic, Inc.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>. */
package com.gorillalogic.fonemonkey.automators;

import java.util.List;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

import com.gorillalogic.fonemonkey.Log;
import com.gorillalogic.monkeytalk.automators.AutomatorConstants;

/**
 * Base automator for ListView and GridView.
 */
public class AdapterViewAutomator extends ViewAutomator implements OnItemClickListener,
		OnItemSelectedListener, OnItemLongClickListener {

	static {
		Log.log("Initializing AdapterViewAutomator");
	}

	@Override
	public void record(String operation, String... args) {
		if (operation.equalsIgnoreCase(AutomatorConstants.ACTION_DRAG)
				|| operation.equalsIgnoreCase(AutomatorConstants.ACTION_SWIPE)
				|| operation.equalsIgnoreCase(AutomatorConstants.ACTION_TAP)) {
			// ignore Drag, Swipe, and Tap (which will be recorded as a Select)
			return;
		}
		super.record(operation, args);
	}

	protected AdapterView<?> getAdapterView() {
		return (AdapterView<?>) getComponent();
	}

	@Override
	public String play(String action, String... args) {
		if (action.equalsIgnoreCase(AutomatorConstants.ACTION_SELECT)
				|| action.equalsIgnoreCase(AutomatorConstants.ACTION_LONG_SELECT)) {
			assertArgCount(action, args, 1);

			int position = findPosition(args[0]);

			if (position != -1) {
				scrollToPositionAndSelect(position, true,
						action.equalsIgnoreCase(AutomatorConstants.ACTION_LONG_SELECT));
				return null;
			}

			throw new IllegalArgumentException("Unable to find " + getComponentType()
					+ " item with value " + args[0]);
		} else if (action.equalsIgnoreCase(AutomatorConstants.ACTION_SELECT_ROW)
				|| action.equalsIgnoreCase(AutomatorConstants.ACTION_SELECT_INDEX)
				|| action.equalsIgnoreCase(AutomatorConstants.ACTION_LONG_SELECT_INDEX)) {
			assertArgCount(action, args, 1);
			int row = getIndexArg(action, args[0]);

			int max = getAdapterView().getCount();
			assertMaxArg(row, max);

			scrollToPositionAndSelect(row - 1, true,
					action.equalsIgnoreCase(AutomatorConstants.ACTION_LONG_SELECT_INDEX));
			return null;
		}

		return super.play(action, args);
	}

	/**
	 * Scroll then tap.
	 * 
	 * @param position
	 *            0-based item index
	 * @param shouldTap
	 *            if true, then tap on the item
	 * @param isLong
	 *            if true, then tap should be a long press
	 */
	private void scrollToPositionAndSelect(int position, boolean shouldTap, boolean isLong) {
		scrollToPosition(position);

		if (shouldTap) {
			tapPosition(position, isLong);
		}
	}

	/** Scroll to the given position (0-based) */
	// Typically overwritten in subclass.
	protected void scrollToPosition(int position) {
		getAdapterView().setSelection(position);
	}

	/** Tap at the given position (0-based), and optionally make it a long press. */
	protected void tapPosition(int position, boolean isLong) {
		int i = position - getAdapterView().getFirstVisiblePosition();
		View v = getAdapterView().getChildAt(i);

		if (isLong) {
			// Long press (down, then up after 1025ms) on the AdapterView at...
			tap(v.getLeft() + v.getWidth() / 2, v.getTop() + v.getHeight() / 2, 1025);
		} else {
			// Tap on the AdapterView at...
			tap(v.getLeft() + v.getWidth() / 2, v.getTop() + v.getHeight() / 2);
		}
	}

	/**
	 * Find the position (0-based) for the given item.
	 * 
	 * @param value
	 *            the item value
	 * @return the position
	 */
	protected int findPosition(String value) {
		for (int i = 0; i < getAdapterView().getAdapter().getCount(); i++) {
			Object obj = getAdapterView().getItemAtPosition(i);
			if (value.equals((obj.toString()).trim())) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		recordSelection(position, false);
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		recordSelection(position, false);
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// do nothing
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		recordSelection(position, true);
		return false;
	}

	/**
	 * @param position
	 *            0-based index
	 */
	private void recordSelection(int position, boolean isLong) {
		Object obj = getAdapterView().getItemAtPosition(position);
		if (obj instanceof String) {
			record(isLong ? AutomatorConstants.ACTION_LONG_SELECT
					: AutomatorConstants.ACTION_SELECT, (String) obj);
		} else {
			record(isLong ? AutomatorConstants.ACTION_LONG_SELECT_INDEX
					: AutomatorConstants.ACTION_SELECT_INDEX, String.valueOf(position + 1));
		}
	}

	@Override
	public boolean hides(View v) {
		return false;
	}

	public String getValue() {
		Object obj = getAdapterView().getSelectedItem();
		if (obj instanceof String) {
			return obj.toString();
		}

		int pos = getAdapterView().getSelectedItemPosition();
		if (pos != AdapterView.INVALID_POSITION) {
			return String.valueOf(pos + 1);
		}
		return obj.toString();
	}

	@Override
	protected String getProperty(String propertyPath) {
		int size = getAdapterView().getCount();
		if (propertyPath.equals("size")) {
			return String.valueOf(size);
		}

		return super.getProperty(propertyPath);
	}

	@Override
	protected String getArrayItem(String name, List<Integer> indices) {
		if (name.equals("item")) {
			int position = indices.get(0);
			Object obj = getAdapterView().getItemAtPosition(position);
			return obj.toString();
		} else if (name.equals("detail")) {
			throw new IllegalStateException(getComponent() + " does not support 'detail' property");
		}
		return super.getArrayItem(name, indices);
	}
}