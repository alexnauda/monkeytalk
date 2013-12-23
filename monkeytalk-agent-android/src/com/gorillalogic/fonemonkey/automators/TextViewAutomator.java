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

import java.lang.reflect.Field;
import java.util.List;

import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.gorillalogic.fonemonkey.Log;
import com.gorillalogic.monkeytalk.automators.AutomatorConstants;

public class TextViewAutomator extends ViewAutomator {
	private static String componentType = "Label";
	private static Class<?> componentClass = TextView.class;
	static {
		Log.log("Initializing TextViewAutomator");
	}

	private static boolean ignoreNext = false;

	// @Override
	// public String getMonkeyID() {
	// String text = getTextView().getText().toString();
	// return (text != null && text.trim().length() > 0) ?
	// text : super.getMonkeyID();
	// }

	@Override
	public String getComponentType() {
		// if (getTextView() == null ||
		// !(getTextView().getTransformationMethod() instanceof
		// SingleLineTransformationMethod)) {
		// return "TextArea";
		// }
		// if (getTextView().getInputType() == InputType.TYPE_NULL) {
		// return "Label";
		// }
		// return "Input";
		return componentType;
	}

	@Override
	public Class<?> getComponentClass() {
		return componentClass;
	}

	public TextView getTextView() {
		return (TextView) getComponent();
	}

	@Override
	public String play(String action, String... args) {
		if (action.equalsIgnoreCase(AutomatorConstants.ACTION_ENTER_TEXT)) {
			if (args.length < 1) {
				throw new IllegalArgumentException("EnterText action requires one argument");
			}

			final String text = args[0];

			boolean hitDone = false;
			if (args.length > 1 && (Boolean.valueOf(args[1]) || args[1].equalsIgnoreCase("enter"))) {
				hitDone = true;
			}
			final boolean hitEnter = hitDone;
			AutomationManager.runOnUIThread(new Runnable() {
				public void run() {
					getTextView().setText("");
					tap();
					enterText(text, hitEnter);
				}
			});

			return null;
		}

		if (action.equalsIgnoreCase(AutomatorConstants.ACTION_CLEAR)) {

			AutomationManager.runOnUIThread(new Runnable() {
				public void run() {
					getTextView().setText("");
				}
			});

			return null;
		}

		return super.play(action, args);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gorillalogic.fonemonkey.automators.AutomatorBase#record(java.lang .String,
	 * java.lang.String[])
	 */
	@Override
	public void record(String action, String... args) {
		if (action.equalsIgnoreCase("ignoreNext")) {
			ignoreNext = true;
			return;
		}
		if (ignoreNext) {
			ignoreNext = false;
			return;
		}
		super.record(action, args);
	}

	// public String[] getAliases() {
	// return new String[] {"Label"};
	// }

	public String getValue() {
		return getTextView().getText().toString();
	}

	// @Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		AutomationManager
				.record(AutomatorConstants.ACTION_ENTER_TEXT, (String) v.getText(), "true");
		return false;
	}

	@Override
	public boolean installDefaultListeners() {
		OnEditorActionListener listener = null;
		Field inputContentTypeField;
		Object target = getTextView();
		Class<?> targetClass = TextView.class;
		try {

			try {
				// mInputContentType moved to inner class in Android 4.1 (JellyBean)
				Field editorField = TextView.class.getDeclaredField("mEditor");
				editorField.setAccessible(true);
				Object editor = editorField.get(getTextView());
				if (editor != null) {
					targetClass = editor.getClass();
					target = editor;
				} else {
					target = null;
				}
			} catch (NoSuchFieldException e) {
				// Pre-4.1 JellyBean
			}

			if (target != null) {
				inputContentTypeField = targetClass.getDeclaredField("mInputContentType");
				inputContentTypeField.setAccessible(true);
				Object inputContentType = inputContentTypeField.get(target);
				if (inputContentType != null) {
					Field editorActionListenerField = inputContentType.getClass().getDeclaredField(
							"onEditorActionListener");
					editorActionListenerField.setAccessible(true);
					listener = (OnEditorActionListener) editorActionListenerField
							.get(inputContentType);
				}
			}
		} catch (Exception e) {
			throw new IllegalStateException("Error chaining onEditorActionListener", e);
		}
		final OnEditorActionListener origListener = listener;
		getTextView().setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				AutomationManager.record(AutomatorConstants.ACTION_ENTER_TEXT, getView(), v
						.getText().toString(), "enter");
				if (origListener == null) {
					return false;
				}
				return origListener.onEditorAction(v, actionId, event);

			}
		});

		return super.installDefaultListeners();

	}

	@Override
	public List<String> getIdentifyingValues() {
		List<String> list = super.getIdentifyingValues();
		CharSequence text = getTextView().getText();
		if (text != null) {
			list.add(text.toString());
		}
		return list;

	}

}