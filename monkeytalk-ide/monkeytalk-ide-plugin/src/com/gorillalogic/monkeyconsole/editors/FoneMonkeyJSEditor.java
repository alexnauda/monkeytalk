package com.gorillalogic.monkeyconsole.editors;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextEditor;

import com.gorillalogic.monkeyconsole.plugin.FoneMonkeyPlugin;
import com.gorillalogic.monkeyconsole.tableview.MonkeyTalkTabularEditor;

public class FoneMonkeyJSEditor extends
		org.eclipse.wst.jsdt.internal.ui.javaeditor.CompilationUnitEditor {

	private TextEditor textEditor;

	private MonkeyTalkTabularEditor fmc;

	public FoneMonkeyJSEditor() {
		super();
	}

	public void init(IEditorSite site, IEditorInput editorInput) throws PartInitException {
		setPartName(editorInput.getName());

		if (!(editorInput instanceof IFileEditorInput))
			throw new PartInitException("Invalid Input: Must be IFileEditorInput");
		else {
			textEditor = new TextEditor();
			// textEditor.setInput(editorInput);
		}
		super.init(site, editorInput);

		try {
			fmc = new MonkeyTalkTabularEditor(FoneMonkeyPlugin.getDefault().getController());
			fmc.init(site, editorInput);
		} catch (Exception e) { // TODO Auto-generated catch block e.printStackTrace();

		}

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
