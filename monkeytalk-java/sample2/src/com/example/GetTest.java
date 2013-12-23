package com.example;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class GetTest extends BaseHelper {

	@Test
	public void testGet() {
		app.tabBar().select("login");
		assertThat(app.label("myTitle").get(), is("Login"));

		app.input("username").enterText("");
		app.input("password").enterText("");
		assertThat(app.input("username").get(), is(""));
		assertThat(app.input("password").get(), is(""));

		app.input("username").enterText("joe");
		app.input("password").enterText("my pass");
		assertThat(app.input("username").get(), is("joe"));
		assertThat(app.input("password").get(), is("my pass"));

		app.button("LOGIN").tap();

		app.label("myError").verifyWildcard("*4 or more characters.");
	}
}
