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

import com.gorillalogic.monkeytalk.processor.ScriptProcessor;
import com.gorillalogic.monkeytalk.shell.Print;

public class Timeout extends BaseCommand {

	public Timeout(String line, ScriptProcessor processor) {
		super(line, processor);
	}

	@Override
	public void run() {
		int n = getIntArg();
		if (n >= 0) {
			processor.setGlobalTimeout(n);
			Print.info("timeout = " + n + "ms");
		} else {
			Print.err("ERROR: bad timeout");
		}
	}

	protected int getIntArg() {
		int n = -1;
		String[] parts = line.split(" ");
		if (line.length() > 1) {
			try {
				return Integer.parseInt(parts[1].replaceAll("\\D+", ""));
			} catch (NumberFormatException ex) {
				return -1;
			}
		}
		return n;
	}
}
