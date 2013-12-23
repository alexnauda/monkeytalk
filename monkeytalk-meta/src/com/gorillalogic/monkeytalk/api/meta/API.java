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
package com.gorillalogic.monkeytalk.api.meta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Static utility class to provide various entry points to the meta MonkeyTalk API. The meta API is
 * designed to support tooling in the IDE, including command completion, command validation, user
 * help, etc.
 */
public class API {
	private static List<Component> components;
	private static List<String> componentTypes;
	private static List<String> commandNames;
	
	/**
	 * When true, ignore all API components from Adobe Flex.
	 */
	public static final boolean IGNORE_FLEX;

	static {
		components = new ArrayList<Component>();
		
		// codegen-components
IGNORE_FLEX = true;

components.add(new Component(
  "App",
  "The application under test.",
  null,
  new ArrayList<Action>(Arrays.asList(
    new Action(
      "Exec",
      "Execute a method on a native class. The method must take zero or more String arguments. The class is given in the monkeyId by its fully qualified class name.",
      new ArrayList<Arg>(Arrays.asList(
        new Arg("method","the method to call","String"),
        new Arg("args","the String args to be supplied to the method","String",true)
      )),
      "void",
      ""),
    new Action(
      "ExecAndReturn",
      "Execute a method on a native class. The method must take zero or more String arguments and return a String result. The returned value is set into the given variable name. The class is given in the monkeyId by its fully qualified class name.",
      new ArrayList<Arg>(Arrays.asList(
        new Arg("variable","the name of the variable to set","String"),
        new Arg("method","the method to call","String"),
        new Arg("args","the String args to be supplied to the method","String",true)
      )),
      "String",
      "the return value")
    ))
  ,
  null)
);
components.add(new Component(
  "Browser",
  "The browser hosting the webapp under test.",
  null,
  new ArrayList<Action>(Arrays.asList(
    new Action(
      "Open",
      "Open the given url.",
      new ArrayList<Arg>(Arrays.asList(
        new Arg("url","the url to be opened","String")
      )),
      "void",
      ""),
    new Action(
      "Back",
      "Navigate the browser back to the previous page. Ignored if this is the first page.",
      null,
      "void",
      ""),
    new Action(
      "Forward",
      "Navigate the browser forward to the next page. Ignored if this is the last page.",
      null,
      "void",
      "")
    ))
  ,
  new ArrayList<Property>(Arrays.asList(
    new Property("value", null, "the current url")
    ))
  )
);
components.add(new Component(
  "Button",
  "A Button. iOS: UIButton. Android: Button. Web: Button tag, or Input tag with type='submit' or type='reset'. If the button has a label, it is used as the monkeyId.",
  "View",
  null,
  new ArrayList<Property>(Arrays.asList(
    new Property("value", null, "the button label text")
    ))
  )
);
components.add(new Component(
  "ButtonSelector",
  "A group of radio (mutually exclusive) buttons. iOS: UISegmentedControl. Android: RadioGroup. Web: A set of Input tags with type='radio' and name='group'.",
  "ItemSelector",
  null,
  new ArrayList<Property>(Arrays.asList(
    new Property("value", null, "the selected button's label")
    ))
  )
);
components.add(new Component(
  "CheckBox",
  "A component that can be checked or unchecked. iOS: ignored. Android: CheckBox.",
  "View",
  new ArrayList<Action>(Arrays.asList(
    new Action(
      "On",
      "Check the checkbox.",
      null,
      "void",
      ""),
    new Action(
      "Off",
      "Uncheck the checkbox.",
      null,
      "void",
      "")
    ))
  ,
  new ArrayList<Property>(Arrays.asList(
    new Property("value", null, "the state of the checkbox (either 'on' or 'off')")
    ))
  )
);
components.add(new Component(
  "DatePicker",
  "A component that edits a date. iOS: UIDatePicker. Android: DatePicker.",
  "View",
  new ArrayList<Action>(Arrays.asList(
    new Action(
      "EnterDate",
      "Enter the date value.",
      new ArrayList<Arg>(Arrays.asList(
        new Arg("date","A date with the format YYYY-MM-DD where YYYY is the year, MM is the month (01-12), and DD is the day (01-31).","String")
      )),
      "void",
      ""),
    new Action(
      "EnterTime",
      "Enter the time value.",
      new ArrayList<Arg>(Arrays.asList(
        new Arg("time","A time with the format hh:mm am/pm, where hh is the hour (01-12), mm is the minute (00-59), and am/pm is the marker.","String")
      )),
      "void",
      ""),
    new Action(
      "EnterDateAndTime",
      "Enter the date and time value.",
      new ArrayList<Arg>(Arrays.asList(
        new Arg("dateAndTime","A date and time with the format YYYY-MM-DD hh:mm am/pm, where YYYY is the year, MM is the month (01-12), DD is the day (01-31), hh is the hour (01-12), mm is the minute (00-59), and am/pm is the marker.","String")
      )),
      "void",
      ""),
    new Action(
      "EnterCountDownTimer",
      "Enter the count down timer value. (iOS only)",
      new ArrayList<Arg>(Arrays.asList(
        new Arg("timer","A timer with the format hh:mm, where hh is the hour (00-23), and mm is the minute (00-59).","String")
      )),
      "void",
      "")
    ))
  ,
  new ArrayList<Property>(Arrays.asList(
    new Property("value", null, "the date (as YYYY-MM-DD), or time (as HH:MM am/pm), or date and time (as YYYY-MM-DD HH:MM am/pm)")
    ))
  )
);
components.add(new Component(
  "Debug",
  "Logging and diagnostics.",
  null,
  new ArrayList<Action>(Arrays.asList(
    new Action(
      "Vars",
      "Print all variables (and their values) that are currently in the local scope.",
      null,
      "void",
      ""),
    new Action(
      "Print",
      "Print the given message. Use this to add debugging messages to script output.",
      new ArrayList<Arg>(Arrays.asList(
        new Arg("message","the message to be printed","String")
      )),
      "void",
      "")
    ))
  ,
  null)
);
components.add(new Component(
  "Device",
  "The device hosting the application under test.",
  null,
  new ArrayList<Action>(Arrays.asList(
    new Action(
      "Shake",
      "Shake the device. iOS: works great. Android: not yet implemented.",
      null,
      "void",
      ""),
    new Action(
      "Rotate",
      "Change the device orientation.",
      new ArrayList<Arg>(Arrays.asList(
        new Arg("direction","iOS: 'left' or 'right', Android: 'portrait' or 'landscape'","String")
      )),
      "void",
      ""),
    new Action(
      "Back",
      "Navigate back. iOS: Pops the current UINavigationItem (if there is one). Android: Presses the hardware device key.",
      null,
      "void",
      ""),
    new Action(
      "Forward",
      "Navigate forward. iOS: Pushes the next UINavigationItem, if there is one. Android: ignored.",
      null,
      "void",
      ""),
    new Action(
      "Search",
      "Press the search key. iOS: ignored. Android: Presses the device search key.",
      null,
      "void",
      ""),
    new Action(
      "Menu",
      "Press the menu key. iOS: ignored. Android: Presses the device menu key.",
      null,
      "void",
      ""),
    new Action(
      "Screenshot",
      "Take a screenshot of the app under test.",
      null,
      "void",
      ""),
    new Action(
      "Get",
      "Gets the value of the given property from the component, and set it into the given variable name.",
      new ArrayList<Arg>(Arrays.asList(
        new Arg("variable","the name of the variable to set","String"),
        new Arg("propPath","the property name or path expression (defaults to 'value')","String")
      )),
      "String",
      "the value")
    ))
  ,
  new ArrayList<Property>(Arrays.asList(
    new Property("value", null, "the device OS"),
    new Property("os", null, "the device OS (ex: iOS or Android)"),
    new Property("version", null, "the device OS version (ex: 5.1.1 or 2.3.3)"),
    new Property("resolution", null, "the device screen resolution in pixels (ex: 640x960)"),
    new Property("name", null, "the device name (ex: iPhone 4S)"),
    new Property("orientation", null, "the device orientation (either 'portrait' or 'landscape')"),
    new Property("battery", null, "the percentage of the battery that is full"),
    new Property("memory", null, "percentage of memory in use"),
    new Property("cpu", null, "percentage of the cpu in use"),
    new Property("diskspace", null, "percentage of the disk in use"),
    new Property("allinfo", null, "memory, cpu, diskpace, and battery percentages returned comma separated"),
    new Property("totalMemory", null, "total ram in the phone, in bytes"),
    new Property("totalDiskSpace", null, "total space on disk, in bytes")
    ))
  )
);
components.add(new Component(
  "Doc",
  "Document the script and its named variables.",
  null,
  new ArrayList<Action>(Arrays.asList(
    new Action(
      "Vars",
      "Document the named variables used in the script.",
      new ArrayList<Arg>(Arrays.asList(
        new Arg("args","the named variables and their doc, in the form of: var='some doc' var2='other doc'","String",true)
      )),
      "void",
      ""),
    new Action(
      "Script",
      "Document the script itself.",
      new ArrayList<Arg>(Arrays.asList(
        new Arg("doc","the doc","String")
      )),
      "void",
      "")
    ))
  ,
  null)
);
components.add(new Component(
  "Globals",
  "Setup and initialize global variables.",
  null,
  new ArrayList<Action>(Arrays.asList(
    new Action(
      "Define",
      "Set global variables.",
      new ArrayList<Arg>(Arrays.asList(
        new Arg("args","the global variables, in the form of: var=val var2=val2","String",true)
      )),
      "void",
      ""),
    new Action(
      "Set",
      "Set global variables (alias for Globals.Define).",
      new ArrayList<Arg>(Arrays.asList(
        new Arg("args","the global variables, in the form of: var=val var2=val2","String",true)
      )),
      "void",
      "")
    ))
  ,
  null)
);
components.add(new Component(
  "Grid",
  "A component that provides a grid view of data. iOS: UICollectionView. Android: GridView.",
  "ItemSelector",
  null,
  new ArrayList<Property>(Arrays.asList(
    new Property("detail", "int row, int section", "the detail text of the item per row (and per section on iOS)")
    ))
  )
);
components.add(new Component(
  "Image",
  "An image. iOS: UIImage, Android: ImageView.",
  "View",
  null,
  new ArrayList<Property>(Arrays.asList(
    new Property("value", null, "the image src (if available)")
    ))
  )
);
components.add(new Component(
  "IndexedSelector",
  "A component that provides for selection of an item from a list of items. Item selection is recorded and played back with an index indicating the selected item.",
  "View",
  new ArrayList<Action>(Arrays.asList(
    new Action(
      "SelectIndex",
      "Selects an item by index.",
      new ArrayList<Arg>(Arrays.asList(
        new Arg("itemNumber","the index of the item to select.","int")
      )),
      "void",
      ""),
    new Action(
      "LongSelectIndex",
      "Long press an item by index.",
      new ArrayList<Arg>(Arrays.asList(
        new Arg("itemNumber","the index of the item to long press.","int")
      )),
      "void",
      "")
    ))
  ,
  new ArrayList<Property>(Arrays.asList(
    new Property("size", "int section", "the total number of items in the list (per section on iOS)")
    ))
  )
);
components.add(new Component(
  "Input",
  "A single-line input field. iOS: UITextField. Android: single-line editable TextView. Web: Input tag with type='text'. If the input as a hint/prompt, it is used as the monkeyId.",
  "Label",
  new ArrayList<Action>(Arrays.asList(
    new Action(
      "EnterText",
      "Enter text into the input field.",
      new ArrayList<Arg>(Arrays.asList(
        new Arg("text","the text to enter","String"),
        new Arg("hitEnter","if 'enter', hit the Enter/Return/Done/Next key after entering the text.","String")
      )),
      "void",
      ""),
    new Action(
      "Clear",
      "Clear text from the input field.",
      null,
      "void",
      "")
    ))
  ,
  new ArrayList<Property>(Arrays.asList(
    new Property("value", null, "the input field text")
    ))
  )
);
components.add(new Component(
  "ItemSelector",
  "A component that provides for selecting a text item from a list of items. iOS: UIPickerView. Android: UISpinner. Web: Select tag.",
  "IndexedSelector",
  new ArrayList<Action>(Arrays.asList(
    new Action(
      "Select",
      "Select an item by value.",
      new ArrayList<Arg>(Arrays.asList(
        new Arg("value","the value of the item to select.","String")
      )),
      "void",
      ""),
    new Action(
      "LongSelect",
      "Long select an item by value.",
      new ArrayList<Arg>(Arrays.asList(
        new Arg("value","the value of the item to select.","String")
      )),
      "void",
      "")
    ))
  ,
  new ArrayList<Property>(Arrays.asList(
    new Property("item", "int row, int section", "(array) the text of the item per row (and per section on iOS)")
    ))
  )
);
components.add(new Component(
  "Label",
  "A read-only text field. iOS: UILabel. Android: An uneditable TextView.",
  "View",
  null,
  new ArrayList<Property>(Arrays.asList(
    new Property("value", null, "the label text")
    ))
  )
);
components.add(new Component(
  "Link",
  "A web hypertext link.",
  "Label",
  null,
  null)
);
components.add(new Component(
  "Menu",
  "A menu UI component. iOS: TabBar. Android: Menu",
  "ItemSelector",
  null,
  null)
);
components.add(new Component(
  "NumericSelector",
  "A component that provides for selecting a number for a set of numbers.",
  "View",
  new ArrayList<Action>(Arrays.asList(
    new Action(
      "Select",
      "Select a numeric value",
      new ArrayList<Arg>(Arrays.asList(
        new Arg("value","the value to select","float")
      )),
      "void",
      "")
    ))
  ,
  new ArrayList<Property>(Arrays.asList(
    new Property("min", null, "the minimum value"),
    new Property("max", null, "the maximum value"),
    new Property("value", null, "the selected value")
    ))
  )
);
components.add(new Component(
  "RadioButtons",
  "A RadioButton group. An alias for ButtonSelector.",
  "ButtonSelector",
  null,
  null)
);
components.add(new Component(
  "RatingBar",
  "A component that captures a rating (eg, with stars). iOS: Slider. Android: RatingBar.",
  "NumericSelector",
  null,
  null)
);
components.add(new Component(
  "Script",
  "A MonkeyTalk script. The monkeyId is the name of the script. If no extension is specified, then the script runner will first search for a .js file, and if one is not found, the runner will then search for an .mt file.",
  null,
  new ArrayList<Action>(Arrays.asList(
    new Action(
      "Run",
      "Run the script with the given args.",
      new ArrayList<Arg>(Arrays.asList(
        new Arg("args","the arguments","String",true)
      )),
      "void",
      ""),
    new Action(
      "RunWith",
      "Data-drive the script with the given CSV data file.",
      new ArrayList<Arg>(Arrays.asList(
        new Arg("args","the arguments (where the first arg is the datafile)","String",true)
      )),
      "void",
      ""),
    new Action(
      "RunIf",
      "Run the script only if the given verify command (in the args) is true, otherwise do nothing.",
      new ArrayList<Arg>(Arrays.asList(
        new Arg("args","the arguments (a MonkeyTalk verify command)","String",true)
      )),
      "void",
      "")
    ))
  ,
  null)
);
components.add(new Component(
  "Scroller",
  "A component that provides a scrollable view of its contents. iOS: UIScrollView. Android: ScrollView.",
  "View",
  new ArrayList<Action>(Arrays.asList(
    new Action(
      "Scroll",
      "Scroll to the specified coordinates.",
      new ArrayList<Arg>(Arrays.asList(
        new Arg("x","the x-coordinate (horizontal)","int"),
        new Arg("y","the y-coordinate (vertical)","int")
      )),
      "void",
      "")
    ))
  ,
  null)
);
components.add(new Component(
  "Setup",
  "A MonkeyTalk script run before each test script in the suite.",
  null,
  new ArrayList<Action>(Arrays.asList(
    new Action(
      "Run",
      "Run the setup script with the given args.",
      new ArrayList<Arg>(Arrays.asList(
        new Arg("args","the arguments","String",true)
      )),
      "void",
      ""),
    new Action(
      "RunWith",
      "Data-drive the setup script with the given CSV data file.",
      new ArrayList<Arg>(Arrays.asList(
        new Arg("args","the arguments (where the first arg is the datafile)","String",true)
      )),
      "void",
      "")
    ))
  ,
  null)
);
components.add(new Component(
  "Slider",
  "A Slider control. iOS: UISlider. Android: SeekBar.",
  "NumericSelector",
  null,
  null)
);
components.add(new Component(
  "Stepper",
  "A stepper control. iOS: UIStepper. Android: ignored.",
  "NumericSelector",
  new ArrayList<Action>(Arrays.asList(
    new Action(
      "Increment",
      "Increase the value by the stepsize. Ignored if at max.",
      null,
      "void",
      ""),
    new Action(
      "Decrement",
      "Decrease the value by the stepsize. Ignore if at min.",
      null,
      "void",
      "")
    ))
  ,
  new ArrayList<Property>(Arrays.asList(
    new Property("stepsize", null, null)
    ))
  )
);
components.add(new Component(
  "Suite",
  "A MonkeyTalk suite. The monkeyId is the name of the suite, including the .mts extension.",
  null,
  new ArrayList<Action>(Arrays.asList(
    new Action(
      "Run",
      "Run the suite with the given args.",
      new ArrayList<Arg>(Arrays.asList(
        new Arg("args","the arguments","String",true)
      )),
      "void",
      "")
    ))
  ,
  null)
);
components.add(new Component(
  "System",
  "The system running the tests (aka the runner).",
  null,
  new ArrayList<Action>(Arrays.asList(
    new Action(
      "Exec",
      "Execute the given command on the system. The system is the computer running the tests, not to be confused with the app under test that runs in the simulator/emulator or on the device.",
      new ArrayList<Arg>(Arrays.asList(
        new Arg("command","the system command to execute","String")
      )),
      "void",
      ""),
    new Action(
      "ExecAndReturn",
      "Execute the given command on the system. The output from the command is set into the given variable name.",
      new ArrayList<Arg>(Arrays.asList(
        new Arg("variable","the name of the variable to set","String"),
        new Arg("command","the system command to execute the method to call","String")
      )),
      "String",
      "the result of running the system command")
    ))
  ,
  null)
);
components.add(new Component(
  "TabBar",
  "A TabBar. iOS: UITabBar. Android: TabHost. On Android, tabs are selected by 'tag'.",
  "ItemSelector",
  null,
  null)
);
components.add(new Component(
  "Table",
  "A component that provides a tabular view of data. iOS: UITableView. Android: Table. Web: Table tag. For web tables, the section specifies the column.",
  "ItemSelector",
  new ArrayList<Action>(Arrays.asList(
    new Action(
      "SelectRow",
      "Select a row.",
      new ArrayList<Arg>(Arrays.asList(
        new Arg("row","the row to select","int"),
        new Arg("section","the section containing the row, defaults to section #1. (Ignored on Android)","int")
      )),
      "void",
      ""),
    new Action(
      "SelectIndicator",
      "Select the indicator (the icon on the right). Android: Ignored.",
      new ArrayList<Arg>(Arrays.asList(
        new Arg("row","the row to select","int"),
        new Arg("section","the section containing the row, defaults to section #1.","int")
      )),
      "void",
      ""),
    new Action(
      "ScrollToRow",
      "Scroll to a row by row number.",
      new ArrayList<Arg>(Arrays.asList(
        new Arg("row","the row to scroll to","int"),
        new Arg("section","the section containing the row, defaults to section #1. (Ignored on Android)","int")
      )),
      "void",
      ""),
    new Action(
      "ScrollToRow",
      "Scroll to a row by value.",
      new ArrayList<Arg>(Arrays.asList(
        new Arg("value","the value of the row to scroll to.","String")
      )),
      "void",
      ""),
    new Action(
      "SetEditing",
      "Enable/disable table editing. iOS: Enabled editing mode for table. Android: ignored.",
      new ArrayList<Arg>(Arrays.asList(
        new Arg("enabled","if true, enable editing, else disable editing.","boolean")
      )),
      "void",
      ""),
    new Action(
      "Insert",
      "Insert a row into the table. iOS: Inserts a row. Android: Ignored.",
      new ArrayList<Arg>(Arrays.asList(
        new Arg("row","the index of the row after which to insert a new row.","int"),
        new Arg("section","the section containing the row, defaults to section #1.","int")
      )),
      "void",
      ""),
    new Action(
      "Remove",
      "Remove a row from the table. iOS: Deletes the row. Android: Ignored.",
      new ArrayList<Arg>(Arrays.asList(
        new Arg("row","the index of the row to be removed.","int"),
        new Arg("section","the section containing the row, defaults to section #1.","int")
      )),
      "void",
      ""),
    new Action(
      "Move",
      "Move a row. iOS: Moves a row. Android: Ignored.",
      new ArrayList<Arg>(Arrays.asList(
        new Arg("from","the index of the row to be moved.","int"),
        new Arg("to","the destination row for the move.","int")
      )),
      "void",
      "")
    ))
  ,
  new ArrayList<Property>(Arrays.asList(
    new Property("detail", "int row, int section", "the detail text of the item per row (and per section on iOS)")
    ))
  )
);
components.add(new Component(
  "Teardown",
  "A MonkeyTalk script run after each test script in the suite.",
  null,
  new ArrayList<Action>(Arrays.asList(
    new Action(
      "Run",
      "Run the teardown script with the given args.",
      new ArrayList<Arg>(Arrays.asList(
        new Arg("args","the arguments","String",true)
      )),
      "void",
      ""),
    new Action(
      "RunWith",
      "Data-drive the teardown script with the given CSV data file.",
      new ArrayList<Arg>(Arrays.asList(
        new Arg("args","the arguments (where the first arg is the datafile)","String",true)
      )),
      "void",
      "")
    ))
  ,
  null)
);
components.add(new Component(
  "Test",
  "A MonkeyTalk test script run in a suite.",
  null,
  new ArrayList<Action>(Arrays.asList(
    new Action(
      "Run",
      "Run the test with the given args.",
      new ArrayList<Arg>(Arrays.asList(
        new Arg("args","the arguments","String",true)
      )),
      "void",
      ""),
    new Action(
      "RunWith",
      "Data-drive the test with the given CSV data file.",
      new ArrayList<Arg>(Arrays.asList(
        new Arg("args","the arguments (where the first arg is the datafile)","String",true)
      )),
      "void",
      "")
    ))
  ,
  null)
);
components.add(new Component(
  "TextArea",
  "A multi-line input field. iOS: UITextView. Android: multiline editable TextView.",
  "Input",
  null,
  new ArrayList<Property>(Arrays.asList(
    new Property("value", null, "the text of the text area")
    ))
  )
);
components.add(new Component(
  "Toggle",
  "An On/Off switch. iOS: UISwitch. Android: ToggleButton",
  "View",
  new ArrayList<Action>(Arrays.asList(
    new Action(
      "On",
      "Toggle the button on.",
      null,
      "void",
      ""),
    new Action(
      "Off",
      "Toggle the button off.",
      null,
      "void",
      "")
    ))
  ,
  null)
);
components.add(new Component(
  "ToolBar",
  "A group of tool buttons. iOS: UIToolBar. Android: ActionBar.",
  "IndexedSelector",
  null,
  null)
);
components.add(new Component(
  "Vars",
  "Setup and initialize the named variables.",
  "Verifiable",
  new ArrayList<Action>(Arrays.asList(
    new Action(
      "Define",
      "Define the named variables used in the script, and an optional default value.",
      new ArrayList<Arg>(Arrays.asList(
        new Arg("args","the named variables, in the form of: var1=default var2=default","String",true)
      )),
      "void",
      "")
    ))
  ,
  null)
);
components.add(new Component(
  "Verifiable",
  "Base class for all verifiable components.",
  null,
  new ArrayList<Action>(Arrays.asList(
    new Action(
      "Verify",
      "Verifies that a property of the component has some expected value.",
      new ArrayList<Arg>(Arrays.asList(
        new Arg("expectedValue","the expected value of the property. If null, verifies the existence of the component.","String"),
        new Arg("propPath","the property name or property path expression (defaults to 'value')","String"),
        new Arg("failMessage","the custom failure message","String")
      )),
      "void",
      ""),
    new Action(
      "VerifyNot",
      "Verifies that a property of the component does not have some value.",
      new ArrayList<Arg>(Arrays.asList(
        new Arg("expectedValue","the value the component shouldn't have. If null, verifies the non-existence of the component.","String"),
        new Arg("propPath","the property name or property path expression (defaults to 'value')","String"),
        new Arg("failMessage","the custom failure message","String")
      )),
      "void",
      ""),
    new Action(
      "VerifyRegex",
      "Verifies that a property of the component matches some regular expression.",
      new ArrayList<Arg>(Arrays.asList(
        new Arg("regex","the regular expression to match","String"),
        new Arg("propPath","the property name or property path expression (defaults to 'value')","String"),
        new Arg("failMessage","the custom failure message","String")
      )),
      "void",
      ""),
    new Action(
      "VerifyNotRegex",
      "Verifies that a property of the component does not have a value matching a regular expression.",
      new ArrayList<Arg>(Arrays.asList(
        new Arg("regex","the regular expression that should not match.","String"),
        new Arg("propPath","the property name or property path expression (defaults to 'value')","String"),
        new Arg("failMessage","the custom failure message","String")
      )),
      "void",
      ""),
    new Action(
      "VerifyWildcard",
      "Verifies that a property of the component matches some wildcard expression.",
      new ArrayList<Arg>(Arrays.asList(
        new Arg("wildcard","the wildcard expression to match","String"),
        new Arg("propPath","the property name or property path expression (defaults to 'value')","String"),
        new Arg("failMessage","the custom failure message","String")
      )),
      "void",
      ""),
    new Action(
      "VerifyNotWildcard",
      "Verifies that a property of the component does not have a value matching some wildcard expression.",
      new ArrayList<Arg>(Arrays.asList(
        new Arg("wildcard","the wildcard expression that should not match","String"),
        new Arg("propPath","the property name or property path expression (defaults to 'value')","String"),
        new Arg("failMessage","the custom failure message","String")
      )),
      "void",
      ""),
    new Action(
      "VerifyImage",
      "Verifies that the screen image of a component matches the expected appearance.",
      new ArrayList<Arg>(Arrays.asList(
        new Arg("expectedImagePath","the project-relative path to an image file, which contains the expected appearance. If the file does not exist, it will be created from the current appearance of the component","String"),
        new Arg("tolerance","the 'fuzziness' to apply to the match in terms of color and sharpness, where 0=perfect match and 10=maximum tolerance (defaults to '0')","int"),
        new Arg("failMessage","the custom failure message","String")
      )),
      "void",
      ""),
    new Action(
      "WaitFor",
      "Waits for a component to be created and/or become visible.",
      new ArrayList<Arg>(Arrays.asList(
        new Arg("seconds","how many seconds to wait before giving up and failing the command (defaults to 10).","int")
      )),
      "void",
      ""),
    new Action(
      "WaitForNot",
      "Waits for a component to no longer be found, or become hidden.",
      new ArrayList<Arg>(Arrays.asList(
        new Arg("seconds","how many seconds to wait before giving up and failing the command (defaults to 10).","int")
      )),
      "void",
      "")
    ))
  ,
  new ArrayList<Property>(Arrays.asList(
    new Property("value", null, null)
    ))
  )
);
components.add(new Component(
  "VideoPlayer",
  "A component that displays videos. iOS: MPMoviePlayerController. Android: VideoView.",
  "View",
  new ArrayList<Action>(Arrays.asList(
    new Action(
      "Play",
      "Play the video from the current playback point.",
      null,
      "void",
      ""),
    new Action(
      "Pause",
      "Stop the video at the current playback point.",
      null,
      "void",
      ""),
    new Action(
      "Stop",
      "Stop the video and set playback to the starting point.",
      null,
      "void",
      "")
    ))
  ,
  null)
);
components.add(new Component(
  "View",
  "Base class for all UI components. On iOS, monkeyId defaults to the accessibilityLabel. On Android, monkeyId defaults to contentDescription if one exists, otherwise the component's tag value if it has a string value.",
  "Verifiable",
  new ArrayList<Action>(Arrays.asList(
    new Action(
      "Tap",
      "Taps on the component. On Android, plays a 'click'. On iOS, plays a TouchDown/TouchMove/TouchUp sequence.",
      null,
      "void",
      ""),
    new Action(
      "LongPress",
      "Performs a long press on the component. On Android, plays a 'longClick'. On iOS, plays a longPush gesture.",
      null,
      "void",
      ""),
    new Action(
      "TouchDown",
      "Start touching the component.",
      new ArrayList<Arg>(Arrays.asList(
        new Arg("x","x-coordinate of the touch","int"),
        new Arg("y","y-coordinate of the touch","int")
      )),
      "void",
      ""),
    new Action(
      "TouchMove",
      "Drag across the component",
      new ArrayList<Arg>(Arrays.asList(
        new Arg("coords","one or more (x,y) coordinate pairs specifying the path of the drag gesture","int",true)
      )),
      "void",
      ""),
    new Action(
      "TouchUp",
      "Stop touching the component.",
      new ArrayList<Arg>(Arrays.asList(
        new Arg("x","x-coordinate of where touch is released","int"),
        new Arg("y","y-coordinate of where touch is released","int")
      )),
      "void",
      ""),
    new Action(
      "Pinch",
      "Pinch the component.",
      new ArrayList<Arg>(Arrays.asList(
        new Arg("scale","The scale factor relative to the points of the two touches in screen coordinates","float"),
        new Arg("velocity","The velocity of the pinch in scale factor per second (read-only)","float")
      )),
      "void",
      ""),
    new Action(
      "Swipe",
      "A simple directional swipe across the component.",
      new ArrayList<Arg>(Arrays.asList(
        new Arg("direction","Left, Right, Up, or Down (case insensitive)","String")
      )),
      "void",
      ""),
    new Action(
      "Drag",
      "Touch down at the first coordinate pair, move from pair to pair for all the given coordinates, and touch up at the last coordinate pair.",
      new ArrayList<Arg>(Arrays.asList(
        new Arg("coords","one or more (x,y) coordinate pairs specifying the path of a drag gesture","int",true)
      )),
      "void",
      ""),
    new Action(
      "Get",
      "Gets the value of the given property from the component, and set it into the given variable name.",
      new ArrayList<Arg>(Arrays.asList(
        new Arg("variable","the name of the variable to set","String"),
        new Arg("propPath","the property name or path expression (defaults to 'value')","String")
      )),
      "String",
      "the value")
    ))
  ,
  new ArrayList<Property>(Arrays.asList(
    new Property("value", null, null)
    ))
  )
);
components.add(new Component(
  "WebView",
  "A component that displays web pages. iOS: UIWebView. Android: WebView.",
  "View",
  new ArrayList<Action>(Arrays.asList(
    new Action(
      "Scroll",
      "Scroll to the specified coordinates.",
      new ArrayList<Arg>(Arrays.asList(
        new Arg("x","the x-coordinate (horizontal)","int"),
        new Arg("y","the y-coordinate (vertical)","int")
      )),
      "void",
      "")
    ))
  ,
  null)
);
// codegen-end
	}

	private API() {
	}

	/**
	 * Get the list of all built-in MonkeyTalk components.
	 * 
	 * @return the list of components
	 */
	public static List<Component> getComponents() {
		return components;
	}

	/**
	 * Get the list of all MonkeyTalk componentTypes.
	 * 
	 * @return the list of componentTypes
	 */
	public static List<String> getComponentTypes() {
		if (componentTypes == null) {
			Set<String> s = new HashSet<String>();
			for (Component c : components) {
				s.add(c.getName());
			}
			componentTypes = new ArrayList<String>(s);
			Collections.sort(componentTypes);
		}
		return componentTypes;
	}

	/**
	 * Get a single built-in MonkeyTalk component by componentType. Returns null if not found.
	 * 
	 * @param componentType
	 *            the MonkeyTalk componentType
	 * @return the meta Component
	 */
	public static Component getComponent(String componentType) {
		for (Component c : components) {
			if (c.getName().equalsIgnoreCase(componentType)) {
				return c;
			}
		}
		return null;
	}

	/**
	 * Get the list of all built-in MonkeyTalk command names.
	 * 
	 * @return the list of command names
	 */
	public static List<String> getCommandNames() {
		if (commandNames == null) {
			Set<String> s = new HashSet<String>();
			for (Component c : components) {
				for (Action a : c.getActions()) {
					s.add(c.getName() + "." + a.getName());
				}
			}
			commandNames = new ArrayList<String>(s);
			Collections.sort(commandNames);
		}
		return commandNames;
	}
}