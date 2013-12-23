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
package com.gorillalogic.monkeytalk.api.impl.tests.integration;

import java.io.File;
import java.io.IOException;

import org.junit.AfterClass;

import com.gorillalogic.monkeytalk.api.js.tools.JSAPIGenerator;
import com.gorillalogic.monkeytalk.api.js.tools.JSLibGenerator;
import com.gorillalogic.monkeytalk.api.js.tools.JSMTGenerator;
import com.gorillalogic.monkeytalk.utils.TestHelper;

public class BaseIntegrationTest extends TestHelper {

	@AfterClass
	public static void afterClass() throws IOException {
		cleanup();
	}

	protected void genAPIAndLib(File dir) throws IOException {
		// make libs dir
		File libs = new File(dir, "libs");
		libs.mkdir();

		// generate API.js
		File apiJS = tempScript("MonkeyTalkAPI.js", "gen", libs);
		JSAPIGenerator.main(new String[] { "../monkeytalk-api/src", apiJS.getAbsolutePath() });

		// generate Lib.js
		File libJS = tempScript("MyProj.js", "gen", libs);
		JSLibGenerator.main(new String[] { dir.getAbsolutePath(), libJS.getAbsolutePath() });
	}

	protected void genJS(File mt) throws IOException {
		JSMTGenerator.main(new String[] { "MyProj", mt.getAbsolutePath() });
	}
}