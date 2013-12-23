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
package com.gorillalogic.monkeyconsole.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.IntroPart;


import com.gorillalogic.monkeyconsole.wizard.NewProjectWizard;

/**
 * @author sstern
 *
 */
public class NewProject implements IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow win;
	private ISelection sel;
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	// @Override - breaks tycho compile
	public void run(IAction action) {
	      NewProjectWizard wizard = new NewProjectWizard();
	      IWorkbench wb = win.getWorkbench();
	      wizard.init(wb, (IStructuredSelection)  win.getActivePage().getSelection());

	      // Create the wizard dialog
	      WizardDialog dialog = new WizardDialog(wb.getActiveWorkbenchWindow().getShell(),wizard);
	      // Open the wizard dialog
	      dialog.open();
	      wb.getIntroManager().closeIntro(wb.getIntroManager().getIntro());
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	// @Override - breaks tycho compile
	public void selectionChanged(IAction action, ISelection selection) {
		sel = selection;
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	// @Override - breaks tycho compile
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	// @Override - breaks tycho compile
	public void init(IWorkbenchWindow window) {
		win = window;
		
	}

}