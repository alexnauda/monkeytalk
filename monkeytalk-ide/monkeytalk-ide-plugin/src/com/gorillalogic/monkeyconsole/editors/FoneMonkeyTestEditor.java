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
package com.gorillalogic.monkeyconsole.editors;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;

import com.gorillalogic.monkeyconsole.editors.utils.FoneMonkeyConsoleHelper;
import com.gorillalogic.monkeyconsole.editors.utils.MonkeyTalkUtils;
import com.gorillalogic.monkeyconsole.plugin.FoneMonkeyPlugin;
import com.gorillalogic.monkeyconsole.tableview.MonkeyTalkTabularEditor;
import com.gorillalogic.monkeytalk.Command;
import com.gorillalogic.monkeytalk.api.js.tools.JSLibGenerator;
import com.gorillalogic.monkeytalk.api.js.tools.JSMTGenerator;

/**
 * An example showing how to create a multi-page editor. This example has 3 pages:
 * <ul>
 * <li>page 0 contains a nested text editor.
 * <li>page 1 allows you to change the font used in page 2
 * <li>page 2 shows the words in page 0 in sorted order
 * </ul>
 */
public class FoneMonkeyTestEditor extends MultiPageEditorPart implements IResourceChangeListener {

	/** The text editor used in page 0. */
	private TextEditor textEditor;
	private MonkeyTalkTabularEditor fmc;
	private FoneMonkeyConsoleHelper fmch;
	private int pageIndex;
	public static Map<String, Color> colors = new HashMap<String, Color>();

	/**
	 * Creates a multi-page editor example.
	 */
	public FoneMonkeyTestEditor() {
		super();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
		// colors.put("RED", this.getSite().getShell().getDisplay().getSystemColor(SWT.COLOR_RED));
		fmch = new FoneMonkeyConsoleHelper(this.getEditorSite());
	}

	/**
	 * Creates page 0 the tabular view
	 */
	void createPage0() {

		int index;
		try {
			index = this.addPage(fmc, getEditorInput());
			setPageText(index, "Table View");
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Creates page 1 the monkey talk view
	 */
	void createPage1() {
		try {
			textEditor = new TextEditor();
			int index = addPage(textEditor, getEditorInput());
			setPageText(index, "MonkeyTalk");
		} catch (PartInitException e) {
			ErrorDialog.openError(getSite().getShell(), "Error creating nested text editor", null,
					e.getStatus());
		}
	}

	/**
	 * Creates page 2 of the multi-page editor, which shows the java script
	 */
	StyledText t;

	void createPage2() {
		Composite mainMainComposite = new Composite(getContainer(), SWT.NONE);
		mainMainComposite.setLayout(new FormLayout());
		Button b = new Button(mainMainComposite, SWT.NONE);
		b.setText("Export");
		b.addMouseListener(new MouseListener() {

			// @Override -- breaks tycho compile
			public void mouseDoubleClick(MouseEvent arg0) {
				// TODO Auto-generated method stub

			}

			// @Override -- breaks tycho compile
			public void mouseDown(MouseEvent arg0) {

			}

			// @Override -- breaks tycho compile
			public void mouseUp(MouseEvent arg0) {
				MonkeyTalkUtils.generateJScript(
						(FileEditorInput) FoneMonkeyTestEditor.this.getEditorInput(),
						fmc.getCommands(), FoneMonkeyTestEditor.this.getSite());

			}

		});
		t = new StyledText(mainMainComposite, SWT.H_SCROLL | SWT.V_SCROLL);

		t.setEditable(false);
		FormData data1 = new FormData();
		data1.left = new FormAttachment(0, 5);
		data1.right = new FormAttachment(25, 0);
		b.setLayoutData(data1);

		FormData data3 = new FormData();
		data3.top = new FormAttachment(b, 5);
		data3.left = new FormAttachment(0, 0);
		data3.right = new FormAttachment(100, 0);
		data3.bottom = new FormAttachment(100, 0);
		t.setLayoutData(data3);
		if (((FileEditorInput) getEditorInput()).getFile().getFileExtension()
				.equalsIgnoreCase("mt")) {
			int index = addPage(mainMainComposite);
			setPageText(index, "JavaScript");
		}
	}

	/**
	 * Creates the pages of the multi-page editor.
	 */
	protected void createPages() {
		createPage0();
		createPage1();
		createPage2();
		convertFromMonkeyTalk();
		FoneMonkeyPlugin
				.getDefault()
				.getController()
				.setContextualData(
						fmc,
						textEditor,
						((FoneMonkeyTestContributor) getEditorSite().getActionBarContributor())
								.getClearToolItem(),
						((FoneMonkeyTestContributor) getEditorSite().getActionBarContributor())
								.getComponentTreeToolItem(),
						((FoneMonkeyTestContributor) getEditorSite().getActionBarContributor())
								.getPlayToolItem(),
						((FoneMonkeyTestContributor) getEditorSite().getActionBarContributor())
								.getStopToolItem(),
						((FoneMonkeyTestContributor) getEditorSite().getActionBarContributor())
								.getRecordToolItem(),
						((FoneMonkeyTestContributor) getEditorSite().getActionBarContributor())
								.getPlayOnCloudAction());
	}

	/**
	 * The <code>MultiPageEditorPart</code> implementation of this <code>IWorkbenchPart</code>
	 * method disposes all nested editors. Subclasses may extend.
	 */
	public void dispose() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		FoneMonkeyPlugin.getDefault().getController().stopRecordServer();
		super.dispose();
	}

	/**
	 * Saves the multi-page editor's document.
	 */
	public void doSave(IProgressMonitor monitor) {
		if (this.getActivePage() == 0) {
			this.convertToMonkeyTalk();
		}
		getEditor(1).doSave(monitor);
		String js = "";
		try {
			js = JSMTGenerator.createScript(((FileEditorInput) getEditorInput()).getFile()
					.getProject().getName(), getEditorInput().getName(), fmc.getCommands());
		} catch (Exception e) {
			// /couldn't convert
		}
		try {
			File f = new File(((FileEditorInput) getEditorInput()).getPath().toString());
			f = f.getParentFile();

			String jsLIB = JSLibGenerator.createLib(((FileEditorInput) getEditorInput()).getFile()
					.getProject().getName(), f);
			File outfile = new File(f.getAbsolutePath() + "/libs/"
					+ ((FileEditorInput) getEditorInput()).getFile().getProject().getName() + ".js");
			writeStringToFile(outfile, jsLIB);
		} catch (Exception e) {
			e.printStackTrace();
		}
		fmc.setDirty(false);

	}

	public boolean writeStringToFile(File f, String data) {
		try {

			BufferedWriter out = new BufferedWriter(new FileWriter(f));
			out.write(data);
			out.close();

			((FileEditorInput) getEditorInput()).getFile().getProject()
					.refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (CoreException e) {
			return false;
		} catch (IOException e) {
			return false;

		}
		return true;
	}

	/**
	 * Saves the multi-page editor's document as another file. Also updates the text for page 0's
	 * tab, and updates this multi-page editor's input to correspond to the nested editor's.
	 */
	public void doSaveAs() {
		IEditorPart editor = getEditor(1);
		editor.doSaveAs();
		setPageText(0, editor.getTitle());
		setInput(editor.getEditorInput());
		this.setPartName(editor.getTitle());
	}

	/*
	 * (non-Javadoc) Method declared on IEditorPart
	 */
	public void gotoMarker(IMarker marker) {
		setActivePage(0);
		IDE.gotoMarker(getEditor(0), marker);
	}

	/**
	 * The <code>MultiPageEditorExample</code> implementation of this method checks that the input
	 * is an instance of <code>IFileEditorInput</code>.
	 */
	public void init(IEditorSite site, IEditorInput editorInput) throws PartInitException {
		setPartName(editorInput.getName());

		if (!(editorInput instanceof IFileEditorInput))
			throw new PartInitException("Invalid Input: Must be IFileEditorInput");
		super.init(site, editorInput);
		try {
			if (((FileEditorInput) editorInput).getFile().getFileExtension()
					.equalsIgnoreCase("mts")) {
				fmc = new MonkeyTalkTabularEditor(new String[] { "SetUp", "TearDown", "Test",
						"Suite" }, FoneMonkeyPlugin.getDefault().getController());
			} else {
				fmc = new MonkeyTalkTabularEditor(FoneMonkeyPlugin.getDefault().getController());
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/*
	 * (non-Javadoc) Method declared on IEditorPart.
	 */
	public boolean isSaveAsAllowed() {
		return true;
	}

	/**
	 * Calculates the contents of page 2 when the it is activated.
	 */
	protected void pageChange(int newPageIndex) {
		super.pageChange(newPageIndex);
		if (newPageIndex == 0) {
			convertFromMonkeyTalk();
		}
		if (newPageIndex == 1) {
			convertToMonkeyTalk();
		}
		if (newPageIndex == 2) {
			if (pageIndex == 0) {
				convertToMonkeyTalk();
			} else if (pageIndex == 1) {
				convertFromMonkeyTalk();
			}
			loadJS();
		}
		pageIndex = newPageIndex;
	}

	/**
	 * Load the data from the tabular editor into the JavaScript view
	 */
	private void loadJS() {
		String js = "";
		try {
			js = JSMTGenerator.createScript(((FileEditorInput) getEditorInput()).getFile()
					.getProject().getName(), getEditorInput().getName(), fmc.getCommands());
		} catch (Exception e) {
			// /couldn't convert. this it not the end of the world
		}
		t.setText(js);
	}

	/**
	 * Convert what has been entered into the tabular editor and place it in the text editor
	 */
	private void convertToMonkeyTalk() {
		if (textEditor == null)
			return;
		textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput())
				.set(fmc.getCommandsAsString());

	}

	/**
	 * Converts the monkey talk that has been typed into the text editor into something that the
	 * tabular editor can understand
	 */
	public void convertFromMonkeyTalk() {
		// fmc.isLoading = true;
		StringTokenizer st = new StringTokenizer(textEditor.getDocumentProvider()
				.getDocument(textEditor.getEditorInput()).get(), "\n");
		List<Command> commands = new ArrayList<Command>();
		while (st.hasMoreElements()) {
			String line = st.nextToken();
			if (line.trim().length() > 0) {
				Command c = new Command(line);
				if (c.isComment()) {
					String comment = c.toString();
					c.setComponentType(comment);
				}
				commands.add(c);

			}
		}
		fmc.setCommands(commands);
	}

	/**
	 * Closes all project files on project close.
	 */
	public void resourceChanged(final IResourceChangeEvent event) {
		if (event.getType() == IResourceChangeEvent.PRE_CLOSE) {
			FoneMonkeyPlugin.getDefault().getController().getRecordServer().stop();
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					IWorkbenchPage[] pages = getSite().getWorkbenchWindow().getPages();
					for (int i = 0; i < pages.length; i++) {
						if (((FileEditorInput) textEditor.getEditorInput()).getFile().getProject()
								.equals(event.getResource())) {
							IEditorPart editorPart = pages[i].findEditor(textEditor
									.getEditorInput());
							pages[i].closeEditor(editorPart, true);
						}
					}
				}
			});
		}
	}

	public FoneMonkeyConsoleHelper getFmch() {
		return fmch;
	}

	public void setFmch(FoneMonkeyConsoleHelper fmch) {
		this.fmch = fmch;
	}

	@Override
	public void setFocus() {
		FoneMonkeyPlugin
				.getDefault()
				.getController()
				.setContextualData(
						fmc,
						textEditor,
						((FoneMonkeyTestContributor) getEditorSite().getActionBarContributor())
								.getClearToolItem(),
						((FoneMonkeyTestContributor) getEditorSite().getActionBarContributor())
								.getComponentTreeToolItem(),
						((FoneMonkeyTestContributor) getEditorSite().getActionBarContributor())
								.getPlayToolItem(),
						((FoneMonkeyTestContributor) getEditorSite().getActionBarContributor())
								.getStopToolItem(),
						((FoneMonkeyTestContributor) getEditorSite().getActionBarContributor())
								.getRecordToolItem(),
						((FoneMonkeyTestContributor) getEditorSite().getActionBarContributor())
								.getPlayOnCloudAction());

		super.setFocus();
	}
}