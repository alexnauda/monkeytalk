/*  MonkeyTalk - a cross-platform functional testing tool
    Copyright (C) 2013 Gorilla Logic, Inc.

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
package com.gorillalogic.monkeytalk.shell.command;

import java.util.Map;

import com.gorillalogic.monkeytalk.processor.Globals;
import com.gorillalogic.monkeytalk.processor.Scope;
import com.gorillalogic.monkeytalk.processor.ScriptProcessor;
import com.gorillalogic.monkeytalk.shell.Print;

public class Vars extends BaseCommand {
	private Scope scope;

	public Vars(String line, ScriptProcessor processor, Scope scope) {
		super(line, processor);
		this.scope = scope;
	}

	@Override
	public void run() {
		printVars("local", scope.getVariables());
		printVars("global", Globals.getGlobals());
	}

	private void printVars(String name, Map<String, String> vars) {
		Print.println(name.toUpperCase() + ":");
		if (vars.size() == 0) {
			Print.println("  -none-");
		} else {
			for (Map.Entry<String, String> entry : vars.entrySet()) {
				Print.println("  " + entry.getKey() + "=" + entry.getValue());
			}
		}
	}
}
