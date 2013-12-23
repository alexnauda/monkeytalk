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

import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.SingleLineTransformationMethod;
import android.widget.EditText;

import com.gorillalogic.fonemonkey.Log;
import com.gorillalogic.monkeytalk.automators.AutomatorConstants;

/**
 * Automator for EditText component.
 */
public class EditTextAutomator extends TextViewAutomator implements TextWatcher {

	private static Class<?> componentClass = EditText.class;
	static {
		Log.log("Initializing EditTextAutomator");
	}

	@Override
	public String getMonkeyID() {
		if (getTextView() != null && getTextView().getHint() != null
				&& getTextView().getHint().toString().trim().length() > 0) {
			return getTextView().getHint().toString().trim();
		}
		return super.getMonkeyID();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gorillalogic.fonemonkey.automators.ViewAutomator#installDefaultListeners()
	 */
	@Override
	public boolean installDefaultListeners() {
		getEditText().addTextChangedListener(this);
		return super.installDefaultListeners();
	}

	@Override
	public String getComponentType() {
		if (getTextView() == null) {
			return "Input";
		} else if (getTextView().getTransformationMethod() instanceof SingleLineTransformationMethod) {
			return "Input";
		} else if ((getTextView().getInputType() & InputType.TYPE_TEXT_FLAG_MULTI_LINE) != InputType.TYPE_TEXT_FLAG_MULTI_LINE
				&& (getTextView().getInputType() & InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE) != InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE) {
			return "Input";
		} else {
			return "TextArea";
		}
	}

	@Override
	public Class<?> getComponentClass() {
		return componentClass;
	}

	public EditText getEditText() {
		return (EditText) getComponent();
	}

	private static String[] aliases = { "TextArea", "Input" };

	@Override
	public String[] getAliases() {
		return aliases;
	}

	@Override
	public void afterTextChanged(Editable arg0) {
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		// if (getTextView().getParent().getClass() != WebView.class) {
		AutomationManager.record(AutomatorConstants.ACTION_ENTER_TEXT, getView(), s.toString());
		// }
	}
}