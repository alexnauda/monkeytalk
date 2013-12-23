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
package com.gorillalogic.monkeyconsole.tableview.editors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.LayoutConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.gorillalogic.monkeyconsole.tableview.TableRow;
import com.gorillalogic.monkeytalk.api.meta.API;
import com.gorillalogic.monkeytalk.api.meta.Arg;

/**
 * Class to allow users to edit arguments to a command
 * 
 * @author digitalogic8
 * 
 */
public class ArgumentEditorDialog extends Dialog {

	/**
	 * Store for values to edit
	 */
	Map<String, String> args; // Only for passing in

	/**
	 * Store for the dynamically created editors so we can access them later
	 */
	Map<String, Text> editors; // Only for reading

	TableRow currentRow;

	/**
	 * Constructor take the shell to disply the dialog on and a list of
	 * arguments to edit
	 * 
	 * @param parentShell
	 *            app shell
	 * @param nameValuePairs
	 *            name values of arguments to edit
	 */
	protected ArgumentEditorDialog(Shell parentShell,
			Map<String, String> nameValuePairs, TableRow currentRow) {
		super(parentShell);
		args = nameValuePairs;
		this.currentRow = currentRow;
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite contents = new Composite(parent, SWT.NONE);
		contents.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		this.createButtonBar(parent);

		Composite argsHolder = new Composite(contents, SWT.NONE);
		FillLayout fillLayout = new FillLayout();
		fillLayout.type = SWT.VERTICAL;
		argsHolder.setLayout(fillLayout);

		editors = new HashMap<String, Text>();
		for (String name : args.keySet()) {
			Composite c = new Composite(argsHolder, SWT.NONE);
			RowLayout rl = new RowLayout();
			c.setLayout(rl);

			Label l = new Label(c, SWT.LEFT);
			l.setText(name + ":");
			l.setLayoutData(new RowData(125, 20));
			Text input = new Text(c, SWT.BORDER);
			input.setText(args.get(name));
			input.setLayoutData(new RowData(200, 20));
			editors.put(name, input);
		}
		Dialog.applyDialogFont(parent);

		Point defaultMargins = LayoutConstants.getMargins();
		GridLayoutFactory.fillDefaults().numColumns(2)
				.margins(defaultMargins.x, defaultMargins.y)
				.generateLayout(contents);
        
		return contents;
	}

	@Override
	protected void okPressed() {
		for (String name : args.keySet()) {
			args.put(name, editors.get(name).getText().trim());
		}
		super.okPressed();
	}

	public Map<String, String> getArgs() {
		return args;
	}

	public void setArgs(Map<String, String> args) {
		this.args = args;
	}

	public String getComposedValue() {
		if(null != args.get("args")){
		 return args.get("args");	
		}
		String s = "";
		List<Arg> argNames2 = API.getComponent(currentRow.getComponentType())
				.getAction(currentRow.getAction()).getArgs();

		for (Arg arg : argNames2) {
			s += args.get(arg.getName()).contains(" ") && !arg.isVarArgs() ? ("\""
					+ args.get(arg.getName()) + "\"")
					: args.get(arg.getName());
			s += " ";
		}
		return s;

	}

}