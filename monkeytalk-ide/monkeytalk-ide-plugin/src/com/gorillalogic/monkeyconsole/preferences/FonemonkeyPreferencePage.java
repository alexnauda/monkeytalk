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
package com.gorillalogic.monkeyconsole.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.gorillalogic.monkeyconsole.ADBHelper;
import com.gorillalogic.monkeyconsole.editors.utils.CloudServices;
import com.gorillalogic.monkeyconsole.plugin.FoneMonkeyPlugin;

/**
 * <p>
 * This class represents a preference page that is contributed to the Preferences dialog. By
 * subclassing <code>FieldEditorPreferencePage</code>, we can use the field support built into JFace
 * that allows us to create a page that is small and knows how to save, restore and apply itself.
 * </p>
 * <p>
 * This page is used to modify preferences only. They are stored in the preference store that
 * belongs to the main plug-in class. That way, preferences can be accessed directly via the
 * preference store.
 * </p>
 */
public class FonemonkeyPreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	DirectoryFieldEditor androidSdkEditor;

	BooleanFieldEditor includeAndroidEditor;
	BooleanFieldEditor logEventConsent;
	BooleanFieldEditor takeAfterScreenshots;

	boolean wasOptIn = true;

	public FonemonkeyPreferencePage() {
		super(GRID);
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common GUI blocks needed to
	 * manipulate various types of preferences. Each field editor knows how to save and restore
	 * itself.
	 */
	public void createFieldEditors() {

		// GENERAL PREFERENCES

		// Test on iOS
		addField(new BooleanFieldEditor(PreferenceConstants.P_INCLUDEIOS, "Test on iOS",
				getFieldEditorParent()));

		// Test on Android
		// Enables or disables the directory search for the Android SDK
		includeAndroidEditor = new BooleanFieldEditor(PreferenceConstants.P_INCLUDEANDROID,
				"Test on Android", getFieldEditorParent());
		addField(includeAndroidEditor);

		// Directory field for Android SDK Location
		androidSdkEditor = new DirectoryFieldEditor(PreferenceConstants.P_ANDROIDHOME,
				"Android SDK:", this.getFieldEditorParent());
		addField(androidSdkEditor);

		// Thinktime and Timeout Defaults
		addField(new IntegerFieldEditor(PreferenceConstants.P_THINKTIME, "Default Thinktime:",
				getFieldEditorParent()));
		addField(new IntegerFieldEditor(PreferenceConstants.P_DEFAULTTIMEOUT, "Default Timeout:",
				getFieldEditorParent()));

		// Take screenshots after each command
		takeAfterScreenshots = new BooleanFieldEditor(PreferenceConstants.P_TAKEAFTERSCREENSHOTS,
				"Take screenshot after each command", getFieldEditorParent());
		takeAfterScreenshots.getDescriptionControl(getFieldEditorParent()).setToolTipText(
				"Take a screenshot after every command is executed.");
		addField(takeAfterScreenshots);

		// END GENERAL PREFERENCES

		// LOGGING PREFERENCES

		if (FoneMonkeyPlugin.getDefault().getPreferenceStore()
				.contains(PreferenceConstants.P_LOGEVENTCONSENT)) {
			wasOptIn = FoneMonkeyPlugin.getDefault().getPreferenceStore()
					.getBoolean(PreferenceConstants.P_LOGEVENTCONSENT);
		}
		logEventConsent = new BooleanFieldEditor(PreferenceConstants.P_LOGEVENTCONSENT,
				"Upload Usage Information", getFieldEditorParent());
		logEventConsent
				.getDescriptionControl(getFieldEditorParent())
				.setToolTipText(
						"Can we send some information to the MonkeyTalk dev team about which features are being used and whether they are working?");
		addField(logEventConsent);

		// END LOGGING PREFERENCES

		checkState();

	}

	@Override
	protected void checkState() {
		setErrorMessage(null);
		setValid(true);

		super.checkState();

		androidSdkEditor.setEnabled(includeAndroidEditor.getBooleanValue(), getFieldEditorParent());

		if (includeAndroidEditor.getBooleanValue()) {
			String msg = ADBHelper.validateAndroidSdkPath(androidSdkEditor.getStringValue());
			setErrorMessage(msg);
			setValid(msg == null);
		} else {
			setErrorMessage(null);
			setValid(true);
		}
	}

	@Override
	protected void performDefaults() {
		super.performDefaults();
		checkState();
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		super.propertyChange(event);

		if (event.getProperty().equals(FieldEditor.VALUE)) {
			checkState();
		}
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(FoneMonkeyPlugin.getDefault().getPreferenceStore());
	}

	@Override
	public boolean performOk() {

		if (wasOptIn) {
			if (this.logEventConsent.getBooleanValue() == false) {
				CloudServices.optOutAsync();
			}
		} else {
			if (this.logEventConsent.getBooleanValue() == true) {
				CloudServices.optInAsync();
			}
		}
		return super.performOk();
	}

}