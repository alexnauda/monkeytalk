package com.gorillalogic.monkeyconsole.editors.utils;

import org.eclipse.jface.wizard.Wizard;

import com.gorillalogic.monkeyconsole.plugin.FoneMonkeyPlugin;
import com.gorillalogic.monkeyconsole.preferences.PreferenceConstants;

public class TimeSetupWizard extends Wizard {

	TimeSetupWizardPage page;

	public void addPages() {
		page = new TimeSetupWizardPage("Thinktime/Timeout Setup");
		addPage(page);
	}

	@Override
	public boolean performFinish() {
		boolean perform = true;
		int thinktimeValue = Integer.parseInt(page.thinktimeText.getText());
		int timeoutValue = Integer.parseInt(page.timeoutText.getText());
		FoneMonkeyPlugin.getDefault().getPreferenceStore()
				.setValue(PreferenceConstants.P_THINKTIME, thinktimeValue);
		FoneMonkeyPlugin.getDefault().getPreferenceStore()
				.setValue(PreferenceConstants.P_DEFAULTTIMEOUT, timeoutValue);

		return perform;
	}
}
