package com.gorillalogic.monkeyconsole.navigator;

import java.util.List;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

import com.gorillalogic.monkeyconsole.builder.MonkeyTalkNature;

public class ContentProvider implements ITreeContentProvider {

	private static final Object[] NO_CHILDREN = {};
	private CustomProjectParent[] customProjectParents;

	@Override
	public Object[] getChildren(Object parentElement) {
		Object[] children = null;
		if (CustomProjectWorkbenchRoot.class.isInstance(parentElement)) {
			if (customProjectParents == null) {
				customProjectParents = initializeParent(parentElement);
			}

			children = customProjectParents;
		} else {
			children = NO_CHILDREN;
		}

		return children;
	}

	private CustomProjectParent[] initializeParent(Object parentElement) {

		String name = "Console";
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		
		MessageConsole myConsole = null;
		for (int i = 0; i < existing.length; i++){
			if (name.equals(existing[i].getName())){
				myConsole = (MessageConsole) existing[i];
			}
		}
		if(myConsole ==null){
		myConsole = new MessageConsole(name, null);
		conMan.addConsoles(new IConsole[]{myConsole});
		}


		MessageConsoleStream out = myConsole.newMessageStream();
		out.println("Hello from Generic console sample action");


		IProject [] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        CustomProjectParent[] result = new CustomProjectParent[projects.length];
        for (int i = 0; i < projects.length; i++) {
            result[i] = new CustomProjectParent(projects[i]);
        }

        return result;
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object[] getElements(Object inputElement) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getParent(Object element) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		// TODO Auto-generated method stub
		return false;
	}

}
