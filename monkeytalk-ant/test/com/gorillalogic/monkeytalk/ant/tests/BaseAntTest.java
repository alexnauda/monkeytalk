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
package com.gorillalogic.monkeytalk.ant.tests;

import static org.junit.Assert.fail;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.AfterClass;

import com.gorillalogic.monkeytalk.utils.FileUtils;
import com.gorillalogic.monkeytalk.utils.TestHelper;
import com.gorillalogic.monkeytalk.utils.TestHelper.CommandServer;

public class BaseAntTest {
	protected static final String DIR_PREFIX = "tmpMT";

	@AfterClass
	public static void afterClass() throws IOException {
		File dummy = File.createTempFile("dummy", null);
		dummy.deleteOnExit();

		for (File f : dummy.getParentFile().listFiles()) {
			if (f.isDirectory() && f.getName().startsWith(DIR_PREFIX)) {
				deleteDir(f);
			}
		}
	}

	protected File tempDir() throws IOException {
		final String now = Long.toString(System.nanoTime());
		final File dir = File.createTempFile(DIR_PREFIX, now);

		if (!dir.delete()) {
			fail("failed to delete file: " + dir.getAbsolutePath());
		}

		if (!dir.mkdir()) {
			fail("failed to create dir: " + dir.getAbsolutePath());
		}

		return dir;
	}

	protected File tempScript(String filename, String contents, File dir) throws IOException {
		File tmp = new File(dir, filename);
		FileUtils.writeFile(tmp, contents);
		return tmp;
	}

	protected static void deleteDir(File dir) throws IOException {
		for (File f : dir.listFiles()) {
			if (f.isDirectory()) {
				deleteDir(f);
			} else {
				f.delete();
			}
		}
		dir.delete();
	}

	protected File findFile(String filename, File dir) {
		if (dir != null && dir.isDirectory()) {
			for (File f : dir.listFiles()) {
				if (f.getName().equalsIgnoreCase(filename)) {
					return f;
				}
			}
		}
		return null;
	}

	protected String readFile(File f) throws IOException {
		byte[] buf = new byte[(int) f.length()];
		BufferedInputStream in = null;
		try {
			in = new BufferedInputStream(new FileInputStream(f));
			in.read(buf);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException ignored) {
				}
			}
		}
		return new String(buf);
	}

	protected class PlaybackCommandServer extends CommandServer {

		public PlaybackCommandServer(TestHelper testHelper, int port) throws IOException {
			testHelper.super(port);
		}

		@Override
		public Response serve(String uri, String method, Map<String, String> headers,
				JSONObject json) {
			try {
				if (!json.getString("mtcommand").equals("PLAY")) {
					return new Response(HttpStatus.OK, "{result:\"ERROR\"}");
				}
			} catch (JSONException ex) {
				throw new RuntimeException(ex);
			}
			return super.serve(uri, method, headers, json);
		}
	}
}