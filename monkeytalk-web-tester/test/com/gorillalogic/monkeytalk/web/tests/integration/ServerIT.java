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
package com.gorillalogic.monkeytalk.web.tests.integration;

import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.BeforeClass;
import org.junit.Test;

public class ServerIT {
	private static final int MAX = 1000;
	private static String testerJar;

	@BeforeClass
	public static void beforeClass() {
		testerJar = null;

		// search the bin folder for the correct jar
		File dir = new File("bin");
		for (File f : dir.listFiles()) {
			if (f.getName().startsWith("monkeytalk-web-tester-")
					&& !f.getName().endsWith("-sources.jar")
					&& !f.getName().endsWith("-javadoc.jar")) {
				testerJar = f.getAbsolutePath();
			}
		}
	}

	@Test
	public void testPort() throws Exception {
		// start server (use default web dir inside the jar)
		Process p = Runtime.getRuntime().exec("java -jar " + testerJar + " 13001");
		Thread.sleep(250L);
		
		new StreamEater(p.getInputStream()).start();
		new StreamEater(p.getErrorStream()).start();

		try {
			String url = "http://localhost:13001";
			System.out.println("\ntestPort:\nurl=" + url);

			// poll until server is up
			String index = get(url + "/index.html");
			int i = 1;
			while (i < MAX && !containsString("Bacon ipsum dolor sit amet").matches(index)) {
				Thread.sleep(10L);
				i++;
				System.out.println(url + " : try #" + i);
				index = get(url + "/index.html");
			}
			assertThat(index, containsString("Bacon ipsum dolor sit amet"));

			String fred = get(url + "/fred.html");
			assertThat(fred, containsString("This is fred!"));

			String forms = get(url + "/forms.html");
			assertThat(forms, containsString("Some form elements!"));

			String missing = get(url + "/missing.html");
			assertThat(missing, containsString("Not Found"));
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			Thread.sleep(1000L);
			p.destroy();
		}
	}

	@Test
	public void testFolderPath() throws IOException, InterruptedException {
		// start server with the given web dir
		Process p = Runtime.getRuntime().exec("java -jar " + testerJar + " 13002 resources/test");
		Thread.sleep(250L);
		
		new StreamEater(p.getInputStream()).start();
		new StreamEater(p.getErrorStream()).start();

		try {
			String url = "http://localhost:13002";
			System.out.println("\ntestFolderPath:\nurl=" + url);

			// poll until server is up
			String foo = get(url + "/foo.html");
			int i = 1;
			while (i < MAX && !containsString("This is FOO!").matches(foo)) {
				Thread.sleep(10L);
				i++;
				System.out.println(url + " : try #" + i);
				foo = get(url + "/foo.html");
			}
			assertThat(foo, containsString("This is FOO!"));

			String index = get(url + "/index.html");
			assertThat(index, containsString("Not Found"));

			String missing = get(url + "/missing.html");
			assertThat(missing, containsString("Not Found"));
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			Thread.sleep(1000L);
			p.destroy();
		}
	}
	
	@Test
	public void testFolderWeb() throws IOException, InterruptedException {
		// start server in the resources folder (so we pick up the local web folder)
		Process p = Runtime.getRuntime().exec("java -jar " + testerJar + " 13003", null, new File("resources"));
		Thread.sleep(250L);
		
		new StreamEater(p.getInputStream()).start();
		new StreamEater(p.getErrorStream()).start();
		
		try {
			String url = "http://localhost:13003";
			System.out.println("\ntestFolderWeb:\nurl=" + url);
			
			// poll until server is up
			String foo = get(url + "/bar.html");
			int i = 1;
			while (i < MAX && !containsString("This is BAR!").matches(foo)) {
				Thread.sleep(10L);
				i++;
				System.out.println(url + " : try #" + i);
				foo = get(url + "/bar.html");
			}
			assertThat(foo, containsString("This is BAR!"));
			
			String index = get(url + "/index.html");
			assertThat(index, containsString("Not Found"));
			
			String missing = get(url + "/missing.html");
			assertThat(missing, containsString("Not Found"));
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			Thread.sleep(1000L);
			p.destroy();
		}
	}

	/**
	 * Send an HTTP GET to the target url and return the response body as a string.
	 * 
	 * @param url
	 *            the target url
	 * @return the response body
	 * @throws IOException
	 */
	private String get(String url) {
		InputStream in = null;

		try {
			HttpClient client = new DefaultHttpClient();
			HttpGet get = new HttpGet(url);

			HttpResponse resp = client.execute(get);
			in = resp.getEntity().getContent();

			StringBuilder sb = new StringBuilder();
			String line = null;
			BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			while ((line = reader.readLine()) != null) {
				sb.append(line).append('\n');
			}

			return sb.toString();
		} catch (Exception ex) {
			System.out.println("ERROR during GET");
			ex.printStackTrace();
			return null;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException ex) {
					// ignore
				}
			}
		}
	}

	public class StreamEater extends Thread {
		private InputStream is;

		public StreamEater(InputStream is) {
			this.is = is;
		}

		public void run() {
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(is));
				String line = null;
				while ((line = br.readLine()) != null) {
					System.out.println(line);
				}
			} catch (IOException ex) {
				System.out.println("ERROR during StreamEater");
				ex.printStackTrace();
			}
		}
	}
}