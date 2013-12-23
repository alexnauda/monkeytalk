package com.example;

import org.junit.Test;

public class FormsTest extends BaseHelper {

	@Test
	public void testForms() {
		app.tabBar().select("forms");
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
