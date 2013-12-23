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
package com.gorillalogic.monkeyconsole.perspectives;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPlaceholderFolderLayout;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.internal.browser.WebBrowserView;
import org.eclipse.ui.navigator.resources.ProjectExplorer;


@SuppressWarnings("restriction")
public class TestPerspective implements IPerspectiveFactory {
	public static final String PERSPECTIVE_ID = "com.gorillalogic.monkeytalk.ide.TestPerspective";
	
	// @Override -- breaks tycho compile
	public void createInitialLayout(IPageLayout layout) {
		// Get the editor area.
		String editorArea = layout.getEditorArea();

        // Top left: Resource Navigator view and Bookmarks view placeholder
       // IFolderLayout topLeft = layout.createFolder("topLeft", IPageLayout.LEFT, 0.25f,editorArea);
        
        //topLeft.addView(ProjectExplorer.VIEW_ID);
        // topLeft.addView(IPageLayout.ID_PROJECT_EXPLORER);
        // topLeft.addPlaceholder(IPageLayout.ID_BOOKMARKS);
        IPlaceholderFolderLayout folder = layout.createPlaceholderFolder("CloudBrowser", IPageLayout.RIGHT, 0.75f, editorArea);
        folder.addPlaceholder(WebBrowserView.WEB_BROWSER_VIEW_ID + ":*");

        
        // Bottom left: Outline view and Property Sheet view
        // IFolderLayout bottomLeft = layout.createFolder("bottomLeft", IPageLayout.BOTTOM, 0.50f, "topLeft");
        // bottomLeft.addView(IPageLayout.ID_OUTLINE);
        // bottomLeft.addView(IPageLayout.ID_PROP_SHEET);

        // Bottom right: Task List view
        // layout.addView(IPageLayout.ID_PROBLEM_VIEW, IPageLayout.BOTTOM, 0.99f, editorArea);
        layout.addView(IConsoleConstants.ID_CONSOLE_VIEW, IPageLayout.BOTTOM, .75f, editorArea);
        layout.addNewWizardShortcut("com.gorillalogic.monkeyconsole.wizard.NewProjectWizard");
        layout.addNewWizardShortcut("com.gorillalogic.monkeyconsole.wizard.NewTestWizard");
        layout.addNewWizardShortcut("com.gorillalogic.monkeyconsole.wizard.NewTestSuiteWizard");  

	}
}