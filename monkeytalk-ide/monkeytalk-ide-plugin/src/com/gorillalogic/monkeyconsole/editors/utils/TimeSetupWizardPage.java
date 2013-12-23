package com.gorillalogic.monkeyconsole.editors.utils;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.gorillalogic.monkeyconsole.plugin.FoneMonkeyPlugin;
import com.gorillalogic.monkeyconsole.preferences.PreferenceConstants;

public class TimeSetupWizardPage extends WizardPage {

	Text thinktimeText;
	Text timeoutText;

	protected TimeSetupWizardPage(String pageName) {
		super(pageName);
		setTitle("Thinktime and Timeout Setup");
		setDescription("Please set you prefered thinktime and timeout values.");
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);
		setControl(composite);
		new Label(composite, SWT.NONE).setText("Thinktime");
		thinktimeText = new Text(composite, SWT.BORDER);
		new Label(composite, SWT.NONE).setText("Timeout");
		timeoutText = new Text(composite, SWT.BORDER);

		int thinktimeValue = FoneMonkeyPlugin.getDefault().getPreferenceStore()
				.getInt(PreferenceConstants.P_THINKTIME);
		int timeoutValue = FoneMonkeyPlugin.getDefault().getPreferenceStore()
				.getInt(PreferenceConstants.P_DEFAULTTIMEOUT);

		thinktimeText.setText("" + thinktimeValue);
		timeoutText.setText("" + timeoutValue);

		thinktimeText.setLayoutData(new GridData(50, 15));
		timeoutText.setLayoutData(new GridData(50, 15));

		System.out.println("think : " + thinktimeValue + ", out : " + timeoutValue);

		thinktimeText.addVerifyListener(new VerifyListener() {
			@Override
			public void verifyText(VerifyEvent e) {
				String currentText = ((Text) e.widget).getText();
				String number = currentText.substring(0, e.start) + e.text
						+ currentText.substring(e.end);
				try {
					int value = Integer.valueOf(number);
				} catch (NumberFormatException ex) {
					if (!number.equals(""))
						e.doit = false;
				}
			}
		});

		timeoutText.addVerifyListener(new VerifyListener() {
			@Override
			public void verifyText(VerifyEvent e) {
				String currentText = ((Text) e.widget).getText();
				String number = currentText.substring(0, e.start) + e.text
						+ currentText.substring(e.end);
				try {
					int value = Integer.valueOf(number);
				} catch (NumberFormatException ex) {
					if (!number.equals(""))
						e.doit = false;
				}
			}
		});
	}

}
