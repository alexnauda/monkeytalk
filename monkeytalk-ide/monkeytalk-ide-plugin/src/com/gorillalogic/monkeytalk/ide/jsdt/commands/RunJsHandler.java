package com.gorillalogic.monkeytalk.ide.jsdt.commands;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;

import com.gorillalogic.monkeyconsole.editors.utils.MonkeyTalkController;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class RunJsHandler implements IEditorActionDelegate {
	/**
	 * The constructor.
	 */
	AbstractDecoratedTextEditor editor;
	 MonkeyTalkController monkeyControls;
	public RunJsHandler() {
		monkeyControls = new MonkeyTalkController();
		
	}


	@Override
	public void run(IAction arg0) {
		monkeyControls.startJScriptReplay();
	}


	@Override
	public void selectionChanged(IAction arg0, ISelection arg1) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void setActiveEditor(IAction arg0, IEditorPart arg1) {
		if(arg1 != null && arg1 instanceof AbstractDecoratedTextEditor){
		editor = (AbstractDecoratedTextEditor)arg1;
		String path = (((FileEditorInput)editor.getEditorInput()).getPath() + "----blahhhhh!");
         path.length();		

		monkeyControls.setJSContextualData(editor);
		}
	}
}
