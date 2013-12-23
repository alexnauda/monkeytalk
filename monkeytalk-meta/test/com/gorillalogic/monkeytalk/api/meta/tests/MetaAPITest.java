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
package com.gorillalogic.monkeytalk.api.meta.tests;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.containsString;
import static org.junit.matchers.JUnitMatchers.hasItems;

import org.junit.Test;

import com.gorillalogic.monkeytalk.api.meta.API;
import com.gorillalogic.monkeytalk.api.meta.Component;

public class MetaAPITest {

	@Test
	public void testGetComponents() {
		assertThat(API.getComponents(), notNullValue());
		assertThat(API.getComponents().size(), is(not(0)));

		Component browser = API.getComponents().get(1);
		assertThat(browser.getName(), is("Browser"));
		assertThat(browser.getSuper(), nullValue());
		assertThat(browser.getActionNames(), hasItems("Open", "Back", "Forward"));

		Component button = API.getComponents().get(2);
		assertThat(button.getName(), is("Button"));
		assertThat(button.getSuper().getName(), is("View"));
		assertThat(button.getActionNames(), hasItems("Verify", "TouchDown", "TouchUp", "Tap"));
		
		assertTrue(browser.compareTo(button) < 0);
		assertTrue(browser.compareTo(browser) == 0);
		
		assertTrue(button.compareTo(browser) > 0);
		assertTrue(button.compareTo(button) == 0);
		
		assertTrue(button.equals(button));
	}

	@Test
	public void testComponentTypes() {
		assertThat(API.getComponentTypes(), notNullValue());
		assertThat(API.getComponentTypes(), hasItems("Button", "CheckBox", "Input", "VideoPlayer"));
	}

	@Test
	public void testGetByComponentTypeForButton() {
		Component c = API.getComponent("Button");
		assertThat(c.getName(), is("Button"));
		assertThat(c.getDescription(), containsString("UIButton"));
		assertThat(c.getSuper().getName(), is("View"));
		assertThat(c.getActionNames(), hasItems("Verify", "TouchDown", "TouchUp", "Tap"));
	}

	@Test
	public void testGetByComponentTypeForDevice() {
		Component c = API.getComponent("Device");
		assertThat(c.getName(), is("Device"));
		assertThat(c.getDescription(), containsString("The device"));
		assertThat(c.getSuper(), nullValue());
		assertThat(c.getActionNames(), hasItems("Back", "Rotate", "Shake", "Get"));
	}

	@Test
	public void testCommandNames() {
		assertThat(API.getCommandNames(), notNullValue());
		assertThat(
				API.getCommandNames(),
				hasItems("Browser.Open", "Button.Tap", "CheckBox.VerifyNotRegex", "Device.Get", "Input.EnterText",
						"ButtonSelector.Select", "VideoPlayer.VerifyWildcard"));
	}
}