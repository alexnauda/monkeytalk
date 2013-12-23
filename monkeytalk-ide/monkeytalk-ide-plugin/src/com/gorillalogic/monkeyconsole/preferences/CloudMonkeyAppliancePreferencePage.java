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
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.gorillalogic.monkeyconsole.editors.utils.CloudServiceException;
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
public class CloudMonkeyAppliancePreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	private BooleanFieldEditor useProxy;
	private BooleanFieldEditor useProxyAuthentication;
	private RadioGroupFieldEditor protocolEditor;

	private Text cloudUsername;
	private Text cloudPassword;

	private Text controllerHost;
	private Text controllerPort;
	private Text controllerSslPort;

	private Text proxyHost;
	private Text proxyPort;
	private Text proxyUsername;
	private Text proxyPassword;

	public CloudMonkeyAppliancePreferencePage() {
		super(GRID);
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common GUI blocks needed to
	 * manipulate various types of preferences. Each field editor knows how to save and restore
	 * itself.
	 */
	public void createFieldEditors() {

		// CLOUD PREFERENCES

		// / CLOUD CONTROLLER NETWORK PREFS
		Label controllerHostLabel = new Label(this.getFieldEditorParent(), SWT.NONE);
		controllerHostLabel.setText("CloudMonkey Appliance Hostname:");
		controllerHostLabel.setToolTipText("The hostname or IP address for the cloud server.");

		GridData gdControllerHost = new GridData(GridData.VERTICAL_ALIGN_END);
		gdControllerHost.horizontalSpan = 3;
		controllerHostLabel.setLayoutData(gdControllerHost);

		controllerHost = new Text(this.getFieldEditorParent(), SWT.BORDER);

		gdControllerHost = new GridData(300, 15);
		controllerHost.setLayoutData(gdControllerHost);
		controllerHost.setText(getPreferenceStore()
				.getString(PreferenceConstants.P_CONTROLLER_HOST));
		controllerHost.addVerifyListener(new DomainNameVerifyListener());

		Label controllerPortLabel = new Label(this.getFieldEditorParent(), SWT.NONE);
		controllerPortLabel.setText("CloudMonkey Appliance Port:");
		controllerPortLabel
				.setToolTipText("The port on which the cloud server will listen for normal HTTP connections. Defaults to 8080.");
		controllerPort = new Text(this.getFieldEditorParent(), SWT.BORDER);
		GridData gdControllerPort = new GridData(100, 15);
		controllerPort.setLayoutData(gdControllerPort);
		controllerPort.setText(getPreferenceStore()
				.getString(PreferenceConstants.P_CONTROLLER_PORT));
		controllerPort.addVerifyListener(new NetworkPortVerifyListener());

		Label controllerSSlPortLabel = new Label(this.getFieldEditorParent(), SWT.NONE);
		controllerSSlPortLabel.setText("CloudMonkey Appliance SSL Port:");
		controllerSSlPortLabel
				.setToolTipText("The port on which the cloud server will listen for secure (encrypted) HTTP connections. Defaults to 4430.");
		controllerSslPort = new Text(this.getFieldEditorParent(), SWT.BORDER);
		GridData gdControllerSslPort = new GridData(100, 15);
		controllerSslPort.setLayoutData(gdControllerSslPort);
		controllerSslPort.setText(getPreferenceStore().getString(
				PreferenceConstants.P_CONTROLLER_SSL_PORT));
		controllerSslPort.addVerifyListener(new NetworkPortVerifyListener());

		// Radio Buttons to select protocol
		protocolEditor = new RadioGroupFieldEditor(PreferenceConstants.P_CONTROLLER_PROTOCOL,
				"Protocol", 1, new String[][] { { "HTTP", "http" }, { "HTTPS", "https" } },
				this.getFieldEditorParent());
		addField(protocolEditor);

		// END CLOUD CONTROLLER NETWORK PREFS

		// CLOUD LOGIN INFO

		Label cloudAuthenticationLabel = new Label(this.getFieldEditorParent(), SWT.NONE);
		cloudAuthenticationLabel
				.setText("To access the CloudMonkey Appliance, please enter your username and password.");

		GridData gdCloudAuthentication = new GridData(GridData.VERTICAL_ALIGN_END);
		gdCloudAuthentication.horizontalSpan = 2;
		cloudAuthenticationLabel.setLayoutData(gdCloudAuthentication);

		Label cloudUsernameLabel = new Label(this.getFieldEditorParent(), SWT.NONE);
		cloudUsernameLabel.setText("Username:");
		cloudUsername = new Text(this.getFieldEditorParent(), SWT.BORDER);

		GridData gdCloudUsername = new GridData(200, 15);
		cloudUsername.setLayoutData(gdCloudUsername);
		cloudUsername.setText(getPreferenceStore().getString(PreferenceConstants.P_CLOUDUSR));

		Label cloudPasswordLabel = new Label(this.getFieldEditorParent(), SWT.NONE);
		cloudPasswordLabel.setText("Password:");
		cloudPassword = new Text(this.getFieldEditorParent(), SWT.BORDER | SWT.PASSWORD);

		GridData gdCloudPassword = new GridData(200, 15);
		cloudPassword.setLayoutData(gdCloudPassword);
		cloudPassword.setText(getPreferenceStore().getString(PreferenceConstants.P_CLOUDPASS));

		Button verifyLogin = new Button(this.getFieldEditorParent(), SWT.NONE);
		verifyLogin.setText("Verify Login");
		verifyLogin.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				try {
					// Apply the settings first, otherwise a rewrite of the networking code is
					// needed.
					performApply();

					setErrorMessage(null);
					setMessage("sending login request");

					String token = CloudServices.login(cloudUsername.getText(),
							cloudPassword.getText());
					if (token == null || token.length() == 0) {
						throw new CloudServiceException(
								"Invalid Connection prefs - hostname, ports, or proxy");
					}
					setErrorMessage(null);
					setMessage("Username and password verified");

				} catch (CloudServiceException ex) {
					String errorMessage = ex.getMessage();
					if (errorMessage == null || errorMessage.length() == 0) {
						errorMessage = "The login information was not valid";
					}
					setErrorMessage(errorMessage);
					setMessage("CloudMonkey Appliance Preferences");
				}

			}
		});

		GridData gdVerifyLogin = new GridData(GridData.VERTICAL_ALIGN_END);
		gdVerifyLogin.horizontalSpan = 2;
		verifyLogin.setLayoutData(gdVerifyLogin);

		// END CLOUD LOGIN INFO

		// PROXY NETWORK PREFS

		Group proxyPreferencesGroup = new Group(this.getFieldEditorParent(), SWT.NONE);
		GridLayout cloudPreferencesLayout = new GridLayout();
		cloudPreferencesLayout.numColumns = 2;
		this.getFieldEditorParent().setLayout(cloudPreferencesLayout);

		// Check box for Proxy Usage
		useProxy = new BooleanFieldEditor(PreferenceConstants.P_USE_PROXY, "Use Proxy",
				proxyPreferencesGroup);
		addField(useProxy);

		// Proxy Host
		Label proxyHostLabel = new Label(proxyPreferencesGroup, SWT.NONE);
		proxyHostLabel.setText("Proxy Hostname:");
		proxyHostLabel.setToolTipText("The hostname or IP address for the proxy server.");

		proxyHost = new Text(proxyPreferencesGroup, SWT.BORDER);
		proxyHost.setText(getPreferenceStore().getString(PreferenceConstants.P_PROXY_HOST));
		proxyHost.addVerifyListener(new DomainNameVerifyListener());

		GridData gdProxyHost = new GridData(300, 15);
		proxyHost.setLayoutData(gdProxyHost);

		// Proxy Port
		Label proxyPortLabel = new Label(proxyPreferencesGroup, SWT.NONE);
		proxyPortLabel.setText("Proxy Port:");
		proxyPortLabel.setToolTipText("Proxy port number");

		proxyPort = new Text(proxyPreferencesGroup, SWT.BORDER);
		proxyPort.setText(getPreferenceStore().getString(PreferenceConstants.P_PROXY_PORT));
		proxyPort.addVerifyListener(new NetworkPortVerifyListener());

		GridData gdProxyPort = new GridData(100, 15);
		proxyPort.setLayoutData(gdProxyPort);

		// Checkbox for Proxy Authentication
		useProxyAuthentication = new BooleanFieldEditor(
				PreferenceConstants.P_USE_PROXY_AUTHENTICATION, "Proxy server requires password",
				proxyPreferencesGroup);
		addField(useProxyAuthentication);

		// Proxy Username
		Label proxyUsernameLabel = new Label(proxyPreferencesGroup, SWT.NONE);
		proxyUsernameLabel.setText("Proxy Username:");
		proxyUsernameLabel.setToolTipText("The username for the authenticated proxy server.");

		proxyUsername = new Text(proxyPreferencesGroup, SWT.BORDER);
		proxyUsername.setText(getPreferenceStore().getString(PreferenceConstants.P_PROXY_USERNAME));

		GridData gdProxyUsername = new GridData(300, 15);
		proxyUsername.setLayoutData(gdProxyUsername);

		// Proxy Password
		Label proxyPasswordLabel = new Label(proxyPreferencesGroup, SWT.NONE);
		proxyPasswordLabel.setText("Proxy Password:");
		proxyPasswordLabel.setToolTipText("The password for the authenticated proxy server.");

		proxyPassword = new Text(proxyPreferencesGroup, SWT.BORDER | SWT.PASSWORD);
		proxyPassword.setText(getPreferenceStore().getString(PreferenceConstants.P_PROXY_PASSWORD));

		GridData gdProxyPassword = new GridData(200, 15);
		proxyPassword.setLayoutData(gdProxyPassword);

		// END PROXY NETWORK PREFS

		checkState();

	}

	@Override
	protected void checkState() {
		setErrorMessage(null);
		setValid(true);

		proxyPort.setEnabled(useProxy.getBooleanValue());
		proxyHost.setEnabled(useProxy.getBooleanValue());

		proxyUsername.setEnabled(useProxy.getBooleanValue()
				&& useProxyAuthentication.getBooleanValue());
		proxyPassword.setEnabled(useProxy.getBooleanValue()
				&& useProxyAuthentication.getBooleanValue());

		super.checkState();
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

	public void applySettings() {
		getPreferenceStore().setValue(PreferenceConstants.P_CLOUDUSR, cloudUsername.getText());
		getPreferenceStore().setValue(PreferenceConstants.P_CLOUDPASS, cloudPassword.getText());

		getPreferenceStore().setValue(PreferenceConstants.P_CONTROLLER_HOST,
				controllerHost.getText());

		setNetworkPortFromText(controllerPort, PreferenceConstants.P_CONTROLLER_PORT);
		setNetworkPortFromText(controllerSslPort, PreferenceConstants.P_CONTROLLER_SSL_PORT);

		getPreferenceStore().setValue(PreferenceConstants.P_PROXY_HOST, proxyHost.getText());

		setNetworkPortFromText(proxyPort, PreferenceConstants.P_PROXY_PORT);

		getPreferenceStore()
				.setValue(PreferenceConstants.P_PROXY_USERNAME, proxyUsername.getText());
		getPreferenceStore()
				.setValue(PreferenceConstants.P_PROXY_PASSWORD, proxyPassword.getText());
	}
	
	@Override
	public void performApply() {
		applySettings();
		super.performApply();
	}

	@Override
	public boolean performOk() {
		applySettings();
		return super.performOk();
	}

	// Shortcut for setIntegerPrefFromText. Cleans up the performOk function.
	private void setNetworkPortFromText(Text control, String propName) {
		setIntegerPrefFromText(control, propName, 1, 65535);
	}

	// Validates an Integer from a string. Min and Max used for validation of network port.
	private void setIntegerPrefFromText(Text control, String propName, int min, int max) {
		int port = -1;
		try {
			String s = control.getText();
			if (s != null) {
				port = Integer.parseInt(s);
			}
		} catch (NumberFormatException nfe) {
			port = -1;
		}
		if (port >= min && port <= max) {
			getPreferenceStore().setValue(propName, port);
		}
	}

	private static class CharFilterVerifyListener implements VerifyListener {
		int minlength;
		int maxlength;
		String validchars;

		CharFilterVerifyListener(String validchars, int minlength, int maxlength) {
			this.validchars = validchars;
			this.maxlength = maxlength;
			this.minlength = minlength;
		}

		@Override
		public void verifyText(VerifyEvent evt) {
			// String txt=((Text)evt . getSource()).getText();
			String txt = evt.text;
			if (txt == null) {
				evt.doit = false;
				return;
			}
			if (txt.length() < minlength) {
				evt.doit = false;
				return;
			}
			if (txt.length() > maxlength) {
				evt.doit = false;
				return;
			}
			for (int i = 0; i < txt.length(); i++) {
				if (validchars.indexOf(txt.charAt(i)) == -1) {
					evt.doit = false;
					break;
				}
			}
		}
	}

	private static class DomainNameVerifyListener extends CharFilterVerifyListener {
		DomainNameVerifyListener() {
			super("01234567890-.abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ", 0, 63);
		}
	}

	private static class NetworkPortVerifyListener extends CharFilterVerifyListener {
		NetworkPortVerifyListener() {
			super("01234567890", 0, 5);
		}

		@Override
		public void verifyText(VerifyEvent evt) {
			super.verifyText(evt);
			if (!evt.doit) {
				return;
			}
			Text control = (Text) evt.getSource();
			String oldtxt = control.getText();
			String newtxt = oldtxt.substring(0, evt.start) + evt.text + oldtxt.substring(evt.end);
			if (newtxt.length() == 0) {
				return; // ok for now
			}
			int val = -1;
			try {
				val = Integer.parseInt(newtxt);
			} catch (NumberFormatException nfe) {
				evt.doit = false;
				return;
			}
			if (val < 1 || val > 65535) {
				evt.doit = false;
				return;
			}
		}
	}
}