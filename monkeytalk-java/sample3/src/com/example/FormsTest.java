package com.example;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.gorillalogic.monkeytalk.java.MonkeyTalkDriver;
import com.gorillalogic.monkeytalk.java.api.Application;

public class FormsTest {
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
		app.tabBar().select("forms");
	}

	@Test
	public void testForms() {
		setFormAndVerify("Boron", true, "B", 37);
		setFormAndVerify("Helium", false, "A", 81);
		setFormAndVerify("Carbon", true, "C", 55);
		setFormAndVerify("Hydrogen", false, "A", 18);
	}

	public void setFormAndVerify(String elem, boolean chk, String radio, int slider) {
		app.itemSelector("myDropdown").select(elem);
		if (chk) {
			app.checkBox("mySwitch").on();
		} else {
			app.checkBox("mySwitch").off();
		}
		app.buttonSelector("myRadios").select(radio);
		app.slider("mySlider").select(slider);

		String val = elem + " | " + (chk ? "on" : "off") + " | " + radio + " | " + slider;
		app.label("myValue").verify(val);
		app.label("myValue").verifyNot("FRED");
	}
}
