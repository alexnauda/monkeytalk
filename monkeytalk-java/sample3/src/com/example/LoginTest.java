package com.example;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.gorillalogic.monkeytalk.java.MonkeyTalkDriver;
import com.gorillalogic.monkeytalk.java.api.Application;
import com.gorillalogic.monkeytalk.java.error.MonkeyTalkFailure;
import com.gorillalogic.monkeytalk.java.utils.Mods;

public class LoginTest {
	private static MonkeyTalkDriver mt;
	private Application app;

	@BeforeClass
	public static void beforeClass() throws IOException {
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

	@Test(expected = MonkeyTalkFailure.class)
	public void testBadLogin() {
		app.input("username").enterText("joe");
		app.input("password").enterText("pass");
		app.button("LOGIN").tap();
		app.button("LOGOUT").verify(new Mods.Builder().timeout(3500).build());
	}

	@Test
	public void testGet() {
		app.input("username").enterText("i like cheese");

		String val = app.input("username").get();
		assertThat(val, is("i like cheese"));
	}

	@Test
	public void testRaw() {
		app.raw("Input username EnterText \"i am fred\"");

		String val = app.raw("Input username Get dummy");
		assertThat(val, is("i am fred"));
	}
}
