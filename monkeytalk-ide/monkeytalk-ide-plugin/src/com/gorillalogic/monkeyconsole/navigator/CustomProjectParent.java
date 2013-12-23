package com.gorillalogic.monkeyconsole.navigator;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.graphics.Image;

import com.gorillalogic.monkeyconsole.plugin.FoneMonkeyPlugin;

public class CustomProjectParent {

    private IProject project;
    private Image image;

    public Image getImage() {
        if (image == null) {
            image = FoneMonkeyPlugin.getImage("icons/project-folder.png"); //$NON-NLS-1$
        }

        return image;
    }
 
    public CustomProjectParent(IProject iProject) {
        project = iProject;
    }
 
    public String getProjectName() {
        return project.getName();
    }
}
