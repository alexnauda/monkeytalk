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
package com.gorillalogic.monkeyconsole.wizard;

/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
import java.util.List;
import java.util.ListIterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.ide.DialogUtil;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.wizards.newresource.ResourceMessages;

import com.gorillalogic.monkeyconsole.editors.utils.MonkeyTalkUtils;
import com.gorillalogic.monkeytalk.api.meta.API;

/**
 * Standard workbench wizard that create a new file resource in the workspace.
 * <p>
 * This class may be instantiated and used without further configuration; this
 * class is not intended to be subclassed.
 * </p>
 * <p>
 * Example:
 * 
 * <pre>
 * IWorkbenchWizard wizard = new BasicNewFileResourceWizard();
 * wizard.init(workbench, selection);
 * WizardDialog dialog = new WizardDialog(shell, wizard);
 * dialog.open();
 * </pre>
 * 
 * During the call to <code>open</code>, the wizard dialog is presented to the
 * user. When the user hits Finish, a file resource at the user-specified
 * workspace path is created, the dialog closes, and the call to
 * <code>open</code> returns.
 * </p>
 */
public class NewTestWizard extends BasicNewResourceWizard {
	private WizardNewFileCreationPage mainPage;

	/**
	 * Creates a wizard for creating a new file resource in the workspace.
	 */
	public NewTestWizard() {
		super();
	}

	/*
	 * (non-Javadoc) Method declared on IWizard.
	 */
	public void addPages() {
		super.addPages();
		mainPage = new WizardNewFileCreationPage("newFilePage1", getSelection()){

			@Override
			protected boolean validatePage() {
				// TODO Auto-generated method stub
				boolean parentIsValid =  super.validatePage();
				if(!parentIsValid) return parentIsValid;
				 String projectFieldContents = super.getFileName();
					if (projectFieldContents.contains(" ")) { //$NON-NLS-1$
						setErrorMessage("The file name must not contain spaces.");
						setMessage(IDEWorkbenchMessages.WizardNewProjectCreationPage_projectNameEmpty);
						return false;
					}

					if (MonkeyTalkUtils.toLowerList(API.getComponentTypes()).contains(projectFieldContents.toLowerCase().substring(0, projectFieldContents.indexOf(".")))) { //$NON-NLS-1$
						setErrorMessage("This file name will override a root monkeytalk component and is therefore illegal.");
						//setMessage(IDEWorkbenchMessages.WizardNewFileCreationPage_errorTitle);
						return false;
					}
				return true;
			}
			
		};
		mainPage.setTitle("Create a new MonkeyTalk Script");
		mainPage.setFileExtension("mt");
		mainPage.setDescription("Monkey scripts are the building blocks for all MonkeyTalk testing setups.");
		addPage(mainPage);
	}

	/*
	 * (non-Javadoc) Method declared on IWorkbenchWizard.
	 */
	public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
		super.init(workbench, currentSelection);
		setWindowTitle("MonkeyTalk is provided by Gorilla Logic");
		setNeedsProgressMonitor(true);
	}

	/*
	 * (non-Javadoc) Method declared on BasicNewResourceWizard.
	 */
	protected void initializeDefaultPageImageDescriptor() {
		ImageDescriptor desc = IDEWorkbenchPlugin
				.getIDEImageDescriptor("icons/gl-icon-16.png");//$NON-NLS-1$
		setDefaultPageImageDescriptor(desc);
	}

	/*
	 * (non-Javadoc) Method declared on IWizard.
	 */
	public boolean performFinish() {
		IFile file = mainPage.createNewFile();
		if (file == null) {
			return false;
		}

		selectAndReveal(file);

		// Open editor on new file.
		IWorkbenchWindow dw = getWorkbench().getActiveWorkbenchWindow();
		try {
			if (dw != null) {
				IWorkbenchPage page = dw.getActivePage();
				if (page != null) {
					IDE.openEditor(page, file, true);
				}
			}
		} catch (PartInitException e) {
			DialogUtil.openError(dw.getShell(),
					ResourceMessages.FileResource_errorMessage, e.getMessage(),
					e);
		}

		return true;
	}


}