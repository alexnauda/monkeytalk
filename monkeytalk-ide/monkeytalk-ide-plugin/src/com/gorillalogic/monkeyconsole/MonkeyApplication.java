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
package com.gorillalogic.monkeyconsole;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import com.gorillalogic.monkeyconsole.plugin.FoneMonkeyPlugin;
 
/**
 * This class controls all aspects of the application's execution
 */
public class MonkeyApplication implements IApplication {
 
   public Object start(IApplicationContext context) {
      Display display = PlatformUI.createDisplay();
      try {
         int returnCode = PlatformUI.createAndRunWorkbench(display, new MonkeyWorkbenchAdvisor());
         if (returnCode == PlatformUI.RETURN_RESTART) {
            return IApplication.EXIT_RESTART;
         }
         return IApplication.EXIT_OK;
      } finally {
         display.dispose();
      }
   }
 
   public void stop() {
      if (!PlatformUI.isWorkbenchRunning())
         return;
      final IWorkbench workbench = PlatformUI.getWorkbench();
      FoneMonkeyPlugin.getDefault().getController().stopRecordServer();
      final Display display = workbench.getDisplay();
      display.syncExec(new Runnable() {
         public void run() {
            if (!display.isDisposed())
               workbench.close();
         }
      });
   }
}