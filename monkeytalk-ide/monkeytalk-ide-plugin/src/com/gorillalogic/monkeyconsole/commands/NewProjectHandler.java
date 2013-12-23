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
package com.gorillalogic.monkeyconsole.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.handlers.HandlerUtil;

import com.gorillalogic.monkeyconsole.wizard.NewProjectWizard;

/**
 * @author sstern
 *
 */
public class NewProjectHandler extends AbstractHandler{

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	// @Override - breaks tycho compile
	public Object execute(ExecutionEvent event) throws ExecutionException {
	      NewProjectWizard wizard = new NewProjectWizard();
	      IWorkbench wb = HandlerUtil.getActiveWorkbenchWindow(event).getWorkbench();
	      wizard.init(wb, (IStructuredSelection) HandlerUtil.getCurrentSelection(event));

	      // Create the wizard dialog
	      WizardDialog dialog = new WizardDialog(wb.getActiveWorkbenchWindow().getShell(),wizard);
	      // Open the wizard dialog
	      dialog.open();
	      return null;
	}

}