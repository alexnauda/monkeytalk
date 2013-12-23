package com.example;

import org.junit.Test;

import com.gorillalogic.monkeytalk.java.utils.Mods;

public class LoginTest extends BaseHelper {

	@Test
	public void testLogin() {
		app.tabBar().select("login");
		app.input("username").enterText("fred");
		app.input("password").enterText("pass");
		app.button("LOGIN").tap();
		app.button("LOGOUT").verify(new Mods.Builder().timeout(9999).build());
		app.label().verify("Welcome, fred!");
		app.label().verifyWildcard("Welcome, *!");
		app.label().verifyRegex("Welcome, \\w+!");
		app.label().verify("XXX", new Mods.Builder().shouldFail(true).timeout(500).build());
		app.button("LOGOUT").tap();
	}
}
