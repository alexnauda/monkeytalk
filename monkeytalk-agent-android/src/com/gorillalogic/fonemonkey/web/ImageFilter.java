package com.gorillalogic.fonemonkey.web;

public class ImageFilter extends WebFilterBase {

	private static String[] componentTypes = new String[] { "image" };
	private static String[] tagNames = new String[] { "img" };

	@Override
	public String[] getComponentTypes() {
		return componentTypes;
	}

	@Override
	protected String[] getTagNames() {
		return tagNames;
	}

}
