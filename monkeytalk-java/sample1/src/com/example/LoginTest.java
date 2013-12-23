package com.example;

import java.io.File;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.gorillalogic.monkeytalk.java.MonkeyTalkDriver;
import com.gorillalogic.monkeytalk.java.api.Application;
import com.gorillalogic.monkeytalk.java.utils.Mods;

public class LoginTest {
	private static MonkeyTalkDriver mt;
	private Application app;

	@BeforeClass
	public static void beforeClass() {
		File dir = new File(".");

		// Android driver
		// mt = new MonkeyTalkDriver(dir, "AndroidEmulator");
		// mt.setAdb(new File("/path/to/adb"));

		// iOS driver
		mt = new MonkeyTalkDriver(dir, "iOS");

		mt.setThinktime(250);
	}

	@Before
	public void before() {
		app = mt.app();
		app.tabBar().select("login");
	}

	@Test
	public void testLogin() {
		app.input("username").enterText("fred");
		app.input("password").enterText("pass");
		app.button("LOGIN").tap();
		app.button("LOGOUT").verify(new Mods.Builder().timeout(3500).build());
		app.label().verify("Welcome, fred!");
		app.button("LOGOUT").tap();
	}
}
