/*  MonkeyTalk - a cross-platform functional testing tool
    Copyright (C) 2013 Gorilla Logic, Inc.

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
package com.gorillalogic.monkeytalk.java.api;

/**
 * Helper for the MonkeyTalk application under test.
 */
public interface Application {
	/**
	 * The application under test.
	 * @return the App component
	 */
	public App app();
	/**
	 * The application under test.
	 * @param monkeyId the monkeyId
	 * @return the App component
	 */
	public App app(String monkeyId);

	/**
	 * The browser hosting the webapp under test.
	 * @return the Browser component
	 */
	public Browser browser();
	/**
	 * The browser hosting the webapp under test.
	 * @param monkeyId the monkeyId
	 * @return the Browser component
	 */
	public Browser browser(String monkeyId);

	/**
	 * A Button. iOS: UIButton. Android: Button. Web: Button tag, or Input tag with type='submit' or type='reset'. If the button has a label, it is used as the monkeyId.
	 * @return the Button component
	 */
	public Button button();
	/**
	 * A Button. iOS: UIButton. Android: Button. Web: Button tag, or Input tag with type='submit' or type='reset'. If the button has a label, it is used as the monkeyId.
	 * @param monkeyId the monkeyId
	 * @return the Button component
	 */
	public Button button(String monkeyId);

	/**
	 * A group of radio (mutually exclusive) buttons. iOS: UISegmentedControl. Android: RadioGroup. Web: A set of Input tags with type='radio' and name='group'.
	 * @return the ButtonSelector component
	 */
	public ButtonSelector buttonSelector();
	/**
	 * A group of radio (mutually exclusive) buttons. iOS: UISegmentedControl. Android: RadioGroup. Web: A set of Input tags with type='radio' and name='group'.
	 * @param monkeyId the monkeyId
	 * @return the ButtonSelector component
	 */
	public ButtonSelector buttonSelector(String monkeyId);

	/**
	 * A component that can be checked or unchecked. iOS: ignored. Android: CheckBox.
	 * @return the CheckBox component
	 */
	public CheckBox checkBox();
	/**
	 * A component that can be checked or unchecked. iOS: ignored. Android: CheckBox.
	 * @param monkeyId the monkeyId
	 * @return the CheckBox component
	 */
	public CheckBox checkBox(String monkeyId);

	/**
	 * A component that edits a date. iOS: UIDatePicker. Android: DatePicker.
	 * @return the DatePicker component
	 */
	public DatePicker datePicker();
	/**
	 * A component that edits a date. iOS: UIDatePicker. Android: DatePicker.
	 * @param monkeyId the monkeyId
	 * @return the DatePicker component
	 */
	public DatePicker datePicker(String monkeyId);

	/**
	 * The device hosting the application under test.
	 * @return the Device component
	 */
	public Device device();
	/**
	 * The device hosting the application under test.
	 * @param monkeyId the monkeyId
	 * @return the Device component
	 */
	public Device device(String monkeyId);

	/**
	 * A component that provides a grid view of data. iOS: UICollectionView. Android: GridView.
	 * @return the Grid component
	 */
	public Grid grid();
	/**
	 * A component that provides a grid view of data. iOS: UICollectionView. Android: GridView.
	 * @param monkeyId the monkeyId
	 * @return the Grid component
	 */
	public Grid grid(String monkeyId);

	/**
	 * An image. iOS: UIImage, Android: ImageView.
	 * @return the Image component
	 */
	public Image image();
	/**
	 * An image. iOS: UIImage, Android: ImageView.
	 * @param monkeyId the monkeyId
	 * @return the Image component
	 */
	public Image image(String monkeyId);

	/**
	 * A component that provides for selection of an item from a list of items. Item selection is recorded and played back with an index indicating the selected item.
	 * @return the IndexedSelector component
	 */
	public IndexedSelector indexedSelector();
	/**
	 * A component that provides for selection of an item from a list of items. Item selection is recorded and played back with an index indicating the selected item.
	 * @param monkeyId the monkeyId
	 * @return the IndexedSelector component
	 */
	public IndexedSelector indexedSelector(String monkeyId);

	/**
	 * A single-line input field. iOS: UITextField. Android: single-line editable TextView. Web: Input tag with type='text'. If the input as a hint/prompt, it is used as the monkeyId.
	 * @return the Input component
	 */
	public Input input();
	/**
	 * A single-line input field. iOS: UITextField. Android: single-line editable TextView. Web: Input tag with type='text'. If the input as a hint/prompt, it is used as the monkeyId.
	 * @param monkeyId the monkeyId
	 * @return the Input component
	 */
	public Input input(String monkeyId);

	/**
	 * A component that provides for selecting a text item from a list of items. iOS: UIPickerView. Android: UISpinner. Web: Select tag.
	 * @return the ItemSelector component
	 */
	public ItemSelector itemSelector();
	/**
	 * A component that provides for selecting a text item from a list of items. iOS: UIPickerView. Android: UISpinner. Web: Select tag.
	 * @param monkeyId the monkeyId
	 * @return the ItemSelector component
	 */
	public ItemSelector itemSelector(String monkeyId);

	/**
	 * A read-only text field. iOS: UILabel. Android: An uneditable TextView.
	 * @return the Label component
	 */
	public Label label();
	/**
	 * A read-only text field. iOS: UILabel. Android: An uneditable TextView.
	 * @param monkeyId the monkeyId
	 * @return the Label component
	 */
	public Label label(String monkeyId);

	/**
	 * A web hypertext link.
	 * @return the Link component
	 */
	public Link link();
	/**
	 * A web hypertext link.
	 * @param monkeyId the monkeyId
	 * @return the Link component
	 */
	public Link link(String monkeyId);

	/**
	 * A menu UI component. iOS: TabBar. Android: Menu
	 * @return the Menu component
	 */
	public Menu menu();
	/**
	 * A menu UI component. iOS: TabBar. Android: Menu
	 * @param monkeyId the monkeyId
	 * @return the Menu component
	 */
	public Menu menu(String monkeyId);

	/**
	 * A component that provides for selecting a number for a set of numbers.
	 * @return the NumericSelector component
	 */
	public NumericSelector numericSelector();
	/**
	 * A component that provides for selecting a number for a set of numbers.
	 * @param monkeyId the monkeyId
	 * @return the NumericSelector component
	 */
	public NumericSelector numericSelector(String monkeyId);

	/**
	 * A RadioButton group. An alias for ButtonSelector.
	 * @return the RadioButtons component
	 */
	public RadioButtons radioButtons();
	/**
	 * A RadioButton group. An alias for ButtonSelector.
	 * @param monkeyId the monkeyId
	 * @return the RadioButtons component
	 */
	public RadioButtons radioButtons(String monkeyId);

	/**
	 * A component that captures a rating (eg, with stars). iOS: Slider. Android: RatingBar.
	 * @return the RatingBar component
	 */
	public RatingBar ratingBar();
	/**
	 * A component that captures a rating (eg, with stars). iOS: Slider. Android: RatingBar.
	 * @param monkeyId the monkeyId
	 * @return the RatingBar component
	 */
	public RatingBar ratingBar(String monkeyId);

	/**
	 * A MonkeyTalk script. The monkeyId is the name of the script. If no extension is specified, then the script runner will first search for a .js file, and if one is not found, the runner will then search for an .mt file.
	 * @return the Script component
	 */
	public Script script();
	/**
	 * A MonkeyTalk script. The monkeyId is the name of the script. If no extension is specified, then the script runner will first search for a .js file, and if one is not found, the runner will then search for an .mt file.
	 * @param monkeyId the monkeyId
	 * @return the Script component
	 */
	public Script script(String monkeyId);

	/**
	 * A component that provides a scrollable view of its contents. iOS: UIScrollView. Android: ScrollView.
	 * @return the Scroller component
	 */
	public Scroller scroller();
	/**
	 * A component that provides a scrollable view of its contents. iOS: UIScrollView. Android: ScrollView.
	 * @param monkeyId the monkeyId
	 * @return the Scroller component
	 */
	public Scroller scroller(String monkeyId);

	/**
	 * A Slider control. iOS: UISlider. Android: SeekBar.
	 * @return the Slider component
	 */
	public Slider slider();
	/**
	 * A Slider control. iOS: UISlider. Android: SeekBar.
	 * @param monkeyId the monkeyId
	 * @return the Slider component
	 */
	public Slider slider(String monkeyId);

	/**
	 * A stepper control. iOS: UIStepper. Android: ignored.
	 * @return the Stepper component
	 */
	public Stepper stepper();
	/**
	 * A stepper control. iOS: UIStepper. Android: ignored.
	 * @param monkeyId the monkeyId
	 * @return the Stepper component
	 */
	public Stepper stepper(String monkeyId);

	/**
	 * A TabBar. iOS: UITabBar. Android: TabHost. On Android, tabs are selected by 'tag'.
	 * @return the TabBar component
	 */
	public TabBar tabBar();
	/**
	 * A TabBar. iOS: UITabBar. Android: TabHost. On Android, tabs are selected by 'tag'.
	 * @param monkeyId the monkeyId
	 * @return the TabBar component
	 */
	public TabBar tabBar(String monkeyId);

	/**
	 * A component that provides a tabular view of data. iOS: UITableView. Android: Table. Web: Table tag. For web tables, the section specifies the column.
	 * @return the Table component
	 */
	public Table table();
	/**
	 * A component that provides a tabular view of data. iOS: UITableView. Android: Table. Web: Table tag. For web tables, the section specifies the column.
	 * @param monkeyId the monkeyId
	 * @return the Table component
	 */
	public Table table(String monkeyId);

	/**
	 * A multi-line input field. iOS: UITextView. Android: multiline editable TextView.
	 * @return the TextArea component
	 */
	public TextArea textArea();
	/**
	 * A multi-line input field. iOS: UITextView. Android: multiline editable TextView.
	 * @param monkeyId the monkeyId
	 * @return the TextArea component
	 */
	public TextArea textArea(String monkeyId);

	/**
	 * An On/Off switch. iOS: UISwitch. Android: ToggleButton
	 * @return the Toggle component
	 */
	public Toggle toggle();
	/**
	 * An On/Off switch. iOS: UISwitch. Android: ToggleButton
	 * @param monkeyId the monkeyId
	 * @return the Toggle component
	 */
	public Toggle toggle(String monkeyId);

	/**
	 * A group of tool buttons. iOS: UIToolBar. Android: ActionBar.
	 * @return the ToolBar component
	 */
	public ToolBar toolBar();
	/**
	 * A group of tool buttons. iOS: UIToolBar. Android: ActionBar.
	 * @param monkeyId the monkeyId
	 * @return the ToolBar component
	 */
	public ToolBar toolBar(String monkeyId);

	/**
	 * A component that displays videos. iOS: MPMoviePlayerController. Android: VideoView.
	 * @return the VideoPlayer component
	 */
	public VideoPlayer videoPlayer();
	/**
	 * A component that displays videos. iOS: MPMoviePlayerController. Android: VideoView.
	 * @param monkeyId the monkeyId
	 * @return the VideoPlayer component
	 */
	public VideoPlayer videoPlayer(String monkeyId);

	/**
	 * Base class for all UI components. On iOS, monkeyId defaults to the accessibilityLabel. On Android, monkeyId defaults to contentDescription if one exists, otherwise the component's tag value if it has a string value.
	 * @return the View component
	 */
	public View view();
	/**
	 * Base class for all UI components. On iOS, monkeyId defaults to the accessibilityLabel. On Android, monkeyId defaults to contentDescription if one exists, otherwise the component's tag value if it has a string value.
	 * @param monkeyId the monkeyId
	 * @return the View component
	 */
	public View view(String monkeyId);

	/**
	 * A component that displays web pages. iOS: UIWebView. Android: WebView.
	 * @return the WebView component
	 */
	public WebView webView();
	/**
	 * A component that displays web pages. iOS: UIWebView. Android: WebView.
	 * @param monkeyId the monkeyId
	 * @return the WebView component
	 */
	public WebView webView(String monkeyId);

	/**
	 * Send a raw text MonkeyTalk command to the app under test.
	 * @return the return value (as from a Get action), or {@code null} if it doesn't exist
	 */
	public String raw();
	/**
	 * Send a raw text MonkeyTalk command to the app under test.
	 * @param command the MonkeyTalk command
	 * @return the return value (as from a Get action), or {@code null} if it doesn't exist
	 */
	public String raw(String command);
}
