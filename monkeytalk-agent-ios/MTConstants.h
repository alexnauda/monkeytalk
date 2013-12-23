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

// Commands available in MonkeyTalk
#define MTCommandTouch @"Touch"
#define MTCommandTouchLeft @"TouchLeft"
#define MTCommandTouchRight @"TouchRight"
#define MTCommandSwitch @"Switch"
#define MTCommandSlide @"Slide"
#define MTCommandScroll @"Scroll"
#define MTCommandScrollRight @"ScrollRight"
#define MTCommandScrollLeft @"ScrollLeft"
#define MTCommandScrollUp @"ScrollUp"
#define MTCommandScrollDown @"ScrollDown"
#define MTCommandVerify @"Verify"
#define MTCommandInputText @"InputText"
#define MTCommandShake @"Shake"
#define MTCommandMove @"Move"
#define MTCommandVScroll @"VScroll"
#define MTCommandPause @"Pause"
#define MTCommandSelect @"Select"
#define MTCommandRotate @"Rotate"
#define MTCommandDelete @"Delete"
#define MTCommandEdit @"Edit"
#define MTCommandReturn @"Return"
#define MTCommandEnd @"End"
#define MTCommandClear @"Clear"
#define MTCommandGetVariable @"GetVariable"
#define MTCommandDataDrive @"DataDrive"
#define MTCommandPlayMovie @"Play"
#define MTCommandPauseMovie @"Pause"
#define MTCommandWebDriver @"WebDriver"
#define MTCommandRun @"Run"
#define MTCommandForwardMovie @"Forward"
#define MTCommandBackwardMovie @"Backward"


// New in MonkeyTalk
#define MTCommandSet @"Set"
#define MTCommandTap @"Tap"
#define MTCommandDoubleTap @"DoubleTap"
#define MTCommandEnterText @"EnterText"
#define MTCommandSelectRow @"SelectRow"
#define MTCommandScrollToRow @"ScrollToRow"
#define MTCommandEnterDate @"EnterDate"
#define MTCommandEnterDateAndTime @"EnterDateAndTime"
#define MTCommandEnterTime @"EnterTime"
#define MTCommandEnterCountDownTimer @"EnterCountDownTimer"
#define MTCommandSelectIndex @"SelectIndex"
#define MTCommandLongSelectIndex @"LongSelectIndex"
#define MTCommandGet @"Get"
#define MTCommandBack @"Back"
#define MTCommandRemove @"Remove"
#define MTCommandOn @"On"
#define MTCommandOff @"Off"
#define MTCommandSwipe @"Swipe"
#define MTCommandVerifyNot @"VerifyNot"
#define MTCommandVerifyRegex @"VerifyRegex"
#define MTCommandVerifyNotRegex @"VerifyNotRegex"
#define MTCommandVerifyWildcard @"VerifyWildcard"
#define MTCommandVerifyNotWildcard @"VerifyNotWildcard"
#define MTCommandVerifyImage @"VerifyImage"
#define MTCommandSetEditing @"SetEditing"
#define MTCommandInsert @"Insert"
#define MTCommandPinch @"Pinch"
#define MTCommandSelectIndicator @"SelectIndicator"
#define MTCommandScreenshot @"Screenshot"
#define MTCommandIncrement @"Increment"
#define MTCommandDecrement @"Decrement"
#define MTCommandOpen @"Open"
#define MTCommandForward @"Forward"
#define MTCommandExec @"Exec"
#define MTCommandExecAndReturn @"ExecAndReturn"
#define MTCommandLongPress @"LongPress"
#define MTCommandSelectPage @"SelectPage"
#define MTCommandClick @"Click" // Added for web components

// Low level commands
#define MTCommandTouchDown @"TouchDown"
#define MTCommandTouchMove @"TouchMove"
#define MTCommandTouchUp @"TouchUp"

#define MTCommandDrag @"Drag"

// Swipe args
#define MTSwipeDirectionUp @"Up"
#define MTSwipeDirectionDown @"Down"
#define MTSwipeDirectionLeft @"Left"
#define MTSwipeDirectionRight @"Right"

#define MTNotificationMonkeySuspended @"MTNotificationMonkeySuspended"
#define MTNotificationCommandPosted @"MTNotificationCommandPosted"
#define MTNotificationScriptOpened @"MTNotificationScriptOpened"
#define MTNotificationPlayingDone @"MTNotificationPlayingDone"

// Main base classes
#define MTObjCComponentsArray [NSArray arrayWithObjects: @"UIButton",@"UITextField",@"UILabel",@"UITextView",@"UITableView",@"UIPickerView",@"UIDatePicker",@"UISegmentedControl",@"UISlider",@"UIScrollView",@"UITabBar",@"UISwitch",@"UIToolbar",nil]