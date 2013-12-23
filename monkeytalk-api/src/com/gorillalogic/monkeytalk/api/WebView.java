package com.gorillalogic.monkeytalk.api;

/**
 * A component that displays web pages. iOS: UIWebView. Android: WebView.
 */
public interface WebView extends View {

	/**
	 * Scroll to the specified coordinates.
	 * 
	 * @param x
	 *            the x-coordinate (horizontal)
	 * @param y
	 *            the y-coordinate (vertical)
	 */
	public void scroll(int x, int y);

}
